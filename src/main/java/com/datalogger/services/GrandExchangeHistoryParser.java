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
import com.datalogger.models.grandexchange.GrandExchangeHistoryEntry;
import static com.datalogger.services.FileIOService.INTERNAL_GE_HISTORY_DIR;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
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
	private final ItemManager itemManager;
	private final Gson gson;
	private final ClientThread clientThread;

	@Setter
	private boolean isEnabled = false;

	private long accountHash = -1;
	private String accountName = null;
	private File geHistoryLogFile = null;

	private long parseTimeMillis;

	private Long lastParsedHistory = null;
	private final int HISTORY_PARSE_COOLDOWN_MS = 120000;

	private boolean hasParsed = false;
	private long lastParsed = 0;
	private static final int ELEMENTS_PER_ROW = 6;
	private static final long COOLDOWN_MS = 5 * 60 * 1000;

	private final List<GrandExchangeHistoryEntry> recentHistoryCache = new ArrayList<>();
	private boolean isCacheLoaded = false;

	@Inject
	public GrandExchangeHistoryParser(Client client, FileIOService fileIOService, DataLoggerConfig config, ItemManager itemManager, Gson gson, ClientThread clientThread)
	{
		this.client = client;
		this.fileIOService = fileIOService;
		this.config = config;
		this.itemManager = itemManager;
		this.gson = gson;
		this.clientThread = clientThread;
	}

	@Subscribe
	public void onAccountSessionStarted(AccountSessionStarted event)
	{
		accountHash = event.getAccountHash();
		accountName = event.getAccountName();
		geHistoryLogFile = new File(INTERNAL_GE_HISTORY_DIR, accountHash + ".jsonl");

		hasParsed = false;
		isCacheLoaded = false;
		recentHistoryCache.clear();
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (!isEnabled) return;

		if (event.getGroupId() == GE_GROUP_ID)
		{
			if (canParseHistory())
			{
				historyUI();
			}
		}
	}

	private void historyUI()
	{
		Widget cw = client.getWidget(GE_GROUP_ID, GE_HISTORY_CHILD_ID);
		if (cw != null && !cw.isHidden())
		{
			this.lastParsedHistory = Instant.now().toEpochMilli();
			log.debug("Attempting to parse GE history....");
			clientThread.invokeLater(this::parseGeHistory);
		}
	}

	/**
	 * Return true if the GE history may be parsed again. Should be constrained to some degree to prevent
	 * excessive/duplicate calls
	 */
	private boolean canParseHistory()
	{
		return lastParsedHistory == null || Instant.now().toEpochMilli() - lastParsedHistory > HISTORY_PARSE_COOLDOWN_MS;
	}

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
	 * Load the last 40 history entries from the jsonl file into memory.
	 */
	private void loadCacheFromFile()
	{
		List<GrandExchangeHistoryEntry> fileEntries = fileIOService.readJsonlFile(geHistoryLogFile, GrandExchangeHistoryEntry.class);

		if (fileEntries == null || fileEntries.isEmpty()) return;

		Collections.reverse(fileEntries);
		recentHistoryCache.addAll(fileEntries.subList(0, Math.min(fileEntries.size(), 40)));
	}

	private GrandExchangeHistoryEntry parseRow(Widget[] children, int start)
	{
		Widget typeWidget = children[start + 2];
		Widget itemWidget = children[start + 4];
		Widget valueWidget = children[start + 5];

		if (typeWidget == null || itemWidget == null || valueWidget == null)
		{
			return null;
		}

		String typeText = Text.removeTags(typeWidget.getText()).trim();
		boolean isBuy = typeText.equalsIgnoreCase("Bought:");

		int itemId = itemWidget.getItemId();
		int itemQuantity = itemWidget.getItemQuantity();
		ItemComposition item = itemManager.getItemComposition(itemId);
		String itemName = item.getMembersName();

		try
		{
			ExtractedHistoryValue historyEntry = parseValueWidgetText(valueWidget.getText());

			return GrandExchangeHistoryEntry.builder()
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

		}
		catch (IllegalStateException e)
		{
			log.error("Failed to parse history entry (possible quantity=0). Raw text: {}", valueWidget.getText(), e);
		}

		return null;
	}

	public void parseGeHistory()
	{
		if (!allowedToParseHistory())
		{
			log.debug("Not parsing Grand Exchange history");
			return;
		}

		Widget historyContainer = client.getWidget(GE_GROUP_ID, GE_HISTORY_CHILD_ID);

		if (historyContainer == null || historyContainer.isHidden()) return;

		Widget[] children = historyContainer.getDynamicChildren();
		if (children == null || children.length == 0) return;

		if (!isCacheLoaded)
		{
			loadCacheFromFile();
			isCacheLoaded = true;
		}

		List<GrandExchangeHistoryEntry> uiEntries = new ArrayList<>();
		parseTimeMillis = System.currentTimeMillis();

		for (int i = 0; i < children.length; i += ELEMENTS_PER_ROW)
		{
			if (i + (ELEMENTS_PER_ROW - 1) >= children.length) break;

			GrandExchangeHistoryEntry entry = parseRow(children, i);
			if (entry != null)
			{
				uiEntries.add(entry);
			}
		}

		if (uiEntries.isEmpty()) return;

		List<GrandExchangeHistoryEntry> newEntries = getNewEntries(uiEntries, recentHistoryCache);

		if (!newEntries.isEmpty())
		{
			List<GrandExchangeHistoryEntry> toAppend = new ArrayList<>(newEntries);
			Collections.reverse(toAppend);

			try (FileWriter writer = new FileWriter(geHistoryLogFile, true))
			{
				for (GrandExchangeHistoryEntry entry : toAppend)
				{
					writer.write(gson.toJson(entry) + "\n");
				}
				log.debug("Successfully appended {} new GE History entries.", toAppend.size());
			}
			catch (IOException e)
			{
				log.error("Failed to append to GE history log file", e);
			}

			recentHistoryCache.addAll(0, newEntries);
			if (recentHistoryCache.size() > 40)
			{
				recentHistoryCache.subList(40, recentHistoryCache.size()).clear();
			}
		}
		else
		{
			log.debug("No new GE history entries found to log.");
		}

		lastParsed = System.currentTimeMillis();
		hasParsed = true;
	}

	/**
	 * Compares newly parsed UI entries with the cached entries to identify the exact overlap sequence.
	 * Returns a list containing only the entries that haven't been saved yet.
	 */
	private List<GrandExchangeHistoryEntry> getNewEntries(List<GrandExchangeHistoryEntry> uiEntries, List<GrandExchangeHistoryEntry> savedEntries)
	{
		if (savedEntries.isEmpty()) return uiEntries;

		int uiSize = uiEntries.size();

		for (int i = 0; i < uiSize; i++)
		{
			boolean match = true;
			int elementsToCompare = Math.min(uiSize - i, savedEntries.size());

			for (int j = 0; j < elementsToCompare; j++)
			{
				if (!isSameEntry(uiEntries.get(i + j), savedEntries.get(j)))
				{
					match = false;
					break;
				}
			}

			if (match)
			{
				return uiEntries.subList(0, i);
			}
		}
		return uiEntries;
	}

	/**
	 * Deeply compares two entries based on immutable data fields to verify if they represent the same transaction.
	 */
	private boolean isSameEntry(GrandExchangeHistoryEntry a, GrandExchangeHistoryEntry b)
	{
		return a.getItemId() == b.getItemId() &&
			a.getQuantity() == b.getQuantity() &&
			a.getPrice() == b.getPrice() &&
			a.isBuy() == b.isBuy() &&
			a.getGrossValue() == b.getGrossValue() &&
			a.getNetValue() == b.getNetValue() &&
			a.getTax() == b.getTax();
	}

	@Data
	private static class ExtractedHistoryValue
	{
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

	private ExtractedHistoryValue parseValueWidgetText(String rawText) throws IllegalStateException
	{
		String preProcessed = rawText.replaceAll("(?i)<br\\s*/?>", "\n");
		String cleanText = preProcessed.replaceAll("<[^>]+>", "").replace(",", "");
		String[] lines = cleanText.split("\\n");

		ExtractedHistoryValue result = new ExtractedHistoryValue();
		result.setNetCoins(Integer.parseInt(lines[0].replace(" coins", "").trim()));
		result.setGrossCoins(result.getNetCoins());
		result.setPriceEach(result.getNetCoins());
		result.setTaxPaid(0);

		for (int i = 1; i < lines.length; i++)
		{
			String line = lines[i].trim();

			if (line.startsWith("("))
			{
				line = line.replace("(", "").replace(")", "");
				String[] parts = line.split("-");
				if (parts.length == 2)
				{
					result.setGrossCoins(Integer.parseInt(parts[0].trim()));
					result.setTaxPaid(Integer.parseInt(parts[1].trim()));
				}
			}
			else if (line.startsWith("="))
			{
				line = line.replace("=", "").replace("each", "").trim();
				result.setPriceEach(Integer.parseInt(line));
			}
		}

		if (result.getPriceEach() > 0)
		{
			int derivedQuantity = Math.round((float) result.getNetCoins() / result.getPriceEach());
			result.setQuantity(Math.max(1, derivedQuantity));
		}
		else
		{
			result.setQuantity(0);
			throw new IllegalStateException("Calculated quantity resulted in 0 or less. Raw text:\n" + rawText);
		}

		return result;
	}
}