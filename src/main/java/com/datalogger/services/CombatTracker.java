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

import com.datalogger.models.combat.TargetNpc;
import com.datalogger.models.enums.AttackSourceTracker;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import net.runelite.api.Skill;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.FakeXpDrop;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
public class CombatTracker
{
	@Inject private Client client;
	@Inject private EventBus eventBus;
	@Inject private EquipmentTracker equipmentTracker;

	@Getter
	private final Map<Integer, TargetNpc> activeCombatStats = new HashMap<>();

	// --- Attack Heuristic Tracking Variables ---
	private int lastAttackTick = -1;
	private int nextAttackTick = -1;
	private boolean receivedHpExp = false;
	private boolean receivedCombatExp = false;
	private boolean registeredExpAttack = false;
	private boolean isCurrentlyRegisteringAttack = false;
	private final Map<Skill, Integer> previousXp = new EnumMap<>(Skill.class);

	/**
	 * Main Hitsplat Engine: Logs details to TargetNpc, pushes Blood Fury data to
	 * EquipmentTracker, and broadcasts OutgoingHitApplied.
	 */
	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		Hitsplat hitsplat = event.getHitsplat();

		// Fast exit: only track our own hits
		if (!hitsplat.isMine()) return;

		int hitsplatType = hitsplat.getHitsplatType();
		boolean isMiss = (hitsplatType == HitsplatID.BLOCK_ME);

		if (equipmentTracker.canConsumeBloodFuryCharges() && !isMiss)
			equipmentTracker.incrementBloodFuryCharge();

//		Actor target = event.getActor();
//		if (target == client.getLocalPlayer() || !(target instanceof NPC)) return;
//
//		NPC npc = (NPC) target;
//		int npcIndex = npc.getIndex();
//		int damage = isMiss ? 0 : hitsplat.getAmount();
//
//		TargetNpc targetNpc = activeCombatStats.computeIfAbsent(
//			npcIndex,
//			k -> new TargetNpc(npcIndex, npc.getId(), npc.getName())
//		);
//
//		PlayerAttack attack = new PlayerAttack(
//			npcIndex,
//			client.getTickCount(),
//			damage,
//			isMiss,
//			hitsplatType,
//			equipmentTracker.getBaseWeaponId(),
//			equipmentTracker.getCurrentCombatType()
//		);
//
//		targetNpc.addPlayerAttack(attack);
//
//		// Broadcast for downstream loggers (e.g. Colosseum loggers)
//		eventBus.post(new OutgoingHitApplied(
//			target, hitsplat, damage,
//			equipmentTracker.getBaseWeaponId(),
//			equipmentTracker.getCurrentCombatType(),
//			client.getTickCount()
//		));
	}

	// =========================================================================
	// Attack Identification Heuristics (XP, Animation, Sounds)
	// =========================================================================

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		if (!equipmentTracker.isTrackingWeaponCharges()) return;
		int tickCount = client.getTickCount();
		if (checkAndUpdateXpAttackTrackerFlags(tickCount)) return;

		if (equipmentTracker.getAttackStyleSoundId() == event.getSoundId())
		{
			registerAttack(tickCount, AttackSourceTracker.SOUND);
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (equipmentTracker.getCurrentWeapon() == null || event.getActor() != client.getLocalPlayer()) return;
		int tickCount = client.getTickCount();
		if (checkAndUpdateXpAttackTrackerFlags(tickCount)) return;

		int animId = event.getActor().getAnimation();
		if (equipmentTracker.getCurrentWeapon().isAttackAnimation(animId))
		{
			registerAttack(tickCount, AttackSourceTracker.ANIMATION);
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (!equipmentTracker.isTrackingWeaponCharges()) return;
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
		if (!equipmentTracker.isTrackingWeaponCharges()) return;
		int tickCount = client.getTickCount();
		if (checkAndUpdateXpAttackTrackerFlags(tickCount)) return;
		processCombatXpDrop(tickCount, event.getSkill());
	}

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

	private void processCombatXpDrop(int tickCount, Skill skill)
	{
		if (checkAndUpdateXpAttackTrackerFlags(tickCount)) return;
		switch (skill)
		{
			case HITPOINTS: receivedHpExp = true; break;
			case ATTACK: case STRENGTH: case DEFENCE: case RANGED: case MAGIC: receivedCombatExp = true; break;
		}
		if (receivedCombatExp && receivedHpExp && !registeredExpAttack)
		{
			registeredExpAttack = true;
			registerAttack(tickCount, AttackSourceTracker.XP_DROP);
		}
	}

	private void registerAttack(int tickCount, AttackSourceTracker attackSource)
	{
		if (tickCount < nextAttackTick || isCurrentlyRegisteringAttack) return;

		isCurrentlyRegisteringAttack = true;
		lastAttackTick = tickCount;
		nextAttackTick = tickCount + equipmentTracker.getAttackSpeed() - 1;

		int baseWeaponId = equipmentTracker.getBaseWeaponId();

		equipmentTracker.addWeaponAttack(baseWeaponId);

		log.info("[TICK {}] Verified attack with baseWeaponId={} via [{}]", tickCount, baseWeaponId, attackSource.name());
		isCurrentlyRegisteringAttack = false;
	}

	// =========================================================================
	// Target NPC Lifecycle Management
	// =========================================================================

//	@Subscribe
//	public void onNpcDespawned(NpcDespawned event)
//	{
//		TargetNpc finalStats = activeCombatStats.remove(event.getNpc().getIndex());
//		if (finalStats != null)
//		{
//			log.debug("Target despawned: {} (ID: {}) took {} hits, {} misses, {} total damage.",
//				finalStats.getNpcName(), finalStats.getNpcId(),
//				finalStats.getSuccessfulHits(), finalStats.getMissedHits(), finalStats.getTotalDamage());
//		}
//	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
		{
			clearState();
		}
	}

	public void clearState()
	{
		activeCombatStats.clear();
		log.debug("Combat tracker state cleared.");
	}
}