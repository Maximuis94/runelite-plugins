package com.activitycounter.listeners.core;

import com.activitycounter.events.ExperienceDrop;
import com.activitycounter.listeners.AbstractListenerNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

/**
 * Class that handles stat changes that relate to exp drops / differences specifically.
 */
@Slf4j
@Singleton
public class ExperienceDropListener extends AbstractListenerNode
{
	@Inject
	private EventBus eventBus;

	@Getter
	private int lastExpDropTick = -1;

	private final EnumSet<Skill> skillsGainedLastTick = EnumSet.noneOf(Skill.class);

	private final int[] lastXpTicks = new int[Skill.values().length];
	private final int[] previousXp = new int[Skill.values().length];

	private static final int MAX_SOUND_ID = 10000;

	private final Skill[][] ambiguousSoundMap = new Skill[MAX_SOUND_ID][];

	public ExperienceDropListener()
	{
		Arrays.fill(lastXpTicks, -100);
		Arrays.fill(previousXp, -1);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING)
		{
			Arrays.fill(previousXp, -1);
			skillsGainedLastTick.clear();
			lastExpDropTick = -1;
		}
	}

	@Subscribe(priority=100.0f)
	public void onStatChanged(StatChanged event)
	{
		int skillIdx = event.getSkill().ordinal();
		int newTotalXp = event.getXp();
		int previousTotalXp = previousXp[skillIdx];

		if (previousTotalXp != -1 && newTotalXp > previousTotalXp)
		{
			int xpGained = newTotalXp - previousTotalXp;
			int currentTick = client.getTickCount();

			// If this XP drop is on a new tick, clear the tracker and advance the tick
			if (currentTick > lastExpDropTick)
			{
				lastExpDropTick = currentTick;
				skillsGainedLastTick.clear();
			}

			skillsGainedLastTick.add(event.getSkill());
			lastXpTicks[skillIdx] = currentTick;

			eventBus.post(new ExperienceDrop(event.getSkill(), xpGained, currentTick));
		}

		previousXp[skillIdx] = newTotalXp;
	}

	/**
	 * Returns a read-only view of all skills that received experience on the last active drop tick.
	 */
	public Set<Skill> getSkillsGainedLastTick()
	{
		return Collections.unmodifiableSet(skillsGainedLastTick);
	}

	/**
	 * Given a set of Skills, return the Skill for which a tick was registered most recently.
	 */
	public Skill getMostRecentDrop(Skill... skills)
	{
		Skill mostRecentSkill = null;
		int highestTick = -1;
		int currentTick = client.getTickCount();

		for (Skill skill : skills)
		{
			int dropTick = lastXpTicks[skill.ordinal()];

			if (currentTick - dropTick <= 2 && dropTick > highestTick)
			{
				highestTick = dropTick;
				mostRecentSkill = skill;
			}
		}

		return mostRecentSkill;
	}

	/**
	 * Checks if a specific skill received an experience drop on the most recent registered tick.
	 */
	public boolean hasSkillGainedExpLastTick(Skill skill)
	{
		if (skill == null)
		{
			return false;
		}

		return skillsGainedLastTick.contains(skill);
	}
}