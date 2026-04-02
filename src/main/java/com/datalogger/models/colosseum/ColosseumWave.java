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
package com.datalogger.models.colosseum;

import com.datalogger.dto.ColosseumWaveDTO;
import com.datalogger.framework.DataRow;
import com.datalogger.models.enums.ColosseumModifier;
import com.datalogger.models.enums.WaveStatus;
import com.datalogger.models.itemvault.ItemBundle;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Singular;
import net.runelite.api.coords.WorldPoint;

/**
 * Data of a particular wave in a Colosseum attempt. It reflects data after completion/failure.
 */
@Data
@Builder
public class ColosseumWave implements DataRow
{
	private int wave;

	@NonNull
	private WaveStatus status;

	private String accountName;
	private String tag;

	private ItemBundle earnedLoot;
	private int lootValue;

	@Singular
	private List<ColosseumModifier> modifierChoices;

	private String activeModifiers;

	private ColosseumModifier chosenModifier;

	private int startTick;
	private int endTick;

	private double timeTaken;
	private double totalTimeTaken;
	private int speedBonus;

	private int damageTaken;
	private int damageBonus;

	private int modifierGlory;

	private int completionBonus;
	private int waveGlory;
	private int totalGlory;

	private WorldPoint serpentShamanSpawn;

	private WorldPoint javelinColossusSpawnA;
	private WorldPoint javelinColossusSpawnB;

	private WorldPoint manticoreSpawnA;
	ManticoreAttackSequence manticoreSequenceA;
	private WorldPoint manticoreSpawnB;
	ManticoreAttackSequence manticoreSequenceB;

	private WorldPoint shockwaveColossusSpawnA;
	private WorldPoint shockwaveColossusSpawnB;

	private WorldPoint jaguarWarriorReinforcementsSpawn;
	private WorldPoint serpentShamanReinforcementsSpawn;
	private WorldPoint minotaurReinforcementsSpawn;

	public ColosseumWaveDTO toDTO() {
		return ColosseumWaveDTO.builder()
			.wave(wave)
			.status(status.name())
			.accountName(accountName)
			.tag(tag)
			.earnedLoot(earnedLoot)
			.lootValue(lootValue)
			.modifierChoices(modifierChoices != null ? modifierChoices.stream()
				.map(ColosseumModifier::name)
				.collect(Collectors.toList()) : new java.util.ArrayList<>())
			.chosenModifier(chosenModifier != null ? chosenModifier.name() : null)
			.activeModifiers(activeModifiers != null ? activeModifiers : "")
			.timeTaken(timeTaken)
			.totalTimeTaken(BigDecimal.valueOf(totalTimeTaken).setScale(1, RoundingMode.HALF_UP).doubleValue())
			.speedBonus(speedBonus)
			.damageTaken(damageTaken)
			.damageBonus(damageBonus)
			.modifierGlory(modifierGlory)
			.completionBonus(completionBonus)
			.totalGlory(totalGlory)
			.waveGlory(waveGlory)

			.serpentShamanSpawnX(serpentShamanSpawn != null ? serpentShamanSpawn.getRegionX() : null)
			.serpentShamanSpawnY(serpentShamanSpawn != null ? serpentShamanSpawn.getRegionY() : null)

			.javelinColossusSpawnAX(javelinColossusSpawnA != null ? javelinColossusSpawnA.getRegionX() : null)
			.javelinColossusSpawnAY(javelinColossusSpawnA != null ? javelinColossusSpawnA.getRegionY() : null)
			.javelinColossusSpawnBX(javelinColossusSpawnB != null ? javelinColossusSpawnB.getRegionX() : null)
			.javelinColossusSpawnBY(javelinColossusSpawnB != null ? javelinColossusSpawnB.getRegionY() : null)

			.manticoreSpawnAX(manticoreSpawnA != null ? manticoreSpawnA.getRegionX() : null)
			.manticoreSpawnAY(manticoreSpawnA != null ? manticoreSpawnA.getRegionY() : null)
			.manticoreSequenceA(manticoreSequenceA != null ? manticoreSequenceA.getOrbs() : null)

			.manticoreSpawnBX(manticoreSpawnB != null ? manticoreSpawnB.getRegionX() : null)
			.manticoreSpawnBY(manticoreSpawnB != null ? manticoreSpawnB.getRegionY() : null)
			.manticoreSequenceB(manticoreSequenceB != null ? manticoreSequenceB.getOrbs() : null)

			.shockwaveColossusSpawnAX(shockwaveColossusSpawnA != null ? shockwaveColossusSpawnA.getRegionX() : null)
			.shockwaveColossusSpawnAY(shockwaveColossusSpawnA != null ? shockwaveColossusSpawnA.getRegionY() : null)
			.shockwaveColossusSpawnBX(shockwaveColossusSpawnB != null ? shockwaveColossusSpawnB.getRegionX() : null)
			.shockwaveColossusSpawnBY(shockwaveColossusSpawnB != null ? shockwaveColossusSpawnB.getRegionY() : null)

			.jaguarWarriorReinforcementsSpawnX(jaguarWarriorReinforcementsSpawn != null ? jaguarWarriorReinforcementsSpawn.getRegionX() : null)
			.jaguarWarriorReinforcementsSpawnY(jaguarWarriorReinforcementsSpawn != null ? jaguarWarriorReinforcementsSpawn.getRegionY() : null)

			.serpentShamanReinforcementsSpawnX(serpentShamanReinforcementsSpawn != null ? serpentShamanReinforcementsSpawn.getRegionX() : null)
			.serpentShamanReinforcementsSpawnY(serpentShamanReinforcementsSpawn != null ? serpentShamanReinforcementsSpawn.getRegionY() : null)

			.minotaurReinforcementsSpawnX(minotaurReinforcementsSpawn != null ? minotaurReinforcementsSpawn.getRegionX() : null)
			.minotaurReinforcementsSpawnY(minotaurReinforcementsSpawn != null ? minotaurReinforcementsSpawn.getRegionY() : null)
			.build();
	}

