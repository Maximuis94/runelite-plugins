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

package com.dynamiclos;

import static com.dynamiclos.PluginConstants.*;

/**
 * Static utility class for fetching and maintaining NPC related Line of Sight values of Colosseum, Inferno and Fight Caves foes
 */
public final class NpcLosConstants
{
	private NpcLosConstants() {
		throw new UnsupportedOperationException("Utility class cannot be instantiated");
	}

	private static final int MAX_NPC_ID = 34000;
	private static final int[] NPC_ATTACK_RANGES = new int[MAX_NPC_ID];
	private static final CombatStyle[] NPC_COMBAT_STYLES = new CombatStyle[MAX_NPC_ID];

	static {

		// =========================================
		// COLOSSEUM
		// =========================================
		mapNpc(JAGUAR_WARRIOR_ID, 1, CombatStyle.MELEE);
		mapNpc(SERPENT_SHAMAN_ID, 10, CombatStyle.MAGIC);
		mapNpc(MINOTAUR_ID, 1, CombatStyle.MELEE);
		mapNpc(MINOTAUR_ROUTEFIND_ID, 1, CombatStyle.MELEE);
		mapNpc(WARBANDER_RANGED_ID, 1, CombatStyle.RANGED);
		mapNpc(WARBANDER_MAGE_ID, 1, CombatStyle.MAGIC);
		mapNpc(WARBANDER_MELEE_ID, 1, CombatStyle.MELEE);
		mapNpc(JAVELIN_COLOSSUS_ID, 15, CombatStyle.RANGED);
		mapNpc(MANTICORE_ID, 15, CombatStyle.OTHER);
		mapNpc(SHOCKWAVE_COLOSSUS_ID, 15, CombatStyle.MAGIC);
		mapNpc(SOL_HEREDIT_ID, 1, CombatStyle.OTHER);
		mapNpc(HEALING_TOTEM_ID, 20, CombatStyle.OTHER);

		// =========================================
		// FIGHT CAVES
		// =========================================
		mapNpc(TZ_KIH_A_ID, 1, CombatStyle.MELEE);
		mapNpc(TZ_KIH_B_ID, 1, CombatStyle.MELEE);
		mapNpc(TZ_KEK_A_ID, 1, CombatStyle.MELEE);
		mapNpc(TZ_KEK_B_ID, 1, CombatStyle.MELEE);
		mapNpc(TZ_KEK_SPAWN_ID, 1, CombatStyle.MELEE);
		mapNpc(TOK_XIL_A_ID, 15, CombatStyle.RANGED);
		mapNpc(TOK_XIL_B_ID, 15, CombatStyle.RANGED);
		mapNpc(YT_MEJKOT_A_ID, 1, CombatStyle.MELEE);
		mapNpc(YT_MEJKOT_B_ID, 1, CombatStyle.MELEE);
		mapNpc(KET_ZEK_A_ID, 15, CombatStyle.MAGIC);
		mapNpc(KET_ZEK_B_ID, 15, CombatStyle.MAGIC);
		mapNpc(TZTOK_JAD_ID, 15, CombatStyle.OTHER);
		mapNpc(YT_HURKOT_ID, 1, CombatStyle.MELEE);

		// =========================================
		// INFERNO
		// =========================================
		mapNpc(JAL_NIB_ID, 1, CombatStyle.MELEE);
		mapNpc(JAL_MEJ_RAH_ID, 4, CombatStyle.RANGED);
		mapNpc(JAL_AK_ID, 15, CombatStyle.OTHER);
		mapNpc(JAL_AKREK_MEJ_ID, 15, CombatStyle.MAGIC);
		mapNpc(JAL_AKREK_XIL_ID, 15, CombatStyle.RANGED);
		mapNpc(JAL_AKREK_KET_ID, 1, CombatStyle.MELEE);
		mapNpc(JAL_IMKOT_ID, 1, CombatStyle.MELEE);
		mapNpc(JAL_XIL_ID, 15, CombatStyle.RANGED);
		mapNpc(JAL_ZEK_ID, 15, CombatStyle.MAGIC);
		mapNpc(JALTOK_JAD_ID, 15, CombatStyle.OTHER);
		mapNpc(JAL_YT_HURKOT_ID, 1, CombatStyle.MELEE);
		mapNpc(JALTOK_JAD_FINAL_ID, 15, CombatStyle.OTHER);
		mapNpc(JAL_YT_HURKOT_FINAL_ID, 1, CombatStyle.MELEE);
		mapNpc(TZKAL_ZUK_ID, 0, CombatStyle.OTHER);
		mapNpc(JAL_MEJJAK_ID, 3, CombatStyle.OTHER);

		// Challenges
		mapNpc(CHALLENGE_JALTOK_JAD_ID, 15, CombatStyle.OTHER);
		mapNpc(CHALLENGE_YT_HURKOT_ID, 1, CombatStyle.MELEE);
	}

	/**
	 * Helper method to cleanly populate the arrays in the static block
	 */
	private static void mapNpc(int npcId, int range, CombatStyle style) {
		NPC_ATTACK_RANGES[npcId] = range;
		NPC_COMBAT_STYLES[npcId] = style;
	}

	public static int getNpcAttackRange(int npcId) {
		if (npcId < 0 || npcId >= MAX_NPC_ID) return 0;
		return NPC_ATTACK_RANGES[npcId];
	}

	public static CombatStyle getNpcCombatStyle(int npcId) {
		if (npcId < 0 || npcId >= MAX_NPC_ID) return null;
		return NPC_COMBAT_STYLES[npcId];
	}
}