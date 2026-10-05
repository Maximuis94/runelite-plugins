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
import com.activitycounter.events.ExperienceDrop;
import com.activitycounter.models.TrackedActivity;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

/**
 * Class for handling custom farming-related counters.
 */
@Slf4j
@Singleton
public class FarmingListener {
	@Setter
	@Getter
	private boolean isRegistered = false;

	@Inject
	private Client client;

	@Inject
	private ActivityCounterPlugin plugin;

	@Inject
	private ConfigListener configListener;

	private boolean isHarvesting = false;
	private TrackedActivity activityInProgress = null;

	private int expectedXp = -1;
	private int timeoutTick = -1;
	private static final int HARVESTING_TICK_WINDOW = 10;

	/**
	 * Minimal check to evaluate if the given String is potentially farming-related.
	 * Returns the resulting status.
	 */
	private boolean isFarmingChatType(ChatMessageType type) {
		return type == ChatMessageType.GAMEMESSAGE || type == ChatMessageType.SPAM;
	}

	/**
	 * Process the preprocessed chat message. The returned value indicates whether the message was relevant for the
	 * FarmingListener.
	 */
	public boolean onChatMessage(String message, ChatMessageType type) {
		if (!isRegistered || !isFarmingChatType(type)) return false;

		if (!isHarvesting) {
			TrackedActivity activity;
			switch (message)
			{
				case "You begin to harvest the herb patch.":
					startHarvesting(TrackedActivity.HERBS_HARVESTED);
					return true;
				case "You begin to harvest the allotment.":
					startHarvesting(TrackedActivity.CROPS_HARVESTED);
					return true;
				case "Your spell fails and the patch is cleared.":
					activity = TrackedActivity.CROP_RESURRECTION_FAIL;
					break;
				case "You restore the patch to life.":
					activity = TrackedActivity.CROP_RESURRECTION_SUCCESS;
					break;
				default:
					activity = null;
			}
			if (activity != null)
			{
				if (configListener.isKcVisible(activity))
				{
					plugin.increaseCountByOne(activity.getId());
				}
				return true;
			}
		}

		else if (activityInProgress != null) {
			if ((message.equals("The herb patch is now empty.") && activityInProgress == TrackedActivity.HERBS_HARVESTED) ||
				(message.equals("The allotment is now empty.") && activityInProgress == TrackedActivity.CROPS_HARVESTED)) {

				plugin.increaseCountByOne(activityInProgress.getId());
				isHarvesting = false;
				activityInProgress = null;
				timeoutTick = client.getTickCount();
				return true;
			}
		}
		return false;
	}

	private void startHarvesting(TrackedActivity activity) {
		isHarvesting = true;
		activityInProgress = activity;
		expectedXp = -1;
		timeoutTick = client.getTickCount() + HARVESTING_TICK_WINDOW;
	}

	@Subscribe
	public void onExperienceDrop(ExperienceDrop event) {
		if (!isHarvesting || event.getSkill() != Skill.FARMING) return;

		int xpGained = event.getXpGained();

		if (activityInProgress != null)
		{
			if (expectedXp == -1)
			{
				expectedXp = xpGained;
				plugin.increaseCountByOne(activityInProgress.getId());
				timeoutTick = client.getTickCount() + HARVESTING_TICK_WINDOW;
				log.debug("Registered harvest exp drop of {}", expectedXp);
			}
			else if (Math.abs(xpGained - expectedXp) <= 2)
			{
				plugin.increaseCountByOne(activityInProgress.getId());
				timeoutTick = client.getTickCount() + HARVESTING_TICK_WINDOW;
			}
			else {
				log.debug("Harvesting activity with {} exp was reset", expectedXp);
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (isHarvesting && client.getTickCount() > timeoutTick) {
			isHarvesting = false;
			activityInProgress = null;
		}
	}
}