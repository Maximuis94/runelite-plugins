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
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.eventbus.Subscribe;

/**
 * Listeners that count whenever a specific sound is played. Aside from handling singular soundEffectListeners, it also
 * manages the volume options and notifies other classes that rely on soundEffects accordingly, if need be.
 */
@Slf4j
@Singleton
public class SoundEffectListener
{
	@Inject
	private Client client;

	@Inject
	private ActivityCounterPlugin plugin;

	@Inject
	private ChaosAltarPrayerListener chaosAltarPrayerListener;

	@Getter
	private boolean hasAnySound = false;
	@Getter
	private boolean hasSoundEffects = false;
	@Getter
	private boolean hasAreaSound = false;
	@Getter
	private boolean hasMusic = false;

	// Tick scheduling for debouncing logs
	private int scheduledLogTick = -1;

	private static final int SOUND_EFFECT_VOLUME_VARPLAYERID = VarPlayerID.OPTION_SOUNDS;
	private static final int SOUND_AREA_VOLUME_VARPLAYERID = VarPlayerID.OPTION_AREASOUNDS;
	private static final int SOUND_MUSIC_VOLUME_VARPLAYERID = VarPlayerID.OPTION_MUSIC;
	private static final int SOUND_MASTER_VOLUME_VARPLAYERID = VarPlayerID.OPTION_MASTER_VOLUME;

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
	public void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		if (!hasSoundEffects) return;

		int soundId = event.getSoundId();
		TrackedActivity activity;
		switch (soundId)
		{
			case PluginConstants.SoundID.TELEPORT_TABLET:
				activity = TrackedActivity.TELEPORT_TABLETS_USED;
				plugin.increaseCountByOne(activity.getId());
				break;

			case PluginConstants.SoundID.SCYTHE_SLASH:
			case PluginConstants.SoundID.SCYTHE_CRUSH:
				activity = TrackedActivity.SCYTHE_SWIPE;
				plugin.increaseCountByOne(activity.getId());
				break;


			default:
				log.debug("Unknown soundId={}", soundId);
				return;
		}

		log.debug("Successfully recognized sound for counter={}", activity.getName());

	}

//	@Subscribe
//	public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed event)
//	{
//		if (!hasAreaSound) return;
//	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (scheduledLogTick != -1 && client.getTickCount() >= scheduledLogTick)
		{
			executeNoSoundActiveLog();
			scheduledLogTick = -1;
		}
	}

	/**
	 * Parse the sound settings and update flags accordingly. If a volume is 0, no call is made if the sound would have
	 * been played, which will severely impair this listener.
	 */
	public void parseCurrentState()
	{
		hasAnySound = client.getVarpValue(SOUND_MASTER_VOLUME_VARPLAYERID) > 0;
		hasSoundEffects = hasAnySound && client.getVarpValue(SOUND_EFFECT_VOLUME_VARPLAYERID) > 0;
		hasAreaSound = hasAnySound && client.getVarpValue(SOUND_AREA_VOLUME_VARPLAYERID) > 0;
		hasMusic = hasAnySound && client.getVarpValue(SOUND_MUSIC_VOLUME_VARPLAYERID) > 0;
		scheduleNoSoundActiveLog();
	}

	/**
	 * Pushes the target log tick 5 ticks into the future.
	 */
	private void scheduleNoSoundActiveLog()
	{
		scheduledLogTick = client.getTickCount() + 5;
	}

	/**
	 * Logs which audio channels are currently muted.
	 */
	private void executeNoSoundActiveLog()
	{
		if (hasAnySound && hasSoundEffects && hasAreaSound && hasMusic)
		{
			return;
		}

		List<String> muted = new ArrayList<>();

		if (!hasAnySound)
		{
			muted.add("Master Volume (which mutes the rest)");
		}
		if (!hasSoundEffects) muted.add("Sound Effects");
		if (!hasAreaSound) muted.add("Area Sounds");
		if (!hasMusic) muted.add("Music");

		log.warn("The following audio channels are muted: {}", String.join(", ", muted));
	}
}