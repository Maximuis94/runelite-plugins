package com.slayerbanktab.models;

import static com.slayerbanktab.PluginConstants.BOSS_TASK_ID;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.gameval.ItemID;

@Getter
@RequiredArgsConstructor
public enum Task {
	ABERRANT_SPECTRE("Aberrant spectre", ItemID.SLAYERGUIDE_ABERRANTSPECTER, 6254, 41, 0),
	ABYSSAL_DEMON("Abyssal demon", ItemID.SLAYERGUIDE_ABYSSALDEMON, 6255, 42, 0),
	ADAMANT_DRAGON("Adamant dragon", ItemID.ADAMANTITE_BAR, 6320, 108, 0),
	ANKOU("Ankou", ItemID.ANKOU_HEAD, 6292, 79, 0),
	AQUANITE("Aquanite", ItemID.SLAYERGUIDE_AQUANITE, 9415, 129, 0),
	ARAXYTE("Araxyte", ItemID.ARAXYTE_VENOM_SACK, 6339, 124, 0),
	AVIANSIE("Aviansie", ItemID.ARCEUUS_CORPSE_AVIANSIE, 6306, 94, 0),
	BANDIT("Bandit", ItemID.HIGHWAYMAN_MASK, 6315, 102, 0),
	BANSHEE("Banshee", ItemID.SLAYERGUIDE_BANSHEE, 6251, 38, 0),
	BASILISK("Basilisk", ItemID.SLAYERGUIDE_BASILISK, 6256, 43, 0),
	BAT("Bat", ItemID.RAG_BAT_BONE, 6221, 8, 0),
	BEAR("Bear", ItemID.ARCEUUS_CORPSE_BEAR, 6226, 13, 0),
	BIRD("Bird", ItemID.FEATHER, 6218, 5, 0),
	BLACK_DEMON("Black demon", ItemID.BLACK_DEMON_MASK, 6243, 30, 0),
	BLACK_DRAGON("Black dragon", ItemID.DRAGONHIDE_BLACK, 6240, 27, 0),
	BLACK_KNIGHT("Black knight", ItemID.BLACK_FULL_HELM, 6334, 119, 0),
	BLOODVELD("Bloodveld", ItemID.SLAYERGUIDE_BLOODVELD, 6261, 48, 0),
	BLUE_DRAGON("Blue dragon", ItemID.DRAGONHIDE_BLUE, 6238, 25, 0),
	BRINE_RAT("Brine rat", ItemID.OLAF2_BRINE_RAT_INV, 6296, 84, 0),
	BRONZE_DRAGON("Bronze dragon", ItemID.BRONZE_BAR, 6271, 58, 0),
	BRUTAL_BLACK_DRAGON("Brutal black dragon", ItemID.DRAGONHIDE_BLACK, 6330, 117, 0),
	CATABLEPON("Catablepon", ItemID.SOS_BOOTS, 6291, 78, 0),
	CAVE_BUG("Cave bug", ItemID.SWAMP_CAVE_BUG, 6276, 63, 0),
	CAVE_CRAWLER("Cave crawler", ItemID.SLAYERGUIDE_CAVECRAWLER, 6250, 37, 0),
	CAVE_HORROR("Cave horror", ItemID.SLAYERGUIDE_HARMLESS_CAVE_HORROR, 6293, 80, 0),
	CAVE_KRAKEN("Cave kraken", ItemID.KRAKEN_TENTACLE, 6304, 92, 0),
	CAVE_SLIME("Cave slime", ItemID.SWAMP_CAVE_SLIME, 6275, 62, 0),
	CHAOS_DRUID("Chaos druid", ItemID.ARCEUUS_CORPSE_CHAOSDRUID, 6323, 110, 0),
	COCKATRICE("Cockatrice", ItemID.SLAYERGUIDE_COCKATRICE, 6257, 44, 0),
	COW("Cow", ItemID.COW_MASK, 6219, 6, 0),
	CRAB("Crab", ItemID.BLUE_CRAB, 6333, 125, 0),
	CRAWLING_HAND("Crawling hand", ItemID.SLAYERGUIDE_CRAWLINGHAND, 6252, 39, 0),
	CROCODILE("Crocodile", ItemID.BCS_RIDDLE_EMBLEM_CROCODILE, 6278, 65, 0),
	CUSTODIAN_STALKER("Custodian stalker", ItemID.SLAYERGUIDE_CUSTODIAN_STALKER_ELDER, 6340, 126, 0),
	DAGANNOTH("Dagannoth", ItemID.ARCEUUS_CORPSE_DAGANNOTH, 6248, 35, 0),
	DARK_BEAST("Dark beast", ItemID.SLAYERGUIDE_DARK_BEAST, 6279, 66, 0),
	DARK_WARRIOR("Dark warrior", ItemID.BLACK_MED_HELM, 6316, 103, 0),
	DOG("Dog", ItemID.ARCEUUS_CORPSE_DOG, 6235, 22, 0),
	DRAKE("Drake", ItemID.SLAYERGUIDE_DRAKE, 6325, 112, 0),
	DUST_DEVIL("Dust devil", ItemID.SLAYERGUIDE_DUSTDEVIL, 6262, 49, 0),
	DWARF("Dwarf", ItemID.DWARVEN_STOUT, 6270, 57, 0),
	EARTH_WARRIOR("Earth warrior", ItemID.BRONZE_FULL_HELM, 6267, 54, 0),
	ELF("Elf", ItemID.ARCEUUS_CORPSE_ELF, 6269, 56, 0),
	ENT("Ent", ItemID.LOGS, 6314, 101, 0),
	FEVER_SPIDER("Fever spider", ItemID.SLAYERGUIDE_FEVER_SPIDER, 6282, 69, 0),
	FIRE_GIANT("Fire giant", ItemID.RTBRANDAPET, 6229, 16, 0),
	FLESH_CRAWLER("Flesh crawler", ItemID.SOS_BOOTS, 6290, 77, 0),
	FOSSIL_ISLAND_WYVERN("Fossil island wyvern", ItemID.SLAYERGUIDE_FOSSILWYVERN, 6318, 106, 0),
	FROST_DRAGON("Frost dragon", ItemID.FROST_DRAGON_BONES, 9416, 130, 0),
	GARGOYLE("Gargoyle", ItemID.SLAYERGUIDE_GARGOYLE, 6259, 46, 0),
	GHOST("Ghost", ItemID.AMULET_OF_GHOSTSPEAK, 6225, 12, 0),
	GHOUL("Ghoul", ItemID.RAG_GHOUL_BONE, 6236, 23, 0),
	GOBLIN("Goblin", ItemID.ARCEUUS_CORPSE_GOBLIN, 6215, 2, 0),
	GREATER_DEMON("Greater demon", ItemID.GREATER_DEMON_MASK, 6242, 29, 0),
	GREEN_DRAGON("Green dragon", ItemID.DRAGONHIDE_GREEN, 6237, 24, 0),
	GRYPHON("Gryphon", ItemID.SLAYERGUIDE_GRYPHON, 9414, 128, 0),
	HARPIE_BUG_SWARM("Harpie bug swarm", ItemID.SLAYERGUIDE_SWARM, 6283, 70, 0),
	HELLHOUND("Hellhound", ItemID.POH_HELLHOUND, 6244, 31, 0),
	HILL_GIANT("Hill giant", ItemID.ARCEUUS_CORPSE_GIANT, 6227, 14, 0),
	HOBGOBLIN("Hobgoblin", ItemID.POH_HOBGOBLIN, 6234, 21, 0),
	HYDRA("Hydra", ItemID.SLAYERGUIDE_HYDRA, 6326, 113, 0),
	ICE_GIANT("Ice giant", ItemID.RTELDRICPET, 6228, 15, 0),
	ICE_WARRIOR("Ice warrior", ItemID.ICE_GLOVES, 6232, 19, 0),
	ICEFIEND("Icefiend", ItemID.FD_ICEDIAMOND, 6288, 75, 0),
	INFERNAL_MAGE("Infernal mage", ItemID.SLAYERGUIDE_INFERNALMAGE, 6253, 40, 0),
	IRON_DRAGON("Iron dragon", ItemID.IRON_BAR, 6272, 59, 0),
	JELLY("Jelly", ItemID.SLAYERGUIDE_JELLY, 6263, 50, 0),
	JUNGLE_HORROR("Jungle horror", ItemID.ARCEUUS_CORPSE_HORROR, 6294, 81, 0),
	KALPHITE("Kalphite", ItemID.POH_KALPHITE_SOLDIER, 6266, 53, 0),
	KILLERWATT("Killerwatt", ItemID.SLAYERGUIDE_KILLERWATT, 6286, 73, 0),
	KURASK("Kurask", ItemID.SLAYERGUIDE_KURASK, 6258, 45, 0),
	LAVA_DRAGON("Lava dragon", ItemID.LAVA_SCALE, 6317, 104, 0),
	LESSER_DEMON("Lesser demon", ItemID.LESSER_DEMON_MASK, 6241, 28, 0),
	LESSER_NAGUA("Lesser nagua", ItemID.SLAYERGUIDE_LESSER_NAGUA, 6338, 123, 0),
	LIZARD("Lizard", ItemID.SLAYERGUIDE_LIZARD, 6281, 68, 0),
	LIZARDMAN("Lizardman", ItemID.LIZARDMAN_FANG, 6302, 90, 0),
	MAGIC_AXE("Magic axe", ItemID.STEEL_BATTLEAXE, 6303, 91, 0),
	MAMMOTH("Mammoth", ItemID.HW22_TRICK_WOOL, 6312, 99, 0),
	METAL_DRAGON("Metal dragon", ItemID.POH_STEEL_DRAGON, 6322, 127, 0),
	MINOTAUR("Minotaur", ItemID.ARCEUUS_CORPSE_MINOTAUR, 6289, 76, 0),
	MITHRIL_DRAGON("Mithril dragon", ItemID.BRUT_DRAGON_FULL_HELM, 6305, 93, 0),
	MOGRE("Mogre", ItemID.SLAYERGUIDE_MOGRE, 6280, 67, 0),
	MOLANISK("Molanisk", ItemID.SLAYERGUIDE_MOLANISK, 6299, 87, 0),
	MONKEY("Monkey", ItemID.MM_MONKEY_IN_BACKPACK, 6214, 1, 0),
	MOSS_GIANT("Moss giant", ItemID.MOSSY_KEY, 6230, 17, 0),
	MUTATED_ZYGOMITE("Mutated zygomite", ItemID.SLAYER_ZYGOMITE_OBJECT, 6287, 74, 0),
	NECHRYAEL("Nechryael", ItemID.SLAYERGUIDE_NECHRYAEL, 6265, 52, 0),
	OGRE("Ogre", ItemID.ARCEUUS_CORPSE_OGRE, 6233, 20, 0),
	OTHERWORLDLY_BEING("Otherworldly being", ItemID.BLACKWIZHAT, 6268, 55, 0),
	PIRATE("Pirate", ItemID.PICKPOCKET_GUIDE_PIRATE, 6335, 120, 0),
	PYREFIEND("Pyrefiend", ItemID.SLAYERGUIDE_PYRFIEND, 6260, 47, 0),
	RAT("Rat", ItemID.RATS_TAIL, 6216, 3, 0),
	RED_DRAGON("Red dragon", ItemID.DRAGONHIDE_RED, 6239, 26, 0),
	REVENANT("Revenant", ItemID.WILD_CAVE_SHARD_GRAPHIC_5X, 6319, 107, 0),
	ROCKSLUG("Rockslug", ItemID.SLAYERGUIDE_ROCKSLUG, 6264, 51, 0),
	ROGUE("Rogue", ItemID.PICKPOCKET_GUIDE_ROGUE, 6313, 100, 0),
	RUNE_DRAGON("Rune dragon", ItemID.RUNITE_BAR, 6321, 109, 0),
	SAND_CRAB("Sand crab", ItemID.BUCKET_SAND, 6332, 118, 0),
	SCABARITE("Scabarite", ItemID.NTK_SCARAB_POTTERY, 6297, 85, 0),
	SCORPION("Scorpion", ItemID.ARCEUUS_CORPSE_SCORPION, 6220, 7, 0),
	SEA_SNAKE("Sea snake", ItemID.VILLAGE_SNAKE_SKIN, 6284, 71, 0),
	SHADE("Shade", ItemID.SHADE_BONES5, 6277, 64, 0),
	SHADOW_WARRIOR("Shadow warrior", ItemID.CAPE_OF_LEGENDS, 6245, 32, 0),
	SKELETAL_WYVERN("Skeletal wyvern", ItemID.SLAYERGUIDE_SKELETALWYVERN, 6285, 72, 0),
	SKELETON("Skeleton", ItemID.POH_SKELETON_GUARD, 6224, 11, 0),
	SMOKE_DEVIL("Smoke devil", ItemID.CERT_GUIDE_ICON_DUMMY, 6307, 95, 0),
	SOURHOG("Sourhog", ItemID.SLAYER_REINFORCED_GOGGLES, 6336, 121, 0),
	SPIDER("Spider", ItemID.DEAL_SPIDER_BODY, 6217, 4, 0),
	SPIRITUAL_CREATURE("Spiritual creature", ItemID.GODWARS_SPIRITUAL_MAGE_INV, 6301, 89, 0),
	STEEL_DRAGON("Steel dragon", ItemID.POH_STEEL_DRAGON, 6273, 60, 0),
	SULPHUR_LIZARD("Sulphur lizard", ItemID.SLAYERGUIDE_LIZARD, 6329, 116, 0),
	SUQAH("Suqah", ItemID.SLAYERGUIDE_SUQAH_MONSTER, 6295, 83, 0),
	TEMPLE_SPIDER("Temple spider", ItemID.RED_SPIDERS_EGGS, 6327, 114, 0),
	TERROR_DOG("Terror dog", ItemID.SLAYERGUIDE_TERRORDOG, 6298, 86, 0),
	TROLL("Troll", ItemID.ARCEUUS_CORPSE_TROLL, 6231, 18, 0),
	TUROTH("Turoth", ItemID.SLAYERGUIDE_TUROTH, 6249, 36, 0),
	TZHAAR("TzHaar", ItemID.ARCEUUS_CORPSE_TZHAAR, 6308, 96, 0),
	TZKAL_ZUK("TzKal-Zuk", ItemID.INFERNOPET_ZUK, 6309, 105, 0),
	TZTOK_JAD("TzTok-Jad", ItemID.JAD_PET, 6310, 97, 0),
	UNDEAD_DRUID("Undead druid", ItemID.HOSDUN_TEMPLE_MASK, 6328, 115, 0),
	VAMPYRE("Vampyre", ItemID.SLAYERGUIDE_VAMPYRE, 6247, 34, 0),
	WALL_BEAST("Wall beast", ItemID.WALLBEAST_SPIKE_HELMET, 6274, 61, 0),
	WARPED_CREATURE("Warped creature", ItemID.POG_SLAYER_DUMMY_WARPED_TORTOISE, 6337, 122, 0),
	WATERFIEND("Waterfiend", ItemID.MIST_BATTLESTAFF, 6300, 88, 0),
	WEREWOLF("Werewolf", ItemID.DAGGER_WOLFBANE, 6246, 33, 0),
	WOLF("Wolf", ItemID.WOLF_MASK, 6222, 9, 0),
	WYRM("Wyrm", ItemID.SLAYERGUIDE_WYRM, 6324, 111, 0),
	ZOMBIE("Zombie", ItemID.TRICK_OR_TREAT_HEAD, 6223, 10, 0),

