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

import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import net.runelite.api.Skill;

/**
 * Exhaustive list of weapon styles, with invisible skill-, attack speed- and attack range boosts
 */
@Getter
public enum WeaponStyle
{
	MELEE_ACCURATE(Map.of(Skill.ATTACK, 3), 0, 0, Skill.ATTACK, Skill.HITPOINTS),
	MELEE_AGGRESSIVE(Map.of(Skill.STRENGTH, 3), 0, 0, Skill.STRENGTH, Skill.HITPOINTS),
	MELEE_DEFENSIVE(Map.of(Skill.DEFENCE, 3), 0, 0, Skill.DEFENCE, Skill.HITPOINTS),
	MELEE_CONTROLLED(Map.of(Skill.ATTACK, 1, Skill.STRENGTH, 1, Skill.DEFENCE, 1), 0, 0, Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.HITPOINTS),

	RANGED_ACCURATE(Map.of(Skill.RANGED, 3), 0, 0, Skill.RANGED, Skill.HITPOINTS),
	RANGED_RAPID(Collections.emptyMap(), 1, 0, Skill.RANGED, Skill.HITPOINTS),
	RANGED_LONGRANGE(Map.of(Skill.DEFENCE, 3), 0, 2, Skill.RANGED, Skill.DEFENCE, Skill.HITPOINTS),

	MAGIC_ACCURATE(Map.of(Skill.MAGIC, 3), 0, 0, Skill.MAGIC, Skill.HITPOINTS),
	MAGIC_LONGRANGE(Map.of(Skill.MAGIC, 1, Skill.DEFENCE, 3), 0, 2, Skill.MAGIC, Skill.DEFENCE, Skill.HITPOINTS),
	MAGIC_AUTOCAST(Collections.emptyMap(), 0, 0, Skill.MAGIC, Skill.HITPOINTS),
	MAGIC_DEFENSIVE_AUTOCAST(Collections.emptyMap(), 0, 0, Skill.MAGIC, Skill.DEFENCE, Skill.HITPOINTS),

	SALAMANDER_MAGIC(Map.of(Skill.DEFENCE, 3), 0, 0, Skill.MAGIC, Skill.HITPOINTS);

	private final Map<Skill, Integer> invisibleSkillBoosts;
	private final int attackSpeedBoost;
	private final int attackRangeBoost;
	private final Skill[] experienceGained;

	WeaponStyle(Map<Skill, Integer> invisibleSkillBoosts, int attackSpeedBoost, int attackRangeBoost, Skill... experienceGained)
	{
		this.invisibleSkillBoosts = invisibleSkillBoosts;
		this.attackSpeedBoost = attackSpeedBoost;
		this.attackRangeBoost = attackRangeBoost;
		this.experienceGained = experienceGained;
	}
}