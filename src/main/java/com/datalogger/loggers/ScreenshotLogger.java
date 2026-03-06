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
package com.datalogger.loggers;

import com.datalogger.DataLoggerConfig;
import com.datalogger.events.ColosseumAttemptStarted;
import static com.datalogger.constants.Colosseum.Intermission.INTERMISSION_GROUP_ID;
import static com.datalogger.constants.Colosseum.Message.BOSS_WAVE_START_PREFIX;
import static com.datalogger.constants.Colosseum.Message.WAVE_START_PREFIX;
import static com.datalogger.constants.Colosseum.RewardsChest.REWARDS_CHEST_GROUP_ID;
import com.datalogger.services.FileIOService;

import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class ScreenshotLogger {
	@Inject private Client client;
	@Inject private DataLoggerConfig config;
	@Inject private DrawManager drawManager;
	@Inject private ScheduledExecutorService executor;
	@Inject private FileIOService fileIOService;

	private boolean colosseumScreenshotPending = false;
	private int colosseumCurrentWave = 0;

	private String colosseumAttemptId = null;

	@Subscribe
	public void onColosseumAttemptStarted(ColosseumAttemptStarted event) {
		this.colosseumAttemptId = event.getStartTime();
		this.colosseumCurrentWave = 1;
		this.colosseumScreenshotPending = true;
		log.debug("Screenshot logger synced with attempt ID: {}", this.colosseumAttemptId);
	}

	/**
	 * Gets the synced Attempt ID, or generates a fallback if none exists.
	 */
	private String getOrCreateAttemptId() {
		if (this.colosseumAttemptId == null) {
			this.colosseumAttemptId = Instant.ofEpochMilli(System.currentTimeMillis())
				.atZone(ZoneId.systemDefault())
				.format(DateTimeFormatter.ofPattern("yyMMdd_HHmmss"));
			log.debug("No active attempt ID found. Generated fallback ID: {}", this.colosseumAttemptId);
		}
		return this.colosseumAttemptId;
	}

	public void captureAndSaveScreenshot(String subDirectory, String fileName) {
		drawManager.requestNextFrameListener(image -> {
			executor.submit(() -> {
				fileIOService.saveScreenshot((BufferedImage) image, subDirectory, fileName);
			});
		});
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if (!config.screenshotBetweenWaves()) return;

		if (event.getType() != ChatMessageType.NPC_SAY && event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.CONSOLE) {
			return;
		}

		String message = Text.removeTags(event.getMessage());

		if (colosseumCurrentWave < 11 && message.startsWith(WAVE_START_PREFIX)) {
			colosseumCurrentWave = Integer.parseInt(message.replace("Wave: ", "").trim());
			colosseumScreenshotPending = true;
		} else if (colosseumCurrentWave == 11 && message.startsWith(BOSS_WAVE_START_PREFIX)) {
			colosseumCurrentWave = 12;
			colosseumScreenshotPending = true;
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event) {
		if (!config.screenshotBetweenWaves() || !colosseumScreenshotPending) return;

		int groupId = event.getGroupId();
		if (groupId == INTERMISSION_GROUP_ID || groupId == REWARDS_CHEST_GROUP_ID) {
			String attemptId = getOrCreateAttemptId();

			String subDir = "colosseum/" + attemptId;
			String fileName = String.format("wave-%02d", colosseumCurrentWave);

			captureAndSaveScreenshot(subDir, fileName);
			colosseumScreenshotPending = false;

			if (groupId == REWARDS_CHEST_GROUP_ID) {
				this.colosseumAttemptId = null;
			}
		}
	}
}