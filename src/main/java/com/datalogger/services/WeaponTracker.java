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
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.game.ItemVariationMapping;

@Singleton
@Slf4j
public class WeaponTracker
{
	private final static int WEAPON_SLOT_INDEX = 3;

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Getter
	private int currentWeaponId = -1;

	@Getter
	private int baseWeaponId = -1;

	private ItemEquipmentStats weaponStats;

	/**
	 * Updates the cached weapon ID by looking directly at the equipment container.
	 */
	public void updateCurrentWeapon()
	{
		ItemContainer equipment = client.getItemContainer(EQUIPMENT_CONTAINER_ID);

		if (equipment == null)
		{
			currentWeaponId = -1;
			return;
		}

		Item weapon = equipment.getItem(WEAPON_SLOT_INDEX);

		if (weapon == null)
		{
			currentWeaponId = -1;
		}
		else
		{
			int itemId = weapon.getId();
			if (itemId != currentWeaponId)
			{
				log.debug("Updated carried weapon from {} to {}", currentWeaponId, itemId);
				currentWeaponId = itemId;
				baseWeaponId = ItemVariationMapping.map(itemId);
				weaponStats = null;
				ItemStats stats = itemManager.getItemStats(currentWeaponId);
				if (stats == null) return;

				weaponStats = stats.getEquipment();
			}
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == EQUIPMENT_CONTAINER_ID)
		{
			updateCurrentWeapon();
		}
	}
}