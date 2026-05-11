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

@Slf4j
@Singleton
public class TonalzticsOfRalosParser extends AbstractItemChargeParser
{
	private static final int BASE_ID = ItemVariationMapping.map(ItemID.TONALZTICS_OF_RALOS_CHARGED);

	private static final String UNCHARGE_MESSAGE = "You uncharge your Tonalztics of ralos";

	private static final String CHECK_UPDATE_PREFIX = "Your Tonalztics of ralos has ";
	private static final String CHECK_UPDATE_SUFFIX = " charges remaining.";

	private static final String CHARGE_UNCHARGED_PREFIX = "You apply ";
	private static final String CHARGE_UNCHARGED_SUFFIX = " charges to your Tonalztics of ralos.";

	private static final String CHARGE_CHARGED_PREFIX = "You apply an additional ";
	private static final String CHARGE_CHARGED_MID = "It now has ";
	private static final String CHARGE_CHARGED_SUFFIX = " charges in total.";

	@Override
	protected int getBaseItemId()
	{
		return BASE_ID;
	}

	@Override
	protected @NonNull ItemCharge getItemChargeType()
	{
		return ItemCharge.TONALZTICS_OF_RALOS;
	}

	@Override
	protected String[] getMessagePrefixes()
	{
		return new String[] {
			UNCHARGE_MESSAGE,
			CHECK_UPDATE_PREFIX,
			CHARGE_UNCHARGED_PREFIX,
			CHARGE_CHARGED_PREFIX
		};
	}

	@Override
	protected Integer parseChargeCount(String message)
	{
		if (message.startsWith(UNCHARGE_MESSAGE))
		{
			return 0;
		}

		if (message.startsWith(CHECK_UPDATE_PREFIX))
		{
			int endIndex = message.indexOf(CHECK_UPDATE_SUFFIX, CHECK_UPDATE_PREFIX.length());
			if (endIndex != -1)
			{
				String numberStr = message.substring(CHECK_UPDATE_PREFIX.length(), endIndex);
				return cleanAndParseInt(numberStr);
			}
		}

		if (message.startsWith(CHARGE_UNCHARGED_PREFIX) && message.contains(CHARGE_UNCHARGED_SUFFIX))
		{
			int endIndex = message.indexOf(CHARGE_UNCHARGED_SUFFIX, CHARGE_UNCHARGED_PREFIX.length());
			if (endIndex != -1)
			{
				String numberStr = message.substring(CHARGE_UNCHARGED_PREFIX.length(), endIndex);
				return cleanAndParseInt(numberStr);
			}
		}

		if (message.startsWith(CHARGE_CHARGED_PREFIX) && message.contains(CHARGE_CHARGED_MID))
		{
			int startIndex = message.indexOf(CHARGE_CHARGED_MID) + CHARGE_CHARGED_MID.length();
			int endIndex = message.indexOf(CHARGE_CHARGED_SUFFIX, startIndex);
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
			log.error("Failed to parse Tonalztics of ralos charge string: {}", amount, e);
			return null;
		}
	}
}