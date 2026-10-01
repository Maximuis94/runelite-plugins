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
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

/**
 * Global constants used by the plugin
 */
public final class PluginConstants {
	public PluginConstants() {}

	public static final String PLUGIN_NAME = "Activity Counter";
	public static final String CONFIG_GROUP = "activitycounter";
	public static final String PLUGIN_DIR_NAME = "activity-counter";

	public static final int VARBIT_OFFSET = 100000;
	public static final int SOUND_EFFECT_OFFSET = 200000;
	public static final int SPOT_ANIM_OFFSET = 300000;

	public static final int EQUIPPED_ITEM_CONTAINER_ID = InventoryID.WORN;

	public static final class SoundID {
		public static final int TELEPORT_TABLET = 965;
		public static final int SCYTHE_SLASH = 2522;
		public static final int SCYTHE_CRUSH = 2524;
		public static final int ALTAR_OFFER_SOUND_ID = 958;
		public static final int STUNNED_SOUND_EFFECT_ID = 2727;
	}


	public static final class ActivityID {

		// Existing Agility Constants
		public static final int AGILITY_GNOME_STRONGHOLD = -1001;
		public static final int AGILITY_PENGUIN = -1003;
		public static final int AGILITY_BARBARIAN_OUTPOST = -1004;
		public static final int AGILITY_APE_ATOLL = -1005;
		public static final int AGILITY_WILDERNESS = -1006;
		public static final int AGILITY_COLOSSAL_WYRM_ADVANCED = -1007;
		public static final int AGILITY_COLOSSAL_WYRM_BASIC = -1024;
		public static final int AGILITY_WEREWOLF = -1008;
		public static final int AGILITY_PRIFDDINAS = -1009;
		public static final int AGILITY_DRAYNOR_ROOFTOP = -1010;
		public static final int AGILITY_AL_KHARID_ROOFTOP = -1011;
		public static final int AGILITY_VARROCK_ROOFTOP = -1012;
		public static final int AGILITY_CANIFIS_ROOFTOP = -1013;
		public static final int AGILITY_FALADOR_ROOFTOP = -1014;
		public static final int AGILITY_SEERS_ROOFTOP = -1015;
		public static final int AGILITY_POLLNIVNEACH_ROOFTOP = -1016;
		public static final int AGILITY_RELLEKKA_ROOFTOP = -1017;
		public static final int AGILITY_ARDOUGNE_ROOFTOP = -1018;
		public static final int AGILITY_PYRAMID = -1019;
		public static final int AGILITY_DORGESH_KAAN = -1020;
		public static final int AGILITY_BRIMHAVEN = -1021;
		public static final int AGILITY_SHAYZIEN_LOW = -1022;
		public static final int AGILITY_SHAYZIEN_HIGH = -1023;

