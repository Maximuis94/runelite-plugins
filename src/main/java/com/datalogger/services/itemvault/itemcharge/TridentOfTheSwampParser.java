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

@Slf4j
@Singleton
public class TridentOfTheSwampParser extends AbstractItemChargeParser
{
	private static final Pattern CHARGE_PATTERN = Pattern.compile("^Your Trident of the swamp has ([\\d,]+) c");

//	private static final String FULLY_CHARGED_MESSAGE = "Your weapon is fully charged.";
	private static final int MAX_CHARGES = 2500;

	@Override
	protected EquipmentInventorySlot getEquipmentSlot()
	{
		return EquipmentInventorySlot.WEAPON;
	}

	@Override
	protected int getBaseItemId()
	{
		return ItemID.TOXIC_TOTS_CHARGED;
	}

	@Override
	protected Integer parseChargeCount(String message)
	{
		Matcher matcher = CHARGE_PATTERN.matcher(message);
		if (matcher.find())
		{
			String cleanNumber = matcher.group(1).replace(",", "");
			try
			{
				return Integer.parseInt(cleanNumber);
			}
			catch (NumberFormatException e)
			{
				log.warn("Failed to parse toxic trident charges from string: {}", cleanNumber);
				return null;
			}
		}
		return null;
	}

	@Override
	protected @NonNull ItemCharge getItemChargeType()
	{
		return ItemCharge.TOXIC_TRIDENT;
	}
}