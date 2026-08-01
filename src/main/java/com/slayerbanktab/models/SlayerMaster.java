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

package com.slayerbanktab.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;

@Getter
@RequiredArgsConstructor
public enum SlayerMaster {
	// Unidentified slayer master; used for any id that does not belong to other SlayerMasters.
	UNKNOWN(-1, "Unknown Slayer Master", ItemID._100GUIDE_GUIDECAKE, Collections.emptyList()),

	// No slayer master
	NONE(0, "", ItemID._100GUIDE_GUIDECAKE, Collections.emptyList()),

	TURAEL(1, "Turael", ItemID.NECKLACE_OF_MINIGAMES_8, Arrays.asList(
			   Task.BANSHEE, Task.BAT, Task.BEAR, Task.BIRD, Task.CAVE_BUG, Task.CAVE_CRAWLER,
		   Task.CAVE_SLIME, Task.COW, Task.CRAWLING_HAND, Task.DOG, Task.DWARF, Task.GHOST,
		   Task.GOBLIN, Task.ICEFIEND, Task.KALPHITE, Task.LIZARD, Task.MINOTAUR, Task.MONKEY,
		   Task.RAT, Task.SCORPION, Task.SKELETON, Task.SPIDER, Task.WOLF, Task.ZOMBIE
		   )),

	SPRIA(9, "Spria", ItemID.SLAYER_REINFORCED_GOGGLES, Arrays.asList(
			  Task.BANSHEE, Task.BAT, Task.BEAR, Task.BIRD, Task.CAVE_BUG, Task.CAVE_CRAWLER,
		  Task.CAVE_SLIME, Task.COW, Task.CRAWLING_HAND, Task.DOG, Task.DWARF, Task.GHOST,
		  Task.GOBLIN, Task.ICEFIEND, Task.KALPHITE, Task.LIZARD, Task.MINOTAUR, Task.MONKEY,
		  Task.RAT, Task.SCORPION, Task.SKELETON, Task.SOURHOG, Task.SPIDER, Task.WOLF, Task.ZOMBIE
		  )),

	MAZCHNA(2, "Mazchna", ItemID.TABLET_KHARYLL, Arrays.asList(
				Task.BANSHEE, Task.BAT, Task.BEAR, Task.CATABLEPON, Task.CAVE_BUG, Task.CAVE_CRAWLER,
			Task.CAVE_SLIME, Task.COCKATRICE, Task.CRAB, Task.CRAWLING_HAND, Task.DOG, Task.FLESH_CRAWLER,
			Task.GHOST, Task.GHOUL, Task.HILL_GIANT, Task.HOBGOBLIN, Task.ICE_WARRIOR, Task.KALPHITE,
			Task.KILLERWATT, Task.LIZARD, Task.MOGRE, Task.PYREFIEND, Task.ROCKSLUG, Task.SCORPION,
			Task.SHADE, Task.SKELETON, Task.VAMPYRE, Task.WALL_BEAST, Task.WOLF, Task.ZOMBIE
			)),

	VANNAKA(3, "Vannaka", ItemID.DRAGON_SQ_SHIELD, Arrays.asList(
				Task.ABERRANT_SPECTRE, Task.ABYSSAL_DEMON, Task.ANKOU, Task.BASILISK, Task.BLOODVELD,
			Task.BLUE_DRAGON, Task.BRINE_RAT, Task.COCKATRICE, Task.CRAB, Task.CROCODILE, Task.DAGANNOTH,
			Task.DUST_DEVIL, Task.ELF, Task.FEVER_SPIDER, Task.FIRE_GIANT, Task.GARGOYLE, Task.GHOUL,
			Task.GRYPHON, Task.HARPIE_BUG_SWARM, Task.HELLHOUND, Task.HILL_GIANT, Task.HOBGOBLIN,
			Task.ICE_GIANT, Task.ICE_WARRIOR, Task.INFERNAL_MAGE, Task.JELLY, Task.JUNGLE_HORROR,
			Task.KALPHITE, Task.KURASK, Task.LESSER_DEMON, Task.MOGRE, Task.MOLANISK, Task.MOSS_GIANT,
			Task.NECHRYAEL, Task.OGRE, Task.OTHERWORLDLY_BEING, Task.PYREFIEND, Task.SEA_SNAKE,
			Task.SHADE, Task.SHADOW_WARRIOR, Task.SPIRITUAL_CREATURE, Task.TERROR_DOG, Task.TROLL,
			Task.TUROTH, Task.VAMPYRE, Task.WEREWOLF
			)),

