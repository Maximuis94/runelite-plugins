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

package com.frequentlytradeditemstab.ui;

import com.frequentlytradeditemstab.FrequentlyTradedItemsTabConfig;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.MenuEntry;
import net.runelite.api.gameval.InterfaceID;
import static net.runelite.api.gameval.InterfaceID.BANKMAIN;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.QuantityFormatter;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class FrequentlyTradedTooltipOverlay extends Overlay {

	private final Client client;
	private final TooltipManager tooltipManager;
	private final ItemManager itemManager;
	private final FrequentlyTradedItemsTabConfig config;
	private final FrequentlyTradedBankFilter bankFilter;

	private static final Color ITEM_NAME_COLOR = new Color(230, 140, 75);

	@Inject
	public FrequentlyTradedTooltipOverlay(Client client, TooltipManager tooltipManager, ItemManager itemManager,
										  FrequentlyTradedItemsTabConfig config, FrequentlyTradedBankFilter bankFilter) {
		setPosition(OverlayPosition.DYNAMIC);
		this.client = client;
		this.tooltipManager = tooltipManager;
		this.itemManager = itemManager;
		this.config = config;
		this.bankFilter = bankFilter;
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		if (client.isMenuOpen() || !config.showPricesTooltip() || !bankFilter.isFilterActive()) {
			return null;
		}

		MenuEntry[] menuEntries = client.getMenu().getMenuEntries();
		if (menuEntries.length == 0) {
			return null;
		}

		MenuEntry entry = menuEntries[menuEntries.length - 1];
		int widgetId = entry.getParam1();
		int groupId = WidgetUtil.componentToInterface(widgetId);

		if (groupId != BANKMAIN) {
			return null;
		}

		Widget container = client.getWidget(widgetId);
		if (container == null) {
			return null;
		}

		int index = entry.getParam0();
		Widget itemWidget = container.getChild(index);

		// We only care about unowned placeholders (quantity == 0)
		if (itemWidget == null || itemWidget.getItemQuantity() > 0) {
			return null;
		}

		int itemId = itemWidget.getItemId();
		if (itemId <= 0) {
			return null;
		}

		ItemComposition itemDef = itemManager.getItemComposition(itemId);
		int realItemId = itemId;

		// Resolve placeholder IDs to their canonical item IDs
		if (itemDef.getPlaceholderTemplateId() != -1) {
			realItemId = itemDef.getPlaceholderId();
		}

		// Fetch the actively traded GE Price
		int gePrice = itemManager.getItemPrice(realItemId);
		if (gePrice <= 0) {
			return null;
		}

		String tooltipStr = ColorUtil.wrapWithColorTag(itemDef.getName(), ITEM_NAME_COLOR) +
			"</br>Actively traded price: " +
			ColorUtil.wrapWithColorTag(QuantityFormatter.quantityToStackSize(gePrice) + " gp", Color.LIGHT_GRAY);

		tooltipManager.add(new Tooltip(tooltipStr));

		return null;
	}
}