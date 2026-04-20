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
import com.datalogger.framework.AbstractLogger;
import com.datalogger.framework.LogType;
import com.datalogger.models.grandexchange.ActiveGeOffer;
import com.datalogger.models.itemvault.BankedItem;
import com.datalogger.models.itemvault.enums.VaultType;
import com.datalogger.services.AccountHashMapper;
import com.datalogger.services.FileIOService;
import static com.datalogger.services.FileIOService.AGGREGATED_ITEM_VAULT_CSV;
import static com.datalogger.services.FileIOService.AGGREGATED_ITEM_VAULT_JSON;
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
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

@Slf4j
@Singleton
public class ItemVaultLogger extends AbstractLogger
{
	private final FileIOService fileIOService;
	private final DataLoggerConfig config;
	private final AccountHashMapper accountHashMapper;
	private final ScheduledExecutorService executor;

	private final Map<Long, Map<VaultType, List<BankedItem>>> vaultCache = new ConcurrentHashMap<>();
	private Set<Long> ignoredAccountHashes;

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
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(DataLoggerConfig.CONFIG_GROUP)) return;

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
						type.name(),
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
	 * Iterates over all saved vault JSON files, parses the VaultType and accountHash
	 * from the file names, and loads them into the live memory cache.
	 */
	public void loadAllVaultsIntoMemory() {
		// Wrap the entire load process in the background executor!
		executor.submit(() -> {
			// 1. Let FileIOService handle the disk reads
			Map<String, BankedItem[]> allVaultData = fileIOService.readAllVaultFilesRaw();

			if (allVaultData.isEmpty()) {
				log.debug("No saved vaults found to load into memory.");
				return;
			}

			int loadedFiles = 0;

			// 2. Process the in-memory data
			for (Map.Entry<String, BankedItem[]> entry : allVaultData.entrySet()) {
				String fileName = entry.getKey();
				BankedItem[] parsedItems = entry.getValue();

				String nameWithoutExt = fileName.substring(0, fileName.length() - 5);
				int lastDashIndex = nameWithoutExt.lastIndexOf('_');

				if (lastDashIndex == -1) {
					log.warn("Skipping file with unrecognizable format: {}", fileName);
					continue;
				}

				String vaultTypeStr = nameWithoutExt.substring(0, lastDashIndex);
				String hashStr = nameWithoutExt.substring(lastDashIndex + 1);

				try {
					String enumName = vaultTypeStr.toUpperCase().replace("-", "_");
					VaultType vaultType = VaultType.valueOf(enumName);
					long accountHash = Long.parseLong(hashStr);
					String accountName = accountHashMapper.getAccountName(accountHash);

					List<BankedItem> enrichedItems = new ArrayList<>();
					for (BankedItem item : parsedItems) {
						enrichedItems.add(new BankedItem(
							vaultType.name(),
							accountHash,
							accountName,
							item.getItemId(),
							item.getItemName(),
							item.getQuantity()
						));
					}

					vaultCache.computeIfAbsent(accountHash, k -> new ConcurrentHashMap<>())
						.put(vaultType, enrichedItems);

					loadedFiles++;
				} catch (IllegalArgumentException e) {
					log.warn("Could not parse VaultType or AccountHash from file name: {}. Skipping.", fileName);
				}
			}

			// 3. Trigger GE Vault loads
			for (long accountHash : vaultCache.keySet()) {
				loadGeVaultIntoMemory(accountHash);
			}

			log.debug("Successfully loaded {} vault files into memory across {} accounts.", loadedFiles, vaultCache.size());
		});
	}

	/**
	 * Receives parsed items from the BankParser, updates the cache, and saves to disk.
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

			File internalJson = vaultType.getInternalFile(accountHash);
			fileIOService.saveJson(internalJson, slimItems);

			if (config.logItemVaultJSON())
			{
				File externalJson = vaultType.getExternalJSONFile(accountHash);
				fileIOService.saveJson(externalJson, slimItems);
			}

			if (config.logItemVaultCSV())
			{
				File csvFile = vaultType.getExternalCSVFile(accountHash);
				fileIOService.writeVaultCsv(csvFile, items, false);;
			}
		});
	}

	/**
	 * Exports the total aggregated wealth across ALL accounts to a single CSV and JSON file.
	 */
	public void exportAggregatedData()
	{
		log.debug("Attempting to export aggregated vault data...");
		List<BankedItem> items = aggregateAllAccounts();

		if (items == null || items.isEmpty()) {
			log.debug("No aggregated data found to export.");
			return;
		}

		fileIOService.saveJson(AGGREGATED_ITEM_VAULT_JSON, items);
		fileIOService.writeVaultCsv(AGGREGATED_ITEM_VAULT_CSV, items, true);

		log.debug("Successfully exported aggregated wealth summary for {} unique items.", items.size());
	}

	/**
	 * Parses the skipItemVaultAccountList config entry and translates the account names
	 * into a Set of Account Hashes to be ignored.
	 * * @return A Set of account hashes that should be skipped
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

	/**
	 * Translates a single GE offer into physical BankedItems representing ONLY
	 * the unfulfilled portion of the offer (assuming completed parts are collected).
	 */
	private BankedItem translateGeOfferToItems(long accountHash, String accountName, ActiveGeOffer offer)
	{
		final int COINS_ITEM_ID = 995;

		int remainingQuantity = offer.getTotalQuantity() - offer.getQuantitySold();

		if (remainingQuantity <= 0) {
			return null;
		}

		if (offer.isBuy())
		{
			return new BankedItem(
				VaultType.GRAND_EXCHANGE.name(),
				accountHash,
				accountName,
				COINS_ITEM_ID,
				"Coins",
				remainingQuantity * offer.getOfferPrice()
			);
		}
		else
		{
			return new BankedItem(
				VaultType.GRAND_EXCHANGE.name(),
				accountHash,
				accountName,
				offer.getItemId(),
				offer.getItemName(),
				remainingQuantity
			);
		}

	}

	private boolean isActiveOffer(GrandExchangeOfferState state)
	{
		return state == GrandExchangeOfferState.BUYING || state == GrandExchangeOfferState.SELLING;
	}

	/**
	 * Loads active GE offers from disk, translates unfulfilled offers into items,
	 * merges duplicates (like Coins), and stores them in the vault cache.
	 */
	public void loadGeVaultIntoMemory(long accountHash)
	{
		String hashStr = String.valueOf(accountHash);
		String accountName = accountHashMapper.getAccountName(accountHash);

		List<ActiveGeOffer> activeOffers = fileIOService.loadActiveGeOffers(hashStr);

		if (activeOffers == null || activeOffers.isEmpty()) {
			return;
		}

		// Use a map to merge identical items (e.g., combining GP from multiple buy slots)
		Map<Integer, BankedItem> mergedGeVault = new HashMap<>();

		for (ActiveGeOffer offer : activeOffers)
		{
			if (!isActiveOffer(offer.getState())) {
				continue;
			}

			BankedItem geOffer = translateGeOfferToItems(accountHash, accountName, offer);
			if (geOffer == null) continue;

			mergedGeVault.merge(
				geOffer.getItemId(),
				geOffer,
				(existing, incoming) -> new BankedItem(
					VaultType.GRAND_EXCHANGE.name(),
					accountHash,
					accountName,
					existing.getItemId(),
					existing.getItemName(),
					(long) (existing.getQuantity() + incoming.getQuantity())
				)
				);
		}

		if (!mergedGeVault.isEmpty()) {
			List<BankedItem> finalVaultItems = new ArrayList<>(mergedGeVault.values());
			updateVault(accountHash, VaultType.GRAND_EXCHANGE, finalVaultItems);
			log.debug("Loaded {} unique physical item stacks from locked GE offers into memory for {}", finalVaultItems.size(), accountName);
		}
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
		return config.logColosseum();
	}

	@Override
	public void setup()
	{
		super.setup();
	}
}