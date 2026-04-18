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

import com.frequentlytradeditemstab.PluginConstants;
import static com.frequentlytradeditemstab.PluginConstants.BankUI.ITEMS_PER_ROW;
import static com.frequentlytradeditemstab.PluginConstants.BankUI.ITEM_HEIGHT;
import static com.frequentlytradeditemstab.PluginConstants.BankUI.ITEM_WIDTH;
import static com.frequentlytradeditemstab.PluginConstants.BankUI.MAX_ITEM_ID;
import static com.frequentlytradeditemstab.PluginConstants.BankUI.ITEM_START_X;
import static com.frequentlytradeditemstab.PluginConstants.BankUI.ITEM_START_Y;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;

@Slf4j
@Singleton
public class FrequentlyTradedBankFilter {
	private final Client client;
	private final ItemManager itemManager;
	private final ClientThread clientThread; // NEW: Added ClientThread

	private final boolean[] isFrequentlyTraded = new boolean[MAX_ITEM_ID];

	@Inject
	public FrequentlyTradedBankFilter(Client client, ItemManager itemManager, ClientThread clientThread) {
		this.client = client;
		this.itemManager = itemManager;
		this.clientThread = clientThread;
	}

	@Getter
	private boolean isFilterActive = false;

	public void setFilterActive(boolean active) {
		this.isFilterActive = active;
	}

	/**
	 * Updates the list of items that is considered a frequently traded.
	 */
	public void updateFrequentItems(Iterable<Integer> itemIds) {
		Arrays.fill(isFrequentlyTraded, false);

		for (int itemId : itemIds) {
			if (itemId >= 0 && itemId < isFrequentlyTraded.length) {
				isFrequentlyTraded[itemId] = true;
			}
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event) {
		if (event.getScriptId() != ScriptID.BANKMAIN_FINISHBUILDING) {
			return;
		}

		if (!isFilterActive) {
			return;
		}

		Widget itemContainer = client.getWidget(PluginConstants.BankUI.ITEM_CONTAINER_ID);
		if (itemContainer == null) {
			return;
		}

		Widget[] children = itemContainer.getDynamicChildren();
		if (children == null) {
			return;
		}

		int x = 0;
		int y = 0;

		for (Widget child : children) {
			if (child == null || child.isHidden()) continue;

			int itemId = itemManager.canonicalize(child.getItemId());

			if (itemId >= 0 && itemId < MAX_ITEM_ID && isFrequentlyTraded[itemId]) {
				child.setOriginalX(ITEM_START_X + (x * ITEM_WIDTH));
				child.setOriginalY(ITEM_START_Y + (y * ITEM_HEIGHT));
				child.revalidate();

				x++;
				if (x == ITEMS_PER_ROW) {
					x = 0;
					y++;
				}
			} else {
				child.setHidden(true);
			}
		}

		int totalRows = (x == 0) ? y : y + 1;
		int newScrollHeight = ITEM_START_Y + (totalRows * ITEM_HEIGHT);
		itemContainer.setScrollHeight(Math.max(newScrollHeight, itemContainer.getHeight()));

		clientThread.invokeLater(() -> {
			client.runScript(ScriptID.UPDATE_SCROLLBAR, PluginConstants.BankUI.SCROLLBAR_ID, itemContainer.getId(), newScrollHeight);
		});
	}
}