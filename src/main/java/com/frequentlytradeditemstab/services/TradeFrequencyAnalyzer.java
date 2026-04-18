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

import com.frequentlytradeditemstab.FrequentlyTradedItemsTabConfig;
import com.frequentlytradeditemstab.PluginConstants;
import com.frequentlytradeditemstab.models.CachedGrandExchangeTrade;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class TradeFrequencyAnalyzer {

	private final FrequentlyTradedItemsTabConfig config;
	private final ItemManager itemManager;

	@Inject
	public TradeFrequencyAnalyzer(FrequentlyTradedItemsTabConfig config, ItemManager itemManager) {
		this.config = config;
		this.itemManager = itemManager;
	}

	public Set<Integer> analyze(List<CachedGrandExchangeTrade> trades) {
		Map<Integer, Integer> tradeCounts = new HashMap<>();

		for (CachedGrandExchangeTrade trade : trades) {
			if (!trade.isBuy() && !config.includeSales()) continue;
			if (trade.isBuy() && !config.includePurchases()) continue;
			if (trade.isCancelled()) continue;

			int quantityTraded = trade.getQuantity();
			tradeCounts.put(trade.getItemId(), tradeCounts.getOrDefault(trade.getItemId(), 0) + quantityTraded);
		}

		Set<Integer> frequentItems = tradeCounts.entrySet().stream()
			.filter(entry -> {
				int itemId = entry.getKey();
				int totalQuantity = entry.getValue();

				ItemStats stats = itemManager.getItemStats(itemId);

				if (stats != null && stats.getGeLimit() > 0) {
					double requiredAmount = stats.getGeLimit() * config.buyLimitThreshold();
					return totalQuantity >= requiredAmount;
				}

				return totalQuantity >= PluginConstants.Analysis.DEFAULT_FREQUENCY_THRESHOLD;
			})
			.map(Map.Entry::getKey)
			.collect(Collectors.toSet());

		Set<Integer> expandedItems = new HashSet<>();
		for (Integer itemId : frequentItems) {
			expandAndAddVariantGroup(expandedItems, itemId);
		}

		applyConfigOverrides(expandedItems);

		return expandedItems;
	}

	private void applyConfigOverrides(Set<Integer> expandedItems) {
		List<String> includes = Text.fromCSV(config.includeItems());
		for (String name : includes) {
			if (name.isEmpty()) continue;

			itemManager.search(name).stream()
				.filter(itemPrice -> itemPrice.getName().equalsIgnoreCase(name))
				.forEach(itemPrice -> expandAndAddVariantGroup(expandedItems, itemPrice.getId()));
		}

		List<String> excludes = Text.fromCSV(config.excludeItems());
		for (String name : excludes) {
			if (name.isEmpty()) continue;

			itemManager.search(name).stream()
				.filter(itemPrice -> itemPrice.getName().equalsIgnoreCase(name))
				.forEach(itemPrice -> expandAndRemoveVariantGroup(expandedItems, itemPrice.getId()));
		}
	}

	private void expandAndAddVariantGroup(Set<Integer> itemSet, int itemId) {
		itemSet.add(itemId);
		int baseId = ItemVariationMapping.map(itemId);
		Collection<Integer> variations = ItemVariationMapping.getVariations(baseId);
		if (variations != null) {
			itemSet.addAll(variations);
		}
	}

	private void expandAndRemoveVariantGroup(Set<Integer> itemSet, int itemId) {
		itemSet.remove(itemId);
		int baseId = ItemVariationMapping.map(itemId);
		Collection<Integer> variations = ItemVariationMapping.getVariations(baseId);
		if (variations != null) {
			itemSet.removeAll(variations);
		}
	}
}