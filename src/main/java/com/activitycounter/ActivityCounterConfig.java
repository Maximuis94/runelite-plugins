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

import static com.activitycounter.PluginConstants.CONFIG_GROUP;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(CONFIG_GROUP)
public interface ActivityCounterConfig extends Config {

	// --- SECTIONS ---

	@ConfigSection(
		name = "General",
		description = "General plugin settings",
		position = 0
	)
	String generalSection = "generalSection";

	@ConfigSection(
		name = "Category display order",
		description = "Define the display order of categories in the panel (lower numbers appear first)",
		position = 1,
		closedByDefault = true
	)
	String categorySortingSection = "categorySortingSection";

	@ConfigSection(
		name = "Bosses",
		description = "Tracked Bosses",
		position = 2,
		closedByDefault = true
	)
	String bossesSection = "bossesSection";

	@ConfigSection(
		name = "Chests",
		description = "Tracked Chests",
		position = 3,
		closedByDefault = true
	)
	String chestsSection = "chestsSection";

	@ConfigSection(
		name = "Experience",
		description = "Tracked Skill Experience",
		position = 4,
		closedByDefault = true
	)
	String experienceSection = "experienceSection";

	@ConfigSection(
		name = "Levels",
		description = "Tracked Skill Levels",
		position = 5,
		closedByDefault = true
	)
	String levelSection = "levelSection";

	@ConfigSection(
		name = "Other",
		description = "Other tracked activities",
		position = 6,
		closedByDefault = true
	)
	String otherSection = "otherSection";

	// --- GENERAL SETTINGS ---

	@ConfigItem(
		keyName = "showSessionDuration",
		name = "Show session duration",
		description = "If checked, display the duration of the session for the actively tracked session.",
		position = 0,
		section = generalSection
	)
	default boolean showSessionDuration() { return true; }

	@ConfigItem(
		keyName = "confirmTerminateSession",
		name = "Confirm session termination",
		description = "If checked, ask for confirmation when attempting to terminate the active session<br>" +
			"to prevent accidentally terminating a session.",
		position = 1,
		section = generalSection
	)
	default boolean confirmTerminateSession() { return false; }

	@ConfigItem(
		keyName = "mergeSlayerTaskCounts ",
		name = "Merge Slayer task counts",
		description = "If checked, show a single, merged slayer task count instead of 3.",
		position = 2,
		section = generalSection
	)
	default boolean mergeSlayerTaskCounts () { return false; }

	// --- SORTING ---

	@ConfigItem(
		keyName = "sortBosses",
		name = "Bosses",
		description = "Sort position for Bosses",
		position = 1,
		section = categorySortingSection
	)
	default int sortBosses() { return 1; }

	@ConfigItem(
		keyName = "sortChests",
		name = "Chests",
		description = "Sort position for Chests",
		position = 2,
		section = categorySortingSection
	)
	default int sortChests() { return 2; }

	@ConfigItem(
		keyName = "sortClue",
		name = "Clue Scrolls",
		description = "Sort position for Clue Scrolls",
		position = 3,
		section = categorySortingSection
	)
	default int sortClue() { return 3; }

	@ConfigItem(
		keyName = "sortSlayer",
		name = "Slayer",
		description = "Sort position for Slayer",
		position = 4,
		section = categorySortingSection
	)
	default int sortSlayer() { return 4; }

	@ConfigItem(
		keyName = "sortAgility",
		name = "Agility",
		description = "Sort position for Agility",
		position = 5,
		section = categorySortingSection
	)
	default int sortAgility() { return 5; }

	@ConfigItem(
		keyName = "sortExperience",
		name = "Experience",
		description = "Sort position for Experience",
		position = 6,
		section = categorySortingSection
	)
	default int sortExperience() { return 6; }

	@ConfigItem(
		keyName = "sortLevels",
		name = "Levels",
		description = "Sort position for Levels",
		position = 7,
		section = categorySortingSection
	)
	default int sortLevels() { return 7; }

	@ConfigItem(
		keyName = "sortOther",
		name = "Other",
		description = "Sort position for Other activities",
		position = 8,
		section = categorySortingSection
	)
	default int sortOther() { return 8; }

	// --- BOSSES ---

	@ConfigItem(
		keyName = "trackCowBoss",
		name = "Brutus",
		description = "If checked, Brutus kills are tracked",
		position = 0,
		section = bossesSection
	)
	default boolean trackCowBoss() { return true; }

	@ConfigItem(
		keyName = "trackObor",
		name = "Obor",
		description = "If checked, Obor kills are tracked",
		position = 1,
		section = bossesSection
	)
	default boolean trackObor() { return true; }

	@ConfigItem(
		keyName = "trackBryophyta",
		name = "Bryophyta",
		description = "If checked, Bryophyta kills are tracked",
		position = 2,
		section = bossesSection
	)
	default boolean trackBryophyta() { return true; }

	@ConfigItem(
		keyName = "trackMimic",
		name = "Mimic",
		description = "If checked, Mimic kills are tracked",
		position = 3,
		section = bossesSection
	)
	default boolean trackMimic() { return true; }

