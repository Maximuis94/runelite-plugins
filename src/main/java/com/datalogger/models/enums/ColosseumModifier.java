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
	BEES_I(1, "BE1", "Bees!"),
	BEES_II(1, "BE2", "Bees! (II)"),
	BEES_III(1, "BE3", "Bees! (III)"),

	BLASPHEMY_I(2, "BL1", "Blasphemy"),
	BLASPHEMY_II(2, "BL2", "Blasphemy (II)"),
	BLASPHEMY_III(2, "BL3", "Blasphemy (III)"),

	DOOM_I(3, "DM1", "Doom"),
	DOOM_II(3, "DM2", "Doom (II)"),
	DOOM_III(3, "DM3", "Doom (III)"),

	DYNAMIC_DUO(4, "DUO", "Dynamic Duo"),

	FRAILTY_I(5, "FR1", "Frailty"),
	FRAILTY_II(5, "FR2", "Frailty (II)"),
	FRAILTY_III(5, "FR3", "Frailty (III)"),

	MANTIMAYHEM_I(6, "MM1", "Mantimayhem"),
	MANTIMAYHEM_II(6, "MM2", "Mantimayhem (II)"),
	MANTIMAYHEM_III(6, "MM3", "Mantimayhem (III)"),

	MYOPIA_I(7, "MY1", "Myopia"),
	MYOPIA_II(7, "MY2", "Myopia (II)"),
	MYOPIA_III(7, "MY3", "Myopia (III)"),

	QUARTET(8, "QRT", "Quartet"),

	RED_FLAG(9, "RFL", "Red Flag"),

	REENTRY_I(10, "RN1", "Reentry"),
	REENTRY_II(10, "RN2", "Reentry (II)"),
	REENTRY_III(10, "RN3", "Reentry (III)"),

	RELENTLESS_I(11, "RL1", "Relentless"),
	RELENTLESS_II(11, "RL2", "Relentless (II)"),
	RELENTLESS_III(11, "RL3", "Relentless (III)"),

	SOLARFLARE_I(12, "SF1", "Solarflare"),
	SOLARFLARE_II(12, "SF2", "Solarflare (II)"),
	SOLARFLARE_III(12, "SF3", "Solarflare (III)"),

	TOTEMIC(13, "TOT", "Totemic"),

	VOLATILITY_I(14, "VO1", "Volatility"),
	VOLATILITY_II(14, "VO2", "Volatility (II)"),
	VOLATILITY_III(14, "VO3", "Volatility (III)");

	private final int id;

	@NonNull
	private final String alias;

	@NonNull
	private final String uiLabel;

	ColosseumModifier(int id, @NonNull String alias, @NonNull String uiLabel) {
		this.id = id;
		this.alias = alias;
		this.uiLabel = uiLabel;
	}

	private static final ImmutableMap<String, ColosseumModifier> NAME_LOOKUP;
	private static final ImmutableMap<String, ColosseumModifier> UI_LABEL_LOOKUP;

	static {
		ImmutableMap.Builder<String, ColosseumModifier> nameBuilder = ImmutableMap.builder();
		ImmutableMap.Builder<String, ColosseumModifier> uiLabelBuilder = ImmutableMap.builder();

		for (ColosseumModifier mod : values()) {
			nameBuilder.put(mod.name().toUpperCase(), mod);
			uiLabelBuilder.put(mod.getUiLabel(), mod);
		}

		NAME_LOOKUP = nameBuilder.build();
		UI_LABEL_LOOKUP = uiLabelBuilder.build();
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
}