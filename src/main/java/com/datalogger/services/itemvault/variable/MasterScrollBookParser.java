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
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
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
public class MasterScrollBookParser extends AbstractVariableVaultParser
{
	private final Map<Integer, String> itemNameCache = new HashMap<>();
	private static final Map<Integer, ItemRatio[]> SCROLL_VARBITS = Map.ofEntries(
		Map.entry(VarbitID.BOOKOFSCROLLS_NARDAH, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_NARDAH, 1) }),
		Map.entry(VarbitID.BOOKOFSCROLLS_DIGSITE, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_DIGSITE, 1) }),
		Map.entry(VarbitID.BOOKOFSCROLLS_FELDIP, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_FELDIP, 1) }),
		Map.entry(VarbitID.BOOKOFSCROLLS_LUNARISLE, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_LUNARISLE, 1) }),
		Map.entry(VarbitID.BOOKOFSCROLLS_MORTTON, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_MORTTON, 1) }),
		Map.entry(VarbitID.BOOKOFSCROLLS_PESTCONTROL, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_PESTCONTROL, 1) }),
		Map.entry(VarbitID.BOOKOFSCROLLS_PISCATORIS, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_PISCATORIS, 1) }),
		Map.entry(VarbitID.BOOKOFSCROLLS_TAIBWO, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_TAIBWO, 1) }),
		Map.entry(VarbitID.BOOKOFSCROLLS_ELF, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_ELF, 1) }),
		Map.entry(VarbitID.BOOKOFSCROLLS_MOSLES, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_MOSLES, 1) }),
		Map.entry(VarbitID.BOOKOFSCROLLS_LUMBERYARD, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_LUMBERYARD, 1) }),
		Map.entry(VarbitID.BOOKOFSCROLLS_ZULANDRA, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_ZULANDRA, 1) }),
		Map.entry(VarbitID.BOOKOFSCROLLS_CERBERUS, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_CERBERUS, 1) }),
		Map.entry(VarbitID.BOOKOFSCROLLS_REVENANTS, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_REVENANTS, 1) }),
		Map.entry(VarbitID.BOOKOFSCROLLS_GUTHIXIAN_TEMPLE, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_GUTHIXIAN_TEMPLE, 1) }),
		Map.entry(VarbitID.BOOKOFSCROLLS_SPIDERCAVE, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_SPIDERCAVE, 1) }),
		Map.entry(VarbitID.BOOKOFSCROLLS_COLOSSAL_WYRM, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_COLOSSAL_WYRM, 1) }),
		Map.entry(VarbitID.BOOKOFSCROLLS_CHASMOFFIRE, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_CHASMOFFIRE, 1) }),

		Map.entry(VarbitID.BOOKOFSCROLLS_WATSON_HIGHBITS, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_WATSON, 1) }),
		Map.entry(VarbitID.BOOKOFSCROLLS_WATSON_LOWBITS, new ItemRatio[]{ new ItemRatio(ItemID.TELEPORTSCROLL_WATSON, 1) })
	);

	@Override
	public VaultType getVaultType()
	{
		return VaultType.MASTER_SCROLL_BOOK;
	}

	@Override
	protected Set<Integer> getTrackedVarbitIds()
	{
		return SCROLL_VARBITS.keySet();
	}

	@Override
	protected List<BankedItem> translateVariablesToItems()
	{
		List<BankedItem> items = new ArrayList<>();

		for (Map.Entry<Integer, ItemRatio[]> entry : SCROLL_VARBITS.entrySet())
		{
			int varbitId = entry.getKey();

			if (varbitId == VarbitID.BOOKOFSCROLLS_WATSON_HIGHBITS || varbitId == VarbitID.BOOKOFSCROLLS_WATSON_LOWBITS)
			{
				continue;
			}

			int amount = currentVarbitValues.getOrDefault(varbitId, 0);
			if (amount > 0)
			{
				ItemRatio ratio = entry.getValue()[0];
				String name = itemNameCache.computeIfAbsent(ratio.getItemId(), id -> itemManager.getItemComposition(id).getName());

				items.add(new BankedItem(
					getVaultLabel(),
					currentAccountHash,
					currentAccountName,
					ratio.getItemId(),
					name,
					(long) amount * ratio.getQuantityPerVarbit()
				));
			}
		}

		int watsonLow = currentVarbitValues.getOrDefault(VarbitID.BOOKOFSCROLLS_WATSON_LOWBITS, 0);
		int watsonHigh = currentVarbitValues.getOrDefault(VarbitID.BOOKOFSCROLLS_WATSON_HIGHBITS, 0);
		int watsonTotal = (watsonHigh << 14) | watsonLow;

		if (watsonTotal > 0)
		{
			String name = itemNameCache.computeIfAbsent(ItemID.TELEPORTSCROLL_WATSON, id -> itemManager.getItemComposition(id).getName());
			items.add(new BankedItem(
				getVaultLabel(),
				currentAccountHash,
				currentAccountName,
				ItemID.TELEPORTSCROLL_WATSON,
				name,
				watsonTotal
			));
		}

		return items;
	}

