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

import static com.frequentlytradeditemstab.PluginConstants.BankUI.BANK_MARGIN_PADDING;
import static com.frequentlytradeditemstab.PluginConstants.BankUI.BTN_ORIGINAL_HEIGHT;
import static com.frequentlytradeditemstab.PluginConstants.BankUI.BTN_ORIGINAL_WIDTH;
import com.frequentlytradeditemstab.services.GeHistoryCacheManager;
import com.frequentlytradeditemstab.services.GeHistoryRecorder;
import com.frequentlytradeditemstab.services.GeTradeRecorder;
import com.frequentlytradeditemstab.services.TradeCacheManager;
import com.frequentlytradeditemstab.services.TradeFrequencyAnalyzer;
import com.frequentlytradeditemstab.ui.FrequentlyTradedBankFilter;
import com.frequentlytradeditemstab.ui.FrequentlyTradedTooltipOverlay;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ScriptID;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.gameval.InterfaceID;
import static net.runelite.api.gameval.ItemID.PLATINUM;
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
import net.runelite.client.ui.overlay.OverlayManager;

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
	@Inject private GeHistoryCacheManager geHistoryCacheManager;
	@Inject private GeTradeRecorder tradeRecorder;
	@Inject private GeHistoryRecorder historyRecorder;
	@Inject private FrequentlyTradedItemsTabConfig config;
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

	@Override
	protected void shutDown() {
		eventBus.unregister(tradeRecorder);
		eventBus.unregister(historyRecorder);
		eventBus.unregister(bankFilter);
		overlayManager.remove(tooltipOverlay);
		bankFilter.setFilterActive(false);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		if (event.getGameState() == GameState.LOGGED_IN) {
			reloadFilterData();
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event) {
		int scriptId = event.getScriptId();
		if (scriptId == ScriptID.BANKMAIN_INIT || scriptId == ScriptID.BANKMAIN_FINISHBUILDING) {
			createBankToggleButton();
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		if (!bankFilter.isFilterActive() || !config.deactivateAtTabSwitch()) {
			return;
		}
		String option = event.getMenuOption();
		if (option != null && (option.equals("View tab") || option.equals("View all items"))) {
			bankFilter.setFilterActive(false);
		}
	}

	private void createBankToggleButton() {
		Widget parent = client.getWidget(InterfaceID.BANKMAIN, 1);
		if (parent == null) return;

		int targetX = parent.getWidth() - BANK_MARGIN_PADDING - 51;
		int targetY = 80;

		Widget[] children = parent.getDynamicChildren();
		if (children != null) {
			for (Widget child : children) {
				if (child != null && child.getName() != null && child.getName().contains("Frequently Traded")) {

					if (child.getOriginalX() != targetX || child.getOriginalY() != targetY || child.isHidden()) {
						child.setOriginalX(targetX);
						child.setOriginalY(targetY);
						child.setHidden(false);
						child.revalidate();
					}

					Widget[] inner = child.getDynamicChildren();
					if (inner != null && inner.length > 0) {
						inner[0].setTextColor(bankFilter.isFilterActive() ? 0x5A5245 : 0x383023);
					}

					return;
				}
			}
		}

		Widget toggleBtn = parent.createChild(-1, WidgetType.LAYER);
		toggleBtn.setOriginalX(targetX);
		toggleBtn.setOriginalY(targetY);
		toggleBtn.setOriginalWidth(BTN_ORIGINAL_WIDTH);
		toggleBtn.setOriginalHeight(BTN_ORIGINAL_HEIGHT);

		toggleBtn.setHasListener(true);
		toggleBtn.setNoClickThrough(true);
		toggleBtn.setName("<col=ff9040>Frequently Traded Items Tab</col>");
		toggleBtn.setAction(0, "Toggle");
		toggleBtn.setOnOpListener((JavaScriptCallback) ev -> toggleFilter());

		Widget bg = toggleBtn.createChild(-1, WidgetType.RECTANGLE);
		bg.setOriginalX(0);
		bg.setOriginalY(0);
		bg.setOriginalWidth(BTN_ORIGINAL_WIDTH);
		bg.setOriginalHeight(BTN_ORIGINAL_HEIGHT);
		bg.setFilled(true);
		bg.setTextColor(bankFilter.isFilterActive() ? 0x5A5245 : 0x383023);
		bg.revalidate();

		Widget icon = toggleBtn.createChild(-1, WidgetType.GRAPHIC);
		icon.setOriginalX(2);
		icon.setOriginalY(2);
		icon.setOriginalWidth(BTN_ORIGINAL_WIDTH - 4);
		icon.setOriginalHeight(BTN_ORIGINAL_HEIGHT - 4);
		icon.setItemId(PLATINUM);
		icon.setItemQuantity(10000);
		icon.setItemQuantityMode(0);
		icon.revalidate();

		toggleBtn.revalidate();
	}

	private void toggleFilter() {
		boolean newState = !bankFilter.isFilterActive();
		bankFilter.setFilterActive(newState);

		clientThread.invokeLater(() -> {
			// 1. Update the button's background color visually
			Widget rootContainer = client.getWidget(InterfaceID.BANKMAIN, 1);
			if (rootContainer != null && rootContainer.getDynamicChildren() != null) {
				for (Widget child : rootContainer.getDynamicChildren()) {
					if (child != null && child.getName() != null && child.getName().contains("Frequently Traded")) {
						Widget[] innerChildren = child.getDynamicChildren();
						if (innerChildren != null && innerChildren.length > 0) {
							Widget bg = innerChildren[0];
							bg.setTextColor(newState ? 0x5A5245 : 0x383023);
						}
						break;
					}
				}
			}

			// 2. Just trigger the native bank rebuild script.
			// When it finishes, your BANKMAIN_FINISHBUILDING hook will fire,
			// and your filter logic will take over the screen!
			Widget bankContainer = client.getWidget(PluginConstants.BankUI.ITEM_CONTAINER_ID);
			if (bankContainer != null) {
				Object[] buildScriptArgs = bankContainer.getOnInvTransmitListener();
				if (buildScriptArgs != null) {
					client.runScript(buildScriptArgs);
				}
			}
		});
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
			.thenAccept(trades -> clientThread.invokeLater(() -> {
				try {
					var frequentItems = analyzer.analyzeTrades(trades);
					bankFilter.updateFrequentItems(frequentItems);
				} catch (Exception e) {
					log.error("Error analyzing frequent items on client thread", e);
				}
			}))
			.exceptionally(ex -> {
				log.error("Failed to load trades from cache", ex);
				return null;
			});
		geHistoryCacheManager.loadHistoryAsync()
			.thenAccept(entries -> clientThread.invokeLater(() -> {
				try {
					var frequentItems = analyzer.analyzeHistory(entries);
					bankFilter.updateFrequentItems(frequentItems);
				} catch (Exception e) {
					log.error("Error analyzing frequent items on client thread", e);
				}
			}))
			.exceptionally(ex -> {
				log.error("Failed to load trades from cache", ex);
				return null;
			});
	}
}