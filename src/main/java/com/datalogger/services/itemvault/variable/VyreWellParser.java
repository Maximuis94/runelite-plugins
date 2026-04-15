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

import com.datalogger.models.enums.ItemCharge;
import com.datalogger.models.enums.VaultType;
import com.datalogger.models.itemvault.BankedItem;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import static net.runelite.api.gameval.VarbitID.TOB_LOBBY_WELL_CONTENTS;

@Singleton
public class VyreWellParser extends AbstractVariableVaultParser
{
	private static final ItemCharge vyreWellCharge = ItemCharge.SCYTHE_OF_VITUR;
	private final Map<Integer, String> itemNameCache = new HashMap<>();

	@Override
	public VaultType getVaultType()
	{
		return VaultType.VYRE_WELL;
	}

	@Override
	protected Set<Integer> getTrackedVarbitIds()
	{
		// Only track the Varbits; parent class will ignore Varps
		return Set.of(TOB_LOBBY_WELL_CONTENTS);
	}

	@Override
	protected List<BankedItem> translateVariablesToItems()
	{
		List<BankedItem> items = new ArrayList<>();
		int varbitValue = currentVarbitValues.getOrDefault(TOB_LOBBY_WELL_CONTENTS, 0);
		if (varbitValue <= 0) return items;

		for (Map.Entry<Integer, Integer> entry : vyreWellCharge.getInputItem().entrySet())
		{
			int itemId = entry.getKey();
			int quantityPerBatch = entry.getValue();

			// Math fixed: Added division by nCharges (100) to calculate the correct refund
			long totalQuantity = (long) varbitValue * quantityPerBatch / vyreWellCharge.getNCharges();

			if (totalQuantity > 0)
			{
				String itemName = itemNameCache.computeIfAbsent(itemId, id -> itemManager.getItemComposition(id).getName());

				items.add(new BankedItem(
					getVaultLabel(),
					currentAccountHash,
					currentAccountName,
					itemId,
					itemName,
					totalQuantity
				));
			}
		}
		return items;
	}

	@Override
	public List<BankedItem> parseOfflineFile(long accountHash, File vaultFile)
	{
		// Read using the new VariableState wrapper to prevent Type Erasure
		VariableState loadedState = fileIOService.readJson(vaultFile, VariableState.class);

		if (loadedState == null || loadedState.varbits == null)
		{
			return new ArrayList<>();
		}

		int charges = loadedState.varbits.getOrDefault(TOB_LOBBY_WELL_CONTENTS, 0);

		if (charges <= 0)
		{
			return new ArrayList<>();
		}

		List<BankedItem> items = new ArrayList<>();

		for (Map.Entry<Integer, Integer> entry : vyreWellCharge.getOutputItem().entrySet())
		{
			int itemId = entry.getKey();
			int qtyPerBatch = entry.getValue();

			// Math fixed: Added division by nCharges
			long totalQty = (long) charges * qtyPerBatch / vyreWellCharge.getNCharges();

			if (totalQty > 0)
			{
				String itemName = itemNameCache.computeIfAbsent(itemId, id -> itemManager.getItemComposition(id).getName());
				items.add(new BankedItem(
					getVaultLabel(),
					accountHash,
					"Offline Account", // Name resolution is now delegated to VaultManager
					itemId,
					itemName,
					totalQty
				));
			}
		}
		return items;
	}
}