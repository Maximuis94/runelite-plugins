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
import com.datalogger.models.enums.ColosseumBroadcastMode;
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
	private ColosseumBroadcastMode broadcastCompletedTrial;
	private ColosseumBroadcastMode broadcastCancelledTrial;
	private ColosseumBroadcastMode broadcastFailedTrial;
	private Integer minRewardValue;
	private Integer minWave;

	private String webhookUrl;
	private ScreenshotFormat screenshotFormat;
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
		screenshotFormat = config.screenshotFormat();
		customTemplate = config.colosseumCustomTemplate();
		minRewardValue = config.broadcastRewardThreshold() * 1000;
		minWave = config.broadcastWaveThreshold();
	}

	private JsonObject generatePayload(ColosseumAttemptDTO attemptDTO, ColosseumWebhookFormatter formatter, boolean attachScreenshot, boolean isTest)
	{
		if (formatter == null)
		{
			return attachScreenshot ? ColosseumScreenshotDiscordFormatter.buildPayload(isTest) : null;
		}

		switch (formatter)
		{
			case CONCISE:
				return ColosseumConciseDiscordFormatter.buildPayload(attemptDTO, config, isTest);
			case CUSTOM:
				return ColosseumCustomDiscordFormatter.buildPayload(attemptDTO, customTemplate, isTest);
			case SCREENSHOT:
				return ColosseumScreenshotDiscordFormatter.buildPayload(isTest);
			case DETAILED:
				return ColosseumDetailedDiscordFormatter.buildPayload(attemptDTO, config, isTest);
			default:
				log.debug("Unexpected state occurred for webhook format: {}", formatter);
				return null;
		}
	}

	private JsonObject generateCustomTestPayload(ColosseumAttemptDTO attemptDTO, boolean attachScreenshot, String customTemplate)
	{
		return ColosseumCustomDiscordFormatter.buildPayload(attemptDTO, customTemplate, true);
	}

	/**
	 * Checks if the given data should be broadcast and if so, formats and sends it to the webhook.
	 */
	public void broadcastToDiscord(ColosseumAttemptDTO attemptDto, boolean isTest)
	{
		List<ColosseumWaveDTO> waves = attemptDto.getWaves();
		if (waves == null || waves.isEmpty())
		{
			log.debug("Did not broadcast trial-- there are no waves...");
			return;
		}

		ColosseumWaveDTO finalWave = waves.get(waves.size() - 1);

		ColosseumBroadcastMode broadcastMode;
		switch (WaveStatus.fromString(attemptDto.getResult()))
		{
			case COMPLETED:
				broadcastMode = broadcastCompletedTrial;
//				webhookFormatter = broadcastCompletedTrial;
//				attachScreenshot = broadcastCompletedTrial.isAttachScreenshot();
//				isScreenshotFormat = (webhookFormat == ColosseumWebhookFormatter.SCREENSHOT);
//				payload = generatePayload(attemptDto, broadcastCompletedTrial);
//				attachScreenshot = isScreenshotFormat || shouldBroadcastScreenshot(finalWave);
				break;
			case FAILED:
				broadcastMode = broadcastFailedTrial;
//				isScreenshotFormat = (webhookFormat == ColosseumWebhookFormatter.SCREENSHOT);
//				payload = generatePayload(attemptDto, broadcastFailedTrial);
//				attachScreenshot = isScreenshotFormat || shouldBroadcastScreenshot(finalWave);
				break;
			default:
				broadcastMode = broadcastCancelledTrial;

//				isScreenshotFormat = (webhookFormat == ColosseumWebhookFormatter.SCREENSHOT);
//				payload = generatePayload(attemptDto, broadcastCancelledTrial);
//				attachScreenshot = isScreenshotFormat || shouldBroadcastScreenshot(finalWave);
				break;
		}
		ColosseumWebhookFormatter webhookFormatter = broadcastMode.getFormatter();
		boolean attachScreenshot = broadcastMode.isAttachScreenshot();
		if (!shouldBroadcastAttempt(finalWave, attemptDto.getRewardsValue(), webhookFormatter != null || attachScreenshot)) return;

		JsonObject payload = generatePayload(attemptDto, webhookFormatter, attachScreenshot, isTest);

		if (payload == null) return;
		log.debug("Submitting {} payload to webhookUrl {}", webhookFormatter, webhookUrl);

		if (attachScreenshot)
		{
			File screenshotFile = getBroadcastScreenshotFile(finalWave, attemptDto.getAttemptId());

			if (webhookFormatter == ColosseumWebhookFormatter.SCREENSHOT && screenshotFile != null && screenshotFile.exists())
			{
				screenshotFile = ColosseumScreenshotDiscordFormatter.createProjectedScreenshotFile(screenshotFile, attemptDto, config);
			}

			discordWebhookService.queueWebhook(webhookUrl, payload, screenshotFile);
		}
		else
		{
			discordWebhookService.sendWebhook(webhookUrl, payload);
		}
	}

	/**
	 * Checks if the given data should be broadcast and if so, formats and sends it to the webhook.
	 */
	public void broadcastToDiscordDebug(ColosseumAttemptDTO attemptDto, ColosseumBroadcastMode broadcastMode)
	{
		List<ColosseumWaveDTO> waves = attemptDto.getWaves();
		if (waves == null || waves.isEmpty())
		{
			log.debug("Did not broadcast trial-- there are no waves...");
			return;
		}

		ColosseumWaveDTO finalWave = waves.get(waves.size() - 1);

		ColosseumWebhookFormatter webhookFormatter = broadcastMode.getFormatter();
		boolean attachScreenshot = broadcastMode.isAttachScreenshot();

		JsonObject payload = generatePayload(attemptDto, webhookFormatter, attachScreenshot, true);

		if (payload == null)
		{
			return;
		}
		log.debug("Submitting debug {} payload to webhookUrl {}", webhookFormatter, webhookUrl);

		if (attachScreenshot)
		{
			File screenshotFile = getBroadcastScreenshotFile(finalWave, attemptDto.getAttemptId());

			if (webhookFormatter == ColosseumWebhookFormatter.SCREENSHOT && screenshotFile != null && screenshotFile.exists())
			{
				screenshotFile = ColosseumScreenshotDiscordFormatter.createProjectedScreenshotFile(screenshotFile, attemptDto, config);
			}

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
	private boolean shouldBroadcastAttempt(ColosseumWaveDTO finalWave, int totalReward, boolean statusConditionMet)
	{
		WaveStatus status = WaveStatus.fromString(finalWave.getStatus());
		boolean hasSucceeded = status != WaveStatus.FAILED && status != WaveStatus.LOGGED_OUT;
		int netReward = status != WaveStatus.COMPLETED ? 0 : totalReward + finalWave.getLootValue();
		boolean result = (netReward > minRewardValue || finalWave.getWave() >= minWave);

		log.debug("CurrentAttempt has {}; waveNumber and rewardsValue conditions have {}been met; {}broadcasting run", hasSucceeded ? "succeeded" : "failed", result ? "" : "not ", statusConditionMet&&result ? "" : "not ");
		return statusConditionMet && result;
	}

//	/**
//	 * Return true if a screenshot is to be attached, given configurations and the final state.
//	 */
//	private boolean shouldBroadcastScreenshot(ColosseumWaveDTO finalWave)
//	{
//		WaveStatus status = WaveStatus.fromString(finalWave.getStatus());
//		switch (status) {
//			case COMPLETED:
//				return broadcastCompletedTrial && broadcastScreenshot.isScreenshotOnSuccess();
//			case FAILED:
//				return broadcastFailedTrial && broadcastScreenshot.isScreenshotOnFailure();
//			case CANCELLED:
//			case CLAIMED:
//				return broadcastCancelledTrial && broadcastScreenshot.isScreenshotOnClaim();
//			default:
//				log.debug("Encountered a non-implemented status of {}", status);
//				return false;
//		}
//	}

	/**
	 * Return the expected screenshotFile, given ColosseumAttemptDTO and its associated root.
	 */
	public File getBroadcastScreenshotFile(ColosseumWaveDTO finalWave, String trialId)
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

		String extension = config.screenshotFormat().getExtension();
		File root = new File(COLOSSEUM_ATTEMPT_DIR, trialId);
		File file = new File(root, String.format("wave-%02d%s.%s", waveNumber, affix, extension));
		log.debug("Identified broadcast screenshot file {}", file.getAbsolutePath());

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
	 * Generate and return an embed with just the footer
	 */
	public static JsonObject generateMinimalEmbed()
	{
		JsonObject embed = new JsonObject();
		addFooter(embed, FOOTER_STANDARD);
		return embed;
	}

	/**
	 * Generate a basic embed with an alternative title and footer that emphasizes that it is a mock submission.
	 */
	public static JsonObject generateTestEmbed(String account)
	{
		JsonObject embed = new JsonObject();
		embed.addProperty("title", "🚧 Colosseum Run by " + account + " (**test**)");
		addFooter(embed, FOOTER_TEST);
		setColor(embed, TEST_COLOR);
		return embed;
	}

	/**
	 * Generate and return an embed with just the footer
	 */
	public static JsonObject generateMinimalTestEmbed()
	{
		JsonObject embed = new JsonObject();
		addFooter(embed, FOOTER_TEST);
		return embed;
	}
}