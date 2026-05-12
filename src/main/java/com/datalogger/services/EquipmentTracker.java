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

import static com.datalogger.constants.Item.EQUIPMENT_CONTAINER_ID;
import com.datalogger.events.AccountSessionStarted;
import com.datalogger.models.enums.CombatType;
import com.datalogger.models.enums.EquippedWeaponType;
import com.datalogger.models.enums.ItemCharge;
import com.datalogger.models.enums.TrackedEquipment;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import static net.runelite.api.gameval.ItemID.BLOOD_AMULET;
import static net.runelite.api.gameval.VarPlayerID.COM_MODE;
import static net.runelite.api.gameval.VarbitID.COMBAT_WEAPON_CATEGORY;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.game.ItemVariationMapping;

@Singleton
@Slf4j
public class EquipmentTracker
{
	private final static int WEAPON_SLOT_INDEX = EquipmentInventorySlot.WEAPON.getSlotIdx();
	private final static int AMULET_SLOT_INDEX = EquipmentInventorySlot.AMULET.getSlotIdx();
	private final int DEFAULT_ATTACK_SPEED = 4;
	private final int SALAMANDER_WEAPON_TYPE = 24;

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Getter private int currentWeaponId = -2;
	@Getter private int baseWeaponId = -2;
	@Getter private boolean hasBloodFury = false;
	@Getter private boolean isTrackingWeaponCharges = false;
	@Getter private TrackedEquipment currentWeapon = null;
	@Getter private CombatType currentCombatType = null;
	@Getter private int attackStyleSoundId = -1;
	@Getter private int attackSpeed = DEFAULT_ATTACK_SPEED;
	@Getter private ItemEquipmentStats currentWeaponStats = null;
	@Getter private boolean canConsumeBloodFuryCharges = false;
	@Getter private int consumedBloodFuryCharges = 0;

	private int attackStyleIndex = -1;
	private int equippedWeaponType = -1;
	private final int[] cachedEquipment = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
	private final Map<Integer, Integer> trackedAttacks = new HashMap<>();

	public void updateCurrentWeapon()
	{
		int weaponId = cachedEquipment[WEAPON_SLOT_INDEX];
		log.debug("Updating carried weapon from {} to {}", baseWeaponId, ItemVariationMapping.map(weaponId));
		currentWeaponId = weaponId;
		baseWeaponId = weaponId != -1 ? ItemVariationMapping.map(weaponId) : -1;

		ItemStats stats = itemManager.getItemStats(weaponId);
		currentWeaponStats = stats == null ? null : stats.getEquipment();
		currentWeapon = TrackedEquipment.getByBaseId(baseWeaponId);

		isTrackingWeaponCharges = currentWeapon != null;
		updateCombatParameters();
	}

	private void updateCombatParameters()
	{
		log.debug("Updating combat parameters...");
		equippedWeaponType = client.getVarbitValue(COMBAT_WEAPON_CATEGORY);
		attackStyleIndex = client.getVarpValue(COM_MODE);

		EquippedWeaponType weaponType = EquippedWeaponType.getByVarbitValue(equippedWeaponType);
		currentCombatType = null;
		if (weaponType != null)
		{
			EquippedWeaponType.Stance stance = weaponType.getStance(attackStyleIndex);
			if (stance != null)
				currentCombatType = stance.getAttackType().getCombatType();
		}

		if (currentCombatType == null)
		{
			switch (equippedWeaponType)
			{
				case 3: case 5: case 7: case 19:
				currentCombatType = CombatType.RANGED;
				break;
				case 17:
				currentCombatType = CombatType.MAGIC;
				break;
				case 18:
					currentCombatType = (attackStyleIndex == 4) ? CombatType.MAGIC : CombatType.MELEE;
					break;
				case SALAMANDER_WEAPON_TYPE:
					if (attackStyleIndex == 0)
						currentCombatType = CombatType.MELEE;
					else if (attackStyleIndex == 1)
						currentCombatType = CombatType.RANGED;
					else
						currentCombatType = CombatType.MAGIC;
					break;
				default:
					currentCombatType = CombatType.MELEE;
			}
		}

		attackStyleSoundId = currentWeaponId == -1 || currentWeapon == null ? -1 : currentWeapon.getSoundIdForStyle(attackStyleIndex);

		if (currentWeaponStats != null)
		{
			if (currentCombatType == CombatType.RANGED)
			{
				attackSpeed = currentWeaponStats.getAspeed() - (attackStyleIndex == 1 ? 1 : 0);
			}
			else
			{
				attackSpeed = currentWeaponStats.getAspeed();
			}
		}
		else attackSpeed = DEFAULT_ATTACK_SPEED;
		canConsumeBloodFuryCharges = currentCombatType == CombatType.MELEE && hasBloodFury;

		EquippedWeaponType weaponEnum = EquippedWeaponType.getByVarbitValue(equippedWeaponType);
		String name = weaponEnum == null ? "Unknown" : weaponEnum.name();
		log.debug("Updated combat parameters. enum={} weapon={} baseId={} attackStyleIndex={} currentCombatType={} soundId={} equippedWeaponType={} attackSpeed={}", name, currentWeaponId, baseWeaponId, attackStyleIndex, currentCombatType.name(), attackStyleSoundId, equippedWeaponType, attackSpeed);
	}

