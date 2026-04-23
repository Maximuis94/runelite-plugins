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

package com.dynamiclos;

import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
@Slf4j
@PluginDescriptor(
	name = "Dynamic line of sight"
)
public class DynamicLineOfSightPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private LineOfSightOverlay overlay;

	@Getter
	private int cachedAttackRange = 1;

	private static final int WORN_EQUIPMENT_ID = InventoryID.WORN;
	private static final int ATTACK_STYLE_VARP_ID = VarPlayerID.ATTACK;
	private static final int MYOPIA_VARBIT_ID = VarbitID.COLOSSEUM_MODIFIER_MYOPIA_STACKS_CLIENT;
	private static final int EQUIPPED_WEAPON_TYPE_VARBIT_ID = VarbitID.COMBAT_WEAPON_CATEGORY;

	private final static int MAX_PLAYER_ATTACK_RANGE = 10;
	private final static int WEAPON_SLOT_INDEX = EquipmentInventorySlot.WEAPON.getSlotIdx();
	private static final int LONGRANGE_STYLE_INDEX = 3;
	private static final Set<Integer> LONGRANGE_STYLE_IDS = Set.of(3, 7, 19, 24);

	private int equippedWeaponId = -1;
	private int myopiaTier = 0;
	private int attackStyleIndex = -1;
	private boolean hasLongRangeWeapon = false;
	private int longRangeBonus = 0;

	@Override
	protected void startUp() throws Exception {
		overlayManager.add(overlay);
		initialize();
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event) {
		if (event.getContainerId() == WORN_EQUIPMENT_ID) {
			Item weaponId = event.getItemContainer().getItem(WEAPON_SLOT_INDEX);
			equippedWeaponId = weaponId != null ? ItemVariationMapping.map(weaponId.getId()) : -1;
			updateCachedRange();
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event) {
		int id = event.getVarbitId();
		if (id == ATTACK_STYLE_VARP_ID) {
			attackStyleIndex = client.getVarpValue(ATTACK_STYLE_VARP_ID);
			updateActiveLongRangeBonus();
			updateCachedRange();
		}
		else if (id == MYOPIA_VARBIT_ID)
		{
			myopiaTier = client.getVarpValue(MYOPIA_VARBIT_ID);
			updateCachedRange();
		}
		else if (id == EQUIPPED_WEAPON_TYPE_VARBIT_ID)
		{
			int equippedWeaponType = client.getVarbitValue(EQUIPPED_WEAPON_TYPE_VARBIT_ID);
			hasLongRangeWeapon = LONGRANGE_STYLE_IDS.contains(equippedWeaponType);
			updateActiveLongRangeBonus();
			updateCachedRange();
		}
	}

	/**
	 * Initialization method that attempts to determine all dynamic values that may affect range.
	 */
	private void initialize()
	{
		int equippedWeaponType = client.getVarbitValue(EQUIPPED_WEAPON_TYPE_VARBIT_ID);
		hasLongRangeWeapon = LONGRANGE_STYLE_IDS.contains(equippedWeaponType);
		myopiaTier = client.getVarpValue(MYOPIA_VARBIT_ID);
		attackStyleIndex = client.getVarpValue(ATTACK_STYLE_VARP_ID);
		updateCachedRange();
	}

	/**
	 * Updates the active long range bonus based on weapon and attack style index.
	 */
	private void updateActiveLongRangeBonus()
	{
		longRangeBonus = hasLongRangeWeapon && attackStyleIndex == LONGRANGE_STYLE_INDEX ? 2 : 0;
	}

	/**
	 * Updates the cached range by checking the weapon and current attack style.
	 */
	private void updateCachedRange() {
		if (client.getGameState() != net.runelite.api.GameState.LOGGED_IN) {
			return;
		}

		int baseRange = WeaponRangeConstants.getBaseRange(equippedWeaponId);
		int newRange = Math.min(baseRange + longRangeBonus, MAX_PLAYER_ATTACK_RANGE) - myopiaTier;

		if (newRange != cachedAttackRange)
		{
			cachedAttackRange = newRange;
			overlay.setActiveAttackRange(baseRange);
		}
	}
}