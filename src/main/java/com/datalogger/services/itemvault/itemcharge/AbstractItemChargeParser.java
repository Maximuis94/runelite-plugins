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
import com.datalogger.services.itemvault.AbstractVaultParser;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;

/**
 * Template class for tracking item-specific charges. Charges covered by subclasses are tied to a specific item, i.e. a
 * Trident of the seas, as one can have multiple tridents that each have their own, separate count of item charges.
 */
@Slf4j
public abstract class AbstractItemChargeParser extends AbstractVaultParser
{
	@Inject protected ItemManager itemManager;

	protected int currentCharges = -1;

	protected abstract EquipmentInventorySlot getEquipmentSlot();
	protected abstract int getBaseItemId();
	protected abstract Integer parseChargeCount(String message);
	@NonNull protected abstract ItemCharge getItemChargeType();

	@Override
	public VaultType getVaultType()
	{
		return VaultType.ITEM_CHARGES;
	}

	@Override
	public String getVaultLabel() {
		return getItemChargeType().name();
	}


	@Override
	protected void loadSessionData(File cacheFile)
	{
		Integer loadedCharges = fileIOService.readJson(cacheFile, Integer.class);
		currentCharges = (loadedCharges != null) ? loadedCharges : -1;
	}

	@Override
	public List<BankedItem> parseVault()
	{
		List<BankedItem> items = new ArrayList<>();
		if (currentCharges > 0 && isEnabled)
		{
			ItemCharge chargeType = getItemChargeType();

			if (chargeType.getInputItem() != null)
			{
				for (Map.Entry<Integer, Integer> entry : chargeType.getOutputItem().entrySet())
				{
					int outputItemId = entry.getKey();
					int quantityPerBatch = entry.getValue();

					int totalQty = currentCharges * quantityPerBatch / chargeType.getNCharges();

					if (totalQty > 0)
					{
						String itemName = itemManager.getItemComposition(outputItemId).getName();

						items.add(new BankedItem(
							getVaultType(),
							currentAccountHash,
							currentAccountName,
							outputItemId,
							itemName,
							totalQty
						));
					}
				}
			}
		}
		return items;
	}

//	@Override
//	public List<BankedItem> parseVault()
//	{
//		List<BankedItem> items = new ArrayList<>();
//		if (currentCharges > 0 && isEnabled)
//		{
//			String itemName = itemManager.getItemComposition(getBaseItemId()).getName();
//			items.add(new BankedItem(
//				getVaultType(),
//				currentAccountHash,
//				currentAccountName,
//				getBaseItemId(),
//				itemName,
//				currentCharges
//			));
//		}
//		return items;
//	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!isEnabled) return;
		if (!isRelevantChatMessage(event.getType())) return;
		if (!isItemCurrentlyPossessed()) return;

		ensureAccountNameIsCached();

		Integer parsedCharges = parseChargeCount(event.getMessage());
		if (parsedCharges != null)
		{
			this.currentCharges = parsedCharges;
			log.debug("Parsed {} charges: {}", getItemChargeType().name(), currentCharges);
			saveChargesToDisk();
		}
	}

	private boolean isRelevantChatMessage(ChatMessageType messageType)
	{
		return messageType == ChatMessageType.GAMEMESSAGE || messageType == ChatMessageType.SPAM;
	}

	private boolean isItemCurrentlyPossessed()
	{
		EquipmentInventorySlot slot = getEquipmentSlot();
		if (slot != null)
		{
			ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
			if (equipment != null)
			{
				Item baseItem = equipment.getItem(getEquipmentSlot().getSlotIdx());
				if (baseItem != null)
				{

					if (ItemVariationMapping.map(baseItem.getId()) == getBaseItemId())
					{
						return true;
					}
				}
			}
		}

		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null) return false;

		int baseTargetId = ItemVariationMapping.map(getBaseItemId());

		return Arrays.stream(inventory.getItems())
			.anyMatch(item -> ItemVariationMapping.map(item.getId()) == baseTargetId);
	}

	protected void saveChargesToDisk()
	{
		if (hasValidAccountHash)
		{
			fileIOService.writeJson(vaultFile, currentCharges);
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
		// Uses the helper method from ItemCharge.java
		return getItemChargeType().fileNameString();
	}

	@Override
	public List<BankedItem> parseOfflineFile(long accountHash, File vaultFile)
	{
		Integer charges = fileIOService.readJson(vaultFile, Integer.class);

		if (charges == null || charges <= 0)
		{
			return new ArrayList<>();
		}

		return convertChargesToBankedItems(accountHash, charges, getItemChargeType());
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
		return items;
	}
}