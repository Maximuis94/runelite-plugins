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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(DataLoggerConfig.CONFIG_GROUP)
public interface DataLoggerConfig extends Config {
	String CONFIG_GROUP = "datalogger";

	// --- Sections ---

	@ConfigSection(
		name = "Grand Exchange",
		description = "Settings for logging Grand Exchange activity",
		position = 0
	)
	String grandExchangeSection = "grandExchange";

	@ConfigSection(
		name = "Colosseum",
		description = "Settings for Fortis Colosseum trial logging",
		position = 1
	)
	String colosseumSection = "colosseum";

	@ConfigSection(
		name = "Colosseum NPCs",
		description = "Toggle logging for specific optional NPCs during trials",
		position = 2
	)
	String colosseumNpcSection = "colosseumNpc";

	@ConfigSection(
		name = "Screenshots",
		description = "Settings for automatic screenshot capturing",
		position = 3
	)
	String screenshotSection = "screenshot";

	// --- Grand Exchange Items ---

	@ConfigItem(
		keyName = "logGrandExchange",
		name = "Log Grand Exchange",
		description = "If enabled, Grand Exchange offers are logged upon completion.",
		position = 0,
		section = grandExchangeSection
	)
	default boolean logGrandExchange() { return true; }

	// --- Colosseum Items ---

	@ConfigItem(
		keyName = "logColosseum",
		name = "Log Colosseum Trials",
		description = "If enabled, Fortis Colosseum trials are logged.",
		position = 0,
		section = colosseumSection
	)
	default boolean logColosseum() { return true; }

	@ConfigItem(
		keyName = "logColosseumCSV",
		name = "Log Colosseum Trials (CSV)",
		description = "If enabled, Fortis Colosseum waves are also logged in a CSV file.",
		position = 1,
		section = colosseumSection
	)
	default boolean logColosseumCSV() { return true; }

	@ConfigItem(
		keyName = "logWaveTimeline",
		name = "Track player and NPC locations every tick during waves.",
		description = "If enabled, a state composed of player- and relevant NPC-locations is generated every game tick and added to a timeline.",
		position = 2,
		section = colosseumSection
	)
	default boolean logWaveTimeline() { return true; }

	@ConfigItem(
		keyName = "logQuiverAsSplinters",
		name = "Log Dizana's quiver (uncharged) Sunfire splinters",
		description = "If enabled, the Dizana's quiver (uncharged) reward is logged as 4,000 Sunfire splinters instead.",
		position = 3,
		section = colosseumSection
	)
	default boolean logQuiverAsSplinters() { return true; }

	// --- Colosseum NPC Optional Items ---

	@ConfigItem(
		keyName = "logFremenniks",
		name = "Log Fremenniks",
		description = "Include Fremennik warband NPC locations in timeline data",
		position = 0,
		section = colosseumNpcSection
	)
	default boolean logFremenniks() { return false; }

	@ConfigItem(
		keyName = "logSolarFlare",
		name = "Log Solar Flares",
		description = "Include Solar flare locations in timeline data",
		position = 1,
		section = colosseumNpcSection
	)
	default boolean logSolarFlare() { return false; }

	@ConfigItem(
		keyName = "logHealingTotem",
		name = "Log Healing Totems",
		description = "Include Healing totem locations in timeline data",
		position = 2,
		section = colosseumNpcSection
	)
	default boolean logHealingTotem() { return false; }

	@ConfigItem(
		keyName = "logBeeSwarm",
		name = "Log Bee Swarms",
		description = "Include Bee Swarm locations in timeline data",
		position = 3,
		section = colosseumNpcSection
	)
	default boolean logBeeSwarm() { return false; }

	@ConfigItem(
		keyName = "logBeamCrystal",
		name = "Log Beam Crystals",
		description = "Include Beam Crystal locations in timeline data",
		position = 4,
		section = colosseumNpcSection
	)
	default boolean logBeamCrystal() { return false; }

	// --- Screenshot Items ---

	@ConfigItem(
		keyName = "screenshotBetweenWaves",
		name = "Colosseum Wave Completion",
		description = "Automatically take a screenshot after completing a wave when the intermission/rewards chest UI is visible",
		position = 0,
		section = screenshotSection
	)
	default boolean screenshotBetweenWaves() { return false; }
}