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

import java.awt.Color;
import net.runelite.api.gameval.NpcID;

/**
 * Static values used by the plugin
 */
public final class PluginConstants
{
	private PluginConstants() {throw new UnsupportedOperationException("Utility class cannot be instantiated");}

	public static final String PLUGIN_CONFIG_GROUP = "dynamiclineofsight";

	public static final int STAFF_EQUIPMENT_TYPE_ID = 18;
	public static final int STAFF_AUTOCAST_STYLE_INDEX = 3;
	public static final int STAFF_DEFENSIVE_AUTOCAST_STYLE_INDEX = 4;
	public static final int SPELL_CAST_ATTACK_RANGE = 10;

	public static final float DEFAULT_LINE_WIDTH = .2f;

	public static final int DEFAULT_OUTLINE_ALPHA = 200;
	public static final int DEFAULT_FILL_ALPHA = 15;

	public static final Color DEFAULT_COLOR_MELEE = new Color(255, 0, 0);
	public static final Color DEFAULT_COLOR_MELEE_OUTLINE = new Color(255, 0, 0, DEFAULT_OUTLINE_ALPHA);
	public static final Color DEFAULT_COLOR_MELEE_FILL = new Color(255, 0, 0, DEFAULT_FILL_ALPHA);
	
	public static final Color DEFAULT_COLOR_RANGED = new Color(0, 255, 0);
	public static final Color DEFAULT_COLOR_RANGED_OUTLINE = new Color(0, 255, 0, DEFAULT_OUTLINE_ALPHA);
	public static final Color DEFAULT_COLOR_RANGED_FILL = new Color(0, 255, 0, DEFAULT_FILL_ALPHA);
	
	public static final Color DEFAULT_COLOR_MAGIC = new Color(0, 0, 255);
	public static final Color DEFAULT_COLOR_MAGIC_OUTLINE = new Color(0,0, 255, DEFAULT_OUTLINE_ALPHA);
	public static final Color DEFAULT_COLOR_MAGIC_FILL = new Color(0, 0, 255, DEFAULT_FILL_ALPHA);

	public static final Color DEFAULT_COLOR_OTHER = new Color(255, 125, 200);
	public static final Color DEFAULT_COLOR_OTHER_OUTLINE = new Color(255, 125, 200, DEFAULT_OUTLINE_ALPHA);
	public static final Color DEFAULT_COLOR_OTHER_FILL = new Color(255, 125, 200, DEFAULT_FILL_ALPHA);

	public static final Color DEFAULT_COLOR_ACTIVE_WEAPON = new Color(255, 0, 125);
	public static final Color DEFAULT_COLOR_ACTIVE_WEAPON_OUTLINE = new Color(255, 0, 125, DEFAULT_OUTLINE_ALPHA);
	public static final Color DEFAULT_COLOR_ACTIVE_WEAPON_FILL = new Color(255, 0, 125, DEFAULT_FILL_ALPHA);

	public static final Color DEFAULT_COLOR_ACTIVE_WEAPON_SPEC = new Color(100, 0, 100);
	public static final Color DEFAULT_COLOR_ACTIVE_WEAPON_SPEC_OUTLINE = new Color(100, 0, 100, DEFAULT_OUTLINE_ALPHA);
	public static final Color DEFAULT_COLOR_ACTIVE_WEAPON_SPEC_FILL = new Color(100, 0, 100, DEFAULT_FILL_ALPHA);

	public static final int TZ_KIH_A_ID = NpcID.TZHAAR_FIGHTCAVE_SWARM_1A;
	public static final int TZ_KIH_B_ID = NpcID.TZHAAR_FIGHTCAVE_SWARM_1B;
	public static final int TZ_KEK_A_ID = NpcID.TZHAAR_FIGHTCAVE_SWARM_2A;
	public static final int TZ_KEK_B_ID = NpcID.TZHAAR_FIGHTCAVE_SWARM_2B;
	public static final int TZ_KEK_SPAWN_ID = NpcID.TZHAAR_FIGHTCAVE_SWARM_2SPAWN;
	public static final int TOK_XIL_A_ID = NpcID.TZHAAR_FIGHTCAVE_SWARM_3A;
	public static final int TOK_XIL_B_ID = NpcID.TZHAAR_FIGHTCAVE_SWARM_3B;
	public static final int YT_MEJKOT_A_ID = NpcID.TZHAAR_FIGHTCAVE_SWARM_4A;
	public static final int YT_MEJKOT_B_ID = NpcID.TZHAAR_FIGHTCAVE_SWARM_4B;
	public static final int KET_ZEK_A_ID = NpcID.TZHAAR_FIGHTCAVE_SWARM_5A;
	public static final int KET_ZEK_B_ID = NpcID.TZHAAR_FIGHTCAVE_SWARM_5B;
	public static final int TZTOK_JAD_ID = NpcID.TZHAAR_FIGHTCAVE_SWARM_BOSS;
	public static final int YT_HURKOT_ID = NpcID.TZHAAR_FIGHTCAVE_SWARM_BOSS_CLERIC;

