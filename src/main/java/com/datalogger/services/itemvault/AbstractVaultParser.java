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

package com.datalogger.services.itemvault;

import com.datalogger.DataLoggerConfig;
import com.datalogger.events.AccountSessionStarted;
import com.datalogger.events.DataLoggerConfigChanged;
import com.datalogger.models.itemvault.BankedItem;
import com.datalogger.services.AccountHashMapper;
import com.datalogger.services.FileIOService;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.eventbus.Subscribe;

/**
 * The Master State Manager for all Vault Parsers.
 * Handles RuneLite lifecycles, configuration tracking, and account hashing.
 * Delegates the actual event parsing and file parsing to its subclasses.
 */
@Slf4j
public abstract class AbstractVaultParser implements VaultParser
{
	@Inject protected Client client;
	@Inject protected DataLoggerConfig config;
	@Inject protected FileIOService fileIOService;
	@Inject protected AccountHashMapper accountHashMapper;
//	@Inject protected VaultScanner vaultScanner;

	protected long currentAccountHash = -1;
	protected String currentAccountName = "Unknown";
	protected File vaultFile = null;

	protected boolean hasValidAccountHash = false;
	protected boolean hasGlobalConfigEnabled = false;
	protected boolean hasItemConfigEnabled = true;

	protected boolean isEnabled = false;

	/**
	 * Representation of a certain quantity of items per ItemCharge, for instance.
	 */
	@Value
	protected static class ItemRatio
	{
		int itemId;
		int quantityPerVarbit;
	}

	/**
	 * Allows children to read their specific data structures (Integer, Map, etc.) from disk.
	 */
	protected abstract void loadSessionData(File cacheFile);

	@Subscribe
	public void onDataLoggerConfigChanged(DataLoggerConfigChanged event)
	{
		updateConfigFlags();
	}

	@Subscribe
	public void onAccountSessionStarted(AccountSessionStarted event)
	{
		updateAccountHash(event.getAccountHash(), event.getAccountName());
		updateConfigFlags();
	}

	/**
	 * Startup method to be called by the VaultManager to handle mid-session plugin activations.
	 */
	public void setupAccountHash()
	{
		long hash = client.getAccountHash();
		String name = client.getLocalPlayer() != null ? client.getLocalPlayer().getName() : null;

		updateAccountHash(hash, name);
	}

	protected File getInternalVaultFile()
	{
		return fileIOService.getInternalVaultFile(getVaultType());
	}

	private void updateAccountHash(long accountHash, String accountName)
	{
		hasValidAccountHash = accountHash != -1;

		if (!hasValidAccountHash)
		{
			updateIsEnabled();
			return;
		}

		if (accountName != null)
			currentAccountName = accountName;

		if (currentAccountHash != accountHash)
		{
			currentAccountHash = accountHash;
			vaultFile = getInternalVaultFile();
			loadSessionData(vaultFile);
		}
		updateIsEnabled();
	}

	public void updateConfigFlags()
	{
		hasGlobalConfigEnabled = config.logItemVault();
		updateItemSpecificConfig();
		updateIsEnabled();
	}

	/**
	 * Hook method for concrete subclasses to override if they require a specific RuneLite setting.
	 */
	protected void updateItemSpecificConfig()
	{
		// Empty by default, acts as 'true'
	}

	private void updateIsEnabled()
	{
		isEnabled = hasValidAccountHash && hasGlobalConfigEnabled && hasItemConfigEnabled;
	}

	/**
	 * Helper method for child subclasses to call inside their event listeners to ensure
	 * the account name is captured if the player hopped worlds or just logged in.
	 */
	protected void ensureAccountNameIsCached()
	{
		if (currentAccountName.equals("Unknown") && client.getLocalPlayer() != null)
		{
			currentAccountName = client.getLocalPlayer().getName();
		}
	}

	@Override
	public String getFilePrefix() {
		return getVaultType().name().toLowerCase().replace("_", "-");
	}

	@Override
	public List<BankedItem> parseOfflineFile(long accountHash, File vaultFile)
	{
		// 1. Define the type for a List of BankedItems to avoid type erasure
		Type type = new TypeToken<List<BankedItem>>(){}.getType();
		List<BankedItem> loadedItems = fileIOService.readJson(vaultFile, type);

		if (loadedItems == null || loadedItems.isEmpty())
		{
			return new ArrayList<>();
		}

		// 2. Resolve the account name from the hash mapper
		String resolvedName = accountHashMapper.getAccountName(accountHash);
		List<BankedItem> items = new ArrayList<>();

		// 3. Reconstruct items with the correct offline context
		for (BankedItem item : loadedItems)
		{
			items.add(new BankedItem(
				getVaultType(),
				accountHash,
				resolvedName,
				item.getItemId(),
				item.getItemName(),
				item.getQuantity()
			));
		}

		return items;
	}

	@Override
	public String getVaultLabel() {
		return getVaultType().name();
	}
}