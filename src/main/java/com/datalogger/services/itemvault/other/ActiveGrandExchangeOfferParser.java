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

import static com.datalogger.constants.Item.Values.N_GE_SLOTS;
import com.datalogger.events.AccountSessionStarted;
import com.datalogger.loggers.ItemVaultLogger;
import com.datalogger.models.enums.VaultType;
import com.datalogger.models.itemvault.BankedItem;
import com.datalogger.models.itemvault.ItemBundle;
import com.datalogger.services.itemvault.AbstractVaultParser;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;

@Slf4j
@Singleton
public class ActiveGrandExchangeOfferParser extends AbstractVaultParser
{
	@Inject private ItemManager itemManager;
	@Inject private ItemVaultLogger itemVaultLogger;

	private final GrandExchangeOffer[] currentOffers = new GrandExchangeOffer[N_GE_SLOTS];
	private List<BankedItem> currentVaultItems = new ArrayList<>();

	private boolean needsLogging = false;

	@Override
	public VaultType getVaultType()
	{
		return VaultType.GRAND_EXCHANGE;
	}

	@Override
	protected void loadSessionData(File cacheFile)
	{
		List<BankedItem> loadedItems = fileIOService.readJson(cacheFile, ItemBundle.LIST_TYPE);
		if (loadedItems != null)
		{
			this.currentVaultItems = loadedItems;
		}
	}

	@Override
	public List<BankedItem> parseVault()
	{
		if (!isEnabled) return new ArrayList<>();
		return currentVaultItems;
	}

	@Subscribe
	public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged event)
	{
		if (!isEnabled) return;

		int slot = event.getSlot();
		currentOffers[slot] = event.getOffer();

		needsLogging = true;
	}

	@Override
	@Subscribe
	public void onAccountSessionStarted(AccountSessionStarted event)
	{
		super.onAccountSessionStarted(event);

		if (!isEnabled) return;
		if (event.getAccountHash() == -1) return;

		GrandExchangeOffer[] offers = client.getGrandExchangeOffers();
		if (offers != null)
		{
			log.debug("Loaded offers for {}", event.getAccountName());
			System.arraycopy(offers, 0, currentOffers, 0, Math.min(offers.length, currentOffers.length));
			ensureAccountNameIsCached();
			processOffers();
		}
		else
		{
			log.debug("Failed to load offers");
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!isEnabled || !needsLogging) return;

		needsLogging = false;
		ensureAccountNameIsCached();
		processOffers();
	}

	private void processOffers()
	{
		Map<Integer, Long> aggregatedItems = getAggregatedItemsFromOffers();

		List<BankedItem> newItems = new ArrayList<>();
		for (Map.Entry<Integer, Long> entry : aggregatedItems.entrySet())
		{
			int itemId = entry.getKey();
			long quantity = entry.getValue();
			String itemName = itemManager.getItemComposition(itemId).getName();

			newItems.add(new BankedItem(
				getVaultType(),
				currentAccountHash,
				currentAccountName,
				itemId,
				itemName,
				quantity
			));
		}

		this.currentVaultItems = newItems;

//		if (hasValidAccountHash)
//		{
//			fileIOService.writeJson(vaultFile, currentVaultItems);
//		}
		saveSlimVaultCache(currentVaultItems);

		log.debug("Parsed {} uncompleted items from Active GE Offers.", newItems.size());
		itemVaultLogger.logVault(currentAccountHash, currentAccountName, getVaultType(), newItems);
	}

	private Map<Integer, Long> getAggregatedItemsFromOffers()
	{
		Map<Integer, Long> aggregatedItems = new HashMap<>();

		for (GrandExchangeOffer offer : currentOffers)
		{
			if (offer == null) continue;

			GrandExchangeOfferState state = offer.getState();

			if (state == GrandExchangeOfferState.BUYING)
			{
				long totalCost = (long) offer.getTotalQuantity() * offer.getPrice();
				long unspentGp = totalCost - offer.getSpent();

				if (unspentGp > 0)
				{
					aggregatedItems.put(ItemID.COINS, aggregatedItems.getOrDefault(ItemID.COINS, 0L) + unspentGp);
				}
			}
			else if (state == GrandExchangeOfferState.SELLING)
			{
				long unsoldQuantity = (long) offer.getTotalQuantity() - offer.getQuantitySold();

				if (unsoldQuantity > 0)
				{
					int itemId = offer.getItemId();
					aggregatedItems.put(itemId, aggregatedItems.getOrDefault(itemId, 0L) + unsoldQuantity);
				}
			}
		}
		return aggregatedItems;
	}
}