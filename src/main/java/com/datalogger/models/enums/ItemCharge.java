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

import static com.datalogger.constants.PluginConstants.IGNORED_ENUM_ID;
import static com.datalogger.constants.PluginConstants.ITEM_VALUE_UPDATE_FREQUENCY_SECONDS;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;

public enum ItemCharge
{
	SCYTHE_OF_VITUR(ItemID.SCYTHE_OF_VITUR, 100, Map.of(ItemID.VIAL_BLOOD, 1L, ItemID.BLOODRUNE, 200L), null),
	TUMEKENS_SHADOW(ItemID.TUMEKENS_SHADOW, 1, Map.of(ItemID.SOULRUNE, 2L, ItemID.CHAOSRUNE, 5L), null),
	SANGUINESTI_STAFF(ItemID.SANGUINESTI_STAFF, 1, Map.of(ItemID.BLOODRUNE, 3L), null),
	AMULET_OF_BLOOD_FURY(ItemID.BLOOD_AMULET, 10000, Map.of(ItemID.BLOOD_SHARD, 1L), Map.of()),
	TOXIC_BLOWPIPE(ItemID.TOXIC_BLOWPIPE_LOADED, 1, Map.of(ItemID.SNAKEBOSS_SCALE, 1L), null),
	SERPENTINE_HELMET(ItemID.SERPENTINE_HELM_CHARGED, 1, Map.of(ItemID.SNAKEBOSS_SCALE, 1L), null),
	TOXIC_STAFF_OF_THE_DEAD(ItemID.TOXIC_SOTD_CHARGED, 1, Map.of(ItemID.SNAKEBOSS_SCALE, 1L), null),
	TOXIC_TRIDENT(ItemID.TOXIC_TOTS_CHARGED, 1, Map.of(ItemID.DEATHRUNE, 1L, ItemID.CHAOSRUNE, 1L, ItemID.FIRERUNE, 5L, ItemID.SNAKEBOSS_SCALE, 1L), null),
	TOXIC_TRIDENT_E(ItemID.TOXIC_TOTS_I_CHARGED, 1, Map.of(ItemID.DEATHRUNE, 1L, ItemID.CHAOSRUNE, 1L, ItemID.FIRERUNE, 5L, ItemID.SNAKEBOSS_SCALE, 1L), null),
	TRIDENT_OF_THE_SEAS(ItemID.TOTS, 1, Map.of(ItemID.DEATHRUNE, 1L, ItemID.CHAOSRUNE, 1L, ItemID.FIRERUNE, 5L, ItemID.COINS, 10L), Map.of(ItemID.DEATHRUNE, 1L, ItemID.CHAOSRUNE, 1L, ItemID.FIRERUNE, 5L)),
	TRIDENT_OF_THE_SEAS_E(ItemID.TOTS_I_CHARGED, 1, Map.of(ItemID.DEATHRUNE, 1L, ItemID.CHAOSRUNE, 1L, ItemID.FIRERUNE, 5L, ItemID.COINS, 10L), Map.of(ItemID.DEATHRUNE, 1L, ItemID.CHAOSRUNE, 1L, ItemID.FIRERUNE, 5L)),
	VENATOR_BOW(ItemID.VENATOR_BOW, 1, Map.of(ItemID.ANCIENT_ESSENCE, 1L), null),
	TONALZTICS_OF_RALOS(ItemID.TONALZTICS_OF_RALOS_CHARGED, 1, Map.of(ItemID.SUNFIRESPLINTER, 1L), null),
	WARPED_SCEPTRE(ItemID.WARPED_SCEPTRE, 1, Map.of(ItemID.CHAOSRUNE, 2L, ItemID.EARTHRUNE, 5L), null),
	VIGGORAS_CHAINMACE(ItemID.WILD_CAVE_CHAINMACE_CHARGED, 1, Map.of(ItemID.WILD_CAVE_SHARD, 1L), null),
	URSINE_CHAINMACE(ItemID.WILD_CAVE_URSINE_CHARGED, 1, Map.of(ItemID.WILD_CAVE_SHARD, 1L), null),
	CRAWS_BOW(ItemID.WILD_CAVE_BOW_CHARGED, 1, Map.of(ItemID.WILD_CAVE_SHARD, 1L), null),
	WEBWEAVER_BOW(ItemID.WILD_CAVE_WEBWEAVER_CHARGED, 1, Map.of(ItemID.WILD_CAVE_SHARD, 1L), null),
	THAMMARONS_SCEPTRE(ItemID.WILD_CAVE_SCEPTRE_CHARGED, 1, Map.of(ItemID.WILD_CAVE_SHARD, 1L), null),
	ACCURSED_SCEPTRE(ItemID.WILD_CAVE_ACCURSED_CHARGED, 1, Map.of(ItemID.WILD_CAVE_SHARD, 1L), null),
	BRACELET_OF_ETHEREUM(ItemID.WILD_CAVE_BRACELET_CHARGED, 1, Map.of(ItemID.WILD_CAVE_SHARD, 1L), null),
	TOME_OF_FIRE(ItemID.TOME_OF_FIRE, 20, Map.of(ItemID.WINT_BURNT_PAGE, 1L), null),
	TOME_OF_EARTH(ItemID.TOME_OF_EARTH, 20, Map.of(ItemID.SOILED_PAGE, 1L), null),
	TOME_OF_WATER(ItemID.TOME_OF_WATER, 20, Map.of(ItemID.SOAKED_PAGE, 1L), null),

