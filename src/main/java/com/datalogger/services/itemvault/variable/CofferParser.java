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

package com.datalogger.services.itemvault.variable;

import com.datalogger.models.enums.VaultType;
import com.datalogger.models.itemvault.BankedItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.ItemID;
import static net.runelite.api.gameval.VarbitID.BLAST_FURNACE_COINSINCOFFER;
import static net.runelite.api.gameval.VarbitID.MISC_COFFERS;
import static net.runelite.api.gameval.VarbitID.NZONE_CASH;
import static net.runelite.api.gameval.VarbitID.BR_COFFER;

@Slf4j
@Singleton
public class CofferParser extends AbstractVariableVaultParser
{

	private static final Map<Integer, String> VARP_COFFERS = Map.of();

	private static final Map<Integer, String> VARBIT_COFFERS = Map.of(
		BLAST_FURNACE_COINSINCOFFER, "BLAST_FURNACE_COFFER",
		NZONE_CASH, "NIGHTMARE_ZONE_COFFER",
		MISC_COFFERS, "MISCELLANIA_COFFER",
//		SERVANTS_MONEYBAG_VARBIT, "SERVANT_COFFER",
		BR_COFFER, "LMS_COFFER"
	);

	@Override
	public VaultType getVaultType()
	{
		return VaultType.COFFER;
	}

	@Override
	protected Set<Integer> getTrackedVarpIds()
	{
		return VARP_COFFERS.keySet();
	}

	@Override
	protected Set<Integer> getTrackedVarbitIds()
	{
		return VARBIT_COFFERS.keySet();
	}

	@Override
	protected List<BankedItem> translateVariablesToItems()
	{
		List<BankedItem> items = new ArrayList<>();

		for (Map.Entry<Integer, String> entry : VARP_COFFERS.entrySet())
		{
			int varpId = entry.getKey();
			String cofferName = entry.getValue();
			int amount = currentVarpValues.getOrDefault(varpId, 0);

			if (amount > 0)
			{
				items.add(createCofferItem(cofferName, amount));
			}
		}

		for (Map.Entry<Integer, String> entry : VARBIT_COFFERS.entrySet())
		{
			int varbitId = entry.getKey();
			String cofferName = entry.getValue();
			int amount = currentVarbitValues.getOrDefault(varbitId, 0);

			if (amount > 0)
			{
				items.add(createCofferItem(cofferName, amount));
			}
		}

		return items;
	}

	/**
	 * Helper method to construct the BankedItem.
	 * Binds the GP amount to the specific coffer name while keeping the standard Coin Item ID.
	 */
	private BankedItem createCofferItem(String cofferName, long amount)
	{
		return new BankedItem(
			getVaultType(),
			currentAccountHash,
			currentAccountName,
			ItemID.COINS,
			cofferName,
			amount
		);
	}
}