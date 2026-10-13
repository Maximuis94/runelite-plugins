package com.activitycounter.listeners.core;

import static com.activitycounter.PluginConstants.EQUIPPED_ITEM_CONTAINER_ID;
import static com.activitycounter.PluginConstants.INVENTORY_CONTAINER_ID;
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

@Slf4j
@Singleton
public class InventoryListener extends AbstractListenerNode
{
	@Inject
	private EquippedItemListener equippedItemListener;

	@Inject
	private MagicSpellListener magicSpellListener;

	private static final int N_RUNE_POUCH_SLOTS = 4;

	private final int[] runePouchItemIds = new int[N_RUNE_POUCH_SLOTS];
	private final int[] runePouchAmounts = new int[N_RUNE_POUCH_SLOTS];
	private final int[] runeProviderCounts = new int[RuneProvider.values().length];
	private final int[] runeElementTotals = new int[RuneElement.values().length];

	private boolean requiresRecalculation = false;
	private boolean isInitialized = false;

	public void initializeRoutingCaches()
	{
		if (isInitialized) return;

		Arrays.fill(runePouchItemIds, -1);
		Arrays.fill(runePouchAmounts, 0);
		Arrays.fill(runeProviderCounts, 0);
		Arrays.fill(runeElementTotals, 0);

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			recalculateRuneState(false);
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
		if (id == INVENTORY_CONTAINER_ID || id == EQUIPPED_ITEM_CONTAINER_ID)
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
			recalculateRuneState(isInitialized);
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

	private void recalculateRuneState(boolean trackDeltas)
	{
		int[] nextProviderCounts = new int[RuneProvider.values().length];
		Arrays.fill(runeElementTotals, 0);

		// 1. Tally Inventory Items
		ItemContainer inventory = client.getItemContainer(INVENTORY_CONTAINER_ID);
		if (inventory != null)
		{
			for (Item item : inventory.getItems())
			{
				RuneProvider rp = RuneProvider.getByItemId(item.getId());
				if (rp != null && !rp.isInfinite())
				{
					nextProviderCounts[rp.ordinal()] += item.getQuantity();
				}
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

		boolean hasDeltas = false;
		int[] consumedDeltas = new int[RuneProvider.values().length];

		if (trackDeltas)
		{
			for (RuneProvider rp : RuneProvider.values())
			{
				if (rp.isInfinite()) continue;

				int oldQty = runeProviderCounts[rp.ordinal()];
				int newQty = nextProviderCounts[rp.ordinal()];

				if (newQty < oldQty)
				{
					consumedDeltas[rp.ordinal()] = oldQty - newQty;
					hasDeltas = true;
				}
			}
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

		if (hasDeltas)
		{
			magicSpellListener.processRuneDeltas(consumedDeltas);
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