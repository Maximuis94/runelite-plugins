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
import com.activitycounter.models.TrackedActivity;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import net.runelite.api.Skill;
import net.runelite.api.events.AreaSoundEffectPlayed;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
public class SoundEffectListener
{
	@Inject
	private Client client;

	@Inject
	private ActivityCounterPlugin plugin;

	@Inject
	private ExperienceDropListener xpDropListener;

	// Map specific activities to the Skill they grant XP in
	private final Map<TrackedActivity, Skill> activitySkillMap = new EnumMap<>(TrackedActivity.class);

	// Add this to your initializeRoutingCaches() or Constructor:
	private void buildSkillMap() {
		activitySkillMap.put(TrackedActivity.PICKPOCKET_SUCCESS, Skill.THIEVING);
		activitySkillMap.put(TrackedActivity.CROPS_HARVESTED, Skill.FARMING); // Revert to whichever enum handles the sound
		// Add any future XP-generating sound activities here
	}

	@Getter
	private boolean hasAnySound = false;
	@Getter
	private boolean hasSoundEffects = false;
	@Getter
	private boolean hasAreaSound = false;
	@Getter
	private boolean hasMusic = false;

	private int scheduledLogTick = -1;
	private int hitSplatTick = 0;

	private int pickpocketTick = 0;
	private static final int PICKPOCKET_TICK_WINDOW = 5;

	private static final int SOUND_EFFECT_VOLUME_VARPLAYERID = VarPlayerID.OPTION_SOUNDS;
	private static final int SOUND_AREA_VOLUME_VARPLAYERID = VarPlayerID.OPTION_AREASOUNDS;
	private static final int SOUND_MUSIC_VOLUME_VARPLAYERID = VarPlayerID.OPTION_MUSIC;
	private static final int SOUND_MASTER_VOLUME_VARPLAYERID = VarPlayerID.OPTION_MASTER_VOLUME;

	private static final int MAX_SOUND_ID = 10000;

	@SuppressWarnings("unchecked")
	private final List<TrackedActivity>[] soundEffectArray = new List[MAX_SOUND_ID];

	@SuppressWarnings("unchecked")
	private final List<TrackedActivity>[] areaSoundEffectArray = new List[MAX_SOUND_ID];

	private boolean isInitialized = false;

	/**
	 * Sets up routing caches for sound listeners
	 */
	public void initializeRoutingCaches() {
		if (isInitialized) return;
		buildSkillMap();
		for (TrackedActivity act : TrackedActivity.values()) {
			int[] sourceIds = act.getGameSourceIds();
			if (sourceIds == null) continue;

			for (int soundId : sourceIds) {
				if (soundId <= 0 || soundId >= MAX_SOUND_ID) continue;

				switch (act.getTrackerType()) {
					case SOUND_EFFECT:
						if (soundEffectArray[soundId] == null) {
							soundEffectArray[soundId] = new ArrayList<>();
						}
						soundEffectArray[soundId].add(act);
						break;
					case AREA_SOUND:
						if (areaSoundEffectArray[soundId] == null) {
							areaSoundEffectArray[soundId] = new ArrayList<>();
						}
						areaSoundEffectArray[soundId].add(act);
						break;
				}
			}
		}
		isInitialized = true;
	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event) {
		int soundId = event.getSoundId();
		if (soundId < 0 || soundId >= MAX_SOUND_ID) return;

		if (client.getTickCount() < pickpocketTick) {
			if (soundId == PluginConstants.ActivityID.PICKPOCKET_SUCCESS)
			{
				plugin.increaseCountByOne(TrackedActivity.PICKPOCKET_SUCCESS.getId());
				pickpocketTick = 0;
				return;
			}
			if (soundId == PluginConstants.SoundID.STUNNED_SOUND_EFFECT_ID)
			{
				plugin.increaseCountByOne(TrackedActivity.PICKPOCKET_FAIL.getId());
				pickpocketTick = 0;
				return;
			}
		}

		List<TrackedActivity> activities = soundEffectArray[soundId];
		if (activities == null || activities.isEmpty()) return;

		if (activities.size() == 1) {
			plugin.increaseCountByOne(activities.get(0).getId());
			return;
		}

		Skill[] competingSkills = new Skill[activities.size()];
		for (int i = 0; i < activities.size(); i++) {
			competingSkills[i] = activitySkillMap.get(activities.get(i));
		}

		Skill winningSkill = xpDropListener.getMostRecentDrop(competingSkills);

		if (winningSkill != null) {
			for (TrackedActivity act : activities) {
				if (activitySkillMap.get(act) == winningSkill) {
					plugin.increaseCountByOne(act.getId());
					break;
				}
			}
		}
	}

