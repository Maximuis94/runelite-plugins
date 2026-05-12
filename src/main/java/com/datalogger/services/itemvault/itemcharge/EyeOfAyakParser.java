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
import com.datalogger.models.itemvault.BankedItem;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.game.ItemVariationMapping;

@Slf4j
@Singleton
public class EyeOfAyakParser extends AbstractItemChargeParser
{
	private static final int BASE_ID = ItemVariationMapping.map(ItemID.EYE_OF_AYAK);

	private static final String UNCHARGE_MESSAGE = "You uncharge the Eye of Ayak.";
	private static final String UMBRELLA_PREFIX = "The Eye of Ayak has ";
	private static final String TEARS_PREFIX = "The Eye of Ayak has been charged with demon tears. It currently has ";
	private static final String RUNES_PREFIX = "The Eye of Ayak has been charged with runes. It currently has ";
	private static final String CHARGES_SUFFIX = " charges.";
	private static final String REMAINING_SUFFIX = " charges remaining.";

	private ItemCharge currentType = null;

	@Override
	protected int getBaseItemId()
	{
		return BASE_ID;
	}

	@Override
	protected @NonNull ItemCharge getItemChargeType()
	{
		return ItemCharge.EYE_OF_AYAK;
	}

	@Override
	protected String[] getMessagePrefixes()
	{
		return new String[] {
			UNCHARGE_MESSAGE,
			UMBRELLA_PREFIX
		};
	}

	@Override
	protected Integer parseChargeCount(String message)
	{
		if (message.equals(UNCHARGE_MESSAGE))
		{
			this.currentType = null;
			return 0;
		}

		if (message.startsWith(TEARS_PREFIX))
		{
			int endIndex = message.indexOf(CHARGES_SUFFIX, TEARS_PREFIX.length());
			if (endIndex != -1)
			{
				this.currentType = ItemCharge.EYE_OF_AYAK_TEARS;
				return cleanAndParseInt(message.substring(TEARS_PREFIX.length(), endIndex));
			}
		}

		if (message.startsWith(RUNES_PREFIX))
		{
			int endIndex = message.indexOf(CHARGES_SUFFIX, RUNES_PREFIX.length());
			if (endIndex != -1)
			{
				this.currentType = ItemCharge.EYE_OF_AYAK_RUNES;
				return cleanAndParseInt(message.substring(RUNES_PREFIX.length(), endIndex));
			}
		}

		if (message.startsWith(UMBRELLA_PREFIX))
		{
			int endIndex = message.indexOf(REMAINING_SUFFIX, UMBRELLA_PREFIX.length());
			if (endIndex != -1)
			{
				if (this.currentType == null)
				{
					log.debug("Eye of Ayak charge update received, but charge type is unknown. Defaulting to Demon Tears for export.");
				}
				return cleanAndParseInt(message.substring(UMBRELLA_PREFIX.length(), endIndex));
			}
		}

		return null;
	}

	private Integer cleanAndParseInt(String amount)
	{
		try
		{
			return Integer.parseInt(amount.replace(",", "").trim());
		}
		catch (NumberFormatException e)
		{
			log.error("Failed to parse Eye of Ayak charge string: {}", amount, e);
			return null;
		}
	}

	/**
	 * Return the relevant ItemCharge. Defaults to demon tears.
	 */
	private @NonNull ItemCharge getRelevantChargeType()
	{
		return this.currentType != null ? this.currentType : ItemCharge.EYE_OF_AYAK_TEARS;
	}

	@Override
	protected void loadSessionData(File cacheFile)
	{
		List<BankedItem> loadedItems = fileIOService.readJson(cacheFile, BankedItem.LIST_TYPE);

		if (loadedItems == null || loadedItems.isEmpty())
		{
			this.currentType = null;
			this.currentCharges = -1;
			return;
		}

		BankedItem item = loadedItems.get(0);
		int itemId = item.getItemId();

		if (itemId == ItemID.DEMON_TEAR)
		{
			this.currentType = ItemCharge.EYE_OF_AYAK_TEARS;
			this.currentCharges = (int) item.getQuantity();
		}
		else if (itemId == ItemID.CHAOSRUNE || itemId == ItemID.DEATHRUNE)
		{
			this.currentType = ItemCharge.EYE_OF_AYAK_RUNES;

			if (itemId == ItemID.CHAOSRUNE)
			{
				this.currentCharges = (int) item.getQuantity();
			}
			else
			{
				this.currentCharges = (int) (item.getQuantity() / 2);
			}
		}
		else
		{
			this.currentType = null;
			this.currentCharges = -1;
		}

		log.debug("Restored {} Eye of Ayak charges of type {}", currentCharges, currentType);
	}

	@Override
	public List<BankedItem> parseVault()
	{
		List<BankedItem> items = new ArrayList<>();

		if (currentCharges > 0 && isEnabled)
		{
			ItemCharge exportType = getRelevantChargeType();

			if (exportType.getInputItem() != null)
			{
				for (Map.Entry<Integer, Integer> entry : exportType.getInputItem().entrySet())
				{
					int inputItemId = entry.getKey();
					int quantityPerBatch = entry.getValue();

					long totalQty = (long) currentCharges * quantityPerBatch / exportType.getNCharges();

					if (totalQty > 0)
					{
						String itemName = itemManager.getItemComposition(inputItemId).getName();

						items.add(new BankedItem(
							getVaultType(),
							currentAccountHash,
							currentAccountName,
							inputItemId,
							itemName,
							totalQty
						));
					}
				}
			}
		}
		return items;
	}

	@Override
	protected void saveChargesToDisk()
	{
		if (hasValidAccountHash && currentCharges >= 0)
		{
			List<BankedItem> itemsToSave = parseVault();
			fileIOService.writeJson(vaultFile, itemsToSave);
		}
	}
}