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

package com.datalogger.constants;

import com.datalogger.models.enums.GameMode;
import java.io.File;
import net.runelite.client.RuneLite;

/**
 * Constants that are generally applicable
 */
public final class PluginConstants
{
	public static final String CONFIG_GROUP = "datalogger";

	public static final File PLUGIN_ROOT = new File(RuneLite.RUNELITE_DIR, "data-logger");
	public static final File COLOSSEUM_ROOT_DIR = new File(PLUGIN_ROOT, "colosseum");
	public static final File COLOSSEUM_TRIALS_DIR = new File(COLOSSEUM_ROOT_DIR, "trials");
	public static final File ITEM_VAULT_DIR = new File(PLUGIN_ROOT, "items");
	public static final File GRAND_EXCHANGE_DIR = new File(PLUGIN_ROOT, "grand-exchange");
	public static final File INTERNAL_ROOT_DIR = new File(PLUGIN_ROOT, "internal");
	public static final File INTERNAL_VAULT_DIR = new File(INTERNAL_ROOT_DIR, "items");
	public static final File INTERNAL_STASH_DIR = new File(INTERNAL_ROOT_DIR, "stash-units");
	public static final File INTERNAL_COLOSSEUM_DIR = new File(INTERNAL_ROOT_DIR, "colosseum");
	public static final File INTERNAL_COLOSSEUM_TRIAL_HISTORY = new File(INTERNAL_COLOSSEUM_DIR, "trial-history.jsonl");

	private PluginConstants() {}

	/**
	 * The frequency (in seconds) at which live Grand Exchange prices
	 * should be refreshed from the RuneLite ItemManager cache.
	 */
	public static final int ITEM_VALUE_UPDATE_FREQUENCY_SECONDS = 600;
	public static final int WEBHOOK_TEST_COOLDOWN_SECONDS_SUCCESS = 5;

	// ID that is ignored in enum mapping creation
	public static final int IGNORED_ENUM_ID = -1;

	public static final int CURRENT_COLOSSEUM_TRIAL_LOG_VERSION = 1;

	public static final GameMode DEFAULT_GAME_MODE = GameMode.REGULAR;
	public static final String DEFAULT_GAMEMODE_DTO = DEFAULT_GAME_MODE.name();
}