	private void syncEquipment()
	{
		if (client.getGameState() != GameState.LOGGED_IN) return;
		ItemContainer equipment = client.getItemContainer(EQUIPMENT_CONTAINER_ID);
		if (equipment == null) return;

		Item[] currentItems = equipment.getItems();
		int nItems = currentItems.length;

		for (int slot = 0; slot < 14; slot++)
		{
			int itemId = (slot < nItems && currentItems[slot] != null) ? currentItems[slot].getId() : -1;
			cachedEquipment[slot] = itemId;
		}
		hasBloodFury = (cachedEquipment[AMULET_SLOT_INDEX] == BLOOD_AMULET);
		updateCurrentWeapon();
	}

	@Subscribe
	public void onAccountSessionStarted(AccountSessionStarted event)
	{
		Arrays.fill(cachedEquipment, -1);
		syncEquipment();
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarpId() == COM_MODE)
			updateCombatParameters();
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == EQUIPMENT_CONTAINER_ID)
		{
			ItemContainer equipment = event.getItemContainer();
			if (equipment == null) return;

			boolean weaponChanged = false;
			Item[] newItems = equipment.getItems();
			int nItems = newItems.length;
			for (int i = 0; i < 14; i++)
			{
				int newId = (i < nItems && newItems[i] != null) ? newItems[i].getId() : -1;
				if (cachedEquipment[i] != newId)
				{
					cachedEquipment[i] = newId;
					if (i == WEAPON_SLOT_INDEX) weaponChanged = true;
					else if (i == AMULET_SLOT_INDEX) hasBloodFury = newId == BLOOD_AMULET;
				}
			}
			if (weaponChanged) updateCurrentWeapon();
		}
	}

	// --- Methods invoked by CombatTracker to submit charge updates ---

	public void addWeaponAttack(int baseWeaponId)
	{
		trackedAttacks.merge(baseWeaponId, 1, Integer::sum);
	}

	public void incrementBloodFuryCharge()
	{
		consumedBloodFuryCharges++;
		log.debug("Increased consumedBloodFuryCharges to {}", consumedBloodFuryCharges);
	}

	// --- Charge retrieval and reset logic ---

	public int getAttackCount(int baseWeaponId)
	{
		return trackedAttacks.getOrDefault(trackedAttacks.containsKey(baseWeaponId) ? baseWeaponId : ItemVariationMapping.map(baseWeaponId), 0);
	}

	public int getAttackCount(ItemComposition itemComposition)
	{
		return trackedAttacks.getOrDefault(ItemVariationMapping.map(itemComposition.getId()), 0);
	}

	public int getAttackCount(Item item)
	{
		return trackedAttacks.getOrDefault(ItemVariationMapping.map(item.getId()), 0);
	}

	public Map<ItemCharge, Integer> getTrackedCharges()
	{
		Map<ItemCharge, Integer> chargesMap = new HashMap<>();
		for (Map.Entry<Integer, Integer> entry : trackedAttacks.entrySet())
		{
			ItemCharge mappedCharge = ItemCharge.getByBaseId(entry.getKey());
			int nCharges = entry.getValue();
			if (mappedCharge != null && nCharges > 0)
			{
				chargesMap.put(mappedCharge, nCharges);
			}
		}
		return chargesMap;
	}

	public void resetAttackCount()
	{
		trackedAttacks.clear();
		consumedBloodFuryCharges = 0;
	}

	public void resetAttackCount(int... itemIds)
	{
		for (int itemId : itemIds)
		{
			int baseId = ItemVariationMapping.map(itemId);
			trackedAttacks.put(baseId, 0);
		}
	}

	public boolean hasBloodFury()
	{
		return hasBloodFury;
	}

	public boolean canConsumeBloodFuryCharges()
	{
		return canConsumeBloodFuryCharges;
	}
}