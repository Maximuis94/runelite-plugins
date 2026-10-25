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

package com.activitycounter.listeners.core;

import com.activitycounter.ActivityCounterConfig;
import com.activitycounter.ActivityCounterPlugin;
import com.activitycounter.PluginConstants;
import com.activitycounter.listeners.AbstractListenerNode;
import com.activitycounter.models.Category;
import com.activitycounter.models.TrackedActivity;
import static com.activitycounter.models.TrackedActivity.MAX_TRACKING_ID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

@Singleton
public class ConfigListener extends AbstractListenerNode
{
	@Inject
	private ActivityCounterConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ActivityCounterPlugin plugin;

	private final static String HIDDEN_ACTIVITIES_CONFIG_KEY = "hiddenActivities";
	private final static String ACTIVITY_SCORES_CONFIG_KEY = "activityScores";
	private final static String HOURLY_RATE_CONFIG_KEY = "hourlyRateActivities";

	private final boolean[] visibilityCache = new boolean[MAX_TRACKING_ID + 1];
	private final int[] categorySortCache = new int[Category.values().length];
	private final float[] scoreCache = new float[MAX_TRACKING_ID + 1];

	public void buildAllCaches() {
		parseHiddenActivities();
		cacheCategorySortOrder();
		parseActivityScores();
		loadPinnedActivitiesConfig();
	}

	@Getter
	private final List<TrackedActivity> pinnedActivities = new ArrayList<>();

	/**
	 * Restores the pinned activities from the ConfigManager into RAM.
	 */
	public void loadPinnedActivitiesConfig()
	{
		pinnedActivities.clear();
		String csv = configManager.getConfiguration(PluginConstants.CONFIG_GROUP, "pinnedActivities");

		if (csv != null && !csv.trim().isEmpty())
		{
			for (String part : csv.split(","))
			{
				try
				{
					int id = Integer.parseInt(part.trim());
					for (TrackedActivity act : TrackedActivity.values()) {
						if (act.getId() == id) {
							pinnedActivities.add(act);
							break;
						}
					}
				}
				catch (NumberFormatException ignored) {}
			}
		}
	}

	/**
	 * Serializes the current RAM list into a CSV string and pushes it to the ConfigManager.
	 */
	private void savePinnedActivitiesConfig()
	{
		String csv = pinnedActivities.stream()
			.map(act -> String.valueOf(act.getId()))
			.collect(Collectors.joining(","));
		configManager.setConfiguration(PluginConstants.CONFIG_GROUP, "pinnedActivities", csv);
	}

	public void togglePinnedActivity(TrackedActivity activity)
	{
		if (pinnedActivities.contains(activity))
		{
			pinnedActivities.remove(activity);
		}
		else
		{
			pinnedActivities.add(activity);
		}
		savePinnedActivitiesConfig();
	}

	public void movePinnedActivityToTop(TrackedActivity activity)
	{
		pinnedActivities.remove(activity);
		pinnedActivities.add(0, activity);
		savePinnedActivitiesConfig();
	}

	public boolean isActivityPinned(TrackedActivity activity)
	{
		return pinnedActivities.contains(activity);
	}

	public void pinAllActivities(Iterable<TrackedActivity> activities)
	{
		for (TrackedActivity activity : activities)
		{
			if (!pinnedActivities.contains(activity)) {
				pinnedActivities.add(activity);
			}
		}
		savePinnedActivitiesConfig();
	}

	public void unpinAllActivities(Iterable<TrackedActivity> activities)
	{
		for (TrackedActivity activity : activities)
		{
			pinnedActivities.remove(activity);
		}
		savePinnedActivitiesConfig();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals(PluginConstants.CONFIG_GROUP)) return;

		String key = event.getKey();

