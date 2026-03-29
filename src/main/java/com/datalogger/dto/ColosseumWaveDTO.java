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

package com.datalogger.dto;

import com.datalogger.models.colosseum.ManticoreAttackSequence;
import com.datalogger.models.itemvault.ItemBundle;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ColosseumWaveDTO {
	private int wave;
	private String status;

	private String accountName;
	private String tag;

	private ItemBundle earnedLoot;

	private List<String> modifierChoices;
	private String chosenModifier;
	private String activeModifiers;

	private double timeTaken;
	private int speedBonus;
	private int damageTaken;
	private int damageBonus;
	private int modifierGlory;
	private int completionBonus;
	private int waveGlory;
	private int totalGlory;
	private double totalTimeTaken;

	private Integer serpentShamanSpawnX;
	private Integer serpentShamanSpawnY;

	private Integer javelinColossusSpawnAX;
	private Integer javelinColossusSpawnAY;
	private Integer javelinColossusSpawnBX;
	private Integer javelinColossusSpawnBY;

	private Integer manticoreSpawnAX;
	private Integer manticoreSpawnAY;
	public List<ManticoreAttackSequence.ManticoreOrb> manticoreSequenceA;
	private Integer manticoreSpawnBX;
	private Integer manticoreSpawnBY;
	private List<ManticoreAttackSequence.ManticoreOrb> manticoreSequenceB;

	private Integer shockwaveColossusSpawnAX;
	private Integer shockwaveColossusSpawnAY;
	private Integer shockwaveColossusSpawnBX;
	private Integer shockwaveColossusSpawnBY;

	private Integer jaguarWarriorReinforcementsSpawnX;
	private Integer jaguarWarriorReinforcementsSpawnY;

	private Integer serpentShamanReinforcementsSpawnX;
	private Integer serpentShamanReinforcementsSpawnY;

	private Integer minotaurReinforcementsSpawnX;
	private Integer minotaurReinforcementsSpawnY;
}