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

import net.runelite.api.gameval.InventoryID;

/**
 * Global constants used by the plugin
 */
public final class PluginConstants {
	public PluginConstants() {}

	public static final String PLUGIN_NAME = "Activity Counter";
	public static final String CONFIG_GROUP = "activitycounter";
	public static final String PLUGIN_DIR_NAME = "activity-counter";
	public static final String PRESETS_DIR_NAME = "presets";

	public static final String CONFIG_KEY_ACTIVITY_GROUP_BOLT_PROC = "enableBoltProcListener";
	public static final String CONFIG_KEY_ACTIVITY_GROUP_FARMING = "enableFarmingListener";
	public static final String CONFIG_KEY_ACTIVITY_GROUP_GRAND_EXCHANGE = "enableGeListener";
	public static final String CONFIG_KEY_ACTIVITY_GROUP_HERBLORE = "enableHerbloreListener";
	public static final String CONFIG_KEY_ACTIVITY_GROUP_MAGIC_SPELLS = "enableMagicSpellListener";

	public static final int MAX_ITEM_ID = 35000;
	public static final int MAX_DELTA = Integer.MAX_VALUE;

	public static final int EQUIPPED_ID = InventoryID.WORN;
	public static final int INVENTORY_ID = InventoryID.INV;

	public static final class FileName {
		public static final String ICON_FILE_PLAY = "play.png";
		public static final String ICON_FILE_PAUSE = "pause.png";
		public static final String ICON_FILE_SESSION = "osrs-wiki_agility-icon_32x32.png";
		public static final String ICON_FILE_PRESETS = "osrs-wiki_white-cog_32x30.png";
		public static final String ICON_FILE_ABOUT = "icon-no-bg_32x32.png";
	}

	// Magic numbers that represent SoundIDs, most of them manually identified.
	public static final class SoundID {
		public static final int TELEPORT_TABLET = 965;
		public static final int SCYTHE_SLASH = 2522;
		public static final int SCYTHE_CRUSH = 2524;
		public static final int POWERED_STAFF_ATTACK = 178;
		public static final int ALTAR_OFFER_SOUND_ID = 958;
		public static final int STUNNED_SOUND_EFFECT_ID = 2727;
		public static final int FARMING_COMPOST = 2427;
		public static final int SEEDS_PLANTED = 2432;
		public static final int CROPS_PICKED_FRUIT_TREE = 2437;
		public static final int WEEDS_RAKED = 2442;
		public static final int PICKPOCKET_SUCCESS = 2581;
		public static final int LOGS_BURNT = 2596;
	}

	// Pre-defined integer arrays with SoundIDs
	public static final class SoundIDs {
		public static final int[] SCYTHE_SOUND_EFFECTS = new int[]{SoundID.SCYTHE_SLASH, SoundID.SCYTHE_CRUSH};
		public static final int[] POWERED_STAFF_ATTACK = new int[]{SoundID.POWERED_STAFF_ATTACK};
		public static final int[] ETHER_USED = new int[]{2566,2693,178};
		public static final int[] WILDY_SCEPTRE_ATTACK = new int[]{SoundID.POWERED_STAFF_ATTACK};
	}

	public static final class RegionID {
		public static final int MASTERING_MIXOLOGY_BASEMENT = 5521;
		public static final int CRYSTAL_CHEST_REGION_ID = 11573;

	}
}