	ABYSSAL_SIRE("Abyssal Sire", ItemID.ABYSSALSIRE_PET, 6193, BOSS_TASK_ID, 21),
	ALCHEMICAL_HYDRA("Alchemical Hydra", ItemID.HYDRAPET, 6199, BOSS_TASK_ID, 24),
	ARAXXOR("Araxxor", ItemID.ARAXXORPET, 6213, BOSS_TASK_ID, 31),
	BARROWS_BROTHER("Barrows brother", ItemID.BARROWS_VERAC_HEAD, 6185, BOSS_TASK_ID, 17),
	BOSS("Boss", ItemID.DRAGON_SLAYER_QIP_ELVARGS_HEAD, 6311, BOSS_TASK_ID, -1),
	CALLISTO("Callisto", ItemID.CALLISTO_PET, 6169, BOSS_TASK_ID, 9),
	CERBERUS("Cerberus", ItemID.HELL_PET, 6191, BOSS_TASK_ID, 20),
	CHAOS_ELEMENTAL("Chaos Elemental", ItemID.CHAOSELEPET, 6175, BOSS_TASK_ID, 12),
	CHAOS_FANATIC("Chaos Fanatic", ItemID.BR_ANCIENT_STAFF, 6177, BOSS_TASK_ID, 13),
	COMMANDER_ZILYANA("Commander Zilyana", ItemID.SARADOMINPET, 6155, BOSS_TASK_ID, 2),
	CRAZY_ARCHAEOLOGIST("Crazy archaeologist", ItemID.FEDORA, 6179, BOSS_TASK_ID, 14),
	DAGANNOTH_KING("Dagannoth King", ItemID.REXPET, 6161, BOSS_TASK_ID, 5),
	DUKE_SUCELLUS("Duke Sucellus", ItemID.DUKESUCELLUSPET, 6207, BOSS_TASK_ID, 28),
	GENERAL_GRAARDOR("General Graardor", ItemID.BANDOSPET, 6157, BOSS_TASK_ID, 3),
	GIANT_MOLE("Giant Mole", ItemID.MOLEPET, 6163, BOSS_TASK_ID, 6),
	GROTESQUE_GUARDIAN("Grotesque Guardian", ItemID.DAWNPET, 6195, BOSS_TASK_ID, 22),
	KALPHITE_QUEEN("Kalphite Queen", ItemID.KQPET_WALKING, 6165, BOSS_TASK_ID, 7),
	KING_BLACK_DRAGON("King Black Dragon", ItemID.KBDPET, 6167, BOSS_TASK_ID, 8),
	KRAKEN("Kraken", ItemID.KRAKENPET, 6187, BOSS_TASK_ID, 18),
	KREE_ARRA("Kree'arra", ItemID.ARMADYLPET, 545, BOSS_TASK_ID, 1),
	KRIL_TSUTSAROTH("K'ril Tsutsaroth", ItemID.ZAMORAKPET, 6159, BOSS_TASK_ID, 4),
	LEVIATHAN("The Leviathan", ItemID.LEVIATHANPET, 6211, BOSS_TASK_ID, 30),
	PHANTOM_MUSPAH("Phantom Muspah", ItemID.MUSPAHPET, 6203, BOSS_TASK_ID, 26),
	SARACHNIS("Sarachnis", ItemID.SARACHNISPET, 6201, BOSS_TASK_ID, 25),
	SCORPIA("Scorpia", ItemID.SCORPIA_PET, 6181, BOSS_TASK_ID, 15),
	SHELLBANE_GRYPHON("Shellbane gryphon", ItemID.GRYPHONBOSSPET, 9405, BOSS_TASK_ID, 32),
	THERMONUCLEAR_SMOKE_DEVIL("Thermonuclear smoke devil", ItemID.SMOKEPET, 6189, BOSS_TASK_ID, 19),
	VARDORVIS("Vardorvis", ItemID.VARDORVISPET, 6205, BOSS_TASK_ID, 27),
	VENENATIS("Venenatis", ItemID.VENENATIS_PET, 6171, BOSS_TASK_ID, 10),
	VETION("Vet'ion", ItemID.VETION_PET, 6173, BOSS_TASK_ID, 11),
	VORKATH("Vorkath", ItemID.VORKATHPET, 6197, BOSS_TASK_ID, 23),
	WHISPERER("The Whisperer", ItemID.WHISPERERPET, 6209, BOSS_TASK_ID, 29),
	ZULRAH("Zulrah", ItemID.SNAKEPET, 6183, BOSS_TASK_ID, 16),

