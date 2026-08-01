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

import com.slayerbanktab.PluginConstants;
import static com.slayerbanktab.PluginConstants.BOSS_TASK_ID;
import static com.slayerbanktab.PluginConstants.MAX_ACCOUNT_HASH_CHARACTERS;
import static com.slayerbanktab.PluginConstants.MIN_ACCOUNT_HASH_CHARACTERS;
import static com.slayerbanktab.PluginConstants.NO_TASK_KEY_SUFFIX;
import com.slayerbanktab.events.NewSlayerTask;
import com.slayerbanktab.models.SlayerArea;
import com.slayerbanktab.models.SlayerMaster;
import com.slayerbanktab.models.Task;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

/**
 * Tracker that provides data about the currently assigned task
 */
@Slf4j
@Singleton
public class SlayerTaskTracker {
	private static final NewSlayerTask NULL_TASK = new NewSlayerTask(null, 0, "None", 0, "None", 0, "NA", 0, "NA");

	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	@Inject
	private ConfigManager configManager;

	@Inject
	private DBTableScraper dbTableScraper;

	@Inject
	private TaskKeyCompiler taskKeyCompiler;

	@Getter
	private int activeTaskCount = -1;

	@Getter
	private int activeTaskId = -1;

	@Getter
	private int activeSlayerMasterId = -1;

	@Getter
	private int activeAreaId = -1;

	@Getter
	private int activeBossId = -1;

	@Getter
	private String setupKey = null;

	@Getter
	private NewSlayerTask currentTask = null;

	private boolean isUpdated = false;

