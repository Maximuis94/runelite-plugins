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

package com.datalogger.services;

import static com.datalogger.constants.Item.RUNE_POUCH_AMOUNT_VARBITS;
import static com.datalogger.constants.Item.RUNE_POUCH_TYPE_VARBITS;
import com.datalogger.dto.TrackedSuppliesDTO;
import com.datalogger.models.enums.ConsumableItemGroup;
import com.datalogger.models.supplytracker.TrackedSupplies;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.VarbitChanged;
import static net.runelite.api.gameval.AnimationID.SCYTHE_OF_VITUR_ATTACK;
import static net.runelite.api.gameval.AnimationID.TOA_SOT_CAST_A;
import static net.runelite.api.gameval.AnimationID.TOA_SOT_CAST_B;
import static net.runelite.api.gameval.ItemID.BH_RUNE_POUCH;
import static net.runelite.api.gameval.ItemID.DIVINE_RUNE_POUCH;
import static net.runelite.api.gameval.ItemID.DIZANAS_QUIVER_BROKEN;
import static net.runelite.api.gameval.ItemID.DIZANAS_QUIVER_INFINITE_BROKEN;
import static net.runelite.api.gameval.ItemID.SCYTHE_OF_VITUR;
import static net.runelite.api.gameval.ItemID.SKILLCAPE_MAX_DIZANAS_BROKEN;
import static net.runelite.api.gameval.ItemID.TUMEKENS_SHADOW;
import static net.runelite.api.gameval.VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO;
import static net.runelite.api.gameval.VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO_AMOUNT;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;

;

@Slf4j
@Singleton
public class SupplyTracker
{
	private final Client client;

	private final ItemManager itemManager;

	private final WeaponTracker weaponTracker;

	private final FileIOService fileIOService;

	// 93 is the raw ID for the player's main 28-slot inventory.
	private static final int INVENTORY_CONTAINER_ID = 93;
	private static final int EQUIPMENT_CONTAINER_ID = 94;

	@Getter
	private boolean isTracking = false;
	private Map<Integer, Integer> inventory;
	private Map<ConsumableItemGroup, Integer> doses;

	private boolean trackAttackAnims;
	private int nextAttackTick;

	private boolean hasScythe;
	private int scytheAttacks;

	private boolean hasShadow;
	private int shadowAttacks;

	private boolean hasRunePouch;
	private boolean hasQuiver;
	private int quiverItemId;
	private ItemComposition quiverItemComposition;
	private int quiverItemQuantity;

	private final int RUNE_POUCH_SLOTS = RUNE_POUCH_TYPE_VARBITS.length;

	@Inject
	public SupplyTracker(Client client, ItemManager itemManager, WeaponTracker weaponTracker, FileIOService fileIOService)
	{
		this.client = client;
		this.itemManager = itemManager;
		this.weaponTracker = weaponTracker;
		this.fileIOService = fileIOService;
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (!isTracking) return;

		if (hasQuiver && event.getVarpId() == DIZANAS_QUIVER_TEMP_AMMO_AMOUNT)
		{
			int newQuantity = client.getVarpValue(DIZANAS_QUIVER_TEMP_AMMO_AMOUNT);

			// Prevents the data to be purged on death
			if (quiverItemQuantity - newQuantity > 5 && newQuantity == 0) return;

			quiverItemQuantity = newQuantity;
			log.info("Updated quiver arrow quantity to {}", quiverItemQuantity);
		}
	}

