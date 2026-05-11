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

import com.datalogger.events.AccountSessionStarted;
import com.datalogger.loggers.ItemVaultLogger;
import com.datalogger.models.enums.VaultType;
import com.datalogger.models.itemvault.BankedItem;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import static net.runelite.api.gameval.VarbitID.TOB_LOBBY_WELL_CONTENTS;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class ItemVaultParser
{
	private long accountHash = -1;
	private String accountName;
	private boolean isMembersWorld;

	@Inject private Client client;
	@Inject private ItemVaultLogger itemVaultLogger;

	@Subscribe
	private void onAccountSessionStarted(AccountSessionStarted event)
	{
		accountHash = event.getAccountHash();
		accountName = event.getAccountName();
		isMembersWorld = event.isOnMembersWorld();
		log.debug("BankParser received account session data for: {} and is using a {}members slot parser", accountName, isMembersWorld ? "" : "non-");
	}

	private BankedItem parseSlotFree(VaultType vaultType, Widget slot)
	{
		return new BankedItem(
			vaultType,
			accountHash,
			accountName,
			slot.getItemId(),
			Text.removeTags(slot.getName()).replace(" (Members)", ""),
			slot.getItemQuantity()
		);
	}

	private BankedItem parseSlotMembers(VaultType vaultType, Widget slot)
	{
		return new BankedItem(
			vaultType,
			accountHash,
			accountName,
			slot.getItemId(),
			Text.removeTags(slot.getName()),
			slot.getItemQuantity()
		);
	}

	private List<BankedItem> parseVault(VaultType vaultType)
	{
		Widget widget = client.getWidget(vaultType.getGroupId(), vaultType.getChildId());
		List<BankedItem> parsedItems = new ArrayList<>();

		if (widget == null || widget.getChildren() == null)
		{
			log.debug("Unable to extract or parse widget for vault {}", vaultType.name());
			return parsedItems;
		}
		if (isMembersWorld)
		{
			for (Widget w : widget.getChildren())
			{
				if (w == null || w.getItemId() <= 0 || w.getItemQuantity() <= 0)
				{
					continue;
				}
				parsedItems.add(parseSlotFree(vaultType, w));
			}
		} else {
			for (Widget w : widget.getChildren())
			{
				if (w == null || w.getItemId() <= 0 || w.getItemQuantity() <= 0)
				{
					continue;
				}
				parsedItems.add(parseSlotMembers(vaultType, w));
			}
		}

		log.debug("Parsed a total of {} different items for vault {}", parsedItems.size(), vaultType.name());
		return parsedItems;
	}

	private void processWidget(int groupId)
	{
		VaultType vaultType = VaultType.byGroupId(groupId);
		if (vaultType == null) return;

		if (accountHash == -1) {
			log.warn("Bank loaded but account hash is not yet resolved. Skipping.");
			return;
		}

		List<BankedItem> vault = parseVault(vaultType);
		itemVaultLogger.logVault(accountHash, accountName, vaultType, vault);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event) {
		processWidget(event.getGroupId());
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event) {
		processWidget(event.getGroupId());
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int varbitId = event.getVarbitId();
		switch (varbitId)
		{
			case TOB_LOBBY_WELL_CONTENTS:
		}
	}
}