/*
 * Copyright (c) 2026, maximuis94 <https://github.com/maximuis94>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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

@Slf4j
@Singleton
public class AccursedSceptreParser extends AbstractItemChargeParser
{
	private static final int BASE_ID = ItemVariationMapping.map(ItemID.WILD_CAVE_ACCURSED_CHARGED);

	private static final String CHECK_PREFIX = "Your sceptre has ";
	private static final String UPDATE_PREFIX = "Your weapon has ";
	private static final String CHARGE_PREFIX = "You add ";

	private static final String CHARGE_TOTAL_PHRASE = " giving it a total of ";

	@Override
	protected int getBaseItemId()
	{
		return BASE_ID;
	}

	@Override
	protected @NonNull ItemCharge getItemChargeType()
	{
		return ItemCharge.ACCURSED_SCEPTRE;
	}

	@Override
	protected String[] getMessagePrefixes()
	{
		return new String[] {
			CHECK_PREFIX,
			UPDATE_PREFIX,
			CHARGE_PREFIX
		};
	}

	@Override
	protected Integer parseChargeCount(String message)
	{
		if (message.startsWith(CHECK_PREFIX))
		{
			int endIndex = message.indexOf(" charges left", CHECK_PREFIX.length());
			if (endIndex != -1)
			{
				String numberStr = message.substring(CHECK_PREFIX.length(), endIndex);
				return cleanAndParseInt(numberStr);
			}
		}

		if (message.startsWith(UPDATE_PREFIX))
		{
			int endIndex = message.indexOf(" charges remaining", UPDATE_PREFIX.length());
			if (endIndex != -1)
			{
				String numberStr = message.substring(UPDATE_PREFIX.length(), endIndex);
				return cleanAndParseInt(numberStr);
			}
		}

		if (message.startsWith(CHARGE_PREFIX) && message.contains(CHARGE_TOTAL_PHRASE))
		{
			int startIndex = message.indexOf(CHARGE_TOTAL_PHRASE) + CHARGE_TOTAL_PHRASE.length();

			int endIndex = message.indexOf(" charge", startIndex);
			if (endIndex != -1)
			{
				String numberStr = message.substring(startIndex, endIndex);
				return cleanAndParseInt(numberStr);
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
			log.error("Failed to parse Accursed sceptre charge string: {}", amount, e);
			return null;
		}
	}
}