	@ConfigItem(
		keyName = "trackScurrius",
		name = "Scurrius",
		description = "If checked, Scurrius kills are tracked",
		position = 4,
		section = bossesSection
	)
	default boolean trackScurrius() { return true; }

	@ConfigItem(
		keyName = "trackChaosFanatic",
		name = "Chaos Fanatic",
		description = "If checked, Chaos Fanatic kills are tracked",
		position = 5,
		section = bossesSection
	)
	default boolean trackChaosFanatic() { return true; }

	@ConfigItem(
		keyName = "trackCrazyArchaeologist",
		name = "Crazy Archaeologist",
		description = "If checked, Crazy Archaeologist kills are tracked",
		position = 6,
		section = bossesSection
	)
	default boolean trackCrazyArchaeologist() { return true; }

	@ConfigItem(
		keyName = "trackDerangedArchaeologist",
		name = "Deranged Archaeologist",
		description = "If checked, Deranged Archaeologist kills are tracked",
		position = 6,
		section = bossesSection
	)
	default boolean trackDerangedArchaeologist() { return true; }

	@ConfigItem(
		keyName = "trackScorpia",
		name = "Scorpia",
		description = "If checked, Scorpia kills are tracked",
		position = 7,
		section = bossesSection
	)
	default boolean trackScorpia() { return true; }

	@ConfigItem(
		keyName = "trackGiantMole",
		name = "Giant Mole",
		description = "If checked, Giant Mole kills are tracked",
		position = 8,
		section = bossesSection
	)
	default boolean trackGiantMole() { return true; }

	@ConfigItem(
		keyName = "trackGryphonBoss",
		name = "Gryphon Boss",
		description = "If checked, Gryphon Boss kills are tracked",
		position = 9,
		section = bossesSection
	)
	default boolean trackGryphonBoss() { return true; }

	@ConfigItem(
		keyName = "trackGrotesqueGuardians",
		name = "Grotesque Guardians",
		description = "If checked, Grotesque Guardians kills are tracked",
		position = 10,
		section = bossesSection
	)
	default boolean trackGrotesqueGuardians() { return true; }

	@ConfigItem(
		keyName = "trackAmoxliatl",
		name = "Amoxliatl",
		description = "If checked, Amoxliatl kills are tracked",
		position = 11,
		section = bossesSection
	)
	default boolean trackAmoxliatl() { return true; }

	@ConfigItem(
		keyName = "trackCalvarion",
		name = "Calvar'ion",
		description = "If checked, Calvar'ion kills are tracked",
		position = 12,
		section = bossesSection
	)
	default boolean trackCalvarion() { return true; }

	@ConfigItem(
		keyName = "trackKingBlackDragon",
		name = "King Black Dragon",
		description = "If checked, King Black Dragon kills are tracked",
		position = 13,
		section = bossesSection
	)
	default boolean trackKingBlackDragon() { return true; }

	@ConfigItem(
		keyName = "trackHespori",
		name = "Hespori",
		description = "If checked, Hespori kills are tracked",
		position = 14,
		section = bossesSection
	)
	default boolean trackHespori() { return true; }

	@ConfigItem(
		keyName = "trackKraken",
		name = "Kraken",
		description = "If checked, Kraken kills are tracked",
		position = 15,
		section = bossesSection
	)
	default boolean trackKraken() { return true; }

	@ConfigItem(
		keyName = "trackThermonuclearSmokeDevil",
		name = "Thermonuclear Smoke Devil",
		description = "If checked, Thermonuclear Smoke Devil kills are tracked",
		position = 16,
		section = bossesSection
	)
	default boolean trackThermonuclearSmokeDevil() { return true; }

	@ConfigItem(
		keyName = "trackSpindel",
		name = "Spindel",
		description = "If checked, Spindel kills are tracked",
		position = 17,
		section = bossesSection
	)
	default boolean trackSpindel() { return true; }

	@ConfigItem(
		keyName = "trackDagannothPrime",
		name = "Dagannoth Prime",
		description = "If checked, Dagannoth Prime kills are tracked",
		position = 18,
		section = bossesSection
	)
	default boolean trackDagannothPrime() { return true; }

	@ConfigItem(
		keyName = "trackDagannothRex",
		name = "Dagannoth Rex",
		description = "If checked, Dagannoth Rex kills are tracked",
		position = 19,
		section = bossesSection
	)
	default boolean trackDagannothRex() { return true; }

	@ConfigItem(
		keyName = "trackDagannothSupreme",
		name = "Dagannoth Supreme",
		description = "If checked, Dagannoth Supreme kills are tracked",
		position = 20,
		section = bossesSection
	)
	default boolean trackDagannothSupreme() { return true; }

	@ConfigItem(
		keyName = "trackCerberus",
		name = "Cerberus",
		description = "If checked, Cerberus kills are tracked",
		position = 21,
		section = bossesSection
	)
	default boolean trackCerberus() { return true; }

	@ConfigItem(
		keyName = "trackSarachnis",
		name = "Sarachnis",
		description = "If checked, Sarachnis kills are tracked",
		position = 22,
		section = bossesSection
	)
	default boolean trackSarachnis() { return true; }

