/*
 * Copyright (c) 2026, maximuis94 <https://github.com/maximuis94>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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
package com.datalogger;

import com.datalogger.models.enums.ScreenshotFormat;
import com.datalogger.models.enums.TimestampFormat;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;


@ConfigGroup(DataLoggerConfig.CONFIG_GROUP)
public interface DataLoggerConfig extends Config {
	String CONFIG_GROUP = "datalogger";

	// --- Sections ---

	@ConfigSection(
		name = "Item loggers",
		description = "Settings for logging Grand Exchange activity",
		position = 0,
		closedByDefault = true
	)
	String ITEM_LOGGER_SECTION = "itemLogger";

	@ConfigSection(
		name = "Colosseum",
		description = "Settings for Fortis Colosseum trial logging",
		position = 1,
	closedByDefault = true
	)
	String COLOSSEUM_SECTION = "colosseum";

	@ConfigSection(
		name = "Colosseum timeline logger",
		description = "Toggle to track player and NPC data during Colosseum waves",
		position = 2,
		closedByDefault = true
	)
	String COLOSSEUM_TIMELINE_SECTION = "colosseumTimeline";

	@ConfigSection(
		name = "Screenshots",
		description = "Settings for automatic screenshot capturing",
		position = 3,
		closedByDefault = true
	)
	String SCREENSHOT_SECTION = "screenshot";

	// --- Grand Exchange Items ---

	@ConfigItem(
		keyName = "logGrandExchange",
		name = "Log Grand Exchange",
		description = "If enabled, Grand Exchange offers are logged upon completion.",
		position = 0,
		section = ITEM_LOGGER_SECTION
	)
	default boolean logGrandExchange() { return true; }

	@ConfigItem(
		keyName = "logGrandExchangeCSV",
		name = "Log Grand Exchange (CSV)",
		description = "If enabled, Grand Exchange offers are added to a CSV log upon completion.",
		position = 1,
		section = ITEM_LOGGER_SECTION
	)
	default boolean logGrandExchangeCSV() { return true; }

	@ConfigItem(
		keyName = "logGrandExchangeJSON",
		name = "Log Grand Exchange (JSON)",
		description = "If enabled, a copy of the internal ledger JSON is created after updating it.",
		position = 2,
		section = ITEM_LOGGER_SECTION
	)
	default boolean logGrandExchangeJSON() { return true; }

	@ConfigItem(
		keyName = "logItemVault",
		name = "Log Item vaults",
		description = "If enabled, Item vaults (bank/seed vault) are parsed and saved when interacting with them.",
		position = 3,
		section = ITEM_LOGGER_SECTION
	)
	default boolean logItemVault() { return true; }

	@ConfigItem(
		keyName = "logItemVaultCSV",
		name = "Log Item Vaults (CSV)",
		description = "If enabled, a copy of the internal (aggregated) vaults are created as CSV files.",
		position = 4,
		section = ITEM_LOGGER_SECTION
	)
	default boolean logItemVaultCSV() { return true; }

	@ConfigItem(
		keyName = "logItemVaultJSON",
		name = "Log Item vaults (JSON)",
		description = "If enabled, a copy of the internal (aggregated) vaults are created as JSON files.",
		position = 5,
		section = ITEM_LOGGER_SECTION
	)
	default boolean logItemVaultJSON() { return true; }

	@ConfigItem(
		keyName = "skipItemVaultAccountList",
		name = "Exclude accounts",
		description = "Accounts that should not be included by the Item Vault logger. Accounts should be separated using ','; e.g. ACCOUNT_NAME,OTHER_ACCOUNT_NAME",
		position = 6,
		section = ITEM_LOGGER_SECTION
	)
	default String skipItemVaultAccountList() { return ""; }

	// --- Colosseum Items ---

	@ConfigItem(
		keyName = "logColosseum",
		name = "Log Colosseum Trials",
		description = "If enabled, Fortis Colosseum trials are logged.",
		position = 0,
		section = COLOSSEUM_SECTION
	)
	default boolean logColosseum() { return true; }


	@ConfigItem(
		keyName = "logColosseumCSV",
		name = "Log Colosseum Trials (CSV)",
		description = "If enabled, Fortis Colosseum waves are also logged in a CSV file.",
		position = 1,
		section = COLOSSEUM_SECTION
	)
	default boolean logColosseumCSV() { return true; }

	@ConfigItem(
		keyName = "logQuiverAsSplinters",
		name = "Log quiver reward as splinters",
		description = "If enabled, the Dizana's quiver (uncharged) reward is logged as 4,000 Sunfire splinters instead.",
		position = 2,
		section = COLOSSEUM_SECTION
	)
	default boolean logQuiverAsSplinters() { return false; }

	@ConfigItem(
		keyName = "colosseumTag",
		name = "Tag",
		description = "A custom tag that will be assigned to logged entries (e.g. to distinguish between specific setups)",
		position = 3,
		section = COLOSSEUM_SECTION
	)
	default String colosseumTag() { return ""; }

	@ConfigItem(
		keyName = "autoMergeWaveLogs",
		name = "Auto merge wave logs",
		description = "Automatically merge all wave log files after an attempt is completed.",
		position = 4,
		section = COLOSSEUM_SECTION
	)
	default boolean autoMergeWaveLogs() { return false; }

	@ConfigItem(
		keyName = "trackSupplies",
		name = "Track supply/ammo consumption",
		description = "Track supplies and ammo consumed during a Colosseum attempt",
		position = 5,
		section = COLOSSEUM_SECTION
	)
	default boolean trackSupplies() { return true; }

	// --- Colosseum NPC Optional Items ---

	@ConfigItem(
		keyName = "logWaveTimeline",
		name = "Log Colosseum wave timeline",
		description = "If enabled, player- and NPC- data is logged every game tick during waves and added to a timeline.",
		position = 0,
		section = COLOSSEUM_TIMELINE_SECTION
	)
	default boolean logWaveTimeline() { return true; }

	@ConfigItem(
		keyName = "logTimestamp",
		name = "Log Timestamp",
		description = "Select the format of the timestamp added to the timeline data, if any.",
		position = 1,
		section = COLOSSEUM_TIMELINE_SECTION
	)
	default TimestampFormat logTimestamp() {
		return TimestampFormat.NONE;
	}

	@ConfigItem(
		keyName = "logFremenniks",
		name = "Track Fremenniks",
		description = "Include Fremennik warband NPC data in the timeline",
		position = 2,
		section = COLOSSEUM_TIMELINE_SECTION
	)
	default boolean logFremenniks() { return false; }

	@ConfigItem(
		keyName = "logSolarFlare",
		name = "Track Solar Flares",
		description = "Include Solarflare locations in timeline data",
		position = 3,
		section = COLOSSEUM_TIMELINE_SECTION
	)
	default boolean logSolarFlare() { return false; }

	@ConfigItem(
		keyName = "logHealingTotem",
		name = "Track Healing Totems",
		description = "Include Healing totem locations in timeline data",
		position = 4,
		section = COLOSSEUM_TIMELINE_SECTION
	)
	default boolean logHealingTotem() { return false; }

	@ConfigItem(
		keyName = "logBeeSwarm",
		name = "Track Bee Swarms",
		description = "Include Bee Swarm locations in timeline data",
		position = 5,
		section = COLOSSEUM_TIMELINE_SECTION
	)
	default boolean logBeeSwarm() { return false; }

	@ConfigItem(
		keyName = "logBeamCrystal",
		name = "Track Beam Crystals",
		description = "Include Beam Crystal locations during wave 12 in timeline data",
		position = 6,
		section = COLOSSEUM_TIMELINE_SECTION
	)
	default boolean logBeamCrystal() { return false; }

	// --- Screenshot Items ---

	@ConfigItem(
		keyName = "screenshotBetweenWaves",
		name = "Colosseum Wave Completion",
		description = "Automatically take a screenshot after completing a wave when the intermission/rewards chest UI is visible",
		position = 0,
		section = SCREENSHOT_SECTION
	)
	default boolean screenshotBetweenWaves() { return false; }
	@ConfigItem(
		keyName = "screenshotFormat",
		name = "Screenshot format",
		description = "Select the file format for screenshots. PNG is lossless quality, JPEG reduces file size.",
		position = 1,
		section = SCREENSHOT_SECTION
	)
	default ScreenshotFormat screenshotFormat() {
		return ScreenshotFormat.PNG;
	}
}