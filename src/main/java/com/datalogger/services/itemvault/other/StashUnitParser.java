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

package com.datalogger.services.itemvault.other;

import com.datalogger.models.enums.StashUnitData;
import com.datalogger.models.enums.VaultType;
import com.datalogger.models.itemvault.BankedItem;
import com.datalogger.services.itemvault.AbstractVaultParser;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ScriptID;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.cluescrolls.clues.emote.STASHUnit;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class StashUnitParser extends AbstractVaultParser
{
	@Inject
	private ItemManager itemManager;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StashItem
	{
		private int itemId;
		private String itemName;
		private int quantity;
	}

	private final Map<Integer, List<StashItem>> stashStates = new HashMap<>();

	private List<Integer> previousInventory = new ArrayList<>();
	private List<Integer> recentlyRemovedItems = new ArrayList<>();

	private int lastCheckedStashObjectId = -1;
	private boolean watsonSyncPending = false;

	@Override
	public VaultType getVaultType()
	{
		return VaultType.STASH_UNITS;
	}

	@Override
	protected void loadSessionData(File cacheFile)
	{
		Type type = new TypeToken<Map<Integer, List<StashItem>>>(){}.getType();
		Map<Integer, List<StashItem>> loadedStates = fileIOService.readJson(cacheFile, type);

		if (loadedStates != null)
		{
			stashStates.clear();
			stashStates.putAll(loadedStates);
		}
	}

	@Override
	public void setupAccountHash()
	{
		super.setupAccountHash();
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (!isEnabled || event.getContainerId() != InventoryID.INV) return;

		List<Integer> currentInventory = new ArrayList<>();
		for (Item item : event.getItemContainer().getItems())
		{
			if (item.getId() != -1) currentInventory.add(item.getId());
		}

		List<Integer> removed = new ArrayList<>(previousInventory);
		for (Integer id : currentInventory)
		{
			removed.remove(id);
		}

		if (!removed.isEmpty())
		{
			recentlyRemovedItems = removed;
		}

		previousInventory = currentInventory;
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!isEnabled) return;
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM) return;

		String message = Text.removeTags(event.getMessage());

		if (message.equals("You deposit your items into the STASH unit."))
		{
			updateClosestStashUnit(true);
		}
		else if (message.equals("You withdraw your items from the STASH unit."))
		{
			updateClosestStashUnit(false);
		}
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired event)
	{
		if (!isEnabled) return;

		if (event.getScriptId() == ScriptID.WATSON_STASH_UNIT_CHECK)
		{
			int[] intStack = client.getIntStack();
			int stackSize = client.getIntStackSize();

			if (stackSize > 0)
			{
				for (int i = stackSize - 1; i >= Math.max(0, stackSize - 6); i--)
				{
					if (StashUnitData.getByObjectId(intStack[i]) != null)
					{
						lastCheckedStashObjectId = intStack[i];
						break;
					}
				}
			}
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (!isEnabled) return;

		if (event.getScriptId() == ScriptID.WATSON_STASH_UNIT_CHECK)
		{
			parseWatsonNoticeBoard();
		}
	}

	/**
	 * Extract STASH unit data provided when reading the notice board at Watson. Only add item fixed item sets if the
	 * STASH unit is filled. If a STASH unit is empty, update the cached data if need be.
	 */
	private void parseWatsonNoticeBoard()
	{
		if (lastCheckedStashObjectId == -1) return;

		StashUnitData expectedData = StashUnitData.getByObjectId(lastCheckedStashObjectId);

		if (expectedData == null)
		{
			lastCheckedStashObjectId = -1;
			return;
		}

		int[] intStack = client.getIntStack();
		int stackSize = client.getIntStackSize();

		if (stackSize >= 1)
		{
			boolean isFilled = intStack[stackSize - 1] == 1;

			if (isFilled)
			{
				if (!expectedData.isFlexibleItemSet())
				{
					List<StashItem> itemsToSave = new ArrayList<>();
					for (int itemId : expectedData.getFallbackItemIds())
					{
						String name = itemManager.getItemComposition(itemId).getName();
						itemsToSave.add(new StashItem(itemId, name, 1));
					}

					stashStates.put(lastCheckedStashObjectId, itemsToSave);
					watsonSyncPending = true;
				}
			}
			else
			{
				if (stashStates.containsKey(lastCheckedStashObjectId))
				{
					stashStates.remove(lastCheckedStashObjectId);
					watsonSyncPending = true;
				}
			}
		}
		lastCheckedStashObjectId = -1;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (watsonSyncPending && hasValidAccountHash)
		{
			fileIOService.writeJson(vaultFile, stashStates);
			log.debug("Watson Noticeboard bulk sync saved to disk.");
			watsonSyncPending = false;
		}
	}

	private boolean isValidStashItem(int itemId)
	{
		if (itemManager.getItemStats(itemId) == null)
		{
			return itemId == ItemID.LOCKPICK
				|| itemId == ItemID.BULLROARER
				|| itemId == ItemID.TROLLROMANCE_TOBOGGON_WAXED;
		}
		return true;
	}

	private void updateClosestStashUnit(boolean isDepositing)
	{
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null) return;

		WorldPoint playerLocation = localPlayer.getWorldLocation();
		int closestObjectId = -1;
		int minDistance = Integer.MAX_VALUE;

		for (STASHUnit stashUnit : STASHUnit.values())
		{
			for (WorldPoint wp : stashUnit.getWorldPoints())
			{
				if (wp.getPlane() != playerLocation.getPlane()) continue;

				int distance = playerLocation.distanceTo(wp);
				if (distance < minDistance)
				{
					minDistance = distance;
					closestObjectId = stashUnit.getObjectId();
				}
			}
		}

		if (closestObjectId != -1 && minDistance <= 5)
		{
			if (isDepositing)
			{
				StashUnitData expectedData = StashUnitData.getByObjectId(closestObjectId);

				if (expectedData == null)
				{
					log.warn("Interacted with unmapped STASH unit: {}", closestObjectId);
					return;
				}

				List<StashItem> itemsToSave = new ArrayList<>();

				if (expectedData.isFlexibleItemSet())
				{
					List<Integer> validStashItems = new ArrayList<>();
					if (recentlyRemovedItems != null)
					{
						for (Integer itemId : recentlyRemovedItems)
						{
							if (isValidStashItem(itemId))
							{
								validStashItems.add(itemId);
							}
						}
					}

					int expectedItemCount = expectedData.getFallbackItemIds().length;

					if (expectedItemCount > 0 && validStashItems.size() == expectedItemCount)
					{
						for (int itemId : validStashItems)
						{
							String name = itemManager.getItemComposition(itemId).getName();
							itemsToSave.add(new StashItem(itemId, name, 1));
						}
						log.debug("Saved exact items to flexible STASH {}.", closestObjectId);
					}
					else
					{
						for (int itemId : expectedData.getFallbackItemIds())
						{
							String name = itemManager.getItemComposition(itemId).getName();
							itemsToSave.add(new StashItem(itemId, name, 1));
						}
						log.warn("STASH Desync at {}. Falling back to default items.", closestObjectId);
					}
				}
				else
				{
					for (int itemId : expectedData.getFallbackItemIds())
					{
						String name = itemManager.getItemComposition(itemId).getName();
						itemsToSave.add(new StashItem(itemId, name, 1));
					}
					log.debug("Saved strict STASH {} using explicit items.", closestObjectId);
				}

				stashStates.put(closestObjectId, itemsToSave);
			}
			else
			{
				stashStates.remove(closestObjectId);
			}

			if (hasValidAccountHash)
			{
				fileIOService.writeJson(vaultFile, stashStates);
			}
		}
	}

	@Override
	public List<BankedItem> parseVault()
	{
		List<BankedItem> items = new ArrayList<>();
		for (Map.Entry<Integer, List<StashItem>> entry : stashStates.entrySet())
		{
			for (StashItem stashItem : entry.getValue())
			{
				items.add(new BankedItem(
					getVaultType(),
					currentAccountHash,
					currentAccountName,
					stashItem.getItemId(),
					stashItem.getItemName(),
					stashItem.getQuantity()
				));
			}
		}

		return items;
	}

	@Override
	public List<BankedItem> parseOfflineFile(long accountHash, File vaultFile)
	{
		Type type = new TypeToken<Map<Integer, List<StashItem>>>(){}.getType();
		Map<Integer, List<StashItem>> offlineStates = fileIOService.readJson(vaultFile, type);

		if (offlineStates == null || offlineStates.isEmpty())
		{
			return new ArrayList<>();
		}

		List<BankedItem> items = new ArrayList<>();

		String accountName = accountHashMapper.getAccountName(accountHash);

		for (Map.Entry<Integer, List<StashItem>> entry : offlineStates.entrySet())
		{
			List<StashItem> savedItems = entry.getValue();

			if (savedItems != null && !savedItems.isEmpty())
			{
				for (StashItem stashItem : savedItems)
				{
					items.add(new BankedItem(
						getVaultType(),
						accountHash,
						accountName,
						stashItem.getItemId(),
						stashItem.getItemName(),
						stashItem.getQuantity()
					));
				}
			}
		}

		return items;
	}
}