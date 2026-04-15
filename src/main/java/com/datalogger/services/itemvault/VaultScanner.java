///*
// * Copyright (c) 2026, maximuis94 <https://github.com/maximuis94>
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are met:
// *
// * 1. Redistributions of source code must retain the above copyright notice, this
// *    list of conditions and the following disclaimer.
// * 2. Redistributions in binary form must reproduce the above copyright notice,
// *    this list of conditions and the following disclaimer in the documentation
// *    and/or other materials provided with the distribution.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
// * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//
//
//package com.datalogger.services.itemvault;
//
//import com.datalogger.DataLoggerConfig;
//import com.datalogger.events.AccountSessionStarted;
//import com.datalogger.events.DataLoggerConfigChanged;
//import com.datalogger.models.enums.VaultType;
//import com.datalogger.services.FileIOService;
//import com.google.gson.reflect.TypeToken;
//import java.io.File;
//import java.lang.reflect.Type;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//import javax.inject.Inject;
//import javax.inject.Singleton;
//import lombok.extern.slf4j.Slf4j;
//import net.runelite.api.Client;
//import net.runelite.api.gameval.InventoryID;
//import net.runelite.api.Item;
//import net.runelite.api.ItemContainer;
//import net.runelite.api.events.GameObjectSpawned;
//import net.runelite.api.events.GameStateChanged;
//import net.runelite.api.events.ItemContainerChanged;
//import net.runelite.api.events.WidgetLoaded;
//import net.runelite.api.gameval.ItemID;
//import net.runelite.client.eventbus.Subscribe;
//import net.runelite.client.game.ItemVariationMapping;
//
///**
// * Scanner class that keeps track of which vaults are unlocked, and should therefore be parsed.
// */
//@Slf4j
//@Singleton
//public class VaultScanner
//{
//	@Inject private Client client;
//	@Inject private FileIOService fileIOService;
//	@Inject private DataLoggerConfig config;
//
//	private boolean hasScannedBank = false;
//
//	// --- TRIGGER MAPPINGS ---
//
//	private static final Map<Integer, VaultType> ITEM_TRIGGERS = Map.of(
//		ItemID.BOOKOFSCROLLS_CHARGED, VaultType.MASTER_SCROLL_BOOK,
//		ItemID.BH_RUNE_POUCH, VaultType.RUNE_POUCH,
//		ItemID.DIVINE_RUNE_POUCH, VaultType.RUNE_POUCH,
//		ItemID.SCYTHE_OF_VITUR_UNCHARGED, VaultType.VYRE_WELL,
//		ItemID.DIZANAS_QUIVER_UNCHARGED, VaultType.QUIVER,
//		ItemID.XBOWS_BOLT_POUCH, VaultType.BOLT_POUCH
//
//		// POWERED STAVES
//	);
//
//	// 2. Regions (Map Region IDs to Vaults)
//	private static final Map<Integer, VaultType> REGION_TRIGGERS = Map.of(
//		12598, VaultType.SEED_VAULT
//	);
//
//	private static final Map<Integer, VaultType> OBJECT_TRIGGERS = Map.of(
//	);
//
//	private static final Map<Integer, VaultType> WIDGET_TRIGGERS = Map.of(
//		InventoryID.SEED_VAULT, VaultType.SEED_VAULT
//	);
//
//	private static final Set<VaultType> POSSIBLE_ITEM_VAULTS = new HashSet<>(ITEM_TRIGGERS.values());
//	private static final Set<VaultType> POSSIBLE_REGION_VAULTS = new HashSet<>(REGION_TRIGGERS.values());
//	private static final Set<VaultType> POSSIBLE_OBJECT_VAULTS = new HashSet<>(OBJECT_TRIGGERS.values());
//	private static final Set<VaultType> POSSIBLE_WIDGET_VAULTS = new HashSet<>(WIDGET_TRIGGERS.values());
//
//
//	// --- STATE MANAGEMENT ---
//
//	private long currentAccountHash = -1;
//	private File currentVaultCacheFile = null;
//	private boolean isActivelyScanning = false;
//
//	// We store VaultTypes directly!
//	private final Set<VaultType> unlockedVaults = new HashSet<>();
//
//	/**
//	 * Load cached unlocked vaults for the given accountHash, if there is any.
//	 */
//	private void loadSession(long accountHash)
//	{
//		this.currentAccountHash = accountHash;
//		currentVaultCacheFile = fileIOService.getInternalVaultFile(VaultType.)VaultType.getUnlockedVaultCacheFile(accountHash);
//		isActivelyScanning = config.logItemVault();
//
//		this.unlockedVaults.clear();
//		hasScannedBank = false;
//		Type type = new TypeToken<Set<VaultType>>() {}.getType();
//		Set<VaultType> loaded = fileIOService.readJson(currentVaultCacheFile, type);
//
//		if (loaded != null)
//		{
//			unlockedVaults.addAll(loaded);
//		}
//	}
//
//	public void updateConfigurations()
//	{
//		isActivelyScanning = config.logItemVault() && currentAccountHash != -1;
//	}
//
//	/**
//	 * Single point of entry for unlocking and saving a vault.
//	 */
//	private void unlockVault(VaultType vaultType)
//	{
//		if (vaultType != null && unlockedVaults.add(vaultType))
//		{
//			log.debug("Actively scanning vault: {}", vaultType.name());
//			saveToDisk();
//		}
//	}
//
//	/**
//	 * Return true if the vault is unlocked
//	 */
//	public boolean isVaultUnlocked(VaultType vaultType)
//	{
//		return unlockedVaults.contains(vaultType);
//	}
//
//	/**
//	 * Saves the vault unlocks for the loaded account locally
//	 */
//	private void saveToDisk()
//	{
//		if (!isActivelyScanning)
//		{
//			log.error("Unable to save cached vault data-- accountHash is -1.");
//			return;
//		}
//		fileIOService.writeJson(currentVaultCacheFile, unlockedVaults);
//	}
//
//	@Subscribe
//	public void onAccountSessionStarted(AccountSessionStarted event)
//	{
//		loadSession(event.getAccountHash());
//	}
//
//	@Subscribe
//	public void onDataLoggerConfigChanged(DataLoggerConfigChanged event)
//	{
//		updateConfigurations();
//	}
//
//	@Subscribe
//	public void onItemContainerChanged(ItemContainerChanged event)
//	{
//		if (!isActivelyScanning) return;
//
//		if (unlockedVaults.containsAll(POSSIBLE_ITEM_VAULTS)) return;
//
//		int containerId = event.getContainerId();
//
//		if (containerId == InventoryID.BANK)
//		{
//			if (hasScannedBank) return;
//
//			hasScannedBank = true;
//			log.debug("Bank opened. Scanning for trigger items for the first and only time this session.");
//		}
//		else if (containerId != InventoryID.INV &&
//			containerId != InventoryID.WORN)
//		{
//			return;
//		}
//
//		ItemContainer container = event.getItemContainer();
//		for (Item item : container.getItems())
//		{
//			int baseId = ItemVariationMapping.map(item.getId());
//			unlockVault(ITEM_TRIGGERS.get(baseId));
//		}
//	}
//
//	@Subscribe
//	public void onGameStateChanged(GameStateChanged event)
//	{
//		if (!isActivelyScanning) return;
//
//		if (unlockedVaults.containsAll(POSSIBLE_REGION_VAULTS)) return;
//
//		if (event.getGameState() == net.runelite.api.GameState.LOGGED_IN)
//		{
//			if (client.getLocalPlayer() != null)
//			{
//				int regionId = client.getLocalPlayer().getWorldLocation().getRegionID();
//				unlockVault(REGION_TRIGGERS.get(regionId));
//			}
//		}
//	}
//
//	@Subscribe
//	public void onGameObjectSpawned(GameObjectSpawned event)
//	{
//		if (!isActivelyScanning) return;
//
//		if (unlockedVaults.containsAll(POSSIBLE_OBJECT_VAULTS)) return;
//
//		int objectId = event.getGameObject().getId();
//		unlockVault(OBJECT_TRIGGERS.get(objectId));
//	}
//
//	@Subscribe
//	public void onWidgetLoaded(WidgetLoaded event)
//	{
//		if (!isActivelyScanning) return;
//
//		if (unlockedVaults.containsAll(POSSIBLE_WIDGET_VAULTS)) return;
//
//		int groupId = event.getGroupId();
//		unlockVault(WIDGET_TRIGGERS.get(groupId));
//	}
//}