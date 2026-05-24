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

import com.datalogger.DataLoggerConfig;
import static com.datalogger.constants.Item.EQUIPMENT_CONTAINER_ID;
import static com.datalogger.constants.Item.INVENTORY_CONTAINER_ID;
import static com.datalogger.constants.Item.RUNE_POUCH_AMOUNT_VARBITS;
import static com.datalogger.constants.Item.RUNE_POUCH_TYPE_VARBITS;
import static com.datalogger.constants.PluginConstants.UNMUTE_COOLDOWN_SECONDS;
import com.datalogger.dto.TrackedSuppliesDTO;
import com.datalogger.models.enums.ConsumableItemGroup;
import com.datalogger.models.enums.ItemCharge;
import com.datalogger.models.enums.TrackedEquipment;
import com.datalogger.models.supplytracker.SupplySnapshot;
import com.datalogger.models.supplytracker.TrackedSupplies;
import com.datalogger.models.supplytracker.ValuedItemStack;
import java.io.File;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.EnumID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.Preferences;
import net.runelite.api.events.VarbitChanged;
import static net.runelite.api.gameval.ItemID.BH_RUNE_POUCH;
import static net.runelite.api.gameval.ItemID.BLOOD_AMULET;
import static net.runelite.api.gameval.ItemID.DIVINE_RUNE_POUCH;
import static net.runelite.api.gameval.ItemID.DIZANAS_QUIVER_BROKEN;
import static net.runelite.api.gameval.ItemID.DIZANAS_QUIVER_INFINITE_BROKEN;
import static net.runelite.api.gameval.ItemID.SKILLCAPE_MAX_DIZANAS_BROKEN;
import static net.runelite.api.gameval.VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO;
import static net.runelite.api.gameval.VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO_AMOUNT;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;

@Slf4j
@Singleton
public class SupplyTracker
{
	private final Client client;

	private final ItemManager itemManager;

	private final EquipmentTracker equipmentTracker;

	private final FileIOService fileIOService;

	private final DataLoggerConfig config;

	@Setter
	private String tag = null;

	private Instant nextUnmuteTimestamp = null;
	private boolean hasUnmutedAudio = false;
	private boolean hasWeaponWithCharges = false;

	@Getter
	private boolean isTracking = false;
	private SupplySnapshot initialSupplies;
	private Map<Integer, Integer> inventory;
	private Map<ConsumableItemGroup, Integer> doses;

	private boolean hasRunePouch;
	private boolean hasQuiver;
	private int quiverItemId;
	private ItemComposition quiverItemComposition;
	private int quiverItemQuantity;

	private final int RUNE_POUCH_SLOTS = RUNE_POUCH_TYPE_VARBITS.length;

