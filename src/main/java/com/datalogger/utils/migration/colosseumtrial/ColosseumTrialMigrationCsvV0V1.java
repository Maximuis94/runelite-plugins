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
import static com.datalogger.constants.Colosseum.Item.DIZANAS_QUIVER_UNCHARGED_ID;
import static com.datalogger.constants.Colosseum.Item.SUNFIRE_SPLINTERS_ID;
import static com.datalogger.constants.PluginConstants.DEFAULT_GAMEMODE_DTO;
import static com.datalogger.constants.PluginConstants.INTERNAL_COLOSSEUM_TRIAL_HISTORY;
import com.datalogger.models.colosseum.ManticoreAttackSequence;
import com.datalogger.models.itemvault.ItemBundle;
import com.datalogger.models.supplytracker.ValuedItemStack;
import com.datalogger.utils.migration.DataMigration;
import com.datalogger.utils.migration.colosseumtrial.models.ColosseumAttemptDtoV1;
import com.datalogger.utils.migration.colosseumtrial.models.ColosseumWaveDtoV1;
import com.google.gson.Gson;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;

/**
 * Migrates CSV trial data from 1.1.0 to 1.2.0
 */
@Slf4j
@Singleton
public class ColosseumTrialMigrationCsvV0V1 implements DataMigration {
	private final ItemManager itemManager;
	private final Gson gson;
	private final ClientThread clientThread;
	private final DataLoggerConfig config;

	@Getter
	private final File outFile = null;

	private final File jsonlFile = INTERNAL_COLOSSEUM_TRIAL_HISTORY;

	@Inject
	public ColosseumTrialMigrationCsvV0V1(ItemManager itemManager, Gson gson, ClientThread clientThread, DataLoggerConfig config) {
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
		return file.getName().endsWith("_wave-log.csv");
	}

