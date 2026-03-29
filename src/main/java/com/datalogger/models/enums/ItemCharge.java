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

import static com.datalogger.constants.PluginConstants.ITEM_VALUE_UPDATE_FREQUENCY_SECONDS;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;


public enum ItemCharge
{
	SCYTHE_OF_VITUR(ItemID.SCYTHE_OF_VITUR, 100, Map.of(ItemID.VIAL_BLOOD, 1, ItemID.BLOODRUNE, 200)),
	TUMEKENS_SHADOW(ItemID.TUMEKENS_SHADOW, 1, Map.of(ItemID.SOULRUNE, 2, ItemID.CHAOSRUNE, 5)),
	SANGUINESTI_STAFF(ItemID.SANGUINESTI_STAFF, 1, Map.of(ItemID.BLOODRUNE, 3)),
	EYE_OF_AYAK(ItemID.EYE_OF_AYAK, 1, Map.of(ItemID.DEMON_TEAR, 1)),
	AMULET_OF_BLOOD_FURY(ItemID.BLOOD_AMULET, 10000, Map.of(ItemID.BLOOD_SHARD, 1)),
	TOXIC_TRIDENT(ItemID.TOXIC_TOTS_CHARGED, 1, Map.of(ItemID.DEATHRUNE, 1, ItemID.CHAOSRUNE, 1, ItemID.FIRERUNE, 5, ItemID.SNAKEBOSS_SCALE, 1)),
	TRIDENT_OF_THE_SEAS(ItemID.TOTS, 1, Map.of(ItemID.DEATHRUNE, 1, ItemID.CHAOSRUNE, 1, ItemID.FIRERUNE, 5, ItemID.COINS, 10)),
	VENATOR_BOW(ItemID.VENATOR_BOW, 1, Map.of(ItemID.ANCIENT_ESSENCE, 1));

	private final int baseId;
	@Getter
	private final String formattedName;
	private final int nCharges;
	private final Map<Integer, Integer> inputItem;
	private Integer chargeValue = null;
	private Instant cacheUpdateTime = null;

	private static final Map<Integer, ItemCharge> BY_BASE_ID = new HashMap<>();
	static
	{
		for (ItemCharge itemCharge : values())
		{
			BY_BASE_ID.put(itemCharge.baseId, itemCharge);
		}
	}

	private static ItemManager itemManager = null;

	/**
	 * Sets the ItemManager for the ItemCharge enum.
	 */
	public static void setItemManager(ItemManager itemManager)
	{
		ItemCharge.itemManager = itemManager;
	}

	ItemCharge(int baseId, int nCharges, Map<Integer, Integer> inputItem)
	{
		this.baseId = baseId;
		this.nCharges = nCharges;
		this.inputItem = inputItem;
		formattedName = name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase().replace('_', ' ');
	}

	/**
	 * Compute the value per charge, cache it and return it. Or return the cached value, if it is still viable.
	 */
	public int getChargeValue()
	{
		if (itemManager == null) return 0;

		Instant curTime = Instant.now();
		if (cacheUpdateTime != null && curTime.isBefore(cacheUpdateTime))
		{
			return chargeValue;
		}

		int totalCost = 0;

		for (Map.Entry<Integer, Integer> entry : inputItem.entrySet())
		{
			int itemId = entry.getKey();
			int quantityRequired = entry.getValue();

			int gePrice = itemId != ItemID.COINS ? itemManager.getItemPrice(itemId) : 1;

			totalCost += (gePrice * quantityRequired);
		}

		chargeValue = totalCost / nCharges;
		cacheUpdateTime = curTime.plusSeconds(ITEM_VALUE_UPDATE_FREQUENCY_SECONDS);
		return chargeValue;
	}

	/**
	 * Return the ItemCharge associated with the baseId
	 */
	public static ItemCharge getByBaseId(int baseId)
	{
		return BY_BASE_ID.getOrDefault(baseId, null);
	}

	/**
	 * Return the ItemCharge associated with the itemId. The itemId is converted to a baseId and used to get the ItemCharge.
	 */
	public static ItemCharge getByItemId(int itemId)
	{
		return BY_BASE_ID.getOrDefault(ItemVariationMapping.map(itemId), null);
	}
}
