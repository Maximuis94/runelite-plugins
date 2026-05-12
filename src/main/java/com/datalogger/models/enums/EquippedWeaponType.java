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
import static com.datalogger.models.enums.AttackType.MELEE_STAB;
import static com.datalogger.models.enums.AttackType.RANGED_HEAVY;
import static com.datalogger.models.enums.AttackType.RANGED_LIGHT;
import static com.datalogger.models.enums.AttackType.RANGED_NORMAL;
import static com.datalogger.models.enums.AttackType.RANGED_TYPELESS;
import static com.datalogger.models.enums.WeaponStyle.*;
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
		new Stance(MELEE_DEFENSIVE, MELEE_CRUSH),
		null
	),
	AXE(1,
		new Stance(MELEE_ACCURATE, MELEE_SLASH),
		new Stance(MELEE_AGGRESSIVE, MELEE_SLASH),
		new Stance(MELEE_AGGRESSIVE, MELEE_CRUSH),
		new Stance(MELEE_DEFENSIVE, MELEE_SLASH),
		null
	),
	MAUL(2,
		new Stance(MELEE_ACCURATE, MELEE_CRUSH),
		new Stance(MELEE_AGGRESSIVE, MELEE_CRUSH),
		null,
		new Stance(MELEE_DEFENSIVE, MELEE_CRUSH),
		null
	),

	BOW(3,
		new Stance(RANGED_ACCURATE, RANGED_NORMAL),
		new Stance(RANGED_RAPID, RANGED_NORMAL),
		null,
		new Stance(RANGED_LONGRANGE, RANGED_NORMAL),
		null
	),

	CLAW(4,
		new Stance(MELEE_ACCURATE, MELEE_SLASH),
		new Stance(MELEE_AGGRESSIVE, MELEE_SLASH),
		new Stance(MELEE_CONTROLLED, MELEE_STAB),
		new Stance(MELEE_DEFENSIVE, MELEE_SLASH),
		null
	),

	CROSSBOW(5,
		new Stance(RANGED_ACCURATE, RANGED_HEAVY),
		new Stance(RANGED_RAPID, RANGED_HEAVY),
		null,
		new Stance(RANGED_LONGRANGE, RANGED_HEAVY),
		null
	),

	SALAMANDER(6,
		new Stance(MELEE_AGGRESSIVE, MELEE_SLASH),
		new Stance(RANGED_RAPID, RANGED_TYPELESS),
		new Stance(SALAMANDER_MAGIC, MAGIC_TYPELESS),
		null,
		null
	),

	CHINCHOMPA(7,
		new Stance(RANGED_ACCURATE, RANGED_HEAVY),
		new Stance(RANGED_RAPID, RANGED_HEAVY),
		null,
		new Stance(RANGED_LONGRANGE, RANGED_HEAVY),
		null
	),

	// Bazooka from Mourning's End Part I
	FIXED_DEVICE(8,
		new Stance(NO_COMBAT, AttackType.NO_COMBAT),
		new Stance(MELEE_AGGRESSIVE, MELEE_CRUSH),
		null,
		null,
		null
	),

	ONE_HANDED_SWORD(9,
		new Stance(MELEE_ACCURATE, MELEE_SLASH),
		new Stance(MELEE_AGGRESSIVE, MELEE_SLASH),
		new Stance(MELEE_CONTROLLED, MELEE_STAB),
		new Stance(MELEE_DEFENSIVE, MELEE_SLASH),
		null
	),

	TWO_HANDED_SWORD(10,
		new Stance(MELEE_ACCURATE, MELEE_SLASH),
		new Stance(MELEE_AGGRESSIVE, MELEE_SLASH),
		new Stance(MELEE_AGGRESSIVE, MELEE_CRUSH),
		new Stance(MELEE_DEFENSIVE, MELEE_SLASH)
	),

	PICKAXE(11,
		new Stance(WeaponStyle.MELEE_ACCURATE, AttackType.MELEE_STAB),
		new Stance(WeaponStyle.MELEE_AGGRESSIVE, AttackType.MELEE_STAB),
		new Stance(WeaponStyle.MELEE_DEFENSIVE, AttackType.MELEE_CRUSH),
		new Stance(WeaponStyle.MAGIC_DEFENSIVE_AUTOCAST, AttackType.MELEE_STAB)
	),

	HALBERD(12,
		new Stance(MELEE_CONTROLLED, MELEE_STAB),
		new Stance(MELEE_AGGRESSIVE, MELEE_SLASH),
		null,
		new Stance(MELEE_DEFENSIVE, MELEE_STAB)
	),

	BANNER(13,
		new Stance(MELEE_ACCURATE, MELEE_CRUSH),
		new Stance(MELEE_AGGRESSIVE, MELEE_CRUSH),
		null,
		new Stance(MELEE_DEFENSIVE, MELEE_CRUSH),
		null
	),

	SCYTHE_OF_VITUR(14,
		new Stance(MELEE_ACCURATE, MELEE_SLASH),
		new Stance(MELEE_AGGRESSIVE, MELEE_SLASH),
		new Stance(MELEE_AGGRESSIVE, MELEE_CRUSH),
		new Stance(MELEE_DEFENSIVE, MELEE_SLASH),
		null
	),

	SPEAR(15,
		new Stance(MELEE_CONTROLLED, MELEE_STAB),
		new Stance(MELEE_CONTROLLED, MELEE_SLASH),
		new Stance(MELEE_CONTROLLED, MELEE_CRUSH),
		new Stance(MELEE_DEFENSIVE, MELEE_STAB)
	),

	MACE(16,
		new Stance(MELEE_ACCURATE, MELEE_CRUSH),
		new Stance(MELEE_AGGRESSIVE, MELEE_CRUSH),
		new Stance(MELEE_CONTROLLED, MELEE_STAB),
		new Stance(MELEE_DEFENSIVE, MELEE_CRUSH),
		null
	),

	DAGGER(17,
		new Stance(MELEE_ACCURATE, MELEE_STAB),
		new Stance(MELEE_AGGRESSIVE, MELEE_STAB),
		new Stance(MELEE_AGGRESSIVE, MELEE_SLASH),
		new Stance(MELEE_DEFENSIVE, MELEE_STAB)
	),

	STAFF(18,
		new Stance(WeaponStyle.MELEE_ACCURATE, AttackType.MELEE_CRUSH),    // Index 0: Bash
		new Stance(WeaponStyle.MELEE_AGGRESSIVE, AttackType.MELEE_CRUSH),  // Index 1: Pound
		new Stance(WeaponStyle.MELEE_DEFENSIVE, AttackType.MELEE_CRUSH),   // Index 2: Focus
		new Stance(WeaponStyle.MAGIC_AUTOCAST, MAGIC_TYPELESS),
		new Stance(WeaponStyle.MAGIC_DEFENSIVE_AUTOCAST, MAGIC_TYPELESS)
	),

	THROWN(19,
		new Stance(RANGED_ACCURATE, RANGED_LIGHT),
		new Stance(RANGED_RAPID, RANGED_LIGHT),
		null,
		new Stance(RANGED_LONGRANGE, RANGED_LIGHT),
		null
	),

	WHIP(20,
		new Stance(MELEE_ACCURATE, MELEE_SLASH),
		new Stance(MELEE_CONTROLLED, MELEE_SLASH),
		null,
		new Stance(MELEE_DEFENSIVE, MELEE_SLASH),
		null
	),

