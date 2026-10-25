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

package com.activitycounter.listeners.conditional;

import com.activitycounter.ActivityCounterPlugin;
import static com.activitycounter.PluginConstants.CONFIG_KEY_ACTIVITY_GROUP_GRAND_EXCHANGE;
import com.activitycounter.listeners.AbstractListenerLeaf;
import com.activitycounter.listeners.core.PlayerListener;
import com.activitycounter.models.Category;
import com.activitycounter.models.TrackedActivity;
import com.google.gson.Gson;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;

/**
 * Class that counts activities related to the Grand Exchange. By definition, it is only enabled if the active game mode
 * allows full access to the Grand Exchange.
 * TODO: Add counters for N completed buy/sell offers
 * TODO: Add counters for money spent/received
 */
@Slf4j
@Singleton
public class GrandExchangeListener extends AbstractListenerLeaf
{
	private static final TrackedActivity[] MANAGED_ACTIVITIES = new TrackedActivity[]{
		TrackedActivity.GE_TAX_PAID,
	};

	@Override
	protected TrackedActivity[] getManagedActivities()
	{
		return MANAGED_ACTIVITIES;
	}

	@Override
	protected String getActivityGroupConfigKey()
	{
		return CONFIG_KEY_ACTIVITY_GROUP_GRAND_EXCHANGE;
	}

	@Data
	private static class GeOfferSignature
	{
		private int itemId;
		private int totalQuantity;
		private long price;
		private long taxPaid;
	}

	@Inject
	private PlayerListener playerListener;

	@Inject
	private ActivityCounterPlugin plugin;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Gson gson;

	private static final String CONFIG_GROUP = "activitycounter";
	private static final String TAX_KEY_PREFIX = "ge_offer_slot_";

	private static final int GE_TAX_VARP_START = 5754;
	private static final int N_GE_SLOTS = 8;

	private final long[] slotTaxCache = new long[N_GE_SLOTS];
	private boolean isInitialized = false;
	private boolean requiresSync = false;

	@Subscribe(priority = 100.f)
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			// Instant gatekeeper: Ironmen (including Hardcore/Ultimate/Group) cannot use the GE.
			// By returning here, isInitialized remains false, effectively disabling the listener logic.
			if (!playerListener.isAbleToFullyAccessGE() ) {
				log.debug("Ironman account detected. GE tracking disabled.");
				isInitialized = false;
				return;
			}

			// Flag for sync, but wait for GameTick to ensure GE offers have populated
			requiresSync = true;
		}
		else if (event.getGameState() == GameState.HOPPING || event.getGameState() == GameState.LOGIN_SCREEN)
		{
			isInitialized = false;
			requiresSync = false;
			Arrays.fill(slotTaxCache, 0);
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!isInitialized) return;
		if (requiresSync)
		{
			syncOfflineTax();
			requiresSync = false;
		}
	}

	private void syncOfflineTax()
	{
		if (isInitialized) return;

		GrandExchangeOffer[] offers = client.getGrandExchangeOffers();

		for (int i = 0; i < N_GE_SLOTS; i++)
		{
			int varpId = GE_TAX_VARP_START + i;
			long currentTax = getSafeVarpValue(varpId);
			GrandExchangeOffer currentOffer = offers[i];

			// Load the last known offer signature from the account's local profile file
			String json = configManager.getRSProfileConfiguration(CONFIG_GROUP, TAX_KEY_PREFIX + i);
			GeOfferSignature savedSignature = json != null ? gson.fromJson(json, GeOfferSignature.class) : null;

			if (currentOffer != null && currentOffer.getState() != GrandExchangeOfferState.EMPTY)
			{
				// Match Check: Is this the EXACT same offer we left behind?
				boolean isMatch = savedSignature != null &&
					savedSignature.getItemId() == currentOffer.getItemId() &&
					savedSignature.getTotalQuantity() == currentOffer.getTotalQuantity() &&
					savedSignature.getPrice() == currentOffer.getPrice();

				if (isMatch)
				{
					if (currentTax > savedSignature.getTaxPaid())
					{
						long offlineTaxPaid = currentTax - savedSignature.getTaxPaid();

						// Updated to use the central Facade plugin architecture
						plugin.increaseCountByAmount(TrackedActivity.GE_TAX_PAID.getId(), (int) offlineTaxPaid);
					}
				}

				saveOfferSignature(i, currentOffer, (int) currentTax);
			}

			// RAM cache is ALWAYS set to the current Varp value as the new baseline
			slotTaxCache[i] = currentTax;
		}

		isInitialized = true;
	}

	@Subscribe(priority = 100.f)
	public void onVarbitChanged(VarbitChanged event)
	{
		int varpId = event.getVarpId();
		int varbitId = event.getVarbitId();

		if (varbitId == VarbitID.IRONMAN)
		{
			isInitialized = playerListener.isAbleToFullyAccessGE();
		}

		if (varpId >= GE_TAX_VARP_START && varpId < GE_TAX_VARP_START + N_GE_SLOTS)
		{
			if (!isInitialized) return;

			int slotIndex = varpId - GE_TAX_VARP_START;
			long newTax = getSafeVarpValue(varpId);
			long oldTax = slotTaxCache[slotIndex];

			// Only process active tax deductions
			if (newTax > oldTax)
			{
				long taxPaidDelta = newTax - oldTax;

				// Updated to use the central Facade plugin architecture
				plugin.increaseCountByAmount(TrackedActivity.GE_TAX_PAID.getId(), (int) taxPaidDelta);
			}

			// Always sync RAM Cache
			slotTaxCache[slotIndex] = newTax;

			// Update the persistent account file so it's ready for the next logout
			GrandExchangeOffer[] offers = client.getGrandExchangeOffers();
			if (offers[slotIndex] != null && offers[slotIndex].getState() != GrandExchangeOfferState.EMPTY)
			{
				saveOfferSignature(slotIndex, offers[slotIndex], (int) newTax);
			}
		}
	}

	private void saveOfferSignature(int slotIndex, GrandExchangeOffer offer, int taxPaid)
	{
		GeOfferSignature sig = new GeOfferSignature();
		sig.setItemId(offer.getItemId());
		sig.setTotalQuantity(offer.getTotalQuantity());
		sig.setPrice(offer.getPrice());
		sig.setTaxPaid(taxPaid);

		configManager.setRSProfileConfiguration(CONFIG_GROUP, TAX_KEY_PREFIX + slotIndex, gson.toJson(sig));
	}

	/**
	 * Safely retrieves a varp value regardless of whether Jagex has defined it
	 * as a 32-bit integer or a 64-bit long in the current cache.
	 */
	private long getSafeVarpValue(int varpId)
	{
		try
		{
			return client.getVarpValue(varpId);
		}
		catch (IllegalArgumentException e)
		{
			return client.getVarpLongValue(varpId);
		}
	}
}