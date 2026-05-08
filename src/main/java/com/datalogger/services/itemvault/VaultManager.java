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

import com.datalogger.constants.PluginConstants;
import static com.datalogger.constants.PluginConstants.INTERNAL_VAULT_DIR;
import com.datalogger.models.itemvault.BankedItem;
import com.datalogger.models.itemvault.ValuedItemBundle;
import com.datalogger.services.AccountHashMapper;
import com.datalogger.services.FileIOService;
import com.datalogger.services.itemvault.container.BankParser;
import com.datalogger.services.itemvault.container.SeedVaultParser;
import com.datalogger.services.itemvault.itemcharge.EyeOfAyakParser;
import com.datalogger.services.itemvault.itemcharge.SanguinestiStaffParser;
import com.datalogger.services.itemvault.itemcharge.ScytheOfViturParser;
import com.datalogger.services.itemvault.itemcharge.TridentOfTheSeasParser;
import com.datalogger.services.itemvault.itemcharge.TridentOfTheSwampParser;
import com.datalogger.services.itemvault.itemcharge.TumekensShadowParser;
import com.datalogger.services.itemvault.itemcharge.VenatorBowParser;
import com.datalogger.services.itemvault.variable.MasterScrollBookParser;
import com.datalogger.services.itemvault.variable.QuiverParser;
import com.datalogger.services.itemvault.variable.RunePouchParser;
import com.datalogger.services.itemvault.variable.VyreWellParser;
import com.google.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;

@Slf4j
@Singleton
public class VaultManager {
	@Inject private EventBus eventBus;
	@Inject private FileIOService fileIOService;
	@Inject private AccountHashMapper accountHashMapper;
	@Inject private ItemManager itemManager;

	@Inject private BankParser bankParser;
	@Inject private SeedVaultParser seedVaultParser;
	@Inject private VyreWellParser vyreWellParser;
	@Inject private RunePouchParser runePouchParser;
	@Inject private TridentOfTheSeasParser tridentOfTheSeasParser;
	@Inject private TridentOfTheSwampParser tridentOfTheSwampParser;
	@Inject private ScytheOfViturParser scytheOfViturParser;
	@Inject private TumekensShadowParser tumekensShadowParser;
	@Inject private VenatorBowParser venatorBowParser;
	@Inject private EyeOfAyakParser eyeOfAyakParser;
	@Inject private SanguinestiStaffParser sanguinestiStaffParser;
	@Inject private QuiverParser quiverParser;
	@Inject private MasterScrollBookParser masterScrollBookParser;
	@Inject private StashUnitParser stashUnitParser;

	private static final File ITEM_VAULT_ALL_JSON = new File(PluginConstants.ITEM_VAULT_DIR, "item-vaults-all.json");
	private static final File ITEM_VAULT_ALL_CSV = new File(PluginConstants.ITEM_VAULT_DIR, "item-vaults-all.csv");
	private static final File ITEM_VAULTS_MERGED_JSON = new File(PluginConstants.ITEM_VAULT_DIR, "item-vaults-merged.json");
	private static final File ITEM_VAULTS_MERGED_CSV = new File(PluginConstants.ITEM_VAULT_DIR, "item-vaults-merged.csv");

	private List<VaultParser> activeParsers;
	private final List<BankedItem> masterList = new ArrayList<>();
	private final List<ValuedItemBundle> mergedItemCounts = new ArrayList<>();

	/**
	 * Guice automatically calls methods annotated with @Inject after the constructor finishes.
	 * We use this to group our parsers into a manageable list.
	 */
	@Inject
	private void init() {
		activeParsers = List.of(
			bankParser,
			seedVaultParser,
			vyreWellParser,
			runePouchParser,
			tridentOfTheSeasParser,
			tridentOfTheSwampParser,
			tumekensShadowParser,
			scytheOfViturParser,
			venatorBowParser,
			eyeOfAyakParser,
			sanguinestiStaffParser,
			quiverParser,
			masterScrollBookParser,
			stashUnitParser
		);
	}

	public void startUp() {
		eventBus.register(fileIOService);

		for (VaultParser parser : activeParsers) {
			eventBus.register(parser);

			if (parser instanceof AbstractVaultParser) {
				((AbstractVaultParser) parser).setupAccountHash();
			}
		}
		log.info("Vault Manager initialized and registered {} parsers.", activeParsers.size());
	}

	public void shutDown() {
		eventBus.unregister(fileIOService);

		for (VaultParser parser : activeParsers) {
			eventBus.unregister(parser);
		}
		log.info("Vault Manager shut down successfully.");
	}

	/**
	 * Aggregate all vault data from the internal vault files. Generate aggregated and merged files.
	 */
	public void writeMergedCsvFile()
	{
		aggregateAllOfflineVaultData();
		fileIOService.writeVaultCsv(ITEM_VAULT_ALL_CSV, masterList, true);
		fileIOService.writeVaultCsv(ITEM_VAULTS_MERGED_CSV, mergedItemCounts);
	}

	private void aggregateAllOfflineVaultData() {
		masterList.clear();
		Map<Integer, Long> aggregatedItemCounts = new HashMap<>();

		if (!INTERNAL_VAULT_DIR.exists() || !INTERNAL_VAULT_DIR.isDirectory()) {
			log.warn("Internal vault directory does not exist.");
			return;
		}

		Map<String, VaultParser> parserMap = new HashMap<>();
		for (VaultParser parser : activeParsers) {
			parserMap.put(parser.getFilePrefix(), parser);
		}

		File[] hashDirectories = INTERNAL_VAULT_DIR.listFiles();
		if (hashDirectories == null) return;

		for (File accountDir : hashDirectories) {
			if (!accountDir.isDirectory()) continue;

			long accountHash;
			try {
				accountHash = Long.parseLong(accountDir.getName());
			} catch (NumberFormatException e) {
				continue;
			}

			File[] jsonFiles = accountDir.listFiles();
			if (jsonFiles == null) continue;

			String accountName = accountHashMapper.getAccountName(accountHash);
			log.info("Parsing vault files for account: {}", accountName);

			for (File vaultFile : jsonFiles) {
				if (!vaultFile.getName().endsWith(".json")) continue;

				String prefix = vaultFile.getName().split("_")[0];
				VaultParser matchedParser = parserMap.get(prefix);

				if (matchedParser != null) {
					log.info("Parsing vault file {} of account {}", vaultFile.getName(), accountName);
					try {
						List<BankedItem> rawItems = matchedParser.parseOfflineFile(accountHash, vaultFile);
						String vaultLabel = matchedParser.getVaultLabel();

						for (BankedItem rawItem : rawItems) {
							int id = rawItem.getItemId();
							long qty = rawItem.getQuantity();

							masterList.add(new BankedItem(
								vaultLabel,
								accountHash,
								accountName,
								id,
								rawItem.getItemName(),
								qty
							));

							aggregatedItemCounts.merge(id, qty, Long::sum);

						}
					} catch (Exception e) {
						log.error("Failed to parse offline file: {}", vaultFile.getName(), e);
					}
				}
			}
		}

		mergedItemCounts.clear();
		for (Map.Entry<Integer, Long> entry : aggregatedItemCounts.entrySet()) {
			int id = entry.getKey();
			long totalQty = entry.getValue();
			String name = itemManager.getItemComposition(id).getMembersName();
			int gePrice = itemManager.getItemPrice(id);
			int price = gePrice > 0 ? gePrice : itemManager.getItemComposition(id).getHaPrice();

			if (price == 0) continue;

			mergedItemCounts.add(new ValuedItemBundle(
				id,
				name,
				totalQty,
				price
			));
		}
	}
}