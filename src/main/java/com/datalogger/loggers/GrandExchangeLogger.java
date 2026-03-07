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

import static com.datalogger.constants.GrandExchange.Values.MAX_ITEM_TAX;
import static com.datalogger.constants.GrandExchange.Values.MAX_TAXED_PRICE;
import static com.datalogger.constants.GrandExchange.Values.TAX_MULTIPLIER;
import com.datalogger.framework.AbstractLogger;
import com.datalogger.framework.LogType;
import com.datalogger.models.grandexchange.ActiveGeOffer;
import com.datalogger.models.grandexchange.GeLedgerEntry;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;

@Slf4j
@Singleton
public class GrandExchangeLogger extends AbstractLogger
{
	// The other dependencies (Client, FileIOService, Config) are inherited from AbstractLogger!
	@Inject private ItemManager itemManager;

	private static final String CSV_HEADER = "ItemId,ItemName,OfferCreationTime,Timestamp,TradeType,Quantity,OfferQuantity,Price,OfferPrice,Value,Tax,AccountName,AccountHash,GeSlot,IsHistoryEntry,IsCancelled";

	// Memory cache for Wealth Tracker
	private ActiveGeOffer[] currentActiveOffers = new ActiveGeOffer[8];

	@Override
	public LogType getLogType() { return LogType.GRAND_EXCHANGE; }

	@Override
	public String getCsvHeader() { return CSV_HEADER; }

	@Override
	public boolean isEnabled() { return config.logGrandExchange(); }

	/**
	 * Triggered automatically by AbstractLogger when a valid session begins.
	 */
	@Override
	public void setup()
	{
		String hash = getAccountHashString();
		if (hash.equals("-1")) return;

		List<ActiveGeOffer> savedState = utils.loadActiveGeOffers(hash);
		currentActiveOffers = new ActiveGeOffer[8];
		for (ActiveGeOffer offer : savedState) {
			if (offer != null && offer.getSlot() >= 0 && offer.getSlot() < 8) {
				currentActiveOffers[offer.getSlot()] = offer;
			}
		}
		initialScan();
	}

	@Subscribe
	public void onGrandExchangeOfferChanged(GrandExchangeOfferChanged event) {
		String hash = getAccountHashString();
		if (hash.equals("-1")) return;

		int slot = event.getSlot();
		GrandExchangeOffer offer = event.getOffer();

		updateActiveOfferState(slot, offer, hash);
		handleOffer(slot, offer);
	}

	/**
	 * Maintains the 8-slot array of active GE offers and saves to disk on change.
	 */
	private void updateActiveOfferState(int slot, GrandExchangeOffer offer, String hash) {
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

		utils.saveActiveGeOffers(hash, listToSave);
	}

