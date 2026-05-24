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

import com.datalogger.dto.ColosseumAttemptDTO;
import com.datalogger.dto.ColosseumWaveDTO;
import com.datalogger.models.enums.ColosseumModifier;
import com.datalogger.models.itemvault.ItemBundle;
import static com.datalogger.webhook.ColosseumDiscordBroadcaster.generateBaseEmbed;
import static com.datalogger.webhook.ColosseumDiscordBroadcaster.generateTestEmbed;
import static com.datalogger.webhook.DiscordWebhookUtils.wrapEmbedIntoPayload;
import static com.datalogger.webhook.WebhookFormatUtils.formatActiveModifiers;
import static com.datalogger.webhook.WebhookFormatUtils.formatModifierChoices;
import static com.datalogger.webhook.WebhookFormatUtils.formatSeconds;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Static class that handles the generation of Discord Webhook payloads based on user-defined string templates.
 */
public final class ColosseumCustomDiscordFormatter
{
	private ColosseumCustomDiscordFormatter() {}

	private static final Pattern WAVE_PROPERTY_PATTERN = Pattern.compile("<(\\d+):([A-Z_]+)>");

	/**
	 * Builds a Discord payload where the embed description is parsed from a user template.
	 * @param dto      The attempt data.
	 * @param template The raw string from the user config (e.g., "Player <PLAYER> got <GLORY>!")
	 * @return A JsonObject formatted for Discord.
	 */
	public static JsonObject buildPayload(ColosseumAttemptDTO dto, String template, boolean isTest)
	{
		JsonObject embed = isTest ? generateTestEmbed(dto.getAccountName()) : generateBaseEmbed(dto.getAccountName());
		int color = "COMPLETED".equals(dto.getResult()) ? 0x00FF00 : 0xFF0000;

		embed.addProperty("color", color);
		embed.addProperty("description", resolveTemplate(template, dto));
		return wrapEmbedIntoPayload(embed);
	}

	/**
	 * Builds a Discord payload where the embed description is parsed from a user template. Several properties indicate
	 * that this is a mock payload used for testing
	 * @param dto      The attempt data.
	 * @param template The raw string from the user config (e.g., "Player <PLAYER> got <GLORY>!")
	 * @return A JsonObject formatted for Discord.
	 */
	public static JsonObject buildTestPayload(ColosseumAttemptDTO dto, String template)
	{
		JsonObject embed = generateTestEmbed(dto.getAccountName());
		embed.addProperty("description", resolveTemplate(template, dto));
		return wrapEmbedIntoPayload(embed);
	}

	/**
	 * Replaces all supported keywords within a template string with actual DTO values.
	 */
	@Nonnull
	private static String resolveTemplate(String template, ColosseumAttemptDTO dto)
	{
		if (template == null || template.isEmpty())
		{
			return "No template defined in configuration.";
		}

		String cleanTemplate = Arrays.stream(template.split("\n"))
			.filter(line -> !line.trim().startsWith("#"))
			.collect(Collectors.joining("\n"))
			.trim();

		List<ColosseumWaveDTO> waves = dto.getWaves();
		ColosseumWaveDTO lastWave = waves.isEmpty() ? null : waves.get(waves.size() - 1);

		// Chain replacements for all supported tokens
		String resolved = cleanTemplate
			.replace("<PLAYER>", dto.getAccountName())
			.replace("<RESULT>", dto.getResult())
			.replace("<WAVES>", String.valueOf(dto.getWaves().size()))
			.replace("<TIME>", formatSeconds(lastWave != null ? lastWave.getTotalTimeTaken() : 0))
			.replace("<SECONDS>", String.format("%.1f", dto.getTotalTime()))
			.replace("<GLORY>", String.format("%,d", dto.getTotalGlory()))
			.replace("<VALUE>", String.format("%,d", dto.getRewardsValue()))
			.replace("<COST>", String.format("%,d", dto.getConsumedSupplyValue()))
			.replace("<MODS>", formatActiveModifiers(dto.getActiveModifiers(), true))
			.replace("<MODIFIERS>", formatActiveModifiers(dto.getActiveModifiers(), false));

		Matcher matcher = WAVE_PROPERTY_PATTERN.matcher(resolved);
		StringBuilder sb = new StringBuilder();
		int lastEnd = 0;

		while (matcher.find())
		{
			sb.append(resolved, lastEnd, matcher.start());

			int waveNum = Integer.parseInt(matcher.group(1));
			String property = matcher.group(2);

			sb.append(getWaveProperty(dto, waveNum, property));

			lastEnd = matcher.end();
		}
		sb.append(resolved.substring(lastEnd));

		return sb.toString();
	}

	private static String getWaveProperty(ColosseumAttemptDTO dto, int waveNum, String property)
	{
		List<ColosseumWaveDTO> waves = dto.getWaves();
		int index = waveNum - 1;

		if (index < 0 || index >= waves.size())
		{
			return "N/A";
		}

		ColosseumWaveDTO wave = waves.get(index);
		String mod;
		ItemBundle loot;
		List<String> modChoices;
		switch (property)
		{
			case "LOOT":
				loot = wave.getEarnedLoot();
				return String.format("%dx %s", loot.getQuantity(), loot.getItemName());
			case "LOOTNAME":
				loot = wave.getEarnedLoot();
				return String.valueOf(loot.getItemName());
			case "LOOTAMOUNT":
				loot = wave.getEarnedLoot();
				return String.valueOf(loot.getQuantity());
			case "MODIFIER":
			case "CHOSENMODIFIER":
				mod = wave.getChosenModifier();
				return mod != null ? mod : "None";
			case "MOD":
			case "CHOSENMOD":
				mod = wave.getChosenModifier();
				return ColosseumModifier.getAliasFromName(mod);
			case "MODIFIERCHOICES":
				modChoices = wave.getModifierChoices();
				if (modChoices == null || modChoices.isEmpty())
				{
					return "None";
				}
				return String.join(" ", modChoices);
			case "MODCHOICES":
				modChoices = wave.getModifierChoices();
				if (modChoices == null || modChoices.isEmpty())
				{
					return "None";
				}
				return modChoices.stream()
					.map(ColosseumModifier::getAliasFromName)
					.collect(Collectors.joining(" "));
			case "SELECTEDMODIFIERCHOICES":
				return formatModifierChoices(wave, false);
			case "SELECTEDMODCHOICES":
				return formatModifierChoices(wave, true);
			case "DAMAGETAKEN":
				return String.valueOf(wave.getDamageTaken());
			case "GLORY":
				return String.format("%,d", wave.getWaveGlory());
			case "TOTALGLORY":
				return String.format("%,d", wave.getTotalGlory());
			case "TIME":
				return formatSeconds(wave.getTimeTaken());
			case "SECONDS":
				return String.format("%.1f", wave.getTimeTaken());
			case "STATUS":
				return wave.getStatus();
			default:
				return "Unknown Property";
		}
	}
}