	@ConfigItem(
		keyName = "trackSkotizo",
		name = "Skotizo",
		description = "If checked, Skotizo kills are tracked",
		position = 23,
		section = bossesSection
	)
	default boolean trackSkotizo() { return true; }

	@ConfigItem(
		keyName = "trackArtio",
		name = "Artio",
		description = "If checked, Artio kills are tracked",
		position = 24,
		section = bossesSection
	)
	default boolean trackArtio() { return true; }

	@ConfigItem(
		keyName = "trackKalphiteQueen",
		name = "Kalphite Queen",
		description = "If checked, Kalphite Queen kills are tracked",
		position = 25,
		section = bossesSection
	)
	default boolean trackKalphiteQueen() { return true; }

	@ConfigItem(
		keyName = "trackAbyssalSire",
		name = "Abyssal Sire",
		description = "If checked, Abyssal Sire kills are tracked",
		position = 26,
		section = bossesSection
	)
	default boolean trackAbyssalSire() { return true; }

	@ConfigItem(
		keyName = "trackRoyalTitan",
		name = "Royal Titan",
		description = "If checked, Royal Titan kills are tracked",
		position = 27,
		section = bossesSection
	)
	default boolean trackRoyalTitan() { return true; }

	@ConfigItem(
		keyName = "trackAlchemicalHydra",
		name = "Alchemical Hydra",
		description = "If checked, Alchemical Hydra kills are tracked",
		position = 28,
		section = bossesSection
	)
	default boolean trackAlchemicalHydra() { return true; }

	@ConfigItem(
		keyName = "trackVetion",
		name = "Vet'ion",
		description = "If checked, Vet'ion kills are tracked",
		position = 29,
		section = bossesSection
	)
	default boolean trackVetion() { return true; }

	@ConfigItem(
		keyName = "trackVenenatis",
		name = "Venenatis",
		description = "If checked, Venenatis kills are tracked",
		position = 30,
		section = bossesSection
	)
	default boolean trackVenenatis() { return true; }

	@ConfigItem(
		keyName = "trackCallisto",
		name = "Callisto",
		description = "If checked, Callisto kills are tracked",
		position = 31,
		section = bossesSection
	)
	default boolean trackCallisto() { return true; }

	@ConfigItem(
		keyName = "trackChaosElemental",
		name = "Chaos Elemental",
		description = "If checked, Chaos Elemental kills are tracked",
		position = 32,
		section = bossesSection
	)
	default boolean trackChaosElemental() { return true; }

	@ConfigItem(
		keyName = "trackArmadyl",
		name = "Kree'arra",
		description = "If checked, Kree'arra kills are tracked",
		position = 33,
		section = bossesSection
	)
	default boolean trackArmadyl() { return true; }

	@ConfigItem(
		keyName = "trackMadAngel",
		name = "Mad Angel",
		description = "If checked, Mad Angel kills are tracked",
		position = 34,
		section = bossesSection
	)
	default boolean trackMadAngel() { return true; }

	@ConfigItem(
		keyName = "trackSaradomin",
		name = "Commander Zilyana",
		description = "If checked, Commander Zilyana kills are tracked",
		position = 35,
		section = bossesSection
	)
	default boolean trackSaradomin() { return true; }

	@ConfigItem(
		keyName = "trackBandos",
		name = "General Graardor",
		description = "If checked, General Graardor kills are tracked",
		position = 36,
		section = bossesSection
	)
	default boolean trackBandos() { return true; }

	@ConfigItem(
		keyName = "trackTheHueycoatl",
		name = "The Hueycoatl",
		description = "If checked, The Hueycoatl kills are tracked",
		position = 37,
		section = bossesSection
	)
	default boolean trackTheHueycoatl() { return true; }

	@ConfigItem(
		keyName = "trackZamorak",
		name = "K'ril Tsutsaroth",
		description = "If checked, K'ril Tsutsaroth kills are tracked",
		position = 38,
		section = bossesSection
	)
	default boolean trackZamorak() { return true; }

	@ConfigItem(
		keyName = "trackTzTokJad",
		name = "TzTok-Jad",
		description = "If checked, TzTok-Jad kills are tracked",
		position = 39,
		section = bossesSection
	)
	default boolean trackTzTokJad() { return true; }

	@ConfigItem(
		keyName = "trackZulrah",
		name = "Zulrah",
		description = "If checked, Zulrah kills are tracked",
		position = 40,
		section = bossesSection
	)
	default boolean trackZulrah() { return true; }

	@ConfigItem(
		keyName = "trackVorkath",
		name = "Vorkath",
		description = "If checked, Vorkath kills are tracked",
		position = 41,
		section = bossesSection
	)
	default boolean trackVorkath() { return true; }

	@ConfigItem(
		keyName = "trackPhantomMuspah",
		name = "Phantom Muspah",
		description = "If checked, Phantom Muspah kills are tracked",
		position = 42,
		section = bossesSection
	)
	default boolean trackPhantomMuspah() { return true; }

	@ConfigItem(
		keyName = "trackMaggotKing",
		name = "Maggot King",
		description = "If checked, Maggot King kills are tracked",
		position = 43,
		section = bossesSection
	)
	default boolean trackMaggotKing() { return true; }