	UNKNOWN_BOSS("Unknown Boss", ItemID.CERT_GUIDE_ICON_DUMMY, 6331, BOSS_TASK_ID, -1);

	private final String name;
	private final int baseMonsterId;
	private final int structId;
	private final int slayerTargetId;
	private final int slayerTargetBossId;

	public boolean isBoss() {
		return slayerTargetId == BOSS_TASK_ID;
	}

	private static final Map<String, Task> SANITIZED_NAME_CACHE = new HashMap<>();
	private static final Map<Long, Task> BY_ID = new HashMap<>();

	private static final Map<Task, Set<SlayerArea>> SLAYER_AREA_MAPPING = new HashMap<>();

	static {
		for (Task task : values()) {
			SANITIZED_NAME_CACHE.putIfAbsent(sanitize(task.getName()), task);
			long packedKey = ((long) task.getSlayerTargetId() << 32) | (task.getSlayerTargetBossId() & 0xFFFFFFFFL);
			BY_ID.put(packedKey, task);
		}

		SLAYER_AREA_MAPPING.put(ABERRANT_SPECTRE, Set.of(SlayerArea.CATACOMBS_OF_KOUREND, SlayerArea.SLAYER_TOWER, SlayerArea.STRONGHOLD_SLAYER_DUNGEON));
		SLAYER_AREA_MAPPING.put(ABYSSAL_DEMON, Set.of(SlayerArea.CATACOMBS_OF_KOUREND, SlayerArea.THE_ABYSS, SlayerArea.SLAYER_TOWER));
		SLAYER_AREA_MAPPING.put(ANKOU, Set.of(SlayerArea.STRONGHOLD_OF_SECURITY, SlayerArea.STRONGHOLD_SLAYER_DUNGEON, SlayerArea.CATACOMBS_OF_KOUREND));
		SLAYER_AREA_MAPPING.put(AVIANSIE, Set.of(SlayerArea.GOD_WARS_DUNGEON));
		SLAYER_AREA_MAPPING.put(BASILISK, Set.of(SlayerArea.FREMENNIK_SLAYER_DUNGEON, SlayerArea.JORMUNGANDS_PRISON));
		SLAYER_AREA_MAPPING.put(BLACK_DEMON, Set.of(SlayerArea.CATACOMBS_OF_KOUREND, SlayerArea.CHASM_OF_FIRE, SlayerArea.TAVERLEY_DUNGEON, SlayerArea.BRIMHAVEN_DUNGEON));
		SLAYER_AREA_MAPPING.put(BLACK_DRAGON, Set.of(SlayerArea.CATACOMBS_OF_KOUREND, SlayerArea.MYTHS_GUILD_DUNGEON, SlayerArea.EVIL_CHICKENS_LAIR, SlayerArea.TAVERLEY_DUNGEON));
		SLAYER_AREA_MAPPING.put(BLOODVELD, Set.of(SlayerArea.CATACOMBS_OF_KOUREND, SlayerArea.GOD_WARS_DUNGEON, SlayerArea.IORWERTH_DUNGEON, SlayerArea.MEIYERDITCH, SlayerArea.SLAYER_TOWER, SlayerArea.STRONGHOLD_SLAYER_DUNGEON));
		SLAYER_AREA_MAPPING.put(BLUE_DRAGON, Set.of(SlayerArea.CATACOMBS_OF_KOUREND, SlayerArea.ISLE_OF_SOULS, SlayerArea.MYTHS_GUILD_DUNGEON, SlayerArea.OGRE_ENCLAVE, SlayerArea.RUINS_OF_TAPOYAUIK, SlayerArea.TAVERLEY_DUNGEON));
		SLAYER_AREA_MAPPING.put(BRONZE_DRAGON, Set.of(SlayerArea.CATACOMBS_OF_KOUREND, SlayerArea.BRIMHAVEN_DUNGEON));
		SLAYER_AREA_MAPPING.put(CAVE_KRAKEN, Set.of(SlayerArea.KRAKEN_COVE));
		SLAYER_AREA_MAPPING.put(DAGANNOTH, Set.of(SlayerArea.CATACOMBS_OF_KOUREND, SlayerArea.WATERBIRTH_ISLAND, SlayerArea.LIGHTHOUSE));
		SLAYER_AREA_MAPPING.put(DARK_BEAST, Set.of(SlayerArea.MOURNER_TUNNELS, SlayerArea.IORWERTH_DUNGEON));
		SLAYER_AREA_MAPPING.put(DRAKE, Set.of(SlayerArea.KARUULM_SLAYER_DUNGEON));
		SLAYER_AREA_MAPPING.put(DUST_DEVIL, Set.of(SlayerArea.CATACOMBS_OF_KOUREND, SlayerArea.SMOKE_DEVIL_DUNGEON));
		SLAYER_AREA_MAPPING.put(FIRE_GIANT, Set.of(SlayerArea.CATACOMBS_OF_KOUREND, SlayerArea.GIANTS_DEN, SlayerArea.ISLE_OF_SOULS, SlayerArea.KARUULM_SLAYER_DUNGEON, SlayerArea.SMOKE_DUNGEON, SlayerArea.WATERFALL_DUNGEON, SlayerArea.BRIMHAVEN_DUNGEON));
		SLAYER_AREA_MAPPING.put(FOSSIL_ISLAND_WYVERN, Set.of(SlayerArea.FOSSIL_ISLAND));
		SLAYER_AREA_MAPPING.put(GARGOYLE, Set.of(SlayerArea.SLAYER_TOWER));
		SLAYER_AREA_MAPPING.put(GREATER_DEMON, Set.of(SlayerArea.CATACOMBS_OF_KOUREND, SlayerArea.CHASM_OF_FIRE, SlayerArea.ISLE_OF_SOULS, SlayerArea.KARUULM_SLAYER_DUNGEON, SlayerArea.BRIMHAVEN_DUNGEON));
		SLAYER_AREA_MAPPING.put(GREEN_DRAGON, Set.of(SlayerArea.MYTHS_GUILD_DUNGEON));
		SLAYER_AREA_MAPPING.put(HELLHOUND, Set.of(SlayerArea.CATACOMBS_OF_KOUREND, SlayerArea.CHASM_OF_FIRE, SlayerArea.FREMENNIK_SLAYER_DUNGEON, SlayerArea.KARUULM_SLAYER_DUNGEON, SlayerArea.TAVERLEY_DUNGEON, SlayerArea.WITCHAVEN_DUNGEON));
		SLAYER_AREA_MAPPING.put(HYDRA, Set.of(SlayerArea.KARUULM_SLAYER_DUNGEON));
		SLAYER_AREA_MAPPING.put(IRON_DRAGON, Set.of(SlayerArea.CATACOMBS_OF_KOUREND, SlayerArea.ISLE_OF_SOULS, SlayerArea.KARUULM_SLAYER_DUNGEON, SlayerArea.BRIMHAVEN_DUNGEON));
		SLAYER_AREA_MAPPING.put(JELLY, Set.of(SlayerArea.CATACOMBS_OF_KOUREND, SlayerArea.FREMENNIK_SLAYER_DUNGEON, SlayerArea.RUINS_OF_TAPOYAUIK));
		SLAYER_AREA_MAPPING.put(KALPHITE, Set.of(SlayerArea.KALPHITE_LAIR, SlayerArea.KALPHITE_CAVE));
		SLAYER_AREA_MAPPING.put(KURASK, Set.of(SlayerArea.FREMENNIK_SLAYER_DUNGEON, SlayerArea.IORWERTH_DUNGEON));
		SLAYER_AREA_MAPPING.put(LIZARDMAN, Set.of(SlayerArea.LIZARDMAN_CANYON, SlayerArea.LIZARDMAN_SETTLEMENT, SlayerArea.KEBOS_SWAMP, SlayerArea.MOLCH));
		SLAYER_AREA_MAPPING.put(MITHRIL_DRAGON, Set.of(SlayerArea.ANCIENT_CAVERN));
		SLAYER_AREA_MAPPING.put(MUTATED_ZYGOMITE, Set.of(SlayerArea.FOSSIL_ISLAND, SlayerArea.ZANARIS));
		SLAYER_AREA_MAPPING.put(NECHRYAEL, Set.of(SlayerArea.CATACOMBS_OF_KOUREND, SlayerArea.IORWERTH_DUNGEON, SlayerArea.SLAYER_TOWER));
		SLAYER_AREA_MAPPING.put(RED_DRAGON, Set.of(SlayerArea.CATACOMBS_OF_KOUREND, SlayerArea.FORTHOS_DUNGEON, SlayerArea.BRIMHAVEN_DUNGEON, SlayerArea.MYTHS_GUILD_DUNGEON));
		SLAYER_AREA_MAPPING.put(RUNE_DRAGON, Set.of(SlayerArea.LITHKREN_VAULT));
		SLAYER_AREA_MAPPING.put(SKELETAL_WYVERN, Set.of(SlayerArea.ASGARNIAN_ICE_DUNGEON));
		SLAYER_AREA_MAPPING.put(SMOKE_DEVIL, Set.of(SlayerArea.SMOKE_DEVIL_DUNGEON));
		SLAYER_AREA_MAPPING.put(STEEL_DRAGON, Set.of(SlayerArea.CATACOMBS_OF_KOUREND, SlayerArea.KARUULM_SLAYER_DUNGEON, SlayerArea.BRIMHAVEN_DUNGEON));
		SLAYER_AREA_MAPPING.put(TROLL, Set.of(SlayerArea.DEATH_PLATEAU, SlayerArea.FREMENNIK_ISLES, SlayerArea.KELDAGRIM, SlayerArea.SOUTH_OF_MOUNT_QUIDAMORTEM, SlayerArea.TROLL_STRONGHOLD));
		SLAYER_AREA_MAPPING.put(TUROTH, Set.of(SlayerArea.FREMENNIK_SLAYER_DUNGEON));
		SLAYER_AREA_MAPPING.put(WATERFIEND, Set.of(SlayerArea.ANCIENT_CAVERN, SlayerArea.IORWERTH_DUNGEON, SlayerArea.KRAKEN_COVE));
		SLAYER_AREA_MAPPING.put(WYRM, Set.of(SlayerArea.KARUULM_SLAYER_DUNGEON, SlayerArea.NEYPOTZLI, SlayerArea.CHARRED_DUNGEON));
		SLAYER_AREA_MAPPING.put(LESSER_NAGUA, Set.of(SlayerArea.NEYPOTZLI, SlayerArea.RUINS_OF_TAPOYAUIK, SlayerArea.CRYPT_OF_TONALI));
		SLAYER_AREA_MAPPING.put(WARPED_CREATURE, Set.of(SlayerArea.POISON_WASTE_DUNGEON));
		SLAYER_AREA_MAPPING.put(VAMPYRE, Set.of(SlayerArea.DARKMEYER, SlayerArea.MEIYERDITCH));
	}

