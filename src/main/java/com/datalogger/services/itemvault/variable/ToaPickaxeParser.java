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
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarbitID;

@Slf4j
@Singleton
public class ToaPickaxeParser extends AbstractVariableVaultParser
{
	private static final int TOA_PICKAXE_VARBIT = VarbitID.TOA_PICKAXE_STORED;

	private static final Map<Integer, Integer> PICKAXE_MAPPING = Map.ofEntries(
		Map.entry(1, ItemID.IRON_PICKAXE),
		Map.entry(2, ItemID.STEEL_PICKAXE),
		Map.entry(3, ItemID.BLACK_PICKAXE),
		Map.entry(4, ItemID.MITHRIL_PICKAXE),
		Map.entry(5, ItemID.ADAMANT_PICKAXE),
		Map.entry(6, ItemID.RUNE_PICKAXE),
		Map.entry(7, ItemID.TRAIL_GILDED_PICKAXE),
		Map.entry(8, ItemID.DRAGON_PICKAXE),
		Map.entry(9, ItemID.DRAGON_PICKAXE_PRETTY),
		Map.entry(10, ItemID.ZALCANO_PICKAXE),
//		Map.entry(11, ItemID._3A_PICKAXE),
//		Map.entry(12, ItemID.INFERNAL_PICKAXE),
//		Map.entry(13, ItemID.INFERNAL_PICKAXE_EMPTY),
		Map.entry(14, ItemID.CRYSTAL_PICKAXE),
//		Map.entry(15, ItemID.TRAILBLAZER_PICKAXE),
//		Map.entry(16, ItemID.TRAILBLAZER_PICKAXE_EMPTY),
		Map.entry(17, ItemID.TRAILBLAZER_PICKAXE_NO_INFERNAL)
//		Map.entry(18, ItemID.TRAILBLAZER_RELOADED_PICKAXE),
//		Map.entry(19, ItemID.TRAILBLAZER_RELOADED_PICKAXE_EMPTY),
//		Map.entry(20, ItemID.TRAILBLAZER_RELOADED_PICKAXE_NO_INFERNAL)
	);

	@Override
	public VaultType getVaultType()
	{
		return VaultType.TOA_PICKAXE;
	}

	@Override
	protected Set<Integer> getTrackedVarbitIds()
	{
		return Set.of(TOA_PICKAXE_VARBIT);
	}

	@Override
	protected List<BankedItem> translateVariablesToItems()
	{
		int pickaxeIndex = currentVarbitValues.getOrDefault(TOA_PICKAXE_VARBIT, 0);

		if (!isEnabled || pickaxeIndex <= 0 || !PICKAXE_MAPPING.containsKey(pickaxeIndex))
		{
			return new ArrayList<>();
		}

		int pickaxeItemId = PICKAXE_MAPPING.get(pickaxeIndex);
		String itemName = itemManager.getItemComposition(pickaxeItemId).getName();

		return List.of(new BankedItem(
			getVaultLabel(),
			currentAccountHash,
			currentAccountName,
			pickaxeItemId,
			itemName,
			1L
		));
	}

	@Override
	protected void loadSessionData(File cacheFile)
	{
		int existingVarbitValue = currentVarbitValues.getOrDefault(TOA_PICKAXE_VARBIT, 0);
		if (existingVarbitValue > 0)
		{
			log.debug("ToA Pickaxe varbit already populated from client state. Skipping cache load.");
			return;
		}

		List<BankedItem> loadedItems = fileIOService.readJson(cacheFile, BankedItem.LIST_TYPE);

		if (loadedItems == null || loadedItems.isEmpty())
		{
			return;
		}

		BankedItem item = loadedItems.get(0);
		int targetItemId = item.getItemId();

		for (Map.Entry<Integer, Integer> entry : PICKAXE_MAPPING.entrySet())
		{
			if (entry.getValue() == targetItemId)
			{
				currentVarbitValues.put(TOA_PICKAXE_VARBIT, entry.getKey());
				log.debug("Loaded ToA Pickaxe from file: {}", item.getItemName());
				break;
			}
		}
	}
}