	@ConfigItem(
		keyName = "trackDukeSucellus",
		name = "Duke Sucellus",
		description = "If checked, Duke Sucellus kills are tracked",
		position = 44,
		section = bossesSection
	)
	default boolean trackDukeSucellus() { return true; }

	@ConfigItem(
		keyName = "trackVardorvis",
		name = "Vardorvis",
		description = "If checked, Vardorvis kills are tracked",
		position = 45,
		section = bossesSection
	)
	default boolean trackVardorvis() { return true; }

	@ConfigItem(
		keyName = "trackCorporealBeast",
		name = "Corporeal Beast",
		description = "If checked, Corporeal Beast kills are tracked",
		position = 46,
		section = bossesSection
	)
	default boolean trackCorporealBeast() { return true; }

	@ConfigItem(
		keyName = "trackTheWhisperer",
		name = "The Whisperer",
		description = "If checked, The Whisperer kills are tracked",
		position = 47,
		section = bossesSection
	)
	default boolean trackTheWhisperer() { return true; }

	@ConfigItem(
		keyName = "trackTheLeviathan",
		name = "The Leviathan",
		description = "If checked, The Leviathan kills are tracked",
		position = 48,
		section = bossesSection
	)
	default boolean trackTheLeviathan() { return true; }

	@ConfigItem(
		keyName = "trackTheNightmare",
		name = "The Nightmare",
		description = "If checked, The Nightmare kills are tracked",
		position = 49,
		section = bossesSection
	)
	default boolean trackTheNightmare() { return true; }

	@ConfigItem(
		keyName = "trackAraxxor",
		name = "Araxxor",
		description = "If checked, Araxxor kills are tracked",
		position = 50,
		section = bossesSection
	)
	default boolean trackAraxxor() { return true; }

	@ConfigItem(
		keyName = "trackNex",
		name = "Nex",
		description = "If checked, Nex kills are tracked",
		position = 51,
		section = bossesSection
	)
	default boolean trackNex() { return true; }

	@ConfigItem(
		keyName = "trackPhosanisNightmare",
		name = "Phosani's Nightmare",
		description = "If checked, Phosani's Nightmare kills are tracked",
		position = 52,
		section = bossesSection
	)
	default boolean trackPhosanisNightmare() { return true; }

	@ConfigItem(
		keyName = "trackCowBossHardMode",
		name = "Demonic Brutus",
		description = "If checked, Demonic Brutus kills are tracked",
		position = 53,
		section = bossesSection
	)
	default boolean trackCowBossHardMode() { return true; }

	@ConfigItem(
		keyName = "trackYama",
		name = "Yama",
		description = "If checked, Yama kills are tracked",
		position = 54,
		section = bossesSection
	)
	default boolean trackYama() { return true; }

	@ConfigItem(
		keyName = "trackTzKalZuk",
		name = "TzKal-Zuk",
		description = "If checked, TzKal-Zuk kills are tracked",
		position = 55,
		section = bossesSection
	)
	default boolean trackTzKalZuk() { return true; }

	@ConfigItem(
		keyName = "trackColosseum",
		name = "Fortis Colosseum",
		description = "If checked, Colosseum (wave) completions are tracked",
		position = 56,
		section = bossesSection
	)
	default boolean trackColosseum() { return true; }

	@ConfigItem(
		keyName = "trackDomLevels",
		name = "Doom of Mokhaiotl",
		description = "If checked, Doom of Mokhaiotl delves are tracked",
		position = 57,
		section = bossesSection
	)
	default boolean trackDomLevels() { return true; }

	// --- CHESTS ---

	@ConfigItem(
		keyName = "trackBarrowsChests",
		name = "Barrows Chests",
		description = "If checked, Barrows Chests are tracked",
		position = 0,
		section = chestsSection
	)
	default boolean trackBarrowsChests() { return true; }

	@ConfigItem(
		keyName = "trackChambersOfXeric",
		name = "Chambers of Xeric",
		description = "If checked, Chambers of Xeric completions are tracked",
		position = 1,
		section = chestsSection
	)
	default boolean trackChambersOfXeric() { return true; }

	@ConfigItem(
		keyName = "trackTheatreOfBlood",
		name = "Theatre of Blood",
		description = "If checked, Theatre of Blood completions are tracked",
		position = 2,
		section = chestsSection
	)
	default boolean trackTheatreOfBlood() { return true; }

	@ConfigItem(
		keyName = "trackTheGauntlet",
		name = "The Gauntlet",
		description = "If checked, The (Corrupted) Gauntlet completions are tracked",
		position = 3,
		section = chestsSection
	)
	default boolean trackTheGauntlet() { return true; }

	@ConfigItem(
		keyName = "trackTombsOfAmascut",
		name = "Tombs of Amascut",
		description = "If checked, Tombs of Amascut completions are tracked",
		position = 4,
		section = chestsSection
	)
	default boolean trackTombsOfAmascut() { return true; }