		// Bosses
		public static final int BRUTUS = VarPlayerID.TOTAL_COWBOSS_KILLS;
		public static final int OBOR = VarPlayerID.TOTAL_HILLGIANT_BOSS_KILLS;
		public static final int BRYOPHYTA = VarPlayerID.TOTAL_BRYOPHYTA_KILLS;
		public static final int SCURRIUS = VarPlayerID.TOTAL_RAT_BOSS_KILLS;
		public static final int CHAOS_FANATIC = VarPlayerID.TOTAL_CHAOSFANATIC_KILLS;
		public static final int DERANGED_ARCHAEOLOGIST = VarPlayerID.TOTAL_DERANGEDARCHAEOLOGIST_KILLS;
		public static final int CRAZY_ARCHAEOLOGIST = VarPlayerID.TOTAL_CRAZYARCHAEOLOGIST_KILLS;
		public static final int SCORPIA = VarPlayerID.TOTAL_SCORPIA_KILLS;
		public static final int GIANT_MOLE = VarPlayerID.TOTAL_MOLE_KILLS;
		public static final int SHELLBANE_GRYPHON = VarPlayerID.TOTAL_GRYPHON_BOSS_KILLS;
		public static final int GROTESQUE_GUARDIANS = VarPlayerID.TOTAL_GARGBOSS_KILLS;
		public static final int AMOXLIATL = VarPlayerID.TOTAL_AMOXLIATL_KILLS;
		public static final int CALVARION = VarPlayerID.TOTAL_CALVARION_KILLS;
		public static final int KING_BLACK_DRAGON = VarPlayerID.TOTAL_KBD_KILLS;
		public static final int HESPORI = VarPlayerID.TOTAL_HESPORI_KILLS;
		public static final int KRAKEN = VarPlayerID.TOTAL_KRAKEN_BOSS_KILLS;
		public static final int THERMONUCLEAR_SMOKE_DEVIL = VarPlayerID.TOTAL_THERMY_KILLS;
		public static final int SPINDEL = VarPlayerID.TOTAL_SPINDEL_KILLS;
		public static final int DAGANNOTH_PRIME = VarPlayerID.TOTAL_PRIME_KILLS;
		public static final int DAGANNOTH_REX = VarPlayerID.TOTAL_REX_KILLS;
		public static final int DAGANNOTH_SUPREME = VarPlayerID.TOTAL_SUPREME_KILLS;
		public static final int CERBERUS = VarPlayerID.TOTAL_CERBERUS_KILLS;
		public static final int SARACHNIS = VarPlayerID.TOTAL_SARACHNIS_KILLS;
		public static final int SKOTIZO = VarPlayerID.TOTAL_CATA_BOSS_KILLS;
		public static final int ARTIO = VarPlayerID.TOTAL_ARTIO_KILLS;
		public static final int KALPHITE_QUEEN = VarPlayerID.TOTAL_KALPHITE_KILLS;
		public static final int ABYSSAL_SIRE = VarPlayerID.TOTAL_ABYSSALSIRE_KILLS;
		public static final int ROYAL_TITANS = VarPlayerID.TOTAL_ROYAL_TITAN_KILLS;
		public static final int ALCHEMICAL_HYDRA = VarPlayerID.TOTAL_HYDRABOSS_KILLS;
		public static final int VETION = VarPlayerID.TOTAL_VETION_KILLS;
		public static final int VENENATIS = VarPlayerID.TOTAL_VENENATIS_KILLS;
		public static final int CALLISTO = VarPlayerID.TOTAL_CALLISTO_KILLS;
		public static final int CHAOS_ELEMENTAL = VarPlayerID.TOTAL_CHAOSELE_KILLS;
		public static final int KREEARRA = VarPlayerID.TOTAL_ARMADYL_KILLS;
		public static final int MAD_ANGEL = VarPlayerID.TOTAL_MAD_ANGEL_KILLS;
		public static final int COMMANDER_ZILYANA = VarPlayerID.TOTAL_SARADOMIN_KILLS;
		public static final int GENERAL_GRAARDOR = VarPlayerID.TOTAL_BANDOS_KILLS;
		public static final int THE_HUEYCOATL = VarPlayerID.TOTAL_HUEY_KILLS;
		public static final int KRIL_TSUTSAROTH = VarPlayerID.TOTAL_ZAMORAK_KILLS;
		public static final int TZTOK_JAD = VarPlayerID.TOTAL_JAD_KILLS;
		public static final int ZULRAH = VarPlayerID.TOTAL_SNAKEBOSS_KILLS;
		public static final int VORKATH = VarPlayerID.TOTAL_VORKATH_KILLS;
		public static final int PHANTOM_MUSPAH = VarPlayerID.TOTAL_MUSPAH_KILLS;
		public static final int MAGGOT_KING = VarPlayerID.TOTAL_MAGGOT_KING_KILLS;
		public static final int DUKE_SUCELLUS = VarPlayerID.TOTAL_DUKE_SUCELLUS_KILLS;
		public static final int VARDORVIS = VarPlayerID.TOTAL_VARDORVIS_KILLS;
		public static final int CORPOREAL_BEAST = VarPlayerID.TOTAL_CORP_KILLS;
		public static final int THE_WHISPERER = VarPlayerID.TOTAL_WHISPERER_KILLS;
		public static final int THE_LEVIATHAN = VarPlayerID.TOTAL_LEVIATHAN_KILLS;
		public static final int THE_NIGHTMARE = VarPlayerID.TOTAL_NIGHTMARE_KILLS;
		public static final int ARAXXOR = VarPlayerID.TOTAL_ARAXXOR_KILLS;
		public static final int NEX = VarPlayerID.TOTAL_NEX_KILLS;
		public static final int PHOSANIS_NIGHTMARE = VarPlayerID.TOTAL_NIGHTMARE_CHALLENGE_KILLS;
		public static final int DUKE_SUCELLUS_AWAKENED = VarPlayerID.TOTAL_DUKE_SUCELLUS_AWAKENED_KILLS;
		public static final int VARDORVIS_AWAKENED = VarPlayerID.TOTAL_VARDORVIS_AWAKENED_KILLS;
		public static final int THE_WHISPERER_AWAKENED = VarPlayerID.TOTAL_WHISPERER_AWAKENED_KILLS;
		public static final int THE_LEVIATHAN_AWAKENED = VarPlayerID.TOTAL_LEVIATHAN_AWAKENED_KILLS;
		public static final int DEMONIC_BRUTUS = VarPlayerID.TOTAL_COWBOSS_HARDMODE_KILLS;
		public static final int YAMA = VarPlayerID.TOTAL_YAMA_KILLS;
		public static final int TZKAL_ZUK = VarPlayerID.TOTAL_ZUK_KILLS;
		public static final int SOL_HEREDIT = VarPlayerID.TOTAL_SOL_KILLS;

