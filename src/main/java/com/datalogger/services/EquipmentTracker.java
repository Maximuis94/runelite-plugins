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
import com.datalogger.models.enums.AttackSourceTracker;
import com.datalogger.models.enums.CombatType;
import com.datalogger.models.enums.EquippedWeaponType;
import com.datalogger.models.enums.ItemCharge;
import com.datalogger.models.enums.TrackedEquipment;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Hitsplat;
import static net.runelite.api.HitsplatID.BLOCK_ME;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.FakeXpDrop;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import static net.runelite.api.gameval.ItemID.BLOOD_AMULET;
import static net.runelite.api.gameval.VarPlayerID.COM_MODE;
import static net.runelite.api.gameval.VarbitID.COMBAT_WEAPON_CATEGORY;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.game.ItemVariationMapping;

/**
 * Tracking service for equipment-related information and variables directly related to equipment worn, like estimated
 * weapon/armour charge mutations.
 */
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

	@Getter
	private int currentWeaponId = -2;

	@Getter
	private int baseWeaponId = -2;

	private boolean hasBloodFury = false;

	private boolean isTrackingWeaponCharges = false;

	private TrackedEquipment currentWeapon = null;

	private int attackStyleIndex = -1;
	private CombatType currentCombatType = null;
	private int attackStyleSoundId = -1;
	private int equippedWeaponType = -1;
	private final int[] cachedEquipment = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

	@Getter
	private int attackSpeed = DEFAULT_ATTACK_SPEED;

	@Getter
	private ItemEquipmentStats currentWeaponStats = null;

	@Getter
	private int consumedBloodFuryCharges = 0;

	private final Map<Integer, Integer> trackedAttacks = new HashMap<>();

	private int lastAttackTick = -1;
	private int nextAttackTick = -1;
	private boolean receivedHpExp = false;
	private boolean receivedCombatExp = false;
	private boolean registeredExpAttack = false;
	private boolean isCurrentlyRegisteringAttack = false;
	private final Map<Skill, Integer> previousXp = new EnumMap<>(Skill.class);

	/**
	 * Updates the hard-coded weapon ID and applies subsequent changes. Should be called following a weapon swap.
	 */
	public void updateCurrentWeapon()
	{
		int weaponId = cachedEquipment[WEAPON_SLOT_INDEX];
		log.info("Updating carried weapon from {} to {}", baseWeaponId, ItemVariationMapping.map(weaponId));
		currentWeaponId = weaponId;
		baseWeaponId = weaponId != -1 ? ItemVariationMapping.map(weaponId) : -1;

		ItemStats stats = itemManager.getItemStats(weaponId);
		currentWeaponStats = stats == null ? null : stats.getEquipment();
		currentWeapon = TrackedEquipment.getByBaseId(baseWeaponId);

		isTrackingWeaponCharges = currentWeapon != null;
		updateCombatParameters();
	}

	/**
	 * Update attack style and equipped weapon type varbit values and variables derived from these values.
	 */
	private void updateCombatParameters()
	{
		log.info("Updating combat parameters...");
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
			log.debug("Detected an EquippedWeaponType that has not been registered yet for COMBAT_WEAPON_CATEGORY varbit value {}", equippedWeaponType);

			switch (equippedWeaponType)
			{
				case 3:
				case 5:
				case 7:
				case 19:
					currentCombatType = CombatType.RANGED;
					break;

				case 17:
				case 24:
					currentCombatType = CombatType.MAGIC;
					break;

				case 18: // Standard Staves (can be melee OR magic)
					// Style 4 is usually the "Spell" autocast selected via the spellbook
					// Styles 0, 1, 2 are Bash/Pound (Melee)
					currentCombatType = (attackStyleIndex == 4) ? CombatType.MAGIC : CombatType.MELEE;
					break;

				default:
					currentCombatType = CombatType.MELEE;
			}
		}

		attackStyleSoundId = currentWeaponId == -1 || currentWeapon == null ? -1 : currentWeapon.getSoundIdForStyle(attackStyleIndex);

		if (currentWeaponStats != null)
		{
			if (currentCombatType == CombatType.RANGED && equippedWeaponType != SALAMANDER_WEAPON_TYPE)
			{
				attackSpeed = currentWeaponStats.getAspeed() - (attackStyleIndex == 1 ? 1 : 0);
			}
			else
			{
				attackSpeed = currentWeaponStats.getAspeed();
			}
		}
		else attackSpeed = DEFAULT_ATTACK_SPEED;

		log.info("Updated combat parameters. weapon={} attackStyleIndex={} currentCombatType={} soundId={} equippedWeaponType={} attackSpeed={}", currentWeaponId, attackStyleIndex, currentCombatType.name(), attackStyleSoundId, equippedWeaponType, attackSpeed);

	}

	/**
	 * Track whether or not to count the hitsplat towards blood fury charges.
	 * NOTE: it does not cover edge cases exhaustively; it may proof inaccurate for manual casting / certain special
	 * attacks while the active CombatStyle is set to MELEE, or if a switch to a melee weapon is made while a
	 * projectile has not struck yet, among others. For now, keep it simple.
	 */
	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		if (!hasBloodFury || currentCombatType != CombatType.MELEE) return;

		Hitsplat hitsplat = event.getHitsplat();
		if (hitsplat.isMine() && hitsplat.getHitsplatType() != BLOCK_ME)
		{
			consumedBloodFuryCharges++;
			log.info("Increased consumedBloodFuryCharges to {}", consumedBloodFuryCharges);
		}
	}

	/**
	 * Sync the cached equipment item ids with the current equipment container. Acts as if all cached values have
	 * changed. Is to be called on startup and whenever a new account logs on.
	 */
	private void syncEquipment()
	{
		if (client.getGameState() != GameState.LOGGED_IN) return;

		ItemContainer equipment = client.getItemContainer(EQUIPMENT_CONTAINER_ID);
		if (equipment == null) return;

		Item[] currentItems = equipment.getItems();
		int nItems = currentItems.length;

		for (int slot = 0; slot < 14; slot++)
		{
			int itemId = (slot < nItems && currentItems[slot] != null)
				? currentItems[slot].getId()
				: -1;

			cachedEquipment[slot] = itemId;
		}
		hasBloodFury = (cachedEquipment[AMULET_SLOT_INDEX] == BLOOD_AMULET);
		updateCurrentWeapon();

		log.debug("Equipment was synced forcefully");
	}

	@Subscribe
	public void onAccountSessionStarted(AccountSessionStarted event)
	{
		Arrays.fill(cachedEquipment, -1);
		syncEquipment();
	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		if (!isTrackingWeaponCharges) return;

		int tickCount = client.getTickCount();
		if (checkAndUpdateXpAttackTrackerFlags(tickCount)) return;

		if (attackStyleSoundId == event.getSoundId())
		{
			registerAttack(tickCount, AttackSourceTracker.SOUND);
		}
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

					if (i == WEAPON_SLOT_INDEX)
						weaponChanged = true;

					else if (i == AMULET_SLOT_INDEX)
					{
						hasBloodFury = newId == BLOOD_AMULET;
						log.info("hasBloodFury was set to {}", hasBloodFury);
					}
				}
			}

			if (weaponChanged) updateCurrentWeapon();
		}
	}

	/**
	 * Track attacks made by the player using the animation ID, but only if the weapon worn is a TrackedEquipment
	 */
	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (currentWeapon == null || event.getActor() != client.getLocalPlayer()) return;

		int tickCount = client.getTickCount();
		if (checkAndUpdateXpAttackTrackerFlags(tickCount)) return;

		int animId = event.getActor().getAnimation();
		if (currentWeapon.isAttackAnimation(animId))
		{
			registerAttack(tickCount, AttackSourceTracker.ANIMATION);
		}
	}

	/**
	 * Use xp drops to determine if the player attacked
	 */
	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (!isTrackingWeaponCharges) return;

		int tickCount = client.getTickCount();
		if (checkAndUpdateXpAttackTrackerFlags(tickCount)) return;

		Skill skill = event.getSkill();
		int currentXp = event.getXp();
		int previous = previousXp.getOrDefault(skill, -1);

		if (previous != -1 && currentXp > previous)
		{
			processCombatXpDrop(tickCount, skill);
		}

		previousXp.put(skill, currentXp);
	}

	@Subscribe
	public void onFakeXpDrop(FakeXpDrop event)
	{
		if (!isTrackingWeaponCharges) return;
		int tickCount = client.getTickCount();
		if (checkAndUpdateXpAttackTrackerFlags(tickCount)) return;
		processCombatXpDrop(tickCount, event.getSkill());
	}

	/**
	 * Verify exp tracking flags used for tracking attacks. If they are expired, reset them and return false. If they are
	 * still valid, return whether attacks have already been registered for this tick.
	 */
	private boolean checkAndUpdateXpAttackTrackerFlags(int tickCount)
	{
		if (tickCount < nextAttackTick) return true;
		if (lastAttackTick == tickCount) return registeredExpAttack;

		lastAttackTick = tickCount;
		receivedHpExp = false;
		receivedCombatExp = false;
		registeredExpAttack = false;
		return false;
	}

	/**
	 * Register a combat exp drop. If both HP and another combat skill are detected for an exp drop, register it as an
	 * attack.
	 */
	private void processCombatXpDrop(int tickCount, Skill skill)
	{
		if (checkAndUpdateXpAttackTrackerFlags(tickCount)) return;

		switch (skill)
		{
			case HITPOINTS:
				receivedHpExp = true;
				break;
			case ATTACK:
			case STRENGTH:
			case DEFENCE:
			case RANGED:
			case MAGIC:
				receivedCombatExp = true;
				break;
		}
		if (receivedCombatExp && receivedHpExp && !registeredExpAttack)
		{
			registeredExpAttack = true;
			registerAttack(tickCount, AttackSourceTracker.XP_DROP);
		}
	}

	/**
	 * Register an attack on a particular tick derived from a particular source. Additionally, a flag is set to prevent
	 * concurrent registrations.
	 */
	private void registerAttack(int tickCount, AttackSourceTracker attackSource)
	{
		if (tickCount < nextAttackTick || isCurrentlyRegisteringAttack) return;

		isCurrentlyRegisteringAttack = true;
		lastAttackTick = tickCount;
		nextAttackTick = tickCount + attackSpeed - 1;
		trackedAttacks.merge(baseWeaponId, 1, Integer::sum);

		log.info("[TICK {}] Verified attack with baseWeaponId={} via [{}]", tickCount, baseWeaponId, attackSource.name());
		isCurrentlyRegisteringAttack = false;
	}


	/**
	 * Return the registered attack count for the given baseWeaponId. If the given ID is not in the map, try to get the
	 * ItemVariationMapping of the given Id. If this is not found either, return 0.
	 */
	public int getAttackCount(int baseWeaponId)
	{
		return trackedAttacks.getOrDefault(trackedAttacks.containsKey(baseWeaponId) ? baseWeaponId : ItemVariationMapping.map(baseWeaponId), 0);
	}

	/**
	 * Return the registered attack count for the baseId related to the given ItemComposition
	 */
	public int getAttackCount(ItemComposition itemComposition)
	{
		return trackedAttacks.getOrDefault(ItemVariationMapping.map(itemComposition.getId()), 0);
	}

	/**
	 * Return the registered attack count for the baseId related to the given Item
	 */
	public int getAttackCount(Item item)
	{
		return trackedAttacks.getOrDefault(ItemVariationMapping.map(item.getId()), 0);
	}

	/**
	 * Return a mapping of ItemCharges and item charges consumed.
	 */
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

	/**
	 * Reset all counters
	 */
	public void resetAttackCount()
	{
		log.info("Resetting all tracked attack counts");
		trackedAttacks.clear();
		consumedBloodFuryCharges = 0;
	}

	/**
	 * Reset the count of the given ItemVariationMapping of the given itemId(s)
	 */
	public void resetAttackCount(int... itemIds)
	{
		for (int itemId : itemIds)
		{
			int baseId = ItemVariationMapping.map(itemId);
			if (baseId != itemId)
				log.info("Resetting attack count for baseId={} (baseId was remapped from {})", baseId, itemId);
			else
				log.info("Resetting attack count for itemId={}", itemId);
			trackedAttacks.put(baseId, 0);
		}
	}


}