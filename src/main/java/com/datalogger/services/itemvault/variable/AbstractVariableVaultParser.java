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

import com.datalogger.models.itemvault.BankedItem;
import com.datalogger.services.itemvault.AbstractVaultParser;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;

@Slf4j
public abstract class AbstractVariableVaultParser extends AbstractVaultParser
{
	@Inject protected ItemManager itemManager;

	private boolean pendingLoginSync = false;
	private int loginSyncTicks = 0;

	private boolean needsLogging = false;
	private int debounceTicks = 0;
	private static final int TICKS_TO_WAIT = 5;

	protected Map<Integer, Integer> currentVarpValues = new HashMap<>();
	protected Map<Integer, Integer> currentVarbitValues = new HashMap<>();

	protected Set<Integer> getTrackedVarpIds() { return Collections.emptySet(); }
	protected Set<Integer> getTrackedVarbitIds() { return Collections.emptySet(); }

	protected abstract List<BankedItem> translateVariablesToItems();

	@Override
	protected void loadSessionData(File cacheFile) {}

	@Override
	public void setupAccountHash()
	{
		super.setupAccountHash();

		if (hasValidAccountHash)
		{
			pendingLoginSync = true;
			loginSyncTicks = 0;
		}
	}

	@Override
	protected void updateAccountHash(long accountHash, String accountName)
	{
		super.updateAccountHash(accountHash, accountName);

		if (hasValidAccountHash)
		{
			pendingLoginSync = true;
			loginSyncTicks = 0;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (!isEnabled) return;
		boolean changed = false;

		int varbitId = event.getVarbitId();
		if (varbitId != -1 && getTrackedVarbitIds().contains(varbitId))
		{
			int varbitValue = currentVarbitValues.getOrDefault(varbitId, -1);
			int newValue = client.getVarbitValue(varbitId);
			if (varbitValue != newValue)
			{
				currentVarbitValues.put(varbitId, newValue);
				log.debug("Updating varbitId {} to value {}", varbitId, newValue);
				changed = true;
			}
		}

		int varpId = event.getVarpId();
		if (getTrackedVarpIds().contains(varpId))
		{
			int varpValue = currentVarpValues.getOrDefault(varpId, -1);
			int newValue = client.getVarpValue(varpId);
			if (varpValue != newValue)
			{
				currentVarpValues.put(varpId, newValue);
				changed = true;
			}
		}

		if (changed)
		{
			needsLogging = true;
			debounceTicks = 0;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!isEnabled) return;

		if (pendingLoginSync)
		{
			loginSyncTicks++;
			boolean hasData = false;

			for (int varbitId : getTrackedVarbitIds())
			{
				if (client.getVarbitValue(varbitId) > 0)
				{
					hasData = true;
					break;
				}
			}

			if (!hasData)
			{
				for (int varpId : getTrackedVarpIds())
				{
					if (client.getVarpValue(varpId) > 0)
					{
						hasData = true;
						break;
					}
				}
			}

			if (hasData || loginSyncTicks >= 10)
			{
				pendingLoginSync = false;
				needsLogging = true;
				debounceTicks = TICKS_TO_WAIT; // Bypass debounce to force an immediate save
			}
		}

		if (needsLogging)
		{
			debounceTicks++;
			if (debounceTicks >= TICKS_TO_WAIT)
			{
				needsLogging = false;
				ensureAccountNameIsCached();
				processVault();
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (!isEnabled || !needsLogging) return;

		if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING)
		{
			needsLogging = false;
			ensureAccountNameIsCached();
			processVault();
		}
	}

	@Override
	public List<BankedItem> parseVault()
	{
		if (!isEnabled) return new ArrayList<>();
		return translateVariablesToItems();
	}

	protected void processVault()
	{
		for (int varpId : getTrackedVarpIds()) currentVarpValues.put(varpId, client.getVarpValue(varpId));
		for (int varbitId : getTrackedVarbitIds()) currentVarbitValues.put(varbitId, client.getVarbitValue(varbitId));

		List<BankedItem> parsedItems = translateVariablesToItems();
		if (parsedItems != null && !parsedItems.isEmpty())
		{
			log.debug("Parsed {} items for Variable Vault: {}", parsedItems.size(), getVaultType().name());
			submitVault(parsedItems);
		}
	}
}