		public static final int DOOM_OF_MOKHAIOTL_LEVELS = VarPlayerID.TOTAL_DOM_LEVELS;
		public static final int DOM_LEVEL_1_COMPLETIONS = VarPlayerID.DOM_LEVEL_1_COMPLETIONS;
		public static final int DOM_LEVEL_2_COMPLETIONS = VarPlayerID.DOM_LEVEL_2_COMPLETIONS;
		public static final int DOM_LEVEL_3_COMPLETIONS = VarPlayerID.DOM_LEVEL_3_COMPLETIONS;
		public static final int DOM_LEVEL_4_COMPLETIONS = VarPlayerID.DOM_LEVEL_4_COMPLETIONS;
		public static final int DOM_LEVEL_5_COMPLETIONS = VarPlayerID.DOM_LEVEL_5_COMPLETIONS;
		public static final int DOM_LEVEL_6_COMPLETIONS = VarPlayerID.DOM_LEVEL_6_COMPLETIONS;
		public static final int DOM_LEVEL_7_COMPLETIONS = VarPlayerID.DOM_LEVEL_7_COMPLETIONS;
		public static final int DOM_LEVEL_8_COMPLETIONS = VarPlayerID.DOM_LEVEL_8_COMPLETIONS;
		public static final int DOM_LEVEL_8_PLUS_COMPLETIONS = VarPlayerID.DOM_LEVEL_8_PLUS_COMPLETIONS;

		// Chests Looted
		public static final int BARROWS_CHESTS = VarPlayerID.TOTAL_BARROWS_CHESTS;
		public static final int CHAMBERS_OF_XERIC = VarPlayerID.TOTAL_COMPLETED_XERICCHAMBERS;
		public static final int CHAMBERS_OF_XERIC_CHALLENGE_MODE = VarPlayerID.TOTAL_COMPLETED_XERICCHAMBERS_CHALLENGE;
		public static final int THEATRE_OF_BLOOD = VarPlayerID.TOTAL_COMPLETED_THEATREOFBLOOD;
		public static final int THEATRE_OF_BLOOD_STORY_MODE = VarPlayerID.TOTAL_COMPLETED_THEATREOFBLOOD_STORY;
		public static final int THEATRE_OF_BLOOD_HARD_MODE = VarPlayerID.TOTAL_COMPLETED_THEATREOFBLOOD_HARD;
		public static final int THE_GAUNTLET = VarPlayerID.TOTAL_COMPLETED_GAUNTLET;
		public static final int THE_CORRUPTED_GAUNTLET = VarPlayerID.TOTAL_COMPLETED_GAUNTLET_HM;
		public static final int TOMBS_OF_AMASCUT = VarPlayerID.TOTAL_COMPLETED_TOMBSOFAMASCUT;
		public static final int TOMBS_OF_AMASCUT_ENTRY_MODE = VarPlayerID.TOTAL_COMPLETED_TOMBSOFAMASCUT_ENTRY;
		public static final int TOMBS_OF_AMASCUT_EXPERT_MODE = VarPlayerID.TOTAL_COMPLETED_TOMBSOFAMASCUT_EXPERT;
		public static final int PERILOUS_MOONS_CHESTS = VarPlayerID.TOTAL_PMOON_CHESTS;