		if (key.equals(HIDDEN_ACTIVITIES_CONFIG_KEY)) {
			parseHiddenActivities();
		} else if (key.equals(ACTIVITY_SCORES_CONFIG_KEY)) {
			parseActivityScores();
		} else if (key.startsWith("sort")) {
			cacheCategorySortOrder();
		}
	}

	private void parseHiddenActivities() {
		String hiddenStr = config.hiddenActivities();

		if (hiddenStr == null || hiddenStr.trim().isEmpty()) {
			Arrays.fill(visibilityCache, true);
		} else {
			Set<String> hiddenActivitySet = Arrays.stream(hiddenStr.split(","))
				.map(String::trim)
				.collect(Collectors.toSet());

			for (TrackedActivity activity : TrackedActivity.values()) {
				visibilityCache[activity.getId()] = !hiddenActivitySet.contains(activity.getConfigId());
			}
		}

		// Whenever visibility changes, force a re-evaluation of all conditional listeners
		if (plugin != null) {
			plugin.evaluateConditionalListeners();
		}
	}

	private void parseActivityScores() {
		String scoresStr = config.activityScores();
		Arrays.fill(scoreCache, 0f);

		if (scoresStr == null || scoresStr.trim().isEmpty()) return;

		String[] entries = scoresStr.split(",");
		for (String entry : entries) {
			String[] parts = entry.split(":");
			if (parts.length == 2) {
				String configId = parts[0].trim();
				try {
					float score = Float.parseFloat(parts[1].trim());
					for (TrackedActivity act : TrackedActivity.values()) {
						if (act.getConfigId().equals(configId)) {
							scoreCache[act.getId()] = score;
							break;
						}
					}
				} catch (NumberFormatException ignored) {}
			}
		}
	}

	private final boolean[] hourlyRateCache = new boolean[MAX_TRACKING_ID + 1];

	public void buildHourlyRateCache()
	{
		Arrays.fill(hourlyRateCache, false);

		String raw = config.hourlyRateActivities();
		if (raw == null || raw.trim().isEmpty())
		{
			return;
		}

		for (String part : raw.split(","))
		{
			try
			{
				int id = Integer.parseInt(part.trim());
				if (id >= 0 && id < hourlyRateCache.length)
				{
					hourlyRateCache[id] = true;
				}
			}
			catch (NumberFormatException ignored) {}
		}
	}

	public boolean isHourlyRate(int activityId)
	{
		return activityId >= 0 && activityId < hourlyRateCache.length && hourlyRateCache[activityId];
	}

	public void toggleHourlyRate(TrackedActivity activity)
	{
		if (activity == null) return;

		int id = activity.getId();
		if (id < 0 || id >= hourlyRateCache.length) return;

		hourlyRateCache[id] = !hourlyRateCache[id];

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < hourlyRateCache.length; i++)
		{
			if (hourlyRateCache[i])
			{
				if (sb.length() > 0) sb.append(",");
				sb.append(i);
			}
		}

		configManager.setConfiguration(PluginConstants.CONFIG_GROUP, HOURLY_RATE_CONFIG_KEY, sb.toString());
	}

	public void disableActivity(TrackedActivity activity) {
		if (activity == null || !visibilityCache[activity.getId()]) {
			return;
		}

		visibilityCache[activity.getId()] = false;

		StringBuilder newHiddenString = new StringBuilder();
		for (TrackedActivity act : TrackedActivity.values()) {
			if (!visibilityCache[act.getId()]) {
				if (newHiddenString.length() > 0) newHiddenString.append(",");
				newHiddenString.append(act.getConfigId());
			}
		}

		configManager.setConfiguration(PluginConstants.CONFIG_GROUP, HIDDEN_ACTIVITIES_CONFIG_KEY, newHiddenString.toString());

		// Immediately re-evaluate dynamically
		if (plugin != null) {
			plugin.evaluateConditionalListeners();
		}
	}

	public boolean isKcVisible(int trackedActivityIndex) {
		if (trackedActivityIndex < 0 || trackedActivityIndex > MAX_TRACKING_ID) return false;
		return visibilityCache[trackedActivityIndex];
	}

	public boolean isKcVisible(TrackedActivity trackedActivity) {
		return visibilityCache[trackedActivity.getId()];
	}

	public int getCategorySortOrder(Category category) {
		if (category == null) return 99;
		return categorySortCache[category.ordinal()];
	}

	private void cacheCategorySortOrder()
	{
		for (Category category : Category.values()) {
			categorySortCache[category.ordinal()] = fetchCategorySortOrder(category);
		}
	}

	private int fetchCategorySortOrder(Category category)
	{
		switch (category)
		{
			case BOSSES: return config.sortBosses();
			case CHESTS: return config.sortChests();
			case CLUE: return config.sortClue();
			case SLAYER: return config.sortSlayer();
			case AGILITY: return config.sortAgility();
			case EXPERIENCE: return config.sortExperience();
			case LEVELS: return config.sortLevels();
			case BOLTS: return config.sortBoltProcs();
			case OTHER: return config.sortOther();
			case RAIDS: return config.sortRaids();
			case MINI_GAMES: return config.sortMiniGames();
			case MAGIC_SPELLS: return config.sortMagicSpells();
			case COMBAT: return config.sortCombat();
			case SUPPLIES: return config.sortSupplies();
			case SKILLING: return config.sortSkilling();
			case ACHIEVEMENTS: return config.sortAchievements();
			case SAILING: return config.sortSailing();
			case TERTIARY_DROPS: return config.sortTertiaryDrops();
			case RANDOM_EVENTS: return config.sortRandomEvents();
			default: return 99;
		}
	}

	public void setActivityScore(TrackedActivity activity, float score) {
		if (activity == null) return;
		scoreCache[activity.getId()] = score;

		StringBuilder newScoreString = new StringBuilder();
		for (TrackedActivity act : TrackedActivity.values()) {
			float s = scoreCache[act.getId()];
			if (s != 0f) {
				if (newScoreString.length() > 0) newScoreString.append(",");
				newScoreString.append(act.getConfigId()).append(":").append(s);
			}
		}
		configManager.setConfiguration(PluginConstants.CONFIG_GROUP, ACTIVITY_SCORES_CONFIG_KEY, newScoreString.toString());
	}

	public float getScore(int trackingId) {
		if (trackingId < 0 || trackingId > MAX_TRACKING_ID) return 0f;
		return scoreCache[trackingId];
	}

	public void togglePinScore()
	{
		configManager.setConfiguration(PluginConstants.CONFIG_GROUP, "pinScoreOverlay", !config.pinScoreOverlay());
	}

	public void toggleScoreHourlyRate()
	{
		configManager.setConfiguration(PluginConstants.CONFIG_GROUP, "scoreHourlyRate", !config.scoreHourlyRate());
	}

	public void togglePinTime()
	{
		configManager.setConfiguration(PluginConstants.CONFIG_GROUP, "pinTimeOverlay", !config.pinTimeOverlay());
	}

	public void toggleShowStartTime()
	{
		configManager.setConfiguration(PluginConstants.CONFIG_GROUP, "showStartTime", !config.showStartTime());
	}

	public void toggleShowEndTime()
	{
		configManager.setConfiguration(PluginConstants.CONFIG_GROUP, "showEndTime", !config.showEndTime());
	}

	public void toggleShowNotes()
	{
		configManager.setConfiguration(PluginConstants.CONFIG_GROUP, "showNotes", !config.showNotes());
	}

	public void toggleShowSessionDuration()
	{
		configManager.setConfiguration(PluginConstants.CONFIG_GROUP, "showSessionDuration", !config.showSessionDuration());
	}

	public void setCustomScoreLabel(String label)
	{
		configManager.setConfiguration(PluginConstants.CONFIG_GROUP, "customScoreLabel", label);
	}
}