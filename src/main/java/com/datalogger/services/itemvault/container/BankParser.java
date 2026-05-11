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

package com.datalogger.services.itemvault.container;

import static com.datalogger.constants.Item.InterfaceID.BANK_GROUP_ID;
import static com.datalogger.constants.Item.InterfaceID.BANK_OCCUPIED_SLOTS_CHILD_ID;
import com.datalogger.models.enums.VaultType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.Widget;

@Slf4j
@Singleton
public class BankParser extends AbstractContainerVaultParser
{
	private boolean isShowingFullBank = false;

	@Override
	public VaultType getVaultType()
	{
		return VaultType.BANK;
	}

	@Override
	protected int getContainerId()
	{
		return InventoryID.BANK;
	}

	@Override
	protected boolean isValidStateToSave(ItemContainer container)
	{
		Widget itemCountWidget = client.getWidget(BANK_GROUP_ID, BANK_OCCUPIED_SLOTS_CHILD_ID);

		if (itemCountWidget == null || itemCountWidget.isHidden())
		{
			return isShowingFullBank;
		}

		String text = itemCountWidget.getText();

		Matcher matcher = Pattern.compile("^(\\d+)").matcher(text);

		if (matcher.find())
		{
			int expectedBankSize = Integer.parseInt(matcher.group(1));
			int actualOccupiedSlots = 0;

			for (Item item : container.getItems())
			{
				if (item.getId() > 0)
				{
					actualOccupiedSlots++;
				}
			}

			if (actualOccupiedSlots != expectedBankSize)
			{
				isShowingFullBank = false;
				log.debug("Bank is currently filtered. Expected {} items, but found {}. Skipping save.", expectedBankSize, actualOccupiedSlots);
			}
			else
			{
				log.debug("Bank is currently not filtered. Expected {} items and found {}.", expectedBankSize, actualOccupiedSlots);
				isShowingFullBank = true;
			}
		}
		return isShowingFullBank;
	}
}