		// Other (Minigames & Misc)
		public static final int WINTERTODT = VarPlayerID.TOTAL_WINTERTODT_KILLS;
		public static final int ZALCANO = VarPlayerID.TOTAL_ZALCANO_KILLS;
		public static final int TEMPOROSS = VarPlayerID.TOTAL_TEMPOROSS_KILLS;
		public static final int GUARDIANS_OF_THE_RIFT = VarPlayerID.TOTAL_GOTR_KILLS;
		public static final int JAD_CHALLENGE_1 = VarPlayerID.JAD_CHALLENGE_1_COMPLETIONS;
		public static final int JAD_CHALLENGE_2 = VarPlayerID.JAD_CHALLENGE_2_COMPLETIONS;
		public static final int JAD_CHALLENGE_3 = VarPlayerID.JAD_CHALLENGE_3_COMPLETIONS;
		public static final int JAD_CHALLENGE_4 = VarPlayerID.JAD_CHALLENGE_4_COMPLETIONS;
		public static final int JAD_CHALLENGE_5 = VarPlayerID.JAD_CHALLENGE_5_COMPLETIONS;
		public static final int JAD_CHALLENGE_6 = VarPlayerID.JAD_CHALLENGE_6_COMPLETIONS;
		public static final int COLOSSEUM_WAVES = VarPlayerID.TOTAL_COLOSSEUM_WAVES_COMPLETED;
		public static final int GEMSTONE_CRAB = VarPlayerID.TOTAL_GEMSTONE_CRAB_KILLS;
		public static final int SOUL_WARS_WINS = VarPlayerID.SOUL_WARS_TOTAL_WINS;
		public static final int SOUL_WARS_GAMES = VarPlayerID.SOUL_WARS_TOTAL_GAMES;

		public static final int HUNTER_RUMOURS = -2000;
		public static final int FARMING_CONTRACTS = -3000;
		public static final int MAHOGANY_HOMES = -4000;



		// Pseudo-IDs for Skill Experience (Negative to prevent collisions)
		public static final int XP_ATTACK = -5000;
		public static final int XP_DEFENCE = -5001;
		public static final int XP_STRENGTH = -5002;
		public static final int XP_HITPOINTS = -5003;
		public static final int XP_RANGED = -5004;
		public static final int XP_PRAYER = -5005;
		public static final int XP_MAGIC = -5006;
		public static final int XP_COOKING = -5007;
		public static final int XP_WOODCUTTING = -5008;
		public static final int XP_FLETCHING = -5009;
		public static final int XP_FISHING = -5010;
		public static final int XP_FIREMAKING = -5011;
		public static final int XP_CRAFTING = -5012;
		public static final int XP_SMITHING = -5013;
		public static final int XP_MINING = -5014;
		public static final int XP_HERBLORE = -5015;
		public static final int XP_AGILITY = -5016;
		public static final int XP_THIEVING = -5017;
		public static final int XP_SLAYER = -5018;
		public static final int XP_FARMING = -5019;
		public static final int XP_RUNECRAFT = -5020;
		public static final int XP_HUNTER = -5021;
		public static final int XP_CONSTRUCTION = -5022;
		public static final int XP_TOTAL = -5023;
		public static final int XP_SAILING = -5024;