	@Override
	public boolean migrate(File file) {
		log.debug("Starting Colosseum CSV Migration from V0 to V1 for {}", file.getName());

		try {
			List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
			if (lines.isEmpty()) {
				log.warn("CSV file is empty: {}", file.getName());
				return false;
			}

			String[] oldHeaders = lines.get(0).split(",", -1);
			Map<String, Integer> headerMap = new HashMap<>();
			for (int i = 0; i < oldHeaders.length; i++) {
				headerMap.put(oldHeaders[i].trim(), i);
			}

			if (Arrays.asList(oldHeaders).contains("activeModifiers")) {
				log.debug("CSV already contains activeModifiers. Skipping.");
				return false;
			}
			Map<String, ValuedItemStack> totalRewardsMap = new HashMap<>();
			int totalRewardsValue = 0;

			List<ColosseumWaveDtoV1> waveDtos = new ArrayList<>();
			Map<String, String> activeModsMap = new LinkedHashMap<>();
			String result = "";
			String tag = "";
			for (int i = 1; i < lines.size(); i++) {
				String[] cols = lines.get(i).split(",", -1);
				if (cols.length < oldHeaders.length) continue;

				Function<String, String> getCol = (name) -> {
					Integer idx = headerMap.get(name);
					return (idx != null && idx < cols.length) ? cols[idx] : null;
				};

				String[] ids = getCol.apply("itemIds").split("\\|");
				String[] names = getCol.apply("itemNames").split("\\|");
				String[] qtys = getCol.apply("quantities").split("\\|");

				ItemBundle earnedLoot = (ids.length > 0 && !ids[ids.length-1].isEmpty())
					? new ItemBundle(Integer.parseInt(ids[ids.length-1]), names[ids.length-1], Integer.parseInt(qtys[ids.length-1]))
					: null;

				String chosen = getCol.apply("chosenModifier");
				if (chosen != null && !chosen.isEmpty()) {
					String baseMod = chosen.contains("_") ? chosen.substring(0, chosen.lastIndexOf('_')) : chosen;
					activeModsMap.put(baseMod, chosen);
				}

				Function<String, Integer> getIntCol = (colName) -> {
					String val = getCol.apply(colName);
					if (val == null || val.isEmpty() || val.equals("-1")) {
						return null;
					}
					try {
						return Integer.parseInt(val);
					} catch (NumberFormatException e) {
						return null;
					}
				};
				tag = getCol.apply("tag");
				ColosseumWaveDtoV1 waveDto = ColosseumWaveDtoV1.builder()
					.wave(Integer.parseInt(getCol.apply("wave")))
					.status(getCol.apply("status"))
					.accountName(getCol.apply("accountName"))
					.tag(tag)
					.gameMode(DEFAULT_GAMEMODE_DTO)
					.earnedLoot(earnedLoot)
					.chosenModifier(chosen)
					.completionBonus(Integer.parseInt(getCol.apply("completionBonus")))
					.speedBonus(Integer.parseInt(getCol.apply("speedBonus")))
					.damageBonus(Integer.parseInt(getCol.apply("damageBonus")))
					.damageTaken(Integer.parseInt(getCol.apply("damageTaken")))
					.modifierGlory(Integer.parseInt(getCol.apply("modifierGlory")))
					.activeModifiers(new ArrayList<>(activeModsMap.values()))
					.timeTaken(Double.parseDouble(getCol.apply("timeTaken")))
					.totalTimeTaken(formatTime(Double.parseDouble(getCol.apply("totalTimeTaken"))))
					.waveGlory(Integer.parseInt(getCol.apply("waveGlory")))
					.totalGlory(Integer.parseInt(getCol.apply("totalGlory")))
					.serpentShamanSpawnX(getIntCol.apply("serpentShamanSpawnX"))
					.serpentShamanSpawnY(getIntCol.apply("serpentShamanSpawnY"))
					.javelinColossusSpawnAX(getIntCol.apply("javelinColossusSpawnAX"))
					.javelinColossusSpawnAY(getIntCol.apply("javelinColossusSpawnAY"))
					.javelinColossusSpawnBX(getIntCol.apply("javelinColossusSpawnBX"))
					.javelinColossusSpawnBY(getIntCol.apply("javelinColossusSpawnBY"))
					.manticoreSpawnAX(getIntCol.apply("manticoreSpawnAX"))
					.manticoreSpawnAY(getIntCol.apply("manticoreSpawnAY"))
					.manticoreSequenceA(parseManticoreSequence(getCol.apply("manticoreSequenceA")))
					.manticoreSpawnBX(getIntCol.apply("manticoreSpawnBX"))
					.manticoreSpawnBY(getIntCol.apply("manticoreSpawnBY"))
					.manticoreSequenceB(parseManticoreSequence(getCol.apply("manticoreSequenceB")))
					.shockwaveColossusSpawnAX(getIntCol.apply("shockwaveColossusSpawnAX"))
					.shockwaveColossusSpawnAY(getIntCol.apply("shockwaveColossusSpawnAY"))
					.shockwaveColossusSpawnBX(getIntCol.apply("shockwaveColossusSpawnBX"))
					.shockwaveColossusSpawnBY(getIntCol.apply("shockwaveColossusSpawnBY"))
					.jaguarWarriorReinforcementsSpawnX(getIntCol.apply("jaguarWarriorReinfSpawnX"))
					.jaguarWarriorReinforcementsSpawnY(getIntCol.apply("jaguarWarriorReinfSpawnY"))
					.serpentShamanReinforcementsSpawnX(getIntCol.apply("serpentShamanReinfSpawnX"))
					.serpentShamanReinforcementsSpawnY(getIntCol.apply("serpentShamanReinfSpawnY"))
					.minotaurReinforcementsSpawnX(getIntCol.apply("minotaurReinfSpawnX"))
					.minotaurReinforcementsSpawnY(getIntCol.apply("minotaurReinfSpawnY"))
					.build();

				waveDtos.add(waveDto);

				result = getCol.apply("status");
				if (result.equals("CANCELLED"))
					result = "CLAIMED";
			}
			final String attemptResult = result;

			String fileName = file.getName().replace("_wave-log.csv", "");
			String[] nameParts = fileName.split("_");
			long timestamp = LocalDateTime.parse(nameParts[1] + "_" + nameParts[2],
				DateTimeFormatter.ofPattern("yyMMdd_HHmmss")).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

			Set<Integer> itemIds = waveDtos.stream()
				.map(ColosseumWaveDtoV1::getEarnedLoot)
				.filter(java.util.Objects::nonNull)
				.map(ItemBundle::getItemId)
				.collect(Collectors.toSet());

			Map<Integer, Integer> aggregatedRewards = new HashMap<>();
			int totalGlory = 0;
			double totalTime = .0;

			for (ColosseumWaveDtoV1 wave : waveDtos) {
				ItemBundle loot = wave.getEarnedLoot();
				if (loot != null) {
					aggregatedRewards.merge(loot.getItemId(), loot.getQuantity(), Integer::sum);
					if (wave.getWave() == 12) {
						if (config.logQuiverAsSplinters())
							aggregatedRewards.merge(SUNFIRE_SPLINTERS_ID, 4000, Integer::sum);
						else
							aggregatedRewards.merge(DIZANAS_QUIVER_UNCHARGED_ID, 1, Integer::sum);
					}
				}
				totalGlory = wave.getTotalGlory();
				totalTime = wave.getTotalTimeTaken();
			}

			final int finalWaveTotalGlory = totalGlory;
			final double finalWaveTotalTimeTaken = totalTime;
			final String finalTag = tag;
			clientThread.invokeLater(() -> {
				Map<String, ValuedItemStack> rewardsMap = generateNamedRewardsMap(aggregatedRewards);

				// SET LOOT VALUES HERE ON THE CLIENT THREAD
				for (ColosseumWaveDtoV1 wave : waveDtos) {
					if (wave.getEarnedLoot() != null) {
						int price = itemManager.getItemPrice(wave.getEarnedLoot().getItemId());
						wave.setLootValue(price * wave.getEarnedLoot().getQuantity());
					}
				}

				ColosseumAttemptDtoV1 attemptDto = ColosseumAttemptDtoV1.builder()
					.attemptId(fileName)
					.timestamp(timestamp)
					.accountName(nameParts[0])
					.gameMode(DEFAULT_GAMEMODE_DTO)
					.tag(finalTag)
					.rewardsValue(rewardsMap.values().stream()
						.mapToInt(ValuedItemStack::getTotalValueInGp)
						.sum())
					.rewards(rewardsMap)
					.result(attemptResult)
					.waves(waveDtos)
					.totalGlory(finalWaveTotalGlory)
					.totalTime(finalWaveTotalTimeTaken)
					.activeModifiers(new ArrayList<>(activeModsMap.values()))
					.build();

				try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(jsonlFile, true))) {
					Gson jsonlGson = gson.newBuilder().disableHtmlEscaping().create();
					bw.write(jsonlGson.toJson(attemptDto));
					bw.newLine();
					return true;
				} catch (Exception e) {
					log.error("Failed to append migrated Colosseum CSV to V1 JSONL", e);
					return false;
				}
			});