	EYE_OF_AYAK(ItemID.EYE_OF_AYAK, 1, Map.of(ItemID.DEMON_TEAR, 1L), null),

	// NB TEARS and RUNES variant are used by ItemChargesParser
	EYE_OF_AYAK_TEARS(IGNORED_ENUM_ID, 1, Map.of(ItemID.DEMON_TEAR, 1L), null),
	EYE_OF_AYAK_RUNES(IGNORED_ENUM_ID, 1, Map.of(ItemID.CHAOSRUNE, 1L, ItemID.DEATHRUNE, 2L), null);

	@Getter
	private final int baseId;
	@Getter
	private final String formattedName;
	@Getter
	private final int nCharges;
	@Getter
	private final Map<Integer, Long> inputItem;
	@Getter
	private final Map<Integer, Long> outputItem;

	private Long chargeValue = null;
	private Long unchargeValue = null;
	private Instant cacheUpdateTime = null;

	private static final Map<Integer, ItemCharge> BY_BASE_ID = Arrays.stream(values())
		.filter(charge -> charge.getBaseId() != IGNORED_ENUM_ID)
		.collect(Collectors.toUnmodifiableMap(
			ItemCharge::getBaseId,
			Function.identity()
		));

	private static ItemManager itemManager = null;

	/**
	 * Sets the ItemManager for the ItemCharge enum.
	 */
	public static void setItemManager(ItemManager itemManager)
	{
		ItemCharge.itemManager = itemManager;
	}

	ItemCharge(int baseId, int nCharges, Map<Integer, Long> inputItem, Map<Integer, Long> outputItem)
	{
		this.baseId = baseId;
		this.nCharges = nCharges;
		this.inputItem = Map.copyOf(inputItem);
		this.outputItem = outputItem != null ? Map.copyOf(outputItem) : this.inputItem;
		formattedName = name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase().replace('_', ' ');
	}

	/**
	 * Compute the value per charge, cache it and return it. Or return the cached value, if it is still viable.
	 */
	public long getChargeValue()
	{
		if (itemManager == null)
		{
			return 0;
		}

		if (!isChargeValueExpired())
		{
			return chargeValue;
		}

		updateChargeValue();
		return chargeValue;
	}

	/**
	 * Compute the value per returned charge, cache it and return it. Or return the cached value, if it is still viable.
	 */
	public long getUnchargeValue()
	{
		if (itemManager == null)
		{
			return 0;
		}

		if (!isChargeValueExpired())
		{
			return unchargeValue;
		}

		updateChargeValue();
		return unchargeValue;
	}

	private boolean isChargeValueExpired()
	{
		return cacheUpdateTime == null || Instant.now().isAfter(cacheUpdateTime);
	}

	/**
	 * Compute the value of the items and quantities in the given mapping
	 */
	private long computeValue(Map<Integer, Long> items)
	{
		long totalCost = 0;

		for (Map.Entry<Integer, Long> entry : items.entrySet())
		{
			int itemId = entry.getKey();
			long quantityRequired = entry.getValue();

			long gePrice = itemId != ItemID.COINS ? itemManager.getItemPrice(itemId) : 1;

			totalCost += (gePrice * quantityRequired);
		}
		return totalCost / nCharges;
	}

	/**
	 * Updates charge and uncharge values
	 */
	private void updateChargeValue()
	{
		Instant curTime = Instant.now();
		chargeValue = computeValue(inputItem);
		unchargeValue = inputItem.equals(outputItem) ? chargeValue : computeValue(outputItem);
		cacheUpdateTime = curTime.plusSeconds(ITEM_VALUE_UPDATE_FREQUENCY_SECONDS);
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

	/**
	 * The name of the VaultType, lowercased and with hyphens instead of underscores.
	 */
	public String fileNameString()
	{
		return this.name().toLowerCase().replace("_", "-");
	}
}