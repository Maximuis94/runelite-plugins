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

package com.activitycounter.listeners;

import com.activitycounter.ActivityCounterConfig;
import com.activitycounter.PluginConstants;
import com.activitycounter.models.Category;
import com.activitycounter.models.TrackedActivity;
import static com.activitycounter.models.TrackedActivity.MAX_TRACKING_ID;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

/**
 * Class that handles plugin configurations
 */
@Singleton
public class ConfigListener
{
	@Inject
	private ActivityCounterConfig config;

	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	private final static String HIDDEN_ACTIVITIES_CONFIG_KEY = "hiddenActivities";

	private final boolean[] visibilityCache = new boolean[MAX_TRACKING_ID + 1];
	private final int[] categorySortCache = new int[Category.values().length];

	/**
	 * Initializes/rebuilds all configuration caches
	 */
	public void buildAllCaches() {
		parseHiddenActivities();
		cacheCategorySortOrder();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals(PluginConstants.CONFIG_GROUP)) return;

		String key = event.getKey();

		if (key.equals(HIDDEN_ACTIVITIES_CONFIG_KEY)) {
			parseHiddenActivities();
		} else if (key.startsWith("sort")) {
			cacheCategorySortOrder();
		}
	}

	/**
	 * Parses hiddenActivities config and converts it into an array of booleans that indicate what should (not) be shown
	 */
	private void parseHiddenActivities() {
		String hiddenStr = config.hiddenActivities();

		if (hiddenStr == null || hiddenStr.trim().isEmpty()) {
			Arrays.fill(visibilityCache, true);
			return;
		}

		Set<String> hiddenActivitySet = Arrays.stream(hiddenStr.split(","))
			.map(String::trim)
			.collect(Collectors.toSet());

		for (TrackedActivity activity : TrackedActivity.values()) {
			visibilityCache[activity.getId()] = !hiddenActivitySet.contains(activity.getConfigId());
		}
	}

	/**
	 * Hides an activity by updating the array cache instantly and saving it to the configManager.
	 */
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
	}

	public boolean isKcVisible(int trackedActivityIndex) {
		if (trackedActivityIndex < 0 || trackedActivityIndex > MAX_TRACKING_ID) return false;
		return visibilityCache[trackedActivityIndex];
	}

	public boolean isKcVisible(TrackedActivity trackedActivity) {
		return visibilityCache[trackedActivity.getId()];
	}

	/**
	 * Fetch the O(1) cached category sort order.
	 */
	public int getCategorySortOrder(Category category) {
		if (category == null) return 99;
		return categorySortCache[category.ordinal()];
	}

	/**
	 * Determine and cache the order in which categories are to be sorted in advance
	 */
	private void cacheCategorySortOrder()
	{
		for (Category category : Category.values()) {
			categorySortCache[category.ordinal()] = fetchCategorySortOrder(category);
		}
	}

	/**
	 * Internal method to pull from the config object.
	 */
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
}