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
import static com.datalogger.constants.Item.InterfaceID.GE_GROUP_ID;
import static com.datalogger.constants.Item.InterfaceID.GE_HISTORY_CHILD_ID;
import static com.datalogger.constants.Item.Script.GE_OFFER_COLLECTION_SCRIPT_ID;
import static com.datalogger.constants.Item.Script.GE_OFFER_SCRIPT_ID;
import static com.datalogger.constants.Item.Values.MAX_ITEM_TAX;
import static com.datalogger.constants.Item.Values.MAX_TAXED_PRICE;
import static com.datalogger.constants.Item.Values.TAX_MULTIPLIER;
import com.datalogger.framework.AbstractLogger;
import com.datalogger.framework.LogType;
import com.datalogger.models.grandexchange.ActiveGeOffer;
import com.datalogger.models.grandexchange.GeLedgerEntry;
import com.datalogger.services.GrandExchangeHistoryParser;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.events.GrandExchangeOfferChanged;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;

@Slf4j
@Singleton
public class GrandExchangeLogger extends AbstractLogger
{
	@Inject private ItemManager itemManager;
	@Inject private ClientThread clientThread;

	@Inject private GrandExchangeHistoryParser historyParser;

	private static final String CSV_HEADER = "ItemId,ItemName,OfferCreationTime,Timestamp,TradeType,Quantity,OfferQuantity,Price,OfferPrice,Value,Tax,AccountName,AccountHash,GeSlot,IsHistoryEntry,IsCancelled";

	private ActiveGeOffer[] currentActiveOffers = new ActiveGeOffer[8];

//	private List<GeLedgerEntry> internalLedger;

	private final String[] lastLoggedFingerprints = new String[8];

	private boolean loggerIsEnabled = false;
	private boolean collectionScriptRunning = false;

	@Override
	public LogType getLogType() { return LogType.GRAND_EXCHANGE; }

	@Override
	public String getCsvHeader() { return CSV_HEADER; }

	@Override
	public boolean isEnabled() { return config.logGrandExchange(); }

	private Long lastParsedHistory = null;
	private final int HISTORY_PARSE_COOLDOWN_MS = 120000;

	/**
	 * Triggered automatically by AbstractLogger when a valid session begins.
	 */
	@Override
	public void setup()
	{
		loggerIsEnabled = config.logGrandExchange();
		if (!loggerIsEnabled)
			return;

		Properties state = fileIOService.getAccountState();
		for (int i = 0; i < 8; i++) {
			lastLoggedFingerprints[i] = state.getProperty("last_fp_" + i);
		}

		List<ActiveGeOffer> savedState = fileIOService.loadActiveGeOffers();
		currentActiveOffers = new ActiveGeOffer[8];
		for (ActiveGeOffer offer : savedState) {
			if (offer != null && offer.getSlot() >= 0 && offer.getSlot() < 8) {
				currentActiveOffers[offer.getSlot()] = offer;
			}
		}
		initialScan();
	}

	private boolean isGrandExchangeSubmissionScript(int scriptId)
	{
		return scriptId == GE_OFFER_SCRIPT_ID || scriptId == GE_OFFER_COLLECTION_SCRIPT_ID;
	}

	/**
	 * Submit all active, completed offers that have not been submitted so far.
	 */
	private void submitCompletedOffers()
	{

		GrandExchangeOffer[] offers = client.getGrandExchangeOffers();
		if (offers == null) return;

		String hashString = getAccountHashString();
		if (hashString.equals("-1"))
		{
			return;
		}

		for (int geSlot = 0; geSlot < offers.length; geSlot++)
		{
			GrandExchangeOffer offer = offers[geSlot];

			if (offer == null || !isFinalState(offer.getState())) {continue;}

			log.debug("Attempting to submit completed trade in GE slot {}...", geSlot);
			if (handleOffer(geSlot, offer))
				log.info("Submitted completed offer from GE slot {}", geSlot);
		}
	}

