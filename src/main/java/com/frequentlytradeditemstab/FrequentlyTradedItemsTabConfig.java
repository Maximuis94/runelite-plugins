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

package com.frequentlytradeditemstab;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(FrequentlyTradedItemsTabConfig.GROUP)
public interface FrequentlyTradedItemsTabConfig extends Config
{
	String GROUP = "frequentlytradeditems";

	@ConfigItem(
		keyName = "includeSales",
		name = "Include Sales",
		description = "Include items that you have frequently sold on the Grand Exchange.",
		position = 1
	)
	default boolean includeSales()
	{
		return true;
	}

	@ConfigItem(
		keyName = "includePurchases",
		name = "Include Purchases",
		description = "Include items that you have frequently bought on the Grand Exchange.",
		position = 2
	)
	default boolean includePurchases()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showPricesTooltip",
		name = "Show Prices Tooltip",
		description = "Show GE price tooltips when hovering over unowned placeholder items in the filtered tab.",
		position = 3
	)
	default boolean showPricesTooltip()
	{
		return true;
	}

	@ConfigItem(
		keyName = "includeItems",
		name = "Include Items",
		description = "Comma-separated list of exact item names/ids to always include in the tab (e.g., 'Shark, Prayer potion(4)'), provided the item is in the bank.",
		position = 4
	)
	default String includeItems()
	{
		return "";
	}

	@ConfigItem(
		keyName = "excludeItems",
		name = "Exclude Items",
		description = "Comma-separated list of exact item names/ids to always exclude from the tab.",
		position = 5
	)
	default String excludeItems()
	{
		return "";
	}

	@ConfigItem(
		keyName = "buyLimitThreshold",
		name = "Traded quantity threshold (* item buy limit)",
		description = "Multitude of an item's GE buy limit you must at least trade before it is considered to be frequently traded (e.g. 2 will have an item included if the traded quantity is equal to or greater than 2 times its buy limit).",
		position = 6
	)
	default double buyLimitThreshold()
	{
		return 1.0;
	}
}