	CHAELDAR(4, "Chaeldar", ItemID.DRAMEN_STAFF, Arrays.asList(
				 Task.ABERRANT_SPECTRE, Task.ABYSSAL_DEMON, Task.AVIANSIE, Task.BASILISK, Task.BLACK_DEMON,
			 Task.BLOODVELD, Task.BLUE_DRAGON, Task.BRINE_RAT, Task.CAVE_HORROR, Task.CAVE_KRAKEN,
			 Task.CRAB, Task.CUSTODIAN_STALKER, Task.DAGANNOTH, Task.DUST_DEVIL, Task.ELF,
			 Task.FEVER_SPIDER, Task.FIRE_GIANT, Task.FOSSIL_ISLAND_WYVERN, Task.GARGOYLE,
			 Task.GREATER_DEMON, Task.GRYPHON, Task.HELLHOUND, Task.JELLY, Task.JUNGLE_HORROR,
			 Task.KALPHITE, Task.KURASK, Task.LESSER_DEMON, Task.LESSER_NAGUA, Task.LIZARDMAN,
			 Task.MUTATED_ZYGOMITE, Task.NECHRYAEL, Task.SHADOW_WARRIOR, Task.SKELETAL_WYVERN,
			 Task.SPIRITUAL_CREATURE, Task.TROLL, Task.TUROTH, Task.TZHAAR, Task.VAMPYRE,
			 Task.WARPED_CREATURE, Task.WYRM
			 )),

	NIEVE(6, "Nieve", ItemID.SLAYER_RING_8, withBosses(
			  Task.ABERRANT_SPECTRE, Task.ABYSSAL_DEMON, Task.ANKOU, Task.AQUANITE, Task.ARAXYTE, Task.AVIANSIE,
		  Task.BASILISK, Task.BLACK_DEMON, Task.BLACK_DRAGON, Task.BLOODVELD,
		  Task.BLUE_DRAGON, Task.BRINE_RAT, Task.CAVE_HORROR, Task.CAVE_KRAKEN,
		  Task.CUSTODIAN_STALKER, Task.DAGANNOTH, Task.DARK_BEAST, Task.DRAKE, Task.DUST_DEVIL,
		  Task.ELF, Task.FIRE_GIANT, Task.FOSSIL_ISLAND_WYVERN, Task.FROST_DRAGON, Task.GARGOYLE,
		  Task.GREATER_DEMON, Task.GRYPHON, Task.HELLHOUND, Task.KALPHITE, Task.KURASK, Task.LIZARDMAN,
		  Task.METAL_DRAGON, Task.MUTATED_ZYGOMITE, Task.NECHRYAEL, Task.RED_DRAGON, Task.SCABARITE,
		  Task.SKELETAL_WYVERN, Task.SMOKE_DEVIL, Task.SPIRITUAL_CREATURE, Task.SUQAH, Task.TROLL,
		  Task.TUROTH, Task.TZHAAR, Task.VAMPYRE, Task.WARPED_CREATURE, Task.WYRM
		  )),

	DURADEL(5, "Duradel", ItemID.SKILLCAPE_SLAYER, withBosses(
				Task.ABERRANT_SPECTRE, Task.ABYSSAL_DEMON, Task.ANKOU, Task.AQUANITE, Task.ARAXYTE,
			Task.AVIANSIE, Task.BASILISK, Task.BLACK_DEMON, Task.BLACK_DRAGON, Task.BLOODVELD,
			Task.BLUE_DRAGON, Task.CAVE_HORROR, Task.CAVE_KRAKEN, Task.DAGANNOTH,
			Task.DARK_BEAST, Task.DRAKE, Task.DUST_DEVIL, Task.ELF, Task.FIRE_GIANT,
			Task.FOSSIL_ISLAND_WYVERN, Task.FROST_DRAGON, Task.GARGOYLE, Task.GREATER_DEMON,
			Task.GRYPHON, Task.HELLHOUND, Task.KALPHITE, Task.KURASK, Task.LIZARDMAN, Task.METAL_DRAGON,
			Task.MUTATED_ZYGOMITE, Task.NECHRYAEL, Task.RED_DRAGON, Task.SKELETAL_WYVERN,
			Task.SMOKE_DEVIL, Task.SPIRITUAL_CREATURE, Task.SUQAH, Task.TROLL, Task.TZHAAR,
			Task.VAMPYRE, Task.WARPED_CREATURE, Task.WATERFIEND, Task.WYRM
			)),

