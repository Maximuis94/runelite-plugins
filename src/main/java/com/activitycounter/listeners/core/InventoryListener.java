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

package com.activitycounter.listeners.core;

import static com.activitycounter.PluginConstants.EQUIPPED_ID;
import static com.activitycounter.PluginConstants.INVENTORY_ID;
import static com.activitycounter.PluginConstants.MAX_ITEM_ID;
import com.activitycounter.listeners.AbstractListenerNode;
import com.activitycounter.listeners.conditional.MagicSpellListener;
import com.activitycounter.models.RuneElement;
import com.activitycounter.models.RuneProvider;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

/**
 * Class that listens to the inventory. Inventory updates are actively tracked on a per-tick basis. If a change is
 * registered, a flag is set and the Inventory is updated the next tick.
 */
@Slf4j
@Singleton
public class InventoryListener extends AbstractListenerNode
{
	@Inject
	private EquippedItemListener equippedItemListener;

	@Inject
	private MagicSpellListener magicSpellListener;

	private static final int N_RUNE_POUCH_SLOTS = 4;
	private static final int N_INVENTORY_SLOTS = 28;

	// Array of itemId per inventory slot
	private final int[] inventoryItemIds = new int[N_INVENTORY_SLOTS];

	// Universal Item Counters
	private final int[] inventoryItemCounts = new int[MAX_ITEM_ID];
	private final int[] previousItemCounts = new int[MAX_ITEM_ID];
	private final int[] deltaItemCounts = new int[MAX_ITEM_ID];

	private final int[] runePouchItemIds = new int[N_RUNE_POUCH_SLOTS];
	private final int[] runePouchAmounts = new int[N_RUNE_POUCH_SLOTS];
	private final int[] runeProviderCounts = new int[RuneProvider.values().length];
	private final int[] runeElementTotals = new int[RuneElement.values().length];

	private boolean requiresRecalculation = false;
	private boolean isInitialized = false;

