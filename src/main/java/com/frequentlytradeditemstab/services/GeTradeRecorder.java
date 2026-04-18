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

package com.frequentlytradeditemstab.services;

import com.frequentlytradeditemstab.models.CachedGrandExchangeTrade;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class GeTradeRecorder {

	private final Client client;
	private final ItemManager itemManager;
	private final TradeCacheManager cacheManager;

	@Inject
	public GeTradeRecorder(Client client, ItemManager itemManager, TradeCacheManager cacheManager) {
		this.client = client;
		this.itemManager = itemManager;
		this.cacheManager = cacheManager;
	}

	@Subscribe
	public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged event) {
		GrandExchangeOffer offer = event.getOffer();
		GrandExchangeOfferState state = offer.getState();

		boolean isFinishedBuy = (state == GrandExchangeOfferState.BOUGHT);
		boolean isFinishedSell = (state == GrandExchangeOfferState.SOLD);
		boolean isPartialCancelledBuy = (state == GrandExchangeOfferState.CANCELLED_BUY && offer.getQuantitySold() > 0);
		boolean isPartialCancelledSell = (state == GrandExchangeOfferState.CANCELLED_SELL && offer.getQuantitySold() > 0);

		if (isFinishedBuy || isFinishedSell || isPartialCancelledBuy || isPartialCancelledSell) {
			ItemComposition itemComp = itemManager.getItemComposition(offer.getItemId());

			int quantityTraded = offer.getQuantitySold();
			int averagePrice = quantityTraded > 0 ? (offer.getSpent() / quantityTraded) : 0;

			boolean isBuy = isFinishedBuy || isPartialCancelledBuy;
			boolean isCancelled = isPartialCancelledBuy || isPartialCancelledSell;

			long accountHash = client.getAccountHash();
			String accountName = client.getLocalPlayer() != null ? client.getLocalPlayer().getName() : "Unknown";

			CachedGrandExchangeTrade newTrade = CachedGrandExchangeTrade.builder()
				.itemId(offer.getItemId())
				.itemName(itemComp.getName())
				.isBuy(isBuy)
				.quantity(quantityTraded)
				.price(averagePrice)
				.value(offer.getSpent())
				.accountHash(accountHash)
				.accountName(accountName)
				.geSlot(event.getSlot())
				.isHistoryEntry(false)
				.isCancelled(isCancelled)
				.parseTime(System.currentTimeMillis())
				.originalOfferQuantity(offer.getTotalQuantity())
				.originalOfferPrice(offer.getPrice())
				.build();

			if (accountHash != -1) {
				cacheManager.addTradeAndSaveAsync(newTrade);
			}
		}
	}
}