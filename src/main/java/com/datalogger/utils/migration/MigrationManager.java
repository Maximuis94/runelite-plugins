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

import static com.datalogger.constants.PluginConstants.COLOSSEUM_ROOT_DIR;

import static com.datalogger.constants.PluginConstants.INTERNAL_COLOSSEUM_TRIAL_HISTORY;
import com.datalogger.utils.migration.colosseumtrial.ColosseumTrialMigrationCsvV0V1;
import com.datalogger.utils.migration.colosseumtrial.ColosseumTrialMigrationV0V1;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class MigrationManager
{
	private final ColosseumTrialMigrationCsvV0V1 csvMigration;
	private final ColosseumTrialMigrationV0V1 jsonMigration;

//	private File jsonFile = new File(COLOSSEUM_ROOT_DIR, "migration-test.jsonl");
	private File jsonFile = INTERNAL_COLOSSEUM_TRIAL_HISTORY;

	@Inject
	public MigrationManager(ColosseumTrialMigrationCsvV0V1 csvMigration, ColosseumTrialMigrationV0V1 jsonMigration)
	{
		this.csvMigration = csvMigration;
		this.jsonMigration = jsonMigration;
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
					JsonObject jsonObject = new JsonParser().parse(line).getAsJsonObject();
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

	public void migrateColosseumTrialsV0V1()
	{
		if (!COLOSSEUM_ROOT_DIR.exists() || !COLOSSEUM_ROOT_DIR.isDirectory())
		{
			return;
		}

		List<String> existingTrialIds = parseTrialIds();

		// Iterate over all directories in COLOSSEUM_ROOT_DIR
		File[] directories = COLOSSEUM_ROOT_DIR.listFiles(File::isDirectory);
		if (directories == null)
		{
			return;
		}

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
				}
			}
		}
	}
}