	@Subscribe
	public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed event) {
		int soundId = event.getSoundId();

		if (soundId < 0 || soundId >= MAX_SOUND_ID) return;

		List<TrackedActivity> activities = areaSoundEffectArray[soundId];
		if (activities != null) {
			for (TrackedActivity act : activities) {
				plugin.increaseCountByOne(act.getId());
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		switch (event.getVarpId()) {
			case -1:
				return;
			case SOUND_MASTER_VOLUME_VARPLAYERID:
				parseCurrentState();
				break;
			case SOUND_EFFECT_VOLUME_VARPLAYERID:
				hasSoundEffects = hasAnySound && client.getVarpValue(SOUND_EFFECT_VOLUME_VARPLAYERID) > 0;
				scheduleNoSoundActiveLog();
				break;
			case SOUND_AREA_VOLUME_VARPLAYERID:
				hasAreaSound = hasAnySound && client.getVarpValue(SOUND_AREA_VOLUME_VARPLAYERID) > 0;
				scheduleNoSoundActiveLog();
				break;
			case SOUND_MUSIC_VOLUME_VARPLAYERID:
				hasMusic = hasAnySound && client.getVarpValue(SOUND_MUSIC_VOLUME_VARPLAYERID) > 0;
				scheduleNoSoundActiveLog();
				break;
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		Hitsplat hitsplat = event.getHitsplat();
		int type = hitsplat.getHitsplatType();
		if (type == HitsplatID.DAMAGE_MAX_ME || type == HitsplatID.DAMAGE_ME)
		{
			hitSplatTick = client.getTickCount();
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (event.getSkill() == Skill.MAGIC)
		{
			log.debug("[TICK={}] Magic stat changed event={}", client.getTickCount(), event);
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (scheduledLogTick != -1 && client.getTickCount() >= scheduledLogTick)
		{
			executeNoSoundActiveLog();
			scheduledLogTick = -1;
		}
	}

	public void parseCurrentState()
	{
		hasAnySound = client.getVarpValue(SOUND_MASTER_VOLUME_VARPLAYERID) > 0;
		hasSoundEffects = hasAnySound && client.getVarpValue(SOUND_EFFECT_VOLUME_VARPLAYERID) > 0;
		hasAreaSound = hasAnySound && client.getVarpValue(SOUND_AREA_VOLUME_VARPLAYERID) > 0;
		hasMusic = hasAnySound && client.getVarpValue(SOUND_MUSIC_VOLUME_VARPLAYERID) > 0;
		scheduleNoSoundActiveLog();
	}

	private void scheduleNoSoundActiveLog()
	{
		scheduledLogTick = client.getTickCount() + 5;
	}

	private void executeNoSoundActiveLog()
	{
		if (hasAnySound && hasSoundEffects && hasAreaSound && hasMusic)
		{
			return;
		}

		List<String> muted = new ArrayList<>();
		if (!hasAnySound) muted.add("Master Volume (which mutes the rest)");
		if (!hasSoundEffects) muted.add("Sound Effects");
		if (!hasAreaSound) muted.add("Area Sounds");
		if (!hasMusic) muted.add("Music");

		log.warn("The following audio channels are muted: {}", String.join(", ", muted));
	}

	/**
	 * Sets pickpocket status
	 */
	public void startPickpocketing()
	{
		pickpocketTick = client.getTickCount() + PICKPOCKET_TICK_WINDOW ;
	}
}