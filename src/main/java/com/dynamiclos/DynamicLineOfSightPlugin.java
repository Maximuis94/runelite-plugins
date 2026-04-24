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

import com.google.inject.Provides;
import java.util.Set;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.WorldView;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Dynamic line of sight"
)
public class DynamicLineOfSightPlugin extends Plugin
{
	@Provides
	DynamicLineOfSightConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DynamicLineOfSightConfig.class);
	}

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DynamicLineOfSightConfig config;

	@Inject
	private LineOfSightOverlay overlay;

	private boolean isMyopiaEnabled = false;

	@Getter
	private int cachedAttackRange = 1;

	private static final int COLOSSEUM_REGION_ID = 7175;
	private static final int MYOPIA_VARBIT_ID = VarbitID.COLOSSEUM_MODIFIER_MYOPIA_STACKS_CLIENT;
	private static final int WORN_EQUIPMENT_ID = InventoryID.WORN;
	private static final int ATTACK_STYLE_VARP_ID = VarPlayerID.COM_MODE;
	private static final int EQUIPPED_WEAPON_TYPE_VARBIT_ID = VarbitID.COMBAT_WEAPON_CATEGORY;

	private final static int MIN_PLAYER_ATTACK_RANGE = 1;
	private final static int MAX_PLAYER_ATTACK_RANGE = 10;
	private final static int WEAPON_SLOT_INDEX = EquipmentInventorySlot.WEAPON.getSlotIdx();
	private static final int LONGRANGE_STYLE_INDEX = 3;
	private static final int LONGRANGE_EXTENSION = 2;
	private static final Set<Integer> LONGRANGE_STYLE_IDS = Set.of(3, 5, 7, 19, 24);

	private int equippedWeaponId = -1;
	private int equippedWeaponType = -1;
	private int attackStyleIndex = -1;
	private boolean hasLongRangeWeapon = false;
	private int longRangeBonus = 0;

	private boolean isInColosseum = false;
	private int myopiaReduction = 0;

	@Override
	protected void startUp() throws Exception {
		overlayManager.add(overlay);
		clientThread.invokeLater(this::initialize);
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event) {
		if (event.getContainerId() == WORN_EQUIPMENT_ID) {
			Item weapon = event.getItemContainer().getItem(WEAPON_SLOT_INDEX);
			if (weapon == null) return;

			int weaponId = weapon.getId();
			if (weaponId != equippedWeaponId)
			{
				log.debug("Equipped weapon changed from {} to {}", equippedWeaponId, weaponId);
				equippedWeaponId = weaponId;
				updateCachedRange();
			}
		}
	}

	private void updateInColosseum()
	{
		WorldView topLevelWorldView = client.getTopLevelWorldView();

		if (topLevelWorldView != null)
		{
			int[] mapRegions = topLevelWorldView.getMapRegions();

			if (mapRegions != null)
			{
				for (int region : mapRegions)
				{
					if (region == COLOSSEUM_REGION_ID)
					{
						// If I transition from outside to inside, myopia has to be 0
						myopiaReduction = isInColosseum ? client.getVarbitValue(MYOPIA_VARBIT_ID) * 2 : 0;

						isInColosseum = true;
						break;
					}
				}
				resetInColosseum();
			}
		}
	}

	private void resetInColosseum()
	{
		isInColosseum = false;
		myopiaReduction = 0;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (!isMyopiaEnabled) return;

		GameState state = event.getGameState();

		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING || state == GameState.CONNECTION_LOST)
		{
			resetInColosseum();
			return;
		}

		if (state == GameState.LOGGED_IN)
		{
			updateInColosseum();
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event) {
		int pId = event.getVarpId();
		if (pId == ATTACK_STYLE_VARP_ID) {
			int newIndex = client.getVarpValue(ATTACK_STYLE_VARP_ID);
			log.debug("AttackStyleIndex was changed from {} to {}", attackStyleIndex, newIndex);
			attackStyleIndex = client.getVarpValue(ATTACK_STYLE_VARP_ID);
			updateActiveLongRangeBonus();
			updateCachedRange();
			return;
		}

		int vId = event.getVarbitId();
		if (vId == EQUIPPED_WEAPON_TYPE_VARBIT_ID)
		{
			int newEquippedWeaponType = client.getVarbitValue(EQUIPPED_WEAPON_TYPE_VARBIT_ID);
			hasLongRangeWeapon = LONGRANGE_STYLE_IDS.contains(newEquippedWeaponType);
			log.debug("Equipped Weapon Type was changed from {} to {}", equippedWeaponType, newEquippedWeaponType);
			equippedWeaponType = newEquippedWeaponType;
			updateActiveLongRangeBonus();
			updateCachedRange();
		}

		else if (isInColosseum && vId == MYOPIA_VARBIT_ID)
		{
			myopiaReduction = client.getVarbitValue(MYOPIA_VARBIT_ID) * 2;
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
		attackStyleIndex = client.getVarpValue(ATTACK_STYLE_VARP_ID);

		if (isMyopiaEnabled)
		{
			updateInColosseum();

			// If true, plugin was activated during a trial
			if (isInColosseum)
			{
				myopiaReduction = client.getVarbitValue(MYOPIA_VARBIT_ID) * 2;
			}
		}

		updateCachedRange();
	}

	/**
	 * Updates the active long range bonus based on weapon and attack style index.
	 */
	private void updateActiveLongRangeBonus()
	{
		longRangeBonus = hasLongRangeWeapon && attackStyleIndex == LONGRANGE_STYLE_INDEX ? LONGRANGE_EXTENSION : 0;
		log.debug("Longrange bonus is {}", longRangeBonus == LONGRANGE_EXTENSION ? "active" : "inactive");
	}

	/**
	 * Updates the cached range by checking the weapon and current attack style.
	 */
	private void updateCachedRange() {
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}

		int baseRange = WeaponRangeConstants.getBaseRange(equippedWeaponId);
		int computedRange = Math.min(baseRange + longRangeBonus, MAX_PLAYER_ATTACK_RANGE) - myopiaReduction;
		int newRange = Math.max(computedRange, MIN_PLAYER_ATTACK_RANGE);

		if (newRange != cachedAttackRange)
		{
			log.debug("Updated player attack range parameters from {} to {}", cachedAttackRange, newRange);
			cachedAttackRange = newRange;
			overlay.setActiveAttackRange(cachedAttackRange);
		}
	}
}