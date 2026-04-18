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
import com.datalogger.events.DataLoggerConfigChanged;
import com.datalogger.events.PlayerDied;
import com.datalogger.framework.LogType;
import com.datalogger.models.enums.BroadcastColosseumScreenshotOption;
import com.datalogger.models.enums.ScreenshotFormat;
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
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.Text;

/**
 * Centralized class that handles the creation and logging of screenshots.
 */
@Slf4j
@Singleton
public class ScreenshotLogger {
	@Inject private DrawManager drawManager;
	@Inject private ScheduledExecutorService executor;
	@Inject private FileIOService fileIOService;
	@Inject private EventBus eventBus;

	@Inject private DataLoggerConfig config;
	private ScreenshotFormat screenshotFormat;
	private boolean screenshotBetweenColosseumWaves;
	private boolean screenshotColosseumRewardsUI;
	private boolean screenshotColosseumSuccess;
	private boolean screenshotColosseumClaim;
	private boolean screenshotColosseumDeath;

	private boolean awaitingColosseumReset = false;
	private boolean isActiveAttempt = false;
	private boolean intermissionScreenshotPending = false;
	private boolean rewardChestSpawned = false;
	private int colosseumCurrentWave = 0;

	private String colosseumAttemptId = null;

	private File colosseumRoot;

	@Subscribe
	public void onDataLoggerConfigChanged(DataLoggerConfigChanged event)
	{
		updateConfigFlags();
	}

	/**
	 * Loads configuration values into class variables
	 */
	public void updateConfigFlags()
	{
		BroadcastColosseumScreenshotOption broadcastScreenshotConfig = config.broadcastScreenshot();
		screenshotBetweenColosseumWaves = config.screenshotBetweenWaves();

		screenshotColosseumSuccess = broadcastScreenshotConfig.isScreenshotOnSuccess();
		screenshotColosseumClaim = broadcastScreenshotConfig.isScreenshotOnClaim();
		screenshotColosseumRewardsUI = broadcastScreenshotConfig.isScreenshotOnRewardsUI();

		screenshotColosseumDeath = broadcastScreenshotConfig.isScreenshotOnFailure();

		screenshotFormat = config.screenshotFormat();
	}

	@Subscribe
	public void onColosseumAttemptStarted(ColosseumAttemptStarted event) {
		isActiveAttempt = true;
		colosseumAttemptId = event.getStartTime();
		colosseumCurrentWave = 1;
		intermissionScreenshotPending = true;
		rewardChestSpawned = false;
		colosseumRoot = new File(event.getRoot());
		log.debug("Screenshot logger synced with attempt ID: {}", colosseumAttemptId);
	}

	public void resetColosseum() {
		log.debug("Screenshotlogger ended colosseum attempt with id {}", colosseumAttemptId);
		isActiveAttempt = false;
		colosseumAttemptId = null;
		colosseumCurrentWave = 0;
		intermissionScreenshotPending = false;
		rewardChestSpawned = false;
		colosseumRoot = null;
		awaitingColosseumReset = false;
	}

	/**
	 * Return the File for the screenshot for a specific wave / whether it is a reward
	 */
	private String getScreenshotFileName(int waveNumber, String affix)
	{
		return String.format("wave-%02d%s", waveNumber, affix);
	}

	/**
	 * Create a screenshot and save it in the given fileName in the given subDirectory
	 */
	private void captureAndSaveScreenshot() {
		String fileName = getScreenshotFileName(colosseumCurrentWave, rewardChestSpawned ? "-reward" : "");

		drawManager.requestNextFrameListener(image ->
		{
			BufferedImage bufferedImage = (BufferedImage) image;
			executor.submit(() ->
			{
				try
				{

					fileIOService.saveScreenshot(bufferedImage, colosseumRoot, fileName, screenshotFormat, colosseumCurrentWave == 12 || rewardChestSpawned);

					if (awaitingColosseumReset)
						resetColosseum();
				}
				catch (Exception e)
				{
					log.error("Failed to save screenshot", e);
				}
			});
		});
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
		if (ignoreChatMessageType(event.getType())) {
			return;
		}

		String message = Text.removeTags(event.getMessage());

		if ((screenshotColosseumRewardsUI || screenshotBetweenColosseumWaves) && !intermissionScreenshotPending)
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
		if ((screenshotColosseumRewardsUI || screenshotBetweenColosseumWaves) && intermissionScreenshotPending && isPostColosseumWaveUI(event.getScriptId()))
		{
			takeColosseumScreenshot(event.getScriptId());
		}
	}

	/**
	 * Screenshot taken in the event of a colosseum death
	 */
	private void screenshotColosseumDeath()
	{
		String fileName = getScreenshotFileName(colosseumCurrentWave, "-death");
		drawManager.requestNextFrameListener(image ->
		{
			BufferedImage bufferedImage = (BufferedImage) image;
			executor.submit(() ->
			{
				try
				{
					fileIOService.saveScreenshot(bufferedImage, colosseumRoot, fileName, screenshotFormat, true);

					resetColosseum();
				}
				catch (Exception e)
				{
					log.error("Failed to save screenshot", e);
				}
			});
		});
	}

	@Subscribe
	public void onPlayerDied(PlayerDied event)
	{
		if ((screenshotColosseumDeath || screenshotBetweenColosseumWaves) && event.getLogType() == LogType.COLOSSEUM)
		{
			screenshotColosseumDeath();
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
		awaitingColosseumReset = scriptId == POPULATE_REWARDS_CHEST_UI_SCRIPT_ID;
		if (screenshotBetweenColosseumWaves ||
			awaitingColosseumReset &&
				(screenshotColosseumSuccess && colosseumCurrentWave == 12 ||
				screenshotColosseumClaim && colosseumCurrentWave < 12))
			captureAndSaveScreenshot();
		intermissionScreenshotPending = false;
		rewardChestSpawned = false;
	}
}