	@ConfigItem(
		keyName = "trackPerilousMoonsChests",
		name = "Perilous Moons Chests",
		description = "If checked, Perilous Moons Chests are tracked",
		position = 5,
		section = chestsSection
	)
	default boolean trackPerilousMoonsChests() { return true; }

	// --- OTHER ---

	@ConfigItem(
		keyName = "trackWintertodt",
		name = "Wintertodt",
		description = "If checked, Wintertodt kills are tracked",
		position = 0,
		section = otherSection
	)
	default boolean trackWintertodt() { return true; }

	@ConfigItem(
		keyName = "trackZalcano",
		name = "Zalcano",
		description = "If checked, Zalcano kills are tracked",
		position = 1,
		section = otherSection
	)
	default boolean trackZalcano() { return true; }

	@ConfigItem(
		keyName = "trackTempoross",
		name = "Tempoross",
		description = "If checked, Tempoross kills are tracked",
		position = 2,
		section = otherSection
	)
	default boolean trackTempoross() { return true; }

	@ConfigItem(
		keyName = "trackGuardiansOfTheRift",
		name = "Guardians of the Rift",
		description = "If checked, Guardians of the Rift completions are tracked",
		position = 3,
		section = otherSection
	)
	default boolean trackGuardiansOfTheRift() { return true; }

	@ConfigItem(
		keyName = "trackJadChallenges",
		name = "Jad challenges",
		description = "If checked, Jad challenge completion counts are tracked",
		position = 4,
		section = otherSection
	)
	default boolean trackJadChallenges() { return true; }

	@ConfigItem(
		keyName = "trackGemstoneCrab",
		name = "Gemstone Crab",
		description = "If checked, Gemstone Crab kills are tracked",
		position = 5,
		section = otherSection
	)
	default boolean trackGemstoneCrab() { return true; }

	@ConfigItem(
		keyName = "trackSoulWars",
		name = "Soul wars",
		description = "If checked, Soul Wars completions are tracked",
		position = 6,
		section = otherSection
	)
	default boolean trackSoulWars() { return true; }

	@ConfigItem(
		keyName = "trackHunterRumours",
		name = "Hunter rumours",
		description = "If checked, completed hunter rumours are tracked.",
		position = 7,
		section = otherSection
	)
	default boolean trackHunterRumours() { return true; }

	@ConfigItem(
		keyName = "trackFarmingContracts",
		name = "Farming contracts",
		description = "If checked, completed farming contracts are tracked.",
		position = 8,
		section = otherSection
	)
	default boolean trackFarmingContracts() { return true; }

	@ConfigItem(
		keyName = "trackMahoganyHomesContracts",
		name = "Mahogany homes",
		description = "If checked, completed mahogany homes contracts are tracked.",
		position = 9,
		section = otherSection
	)
	default boolean trackMahoganyHomesContracts() { return true; }

	@ConfigItem(
		keyName = "trackClueScrolls",
		name = "Clue scrolls",
		description = "If checked, completed clue scrolls are tracked",
		position = 10,
		section = otherSection
	)
	default boolean trackClueScrolls() { return true; }

	@ConfigItem(
		keyName = "trackSlayerTasks",
		name = "Slayer tasks",
		description = "If checked, completed slayer tasks are tracked.<br>" +
			"Non-wilderness, wilderness and Mortimer tasks are tracked separately.",
		position = 11,
		section = otherSection
	)
	default boolean trackSlayerTasks() { return true; }

	@ConfigItem(
		keyName = "trackSuperiorSpawns",
		name = "Superior spawns",
		description = "If checked, Superior spawns are tracked.<br>" +
			"Non-wilderness, wilderness and Mortimer tasks are tracked separately.",
		position = 12,
		section = otherSection
	)
	default boolean trackSuperiorSpawns() { return true; }

	@ConfigItem(
		keyName = "trackAgilityLaps",
		name = "Agility laps",
		description = "If checked, completed agility course laps are tracked",
		position = 13,
		section = otherSection
	)
	default boolean trackAgilityLaps() { return true; }

	@ConfigItem(
		keyName = "trackCollectionsLogged",
		name = "Collections logged",
		description = "If checked, new collections logged are tracked",
		position = 14,
		section = otherSection
	)
	default boolean trackCollectionsLogged() { return true; }

	@ConfigItem(
		keyName = "trackBirdEggOfferings",
		name = "Bird egg offerings",
		description = "If checked, bird egg offerings to the shrine in the woodcutting guild are tracked",
		position = 15,
		section = otherSection
	)
	default boolean trackBirdEggOfferings() { return true; }

	@ConfigItem(
		keyName = "trackPlayerDeaths",
		name = "Player deaths",
		description = "If checked, player deaths are tracked",
		position = 16,
		section = otherSection
	)
	default boolean trackPlayerDeaths() { return true; }

	@ConfigItem(
		keyName = "trackPlayerKills",
		name = "Player kills",
		description = "If checked, player kills are tracked",
		position = 17,
		section = otherSection
	)
	default boolean trackPlayerKills() { return true; }

	@ConfigItem(
		keyName = "trackMonsterKills",
		name = "Monster kills",
		description = "If checked, monster kills are tracked",
		position = 18,
		section = otherSection
	)
	default boolean trackMonsterKills() { return true; }