			return true;
		} catch (Exception e) {
			log.error("Failed to migrate Colosseum CSV to V1", e);
			return false;
		}
	}

	private Map<String, ValuedItemStack> generateNamedRewardsMap(Map<Integer, Integer> rewards) {
		Map<String, ValuedItemStack> namedRewards = new LinkedHashMap<>();

		if (rewards == null || rewards.isEmpty()) {
			return namedRewards;
		}

		for (Map.Entry<Integer, Integer> entry : rewards.entrySet()) {
			int itemId = entry.getKey();
			int quantity = entry.getValue();

			ItemComposition comp = itemManager.getItemComposition(itemId);
			String itemName = (comp != null) ? comp.getName() : "Unknown Item (" + itemId + ")";

			int price = itemManager.getItemPrice(itemId);
			int totalValue = price * quantity;

			namedRewards.merge(itemName, new ValuedItemStack(quantity, totalValue),
				(existing, replacement) -> new ValuedItemStack(
					existing.getCount() + replacement.getCount(),
					existing.getTotalValueInGp() + replacement.getTotalValueInGp()
				));
		}

		return namedRewards;
	}

	private double formatTime(double time) {
		return Math.round(time * 10.0) / 10.0;
	}

	private List<ManticoreAttackSequence.ManticoreOrb> parseManticoreSequence(String sequence) {
		if (sequence == null || sequence.isEmpty() || sequence.equalsIgnoreCase("UNKNOWN")) {
			return new ArrayList<>();
		}

		return Arrays.stream(sequence.split("-"))
			.map(s -> {
				try {
					return ManticoreAttackSequence.ManticoreOrb.valueOf(s.toUpperCase());
				} catch (IllegalArgumentException e) {
					return ManticoreAttackSequence.ManticoreOrb.UNKNOWN;
				}
			})
			.collect(Collectors.toList());
	}
}