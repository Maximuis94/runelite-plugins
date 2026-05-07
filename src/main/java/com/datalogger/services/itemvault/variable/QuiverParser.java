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
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import static net.runelite.api.gameval.VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO;
import static net.runelite.api.gameval.VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO_AMOUNT;

@Slf4j
@Singleton
public class QuiverParser extends AbstractVariableVaultParser
{
	@Override
	public VaultType getVaultType()
	{
		return VaultType.QUIVER;
	}

	@Override
	protected Set<Integer> getTrackedVarpIds()
	{
		return Set.of(DIZANAS_QUIVER_TEMP_AMMO, DIZANAS_QUIVER_TEMP_AMMO_AMOUNT);
	}

	@Override
	protected List<BankedItem> translateVariablesToItems()
	{
		int quiverItemId = currentVarpValues.getOrDefault(DIZANAS_QUIVER_TEMP_AMMO, -1);
		int quiverItemQuantity = currentVarpValues.getOrDefault(DIZANAS_QUIVER_TEMP_AMMO_AMOUNT, 0);

		if (!isEnabled || quiverItemQuantity <= 0 || quiverItemId <= 0)
		{
			return new ArrayList<>();
		}

		String itemName = itemManager.getItemComposition(quiverItemId).getName();

		return List.of(new BankedItem(
			getVaultLabel(),
			currentAccountHash,
			currentAccountName,
			quiverItemId,
			itemName,
			quiverItemQuantity
		));
	}

//	@Override
//	protected void loadSessionData(File cacheFile)
//	{
//		Type type = new TypeToken<List<BankedItem>>(){}.getType();
//		List<BankedItem> loadedItems = fileIOService.readJson(cacheFile, type);
//
//		if (loadedItems == null || loadedItems.isEmpty())
//		{
//			return;
//		}
//
//		BankedItem item = loadedItems.get(0);
//
//		currentVarpValues.clear();
//		currentVarpValues.put(DIZANAS_QUIVER_TEMP_AMMO, item.getItemId());
//		currentVarpValues.put(DIZANAS_QUIVER_TEMP_AMMO_AMOUNT, (int) item.getQuantity());
//
//		if (item.getQuantity() > 0)
//		{
//			log.debug("Loaded quiver variables from file: {}x {}", item.getQuantity(), item.getItemName());
//		}
//		else
//		{
//			log.debug("Loaded empty quiver data");
//		}
//	}

//	@Override
//	public List<BankedItem> parseOfflineFile(long accountHash, File vaultFile)
//	{
//		VariableState loadedState = fileIOService.readJson(vaultFile, VariableState.class);
//
//		if (loadedState == null || loadedState.varps == null)
//		{
//			return new ArrayList<>();
//		}
//
//		int itemId = loadedState.varps.getOrDefault(DIZANAS_QUIVER_TEMP_AMMO, -1);
//		int quantity = loadedState.varps.getOrDefault(DIZANAS_QUIVER_TEMP_AMMO_AMOUNT, 0);
//
//		if (itemId <= 0 || quantity <= 0)
//		{
//			return new ArrayList<>();
//		}
//
//		String resolvedName = accountHashMapper.getAccountName(accountHash);
//		return List.of(new BankedItem(
//			getVaultType(),
//			accountHash,
//			resolvedName,
//			itemId,
//			itemManager.getItemComposition(itemId).getName(),
//			quantity
//		));
//	}
}