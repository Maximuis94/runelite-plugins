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

/**
 * Centralized constants related to Fortis Colosseum
 */
public final class Colosseum
{
	/**
	 * Widget child IDs
	 */
	public static class Intermission {
		public static final int INTERMISSION_GROUP_ID = 865;
		public static final int[] INTERMISSION_MODIFIER_CHOICES = {15, 16, 17};
		public static final int INTERMISSION_RESULT_CONTAINER = 38;
		public static final int INTERMISSION_NEXT_LOOT_CONTAINER = 39;
		public static final int INTERMISSION_MODIFIER_LIST_CONTAINER = 34;
	}

	/**
	 * Widget child IDs
	 */
	public static class RewardsChest {
		public static final int REWARDS_CHEST_GROUP_ID = 864;
		public static final int REWARDS_CHEST_REWARDS_TAB_CHILD_ID = 17;
		public static final int REWARDS_CHEST_SUMMARY_TAB_CHILD_ID = 7;
		public static final int REWARDS_CHEST_MODIFIER_LIST_CONTAINER_ID = 11;
	}

	public static class Varbit
	{
		public static final int COLOSSEUM_SELECTED_MODIFIER_VARBIT = 9788;

	}

	/**
	 * NPC IDs of relevant Colosseum encounters
	 */
	public static class NPC
	{
		public static final int MINIMUS_NPC_ID = 12808;
		public static final int JAGUAR_WARRIOR_NPC_ID = 12810;
		public static final int SERPENT_SHAMAN_NPC_ID = 12811;
		public static final int MINOTAUR_NPC_ID = 12812;
		public static final int MINOTAUR_RED_FLAG_NPC_ID = 12813;
		public static final int FREMENNIK_RANGED_NPC_ID = 12814;
		public static final int FREMENNIK_MAGE_NPC_ID = 12815;
		public static final int FREMENNIK_MELEE_NPC_ID = 12816;
		public static final int JAVELIN_COLOSSUS_NPC_ID = 12817;
		public static final int MANTICORE_NPC_ID = 12818;
		public static final int SHOCKWAVE_COLOSSUS_NPC_ID = 12819;
		public static final int SOL_HEREDIT_NPC_ID = 12821;
		public static final int BEE_SWARM_NPC_ID = 12823;
		public static final int BOSS_WAVE_BEAM_CRYSTAL = 12824;
		public static final int HEALING_TOTEM_NPC_ID = 12825;
		public static final int SOLAR_FLARE_NPC_ID = 12826;
		public static final int SITTING_SOL_HEREDIT_NPC_ID = 12827;
	}

	/**
	 * NPC IDs of relevant Colosseum encounters
	 */
	public static class Item
	{
		public static final int SUNFIRE_SPLINTERS_ID = 28924;
		public static final int DIZANAS_QUIVER_UNCHARGED_ID = 28947;
	}

	/**
	 * Region IDs for location checks
	 */
	public static class Region
	{
		public static final int COLOSSEUM_REGION_ID = 7216;
		public static final int COLOSSEUM_LOBBY_ID = 7316;
	}

	/**
	 * Chat messages, or snippets thereof
	 */
	public static class Message
	{
		public static final String START_ATTEMPT_MESSAGE = "Minimus|Let me know when you want to begin.";
		public static final String DEATH_MESSAGE = "Oh dear, you are dead!";
		public static final String WAVE_START_PREFIX = "Wave: ";
		public static final String BOSS_WAVE_START_PREFIX = "Sol Heredit jumps down from his seat...";
		public static final String END_ATTEMPT_MESSAGE = "Search the chest nearby to retrieve your earned rewards";
	}
}