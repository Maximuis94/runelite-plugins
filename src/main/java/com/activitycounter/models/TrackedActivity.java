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
import static com.activitycounter.PluginConstants.ActivityID.ACHIEVEMENT_DIARY_TASK_COUNT;
import static com.activitycounter.PluginConstants.ActivityID.FARMING_COMPOST;
import static com.activitycounter.PluginConstants.ActivityID.SEEDS_PLANTED;
import static com.activitycounter.PluginConstants.SoundID.SCYTHE_CRUSH;
import static com.activitycounter.PluginConstants.SoundID.SCYTHE_SLASH;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.VarbitID;

@Slf4j
@Getter
public enum TrackedActivity
{

	// --- BOSSES (1000 - 1499) (500 spaces for boss releases) ---
	BRUTUS(1001, "Brutus", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.BRUTUS}),
	OBOR(1002, "Obor", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.OBOR}),
	BRYOPHYTA(1003, "Bryophyta", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.BRYOPHYTA}),
	SCURRIUS(1004, "Scurrius", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.SCURRIUS}),
	CHAOS_FANATIC(1005, "Chaos Fanatic", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.CHAOS_FANATIC}),
	DERANGED_ARCHAEOLOGIST(1006, "Deranged Archaeologist", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DERANGED_ARCHAEOLOGIST}),
	CRAZY_ARCHAEOLOGIST(1007, "Crazy Archaeologist", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.CRAZY_ARCHAEOLOGIST}),
	SCORPIA(1008, "Scorpia", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.SCORPIA}),
	GIANT_MOLE(1009, "Giant Mole", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.GIANT_MOLE}),
	SHELLBANE_GRYPHON(1010, "Shellbane Gryphon", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.SHELLBANE_GRYPHON}),
	GROTESQUE_GUARDIANS(1011, "Grotesque Guardians", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.GROTESQUE_GUARDIANS}),
	AMOXLIATL(1012, "Amoxliatl", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.AMOXLIATL}),
	CALVARION(1013, "Calvar'ion", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.CALVARION}),
	KING_BLACK_DRAGON(1014, "King Black Dragon", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.KING_BLACK_DRAGON}),
	HESPORI(1015, "Hespori", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.HESPORI}),
	KRAKEN(1016, "Kraken", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.KRAKEN}),
	THERMONUCLEAR_SMOKE_DEVIL(1017, "Thermonuclear Smoke Devil", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.THERMONUCLEAR_SMOKE_DEVIL}),
	SPINDEL(1018, "Spindel", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.SPINDEL}),
	DAGANNOTH_PRIME(1019, "Dagannoth Prime", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DAGANNOTH_PRIME}),
	DAGANNOTH_REX(1020, "Dagannoth Rex", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DAGANNOTH_REX}),
	DAGANNOTH_SUPREME(1021, "Dagannoth Supreme", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DAGANNOTH_SUPREME}),
	CERBERUS(1022, "Cerberus", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.CERBERUS}),
	SARACHNIS(1023, "Sarachnis", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.SARACHNIS}),
	SKOTIZO(1024, "Skotizo", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.SKOTIZO}),
	ARTIO(1025, "Artio", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.ARTIO}),
	KALPHITE_QUEEN(1026, "Kalphite Queen", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.KALPHITE_QUEEN}),
	ABYSSAL_SIRE(1027, "Abyssal Sire", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.ABYSSAL_SIRE}),
	ROYAL_TITANS(1028, "Royal Titans", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.ROYAL_TITANS}),
	ALCHEMICAL_HYDRA(1029, "Alchemical Hydra", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.ALCHEMICAL_HYDRA}),
	VETION(1030, "Vet'ion", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.VETION}),
	VENENATIS(1031, "Venenatis", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.VENENATIS}),
	CALLISTO(1032, "Callisto", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.CALLISTO}),
	CHAOS_ELEMENTAL(1033, "Chaos Elemental", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.CHAOS_ELEMENTAL}),
	KREEARRA(1034, "Kree'arra", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.KREEARRA}),
	MAD_ANGEL(1035, "Mad Angel", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.MAD_ANGEL}),
	COMMANDER_ZILYANA(1036, "Commander Zilyana", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.COMMANDER_ZILYANA}),
	GENERAL_GRAARDOR(1037, "General Graardor", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.GENERAL_GRAARDOR}),
	THE_HUEYCOATL(1038, "The Hueycoatl", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.THE_HUEYCOATL}),
	KRIL_TSUTSAROTH(1039, "K'ril Tsutsaroth", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.KRIL_TSUTSAROTH}),
	TZTOK_JAD(1040, "TzTok-Jad", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.TZTOK_JAD}),
	ZULRAH(1041, "Zulrah", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.ZULRAH}),
	VORKATH(1042, "Vorkath", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.VORKATH}),
	PHANTOM_MUSPAH(1043, "Phantom Muspah", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.PHANTOM_MUSPAH}),
	MAGGOT_KING(1044, "Maggot King", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.MAGGOT_KING}),
	DUKE_SUCELLUS(1045, "Duke Sucellus", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DUKE_SUCELLUS}),
	VARDORVIS(1046, "Vardorvis", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.VARDORVIS}),
	CORPOREAL_BEAST(1047, "Corporeal Beast", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.CORPOREAL_BEAST}),
	THE_WHISPERER(1048, "The Whisperer", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.THE_WHISPERER}),
	THE_LEVIATHAN(1049, "The Leviathan", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.THE_LEVIATHAN}),
	THE_NIGHTMARE(1050, "The Nightmare", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.THE_NIGHTMARE}),
	ARAXXOR(1051, "Araxxor", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.ARAXXOR}),
	NEX(1052, "Nex", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.NEX}),
	PHOSANIS_NIGHTMARE(1053, "Phosani's Nightmare", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.PHOSANIS_NIGHTMARE}),
	DUKE_SUCELLUS_AWAKENED(1054, "Duke Sucellus (Awakened)", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DUKE_SUCELLUS_AWAKENED}),
	VARDORVIS_AWAKENED(1055, "Vardorvis (Awakened)", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.VARDORVIS_AWAKENED}),
	THE_WHISPERER_AWAKENED(1056, "The Whisperer (Awakened)", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.THE_WHISPERER_AWAKENED}),
	THE_LEVIATHAN_AWAKENED(1057, "The Leviathan (Awakened)", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.THE_LEVIATHAN_AWAKENED}),
	DEMONIC_BRUTUS(1058, "Demonic Brutus", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DEMONIC_BRUTUS}),
	YAMA(1059, "Yama", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.YAMA}),
	TZKAL_ZUK(1060, "TzKal-Zuk", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.TZKAL_ZUK}),
	SOL_HEREDIT(1061, "Sol Heredit", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.SOL_HEREDIT}),
	COLOSSEUM_WAVES(1062, "Colosseum waves", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.COLOSSEUM_WAVES}),
	DOOM_OF_MOKHAIOTL_LEVELS(1063, "Doom of Mokhaiotl levels", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DOOM_OF_MOKHAIOTL_LEVELS}),
	DOOM_OF_MOKHAIOTL_LEVEL_1_COMPLETIONS(1064, "Doom of Mokhaiotl Level 1", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DOM_LEVEL_1_COMPLETIONS}),
	DOOM_OF_MOKHAIOTL_LEVEL_2_COMPLETIONS(1065, "Doom of Mokhaiotl Level 2", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DOM_LEVEL_2_COMPLETIONS}),
	DOOM_OF_MOKHAIOTL_LEVEL_3_COMPLETIONS(1066, "Doom of Mokhaiotl Level 3", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DOM_LEVEL_3_COMPLETIONS}),
	DOOM_OF_MOKHAIOTL_LEVEL_4_COMPLETIONS(1067, "Doom of Mokhaiotl Level 4", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DOM_LEVEL_4_COMPLETIONS}),
	DOOM_OF_MOKHAIOTL_LEVEL_5_COMPLETIONS(1068, "Doom of Mokhaiotl Level 5", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DOM_LEVEL_5_COMPLETIONS}),
	DOOM_OF_MOKHAIOTL_LEVEL_6_COMPLETIONS(1069, "Doom of Mokhaiotl Level 6", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DOM_LEVEL_6_COMPLETIONS}),
	DOOM_OF_MOKHAIOTL_LEVEL_7_COMPLETIONS(1070, "Doom of Mokhaiotl Level 7", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DOM_LEVEL_7_COMPLETIONS}),
	DOOM_OF_MOKHAIOTL_LEVEL_8_COMPLETIONS(1071, "Doom of Mokhaiotl Level 8", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DOM_LEVEL_8_COMPLETIONS}),
	DOOM_OF_MOKHAIOTL_LEVEL_8_PLUS_COMPLETIONS(1072, "Doom of Mokhaiotl Level 8+", Category.BOSSES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DOM_LEVEL_8_PLUS_COMPLETIONS}),

	// --- CHESTS (1500 - 1599) ---
	BARROWS_CHESTS(1501, "Barrows Chests", Category.CHESTS, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.BARROWS_CHESTS}),
	PERILOUS_MOONS_CHESTS(1502, "Perilous Moons Chests", Category.CHESTS, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.PERILOUS_MOONS_CHESTS}),
	LARRANS_SMALL_CHESTS(1503, "Larran's small chests", Category.CHESTS, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.LARRANS_SMALL_CHESTS}),
	LARRANS_BIG_CHESTS(1504, "Larran's big chests", Category.CHESTS, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.LARRANS_BIG_CHESTS}),
	BRIMSTONE_CHESTS(1505, "Brimstone chests", Category.CHESTS, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.BRIMSTONE_CHESTS}),
	ZOMBIE_PIRATE_CHESTS(1506, "Zombie pirate lockers", Category.CHESTS, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.ZOMBIE_PIRATE_CHESTS}),
	CRYSTAL_CHESTS(1507, "Crystal chests", Category.CHESTS, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.CRYSTAL_CHESTS}),
	ELVEN_CRYSTAL_CHESTS(1508, "Elven crystal chests", Category.CHESTS, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.ELVEN_CRYSTAL_CHESTS}),

	// --- RAIDS (1600 - 1699) ---
	CHAMBERS_OF_XERIC(1601, "Chambers of Xeric", Category.RAIDS, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.CHAMBERS_OF_XERIC}),
	CHAMBERS_OF_XERIC_CHALLENGE_MODE(1602, "Chambers of Xeric: Challenge Mode", Category.RAIDS, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.CHAMBERS_OF_XERIC_CHALLENGE_MODE}),
	THEATRE_OF_BLOOD(1603, "Theatre of Blood", Category.RAIDS, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.THEATRE_OF_BLOOD}),
	THEATRE_OF_BLOOD_STORY_MODE(1604, "Theatre of Blood: Story Mode", Category.RAIDS, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.THEATRE_OF_BLOOD_STORY_MODE}),
	THEATRE_OF_BLOOD_HARD_MODE(1605, "Theatre of Blood: Hard Mode", Category.RAIDS, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.THEATRE_OF_BLOOD_HARD_MODE}),
	THE_GAUNTLET(1606, "The Gauntlet", Category.RAIDS, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.THE_GAUNTLET}),
	THE_CORRUPTED_GAUNTLET(1607, "The Corrupted Gauntlet", Category.RAIDS, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.THE_CORRUPTED_GAUNTLET}),
	TOMBS_OF_AMASCUT(1608, "Tombs of Amascut", Category.RAIDS, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.TOMBS_OF_AMASCUT}),
	TOMBS_OF_AMASCUT_ENTRY_MODE(1609, "Tombs of Amascut: Entry Mode", Category.RAIDS, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.TOMBS_OF_AMASCUT_ENTRY_MODE}),
	TOMBS_OF_AMASCUT_EXPERT_MODE(1610, "Tombs of Amascut: Expert Mode", Category.RAIDS, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.TOMBS_OF_AMASCUT_EXPERT_MODE}),

	// --- SLAYER (1700 - 1799) ---
	SLAYER_TASKS_OTHER(1701, "Slayer tasks (Other)", Category.SLAYER, TrackerType.VARBIT_VALUE, new int[]{PluginConstants.ActivityID.SLAYER_TASKS_OTHER}),
	SLAYER_TASKS_WILDERNESS(1702, "Slayer tasks (Wilderness)", Category.SLAYER, TrackerType.VARBIT_VALUE, new int[]{PluginConstants.ActivityID.SLAYER_TASKS_WILDERNESS}),
	SLAYER_TASKS_MORTIMER(1703, "Slayer tasks (Mortimer)", Category.SLAYER, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.SLAYER_TASKS_MORTIMER}),
	SUPERIOR_SPAWNS(1704, "Superior spawns", Category.SLAYER, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.SUPERIOR_SPAWNS}),
	SLAYER_POINTS(1705, "Slayer points", Category.SLAYER, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.SLAYER_POINTS}),

	// --- AGILITY (1800 - 1899) ---
	AGILITY_GNOME_STRONGHOLD(1801, "Gnome Stronghold Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_GNOME_STRONGHOLD}),
	AGILITY_SHAYZIEN_LOW(1802, "Shayzien Laps (Basic)", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_SHAYZIEN_LOW}),
	AGILITY_SHAYZIEN_HIGH(1803, "Shayzien Laps (Advanced)", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_SHAYZIEN_HIGH}),
	AGILITY_PENGUIN(1804, "Penguin Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_PENGUIN}),
	AGILITY_BARBARIAN_OUTPOST(1805, "Barbarian Outpost Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_BARBARIAN_OUTPOST}),
	AGILITY_APE_ATOLL(1806, "Ape Atoll Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_APE_ATOLL}),
	AGILITY_WILDERNESS(1807, "Wilderness Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_WILDERNESS}),
	AGILITY_COLOSSAL_WYRM_ADVANCED(1808, "Colossal Wyrm Laps (Advanced)", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_COLOSSAL_WYRM_ADVANCED}),
	AGILITY_COLOSSAL_WYRM_BASIC(1809, "Colossal Wyrm Laps (Basic)", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_COLOSSAL_WYRM_BASIC}),
	AGILITY_WEREWOLF(1810, "Werewolf Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_WEREWOLF}),
	AGILITY_PRIFDDINAS(1811, "Prifddinas Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_PRIFDDINAS}),
	AGILITY_DRAYNOR_ROOFTOP(1812, "Draynor Village Rooftop Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_DRAYNOR_ROOFTOP}),
	AGILITY_AL_KHARID_ROOFTOP(1813, "Al Kharid Rooftop Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_AL_KHARID_ROOFTOP}),
	AGILITY_VARROCK_ROOFTOP(1814, "Varrock Rooftop Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_VARROCK_ROOFTOP}),
	AGILITY_CANIFIS_ROOFTOP(1815, "Canifis Rooftop Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_CANIFIS_ROOFTOP}),
	AGILITY_FALADOR_ROOFTOP(1816, "Falador Rooftop Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_FALADOR_ROOFTOP}),
	AGILITY_SEERS_ROOFTOP(1817, "Seers' Village Rooftop Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_SEERS_ROOFTOP}),
	AGILITY_POLLNIVNEACH_ROOFTOP(1818, "Pollnivneach Rooftop Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_POLLNIVNEACH_ROOFTOP}),
	AGILITY_RELLEKKA_ROOFTOP(1819, "Rellekka Rooftop Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_RELLEKKA_ROOFTOP}),
	AGILITY_ARDOUGNE_ROOFTOP(1820, "Ardougne Rooftop Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_ARDOUGNE_ROOFTOP}),
	AGILITY_PYRAMID(1821, "Agility Pyramid Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_PYRAMID}),
	AGILITY_DORGESH_KAAN(1822, "Dorgesh-Kaan Laps", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_DORGESH_KAAN}),
	AGILITY_BRIMHAVEN(1823, "Brimhaven Agility Tickets", Category.AGILITY, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.AGILITY_BRIMHAVEN}),

	// --- Skill-level related (1900 - 2099) ---
	XP_TOTAL(1901, "Total XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_TOTAL}),
	XP_ATTACK(1902, "Attack XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_ATTACK}),
	XP_DEFENCE(1903, "Defence XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_DEFENCE}),
	XP_STRENGTH(1904, "Strength XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_STRENGTH}),
	XP_HITPOINTS(1905, "Hitpoints XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_HITPOINTS}),
	XP_RANGED(1906, "Ranged XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_RANGED}),
	XP_PRAYER(1907, "Prayer XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_PRAYER}),
	XP_MAGIC(1908, "Magic XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_MAGIC}),
	XP_COOKING(1909, "Cooking XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_COOKING}),
	XP_WOODCUTTING(1910, "Woodcutting XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_WOODCUTTING}),
	XP_FLETCHING(1911, "Fletching XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_FLETCHING}),
	XP_FISHING(1912, "Fishing XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_FISHING}),
	XP_FIREMAKING(1913, "Firemaking XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_FIREMAKING}),
	XP_CRAFTING(1914, "Crafting XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_CRAFTING}),
	XP_SMITHING(1915, "Smithing XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_SMITHING}),
	XP_MINING(1916, "Mining XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_MINING}),
	XP_HERBLORE(1917, "Herblore XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_HERBLORE}),
	XP_AGILITY(1918, "Agility XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_AGILITY}),
	XP_THIEVING(1919, "Thieving XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_THIEVING}),
	XP_SLAYER(1920, "Slayer XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_SLAYER}),
	XP_FARMING(1921, "Farming XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_FARMING}),
	XP_RUNECRAFT(1922, "Runecraft XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_RUNECRAFT}),
	XP_HUNTER(1923, "Hunter XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_HUNTER}),
	XP_CONSTRUCTION(1924, "Construction XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_CONSTRUCTION}),
	XP_SAILING(1925, "Sailing XP", Category.EXPERIENCE, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.XP_SAILING}),

	LVL_TOTAL(1951, "Total Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_TOTAL}),
	LVL_ATTACK(1952, "Attack Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_ATTACK}),
	LVL_DEFENCE(1953, "Defence Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_DEFENCE}),
	LVL_STRENGTH(1954, "Strength Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_STRENGTH}),
	LVL_HITPOINTS(1955, "Hitpoints Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_HITPOINTS}),
	LVL_RANGED(1956, "Ranged Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_RANGED}),
	LVL_PRAYER(1957, "Prayer Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_PRAYER}),
	LVL_MAGIC(1958, "Magic Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_MAGIC}),
	LVL_COOKING(1959, "Cooking Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_COOKING}),
	LVL_WOODCUTTING(1960, "Woodcutting Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_WOODCUTTING}),
	LVL_FLETCHING(1961, "Fletching Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_FLETCHING}),
	LVL_FISHING(1962, "Fishing Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_FISHING}),
	LVL_FIREMAKING(1963, "Firemaking Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_FIREMAKING}),
	LVL_CRAFTING(1964, "Crafting Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_CRAFTING}),
	LVL_SMITHING(1965, "Smithing Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_SMITHING}),
	LVL_MINING(1966, "Mining Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_MINING}),
	LVL_HERBLORE(1967, "Herblore Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_HERBLORE}),
	LVL_AGILITY(1968, "Agility Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_AGILITY}),
	LVL_THIEVING(1969, "Thieving Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_THIEVING}),
	LVL_SLAYER(1970, "Slayer Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_SLAYER}),
	LVL_FARMING(1971, "Farming Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_FARMING}),
	LVL_RUNECRAFTING(1972, "Runecraft Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_RUNECRAFTING}),
	LVL_HUNTER(1973, "Hunter Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_HUNTER}),
	LVL_CONSTRUCTION(1974, "Construction Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_CONSTRUCTION}),
	LVL_SAILING(1975, "Sailing Level", Category.LEVELS, TrackerType.STAT_CHANGE, new int[]{PluginConstants.ActivityID.LVL_SAILING}),

	// --- CLUES (2100 - 2199) ---
	COMPLETED_BEGINNER_CLUE(2101, "Completed beginner clue", Category.CLUE, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.COMPLETED_BEGINNER_CLUE}),
	MISSED_BEGINNER_CLUE(2102, "Missed beginner clue", Category.CLUE, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.MISSED_BEGINNER_CLUE}),
	COMPLETED_EASY_CLUE(2103, "Completed easy clue", Category.CLUE, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.COMPLETED_EASY_CLUE}),
	MISSED_EASY_CLUE(2104, "Missed easy clue", Category.CLUE, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.MISSED_EASY_CLUE}),
	COMPLETED_MEDIUM_CLUE(2105, "Completed medium clue", Category.CLUE, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.COMPLETED_MEDIUM_CLUE}),
	MISSED_MEDIUM_CLUE(2106, "Missed medium clue", Category.CLUE, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.MISSED_MEDIUM_CLUE}),
	COMPLETED_HARD_CLUE(2107, "Completed hard clue", Category.CLUE, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.COMPLETED_HARD_CLUE}),
	MISSED_HARD_CLUE(2108, "Missed hard clue", Category.CLUE, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.MISSED_HARD_CLUE}),
	COMPLETED_ELITE_CLUE(2109, "Completed elite clue", Category.CLUE, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.COMPLETED_ELITE_CLUE}),
	MISSED_ELITE_CLUE(2110, "Missed elite clue", Category.CLUE, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.MISSED_ELITE_CLUE}),
	COMPLETED_MASTER_CLUE(2111, "Completed master clue", Category.CLUE, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.COMPLETED_MASTER_CLUE}),
	MIMIC(2112, "Mimic", Category.CLUE, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.MIMIC}),

	// --- MINI GAMES (2200 - 2299) ---
	BA_ATTACKER_POINTS(2201, "BA Attacker points", Category.MINI_GAMES, TrackerType.VARBIT_VALUE, new int[]{PluginConstants.ActivityID.BA_ATTACKER_POINTS}),
	BA_COLLECTOR_POINTS(2202, "BA Collector points", Category.MINI_GAMES, TrackerType.VARBIT_VALUE, new int[]{PluginConstants.ActivityID.BA_COLLECTOR_POINTS}),
	BA_DEFENDER_POINTS(2203, "BA Defender points", Category.MINI_GAMES, TrackerType.VARBIT_VALUE, new int[]{PluginConstants.ActivityID.BA_DEFENDER_POINTS}),
	BA_HEALER_POINTS(2204, "BA Healer points", Category.MINI_GAMES, TrackerType.VARBIT_VALUE, new int[]{PluginConstants.ActivityID.BA_HEALER_POINTS}),
	JAD_CHALLENGE_1(2205, "Jad Challenge 1", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.JAD_CHALLENGE_1}),
	JAD_CHALLENGE_2(2206, "Jad Challenge 2", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.JAD_CHALLENGE_2}),
	JAD_CHALLENGE_3(2207, "Jad Challenge 3", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.JAD_CHALLENGE_3}),
	JAD_CHALLENGE_4(2208, "Jad Challenge 4", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.JAD_CHALLENGE_4}),
	JAD_CHALLENGE_5(2209, "Jad Challenge 5", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.JAD_CHALLENGE_5}),
	JAD_CHALLENGE_6(2210, "Jad Challenge 6", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.JAD_CHALLENGE_6}),
	SOUL_WARS_WINS(2211, "Soul Wars wins", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.SOUL_WARS_WINS}),
	SOUL_WARS_GAMES(2212, "Soul Wars games", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.SOUL_WARS_GAMES}),
	NMZ_POINTS(2213, "NMZ points", Category.MINI_GAMES, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.NMZ_POINTS}),
	PEST_CONTROL_POINTS(2214, "Pest control points", Category.MINI_GAMES, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.PEST_CONTROL_POINTS}),
	TITHE_FARM_POINTS(2215, "Tithe farm points", Category.MINI_GAMES, TrackerType.VARBIT_VALUE, new int[]{PluginConstants.ActivityID.TITHE_FARM_POINTS}),
	GIANTS_FOUNDRY_POINTS(2216, "Giant's foundry points", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.GIANTS_FOUNDRY_POINTS}),

	// --- SKILLING (2300 - 2399) ---
	FISH_CAUGHT(2301, "Fish caught", Category.SKILLING, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.FISH_CAUGHT}),
	LOGS_CHOPPED(2302, "Logs chopped", Category.SKILLING, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.LOGS_CHOPPED}),
	ORE_MINED(2303, "Ore mined", Category.SKILLING, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.ORE_MINED}),
	BIRD_HOUSES(2304, "Bird houses built", Category.SKILLING, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.BIRD_HOUSES}),
	FARMING_CONTRACTS(2305, "Farming Contracts", Category.SKILLING, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.FARMING_CONTRACTS}),
	HUNTER_RUMOURS(2306, "Hunter Rumours", Category.SKILLING, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.HUNTER_RUMOURS}),
	MAHOGANY_HOMES(2307, "Mahogany Homes", Category.SKILLING, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.MAHOGANY_HOMES}),
	MIXOLOGY_ORDERS(2308, "Mixology orders", Category.SKILLING, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.MIXOLOGY_ORDERS}),
	MIXOLOGY_AGA_POINTS(2309, "Mixology Aga points", Category.SKILLING, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.MIXOLOGY_AGA_POINTS}),
	MIXOLOGY_LYE_POINTS(2310, "Mixology Lye points", Category.SKILLING, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.MIXOLOGY_LYE_POINTS}),
	MIXOLOGY_MOX_POINTS(2311, "Mixology Mox points", Category.SKILLING, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.MIXOLOGY_MOX_POINTS}),
	MIXOLOGY_DIGWEED_MATURED(2312, "Mixology Digweed matured", Category.SKILLING, TrackerType.CHAT_MESSAGE, PluginConstants.ActivityID.MIXOLOGY_DIGWEED_MATURED),
	MIXOLOGY_DIGWEED_PICKED(2313, "Mixology Digweed picked", Category.SKILLING, TrackerType.CHAT_MESSAGE, new int[]{-10001}),
	GUARDIANS_OF_THE_RIFT(2314, "Guardians of the Rift", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.GUARDIANS_OF_THE_RIFT}),
	WINTERTODT(2315, "Wintertodt", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.WINTERTODT}),
	ZALCANO(2316, "Zalcano", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.ZALCANO}),
	TEMPOROSS(2317, "Tempoross", Category.MINI_GAMES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.TEMPOROSS}),
	TEMPOROSS_PERMITS(2318, "Reward permits", Category.MINI_GAMES, TrackerType.VARBIT_VALUE, new int[]{VarbitID.TEMPOROSS_REWARDPERMITS}),
	PICKPOCKET_SUCCESS(2319, "Pickpockets succeeded", Category.SKILLING, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.PICKPOCKET_SUCCESS}),
	PICKPOCKET_FAIL(2320, "Pickpockets failed", Category.SKILLING, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.PICKPOCKET_FAIL}),
	PATCHES_COMPOSTED(2321, "Patches composted", Category.SKILLING, TrackerType.SOUND_EFFECT, new int[]{FARMING_COMPOST}),
	SEEDS_DIBBED(2322, "Seeds planted", Category.SKILLING, TrackerType.SOUND_EFFECT, new int[]{SEEDS_PLANTED}),
	HERBS_HARVESTED(2323, "Herbs harvested", Category.SKILLING, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.HERBS_HARVESTED}),
	CROPS_HARVESTED(2324, "Crops harvested", Category.SKILLING, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.CROPS_PICKED_SPADE}),
	WEEDS_RAKED(2325, "Weeds raked", Category.SKILLING, TrackerType.SOUND_EFFECT, new int[]{PluginConstants.ActivityID.WEEDS_RAKED}),
	FRUIT_PICKED(2326, "Fruit picked", Category.SKILLING, TrackerType.SOUND_EFFECT, new int[]{PluginConstants.ActivityID.CROPS_PICKED_FRUIT_TREE}),
	CROP_RESURRECTION_SUCCESS(2327, "Crops resurrected", Category.SKILLING, TrackerType.CUSTOM, new int[]{-10001}),
	CROP_RESURRECTION_FAIL(2328, "Crops not resurrected", Category.SKILLING, TrackerType.CUSTOM, new int[]{-10001}),

	POTIONS_MIXED(2329, "Potions mixed", Category.SKILLING, TrackerType.CHAT_MESSAGE, new int[]{-10002}),
	ALCHEMISTS_AMULET_PROCS(2330, "Amulet of Chemistry procs", Category.SKILLING, TrackerType.CUSTOM, new int[]{-10001}),
	LOGS_BURNT(2331, "Logs burnt", Category.SKILLING, TrackerType.SOUND_EFFECT, new int[]{PluginConstants.ActivityID.LOGS_BURNT}),
	FOOD_COOKED(2332, "Food cooked", Category.SKILLING, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.FOOD_COOKED}),
	SACRIFICES_SPARED(2334, "Offerings spared", Category.SKILLING, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.SACRIFICES_SPARED}),
	SACRIFICES_MADE(2335, "Bones offered", Category.SKILLING, TrackerType.SOUND_EFFECT, new int[]{PluginConstants.ActivityID.SACRIFICES_MADE}),
	BIRD_EGGS_OFFERED(2336, "Bird eggs offered", Category.SKILLING, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.BIRD_EGGS_OFFERED}),

	// --- SUPPLIES (2400 - 2499) ---
	POTIONS_SIPPED(2401, "Potions sipped", Category.SUPPLIES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.POTIONS_SIPPED}),
	FOOD_EATEN(2402, "Food eaten", Category.SUPPLIES, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.FOOD_EATEN}),
	TELEPORT_TABLETS_USED(2403, "Teleport tablets used", Category.SUPPLIES, TrackerType.SOUND_EFFECT, new int[]{PluginConstants.ActivityID.TELEPORT_TABLETS_USED}),
	CANNONBALLS_FIRED(2404, "Cannonballs fired", Category.SUPPLIES, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.CANNONBALLS_FIRED}),
	SCYTHE_SWIPE(2405, "Scythe of Vitur charges", Category.SUPPLIES, TrackerType.SOUND_EFFECT, new int[]{SCYTHE_SLASH, SCYTHE_CRUSH}),
	CANNONS_LOST(2406, "Cannons lost&found", Category.SUPPLIES, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.CANNONS_LOST}),

	// --- ACHIEVEMENTS (2500 - 2599) ---
	ACHIEVEMENT_DIARY_TASKS(2500, "Achievement Diary tasks", Category.ACHIEVEMENTS, TrackerType.VARBIT_VALUE, ACHIEVEMENT_DIARY_TASK_COUNT),
	QUESTS(2501, "Quests", Category.ACHIEVEMENTS, TrackerType.VARBIT_VALUE, new int[]{PluginConstants.ActivityID.QUESTS}),
	QUEST_POINTS(2502, "Quest points", Category.ACHIEVEMENTS, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.QUEST_POINTS}),
	CA_DIARY_TASKS(2503, "CA Diary tasks", Category.ACHIEVEMENTS, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.CA_DIARY_TASKS}),
	CA_DIARY_POINTS(2504, "CA Diary points", Category.ACHIEVEMENTS, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.CA_DIARY_POINTS}),
	MUSIC_TRACKS_UNLOCKED(2505, "Music tracks unlocked", Category.ACHIEVEMENTS, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.MUSIC_TRACKS_UNLOCKED}),
	NEW_COLLECTIONS_LOGGED(2506, "New collections logged", Category.ACHIEVEMENTS, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.NEW_COLLECTIONS_LOGGED}),
	VARROCK_MUSEUM_KUDOS(2507, "Varrock Museum kudos", Category.ACHIEVEMENTS, TrackerType.VARBIT_VALUE, new int[]{PluginConstants.ActivityID.VARROCK_MUSEUM_KUDOS}),

	// --- COMBAT (2600 - 2699) ---
	DAMAGE_DEALT_TO_NPCS(2600, "Damage dealt to NPCs", Category.COMBAT, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DAMAGE_DEALT_TO_NPCS}),
	DAMAGE_TAKEN_FROM_NPCS(2601, "Damage taken from NPCs", Category.COMBAT, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.DAMAGE_TAKEN_FROM_NPCS}),
	SPECIAL_ATTACKS_USED(2602, "Special attacks used", Category.COMBAT, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.SPECIAL_ATTACKS_USED}),
	PLAYER_DEATHS(2603, "Player deaths", Category.COMBAT, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.PLAYER_DEATHS}),
	PLAYER_KILLS(2604, "Player kills", Category.COMBAT, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.PLAYER_KILLS}),
	MONSTER_KILLS(2605, "Monster kills", Category.COMBAT, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.MONSTER_KILLS}),
	MAX_HITS(2606, "Max hits", Category.COMBAT, TrackerType.CUSTOM, new int[]{-1}),


	// --- BOLT PROCS (2700 - 2799) ---
	OPAL_BOLT_PROC(2700, "Opal bolt procs", Category.BOLTS, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.BOLT_OPAL}),
	SAPPHIRE_BOLT_PROC(2701, "Sapphire bolt procs", Category.BOLTS, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.BOLT_SAPPHIRE}),
	JADE_BOLT_PROC(2702, "Jade bolt procs", Category.BOLTS, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.BOLT_JADE}),
	PEARL_BOLT_PROC(2703, "Pearl bolt procs", Category.BOLTS, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.BOLT_PEARL}),
	EMERALD_BOLT_PROC(2704, "Emerald bolt procs", Category.BOLTS, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.BOLT_EMERALD}),
	RED_TOPAZ_BOLT_PROC(2705, "Red topaz bolt procs", Category.BOLTS, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.BOLT_REDTOPAZ}),
	RUBY_BOLT_PROC(2706, "Ruby bolt procs", Category.BOLTS, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.BOLT_RUBY}),
	DIAMOND_BOLT_PROC(2707, "Diamond bolt procs", Category.BOLTS, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.BOLT_DIAMOND}),
	DRAGONSTONE_BOLT_PROC(2708, "Dragonstone bolt procs", Category.BOLTS, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.BOLT_DRAGONSTONE}),
	ONYX_BOLT_PROC(2709, "Onyx bolt procs", Category.BOLTS, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.BOLT_ONYX}),

