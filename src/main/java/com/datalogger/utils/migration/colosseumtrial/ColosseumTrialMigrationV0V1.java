/*
 * Copyright (c) 2026, maximuis94 <https://github.com/maximuis94>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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

import com.datalogger.models.itemvault.ItemBundle;
import com.datalogger.utils.migration.DataMigration;
import com.datalogger.utils.migration.colosseumtrial.models.ColosseumAttemptDtoV0;
import com.datalogger.utils.migration.colosseumtrial.models.ColosseumAttemptDtoV1;
import com.datalogger.utils.migration.colosseumtrial.models.ColosseumWaveDtoV0;
import com.datalogger.utils.migration.colosseumtrial.models.ColosseumWaveDtoV1;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

@Slf4j
public class ColosseumTrialMigrationV0V1 implements DataMigration {


	// We can instantiate a local Gson here just for formatting,
	// or you can inject your main Gson instance via a constructor.
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Override
	public int getStartVersion() { return 0; }

	@Override
	public int getEndVersion() { return 1; }

	@Override
	public boolean identify(@Nonnull File file) {
		File parentDirectory = file.getParentFile();
		String trialId = parentDirectory.getName();
		return !file.isFile() || !file.getName().endsWith(trialId + "_wave-log.json");
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
	private ColosseumAttemptDtoV1 convertTrialLog(File inputDirectory)
	{
		File inputFile = new File(inputDirectory, inputDirectory.getName() + "_wave-log.json");

		if (!inputFile.exists()) return null;

		ColosseumAttemptDtoV0 trialV0 = parseInputFile(inputFile);

		if (trialV0 == null) {
			return null;
		}

		String attemptId = inputDirectory.getParent();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd_HHmmss");

		LocalDateTime dateTime = LocalDateTime.parse(attemptId, formatter);

		long timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

		String account = attemptId.substring(0, attemptId.length() - 13);


		String accountFallback = "Unknown";
		String tagFallback = null;
		double totalTimeCalculated = 0.0;
		List<String> topLevelActiveModifiers = new ArrayList<>();

		if (trialV0.getWaves() != null && !trialV0.getWaves().isEmpty()) {
			for (ColosseumWaveDtoV0 wave : trialV0.getWaves()) {
				if (wave.getAccountName() != null && accountFallback.equals("Unknown")) {
					accountFallback = wave.getAccountName();
				}
				if (wave.getTag() != null && tagFallback == null) {
					tagFallback = wave.getTag();
				}
				if (wave.getChosenModifier() != null && !wave.getChosenModifier().isEmpty()) {
					topLevelActiveModifiers.add(wave.getChosenModifier());
				}
			}

			// The total time of the attempt is the totalTimeTaken of the final wave
			ColosseumWaveDtoV0 lastWave = trialV0.getWaves().get(trialV0.getWaves().size() - 1);
			totalTimeCalculated = lastWave.getTotalTimeTaken();
		}

		// 2. Map the waves
		List<ColosseumWaveDtoV1> newWaves = new ArrayList<>();
		List<String> currentWaveActiveModifiers = new ArrayList<>();

		if (trialV0.getWaves() != null) {
			for (ColosseumWaveDtoV0 waveV0 : trialV0.getWaves()) {

				ItemBundle v1Loot = null;
				if (waveV0.getEarnedLoot() != null && !waveV0.getEarnedLoot().isEmpty()) {
					v1Loot = waveV0.getEarnedLoot().get(0);
				}

				ColosseumWaveDtoV1 wave = convertWave(waveV0);

				String chosenModifier = wave.getChosenModifier();


				currentWaveActiveModifiers.add(wave.getChosenModifier());

				ColosseumWaveDtoV1 waveV1 = ColosseumWaveDtoV1.builder()
					.wave(waveV0.getWave())
					.status(waveV0.getStatus())
					.accountName(waveV0.getAccountName())
					.tag(waveV0.getTag())
					.earnedLoot(v1Loot)
					.lootValue(0)
					.modifierChoices(waveV0.getModifierChoices() != null ? new ArrayList<>(waveV0.getModifierChoices()) : new ArrayList<>())
					.chosenModifier(waveV0.getChosenModifier())
					.activeModifiers(activeModsString) // Apply historical running list
					.timeTaken(waveV0.getTimeTaken())
					.speedBonus(waveV0.getSpeedBonus())
					.damageTaken(waveV0.getDamageTaken())
					.damageBonus(waveV0.getDamageBonus())
					.modifierGlory(waveV0.getModifierGlory())
					.completionBonus(waveV0.getCompletionBonus())
					.waveGlory(waveV0.getWaveGlory())
					.totalGlory(waveV0.getTotalGlory())
					.totalTimeTaken(waveV0.getTotalTimeTaken())
					// Spawns mapping (exact 1:1)
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

				newWaves.add();

				// Add the modifier chosen THIS wave to the active list for the NEXT wave
				if (waveV0.getChosenModifier() != null && !waveV0.getChosenModifier().isEmpty()) {
					currentWaveActiveModifiers.add(waveV0.getChosenModifier());
				}
			}
		}

		// 3. Build the final V1 Attempt
		// Note: I am casting newWaves. If AttemptDtoV1 expects "ColosseumWaveDTO" exactly,
		// you may need to adjust the class generic type in your live file.
		return ColosseumAttemptDtoV1.builder()
			.attemptId(String.valueOf(trialV0.getAttemptId()))
			.timestamp(trialV0.getTimestamp())
			.account(accountFallback)
			.result(trialV0.getResult() != null ? trialV0.getResult() : "UNKNOWN")
			.rewardsValue(0) // Default historical
			.rewards(new HashMap<>()) // Default historical
			.consumedSupplyValue(0) // Default historical
			.consumedSupplies(null) // Default historical
			.totalGlory(trialV0.getTotalGlory())
			.totalTime(totalTimeCalculated)
			.tag(tagFallback)
			.activeModifiers(topLevelActiveModifiers)
			.waves((List) newWaves) // Cast/Assign to your wave list
			.build();
	}

	/**
	 * Convert the given trialLog to the newer version
	 */
	private ColosseumWaveDtoV1 convertWave(ColosseumWaveDtoV0 trialLog)
	{

	}

	@Override
	public boolean migrate(File file) {
		log.info("Migrating Colosseum wave-log to V1: {}", file.getName());

		try {
			JsonElement legacyData;
			try (FileReader reader = new FileReader(file)) {
				JsonParser parser = new JsonParser();
				legacyData = parser.parse(reader);
			}

			JsonObject v1Data;

			if (legacyData.isJsonArray()) {
				v1Data = new JsonObject();
				v1Data.addProperty("version", getEndVersion());
				v1Data.add("waves", legacyData);
			} else if (legacyData.isJsonObject()) {
				v1Data = legacyData.getAsJsonObject();
				v1Data.addProperty("version", getEndVersion());
			} else {
				log.warn("Unknown JSON structure in file: {}", file.getName());
				return false;
			}

			File backupFile = new File(file.getAbsolutePath() + ".bak");

			if (backupFile.exists() && !backupFile.delete()) {
				log.error("Could not delete existing backup file: {}", backupFile.getName());
				return false;
			}

			if (!file.renameTo(backupFile)) {
				log.error("Failed to rename original file to backup: {}", file.getName());
				return false;
			}

			try (FileWriter writer = new FileWriter(file)) {
				gson.toJson(v1Data, writer);
			}

			log.debug("Successfully migrated {}", file.getName());
			return true;

		} catch (Exception e) {
			log.error("Exception occurred while migrating file: {}", file.getName(), e);
			return false;
		}
	}
}