		// Pseudo-IDs for Skill Levels (Negative to prevent collisions)
		public static final int LVL_TOTAL = -6000;
		public static final int LVL_ATTACK = -6001;
		public static final int LVL_DEFENCE = -6002;
		public static final int LVL_STRENGTH = -6003;
		public static final int LVL_HITPOINTS = -6004;
		public static final int LVL_RANGED = -6005;
		public static final int LVL_PRAYER = -6006;
		public static final int LVL_MAGIC = -6007;
		public static final int LVL_COOKING = -6008;
		public static final int LVL_WOODCUTTING = -6009;
		public static final int LVL_FLETCHING = -6010;
		public static final int LVL_FISHING = -6011;
		public static final int LVL_FIREMAKING = -6012;
		public static final int LVL_CRAFTING = -6013;
		public static final int LVL_SMITHING = -6014;
		public static final int LVL_MINING = -6015;
		public static final int LVL_HERBLORE = -6016;
		public static final int LVL_AGILITY = -6017;
		public static final int LVL_THIEVING = -6018;
		public static final int LVL_SLAYER = -6019;
		public static final int LVL_FARMING = -6020;
		public static final int LVL_RUNECRAFTING = -6021;
		public static final int LVL_HUNTER = -6022;
		public static final int LVL_CONSTRUCTION = -6023;
		public static final int LVL_SAILING = -6024;

		public static final int NEW_COLLECTIONS_LOGGED = VarPlayerID.COLLECTION_COUNT;
		public static final int BIRD_EGGS_OFFERED = -7001;
		public static final int PLAYER_DEATHS = VarPlayerID.TRACKING_DEATHS;
		public static final int PLAYER_KILLS = VarPlayerID.TRACKING_PLAYERS_KILLED;
		public static final int MONSTER_KILLS = VarPlayerID.TRACKING_MONSTERS_KILLED;
		public static final int QUESTS = VarbitID.QUESTS_COMPLETED_COUNT;
		public static final int QUEST_POINTS = VarPlayerID.QP;
		public static final int CA_DIARY_TASKS = -7002;
		public static final int CA_DIARY_POINTS = VarPlayerID.CA_GENERAL3;
		public static final int MIXOLOGY_ORDERS = VarPlayerID.TOTAL_MIXOLOGY_ORDERS;
		public static final int MIXOLOGY_AGA_POINTS  = VarPlayerID.MIXOLOGY_AGA_POINTS ;
		public static final int MIXOLOGY_LYE_POINTS  = VarPlayerID.MIXOLOGY_LYE_POINTS;
		public static final int MIXOLOGY_MOX_POINTS = VarPlayerID.MIXOLOGY_MOX_POINTS;
		public static final int MUSIC_TRACKS_UNLOCKED = -7004;
		public static final int LARRANS_SMALL_CHESTS = -7005;
		public static final int LARRANS_BIG_CHESTS = -7006;
		public static final int BRIMSTONE_CHESTS = -7007;
		public static final int BIRD_HOUSES = -7008;
		public static final int DAMAGE_DEALT_TO_NPCS = VarPlayerID.TRACKING_DAMAGE_DEALT_TO_NPCS;
		public static final int SPECIAL_ATTACKS_USED = VarPlayerID.TRACKING_SPECIAL_ATTACKS_USED;
		public static final int DAMAGE_TAKEN_FROM_NPCS = VarPlayerID.TRACKING_DAMAGE_TAKEN_FROM_NPCS;
		public static final int FISH_CAUGHT = VarPlayerID.TRACKING_FISH_CAUGHT;
		public static final int LOGS_CHOPPED = VarPlayerID.TRACKING_LOGS_CHOPPED;
		public static final int ORE_MINED = VarPlayerID.TRACKING_ORE_MINED;
		public static final int POTIONS_SIPPED = VarPlayerID.TRACKING_POTIONS_SIPPED;
		public static final int FOOD_EATEN = VarPlayerID.TRACKING_FOOD_EATEN;
		public static final int TELEPORT_TABLETS_USED = 965;
		public static final int SCYTHE_ATTACK = -7215;
		public static final int COINS_GAINED = VarPlayerID.TRACKING_COINS_GAINED;
		public static final int COINS_LOST = VarPlayerID.TRACKING_COINS_LOST;
		public static final int CANNONS_LOST = -7214;
		public static final int CANNONBALLS_FIRED = -7015;
		public static final int NMZ_POINTS = -7016;
		public static final int SLAYER_POINTS = -7017;
		public static final int PEST_CONTROL_POINTS = -7018;
		public static final int TITHE_FARM_POINTS = VarbitID.HOSIDIUS_TITHE_REWARDPOINTS;
		public static final int ZOMBIE_PIRATE_CHESTS = -7019;
		public static final int PICKPOCKET_SUCCESS = 2581;
		public static final int PICKPOCKET_FAIL = -7020;

