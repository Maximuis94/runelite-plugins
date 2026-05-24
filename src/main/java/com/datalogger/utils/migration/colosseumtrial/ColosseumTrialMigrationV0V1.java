/*
 * Copyright (c) 2026, maximuis94 <https://github.com/maximuis94>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.datalogger.utils.migration.colosseumtrial;

import com.datalogger.DataLoggerConfig;
import com.datalogger.constants.Colosseum;
import static com.datalogger.constants.Colosseum.Item.DIZANAS_QUIVER_REWARD;
import static com.datalogger.constants.Colosseum.Item.DIZANAS_QUIVER_SWAPPED_REWARD;
import static com.datalogger.constants.PluginConstants.COLOSSEUM_ROOT_DIR;
import static com.datalogger.constants.PluginConstants.INTERNAL_COLOSSEUM_TRIAL_HISTORY;
import com.datalogger.models.itemvault.ItemBundle;
import com.datalogger.models.supplytracker.ValuedItemStack;
import com.datalogger.utils.migration.DataMigration;
import com.datalogger.utils.migration.colosseumtrial.models.ColosseumAttemptDtoV0;
import com.datalogger.utils.migration.colosseumtrial.models.ColosseumAttemptDtoV1;
import com.datalogger.utils.migration.colosseumtrial.models.ColosseumWaveDtoV0;
import com.datalogger.utils.migration.colosseumtrial.models.ColosseumWaveDtoV1;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;

@Slf4j
public class ColosseumTrialMigrationV0V1 implements DataMigration {
	private final ItemManager itemManager;
	private final Gson gson;
	private final ClientThread clientThread;
	private final DataLoggerConfig config;

	@Getter
	private File outFile = null;

	//	private File jsonlFile = new File(COLOSSEUM_ROOT_DIR, "migration-test.jsonl");
	private File jsonlFile = INTERNAL_COLOSSEUM_TRIAL_HISTORY;

	@Inject
	public ColosseumTrialMigrationV0V1(ItemManager itemManager, Gson gson, ClientThread clientThread, DataLoggerConfig config) {
		this.itemManager = itemManager;
		this.gson = gson;
		this.clientThread = clientThread;
		this.config = config;
	}

	@Override
	public int getStartVersion() { return 0; }

	@Override
	public int getEndVersion() { return 1; }

	@Override
	public boolean identify(File file) {
		if (file == null || !file.isFile()) {
			return false;
		}
		return file.getName().endsWith("_wave-log.json");
	}

	/**
	 * Parse the legacy file and return it as a legacy class instance
	 */
	private ColosseumAttemptDtoV0 parseInputFile(File inputFile)
	{
		if (inputFile == null || !inputFile.exists())
		{
			log.warn("Attempted to parse a null or non-existent file.");
			return null;
		}

		try (FileReader reader = new FileReader(inputFile))
		{
			return gson.fromJson(reader, ColosseumAttemptDtoV0.class);

		} catch (Exception e) {
			log.error("Failed to parse legacy V0 input file: {}", inputFile.getName(), e);
			return null;
		}
	}

	/**
	 * Convert the given trialLog to the newer version
	 */
	public ColosseumAttemptDtoV1 convertTrialLog(File inputDirectory)
	{
		File inputFile = new File(inputDirectory, inputDirectory.getName() + "_wave-log.json");

		if (!inputFile.exists()) return null;

		ColosseumAttemptDtoV0 trialV0 = parseInputFile(inputFile);

		if (trialV0 == null) {
			return null;
		}

		Map<String, ValuedItemStack> totalRewardsMap = new HashMap<>();
		int totalRewardsValue = 0;

		// 1. Extract attemptId, account, and ensure valid timestamp
		String dirName = inputDirectory.getName();
		String accountFallback = "Unknown";
		long timestamp = trialV0.getTimestamp();

		// Ensure we don't OutOfBounds if the directory name doesn't match the convention
		if (dirName.length() >= 14) {
			String dateString = dirName.substring(dirName.length() - 13);
			accountFallback = dirName.substring(0, dirName.length() - 14);

			// Fallback in case legacy timestamp was 0
			if (timestamp == 0) {
				try {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd_HHmmss");
					LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
					timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
				} catch (Exception e) {
					log.warn("Failed to parse date string for timestamp fallback: {}", dateString, e);
				}
			}
		}

		String tagFallback = "";
		double totalTimeCalculated = 0.0;
		List<ColosseumWaveDtoV1> newWaves = new ArrayList<>();

		// Use a LinkedHashMap to preserve insertion order while safely overwriting upgraded modifier tiers
		Map<String, String> activeModsMap = new LinkedHashMap<>();

		if (trialV0.getWaves() != null && !trialV0.getWaves().isEmpty()) {
			for (ColosseumWaveDtoV0 waveV0 : trialV0.getWaves()) {
				// Grab the tag from the first wave that has one
				if (waveV0.getTag() != null && tagFallback.isEmpty()) {
					tagFallback = waveV0.getTag();
				}

				// Register the modifier chosen THIS wave, overwriting lower tiers if they share a base name
				String chosen = waveV0.getChosenModifier();
				if (chosen != null && !chosen.isEmpty()) {
					String baseMod = chosen.contains("_") ? chosen.substring(0, chosen.lastIndexOf('_')) : chosen;
					activeModsMap.put(baseMod, chosen);
				}
				ItemBundle waveLoot = (waveV0.getEarnedLoot() != null && !waveV0.getEarnedLoot().isEmpty())
					? waveV0.getEarnedLoot().get(0) : null;
				if (waveV0.getEarnedLoot() != null) {
					for (ItemBundle bundle : waveV0.getEarnedLoot()) {
						String name = bundle.getItemName();
						int qty = bundle.getQuantity();
						int val = qty * itemManager.getItemPrice(bundle.getItemId());

						ValuedItemStack existing = totalRewardsMap.getOrDefault(name, new ValuedItemStack(0, 0));
						totalRewardsMap.put(name, new ValuedItemStack(existing.getCount() + qty, existing.getTotalValueInGp() + val));
						totalRewardsValue += val;
					}
				}

				List<String> activeModsList = new ArrayList<>(activeModsMap.values());

				// Provide 0 as default for manual directory conversion
				ColosseumWaveDtoV1 waveV1 = convertWave(waveV0, activeModsList, waveLoot, 0);
				newWaves.add(waveV1);
			}

			ColosseumWaveDtoV0 lastWave = trialV0.getWaves().get(trialV0.getWaves().size() - 1);
			totalTimeCalculated = lastWave.getTotalTimeTaken();
		}

		return ColosseumAttemptDtoV1.builder()
			.attemptId(dirName)
			.timestamp(timestamp)
			.accountName(accountFallback)
			.result(trialV0.getResult() != null ? trialV0.getResult() : "UNKNOWN")
			.rewardsValue(0) 			// Default for historical records
			.rewards(new HashMap<>())	// Default for historical records
			.consumedSupplyValue(0)		// Default for historical records
			.consumedSupplies(null) 	// Default for historical records
			.totalGlory(trialV0.getTotalGlory())
			.totalTime(formatTime(totalTimeCalculated))
			.tag(tagFallback)
			.activeModifiers(new ArrayList<>(activeModsMap.values()))
			.waves(newWaves)
			.build();
	}

	/**
	 * Convert the individual legacy wave to the newer version
	 */
	private ColosseumWaveDtoV1 convertWave(ColosseumWaveDtoV0 waveV0, List<String> activeModifiers, ItemBundle v1Loot, int waveLootValue)
	{
		return ColosseumWaveDtoV1.builder()
			.wave(waveV0.getWave())
			.status(waveV0.getStatus())
			.accountName(waveV0.getAccountName())
			.tag(waveV0.getTag())
			.earnedLoot(v1Loot)
			.lootValue(waveLootValue) // Populated correctly!
			.modifierChoices(waveV0.getModifierChoices() != null ? new ArrayList<>(waveV0.getModifierChoices()) : new ArrayList<>())
			.chosenModifier(waveV0.getChosenModifier())
			.activeModifiers(activeModifiers != null ? activeModifiers : Collections.emptyList())
			.timeTaken(formatTime(waveV0.getTimeTaken()))
			.speedBonus(waveV0.getSpeedBonus())
			.damageTaken(waveV0.getDamageTaken())
			.damageBonus(waveV0.getDamageBonus())
			.modifierGlory(waveV0.getModifierGlory())
			.completionBonus(waveV0.getCompletionBonus())
			.waveGlory(waveV0.getWaveGlory())
			.totalGlory(waveV0.getTotalGlory())
			.totalTimeTaken(formatTime(waveV0.getTotalTimeTaken()))
			.serpentShamanSpawnX(waveV0.getSerpentShamanSpawnX())
			.serpentShamanSpawnY(waveV0.getSerpentShamanSpawnY())
			.javelinColossusSpawnAX(waveV0.getJavelinColossusSpawnAX())
			.javelinColossusSpawnAY(waveV0.getJavelinColossusSpawnAY())
			.javelinColossusSpawnBX(waveV0.getJavelinColossusSpawnBX())
			.javelinColossusSpawnBY(waveV0.getJavelinColossusSpawnBY())
			.manticoreSpawnAX(waveV0.getManticoreSpawnAX())
			.manticoreSpawnAY(waveV0.getManticoreSpawnAY())
			.manticoreSequenceA(waveV0.getManticoreSequenceA())
			.manticoreSpawnBX(waveV0.getManticoreSpawnBX())
			.manticoreSpawnBY(waveV0.getManticoreSpawnBY())
			.manticoreSequenceB(waveV0.getManticoreSequenceB())
			.shockwaveColossusSpawnAX(waveV0.getShockwaveColossusSpawnAX())
			.shockwaveColossusSpawnAY(waveV0.getShockwaveColossusSpawnAY())
			.shockwaveColossusSpawnBX(waveV0.getShockwaveColossusSpawnBX())
			.shockwaveColossusSpawnBY(waveV0.getShockwaveColossusSpawnBY())
			.jaguarWarriorReinforcementsSpawnX(waveV0.getJaguarWarriorReinforcementsSpawnX())
			.jaguarWarriorReinforcementsSpawnY(waveV0.getJaguarWarriorReinforcementsSpawnY())
			.serpentShamanReinforcementsSpawnX(waveV0.getSerpentShamanReinforcementsSpawnX())
			.serpentShamanReinforcementsSpawnY(waveV0.getSerpentShamanReinforcementsSpawnY())
			.minotaurReinforcementsSpawnX(waveV0.getMinotaurReinforcementsSpawnX())
			.minotaurReinforcementsSpawnY(waveV0.getMinotaurReinforcementsSpawnY())
			.build();
	}

	@Override
	public boolean migrate(File file) {
		log.info("Migrating Colosseum wave-log to V1: {}", file.getName());

		try {
			ColosseumAttemptDtoV1 v1Data = performMigration(file);
			if (v1Data == null) {
				log.warn("Failed to parse or convert data in file: {}", file.getName());
				return false;
			}

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(jsonlFile, true))) {
				Gson jsonlGson = this.gson.newBuilder().disableHtmlEscaping().create();
				writer.write(jsonlGson.toJson(v1Data));
				writer.newLine();
			}

			log.debug("Successfully migrated {}", file.getName());
			return true;

		} catch (Exception e) {
			log.error("Exception occurred while migrating file: {}", file.getName(), e);
			return false;
		}
	}

	private double formatTime(double time) {
		return Math.round(time * 10.0) / 10.0;
	}

	public ColosseumAttemptDtoV1 performMigration(File file) throws Exception {
		Gson legacyGson = this.gson.newBuilder()
			.registerTypeAdapter(new TypeToken<List<ItemBundle>>(){}.getType(),
				(JsonDeserializer<List<ItemBundle>>) (json, typeOfT, context) -> {
					List<ItemBundle> list = new ArrayList<>();
					if (json.isJsonArray()) {
						for (JsonElement element : json.getAsJsonArray()) {
							list.add(context.deserialize(element, ItemBundle.class));
						}
					} else if (json.isJsonObject()) {
						list.add(context.deserialize(json, ItemBundle.class));
					}
					return list;
				})
			.create();

		ColosseumAttemptDtoV0 v0;
		try (FileReader reader = new FileReader(file)) {
			v0 = legacyGson.fromJson(reader, ColosseumAttemptDtoV0.class);
		}

		Set<Integer> itemIds = v0.getWaves().stream()
			.filter(w -> w.getEarnedLoot() != null)
			.flatMap(w -> w.getEarnedLoot().stream())
			.map(ItemBundle::getItemId)
			.collect(Collectors.toSet());

		Map<Integer, Integer> priceCache = new HashMap<>();
		CompletableFuture<Void> priceFuture = new CompletableFuture<>();

		clientThread.invoke(() -> {
			for (Integer id : itemIds) {
				priceCache.put(id, itemManager.getItemPrice(id));
			}
			priceFuture.complete(null);
		});
		priceFuture.join();

		return processMigration(v0, priceCache);
	}

	private ColosseumAttemptDtoV1 processMigration(ColosseumAttemptDtoV0 v0, Map<Integer, Integer> priceCache) {
		Map<String, ValuedItemStack> totalRewardsMap = new LinkedHashMap<>();
		int totalRewardsValue = 0;
		List<ColosseumWaveDtoV1> newWaves = new ArrayList<>();

		Map<String, String> activeModsMap = new LinkedHashMap<>();

		if (v0.getWaves() != null) {
			for (ColosseumWaveDtoV0 waveV0 : v0.getWaves()) {

				int waveLootValue = 0; // The missing variable!
				ItemBundle singleLoot = null;

				if (waveV0.getEarnedLoot() != null && !waveV0.getEarnedLoot().isEmpty()) {
					if (waveV0.getWave() < 12) {
						singleLoot = waveV0.getEarnedLoot().get(0);
						aggregateReward(totalRewardsMap, singleLoot, priceCache);
						waveLootValue = priceCache.getOrDefault(singleLoot.getItemId(), 0) * singleLoot.getQuantity();
					} else {
						singleLoot = waveV0.getEarnedLoot().get(1);
						ItemBundle bundle;
						if (waveV0.getWave() == 12) {
							if (config.logQuiverAsSplinters())
								bundle = DIZANAS_QUIVER_SWAPPED_REWARD;
							else
								bundle = DIZANAS_QUIVER_REWARD;
							aggregateReward(totalRewardsMap, bundle, priceCache);
						}
						aggregateReward(totalRewardsMap, singleLoot, priceCache);
						waveLootValue = priceCache.getOrDefault(singleLoot.getItemId(), 0) * singleLoot.getQuantity();
					}
				}

				totalRewardsValue = totalRewardsMap.values().stream()
					.mapToInt(ValuedItemStack::getTotalValueInGp)
					.sum();

				String chosen = waveV0.getChosenModifier();
				if (chosen != null && !chosen.isEmpty()) {
					String baseMod = chosen.contains("_") ? chosen.substring(0, chosen.lastIndexOf('_')) : chosen;
					activeModsMap.put(baseMod, chosen);
				}

				// Pass it into convertWave
				newWaves.add(convertWave(waveV0, new ArrayList<>(activeModsMap.values()), singleLoot, waveLootValue));
			}

			String account = v0.getWaves().isEmpty() ? "Unknown" : v0.getWaves().get(0).getAccountName();

			String formattedTimestamp = Instant.ofEpochMilli(v0.getTimestamp())
				.atZone(ZoneId.systemDefault())
				.format(Colosseum.COLOSSEUM_TRIAL_TIMESTAMP_FORMATTER);

			String attemptId = account + "_" + formattedTimestamp;

			return ColosseumAttemptDtoV1.builder()
				.attemptId(attemptId)
				.timestamp(v0.getTimestamp())
				.accountName(v0.getWaves().isEmpty() ? "Unknown" : v0.getWaves().get(0).getAccountName())
				.result(v0.getResult())
				.rewardsValue(totalRewardsValue)
				.rewards(totalRewardsMap)
				.totalGlory(v0.getTotalGlory())
				.totalTime(v0.getWaves().isEmpty() ? 0.0 : formatTime(v0.getWaves().get(v0.getWaves().size() - 1).getTotalTimeTaken()))
				.activeModifiers(new ArrayList<>(activeModsMap.values()))
				.waves(newWaves)
				.build();
		}

		return null;
	}

	private void aggregateReward(Map<String, ValuedItemStack> map, ItemBundle bundle, Map<Integer, Integer> priceCache) {
		String name = bundle.getItemName();
		int qty = bundle.getQuantity();

		int price = priceCache.getOrDefault(bundle.getItemId(), 0);
		int totalValue = price * qty;

		ValuedItemStack existing = map.getOrDefault(name, new ValuedItemStack(0, 0));
		map.put(name, new ValuedItemStack(
			existing.getCount() + qty,
			existing.getTotalValueInGp() + totalValue
		));
	}
}