	public static String csvHeader() {
		return "wave,status,accountName,tag,itemId,itemName,quantity,modifierChoice_I,modifierChoice_II,modifierChoice_III,chosenModifier,activeModifiers,timeTaken,damageTaken,speedBonus,damageBonus,modifierGlory,completionBonus,waveGlory,totalGlory,totalTimeTaken,serpentShamanSpawnX,serpentShamanSpawnY,javelinColossusSpawnAX,javelinColossusSpawnAY,javelinColossusSpawnBX,javelinColossusSpawnBY,manticoreSpawnAX,manticoreSpawnAY,manticoreSequenceA,manticoreSpawnBX,manticoreSpawnBY,manticoreSequenceB,shockwaveColossusSpawnAX,shockwaveColossusSpawnAY,shockwaveColossusSpawnBX,shockwaveColossusSpawnBY,jaguarWarriorReinfSpawnX,jaguarWarriorReinfSpawnY,serpentShamanReinfSpawnX,serpentShamanReinfSpawnY,minotaurReinfSpawnX,minotaurReinfSpawnY";
	}

	public String toCsvRow() {
		String itemId = earnedLoot == null ? "-1" : String.valueOf(earnedLoot.getItemId());

		String itemName = earnedLoot == null ? "" : earnedLoot.getItemName();

		String quantity = earnedLoot == null  ? "0" : String.valueOf(earnedLoot.getQuantity());

		String modStatus = status.name();
		String chosenMod = chosenModifier != null ? chosenModifier.name() : "";

		String mod1 = modifierChoices != null && !modifierChoices.isEmpty() && modifierChoices.get(0) != null ? modifierChoices.get(0).name() : "";
		String mod2 = modifierChoices != null && modifierChoices.size() > 1 && modifierChoices.get(1) != null ? modifierChoices.get(1).name() : "";
		String mod3 = modifierChoices != null && modifierChoices.size() > 2 && modifierChoices.get(2) != null ? modifierChoices.get(2).name() : "";
		String activeModifiersString = String.join("|", activeModifiers);
		return String.join(",",
			String.valueOf(wave),
			modStatus,
			accountName,
			tag,
			itemId,
			itemName,
			quantity,
			mod1,
			mod2,
			mod3,
			chosenMod,
			activeModifiersString,
			String.format("%.1f", timeTaken),
			String.valueOf(damageTaken),
			String.valueOf(speedBonus),
			String.valueOf(damageBonus),
			String.valueOf(modifierGlory),
			String.valueOf(completionBonus),
			String.valueOf(waveGlory),
			String.valueOf(totalGlory),
			String.format("%.1f", totalTimeTaken),

			serpentShamanSpawn != null ? String.valueOf(serpentShamanSpawn.getRegionX()) : "",
			serpentShamanSpawn != null ? String.valueOf(serpentShamanSpawn.getRegionY()) : "",

			javelinColossusSpawnA != null ? String.valueOf(javelinColossusSpawnA.getRegionX()) : "",
			javelinColossusSpawnA != null ? String.valueOf(javelinColossusSpawnA.getRegionY()) : "",
			javelinColossusSpawnB != null ? String.valueOf(javelinColossusSpawnB.getRegionX()) : "",
			javelinColossusSpawnB != null ? String.valueOf(javelinColossusSpawnB.getRegionY()) : "",

			manticoreSpawnA != null ? String.valueOf(manticoreSpawnA.getRegionX()) : "",
			manticoreSpawnA != null ? String.valueOf(manticoreSpawnA.getRegionY()) : "",
			manticoreSequenceA != null ? manticoreSequenceA.toCsvFormat() : "",
			manticoreSpawnB != null ? String.valueOf(manticoreSpawnB.getRegionX()) : "",
			manticoreSpawnB != null ? String.valueOf(manticoreSpawnB.getRegionY()) : "",
			manticoreSequenceB != null ? manticoreSequenceB.toCsvFormat() : "",

			shockwaveColossusSpawnA != null ? String.valueOf(shockwaveColossusSpawnA.getRegionX()) : "",
			shockwaveColossusSpawnA != null ? String.valueOf(shockwaveColossusSpawnA.getRegionY()) : "",
			shockwaveColossusSpawnB != null ? String.valueOf(shockwaveColossusSpawnB.getRegionX()) : "",
			shockwaveColossusSpawnB != null ? String.valueOf(shockwaveColossusSpawnB.getRegionY()) : "",

			jaguarWarriorReinforcementsSpawn != null ? String.valueOf(jaguarWarriorReinforcementsSpawn.getRegionX()) : "",
			jaguarWarriorReinforcementsSpawn != null ? String.valueOf(jaguarWarriorReinforcementsSpawn.getRegionY()) : "",
			serpentShamanReinforcementsSpawn != null ? String.valueOf(serpentShamanReinforcementsSpawn.getRegionX()) : "",
			serpentShamanReinforcementsSpawn != null ? String.valueOf(serpentShamanReinforcementsSpawn.getRegionY()) : "",
			minotaurReinforcementsSpawn != null ? String.valueOf(minotaurReinforcementsSpawn.getRegionX()) : "",
			minotaurReinforcementsSpawn != null ? String.valueOf(minotaurReinforcementsSpawn.getRegionY()) : ""
		);
	}
}
