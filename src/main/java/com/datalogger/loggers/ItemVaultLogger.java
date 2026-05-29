/*
 * Copyright (c) 2026, maximuis94 <https://github.com/maximuis94>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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
import static com.datalogger.constants.PluginConstants.INTERNAL_VAULT_DIR;
import com.datalogger.events.DataLoggerConfigChanged;
import com.datalogger.framework.AbstractLogger;
import com.datalogger.framework.LogType;
import com.datalogger.models.enums.ItemCharge;
import com.datalogger.models.enums.VaultType;
import com.datalogger.models.itemvault.BankedItem;
import com.datalogger.models.itemvault.ValuedItemBundle;
import com.datalogger.services.AccountHashMapper;
import com.datalogger.services.FileIOService;
import com.datalogger.services.itemvault.VaultParser;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;

@Slf4j
@Singleton
public class ItemVaultLogger extends AbstractLogger
{
	public static final File MERGED_ITEM_VAULT_JSON = new File(ITEM_VAULT_DIR, "merged-vaults.json");
	public static final File MERGED_ITEM_VAULT_CSV = new File(ITEM_VAULT_DIR, "merged-vaults.csv");
	public static final File ITEM_VAULTS_MERGED_GROUPED_CSV = new File(ITEM_VAULT_DIR, "item-vaults-merged-grouped.csv");

	private final FileIOService fileIOService;
	private final DataLoggerConfig config;
	private final AccountHashMapper accountHashMapper;
	private final ScheduledExecutorService executor;
	private final ItemManager itemManager;
	private final ClientThread clientThread;

	private final Map<Long, Map<String, List<BankedItem>>> vaultCache = new ConcurrentHashMap<>();
	private Set<Long> ignoredAccountHashes = new HashSet<>();

	@Inject
	private ItemVaultLogger(
		FileIOService fileIOService,
		DataLoggerConfig config,
		AccountHashMapper accountHashMapper,
		ScheduledExecutorService executor,
		ItemManager itemManager,
		ClientThread clientThread
	) {
		this.fileIOService = fileIOService;
		this.config = config;
		this.accountHashMapper = accountHashMapper;
		this.executor = executor;
		this.itemManager = itemManager;
		this.clientThread = clientThread;
	}

	@Subscribe
	public void onDataLoggerConfigChanged(DataLoggerConfigChanged event)
	{
		updateIgnoredAccountHashes();
	}

	public File getInternalVaultFile(VaultType vaultType, String accountHashString)
	{
		return fileIOService.getInternalVaultFile(vaultType, accountHashString);
	}

	public File getInternalVaultFile(ItemCharge itemCharge, String accountHashString)
	{
		return fileIOService.getInternalVaultFile(itemCharge, accountHashString);
	}

	public void updateVault(long accountHash, VaultType vaultType, List<BankedItem> items)
	{
		vaultCache.computeIfAbsent(accountHash, k -> new ConcurrentHashMap<>())
			.put(vaultType.name(), new ArrayList<>(items));
		log.debug("ItemVaultLogger ItemVault updated {} for account {}. Total tracked items in this vault: {}", vaultType.name(), accountHash, items.size());
	}

	public void updateVault(long accountHash, ItemCharge itemCharge, List<BankedItem> items)
	{
		vaultCache.computeIfAbsent(accountHash, k -> new ConcurrentHashMap<>())
			.put(itemCharge.name(), new ArrayList<>(items));
		log.debug("ItemVaultLogger ItemChargeVault updated {} for account {}. Total tracked items in this vault: {}", itemCharge.name(), accountHash, items.size());
	}

	public List<BankedItem> getVault(long accountHash, VaultType vaultType)
	{
		return vaultCache.getOrDefault(accountHash, new HashMap<>()).getOrDefault(vaultType.name(), new ArrayList<>());
	}

	public List<BankedItem> getVault(long accountHash, ItemCharge itemCharge)
	{
		return vaultCache.getOrDefault(accountHash, new HashMap<>()).getOrDefault(itemCharge.name(), new ArrayList<>());
	}

	private boolean ignoreHash(long accountHash)
	{
		return ignoredAccountHashes.contains(accountHash);
	}

	public List<BankedItem> aggregateAllAccounts()
	{
		List<BankedItem> allItems = new ArrayList<>();
		loadAllVaultsIntoMemory();

		for (Map.Entry<Long, Map<String, List<BankedItem>>> accountEntry : vaultCache.entrySet())
		{
			long currentAccountHash = accountEntry.getKey();
			if (ignoreHash(currentAccountHash)) continue;

			for (Map.Entry<String, List<BankedItem>> vaultEntry : accountEntry.getValue().entrySet())
			{
				String type = vaultEntry.getKey();
				for (BankedItem item : vaultEntry.getValue())
				{
					allItems.add(new BankedItem(
						type, currentAccountHash, item.getAccountName(),
						item.getItemId(), item.getItemName(), item.getQuantity()
					));
				}
			}
		}
		allItems.sort(java.util.Comparator.comparing(BankedItem::getItemName, String.CASE_INSENSITIVE_ORDER));
		return allItems;
	}

	public long getTotalItemQuantity(int itemId)
	{
		long total = 0;
		for (Map<String, List<BankedItem>> accountVaults : vaultCache.values())
		{
			for (List<BankedItem> items : accountVaults.values())
			{
				for (BankedItem item : items)
				{
					if (item.getItemId() == itemId) total += item.getQuantity();
				}
			}
		}
		return total;
	}

	public void loadAllVaultsIntoMemory()
	{
		Map<Long, Map<String, List<BankedItem>>> diskData = fileIOService.readAllVaultFilesRaw();
		if (diskData.isEmpty()) return;

		for (Map.Entry<Long, Map<String, List<BankedItem>>> entry : diskData.entrySet())
		{
			long accountHash = entry.getKey();
			String accountName = accountHashMapper.getAccountName(accountHash);

			for (Map.Entry<String, List<BankedItem>> vaultEntry : entry.getValue().entrySet())
			{
				String type = vaultEntry.getKey();
				List<BankedItem> items = vaultEntry.getValue();

				List<BankedItem> enrichedItems = items.stream().map(item -> new BankedItem(
					type, accountHash, accountName, item.getItemId(), item.getItemName(), item.getQuantity()
				)).collect(Collectors.toList());

				vaultCache.computeIfAbsent(accountHash, k -> new ConcurrentHashMap<>()).put(type, enrichedItems);
			}
		}
	}

	/**
	 * Receives parsed items directly from VaultParsers, updates the cache, and saves to disk.
	 */
	public void logVault(long accountHash, String accountName, VaultType vaultType, List<BankedItem> items)
	{
		if (items == null || items.isEmpty() || ignoredAccountHashes.contains(accountHash)) return;

		updateVault(accountHash, vaultType, items);

		executor.submit(() -> {
			List<java.util.Map<String, Object>> slimItems = items.stream()
				.map(item -> {
					java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
					map.put("itemId", item.getItemId());
					map.put("itemName", item.getItemName());
					map.put("quantity", item.getQuantity());
					return map;
				}).collect(Collectors.toList());

			File internalJson = fileIOService.getInternalVaultFile(vaultType, String.valueOf(accountHash));
			fileIOService.writeJson(internalJson, slimItems);

			if (config.logItemVaultJSON()) {
				File externalJson = vaultType.getExternalJSONFile(accountName);
				fileIOService.writeJson(externalJson, slimItems);
			}
			if (config.logItemVaultCSV()) {
				File csvFile = vaultType.getExternalCSVFile(accountName);
				fileIOService.writeVaultCsv(csvFile, items, false);
			}
		});
	}

	/**
	 * Receives parsed items directly from VaultParsers, updates the cache, and saves to disk.
	 */
	public void logVault(long accountHash, String accountName, ItemCharge itemCharge, List<BankedItem> items)
	{
		if (items == null || items.isEmpty() || ignoredAccountHashes.contains(accountHash)) return;

		updateVault(accountHash, itemCharge, items);

		executor.submit(() -> {
			List<java.util.Map<String, Object>> slimItems = items.stream()
				.map(item -> {
					java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
					map.put("itemId", item.getItemId());
					map.put("itemName", item.getItemName());
					map.put("quantity", item.getQuantity());
					return map;
				}).collect(Collectors.toList());

			File internalJson = fileIOService.getInternalVaultFile(itemCharge, String.valueOf(accountHash));
			fileIOService.writeJson(internalJson, slimItems);


		});
	}

	/**
	 * Formats and exports the merged item data across all Vaults and Accounts.
	 * Bounces gracefully between threads to ensure the UI doesn't freeze while fetching Item GE Prices.
	 */
	public void exportAggregatedData(List<VaultParser> itemChargeParsers)
	{
		executor.submit(() -> {
			log.debug("Attempting to export aggregated vault data...");
			List<BankedItem> masterList = aggregateAllAccounts();

			if (masterList == null || masterList.isEmpty()) {
				log.debug("No aggregated data found to export.");
				return;
			}

			Map<Integer, Long> aggregatedItemCounts = new HashMap<>();
			for (BankedItem item : masterList) {
				aggregatedItemCounts.merge(item.getItemId(), item.getQuantity(), Long::sum);
			}

			clientThread.invokeLater(() -> {
				List<ValuedItemBundle> mergedItemCounts = new ArrayList<>();
				for (Map.Entry<Integer, Long> entry : aggregatedItemCounts.entrySet()) {
					int id = entry.getKey();
					long totalQty = entry.getValue();
					String name = itemManager.getItemComposition(id).getMembersName();
					int gePrice = itemManager.getItemPrice(id);
					int price = gePrice > 0 ? gePrice : itemManager.getItemComposition(id).getHaPrice();

					if (price > 0) {
						mergedItemCounts.add(new ValuedItemBundle(id, name, totalQty, price));
					}
				}

				executor.submit(() -> {
					fileIOService.writeJson(MERGED_ITEM_VAULT_JSON, masterList);
					fileIOService.writeVaultCsv(MERGED_ITEM_VAULT_CSV, masterList, true);
					fileIOService.writeVaultCsv(ITEM_VAULTS_MERGED_GROUPED_CSV, mergedItemCounts);

					if (itemChargeParsers != null) {
						fileIOService.exportAggregatedItemCharges(0, "Aggregated", itemChargeParsers);
					}
					log.debug("Successfully exported aggregated wealth summary for {} unique items.", masterList.size());
				});
			});


		});
	}

	public List<BankedItem> parseOfflineFile(long accountHash, File vaultFile, String vaultLabel)
	{
		BankedItem[] loadedItems = fileIOService.readJson(vaultFile, BankedItem[].class);
		if (loadedItems == null || loadedItems.length == 0) return new ArrayList<>();

		String accountName = accountHashMapper.getAccountName(accountHash);

		return Arrays.stream(loadedItems)
			.filter(item -> item.getItemId() > 0 && item.getQuantity() > 0)
			.map(item -> new BankedItem(vaultLabel, accountHash, accountName, item.getItemId(), item.getItemName(), item.getQuantity()))
			.collect(Collectors.toList());
	}

	public List<String> getExistingVaults(long accountHash)
	{
		String hashString = String.valueOf(accountHash);
		File root = new File(INTERNAL_VAULT_DIR, hashString);
		if (!root.exists()) return List.of();

		String affix = "_" + hashString + ".json";
		File[] jsonFiles = root.listFiles((dir, name) -> name.endsWith(affix));

		if (jsonFiles == null) return List.of();

		return Arrays.stream(jsonFiles)
			.map(File::getName)
			.map(name -> name.substring(0, name.length() - affix.length()))
			.collect(Collectors.toList());
	}

	public void updateIgnoredAccountHashes()
	{
		Set<Long> ignoredHashes = new HashSet<>();
		String skipListStr = config.skipItemVaultAccountList();

		if (skipListStr == null || skipListStr.trim().isEmpty()) {
			ignoredAccountHashes = ignoredHashes;
			return;
		}

		for (String name : skipListStr.split(",")) {
			String trimmedName = name.trim();
			if (trimmedName.isEmpty()) continue;

			long hash = accountHashMapper.getAccountHashByAccountName(trimmedName);
			if (hash != -1L) ignoredHashes.add(hash);
		}
		ignoredAccountHashes = ignoredHashes;
	}

	@Override
	public LogType getLogType() { return LogType.ITEM_VAULT; }

	@Override
	public String getCsvHeader() { return ""; }

	@Override
	public boolean isEnabled() { return config.logItemVault(); }
}