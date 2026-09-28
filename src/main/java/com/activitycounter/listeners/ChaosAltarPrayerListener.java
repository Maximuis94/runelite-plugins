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
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

@Singleton
@Slf4j
public class ChaosAltarPrayerListener {
	private static final int CHAOS_ALTAR_REGION_ID = 11835;
	private static final String BONES_SPARED_MESSAGE = "The Dark Lord spares your sacrifice but still rewards you for your efforts.";

	@Inject
	private Client client;

	@Inject
	private ActivityCounterPlugin plugin;

	@Inject
	private SoundEffectListener soundEffectListener;

	private boolean isAtChaosAltarRegion() {
		Player player = client.getLocalPlayer();
		if (player == null) return false;
		WorldPoint wp = player.getWorldLocation();
		return wp.getRegionID() == CHAOS_ALTAR_REGION_ID;
	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event) {
		if (!soundEffectListener.isHasSoundEffects()) {
			return;
		}

		if (event.getSoundId() == PluginConstants.ALTAR_OFFER_SOUND_ID) {
			boolean isAtChaosAltar = isAtChaosAltarRegion();
			plugin.increaseCountByOne(isAtChaosAltar ?
				TrackedActivity.SACRIFICES_MADE.getId() :
				TrackedActivity.POH_SACRIFICES_MADE.getId());
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		String message = Text.removeTags(event.getMessage());

		if (message.equals(BONES_SPARED_MESSAGE)) {
			plugin.increaseCountByOne(TrackedActivity.SACRIFICES_SPARED.getId());
		}
	}
}