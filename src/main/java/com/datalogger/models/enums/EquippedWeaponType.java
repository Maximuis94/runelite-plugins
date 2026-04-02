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

import static com.datalogger.models.enums.AttackType.MAGIC_TYPELESS;
import static com.datalogger.models.enums.AttackType.MELEE_CRUSH;
import static com.datalogger.models.enums.AttackType.MELEE_SLASH;
import static com.datalogger.models.enums.AttackType.RANGED_NORMAL;
import static com.datalogger.models.enums.AttackType.RANGED_TYPELESS;
import static com.datalogger.models.enums.WeaponStyle.MELEE_ACCURATE;
import static com.datalogger.models.enums.WeaponStyle.MELEE_AGGRESSIVE;
import static com.datalogger.models.enums.WeaponStyle.MELEE_CONTROLLED;
import static com.datalogger.models.enums.WeaponStyle.MELEE_DEFENSIVE;
import static com.datalogger.models.enums.WeaponStyle.RANGED_ACCURATE;
import static com.datalogger.models.enums.WeaponStyle.RANGED_LONGRANGE;
import static com.datalogger.models.enums.WeaponStyle.RANGED_RAPID;
import static com.datalogger.models.enums.WeaponStyle.SALAMANDER_MAGIC;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Value;

/**
 * Equipped weapon types that have been manually identified.
 * The varbitValue indicates the value returned from client.getVarbitValue(COMBAT_WEAPON_CATEGORY)
 * The Stance list is ordered such that the index of each element corresponds with the value produced by
 * client.getVarpValue(COM_MODE)
 */
public enum EquippedWeaponType
{

	UNARMED(0,
		new Stance(MELEE_ACCURATE, MELEE_CRUSH),
		new Stance(MELEE_AGGRESSIVE, MELEE_CRUSH),
		null,
		new Stance(MELEE_DEFENSIVE, MELEE_CRUSH)
	),

	BOW(3,
		new Stance(RANGED_ACCURATE, RANGED_NORMAL),
		new Stance(RANGED_RAPID, RANGED_NORMAL),
		new Stance(RANGED_LONGRANGE, RANGED_NORMAL)
	),

	WHIP(4,
		new Stance(MELEE_ACCURATE, MELEE_SLASH),
		new Stance(MELEE_CONTROLLED, MELEE_SLASH),
		null, // No index 2
		new Stance(MELEE_DEFENSIVE, MELEE_SLASH)
	),

	TWO_HANDED_SWORD(5,
		new Stance(MELEE_ACCURATE, MELEE_SLASH),
		new Stance(MELEE_AGGRESSIVE, MELEE_SLASH),
		new Stance(MELEE_AGGRESSIVE, MELEE_CRUSH),
		new Stance(MELEE_DEFENSIVE, MELEE_SLASH)
	),

	SALAMANDER(6,
		new Stance(MELEE_AGGRESSIVE, MELEE_SLASH),
		new Stance(RANGED_RAPID, RANGED_TYPELESS),
		new Stance(SALAMANDER_MAGIC, MAGIC_TYPELESS)
	),

	// --- UNVERIFIED VARBITS (Commented Out) ---
	// The stances and attack types below are mechanically 100% accurate to OSRS,
	// but the exact integer for the EQUIPPED_WEAPON_TYPE varbit needs live verification.

    /*
    DAGGER(???, // Might be 7 or 9
       new Stance(MELEE_ACCURATE, MELEE_STAB),
       new Stance(MELEE_AGGRESSIVE, MELEE_STAB),
       new Stance(MELEE_AGGRESSIVE, MELEE_SLASH),
       new Stance(MELEE_DEFENSIVE, MELEE_STAB)
    ),
    */

    /*
    MACE(???, // Likely 2, but needs verification
       new Stance(MELEE_ACCURATE, MELEE_CRUSH),
       new Stance(MELEE_AGGRESSIVE, MELEE_CRUSH),
       new Stance(MELEE_CONTROLLED, MELEE_STAB), // The "Spike" style
       new Stance(MELEE_DEFENSIVE, MELEE_CRUSH)
    ),
    */

    /*
    SCYTHE_OF_VITUR(14, // Highly likely to be 14, but keeping it safe
       new Stance(MELEE_ACCURATE, MELEE_SLASH),
       new Stance(MELEE_AGGRESSIVE, MELEE_SLASH),
       new Stance(MELEE_AGGRESSIVE, MELEE_CRUSH), // The "Jab" style
       new Stance(MELEE_DEFENSIVE, MELEE_SLASH)
    ),
    */

    /*
    POWERED_STAFF(???, // Tridents, Sanguinesti, Tumeken's Shadow
       new Stance(MAGIC_ACCURATE, MAGIC),
       new Stance(MAGIC_ACCURATE, MAGIC),
       null, // No index 2 for powered staves
       new Stance(MAGIC_LONGRANGE, MAGIC)
    ),
    */

	STAFF(18, // Often 17 or 18, needs live verification via Developer Tools
	new Stance(WeaponStyle.MELEE_ACCURATE, AttackType.MELEE_CRUSH),    // Index 0: Bash
       new Stance(WeaponStyle.MELEE_AGGRESSIVE, AttackType.MELEE_CRUSH),  // Index 1: Pound
       new Stance(WeaponStyle.MELEE_DEFENSIVE, AttackType.MELEE_CRUSH),   // Index 2: Focus

	// Index 3: "Spell" (Standard Autocast)
       new Stance(WeaponStyle.MAGIC_AUTOCAST, MAGIC_TYPELESS),

	// Index 4: Defensive Autocast
       new Stance(WeaponStyle.MAGIC_DEFENSIVE_AUTOCAST, MAGIC_TYPELESS)
    ),

    /*
    CROSSBOW(???, // Usually its own interface separate from bows
       new Stance(RANGED_ACCURATE, RANGED_NORMAL),
       new Stance(RANGED_RAPID, RANGED_NORMAL),
       null, // No index 2
       new Stance(RANGED_LONGRANGE, RANGED_NORMAL)
    ),
    */

    /*
    CHINCHOMPA(???,
       new Stance(RANGED_ACCURATE, RANGED_NORMAL), // Short fuse
       new Stance(RANGED_RAPID, RANGED_NORMAL),    // Medium fuse
       new Stance(RANGED_LONGRANGE, RANGED_NORMAL) // Long fuse
    ),
    */
	;

	@Getter
	private final int varbitValue;

	private final Stance[] stances;

	private static final Map<Integer, EquippedWeaponType> BY_VARBIT = new HashMap<>();

	static
	{
		for (EquippedWeaponType type : values())
		{
			BY_VARBIT.put(type.varbitValue, type);
		}
	}

	EquippedWeaponType(int varbitValue, Stance... stances)
	{
		this.varbitValue = varbitValue;
		this.stances = stances;
	}

	/**
	 * Return the EquippedWeaponType associated with the given varbitValue returned by COMBAT_WEAPON_CATEGORY
	 */
	public static EquippedWeaponType getByVarbitValue(int combatWeaponCategoryVarbitValue)
	{
		return BY_VARBIT.getOrDefault(combatWeaponCategoryVarbitValue, null);
	}

	/**
	 * Return the Stance associated with the given attackStyleIndex of the EquippedWeaponType
	 */
	public Stance getStance(int attackStyleIndex)
	{
		if (stances != null && attackStyleIndex >= 0 && attackStyleIndex < stances.length)
		{
			return stances[attackStyleIndex];
		}
		return null;
	}

	@Value
	public static class Stance
	{
		WeaponStyle weaponStyle;
		AttackType attackType;
	}
}