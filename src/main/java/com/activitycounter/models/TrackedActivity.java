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

package com.activitycounter.models;

import com.activitycounter.PluginConstants;
import lombok.Getter;

@Getter
public enum TrackedActivity
{

	// --- BOSSES ---
	BRUTUS("Brutus", PluginConstants.ActivityID.BRUTUS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	OBOR("Obor", PluginConstants.ActivityID.OBOR, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	BRYOPHYTA("Bryophyta", PluginConstants.ActivityID.BRYOPHYTA, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	SCURRIUS("Scurrius", PluginConstants.ActivityID.SCURRIUS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	CHAOS_FANATIC("Chaos Fanatic", PluginConstants.ActivityID.CHAOS_FANATIC, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	DERANGED_ARCHAEOLOGIST("Deranged Archaeologist", PluginConstants.ActivityID.DERANGED_ARCHAEOLOGIST, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	CRAZY_ARCHAEOLOGIST("Crazy Archaeologist", PluginConstants.ActivityID.CRAZY_ARCHAEOLOGIST, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	SCORPIA("Scorpia", PluginConstants.ActivityID.SCORPIA, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	GIANT_MOLE("Giant Mole", PluginConstants.ActivityID.GIANT_MOLE, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	SHELLBANE_GRYPHON("Shellbane Gryphon", PluginConstants.ActivityID.SHELLBANE_GRYPHON, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	GROTESQUE_GUARDIANS("Grotesque Guardians", PluginConstants.ActivityID.GROTESQUE_GUARDIANS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	AMOXLIATL("Amoxliatl", PluginConstants.ActivityID.AMOXLIATL, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	CALVARION("Calvar'ion", PluginConstants.ActivityID.CALVARION, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	KING_BLACK_DRAGON("King Black Dragon", PluginConstants.ActivityID.KING_BLACK_DRAGON, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	HESPORI("Hespori", PluginConstants.ActivityID.HESPORI, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	KRAKEN("Kraken", PluginConstants.ActivityID.KRAKEN, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	THERMONUCLEAR_SMOKE_DEVIL("Thermonuclear Smoke Devil", PluginConstants.ActivityID.THERMONUCLEAR_SMOKE_DEVIL, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	SPINDEL("Spindel", PluginConstants.ActivityID.SPINDEL, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	DAGANNOTH_PRIME("Dagannoth Prime", PluginConstants.ActivityID.DAGANNOTH_PRIME, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	DAGANNOTH_REX("Dagannoth Rex", PluginConstants.ActivityID.DAGANNOTH_REX, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	DAGANNOTH_SUPREME("Dagannoth Supreme", PluginConstants.ActivityID.DAGANNOTH_SUPREME, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	CERBERUS("Cerberus", PluginConstants.ActivityID.CERBERUS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	SARACHNIS("Sarachnis", PluginConstants.ActivityID.SARACHNIS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	SKOTIZO("Skotizo", PluginConstants.ActivityID.SKOTIZO, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	ARTIO("Artio", PluginConstants.ActivityID.ARTIO, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	KALPHITE_QUEEN("Kalphite Queen", PluginConstants.ActivityID.KALPHITE_QUEEN, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	ABYSSAL_SIRE("Abyssal Sire", PluginConstants.ActivityID.ABYSSAL_SIRE, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	ROYAL_TITANS("Royal Titans", PluginConstants.ActivityID.ROYAL_TITANS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	ALCHEMICAL_HYDRA("Alchemical Hydra", PluginConstants.ActivityID.ALCHEMICAL_HYDRA, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	VETION("Vet'ion", PluginConstants.ActivityID.VETION, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	VENENATIS("Venenatis", PluginConstants.ActivityID.VENENATIS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	CALLISTO("Callisto", PluginConstants.ActivityID.CALLISTO, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	CHAOS_ELEMENTAL("Chaos Elemental", PluginConstants.ActivityID.CHAOS_ELEMENTAL, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	KREEARRA("Kree'arra", PluginConstants.ActivityID.KREEARRA, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	MAD_ANGEL("Mad Angel", PluginConstants.ActivityID.MAD_ANGEL, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	COMMANDER_ZILYANA("Commander Zilyana", PluginConstants.ActivityID.COMMANDER_ZILYANA, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	GENERAL_GRAARDOR("General Graardor", PluginConstants.ActivityID.GENERAL_GRAARDOR, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	THE_HUEYCOATL("The Hueycoatl", PluginConstants.ActivityID.THE_HUEYCOATL, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	KRIL_TSUTSAROTH("K'ril Tsutsaroth", PluginConstants.ActivityID.KRIL_TSUTSAROTH, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	TZTOK_JAD("TzTok-Jad", PluginConstants.ActivityID.TZTOK_JAD, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	ZULRAH("Zulrah", PluginConstants.ActivityID.ZULRAH, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	VORKATH("Vorkath", PluginConstants.ActivityID.VORKATH, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	PHANTOM_MUSPAH("Phantom Muspah", PluginConstants.ActivityID.PHANTOM_MUSPAH, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	MAGGOT_KING("Maggot King", PluginConstants.ActivityID.MAGGOT_KING, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	DUKE_SUCELLUS("Duke Sucellus", PluginConstants.ActivityID.DUKE_SUCELLUS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	VARDORVIS("Vardorvis", PluginConstants.ActivityID.VARDORVIS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	CORPOREAL_BEAST("Corporeal Beast", PluginConstants.ActivityID.CORPOREAL_BEAST, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	THE_WHISPERER("The Whisperer", PluginConstants.ActivityID.THE_WHISPERER, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	THE_LEVIATHAN("The Leviathan", PluginConstants.ActivityID.THE_LEVIATHAN, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	THE_NIGHTMARE("The Nightmare", PluginConstants.ActivityID.THE_NIGHTMARE, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	ARAXXOR("Araxxor", PluginConstants.ActivityID.ARAXXOR, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	NEX("Nex", PluginConstants.ActivityID.NEX, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	PHOSANIS_NIGHTMARE("Phosani's Nightmare", PluginConstants.ActivityID.PHOSANIS_NIGHTMARE, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	DUKE_SUCELLUS_AWAKENED("Duke Sucellus (Awakened)", PluginConstants.ActivityID.DUKE_SUCELLUS_AWAKENED, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	VARDORVIS_AWAKENED("Vardorvis (Awakened)", PluginConstants.ActivityID.VARDORVIS_AWAKENED, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	THE_WHISPERER_AWAKENED("The Whisperer (Awakened)", PluginConstants.ActivityID.THE_WHISPERER_AWAKENED, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	THE_LEVIATHAN_AWAKENED("The Leviathan (Awakened)", PluginConstants.ActivityID.THE_LEVIATHAN_AWAKENED, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	DEMONIC_BRUTUS("Demonic Brutus", PluginConstants.ActivityID.DEMONIC_BRUTUS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	YAMA("Yama", PluginConstants.ActivityID.YAMA, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	TZKAL_ZUK("TzKal-Zuk", PluginConstants.ActivityID.TZKAL_ZUK, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	SOL_HEREDIT("Sol Heredit", PluginConstants.ActivityID.SOL_HEREDIT, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	COLOSSEUM_WAVES("Colosseum waves", PluginConstants.ActivityID.COLOSSEUM_WAVES, Category.BOSSES, TrackerType.VARPLAYER_VALUE),

	DOOM_OF_MOKHAIOTL_LEVELS("Doom of Mokhaiotl levels", PluginConstants.ActivityID.DOOM_OF_MOKHAIOTL_LEVELS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	DOOM_OF_MOKHAIOTL_LEVEL_1_COMPLETIONS("Doom of Mokhaiotl Level 1", PluginConstants.ActivityID.DOM_LEVEL_1_COMPLETIONS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	DOOM_OF_MOKHAIOTL_LEVEL_2_COMPLETIONS("Doom of Mokhaiotl Level 2", PluginConstants.ActivityID.DOM_LEVEL_2_COMPLETIONS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	DOOM_OF_MOKHAIOTL_LEVEL_3_COMPLETIONS("Doom of Mokhaiotl Level 3", PluginConstants.ActivityID.DOM_LEVEL_3_COMPLETIONS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	DOOM_OF_MOKHAIOTL_LEVEL_4_COMPLETIONS("Doom of Mokhaiotl Level 4", PluginConstants.ActivityID.DOM_LEVEL_4_COMPLETIONS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	DOOM_OF_MOKHAIOTL_LEVEL_5_COMPLETIONS("Doom of Mokhaiotl Level 5", PluginConstants.ActivityID.DOM_LEVEL_5_COMPLETIONS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	DOOM_OF_MOKHAIOTL_LEVEL_6_COMPLETIONS("Doom of Mokhaiotl Level 6", PluginConstants.ActivityID.DOM_LEVEL_6_COMPLETIONS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	DOOM_OF_MOKHAIOTL_LEVEL_7_COMPLETIONS("Doom of Mokhaiotl Level 7", PluginConstants.ActivityID.DOM_LEVEL_7_COMPLETIONS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	DOOM_OF_MOKHAIOTL_LEVEL_8_COMPLETIONS("Doom of Mokhaiotl Level 8", PluginConstants.ActivityID.DOM_LEVEL_8_COMPLETIONS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),
	DOOM_OF_MOKHAIOTL_LEVEL_8_PLUS_COMPLETIONS("Doom of Mokhaiotl Level 8+", PluginConstants.ActivityID.DOM_LEVEL_8_PLUS_COMPLETIONS, Category.BOSSES, TrackerType.VARPLAYER_VALUE),

	// --- CHESTS ---
	BARROWS_CHESTS("Barrows Chests", PluginConstants.ActivityID.BARROWS_CHESTS, Category.CHESTS, TrackerType.VARPLAYER_VALUE),
	CHAMBERS_OF_XERIC("Chambers of Xeric", PluginConstants.ActivityID.CHAMBERS_OF_XERIC, Category.CHESTS, TrackerType.VARPLAYER_VALUE),
	CHAMBERS_OF_XERIC_CHALLENGE_MODE("Chambers of Xeric: Challenge Mode", PluginConstants.ActivityID.CHAMBERS_OF_XERIC_CHALLENGE_MODE, Category.CHESTS, TrackerType.VARPLAYER_VALUE),
	THEATRE_OF_BLOOD("Theatre of Blood", PluginConstants.ActivityID.THEATRE_OF_BLOOD, Category.CHESTS, TrackerType.VARPLAYER_VALUE),
	THEATRE_OF_BLOOD_STORY_MODE("Theatre of Blood: Story Mode", PluginConstants.ActivityID.THEATRE_OF_BLOOD_STORY_MODE, Category.CHESTS, TrackerType.VARPLAYER_VALUE),
	THEATRE_OF_BLOOD_HARD_MODE("Theatre of Blood: Hard Mode", PluginConstants.ActivityID.THEATRE_OF_BLOOD_HARD_MODE, Category.CHESTS, TrackerType.VARPLAYER_VALUE),
	THE_GAUNTLET("The Gauntlet", PluginConstants.ActivityID.THE_GAUNTLET, Category.CHESTS, TrackerType.VARPLAYER_VALUE),
	THE_CORRUPTED_GAUNTLET("The Corrupted Gauntlet", PluginConstants.ActivityID.THE_CORRUPTED_GAUNTLET, Category.CHESTS, TrackerType.VARPLAYER_VALUE),
	TOMBS_OF_AMASCUT("Tombs of Amascut", PluginConstants.ActivityID.TOMBS_OF_AMASCUT, Category.CHESTS, TrackerType.VARPLAYER_VALUE),
	TOMBS_OF_AMASCUT_ENTRY_MODE("Tombs of Amascut: Entry Mode", PluginConstants.ActivityID.TOMBS_OF_AMASCUT_ENTRY_MODE, Category.CHESTS, TrackerType.VARPLAYER_VALUE),
	TOMBS_OF_AMASCUT_EXPERT_MODE("Tombs of Amascut: Expert Mode", PluginConstants.ActivityID.TOMBS_OF_AMASCUT_EXPERT_MODE, Category.CHESTS, TrackerType.VARPLAYER_VALUE),
	PERILOUS_MOONS_CHESTS("Perilous Moons Chests", PluginConstants.ActivityID.PERILOUS_MOONS_CHESTS, Category.CHESTS, TrackerType.VARPLAYER_VALUE),

	// --- OTHER ---
	WINTERTODT("Wintertodt", PluginConstants.ActivityID.WINTERTODT, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	ZALCANO("Zalcano", PluginConstants.ActivityID.ZALCANO, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	TEMPOROSS("Tempoross", PluginConstants.ActivityID.TEMPOROSS, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	GUARDIANS_OF_THE_RIFT("Guardians of the Rift", PluginConstants.ActivityID.GUARDIANS_OF_THE_RIFT, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	JAD_CHALLENGE_1("Jad Challenge 1", PluginConstants.ActivityID.JAD_CHALLENGE_1, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	JAD_CHALLENGE_2("Jad Challenge 2", PluginConstants.ActivityID.JAD_CHALLENGE_2, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	JAD_CHALLENGE_3("Jad Challenge 3", PluginConstants.ActivityID.JAD_CHALLENGE_3, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	JAD_CHALLENGE_4("Jad Challenge 4", PluginConstants.ActivityID.JAD_CHALLENGE_4, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	JAD_CHALLENGE_5("Jad Challenge 5", PluginConstants.ActivityID.JAD_CHALLENGE_5, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	JAD_CHALLENGE_6("Jad Challenge 6", PluginConstants.ActivityID.JAD_CHALLENGE_6, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	GEMSTONE_CRAB("Gemstone Crab", PluginConstants.ActivityID.GEMSTONE_CRAB, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	SOUL_WARS_WINS("Soul Wars wins", PluginConstants.ActivityID.SOUL_WARS_WINS, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	SOUL_WARS_GAMES("Soul Wars games", PluginConstants.ActivityID.SOUL_WARS_GAMES, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	HUNTER_RUMOURS("Hunter Rumours", PluginConstants.ActivityID.HUNTER_RUMOURS, Category.OTHER, TrackerType.CHAT_MESSAGE),
	FARMING_CONTRACTS("Farming Contracts", PluginConstants.ActivityID.FARMING_CONTRACTS, Category.OTHER, TrackerType.CHAT_MESSAGE),
	MAHOGANY_HOMES("Mahogany Homes", PluginConstants.ActivityID.MAHOGANY_HOMES, Category.OTHER, TrackerType.CHAT_MESSAGE),
	NEW_COLLECTIONS_LOGGED("New collections logged", PluginConstants.ActivityID.NEW_COLLECTIONS_LOGGED, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	BIRD_EGGS_OFFERED("Bird eggs offered", PluginConstants.ActivityID.BIRD_EGGS_OFFERED, Category.OTHER, TrackerType.CHAT_MESSAGE),
	PLAYER_DEATHS("Player deaths", PluginConstants.ActivityID.PLAYER_DEATHS, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	PLAYER_KILLS("Player kills", PluginConstants.ActivityID.PLAYER_KILLS, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	MONSTER_KILLS("Monster kills", PluginConstants.ActivityID.MONSTER_KILLS, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	QUESTS("Quests", PluginConstants.ActivityID.QUESTS, Category.OTHER, TrackerType.VARBIT_VALUE),
	QUEST_POINTS("Quest points", PluginConstants.ActivityID.QUEST_POINTS, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	CA_DIARY_TASKS("CA Diary tasks", PluginConstants.ActivityID.CA_DIARY_TASKS, Category.OTHER, TrackerType.CHAT_MESSAGE),
	CA_DIARY_POINTS("CA Diary points", PluginConstants.ActivityID.CA_DIARY_POINTS, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	MIXOLOGY_ORDERS("Mixology orders", PluginConstants.ActivityID.MIXOLOGY_ORDERS, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	MIXOLOGY_AGA_POINTS("Mixology Aga points", PluginConstants.ActivityID.MIXOLOGY_AGA_POINTS, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	MIXOLOGY_LYE_POINTS("Mixology Lye points", PluginConstants.ActivityID.MIXOLOGY_LYE_POINTS, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	MIXOLOGY_MOX_POINTS("Mixology Mox points", PluginConstants.ActivityID.MIXOLOGY_MOX_POINTS, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	MUSIC_TRACKS_UNLOCKED("Music tracks unlocked", PluginConstants.ActivityID.MUSIC_TRACKS_UNLOCKED, Category.OTHER, TrackerType.CHAT_MESSAGE),
	LARRANS_SMALL_CHESTS("Larran's small chests", PluginConstants.ActivityID.LARRANS_SMALL_CHESTS, Category.CHESTS, TrackerType.CHAT_MESSAGE),
	LARRANS_BIG_CHESTS("Larran's big chests", PluginConstants.ActivityID.LARRANS_BIG_CHESTS, Category.CHESTS, TrackerType.CHAT_MESSAGE),
	BRIMSTONE_CHESTS("Brimstone chests", PluginConstants.ActivityID.BRIMSTONE_CHESTS, Category.CHESTS, TrackerType.CHAT_MESSAGE),
	BIRD_HOUSES("Bird houses built", PluginConstants.ActivityID.BIRD_HOUSES, Category.OTHER, TrackerType.CHAT_MESSAGE),
	DAMAGE_DEALT_TO_NPCS("Damage dealt to NPCs", PluginConstants.ActivityID.DAMAGE_DEALT_TO_NPCS, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	SPECIAL_ATTACKS_USED("Special attacks used", PluginConstants.ActivityID.SPECIAL_ATTACKS_USED, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	DAMAGE_TAKEN_FROM_NPCS("Damage taken from NPCs", PluginConstants.ActivityID.DAMAGE_TAKEN_FROM_NPCS, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	FISH_CAUGHT("Fish caught", PluginConstants.ActivityID.FISH_CAUGHT, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	LOGS_CHOPPED("Logs chopped", PluginConstants.ActivityID.LOGS_CHOPPED, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	ORE_MINED("Ore mined", PluginConstants.ActivityID.ORE_MINED, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	POTIONS_SIPPED("Potions sipped", PluginConstants.ActivityID.POTIONS_SIPPED, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	FOOD_EATEN("Food eaten", PluginConstants.ActivityID.FOOD_EATEN, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	TELEPORT_TABLETS_USED("Teleport tablets used", PluginConstants.ActivityID.TELEPORT_TABLETS_USED, Category.OTHER, TrackerType.SOUND_EFFECT),
	SCYTHE_SWIPE("Scythe of Vitur charges", PluginConstants.ActivityID.SCYTHE_ATTACK, Category.OTHER, TrackerType.SOUND_EFFECT),
	COINS_GAINED("Coins gained", PluginConstants.ActivityID.COINS_GAINED, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	COINS_LOST("Coins lost", PluginConstants.ActivityID.COINS_LOST, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	CANNONS_LOST("Cannons lost&found", PluginConstants.ActivityID.CANNONS_LOST, Category.OTHER, TrackerType.CUSTOM),
	CANNONBALLS_FIRED("Cannonballs fired", PluginConstants.ActivityID.CANNONBALLS_FIRED, Category.OTHER, TrackerType.CUSTOM),
	NMZ_POINTS("NMZ points", PluginConstants.ActivityID.NMZ_POINTS, Category.OTHER, TrackerType.CUSTOM),
	PEST_CONTROL_POINTS("Pest control points", PluginConstants.ActivityID.PEST_CONTROL_POINTS, Category.OTHER, TrackerType.CHAT_MESSAGE),
	TITHE_FARM_POINTS("Tithe farm points", PluginConstants.ActivityID.TITHE_FARM_POINTS, Category.OTHER, TrackerType.VARBIT_VALUE),
	GIANTS_FOUNDRY_POINTS("Giant's foundry points", PluginConstants.ActivityID.GIANTS_FOUNDRY_POINTS, Category.OTHER, TrackerType.VARPLAYER_VALUE),
	BA_ATTACKER_POINTS("BA Attacker points", PluginConstants.ActivityID.BA_ATTACKER_POINTS, Category.OTHER, TrackerType.VARBIT_VALUE),
	BA_COLLECTOR_POINTS("BA Collector points", PluginConstants.ActivityID.BA_COLLECTOR_POINTS, Category.OTHER, TrackerType.VARBIT_VALUE),
	BA_DEFENDER_POINTS("BA Defender points", PluginConstants.ActivityID.BA_DEFENDER_POINTS, Category.OTHER, TrackerType.VARBIT_VALUE),
	BA_HEALER_POINTS("BA Healer points", PluginConstants.ActivityID.BA_HEALER_POINTS, Category.OTHER, TrackerType.VARBIT_VALUE),
	VARROCK_MUSEUM_KUDOS("Varrock Museum kudos", PluginConstants.ActivityID.VARROCK_MUSEUM_KUDOS, Category.OTHER, TrackerType.VARBIT_VALUE),

	// --- BOLT PROCS ---
	OPAL_BOLT_PROC("Opal bolt procs", PluginConstants.ActivityID.BOLT_OPAL, Category.BOLTS, TrackerType.CUSTOM),
	SAPPHIRE_BOLT_PROC("Sapphire bolt procs", PluginConstants.ActivityID.BOLT_SAPPHIRE, Category.BOLTS, TrackerType.CUSTOM),
	JADE_BOLT_PROC("Jade bolt procs", PluginConstants.ActivityID.BOLT_JADE, Category.BOLTS, TrackerType.CUSTOM),
	PEARL_BOLT_PROC("Pearl bolt procs", PluginConstants.ActivityID.BOLT_PEARL, Category.BOLTS, TrackerType.CUSTOM),
	EMERALD_BOLT_PROC("Emerald bolt procs", PluginConstants.ActivityID.BOLT_EMERALD, Category.BOLTS, TrackerType.CUSTOM),
	RED_TOPAZ_BOLT_PROC("Red topaz bolt procs", PluginConstants.ActivityID.BOLT_REDTOPAZ, Category.BOLTS, TrackerType.CUSTOM),
	RUBY_BOLT_PROC("Ruby bolt procs", PluginConstants.ActivityID.BOLT_RUBY, Category.BOLTS, TrackerType.CUSTOM),
	DIAMOND_BOLT_PROC("Diamond bolt procs", PluginConstants.ActivityID.BOLT_DIAMOND, Category.BOLTS, TrackerType.CUSTOM),
	DRAGONSTONE_BOLT_PROC("Dragonstone bolt procs", PluginConstants.ActivityID.BOLT_DRAGONSTONE, Category.BOLTS, TrackerType.CUSTOM),
	ONYX_BOLT_PROC("Onyx bolt procs", PluginConstants.ActivityID.BOLT_ONYX, Category.BOLTS, TrackerType.CUSTOM),

	// --- CLUE SCROLLS ---
	COMPLETED_BEGINNER_CLUE("Completed beginner clue", PluginConstants.ActivityID.COMPLETED_BEGINNER_CLUE, Category.CLUE, TrackerType.VARPLAYER_VALUE),
	MISSED_BEGINNER_CLUE("Missed beginner clue", PluginConstants.ActivityID.MISSED_BEGINNER_CLUE, Category.CLUE, TrackerType.CHAT_MESSAGE),
	COMPLETED_EASY_CLUE("Completed easy clue", PluginConstants.ActivityID.COMPLETED_EASY_CLUE, Category.CLUE, TrackerType.VARPLAYER_VALUE),
	MISSED_EASY_CLUE("Missed easy clue", PluginConstants.ActivityID.MISSED_EASY_CLUE, Category.CLUE, TrackerType.CHAT_MESSAGE),
	COMPLETED_MEDIUM_CLUE("Completed medium clue", PluginConstants.ActivityID.COMPLETED_MEDIUM_CLUE, Category.CLUE, TrackerType.VARPLAYER_VALUE),
	MISSED_MEDIUM_CLUE("Missed medium clue", PluginConstants.ActivityID.MISSED_MEDIUM_CLUE, Category.CLUE, TrackerType.CHAT_MESSAGE),
	COMPLETED_HARD_CLUE("Completed hard clue", PluginConstants.ActivityID.COMPLETED_HARD_CLUE, Category.CLUE, TrackerType.VARPLAYER_VALUE),
	MISSED_HARD_CLUE("Missed hard clue", PluginConstants.ActivityID.MISSED_HARD_CLUE, Category.CLUE, TrackerType.CHAT_MESSAGE),
	COMPLETED_ELITE_CLUE("Completed elite clue", PluginConstants.ActivityID.COMPLETED_ELITE_CLUE, Category.CLUE, TrackerType.VARPLAYER_VALUE),
	MISSED_ELITE_CLUE("Missed elite clue", PluginConstants.ActivityID.MISSED_ELITE_CLUE, Category.CLUE, TrackerType.CHAT_MESSAGE),
	COMPLETED_MASTER_CLUE("Completed master clue", PluginConstants.ActivityID.COMPLETED_MASTER_CLUE, Category.CLUE, TrackerType.VARPLAYER_VALUE),
	MIMIC("Mimic", PluginConstants.ActivityID.MIMIC, Category.CLUE, TrackerType.VARPLAYER_VALUE),

	// --- SLAYER ---
	SLAYER_TASKS_OTHER("Slayer tasks (Other)", PluginConstants.ActivityID.SLAYER_TASKS_OTHER, Category.SLAYER, TrackerType.VARBIT_VALUE),
	SLAYER_TASKS_WILDERNESS("Slayer tasks (Wilderness)", PluginConstants.ActivityID.SLAYER_TASKS_WILDERNESS, Category.SLAYER, TrackerType.VARBIT_VALUE),
	SLAYER_TASKS_MORTIMER("Slayer tasks (Mortimer)", PluginConstants.ActivityID.SLAYER_TASKS_MORTIMER, Category.SLAYER, TrackerType.VARPLAYER_VALUE),
	SUPERIOR_SPAWNS("Superior spawns", PluginConstants.ActivityID.SUPERIOR_SPAWNS, Category.SLAYER, TrackerType.CHAT_MESSAGE),
	SLAYER_POINTS("Slayer points", PluginConstants.ActivityID.SLAYER_POINTS, Category.SLAYER, TrackerType.CUSTOM),


	// --- AGILITY ---
	AGILITY_GNOME_STRONGHOLD("Gnome Stronghold Laps", PluginConstants.ActivityID.AGILITY_GNOME_STRONGHOLD, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_SHAYZIEN_LOW("Shayzien Laps (Basic)", PluginConstants.ActivityID.AGILITY_SHAYZIEN_LOW, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_SHAYZIEN_HIGH("Shayzien Laps (Advanced)", PluginConstants.ActivityID.AGILITY_SHAYZIEN_HIGH, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_PENGUIN("Penguin Laps", PluginConstants.ActivityID.AGILITY_PENGUIN, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_BARBARIAN_OUTPOST("Barbarian Outpost Laps", PluginConstants.ActivityID.AGILITY_BARBARIAN_OUTPOST, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_APE_ATOLL("Ape Atoll Laps", PluginConstants.ActivityID.AGILITY_APE_ATOLL, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_WILDERNESS("Wilderness Laps", PluginConstants.ActivityID.AGILITY_WILDERNESS, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_COLOSSAL_WYRM_ADVANCED("Colossal Wyrm Laps (Advanced)", PluginConstants.ActivityID.AGILITY_COLOSSAL_WYRM_ADVANCED, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_COLOSSAL_WYRM_BASIC("Colossal Wyrm Laps (Basic)", PluginConstants.ActivityID.AGILITY_COLOSSAL_WYRM_BASIC, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_WEREWOLF("Werewolf Laps", PluginConstants.ActivityID.AGILITY_WEREWOLF, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_PRIFDDINAS("Prifddinas Laps", PluginConstants.ActivityID.AGILITY_PRIFDDINAS, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_DRAYNOR_ROOFTOP("Draynor Village Rooftop Laps", PluginConstants.ActivityID.AGILITY_DRAYNOR_ROOFTOP, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_AL_KHARID_ROOFTOP("Al Kharid Rooftop Laps", PluginConstants.ActivityID.AGILITY_AL_KHARID_ROOFTOP, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_VARROCK_ROOFTOP("Varrock Rooftop Laps", PluginConstants.ActivityID.AGILITY_VARROCK_ROOFTOP, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_CANIFIS_ROOFTOP("Canifis Rooftop Laps", PluginConstants.ActivityID.AGILITY_CANIFIS_ROOFTOP, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_FALADOR_ROOFTOP("Falador Rooftop Laps", PluginConstants.ActivityID.AGILITY_FALADOR_ROOFTOP, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_SEERS_ROOFTOP("Seers' Village Rooftop Laps", PluginConstants.ActivityID.AGILITY_SEERS_ROOFTOP, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_POLLNIVNEACH_ROOFTOP("Pollnivneach Rooftop Laps", PluginConstants.ActivityID.AGILITY_POLLNIVNEACH_ROOFTOP, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_RELLEKKA_ROOFTOP("Rellekka Rooftop Laps", PluginConstants.ActivityID.AGILITY_RELLEKKA_ROOFTOP, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_ARDOUGNE_ROOFTOP("Ardougne Rooftop Laps", PluginConstants.ActivityID.AGILITY_ARDOUGNE_ROOFTOP, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_PYRAMID("Agility Pyramid Laps", PluginConstants.ActivityID.AGILITY_PYRAMID, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_DORGESH_KAAN("Dorgesh-Kaan Laps", PluginConstants.ActivityID.AGILITY_DORGESH_KAAN, Category.AGILITY, TrackerType.CHAT_MESSAGE),
	AGILITY_BRIMHAVEN("Brimhaven Agility Tickets", PluginConstants.ActivityID.AGILITY_BRIMHAVEN, Category.AGILITY, TrackerType.CHAT_MESSAGE),

	// --- EXPERIENCE ---
	XP_TOTAL("Total XP", PluginConstants.ActivityID.XP_TOTAL, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_ATTACK("Attack XP", PluginConstants.ActivityID.XP_ATTACK, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_DEFENCE("Defence XP", PluginConstants.ActivityID.XP_DEFENCE, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_STRENGTH("Strength XP", PluginConstants.ActivityID.XP_STRENGTH, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_HITPOINTS("Hitpoints XP", PluginConstants.ActivityID.XP_HITPOINTS, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_RANGED("Ranged XP", PluginConstants.ActivityID.XP_RANGED, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_PRAYER("Prayer XP", PluginConstants.ActivityID.XP_PRAYER, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_MAGIC("Magic XP", PluginConstants.ActivityID.XP_MAGIC, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_COOKING("Cooking XP", PluginConstants.ActivityID.XP_COOKING, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_WOODCUTTING("Woodcutting XP", PluginConstants.ActivityID.XP_WOODCUTTING, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_FLETCHING("Fletching XP", PluginConstants.ActivityID.XP_FLETCHING, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_FISHING("Fishing XP", PluginConstants.ActivityID.XP_FISHING, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_FIREMAKING("Firemaking XP", PluginConstants.ActivityID.XP_FIREMAKING, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_CRAFTING("Crafting XP", PluginConstants.ActivityID.XP_CRAFTING, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_SMITHING("Smithing XP", PluginConstants.ActivityID.XP_SMITHING, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_MINING("Mining XP", PluginConstants.ActivityID.XP_MINING, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_HERBLORE("Herblore XP", PluginConstants.ActivityID.XP_HERBLORE, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_AGILITY("Agility XP", PluginConstants.ActivityID.XP_AGILITY, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_THIEVING("Thieving XP", PluginConstants.ActivityID.XP_THIEVING, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_SLAYER("Slayer XP", PluginConstants.ActivityID.XP_SLAYER, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_FARMING("Farming XP", PluginConstants.ActivityID.XP_FARMING, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_RUNECRAFT("Runecraft XP", PluginConstants.ActivityID.XP_RUNECRAFT, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_HUNTER("Hunter XP", PluginConstants.ActivityID.XP_HUNTER, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_CONSTRUCTION("Construction XP", PluginConstants.ActivityID.XP_CONSTRUCTION, Category.EXPERIENCE, TrackerType.STAT_CHANGE),
	XP_SAILING("Sailing XP", PluginConstants.ActivityID.XP_SAILING, Category.EXPERIENCE, TrackerType.STAT_CHANGE),

	// --- LEVELS ---
	LVL_TOTAL("Total Level", PluginConstants.ActivityID.LVL_TOTAL, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_ATTACK("Attack Level", PluginConstants.ActivityID.LVL_ATTACK, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_DEFENCE("Defence Level", PluginConstants.ActivityID.LVL_DEFENCE, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_STRENGTH("Strength Level", PluginConstants.ActivityID.LVL_STRENGTH, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_HITPOINTS("Hitpoints Level", PluginConstants.ActivityID.LVL_HITPOINTS, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_RANGED("Ranged Level", PluginConstants.ActivityID.LVL_RANGED, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_PRAYER("Prayer Level", PluginConstants.ActivityID.LVL_PRAYER, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_MAGIC("Magic Level", PluginConstants.ActivityID.LVL_MAGIC, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_COOKING("Cooking Level", PluginConstants.ActivityID.LVL_COOKING, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_WOODCUTTING("Woodcutting Level", PluginConstants.ActivityID.LVL_WOODCUTTING, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_FLETCHING("Fletching Level", PluginConstants.ActivityID.LVL_FLETCHING, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_FISHING("Fishing Level", PluginConstants.ActivityID.LVL_FISHING, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_FIREMAKING("Firemaking Level", PluginConstants.ActivityID.LVL_FIREMAKING, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_CRAFTING("Crafting Level", PluginConstants.ActivityID.LVL_CRAFTING, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_SMITHING("Smithing Level", PluginConstants.ActivityID.LVL_SMITHING, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_MINING("Mining Level", PluginConstants.ActivityID.LVL_MINING, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_HERBLORE("Herblore Level", PluginConstants.ActivityID.LVL_HERBLORE, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_AGILITY("Agility Level", PluginConstants.ActivityID.LVL_AGILITY, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_THIEVING("Thieving Level", PluginConstants.ActivityID.LVL_THIEVING, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_SLAYER("Slayer Level", PluginConstants.ActivityID.LVL_SLAYER, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_FARMING("Farming Level", PluginConstants.ActivityID.LVL_FARMING, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_RUNECRAFTING("Runecraft Level", PluginConstants.ActivityID.LVL_RUNECRAFTING, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_HUNTER("Hunter Level", PluginConstants.ActivityID.LVL_HUNTER, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_CONSTRUCTION("Construction Level", PluginConstants.ActivityID.LVL_CONSTRUCTION, Category.LEVELS, TrackerType.STAT_CHANGE),
	LVL_SAILING("Sailing Level", PluginConstants.ActivityID.LVL_SAILING, Category.LEVELS, TrackerType.STAT_CHANGE);

	final String name;
	final int id;
	final Category category;
	final String configId;
	final TrackerType trackerType;

	TrackedActivity(String name, int id, Category category, TrackerType trackerType)
	{
		this.name = name;


		if (trackerType == TrackerType.VARBIT_VALUE && id < PluginConstants.VARBIT_OFFSET)
		{
			this.id = id + PluginConstants.VARBIT_OFFSET;
		}
		else
		{
			this.id = id;
		}

		this.category = category;
		this.configId = category.toString().toLowerCase() +
			name.replace(" ", "")
				.replace("(", "")
				.replace(")", "")
				.replace("'", "");
		this.trackerType = trackerType;
	}
}