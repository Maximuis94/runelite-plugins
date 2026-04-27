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
import com.datalogger.dto.ColosseumAttemptDTO;
import com.datalogger.dto.ColosseumWaveDTO;
import com.datalogger.models.enums.WaveStatus;
import static com.datalogger.webhook.ColosseumDiscordBroadcaster.generateBaseEmbed;
import static com.datalogger.webhook.DiscordWebhookUtils.addField;
import static com.datalogger.webhook.DiscordWebhookUtils.wrapEmbedIntoPayload;
import static com.datalogger.webhook.WebhookFormatUtils.formatActiveModifiers;
import static com.datalogger.webhook.WebhookFormatUtils.formatColosseumWave;
import static com.datalogger.webhook.WebhookFormatUtils.formatSeconds;
import static com.datalogger.webhook.WebhookFormatUtils.getWavesCompleted;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Static utility class for formatting Colosseum data into Discord JSON payloads.
 */
public final class ColosseumDetailedDiscordFormatter
{

	private ColosseumDetailedDiscordFormatter() {}

	/**
	 * Builds a detailed Discord payload containing a wave-by-wave breakdown.
	 */
	@Nonnull
	public static JsonObject buildPayload(@Nonnull ColosseumAttemptDTO dto, @Nonnull DataLoggerConfig config) {
		JsonObject embed = generateBaseEmbed(dto.getAccount());
//		StringBuilder description = new StringBuilder();
//
//		List<String> topStats = new ArrayList<>();
//		if (config.includeStatus()) {
//			topStats.add(String.format("**Status:** %s | %s/12 completed",
//				dto.getResult().toLowerCase(),
//				getWavesCompleted(dto.getWaves())));
//		}
//		if (config.includeTime()) {
//			topStats.add(String.format("**Time:** %s", formatSeconds(dto.getTotalTime())));
//		}
//		if (config.includeGlory()) {
//			topStats.add(String.format("**Glory:** %,d", dto.getTotalGlory()));
//		}
//
//		if (!topStats.isEmpty()) {
//			description.append(String.join(" | ", topStats)).append("\n");
//		}
//
//		if (config.includeModifiers()) {
//			String activeModifiers = formatActiveModifiers(dto.getActiveModifiers(), true);
//			description.append(String.format("**Modifiers**: [ %s ]\n", activeModifiers));
//		}
//
//		List<String> econStats = new ArrayList<>();
//		if (config.includeRewardValue()) {
//			econStats.add(String.format("**Reward:** %,d gp", dto.getRewardsValue()));
//		}
//		if (config.includeSupplyValue()) {
//			econStats.add(String.format("**Supplies:** %,d gp", dto.getConsumedSupplyValue()));
//		}
//
//		if (!econStats.isEmpty()) {
//			description.append(String.join(" | ", econStats)).append("\n");
//		}

		embed.addProperty("description", buildDescription(dto, config));

		JsonArray fields = new JsonArray();
		if (dto.getWaves() != null) {
			int nWaves = dto.getWaves().size();

			for (ColosseumWaveDTO wave : dto.getWaves()) {
				WaveStatus status = wave.getWave() < nWaves ? WaveStatus.COMPLETED : WaveStatus.fromString(wave.getStatus());

				String statusString = (status == WaveStatus.CLAIMED || status == WaveStatus.CANCELLED)
					? "cancelled"
					: status.getEmoji();

				StringBuilder fieldName = new StringBuilder("Wave " + wave.getWave() + ": " + statusString);

				if (config.includeTime() && !statusString.equals("cancelled")) {
					fieldName.append(" | ").append(formatSeconds(wave.getTimeTaken()));
				}

				String waveSummary = formatColosseumWave(wave, config);

				addField(fields, fieldName.toString(), waveSummary, true);
			}
		}
		embed.add("fields", fields);
		return wrapEmbedIntoPayload(embed);
	}

	public static String buildDescription(@Nonnull ColosseumAttemptDTO dto, @Nonnull DataLoggerConfig config)
	{
		StringBuilder description = new StringBuilder();

		List<String> topStats = new ArrayList<>();
		if (config.includeStatus()) {
			topStats.add(String.format("**Status:** %s | %s/12 completed",
				dto.getResult().toLowerCase(),
				getWavesCompleted(dto.getWaves())));
		}
		if (config.includeTime()) {
			topStats.add(String.format("**Time:** %s", formatSeconds(dto.getTotalTime())));
		}
		if (config.includeGlory()) {
			topStats.add(String.format("**Glory:** %,d", dto.getTotalGlory()));
		}

		if (!topStats.isEmpty()) {
			description.append(String.join(" | ", topStats)).append("\n");
		}

		if (config.includeModifiers()) {
			String activeModifiers = formatActiveModifiers(dto.getActiveModifiers(), true);
			description.append(String.format("**Modifiers**: [ %s ]\n", activeModifiers));
		}

		List<String> econStats = new ArrayList<>();
		if (config.includeRewardValue()) {
			econStats.add(String.format("**Reward:** %,d gp", dto.getRewardsValue()));
		}
		if (config.includeSupplyValue()) {
			econStats.add(String.format("**Supplies:** %,d gp", dto.getConsumedSupplyValue()));
		}

		if (!econStats.isEmpty()) {
			description.append(String.join(" | ", econStats)).append("\n");
		}
		return description.toString();
	}
}