	private static String sanitize(String input) {
		if (input == null) return "";
		return input.replaceAll("[^a-zA-Z]", "").toLowerCase();
	}

	private static long getPackedKey(int targetId, int bossId)
	{
		return ((long) targetId << 32) | (bossId & 0xFFFFFFFFL);
	}

	public static Task getById(int targetId, int bossId) {
		long packedKey = getPackedKey(targetId, bossId);
		Task task = BY_ID.get(packedKey);

		if (task == null && targetId == BOSS_TASK_ID) {
			return UNKNOWN_BOSS;
		}
		return task;
	}

	public static Task getBossTask(int bossId) {
		return getById(BOSS_TASK_ID, bossId);
	}

	public static Task getNonBossTask(int targetId) {
		return getById(targetId, 0);
	}

	public static Task getTaskByName(String taskName) {
		if (taskName == null || taskName.isEmpty()) return null;
		return SANITIZED_NAME_CACHE.get(sanitize(taskName));
	}

	/**
	 * Natively evaluates whether a SlayerArea instance matches the Wiki matrix for this task.
	 */
	public boolean isAllowedKonarArea(SlayerArea area) {
		if (area == null || area == SlayerArea.NO_AREA_SPECIFIED) return true;
		if (this.isBoss()) return true;

		Set<SlayerArea> allowed = SLAYER_AREA_MAPPING.get(this);
		if (allowed == null || allowed.isEmpty()) return true;

		return allowed.contains(area);
	}

	/**
	 * String overload forwarding to the new enum type.
	 */
	public boolean isAllowedKonarArea(String jagexAreaName) {
		return isAllowedKonarArea(SlayerArea.getByName(jagexAreaName));
	}

	public static boolean isLegalId(int taskId, int bossId)
	{
		return BY_ID.containsKey(getPackedKey(taskId, bossId));
	}

	public static boolean isLegalTaskId(int taskId)
	{
		return BY_ID.containsKey(getPackedKey(taskId, 0));
	}

	public static boolean isLegalBossId(int bossId)
	{
		return BY_ID.containsKey(getPackedKey(BOSS_TASK_ID, bossId));
	}
}