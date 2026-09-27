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

import static com.activitycounter.PluginConstants.SOUND_EFFECT_OFFSET;
import static com.activitycounter.PluginConstants.SPOT_ANIM_OFFSET;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import lombok.Getter;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.SpotanimID;

/**
 * Class that describes parameters of crossbow bolt effects that may apply
 */
public enum CrossbowBoltEffect
{
	OPAL("Lucky Lightning", new int[]{
		ItemID.XBOWS_CROSSBOW_BOLTS_BRONZE_TIPPED_OPAL_ENCHANTED,
		ItemID.DRAGON_BOLTS_ENCHANTED_OPAL
	}, SpotanimID.XBOWS_LUCKY_LIGHTENING_STRIKE_SPOT_ANIM, 2918, TrackedActivity.OPAL_BOLT_PROC),

	SAPPHIRE("Clear Mind", new int[]{
		ItemID.XBOWS_CROSSBOW_BOLTS_MITHRIL_TIPPED_SAPPHIRE_ENCHANTED,
		ItemID.DRAGON_BOLTS_ENCHANTED_SAPPHIRE
	}, SpotanimID.XBOWS_CLEAR_MIND_GLOWING_SPOT_ANIM, 2912, TrackedActivity.SAPPHIRE_BOLT_PROC),

	JADE("Earth's Fury", new int[]{
		ItemID.XBOWS_CROSSBOW_BOLTS_BLURITE_TIPPED_JADE_ENCHANTED,
		ItemID.DRAGON_BOLTS_ENCHANTED_JADE
	}, SpotanimID.XBOWS_EARTHS_FURY_SPOT_ANIM, 2916, TrackedActivity.JADE_BOLT_PROC),

	PEARL("Sea Curse", new int[]{
		ItemID.XBOWS_CROSSBOW_BOLTS_IRON_TIPPED_PEARL_ENCHANTED,
		ItemID.DRAGON_BOLTS_ENCHANTED_PEARL
	}, SpotanimID.XBOWS_SEA_CURSE_WATERFALL_SPOT_ANIM, 2920, TrackedActivity.PEARL_BOLT_PROC),

	EMERALD("Magical Poison", new int[]{
		ItemID.XBOWS_CROSSBOW_BOLTS_MITHRIL_TIPPED_EMERALD_ENCHANTED,
		ItemID.DRAGON_BOLTS_ENCHANTED_EMERALD
	}, SpotanimID.XBOWS_MAGICAL_POISON_SPOT_ANIM, 2919, TrackedActivity.EMERALD_BOLT_PROC),

	RED_TOPAZ("Down to Earth", new int[]{
		ItemID.XBOWS_CROSSBOW_BOLTS_STEEL_TIPPED_REDTOPAZ_ENCHANTED,
		ItemID.DRAGON_BOLTS_ENCHANTED_TOPAZ
	}, SpotanimID.XBOWS_DOWN_TO_EARTH_SPOT_ANIM, 2914, TrackedActivity.RED_TOPAZ_BOLT_PROC),

	RUBY("Blood Forfeit", new int[]{
		ItemID.XBOWS_CROSSBOW_BOLTS_ADAMANTITE_TIPPED_RUBY_ENCHANTED,
		ItemID.DRAGON_BOLTS_ENCHANTED_RUBY
	}, SpotanimID.XBOWS_BLOOD_SACRIFICE_SPOT_ANIM, 2911, TrackedActivity.RUBY_BOLT_PROC),

	DIAMOND("Armour Piercing", new int[]{
		ItemID.XBOWS_CROSSBOW_BOLTS_ADAMANTITE_TIPPED_DIAMOND_ENCHANTED,
		ItemID.DRAGON_BOLTS_ENCHANTED_DIAMOND
	}, SpotanimID.XBOWS_DIAMOND_TIPS_SPOTANIM, 2913, TrackedActivity.DIAMOND_BOLT_PROC),

	DRAGONSTONE("Dragon's Breath", new int[]{
		ItemID.XBOWS_CROSSBOW_BOLTS_RUNITE_TIPPED_DRAGONSTONE_ENCHANTED,
		ItemID.DRAGON_BOLTS_ENCHANTED_DRAGONSTONE
	}, SpotanimID.XBOWS_DRAGONS_BREATH_SPOT_ANIM, 2915, TrackedActivity.DRAGONSTONE_BOLT_PROC),

	ONYX("Life Leech", new int[]{
		ItemID.XBOWS_CROSSBOW_BOLTS_RUNITE_TIPPED_ONYX_ENCHANTED,
		ItemID.DRAGON_BOLTS_ENCHANTED_ONYX
	}, SpotanimID.XBOWS_LIFE_LEACH_SPOT_ANIM, 2917, TrackedActivity.ONYX_BOLT_PROC);

	@Getter
	final String procName;
	final HashSet<Integer> itemIds;
	@Getter
	final int spotAnimId;
	@Getter
	final int soundEffectId;
	@Getter
	final TrackedActivity trackedActivity;

	private static final Map<Integer, CrossbowBoltEffect> ITEM_ID_MAP = new HashMap<>();

	static {
		for (CrossbowBoltEffect effect : values()) {
			for (int itemId : effect.itemIds) {
				ITEM_ID_MAP.put(itemId, effect);
			}
		}
	}

	CrossbowBoltEffect(String procName, int[] itemIdsArr, int spotAnimId, int soundEffectId, TrackedActivity trackedActivity)
	{
		this.procName = procName;

		this.itemIds = new HashSet<>();
		for (int id : itemIdsArr) {
			this.itemIds.add(id);
		}

		this.spotAnimId = spotAnimId + SPOT_ANIM_OFFSET;
		this.soundEffectId = soundEffectId + SOUND_EFFECT_OFFSET;
		this.trackedActivity = trackedActivity;
	}

	/**
	 * Retrieves the corresponding CrossbowBoltEffect for a given itemId, or null if it doesn't match an enchanted bolt.
	 */
	public static CrossbowBoltEffect getEffectByItemId(int itemId)
	{
		return ITEM_ID_MAP.get(itemId);
	}
}