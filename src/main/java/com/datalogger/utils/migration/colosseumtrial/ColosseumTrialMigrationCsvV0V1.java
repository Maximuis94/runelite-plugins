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
import com.datalogger.models.colosseum.ManticoreAttackSequence;
import com.datalogger.models.itemvault.ItemBundle;
import com.datalogger.models.supplytracker.ValuedItemStack;
import com.datalogger.utils.migration.DataMigration;
import com.datalogger.utils.migration.colosseumtrial.models.ColosseumAttemptDtoV1;
import com.datalogger.utils.migration.colosseumtrial.models.ColosseumWaveDtoV1;
import com.google.gson.Gson;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;

/**
 * Migrates CSV Colosseum trial data from v1.1.0 (V0) to v1.2.0 (V1).
 */
@Slf4j
@Singleton
public class ColosseumTrialMigrationCsvV0V1 implements DataMigration {

	private static final DateTimeFormatter FALLBACK_DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMdd_HHmmss");

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
		return file != null && file.isFile() && file.getName().endsWith("_wave-log.csv");
	}

	@Override
	public boolean migrate(File file) {
		log.debug("Starting Colosseum CSV Migration from V0 to V1 for {}", file.getName());

		try {
			List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
			if (lines.size() <= 1) {
				log.warn("CSV file is empty or missing data rows: {}", file.getName());
				return false;
			}

			String[] headers = lines.get(0).split(",", -1);
			Map<String, Integer> headerMap = new HashMap<>();
			for (int i = 0; i < headers.length; i++) {
				headerMap.put(cleanCsvValue(headers[i]), i);
			}

			if (headerMap.containsKey("activeModifiers")) {
				log.debug("CSV already migrated to V1 schema. Skipping.");
				return false;
			}

			String rawFileName = file.getName().replace("_wave-log.csv", "");
			long attemptTimestamp = resolveTimestamp(rawFileName);
			String attemptAccount = resolveAccount(rawFileName);
			String attemptId = resolveAttemptId(attemptAccount, attemptTimestamp, rawFileName);

			Map<Integer, Integer> priceMap = preWarmPriceCache(lines, headerMap);

			Map<String, ValuedItemStack> totalRewardsLedger = new LinkedHashMap<>();
			Map<String, String> activeModsMap = new LinkedHashMap<>();
			List<ColosseumWaveDtoV1> v1Waves = new ArrayList<>();

			String attemptResult = "UNKNOWN";
			String attemptTag = "";

			for (int i = 1; i < lines.size(); i++) {
				String[] cols = lines.get(i).split(",", -1);
				if (cols.length < headers.length) continue;

				Function<String, String> getCol = name -> {
					Integer idx = headerMap.get(name);
					return (idx != null && idx < cols.length) ? cleanCsvValue(cols[idx]) : null;
				};

				int waveNum = getInt(getCol.apply("wave"), 0);
				attemptTag = getCol.apply("tag") != null ? getCol.apply("tag") : "";
				String rowStatus = getCol.apply("status");

				// Wave result cancelled -> trial result of claimed
				if (i == lines.size() - 1) {
					attemptResult = "CANCELLED".equalsIgnoreCase(rowStatus) ? "CLAIMED" : rowStatus;
				}

				String chosenMod = getCol.apply("chosenModifier");
				if (chosenMod != null && !chosenMod.isEmpty()) {
					String baseMod = chosenMod.contains("_") ? chosenMod.substring(0, chosenMod.lastIndexOf('_')) : chosenMod;
					activeModsMap.put(baseMod, chosenMod);
				}

				List<String> offeredChoices = new ArrayList<>();
				for (String choiceCol : new String[]{"modifierChoice_I", "modifierChoice_II", "modifierChoice_III"}) {
					String choiceVal = getCol.apply(choiceCol);
					if (choiceVal != null && !choiceVal.isEmpty() && !"-1".equals(choiceVal) && !"UNKNOWN".equalsIgnoreCase(choiceVal)) {
						offeredChoices.add(choiceVal);
					}
				}

				ItemBundle rngLoot = extractRngItemBundle(getCol);
				int waveRngGp = 0;

				if (rngLoot != null) {
					int itemGp = rngLoot.getQuantity() * priceMap.getOrDefault(rngLoot.getItemId(), 0);
					recordReward(totalRewardsLedger, rngLoot, itemGp);
					waveRngGp = itemGp;
				}

				if (waveNum == 12) {
					ItemBundle quiver = config.logQuiverAsSplinters() ? DIZANAS_QUIVER_SWAPPED_REWARD : DIZANAS_QUIVER_REWARD;
					int quiverGp = quiver.getQuantity() * priceMap.getOrDefault(quiver.getItemId(), 0);
					recordReward(totalRewardsLedger, quiver, quiverGp);
				}

				boolean canHaveManticore = waveNum >= 4 && waveNum < 12;

				ColosseumWaveDtoV1 waveDto = ColosseumWaveDtoV1.builder()
					.wave(waveNum)
					.status(rowStatus)
					.accountName(getCol.apply("accountName"))
					.tag(attemptTag)
					.gameMode(DEFAULT_GAMEMODE_DTO)
					.earnedLoot(rngLoot)
					.lootValue(waveRngGp)
					.modifierChoices(offeredChoices) // <-- Attached here!
					.chosenModifier(chosenMod)
					.activeModifiers(new ArrayList<>(activeModsMap.values()))
					.timeTaken(formatTime(getDouble(getCol.apply("timeTaken"), 0.0)))
					.totalTimeTaken(formatTime(getDouble(getCol.apply("totalTimeTaken"), 0.0)))
					.speedBonus(getInt(getCol.apply("speedBonus"), 0))
					.damageBonus(getInt(getCol.apply("damageBonus"), 0))
					.damageTaken(getInt(getCol.apply("damageTaken"), 0))
					.completionBonus(getInt(getCol.apply("completionBonus"), 0))
					.modifierGlory(getInt(getCol.apply("modifierGlory"), 0))
					.waveGlory(getInt(getCol.apply("waveGlory"), 0))
					.totalGlory(getInt(getCol.apply("totalGlory"), 0))
					.serpentShamanSpawnX(getCoord(getCol.apply("serpentShamanSpawnX")))
					.serpentShamanSpawnY(getCoord(getCol.apply("serpentShamanSpawnY")))
					.javelinColossusSpawnAX(getCoord(getCol.apply("javelinColossusSpawnAX")))
					.javelinColossusSpawnAY(getCoord(getCol.apply("javelinColossusSpawnAY")))
					.javelinColossusSpawnBX(getCoord(getCol.apply("javelinColossusSpawnBX")))
					.javelinColossusSpawnBY(getCoord(getCol.apply("javelinColossusSpawnBY")))
					.shockwaveColossusSpawnAX(getCoord(getCol.apply("shockwaveColossusSpawnAX")))
					.shockwaveColossusSpawnAY(getCoord(getCol.apply("shockwaveColossusSpawnAY")))
					.shockwaveColossusSpawnBX(getCoord(getCol.apply("shockwaveColossusSpawnBX")))
					.shockwaveColossusSpawnBY(getCoord(getCol.apply("shockwaveColossusSpawnBY")))
					.jaguarWarriorReinforcementsSpawnX(getCoord(getCol.apply("jaguarWarriorReinfSpawnX")))
					.jaguarWarriorReinforcementsSpawnY(getCoord(getCol.apply("jaguarWarriorReinfSpawnY")))
					.serpentShamanReinforcementsSpawnX(getCoord(getCol.apply("serpentShamanReinfSpawnX")))
					.serpentShamanReinforcementsSpawnY(getCoord(getCol.apply("serpentShamanReinfSpawnY")))
					.minotaurReinforcementsSpawnX(getCoord(getCol.apply("minotaurReinfSpawnX")))
					.minotaurReinforcementsSpawnY(getCoord(getCol.apply("minotaurReinfSpawnY")))
					.manticoreSpawnAX(canHaveManticore ? getCoord(getCol.apply("manticoreSpawnAX")) : null)
					.manticoreSpawnAY(canHaveManticore ? getCoord(getCol.apply("manticoreSpawnAY")) : null)
					.manticoreSequenceA(parseManticoreSequence(getCol.apply("manticoreSequenceA"), canHaveManticore))
					.manticoreSpawnBX(canHaveManticore ? getCoord(getCol.apply("manticoreSpawnBX")) : null)
					.manticoreSpawnBY(canHaveManticore ? getCoord(getCol.apply("manticoreSpawnBY")) : null)
					.manticoreSequenceB(parseManticoreSequence(getCol.apply("manticoreSequenceB"), canHaveManticore))
					.build();

				v1Waves.add(waveDto);
			}

			int grandTotalGp = totalRewardsLedger.values().stream().mapToInt(ValuedItemStack::getTotalValueInGp).sum();
			int grandTotalGlory = v1Waves.isEmpty() ? 0 : v1Waves.get(v1Waves.size() - 1).getTotalGlory();
			double grandTotalDuration = v1Waves.isEmpty() ? 0.0 : v1Waves.get(v1Waves.size() - 1).getTotalTimeTaken();

			ColosseumAttemptDtoV1 attemptDto = ColosseumAttemptDtoV1.builder()
				.attemptId(attemptId)
				.timestamp(attemptTimestamp)
				.accountName(attemptAccount)
				.gameMode(DEFAULT_GAMEMODE_DTO)
				.tag(attemptTag)
				.rewardsValue(grandTotalGp)
				.rewards(totalRewardsLedger)
				.result(attemptResult)
				.totalGlory(grandTotalGlory)
				.totalTime(formatTime(grandTotalDuration))
				.activeModifiers(new ArrayList<>(activeModsMap.values()))
				.waves(v1Waves)
				.build();

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(jsonlFile, true))) {
				Gson noHtmlGson = this.gson.newBuilder().disableHtmlEscaping().create();
				writer.write(noHtmlGson.toJson(attemptDto));
				writer.newLine();
			}

			log.debug("Successfully migrated CSV to V1 JSONL: {}", file.getName());
			return true;

		} catch (Exception e) {
			log.error("Failed to migrate Colosseum CSV file: {}", file.getName(), e);
			return false;
		}
	}

	private ItemBundle extractRngItemBundle(Function<String, String> getCol) {
		String rawIds = getCol.apply("itemIds");
		if (rawIds == null || rawIds.isEmpty()) return null;

		String[] ids = rawIds.split("\\|");
		String[] names = getCol.apply("itemNames").split("\\|");
		String[] qtys = getCol.apply("quantities").split("\\|");

		int targetIdx = ids.length - 1;
		if (targetIdx < 0 || ids[targetIdx].isEmpty()) return null;

		try {
			return new ItemBundle(Integer.parseInt(ids[targetIdx]), names[targetIdx], Integer.parseInt(qtys[targetIdx]));
		} catch (NumberFormatException ignored) { return null; }
	}

	private void recordReward(Map<String, ValuedItemStack> ledger, ItemBundle bundle, int calculatedGp) {
		ValuedItemStack existing = ledger.getOrDefault(bundle.getItemName(), new ValuedItemStack(0, 0));
		ledger.put(bundle.getItemName(), new ValuedItemStack(
			existing.getCount() + bundle.getQuantity(),
			existing.getTotalValueInGp() + calculatedGp
		));
	}

	private Map<Integer, Integer> preWarmPriceCache(List<String> lines, Map<String, Integer> headerMap) {
		Set<Integer> uniqueIds = new HashSet<>();
		uniqueIds.add(DIZANAS_QUIVER_REWARD.getItemId());
		uniqueIds.add(DIZANAS_QUIVER_SWAPPED_REWARD.getItemId());

		Integer idColIdx = headerMap.get("itemIds");
		if (idColIdx != null) {
			for (int i = 1; i < lines.size(); i++) {
				String[] cols = lines.get(i).split(",", -1);
				if (idColIdx < cols.length && !cols[idColIdx].isEmpty()) {
					for (String rawId : cols[idColIdx].split("\\|")) {
						String clean = cleanCsvValue(rawId);
						if (!clean.isEmpty()) {
							try { uniqueIds.add(Integer.parseInt(clean)); } catch (NumberFormatException ignored) {}
						}
					}
				}
			}
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

	private List<ManticoreAttackSequence.ManticoreOrb> parseManticoreSequence(String sequence, boolean canHaveManticore) {
		if (!canHaveManticore) return null;

		// --- NEW: Returning null instead of new ArrayList<>() kills Difference #3! ---
		if (sequence == null || sequence.isEmpty() || "UNKNOWN".equalsIgnoreCase(sequence) || "-1".equals(sequence)) {
			return null;
		}

		List<ManticoreAttackSequence.ManticoreOrb> orbs = Arrays.stream(sequence.split("-"))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.map(s -> {
				try {
					return ManticoreAttackSequence.ManticoreOrb.valueOf(s.toUpperCase());
				} catch (IllegalArgumentException e) {
					return ManticoreAttackSequence.ManticoreOrb.UNKNOWN;
				}
			})
			.collect(Collectors.toList());

		return orbs.isEmpty() ? null : orbs;
	}

	private Integer getCoord(String val) {
		int v = getInt(val, -1);
		return (v == -1) ? null : v;
	}

	private int getInt(String val, int fallback) {
		if (val == null || val.isEmpty() || "-1".equals(val)) return fallback;
		try { return Integer.parseInt(val); } catch (NumberFormatException e) { return fallback; }
	}

	private double getDouble(String val, double fallback) {
		if (val == null || val.isEmpty()) return fallback;
		try { return Double.parseDouble(val); } catch (NumberFormatException e) { return fallback; }
	}

	private String cleanCsvValue(String val) {
		if (val == null) return "";
		String trimmed = val.trim();
		if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
			return trimmed.substring(1, trimmed.length() - 1);
		}
		return trimmed;
	}

	private long resolveTimestamp(String fileName) {
		if (fileName.length() >= 14) {
			try {
				String dateStr = fileName.substring(fileName.length() - 13);
				return LocalDateTime.parse(dateStr, FALLBACK_DATE_FORMAT).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
			} catch (Exception ignored) {}
		}
		return System.currentTimeMillis();
	}

	private String resolveAccount(String fileName) {
		return fileName.length() >= 14 ? fileName.substring(0, fileName.length() - 14) : "Unknown";
	}

	private String resolveAttemptId(String account, long timestamp, String sourceName) {
		if (sourceName.contains("_") && sourceName.length() >= 14) return sourceName;
		return account + "_" + Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(Colosseum.COLOSSEUM_TRIAL_TIMESTAMP_FORMATTER);
	}

	private double formatTime(double time) { return Math.round(time * 10.0) / 10.0; }
}