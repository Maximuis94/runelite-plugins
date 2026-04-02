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

package com.datalogger.webhook;

import com.datalogger.DataLoggerConfig;
import static com.datalogger.constants.PluginConstants.COLOSSEUM_ATTEMPT_DIR;
import com.datalogger.dto.ColosseumAttemptDTO;
import com.datalogger.dto.ColosseumWaveDTO;
import com.datalogger.events.DataLoggerConfigChanged;
import com.datalogger.models.enums.ColosseumWebhookFormatter;
import com.datalogger.models.enums.ScreenshotFormat;
import com.datalogger.models.enums.WaveStatus;
import com.datalogger.services.DiscordWebhookService;
import static com.datalogger.webhook.DiscordWebhookUtils.addFooter;
import static com.datalogger.webhook.DiscordWebhookUtils.setColor;
import com.google.gson.JsonObject;
import java.awt.Color;
import java.io.File;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.Subscribe;

/**
 * Class that handles broadcasting to discord webhooks
 */
@Slf4j
@Singleton
public class ColosseumDiscordBroadcaster
{
	@Getter
	private static final String FOOTER_STANDARD = "RuneLite Data Logger";
	@Getter
	private static final String FOOTER_TEST = "RuneLite Data Logger - custom template webhook test";
	private static final Color TEST_COLOR = new Color(255, 191, 0);

	@Getter
	private boolean enabledDiscordBroadcasting;
	private boolean broadcastCompletedTrial;
	private boolean broadcastCancelledTrial;
	private boolean broadcastFailedTrial;
	private boolean broadcastScreenshot;
	private Integer minRewardValue;
	private Integer minWave;

	private String webhookUrl;
	private ScreenshotFormat screenshotFormat;
	private ColosseumWebhookFormatter webhookFormat;
	private String customTemplate;

	private final DataLoggerConfig config;
	private final DiscordWebhookService discordWebhookService;

	@Inject
	public ColosseumDiscordBroadcaster(DataLoggerConfig config, DiscordWebhookService discordWebhookService)
	{
		this.config = config;
		this.discordWebhookService = discordWebhookService;
	}

	@Subscribe
	public void onDataLoggerConfigChanged(DataLoggerConfigChanged event)
	{
		updateConfigFlags();
	}

	/**
	 * Extract relevant parameters from the plugin configurations
	 */
	public void updateConfigFlags()
	{
		webhookUrl = config.colosseumDiscordWebhookUrl().trim();
		enabledDiscordBroadcasting = config.enableWebhookBroadcasting() && webhookUrl != null && !webhookUrl.isEmpty();
		broadcastFailedTrial = config.broadcastFailedTrials();
		broadcastCancelledTrial = config.broadcastCancelledTrials();
		broadcastCompletedTrial = config.broadcastCompletedTrials();
		broadcastScreenshot = config.broadcastScreenshot();
		screenshotFormat = config.screenshotFormat();
		webhookFormat = config.colosseumWebhookFormat();
		customTemplate = config.colosseumCustomTemplate();
		minRewardValue = config.broadcastRewardThreshold() * 1000;
		minWave = config.broadcastWaveThreshold();
	}

	/**
	 * Checks if the given data should be broadcast and if so, formats and sends it to the webhook.
	 */
	public void broadcastToDiscord(ColosseumAttemptDTO attemptDto)
	{

		List<ColosseumWaveDTO> waves = attemptDto.getWaves();
		if (waves == null || waves.isEmpty())
		{
			log.info("Did not broadcast trial-- there are no waves...");
			return;
		}

		ColosseumWaveDTO finalWave = waves.get(waves.size() - 1);
		if (!shouldBroadcastAttempt(finalWave, attemptDto.getRewardsValue()))
		{
			log.info("Did not broadcast trial-- Prerequisite conditions were not met...");
			return;
		}

		JsonObject payload;

		switch (webhookFormat)
		{
			case CONCISE:
				payload = ColosseumConciseDiscordFormatter.buildPayload(attemptDto, config);
				break;
			case CUSTOM:
				payload = ColosseumCustomDiscordFormatter.buildPayload(attemptDto, customTemplate);
				break;
			case DETAILED:
			default:
				payload = ColosseumDetailedDiscordFormatter.buildPayload(attemptDto, config);
				break;
		}

		log.info("Submitting {} payload to webhookUrl {}", webhookFormat.name(), webhookUrl);

		if (broadcastScreenshot)
		{
			File screenshotFile = getBroadcastScreenshotFile(finalWave, attemptDto.getAttemptId());
			discordWebhookService.queueWebhook(webhookUrl, payload, screenshotFile);
		}
		else
		{
			discordWebhookService.sendWebhook(webhookUrl, payload);
		}
	}

