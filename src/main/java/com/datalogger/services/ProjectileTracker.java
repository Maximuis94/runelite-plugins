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

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Animation;
import net.runelite.api.Client;
import net.runelite.api.Projectile;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.eventbus.Subscribe;
import org.apache.commons.lang3.NotImplementedException;

@Slf4j
public class ProjectileTracker
{
	private final Client client;

	private final WeaponTracker weaponTracker;

	private Actor lastTarget;

	private int nextAttackTick;
	private final Map<Integer, Integer> registeredPlayerProjectiles = new HashMap<>();

	@Inject
	public ProjectileTracker(Client client, WeaponTracker weaponTracker)
	{
		this.client = client;
		this.weaponTracker = weaponTracker;
	}

	/**
	 * Return true if the player can launch an attack, given the previous attack and the weapon currently wielded
	 */
	private boolean canLaunchProjectile()
	{
		throw new NotImplementedException("TODO");
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		if (event.getSource() != client.getLocalPlayer()) return;

		log.info("InteractingChanged target is {}", event.getTarget());
		if (event.getTarget() != null)
		{
			lastTarget = event.getTarget();
			log.debug("lastTarget was set to: {}", lastTarget.getName());
		}
	}

	/**
	 * Return true if the
	 * @return
	 */
	private boolean isOnCooldown()
	{
		return client.getTickCount() < nextAttackTick;
	}

	private boolean isOnCooldown(int tickCount)
	{
		return tickCount < nextAttackTick;
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved event)
	{

		int tickCount = client.getTickCount();
		if (nextAttackTick > tickCount) return;

		Projectile projectile = event.getProjectile();

		if (registeredPlayerProjectiles.containsKey(tickCount)) return;

		if (tickCount < nextAttackTick || !isPlayerProjectile(projectile)) return;
		nextAttackTick = tickCount+1;
		registeredPlayerProjectiles.put(tickCount, projectile.getId());

		int pId = projectile.getId();
		Animation a = projectile.getAnimation();
		log.info("[TICK {}] Detected a projectile with id={} animationId={}", tickCount, pId, a != null ? a.getId() : "null");


//		Actor source = projectile.getSourceActor();
//		if (source == null || source != client.getLocalPlayer()) return;
//
//
//		if (projectile.getId() == TUMEKENS_SHADOW_TRAVEL)
//		{
//			nextAttackTick = client.getTickCount() + 3;
//			shadowAttacks++;
//			log.info("Registered shadow projectile at tick={}", client.getTickCount());
//		}
	}


	/**
	 * Return true if this projectile was spawned by the player
	 */
	private boolean isPlayerProjectile(Projectile projectile)
	{

		return projectile.getTargetActor() == lastTarget && projectile.getSourcePoint().distanceTo(client.getLocalPlayer().getWorldLocation()) <= 2;
//		if (projectile.getTargetActor() != lastTarget) return false;
//
//		WorldPoint pSpawn = ;
//		WorldPoint pLoc = ;
//
//		return ;
	}

	/**
	 * Set the next available tickCount for an attack to tickCount + cooldown.
	 * No new attacks can be registered until the tickCount exceeds this value.
	 */
	public void setNextAttackTick(int tickCount, int cooldown)
	{
		nextAttackTick = tickCount + cooldown - 1;
		nextAttackTick = tickCount + weaponTracker.getBaseWeaponId();
	}
}
