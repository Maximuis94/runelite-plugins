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

package com.frequentlytradeditemstab.services;

import com.frequentlytradeditemstab.PluginConstants;
import static com.frequentlytradeditemstab.PluginConstants.PLUGIN_DIR;
import com.frequentlytradeditemstab.models.GrandExchangeHistoryEntry;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class GeHistoryCacheManager {
	private final Gson gson;
	private final Map<Long, List<GrandExchangeHistoryEntry>> historyByAccount = new HashMap<>();

	@Inject
	public GeHistoryCacheManager(Gson gson) {
		this.gson = gson;
	}

	public CompletableFuture<List<GrandExchangeHistoryEntry>> loadHistoryAsync() {
		return CompletableFuture.supplyAsync(() -> {
			Map<Long, List<GrandExchangeHistoryEntry>> loadedMap = new HashMap<>();

			if (PLUGIN_DIR.exists()) {
				File[] files = PLUGIN_DIR.listFiles((dir, name) ->
					name.startsWith(PluginConstants.Cache.HISTORY_FILE_PREFIX) &&
						name.endsWith(PluginConstants.Cache.FILE_EXTENSION));

				if (files != null) {
					Type listType = new TypeToken<ArrayList<GrandExchangeHistoryEntry>>(){}.getType();
					for (File file : files) {
						try (FileReader reader = new FileReader(file)) {
							List<GrandExchangeHistoryEntry> loaded = gson.fromJson(reader, listType);
							if (loaded != null && !loaded.isEmpty()) {
								long hash = loaded.get(0).getAccountHash();
								loadedMap.put(hash, loaded);
							}
						} catch (Exception e) {
							log.error("Failed to read GE history cache file: " + file.getName(), e);
						}
					}
				}
			}

			synchronized (historyByAccount) {
				historyByAccount.clear();
				historyByAccount.putAll(loadedMap);
			}

			return getAllCachedHistory();
		});
	}

	public void saveHistoryEntriesAsync(long accountHash, List<GrandExchangeHistoryEntry> newEntries) {
		if (accountHash == -1) return;

		synchronized (historyByAccount) {
			historyByAccount.computeIfAbsent(accountHash, k -> new ArrayList<>()).addAll(newEntries);
		}

		CompletableFuture.runAsync(() -> {
			PLUGIN_DIR.mkdirs();
			File accountFile = new File(PLUGIN_DIR, PluginConstants.Cache.HISTORY_FILE_PREFIX + accountHash + PluginConstants.Cache.FILE_EXTENSION);

			try (FileWriter writer = new FileWriter(accountFile)) {
				List<GrandExchangeHistoryEntry> copyToSave;
				synchronized (historyByAccount) {
					copyToSave = new ArrayList<>(historyByAccount.get(accountHash));
				}
				gson.toJson(copyToSave, writer);
			} catch (Exception e) {
				log.error("Failed to save GE history cache for account hash: {}", accountHash, e);
			}
		});
	}

	public List<GrandExchangeHistoryEntry> getAllCachedHistory() {
		synchronized (historyByAccount) {
			return historyByAccount.values().stream()
				.flatMap(List::stream)
				.collect(Collectors.toList());
		}
	}
}