		public static final int GIANTS_FOUNDRY_POINTS = VarPlayerID.GIANTS_FOUNDRY_REWARD_SHOP_POINTS;
		public static final int BA_ATTACKER_POINTS = VarbitID.BARBASSAULT_POINTS_ATTACKER_BASE;
		public static final int BA_COLLECTOR_POINTS = VarbitID.BARBASSAULT_POINTS_COLLECTOR_BASE;
		public static final int BA_DEFENDER_POINTS = VarbitID.BARBASSAULT_POINTS_DEFENDER_BASE;
		public static final int BA_HEALER_POINTS = VarbitID.BARBASSAULT_POINTS_HEALER_BASE;
		public static final int VARROCK_MUSEUM_KUDOS = VarbitID.VM_KUDOS;

		// A pile of bones is sacrificed, but not really
		public static final int SACRIFICES_SPARED = -7030;

		// Aaand it's gone
		public static final int SACRIFICES_MADE = -7031;
		public static final int SACRIFICES_MADE_POH = -7032;

		// Bolts
		public static final int BOLT_OPAL = -7100;
		public static final int BOLT_JADE = -7101;
		public static final int BOLT_REDTOPAZ = -7102;
		public static final int BOLT_SAPPHIRE = -7103;
		public static final int BOLT_EMERALD = -7104;
		public static final int BOLT_PEARL = -7105;
		public static final int BOLT_RUBY = -7106;
		public static final int BOLT_DIAMOND = -7107;
		public static final int BOLT_DRAGONSTONE = -7108;
		public static final int BOLT_ONYX = -7109;

		// --- SPELLS ---

		// Regular spellbook
		public static final int BOLT_GOLD = -7110;




		// Ancient spellbook



		// Lunar spellbook




		// Arceeus spellbook



		// Clue Scrolls
		public static final int COMPLETED_BEGINNER_CLUE = VarPlayerID.COMPLETED_CLUES5;
		public static final int MISSED_BEGINNER_CLUE = -7010;
		public static final int COMPLETED_EASY_CLUE = VarPlayerID.COMPLETED_CLUES;
		public static final int MISSED_EASY_CLUE = -7011;
		public static final int COMPLETED_MEDIUM_CLUE = VarPlayerID.COMPLETED_CLUES1;
		public static final int MISSED_MEDIUM_CLUE = -7012;
		public static final int COMPLETED_HARD_CLUE = VarPlayerID.COMPLETED_CLUES2;
		public static final int MISSED_HARD_CLUE = -7013;
		public static final int COMPLETED_ELITE_CLUE = VarPlayerID.COMPLETED_CLUES3;
		public static final int MISSED_ELITE_CLUE = -7014;
		public static final int COMPLETED_MASTER_CLUE = VarPlayerID.COMPLETED_CLUES4;
		public static final int MIMIC = VarPlayerID.TOTAL_MIMIC_KILLS;

		// Slayer Tasks
		public static final int SLAYER_TASKS_OTHER = VarbitID.SLAYER_TASKS_COMPLETED;
		public static final int SLAYER_TASKS_WILDERNESS = VarbitID.SLAYER_WILDERNESS_TASKS_COMPLETED;
		public static final int SLAYER_TASKS_MORTIMER = VarPlayerID.SLAYER_MORTIMER_TASKS_COMPLETED;
		public static final int SUPERIOR_SPAWNS = -7000;
	}
}