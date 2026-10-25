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

package com.activitycounter.models;

import java.util.Arrays;
import java.util.EnumSet;
import lombok.Getter;

@Getter
public enum RuneProvider
{
	// --- RUNES (itemId, trackedActivity, pouchId, isInfinite, elements) ---
	AIR_RUNE(556, TrackedActivity.AIR_RUNES_CONSUMED, 1, false, RuneElement.AIR),
	WATER_RUNE(555, TrackedActivity.WATER_RUNES_CONSUMED, 2, false, RuneElement.WATER),
	EARTH_RUNE(557, TrackedActivity.EARTH_RUNES_CONSUMED, 3, false, RuneElement.EARTH),
	FIRE_RUNE(554, TrackedActivity.FIRE_RUNES_CONSUMED, 4, false, RuneElement.FIRE),
	SUNFIRE_RUNE(28929, TrackedActivity.SUNFIRE_RUNES_CONSUMED, 22, false, RuneElement.FIRE),

	MIND_RUNE(558, TrackedActivity.MIND_RUNES_CONSUMED, 5, false, RuneElement.MIND),
	CHAOS_RUNE(562, TrackedActivity.CHAOS_RUNES_CONSUMED, 6, false, RuneElement.CHAOS),
	DEATH_RUNE(560, TrackedActivity.DEATH_RUNES_CONSUMED, 7, false, RuneElement.DEATH),
	BLOOD_RUNE(565, TrackedActivity.BLOOD_RUNES_CONSUMED, 8, false, RuneElement.BLOOD),
	COSMIC_RUNE(564, TrackedActivity.COSMIC_RUNES_CONSUMED, 9, false, RuneElement.COSMIC),
	NATURE_RUNE(561, TrackedActivity.NATURE_RUNES_CONSUMED, 10, false, RuneElement.NATURE),
	LAW_RUNE(563, TrackedActivity.LAW_RUNES_CONSUMED, 11, false, RuneElement.LAW),
	BODY_RUNE(559, TrackedActivity.BODY_RUNES_CONSUMED, 12, false, RuneElement.BODY),
	SOUL_RUNE(566, TrackedActivity.SOUL_RUNES_CONSUMED, 13, false, RuneElement.SOUL),
	ASTRAL_RUNE(9075, TrackedActivity.ASTRAL_RUNES_CONSUMED, 14, false, RuneElement.ASTRAL),
	WRATH_RUNE(21880, TrackedActivity.WRATH_RUNES_CONSUMED, 21, false, RuneElement.WRATH),

	// --- COMBINATION RUNES ---
	MIST_RUNE(4695, TrackedActivity.MIST_RUNES_CONSUMED, 15, false, RuneElement.AIR, RuneElement.WATER),
	MUD_RUNE(4698, TrackedActivity.MUD_RUNES_CONSUMED, 16, false, RuneElement.WATER, RuneElement.EARTH),
	DUST_RUNE(4696, TrackedActivity.DUST_RUNES_CONSUMED, 17, false, RuneElement.AIR, RuneElement.EARTH),
	LAVA_RUNE(4699, TrackedActivity.LAVA_RUNES_CONSUMED, 18, false, RuneElement.EARTH, RuneElement.FIRE),
	STEAM_RUNE(4694, TrackedActivity.STEAM_RUNES_CONSUMED, 19, false, RuneElement.WATER, RuneElement.FIRE),
	SMOKE_RUNE(4697, TrackedActivity.SMOKE_RUNES_CONSUMED, 20, false, RuneElement.AIR, RuneElement.FIRE),
	AETHER_RUNE(30843, TrackedActivity.AETHER_RUNES_CONSUMED, 23, false, RuneElement.COSMIC, RuneElement.SOUL),

	// --- Blighted sacks (Consumable Proxies, PouchId = -1) ---
	BLIGHTED_ICE_SACK(24607, TrackedActivity.ICE_SACKS_CONSUMED, -1, false, RuneElement.ICE_SACK),
	BLIGHTED_ENTANGLE_SACK(24613, TrackedActivity.ENTANGLE_SACKS_CONSUMED, -1, false, RuneElement.ENTANGLE_SACK),
	BLIGHTED_TELEPORT_SPELL_SACK(24615, TrackedActivity.TELEPORT_SACKS_CONSUMED, -1, false, RuneElement.TELEPORT_SPELL_SACK),
	BLIGHTED_VENGEANCE_SACK(24621, TrackedActivity.VENGEANCE_SACKS_CONSUMED, -1, false, RuneElement.VENGEANCE_SACK),
	BLIGHTED_SURGE_SACK(26705, TrackedActivity.SURGE_SACKS_CONSUMED, -1, false, RuneElement.SURGE_SACK),


	// --- TOMES (Infinite Sources worn in the off-hand) ---
	TOME_OF_FIRE(20714, null, -1, true, RuneElement.FIRE),
	TOME_OF_WATER(25289, null, -1, true, RuneElement.WATER),
	TOME_OF_EARTH(30064, null, -1, true, RuneElement.EARTH),