	/**
	 * Return true if the finished trial should be broadcast.
	 */
	private boolean shouldBroadcastAttempt(ColosseumWaveDTO finalWave, int totalReward)
	{
		WaveStatus status = WaveStatus.fromString(finalWave.getStatus());
		boolean hasSucceeded = status != WaveStatus.FAILED && status != WaveStatus.LOGGED_OUT;

		// Include the potential loot as well, if the final wave was not completed
		int netReward = status != WaveStatus.COMPLETED ? 0 : totalReward + finalWave.getLootValue();


		boolean statusConditionMet;
		switch (status) {
			case COMPLETED:
				statusConditionMet = broadcastCompletedTrial;
				break;
			case FAILED:
				statusConditionMet = broadcastFailedTrial;
				break;
			case CANCELLED:
			case CLAIMED:
				statusConditionMet = broadcastCancelledTrial;
				break;
			default:
				statusConditionMet = false;
		}

		boolean result = (netReward > minRewardValue || finalWave.getWave() >= minWave);

		log.info("CurrentAttempt has {}; waveNumber and rewardsValue conditions have {}been met; {}broadcasting run", hasSucceeded ? "succeeded" : "failed", result ? "" : "not ", statusConditionMet&&result ? "" : "not ");
		return statusConditionMet && result;
	}

	/**
	 * Return the expected screenshotFile, given ColosseumAttemptDTO and its associated root.
	 */
	private File getBroadcastScreenshotFile(ColosseumWaveDTO finalWave, String trialId)
	{
		WaveStatus waveStatus = WaveStatus.fromString(finalWave.getStatus());
		if (waveStatus == null) return null;

		String affix;
		int waveNumber;
		switch (waveStatus)
		{
			case FAILED:
				affix = "-death";
				waveNumber = finalWave.getWave();
				break;
			case CLAIMED:
			case CANCELLED:
				affix = "-reward";
				waveNumber = finalWave.getWave()-1;
				break;
			case COMPLETED:
				affix = "";
				waveNumber = finalWave.getWave();
				break;
			default:
				return null;
		}

		String extension = screenshotFormat.getExtension();
		File root = new File(COLOSSEUM_ATTEMPT_DIR, trialId);
		File file = new File(root, String.format("wave-%02d%s.%s", waveNumber, affix, extension));
		log.info("Identified broadcast screenshot file {}", file.getAbsolutePath());

		return file;
	}

	/**
	 * Generate a basic embed with a title and footer. The account is added to the title String.
	 */
	public static JsonObject generateBaseEmbed(String account)
	{
		JsonObject embed = new JsonObject();
		embed.addProperty("title", "🏟️ **Colosseum Run by " + account + "**");
		addFooter(embed, FOOTER_STANDARD);
		return embed;
	}

	/**
	 * Generate a basic embed with an alternative title and footer that emphasizes that it is a mock submission.
	 */
	public static JsonObject generateTestEmbed(String account)
	{
		JsonObject embed = new JsonObject();
		embed.addProperty("title", "🚧 Colosseum Run by " + account + " (**custom template test**)");
		addFooter(embed, FOOTER_TEST);
		setColor(embed, TEST_COLOR);
		return embed;
	}
}