	@ConfigItem(
		keyName = "trackQuests",
		name = "Quests",
		description = "If checked, quests are tracked",
		position = 19,
		section = otherSection
	)
	default boolean trackQuests() { return true; }

	@ConfigItem(
		keyName = "trackCombatAchievements",
		name = "Combat Achievements",
		description = "If checked, Combat Achievement diaries are tracked",
		position = 20,
		section = otherSection
	)
	default boolean trackCombatAchievements() { return true; }

	@ConfigItem(
		keyName = "trackMixologyOrders",
		name = "Mixology orders",
		description = "If checked, completed Mixology orders are tracked",
		position = 21,
		section = otherSection
	)
	default boolean trackMixologyOrders() { return true; }

	@ConfigItem(
		keyName = "trackMusicUnlocked",
		name = "Music tracks unlocked",
		description = "If checked, newly unlocked music tracks are tracked",
		position = 22,
		section = otherSection
	)
	default boolean trackMusicUnlocked() { return true; }

	@ConfigItem(
		keyName = "trackLarransChests",
		name = "Larran's chests unlocked",
		description = "If checked, Larran's chests are tracked",
		position = 23,
		section = otherSection
	)
	default boolean trackLarransChests() { return true; }

	@ConfigItem(
		keyName = "trackNpcDamage",
		name = "NPC damage",
		description = "If checked, damage dealt to and taken from NPCs is tracked",
		position = 24,
		section = otherSection
	)
	default boolean trackNpcDamage() { return true; }

	@ConfigItem(
		keyName = "trackSpecialAttacks",
		name = "Special attacks used",
		description = "If checked, special attacks are tracked",
		position = 25,
		section = otherSection
	)
	default boolean trackSpecialAttacks() { return true; }

	@ConfigItem(
		keyName = "trackResourcesGathered",
		name = "Resources gathered",
		description = "If checked, gathered resources (fish, logs, ore) are tracked",
		position = 26,
		section = otherSection
	)
	default boolean trackResourcesGathered() { return true; }

	@ConfigItem(
		keyName = "trackSuppliesConsumed",
		name = "Supplies consumed",
		description = "If checked, consumed supplies (food, potions) are tracked",
		position = 27,
		section = otherSection
	)
	default boolean trackSuppliesConsumed() { return true; }

	@ConfigItem(
		keyName = "trackCoins",
		name = "Coins tracking",
		description = "If checked, coins gained and lost are tracked",
		position = 28,
		section = otherSection
	)
	default boolean trackCoins() { return true; }

	@ConfigItem(
		keyName = "trackCannonballs",
		name = "Cannonballs fired",
		description = "If checked, cannonballs fired by the cannon are tracked",
		position = 29,
		section = otherSection
	)
	default boolean trackCannonballs() { return true; }

	@ConfigItem(
		keyName = "trackNightmareZonePoints",
		name = "NMZ points",
		description = "If checked, Nightmare Zone points are tracked",
		position = 30,
		section = otherSection
	)
	default boolean trackNightmareZonePoints() { return true; }

	// --- EXPERIENCE ---

	@ConfigItem(
		keyName = "trackTotalXp",
		name = "Total XP",
		description = "If checked, Total experience is tracked.",
		position = 0,
		section = experienceSection
	)
	default boolean trackTotalXp() { return true; }

	@ConfigItem(
		keyName = "trackAttackXp",
		name = "Attack XP",
		description = "If checked, Attack experience is tracked.",
		position = 1,
		section = experienceSection
	)
	default boolean trackAttackXp() { return true; }

	@ConfigItem(
		keyName = "trackDefenceXp",
		name = "Defence XP",
		description = "If checked, Defence experience is tracked.",
		position = 2,
		section = experienceSection
	)
	default boolean trackDefenceXp() { return true; }

	@ConfigItem(
		keyName = "trackStrengthXp",
		name = "Strength XP",
		description = "If checked, Strength experience is tracked.",
		position = 3,
		section = experienceSection
	)
	default boolean trackStrengthXp() { return true; }

	@ConfigItem(
		keyName = "trackHitpointsXp",
		name = "Hitpoints XP",
		description = "If checked, Hitpoints experience is tracked.",
		position = 4,
		section = experienceSection
	)
	default boolean trackHitpointsXp() { return true; }

	@ConfigItem(
		keyName = "trackRangedXp",
		name = "Ranged XP",
		description = "If checked, Ranged experience is tracked.",
		position = 5,
		section = experienceSection
	)
	default boolean trackRangedXp() { return true; }

	@ConfigItem(
		keyName = "trackPrayerXp",
		name = "Prayer XP",
		description = "If checked, Prayer experience is tracked.",
		position = 6,
		section = experienceSection
	)
	default boolean trackPrayerXp() { return true; }

	@ConfigItem(
		keyName = "trackMagicXp",
		name = "Magic XP",
		description = "If checked, Magic experience is tracked.",
		position = 7,
		section = experienceSection
	)
	default boolean trackMagicXp() { return true; }

