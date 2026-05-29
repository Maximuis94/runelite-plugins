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

import com.datalogger.models.enums.VaultType;
import com.datalogger.models.itemvault.BankedItem;
import com.datalogger.services.itemvault.AbstractVaultParser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;

@Slf4j
@Singleton
public class POHCostumeRoomParser extends AbstractVaultParser
{
	@Inject
	private ItemManager itemManager;

	private boolean pendingSave = false;
	private final List<BankedItem> storedItems = new ArrayList<>();

	@Override
	public VaultType getVaultType()
	{
		return VaultType.POH_COSTUME_ROOM;
	}

	@Override
	protected void loadSessionData(File cacheFile)
	{
		itemVaultLogger.loadAllVaultsIntoMemory();

		List<BankedItem> loadedItems = itemVaultLogger.getVault(currentAccountHash, getVaultType());

		if (loadedItems != null)
		{
			storedItems.clear();
			storedItems.addAll(loadedItems);
		}
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired event)
	{
		if (!isEnabled) return;

		if (event.getScriptId() == 3538)
		{
			parseCostumeInterface();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (pendingSave && hasValidAccountHash)
		{
			submitVault(storedItems);
			pendingSave = false;
			log.debug("POH Costume Room batch sync saved to disk.");
		}
	}

	/**
	 * Parses the loaded costume furniture interface
	 */
	private void parseCostumeInterface()
	{
		int[] intStack = client.getIntStack();
		int stackSize = client.getIntStackSize();

		if (stackSize < 11) return;

		int itemId = intStack[6];
		boolean isStored = intStack[7] == 1;

		if (isStored)
		{
			updateStoredItem(itemId);
		}
		else
		{
			removeStoredItem(itemId);
		}
	}

	/**
	 * Removes the given itemId from the list of stored items
	 */
	private void removeStoredItem(int itemId)
	{
		boolean removed = storedItems.removeIf(item -> item.getItemId() == itemId);

		if (removed && hasValidAccountHash)
		{
			pendingSave = true;
		}
	}

	/**
	 * Adds the given itemId to the list of stored items.
	 */
	private void updateStoredItem(int itemId)
	{
		boolean isAlreadyTracked = storedItems.stream()
			.anyMatch(item -> item.getItemId() == itemId);

		if (!isAlreadyTracked)
		{
			String itemName = itemManager.getItemComposition(itemId).getName();

			storedItems.add(new BankedItem(getVaultType(), currentAccountHash, currentAccountName, itemId, itemName, 1));

			if (hasValidAccountHash)
			{
				pendingSave = true;
			}
		}
	}

	@Override
	public List<BankedItem> parseVault()
	{
		return storedItems;
	}

//	@Override
//	public List<BankedItem> parseOfflineFile(long accountHash, File vaultFile)
//	{
//		Type type = new TypeToken<List<CostumeItem>>(){}.getType();
//		List<BankedItem> offlineData = itemVaultLogger.getVault(currentAccountHash, getVaultType());
//
//		List<BankedItem> items = new ArrayList<>();
//		if (offlineData == null) return items;
//
//		String accountName = accountHashMapper.getAccountName(accountHash);
//		items.addAll(offlineData);
////		for (List<CostumeItem> entryItems : offlineData.values())
////		{
////			for (CostumeItem item : offlineData.values())
////			{
////				items.add(new BankedItem(getVaultType(), accountHash, accountName, item.getItemId(), item.getItemName(), 1L));
////			}
////		}
//		return items;
//	}
}