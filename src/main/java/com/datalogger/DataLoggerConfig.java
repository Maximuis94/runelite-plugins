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
		keyName = "logColosseum",
		name = "Log Colosseum Trials (CSV)",
		description = "If enabled, Fortis Colosseum waves are also logged in a CSV file.",
		position = 1,
		section = colosseumSection
	)
	default boolean logColosseumCSV() { return true; }

	@ConfigItem(
		keyName = "screenshotBetweenWaves",
		name = "Screenshot Between Waves",
		description = "Automatically take a screenshot between waves when the UI/chest is opened",
		position = 2,
		section = colosseumSection
	)
	default boolean screenshotBetweenWaves() { return false; }

	@ConfigItem(
		keyName = "logQuiverAsSplinters",
		name = "Log Dizana's quiver (uncharged) Sunfire splinters",
		description = "If enabled, the Dizana's quiver (uncharged) reward is logged as 4,000 Sunfire splinters instead.",
		position = 3,
		section = colosseumSection
	)
	default boolean logQuiverAsSplinters() { return true; }
}