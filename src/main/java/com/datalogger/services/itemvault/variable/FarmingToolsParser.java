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

package com.datalogger.services.itemvault.variable;

import com.datalogger.models.enums.VaultType;
import com.datalogger.models.itemvault.BankedItem;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;

@Slf4j
@Singleton
public class FarmingToolsParser extends AbstractVariableVaultParser
{
	private final Map<Integer, String> itemNameCache = new HashMap<>();

	private static final Map<Integer, ItemRatio[]> TOOL_VARBITS = Map.ofEntries(
		Map.entry(VarbitID.FARMING_TOOLS_RAKE, new ItemRatio[]{ new ItemRatio(ItemID.RAKE, 1) }),
		Map.entry(VarbitID.FARMING_TOOLS_DIBBER, new ItemRatio[]{ new ItemRatio(ItemID.DIBBER, 1) }),
		Map.entry(VarbitID.FARMING_TOOLS_SPADE, new ItemRatio[]{ new ItemRatio(ItemID.SPADE, 1) }),
		Map.entry(VarbitID.FARMING_TOOLS_SECATEURS, new ItemRatio[]{ new ItemRatio(ItemID.SECATEURS, 1) }),
		Map.entry(VarbitID.FARMING_TOOLS_FAIRYSECATEURS, new ItemRatio[]{ new ItemRatio(ItemID.FAIRY_ENCHANTED_SECATEURS, 1) }),
		Map.entry(VarbitID.FARMING_TOOLS_WATERINGCAN, new ItemRatio[]{ new ItemRatio(ItemID.WATERING_CAN_8, 1) }),
		Map.entry(VarbitID.FARMING_TOOLS_TROWEL, new ItemRatio[]{ new ItemRatio(ItemID.GARDENING_TROWEL, 1) }),
		Map.entry(VarbitID.FARMING_TOOLS_PLANTCURE, new ItemRatio[]{ new ItemRatio(ItemID.PLANT_CURE, 1) }),
		Map.entry(VarbitID.FARMING_TOOLS_BUCKETS, new ItemRatio[]{ new ItemRatio(ItemID.BUCKET_EMPTY, 1) }),
		Map.entry(VarbitID.FARMING_TOOLS_COMPOST, new ItemRatio[]{ new ItemRatio(ItemID.BUCKET_COMPOST, 1) }),
		Map.entry(VarbitID.FARMING_TOOLS_SUPERCOMPOST, new ItemRatio[]{ new ItemRatio(ItemID.BUCKET_SUPERCOMPOST, 1) }),
		Map.entry(VarbitID.FARMING_TOOLS_ULTRACOMPOST, new ItemRatio[]{ new ItemRatio(ItemID.BUCKET_ULTRACOMPOST, 1) })
	);

	@Override
	public VaultType getVaultType()
	{
		return VaultType.FARMING_TOOLS;
	}

	@Override
	protected Set<Integer> getTrackedVarbitIds()
	{
		Set<Integer> tracked = new java.util.HashSet<>(TOOL_VARBITS.keySet());
		tracked.add(VarbitID.FARMING_TOOLS_BOTTOMLESS_BUCKET_TYPE);
		return tracked;
	}

	@Override
	protected List<BankedItem> translateVariablesToItems()
	{
		return buildItemsFromVarbits(currentVarbitValues, currentAccountHash, currentAccountName);
	}

	private List<BankedItem> buildItemsFromVarbits(Map<Integer, Integer> varbitMap, long accountHash, String accountName)
	{
		Map<Integer, Long> consolidatedQuantities = new HashMap<>();

		for (Map.Entry<Integer, ItemRatio[]> entry : TOOL_VARBITS.entrySet())
		{
			int varbitValue = varbitMap.getOrDefault(entry.getKey(), 0);
			if (varbitValue <= 0) continue;

			for (ItemRatio ratio : entry.getValue())
			{
				long totalQuantity = (long) varbitValue * ratio.getQuantityPerVarbit();
				consolidatedQuantities.merge(ratio.getItemId(), totalQuantity, Long::sum);
			}
		}

		int bucketType = varbitMap.getOrDefault(VarbitID.FARMING_TOOLS_BOTTOMLESS_BUCKET_TYPE, 0);
		if (bucketType > 0)
		{
			consolidatedQuantities.merge(ItemID.BOTTOMLESS_COMPOST_BUCKET, 1L, Long::sum);
		}

		List<BankedItem> items = new ArrayList<>();
		for (Map.Entry<Integer, Long> entry : consolidatedQuantities.entrySet())
		{
			int itemId = entry.getKey();
			String itemName = itemNameCache.computeIfAbsent(itemId, id -> itemManager.getItemComposition(id).getName());

			items.add(new BankedItem(
				getVaultLabel(),
				accountHash,
				accountName,
				itemId,
				itemName,
				entry.getValue()
			));
		}
		return items;
	}

	@Override
	protected void loadSessionData(File cacheFile)
	{
		List<BankedItem> loadedItems = fileIOService.readJson(cacheFile, BankedItem.LIST_TYPE);

		if (loadedItems == null || loadedItems.isEmpty())
		{
			return;
		}

		currentVarbitValues.clear();

		for (BankedItem item : loadedItems)
		{
			int targetItemId = item.getItemId();
			long quantity = item.getQuantity();

			if (targetItemId == ItemID.BOTTOMLESS_COMPOST_BUCKET)
			{
				currentVarbitValues.put(VarbitID.FARMING_TOOLS_BOTTOMLESS_BUCKET_TYPE, 1);
				continue;
			}

			for (Map.Entry<Integer, ItemRatio[]> entry : TOOL_VARBITS.entrySet())
			{
				int varbitId = entry.getKey();
				ItemRatio ratio = entry.getValue()[0];

				if (ratio.getItemId() == targetItemId)
				{
					currentVarbitValues.put(varbitId, (int) (quantity / ratio.getQuantityPerVarbit()));
					break;
				}
			}
		}
	}
}