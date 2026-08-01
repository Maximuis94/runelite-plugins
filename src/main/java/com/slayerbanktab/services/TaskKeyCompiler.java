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
import static com.slayerbanktab.PluginConstants.NO_TASK_KEY_SUFFIX;
import com.slayerbanktab.SlayerBankTabConfig;
import com.slayerbanktab.models.SlayerMaster;
import com.slayerbanktab.models.Task;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;

@Singleton
public class TaskKeyCompiler {
	private final SlayerBankTabConfig config;
	private final SlayerSetupManager setupManager;
	private final DBTableScraper scraper;

	private final Client client;

	@Inject
	public TaskKeyCompiler(SlayerBankTabConfig config, SlayerSetupManager setupManager, DBTableScraper scraper, Client client) {
		this.config = config;
		this.setupManager = setupManager;
		this.scraper = scraper;
		this.client = client;
	}

	public String compileKey(long hash, SlayerMaster master, Task task, int areaId) {
		int masterId = (master != null) ? master.getId() : 0;
		int targetId = (task != null) ? task.getSlayerTargetId() : 0;
		int bossId   = (task != null) ? task.getSlayerTargetBossId() : 0;
		return compileRawKey(hash, masterId, targetId, bossId, areaId);
	}

	public String compileRawKey(long hash, int masterId, int taskId, int bossId, int areaId) {
		if (hash == -1) return null;

		if (masterId == 0 || taskId == 0) {
			return noTaskKey(hash);
		}

		final int resolvedMasterId;
		boolean isBoss = (taskId == BOSS_TASK_ID);
		boolean isNonWildyBoss = (isBoss && masterId != SlayerMaster.KRYSTILIA.getId());

		if (isNonWildyBoss) {
			resolvedMasterId = SlayerMaster.MERGED_STANDARD.getId();
		} else if (config.mergeOtherSetups() && SlayerMaster.isMergeableMasterId(masterId)) {
			resolvedMasterId = SlayerMaster.MERGED_STANDARD.getId();
		}
		else resolvedMasterId = masterId;


		final int resolvedTaskId;
		final int resolvedBossId;
		if (config.mergeTuraelTasks() && masterId == SlayerMaster.TURAEL.getId()) {
			resolvedTaskId = 0;
			resolvedBossId = 0;
		}
		else {
			resolvedTaskId = taskId;
			resolvedBossId = (taskId == BOSS_TASK_ID) ? bossId : 0;
		}
		final int resolvedAreaId = masterId != SlayerMaster.KONAR.getId() ? 0 : areaId;

		String definitiveKey = String.format("%d_%d_%d_%d_%d_0", hash, resolvedMasterId, resolvedTaskId, resolvedBossId, resolvedAreaId);
		String searchPrefix  = String.format("%d_%d_%d_%d_%d_", hash, resolvedMasterId, resolvedTaskId, resolvedBossId, resolvedAreaId);

		for (String savedKey : setupManager.getAllSetups().keySet()) {
			if (savedKey.startsWith(searchPrefix)) return savedKey;
		}

		return definitiveKey;
	}

	/**
	 * Key used if there is no active task; all parameters are 0, except for accountHash.
	 */
	public String noTaskKey(long hash)
	{
		return hash + NO_TASK_KEY_SUFFIX;
	}

	public String formatKeyToReadable(String key)
	{
		if (key.endsWith(PluginConstants.NO_TASK_KEY_SUFFIX)) {
			return "[No Task Assigned]";
		}

		try {
			String[] parts = key.split("_");
			if (parts.length < 6) return key;

			long accountHash = Long.parseLong(parts[0]);
			int masterId = Integer.parseInt(parts[1]);
			int targetId = Integer.parseInt(parts[2]);
			int bossId = Integer.parseInt(parts[3]);
			int areaId = Integer.parseInt(parts[4]);

			SlayerMaster master = (masterId == 99) ? SlayerMaster.MERGED_STANDARD : SlayerMaster.getById(masterId);
			String masterName = (master != null) ? master.getName() : ("Master#" + masterId);

			String taskName;
			if (master == SlayerMaster.TURAEL && targetId == 0 && bossId == 0) {
				taskName = "Merged Turael Tasks";
			} else {
				Task task = Task.getById(targetId, bossId);
				taskName = (task != null) ? task.getName() : ("Target#" + targetId);
			}

			if (master == SlayerMaster.KONAR)
			{
				String areaName = scraper.getAreaName(areaId);
				return String.format("[%s assigned by %s in %s]", taskName, masterName, areaName);
			}
			else if (master == SlayerMaster.KRYSTILIA) {
				return String.format("[%s assigned by %s (wilderness)]", taskName, masterName);
			} else {
				return String.format("[%s assigned by %s]", taskName, masterName);
			}
		} catch (Exception e) {
			return "[" + key + "]";
		}
	}

	private String getConfigCachedKey(long hash)
	{
		if (hash == -1) return null;
		return "cachedTaskKey_" + hash;
	}

	private String getConfigCachedKey()
	{
		long hash = client.getAccountHash();
		return getConfigCachedKey(hash);
	}
}