	KONAR(8, "Konar quo Maten", ItemID.KONAR_KEY, withBosses(
			  Task.ABERRANT_SPECTRE, Task.ABYSSAL_DEMON, Task.ANKOU, Task.AVIANSIE, Task.BASILISK,
		  Task.BLACK_DEMON, Task.BLACK_DRAGON, Task.BLOODVELD, Task.BLUE_DRAGON,
		  Task.BRINE_RAT, Task.CAVE_KRAKEN, Task.DAGANNOTH, Task.DARK_BEAST, Task.DRAKE,
		  Task.DUST_DEVIL, Task.FIRE_GIANT, Task.FOSSIL_ISLAND_WYVERN, Task.GARGOYLE,
		  Task.GREATER_DEMON, Task.HELLHOUND, Task.HYDRA, Task.JELLY, Task.KALPHITE,
		  Task.KURASK, Task.LESSER_NAGUA, Task.LIZARDMAN, Task.METAL_DRAGON, Task.MUTATED_ZYGOMITE,
		  Task.NECHRYAEL, Task.RED_DRAGON, Task.SKELETAL_WYVERN, Task.SMOKE_DEVIL, Task.TROLL,
		  Task.TUROTH, Task.VAMPYRE, Task.WARPED_CREATURE, Task.WATERFIEND, Task.WYRM
		  )),

	KRYSTILIA(7, "Krystilia", ItemID.SLAYER_WILDERNESS_KEY, Arrays.asList(
		Task.ABYSSAL_DEMON, Task.ANKOU, Task.AVIANSIE, Task.BANDIT, Task.BEAR, Task.BLACK_DEMON,
		Task.BLACK_DRAGON, Task.BLACK_KNIGHT, Task.BLOODVELD, Task.CHAOS_DRUID, Task.DARK_WARRIOR,
		Task.DUST_DEVIL, Task.EARTH_WARRIOR, Task.ENT, Task.FIRE_GIANT, Task.GREATER_DEMON,
		Task.GREEN_DRAGON, Task.HELLHOUND, Task.HILL_GIANT, Task.ICE_GIANT, Task.ICE_WARRIOR,
		Task.JELLY, Task.LAVA_DRAGON, Task.LESSER_DEMON, Task.MAGIC_AXE, Task.MAMMOTH,
		Task.MOSS_GIANT, Task.NECHRYAEL, Task.PIRATE, Task.REVENANT, Task.ROGUE, Task.SCORPION,
		Task.SKELETON, Task.SPIDER, Task.SPIRITUAL_CREATURE, Task.ZOMBIE,
		Task.CALLISTO, Task.CHAOS_ELEMENTAL, Task.CHAOS_FANATIC, Task.CRAZY_ARCHAEOLOGIST,
		Task.SCORPIA, Task.VENENATIS, Task.VETION
	)),

	MORTIMER(10, "Mortimer", ItemID.SKULL, Arrays.asList(
		Task.CRAWLING_HAND, Task.CAVE_CRAWLER, Task.BANSHEE, Task.ROCKSLUG,
		Task.COCKATRICE, Task.PYREFIEND, Task.INFERNAL_MAGE, Task.BLOODVELD,
		Task.GRYPHON, Task.JELLY, Task.CUSTODIAN_STALKER, Task.TUROTH,
		Task.WARPED_CREATURE, Task.CAVE_HORROR, Task.ABERRANT_SPECTRE, Task.BASILISK,
		Task.WYRM, Task.DUST_DEVIL, Task.KURASK, Task.VENATOR, Task.GARGOYLE,
		Task.AQUANITE, Task.NECHRYAEL, Task.DRAKE, Task.ABYSSAL_DEMON, Task.DARK_BEAST,
		Task.ARAXYTE, Task.SMOKE_DEVIL, Task.HYDRA
	)),

	// Merged category. Its ID is used in as masterId key for non-wildy boss tasks, and optionally used instead of certain other masters.
	MERGED_STANDARD(99, "Standard Masters", ItemID.SLAYER_ETERNAL_GEM, Collections.emptyList());

	private final int id;
	private final String name;
	private final int iconItemId;
	private List<Task> assignableTasks;

