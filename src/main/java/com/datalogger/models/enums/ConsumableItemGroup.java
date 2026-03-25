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

package com.datalogger.models.enums;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public enum ConsumableItemGroup
{
	ABSORPTION("Absorption", 11734, 11735, 11736, 11737),
	AGILITY_MIX("Agility mix", 11461, 11463),
	AGILITY_POTION("Agility potion", 3032, 3034, 3036, 3038),
	ANCIENT_BREW("Ancient brew", 26340, 26342, 26344, 26346),
	ANCIENT_MIX("Ancient mix", 26350, 26353),
	ANTI_VENOM("Anti-venom", 12905, 12907, 12909, 12911),
	ANTI_VENOMP("Anti-venom+", 12913, 12915, 12917, 12919),
	ANTIPOISON_SUPERMIX("Superantipoison mix", 11473, 11475),
	ANTIDOTEP_MIX("Antidote+ mix", 11501, 11503),
	ANTIDOTEP("Antidote+", 5943, 5945, 5947, 5949),
	ANTIDOTEPP("Antidote++", 5952, 5954, 5956, 5958),
	ANTIFIRE_MIX("Antifire mix", 11505, 11507),
	ANTIFIRE_POTION("Antifire potion", 2452, 2454, 2456, 2458),
	ANTIPOISON_MIX("Antipoison mix", 11433, 11435),
	ANTIPOISON("Antipoison", 2446, 175, 177, 179),
	ATTACK_MIX("Attack mix", 11429, 11431),
	ATTACK_POTION("Attack potion", 2428, 121, 123, 125),
	BASTION_POTION("Bastion potion", 22461, 22464, 22467, 22470),
	BATTLEMAGE_POTION("Battlemage potion", 22449, 22452, 22455, 22458),
	BLIGHTED_OVERLOAD("Blighted overload", 29631, 29634, 29637, 29640),
	BLIGHTED_SUPER_RESTORE("Blighted super restore", 24598, 24601, 24603, 24605),
	COMBAT_MIX("Combat mix", 11445, 11447),
	COMBAT_POTION("Combat potion", 9739, 9741, 9743, 9745),
	DEFENCE_MIX("Defence mix", 11457, 11459),
	DEFENCE_POTION("Defence potion", 2432, 133, 135, 137),
	DIVINE_BASTION_POTION("Divine bastion potion", 24635, 24638, 24641, 24644),
	DIVINE_BATTLEMAGE_POTION("Divine battlemage potion", 24623, 24626, 24629, 24632),
	DIVINE_MAGIC_POTION("Divine magic potion", 23745, 23748, 23751, 23754),
	DIVINE_RANGING_POTION("Divine ranging potion", 23733, 23736, 23739, 23742),
	DIVINE_SUPER_ATTACK_POTION("Divine super attack potion", 23697, 23700, 23703, 23706),
	DIVINE_SUPER_COMBAT_POTION("Divine super combat potion", 23685, 23688, 23691, 23694),
	DIVINE_SUPER_DEFENCE_POTION("Divine super defence potion", 23721, 23724, 23727, 23730),
	DIVINE_SUPER_STRENGTH_POTION("Divine super strength potion", 23709, 23712, 23715, 23718),
	ENERGY_MIX("Energy mix", 11453, 11455),
	ENERGY_POTION("Energy potion", 3008, 3010, 3012, 3014),
	EXTENDED_ANTIFIRE_MIX("Extended antifire mix", 11960, 11962),
	EXTENDED_ANTIFIRE("Extended antifire", 11951, 11953, 11955, 11957),
	EXTENDED_SUPER_ANTIFIRE_MIX("Extended super antifire mix", 22224, 22221),
	EXTENDED_SUPER_ANTIFIRE("Extended super antifire", 22209, 22212, 22215, 22218),
	FISHING_MIX("Fishing mix", 11477, 11479),
	FISHING_POTION("Fishing potion", 2438, 151, 153, 155),
	FORGOTTEN_BREW("Forgotten brew", 27629, 27632, 27635, 27638),
	GOADING_POTION("Goading potion", 30317, 30140, 30143, 30146),
	GOBLIN_POTION("Goblin potion", 26581, 26583, 26585, 26587),
	GUTHIX_REST("Guthix rest", 4417, 4419, 4421, 4423),
	HUNTER_POTION("Hunter potion", 9998, 10000, 10002, 10004),
	HUNTING_MIX("Hunting mix", 11517, 11519),
	MAGIC_ESSENCE_MIX("Magic essence mix", 11489, 11491),
	MAGIC_ESSENCE("Magic essence", 9021, 9022, 9023, 9024),
	MAGIC_MIX("Magic mix", 11515, 11517),
	MAGIC_POTION("Magic potion", 3040, 3042, 3044, 3046),
	MENAPHITE_REMEDY("Menaphite remedy", 27202, 27205, 27208, 27211),
	MOONLIGHT_POTION("Moonlight potion", 29080, 29081, 29082, 29083),
	OVERLOAD("Overload", 11730, 11731, 11732, 11733),
	PRAYER_MIX("Prayer mix", 11465, 11467),
	PRAYER_POTION("Prayer potion", 2434, 139, 141, 143),
	RANGING_MIX("Ranging mix", 11509, 11511),
	RANGING_POTION("Ranging potion", 2444, 169, 171, 173),
	RELICYMS_BALM("Relicym's balm", 4842, 4844, 4846, 4848),
	RELICYMS_MIX("Relicym's mix", 11437, 11439),
	RESTORE_MIX("Restore mix", 11449, 11451),
	RESTORE_POTION("Restore potion", 2430, 127, 129, 131),
	SANFEW_SERUM("Sanfew serum", 10925, 10927, 10929, 10931),
	SARADOMIN_BREW("Saradomin brew", 6685, 6687, 6689, 6691),
	STAMINA_MIX("Stamina mix", 12637, 12639),
	STAMINA_POTION("Stamina potion", 12625, 12627, 12629, 12631),
	STRENGTH_MIX("Strength mix", 11441, 11443),
	STRENGTH_POTION("Strength potion", 113, 115, 117, 119),
	SUPER_ANTIFIRE_MIX("Super antifire mix", 21991, 21994),
	SUPER_ANTIFIRE_POTION("Super antifire potion", 21978, 21981, 21984, 21987),
	SUPER_ATTACK("Super attack", 2436, 145, 147, 149),
	SUPER_COMBAT_POTION("Super combat potion", 12695, 12697, 12699, 12701),
	SUPER_DEFENCE_MIX("Super defence mix", 11497, 11499),
	SUPER_DEFENCE("Super defence", 2442, 163, 165, 167),
	SUPER_ENERGY_MIX("Super energy mix", 11481, 11483),
	SUPER_ENERGY("Super energy", 3016, 3018, 3020, 3022),
	SUPER_MAGIC_POTION("Super magic potion", 11726, 11727, 11728, 11729),
	SUPER_RANGING("Super ranging potion", 11722, 11723, 11724, 11725),
	SUPER_RESTORE_MIX("Super restore mix", 11493, 11495),
	SUPER_RESTORE("Super restore", 3024, 3026, 3028, 3030),
	SUPER_STR_MIX("Super strength mix", 11485, 11487),
	SUPER_STRENGTH("Super strength", 2440, 157, 159, 161),
	SUPERANTIPOISON("Superantipoison", 2448, 181, 183, 185),
	SUPERATTACK_MIX("Super attack mix", 11469, 11471),
	SURGE_POTION("Surge potion", 30875, 30878, 30881, 30884),
	ZAMORAK_BREW("Zamorak brew", 2450, 189, 191, 193),
	ZAMORAK_MIX("Zamorak mix", 11501, 11503);

	@Getter
	private final String baseItemName;
	private final int[] itemIds;

	private static final Map<Integer, ConsumableItemGroup> ITEM_TO_GROUP = new HashMap<>();
	private static final Map<Integer, Integer> POST_CONSUMPTION_ID = new HashMap<>();
	private static final Map<Integer, Integer> ITEM_TO_DOSES = new HashMap<>();

	static
	{
		for (ConsumableItemGroup group : values())
		{
			int nIds = group.itemIds.length;
			Integer postConsumption = null;
			for (int i = 0; i < nIds; i++)
			{
				int dose = Math.abs(i-nIds);
				int id = group.itemIds[i];
				ITEM_TO_GROUP.put(id, group);
				ITEM_TO_DOSES.put(id, dose);
				if (postConsumption != null)
				{
					POST_CONSUMPTION_ID.put(postConsumption, id);
				}

				postConsumption = id;
			}
		}
	}

	ConsumableItemGroup(String baseItemName, int... itemIds)
	{
		this.baseItemName = baseItemName;
		this.itemIds = itemIds;
	}

	/**
	 * Returns the expected itemId that results from consuming the given itemId, if any
	 */
	public static Integer getPostConsumptionId(int itemId)
	{
		return POST_CONSUMPTION_ID.getOrDefault(itemId, null);
	}

	/**
	 * Returns the ConsumableItemGroup associated with the given itemId, if any
	 */
	public static ConsumableItemGroup getGroup(int itemId)
	{
		return ITEM_TO_GROUP.getOrDefault(itemId, null);
	}

	/**
	 * Return true if itemId has an associated ConsumableItemGroup
	 */
	public static boolean hasGroup(int itemId)
	{
		return ITEM_TO_DOSES.containsKey(itemId);
	}

	/**
	 * Given an amount of items, convert it to the total amount of charges/consumable doses
	 */
	public static Map<ConsumableItemGroup, Integer> convertToDoses(int itemId, int quantity)
	{
		ConsumableItemGroup group = getGroup(itemId);
		if (group == null) return null;

		int nDoses = ITEM_TO_DOSES.get(itemId) * quantity;
		return Map.of(group, nDoses);
	}
}