	public void initializeRoutingCaches()
	{
		if (isInitialized) return;

		Arrays.fill(inventoryItemIds, -1);
		Arrays.fill(inventoryItemCounts, 0);
		Arrays.fill(previousItemCounts, 0);
		Arrays.fill(deltaItemCounts, 0);

		Arrays.fill(runePouchItemIds, -1);
		Arrays.fill(runePouchAmounts, 0);
		Arrays.fill(runeProviderCounts, 0);
		Arrays.fill(runeElementTotals, 0);

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			countInventoryItems(false);
			isInitialized = true;
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			isInitialized = false;
			requiresRecalculation = false;
			Arrays.fill(runeProviderCounts, 0);
			Arrays.fill(runeElementTotals, 0);
		}
		else
		{
			initializeRoutingCaches();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		int id = event.getContainerId();
		if (id == INVENTORY_ID || id == EQUIPPED_ID)
		{
			requiresRecalculation = true;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int id = event.getVarbitId();
		int val = event.getValue();

		switch (id)
		{
			case VarbitID.RUNE_POUCH_QUANTITY_1: runePouchAmounts[0] = val; requiresRecalculation = true; break;
			case VarbitID.RUNE_POUCH_QUANTITY_2: runePouchAmounts[1] = val; requiresRecalculation = true; break;
			case VarbitID.RUNE_POUCH_QUANTITY_3: runePouchAmounts[2] = val; requiresRecalculation = true; break;
			case VarbitID.RUNE_POUCH_QUANTITY_4: runePouchAmounts[3] = val; requiresRecalculation = true; break;
			case VarbitID.RUNE_POUCH_TYPE_1: updateRunePouchType(0, val); requiresRecalculation = true; break;
			case VarbitID.RUNE_POUCH_TYPE_2: updateRunePouchType(1, val); requiresRecalculation = true; break;
			case VarbitID.RUNE_POUCH_TYPE_3: updateRunePouchType(2, val); requiresRecalculation = true; break;
			case VarbitID.RUNE_POUCH_TYPE_4: updateRunePouchType(3, val); requiresRecalculation = true; break;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (requiresRecalculation)
		{
			countInventoryItems(isInitialized);
			requiresRecalculation = false;
			isInitialized = true;
		}
	}

	private void updateRunePouchType(int slot, int pouchId)
	{
		if (pouchId <= 0)
		{
			runePouchItemIds[slot] = -1;
		}
		else
		{
			RuneProvider provider = RuneProvider.getByPouchId(pouchId);
			runePouchItemIds[slot] = (provider != null) ? provider.getItemId() : -1;
		}
	}

	private void countInventoryItems(boolean trackDeltas)
	{
		// Save old quantities for all universal items
		System.arraycopy(inventoryItemCounts, 0, previousItemCounts, 0, MAX_ITEM_ID);
		Arrays.fill(inventoryItemCounts, 0);
		Arrays.fill(inventoryItemIds, -1);

		int[] nextProviderCounts = new int[RuneProvider.values().length];
		Arrays.fill(runeElementTotals, 0);

		// 1. Universal Inventory Tracking
		ItemContainer inventory = client.getItemContainer(INVENTORY_ID);
		if (inventory != null)
		{
			int slot = 0;
			for (Item item : inventory.getItems())
			{
				int id = item.getId();
				if (id >= 0 && id < MAX_ITEM_ID)
				{
					inventoryItemIds[slot] = id;
					inventoryItemCounts[id] += item.getQuantity();

					RuneProvider rp = RuneProvider.getByItemId(id);
					if (rp != null && !rp.isInfinite())
					{
						nextProviderCounts[rp.ordinal()] += item.getQuantity();
					}
				}
				slot++;
			}
		}

		// 2. Tally Rune Pouch Items
		for (int i = 0; i < N_RUNE_POUCH_SLOTS; i++)
		{
			int itemId = runePouchItemIds[i];
			if (itemId != -1)
			{
				RuneProvider rp = RuneProvider.getByItemId(itemId);
				if (rp != null && !rp.isInfinite())
				{
					nextProviderCounts[rp.ordinal()] += runePouchAmounts[i];
				}
			}
		}

		// 3. Process Deltas
		boolean hasRuneDeltas = false;
		int[] consumedRuneDeltas = new int[RuneProvider.values().length];

		if (trackDeltas)
		{
			// Calculate universal item deltas
			for (int i = 0; i < MAX_ITEM_ID; i++)
			{
				deltaItemCounts[i] = inventoryItemCounts[i] - previousItemCounts[i];
			}

			// Calculate rune deltas
			for (RuneProvider rp : RuneProvider.values())
			{
				if (rp.isInfinite()) continue;

				int oldQty = runeProviderCounts[rp.ordinal()];
				int newQty = nextProviderCounts[rp.ordinal()];

				if (newQty < oldQty)
				{
					consumedRuneDeltas[rp.ordinal()] = oldQty - newQty;
					hasRuneDeltas = true;
				}
			}
		}
		else
		{
			Arrays.fill(deltaItemCounts, 0);
		}

		System.arraycopy(nextProviderCounts, 0, runeProviderCounts, 0, nextProviderCounts.length);

		applyInfiniteProvider(RuneProvider.getByItemId(equippedItemListener.getWeaponId()));
		applyInfiniteProvider(RuneProvider.getByItemId(equippedItemListener.getOffhandId()));

		for (RuneProvider rp : RuneProvider.values())
		{
			if (rp.isInfinite()) continue;

			int physicalAmount = runeProviderCounts[rp.ordinal()];
			if (physicalAmount > 0)
			{
				for (RuneElement element : rp.getProvidedElements())
				{
					if (runeElementTotals[element.ordinal()] != -1)
					{
						runeElementTotals[element.ordinal()] += physicalAmount;
					}
				}
			}
		}

		if (hasRuneDeltas)
		{
			magicSpellListener.processRuneDeltas(consumedRuneDeltas);
		}
	}

	private void applyInfiniteProvider(RuneProvider rp)
	{
		if (rp != null && rp.isInfinite())
		{
			for (RuneElement element : rp.getProvidedElements())
			{
				runeElementTotals[element.ordinal()] = -1;
			}
		}
	}

	/**
	 * Returns the difference in item quantity from the previous tick.
	 * Negative means the item was consumed or deposited.
	 */
	public int getItemDelta(int itemId)
	{
		if (itemId < 0 || itemId >= MAX_ITEM_ID) return 0;
		return deltaItemCounts[itemId];
	}

	public int getItemCount(int itemId)
	{
		if (itemId < 0 || itemId >= MAX_ITEM_ID) return 0;
		return inventoryItemCounts[itemId];
	}

	// (Existing Rune Getters...)
	public int getProviderCount(RuneProvider provider)
	{
		if (provider == null) return 0;
		return runeProviderCounts[provider.ordinal()];
	}

	public int getElementTotal(RuneElement element)
	{
		if (element == null) return 0;
		return runeElementTotals[element.ordinal()];
	}
}