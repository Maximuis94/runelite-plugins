package com.datalogger.services.itemvault.other;

import com.datalogger.models.enums.VaultType;
import com.datalogger.models.itemvault.BankedItem;
import com.datalogger.services.InventoryStateManager;
import com.datalogger.services.itemvault.AbstractVaultParser;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;

@Slf4j
@Singleton
public class CarriedItemsParser extends AbstractVaultParser
{
	@Inject
	private ItemManager itemManager;

	@Inject
	private InventoryStateManager inventoryStateManager;

	// Flag to delay the initial login parse by one tick to ensure containers are fully loaded
	private boolean needsLoginUpdate = false;
	private final List<BankedItem> carriedItems = new ArrayList<>();
	private final Map<Integer, Long> combinedItems = new HashMap<>();
	private boolean hasUpdated = false;
	private int nextUpdateTick = 0;
	private final int UPDATE_DELAY_TICKS = 15;


	@Override
	public VaultType getVaultType()
	{
		return VaultType.CARRIED_ITEMS;
	}

	@Override
	public List<BankedItem> parseVault()
	{
		return carriedItems;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (!isEnabled) return;

		if (event.getGameState() == GameState.LOGGED_IN)
		{
			needsLoginUpdate = true;
		}
		else if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING)
		{
			updateAndProcessCarriedItems();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!isEnabled || !hasUpdated) return;
		if (client.getTickCount() < nextUpdateTick) return;
		hasUpdated = false;
		nextUpdateTick = 0;

		updateAndProcessCarriedItems();
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!isEnabled) return;

		String option = event.getMenuOption();

		if (hasUpdated && ("Logout".equals(option) || "Switch character".equals(option)))
		{
			log.debug("Logout action detected. Saving carried items state.");
			updateAndProcessCarriedItems();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		int containerId = event.getContainerId();
		if (!hasUpdated && (containerId == InventoryID.WORN || containerId == InventoryID.INV))
		{
			hasUpdated = true;
			nextUpdateTick = client.getTickCount() + UPDATE_DELAY_TICKS;
		}
	}

//	@Subscribe
//	public void onWidgetClosed(WidgetClosed event)
//	{
//		if (!isEnabled || !hasUpdated) return;
//
//		log.debug("Updating carried items = {}.", event.getGroupId());
//		// WidgetID.LOGOUT_PANEL_ID is the group ID for the logout screen
	////		if (event.getGroupId() == 161)
	////		{
	////			log.debug("Logout panel closed. Saving carried items state.");
	////			processCarriedItems();
	////		}
//	}

	private void updateAndProcessCarriedItems()
	{
		Map<Integer, Long> localCombinedItems = inventoryStateManager.getAggregatedCarriedItems();
		log.debug("Fetched combined items from state manager. Combined size is {}.", localCombinedItems.size());
		processCarriedItems(localCombinedItems);
	}

	private void processCarriedItems(Map<Integer, Long> combinedItemsMap)
	{
		log.debug("Processing carried items...");
		List<BankedItem> freshCarriedItems = new ArrayList<>();

		for (Map.Entry<Integer, Long> entry : combinedItemsMap.entrySet())
		{
			int itemId = entry.getKey();
			long quantity = entry.getValue();

			String itemName = itemManager.getItemComposition(itemId).getName();

			freshCarriedItems.add(new BankedItem(
				getVaultType(),
				currentAccountHash,
				currentAccountName,
				itemId,
				itemName,
				quantity
			));
		}

		submitVault(freshCarriedItems);
		hasUpdated = false;
		nextUpdateTick = 0;
	}

	/**
	 * Updated helper method to accept the local map
	 */
	private void aggregateContainer(ItemContainer container, Map<Integer, Long> targetMap)
	{
		for (Item item : container.getItems())
		{
			if (item.getId() > 0 && item.getQuantity() > 0)
			{
				targetMap.merge(item.getId(), (long) item.getQuantity(), Long::sum);
			}
		}
	}

	@Override
	protected void loadSessionData(File cacheFile)
	{
		itemVaultLogger.loadAllVaultsIntoMemory();

		List<BankedItem> loadedItems = itemVaultLogger.getVault(currentAccountHash, getVaultType());

		if (loadedItems != null)
		{
			carriedItems.clear();
			carriedItems.addAll(loadedItems);
		}
	}
}