	/**
	 * Update tracked attack counters, given animId and tickCount, if they are associated with a tracked weapon.
	 */
	private void updateAttackCounters(int animId, int tickCount)
	{
		switch (weaponTracker.getBaseWeaponId())
		{
			case SCYTHE_OF_VITUR:
				if (animId == SCYTHE_OF_VITUR_ATTACK)
				{
					nextAttackTick = tickCount + 2;
					scytheAttacks++;

					log.info("Scythe attacks increased to {}", scytheAttacks);
					break;
				}
			case TUMEKENS_SHADOW:
				if (animId == TOA_SOT_CAST_A || animId == TOA_SOT_CAST_B)
				{
					nextAttackTick = tickCount + 2;
					shadowAttacks++;
					log.info("Shadow attacks increased to {}", shadowAttacks);
					break;
				}
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (!isTracking || !trackAttackAnims || event.getActor() != client.getLocalPlayer()) return;

		int tickCount = client.getTickCount();
		if (tickCount < nextAttackTick) return;

		Actor player = event.getActor();
		updateAttackCounters(player.getAnimation(), tickCount);
	}

	/**
	 * Parse the contents of Dizana's quiver and update the corresponding class variables/flags. Once set, the values
	 * will tracked using onVarbitChanged if any arrows are detected in the quiver.
	 */
	private void initializeQuiver()
	{
		quiverItemId = client.getVarpValue(DIZANAS_QUIVER_TEMP_AMMO);
		if (quiverItemId == -1)
		{
			hasQuiver = false;
			quiverItemQuantity = 0;
			return;
		}

		ItemComposition itemComposition = itemManager.getItemComposition(quiverItemId);
		quiverItemComposition = itemComposition;
		quiverItemQuantity = client.getVarpValue(DIZANAS_QUIVER_TEMP_AMMO_AMOUNT);
		log.info("Parsed initial quiver contents {}x {}", quiverItemQuantity, itemComposition.getName());
	}

	/**
	 * Parse the inventory items container and return the result as an itemId, quantity mapping.
	 */
	private Map<Integer, Integer> parseInventory()
	{
		Map<Integer, Integer> inventoryMap = new HashMap<>();

		ItemContainer inventoryContainer = client.getItemContainer(INVENTORY_CONTAINER_ID);

		if (inventoryContainer == null)
		{
			return inventoryMap;
		}
		Item[] items = inventoryContainer.getItems();

		for (Item item : items)
		{
			if (item == null || item.getId() == -1) continue;

			inventoryMap.merge(item.getId(), item.getQuantity(), Integer::sum);
		}
		return inventoryMap;
	}

	/**
	 * Convert all items with a ConsumableItemGroup to an amount of doses and remove them from inventoryItems while
	 * doing so. Return the result as a ConsumableItemGroup, quantity mapping.
	 */
	private Map<ConsumableItemGroup, Integer> extractDoses(Map<Integer, Integer> inventoryItems)
	{
		Map<ConsumableItemGroup, Integer> output = new HashMap<>();
		Iterator<Map.Entry<Integer, Integer>> iterator = inventoryItems.entrySet().iterator();

		while (iterator.hasNext())
		{
			Map.Entry<Integer, Integer> entry = iterator.next();
			int itemId = entry.getKey();
			int quantity = entry.getValue();

			if (ConsumableItemGroup.hasGroup(itemId))
			{
				iterator.remove();

				Map<ConsumableItemGroup, Integer> converted = ConsumableItemGroup.convertToDoses(itemId, quantity);

				if (converted != null)
				{
					converted.forEach((group, nDoses) ->
						output.merge(group, nDoses, Integer::sum)
					);
				}
			}
		}
		return output;
	}

	/**
	 * Parse the equipped items container and return the result as an itemId, quantity mapping.
	 */
	private Map<Integer, Integer> parseEquipment()
	{
		Map<Integer, Integer> output = new HashMap<>();

		ItemContainer equipmentContainer = client.getItemContainer(EQUIPMENT_CONTAINER_ID);

		if (equipmentContainer == null)
		{
			return output;
		}
		Item[] items = equipmentContainer.getItems();

		for (Item item : items)
		{
			if (item == null || item.getId() == -1) continue;

			int itemId = item.getId();
			output.merge(itemId, item.getQuantity(), Integer::sum);
		}
		return output;
	}

	/**
	 * Parse the contents of the Rune pouch via its varbits and return it as an itemId, quantity mapping
	 */
	private Map<Integer, Integer> parseRunePouch()
	{
		final EnumComposition runepouchEnum = client.getEnum(EnumID.RUNEPOUCH_RUNE);
		Map<Integer, Integer> pouch = new HashMap<>();

		for (int slotId = 0; slotId < RUNE_POUCH_SLOTS; slotId++)
		{
			int runeId = client.getVarbitValue(RUNE_POUCH_TYPE_VARBITS[slotId]);
			if (runeId > 0)
			{
				int itemId = runepouchEnum.getIntValue(runeId);
				int quantity = client.getVarbitValue(RUNE_POUCH_AMOUNT_VARBITS[slotId]);
				pouch.put(itemId, quantity);

				if (pouch.size() == 1)
					log.info("Parsing Rune pouch contents...");
				log.info("Added {}x itemId={} to rune pouch", quantity, itemId);
			}
		}
		return pouch;
	}

	/**
	 * Update the corresponding flags of the given itemId.
	 */
	private void updateItemFlag(int itemId)
	{
		if (itemId == TUMEKENS_SHADOW)
		{
			hasShadow = true;
			log.info("Enabled tracking of Tumeken's shadow attacks");
		}
		else if (itemId == SCYTHE_OF_VITUR)
		{
			hasScythe = true;
			log.info("Enabled tracking of Scythe of vitur attacks");
		}
		else if (itemId == BH_RUNE_POUCH || itemId == DIVINE_RUNE_POUCH)
		{
			hasRunePouch = true;
			log.info("Enabled tracking of Rune pouch contents");
		}
		else if (itemId == DIZANAS_QUIVER_INFINITE_BROKEN   || itemId == SKILLCAPE_MAX_DIZANAS_BROKEN || itemId == DIZANAS_QUIVER_BROKEN)
		{
			hasQuiver = true;
			initializeQuiver();
		}
	}

	private void updateTrackAttackAnims()
	{
		hasScythe = false;
		hasShadow = false;
		hasRunePouch = false;
		hasQuiver = false;

		for (Integer itemId : inventory.keySet())
		{
			updateItemFlag(ItemVariationMapping.map(itemId));
		}

		updateItemFlag(weaponTracker.getBaseWeaponId());

		trackAttackAnims =
			hasScythe ||
				hasShadow;
	}

	/**
	 * Parse the inventory and various other containers and cache the counted values.
	 * @param updateFlags set to true if various flags require to be updated, e.g. when starting the tracking.
	 */
	private void parseItemContainers(boolean updateFlags)
	{
		inventory = parseInventory();

		doses = extractDoses(inventory);

		inventory = mergeMaps(inventory, parseEquipment());

		if (updateFlags) updateTrackAttackAnims();

		if (hasRunePouch)
		{
			inventory = mergeMaps(inventory, parseRunePouch());
		}

		if (hasQuiver)
		{
			log.info("Adding quiver contents {}x {} into inventory", quiverItemQuantity, quiverItemComposition.getName());
			inventory.merge(quiverItemId, quiverItemQuantity, Integer::sum);
		}
	}

	/**
	 * Merge map1 into map2 and return the combined map.
	 */
	public Map<Integer, Integer> mergeMaps(Map<Integer, Integer> map1, Map<Integer, Integer> map2)
	{
		Map<Integer, Integer> combined = new HashMap<>(map1);
		map2.forEach((composition, quantity) ->
			combined.merge(composition, quantity, Integer::sum)
		);
		return combined;
	}

	/**
	 * Start tracking supplies. Reset all counters and values and set isTracking to true
	 */
	public void startTracking()
	{
		isTracking = true;
		scytheAttacks = 0;
		shadowAttacks = 0;
		parseItemContainers(true);
		log.info("Starting tracking of supplies");
	}

	/**
	 * Stop tracking supplies. Use directory as output directory and fileName as the base file name. Store the results
	 * in file paths composed of directory, fileName and the appropriate extension.
	 */
	public void stopTracking(File directory, String fileName, boolean writeJson, boolean writeCsv)
	{
		isTracking = false;
		TrackedSupplies supplies = getConsumedItems();
		TrackedSuppliesDTO suppliesDTO = supplies.toDto(itemManager);

		if (writeJson) fileIOService.exportToJson(new File(directory, fileName+".json"), suppliesDTO);
		if (writeCsv) fileIOService.exportToCsv(new File(directory, fileName+".csv"), suppliesDTO);
	}

//	public void stopTrackingDebug()
//	{
//		parseRegisteredProjectiles();
//
//		TrackedSupplies supplies = getConsumedItems();
//		TrackedSuppliesDTO suppliesDTO = supplies.toDto(itemManager);
//		fileIOService.exportToJson(new File(FileIOService.PLUGIN_ROOT, "supply-test.json"), suppliesDTO);
//		fileIOService.exportToCsv(new File(FileIOService.PLUGIN_ROOT, "supply-test.csv"), suppliesDTO);
//	}

	/**
	 * Compare the initial state with the current state and return the difference as a set of consumed items
	 */
	public TrackedSupplies getConsumedItems()
	{
		Map<Integer, Integer> initialInventory = new HashMap<>(inventory);
		Map<ConsumableItemGroup, Integer> initialDoses = new HashMap<>(doses);
		parseItemContainers(false);
		Map<Integer, Integer> currentInventory = new HashMap<>(inventory);
		Map<ConsumableItemGroup, Integer> currentDoses = new HashMap<>(doses);
		inventory = new HashMap<>(initialInventory);
		doses = new HashMap<>(initialDoses);

		Map<ConsumableItemGroup, Integer> consumedDoses = new HashMap<>();
		Map<Integer, Integer> consumedItems = new HashMap<>();
		initialInventory.forEach((item, startQty) -> {
			int endQty = currentInventory.getOrDefault(item, 0);
			int diff = startQty - endQty;

			if (diff > 0)
			{
				ItemComposition itemComposition = itemManager.getItemComposition(item);
				log.info("Consumed: {} x{}", itemComposition.getName(), diff);
				consumedItems.put(item, diff);
			}
		});
		initialDoses.forEach((itemGroup, startQty) -> {
			int endQty = currentDoses.getOrDefault(itemGroup, 0);
			int diff = startQty - endQty;

			if (diff > 0)
			{
				log.info("Consumed: {} x{}", itemGroup.getBaseItemName(), diff);
				consumedDoses.put(itemGroup, diff);
			}
		});

		return new TrackedSupplies(consumedItems, consumedDoses, scytheAttacks, shadowAttacks);
	}


}