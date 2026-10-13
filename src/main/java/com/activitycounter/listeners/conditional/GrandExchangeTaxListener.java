package com.activitycounter.listeners.conditional;

import com.activitycounter.ActivityCounterPlugin;
import com.activitycounter.listeners.AbstractListenerLeaf;
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
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
public class GrandExchangeTaxListener extends AbstractListenerLeaf
{
	@Override
	public boolean isEnabled()
	{
		return config.enableGeTaxListener();
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

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
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
						if (plugin.getCurrentSession() != null)
						{
							plugin.getCurrentSession().processActivityUpdate(TrackedActivity.GE_TAX_PAID, (int) offlineTaxPaid);
							plugin.setRequiresSaveAndRefresh(true);
						}
					}
				}

				saveOfferSignature(i, currentOffer, (int) currentTax);
			}

			// RAM cache is ALWAYS set to the current Varp value as the new baseline
			slotTaxCache[i] = currentTax;
		}

		isInitialized = true;
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int varpId = event.getVarpId();

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

				if (plugin.getCurrentSession() != null)
				{
					plugin.getCurrentSession().processActivityUpdate(TrackedActivity.GE_TAX_PAID, (int) taxPaidDelta);
					plugin.setRequiresSaveAndRefresh(true);
				}
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