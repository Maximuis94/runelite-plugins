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
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class WarpedSceptreParser extends AbstractItemChargeParser
{
	private static final int BASE_ID = ItemVariationMapping.map(ItemID.WARPED_SCEPTRE);

	private static final String UNCHARGE_PREFIX = "You uncharge your warped sceptre";
	private static final String CHECK_UPDATE_PREFIX = "Your warped sceptre has ";
	private static final String CHARGE_PREFIX = "You add ";
	private static final String CHARGE_TARGET = "warped sceptre";

	@Override
	protected int getBaseItemId()
	{
		return BASE_ID;
	}

	@Override
	protected @NonNull ItemCharge getItemChargeType()
	{
		return ItemCharge.WARPED_SCEPTRE;
	}

	@Override
	protected String[] getMessagePrefixes()
	{
		return new String[] {
			UNCHARGE_PREFIX,
			CHECK_UPDATE_PREFIX,
			CHARGE_PREFIX
		};
	}

	@Override
	protected Integer parseChargeCount(String rawMessage)
	{
		String message = Text.removeTags(rawMessage);

		if (message.startsWith(UNCHARGE_PREFIX))
		{
			return 0;
		}

		if (message.startsWith(CHECK_UPDATE_PREFIX))
		{
			int endIndex = message.indexOf(" charges remaining.", CHECK_UPDATE_PREFIX.length());
			if (endIndex != -1)
			{
				String numberStr = message.substring(CHECK_UPDATE_PREFIX.length(), endIndex);
				return cleanAndParseInt(numberStr);
			}
		}

		if (message.startsWith(CHARGE_PREFIX) && message.contains(CHARGE_TARGET))
		{
			int endIndex = message.indexOf(" charges to", CHARGE_PREFIX.length());
			if (endIndex != -1)
			{
				String numberStr = message.substring(CHARGE_PREFIX.length(), endIndex);
				Integer addedCharges = cleanAndParseInt(numberStr);

				if (addedCharges != null && this.currentCharges >= 0)
				{
					return this.currentCharges + addedCharges;
				}
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
			log.error("Failed to parse Warped Sceptre charge string: {}", amount, e);
			return null;
		}
	}
}