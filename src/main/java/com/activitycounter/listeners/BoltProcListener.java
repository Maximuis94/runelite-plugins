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

package com.activitycounter.listeners;

import com.activitycounter.ActivityCounterPlugin;
import com.activitycounter.PluginConstants;
import static com.activitycounter.PluginConstants.EQUIPPED_ITEM_CONTAINER_ID;
import com.activitycounter.models.Count;
import com.activitycounter.models.CrossbowBoltEffect;
import com.activitycounter.models.Session;
import com.activitycounter.models.TrackedActivity;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ActorSpotAnim;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

/**
 * Listener for enchanted bolt effects. If the user wields a crossbow and enchanted bolts are shot, watch for specific
 * animationId+soundId. If both are detected at the same time, assume the effect was applied.
 * Class may be unregistered completely via configurations.
 */
@Slf4j
@Singleton
public class BoltProcListener
{
	@Inject
	private Client client;

	@Inject
	private ActivityCounterPlugin plugin;

	// Equipment State
	private boolean hasCrossbowEquipped = false;
	private int equippedAmmoItemId = -1;
	private CrossbowBoltEffect equippedAmmoEffect = null;
	private int quiverAmmoItemId = -1;
	private CrossbowBoltEffect equippedQuiverEffect = null;
	private boolean requiresUpdate = true;
	private boolean disableSoundAndAnimListeners = false;

	private CrossbowBoltEffect applicableCrossbowBoltEffect = null;
	private int applicableSoundId = -1;
	private int applicableSpotAnimId = -1;

	private boolean sawSpotAnimThisTick = false;
	private boolean heardSoundThisTick = false;

	private static final int CROSSBOW_WEAPON_CATEGORY = 5;
	private static final int COMBAT_WEAPON_CATEGORY_VARBIT_ID = VarbitID.COMBAT_WEAPON_CATEGORY;
	private static final int QUIVER_AMMO_TYPE_VARP_ID = VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO;
	private static final int AMMO_EQUIPMENT_SLOT_IDX = EquipmentInventorySlot.AMMO.getSlotIdx();

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarpId() == QUIVER_AMMO_TYPE_VARP_ID)
		{
			quiverAmmoItemId = client.getVarpValue(QUIVER_AMMO_TYPE_VARP_ID);
			equippedQuiverEffect = CrossbowBoltEffect.getEffectByItemId(quiverAmmoItemId);
			log.debug("Updated quiver ammo type; quiverAmmoItemId={} equippedQuiverEffect={}", quiverAmmoItemId, equippedQuiverEffect);
			requiresUpdate = true;
			return;
		}

		if (event.getVarbitId() == COMBAT_WEAPON_CATEGORY_VARBIT_ID)
		{
			hasCrossbowEquipped = client.getVarbitValue(COMBAT_WEAPON_CATEGORY_VARBIT_ID) == CROSSBOW_WEAPON_CATEGORY;
			requiresUpdate = true;
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == PluginConstants.EQUIPPED_ITEM_CONTAINER_ID)
		{
			Item ammoItem = event.getItemContainer().getItem(AMMO_EQUIPMENT_SLOT_IDX);
			int ammoItemId = ammoItem != null ? ammoItem.getId() : -1;

			if (equippedAmmoItemId == ammoItemId) return;
			equippedAmmoItemId = ammoItemId;
			equippedAmmoEffect = CrossbowBoltEffect.getEffectByItemId(equippedAmmoItemId);
			log.debug("Updated ammo ammo type; equippedAmmoItemId={} equippedAmmoEffect={}", equippedAmmoItemId, equippedAmmoEffect);
			requiresUpdate = true;
		}
	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged event)
	{
		if (disableSoundAndAnimListeners) return;

		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null || event.getActor() == null) return;

		// We only care if the graphic is playing on the target we are currently attacking
		if (event.getActor() == localPlayer.getInteracting())
		{
			for (ActorSpotAnim spotAnim : event.getActor().getSpotAnims())
			{
				if ((spotAnim.getId() + PluginConstants.SPOT_ANIM_OFFSET) == applicableSpotAnimId)
				{
					sawSpotAnimThisTick = true;
					break;
				}
			}
		}
	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		if (disableSoundAndAnimListeners) return;

		if ((event.getSoundId() + PluginConstants.SOUND_EFFECT_OFFSET) == applicableSoundId)
		{
			heardSoundThisTick = true;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (sawSpotAnimThisTick && heardSoundThisTick) submitProc();

		sawSpotAnimThisTick = false;
		heardSoundThisTick = false;

		if (requiresUpdate) updateParameters();
	}

	/**
	 * Submit a count for the tracker associated with the currently active CrossbowBoltEffect
	 */
	private void submitProc()
	{
		if (applicableCrossbowBoltEffect == null) return;

		TrackedActivity procActivity = applicableCrossbowBoltEffect.getTrackedActivity();

		Session session = plugin.getCurrentSession();
		if (session != null && session.isInProgress() && session.isTracking(procActivity.getId()))
		{
			Count kc = session.getKillCount(procActivity.getId());
			if (kc != null)
			{
				if (kc.getInitialKc() == -1)
				{
					kc.setInitialKc(0);
				}
				kc.setSessionKc(kc.getSessionKc() + 1);
				plugin.setRequiresSaveAndRefresh(true);
			}
		}
	}

	/**
	 * Update parameters related to equipment worn
	 */
	private void updateParameters()
	{
		if (!hasCrossbowEquipped)
			applicableCrossbowBoltEffect = null;
		else
			applicableCrossbowBoltEffect = equippedAmmoEffect != null ? equippedAmmoEffect : equippedQuiverEffect;

		if (applicableCrossbowBoltEffect != null) {
			applicableSoundId = applicableCrossbowBoltEffect.getSoundEffectId();
			applicableSpotAnimId = applicableCrossbowBoltEffect.getSpotAnimId();
		} else {
			applicableSoundId = -1;
			applicableSpotAnimId = -1;
		}
		requiresUpdate = false;
		disableSoundAndAnimListeners = applicableCrossbowBoltEffect == null;
	}

	/**
	 * Manually loads the current equipment state. Called when the listener is registered mid-session.
	 */
	public void initializeState()
	{
		if (client.getGameState() != GameState.LOGGED_IN) return;

		quiverAmmoItemId = client.getVarpValue(QUIVER_AMMO_TYPE_VARP_ID);
		equippedQuiverEffect = CrossbowBoltEffect.getEffectByItemId(quiverAmmoItemId);

		hasCrossbowEquipped = client.getVarbitValue(COMBAT_WEAPON_CATEGORY_VARBIT_ID) == CROSSBOW_WEAPON_CATEGORY;

		ItemContainer equipment = client.getItemContainer(EQUIPPED_ITEM_CONTAINER_ID);
		if (equipment != null)
		{
			Item ammoItem = equipment.getItem(AMMO_EQUIPMENT_SLOT_IDX);
			equippedAmmoItemId = ammoItem != null ? ammoItem.getId() : -1;
			equippedAmmoEffect = CrossbowBoltEffect.getEffectByItemId(equippedAmmoItemId);
		}

		requiresUpdate = true;
	}
}