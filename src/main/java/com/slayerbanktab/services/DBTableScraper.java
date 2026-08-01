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

package com.slayerbanktab.services;

import com.slayerbanktab.models.SlayerArea;
import com.slayerbanktab.models.Task;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.DBTableID;

@Slf4j
@Singleton
public class DBTableScraper
{
	private final Client client;

	@Inject
	public DBTableScraper(Client client)
	{
		this.client = client;
	}

	/**
	 * Fetches the name of a standard Slayer task.
	 * Must be called on the ClientThread.
	 */
	public String getTaskName(int taskId)
	{
		try
		{
			List<Integer> rows = client.getDBRowsByValue(DBTableID.SlayerTask.ID, DBTableID.SlayerTask.COL_ID, 0, taskId);
			if (rows != null && !rows.isEmpty())
			{
				int structId = rows.get(0);
				Object[] fieldValues = client.getDBTableField(structId, DBTableID.SlayerTask.COL_NAME_UPPERCASE, 0);
				if (fieldValues != null && fieldValues.length > 0 && fieldValues[0] instanceof String)
				{
					return (String) fieldValues[0];
				}
			}
		}
		catch (Exception e)
		{
			log.warn("Failed to fetch task name for ID {}", taskId, e);
		}
		return null;
	}

	/**
	 * Fetches the name of a Boss task.
	 * Must be called on the ClientThread.
	 */
	public String getBossTaskName(int bossId)
	{
		try
		{
			List<Integer> bossRows = client.getDBRowsByValue(DBTableID.SlayerTaskSublist.ID, DBTableID.SlayerTaskSublist.COL_TASK_SUBTABLE_ID, 0, bossId);
			if (bossRows != null && !bossRows.isEmpty())
			{
				Object[] taskFieldValues = client.getDBTableField(bossRows.get(0), DBTableID.SlayerTaskSublist.COL_TASK, 0);
				if (taskFieldValues != null && taskFieldValues.length > 0 && taskFieldValues[0] instanceof Integer)
				{
					int bossTaskDBRow = (Integer) taskFieldValues[0];
					Object[] nameFieldValues = client.getDBTableField(bossTaskDBRow, DBTableID.SlayerTask.COL_NAME_UPPERCASE, 0);
					if (nameFieldValues != null && nameFieldValues.length > 0 && nameFieldValues[0] instanceof String)
					{
						return (String) nameFieldValues[0];
					}
				}
			}
		}
		catch (Exception e)
		{
			log.warn("Failed to fetch boss task name for ID {}", bossId, e);
		}
		return null;
	}

	/**
	 * Fetches the name of a specific Slayer area.
	 * Must be called on the ClientThread.
	 */
	public String getAreaName(int areaId)
	{
		try
		{
			List<Integer> rows = client.getDBRowsByValue(DBTableID.SlayerArea.ID, DBTableID.SlayerArea.COL_AREA_ID, 0, areaId);
			if (rows != null && !rows.isEmpty())
			{
				Object[] fieldValues = client.getDBTableField(rows.get(0), DBTableID.SlayerArea.COL_AREA_NAME_IN_HELPER, 0);
				if (fieldValues != null && fieldValues.length > 0 && fieldValues[0] instanceof String)
				{
					return (String) fieldValues[0];
				}
			}
		}
		catch (Exception e)
		{
			log.warn("Failed to fetch area name for ID {}", areaId, e);
		}
		return null;
	}

	/**
	 * Scrapes and returns a map of all valid standard Slayer tasks.
	 */
	public Map<Integer, String> getAllSlayerTasks()
	{
		Map<Integer, String> tasks = new HashMap<>();
		for (int taskId = 1; taskId <= 500; taskId++)
		{
			String name = getTaskName(taskId);
			if (name != null && !name.trim().isEmpty())
			{
				tasks.put(taskId, name.trim());
				log.debug("taskId: {}, name: {}", taskId, name);
			}
		}
		return tasks;
	}

	/**
	 * Scrapes and returns a map of all valid Boss tasks.
	 */
	public Map<Integer, String> getAllBossTasks()
	{
		Map<Integer, String> bosses = new HashMap<>();
		for (int bossId = 1; bossId <= 100; bossId++)
		{
			String name = getBossTaskName(bossId);
			if (name != null && !name.trim().isEmpty())
			{
				bosses.put(bossId, name.trim());
			}
		}
		return bosses;
	}

	/**
	 * Scrapes and returns a map of all valid Slayer areas.
	 */
	public Map<Integer, String> getAllSlayerAreas()
	{
		Map<Integer, String> areas = new HashMap<>();
		for (int areaId = 1; areaId <= 100; areaId++)
		{
			String name = getAreaName(areaId);
			if (name != null && !name.trim().isEmpty())
			{
				areas.put(areaId, name.trim());
			}
		}
		return areas;
	}

	/**
	 * Debug method that iterates over all enum members that were extracted from DBTables and checks whether these
	 * values are up-to-date. If not, notify the user.
	 */
	public void verifyEnumMappingConsistency()
	{
		log.debug("Starting verification of Enum IDs against Client DB tables...");
		int missingIds = 0;
		int mismatchedNames = 0;

		for (SlayerArea area : SlayerArea.values())
		{
			if (area == SlayerArea.NO_AREA_SPECIFIED) continue;

			String dbName = getAreaName(area.getId());
			if (dbName == null)
			{
				log.warn("VERIFY FAILED: SlayerArea '{}' (ID: {}) returned null from the DB. ID may have changed or been removed.", area.name(), area.getId());
				missingIds++;
			}
			else if (!dbName.equalsIgnoreCase(area.getName()))
			{
				log.debug("VERIFY MISMATCH (Area): Enum name '{}' differs from DB string '{}' for ID: {}", area.getName(), dbName, area.getId());
				mismatchedNames++;
			}
		}

		for (Task task : Task.values())
		{
			if (task == Task.BOSS || task == Task.UNKNOWN_BOSS) continue;

			if (task.isBoss())
			{
				String dbBossName = getBossTaskName(task.getSlayerTargetBossId());
				if (dbBossName == null)
				{
					log.warn("VERIFY FAILED: Boss Task '{}' (Boss ID: {}) returned null from the DB.", task.name(), task.getSlayerTargetBossId());
					missingIds++;
				}
				else if (!dbBossName.equalsIgnoreCase(task.getName()))
				{
					log.debug("VERIFY MISMATCH (Boss): Enum name '{}' differs from DB string '{}' for Boss ID: {}", task.getName(), dbBossName, task.getSlayerTargetBossId());
					mismatchedNames++;
				}
			}
			else
			{
				String dbTaskName = getTaskName(task.getSlayerTargetId());
				if (dbTaskName == null)
				{
					log.warn("VERIFY FAILED: Task '{}' (Target ID: {}) returned null from the DB.", task.name(), task.getSlayerTargetId());
					missingIds++;
				}
				else if (!dbTaskName.equalsIgnoreCase(task.getName()))
				{
					log.debug("VERIFY MISMATCH (Task): Enum name '{}' differs from DB string '{}' for Target ID: {}", task.getName(), dbTaskName, task.getSlayerTargetId());
					mismatchedNames++;
				}
				log.debug("dbTaskName: ");
			}
		}

		log.debug("Verification complete. Missing IDs: {}, Name mismatches (can usually be safely ignored): {}", missingIds, mismatchedNames);
	}
}