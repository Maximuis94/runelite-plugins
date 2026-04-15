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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.gameval.ItemID;

@Slf4j
@Singleton
public class EyeOfAyakParser extends AbstractItemChargeParser
{
	private static final String itemMsgName = "Eye of Ayak";
	private static final Pattern CHARGED_TEARS_PATTERN = Pattern.compile("^The Eye of Ayak has been charged with demon tears\\. It currently has ([\\d,]+) charges\\.");
	private static final Pattern CHARGED_RUNES_PATTERN = Pattern.compile("^The Eye of Ayak has been charged with runes\\. It currently has ([\\d,]+) charges\\.");
	private static final Pattern UPDATE_PATTERN = Pattern.compile("^The Eye of Ayak has ([\\d,]+) charges remaining\\.");
	private static final String UNCHARGE_MESSAGE = "You uncharge the Eye of Ayak.";

	private AyakState ayakState = new AyakState();

	private static class AyakState {
		ItemCharge currentType;
		int count;
	}

	@Override
	protected void loadSessionData(File cacheFile)
	{
		AyakState loadedState = fileIOService.readJson(cacheFile, AyakState.class);
		if (loadedState != null) {
			this.ayakState = loadedState;
		}
	}

	@Override
	protected void saveChargesToDisk()
	{
		if (hasValidAccountHash)
		{
			fileIOService.writeJson(vaultFile, ayakState);
		}
	}

	@Override
	protected EquipmentInventorySlot getEquipmentSlot()
	{
		return EquipmentInventorySlot.WEAPON;
	}

	@Override
	protected int getBaseItemId()
	{
		return ItemID.EYE_OF_AYAK;
	}

	@Override
	protected Integer parseChargeCount(String message)
	{
		if (!message.contains(itemMsgName))
		{
			return null;
		}

		if (message.equals(UNCHARGE_MESSAGE))
		{
			ayakState.currentType = null;
			ayakState.count = 0;
			return 0;
		}

		Matcher tearMatcher = CHARGED_TEARS_PATTERN.matcher(message);
		if (tearMatcher.find())
		{
			ayakState.currentType = ItemCharge.EYE_OF_AYAK_TEARS;
			ayakState.count = cleanAndParseInt(tearMatcher.group(1));
			return ayakState.count;
		}

		Matcher runeMatcher = CHARGED_RUNES_PATTERN.matcher(message);
		if (runeMatcher.find())
		{
			ayakState.currentType = ItemCharge.EYE_OF_AYAK_RUNES;
			ayakState.count = cleanAndParseInt(runeMatcher.group(1));
			return ayakState.count;
		}

		Matcher updateMatcher = UPDATE_PATTERN.matcher(message);
		if (updateMatcher.find())
		{
			ayakState.count = cleanAndParseInt(updateMatcher.group(1));

			if (ayakState.currentType == null)
			{
				log.debug("Eye of Ayak charge update received, but charge type is unknown. Defaulting to Demon Tears for export.");
			}
			return ayakState.count;
		}

		return null;
	}

	private Integer cleanAndParseInt(String amount)
	{
		try
		{
			return Integer.parseInt(amount.replace(",", ""));
		}
		catch (NumberFormatException e)
		{
			log.error("Failed to parse Eye of Ayak charge string: {}", amount, e);
			return 0;
		}
	}

	@Override
	protected @NonNull ItemCharge getItemChargeType()
	{
		return ItemCharge.EYE_OF_AYAK;
	}

	/**
	 * Return the relevant ItemCharge, given previous checks. Defaults to demon tears.
	 */
	private @NonNull ItemCharge getRelevantChargeType()
	{
		return ayakState.currentType != null ? ayakState.currentType : ItemCharge.EYE_OF_AYAK_TEARS;
	}

	@Override
	public List<BankedItem> parseVault()
	{
		List<BankedItem> items = new ArrayList<>();

		if (ayakState.count > 0 && isEnabled)
		{
			ItemCharge exportType = getRelevantChargeType();

			for (Map.Entry<Integer, Integer> entry : exportType.getInputItem().entrySet())
			{
				int inputItemId = entry.getKey();
				int quantityPerBatch = entry.getValue();

				int totalQty = ayakState.count * quantityPerBatch / exportType.getNCharges();

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
		return items;
	}

	@Override
	public List<BankedItem> parseOfflineFile(long accountHash, File vaultFile)
	{
		AyakState offlineState = fileIOService.readJson(vaultFile, AyakState.class);
		if (offlineState == null || offlineState.count <= 0)
		{
			return new ArrayList<>();
		}
		ItemCharge chargeType = offlineState.currentType != null
			? offlineState.currentType
			: ItemCharge.EYE_OF_AYAK_TEARS;
		return convertChargesToBankedItems(accountHash, offlineState.count, chargeType);
	}
}