	@ConfigItem(
		keyName = "trackCookingXp",
		name = "Cooking XP",
		description = "If checked, Cooking experience is tracked.",
		position = 8,
		section = experienceSection
	)
	default boolean trackCookingXp() { return true; }

	@ConfigItem(
		keyName = "trackWoodcuttingXp",
		name = "Woodcutting XP",
		description = "If checked, Woodcutting experience is tracked.",
		position = 9,
		section = experienceSection
	)
	default boolean trackWoodcuttingXp() { return true; }

	@ConfigItem(
		keyName = "trackFletchingXp",
		name = "Fletching XP",
		description = "If checked, Fletching experience is tracked.",
		position = 10,
		section = experienceSection
	)
	default boolean trackFletchingXp() { return true; }

	@ConfigItem(
		keyName = "trackFishingXp",
		name = "Fishing XP",
		description = "If checked, Fishing experience is tracked.",
		position = 11,
		section = experienceSection
	)
	default boolean trackFishingXp() { return true; }

	@ConfigItem(
		keyName = "trackFiremakingXp",
		name = "Firemaking XP",
		description = "If checked, Firemaking experience is tracked.",
		position = 12,
		section = experienceSection
	)
	default boolean trackFiremakingXp() { return true; }

	@ConfigItem(
		keyName = "trackCraftingXp",
		name = "Crafting XP",
		description = "If checked, Crafting experience is tracked.",
		position = 13,
		section = experienceSection
	)
	default boolean trackCraftingXp() { return true; }

	@ConfigItem(
		keyName = "trackSmithingXp",
		name = "Smithing XP",
		description = "If checked, Smithing experience is tracked.",
		position = 14,
		section = experienceSection
	)
	default boolean trackSmithingXp() { return true; }

	@ConfigItem(
		keyName = "trackMiningXp",
		name = "Mining XP",
		description = "If checked, Mining experience is tracked.",
		position = 15,
		section = experienceSection
	)
	default boolean trackMiningXp() { return true; }

	@ConfigItem(
		keyName = "trackHerbloreXp",
		name = "Herblore XP",
		description = "If checked, Herblore experience is tracked.",
		position = 16,
		section = experienceSection
	)
	default boolean trackHerbloreXp() { return true; }

	@ConfigItem(
		keyName = "trackAgilityXp",
		name = "Agility XP",
		description = "If checked, Agility experience is tracked.",
		position = 17,
		section = experienceSection
	)
	default boolean trackAgilityXp() { return true; }

	@ConfigItem(
		keyName = "trackThievingXp",
		name = "Thieving XP",
		description = "If checked, Thieving experience is tracked.",
		position = 18,
		section = experienceSection
	)
	default boolean trackThievingXp() { return true; }

	@ConfigItem(
		keyName = "trackSlayerXp",
		name = "Slayer XP",
		description = "If checked, Slayer experience is tracked.",
		position = 19,
		section = experienceSection
	)
	default boolean trackSlayerXp() { return true; }

	@ConfigItem(
		keyName = "trackFarmingXp",
		name = "Farming XP",
		description = "If checked, Farming experience is tracked.",
		position = 20,
		section = experienceSection
	)
	default boolean trackFarmingXp() { return true; }

	@ConfigItem(
		keyName = "trackRunecraftXp",
		name = "Runecraft XP",
		description = "If checked, Runecraft experience is tracked.",
		position = 21,
		section = experienceSection
	)
	default boolean trackRunecraftXp() { return true; }

	@ConfigItem(
		keyName = "trackHunterXp",
		name = "Hunter XP",
		description = "If checked, Hunter experience is tracked.",
		position = 22,
		section = experienceSection
	)
	default boolean trackHunterXp() { return true; }

	@ConfigItem(
		keyName = "trackConstructionXp",
		name = "Construction XP",
		description = "If checked, Construction experience is tracked.",
		position = 23,
		section = experienceSection
	)
	default boolean trackConstructionXp() { return true; }

	@ConfigItem(
		keyName = "trackSailingXp",
		name = "Sailing XP",
		description = "If checked, Sailing experience is tracked.",
		position = 24,
		section = experienceSection
	)
	default boolean trackSailingXp() { return true; }

	// --- LEVELS ---

	@ConfigItem(
		keyName = "trackTotalLevel",
		name = "Total Level",
		description = "If checked, Total Level is tracked.",
		position = 0, section =
		levelSection
	)
	default boolean trackTotalLevel() { return true; }

	@ConfigItem(
		keyName = "trackAttackLevel",
		name = "Attack Level",
		description = "If checked, Attack level is tracked.",
		position = 1,
		section = levelSection
	)
	default boolean trackAttackLevel() { return true; }

	@ConfigItem(
		keyName = "trackDefenceLevel",
		name = "Defence Level",
		description = "If checked, Defence level is tracked.",
		position = 2,
		section = levelSection
	)
	default boolean trackDefenceLevel() { return true; }

	@ConfigItem(
		keyName = "trackStrengthLevel",
		name = "Strength Level",
		description = "If checked, Strength level is tracked.",
		position = 3,
		section = levelSection
	)
	default boolean trackStrengthLevel() { return true; }