	// --- BASIC STAVES (Infinite Sources worn in main hand ---
	STAFF_OF_AIR(1381, null, -1, true, RuneElement.AIR),
	STAFF_OF_WATER(1383, null, -1, true, RuneElement.WATER),
	STAFF_OF_EARTH(1385, null, -1, true, RuneElement.EARTH),
	STAFF_OF_FIRE(1387, null, -1, true, RuneElement.FIRE),

	// --- BATTLESTAVES (Infinite Sources worn in main hand) ---
	AIR_BATTLESTAFF(1397, null, -1, true, RuneElement.AIR),
	WATER_BATTLESTAFF(1395, null, -1, true, RuneElement.WATER),
	EARTH_BATTLESTAFF(1399, null, -1, true, RuneElement.EARTH),
	FIRE_BATTLESTAFF(1393, null, -1, true, RuneElement.FIRE),
	LAVA_BATTLESTAFF(3053, null, -1, true, RuneElement.EARTH, RuneElement.FIRE),
	MUD_BATTLESTAFF(6562, null, -1, true, RuneElement.WATER, RuneElement.EARTH),
	STEAM_BATTLESTAFF(11787, null, -1, true, RuneElement.WATER, RuneElement.FIRE),
	SMOKE_BATTLESTAFF(11998, null, -1, true, RuneElement.AIR, RuneElement.FIRE),
	MIST_BATTLESTAFF(20730, null, -1, true, RuneElement.AIR, RuneElement.WATER),
	DUST_BATTLESTAFF(20736, null, -1, true, RuneElement.AIR, RuneElement.EARTH),

	// --- MYSTIC STAVES (Infinite Sources worn in main hand) ---
	MYSTIC_AIR_STAFF(1405, null, -1, true, RuneElement.AIR),
	MYSTIC_WATER_STAFF(1403, null, -1, true, RuneElement.WATER),
	MYSTIC_EARTH_STAFF(1407, null, -1, true, RuneElement.EARTH),
	MYSTIC_FIRE_STAFF(1401, null, -1, true, RuneElement.FIRE),
	MYSTIC_LAVA_STAFF(3054, null, -1, true, RuneElement.EARTH, RuneElement.FIRE),
	MYSTIC_MUD_STAFF(6563, null, -1, true, RuneElement.WATER, RuneElement.EARTH),
	MYSTIC_STEAM_STAFF(11789, null, -1, true, RuneElement.WATER, RuneElement.FIRE),
	MYSTIC_SMOKE_STAFF(12000, null, -1, true, RuneElement.AIR, RuneElement.FIRE),
	MYSTIC_MIST_STAFF(20733, null, -1, true, RuneElement.AIR, RuneElement.WATER),
	MYSTIC_DUST_STAFF(20739, null, -1, true, RuneElement.AIR, RuneElement.EARTH),

	// --- ORNAMENT KIT VARIANTS (Infinite Sources worn in main hand) ---
	LAVA_BATTLESTAFF_OR(11993, null, -1, true, RuneElement.EARTH, RuneElement.FIRE),
	MYSTIC_LAVA_STAFF_OR(11994, null, -1, true, RuneElement.EARTH, RuneElement.FIRE),
	STEAM_BATTLESTAFF_OR(12795, null, -1, true, RuneElement.WATER, RuneElement.FIRE),
	MYSTIC_STEAM_STAFF_OR(12796, null, -1, true, RuneElement.WATER, RuneElement.FIRE),

	// --- Other (Infinite sources worn in main hand) ---
	KODAI_WAND(21006, null, -1, true, RuneElement.WATER),
	TWINFLAME_STAFF(30634, null, -1, true, RuneElement.WATER, RuneElement.FIRE);


	private final int itemId;
	private final String itemName;
	private final TrackedActivity trackedActivity;
	private final int runePouchId;
	private final boolean isInfinite;
	private final EnumSet<RuneElement> providedElements;

	private static final RuneProvider[] ITEM_ID_MAPPING = new RuneProvider[35000];
	private static final RuneProvider[] POUCH_ID_MAPPING = new RuneProvider[30];

	static
	{
		for (RuneProvider provider : values())
		{
			ITEM_ID_MAPPING[provider.getItemId()] = provider;

			if (provider.getRunePouchId() != -1)
			{
				POUCH_ID_MAPPING[provider.getRunePouchId()] = provider;
			}
		}
	}

	RuneProvider(int itemId, TrackedActivity trackedActivity, int runePouchId, boolean isInfinite, RuneElement... elements)
	{
		this.itemId = itemId;
		this.trackedActivity = trackedActivity;
		this.runePouchId = runePouchId;
		this.isInfinite = isInfinite;
		this.providedElements = EnumSet.noneOf(RuneElement.class);
		this.providedElements.addAll(Arrays.asList(elements));
		this.itemName = name().charAt(0) + name().substring(1).toLowerCase().replace('_', ' ');
	}

	public static RuneProvider getByItemId(int itemId)
	{
		if (itemId < 0 || itemId >= ITEM_ID_MAPPING.length)
		{
			return null;
		}
		return ITEM_ID_MAPPING[itemId];
	}

	public static RuneProvider getByPouchId(int pouchId)
	{
		if (pouchId < 0 || pouchId >= POUCH_ID_MAPPING.length)
		{
			return null;
		}
		return POUCH_ID_MAPPING[pouchId];
	}
}