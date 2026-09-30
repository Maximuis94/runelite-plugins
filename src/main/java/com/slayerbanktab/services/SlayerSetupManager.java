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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.slayerbanktab.PluginConstants;
import com.slayerbanktab.SlayerBankTabPlugin;
import com.slayerbanktab.models.SlayerSetup;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.util.Filepath;

@Slf4j
@Singleton
public class SlayerSetupManager {

	private final Gson gson;
	private final SlayerBankTabPlugin plugin;
	private final ConfigManager configManager;
	private Filepath setupFile;

	private final Map<String, SlayerSetup> taskSetups = new HashMap<>();

	private final ExecutorService fileWriteExecutor = Executors.newSingleThreadExecutor();

	@Inject
	public SlayerSetupManager(Gson gson, SlayerBankTabPlugin plugin, ConfigManager configManager) {
		this.gson = gson;
		this.plugin = plugin;
		this.configManager = configManager;
	}

	/**
	 * Synchronously loads the setups from the local global file.
	 * Called during plugin startUp().
	 */
	public void loadSetups() {
		Filepath dirPath = plugin.getDirectory();
		if (dirPath == null) {
			log.error("Failed to resolve plugin directory. Cannot load setups.");
			return;
		}

		if (!dirPath.exists()) {
			try {
				dirPath.createDirectories();
				log.debug("Created plugin directory '{}'", dirPath);
			} catch (IOException e) {
				log.error("Failed to create plugin directory '{}'", dirPath, e);
				return;
			}
		}

		this.setupFile = dirPath.join("global-setups.json");

		// MIGRATION CHECK: If file doesn't exist, scrape ConfigManager for old data
		if (!setupFile.exists()) {
			migrateFromConfigManager();
		}

		// If it still doesn't exist (e.g. migration yielded 0 results and didn't write), exit early
		if (!setupFile.exists()) {
			return;
		}

		try (Reader reader = setupFile.openBufferedReader()) {
			Type type = new TypeToken<Map<String, SlayerSetup>>(){}.getType();
			Map<String, SlayerSetup> loaded = gson.fromJson(reader, type);
			if (loaded != null) {
				taskSetups.clear();
				taskSetups.putAll(loaded);
				log.debug("Successfully loaded {} global slayer setups from file.", taskSetups.size());
			}
		} catch (Exception e) {
			log.error("Failed to load slayer setups from local file", e);
		}
	}

	/**
	 * Scrapes the RuneLite ConfigManager for old JSON setups, saves them to memory,
	 * unsets them from the config profile, and dumps them to the new file.
	 */
	private void migrateFromConfigManager() {
		log.debug("global-setups.json not found. Scanning ConfigManager for old setups...");
		boolean migratedAny = false;

		String prefix = PluginConstants.CONFIG_GROUP + ".";
		List<String> keys = configManager.getConfigurationKeys(prefix);

		for (String fullKey : keys) {
			String key = fullKey.substring(prefix.length());

			if (!key.startsWith("layout_")) {
				continue;
			}

			String rawValue = configManager.getConfiguration(PluginConstants.CONFIG_GROUP, key);
			if (rawValue != null && !rawValue.isEmpty()) {
				try {

					plugin.updateJsonFallback(key, rawValue);

					migratedAny = true;

					// Delete the giant string from the user's config profile to clean up memory
					// configManager.unsetConfiguration(PluginConstants.CONFIG_GROUP, key);

				} catch (Exception e) {
					log.debug("Failed to migrate setup key: {}", key, e);
				}
			}
		}

		// Because updateJsonFallback calls saveSetup(), the fileWriteExecutor is already queuing saves.
		// We perform one final synchronous dump here to ensure the complete map is written instantly
		// before loadSetups() finishes executing, avoiding any race conditions on startup.
		if (migratedAny) {
			try (Writer writer = setupFile.openBufferedWriter()) {
				gson.toJson(taskSetups, writer);
				log.debug("Successfully migrated {} setups from ConfigManager to global-setups.json", taskSetups.size());
			} catch (IOException e) {
				log.error("Failed to dump migrated setups to file", e);
			}
		}
	}

	/**
	 * Retrieves a saved setup for a specific task key.
	 */
	public SlayerSetup getSetupForTask(String setupKey) {
		if (setupKey == null || setupKey.isEmpty()) return null;
		String lower = setupKey.toLowerCase();
		return taskSetups.get(lower);
	}

	/**
	 * Caches the setup in memory and triggers a background save to the hard drive.
	 */
	public void saveSetup(String setupKey, SlayerSetup setup) {
		if (setupKey == null || setupKey.isEmpty() || setup == null) return;

		taskSetups.put(setupKey.toLowerCase(), setup);
		saveAsync();
	}

	/**
	 * Serializes the map to JSON and saves it to the local file asynchronously.
	 */
	private void saveAsync() {
		if (setupFile == null) {
			log.error("Cannot save setups: setupFile path is null");
			return;
		}

		Map<String, SlayerSetup> snapshot = new HashMap<>(taskSetups);

		fileWriteExecutor.submit(() -> {
			try (Writer writer = setupFile.openBufferedWriter()) {
				gson.toJson(snapshot, writer);
			} catch (Exception e) {
				log.error("Failed to serialize and save slayer setups to file", e);
			}
		});
	}

	/**
	 * Optional utility to clear a layout if you ever add a "Delete Setup" button.
	 */
	public void deleteSetup(String setupKey) {
		if (setupKey == null || setupKey.isEmpty()) return;

		if (taskSetups.remove(setupKey.toLowerCase()) != null) {
			saveAsync();
		}
	}

	/**
	 * Returns a detached snapshot of all currently saved task setups.
	 */
	public Map<String, SlayerSetup> getAllSetups() {
		return new HashMap<>(taskSetups);
	}
}