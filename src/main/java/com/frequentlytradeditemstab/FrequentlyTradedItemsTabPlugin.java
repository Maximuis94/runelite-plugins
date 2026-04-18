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

import com.frequentlytradeditemstab.services.GeHistoryCacheManager;
import com.frequentlytradeditemstab.services.GeHistoryRecorder;
import com.frequentlytradeditemstab.services.GeTradeRecorder;
import com.frequentlytradeditemstab.services.TradeCacheManager;
import com.frequentlytradeditemstab.services.TradeFrequencyAnalyzer;
import com.frequentlytradeditemstab.ui.FrequentlyTradedBankFilter;
import com.frequentlytradeditemstab.ui.FrequentlyTradedTooltipOverlay;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import static net.runelite.api.ScriptID.BANKMAIN_BUILD;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(name = PluginConstants.PLUGIN_NAME)
public class FrequentlyTradedItemsTabPlugin extends Plugin {

	@Inject private OverlayManager overlayManager;
	@Inject private FrequentlyTradedTooltipOverlay tooltipOverlay;

	@Inject private EventBus eventBus;
	@Inject private ClientThread clientThread;

	@Inject private TradeFrequencyAnalyzer analyzer;
	@Inject private FrequentlyTradedBankFilter bankFilter;
	@Inject private TradeCacheManager tradeCacheManager;
	@Inject private GeHistoryCacheManager historyCacheManager;
	@Inject private GeTradeRecorder tradeRecorder;
	@Inject private GeHistoryRecorder historyRecorder;
	@Inject private ClientToolbar clientToolbar;
	@Inject private Client client;

	@Provides
	FrequentlyTradedItemsTabConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(FrequentlyTradedItemsTabConfig.class);
	}

	@Override
	protected void startUp() {
		eventBus.register(tradeRecorder);
		eventBus.register(historyRecorder);
		eventBus.register(bankFilter);
		overlayManager.add(tooltipOverlay);

		bankFilter.setFilterActive(false);
		reloadFilterData();
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event) {
		if (event.getGroupId() == InterfaceID.BANKMAIN) {
			createBankToggleButton();
		}
	}

	private void createBankToggleButton() {
		Widget parent = client.getWidget(PluginConstants.BankUI.BANK_TITLE_BAR_ID);
		if (parent == null) return;

		Widget toggleBtn = parent.createChild(-1, WidgetType.GRAPHIC);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");

		toggleBtn.setSpriteId(SpriteID.GE_BACKBUTTON);
		toggleBtn.setOriginalX(480);
		toggleBtn.setOriginalY(4);
		toggleBtn.setOriginalWidth(20);
		toggleBtn.setOriginalHeight(20);
		toggleBtn.setHasListener(true);
		toggleBtn.setNoClickThrough(true);
		toggleBtn.setName("<col=ff9040>Frequently Traded Items Tab</col>");
		toggleBtn.setAction(0, "Toggle");

		// Define what happens when clicked
		toggleBtn.setOnOpListener((JavaScriptCallback) ev -> toggleFilter());
		toggleBtn.setAction(0, "Toggle Filter");

		toggleBtn.revalidate();
	}

	private void toggleFilter() {
		boolean newState = !bankFilter.isFilterActive();
		bankFilter.setFilterActive(newState);

		clientThread.invokeLater(() -> {
			if (client.getItemContainer(InventoryID.BANK) != null) {
				client.runScript(BANKMAIN_BUILD);
			}
		});
	}
	@Override
	protected void shutDown() {
		eventBus.unregister(tradeRecorder);
		eventBus.unregister(historyRecorder);
		eventBus.unregister(bankFilter);
		overlayManager.remove(tooltipOverlay);
		bankFilter.setFilterActive(false);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (event.getGroup().equals(PluginConstants.CONFIG_GROUP)) {
			reloadFilterData();
		}
	}

	/**
	 * Re-analyzes the cached data and pushes it to the UI thread.
	 * Call this whenever caches or configs are updated.
	 */
	public void reloadFilterData() {
		tradeCacheManager.loadTradesAsync()
			.thenApply(analyzer::analyze)
			.thenAccept(frequentItems -> {
				clientThread.invokeLater(() -> {
					bankFilter.updateFrequentItems(frequentItems);
				});
			})
			.exceptionally(ex -> {
				log.error("Failed to reload frequent items filter data", ex);
				return null;
			});
	}
}