//	@Override
//	public List<BankedItem> parseOfflineFile(long accountHash, File vaultFile)
//	{
//		VariableState loadedState = fileIOService.readJson(vaultFile, VariableState.class);
//
//		if (loadedState == null || loadedState.varbits == null)
//		{
//			return new ArrayList<>();
//		}
//		return buildItemsFromVarbits(loadedState.varbits, accountHash, "Offline Account");
//	}

	/**
	 * Shared logic to convert a map of Varbits into a list of exported BankedItems.
	 */
	private List<BankedItem> buildItemsFromVarbits(Map<Integer, Integer> varbitMap, long accountHash, String accountName)
	{
		Map<Integer, Long> consolidatedQuantities = new HashMap<>();

		for (Map.Entry<Integer, ItemRatio[]> entry : SCROLL_VARBITS.entrySet())
		{
			int varbitValue = varbitMap.getOrDefault(entry.getKey(), 0);
			if (varbitValue <= 0) continue;

			for (ItemRatio ratio : entry.getValue())
			{
				long totalQuantity = (long) varbitValue * ratio.getQuantityPerVarbit();
				consolidatedQuantities.merge(ratio.getItemId(), totalQuantity, Long::sum);
			}
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

//	@Override
//	protected void loadSessionData(File cacheFile)
//	{
//		Type type = new TypeToken<List<BankedItem>>(){}.getType();
//		List<BankedItem> loadedItems = fileIOService.readJson(cacheFile, type);
//
//		if (loadedItems == null || loadedItems.isEmpty())
//		{
//			return;
//		}
//
//		currentVarbitValues.clear();
//
//		for (BankedItem item : loadedItems)
//		{
//			int targetItemId = item.getItemId();
//			long quantity = item.getQuantity();
//
//			if (targetItemId == ItemID.TELEPORTSCROLL_WATSON)
//			{
//				currentVarbitValues.put(VarbitID.BOOKOFSCROLLS_WATSON_LOWBITS, (int) (quantity & 0x3FFF));
//				currentVarbitValues.put(VarbitID.BOOKOFSCROLLS_WATSON_HIGHBITS, (int) (quantity >> 14));
//				continue; // Move to next item
//			}
//
//			for (Map.Entry<Integer, ItemRatio[]> entry : SCROLL_VARBITS.entrySet())
//			{
//				int varbitId = entry.getKey();
//
//				if (varbitId == VarbitID.BOOKOFSCROLLS_WATSON_HIGHBITS || varbitId == VarbitID.BOOKOFSCROLLS_WATSON_LOWBITS)
//				{
//					continue;
//				}
//
//				ItemRatio ratio = entry.getValue()[0];
//				if (ratio.getItemId() == targetItemId)
//				{
//					currentVarbitValues.put(varbitId, (int) (quantity / ratio.getQuantityPerVarbit()));
//					log.debug("Parsed {}x {} from scrollbook cache file", item.getQuantity(), item.getItemName());
//					break;
//				}
//			}
//		}
//	}
}