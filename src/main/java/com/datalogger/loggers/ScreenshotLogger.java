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
import static com.datalogger.constants.Colosseum.Message.BOSS_WAVE_START_PREFIX;
import static com.datalogger.constants.Colosseum.Message.END_ATTEMPT_MESSAGE;
import static com.datalogger.constants.Colosseum.Message.WAVE_START_PREFIX;
import static com.datalogger.constants.Colosseum.Script.POPULATE_INTERMISSION_UI_SCRIPT_ID;
import static com.datalogger.constants.Colosseum.Script.POPULATE_REWARDS_CHEST_UI_SCRIPT_ID;
import com.datalogger.events.ColosseumAttemptStarted;
import com.datalogger.services.FileIOService;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.Text;

/**
 * Centralized class that handles the creation and logging of screenshots.
 */
@Slf4j
@Singleton
public class ScreenshotLogger {
	@Inject private DataLoggerConfig config;
	@Inject private DrawManager drawManager;
	@Inject private ScheduledExecutorService executor;
	@Inject private FileIOService fileIOService;

	private boolean intermissionScreenshotPending = false;
	private boolean rewardChestSpawned = false;
	private int colosseumCurrentWave = 0;

	private String colosseumAttemptId = null;

	private File root;

	@Subscribe
	public void onColosseumAttemptStarted(ColosseumAttemptStarted event) {
		colosseumAttemptId = event.getStartTime();
		colosseumCurrentWave = 1;
		intermissionScreenshotPending = true;
		rewardChestSpawned = false;
		root = new File(event.getRoot());
		log.debug("Screenshot logger synced with attempt ID: {}", colosseumAttemptId);
	}

	/**
	 * Return the File for the screenshot for a specific wave / whether it is a reward
	 */
	private String getScreenshotFileName(int waveNumber, boolean isReward)
	{
		return String.format("wave-%02d%s", waveNumber, isReward ? "-reward" : "");
	}

	/**
	 * Create a screenshot and save it in the given fileName in the given subDirectory
	 */
	private void captureAndSaveScreenshot() {
		String fileName = getScreenshotFileName(colosseumCurrentWave, rewardChestSpawned);

		drawManager.requestNextFrameListener(image ->
			fileIOService.saveScreenshot((BufferedImage) image, root, fileName)
		);
	}

	/**
	 * Return true if the given messageType is to be ignored
	 */
	private boolean ignoreChatMessageType(ChatMessageType messageType)
	{
		return messageType != ChatMessageType.NPC_SAY &&
			messageType != ChatMessageType.GAMEMESSAGE &&
			messageType != ChatMessageType.CONSOLE;
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if (!config.screenshotBetweenWaves() || ignoreChatMessageType(event.getType())) {
			return;
		}

		String message = Text.removeTags(event.getMessage());

		if (!intermissionScreenshotPending)
			updateColosseumFlags(message);
	}


	/**
	 * Return true if the UI intermission/rewards chest UI is ready to be parsed
	 */
	private boolean isPostColosseumWaveUI(int scriptId)
	{
		return scriptId == POPULATE_INTERMISSION_UI_SCRIPT_ID || scriptId == POPULATE_REWARDS_CHEST_UI_SCRIPT_ID;
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event) {
		if (intermissionScreenshotPending && isPostColosseumWaveUI(event.getScriptId()))
		{
			takeColosseumScreenshot(event.getScriptId());
		}
	}

	/**
	 * Set the colosseumScreenshotPending variable to true if a new wave has started, in case of specific chat messages
	 */
	private void updateColosseumFlags(String message)
	{
		if (colosseumCurrentWave < 11 && message.startsWith(WAVE_START_PREFIX)) {
			colosseumCurrentWave = Integer.parseInt(message.replace("Wave: ", "").trim());
			intermissionScreenshotPending = true;

		} else if (colosseumCurrentWave == 11 && message.startsWith(BOSS_WAVE_START_PREFIX)) {
			colosseumCurrentWave = 12;
			intermissionScreenshotPending = true;

		} else if (message.startsWith(END_ATTEMPT_MESSAGE)) {
			intermissionScreenshotPending = true;
			rewardChestSpawned = true;
		}
	}




	/**
	 * Take a screenshot in case the intermission or rewards chest UI has appeared and subsequently update flags
	 * @param scriptId The scriptId of the script that populates the UI
	 */
	private void takeColosseumScreenshot(int scriptId)
	{
		captureAndSaveScreenshot();
		intermissionScreenshotPending = false;
		rewardChestSpawned = false;

		if (scriptId == POPULATE_REWARDS_CHEST_UI_SCRIPT_ID) {
			colosseumAttemptId = null;
		}
	}
}