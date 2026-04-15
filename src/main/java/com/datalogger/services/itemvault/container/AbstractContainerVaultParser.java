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

package com.datalogger.services.itemvault.container;

import com.datalogger.loggers.ItemVaultLogger;
import com.datalogger.models.itemvault.BankedItem;
import com.datalogger.services.itemvault.AbstractVaultParser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

@Slf4j
public abstract class AbstractContainerVaultParser extends AbstractVaultParser
{
	@Inject protected ItemManager itemManager;
	@Inject protected ItemVaultLogger itemVaultLogger;

	private boolean needsLogging = false;
	protected List<BankedItem> currentContainerItems = new ArrayList<>();
	protected abstract int getContainerId();

	@Override
	public List<BankedItem> parseVault()
	{
		if (!isEnabled)
		{
			return new ArrayList<>();
		}
		return currentContainerItems;
	}

	@Override
	protected void loadSessionData(File cacheFile)
	{
		Type type = new TypeToken<List<BankedItem>>(){}.getType();
		List<BankedItem> loadedItems = fileIOService.readJson(cacheFile, type);

		if (loadedItems != null)
		{
			this.currentContainerItems = loadedItems;
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (!isEnabled) return;

		if (event.getContainerId() == getContainerId())
		{
			needsLogging = true;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!isEnabled || !needsLogging) return;
		needsLogging = false;
		ensureAccountNameIsCached();
		processVault();
	}

	private void processVault()
	{
		ItemContainer container = client.getItemContainer(getContainerId());
		if (container == null) return;

		List<BankedItem> parsedItems = new ArrayList<>();

		for (Item item : container.getItems())
		{
			if (item.getId() <= 0 || item.getQuantity() <= 0) continue;

			String itemName = itemManager.getItemComposition(item.getId()).getName();

			parsedItems.add(new BankedItem(
				getVaultType(),
				currentAccountHash,
				currentAccountName,
				item.getId(),
				itemName,
				item.getQuantity()
			));
		}
		this.currentContainerItems = parsedItems;

		if (hasValidAccountHash)
		{
			fileIOService.writeJson(vaultFile, currentContainerItems);
		}

		log.debug("Parsed {} items for {}", parsedItems.size(), getVaultType().name());
		itemVaultLogger.logVault(currentAccountHash, currentAccountName, getVaultType(), parsedItems);
	}
}