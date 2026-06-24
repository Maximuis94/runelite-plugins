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
import static com.datalogger.constants.PluginConstants.DEFAULT_GAMEMODE_DTO;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;

@Slf4j
public class ColosseumTrialMigrationV0V1 implements DataMigration {

	private static final DateTimeFormatter FALLBACK_DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd_HHmmss");

	private final ItemManager itemManager;
	private final Gson standardGson;
	private final ClientThread clientThread;
	private final DataLoggerConfig config;

	@Getter
	private final File outFile = null;
	private final File jsonlFile = INTERNAL_COLOSSEUM_TRIAL_HISTORY;

	@Inject
	public ColosseumTrialMigrationV0V1(ItemManager itemManager, Gson gson, ClientThread clientThread, DataLoggerConfig config) {
		this.itemManager = itemManager;
		this.standardGson = gson;
		this.clientThread = clientThread;
		this.config = config;
	}

	@Override
	public int getStartVersion() { return 0; }

	@Override
	public int getEndVersion() { return 1; }

	@Override
	public boolean identify(File file) {
		return file != null && file.isFile() && file.getName().endsWith("_wave-log.json");
	}

	@Override
	public boolean migrate(File file) {
		log.debug("Migrating Colosseum wave-log to V1: {}", file.getName());
		try {
			ColosseumAttemptDtoV0 v0Data = parseLenientV0File(file);
			if (v0Data == null) return false;

			ColosseumAttemptDtoV1 v1Data = transformToV1(v0Data, file.getName().replace("_wave-log.json", ""));

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(jsonlFile, true))) {
				Gson noHtmlGson = this.standardGson.newBuilder().disableHtmlEscaping().create();
				writer.write(noHtmlGson.toJson(v1Data));
				writer.newLine();
			}

