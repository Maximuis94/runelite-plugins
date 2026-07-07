package com.slayerbanktab.models;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SlayerArea {

	NO_AREA_SPECIFIED(0, "No area specified"),
	CATACOMBS_OF_KOUREND(1, "Catacombs of Kourend"),
	STRONGHOLD_SLAYER_DUNGEON(2, "Stronghold Slayer Dungeon"),
	KARUULM_SLAYER_DUNGEON(3, "Karuulm Slayer Dungeon"),
	CHASM_OF_FIRE(4, "Chasm of Fire"),
	BRIMHAVEN_DUNGEON(5, "Brimhaven Dungeon"),
	TAVERLEY_DUNGEON(6, "Taverley Dungeon"),
	WITCHAVEN_DUNGEON(7, "Witchaven Dungeon"),
	WATERFALL_DUNGEON(8, "Waterfall Dungeon"),
	SLAYER_TOWER(9, "Slayer Tower"),
	GOD_WARS_DUNGEON(10, "God Wars Dungeon"),
	KALPHITE_LAIR(11, "Kalphite Lair"),
	KALPHITE_CAVE(12, "task-only Kalphite Cave"),
	KRAKEN_COVE(13, "Kraken Cove"),
	LIGHTHOUSE(14, "in the Lighthouse"),
	WATERBIRTH_ISLAND(15, "Waterbirth Island"),
	LIZARDMAN_CANYON(16, "Lizardman Canyon"),
	MOLCH(17, "Molch"),
	LIZARDMAN_SETTLEMENT(18, "Lizardman Settlement"),
	SMOKE_DEVIL_DUNGEON(19, "Smoke Devil Dungeon"),
	SMOKE_DUNGEON(20, "Smoke Dungeon"),
	DEATH_PLATEAU(21, "Death Plateau"),
	TROLL_STRONGHOLD(22, "Troll Stronghold"),
	KELDAGRIM(23, "Keldagrim"),
	SOUTH_OF_MOUNT_QUIDAMORTEM(24, "South of Mount Quidamortem"),
	FREMENNIK_ISLES(25, "Fremennik Isles"),
	FREMENNIK_SLAYER_DUNGEON(26, "Fremennik Slayer Dungeon"),
	MYTHS_GUILD_DUNGEON(27, "Myths' Guild Dungeon"),
	MOURNER_TUNNELS(28, "Mourner Tunnels"),
	LITHKREN_VAULT(29, "Lithkren Vault"),
	ANCIENT_CAVERN(30, "Ancient Cavern"),
	STRONGHOLD_OF_SECURITY(31, "Stronghold of Security"),
	FOSSIL_ISLAND(32, "Fossil Island"),
	ASGARNIAN_ICE_DUNGEON(33, "Asgarnian Ice Dungeon"),
	OGRE_ENCLAVE(34, "Ogre Enclave"),
	BRINE_RAT_CAVERN(35, "Brine Rat Cavern"),
	ZANARIS(36, "Zanaris"),
	EVIL_CHICKENS_LAIR(37, "Evil Chicken's Lair"),
	THE_ABYSS(38, "The Abyss"),
	KEBOS_SWAMP(39, "Kebos Swamp"),
	THE_BATTLEFRONT(40, "The Battlefront"),
	FORTHOS_DUNGEON(41, "Forthos Dungeon"),
	IORWERTH_DUNGEON(42, "Iorwerth Dungeon"),
	JORMUNGANDS_PRISON(43, "Jormungand's Prison"),
	DARKMEYER(44, "Darkmeyer"),
	SLEPE(45, "Slepe"),
	MEIYERDITCH(46, "Meiyerditch Laboratories"),
	ISLE_OF_SOULS(47, "Isle of Souls"),
	GIANTS_DEN(48, "Giants' Den"),
	POISON_WASTE_DUNGEON(49, "Poison Waste Dungeon"),
	NEYPOTZLI(50, "Neypotzli"),
	RUINS_OF_TAPOYAUIK(51, "Tapoyauik"),
	CRYPT_OF_TONALI(52, "Crypt of Tonali"),
	GREAT_CONCH(53, "Great Conch"),
	CHARRED_DUNGEON(54, "Charred Dungeon");

	private final int id;
	private final String name;

	private static final Map<Integer, SlayerArea> ID_MAP = new HashMap<>();
	private static final Map<String, SlayerArea> NAME_MAP = new HashMap<>();

	static {
		for (SlayerArea area : values()) {
			ID_MAP.put(area.id, area);
			NAME_MAP.put(sanitize(area.name), area);
		}
		NAME_MAP.put("kalphitecave", KALPHITE_CAVE);
		NAME_MAP.put("lighthouse", LIGHTHOUSE);
	}

	private static String sanitize(String input) {
		if (input == null) return "";
		return input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
	}

	public static SlayerArea getById(int id) {
		return ID_MAP.getOrDefault(id, NO_AREA_SPECIFIED);
	}

	public static SlayerArea getByName(String name) {
		if (name == null || name.isEmpty() || name.contains("NA")) {
			return NO_AREA_SPECIFIED;
		}
		String clean = sanitize(name);
		SlayerArea resolved = NAME_MAP.get(clean);
		if (resolved != null) return resolved;

		for (SlayerArea area : values()) {
			if (area != NO_AREA_SPECIFIED && (clean.contains(sanitize(area.name)) || sanitize(area.name).contains(clean))) {
				return area;
			}
		}
		return NO_AREA_SPECIFIED;
	}

	public static boolean isLegalId(int areaId)
	{
		return ID_MAP.containsKey(areaId);
	}

	@Override
	public String toString() {
		return name;
	}
}