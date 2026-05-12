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

package com.datalogger.loggers;

import com.datalogger.DataLoggerConfig;
import static com.datalogger.constants.PluginConstants.ITEM_VAULT_DIR;
import com.datalogger.events.DataLoggerConfigChanged;
import com.datalogger.framework.AbstractLogger;
import com.datalogger.framework.LogType;
import com.datalogger.models.enums.VaultType;
import com.datalogger.models.itemvault.BankedItem;
import com.datalogger.services.AccountHashMapper;
import com.datalogger.services.FileIOService;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
public class ItemVaultLogger extends AbstractLogger
{
	public static final File MERGED_ITEM_VAULT_JSON = new File(ITEM_VAULT_DIR, "merged-vaults.json");
	public static final File MERGED_ITEM_VAULT_CSV = new File(ITEM_VAULT_DIR, "merged-vaults.csv");

	private final FileIOService fileIOService;
	private final DataLoggerConfig config;
	private final AccountHashMapper accountHashMapper;
	private final ScheduledExecutorService executor;

	private final Map<Long, Map<VaultType, List<BankedItem>>> vaultCache = new ConcurrentHashMap<>();
	private Set<Long> ignoredAccountHashes = new HashSet<>();

	@Inject
	private ItemVaultLogger(
		FileIOService fileIOService,
		DataLoggerConfig config,
		AccountHashMapper accountHashMapper,
		ScheduledExecutorService executor
	) {
		this.fileIOService = fileIOService;
		this.config = config;
		this.accountHashMapper = accountHashMapper;
		this.executor = executor;
	}

	@Subscribe
	public void onDataLoggerConfigChanged(DataLoggerConfigChanged event)
	{
		updateIgnoredAccountHashes();
	}

	/**
	 * Stores or updates the parsed items for a specific account and vault type in memory.
	 */
	public void updateVault(long accountHash, VaultType vaultType, List<BankedItem> items)
	{
		vaultCache.computeIfAbsent(accountHash, k -> new ConcurrentHashMap<>())
			.put(vaultType, new ArrayList<>(items));

		log.debug("ItemVaultLogger updated {} for account {}. Total tracked items in this vault: {}",
			vaultType.name(), accountHash, items.size());
	}

	/**
	 * Retrieves the live in-memory vault for a specific account and vault type.
	 */
	public List<BankedItem> getVault(long accountHash, VaultType vaultType)
	{
		return vaultCache.getOrDefault(accountHash, new HashMap<>())
			.getOrDefault(vaultType, new ArrayList<>());
	}

	private boolean ignoreHash(long accountHash)
	{
		return ignoredAccountHashes.contains(accountHash);
	}

	/**
	 * Compiles a flat, alphabetically sorted list of all items across ALL tracked accounts
	 * currently loaded in memory, preserving specific account and vault metadata.
	 */
	public List<BankedItem> aggregateAllAccounts()
	{
		List<BankedItem> allItems = new ArrayList<>();

		loadAllVaultsIntoMemory();

		for (Map.Entry<Long, Map<VaultType, List<BankedItem>>> accountEntry : vaultCache.entrySet())
		{
			long currentAccountHash = accountEntry.getKey();

			if (ignoreHash(currentAccountHash))
				continue;

			for (Map.Entry<VaultType, List<BankedItem>> vaultEntry : accountEntry.getValue().entrySet())
			{
				VaultType type = vaultEntry.getKey();

				for (BankedItem item : vaultEntry.getValue())
				{
					allItems.add(new BankedItem(
						type,
						currentAccountHash,
						item.getAccountName(),
						item.getItemId(),
						item.getItemName(),
						item.getQuantity()
					));
				}
			}
		}
		allItems.sort(java.util.Comparator.comparing(BankedItem::getItemName, String.CASE_INSENSITIVE_ORDER));
		return allItems;
	}

	/**
	 * Finds the total quantity of a specific item across all accounts and vaults.
	 */
	public long getTotalItemQuantity(int itemId)
	{
		long total = 0;
		for (Map<VaultType, List<BankedItem>> accountVaults : vaultCache.values())
		{
			for (List<BankedItem> items : accountVaults.values())
			{
				for (BankedItem item : items)
				{
					if (item.getItemId() == itemId)
					{
						total += item.getQuantity();
					}
				}
			}
		}
		return total;
	}