// --- SAILING (2800 - 2899) ---

	// --- TERTIARY DROPS (2900 - 2999) ---

	// Spells (3000 - 3499)



	// --- RANDOM EVENT (3500 - 3550) ---
	DRUNKEN_DWARF(3500, "Drunken dwarf", Category.RANDOM_EVENTS, TrackerType.CUSTOM, new int[]{-3500}),


	// --- OTHER (9000+) ---
//	GE_TAX_PAID(9000, "", Category.OTHER, TrackerType.CUSTOM, new int[]{-1}),
	COINS_GAINED(9001, "Coins gained", Category.OTHER, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.COINS_GAINED}),
	COINS_LOST(9002, "Coins lost", Category.OTHER, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.COINS_LOST}),
	GEMSTONE_CRAB(9003, "Gemstone Crab", Category.OTHER, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.GEMSTONE_CRAB}),
	SPELLBOOK_CHANGES(9004, "Spellbook changes", Category.OTHER, TrackerType.VARBIT_VALUE, new int[]{PluginConstants.ActivityID.SPELLBOOK_CHANGES});

//	IMPLING_JARS_BROKEN(9010, "Impling jars broken", Category.OTHER, TrackerType.CUSTOM, new int[]{-1});

	private final int id;
	private final String name;
	private final Category category;
	private final TrackerType trackerType;
	private final int[] gameSourceIds;
	private final String configId;

	// Fast lookup map for retrieving TrackedActivity by its Storage ID
	private static final Map<Integer, TrackedActivity> STORAGE_ID_MAP = new HashMap<>();
	//	private static final Map<String, TrackedActivity> STORAGE_NAME_MAP = new HashMap<>();
	private static final Map<Category, TrackedActivity> CATEGORY_ACTIVITY_MAP = new HashMap<>();
	public static final int MAX_TRACKING_ID;

	static
	{
		int curMax = -1;
		for (TrackedActivity activity : values())
		{
			STORAGE_ID_MAP.put(activity.id, activity);
//			STORAGE_NAME_MAP.put(activity.name, activity);
			curMax = activity.getId();
		}
		MAX_TRACKING_ID = curMax;
		log.debug("Initialized a total of {} TrackedActivity instances", values().length);
	}

	TrackedActivity(int id, String name, Category category, TrackerType trackerType, int[] gameSourceIds, int... activityGroupIds)
	{
		this.id = id;
		this.name = name;
		this.category = category;
		this.trackerType = trackerType;
		this.gameSourceIds = gameSourceIds;
		this.configId = category.toString().toLowerCase() + name.replace(" ", "")
			.replace("(", "")
			.replace(")", "")
			.replace("'", "");
	}

	/**
	 * Safely lookup a TrackedActivity by its permanent Storage ID.
	 */
	public static TrackedActivity getByStorageId(int storageId)
	{
		return STORAGE_ID_MAP.get(storageId);
	}
}