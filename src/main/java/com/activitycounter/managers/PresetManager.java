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

package com.activitycounter.managers;

import com.activitycounter.ActivityCounterPlugin;
import com.activitycounter.PluginConstants;
import com.activitycounter.listeners.core.ConfigListener;
import com.activitycounter.models.ConfigPreset;
import com.activitycounter.models.TrackedActivity;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.util.Filepath;

/**
 * Manager class for plugin config presets.
 * Saves presets locally as JSON files in .runelite/activitycounter/presets/
 */
@Slf4j
@Singleton
public class PresetManager
{
	@Inject
	private ConfigManager configManager;

	@Inject
	private Gson gson;

	@Inject
	private ActivityCounterPlugin plugin;

	@Inject
	private ConfigListener configListener;

	public static final String DEFAULT_PRESET_NAME = "";
	public static final String DEFAULT_PRESET_FILE = "default.json";

	// Flag to prevent recursive saving while a preset is actively being applied
	private boolean isApplyingPreset = false;

	public static final List<String> PRESET_KEYS = Arrays.asList(
		// Session state & Hidden configs
		"pinnedActivities",
		"hourlyRateActivities",
		"hiddenActivities",
		"pinScoreOverlay",
		"scoreHourlyRate",
		"pinTimeOverlay",
		"customScoreLabel",

		// General & UI settings
		"confirmTerminateSession",
		"enableSessionConfigurations",
		"mergeSlayerTaskCounts ",
		"showSessionDuration",
		"showStartTime",
		"showEndTime",
		"showNotesTextBox",

		// Sorting configurations
		"sortBosses",
		"sortChests",
		"sortClue",
		"sortSlayer",
		"sortAgility",
		"sortExperience",
		"sortLevels",
		"sortBoltProcs",
		"sortOther",
		"sortRaids",
		"sortMiniGames",
		"sortMagicSpells",
		"sortCombat",
		"sortSupplies",
		"sortSkilling",
		"sortAchievements",
		"sortSailing",
		"sortTertiaryDrops",
		"sortRandomEvents"
	);

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(PluginConstants.CONFIG_GROUP))
		{
			return;
		}

		// Prevent recursive saving when we are the ones triggering the config changes
		if (isApplyingPreset)
		{
			return;
		}

		// If a relevant key changed, automatically silently overwrite the currently active preset
		if (PRESET_KEYS.contains(event.getKey()))
		{
			String currentPresetName = configManager.getConfiguration(PluginConstants.CONFIG_GROUP, "lastPresetUsed");
			if (currentPresetName == null || currentPresetName.isEmpty())
			{
				currentPresetName = DEFAULT_PRESET_NAME;
			}

			log.debug("Auto-saving preset '{}' due to config change: {}", currentPresetName, event.getKey());
			saveCurrentAsPreset(currentPresetName);
		}
	}

	/**
	 * Retrieves all saved presets from the local file system using Filepath.
	 */
	public List<ConfigPreset> getPresets() {
		List<ConfigPreset> presets = new ArrayList<>();
		Filepath presetDir = plugin.getDirectory().join(PluginConstants.PRESETS_DIR_NAME);
		if (!presetDir.exists() || !presetDir.isDirectory()) {
			return presets;
		}
		try (Stream<Filepath> files = presetDir.walk(1)) {
			files.filter(f -> f.isFile() && f.getFileName().endsWith(".json"))
				.forEach(file -> {
					try (Reader reader = file.openBufferedReader()) {
						ConfigPreset preset = gson.fromJson(reader, ConfigPreset.class);
						if (preset != null) {
							presets.add(preset);
						}
					} catch (IOException e) {
						log.error("Failed to read preset file: {}", file.getFileName(), e);
					}
				});
		} catch (IOException e) {
			log.error("Failed to walk preset directory", e);
		}

		// Sort presets: default preset is always at the top, others alphabetically
		presets.sort((p1, p2) -> {
			if (DEFAULT_PRESET_NAME.equals(p1.getName())) return -1;
			if (DEFAULT_PRESET_NAME.equals(p2.getName())) return 1;
			return p1.getName().compareToIgnoreCase(p2.getName());
		});

		return presets;
	}

	/**
	 * Saves the current state of the tracked configuration keys as a new preset file.
	 */
	public void saveCurrentAsPreset(String presetName)
	{
		Filepath presetDir = plugin.getDirectory().join(PluginConstants.PRESETS_DIR_NAME);
		if (!presetDir.exists())
		{
			try
			{
				presetDir.createDirectories();
			}
			catch (IOException e)
			{
				log.error("Failed to create preset directory", e);
				return;
			}
		}

		ConfigPreset preset = new ConfigPreset(presetName);

		for (String key : PRESET_KEYS)
		{
			// Explicitly intercept pinnedActivities to serialize it directly from RAM
			if (key.equals("pinnedActivities"))
			{
				String pinnedCsv = plugin.getPinnedActivities().stream()
					.map(act -> String.valueOf(act.getId()))
					.collect(Collectors.joining(","));
				preset.getSettings().put(key, pinnedCsv);
			}
			else
			{
				String value = configManager.getConfiguration(PluginConstants.CONFIG_GROUP, key);
				if (value != null)
				{
					preset.getSettings().put(key, value);
				}
			}
		}

		Filepath file = getSanitizedFilepath(presetName);

		try (java.io.Writer writer = file.openBufferedWriter())
		{
			gson.toJson(preset, writer);
			log.debug("Successfully saved preset to {}", file);

			if (!DEFAULT_PRESET_NAME.equals(presetName)) {
				configManager.setConfiguration(PluginConstants.CONFIG_GROUP, "lastPresetUsed", presetName);
			}
		}
		catch (IOException e)
		{
			log.error("Failed to save preset: {}", presetName, e);
		}
	}

	/**
	 * Applies a preset's settings to the active RuneLite configuration.
	 */
	public void applyPreset(ConfigPreset preset)
	{
		isApplyingPreset = true;

		try {
			for (String key : PRESET_KEYS)
			{
				String value = preset.getSettings().get(key);
				if (value != null)
				{
					configManager.setConfiguration(PluginConstants.CONFIG_GROUP, key, value);
				}
				else
				{
					configManager.unsetConfiguration(PluginConstants.CONFIG_GROUP, key);
				}
			}

			plugin.getPinnedActivities().clear();
			String pinnedStr = preset.getSettings().get("pinnedActivities");
			if (pinnedStr != null && !pinnedStr.trim().isEmpty())
			{
				for (String part : pinnedStr.split(","))
				{
					try
					{
						int id = Integer.parseInt(part.trim());
						TrackedActivity act = plugin.getActivityById(id);
						if (act != null)
						{
							plugin.getPinnedActivities().add(act);
						}
					}
					catch (NumberFormatException ignored) {}
				}
			}

			// Update the last used preset
			configManager.setConfiguration(PluginConstants.CONFIG_GROUP, "lastPresetUsed", preset.getName());

			// Force the config cache to rebuild
			configListener.buildAllCaches();
			configListener.buildHourlyRateCache();

			// Evaluate listeners and scores
			plugin.evaluateConditionalListeners();
			plugin.syncActiveSessionScores();

			// Force the UI panel to repaint
			if (plugin.getPanel() != null)
			{
				plugin.getPanel().refreshKcContainer();
			}
		} finally {
			// Ensure the lock is ALWAYS released, even if an exception occurs during UI repaint
			isApplyingPreset = false;
		}
	}


	/**
	 * Deletes a preset's JSON file.
	 */
	public void deletePreset(ConfigPreset presetToDelete)
	{
		Filepath file = getSanitizedFilepath(presetToDelete.getName());
		if (file.exists())
		{
			try
			{
				file.delete();
				log.debug("Deleted preset file: {}", file.getFileName());
			}
			catch (IOException e)
			{
				log.error("Failed to delete preset file: {}", file.getFileName(), e);
			}
		}
		else
		{
			log.warn("Failed to delete preset file or file did not exist: {}", file.getFileName());
		}
	}

	public void loadDefaultPreset() {
		Filepath file = plugin.getDirectory().join(PluginConstants.PRESETS_DIR_NAME).join(DEFAULT_PRESET_FILE);
		if (file.exists()) {
			try (Reader reader = file.openBufferedReader()) {
				ConfigPreset preset = gson.fromJson(reader, ConfigPreset.class);
				if (preset != null) {
					applyPreset(preset);
				}
			} catch (IOException e) {
				log.error("Failed to load default preset", e);
			}
		}
	}

	public void saveDefaultPreset() {
		saveCurrentAsPreset(DEFAULT_PRESET_NAME);
	}

	/**
	 * Strips invalid Windows/Unix path characters from the preset name to create a safe file path.
	 */
	private Filepath getSanitizedFilepath(String presetName) {
		if (DEFAULT_PRESET_NAME.equals(presetName)) {
			return plugin.getDirectory().join(PluginConstants.PRESETS_DIR_NAME).join(DEFAULT_PRESET_FILE);
		}
		String safeName = presetName.replaceAll("[^a-zA-Z0-9.\\- ]", "_");
		return plugin.getDirectory().join(PluginConstants.PRESETS_DIR_NAME).join(safeName + ".json");
	}

	public ConfigPreset getPresetByName(String name) {
		return getPresets().stream()
			.filter(p -> p.getName().equalsIgnoreCase(name))
			.findFirst()
			.orElse(null);
	}

	public void ensureDefaultPresetExists() {
		Filepath file = plugin.getDirectory().join(PluginConstants.PRESETS_DIR_NAME).join(DEFAULT_PRESET_FILE);
		if (!file.exists()) {
			saveDefaultPreset();
		}
	}

	public void loadPresetByName(String name) {
		if (name == null || name.trim().isEmpty()) {
			loadDefaultPreset();
			return;
		}

		ConfigPreset preset = getPresetByName(name);
		if (preset != null) {
			applyPreset(preset);
		} else {
			log.warn("Preset '{}' not found, falling back to default.", name);
			loadDefaultPreset();
		}
	}

	/**
	 * Loads the preset defined by 'lastPresetUsed' in the configuration.
	 * If none exists, loads the default preset.
	 */
	public void loadLastUsedPreset()
	{
		String lastUsed = configManager.getConfiguration(PluginConstants.CONFIG_GROUP, "lastPresetUsed");
		loadPresetByName(lastUsed);
	}
}