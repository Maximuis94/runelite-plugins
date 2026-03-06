/*
 * Copyright (c) 2026, maximuis94 <https://github.com/maximuis94>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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
package com.datalogger.loggers;

import com.datalogger.DataLoggerConfig;
import com.datalogger.framework.AbstractLogger;
import com.datalogger.framework.LogType;
import com.datalogger.models.GrandExchangeOfferData;
import com.datalogger.services.FileIOService;
import java.io.File;
import java.time.Instant;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
public class GrandExchangeLogger extends AbstractLogger
{
	@Inject private DataLoggerConfig config;
	@Inject private Client client;
	@Inject private FileIOService utils;

	private static final String CSV_HEADER = "item_id,timestamp,timestamp_created,is_buy,quantity,offer_quantity,price,offer_price,value,account,ge_slot";

	@Override
	public LogType getLogType() { return LogType.GRAND_EXCHANGE; }

	@Override
	public String getCsvHeader() {return "item_id,timestamp,timestamp_created,is_buy,quantity,offer_quantity,price,offer_price,value,account,ge_slot";}

	@Override
	public boolean isEnabled() { return config.logGrandExchange(); }

	@Override
	public void setup()
	{
		initialScan();
	}

	@Subscribe
	public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged event) {
		handleOffer(event.getSlot(), event.getOffer());
	}

	private GrandExchangeOfferData assembleData(int slot, GrandExchangeOffer offer, String createdTs) {
		int quantity = offer.getQuantitySold();
		long averagePrice = (quantity > 0) ? (offer.getSpent() / quantity) : offer.getPrice();

		return new GrandExchangeOfferData(
			offer.getItemId(),
			Instant.now().toString(),
			createdTs,
			isBuy(offer.getState()),
			quantity,
			offer.getTotalQuantity(),
			averagePrice,
			offer.getPrice(),
			offer.getSpent(),
			getAccountName(),
			slot
		);
	}

	/**
	 * Process the given offer with the given slot index info
	 * @param slot The Grand Exchange slot index of the trade
	 * @param offer The offer that is to be processed
	 */
	public void handleOffer(int slot, GrandExchangeOffer offer) {
		if (!isEnabled()) {
			return;
		}

		if (offer.getState() == GrandExchangeOfferState.EMPTY) {
			clearSlotMemory(slot);
			return;
		}

		String accountHash = getAccountHashString();
		Properties state = utils.getAccountState(accountHash);

		String createdTs = getOrSetCreatedTimestamp(slot, offer, state, accountHash);
		String fingerprint = generateFingerprint(offer);

		String lastLoggedFp = state.getProperty("last_fp_" + slot);

		if (isFinalState(offer.getState()) && offer.getSpent() > 0) {
			if (!fingerprint.equals(lastLoggedFp)) {
				logFinalTrade(slot, offer, createdTs);

				// Update local file memory
				state.setProperty("last_fp_" + slot, fingerprint);
				utils.saveAccountState(accountHash, state);
				log.debug("Logged GE trade for slot {}. FP: {}", slot, fingerprint);
			}
		}
	}

	/**
	 * Scan the offers active on the account that is logged in. Compare the data with previously logged completed offer
	 * data and store fingerprints of unlogged completed offers.
	 */
	public void initialScan() {
		GrandExchangeOffer[] offers = client.getGrandExchangeOffers();
		if (offers == null) return;

		String accountHash = getAccountHashString();
		Properties state = utils.getAccountState(accountHash);
		boolean changed = false;

		for (int i = 0; i < offers.length; i++) {
			GrandExchangeOffer offer = offers[i];
			if (offer == null || offer.getState() == GrandExchangeOfferState.EMPTY) continue;

			getOrSetCreatedTimestamp(i, offer, state, accountHash);

			if (isFinalState(offer.getState())) {
				state.setProperty("last_fp_" + i, generateFingerprint(offer));
				changed = true;
			}
		}

		if (changed) {
			utils.saveAccountState(accountHash, state);
		}
	}

	/**
	 * Format given data into a CSV row that will be appended to the CSV log for the acocunt.
	 * @param slot The Grand Exchange slot index of the trade
	 * @param offer The offer that is to be logged
	 * @param createdTs The timestamp on which the offer was created
	 * @param account The account on which the offer was submitted
	 * @return A String that contains a formatted row for the CSV file
	 */
	private String formatCsvRow(int slot, GrandExchangeOffer offer, String createdTs, String account) {
		int value = offer.getSpent();
		int quantity = offer.getQuantitySold();
		int averagePrice = (quantity > 0) ? (value / quantity) : offer.getPrice();

		return String.format("%d,%s,%s,%b,%d,%d,%d,%d,%d,%s,%d",
			offer.getItemId(),
			Instant.now().toString(),
			createdTs,
			isBuy(offer.getState()),
			quantity,
			offer.getTotalQuantity(),
			averagePrice,
			offer.getPrice(),
			offer.getSpent(),
			account,
			slot
		);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event) {
		// Using the modern InterfaceID check
		if (event.getGroupId() == InterfaceID.GE_HISTORY) {

			// Give the client thread a tick to populate the text fields
			clientThread.invokeLater(this::processGEHistory);
		}
	}

	/**
	 * Iterate over GE history entries and attempt to match them to previously logged transactions.
	 */
	private void processGEHistory()
	{

	}

	/**
	 * Format the data into a CSV file row and append it to the appropriate file
	 * @param slot The Grand Exchange slot index of the trade
	 * @param offer The offer that is to be logged
	 * @param createdTs The timestamp on which the offer was created
	 */
	private void logFinalTrade(int slot, GrandExchangeOffer offer, String createdTs) {
		String currentAccount = getAccountName();
		File logFile = utils.getTargetFile(getLogType(), currentAccount);
		String row = formatCsvRow(slot, offer, createdTs, currentAccount);

		utils.atomicWrite(logFile, CSV_HEADER, row);
	}

	/**
	 * Fetch the created timestamp that is relevant given the args that are provided. Register the timestamp if need be.
	 * @param slot The Grand Exchange slot index of the trade
	 * @param offer The offer of which the created timestamp is to be fetched
	 * @param state The state of the offer
	 * @param hash The hash of the account that placed the offer
	 * @return The created timestamp of the offer.
	 */
	private String getOrSetCreatedTimestamp(int slot, GrandExchangeOffer offer, Properties state, String hash) {
		String tsKey = "slot_created_" + slot;
		String itemKey = "slot_item_" + slot;

		String existing = state.getProperty(tsKey);
		String lastItemId = state.getProperty(itemKey);

		// If no timestamp exists or the item in the slot changed, reset
		if (existing == null || lastItemId == null || Integer.parseInt(lastItemId) != offer.getItemId()) {
			existing = Instant.now().toString();
			state.setProperty(tsKey, existing);
			state.setProperty(itemKey, String.valueOf(offer.getItemId()));
			utils.saveAccountState(hash, state);
		}
		return existing;
	}

	/**
	 * Reset the data logged for a particular slot of the hashed account string and save the properties file.
	 * @param slot The index of the grand exchange slot that is to be reset
	 */
	private void clearSlotMemory(int slot) {
		String hash = getAccountHashString();
		Properties state = utils.getAccountState(hash);

		if (state.containsKey("slot_created_" + slot)) {
			state.remove("slot_created_" + slot);
			state.remove("slot_item_" + slot);
			// We don't remove last_fp so that if the same trade re-appears (lag), we don't double log
			utils.saveAccountState(hash, state);
		}
	}

	/**
	 * Generate a unique fingerprint of a completed trade that will be used to prevent duplicate log submissions.
	 * @param offer The offer to extract the data from
	 * @return A specifically formatted String that captures offer data to store
	 */
	private String generateFingerprint(GrandExchangeOffer offer) {
		return offer.getItemId() + "_" + offer.getTotalQuantity() + "_" + offer.getSpent() + "_" + offer.getState();
	}

	/**
	 * Return true if state refers to a completed or cancelled offer
	 */
	private boolean isFinalState(GrandExchangeOfferState state) {
		return state == GrandExchangeOfferState.BOUGHT ||
			state == GrandExchangeOfferState.SOLD ||
			state == GrandExchangeOfferState.CANCELLED_BUY ||
			state == GrandExchangeOfferState.CANCELLED_SELL;
	}

	/**
	 * Return true if state refers to a buy offer
	 */
	private boolean isBuy(GrandExchangeOfferState state) {
		return state == GrandExchangeOfferState.BUYING ||
			state == GrandExchangeOfferState.BOUGHT ||
			state == GrandExchangeOfferState.CANCELLED_BUY;
	}
}