	public static final int JAL_NIB_ID = NpcID.INFERNO_NIBBLER;
	public static final int JAL_MEJ_RAH_ID = NpcID.INFERNO_CREATURE_HARPIE;
	public static final int JAL_AK_ID = NpcID.INFERNO_CREATURE_SPLITTER;
	public static final int JAL_AKREK_MEJ_ID = NpcID.INFERNO_CREATURE_SPLITTER_MAGE;
	public static final int JAL_AKREK_XIL_ID = NpcID.INFERNO_CREATURE_SPLITTER_RANGE;
	public static final int JAL_AKREK_KET_ID = NpcID.INFERNO_CREATURE_SPLITTER_MELEE;
	public static final int JAL_IMKOT_ID = NpcID.INFERNO_CREATURE_MELEE;
	public static final int JAL_XIL_ID = NpcID.INFERNO_CREATURE_RANGER;
	public static final int JAL_ZEK_ID = NpcID.INFERNO_CREATURE_MAGER;
	public static final int JALTOK_JAD_ID = NpcID.INFERNO_JAD;
	public static final int JAL_YT_HURKOT_ID = NpcID.INFERNO_JAD_HEALER;
	public static final int JALTOK_JAD_FINAL_ID = NpcID.INFERNO_JAD_FINALWAVE;
	public static final int JAL_YT_HURKOT_FINAL_ID = NpcID.INFERNO_JAD_HEALER_FINALWAVE;
	public static final int TZKAL_ZUK_ID = NpcID.INFERNO_TZKALZUK_PLACEHOLDER;
	public static final int JAL_MEJJAK_ID = NpcID.INFERNO_ZUK_HEALER;

	public static final int CHALLENGE_JALTOK_JAD_ID = NpcID.JAD_CHALLENGE_JAD;
	public static final int CHALLENGE_YT_HURKOT_ID = NpcID.JAD_CHALLENGE_HEALER;

	public static final int JAGUAR_WARRIOR_ID = NpcID.COLOSSEUM_JAGUAR_WARRIOR;
	public static final int SERPENT_SHAMAN_ID = NpcID.COLOSSEUM_STANDARD_MAGER;
	public static final int MINOTAUR_ID = NpcID.COLOSSEUM_MINOTAUR;
	public static final int MINOTAUR_ROUTEFIND_ID = NpcID.COLOSSEUM_MINOTAUR_ROUTEFIND;
	public static final int WARBANDER_RANGED_ID = NpcID.COLOSSEUM_WARBANDER_RANGED_FEMALE;
	public static final int WARBANDER_MAGE_ID = NpcID.COLOSSEUM_WARBANDER_MAGE_MALE;
	public static final int WARBANDER_MELEE_ID = NpcID.COLOSSEUM_WARBANDER_MELEE_MALE;
	public static final int JAVELIN_COLOSSUS_ID = NpcID.COLOSSEUM_JAVELIN_COLOSSUS;
	public static final int MANTICORE_ID = NpcID.COLOSSEUM_MANTICORE;
	public static final int SHOCKWAVE_COLOSSUS_ID = NpcID.COLOSSEUM_SHOCKWAVE_COLOSSUS;
	public static final int SOL_HEREDIT_ID = NpcID.COLOSSEUM_SOL_P1;
	public static final int BEE_SWARM_ID = NpcID.COLOSSEUM_MODIFIER_BEES;
	public static final int HEALING_TOTEM_ID = NpcID.COLOSSEUM_HEALING_TOTEM;
}