//	TO_DO_21(21,
//		null,
//		null,
//		null,
//		null,
//		null
//	),

//	TO_DO_22(22,
//		null,
//		null,
//		null,
//		null,
//		null
//	),

	GODSWORD(23,
		new Stance(MELEE_ACCURATE, MELEE_SLASH),
		new Stance(MELEE_AGGRESSIVE, MELEE_SLASH),
		new Stance(MELEE_AGGRESSIVE, MELEE_CRUSH),
		new Stance(MELEE_DEFENSIVE, MELEE_SLASH),
		null
	),

	POWERED_STAFF(24,
		new Stance(MAGIC_ACCURATE, MAGIC_TYPELESS),
		new Stance(MAGIC_ACCURATE, MAGIC_TYPELESS),
		null,
		new Stance(MAGIC_LONGRANGE, MAGIC_TYPELESS),
		null
	),

	CRYSTAL_HALBERD(26,
		new Stance(MELEE_CONTROLLED, MELEE_STAB),
		new Stance(MELEE_AGGRESSIVE, MELEE_SLASH),
		null,
		new Stance(MELEE_DEFENSIVE, MELEE_STAB),
		null
	),

//	TO_DO_27(27,
//		null,
//		null,
//		null,
//		null,
//		null
//	),

	DINHS_BULWARK(28,
		new Stance(MELEE_ACCURATE, MELEE_STAB),
		null,
		null,
		new Stance(NO_COMBAT, AttackType.NO_COMBAT)
	),

//	TO_DO_29(29,
//		null,
//		null,
//		null,
//		null,
//		null
//	),

	KERIS_PARTISAN(30,
		new Stance(MELEE_ACCURATE, MELEE_STAB),
		new Stance(MELEE_AGGRESSIVE, MELEE_STAB),
		new Stance(MELEE_AGGRESSIVE, MELEE_CRUSH),
		new Stance(MELEE_DEFENSIVE, MELEE_STAB),
		null
	),
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