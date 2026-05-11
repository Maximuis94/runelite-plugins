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

package com.datalogger.services.itemvault.itemcharge;

import com.datalogger.models.enums.ItemCharge;
import com.datalogger.models.enums.VaultType;
import com.datalogger.models.itemvault.BankedItem;
import com.datalogger.services.InventoryStateManager;
import com.datalogger.services.itemvault.AbstractVaultParser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.Text;

/**
 * Template class for tracking item-specific charges. Charges covered by subclasses are tied to a specific item, e.g. a
 * Trident of the seas, as one can have multiple tridents that each have their own, separate count of item charges.
 * However, an assumption that is made, is that someone has only one of each item that can hold charges.
 * Item charge parsers only use chat messages to update the charge count.
 */
@Slf4j
public abstract class AbstractItemChargeParser extends AbstractVaultParser
{
	@Inject protected ItemManager itemManager;
	@Inject protected InventoryStateManager inventoryStateManager;

	protected int currentCharges = -1;

	protected abstract int getBaseItemId();

	/**
	 * Define an array of starting strings that indicate a message belongs to this parser.
	 * Acts as an ultra-fast O(1) gatekeeper before complex parsing occurs.
	 */
	protected abstract String[] getMessagePrefixes();

	protected abstract Integer parseChargeCount(String message);

	@NonNull protected abstract ItemCharge getItemChargeType();

	@Override
	public VaultType getVaultType()
	{
		return VaultType.ITEM_CHARGES;
	}

	@Override
	public String getVaultLabel()
	{
		return getItemChargeType().name();
	}

	@Override
	protected void loadSessionData(File cacheFile)
	{
		List<BankedItem> loadedItems = fileIOService.readJson(cacheFile, BankedItem.LIST_TYPE);

		if (loadedItems == null || loadedItems.isEmpty())
		{
			currentCharges = -1;
			return;
		}

		BankedItem item = loadedItems.get(0);
		ItemCharge chargeType = getItemChargeType();
		int batchSize = chargeType.getNCharges();

		Integer qtyPerBatch = chargeType.getOutputItem().get(item.getItemId());

		if (qtyPerBatch != null && qtyPerBatch > 0)
		{
			currentCharges = (int) (item.getQuantity() * batchSize / qtyPerBatch);
			log.debug("Restored {} charges from file based on item {}", currentCharges, item.getItemName());
		}
		else
		{
			currentCharges = -1;
		}
	}

	@Override
	public List<BankedItem> parseVault()
	{
		if (currentCharges > 0 && isEnabled)
		{
			return convertChargesToBankedItems(currentAccountHash, currentCharges, getItemChargeType());
		}
		return new ArrayList<>();
	}

	/**
	 * Gatekeeper function that returns true if the message is to be ignored
	 */
	private boolean skipMessage(ChatMessage event)
	{
		if (!isEnabled) return true;
		if (!isRelevantChatMessage(event.getType())) return true;
		return !inventoryStateManager.hasBaseItem(getBaseItemId());
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (skipMessage(event)) return;

		String cleanMessage = Text.removeTags(event.getMessage());
		boolean matchesPrefix = false;

		for (String prefix : getMessagePrefixes())
		{
			if (cleanMessage.startsWith(prefix))
			{
				matchesPrefix = true;
				break;
			}
		}
		if (!matchesPrefix) return;

		ensureAccountNameIsCached();

		Integer parsedCharges = parseChargeCount(cleanMessage);
		if (parsedCharges != null)
		{
			setItemCharges(parsedCharges);
		}
	}

	/**
	 * Sets the number of item charges to nCharges.
	 */
	public void setItemCharges(int nCharges)
	{
		log.debug("ItemCharges for {} set to {}", getItemChargeType().name(), currentCharges);
		this.currentCharges = nCharges;
		saveChargesToDisk();
	}

	protected boolean isRelevantChatMessage(ChatMessageType messageType)
	{
		return messageType == ChatMessageType.GAMEMESSAGE ||
			messageType == ChatMessageType.MESBOX;
	}

	protected void saveChargesToDisk()
	{
		if (hasValidAccountHash && currentCharges >= 0)
		{
			List<BankedItem> itemsToSave = convertChargesToBankedItems(currentAccountHash, currentCharges, getItemChargeType());
//			fileIOService.writeJson(vaultFile, itemsToSave);
			saveSlimVaultCache(itemsToSave);
		}
	}

	@Override
	protected File getInternalVaultFile()
	{
		return fileIOService.getInternalVaultFile(getItemChargeType());
	}

	@Override
	public String getFilePrefix()
	{
		return getItemChargeType().fileNameString();
	}

	/**
	 * Shared logic to convert a raw charge count into a list of refunded items
	 * based on the ItemCharge enum's outputItem mapping.
	 */
	protected List<BankedItem> convertChargesToBankedItems(long hash, int charges, ItemCharge type)
	{
		List<BankedItem> items = new ArrayList<>();

		String resolvedAccountName = accountHashMapper.getAccountName(hash);
		int batchSize = type.getNCharges();

		if (type.getOutputItem() != null)
		{
			for (Map.Entry<Integer, Integer> entry : type.getOutputItem().entrySet())
			{
				int itemId = entry.getKey();
				int qtyPerBatch = entry.getValue();

				long totalQty = (long) charges * qtyPerBatch / batchSize;

				if (totalQty > 0)
				{
					String itemName = itemManager.getItemComposition(itemId).getName();
					items.add(new BankedItem(
						getVaultType(),
						hash,
						resolvedAccountName,
						itemId,
						itemName,
						totalQty
					));
				}
			}
		}
		return items;
	}
}