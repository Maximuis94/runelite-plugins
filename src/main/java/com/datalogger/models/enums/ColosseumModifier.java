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

package com.datalogger.models.enums;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.NonNull;

/**
 * Enum of modifiers that may be selected throughout a Colosseum trial. Multi-tier modifiers share their id property.
 */
@Getter
public enum ColosseumModifier {
	BEES_I(1, "BE1", "Bees!", 5544, 150),
	BEES_II(1, "BE2", "Bees! (II)", 5559, 300),
	BEES_III(1, "BE3", "Bees! (III)", 5574, 450),

	BLASPHEMY_I(2, "BL1", "Blasphemy", 5538, 100),
	BLASPHEMY_II(2, "BL2", "Blasphemy (II)", 5553, 200),
	BLASPHEMY_III(2, "BL3", "Blasphemy (III)", 5568, 300),

	DOOM_I(3, "DM1", "Doom", 5543, 200),
	DOOM_II(3, "DM2", "Doom (II)", 5558, 400),
	DOOM_III(3, "DM3", "Doom (III)", 5573, 600),

	DYNAMIC_DUO(4, "DUO", "Dynamic Duo", 5545, 150),
	DYNAMIC_DUO_I(4, "DUO", "Dynamic Duo", 5545, 150),

	FRAILTY_I(5, "FR1", "Frailty", 5541, 200),
	FRAILTY_II(5, "FR2", "Frailty (II)", 5556, 400),
	FRAILTY_III(5, "FR3", "Frailty (III)", 5571, 600),

	MANTIMAYHEM_I(6, "MM1", "Mantimayhem", 5539, 150),
	MANTIMAYHEM_II(6, "MM2", "Mantimayhem (II)", 5554, 300),
	MANTIMAYHEM_III(6, "MM3", "Mantimayhem (III)", 5569, 450),

	MYOPIA_I(7, "MY1", "Myopia", 5547, 200),
	MYOPIA_II(7, "MY2", "Myopia (II)", 5562, 400),
	MYOPIA_III(7, "MY3", "Myopia (III)", 5577, 600),

	QUARTET(8, "QRT", "Quartet", 5546, 100),
	QUARTET_I(8, "QRT", "Quartet", 5546, 100),

	RED_FLAG(9, "RFL", "Red Flag", 5540, 250),
	RED_FLAG_I(9, "RFL", "Red Flag", 5540, 250),

	REENTRY_I(10, "RN1", "Reentry", 5536, 150),
	REENTRY_II(10, "RN2", "Reentry (II)", 5551, 300),
	REENTRY_III(10, "RN3", "Reentry (III)", 5566, 450),

	RELENTLESS_I(11, "RL1", "Relentless", 5535, 200),
	RELENTLESS_II(11, "RL2", "Relentless (II)", 5550, 400),
	RELENTLESS_III(11, "RL3", "Relentless (III)", 5565, 600),

	SOLARFLARE_I(12, "SF1", "Solarflare", 5537, 250),
	SOLARFLARE_II(12, "SF2", "Solarflare (II)", 5552, 500),
	SOLARFLARE_III(12, "SF3", "Solarflare (III)", 5567, 750),

	TOTEMIC(13, "TOT", "Totemic", 5542, 200),
	TOTEMIC_I(13, "TOT", "Totemic", 5542, 200),

	VOLATILITY_I(14, "VO1", "Volatility", 5534, 100),
	VOLATILITY_II(14, "VO2", "Volatility (II)", 5549, 200),
	VOLATILITY_III(14, "VO3", "Volatility (III)", 5564, 300);

	private final int id;

	@NonNull
	private final String alias;

	@NonNull
	private final String uiLabel;

	private final int spriteId;

	private final int glory;

	ColosseumModifier(int id, @NonNull String alias, @NonNull String uiLabel, int spriteId, int glory) {
		this.id = id;
		this.alias = alias;
		this.uiLabel = uiLabel;
		this.spriteId = spriteId;
		this.glory = glory;
	}

	private static final ImmutableMap<String, ColosseumModifier> NAME_LOOKUP;
	private static final ImmutableMap<String, ColosseumModifier> UI_LABEL_LOOKUP;

	static {
		ImmutableMap.Builder<String, ColosseumModifier> nameBuilder = ImmutableMap.builder();
		java.util.Map<String, ColosseumModifier> tempUiLabelMap = new java.util.HashMap<>();

		for (ColosseumModifier mod : values()) {
			nameBuilder.put(mod.name().toUpperCase(), mod);

			tempUiLabelMap.putIfAbsent(mod.getUiLabel(), mod);
		}

		NAME_LOOKUP = nameBuilder.build();
		UI_LABEL_LOOKUP = ImmutableMap.copyOf(tempUiLabelMap);
	}

	/**
	 * Return the alias that corresponds to the given name.
	 * @return Alias corresponding to name. If there is no match, return name; If name is null/empty, return "None"
	 */
	@NonNull
	public static String getAliasFromName(String name) {
		if (name == null || name.isEmpty()) {
			return "None";
		}
		ColosseumModifier mod = NAME_LOOKUP.get(name.trim().toUpperCase());
		return mod != null ? mod.getAlias() : name;
	}

	/**
	 * Return the ColosseumModifier that corresponds to the in-game UI description.
	 * @param uiText The exact text scraped from the Colosseum widget.
	 * @return The matching ColosseumModifier, or null if no match is found.
	 */
	public static ColosseumModifier fromUiLabel(String uiText) {
		if (uiText == null || uiText.isEmpty()) {
			return null;
		}
		return UI_LABEL_LOOKUP.get(uiText.trim());
	}

	/**
	 * Safe lookup method that converts the given name to a ColosseumModifier, if it exists.
	 */
	public static ColosseumModifier fromName(String name)
	{
		try
		{
			return ColosseumModifier.valueOf(name.toUpperCase());
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	/**
	 * Calculates the total baseline glory value from a pipe-delimited string of modifier names.
	 */
	public static int calculateTotalGlory(String activeModifiersStr)
	{
		if (activeModifiersStr == null || activeModifiersStr.trim().isEmpty())
		{
			return 0;
		}

		int totalGlory = 0;

		String[] tokens = activeModifiersStr.split("\\|");

		for (String token : tokens)
		{
			String cleanedToken = token.trim().toUpperCase();
			if (cleanedToken.isEmpty()) continue;

			ColosseumModifier mod = NAME_LOOKUP.get(cleanedToken);
			if (mod != null)
			{
				totalGlory += mod.getGlory();
			}
		}

		return totalGlory;
	}

	/**
	 * Return the modifier name used in the UI without any tier
	 */
	public static String getBaseModifierName(String modName) {
		if (modName == null) return null;

		ColosseumModifier mod;
		if (modName.endsWith("_III")) mod = fromName(modName);
		else if (modName.endsWith("_II")) mod = fromName(modName);
		else if (modName.endsWith("_I")) mod = fromName(modName);
		else if (modName.endsWith("_0")) mod = fromName(modName.replace("0","I"));
		else return null;

		return mod != null ? mod.getUiLabel().split(" ")[0] : null;
	}
}