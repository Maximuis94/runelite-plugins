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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Singleton;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class SanguinestiStaffParser extends AbstractItemChargeParser
{
	private static final String INITIAL_CHECK_ITEM_NAME = "anguinesti staff";
	private static final String UNCHARGE_MESSAGE = "You uncharge your Sanguinesti staff";
	private static final Pattern CHARGE_PATTERN = Pattern.compile("^You apply ([\\d,]+) charges to your Sanguinesti staff\\.");
	private static final Pattern UPDATE_PATTERN = Pattern.compile("^Your Sanguinesti staff has ([\\d,]+) charges remaining\\.");

	@Override
	protected EquipmentInventorySlot getEquipmentSlot()
	{
		return EquipmentInventorySlot.WEAPON;
	}

	@Override
	protected int getBaseItemId()
	{
		return ItemID.SANGUINESTI_STAFF;
	}

	@Override
	protected @NonNull ItemCharge getItemChargeType()
	{
		return ItemCharge.SANGUINESTI_STAFF;
	}

	@Override
	protected Integer parseChargeCount(String rawMessage)
	{
		if (!rawMessage.contains(INITIAL_CHECK_ITEM_NAME))
		{
			return null;
		}

		String cleanMessage = Text.removeTags(rawMessage);

		if (cleanMessage.startsWith(UNCHARGE_MESSAGE))
		{
			return 0;
		}

		Matcher chargeMatcher = CHARGE_PATTERN.matcher(cleanMessage);
		if (chargeMatcher.find())
		{
			return cleanAndParseInt(chargeMatcher.group(1));
		}

		Matcher updateMatcher = UPDATE_PATTERN.matcher(cleanMessage);
		if (updateMatcher.find())
		{
			return cleanAndParseInt(updateMatcher.group(1));
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
			log.error("Failed to parse Sanguinesti staff charge string: {}", amount, e);
			return 0;
		}
	}
}