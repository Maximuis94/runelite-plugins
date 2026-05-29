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

package com.datalogger.utils.migration;

import com.datalogger.constants.PluginConstants;
import static com.datalogger.constants.PluginConstants.COLOSSEUM_ROOT_DIR;
import static com.datalogger.constants.PluginConstants.INTERNAL_COLOSSEUM_TRIAL_HISTORY;
import com.datalogger.dto.ColosseumAttemptDTO;
import com.datalogger.dto.ColosseumWaveDTO;
import com.datalogger.utils.migration.colosseumtrial.ColosseumTrialMigrationCsvV0V1;
import com.datalogger.utils.migration.colosseumtrial.ColosseumTrialMigrationV0V1;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class MigrationManager
{
	private final ColosseumTrialMigrationCsvV0V1 csvMigration;
	private final ColosseumTrialMigrationV0V1 jsonMigration;
	private final Gson gson;
	private final static JsonParser jsonParser = new JsonParser();

	@Getter
	private boolean hasMigrated = false;

	public static final long COLOSSEUM_TRIAL_MIGRATION_THRESHOLD = 1780000000000L;

//	private File jsonFile = new File(COLOSSEUM_ROOT_DIR, "migration-test.jsonl");
	private final File jsonFile = INTERNAL_COLOSSEUM_TRIAL_HISTORY;

	@Inject
	public MigrationManager(ColosseumTrialMigrationCsvV0V1 csvMigration, ColosseumTrialMigrationV0V1 jsonMigration, Gson gson)
	{
		this.csvMigration = csvMigration;
		this.jsonMigration = jsonMigration;
		this.gson = gson;
	}

	/**
	 * Parse and return the trialIds from the output jsonl file. These trialIds are used to identify whether a trial is
	 * already logged or not.
	 */
	private List<String> parseTrialIds()
	{
		List<String> trialIds = new ArrayList<>();
		if (jsonFile == null || !jsonFile.exists())
		{
			return trialIds;
		}

		try (BufferedReader reader = Files.newBufferedReader(jsonFile.toPath(), StandardCharsets.UTF_8))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				line = line.strip();
				if (line.isEmpty())
				{
					continue;
				}

				try
				{
					JsonObject jsonObject = jsonParser.parse(line).getAsJsonObject();
					if (jsonObject.has("attemptId"))
					{
						trialIds.add(jsonObject.get("attemptId").getAsString());
					}
				}
				catch (Exception e)
				{
					log.warn("Failed to parse line for trial ID extraction in {}", jsonFile.getName(), e);
				}
			}
		}
		catch (Exception e)
		{
			log.error("Failed to read trial IDs from migration target file: {}", jsonFile.getName(), e);
		}

		return trialIds;
	}

	public int migrateColosseumTrialsV0V1()
	{
		if (!COLOSSEUM_ROOT_DIR.exists() || !COLOSSEUM_ROOT_DIR.isDirectory())
		{
			return 0;
		}
		hasMigrated = true;

		List<String> existingTrialIds = parseTrialIds();

		// Iterate over all directories in COLOSSEUM_ROOT_DIR
		File[] directories = COLOSSEUM_ROOT_DIR.listFiles(File::isDirectory);
		if (directories == null)
		{
			return 0;
		}
		int nMigrated = 0;
		for (File dir : directories)
		{
			File[] files = dir.listFiles(File::isFile);
			if (files == null)
			{
				continue;
			}

			// In each directory, attempt to convert the wave-log.json/csv, depending on the converter.
			for (File file : files)
			{
				// Test JSON Migration
				if (jsonMigration.identify(file))
				{
					String trialId = file.getName().replace("_wave-log.json", "");
					if (existingTrialIds.contains(trialId))
					{
						log.debug("JSON trial already migrated, skipping: {}", trialId);
						continue;
					}

					log.debug("JSON migration identified file: {}", file.getName());
					jsonMigration.migrate(file);

					existingTrialIds.add(trialId);
					nMigrated++;
				}

				// Test CSV Migration
				if (csvMigration.identify(file))
				{
					String trialId = file.getName().replace("_wave-log.csv", "");
					if (existingTrialIds.contains(trialId))
					{
						log.debug("CSV trial already migrated, skipping: {}", trialId);
						continue;
					}

					log.debug("CSV migration identified file: {}", file.getName());
					csvMigration.migrate(file);

					existingTrialIds.add(trialId);
					nMigrated++;
				}
			}
		}

		rebuildTrialDirectoriesFromMigration(jsonFile);
		hasMigrated = true;
		return nMigrated;
	}

	/**
	 * Iterates over the centralized JSONL migration file and recreates the
	 * individual trial directories, populating them with updated .json and .csv logs.
	 */
	public void rebuildTrialDirectoriesFromMigration(File jsonlFile)
	{
		if (!jsonlFile.exists() || !jsonlFile.isFile())
		{
			log.warn("Cannot rebuild directories: JSONL migration file not found at {}", jsonlFile.getAbsolutePath());
			return;
		}

		log.debug("Starting to rebuild Colosseum trial directories from {}", jsonlFile.getName());

		try (BufferedReader reader = new BufferedReader(new FileReader(jsonlFile)))
		{
			String line;
			int count = 0;

			while ((line = reader.readLine()) != null)
			{
				if (line.trim().isEmpty()) continue;

				try
				{
					// Parse directly into the main DTO (version field will default to 1 automatically)
					ColosseumAttemptDTO attempt = gson.fromJson(line, ColosseumAttemptDTO.class);

					if (attempt == null) continue;

					File trialDir = new File(PluginConstants.COLOSSEUM_TRIALS_DIR, attempt.getAttemptId());
					if (!trialDir.exists())
					{
						trialDir.mkdirs();
					}

					String baseFileName = attempt.getAttemptId() + "_wave-log";

					// 2. Dump the updated JSON
					File jsonFile = new File(trialDir, baseFileName + ".json");
					try (BufferedWriter jsonWriter = new BufferedWriter(new FileWriter(jsonFile)))
					{
						// Using a pretty-printing Gson instance here if preferred, or standard gson
						gson.toJson(attempt, jsonWriter);
					}

					// 3. Dump the updated CSV
					File csvFile = new File(trialDir, baseFileName + ".csv");
					writeWaveLogCsv(csvFile, attempt);

					count++;
				}
				catch (Exception e)
				{
					log.error("Failed to parse or write a line from the JSONL file.", e);
				}
			}

			log.debug("Successfully rebuilt {} trial directories.", count);
		}
		catch (Exception e)
		{
			log.error("Failed to process the JSONL migration file.", e);
		}
	}

	/**
	 * Helper method to generate the CSV format for an attempt.
	 * If your FileIOService already has a method for this, you can swap this out!
	 */
	private void writeWaveLogCsv(File csvFile, ColosseumAttemptDTO attempt) throws Exception
	{
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile)))
		{
			// Write Headers
			bw.write("wave,status,accountName,tag,itemId,itemName,quantity,lootValue,chosenModifier,activeModifiers,completionBonus,speedBonus,damageBonus,damageTaken,modifierGlory,timeTaken,totalTimeTaken,waveGlory,totalGlory,serpentShamanSpawnX,serpentShamanSpawnY,javelinColossusSpawnAX,javelinColossusSpawnAY,javelinColossusSpawnBX,javelinColossusSpawnBY,manticoreSpawnAX,manticoreSpawnAY,manticoreSequenceA,manticoreSpawnBX,manticoreSpawnBY,manticoreSequenceB,shockwaveColossusSpawnAX,shockwaveColossusSpawnAY,shockwaveColossusSpawnBX,shockwaveColossusSpawnBY,jaguarWarriorReinfSpawnX,jaguarWarriorReinfSpawnY,serpentShamanReinfSpawnX,serpentShamanReinfSpawnY,minotaurReinfSpawnX,minotaurReinfSpawnY");
			bw.newLine();

			// Write wave rows
			for (ColosseumWaveDTO wave : attempt.getWaves())
			{
				StringBuilder sb = new StringBuilder();

				String itemIds = "";
				String itemNames = "";
				String quantities = "";

				if (wave.getEarnedLoot() != null)
				{
					itemIds = String.valueOf(wave.getEarnedLoot().getItemId());
					itemNames = wave.getEarnedLoot().getItemName();
					quantities = String.valueOf(wave.getEarnedLoot().getQuantity());
				}

				String activeModifiersStr = wave.getActiveModifiers() != null
					? String.join("|", wave.getActiveModifiers())
					: "";

				Object[] rowData = {
					wave.getWave(),
					wave.getStatus(),
					wave.getAccountName(),
					wave.getTag() == null ? "" : wave.getTag(),
					itemIds,
					itemNames,
					quantities,
					wave.getLootValue(),
					wave.getChosenModifier() == null ? "" : wave.getChosenModifier(),
					activeModifiersStr,
					wave.getCompletionBonus(),
					wave.getSpeedBonus(),
					wave.getDamageBonus(),
					wave.getDamageTaken(),
					wave.getModifierGlory(),
					wave.getTimeTaken(),
					wave.getTotalTimeTaken(),
					wave.getWaveGlory(),
					wave.getTotalGlory(),
					valOrEmpty(wave.getSerpentShamanSpawnX()),
					valOrEmpty(wave.getSerpentShamanSpawnY()),
					valOrEmpty(wave.getJavelinColossusSpawnAX()),
					valOrEmpty(wave.getJavelinColossusSpawnAY()),
					valOrEmpty(wave.getJavelinColossusSpawnBX()),
					valOrEmpty(wave.getJavelinColossusSpawnBY()),
					valOrEmpty(wave.getManticoreSpawnAX()),
					valOrEmpty(wave.getManticoreSpawnAY()),
					listOrEmpty(wave.getManticoreSequenceA()),
					valOrEmpty(wave.getManticoreSpawnBX()),
					valOrEmpty(wave.getManticoreSpawnBY()),
					listOrEmpty(wave.getManticoreSequenceB()),
					valOrEmpty(wave.getShockwaveColossusSpawnAX()),
					valOrEmpty(wave.getShockwaveColossusSpawnAY()),
					valOrEmpty(wave.getShockwaveColossusSpawnBX()),
					valOrEmpty(wave.getShockwaveColossusSpawnBY()),
					valOrEmpty(wave.getJaguarWarriorReinforcementsSpawnX()),
					valOrEmpty(wave.getJaguarWarriorReinforcementsSpawnY()),
					valOrEmpty(wave.getSerpentShamanReinforcementsSpawnX()),
					valOrEmpty(wave.getSerpentShamanReinforcementsSpawnY()),
					valOrEmpty(wave.getMinotaurReinforcementsSpawnX()),
					valOrEmpty(wave.getMinotaurReinforcementsSpawnY())
				};

				for (int i = 0; i < rowData.length; i++)
				{
					sb.append(rowData[i]);
					if (i < rowData.length - 1) sb.append(",");
				}

				bw.write(sb.toString());
				bw.newLine();
			}
		}
	}

	private String valOrEmpty(Integer val) {
		return val != null ? String.valueOf(val) : "";
	}

	private String listOrEmpty(java.util.List<?> list) {
		return (list != null && !list.isEmpty()) ? list.stream().map(Object::toString).collect(Collectors.joining("-")) : "";
	}



	/**
	 * Returns true if dir follows the pattern of a logged Colosseum trial directory and if the timestamp suggests it is
	 * a legacy trial.
	 */
	public boolean isMigrateableLoggedTrialDir(File dir)
	{
		String name = dir.getName();

		if (!dir.isDirectory() || name.length() < 15)
		{
			return false;
		}

		try
		{
			int lastUnderscore = name.lastIndexOf('_');
			int firstUnderscore = name.lastIndexOf('_', lastUnderscore - 1);

			if (firstUnderscore == -1 || lastUnderscore == -1 || (lastUnderscore - firstUnderscore != 7))
			{
				return false;
			}

			for (int i = firstUnderscore + 1; i < lastUnderscore; i++)
			{
				if (!Character.isDigit(name.charAt(i)))
				{
					return false;
				}
			}

			if (name.length() - (lastUnderscore + 1) != 6)
			{
				return false;
			}

			for (int i = lastUnderscore + 1; i < name.length(); i++)
			{
				if (!Character.isDigit(name.charAt(i)))
				{
					return false;
				}
			}
			String dirName = dir.getName();
			String timestampPart = dirName.substring(firstUnderscore + 1);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd_HHmmss");
			LocalDateTime trialTime = LocalDateTime.parse(timestampPart, formatter);
			long unix = trialTime.toEpochSecond(ZoneOffset.UTC);
			return COLOSSEUM_TRIAL_MIGRATION_THRESHOLD > unix;
		}
		catch (Exception e)
		{
			return false;
		}
	}
}