			log.debug("Successfully migrated {}", file.getName());
			return true;
		} catch (Exception e) {
			log.error("Failed to migrate file: {}", file.getName(), e);
			return false;
		}
	}

	public ColosseumAttemptDtoV1 convertTrialLog(File inputDirectory) {
		File inputFile = new File(inputDirectory, inputDirectory.getName() + "_wave-log.json");
		ColosseumAttemptDtoV0 v0Data = parseLenientV0File(inputFile);
		return v0Data != null ? transformToV1(v0Data, inputDirectory.getName()) : null;
	}

	private ColosseumAttemptDtoV1 transformToV1(ColosseumAttemptDtoV0 v0, String sourceName) {
		long timestamp = resolveTimestamp(v0.getTimestamp(), sourceName);
		String accountName = resolveAccountName(v0, sourceName);
		String attemptId = resolveAttemptId(accountName, timestamp, sourceName);
		String globalTag = v0.getTag() != null ? v0.getTag() : "";

		Map<Integer, Integer> priceMap = getBatchPrices(v0);

		Map<String, ValuedItemStack> totalRewards = new LinkedHashMap<>();
		Map<String, String> activeModifiers = new LinkedHashMap<>();
		List<ColosseumWaveDtoV1> v1Waves = new ArrayList<>();

		if (v0.getWaves() != null) {
			for (ColosseumWaveDtoV0 w0 : v0.getWaves()) {
				String waveTag = w0.getTag() != null ? w0.getTag() : globalTag;

				String chosenMod = w0.getChosenModifier();
				if (chosenMod != null && !chosenMod.isEmpty()) {
					String baseModName = chosenMod.contains("_") ? chosenMod.substring(0, chosenMod.lastIndexOf('_')) : chosenMod;
					activeModifiers.put(baseModName, chosenMod);
				}

				ItemBundle rngLootToLog = null;
				int waveRngGp = 0;

				List<ItemBundle> lootList = w0.getEarnedLoot();
				if (lootList != null && !lootList.isEmpty()) {
					if (w0.getWave() == 12) {
						rngLootToLog = lootList.size() >= 2 ? lootList.get(1) : lootList.get(0);

						// RULE: Inject Quiver into the Attempt's top-level rewards ledger
						ItemBundle quiver = config.logQuiverAsSplinters() ? DIZANAS_QUIVER_SWAPPED_REWARD : DIZANAS_QUIVER_REWARD;
						recordReward(totalRewards, quiver, priceMap);
					} else {
						rngLootToLog = lootList.get(0);
					}

					// Record the RNG item into the top-level rewards ledger
					recordReward(totalRewards, rngLootToLog, priceMap);

					// RULE: Wave GP value is strictly the RNG drop's GP value
					waveRngGp = rngLootToLog.getQuantity() * priceMap.getOrDefault(rngLootToLog.getItemId(), 0);
				}

				v1Waves.add(convertWave(w0, new ArrayList<>(activeModifiers.values()), rngLootToLog, waveRngGp, waveTag));
			}
		}

		int grandTotalGp = totalRewards.values().stream().mapToInt(ValuedItemStack::getTotalValueInGp).sum();
		double totalDuration = v1Waves.isEmpty() ? 0.0 : v1Waves.get(v1Waves.size() - 1).getTotalTimeTaken();

		return ColosseumAttemptDtoV1.builder()
			.attemptId(attemptId)
			.timestamp(timestamp)
			.accountName(accountName)
			.tag(globalTag)
			.gameMode(DEFAULT_GAMEMODE_DTO)
			.result(v0.getResult() != null ? v0.getResult() : "UNKNOWN")
			.rewardsValue(grandTotalGp)
			.rewards(totalRewards)
			.consumedSupplyValue(0)
			.consumedSupplies(null)
			.totalGlory(v0.getTotalGlory())
			.totalTime(formatTime(totalDuration))
			.activeModifiers(new ArrayList<>(activeModifiers.values()))
			.waves(v1Waves)
			.build();
	}

	private ColosseumWaveDtoV1 convertWave(ColosseumWaveDtoV0 w0, List<String> currentMods, ItemBundle loot, int lootGp, String tag) {
		boolean canHaveManticore = w0.getWave() >= 4;

		return ColosseumWaveDtoV1.builder()
			.wave(w0.getWave())
			.status(w0.getStatus())
			.accountName(w0.getAccountName())
			.tag(tag)
			.gameMode(DEFAULT_GAMEMODE_DTO)
			.earnedLoot(loot)
			.lootValue(lootGp)
			.modifierChoices(w0.getModifierChoices() != null ? new ArrayList<>(w0.getModifierChoices()) : new ArrayList<>())
			.chosenModifier(w0.getChosenModifier())
			.activeModifiers(currentMods)
			.timeTaken(formatTime(w0.getTimeTaken()))
			.speedBonus(w0.getSpeedBonus())
			.damageTaken(w0.getDamageTaken())
			.damageBonus(w0.getDamageBonus())
			.modifierGlory(w0.getModifierGlory())
			.completionBonus(w0.getCompletionBonus())
			.waveGlory(w0.getWaveGlory())
			.totalGlory(w0.getTotalGlory())
			.totalTimeTaken(formatTime(w0.getTotalTimeTaken()))
			.serpentShamanSpawnX(w0.getSerpentShamanSpawnX())
			.serpentShamanSpawnY(w0.getSerpentShamanSpawnY())
			.javelinColossusSpawnAX(w0.getJavelinColossusSpawnAX())
			.javelinColossusSpawnAY(w0.getJavelinColossusSpawnAY())
			.javelinColossusSpawnBX(w0.getJavelinColossusSpawnBX())
			.javelinColossusSpawnBY(w0.getJavelinColossusSpawnBY())
			.shockwaveColossusSpawnAX(w0.getShockwaveColossusSpawnAX())
			.shockwaveColossusSpawnAY(w0.getShockwaveColossusSpawnAY())
			.shockwaveColossusSpawnBX(w0.getShockwaveColossusSpawnBX())
			.shockwaveColossusSpawnBY(w0.getShockwaveColossusSpawnBY())
			.jaguarWarriorReinforcementsSpawnX(w0.getJaguarWarriorReinforcementsSpawnX())
			.jaguarWarriorReinforcementsSpawnY(w0.getJaguarWarriorReinforcementsSpawnY())
			.serpentShamanReinforcementsSpawnX(w0.getSerpentShamanReinforcementsSpawnX())
			.serpentShamanReinforcementsSpawnY(w0.getSerpentShamanReinforcementsSpawnY())
			.minotaurReinforcementsSpawnX(w0.getMinotaurReinforcementsSpawnX())
			.minotaurReinforcementsSpawnY(w0.getMinotaurReinforcementsSpawnY())
			.manticoreSpawnAX(canHaveManticore ? w0.getManticoreSpawnAX() : null)
			.manticoreSpawnAY(canHaveManticore ? w0.getManticoreSpawnAY() : null)
			.manticoreSequenceA(canHaveManticore ? w0.getManticoreSequenceA() : null)
			.manticoreSpawnBX(canHaveManticore ? w0.getManticoreSpawnBX() : null)
			.manticoreSpawnBY(canHaveManticore ? w0.getManticoreSpawnBY() : null)
			.manticoreSequenceB(canHaveManticore ? w0.getManticoreSequenceB() : null)
			.build();
	}

	private void recordReward(Map<String, ValuedItemStack> ledger, ItemBundle bundle, Map<Integer, Integer> prices) {
		if (bundle == null) return;
		int addGp = prices.getOrDefault(bundle.getItemId(), 0) * bundle.getQuantity();

		ValuedItemStack existing = ledger.getOrDefault(bundle.getItemName(), new ValuedItemStack(0, 0));
		ledger.put(bundle.getItemName(), new ValuedItemStack(
			existing.getCount() + bundle.getQuantity(),
			existing.getTotalValueInGp() + addGp
		));
	}

	private Map<Integer, Integer> getBatchPrices(ColosseumAttemptDtoV0 v0) {
		Set<Integer> uniqueIds = new HashSet<>();
		uniqueIds.add(DIZANAS_QUIVER_REWARD.getItemId());
		uniqueIds.add(DIZANAS_QUIVER_SWAPPED_REWARD.getItemId());

		if (v0.getWaves() != null) {
			v0.getWaves().stream()
				.filter(w -> w.getEarnedLoot() != null)
				.flatMap(w -> w.getEarnedLoot().stream())
				.forEach(b -> uniqueIds.add(b.getItemId()));
		}

		Map<Integer, Integer> prices = new HashMap<>();
		CompletableFuture<Void> sync = new CompletableFuture<>();
		clientThread.invoke(() -> {
			uniqueIds.forEach(id -> prices.put(id, itemManager.getItemPrice(id)));
			sync.complete(null);
		});
		sync.join();
		return prices;
	}

	private ColosseumAttemptDtoV0 parseLenientV0File(File file) {
		if (file == null || !file.exists()) return null;

		Gson lenientGson = this.standardGson.newBuilder()
			.registerTypeAdapter(new TypeToken<List<ItemBundle>>(){}.getType(),
				(JsonDeserializer<List<ItemBundle>>) (json, type, ctx) -> {
					List<ItemBundle> list = new ArrayList<>();
					if (json.isJsonArray()) {
						json.getAsJsonArray().forEach(el -> list.add(ctx.deserialize(el, ItemBundle.class)));
					} else if (json.isJsonObject()) {
						list.add(ctx.deserialize(json, ItemBundle.class));
					}
					return list;
				}).create();

		try (FileReader reader = new FileReader(file)) {
			return lenientGson.fromJson(reader, ColosseumAttemptDtoV0.class);
		} catch (Exception e) {
			log.error("Lenient Gson failed to read legacy file: {}", file.getName(), e);
			return null;
		}
	}

	private String resolveAccountName(ColosseumAttemptDtoV0 v0, String folderName) {
		if (v0.getWaves() != null && !v0.getWaves().isEmpty() && v0.getWaves().get(0).getAccountName() != null) {
			return v0.getWaves().get(0).getAccountName();
		}
		return folderName.length() >= 14 ? folderName.substring(0, folderName.length() - 14) : "Unknown";
	}

	private long resolveTimestamp(long v0Timestamp, String folderName) {
		if (v0Timestamp > 0) return v0Timestamp;
		if (folderName.length() >= 14) {
			try {
				String dateStr = folderName.substring(folderName.length() - 13);
				return LocalDateTime.parse(dateStr, FALLBACK_DATE_FORMAT)
					.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
			} catch (Exception ignored) {}
		}
		return System.currentTimeMillis();
	}

	private String resolveAttemptId(String account, long timestamp, String sourceName) {
		if (sourceName.contains("_") && sourceName.length() >= 14) return sourceName;
		String dateFormatted = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(Colosseum.COLOSSEUM_TRIAL_TIMESTAMP_FORMATTER);
		return account + "_" + dateFormatted;
	}

	private double formatTime(double val) { return Math.round(val * 10.0) / 10.0; }
}