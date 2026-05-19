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
import static com.datalogger.webhook.ColosseumDiscordBroadcaster.generateBaseEmbed;
import static com.datalogger.webhook.ColosseumDiscordBroadcaster.generateTestEmbed;
import static com.datalogger.webhook.DiscordWebhookUtils.setColor;
import static com.datalogger.webhook.DiscordWebhookUtils.wrapEmbedIntoPayload;
import static com.datalogger.webhook.WebhookFormatUtils.formatActiveModifiers;
import static com.datalogger.webhook.WebhookFormatUtils.formatSeconds;
import static com.datalogger.webhook.WebhookFormatUtils.getWavesCompleted;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Static utility class for formatting Colosseum data into Discord JSON payloads.
 */
public final class ColosseumConciseDiscordFormatter {

	private ColosseumConciseDiscordFormatter() {}

	/**
	 * Builds a concise Discord payload containing only the final summary statistics.
	 */
	public static JsonObject buildPayload(ColosseumAttemptDTO dto, DataLoggerConfig config, boolean isTest) {
		JsonObject embed = isTest ? generateTestEmbed(dto.getAccount()) : generateBaseEmbed(dto.getAccount());

		setColor(embed, "COMPLETED".equals(dto.getResult()) ? 0x00FF00 : 0xFF0000);

		List<String> descriptionLines = new ArrayList<>();

		List<String> line1 = new ArrayList<>();
		line1.add("**Waves completed:** " + getWavesCompleted(dto.getWaves()));

		if (config.includeStatus()) {
			line1.add("**Status**: " + dto.getResult().toLowerCase());
		}
		descriptionLines.add(String.join(" ", line1));

		List<String> line2 = new ArrayList<>();
		if (config.includeTime()) {
			line2.add("**Time taken:** " + formatSeconds(dto.getTotalTime()));
		}
		if (config.includeGlory()) {
			line2.add(String.format("**Total Glory:** %,d", dto.getTotalGlory()));
		}
		if (!line2.isEmpty()) {
			descriptionLines.add(String.join(" ", line2));
		}

		List<String> line3 = new ArrayList<>();
		if (config.includeRewardValue()) {
			line3.add(String.format("**Reward value:** %,d gp", dto.getRewardsValue()));
		}
		if (config.includeSupplyValue()) {
			line3.add(String.format("**Supply Cost:** %,d gp", dto.getConsumedSupplyValue()));
		}
		if (!line3.isEmpty()) {
			descriptionLines.add(String.join(" ", line3));
		}

		if (config.includeModifiers()) {
			String activeModifiers = formatActiveModifiers(dto.getActiveModifiers(), true);
			descriptionLines.add("**Modifiers**: " + activeModifiers);
		}

		embed.addProperty("description", String.join("\n", descriptionLines));

		return wrapEmbedIntoPayload(embed);
	}
}