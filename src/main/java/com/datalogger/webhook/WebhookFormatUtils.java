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
import com.datalogger.dto.ColosseumWaveDTO;
import com.datalogger.models.enums.ColosseumModifier;
import com.datalogger.models.enums.WaveStatus;
import com.datalogger.models.itemvault.ItemBundle;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class WebhookFormatUtils
{
	private WebhookFormatUtils() {}

	/**
	 * Formats the final list of active modifiers into a single spaced string.
	 */
	public static String formatActiveModifiers(List<String> activeModifiers, boolean useAlias) {
		if (activeModifiers == null || activeModifiers.isEmpty()) {
			return "None";
		}

		return activeModifiers.stream()
			.map(modName -> {
				if (useAlias)
				{
					return ColosseumModifier.getAliasFromName(modName);
				}
				else
					return modName;
			})
			.collect(Collectors.joining(" "));
	}

	/**
	 * Return the number of waves completed as a String based on the given List of waves
	 */
	public static String getWavesCompleted(List<ColosseumWaveDTO> waves)
	{
		if (waves == null || waves.isEmpty()) return "";

		int nWaves = waves.size();
		ColosseumWaveDTO wave = waves.get(nWaves - 1);

		return String.valueOf(nWaves - (wave.getStatus().equals(WaveStatus.COMPLETED.name()) ? 0 : 1));
	}

	public static String damageBonusString(int damageBonus, int damageTaken)
	{
		return damageBonus > 0 ? String.format("%,d (0)", damageBonus) : String.format("0 (%d)", damageTaken);
	}

	/**
	 * Converts seconds into a formatted MM:SS.S string.
	 */
	public static String formatSeconds(double timeTaken) {
		int minutes = (int) (timeTaken / 60);
		double seconds = timeTaken % 60;
		return String.format("%02d:%04.1f", minutes, seconds);
	}

	/**
	 * Combines the modifier choices and the chosen modifier into a single string.
	 */
	public static String formatModifierChoices(ColosseumWaveDTO wave, boolean useAlias) {
		List<String> modifierChoices = wave.getModifierChoices();
		String chosenModifier = wave.getChosenModifier();

		if (modifierChoices == null || modifierChoices.isEmpty()) {
			return "";
		}

		return modifierChoices.stream()
			.map(modName -> {

				String displayName;
				if (useAlias)
				{
					displayName = ColosseumModifier.getAliasFromName(modName);
				}
				else
					displayName = modName;

				if (modName.equals(chosenModifier)) {
					return "[" + displayName + "]";
				} else {
					return displayName;
				}
			})
			.collect(Collectors.joining(" "));
	}

	/**
	 * Formats an individual wave's statistics for the detailed view.
	 */
	public static String formatColosseumWave(ColosseumWaveDTO dto, DataLoggerConfig config) {
		List<String> lines = new ArrayList<>();

		// 1. Reward
		if (config.includeReward()) {
			ItemBundle loot = dto.getEarnedLoot();
			String lootString = loot != null ? String.format("%dx %s", loot.getQuantity(), loot.getItemName()) : "None";
			lines.add(lootString);
		}

		if (config.includeModifiers()) {
			String modString = formatModifierChoices(dto, true);
			lines.add("Mod: " + modString);
		}

		if (config.includeGlory()) {
			lines.add(String.format("Glory: %,d / %,d", dto.getWaveGlory(), dto.getTotalGlory()));
			lines.add(String.format("C:%,d D:%s", dto.getCompletionBonus(), damageBonusString(dto.getDamageBonus(), dto.getDamageTaken())));
			lines.add(String.format("S:%,d M:%,d", dto.getSpeedBonus(), dto.getModifierGlory()));
		}

		return String.join("\n", lines);
	}

	/**
	 * Formats an individual wave's statistics for the detailed view.
	 */
	public static String formatColosseumWaveLine(ColosseumWaveDTO dto, DataLoggerConfig config) {
		List<String> segments = new ArrayList<>();

		if (config.includeReward()) {
			ItemBundle loot = dto.getEarnedLoot();
			String lootString = loot != null ? String.format("%dx %s", loot.getQuantity(), loot.getItemName()) : "None";
			segments.add(lootString);
		}

		if (config.includeModifiers()) {
			String modString = formatModifierChoices(dto, true);
			segments.add("Mod: " + modString);
		}

		if (config.includeGlory()) {
			String gloryBase = String.format("Glory: %,d / %,d", dto.getWaveGlory(), dto.getTotalGlory());
			String gloryDetails = String.format(" - C:%,d D:%s S:%,d M:%,d",
				dto.getCompletionBonus(),
				damageBonusString(dto.getDamageBonus(), dto.getDamageTaken()),
				dto.getSpeedBonus(),
				dto.getModifierGlory()
			);
			segments.add(gloryBase + " " + gloryDetails);
		}

		return String.join(" | ", segments);
	}
}
