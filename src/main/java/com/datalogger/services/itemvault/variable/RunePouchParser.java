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

import com.datalogger.constants.Item;
import com.datalogger.models.enums.VaultType;
import com.datalogger.models.itemvault.BankedItem;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;

@Singleton
public class RunePouchParser extends AbstractVariableVaultParser
{
	private final Map<Integer, String> itemNameCache = new HashMap<>();

	@Override
	public VaultType getVaultType()
	{
		return VaultType.RUNE_POUCH;
	}

	@Override
	protected Set<Integer> getTrackedVarbitIds()
	{
		Set<Integer> trackedVarbits = new HashSet<>();
		for (int typeVarbit : Item.RUNE_POUCH_TYPE_VARBITS) trackedVarbits.add(typeVarbit);
		for (int qtyVarbit : Item.RUNE_POUCH_AMOUNT_VARBITS) trackedVarbits.add(qtyVarbit);
		return trackedVarbits;
	}

	@Override
	protected List<BankedItem> translateVariablesToItems()
	{
		List<BankedItem> items = new ArrayList<>();

		final EnumComposition runepouchEnum = client.getEnum(EnumID.RUNEPOUCH_RUNE);

		if (runepouchEnum == null) return items;

		for (int i = 0; i < Item.RUNE_POUCH_SLOTS; i++)
		{
			int typeVarbitId = Item.RUNE_POUCH_TYPE_VARBITS[i];
			int amountVarbitId = Item.RUNE_POUCH_AMOUNT_VARBITS[i];

			int runeTypeEnumIndex = currentVarbitValues.getOrDefault(typeVarbitId, 0);
			int runeQuantity = currentVarbitValues.getOrDefault(amountVarbitId, 0);

			if (runeTypeEnumIndex > 0 && runeQuantity > 0)
			{
				int itemId = runepouchEnum.getIntValue(runeTypeEnumIndex);

				if (itemId != -1)
				{
					String itemName = itemNameCache.computeIfAbsent(itemId, id -> itemManager.getItemComposition(id).getName());

					items.add(new BankedItem(
						getVaultType(),
						currentAccountHash,
						currentAccountName,
						itemId,
						itemName,
						runeQuantity
					));
				}
			}
		}
		return items;
	}

	@Override
	protected void loadSessionData(File cacheFile)
	{
		List<BankedItem> loadedItems = fileIOService.readJson(cacheFile, BankedItem.LIST_TYPE);

		if (loadedItems == null || loadedItems.isEmpty()) return;

		final EnumComposition runepouchEnum = client.getEnum(EnumID.RUNEPOUCH_RUNE);
		if (runepouchEnum == null) return;

		currentVarbitValues.clear();

		int itemsToLoad = Math.min(loadedItems.size(), Item.RUNE_POUCH_SLOTS);

		for (int i = 0; i < itemsToLoad; i++)
		{
			BankedItem item = loadedItems.get(i);
			int targetItemId = item.getItemId();
			int enumIndex = -1;

			for (int key : runepouchEnum.getKeys())
			{
				if (runepouchEnum.getIntValue(key) == targetItemId)
				{
					enumIndex = key;
					break;
				}
			}

			if (enumIndex != -1)
			{
				int typeVarbitId = Item.RUNE_POUCH_TYPE_VARBITS[i];
				int amountVarbitId = Item.RUNE_POUCH_AMOUNT_VARBITS[i];

				currentVarbitValues.put(typeVarbitId, enumIndex);
				currentVarbitValues.put(amountVarbitId, (int) item.getQuantity());
			}
		}
	}
}