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

package com.activitycounter;

import static com.activitycounter.PluginConstants.CONFIG_GROUP;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(CONFIG_GROUP)
public interface ActivityCounterConfig extends Config {

	// --- SECTIONS ---

	@ConfigSection(
		name = "General",
		description = "General plugin settings",
		position = 0
	)
	String generalSection = "generalSection";

	@ConfigSection(
		name = "Sidebar panel",
		description = "Sidebar panel UI settings",
		position = 1,
		closedByDefault = true
	)
	String uiSection = "uiSection";

	@ConfigSection(
		name = "Category display order",
		description = "Define the display order of categories in the panel (lower numbers appear first)",
		position = 2,
		closedByDefault = true
	)
	String categorySortingSection = "categorySortingSection";

	@ConfigSection(
		name = "Group filters",
		description = "Allows one to enable/disable certain groups, which will enable/disable tracking completely, reducing CPU load.",
		position = 3,
		closedByDefault = true
	)
	String categoryFilterSection = "categoryFilterSection";

	// --- GENERAL SETTINGS ---

	@ConfigItem(
		keyName = "confirmTerminateSession",
		name = "Confirm session termination",
		description = "If checked, ask for confirmation when attempting to terminate the active session<br>" +
			"to prevent accidentally terminating a session.",
		position = 1,
		section = generalSection
	)
	default boolean confirmTerminateSession() { return false; }

	@ConfigItem(
		keyName = "mergeSlayerTaskCounts ",
		name = "Merge Slayer task counts",
		description = "If checked, show a single, merged slayer task count instead of 3.",
		position = 2,
		section = generalSection
	)
	default boolean mergeSlayerTaskCounts () { return false; }

	// --- UI ---

	@ConfigItem(
		keyName = "showSessionDuration",
		name = "Show session duration",
		description = "If checked, display the duration of the session for the actively tracked session.",
		position = 0,
		section = uiSection
	)
	default boolean showSessionDuration() { return true; }

	@ConfigItem(
		keyName = "showStartTime",
		name = "Show start time",
		description = "If checked, show the session start time",
		position = 1,
		section = uiSection
	)
	default boolean showStartTime() { return true; }

	@ConfigItem(
		keyName = "showEndTime",
		name = "Show end time",
		description = "If checked, show the session end time",
		position = 2,
		section = uiSection
	)
	default boolean showEndTime() { return true; }

	@ConfigItem(
		keyName = "showNotesTextBox",
		name = "Show notes text box",
		description = "If checked, show a text box in which you can add notes",
		position = 3,
		section = uiSection
	)
	default boolean showNotes() { return true; }

	// --- Tracker groups ---



	// --- SORTING ---

	@ConfigItem(
		keyName = "sortBosses",
		name = "Bosses",
		description = "Sort position for Bosses",
		position = 1,
		section = categorySortingSection
	)
	default int sortBosses() { return 1; }

	@ConfigItem(
		keyName = "sortChests",
		name = "Chests",
		description = "Sort position for Chests",
		position = 2,
		section = categorySortingSection
	)
	default int sortChests() { return 2; }

	@ConfigItem(
		keyName = "sortClue",
		name = "Clue Scrolls",
		description = "Sort position for Clue Scrolls",
		position = 3,
		section = categorySortingSection
	)
	default int sortClue() { return 3; }

	@ConfigItem(
		keyName = "sortSlayer",
		name = "Slayer",
		description = "Sort position for Slayer",
		position = 4,
		section = categorySortingSection
	)
	default int sortSlayer() { return 4; }

	@ConfigItem(
		keyName = "sortAgility",
		name = "Agility",
		description = "Sort position for Agility",
		position = 5,
		section = categorySortingSection
	)
	default int sortAgility() { return 5; }

	@ConfigItem(
		keyName = "sortExperience",
		name = "Experience",
		description = "Sort position for Experience",
		position = 6,
		section = categorySortingSection
	)
	default int sortExperience() { return 6; }

	@ConfigItem(
		keyName = "sortLevels",
		name = "Levels",
		description = "Sort position for Levels",
		position = 7,
		section = categorySortingSection
	)
	default int sortLevels() { return 7; }

	@ConfigItem(
		keyName = "sortBoltProcs",
		name = "Bolt procs",
		description = "Sort position for Bolt proc counts",
		position = 8,
		section = categorySortingSection
	)
	default int sortBoltProcs() { return 8; }

	@ConfigItem(
		keyName = "sortOther",
		name = "Other",
		description = "Sort position for Other activities",
		position = 9,
		section = categorySortingSection
	)
	default int sortOther() { return 9; }

	@ConfigItem(
		keyName = "enableBoltProcListener",
		name = "Count enchanted bolt procs",
		description = "If checked, enchanted bolt effect procs are counted.",
		position = 0,
		section = categoryFilterSection
	)
	default boolean enableBoltProcListener() { return true; }



	@ConfigItem(
		keyName="hiddenActivities",
		name="<html>Hidden activities<br>" +
			"All the activities that should be hidden.<br>" +
			"Hiding activities can be done by right-clicking them in the sidebar panel and then selecting the 'hide' option.<br>" +
			"Unhiding can be done by removing the keyword from this list." +
			"</html>",
		description = "The activities that should be excluded from the sidebar panel",
		position = 99
	)
	default String hiddenActivities() { return ""; }
}