	public void handleOffer(int slot, GrandExchangeOffer offer) {
		if (!isEnabled()) return;

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
				logFinalTrade(slot, offer, createdTs, accountHash);

				state.setProperty("last_fp_" + slot, fingerprint);
				utils.saveAccountState(accountHash, state);
				log.debug("Logged GE trade for slot {}. FP: {}", slot, fingerprint);
			}
		}
	}

	public void initialScan() {
		GrandExchangeOffer[] offers = client.getGrandExchangeOffers();
		if (offers == null) return;

		String accountHash = getAccountHashString();
		Properties state = utils.getAccountState(accountHash);
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

			getOrSetCreatedTimestamp(i, offer, state, accountHash);

			if (isFinalState(offer.getState())) {
				String fp = generateFingerprint(offer);
				if (!fp.equals(state.getProperty("last_fp_" + i))) {
					state.setProperty("last_fp_" + i, fp);
					propertiesChanged = true;
				}
			}
		}

		if (propertiesChanged) {
			utils.saveAccountState(accountHash, state);
		}

		List<ActiveGeOffer> listToSave = Arrays.stream(currentActiveOffers)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		utils.saveActiveGeOffers(accountHash, listToSave);
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
		int estimatedTaxPaid = 0;

		if (!isBuy && price >= 49) {
			int approxTaxPerItem;

			if (price >= MAX_TAXED_PRICE) {
				approxTaxPerItem = MAX_ITEM_TAX;
			} else {
				long approxGrossPerItem = Math.round(price / TAX_MULTIPLIER);
				approxTaxPerItem = (int) (approxGrossPerItem - price);
			}

			estimatedTaxPaid = approxTaxPerItem * quantity;
		}
		return estimatedTaxPaid;
	}

	/**
	 * Submits a completed trade to the internal ledger and adds it to the CSV file
	 */
	private void logFinalTrade(int slot, GrandExchangeOffer offer, String createdTs, String accountHash) {
		String currentAccountName = getAccountName();
		int quantity = offer.getQuantitySold();

		if (quantity == 0) {
			log.error("Encountered an offer with quantity=0; {}", offer);
			return;
		}

		int totalSpent = offer.getSpent();
		int price = totalSpent / quantity;
		boolean isBuy = isBuy(offer.getState());
		int estimatedTaxPaid = approximateTax(isBuy, quantity, price);


		Instant creationInstant = null;
		if (createdTs != null && !createdTs.isEmpty()) {
			try {
				creationInstant = Instant.parse(createdTs);
			} catch (Exception e) {
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


		List<GeLedgerEntry> internalLedger = utils.loadInternalGeLedger(accountHash);
		internalLedger.add(ledgerEntry);
		utils.saveInternalGeLedger(accountHash, internalLedger);

		String row = formatCsvRow(ledgerEntry);
		logRow(row);
	}

	/**
	 * Extracts all Grand Exchange trades for a specific date from the internal ledger
	 * and exports them to a standalone CSV file.
	 * @param targetDate The specific day to extract (e.g., LocalDate.now())
	 */
	public void exportLedgerForDay(LocalDate targetDate) {
		String hash = getAccountHashString();
		String accountName = getAccountName();

		if (hash.equals("-1") || accountName.equals("unknown")) {
			return;
		}

		List<GeLedgerEntry> fullLedger = utils.loadInternalGeLedger(hash);
		if (fullLedger == null || fullLedger.isEmpty()) {
			log.info("No GE history found in the internal ledger for {}.", accountName);
			return;
		}

		List<GeLedgerEntry> dailyEntries = fullLedger.stream()
			.filter(entry -> {
				Instant ts = entry.getExactTimestamp() != null ? entry.getExactTimestamp() : entry.getParseTime();
				if (ts == null) return false;

				LocalDate entryDate = ts.atZone(ZoneId.systemDefault()).toLocalDate();
				return entryDate.equals(targetDate);
			})
			.collect(Collectors.toList());

		if (dailyEntries.isEmpty()) {
			log.info("No GE entries found for {} on {}", accountName, targetDate);
			return;
		}

		String dateString = targetDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
		File exportDir = new File(RuneLite.RUNELITE_DIR, "data-logger/exports/grand-exchange");
		if (!exportDir.exists() && !exportDir.mkdirs()) {
			log.error("Failed to create export directory: {}", exportDir.getAbsolutePath());
			return;
		}

		File exportFile = new File(exportDir, accountName + "_" + dateString + ".csv");

		try (PrintWriter writer = new PrintWriter(new FileWriter(exportFile))) {
			writer.println(CSV_HEADER);

			for (GeLedgerEntry entry : dailyEntries) {
				Instant ts = entry.getExactTimestamp() != null ? entry.getExactTimestamp() : entry.getParseTime();
				String timestampStr = ts != null ? ts.toString() : "";

				int totalValue = entry.getQuantity() * entry.getPrice();

				String row = String.format("%d,%s,%s,%b,%d,%d,%d,%d,%d,%s,%d",
					entry.getItemId(),
					timestampStr,
					"",
					entry.isBuy(),
					entry.getQuantity(),
					entry.getOriginalOfferQuantity(),
					entry.getPrice(),
					entry.getOriginalOfferPrice(),
					totalValue,
					accountName,
					-1
				);
				writer.println(row);
			}
			log.info("Successfully exported {} GE trades for {} to {}", dailyEntries.size(), accountName, exportFile.getAbsolutePath());
		} catch (IOException e) {
			log.error("Failed to export GE ledger for day {}", targetDate, e);
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
			entry.getAccountHash() != null ? entry.getAccountHash() : "",
			String.valueOf(entry.getGeSlot()),
			String.valueOf(entry.isHistoryEntry()),
			String.valueOf(entry.isCancelled())
		);
	}

	private String getOrSetCreatedTimestamp(int slot, GrandExchangeOffer offer, Properties state, String hash) {
		String tsKey = "slot_created_" + slot;
		String itemKey = "slot_item_" + slot;

		String existing = state.getProperty(tsKey);
		String lastItemId = state.getProperty(itemKey);

		if (existing == null || lastItemId == null || Integer.parseInt(lastItemId) != offer.getItemId()) {
			existing = Instant.now().toString();
			state.setProperty(tsKey, existing);
			state.setProperty(itemKey, String.valueOf(offer.getItemId()));
			utils.saveAccountState(hash, state);
		}
		return existing;
	}

	private void clearSlotMemory(int slot) {
		String hash = getAccountHashString();
		Properties state = utils.getAccountState(hash);

		if (state.containsKey("slot_created_" + slot)) {
			state.remove("slot_created_" + slot);
			state.remove("slot_item_" + slot);
			utils.saveAccountState(hash, state);
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
}