	@ConfigItem(
		keyName = "trackHitpointsLevel",
		name = "Hitpoints Level",
		description = "If checked, Hitpoints level is tracked.",
		position = 4,
		section = levelSection
	)
	default boolean trackHitpointsLevel() { return true; }

	@ConfigItem(
		keyName = "trackRangedLevel",
		name = "Ranged Level",
		description = "If checked, Ranged level is tracked.",
		position = 5,
		section = levelSection
	)
	default boolean trackRangedLevel() { return true; }

	@ConfigItem(
		keyName = "trackPrayerLevel",
		name = "Prayer Level",
		description = "If checked, Prayer level is tracked.",
		position = 6,
		section = levelSection
	)
	default boolean trackPrayerLevel() { return true; }

	@ConfigItem(
		keyName = "trackMagicLevel",
		name = "Magic Level",
		description = "If checked, Magic level is tracked.",
		position = 7,
		section = levelSection
	)
	default boolean trackMagicLevel() { return true; }

	@ConfigItem(
		keyName = "trackCookingLevel",
		name = "Cooking Level",
		description = "If checked, Cooking level is tracked.",
		position = 8,
		section = levelSection
	)
	default boolean trackCookingLevel() { return true; }

	@ConfigItem(
		keyName = "trackWoodcuttingLevel",
		name = "Woodcutting Level",
		description = "If checked, Woodcutting level is tracked.",
		position = 9,
		section = levelSection
	)
	default boolean trackWoodcuttingLevel() { return true; }

	@ConfigItem(
		keyName = "trackFletchingLevel",
		name = "Fletching Level",
		description = "If checked, Fletching level is tracked.",
		position = 10,
		section = levelSection
	)
	default boolean trackFletchingLevel() { return true; }

	@ConfigItem(
		keyName = "trackFishingLevel",
		name = "Fishing Level",
		description = "If checked, Fishing level is tracked.",
		position = 11,
		section = levelSection
	)
	default boolean trackFishingLevel() { return true; }

	@ConfigItem(
		keyName = "trackFiremakingLevel",
		name = "Firemaking Level",
		description = "If checked, Firemaking level is tracked.",
		position = 12,
		section = levelSection
	)
	default boolean trackFiremakingLevel() { return true; }

	@ConfigItem(
		keyName = "trackCraftingLevel",
		name = "Crafting Level",
		description = "If checked, Crafting level is tracked.",
		position = 13,
		section = levelSection
	)
	default boolean trackCraftingLevel() { return true; }

	@ConfigItem(
		keyName = "trackSmithingLevel",
		name = "Smithing Level",
		description = "If checked, Smithing level is tracked.",
		position = 14,
		section = levelSection
	)
	default boolean trackSmithingLevel() { return true; }

	@ConfigItem(
		keyName = "trackMiningLevel",
		name = "Mining Level",
		description = "If checked, Mining level is tracked.",
		position = 15,
		section = levelSection
	)
	default boolean trackMiningLevel() { return true; }

	@ConfigItem(
		keyName = "trackHerbloreLevel",
		name = "Herblore Level",
		description = "If checked, Herblore level is tracked.",
		position = 16,
		section = levelSection
	)
	default boolean trackHerbloreLevel() { return true; }

	@ConfigItem(
		keyName = "trackAgilityLevel",
		name = "Agility Level",
		description = "If checked, Agility level is tracked.",
		position = 17,
		section = levelSection
	)
	default boolean trackAgilityLevel() { return true; }

	@ConfigItem(
		keyName = "trackThievingLevel",
		name = "Thieving Level",
		description = "If checked, Thieving level is tracked.",
		position = 18,
		section = levelSection
	)
	default boolean trackThievingLevel() { return true; }

	@ConfigItem(
		keyName = "trackSlayerLevel",
		name = "Slayer Level",
		description = "If checked, Slayer level is tracked.",
		position = 19,
		section = levelSection
	)
	default boolean trackSlayerLevel() { return true; }

	@ConfigItem(
		keyName = "trackFarmingLevel",
		name = "Farming Level",
		description = "If checked, Farming level is tracked.",
		position = 20,
		section = levelSection
	)
	default boolean trackFarmingLevel() { return true; }

	@ConfigItem(
		keyName = "trackRunecraftLevel",
		name = "Runecraft Level",
		description = "If checked, Runecraft level is tracked.",
		position = 21,
		section = levelSection
	)
	default boolean trackRunecraftLevel() { return true; }

	@ConfigItem(
		keyName = "trackHunterLevel",
		name = "Hunter Level",
		description = "If checked, Hunter level is tracked.",
		position = 22,
		section = levelSection
	)
	default boolean trackHunterLevel() { return true; }

	@ConfigItem(
		keyName = "trackConstructionLevel",
		name = "Construction Level",
		description = "If checked, Construction level is tracked.",
		position = 23,
		section = levelSection
	)
	default boolean trackConstructionLevel() { return true; }

	@ConfigItem(
		keyName = "trackSailingLevel",
		name = "Sailing Level",
		description = "If checked, Sailing level is tracked.",
		position = 24,
		section = levelSection
	)
	default boolean trackSailingLevel() { return true; }
}