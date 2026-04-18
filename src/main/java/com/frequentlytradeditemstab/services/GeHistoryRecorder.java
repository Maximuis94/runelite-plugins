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

import static com.frequentlytradeditemstab.PluginConstants.GeHistory.GE_HISTORY_CHILD_CONTAINER_ID;
import static com.frequentlytradeditemstab.PluginConstants.GeHistory.ELEMENTS_PER_ROW;
import static com.frequentlytradeditemstab.PluginConstants.GeHistory.PARSE_COOLDOWN_MS;
import com.frequentlytradeditemstab.models.GrandExchangeHistoryEntry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.WidgetLoaded;
import static net.runelite.api.gameval.InterfaceID.GE_HISTORY;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class used to keep track of GE History entries. GE History entries are tracked separately from completed GE trades.
 * As such, they are also stored separately, albeit in a similar fashion.
 * GE History entries are registered when the user opens the GE history tab.
 */
@Slf4j
@Singleton
public class GeHistoryRecorder {
	private final Client client;
	private final ClientThread clientThread;
	private final ItemManager itemManager;
	private final GeHistoryCacheManager cacheManager;

	private long lastParsed = 0;

	@Inject
	public GeHistoryRecorder(Client client, ClientThread clientThread, ItemManager itemManager, GeHistoryCacheManager cacheManager) {
		this.client = client;
		this.clientThread = clientThread;
		this.itemManager = itemManager;
		this.cacheManager = cacheManager;
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event) {
		if (event.getGroupId() == GE_HISTORY ) {
			clientThread.invokeLater(this::parseGeHistory);
		}
	}

	private void parseGeHistory() {
		if (System.currentTimeMillis() - lastParsed < PARSE_COOLDOWN_MS) {
			log.debug("Skipping GE history parse; history was parsed recently.");
			return;
		}

		Widget historyContainer = client.getWidget(GE_HISTORY, GE_HISTORY_CHILD_CONTAINER_ID);
		if (historyContainer == null || historyContainer.isHidden()) {
			return;
		}

		Widget[] children = historyContainer.getDynamicChildren();
		if (children == null || children.length == 0) {
			return;
		}

		List<GrandExchangeHistoryEntry> parsedEntries = new ArrayList<>();
		long parseTimeMillis = System.currentTimeMillis();

		long accountHash = client.getAccountHash();
		String accountName = client.getLocalPlayer() != null ? client.getLocalPlayer().getName() : "Unknown";

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

			try {
				ExtractedHistoryValue historyValue = parseValueWidgetText(valueWidget.getText());

				GrandExchangeHistoryEntry entry = GrandExchangeHistoryEntry.builder()
					.itemId(itemId)
					.itemName(item.getMembersName())
					.isBuy(isBuy)
					.quantity(itemQuantity)
					.price(historyValue.getPriceEach())
					.grossValue(historyValue.getGrossCoins())
					.netValue(historyValue.getNetCoins())
					.tax(historyValue.getTax())
					.accountName(accountName)
					.accountHash(accountHash)
					.parseTime(parseTimeMillis)
					.associatedGeSlot(-1)
					.associatedExactTimestamp(-1)
					.build();

				parsedEntries.add(entry);
			} catch (IllegalStateException e) {
				log.error("Failed to parse history entry. Raw text: {}", valueWidget.getText(), e);
			}
		}

		if (!parsedEntries.isEmpty()) {
			Collections.reverse(parsedEntries);

			cacheManager.saveHistoryEntriesAsync(accountHash, parsedEntries);
			log.debug("Successfully parsed {} GE History entries.", parsedEntries.size());
		}

		lastParsed = System.currentTimeMillis();
	}

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

	@Data
	private static class ExtractedHistoryValue {
		private int netCoins;
		private int grossCoins;
		private int taxPaid;
		private int priceEach;
		private int quantity;

		public int getTax() {
			return quantity > 0 ? taxPaid / quantity : 0;
		}
	}
}