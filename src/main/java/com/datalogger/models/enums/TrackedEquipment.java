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

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import static net.runelite.api.gameval.AnimationID.HUMAN_CASTWAVE_STAFF_WALKMERGE;
import static net.runelite.api.gameval.AnimationID.HUMAN_EYE_OF_AYAK_NORMAL;
import static net.runelite.api.gameval.AnimationID.SCYTHE_OF_VITUR_ATTACK;
import static net.runelite.api.gameval.AnimationID.TOA_SOT_CAST_A;
import static net.runelite.api.gameval.AnimationID.TOA_SOT_CAST_B;
import net.runelite.api.gameval.ItemID;
import static net.runelite.api.gameval.SpotanimID.ARROW_VENATOR01_LAUNCH01;

/**
 * Weapons that consume charges and are therefore tracked per attack made using this weapon.
 * TrackedEquipments are identified using the base weapon ID rather than the itemId,
 */
@Getter
public enum TrackedEquipment
{
	SCYTHE_OF_VITUR(ItemID.SCYTHE_OF_VITUR, new int[]{2524, 2524, 2522, 2524}, new int[]{SCYTHE_OF_VITUR_ATTACK}),
	TUMEKENS_SHADOW(ItemID.TUMEKENS_SHADOW, new int[]{6410, 6410, 6410, 6410}, new int[]{TOA_SOT_CAST_A, TOA_SOT_CAST_B}),
	VENATOR_BOW(ItemID.VENATOR_BOW, new int[]{6797, 6797, 6797, 6797}, new int[]{ARROW_VENATOR01_LAUNCH01}),
	EYE_OF_AYAK(ItemID.EYE_OF_AYAK, new int[]{178,178,178,178}, new int[]{HUMAN_EYE_OF_AYAK_NORMAL} ),
	SANGUINESTI_STAFF(ItemID.SANGUINESTI_STAFF, new int[]{178,178,178,178}, new int[]{HUMAN_CASTWAVE_STAFF_WALKMERGE} ),
	TOXIC_TRIDENT(ItemID.TOXIC_TOTS_CHARGED, new int[]{178,178,178,178}, new int[]{HUMAN_CASTWAVE_STAFF_WALKMERGE} ),
//	TOXIC_STAFF_OF_THE_DEAD(),
	TRIDENT_OF_THE_SEAS(ItemID.TOTS, new int[]{178,178,178,178}, new int[]{HUMAN_CASTWAVE_STAFF_WALKMERGE});
//	CRAWS_BOW(),
//	WEBWEAVER_BOW(),
//	BOW_OF_FAERDINHEN(),
//	CRYSTAL_BOW,


	private final int baseId;
	private final ItemCharge itemCharge;
	private final int[] soundIds;
	private final int nSoundIds;
	private final int[] attackAnimationIds;

	private static final Map<Integer, TrackedEquipment> BY_BASE_ID = new HashMap<>();

	static
	{
		for (TrackedEquipment weapon : values())
		{
			BY_BASE_ID.put(weapon.getBaseId(), weapon);
		}
	}

	TrackedEquipment(int baseId, int[] soundIds, int[] attackAnimationIds)
	{
		this.baseId = baseId;
		this.itemCharge = ItemCharge.getByBaseId(baseId);

		this.soundIds = soundIds;
		nSoundIds = soundIds.length;
		this.attackAnimationIds = attackAnimationIds;
	}

	/**
	 * Instantly look up a TrackedEquipment by its base item ID. Return null if it is not registered.
	 */
	public static TrackedEquipment getByBaseId(int baseId)
	{
		return BY_BASE_ID.getOrDefault(baseId, null);
	}

	/**
	 * Return the soundId mapped to the given attackStyleIndex
	 */
	public int getSoundIdForStyle(int attackStyleIndex)
	{
		if (attackStyleIndex >= 0 && attackStyleIndex < nSoundIds)
		{
			return soundIds[attackStyleIndex];
		}
		return -1;
	}

	/**
	 * Return true if the given attack animation ID is associated with this weapon
	 */
	public boolean isAttackAnimation(int animId)
	{
		if (attackAnimationIds == null) return false;

		for (int attackAnimationId : attackAnimationIds)
		{
			if (attackAnimationId == animId)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the value of one ItemCharge associated with this TrackedEquipment
	 */
	public int getChargeValue()
	{
		return itemCharge.getChargeValue();
	}
}
