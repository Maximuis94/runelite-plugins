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

package com.activitycounter.listeners.core;

import com.activitycounter.listeners.AbstractListenerNode;
import com.activitycounter.models.TrackedActivity;
import javax.inject.Singleton;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;

@Singleton
public class StatChangeListener extends AbstractListenerNode
{
	private boolean statsLoaded = false;
	private final int[] effectiveLevels = new int[Skill.values().length];

	/**
	 * Returns the current effective (boosted) level of a skill.
	 */
	public int getEffectiveLevel(Skill skill)
	{
		if (skill == null) return 0;
		return effectiveLevels[skill.ordinal()];
	}

	/**
	 * Maps an OSRS Skill to its corresponding Experience TrackedActivity Storage ID.
	 */
	public int getSkillTrackingId(Skill skill) {
		switch (skill) {
			case ATTACK: return TrackedActivity.XP_ATTACK.getId();
			case DEFENCE: return TrackedActivity.XP_DEFENCE.getId();
			case STRENGTH: return TrackedActivity.XP_STRENGTH.getId();
			case HITPOINTS: return TrackedActivity.XP_HITPOINTS.getId();
			case RANGED: return TrackedActivity.XP_RANGED.getId();
			case PRAYER: return TrackedActivity.XP_PRAYER.getId();
			case MAGIC: return TrackedActivity.XP_MAGIC.getId();
			case COOKING: return TrackedActivity.XP_COOKING.getId();
			case WOODCUTTING: return TrackedActivity.XP_WOODCUTTING.getId();
			case FLETCHING: return TrackedActivity.XP_FLETCHING.getId();
			case FISHING: return TrackedActivity.XP_FISHING.getId();
			case FIREMAKING: return TrackedActivity.XP_FIREMAKING.getId();
			case CRAFTING: return TrackedActivity.XP_CRAFTING.getId();
			case SMITHING: return TrackedActivity.XP_SMITHING.getId();
			case MINING: return TrackedActivity.XP_MINING.getId();
			case HERBLORE: return TrackedActivity.XP_HERBLORE.getId();
			case AGILITY: return TrackedActivity.XP_AGILITY.getId();
			case THIEVING: return TrackedActivity.XP_THIEVING.getId();
			case SLAYER: return TrackedActivity.XP_SLAYER.getId();
			case FARMING: return TrackedActivity.XP_FARMING.getId();
			case RUNECRAFT: return TrackedActivity.XP_RUNECRAFT.getId();
			case HUNTER: return TrackedActivity.XP_HUNTER.getId();
			case CONSTRUCTION: return TrackedActivity.XP_CONSTRUCTION.getId();
			case SAILING: return TrackedActivity.XP_SAILING.getId();
			default: return -1;
		}
	}

	/**
	 * Maps an OSRS Skill to its corresponding Level TrackedActivity Storage ID.
	 */
	public int getSkillLevelTrackingId(Skill skill) {
		switch (skill) {
			case ATTACK: return TrackedActivity.LVL_ATTACK.getId();
			case DEFENCE: return TrackedActivity.LVL_DEFENCE.getId();
			case STRENGTH: return TrackedActivity.LVL_STRENGTH.getId();
			case HITPOINTS: return TrackedActivity.LVL_HITPOINTS.getId();
			case RANGED: return TrackedActivity.LVL_RANGED.getId();
			case PRAYER: return TrackedActivity.LVL_PRAYER.getId();
			case MAGIC: return TrackedActivity.LVL_MAGIC.getId();
			case COOKING: return TrackedActivity.LVL_COOKING.getId();
			case WOODCUTTING: return TrackedActivity.LVL_WOODCUTTING.getId();
			case FLETCHING: return TrackedActivity.LVL_FLETCHING.getId();
			case FISHING: return TrackedActivity.LVL_FISHING.getId();
			case FIREMAKING: return TrackedActivity.LVL_FIREMAKING.getId();
			case CRAFTING: return TrackedActivity.LVL_CRAFTING.getId();
			case SMITHING: return TrackedActivity.LVL_SMITHING.getId();
			case MINING: return TrackedActivity.LVL_MINING.getId();
			case HERBLORE: return TrackedActivity.LVL_HERBLORE.getId();
			case AGILITY: return TrackedActivity.LVL_AGILITY.getId();
			case THIEVING: return TrackedActivity.LVL_THIEVING.getId();
			case SLAYER: return TrackedActivity.LVL_SLAYER.getId();
			case FARMING: return TrackedActivity.LVL_FARMING.getId();
			case RUNECRAFT: return TrackedActivity.LVL_RUNECRAFTING.getId();
			case HUNTER: return TrackedActivity.LVL_HUNTER.getId();
			case CONSTRUCTION: return TrackedActivity.LVL_CONSTRUCTION.getId();
			case SAILING: return TrackedActivity.LVL_SAILING.getId();
			default: return -1;
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event) {
		if (!statsLoaded) {
			for (Skill s : Skill.values()) {
				if (client.getRealSkillLevel(s) == 0) return;
			}
			statsLoaded = true;
		}

		if (plugin.getLoginTicks() < 5 || !plugin.hasActiveSession()) return;

		Skill skill = event.getSkill();
		int xp = event.getXp();
		effectiveLevels[skill.ordinal()] = event.getBoostedLevel();

		boolean isXpUpdated = plugin.getCurrentSession().updateSkillXp(skill, xp);

		if (isXpUpdated) {
			int trackingId = getSkillTrackingId(skill);
			if (plugin.isTracking(trackingId)) {
				plugin.processActivityUpdate(trackingId, xp);
			}

			if (plugin.isTracking(TrackedActivity.XP_TOTAL.getId())) {
				int totalGained = 0;
				for (Integer gained : plugin.getCurrentSession().getGainedXpMap().values()) {
					if (gained != null) {
						totalGained += gained;
					}
				}

				plugin.setCountAbsolute(TrackedActivity.XP_TOTAL.getId(), totalGained);
			}

			int lvlTrackingId = getSkillLevelTrackingId(skill);
			if (plugin.isTracking(lvlTrackingId)) {
				plugin.processActivityUpdate(lvlTrackingId, client.getRealSkillLevel(skill));
			}

			if (plugin.isTracking(TrackedActivity.LVL_TOTAL.getId())) {
				plugin.processActivityUpdate(TrackedActivity.LVL_TOTAL.getId(), client.getTotalLevel());
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		int state = event.getGameState().getState();
		if (state == GameState.LOGGED_IN.getState() || state == GameState.HOPPING.getState()) {
			statsLoaded = false;
		}
	}
}