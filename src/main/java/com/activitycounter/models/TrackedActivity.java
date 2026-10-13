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
	EXPEDITIOUS_BRACELET_PROCS(1706, "Expeditious bracelet procs", Category.SLAYER, TrackerType.CHAT_MESSAGE, new int[]{-1}),
	SLAUGHTER_BRACELET_PROCS(1707, "Bracelet of slaughter procs", Category.SLAYER, TrackerType.CHAT_MESSAGE, new int[]{-1}),

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
	AMULET_OF_BOUNTY_PROCS(2339, "Amulet of bounty procs", Category.SKILLING, TrackerType.CHAT_MESSAGE, new int[]{}),

	DRIFT_NETS_SET_UP(2337, "Drift net traps built", Category.SKILLING, TrackerType.VARBIT_VALUE, PluginConstants.ActivityID.DRIFT_NET_BUILT_IDS),
	DRIFT_NET_CAUGHT(2338, "Drift net fish caught", Category.SKILLING, TrackerType.VARBIT_VALUE, PluginConstants.ActivityID.DRIFT_NET_CAUGHT_IDS),

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
	SCYTHE_SWIPE(2405, "Scythe of Vitur", Category.SUPPLIES, TrackerType.EQUIPMENT_SOUND_EFFECT, PluginConstants.SoundIDs.SCYTHE_SOUND_EFFECTS),
	TRIDENT_OF_THE_SEAS(2407, "Trident of the sea", Category.SUPPLIES, TrackerType.EQUIPMENT_SOUND_EFFECT, PluginConstants.SoundIDs.POWERED_STAFF_ATTACK),
	TRIDENT_OF_THE_SWAMP(2408, "Trident of the swamp", Category.SUPPLIES, TrackerType.EQUIPMENT_SOUND_EFFECT, PluginConstants.SoundIDs.POWERED_STAFF_ATTACK),
	TUMEKENS_SHADOW(2409, "Tumeken's shadow", Category.SUPPLIES, TrackerType.EQUIPMENT_SOUND_EFFECT, PluginConstants.SoundIDs.POWERED_STAFF_ATTACK),
	SANGUINESTI_STAFF(2410, "Sanguinesti staff", Category.SUPPLIES, TrackerType.EQUIPMENT_SOUND_EFFECT, PluginConstants.SoundIDs.POWERED_STAFF_ATTACK),
	EYE_OF_AYAK(2411, "Eye of ayak", Category.SUPPLIES, TrackerType.EQUIPMENT_SOUND_EFFECT, PluginConstants.SoundIDs.POWERED_STAFF_ATTACK),