	@Inject
	public SupplyTracker(Client client, ItemManager itemManager, EquipmentTracker equipmentTracker, FileIOService fileIOService, DataLoggerConfig config)
	{
		this.client = client;
		this.itemManager = itemManager;
		this.equipmentTracker = equipmentTracker;
		this.fileIOService = fileIOService;
		this.config = config;
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
			log.debug("Updated quiver arrow quantity to {}", quiverItemQuantity);
		}
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
		log.debug("Parsed initial quiver contents {}x {}", quiverItemQuantity, itemComposition.getName());
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
					log.debug("Parsing Rune pouch contents...");
				log.debug("Added {}x itemId={} to rune pouch", quantity, itemId);
			}
		}
		return pouch;
	}

	/**
	 * Update the corresponding flags of the given base itemId.
	 */
	private void updateItemFlag(int baseItemId)
	{
		if (!hasWeaponWithCharges && TrackedEquipment.getByBaseId(baseItemId) != null && baseItemId != BLOOD_AMULET)
		{
			hasWeaponWithCharges = true;
			log.debug("Detected a weapon that consumes charges");
		}
		else if (baseItemId == BH_RUNE_POUCH || baseItemId == DIVINE_RUNE_POUCH)
		{
			hasRunePouch = true;
			log.debug("Enabled tracking of Rune pouch contents");
		}
		else if (baseItemId == DIZANAS_QUIVER_INFINITE_BROKEN   || baseItemId == SKILLCAPE_MAX_DIZANAS_BROKEN || baseItemId == DIZANAS_QUIVER_BROKEN)
		{
			hasQuiver = true;
			initializeQuiver();
		}
	}

	private void updateTrackAttackAnims()
	{
		hasWeaponWithCharges = false;
		hasRunePouch = false;
		hasQuiver = false;

		for (Integer itemId : inventory.keySet())
		{
			updateItemFlag(ItemVariationMapping.map(itemId));
		}

		updateItemFlag(equipmentTracker.getBaseWeaponId());
	}

	/**
	 * Parse the inventory and various other containers and cache the counted values. The snapshot produced is used as
	 * initial state of the Inventory.
	 */
	private void parseInitialItemContainers()
	{
		inventory = parseInventory();

		doses = extractDoses(inventory);

		inventory = mergeMaps(inventory, parseEquipment());

		updateTrackAttackAnims();

		if (hasRunePouch)
		{
			inventory = mergeMaps(inventory, parseRunePouch());
		}

		if (hasQuiver)
		{
			inventory.merge(quiverItemId, quiverItemQuantity, Integer::sum);
		}

		initialSupplies = new SupplySnapshot(inventory, doses);
	}


	/**
	 * Parse and return the current contents of the inventory and its containers as a SupplySnapshot
	 */
	private SupplySnapshot getInventorySnapshot()
	{
		Map<Integer, Integer> inventory = parseInventory();

		Map<ConsumableItemGroup, Integer> doses = extractDoses(inventory);

		inventory = mergeMaps(inventory, parseEquipment());

		if (hasRunePouch)
		{
			inventory = mergeMaps(inventory, parseRunePouch());
		}

		if (hasQuiver)
		{
			log.debug("Adding quiver contents {}x {} into inventory", quiverItemQuantity, quiverItemComposition.getName());
			inventory.merge(quiverItemId, quiverItemQuantity, Integer::sum);
		}

		return new SupplySnapshot(inventory, doses);
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
	 * (Re)start tracking supplies by resetting all counters/values and by setting isTracking to true.
	 */
	public void startTracking()
	{
		tag = null;
		isTracking = true;
		parseInitialItemContainers();
		equipmentTracker.resetAttackCount();
		log.debug("Starting tracking of supplies");
	}

	/**
	 * Stop tracking supplies. Use directory as output directory and fileName as the base file name. Store the results
	 * in file paths composed of directory, fileName and the appropriate extension.
	 */
	public void stopTracking(File directory, String fileName, boolean writeJson, boolean writeCsv)
	{
		isTracking = false;
		TrackedSupplies supplies = getConsumedItems();
		if (supplies != null)
		{
			TrackedSuppliesDTO suppliesDTO = supplies.toDto(tag);
			if (writeJson) fileIOService.exportToJson(new File(directory, fileName+".json"), suppliesDTO);
			if (writeCsv) fileIOService.exportToCsv(new File(directory, fileName+".csv"), suppliesDTO);
		}
		else
		{

			log.debug("Supplies is null...");
		}

//		restoreMutedSoundEffects();
	}


	/**
	 * Compare the initial state with the current state and return the difference as a TrackedSupplies instance that
	 * describes all consumed supplies and item charges.
	 */
	public TrackedSupplies getConsumedItems()
	{
		log.debug("Fetching consumed items...");
		SupplySnapshot snapshot = getInventorySnapshot();

		Map<ConsumableItemGroup, Integer> consumedDoses = new HashMap<>();
		Map<Integer, Integer> consumedItems = new HashMap<>();
		Map<String, ValuedItemStack> namedItems = new HashMap<>();
		int[] totalValue = {0};

		inventory.forEach((item, startQty) -> {
			int endQty = snapshot.getInventory().getOrDefault(item, 0);
			int diff = startQty - endQty;

			if (diff > 0)
			{
				ItemComposition itemComposition = itemManager.getItemComposition(item);
				log.debug("Consumed item: {} x{}", itemComposition.getName(), diff);
				consumedItems.put(item, diff);
				int price = itemManager.getItemPrice(item);
				int value = diff * (price > 0 ? price : itemComposition.getHaPrice());
				totalValue[0] += value;
				namedItems.put(itemComposition.getName(), new ValuedItemStack(diff, value));
			}
		});

		Map<String, ValuedItemStack> namedDoses = new HashMap<>();
		doses.forEach((itemGroup, startQty) -> {
			int endQty = snapshot.getDoses().getOrDefault(itemGroup, 0);
			int diff = startQty - endQty;

			if (diff > 0)
			{
				log.debug("Consumed dose: {} x{}", itemGroup.getBaseItemName(), diff);
				consumedDoses.put(itemGroup, diff);
				int value = itemGroup.getDoseValue() * diff;
				totalValue[0] += value;
				namedDoses.put(itemGroup.getBaseItemName(), new ValuedItemStack(diff, value));
			}
		});

		Map<String, ValuedItemStack> namedCharges = new HashMap<>();
		Map<ItemCharge, Integer> consumedCharges = equipmentTracker.getTrackedCharges();
		if (consumedCharges != null)
		{
			consumedCharges.forEach((charge, qty) -> {
				int value = charge.getChargeValue() * qty;

				totalValue[0] += value; // Accumulate total
				namedCharges.put(charge.getFormattedName(), new ValuedItemStack(qty, value));
			});
		}

		return new TrackedSupplies(consumedItems, consumedDoses, consumedCharges, namedItems, namedDoses, namedCharges, totalValue[0]);
	}
}