	/**
	 * Synchronously reads all saved vault JSON files from disk and populates the cache.
	 */
	public void loadAllVaultsIntoMemory()
	{
		Map<Long, Map<VaultType, List<BankedItem>>> diskData = fileIOService.readAllVaultFilesRaw();

		if (diskData.isEmpty()) {
			log.debug("No saved vaults found to load into memory.");
			return;
		}

		for (Map.Entry<Long, Map<VaultType, List<BankedItem>>> entry : diskData.entrySet())
		{
			long accountHash = entry.getKey();
			String accountName = accountHashMapper.getAccountName(accountHash);

			for (Map.Entry<VaultType, List<BankedItem>> vaultEntry : entry.getValue().entrySet())
			{
				VaultType type = vaultEntry.getKey();
				List<BankedItem> items = vaultEntry.getValue();

				// Enforce proper metadata
				List<BankedItem> enrichedItems = new ArrayList<>();
				for (BankedItem item : items)
				{
					enrichedItems.add(new BankedItem(
						type,
						accountHash,
						accountName,
						item.getItemId(),
						item.getItemName(),
						item.getQuantity()
					));
				}

				vaultCache.computeIfAbsent(accountHash, k -> new ConcurrentHashMap<>())
					.put(type, enrichedItems);
			}
		}

		log.debug("Successfully loaded vault files into memory across {} accounts.", diskData.size());
	}

	/**
	 * Receives parsed items from a VaultParser, updates the cache, and saves to disk.
	 */
	public void logVault(long accountHash, String accountName, VaultType vaultType, List<BankedItem> items)
	{
		if (items == null || items.isEmpty()) return;

		if (ignoredAccountHashes.contains(accountHash)) {
			log.debug("Skipping vault log for {} because it is in the ignore list.", accountName);
			return;
		}

		updateVault(accountHash, vaultType, items);

		executor.submit(() -> {
			List<java.util.Map<String, Object>> slimItems = items.stream()
				.map(item -> {
					java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
					map.put("itemId", item.getItemId());
					map.put("itemName", item.getItemName());
					map.put("quantity", item.getQuantity());
					return map;
				}).collect(java.util.stream.Collectors.toList());

			File internalJson = fileIOService.getInternalVaultFile(vaultType);
			fileIOService.saveJson(internalJson, slimItems);

			if (config.logItemVaultJSON())
			{
				File externalJson = vaultType.getExternalJSONFile(accountHash);
				fileIOService.saveJson(externalJson, slimItems);
			}

			if (config.logItemVaultCSV())
			{
				File csvFile = vaultType.getExternalCSVFile(accountHash);
				fileIOService.writeVaultCsv(csvFile, items, false);
			}
		});
	}

	/**
	 * Exports the total aggregated wealth across ALL accounts to a single CSV and JSON file.
	 */
	public void exportAggregatedData()
	{
		// Wrap the export in an executor so the Heavy Disk IO from aggregateAllAccounts() runs safely in the background
		executor.submit(() -> {
			log.debug("Attempting to export aggregated vault data...");
			List<BankedItem> items = aggregateAllAccounts();

			if (items == null || items.isEmpty()) {
				log.debug("No aggregated data found to export.");
				return;
			}

			fileIOService.saveJson(MERGED_ITEM_VAULT_JSON, items);
			fileIOService.writeVaultCsv(MERGED_ITEM_VAULT_CSV, items, true);

			log.debug("Successfully exported aggregated wealth summary for {} unique items.", items.size());
		});
	}

	/**
	 * Parses the skipItemVaultAccountList config entry and translates the account names
	 * into a Set of Account Hashes to be ignored.
	 */
	public void updateIgnoredAccountHashes()
	{
		Set<Long> ignoredHashes = new HashSet<>();
		String skipListStr = config.skipItemVaultAccountList();

		if (skipListStr == null || skipListStr.trim().isEmpty())
		{
			ignoredAccountHashes = ignoredHashes;
			return;
		}

		String[] ignoredNames = skipListStr.split(",");

		for (String name : ignoredNames)
		{
			String trimmedName = name.trim();
			if (trimmedName.isEmpty()) continue;

			long hash = accountHashMapper.getAccountHashByAccountName(trimmedName);

			if (hash != -1L)
			{
				ignoredHashes.add(hash);
			}
			else
			{
				log.debug("Could not find an account hash for ignored account name: '{}'. " +
					"It may be misspelled or hasn't been logged in yet.", trimmedName);
			}
		}
		ignoredAccountHashes = ignoredHashes;
	}

	@Override
	public LogType getLogType()
	{
		return LogType.ITEM_VAULT;
	}

	@Override
	public String getCsvHeader()
	{
		return "";
	}

	@Override
	public boolean isEnabled()
	{
		return config.logItemVault();
	}

	@Override
	public void setup()
	{
		super.setup();
	}
}