	private static final Set<Integer> LEGAL_IDS = new HashSet<>();
	private static final SlayerMaster[] ID_LOOKUP;

	SlayerMaster(int id, String name, int iconItemId, List<Task> assignableTasks)
	{
		this.id = id;
		this.name = name;
		this.iconItemId = iconItemId;
		this.assignableTasks = assignableTasks;
	}

	static {
		int maxId = Arrays.stream(values()).mapToInt(SlayerMaster::getId).max().orElse(0);
		ID_LOOKUP = new SlayerMaster[maxId + 1];
		for (SlayerMaster master : values()) {
			if (master.id != -1)
			{
				ID_LOOKUP[master.id] = master;
				LEGAL_IDS.add(master.id);
			}
		}

		Set<Task> combinedTasks = new HashSet<>();
		for (SlayerMaster master : values()) {
			if (master != TURAEL && master != KONAR && master != KRYSTILIA && master != MORTIMER && master != UNKNOWN && master != NONE && master != MERGED_STANDARD) {
				combinedTasks.addAll(master.getAssignableTasks());
			}
		}

		List<Task> sortedCombinedTasks = new ArrayList<>(combinedTasks);
		Collections.sort(sortedCombinedTasks);
		MERGED_STANDARD.assignableTasks = Collections.unmodifiableList(sortedCombinedTasks);
	}

	/**
	 * Adds all boss tasks to the given tasks and returns the merged result
	 */
	private static List<Task> withBosses(Task... standardTasks) {
		List<Task> tasks = new ArrayList<>(Arrays.asList(standardTasks));
		tasks.addAll(Arrays.asList(
//			Task.BOSS,
			Task.ABYSSAL_SIRE, Task.ALCHEMICAL_HYDRA, Task.ARAXXOR, Task.BARROWS_BROTHER,
			Task.CERBERUS, Task.COMMANDER_ZILYANA, Task.DAGANNOTH_KING, Task.DUKE_SUCELLUS,
			Task.GENERAL_GRAARDOR, Task.GIANT_MOLE, Task.GROTESQUE_GUARDIAN, Task.KALPHITE_QUEEN,
			Task.KING_BLACK_DRAGON, Task.KRAKEN, Task.KREE_ARRA, Task.KRIL_TSUTSAROTH,
			Task.LEVIATHAN, Task.PHANTOM_MUSPAH, Task.SARACHNIS, Task.SHELLBANE_GRYPHON,
			Task.THERMONUCLEAR_SMOKE_DEVIL, Task.VARDORVIS, Task.VORKATH, Task.WHISPERER, Task.ZULRAH
		));
		return tasks;
	}

	public String getDisplayName(Client client)
	{
		if (client == null) return this.name;

		switch (this) {
			case TURAEL:
				return client.getVarbitValue(VarbitID.WGS_TURAEL_RECRUIT) == 1 ? "Aya" : "Turael";
			case NIEVE:
				return client.getVarbitValue(VarbitID.MM2_SLAYER_MASTER) == 1 ? "Steve" : "Nieve";
			case DURADEL:
				return client.getVarbitValue(VarbitID.WGS_DURADEL_RECRUIT) == 1 ? "Kuradal" : "Duradel";
			default:
				return this.name;
		}
	}

	public static SlayerMaster getById(int id) {
		if (id < 0 || id >= ID_LOOKUP.length) return UNKNOWN;
		return ID_LOOKUP[id];
	}

	public String getCleanTaskName(Task task) {
		String rawName = task.getName();
		return rawName.substring(0, 1).toUpperCase() + rawName.substring(1).toLowerCase();
	}

	/**
	 * Model helper explicitly broadcasting location dependency.
	 */
	public boolean usesSlayerAreas() {
		return this == KONAR;
	}

	/**
	 * Return true if masterId identifies to a slayer master with no particular constraints/quirks
	 */
	public static boolean isMergeableMasterId(int masterId)
	{
		if (masterId == UNKNOWN.getId()) return false;
		else if (masterId == NONE.getId()) return false;
		else if (masterId == TURAEL.getId()) return false;
		else if (masterId == KRYSTILIA.getId()) return false;
		else if (masterId == MORTIMER.getId()) return false;
		else return masterId != KONAR.getId();
	}

	/**
	 * Returns true if id is properly identified as a slayer master, or no slayer master (0).
	 */
	public static boolean isLegalId(int id)
	{
		return LEGAL_IDS.contains(id);
	}
}