	/**
	 * Handler for submitting active, completed offers right before collecting the items from the active exchange offers
	 */
	private void onCollectActiveGrandExchangeOffers()
	{
		collectionScriptRunning = true;
		log.info("Offers are being collected - attempting to submit completed offers before they are cleared");
		submitCompletedOffers();
		collectionScriptRunning = false;
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired event)
	{
		if (!loggerIsEnabled) return;

		if (!collectionScriptRunning && isGrandExchangeSubmissionScript(event.getScriptId()))
			onCollectActiveGrandExchangeOffers();

	}

	@Subscribe
	public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged event) {
		String hash = getAccountHashString();
		if (hash.equals("-1")) return;

		int slot = event.getSlot();
		GrandExchangeOffer offer = event.getOffer();

		updateActiveOfferState(slot, offer);
		handleOffer(slot, offer);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (!config.logGrandExchange()) return;

		if (event.getGroupId() == GE_GROUP_ID)
		{
			if (canParseHistory())
			{
				historyUI();
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(DataLoggerConfig.CONFIG_GROUP)) return;

		if (!loggerIsEnabled && config.logGrandExchange())
		{
			setup();
		}

		loggerIsEnabled = config.logGrandExchange();
	}

	private void historyUI()
	{
		Widget cw = client.getWidget(GE_GROUP_ID, GE_HISTORY_CHILD_ID);
		if (cw != null && !cw.isHidden())
		{
			this.lastParsedHistory = Instant.now().toEpochMilli();
			log.info("Attempting to parse GE history....");
			clientThread.invokeLater(this.historyParser::parseGeHistory);
		}
	}

	/**
	 * Maintains the 8-slot array of active GE offers and saves to disk on change.
	 */
	private void updateActiveOfferState(int slot, GrandExchangeOffer offer) {
		if (offer.getState() == GrandExchangeOfferState.EMPTY) {
			currentActiveOffers[slot] = null;
		} else {
			boolean isBuy = isBuy(offer.getState());
			String itemName = itemManager.getItemComposition(offer.getItemId()).getName();

			ActiveGeOffer activeOffer = ActiveGeOffer.builder()
				.accountName(getAccountName())
				.slot(slot)
				.itemId(offer.getItemId())
				.itemName(itemName)
				.isBuy(isBuy)
				.state(offer.getState())
				.totalQuantity(offer.getTotalQuantity())
				.quantitySold(offer.getQuantitySold())
				.offerPrice(offer.getPrice())
				.spent(offer.getSpent())
				.build();

			currentActiveOffers[slot] = activeOffer;
		}

		List<ActiveGeOffer> listToSave = Arrays.stream(currentActiveOffers)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		fileIOService.saveActiveGeOffers(listToSave);
	}

	public boolean handleOffer(int slot, GrandExchangeOffer offer) {
		if (!isEnabled()) return false;

		if (offer.getState() == GrandExchangeOfferState.EMPTY) {
			clearSlotMemory(slot);
			return false;
		}

		long accountHashLong = getAccountHashLong();
		Properties state = fileIOService.getAccountState();

		String createdTs = getOrSetCreatedTimestamp(slot, offer, state);
		String fingerprint = generateFingerprint(offer);
		String lastLoggedFp = lastLoggedFingerprints[slot];


		if (isFinalState(offer.getState()) && offer.getSpent() > 0) {
			if (!fingerprint.equals(lastLoggedFp)) {
				lastLoggedFingerprints[slot] = fingerprint;
				logFinalTrade(slot, offer, createdTs, accountHashLong);

				state.setProperty("last_fp_" + slot, fingerprint);
				fileIOService.saveAccountState(state);
				log.debug("Logged GE trade for slot {}. FP: {}", slot, fingerprint);
				return true;
			}
			else
				log.debug("Did not submit trade for slot {} - it is equal to the last submission for this slot", slot);
		}
		return false;
	}

	public void initialScan() {
		GrandExchangeOffer[] offers = client.getGrandExchangeOffers();
		if (offers == null) return;

		Properties state = fileIOService.getAccountState();
		boolean propertiesChanged = false;

		for (int i = 0; i < offers.length; i++) {
			GrandExchangeOffer offer = offers[i];

			if (offer == null || offer.getState() == GrandExchangeOfferState.EMPTY) {
				currentActiveOffers[i] = null;
				continue;
			} else {
				boolean isBuy = isBuy(offer.getState());
				String itemName = itemManager.getItemComposition(offer.getItemId()).getName();

				currentActiveOffers[i] = ActiveGeOffer.builder()
					.slot(i)
					.itemId(offer.getItemId())
					.itemName(itemName)
					.isBuy(isBuy)
					.state(offer.getState())
					.totalQuantity(offer.getTotalQuantity())
					.quantitySold(offer.getQuantitySold())
					.offerPrice(offer.getPrice())
					.spent(offer.getSpent())
					.build();
			}

			getOrSetCreatedTimestamp(i, offer, state);

			if (isFinalState(offer.getState())) {
				String fp = generateFingerprint(offer);
				if (!fp.equals(lastLoggedFingerprints[i])) {
					state.setProperty("last_fp_" + i, fp);
					propertiesChanged = true;
				}
			}
		}

		if (propertiesChanged) {
			fileIOService.saveAccountState(state);
		}

		List<ActiveGeOffer> listToSave = Arrays.stream(currentActiveOffers)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		fileIOService.saveActiveGeOffers(listToSave);

//		internalLedger = fileIOService.loadInternalGeLedger(accountHash);
		log.debug("Initial GE scan complete. Live active offers saved to disk for {}", getAccountName());
	}

	private boolean isCancelledState(GrandExchangeOfferState state) {
		return state == GrandExchangeOfferState.CANCELLED_BUY || state == GrandExchangeOfferState.CANCELLED_SELL;
	}

	/**
	 * Approximate the tax paid using the offer data that is available. Note that this is not a perfect estimate.
	 */
	private int approximateTax(boolean isBuy, int quantity, int price)
	{
		if (!isBuy && price >= 49) {
			if (price >= MAX_TAXED_PRICE) {
				return MAX_ITEM_TAX;
			} else {
				long approxGrossPerItem = Math.round(price / TAX_MULTIPLIER);
				return (int) (approxGrossPerItem - price);
			}

		}
		return 0;
	}

	/**
	 * Submits a completed trade to the internal ledger and adds it to the CSV file
	 */
	private void logFinalTrade(int slot, GrandExchangeOffer offer, String createdTs, long accountHash)
	{
		String currentAccountName = getAccountName();
		int quantity = offer.getQuantitySold();

		if (quantity == 0)
		{
			log.error("Encountered an offer with quantity=0; {}", offer);
			return;
		}

		int totalSpent = offer.getSpent();
		int price = (int) Math.floor((double) totalSpent / quantity);
		boolean isBuy = isBuy(offer.getState());
		int estimatedTaxPaid = approximateTax(isBuy, quantity, price);

		Instant creationInstant = null;
		if (createdTs != null && !createdTs.isEmpty())
		{
			try
			{
				creationInstant = Instant.parse(createdTs);
			}
			catch (Exception e)
			{
				log.debug("Could not parse createdTs: {}", createdTs);
			}
		}

		GeLedgerEntry ledgerEntry = GeLedgerEntry.builder()
			.itemId(offer.getItemId())
			.itemName(itemManager.getItemComposition(offer.getItemId()).getName())
			.isBuy(isBuy)
			.quantity(quantity)
			.price(price)
			.value(totalSpent)
			.tax(estimatedTaxPaid)
			.accountName(currentAccountName)
			.accountHash(accountHash)
			.geSlot(slot)
			.isHistoryEntry(false)
			.isCancelled(isCancelledState(offer.getState()))
			.offerCreationTime(creationInstant)
			.exactTimestamp(Instant.now())
			.originalOfferQuantity(offer.getTotalQuantity())
			.originalOfferPrice(offer.getPrice())
			.build();

		String hashString = getAccountHashString();
		List<GeLedgerEntry> internalLedger = fileIOService.loadInternalGeLedger(hashString);
		internalLedger.add(ledgerEntry);
		fileIOService.saveInternalGeLedger(internalLedger);

		if (config.logGrandExchangeCSV())
		{
			String row = formatCsvRow(ledgerEntry);
			logRow(row);
		}
	}

	/**
	 * Converts a GeLedgerEntry into a comma-separated string aligning with:
	 * ItemId,ItemName,OfferCreationTime,Timestamp,TradeType,Quantity,OfferQuantity,Price,OfferPrice,Value,Tax,AccountName,AccountHash,GeSlot,IsHistoryEntry,IsCancelled
	 */
	private String formatCsvRow(GeLedgerEntry entry) {
		String safeItemName = entry.getItemName() != null ? entry.getItemName() : "Unknown";
		if (safeItemName.contains(",")) {
			safeItemName = "\"" + safeItemName + "\"";
		}

		String creationTimeStr = entry.getOfferCreationTime() != null ? entry.getOfferCreationTime().toString() : "";

		Instant mainTime = entry.getExactTimestamp() != null ? entry.getExactTimestamp() : entry.getParseTime();
		String timeStr = mainTime != null ? mainTime.toString() : "";

		String tradeType = entry.isBuy() ? "BUY" : "SELL";

		return String.join(",",
			String.valueOf(entry.getItemId()),
			safeItemName,
			creationTimeStr,
			timeStr,
			tradeType,
			String.valueOf(entry.getQuantity()),
			String.valueOf(entry.getOriginalOfferQuantity()),
			String.valueOf(entry.getPrice()),
			String.valueOf(entry.getOriginalOfferPrice()),
			String.valueOf(entry.getValue()),
			String.valueOf(entry.getTax()),
			entry.getAccountName() != null ? entry.getAccountName() : "",
			String.valueOf(entry.getAccountHash()),
			String.valueOf(entry.getGeSlot()),
			String.valueOf(entry.isHistoryEntry()),
			String.valueOf(entry.isCancelled())
		);
	}

	/**
	 * Fetch and return the createdTimestamp registered to the given offer. If no registration is found, return the
	 * current timestamp instead.
	 */
	private String getOrSetCreatedTimestamp(int slot, GrandExchangeOffer offer, Properties state) {
		String tsKey = "slot_created_" + slot;
		String itemKey = "slot_item_" + slot;

		String existing = state.getProperty(tsKey);
		String lastItemId = state.getProperty(itemKey);

		if (existing == null || lastItemId == null || Integer.parseInt(lastItemId) != offer.getItemId()) {
			existing = Instant.now().toString();
			state.setProperty(tsKey, existing);
			state.setProperty(itemKey, String.valueOf(offer.getItemId()));
			fileIOService.saveAccountState(state);
		}
		return existing;
	}

	private void clearSlotMemory(int slot) {
		String hash = getAccountHashString();
		Properties state = fileIOService.getAccountState();

		if (state.containsKey("slot_created_" + slot)) {
			state.remove("slot_created_" + slot);
			state.remove("slot_item_" + slot);
			fileIOService.saveAccountState(state);
		}
	}

	private String generateFingerprint(GrandExchangeOffer offer) {
		return offer.getItemId() + "_" + offer.getTotalQuantity() + "_" + offer.getSpent() + "_" + offer.getState();
	}

	private boolean isFinalState(GrandExchangeOfferState state) {
		return state == GrandExchangeOfferState.BOUGHT ||
			state == GrandExchangeOfferState.SOLD ||
			state == GrandExchangeOfferState.CANCELLED_BUY ||
			state == GrandExchangeOfferState.CANCELLED_SELL;
	}

	private boolean isBuy(GrandExchangeOfferState state) {
		return state == GrandExchangeOfferState.BUYING ||
			state == GrandExchangeOfferState.BOUGHT ||
			state == GrandExchangeOfferState.CANCELLED_BUY;
	}

	/**
	 * Updates relevant configurations into class variables
	 */
	private void updateConfigurations()
	{
		loggerIsEnabled = config.logGrandExchange();
	}

	/**
	 * Return true if the GE history may be parsed again. Should be constrained to some degree to prevent
	 * excessive/duplicate calls
	 */
	private boolean canParseHistory()
	{
		return lastParsedHistory == null || Instant.now().toEpochMilli() - lastParsedHistory > HISTORY_PARSE_COOLDOWN_MS;
	}
}