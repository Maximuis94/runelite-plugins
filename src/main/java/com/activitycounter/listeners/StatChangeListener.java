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
import static com.activitycounter.PluginConstants.ActivityID.*;
import com.activitycounter.models.Count;
import com.activitycounter.models.Session;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;

@Singleton
public class StatChangeListener {

	@Inject
	private Client client;

	@Inject
	private ActivityCounterPlugin plugin;

	public int getSkillTrackingId(Skill skill) {
		switch (skill) {
			case ATTACK: return XP_ATTACK;
			case DEFENCE: return XP_DEFENCE;
			case STRENGTH: return XP_STRENGTH;
			case HITPOINTS: return XP_HITPOINTS;
			case RANGED: return XP_RANGED;
			case PRAYER: return XP_PRAYER;
			case MAGIC: return XP_MAGIC;
			case COOKING: return XP_COOKING;
			case WOODCUTTING: return XP_WOODCUTTING;
			case FLETCHING: return XP_FLETCHING;
			case FISHING: return XP_FISHING;
			case FIREMAKING: return XP_FIREMAKING;
			case CRAFTING: return XP_CRAFTING;
			case SMITHING: return XP_SMITHING;
			case MINING: return XP_MINING;
			case HERBLORE: return XP_HERBLORE;
			case AGILITY: return XP_AGILITY;
			case THIEVING: return XP_THIEVING;
			case SLAYER: return XP_SLAYER;
			case FARMING: return XP_FARMING;
			case RUNECRAFT: return XP_RUNECRAFT;
			case HUNTER: return XP_HUNTER;
			case CONSTRUCTION: return XP_CONSTRUCTION;
			case SAILING: return XP_SAILING;
			default: return -1;
		}
	}

	public int getSkillLevelTrackingId(Skill skill) {
		switch (skill) {
			case ATTACK: return LVL_ATTACK;
			case DEFENCE: return LVL_DEFENCE;
			case STRENGTH: return LVL_STRENGTH;
			case HITPOINTS: return LVL_HITPOINTS;
			case RANGED: return LVL_RANGED;
			case PRAYER: return LVL_PRAYER;
			case MAGIC: return LVL_MAGIC;
			case COOKING: return LVL_COOKING;
			case WOODCUTTING: return LVL_WOODCUTTING;
			case FLETCHING: return LVL_FLETCHING;
			case FISHING: return LVL_FISHING;
			case FIREMAKING: return LVL_FIREMAKING;
			case CRAFTING: return LVL_CRAFTING;
			case SMITHING: return LVL_SMITHING;
			case MINING: return LVL_MINING;
			case HERBLORE: return LVL_HERBLORE;
			case AGILITY: return LVL_AGILITY;
			case THIEVING: return LVL_THIEVING;
			case SLAYER: return LVL_SLAYER;
			case FARMING: return LVL_FARMING;
			case RUNECRAFT: return LVL_RUNECRAFTING;
			case HUNTER: return LVL_HUNTER;
			case CONSTRUCTION: return LVL_CONSTRUCTION;
			case SAILING: return LVL_SAILING;
			default: return -1;
		}
	}

	private void processActivityUpdate(int trackingId, int currentValue) {
		Session session = plugin.getCurrentSession();
		if (session == null) return;

		Count kc = session.getKillCount(trackingId);
		if (kc == null) return;

		if (kc.getInitialKc() == -1) {
			kc.setInitialKc(currentValue);
		}

		int newSessionKc = currentValue - kc.getInitialKc();
		if (newSessionKc > kc.getSessionKc()) {
			kc.setSessionKc(newSessionKc);
			plugin.setRequiresSaveAndRefresh(true);
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event) {
		Session currentSession = plugin.getCurrentSession();
		if (currentSession == null || !currentSession.isInProgress()) return;

		boolean isXpUpdated = false;

		if (currentSession.updateSkillXp(event.getSkill(), event.getXp())) {
			plugin.setRequiresSaveAndRefresh(true);
			isXpUpdated = true;
		}

		int trackingId = getSkillTrackingId(event.getSkill());
		if (currentSession.isTracking(trackingId)) {
			processActivityUpdate(trackingId, event.getXp());
			isXpUpdated = true;
		}

		if (isXpUpdated && currentSession.isTracking(XP_TOTAL)) {
			int totalGained = currentSession.getGainedXpMap().values().stream()
				.mapToInt(Integer::intValue)
				.sum();
			currentSession.getKillCount(XP_TOTAL).setSessionKc(totalGained);
		}

		int lvlTrackingId = getSkillLevelTrackingId(event.getSkill());
		if (currentSession.isTracking(lvlTrackingId)) {
			processActivityUpdate(lvlTrackingId, client.getRealSkillLevel(event.getSkill()));
		}

		if (currentSession.isTracking(LVL_TOTAL)) {
			processActivityUpdate(LVL_TOTAL, client.getTotalLevel());
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		Session currentSession = plugin.getCurrentSession();
		if (currentSession == null || !currentSession.isInProgress()) return;
		if (plugin.getLoginTicks() < 5) return;

		boolean baselinesUpdated = false;

		for (Skill skill : Skill.values()) {
			int xpTrackingId = getSkillTrackingId(skill);
			if (currentSession.isTracking(xpTrackingId)) {
				Count kc = currentSession.getKillCount(xpTrackingId);
				if (kc != null && kc.getInitialKc() == -1) {
					kc.setInitialKc(client.getSkillExperience(skill));
					currentSession.initializeSkill(skill, client.getSkillExperience(skill));
					baselinesUpdated = true;
				}
			}

			int lvlTrackingId = getSkillLevelTrackingId(skill);
			if (currentSession.isTracking(lvlTrackingId)) {
				Count kc = currentSession.getKillCount(lvlTrackingId);
				if (kc != null && kc.getInitialKc() == -1) {
					kc.setInitialKc(client.getRealSkillLevel(skill));
					baselinesUpdated = true;
				}
			}
		}

		if (currentSession.isTracking(LVL_TOTAL)) {
			Count kc = currentSession.getKillCount(LVL_TOTAL);
			if (kc != null && kc.getInitialKc() == -1) {
				kc.setInitialKc(client.getTotalLevel());
				baselinesUpdated = true;
			}
		}

		if (baselinesUpdated) {
			plugin.setRequiresSaveAndRefresh(true);
		}
	}
}