//	WILDY_CHAINMACE(2412, "Wildy chainmace", Category.SUPPLIES, TrackerType.EQUIPMENT_SOUND_EFFECT, PluginConstants.SoundIDs.POWERED_STAFF_ATTACK),
//	WILDY_BOW(2413, "Wildy bow", Category.SUPPLIES, TrackerType.EQUIPMENT_SOUND_EFFECT, PluginConstants.SoundIDs.POWERED_STAFF_ATTACK),
//	WILDY_SCEPTRE(2414, "Wildy sceptre", Category.SUPPLIES, TrackerType.EQUIPMENT_SOUND_EFFECT, PluginConstants.SoundIDs.POWERED_STAFF_ATTACK),
	REVENANT_ETHER(2414, "Revenant ether", Category.SUPPLIES, TrackerType.EQUIPMENT_SOUND_EFFECT, PluginConstants.SoundIDs.ETHER_USED),
	CANNONS_LOST(2406, "Cannons lost&found", Category.SUPPLIES, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.CANNONS_LOST}),

	// --- ACHIEVEMENTS (2500 - 2599) ---
	ACHIEVEMENT_DIARY_TASKS(2500, "Achievement Diary tasks", Category.ACHIEVEMENTS, TrackerType.VARBIT_VALUE, ACHIEVEMENT_DIARY_TASK_COUNT),
	QUESTS(2501, "Quests", Category.ACHIEVEMENTS, TrackerType.VARBIT_VALUE, new int[]{PluginConstants.ActivityID.QUESTS}),
	QUEST_POINTS(2502, "Quest points", Category.ACHIEVEMENTS, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.QUEST_POINTS}),
	CA_DIARY_TASKS(2503, "CA Diary tasks", Category.ACHIEVEMENTS, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.CA_DIARY_TASKS}),
	CA_DIARY_POINTS(2504, "CA Diary points", Category.ACHIEVEMENTS, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.CA_DIARY_POINTS}),
	MUSIC_TRACKS_UNLOCKED(2505, "Music tracks unlocked", Category.ACHIEVEMENTS, TrackerType.CHAT_MESSAGE, new int[]{PluginConstants.ActivityID.MUSIC_TRACKS_UNLOCKED}),
	NEW_COLLECTIONS_LOGGED(2506, "New collections logged", Category.ACHIEVEMENTS, TrackerType.CHAT_MESSAGE, new int[]{-1}),
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
	// Runes used (3000-3050)
	AIR_RUNES_CONSUMED(3000, "Air runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	WATER_RUNES_CONSUMED(3001, "Water runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	EARTH_RUNES_CONSUMED(3002, "Earth runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	FIRE_RUNES_CONSUMED(3003, "Fire runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	MIND_RUNES_CONSUMED(3004, "Mind runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CHAOS_RUNES_CONSUMED(3005, "Chaos runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	DEATH_RUNES_CONSUMED(3006, "Death runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	BLOOD_RUNES_CONSUMED(3007, "Blood runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	COSMIC_RUNES_CONSUMED(3008, "Cosmic runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	NATURE_RUNES_CONSUMED(3009, "Nature runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	LAW_RUNES_CONSUMED(3010, "Law runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	BODY_RUNES_CONSUMED(3011, "Body runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SOUL_RUNES_CONSUMED(3012, "Soul runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ASTRAL_RUNES_CONSUMED(3013, "Astral runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	WRATH_RUNES_CONSUMED(3014, "Wrath runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	AETHER_RUNES_CONSUMED(3015, "Aether runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),

	// --- COMBINATION RUNES ---
	MIST_RUNES_CONSUMED(3016, "Mist runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	MUD_RUNES_CONSUMED(3017, "Mud runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	DUST_RUNES_CONSUMED(3018, "Dust runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	LAVA_RUNES_CONSUMED(3019, "Lava runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	STEAM_RUNES_CONSUMED(3020, "Steam runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SMOKE_RUNES_CONSUMED(3021, "Smoke runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SUNFIRE_RUNES_CONSUMED(3022, "Sunfire runes used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),

	ICE_SACKS_CONSUMED(3030, "Ice sacks used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ENTANGLE_SACKS_CONSUMED(3031, "Entangle sacks used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	TELEPORT_SACKS_CONSUMED(3032, "Teleport sacks used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	VENGEANCE_SACKS_CONSUMED(3033, "Vengeance sacks used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SURGE_SACKS_CONSUMED(3034, "Surge sacks used", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),

	// Standard Spellbook (3050 - 3149)
	WIND_STRIKE(3050, "Wind Strike", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CONFUSE(3051, "Confuse", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ENCHANT_CROSSBOW_BOLT_OPAL(3052, "Enchant Crossbow Bolt Opal", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	WATER_STRIKE(3053, "Water Strike", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	LVL_1_ENCHANT(3054, "Lvl 1 Enchant", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ENCHANT_CROSSBOW_BOLT_SAPPHIRE(3055, "Enchant Crossbow Bolt Sapphire", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	EARTH_STRIKE(3056, "Earth Strike", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	WEAKEN(3057, "Weaken", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	FIRE_STRIKE(3058, "Fire Strike", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ENCHANT_CROSSBOW_BOLT_JADE(3059, "Enchant Crossbow Bolt Jade", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	BONES_TO_BANANAS(3060, "Bones To Bananas", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	WIND_BOLT(3061, "Wind Bolt", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CURSE(3062, "Curse", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	BIND(3063, "Bind", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	LOW_LEVEL_ALCHEMY(3064, "Low Level Alchemy", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	WATER_BOLT(3065, "Water Bolt", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ENCHANT_CROSSBOW_BOLT_PEARL(3066, "Enchant Crossbow Bolt Pearl", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	VARROCK_TELEPORT(3067, "Varrock Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ENCHANT_CROSSBOW_BOLT_EMERALD(3068, "Enchant Crossbow Bolt Emerald", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	LVL_2_ENCHANT(3069, "Lvl 2 Enchant", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	EARTH_BOLT(3070, "Earth Bolt", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ENCHANT_CROSSBOW_BOLT_RED_TOPAZ(3071, "Enchant Crossbow Bolt Red Topaz", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	LUMBRIDGE_TELEPORT(3072, "Lumbridge Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	TELEKINETIC_GRAB(3073, "Telekinetic Grab", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	FIRE_BOLT(3074, "Fire Bolt", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	FALADOR_TELEPORT(3075, "Falador Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CRUMBLE_UNDEAD(3076, "Crumble Undead", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	TELEPORT_TO_HOUSE(3077, "Teleport To House", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	WIND_BLAST(3078, "Wind Blast", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	MONSTER_INSPECT(3079, "Monster Inspect", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SUPERHEAT_ITEM(3080, "Superheat Item", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CAMELOT_TELEPORT(3081, "Camelot Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	WATER_BLAST(3082, "Water Blast", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	KOUREND_CASTLE_TELEPORT(3083, "Kourend Castle Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	LVL_3_ENCHANT(3084, "Lvl 3 Enchant", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ENCHANT_CROSSBOW_BOLT_RUBY(3085, "Enchant Crossbow Bolt Ruby", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	IBAN_BLAST(3086, "Iban Blast", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SNARE(3087, "Snare", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	MAGIC_DART(3088, "Magic Dart", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ARDOUGNE_TELEPORT(3089, "Ardougne Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	EARTH_BLAST(3090, "Earth Blast", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CIVITAS_ILLA_FORTIS_TELEPORT(3091, "Civitas Illa Fortis Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	HIGH_LEVEL_ALCHEMY(3092, "High Level Alchemy", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CHARGE_WATER_ORB(3093, "Charge Water Orb", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SUMMON_BOAT(3094, "Summon Boat", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	LVL_4_ENCHANT(3095, "Lvl 4 Enchant", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ENCHANT_CROSSBOW_BOLT_DIAMOND(3096, "Enchant Crossbow Bolt Diamond", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	WATCHTOWER_TELEPORT(3097, "Watchtower Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	FIRE_BLAST(3098, "Fire Blast", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CLAWS_OF_GUTHIX(3099, "Claws Of Guthix", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	FLAMES_OF_ZAMORAK(3100, "Flames Of Zamorak", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CHARGE_EARTH_ORB(3101, "Charge Earth Orb", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	BONES_TO_PEACHES(3102, "Bones To Peaches", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SARADOMIN_STRIKE(3103, "Saradomin Strike", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	TROLLHEIM_TELEPORT(3104, "Trollheim Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	WIND_WAVE(3105, "Wind Wave", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CHARGE_FIRE_ORB(3106, "Charge Fire Orb", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	REGULAR_APE_ATOLL_TELEPORT(3107, "Regular Ape Atoll Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	WATER_WAVE(3108, "Water Wave", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	VULNERABILITY(3109, "Vulnerability", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CHARGE_AIR_ORB(3110, "Charge Air Orb", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	TELEPORT_TO_BOAT(3111, "Teleport To Boat", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	LVL_5_ENCHANT(3112, "Lvl 5 Enchant", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ENCHANT_CROSSBOW_BOLT_DRAGONSTONE(3113, "Enchant Crossbow Bolt Dragonstone", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	EARTH_WAVE(3114, "Earth Wave", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ENFEEBLE(3115, "Enfeeble", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	TELEOTHER_LUMBRIDGE(3116, "Teleother Lumbridge", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	FIRE_WAVE(3117, "Fire Wave", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ENTANGLE(3118, "Entangle", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CHARGE(3119, "Charge", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	STUN(3120, "Stun", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	WIND_SURGE(3121, "Wind Surge", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	TELEOTHER_FALADOR(3122, "Teleother Falador", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	WATER_SURGE(3123, "Water Surge", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	TELE_BLOCK(3124, "Tele Block", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	REGULAR_TELEPORT_TO_TARGET(3125, "Regular Teleport To Target", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	LVL_6_ENCHANT(3126, "Lvl 6 Enchant", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ENCHANT_CROSSBOW_BOLT_ONYX(3127, "Enchant Crossbow Bolt Onyx", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	TELEOTHER_CAMELOT(3128, "Teleother Camelot", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	EARTH_SURGE(3129, "Earth Surge", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	LVL_7_ENCHANT(3130, "Lvl 7 Enchant", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	FIRE_SURGE(3131, "Fire Surge", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),

	// Ancient Magicks (3150 - 3249)
	SMOKE_RUSH(3150, "Smoke Rush", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SHADOW_RUSH(3151, "Shadow Rush", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	PADDEWWA_TELEPORT(3152, "Paddewwa Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	BLOOD_RUSH(3153, "Blood Rush", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ICE_RUSH(3154, "Ice Rush", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SENNTISTEN_TELEPORT(3155, "Senntisten Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SMOKE_BURST(3156, "Smoke Burst", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SHADOW_BURST(3157, "Shadow Burst", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	KHARYRLL_TELEPORT(3158, "Kharyrll Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	BLOOD_BURST(3159, "Blood Burst", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ICE_BURST(3160, "Ice Burst", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	LASSAR_TELEPORT(3161, "Lassar Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SMOKE_BLITZ(3162, "Smoke Blitz", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SHADOW_BLITZ(3163, "Shadow Blitz", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	DAREEYAK_TELEPORT(3164, "Dareeyak Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	BLOOD_BLITZ(3165, "Blood Blitz", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ICE_BLITZ(3166, "Ice Blitz", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CARRALLANGER_TELEPORT(3167, "Carrallanger Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ANCIENT_TELEPORT_TO_TARGET(3168, "Ancient Teleport To Target", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SMOKE_BARRAGE(3169, "Smoke Barrage", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SHADOW_BARRAGE(3170, "Shadow Barrage", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ANNAKARL_TELEPORT(3171, "Annakarl Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	BLOOD_BARRAGE(3172, "Blood Barrage", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ICE_BARRAGE(3173, "Ice Barrage", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	GHORROCK_TELEPORT(3174, "Ghorrock Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),

	// Lunar Spellbook (3250 - 3349)
	BAKE_PIE(3250, "Bake Pie", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	GEOMANCY(3251, "Geomancy", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CURE_PLANT(3252, "Cure Plant", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	MONSTER_EXAMINE(3253, "Monster Examine", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ASTRAL_CONTACT(3254, "Astral Contact", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CURE_OTHER(3255, "Cure Other", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	HUMIDIFY(3256, "Humidify", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	MOONCLAN_TELEPORT(3257, "Moonclan Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	TELE_GROUP_MOONCLAN(3258, "Tele Group Moonclan", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	HUNTER_KIT(3259, "Hunter Kit", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CURE_ME(3260, "Cure Me", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	OURANIA_TELEPORT(3261, "Ourania Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	WATERBIRTH_TELEPORT(3262, "Waterbirth Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	TELE_GROUP_WATERBIRTH(3263, "Tele Group Waterbirth", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CURE_GROUP(3264, "Cure Group", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	STAT_SPY(3265, "Stat Spy", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	BARBARIAN_TELEPORT(3266, "Barbarian Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	TELE_GROUP_BARBARIAN(3267, "Tele Group Barbarian", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SPIN_FLAX(3268, "Spin Flax", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SUPERGLASS_MAKE(3269, "Superglass Make", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	KHAZARD_TELEPORT(3270, "Khazard Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	TAN_LEATHER(3271, "Tan Leather", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	TELE_GROUP_KHAZARD(3272, "Tele Group Khazard", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	DREAM(3273, "Dream", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	STRING_JEWELLERY(3274, "String Jewellery", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	STAT_RESTORE_POT_SHARE(3275, "Stat Restore Pot Share", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	MAGIC_IMBUE(3276, "Magic Imbue", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	FERTILE_SOIL(3277, "Fertile Soil", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	BOOST_POTION_SHARE(3278, "Boost Potion Share", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	FISHING_GUILD_TELEPORT(3279, "Fishing Guild Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	LUNAR_TELEPORT_TO_TARGET(3280, "Lunar Teleport To Target", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	PLANK_MAKE(3281, "Plank Make", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	TELE_GROUP_FISHING_GUILD(3282, "Tele Group Fishing Guild", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CATHERBY_TELEPORT(3283, "Catherby Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	TELE_GROUP_CATHERBY(3284, "Tele Group Catherby", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	RECHARGE_DRAGONSTONE(3285, "Recharge Dragonstone", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ICE_PLATEAU_TELEPORT(3286, "Ice Plateau Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	TELE_GROUP_ICE_PLATEAU(3287, "Tele Group Ice Plateau", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ENERGY_TRANSFER(3288, "Energy Transfer", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	HEAL_OTHER(3289, "Heal Other", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	VENGEANCE_OTHER(3290, "Vengeance Other", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	VENGEANCE(3291, "Vengeance", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	HEAL_GROUP(3292, "Heal Group", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SPELLBOOK_SWAP(3293, "Spellbook Swap", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),

	// Arceuus Spellbook (3350 - 3449)
	ARCEUUS_LIBRARY_TELEPORT(3350, "Arceuus Library Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	BASIC_REANIMATION(3351, "Basic Reanimation", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	DRAYNOR_MANOR_TELEPORT(3352, "Draynor Manor Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	BATTLEFRONT_TELEPORT(3353, "Battlefront Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	MIND_ALTAR_TELEPORT(3354, "Mind Altar Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	RESPAWN_TELEPORT(3355, "Respawn Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	GHOSTLY_GRASP(3356, "Ghostly Grasp", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	RESURRECT_LESSER_ZOMBIE(3357, "Resurrect Lesser Zombie", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	RESURRECT_LESSER_SKELETON(3358, "Resurrect Lesser Skeleton", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	RESURRECT_LESSER_GHOST(3359, "Resurrect Lesser Ghost", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SALVE_GRAVEYARD_TELEPORT(3360, "Salve Graveyard Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ADEPT_REANIMATION(3361, "Adept Reanimation", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	INFERIOR_DEMONBANE(3362, "Inferior Demonbane", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SHADOW_VEIL(3363, "Shadow Veil", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	FENKENSTRAIN_S_CASTLE_TELEPORT(3364, "Fenkenstrain S Castle Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	DARK_LURE(3365, "Dark Lure", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SKELETAL_GRASP(3366, "Skeletal Grasp", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	RESURRECT_SUPERIOR_GHOST(3367, "Resurrect Superior Ghost", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	RESURRECT_SUPERIOR_SKELETON(3368, "Resurrect Superior Skeleton", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	RESURRECT_SUPERIOR_ZOMBIE(3369, "Resurrect Superior Zombie", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	MARK_OF_DARKNESS(3370, "Mark Of Darkness", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	WEST_ARDOUGNE_TELEPORT(3371, "West Ardougne Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SUPERIOR_DEMONBANE(3372, "Superior Demonbane", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	LESSER_CORRUPTION(3373, "Lesser Corruption", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	HARMONY_ISLAND_TELEPORT(3374, "Harmony Island Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	VILE_VIGOUR(3375, "Vile Vigour", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	DEGRIME(3376, "Degrime", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	CEMETERY_TELEPORT(3377, "Cemetery Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	EXPERT_REANIMATION(3378, "Expert Reanimation", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	WARD_OF_ARCEUUS(3379, "Ward Of Arceuus", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	RESURRECT_GREATER_SKELETON(3380, "Resurrect Greater Skeleton", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	RESURRECT_GREATER_ZOMBIE(3381, "Resurrect Greater Zombie", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	RESURRECT_GREATER_GHOST(3382, "Resurrect Greater Ghost", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	RESURRECT_CROPS(3383, "Resurrect Crops", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	UNDEAD_GRASP(3384, "Undead Grasp", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	DEATH_CHARGE(3385, "Death Charge", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	DARK_DEMONBANE(3386, "Dark Demonbane", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	BARROWS_TELEPORT(3387, "Barrows Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	DEMONIC_OFFERING(3388, "Demonic Offering", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	GREATER_CORRUPTION(3389, "Greater Corruption", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ARCEUUS_TELEPORT_TO_TARGET(3390, "Arceuus Teleport To Target", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	ARCEUUS_APE_ATOLL_TELEPORT(3391, "Arceuus Ape Atoll Teleport", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	MASTER_REANIMATION(3392, "Master Reanimation", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),
	SINISTER_OFFERING(3393, "Sinister Offering", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),

	SPLASH_CAST(3399, "Splash cast (miss)", Category.MAGIC_SPELLS, TrackerType.CUSTOM, new int[]{-1}),




	// --- RANDOM EVENT (3500 - 3550) ---
	DRUNKEN_DWARF(3500, "Drunken dwarf", Category.RANDOM_EVENTS, TrackerType.CHAT_MESSAGE, new int[]{-3500}),
	GENIE(3500, "Genie", Category.RANDOM_EVENTS, TrackerType.CHAT_MESSAGE, new int[]{-3500}),


	// --- OTHER (9000+) ---
	GE_TAX_PAID(9000, "GE Tax Paid", Category.OTHER, TrackerType.CUSTOM, new int[]{-1}),
	COINS_GAINED(9001, "Coins gained", Category.OTHER, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.COINS_GAINED}),
	COINS_LOST(9002, "Coins lost", Category.OTHER, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.COINS_LOST}),
	GEMSTONE_CRAB(9003, "Gemstone Crab", Category.OTHER, TrackerType.VARPLAYER_VALUE, new int[]{PluginConstants.ActivityID.GEMSTONE_CRAB}),
	SPELLBOOK_CHANGES(9004, "Spellbook changes", Category.OTHER, TrackerType.CUSTOM, new int[]{PluginConstants.ActivityID.SPELLBOOK_CHANGES});

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