	@Subscribe
	public void onVarbitChanged(VarbitChanged event) {
		boolean isRelevant = event == null ||
			event.getVarpId() == VarPlayerID.SLAYER_COUNT ||
			event.getVarpId() == VarPlayerID.SLAYER_TARGET ||
			event.getVarpId() == VarPlayerID.SLAYER_AREA ||
			event.getVarbitId() == VarbitID.SLAYER_MASTER ||
			event.getVarbitId() == VarbitID.SLAYER_TARGET_BOSSID;

		if (!isRelevant) return;
		checkTaskState();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		if (event.getGameState() == GameState.LOGGED_IN) {
			checkTaskState();
		} else if (event.getGameState() == GameState.LOGIN_SCREEN) {
			if (setupKey != null) {
				setupKey = null;
				currentTask = NULL_TASK;
				eventBus.post(NULL_TASK);
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals(PluginConstants.CONFIG_GROUP)) return;

		String k = event.getKey();
		if (k.equals("mergeTuraelTasks") || k.equals("mergeOtherSetups")) {
			isUpdated = false;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (isUpdated) return;
		isUpdated = updateActiveTask();
	}

	public void startUp() {
		if (client.getGameState() == GameState.LOGGED_IN) {
			checkTaskState();
			updateActiveTask();
		}
	}

	/**
	 * Check the current task via varbits and update variables if it is no longer synced.
	 */
	public void checkTaskState() {
		if (client.getGameState() != GameState.LOGGED_IN) return;

		int newCount = client.getVarpValue(VarPlayerID.SLAYER_COUNT);
		int newTaskId = client.getVarpValue(VarPlayerID.SLAYER_TARGET);
		int newAreaId = client.getVarpValue(VarPlayerID.SLAYER_AREA);
		int newMasterId = client.getVarbitValue(VarbitID.SLAYER_MASTER);

		int newBossId = (newTaskId == BOSS_TASK_ID) ? client.getVarbitValue(VarbitID.SLAYER_TARGET_BOSSID) : 0;

		if (newCount != activeTaskCount || newTaskId != activeTaskId || newAreaId != activeAreaId || newMasterId != activeSlayerMasterId || newBossId != activeBossId) {
			activeTaskCount = newCount;
			activeTaskId = newTaskId;
			activeAreaId = newAreaId;
			activeSlayerMasterId = newMasterId;
			activeBossId = newBossId;
			isUpdated = false;
		}
	}

	/**
	 * Extracts the 5th token (Area ID) out of a strictly compiled numerical key.
	 * e.g. "1166583798855853255_8_42_0_15_0" -> extracts '15'
	 */
	private static int extractAreaFromKey(String key) {
		if (key == null) return -1;
		int u1 = key.indexOf('_');
		if (u1 == -1) return -1;
		int u2 = key.indexOf('_', u1 + 1);
		if (u2 == -1) return -1;
		int u3 = key.indexOf('_', u2 + 1);
		if (u3 == -1) return -1;
		int u4 = key.indexOf('_', u3 + 1);
		if (u4 == -1) return -1;
		int u5 = key.indexOf('_', u4 + 1);
		if (u5 == -1) return -1;

		return parseIntSlice(key, u4 + 1, u5);
	}

	/**
	 * Updates the active task; returns false if there is no legal hash, otherwise true
	 */
	private boolean updateActiveTask() {
		long hash = client.getAccountHash();
		if (hash == -1) return false;

		String accountCacheKey = "cachedTaskKey_" + hash;
		String candidateKey;

		if (activeTaskCount == 0 || activeTaskId == 0 || activeSlayerMasterId == 0) {
			candidateKey = taskKeyCompiler.noTaskKey(hash);
		} else if (activeAreaId == -1 && activeSlayerMasterId == SlayerMaster.KONAR.getId()) {
			candidateKey = configManager.getConfiguration(PluginConstants.CONFIG_GROUP, accountCacheKey);

			if (candidateKey != null && !candidateKey.endsWith(PluginConstants.NO_TASK_KEY_SUFFIX)) {
				int extractedArea = extractAreaFromKey(candidateKey);
				if (extractedArea >= 0) {
					this.activeAreaId = extractedArea;
				}
				log.debug("Slayer Cape repeat: Restored cached key '{}' (Extracted Area ID: {})", candidateKey, extractedArea);
			} else {
				log.error("Unable to extract a legal areaId; invalidating key...");
				candidateKey = null;
			}
		} else {
			int effectiveAreaId = activeSlayerMasterId == SlayerMaster.KONAR.getId() ? activeAreaId : 0;
			candidateKey = taskKeyCompiler.compileRawKey(hash, activeSlayerMasterId, activeTaskId, activeBossId, effectiveAreaId);

			if (candidateKey != null && !candidateKey.endsWith(PluginConstants.NO_TASK_KEY_SUFFIX)) {
				configManager.setConfiguration(PluginConstants.CONFIG_GROUP, accountCacheKey, candidateKey);
			}
		}

		if (!Objects.equals(setupKey, candidateKey)) {
			setupKey = candidateKey;
			log.debug("Slayer task state resolved. Active key set to: {}", setupKey);
			dispatchTaskEvent();
		}
		return true;
	}

	private void dispatchTaskEvent() {
		if (setupKey == null) {
			currentTask = NULL_TASK;
			eventBus.post(NULL_TASK);
			return;
		}

		String taskName = "Unknown";
		String bossName = "NA";
		String taskLocation = "NA";

		try {
			if (activeTaskId == BOSS_TASK_ID) {
				taskName = "Boss";
				String scrapedBoss = dbTableScraper.getBossTaskName(activeBossId);
				if (scrapedBoss != null) bossName = scrapedBoss;
			} else {
				String scrapedTask = dbTableScraper.getTaskName(activeTaskId);
				if (scrapedTask != null) taskName = scrapedTask;
			}

			if (activeAreaId > 0) {
				String scrapedLocation = dbTableScraper.getAreaName(activeAreaId);
				if (scrapedLocation != null) taskLocation = scrapedLocation;
			}

			SlayerMaster master = SlayerMaster.getById(activeSlayerMasterId);
			String masterName = getSlayerMasterName(master);

			Player localPlayer = client.getLocalPlayer();
			String accountName = localPlayer != null ? localPlayer.getName() : "Unknown";

			currentTask = new NewSlayerTask(setupKey, activeTaskId, taskName, activeSlayerMasterId, masterName, activeBossId, bossName, activeAreaId, taskLocation);
			eventBus.post(currentTask);

		} catch (Exception e) {
			log.warn("Failed to lookup Slayer task DB details for event dispatch", e);
			SlayerMaster master = SlayerMaster.getById(activeSlayerMasterId);
			currentTask = new NewSlayerTask(setupKey, activeTaskId, taskName, activeSlayerMasterId, getSlayerMasterName(master), activeBossId, bossName, activeAreaId, taskLocation);
			eventBus.post(currentTask);
		}
	}

	private String getSlayerMasterName(SlayerMaster master) {
		if (master == null || master == SlayerMaster.NONE) return "None";
		switch (master) {
			case TURAEL: return client.getVarbitValue(VarbitID.WGS_TURAEL_RECRUIT) == 1 ? "Aya" : "Turael";
			case DURADEL: return client.getVarbitValue(VarbitID.WGS_DURADEL_RECRUIT) == 1 ? "Kuradal" : "Duradel";
			case NIEVE: return client.getVarbitValue(VarbitID.MM2_SLAYER_MASTER) == 1 ? "Steve" : "Nieve";
			case MERGED_STANDARD: return "Standard Masters";
			case MAZCHNA: return "Mazchna";
			case VANNAKA: return "Vannaka";
			case CHAELDAR: return "Chaeldar";
			case KRYSTILIA: return "Krystilia";
			case KONAR: return "Konar";
			case SPRIA: return "Spria";
			case MORTIMER: return "Mortimer";
			default: return master.toString();
		}
	}

	public String getCurrentArea() {
		if (activeAreaId <= 0) return null;
		return dbTableScraper.getAreaName(activeAreaId);
	}

	/**
	 * FIXED: Now generates 100% valid, playable modern layout keys for the UI search bar.
	 */
	public List<String> getAllSetupKeysForTarget(String targetName) {
		long hash = client.getAccountHash();
		if (hash == -1 || targetName == null) return Collections.emptyList();

		Task task = Task.getTaskByName(targetName);
		if (task == null) return Collections.emptyList();

		List<String> validKeys = new ArrayList<>();
		for (SlayerMaster master : SlayerMaster.values()) {
			if (master == SlayerMaster.UNKNOWN || master == SlayerMaster.NONE) continue;

			if (master == SlayerMaster.KONAR) {
				for (int areaId : dbTableScraper.getAllSlayerAreas().keySet()) {
					String k = taskKeyCompiler.compileKey(hash, master, task, areaId);
					if (k != null && !validKeys.contains(k)) validKeys.add(k);
				}
			} else {
				String k = taskKeyCompiler.compileKey(hash, master, task, 0);
				if (k != null && !validKeys.contains(k)) validKeys.add(k);
			}
		}
		return validKeys;
	}

	public boolean isLegalKey(String key) {
		if (key == null) return false;
		int idx1 = key.indexOf('_');
		if (idx1 > MAX_ACCOUNT_HASH_CHARACTERS || idx1 < MIN_ACCOUNT_HASH_CHARACTERS) return false;
		else if (key.endsWith(NO_TASK_KEY_SUFFIX)) return true;

		int idx2 = key.indexOf('_', idx1 + 1);
		if (idx2 == -1) return false;

		int idx3 = key.indexOf('_', idx2 + 1);
		if (idx3 == -1) return false;

		int idx4 = key.indexOf('_', idx3 + 1);
		if (idx4 == -1) return false;

		int idx5 = key.indexOf('_', idx4 + 1);
		if (idx5 == -1) return false;

		if (key.indexOf('_', idx5 + 1) != -1) return false;
		if (parseIntSlice(key, idx5 + 1, key.length()) != 0) return false;

		int masterId = parseIntSlice(key, idx1 + 1, idx2);
		if (masterId == Integer.MIN_VALUE || !SlayerMaster.isLegalId(masterId)) return false;

		int taskId = parseIntSlice(key, idx2 + 1, idx3);
		int bossId = parseIntSlice(key, idx3 + 1, idx4);
		if (taskId == Integer.MIN_VALUE || !Task.isLegalId(taskId, bossId)) return false;

		int areaId = parseIntSlice(key, idx4 + 1, idx5);
		if (areaId == Integer.MIN_VALUE || !SlayerArea.isLegalId(areaId)) return false;

		return true;
	}

	private static int parseIntSlice(String s, int start, int end) {
		if (start >= end) return Integer.MIN_VALUE;
		int i = start;
		boolean negative = false;
		if (s.charAt(i) == '-') {
			negative = true;
			i++;
			if (i == end) return Integer.MIN_VALUE;
		}
		int result = 0;
		while (i < end) {
			char c = s.charAt(i++);
			if (c < '0' || c > '9') return Integer.MIN_VALUE;
			result = result * 10 + (c - '0');
		}
		return negative ? -result : result;
	}

	/**
	 * Returns true if there currently is a task assigned
	 */
	public boolean hasActiveTask()
	{
		return setupKey != null;
	}

	/**
	 * Returns true if the given key corresponds with the active task key
	 */
	public boolean isActiveTaskKey(String key)
	{
		return setupKey.equals(key);
	}
}