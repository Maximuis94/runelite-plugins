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

import com.datalogger.loggers.ItemVaultLogger;
import com.datalogger.models.enums.VaultType;
import com.datalogger.services.itemvault.container.BankParser;
import com.datalogger.services.itemvault.container.SeedVaultParser;
import com.datalogger.services.itemvault.itemcharge.*;
import com.datalogger.services.itemvault.other.*;
import com.datalogger.services.itemvault.variable.*;
import com.google.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.EventBus;

@Slf4j
@Singleton
public class VaultManager {
	@Inject private EventBus eventBus;
	@Inject private ItemVaultLogger itemVaultLogger;

	@Inject private BankParser bankParser;
	@Inject private SeedVaultParser seedVaultParser;
	@Inject private VyreWellParser vyreWellParser;
	@Inject private RunePouchParser runePouchParser;
	@Inject private CofferParser cofferParser;
	@Inject private TridentOfTheSeasParser tridentOfTheSeasParser;
	@Inject private TridentOfTheSeasEParser tridentOfTheSeasEParser;
	@Inject private TridentOfTheSwampParser tridentOfTheSwampParser;
	@Inject private TridentOfTheSwampEParser tridentOfTheSwampEParser;
	@Inject private ScytheOfViturParser scytheOfViturParser;
	@Inject private TumekensShadowParser tumekensShadowParser;
	@Inject private VenatorBowParser venatorBowParser;
	@Inject private EyeOfAyakParser eyeOfAyakParser;
	@Inject private SanguinestiStaffParser sanguinestiStaffParser;
	@Inject private WarpedSceptreParser warpedSceptreParser;
	@Inject private ViggorasChainmaceParser viggorasChainmaceParser;
	@Inject private UrsineChainmaceParser ursineChainmaceParser;
	@Inject private CrawsBowParser crawsBowParser;
	@Inject private WebweaverBowParser webweaverBowParser;
	@Inject private ThammaronsSceptreParser thammaronsSceptreParser;
	@Inject private AccursedSceptreParser accursedSceptreParser;
	@Inject private TomeOfEarthParser tomeOfEarthParser;
	@Inject private TomeOfFireParser tomeOfFireParser;
	@Inject private TomeOfWaterParser tomeOfWaterParser;
	@Inject private TonalzticsOfRalosParser tonalzticsOfRalosParser;
	@Inject private ToxicBlowpipeParser toxicBlowpipeParser;
	@Inject private QuiverParser quiverParser;
	@Inject private MasterScrollBookParser masterScrollBookParser;
	@Inject private StashUnitParser stashUnitParser;
	@Inject private POHCostumeRoomParser pohCostumeRoomParser;
	@Inject private FarmingToolsParser farmingToolsParser;
	@Inject private ToaPickaxeParser toaPickaxeParser;
	@Inject private ActiveGrandExchangeOfferParser activeGrandExchangeOfferParser;
	@Inject private CarriedItemsParser carriedItemsParser;

	@Setter
	@Getter
	private boolean isEnabled = false;
	private List<VaultParser> activeParsers;
	@Getter
	private List<VaultParser> itemChargeParsers;

	@Inject
	private void init() {
		activeParsers = List.of(
			bankParser, seedVaultParser, vyreWellParser, runePouchParser,
			cofferParser, tridentOfTheSeasParser, tridentOfTheSeasEParser,
			tridentOfTheSwampParser, tridentOfTheSwampEParser, tumekensShadowParser,
			scytheOfViturParser, venatorBowParser, eyeOfAyakParser,
			sanguinestiStaffParser, warpedSceptreParser, viggorasChainmaceParser,
			ursineChainmaceParser, crawsBowParser, webweaverBowParser,
			thammaronsSceptreParser, accursedSceptreParser, tomeOfEarthParser,
			tomeOfFireParser, tomeOfWaterParser, tonalzticsOfRalosParser,
			toxicBlowpipeParser, quiverParser, masterScrollBookParser,
			stashUnitParser, pohCostumeRoomParser, farmingToolsParser,
			toaPickaxeParser, activeGrandExchangeOfferParser, carriedItemsParser
		);

		itemChargeParsers = activeParsers.stream()
			.filter(p -> p.getVaultType() == VaultType.ITEM_CHARGES)
			.collect(Collectors.toList());
	}

	public void startUp() {
		for (VaultParser parser : activeParsers) {
			eventBus.register(parser);
			if (parser instanceof AbstractVaultParser) {
				((AbstractVaultParser) parser).setupAccountHash();
			}
		}
		log.debug("Vault Manager initialized and registered {} parsers.", activeParsers.size());
	}

	public void shutDown() {
		for (VaultParser parser : activeParsers) {
			eventBus.unregister(parser);
		}
		log.debug("Vault Manager shut down successfully.");
	}

	/**
	 * Defers all aggregation and exporting logic exclusively to the Logger.
	 */
	public void writeMergedCsvFile()
	{
		itemVaultLogger.exportAggregatedData(itemChargeParsers);
	}

	public List<String> getExistingVaults(long accountHash)
	{
		return itemVaultLogger.getExistingVaults(accountHash);
	}

	public void resetVault() {}
}