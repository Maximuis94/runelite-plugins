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
package com.venatorpathfinder;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.api.events.VarbitChanged;
import static net.runelite.api.gameval.ItemID.VENATOR_BOW;
import static net.runelite.api.gameval.VarbitID.MULTIWAY_INDICATOR;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Venator Path Finder",
	description = "Highlights the path(s) of bouncing Venator bow projectiles shot at the NPC you are hovering over.",
	tags = {"combat", "venator", "bow", "bounce", "projectile", "multi", "multicombat", "path", "arrow", "ranged", "ranging", "slayer"}

)
public class VenatorPathFinderPlugin extends Plugin
{
	public static final String CONFIG_GROUP = "venatorpathfinder";
	public static final int VENATOR_ATTACK_SOUND = 6797;
	public static final int VENATOR_BOUNCE_SOUND_1 = 6672;
	public static final int VENATOR_BOUNCE_SOUND_2 = 6735;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private VenatorPathFinderOverlay overlay;

	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	@Inject
	private VenatorPathFinderConfig config;

	private final static int WEAPON_SLOT_INDEX = EquipmentInventorySlot.WEAPON.getSlotIdx();
	private final static int INVENTORY_ID_EQUIPPED = net.runelite.api.gameval.InventoryID.WORN;
	private final static int VENATOR_BOW_ITEM_ID = ItemVariationMapping.map(VENATOR_BOW);
	public final static int IN_RANGE_RADIUS = 10;

	private boolean debugLoggerRegistered = false;

	@Inject
	private VenatorPathTracker venatorPathTracker;

	private void toggleDebugLogger(boolean isEnabled)
	{
		if (isEnabled && !debugLoggerRegistered)
		{
			eventBus.register(venatorPathTracker);
			debugLoggerRegistered = true;
		}
		else if (!isEnabled && debugLoggerRegistered)
		{
			eventBus.unregister(venatorPathTracker);
			debugLoggerRegistered = false;
		}
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		toggleDebugLogger(debugLoggingEnabled());
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		toggleDebugLogger(false);
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarbitId() == MULTIWAY_INDICATOR)
		{
			overlay.setInMultiCombat(client.getVarbitValue(MULTIWAY_INDICATOR) == 1);
		}
	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		if (event.getSoundId() == VENATOR_ATTACK_SOUND)
		{
			Player player = client.getLocalPlayer();
			if (player != null && player.getInteracting() instanceof NPC)
			{
				overlay.notifyAttack((NPC) player.getInteracting(), client.getTickCount());
			}
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != INVENTORY_ID_EQUIPPED) return;

		ItemContainer equippedItems = event.getItemContainer();
		Item weapon = equippedItems.getItem(WEAPON_SLOT_INDEX);
		overlay.setHasEquippedVenatorBow(weapon != null && ItemVariationMapping.map(weapon.getId()) == VENATOR_BOW_ITEM_ID);
	}

	public boolean debugLoggingEnabled()
	{
		return config.debugLoggingJsonEnabled() || config.debugLoggingScreenshotEnabled() || config.debugLoggingCsvEnabled();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(CONFIG_GROUP)) return;

		overlay.parseConfigs();
		toggleDebugLogger(debugLoggingEnabled());
	}

	@Provides
	VenatorPathFinderConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(VenatorPathFinderConfig.class);
	}
}