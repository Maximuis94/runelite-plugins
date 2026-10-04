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

import com.activitycounter.events.ExperienceDrop;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

/**
 * Class that handles statchanges that relate to exp drops / differences specifically.
 */
@Slf4j
@Singleton
public class ExperienceDropListener
{
	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	@Getter
	private Skill lastSkillExpGained = null;
	private final int[] lastXpTicks = new int[Skill.values().length];
	private final int[] previousXp = new int[Skill.values().length];

	private static final int MAX_SOUND_ID = 10000;

	// Array index = Sound ID. Value = Array of competing Skills.
	private final Skill[][] ambiguousSoundMap = new Skill[MAX_SOUND_ID][];

	public ExperienceDropListener() {
		Arrays.fill(lastXpTicks, -100);
		Arrays.fill(previousXp, -1);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING) {
			Arrays.fill(previousXp, -1);
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		int skillIdx = event.getSkill().ordinal();
		int newTotalXp = event.getXp();
		int previousTotalXp = previousXp[skillIdx];

		if (previousTotalXp != -1 && newTotalXp > previousTotalXp) {
			int xpGained = newTotalXp - previousTotalXp;
			lastXpTicks[skillIdx] = client.getTickCount();

			eventBus.post(new ExperienceDrop(event.getSkill(), xpGained));
		}

		previousXp[skillIdx] = newTotalXp;
		lastSkillExpGained = event.getSkill();
	}


	/**
	 * Given a set of Skills, return the Skill for which a tick was registered most recently
	 */
	public Skill getMostRecentDrop(Skill... skills)
	{
		Skill mostRecentSkill = null;
		int highestTick = -1;
		int currentTick = client.getTickCount();

		for (Skill skill : skills) {
			int dropTick = lastXpTicks[skill.ordinal()];

			if (currentTick - dropTick <= 2 && dropTick > highestTick) {
				highestTick = dropTick;
				mostRecentSkill = skill;
			}
		}

		return mostRecentSkill;
	}
}