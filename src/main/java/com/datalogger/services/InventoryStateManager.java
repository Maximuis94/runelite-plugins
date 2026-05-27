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

package com.datalogger.services;

import com.datalogger.models.itemvault.BankedItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemVariationMapping;

/**
 * Service used to quickly lookup if the Player is carrying a specific item in the inventory/worn equipment
 */
@Singleton
public class InventoryStateManager
{
	@Inject
	private Client client;

	private static final int MAX_ITEMS = 50;

	private final int[] possessedBaseItems = new int[MAX_ITEMS];
	private final int[] possessedItems = new int[MAX_ITEMS];
	private final int[] itemQuantities = new int[MAX_ITEMS];

	private int itemCount = 0;

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		int containerId = event.getContainerId();

		if (containerId == InventoryID.WORN || containerId == InventoryID.INV)
		{
			rebuildPossessedItems();
		}
	}

	/**
	 * Reset the counter and refill the primitive arrays with items currently worn or in the inventory.
	 */
	private void rebuildPossessedItems()
	{
		itemCount = 0;

		extractItemIds(client.getItemContainer(InventoryID.WORN));
		extractItemIds(client.getItemContainer(InventoryID.INV));
//		log.debug("Rebuilt possessed items {} {}", possessedBaseItems, possessedItems);
	}

	/**
	 * Extract the itemIds from container and insert them directly into our primitive arrays.
	 */
	private void extractItemIds(ItemContainer container)
	{
		if (container != null)
		{
			for (Item item : container.getItems())
			{
				int itemId = item.getId();
				if (itemId != -1 && itemCount < MAX_ITEMS)
				{
					possessedItems[itemCount] = itemId;
					possessedBaseItems[itemCount] = ItemVariationMapping.map(itemId);
					itemQuantities[itemCount] = item.getQuantity();
					itemCount++;
				}
			}
		}
	}

	/**
	 * Return true if the given baseItemId is in any of the equipped item / inventory slots.
	 */
	public boolean hasBaseItem(int baseItemId)
	{
		for (int i = 0; i < itemCount; i++)
		{
			if (possessedBaseItems[i] == baseItemId)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Return true if the given itemId is in any of the equipped item / inventory slots.
	 */
	public boolean hasItem(int itemId)
	{
		for (int i = 0; i < itemCount; i++)
		{
			if (possessedItems[i] == itemId)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns an aggregated map of all currently carried item IDs and their total quantities.
	 */
	public Map<Integer, Long> getAggregatedCarriedItems()
	{
		Map<Integer, Long> aggregatedMap = new HashMap<>();
		for (int i = 0; i < itemCount; i++)
		{
			int itemId = possessedItems[i];
			long quantity = itemQuantities[i];

			aggregatedMap.merge(itemId, quantity, Long::sum);
		}
		return aggregatedMap;
	}


}