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
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public enum TrackedActivity {

	// --- BOSSES (1000 - 1499) (500 spaces for boss releases) ---
	BRUTUS(1001, "Brutus", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.BRUTUS),
	OBOR(1002, "Obor", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.OBOR),
	BRYOPHYTA(1003, "Bryophyta", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.BRYOPHYTA),
	SCURRIUS(1004, "Scurrius", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.SCURRIUS),
	CHAOS_FANATIC(1005, "Chaos Fanatic", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.CHAOS_FANATIC),
	DERANGED_ARCHAEOLOGIST(1006, "Deranged Archaeologist", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DERANGED_ARCHAEOLOGIST),
	CRAZY_ARCHAEOLOGIST(1007, "Crazy Archaeologist", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.CRAZY_ARCHAEOLOGIST),
	SCORPIA(1008, "Scorpia", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.SCORPIA),
	GIANT_MOLE(1009, "Giant Mole", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.GIANT_MOLE),
	SHELLBANE_GRYPHON(1010, "Shellbane Gryphon", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.SHELLBANE_GRYPHON),
	GROTESQUE_GUARDIANS(1011, "Grotesque Guardians", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.GROTESQUE_GUARDIANS),
	AMOXLIATL(1012, "Amoxliatl", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.AMOXLIATL),
	CALVARION(1013, "Calvar'ion", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.CALVARION),
	KING_BLACK_DRAGON(1014, "King Black Dragon", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.KING_BLACK_DRAGON),
	HESPORI(1015, "Hespori", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.HESPORI),
	KRAKEN(1016, "Kraken", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.KRAKEN),
	THERMONUCLEAR_SMOKE_DEVIL(1017, "Thermonuclear Smoke Devil", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.THERMONUCLEAR_SMOKE_DEVIL),
	SPINDEL(1018, "Spindel", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.SPINDEL),
	DAGANNOTH_PRIME(1019, "Dagannoth Prime", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DAGANNOTH_PRIME),
	DAGANNOTH_REX(1020, "Dagannoth Rex", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DAGANNOTH_REX),
	DAGANNOTH_SUPREME(1021, "Dagannoth Supreme", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DAGANNOTH_SUPREME),
	CERBERUS(1022, "Cerberus", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.CERBERUS),
	SARACHNIS(1023, "Sarachnis", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.SARACHNIS),
	SKOTIZO(1024, "Skotizo", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.SKOTIZO),
	ARTIO(1025, "Artio", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.ARTIO),
	KALPHITE_QUEEN(1026, "Kalphite Queen", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.KALPHITE_QUEEN),
	ABYSSAL_SIRE(1027, "Abyssal Sire", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.ABYSSAL_SIRE),
	ROYAL_TITANS(1028, "Royal Titans", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.ROYAL_TITANS),
	ALCHEMICAL_HYDRA(1029, "Alchemical Hydra", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.ALCHEMICAL_HYDRA),
	VETION(1030, "Vet'ion", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.VETION),
	VENENATIS(1031, "Venenatis", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.VENENATIS),
	CALLISTO(1032, "Callisto", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.CALLISTO),
	CHAOS_ELEMENTAL(1033, "Chaos Elemental", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.CHAOS_ELEMENTAL),
	KREEARRA(1034, "Kree'arra", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.KREEARRA),
	MAD_ANGEL(1035, "Mad Angel", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.MAD_ANGEL),
	COMMANDER_ZILYANA(1036, "Commander Zilyana", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.COMMANDER_ZILYANA),
	GENERAL_GRAARDOR(1037, "General Graardor", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.GENERAL_GRAARDOR),
	THE_HUEYCOATL(1038, "The Hueycoatl", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.THE_HUEYCOATL),
	KRIL_TSUTSAROTH(1039, "K'ril Tsutsaroth", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.KRIL_TSUTSAROTH),
	TZTOK_JAD(1040, "TzTok-Jad", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.TZTOK_JAD),
	ZULRAH(1041, "Zulrah", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.ZULRAH),
	VORKATH(1042, "Vorkath", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.VORKATH),
	PHANTOM_MUSPAH(1043, "Phantom Muspah", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.PHANTOM_MUSPAH),
	MAGGOT_KING(1044, "Maggot King", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.MAGGOT_KING),
	DUKE_SUCELLUS(1045, "Duke Sucellus", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DUKE_SUCELLUS),
	VARDORVIS(1046, "Vardorvis", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.VARDORVIS),
	CORPOREAL_BEAST(1047, "Corporeal Beast", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.CORPOREAL_BEAST),
	THE_WHISPERER(1048, "The Whisperer", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.THE_WHISPERER),
	THE_LEVIATHAN(1049, "The Leviathan", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.THE_LEVIATHAN),
	THE_NIGHTMARE(1050, "The Nightmare", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.THE_NIGHTMARE),
	ARAXXOR(1051, "Araxxor", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.ARAXXOR),
	NEX(1052, "Nex", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.NEX),
	PHOSANIS_NIGHTMARE(1053, "Phosani's Nightmare", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.PHOSANIS_NIGHTMARE),
	DUKE_SUCELLUS_AWAKENED(1054, "Duke Sucellus (Awakened)", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DUKE_SUCELLUS_AWAKENED),
	VARDORVIS_AWAKENED(1055, "Vardorvis (Awakened)", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.VARDORVIS_AWAKENED),
	THE_WHISPERER_AWAKENED(1056, "The Whisperer (Awakened)", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.THE_WHISPERER_AWAKENED),
	THE_LEVIATHAN_AWAKENED(1057, "The Leviathan (Awakened)", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.THE_LEVIATHAN_AWAKENED),
	DEMONIC_BRUTUS(1058, "Demonic Brutus", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DEMONIC_BRUTUS),
	YAMA(1059, "Yama", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.YAMA),
	TZKAL_ZUK(1060, "TzKal-Zuk", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.TZKAL_ZUK),
	SOL_HEREDIT(1061, "Sol Heredit", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.SOL_HEREDIT),
	COLOSSEUM_WAVES(1062, "Colosseum waves", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.COLOSSEUM_WAVES),
	DOOM_OF_MOKHAIOTL_LEVELS(1063, "Doom of Mokhaiotl levels", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DOOM_OF_MOKHAIOTL_LEVELS),
	DOOM_OF_MOKHAIOTL_LEVEL_1_COMPLETIONS(1064, "Doom of Mokhaiotl Level 1", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DOM_LEVEL_1_COMPLETIONS),
	DOOM_OF_MOKHAIOTL_LEVEL_2_COMPLETIONS(1065, "Doom of Mokhaiotl Level 2", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DOM_LEVEL_2_COMPLETIONS),
	DOOM_OF_MOKHAIOTL_LEVEL_3_COMPLETIONS(1066, "Doom of Mokhaiotl Level 3", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DOM_LEVEL_3_COMPLETIONS),
	DOOM_OF_MOKHAIOTL_LEVEL_4_COMPLETIONS(1067, "Doom of Mokhaiotl Level 4", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DOM_LEVEL_4_COMPLETIONS),
	DOOM_OF_MOKHAIOTL_LEVEL_5_COMPLETIONS(1068, "Doom of Mokhaiotl Level 5", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DOM_LEVEL_5_COMPLETIONS),
	DOOM_OF_MOKHAIOTL_LEVEL_6_COMPLETIONS(1069, "Doom of Mokhaiotl Level 6", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DOM_LEVEL_6_COMPLETIONS),
	DOOM_OF_MOKHAIOTL_LEVEL_7_COMPLETIONS(1070, "Doom of Mokhaiotl Level 7", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DOM_LEVEL_7_COMPLETIONS),
	DOOM_OF_MOKHAIOTL_LEVEL_8_COMPLETIONS(1071, "Doom of Mokhaiotl Level 8", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DOM_LEVEL_8_COMPLETIONS),
	DOOM_OF_MOKHAIOTL_LEVEL_8_PLUS_COMPLETIONS(1072, "Doom of Mokhaiotl Level 8+", Category.BOSSES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DOM_LEVEL_8_PLUS_COMPLETIONS),

	// --- CHESTS (1500 - 1599) ---
	BARROWS_CHESTS(1501, "Barrows Chests", Category.CHESTS, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.BARROWS_CHESTS),
	PERILOUS_MOONS_CHESTS(1502, "Perilous Moons Chests", Category.CHESTS, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.PERILOUS_MOONS_CHESTS),
	LARRANS_SMALL_CHESTS(1503, "Larran's small chests", Category.CHESTS, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.LARRANS_SMALL_CHESTS),
	LARRANS_BIG_CHESTS(1504, "Larran's big chests", Category.CHESTS, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.LARRANS_BIG_CHESTS),
	BRIMSTONE_CHESTS(1505, "Brimstone chests", Category.CHESTS, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.BRIMSTONE_CHESTS),

	// --- RAIDS (1600 - 1699) ---
	CHAMBERS_OF_XERIC(1601, "Chambers of Xeric", Category.RAIDS, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.CHAMBERS_OF_XERIC),
	CHAMBERS_OF_XERIC_CHALLENGE_MODE(1602, "Chambers of Xeric: Challenge Mode", Category.RAIDS, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.CHAMBERS_OF_XERIC_CHALLENGE_MODE),
	THEATRE_OF_BLOOD(1603, "Theatre of Blood", Category.RAIDS, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.THEATRE_OF_BLOOD),
	THEATRE_OF_BLOOD_STORY_MODE(1604, "Theatre of Blood: Story Mode", Category.RAIDS, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.THEATRE_OF_BLOOD_STORY_MODE),
	THEATRE_OF_BLOOD_HARD_MODE(1605, "Theatre of Blood: Hard Mode", Category.RAIDS, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.THEATRE_OF_BLOOD_HARD_MODE),
	THE_GAUNTLET(1606, "The Gauntlet", Category.RAIDS, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.THE_GAUNTLET),
	THE_CORRUPTED_GAUNTLET(1607, "The Corrupted Gauntlet", Category.RAIDS, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.THE_CORRUPTED_GAUNTLET),
	TOMBS_OF_AMASCUT(1608, "Tombs of Amascut", Category.RAIDS, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.TOMBS_OF_AMASCUT),
	TOMBS_OF_AMASCUT_ENTRY_MODE(1609, "Tombs of Amascut: Entry Mode", Category.RAIDS, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.TOMBS_OF_AMASCUT_ENTRY_MODE),
	TOMBS_OF_AMASCUT_EXPERT_MODE(1610, "Tombs of Amascut: Expert Mode", Category.RAIDS, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.TOMBS_OF_AMASCUT_EXPERT_MODE),

	// --- SLAYER (1700 - 1799) ---
	SLAYER_TASKS_OTHER(1701, "Slayer tasks (Other)", Category.SLAYER, TrackerType.VARBIT_VALUE, PluginConstants.ActivityID.SLAYER_TASKS_OTHER),
	SLAYER_TASKS_WILDERNESS(1702, "Slayer tasks (Wilderness)", Category.SLAYER, TrackerType.VARBIT_VALUE, PluginConstants.ActivityID.SLAYER_TASKS_WILDERNESS),
	SLAYER_TASKS_MORTIMER(1703, "Slayer tasks (Mortimer)", Category.SLAYER, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.SLAYER_TASKS_MORTIMER),
	SUPERIOR_SPAWNS(1704, "Superior spawns", Category.SLAYER, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.SUPERIOR_SPAWNS),
	SLAYER_POINTS(1705, "Slayer points", Category.SLAYER, TrackerType.CUSTOM, PluginConstants.ActivityID.SLAYER_POINTS),

	// --- AGILITY (1800 - 1899) ---
	AGILITY_GNOME_STRONGHOLD(1801, "Gnome Stronghold Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_GNOME_STRONGHOLD),
	AGILITY_SHAYZIEN_LOW(1802, "Shayzien Laps (Basic)", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_SHAYZIEN_LOW),
	AGILITY_SHAYZIEN_HIGH(1803, "Shayzien Laps (Advanced)", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_SHAYZIEN_HIGH),
	AGILITY_PENGUIN(1804, "Penguin Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_PENGUIN),
	AGILITY_BARBARIAN_OUTPOST(1805, "Barbarian Outpost Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_BARBARIAN_OUTPOST),
	AGILITY_APE_ATOLL(1806, "Ape Atoll Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_APE_ATOLL),
	AGILITY_WILDERNESS(1807, "Wilderness Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_WILDERNESS),
	AGILITY_COLOSSAL_WYRM_ADVANCED(1808, "Colossal Wyrm Laps (Advanced)", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_COLOSSAL_WYRM_ADVANCED),
	AGILITY_COLOSSAL_WYRM_BASIC(1809, "Colossal Wyrm Laps (Basic)", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_COLOSSAL_WYRM_BASIC),
	AGILITY_WEREWOLF(1810, "Werewolf Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_WEREWOLF),
	AGILITY_PRIFDDINAS(1811, "Prifddinas Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_PRIFDDINAS),
	AGILITY_DRAYNOR_ROOFTOP(1812, "Draynor Village Rooftop Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_DRAYNOR_ROOFTOP),
	AGILITY_AL_KHARID_ROOFTOP(1813, "Al Kharid Rooftop Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_AL_KHARID_ROOFTOP),
	AGILITY_VARROCK_ROOFTOP(1814, "Varrock Rooftop Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_VARROCK_ROOFTOP),
	AGILITY_CANIFIS_ROOFTOP(1815, "Canifis Rooftop Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_CANIFIS_ROOFTOP),
	AGILITY_FALADOR_ROOFTOP(1816, "Falador Rooftop Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_FALADOR_ROOFTOP),
	AGILITY_SEERS_ROOFTOP(1817, "Seers' Village Rooftop Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_SEERS_ROOFTOP),
	AGILITY_POLLNIVNEACH_ROOFTOP(1818, "Pollnivneach Rooftop Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_POLLNIVNEACH_ROOFTOP),
	AGILITY_RELLEKKA_ROOFTOP(1819, "Rellekka Rooftop Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_RELLEKKA_ROOFTOP),
	AGILITY_ARDOUGNE_ROOFTOP(1820, "Ardougne Rooftop Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_ARDOUGNE_ROOFTOP),
	AGILITY_PYRAMID(1821, "Agility Pyramid Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_PYRAMID),
	AGILITY_DORGESH_KAAN(1822, "Dorgesh-Kaan Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_DORGESH_KAAN),
	AGILITY_BRIMHAVEN(1823, "Brimhaven Agility Tickets", Category.AGILITY, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.AGILITY_BRIMHAVEN),

	// --- Skill-level related (1900 - 2099) ---
	XP_TOTAL(1901, "Total XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_TOTAL),
	XP_ATTACK(1902, "Attack XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_ATTACK),
	XP_DEFENCE(1903, "Defence XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_DEFENCE),
	XP_STRENGTH(1904, "Strength XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_STRENGTH),
	XP_HITPOINTS(1905, "Hitpoints XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_HITPOINTS),
	XP_RANGED(1906, "Ranged XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_RANGED),
	XP_PRAYER(1907, "Prayer XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_PRAYER),
	XP_MAGIC(1908, "Magic XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_MAGIC),
	XP_COOKING(1909, "Cooking XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_COOKING),
	XP_WOODCUTTING(1910, "Woodcutting XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_WOODCUTTING),
	XP_FLETCHING(1911, "Fletching XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_FLETCHING),
	XP_FISHING(1912, "Fishing XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_FISHING),
	XP_FIREMAKING(1913, "Firemaking XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_FIREMAKING),
	XP_CRAFTING(1914, "Crafting XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_CRAFTING),
	XP_SMITHING(1915, "Smithing XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_SMITHING),
	XP_MINING(1916, "Mining XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_MINING),
	XP_HERBLORE(1917, "Herblore XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_HERBLORE),
	XP_AGILITY(1918, "Agility XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_AGILITY),
	XP_THIEVING(1919, "Thieving XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_THIEVING),
	XP_SLAYER(1920, "Slayer XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_SLAYER),
	XP_FARMING(1921, "Farming XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_FARMING),
	XP_RUNECRAFT(1922, "Runecraft XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_RUNECRAFT),
	XP_HUNTER(1923, "Hunter XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_HUNTER),
	XP_CONSTRUCTION(1924, "Construction XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_CONSTRUCTION),
	XP_SAILING(1925, "Sailing XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.XP_SAILING),

	LVL_TOTAL(1951, "Total Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_TOTAL),
	LVL_ATTACK(1952, "Attack Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_ATTACK),
	LVL_DEFENCE(1953, "Defence Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_DEFENCE),
	LVL_STRENGTH(1954, "Strength Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_STRENGTH),
	LVL_HITPOINTS(1955, "Hitpoints Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_HITPOINTS),
	LVL_RANGED(1956, "Ranged Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_RANGED),
	LVL_PRAYER(1957, "Prayer Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_PRAYER),
	LVL_MAGIC(1958, "Magic Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_MAGIC),
	LVL_COOKING(1959, "Cooking Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_COOKING),
	LVL_WOODCUTTING(1960, "Woodcutting Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_WOODCUTTING),
	LVL_FLETCHING(1961, "Fletching Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_FLETCHING),
	LVL_FISHING(1962, "Fishing Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_FISHING),
	LVL_FIREMAKING(1963, "Firemaking Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_FIREMAKING),
	LVL_CRAFTING(1964, "Crafting Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_CRAFTING),
	LVL_SMITHING(1965, "Smithing Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_SMITHING),
	LVL_MINING(1966, "Mining Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_MINING),
	LVL_HERBLORE(1967, "Herblore Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_HERBLORE),
	LVL_AGILITY(1968, "Agility Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_AGILITY),
	LVL_THIEVING(1969, "Thieving Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_THIEVING),
	LVL_SLAYER(1970, "Slayer Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_SLAYER),
	LVL_FARMING(1971, "Farming Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_FARMING),
	LVL_RUNECRAFTING(1972, "Runecraft Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_RUNECRAFTING),
	LVL_HUNTER(1973, "Hunter Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_HUNTER),
	LVL_CONSTRUCTION(1974, "Construction Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_CONSTRUCTION),
	LVL_SAILING(1975, "Sailing Level", Category.LEVELS, TrackerType.STAT_CHANGE, PluginConstants.ActivityID.LVL_SAILING),

	// --- CLUES (2100 - 2199) ---
	COMPLETED_BEGINNER_CLUE(2101, "Completed beginner clue", Category.CLUE, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.COMPLETED_BEGINNER_CLUE),
	MISSED_BEGINNER_CLUE(2102, "Missed beginner clue", Category.CLUE, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.MISSED_BEGINNER_CLUE),
	COMPLETED_EASY_CLUE(2103, "Completed easy clue", Category.CLUE, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.COMPLETED_EASY_CLUE),
	MISSED_EASY_CLUE(2104, "Missed easy clue", Category.CLUE, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.MISSED_EASY_CLUE),
	COMPLETED_MEDIUM_CLUE(2105, "Completed medium clue", Category.CLUE, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.COMPLETED_MEDIUM_CLUE),
	MISSED_MEDIUM_CLUE(2106, "Missed medium clue", Category.CLUE, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.MISSED_MEDIUM_CLUE),
	COMPLETED_HARD_CLUE(2107, "Completed hard clue", Category.CLUE, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.COMPLETED_HARD_CLUE),
	MISSED_HARD_CLUE(2108, "Missed hard clue", Category.CLUE, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.MISSED_HARD_CLUE),
	COMPLETED_ELITE_CLUE(2109, "Completed elite clue", Category.CLUE, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.COMPLETED_ELITE_CLUE),
	MISSED_ELITE_CLUE(2110, "Missed elite clue", Category.CLUE, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.MISSED_ELITE_CLUE),
	COMPLETED_MASTER_CLUE(2111, "Completed master clue", Category.CLUE, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.COMPLETED_MASTER_CLUE),
	MIMIC(2112, "Mimic", Category.CLUE, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.MIMIC),

	// --- MINI GAMES (2200 - 2299) ---
	BA_ATTACKER_POINTS(2201, "BA Attacker points", Category.MINI_GAMES, TrackerType.VARBIT_VALUE, PluginConstants.ActivityID.BA_ATTACKER_POINTS),
	BA_COLLECTOR_POINTS(2202, "BA Collector points", Category.MINI_GAMES, TrackerType.VARBIT_VALUE, PluginConstants.ActivityID.BA_COLLECTOR_POINTS),
	BA_DEFENDER_POINTS(2203, "BA Defender points", Category.MINI_GAMES, TrackerType.VARBIT_VALUE, PluginConstants.ActivityID.BA_DEFENDER_POINTS),
	BA_HEALER_POINTS(2204, "BA Healer points", Category.MINI_GAMES, TrackerType.VARBIT_VALUE, PluginConstants.ActivityID.BA_HEALER_POINTS),
	JAD_CHALLENGE_1(2205, "Jad Challenge 1", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.JAD_CHALLENGE_1),
	JAD_CHALLENGE_2(2206, "Jad Challenge 2", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.JAD_CHALLENGE_2),
	JAD_CHALLENGE_3(2207, "Jad Challenge 3", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.JAD_CHALLENGE_3),
	JAD_CHALLENGE_4(2208, "Jad Challenge 4", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.JAD_CHALLENGE_4),
	JAD_CHALLENGE_5(2209, "Jad Challenge 5", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.JAD_CHALLENGE_5),
	JAD_CHALLENGE_6(2210, "Jad Challenge 6", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.JAD_CHALLENGE_6),
	SOUL_WARS_WINS(2211, "Soul Wars wins", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.SOUL_WARS_WINS),
	SOUL_WARS_GAMES(2212, "Soul Wars games", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.SOUL_WARS_GAMES),
	NMZ_POINTS(2213, "NMZ points", Category.MINI_GAMES, TrackerType.CUSTOM, PluginConstants.ActivityID.NMZ_POINTS),
	PEST_CONTROL_POINTS(2214, "Pest control points", Category.MINI_GAMES, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.PEST_CONTROL_POINTS),
	TITHE_FARM_POINTS(2215, "Tithe farm points", Category.MINI_GAMES, TrackerType.VARBIT_VALUE, PluginConstants.ActivityID.TITHE_FARM_POINTS),
	GIANTS_FOUNDRY_POINTS(2216, "Giant's foundry points", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.GIANTS_FOUNDRY_POINTS),

	// --- SKILLING (2300 - 2399) ---
	FISH_CAUGHT(2301, "Fish caught", Category.SKILLING, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.FISH_CAUGHT),
	LOGS_CHOPPED(2302, "Logs chopped", Category.SKILLING, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.LOGS_CHOPPED),
	ORE_MINED(2303, "Ore mined", Category.SKILLING, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.ORE_MINED),
	BIRD_HOUSES(2304, "Bird houses built", Category.SKILLING, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.BIRD_HOUSES),
	FARMING_CONTRACTS(2305, "Farming Contracts", Category.SKILLING, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.FARMING_CONTRACTS),
	HUNTER_RUMOURS(2306, "Hunter Rumours", Category.SKILLING, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.HUNTER_RUMOURS),
	MAHOGANY_HOMES(2307, "Mahogany Homes", Category.SKILLING, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.MAHOGANY_HOMES),
	MIXOLOGY_ORDERS(2308, "Mixology orders", Category.SKILLING, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.MIXOLOGY_ORDERS),
	MIXOLOGY_AGA_POINTS(2309, "Mixology Aga points", Category.SKILLING, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.MIXOLOGY_AGA_POINTS),
	MIXOLOGY_LYE_POINTS(2310, "Mixology Lye points", Category.SKILLING, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.MIXOLOGY_LYE_POINTS),
	MIXOLOGY_MOX_POINTS(2311, "Mixology Mox points", Category.SKILLING, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.MIXOLOGY_MOX_POINTS),
	SACRIFICES_SPARED(2312, "Offerings spared", Category.SKILLING, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.SACRIFICES_SPARED),
	SACRIFICES_MADE(2313, "Bones offered", Category.SKILLING, TrackerType.SOUND_EFFECT, PluginConstants.ActivityID.SACRIFICES_MADE),
	GUARDIANS_OF_THE_RIFT(2314, "Guardians of the Rift", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.GUARDIANS_OF_THE_RIFT),
	WINTERTODT(2315, "Wintertodt", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.WINTERTODT),
	ZALCANO(2316, "Zalcano", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.ZALCANO),
	TEMPOROSS(2317, "Tempoross", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.TEMPOROSS),

	// --- SUPPLIES (2400 - 2499) ---
	POTIONS_SIPPED(2401, "Potions sipped", Category.SUPPLIES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.POTIONS_SIPPED),
	FOOD_EATEN(2402, "Food eaten", Category.SUPPLIES, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.FOOD_EATEN),
	TELEPORT_TABLETS_USED(2403, "Teleport tablets used", Category.SUPPLIES, TrackerType.SOUND_EFFECT, PluginConstants.ActivityID.TELEPORT_TABLETS_USED),
	CANNONBALLS_FIRED(2404, "Cannonballs fired", Category.SUPPLIES, TrackerType.CUSTOM, PluginConstants.ActivityID.CANNONBALLS_FIRED),
	SCYTHE_SWIPE(2405, "Scythe of Vitur charges", Category.SUPPLIES, TrackerType.SOUND_EFFECT, PluginConstants.ActivityID.SCYTHE_ATTACK),
	CANNONS_LOST(2406, "Cannons lost&found", Category.SUPPLIES, TrackerType.CUSTOM, PluginConstants.ActivityID.CANNONS_LOST),

	// --- ACHIEVEMENTS (2500 - 2599) ---
	QUESTS(2501, "Quests", Category.ACHIEVEMENTS, TrackerType.VARBIT_VALUE, PluginConstants.ActivityID.QUESTS),
	QUEST_POINTS(2502, "Quest points", Category.ACHIEVEMENTS, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.QUEST_POINTS),
	CA_DIARY_TASKS(2503, "CA Diary tasks", Category.ACHIEVEMENTS, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.CA_DIARY_TASKS),
	CA_DIARY_POINTS(2504, "CA Diary points", Category.ACHIEVEMENTS, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.CA_DIARY_POINTS),
	MUSIC_TRACKS_UNLOCKED(2505, "Music tracks unlocked", Category.ACHIEVEMENTS, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.MUSIC_TRACKS_UNLOCKED),
	NEW_COLLECTIONS_LOGGED(2506, "New collections logged", Category.ACHIEVEMENTS, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.NEW_COLLECTIONS_LOGGED),
	VARROCK_MUSEUM_KUDOS(2507, "Varrock Museum kudos", Category.ACHIEVEMENTS, TrackerType.VARBIT_VALUE, PluginConstants.ActivityID.VARROCK_MUSEUM_KUDOS),

	// --- COMBAT (2600 - 2699) ---
	DAMAGE_DEALT_TO_NPCS(2600, "Damage dealt to NPCs", Category.COMBAT, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DAMAGE_DEALT_TO_NPCS),
	DAMAGE_TAKEN_FROM_NPCS(2601, "Damage taken from NPCs", Category.COMBAT, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.DAMAGE_TAKEN_FROM_NPCS),
	SPECIAL_ATTACKS_USED(2602, "Special attacks used", Category.COMBAT, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.SPECIAL_ATTACKS_USED),
	PLAYER_DEATHS(2603, "Player deaths", Category.COMBAT, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.PLAYER_DEATHS),
	PLAYER_KILLS(2604, "Player kills", Category.COMBAT, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.PLAYER_KILLS),
	MONSTER_KILLS(2605, "Monster kills", Category.COMBAT, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.MONSTER_KILLS),

	// --- BOLT PROCS (2700 - 2799) ---
	OPAL_BOLT_PROC(2700, "Opal bolt procs", Category.BOLTS, TrackerType.CUSTOM, PluginConstants.ActivityID.BOLT_OPAL),
	SAPPHIRE_BOLT_PROC(2701, "Sapphire bolt procs", Category.BOLTS, TrackerType.CUSTOM, PluginConstants.ActivityID.BOLT_SAPPHIRE),
	JADE_BOLT_PROC(2702, "Jade bolt procs", Category.BOLTS, TrackerType.CUSTOM, PluginConstants.ActivityID.BOLT_JADE),
	PEARL_BOLT_PROC(2703, "Pearl bolt procs", Category.BOLTS, TrackerType.CUSTOM, PluginConstants.ActivityID.BOLT_PEARL),
	EMERALD_BOLT_PROC(2704, "Emerald bolt procs", Category.BOLTS, TrackerType.CUSTOM, PluginConstants.ActivityID.BOLT_EMERALD),
	RED_TOPAZ_BOLT_PROC(2705, "Red topaz bolt procs", Category.BOLTS, TrackerType.CUSTOM, PluginConstants.ActivityID.BOLT_REDTOPAZ),
	RUBY_BOLT_PROC(2706, "Ruby bolt procs", Category.BOLTS, TrackerType.CUSTOM, PluginConstants.ActivityID.BOLT_RUBY),
	DIAMOND_BOLT_PROC(2707, "Diamond bolt procs", Category.BOLTS, TrackerType.CUSTOM, PluginConstants.ActivityID.BOLT_DIAMOND),
	DRAGONSTONE_BOLT_PROC(2708, "Dragonstone bolt procs", Category.BOLTS, TrackerType.CUSTOM, PluginConstants.ActivityID.BOLT_DRAGONSTONE),
	ONYX_BOLT_PROC(2709, "Onyx bolt procs", Category.BOLTS, TrackerType.CUSTOM, PluginConstants.ActivityID.BOLT_ONYX),

	// --- SAILING (2800 - 2899) ---
	// (Reserved space for future Sailing activities)

	// --- TERTIARY DROPS (2900 - 2999) ---
	BIRD_EGGS_OFFERED(2900, "Bird eggs offered", Category.TERTIARY_DROPS, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.BIRD_EGGS_OFFERED),

	// Standard Spellbook (3000 - 3099)
	LUMBRIDGE_HOME_TELEPORT(3000, "Lumbridge Home Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -500),
	REGULAR_MINIGAME_TELEPORT(3001, "Regular Minigame Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -501),
	WIND_STRIKE(3002, "Wind Strike", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 220),
	CONFUSE(3003, "Confuse", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 119),
	ENCHANT_CROSSBOW_BOLT_OPAL(3004, "Enchant Crossbow Bolt Opal", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -504),
	WATER_STRIKE(3005, "Water Strike", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 211),
	LVL_1_ENCHANT(3006, "Lvl 1 Enchant", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -506),
	ENCHANT_CROSSBOW_BOLT_SAPPHIRE(3007, "Enchant Crossbow Bolt Sapphire", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -507),
	EARTH_STRIKE(3008, "Earth Strike", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 132),
	WEAKEN(3009, "Weaken", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 215),
	FIRE_STRIKE(3010, "Fire Strike", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 160),
	ENCHANT_CROSSBOW_BOLT_JADE(3011, "Enchant Crossbow Bolt Jade", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -511),
	BONES_TO_BANANAS(3012, "Bones To Bananas", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -512),
	WIND_BOLT(3013, "Wind Bolt", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 218),
	CURSE(3014, "Curse", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 127),
	BIND(3015, "Bind", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 101),
	LOW_LEVEL_ALCHEMY(3016, "Low Level Alchemy", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 98),
	WATER_BOLT(3017, "Water Bolt", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 209),
	ENCHANT_CROSSBOW_BOLT_PEARL(3018, "Enchant Crossbow Bolt Pearl", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -518),
	VARROCK_TELEPORT(3019, "Varrock Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -519),
	ENCHANT_CROSSBOW_BOLT_EMERALD(3020, "Enchant Crossbow Bolt Emerald", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -520),
	LVL_2_ENCHANT(3021, "Lvl 2 Enchant", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -521),
	EARTH_BOLT(3022, "Earth Bolt", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 130),
	ENCHANT_CROSSBOW_BOLT_RED_TOPAZ(3023, "Enchant Crossbow Bolt Red Topaz", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -523),
	LUMBRIDGE_TELEPORT(3024, "Lumbridge Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -524),
	TELEKINETIC_GRAB(3025, "Telekinetic Grab", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -525),
	FIRE_BOLT(3026, "Fire Bolt", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 157),
	FALADOR_TELEPORT(3027, "Falador Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -527),
	CRUMBLE_UNDEAD(3028, "Crumble Undead", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 122),
	TELEPORT_TO_HOUSE(3029, "Teleport To House", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -529),
	WIND_BLAST(3030, "Wind Blast", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 216),
	MONSTER_INSPECT(3031, "Monster Inspect", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -531),
	SUPERHEAT_ITEM(3032, "Superheat Item", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -532),
	CAMELOT_TELEPORT(3033, "Camelot Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -533),
	WATER_BLAST(3034, "Water Blast", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 207),
	KOUREND_CASTLE_TELEPORT(3035, "Kourend Castle Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -535),
	LVL_3_ENCHANT(3036, "Lvl 3 Enchant", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -536),
	ENCHANT_CROSSBOW_BOLT_RUBY(3037, "Enchant Crossbow Bolt Ruby", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -537),
	IBAN_BLAST(3038, "Iban Blast", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -538),
	SNARE(3039, "Snare", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 186),
	MAGIC_DART(3040, "Magic Dart", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -540),
	ARDOUGNE_TELEPORT(3041, "Ardougne Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -541),
	EARTH_BLAST(3042, "Earth Blast", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 128),
	CIVITAS_ILLA_FORTIS_TELEPORT(3043, "Civitas Illa Fortis Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -543),
	HIGH_LEVEL_ALCHEMY(3044, "High Level Alchemy", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 97),
	CHARGE_WATER_ORB(3045, "Charge Water Orb", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 118),
	SUMMON_BOAT(3046, "Summon Boat", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -546),
	LVL_4_ENCHANT(3047, "Lvl 4 Enchant", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -547),
	ENCHANT_CROSSBOW_BOLT_DIAMOND(3048, "Enchant Crossbow Bolt Diamond", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -548),
	WATCHTOWER_TELEPORT(3049, "Watchtower Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -549),
	FIRE_BLAST(3050, "Fire Blast", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 155),
	CLAWS_OF_GUTHIX(3051, "Claws Of Guthix", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -551),
	FLAMES_OF_ZAMORAK(3052, "Flames Of Zamorak", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -552),
	CHARGE_EARTH_ORB(3053, "Charge Earth Orb", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 115),
	BONES_TO_PEACHES(3054, "Bones To Peaches", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -554),
	SARADOMIN_STRIKE(3055, "Saradomin Strike", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -555),
	TROLLHEIM_TELEPORT(3056, "Trollheim Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -556),
	WIND_WAVE(3057, "Wind Wave", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 222),
	CHARGE_FIRE_ORB(3058, "Charge Fire Orb", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 117),
	REGULAR_APE_ATOLL_TELEPORT(3059, "Regular Ape Atoll Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -559),
	WATER_WAVE(3060, "Water Wave", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 213),
	VULNERABILITY(3061, "Vulnerability", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 3009),
	CHARGE_AIR_ORB(3062, "Charge Air Orb", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 116),
	TELEPORT_TO_BOAT(3063, "Teleport To Boat", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -563),
	LVL_5_ENCHANT(3064, "Lvl 5 Enchant", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -564),
	ENCHANT_CROSSBOW_BOLT_DRAGONSTONE(3065, "Enchant Crossbow Bolt Dragonstone", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -565),
	EARTH_WAVE(3066, "Earth Wave", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 134),
	ENFEEBLE(3067, "Enfeeble", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 148),
	TELEOTHER_LUMBRIDGE(3068, "Teleother Lumbridge", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -568),
	FIRE_WAVE(3069, "Fire Wave", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 162),
	ENTANGLE(3070, "Entangle", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 151),
	CHARGE(3071, "Charge", Category.MAGIC_SPELLS, TrackerType.CUSTOM, 1651),
	STUN(3072, "Stun", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 3004),
	WIND_SURGE(3073, "Wind Surge", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -573),
	TELEOTHER_FALADOR(3074, "Teleother Falador", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -574),
	WATER_SURGE(3075, "Water Surge", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -575),
	TELE_BLOCK(3076, "Tele Block", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 202),
	REGULAR_TELEPORT_TO_TARGET(3077, "Regular Teleport To Target", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -577),
	LVL_6_ENCHANT(3078, "Lvl 6 Enchant", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -578),
	ENCHANT_CROSSBOW_BOLT_ONYX(3079, "Enchant Crossbow Bolt Onyx", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -579),
	TELEOTHER_CAMELOT(3080, "Teleother Camelot", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -580),
	EARTH_SURGE(3081, "Earth Surge", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -581),
	LVL_7_ENCHANT(3082, "Lvl 7 Enchant", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -582),
	FIRE_SURGE(3083, "Fire Surge", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -583),

	// Ancient Magicks (3100 - 3199)
	EDGEVILLE_HOME_TELEPORT(3100, "Edgeville Home Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -600),
	ANCIENT_MINIGAME_TELEPORT(3101, "Ancient Minigame Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -601),
	SMOKE_RUSH(3102, "Smoke Rush", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 185),
	SHADOW_RUSH(3103, "Shadow Rush", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 179),
	PADDEWWA_TELEPORT(3104, "Paddewwa Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -604),
	BLOOD_RUSH(3105, "Blood Rush", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 110),
	ICE_RUSH(3106, "Ice Rush", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 173),
	SENNTISTEN_TELEPORT(3107, "Senntisten Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -607),
	SMOKE_BURST(3108, "Smoke Burst", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 182),
	SHADOW_BURST(3109, "Shadow Burst", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 177),
	KHARYRLL_TELEPORT(3110, "Kharyrll Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -610),
	BLOOD_BURST(3111, "Blood Burst", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 105),
	ICE_BURST(3112, "Ice Burst", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 170),
	LASSAR_TELEPORT(3113, "Lassar Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -613),
	SMOKE_BLITZ(3114, "Smoke Blitz", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 181),
	SHADOW_BLITZ(3115, "Shadow Blitz", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 176),
	DAREEYAK_TELEPORT(3116, "Dareeyak Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -616),
	BLOOD_BLITZ(3117, "Blood Blitz", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 104),
	ICE_BLITZ(3118, "Ice Blitz", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 169),
	CARRALLANGER_TELEPORT(3119, "Carrallanger Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -619),
	ANCIENT_TELEPORT_TO_TARGET(3120, "Ancient Teleport To Target", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -620),
	SMOKE_BARRAGE(3121, "Smoke Barrage", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 180),
	SHADOW_BARRAGE(3122, "Shadow Barrage", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 175),
	ANNAKARL_TELEPORT(3123, "Annakarl Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -623),
	BLOOD_BARRAGE(3124, "Blood Barrage", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 102),
	ICE_BARRAGE(3125, "Ice Barrage", Category.MAGIC_SPELLS, TrackerType.SOUND_EFFECT, 168),
	GHORROCK_TELEPORT(3126, "Ghorrock Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -626),

	// Lunar Spellbook (3200 - 3299)
	LUNAR_HOME_TELEPORT(3200, "Lunar Home Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -700),
	LUNAR_MINIGAME_TELEPORT(3201, "Lunar Minigame Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -701),
	BAKE_PIE(3202, "Bake Pie", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -702),
	GEOMANCY(3203, "Geomancy", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -703),
	CURE_PLANT(3204, "Cure Plant", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -704),
	MONSTER_EXAMINE(3205, "Monster Examine", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -705),
	ASTRAL_CONTACT(3206, "Astral Contact", Category.MAGIC_SPELLS, TrackerType.CUSTOM, 3618),
	CURE_OTHER(3207, "Cure Other", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -707),
	HUMIDIFY(3208, "Humidify", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -708),
	MOONCLAN_TELEPORT(3209, "Moonclan Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -709),
	TELE_GROUP_MOONCLAN(3210, "Tele Group Moonclan", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -710),
	HUNTER_KIT(3211, "Hunter Kit", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -711),
	CURE_ME(3212, "Cure Me", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -712),
	OURANIA_TELEPORT(3213, "Ourania Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -713),
	WATERBIRTH_TELEPORT(3214, "Waterbirth Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -714),
	TELE_GROUP_WATERBIRTH(3215, "Tele Group Waterbirth", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -715),
	CURE_GROUP(3216, "Cure Group", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -716),
	STAT_SPY(3217, "Stat Spy", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -717),
	BARBARIAN_TELEPORT(3218, "Barbarian Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -718),
	TELE_GROUP_BARBARIAN(3219, "Tele Group Barbarian", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -719),
	SPIN_FLAX(3220, "Spin Flax", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -720),
	SUPERGLASS_MAKE(3221, "Superglass Make", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -721),
	KHAZARD_TELEPORT(3222, "Khazard Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -722),
	TAN_LEATHER(3223, "Tan Leather", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -723),
	TELE_GROUP_KHAZARD(3224, "Tele Group Khazard", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -724),
	DREAM(3225, "Dream", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -725),
	STRING_JEWELLERY(3226, "String Jewellery", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -726),
	STAT_RESTORE_POT_SHARE(3227, "Stat Restore Pot Share", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -727),
	MAGIC_IMBUE(3228, "Magic Imbue", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -728),
	FERTILE_SOIL(3229, "Fertile Soil", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -729),
	BOOST_POTION_SHARE(3230, "Boost Potion Share", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -730),
	FISHING_GUILD_TELEPORT(3231, "Fishing Guild Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -731),
	LUNAR_TELEPORT_TO_TARGET(3232, "Lunar Teleport To Target", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -732),
	PLANK_MAKE(3233, "Plank Make", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -733),
	TELE_GROUP_FISHING_GUILD(3234, "Tele Group Fishing Guild", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -734),
	CATHERBY_TELEPORT(3235, "Catherby Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -735),
	TELE_GROUP_CATHERBY(3236, "Tele Group Catherby", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -736),
	RECHARGE_DRAGONSTONE(3237, "Recharge Dragonstone", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -737),
	ICE_PLATEAU_TELEPORT(3238, "Ice Plateau Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -738),
	TELE_GROUP_ICE_PLATEAU(3239, "Tele Group Ice Plateau", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -739),
	ENERGY_TRANSFER(3240, "Energy Transfer", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -740),
	HEAL_OTHER(3241, "Heal Other", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -741),
	VENGEANCE_OTHER(3242, "Vengeance Other", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -742),
	VENGEANCE(3243, "Vengeance", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -743),
	HEAL_GROUP(3244, "Heal Group", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -744),
	SPELLBOOK_SWAP(3245, "Spellbook Swap", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -745),

	// Arceuus Spellbook (3300 - 3399)
	ARCEUUS_HOME_TELEPORT(3300, "Arceuus Home Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -800),
	ARCEUUS_MINIGAME_TELEPORT(3301, "Arceuus Minigame Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -801),
	ARCEUUS_LIBRARY_TELEPORT(3302, "Arceuus Library Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -802),
	BASIC_REANIMATION(3303, "Basic Reanimation", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -803),
	DRAYNOR_MANOR_TELEPORT(3304, "Draynor Manor Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -804),
	BATTLEFRONT_TELEPORT(3305, "Battlefront Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -805),
	MIND_ALTAR_TELEPORT(3306, "Mind Altar Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -806),
	RESPAWN_TELEPORT(3307, "Respawn Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -807),
	GHOSTLY_GRASP(3308, "Ghostly Grasp", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -808),
	RESURRECT_LESSER_ZOMBIE(3309, "Resurrect Lesser Zombie", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -809),
	RESURRECT_LESSER_SKELETON(3310, "Resurrect Lesser Skeleton", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -810),
	RESURRECT_LESSER_GHOST(3311, "Resurrect Lesser Ghost", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -811),
	SALVE_GRAVEYARD_TELEPORT(3312, "Salve Graveyard Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -812),
	ADEPT_REANIMATION(3313, "Adept Reanimation", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -813),
	INFERIOR_DEMONBANE(3314, "Inferior Demonbane", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -814),
	SHADOW_VEIL(3315, "Shadow Veil", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -815),
	FENKENSTRAIN_S_CASTLE_TELEPORT(3316, "Fenkenstrain S Castle Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -816),
	DARK_LURE(3317, "Dark Lure", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -817),
	SKELETAL_GRASP(3318, "Skeletal Grasp", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -818),
	RESURRECT_SUPERIOR_GHOST(3319, "Resurrect Superior Ghost", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -819),
	RESURRECT_SUPERIOR_SKELETON(3320, "Resurrect Superior Skeleton", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -820),
	RESURRECT_SUPERIOR_ZOMBIE(3321, "Resurrect Superior Zombie", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -821),
	MARK_OF_DARKNESS(3322, "Mark Of Darkness", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -822),
	WEST_ARDOUGNE_TELEPORT(3323, "West Ardougne Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -823),
	SUPERIOR_DEMONBANE(3324, "Superior Demonbane", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -824),
	LESSER_CORRUPTION(3325, "Lesser Corruption", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -825),
	HARMONY_ISLAND_TELEPORT(3326, "Harmony Island Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -826),
	VILE_VIGOUR(3327, "Vile Vigour", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -827),
	DEGRIME(3328, "Degrime", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -828),
	CEMETERY_TELEPORT(3329, "Cemetery Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -829),
	EXPERT_REANIMATION(3330, "Expert Reanimation", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -830),
	WARD_OF_ARCEUUS(3331, "Ward Of Arceuus", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -831),
	RESURRECT_GREATER_SKELETON(3332, "Resurrect Greater Skeleton", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -832),
	RESURRECT_GREATER_ZOMBIE(3333, "Resurrect Greater Zombie", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -833),
	RESURRECT_GREATER_GHOST(3334, "Resurrect Greater Ghost", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -834),
	RESURRECT_CROPS(3335, "Resurrect Crops", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -835),
	UNDEAD_GRASP(3336, "Undead Grasp", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -836),
	DEATH_CHARGE(3337, "Death Charge", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -837),
	DARK_DEMONBANE(3338, "Dark Demonbane", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -838),
	BARROWS_TELEPORT(3339, "Barrows Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -839),
	DEMONIC_OFFERING(3340, "Demonic Offering", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -840),
	GREATER_CORRUPTION(3341, "Greater Corruption", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -841),
	ARCEUUS_TELEPORT_TO_TARGET(3342, "Arceuus Teleport To Target", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -842),
	ARCEUUS_APE_ATOLL_TELEPORT(3343, "Arceuus Ape Atoll Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -843),
	MASTER_REANIMATION(3344, "Master Reanimation", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -844),
	SINISTER_OFFERING(3345, "Sinister Offering", Category.MAGIC_SPELLS, TrackerType.CUSTOM, -845),

	SPLASH_CAST(3399, "Splash cast (miss)", Category.MAGIC_SPELLS, TrackerType.AREA_SOUND, 227),

	// --- OTHER (9000+) ---
	GE_TAX_PAID(9000, "", Category.OTHER, TrackerType.CUSTOM, -1),
	COINS_GAINED(9001, "Coins gained", Category.OTHER, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.COINS_GAINED),
	COINS_LOST(9002, "Coins lost", Category.OTHER, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.COINS_LOST),
	GEMSTONE_CRAB(9003, "Gemstone Crab", Category.OTHER, TrackerType.VARPLAYER_VALUE, PluginConstants.ActivityID.GEMSTONE_CRAB);

	private final int id;
	private final String name;
	private final Category category;
	private final TrackerType trackerType;
	private final int gameSourceId;
	private final String configId;

	// Fast lookup map for retrieving TrackedActivity by its Storage ID
	private static final Map<Integer, TrackedActivity> STORAGE_ID_MAP = new HashMap<>();

	static {
		for (TrackedActivity activity : values()) {
			STORAGE_ID_MAP.put(activity.id, activity);
		}
	}

	TrackedActivity(int id, String name, Category category, TrackerType trackerType, int gameSourceId) {
		this.id = id;
		this.name = name;
		this.category = category;
		this.trackerType = trackerType;
		this.gameSourceId = gameSourceId;
		this.configId = category.toString().toLowerCase() + name.replace(" ", "")
			.replace("(", "")
			.replace(")", "")
			.replace("'", "");
	}

	/**
	 * Safely lookup a TrackedActivity by its permanent Storage ID.
	 */
	public static TrackedActivity getByStorageId(int storageId) {
		return STORAGE_ID_MAP.get(storageId);
	}
}