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

package com.datalogger.services;

import com.datalogger.DataLoggerConfig;
import static com.datalogger.constants.Item.InterfaceID.GE_GROUP_ID;
import static com.datalogger.constants.Item.InterfaceID.GE_HISTORY_CHILD_ID;
import com.datalogger.events.AccountSessionStarted;
import com.datalogger.models.grandexchange.GeLedgerEntry;
import com.datalogger.models.grandexchange.GrandExchangeHistoryEntry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class GrandExchangeHistoryParser
{
	private final Client client;
	private final FileIOService fileIOService;
	private final DataLoggerConfig config;
	private final GeHistoryReconciler reconciler;
	private final ItemManager itemManager;

	private long accountHash = -1;
	private String accountName = null;

	private long parseTimeMillis;

	private boolean hasParsed = false;
	private long lastParsed = 0;
	private static final int ELEMENTS_PER_ROW = 6;
	private static final long COOLDOWN_MS = 5 * 60 * 1000; // 5 minutes in milliseconds

	@Inject
	public GrandExchangeHistoryParser(Client client, FileIOService fileIOService, DataLoggerConfig config, ItemManager itemManager)
	{
		this.client = client;
		this.fileIOService = fileIOService;
		this.config = config;
		this.reconciler = new GeHistoryReconciler();
		this.itemManager = itemManager;
	}

	/**
	 * Listens for our custom event and caches the account hash globally for this class.
	 */
	@Subscribe
	public void onAccountSessionStarted(AccountSessionStarted event)
	{
		accountHash = event.getAccountHash();
		accountName = event.getAccountName();
		hasParsed = false;
	}

	/**
	 * Return true if history was parsed more than 5 minutes ago and has not been parsed during this session
	 */
	private boolean allowedToParseHistory()
	{
		if (!config.logGrandExchange() || accountHash == -1 || hasParsed) return false;

		if (System.currentTimeMillis() - lastParsed < COOLDOWN_MS) {
			log.debug("Skipping GE history parse; history was parsed less than 5 minutes ago.");
			hasParsed = true;
			return false;
		}
		return true;
	}

	/**
	 * Convert a row of the Grand Exchange History UI into a GrandExchangeHistoryEntry instance and return it.
	 */
	private GrandExchangeHistoryEntry parseRow(Widget[] children, int start)
	{
		Widget typeWidget = children[start + 2];
		Widget itemWidget = children[start + 4];
		Widget valueWidget = children[start + 5];

		if (typeWidget == null || itemWidget == null || valueWidget == null) {
			return null;
		}

		String typeText = Text.removeTags(typeWidget.getText()).trim();
		boolean isBuy = typeText.equalsIgnoreCase("Bought:");

		int itemId = itemWidget.getItemId();
		int itemQuantity = itemWidget.getItemQuantity();
		ItemComposition item = itemManager.getItemComposition(itemId);
		String itemName = item.getMembersName();

		try {
			String rawValueText = valueWidget.getText();
			ExtractedHistoryValue historyEntry = parseValueWidgetText(rawValueText);

			GrandExchangeHistoryEntry entry = GrandExchangeHistoryEntry.builder()
				.itemId(itemId)
				.itemName(itemName)
				.isBuy(isBuy)
				.price(historyEntry.getPriceEach())
				.quantity(itemQuantity)
				.grossValue(historyEntry.getGrossCoins())
				.netValue(historyEntry.getNetCoins())
				.tax(historyEntry.getTax())
				.accountName(accountName)
				.accountHash(accountHash)
				.parseTime(parseTimeMillis)
				.build();
			return entry;

		} catch (IllegalStateException e) {
			log.error("Failed to parse history entry (possible quantity=0). Raw text: {}", valueWidget.getText(), e);
		}
		return null;
	}

	/**
	 * Parse the GE History UI and submit the parsed entries. Check encountered entries against existing entries and
	 * skip them if they are already logged.
	 */
	public void parseGeHistory() {
		if (!allowedToParseHistory())
		{
			log.info("Not parsing Grand Exchange history");
			return;
		}

		Widget historyContainer = client.getWidget(GE_GROUP_ID, GE_HISTORY_CHILD_ID);

		if (historyContainer == null || historyContainer.isHidden()) {
			return;
		}

		Widget[] children = historyContainer.getDynamicChildren();
		if (children == null || children.length == 0) {
			return;
		}

		List<GeLedgerEntry> parsedEntries = new ArrayList<>();
		Instant currentParseTime = Instant.now();
		parseTimeMillis = currentParseTime.toEpochMilli();

		for (int i = 0; i < children.length; i += ELEMENTS_PER_ROW) {
			if (i + (ELEMENTS_PER_ROW - 1) >= children.length) {
				break;
			}

			Widget typeWidget = children[i + 2];
			Widget itemWidget = children[i + 4];
			Widget valueWidget = children[i + 5];

			if (typeWidget == null || itemWidget == null || valueWidget == null) {
				continue;
			}

			String typeText = Text.removeTags(typeWidget.getText()).trim();
			boolean isBuy = typeText.equalsIgnoreCase("Bought:");

			int itemId = itemWidget.getItemId();
			int itemQuantity = itemWidget.getItemQuantity();
			ItemComposition item = itemManager.getItemComposition(itemId);
			String itemName = item.getMembersName();

			try {
				String rawValueText = valueWidget.getText();
				ExtractedHistoryValue historyEntry = parseValueWidgetText(rawValueText);

				GeLedgerEntry entry = GeLedgerEntry.builder()
					.itemId(itemId)
					.itemName(itemName)
					.isBuy(isBuy)
					.price(historyEntry.getPriceEach())
					.quantity(itemQuantity)
					.value(historyEntry.getNetCoins())
					.tax(historyEntry.getTax())
					.accountName(accountName)
					.accountHash(accountHash)
					.geSlot(-1)
					.isHistoryEntry(true)
					.isCancelled(false)
					.parseTime(currentParseTime)
					.build();

				parsedEntries.add(entry);

			} catch (IllegalStateException e) {
				log.error("Failed to parse history entry (possible quantity=0). Raw text: {}", valueWidget.getText(), e);
			}
		}

		if (!parsedEntries.isEmpty()) {
			Collections.reverse(parsedEntries);

			String hash = String.valueOf(accountHash);
			List<GeLedgerEntry> savedEntries = fileIOService.loadInternalGeLedger(hash);

			List<GeLedgerEntry> mergedEntries = reconciler.weaveLedgers(savedEntries, parsedEntries);

			fileIOService.saveInternalGeLedger(mergedEntries);

			log.info("Successfully parsed and saved GE History.");
		}
		else
			log.info("Did not enter any entry data from the Grand Exchange history");

		lastParsed = System.currentTimeMillis();
		hasParsed = true;
	}

	@Data
	private static class ExtractedHistoryValue {
		private int netCoins;
		private int grossCoins;
		private int taxPaid;
		private int priceEach;
		private int quantity;

		public int getTax()
		{
			return taxPaid / quantity;
		}
	}

	/**
	 * Parses the complex OSRS GE History Value widget text.
	 * Handles color tags, commas, taxes, and dynamic line rendering.
	 */
	private ExtractedHistoryValue parseValueWidgetText(String rawText) throws IllegalStateException {
		String preProcessed = rawText.replaceAll("(?i)<br\\s*/?>", "\n");
		String cleanText = preProcessed.replaceAll("<[^>]+>", "").replace(",", "");
		String[] lines = cleanText.split("\\n");

		ExtractedHistoryValue result = new ExtractedHistoryValue();
		result.setNetCoins(Integer.parseInt(lines[0].replace(" coins", "").trim()));
		result.setGrossCoins(result.getNetCoins());
		result.setPriceEach(result.getNetCoins());
		result.setTaxPaid(0);

		for (int i = 1; i < lines.length; i++) {
			String line = lines[i].trim();

			if (line.startsWith("(")) {
				line = line.replace("(", "").replace(")", "");
				String[] parts = line.split("-");
				if (parts.length == 2) {
					result.setGrossCoins(Integer.parseInt(parts[0].trim()));
					result.setTaxPaid(Integer.parseInt(parts[1].trim()));
				}

			} else if (line.startsWith("=")) {
				line = line.replace("=", "").replace("each", "").trim();
				result.setPriceEach(Integer.parseInt(line));
			}
		}

		if (result.getPriceEach() > 0) {
			int derivedQuantity = Math.round((float) result.getNetCoins() / result.getPriceEach());
			result.setQuantity(Math.max(1, derivedQuantity));
		} else {
			result.setQuantity(0);
			throw new IllegalStateException("Calculated quantity resulted in 0 or less. Raw text:\n" + rawText);
		}
		return result;
	}
}