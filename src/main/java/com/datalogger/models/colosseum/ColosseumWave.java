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
import com.datalogger.models.colosseum.enums.ColosseumModifier;
import com.datalogger.models.colosseum.enums.WaveStatus;
import com.datalogger.models.common.ItemBundle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import net.runelite.api.World;
import net.runelite.api.coords.WorldPoint;

/**
 * Data of a particular wave in a Colosseum attempt. It reflects data after completion/failure.
 */
@Data
@Builder
public class ColosseumWave implements DataRow
{
	private int wave;
	private WaveStatus status;

	private String accountName;
	private String tag;

	private List<ItemBundle> earnedLoot;

	@Singular
	private List<ColosseumModifier> modifierChoices;

	private ColosseumModifier chosenModifier;

	private int startTick;
	private int endTick;

	private double timeTaken;
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
			.status(status != null ? status.name() : "UNKNOWN")
			.accountName(accountName)
			.tag(tag)

			.earnedLoot(earnedLoot != null ? earnedLoot : new java.util.ArrayList<>())

			.modifierChoices(modifierChoices != null ? modifierChoices.stream()
				.map(ColosseumModifier::name)
				.collect(Collectors.toList()) : new java.util.ArrayList<>())
			.chosenModifier(chosenModifier != null ? chosenModifier.name() : null)
			.timeTaken(timeTaken)
			.speedBonus(speedBonus)
			.damageTaken(damageTaken)
			.damageBonus(damageBonus)
			.modifierGlory(modifierGlory)
			.completionBonus(completionBonus)
			.totalGlory(totalGlory)
			.waveGlory(waveGlory)

			.serpentShamanSpawnX(serpentShamanSpawn != null ? serpentShamanSpawn.getX() : null)
			.serpentShamanSpawnY(serpentShamanSpawn != null ? serpentShamanSpawn.getY() : null)

			.javelinColossusSpawnAX(javelinColossusSpawnA != null ? javelinColossusSpawnA.getX() : null)
			.javelinColossusSpawnAY(javelinColossusSpawnA != null ? javelinColossusSpawnA.getY() : null)
			.javelinColossusSpawnBX(javelinColossusSpawnB != null ? javelinColossusSpawnB.getX() : null)
			.javelinColossusSpawnBY(javelinColossusSpawnB != null ? javelinColossusSpawnB.getY() : null)

			.manticoreSpawnAX(manticoreSpawnA != null ? manticoreSpawnA.getX() : null)
			.manticoreSpawnAY(manticoreSpawnA != null ? manticoreSpawnA.getY() : null)
			.manticoreSequenceA(manticoreSequenceA != null ? manticoreSequenceA.getOrbs() : null)

			.manticoreSpawnBX(manticoreSpawnB != null ? manticoreSpawnB.getX() : null)
			.manticoreSpawnBY(manticoreSpawnB != null ? manticoreSpawnB.getY() : null)
			.manticoreSequenceB(manticoreSequenceB != null ? manticoreSequenceB.getOrbs() : null)

			.shockwaveColossusSpawnAX(shockwaveColossusSpawnA != null ? shockwaveColossusSpawnA.getX() : null)
			.shockwaveColossusSpawnAY(shockwaveColossusSpawnA != null ? shockwaveColossusSpawnA.getY() : null)
			.shockwaveColossusSpawnBX(shockwaveColossusSpawnB != null ? shockwaveColossusSpawnB.getX() : null)
			.shockwaveColossusSpawnBY(shockwaveColossusSpawnB != null ? shockwaveColossusSpawnB.getY() : null)

			.jaguarWarriorReinforcementsSpawnX(jaguarWarriorReinforcementsSpawn != null ? jaguarWarriorReinforcementsSpawn.getX() : null)
			.jaguarWarriorReinforcementsSpawnY(jaguarWarriorReinforcementsSpawn != null ? jaguarWarriorReinforcementsSpawn.getY() : null)

			.serpentShamanReinforcementsSpawnX(serpentShamanReinforcementsSpawn != null ? serpentShamanReinforcementsSpawn.getX() : null)
			.serpentShamanReinforcementsSpawnY(serpentShamanReinforcementsSpawn != null ? serpentShamanReinforcementsSpawn.getY() : null)

			.minotaurReinforcementsSpawnX(minotaurReinforcementsSpawn != null ? minotaurReinforcementsSpawn.getX() : null)
			.minotaurReinforcementsSpawnY(minotaurReinforcementsSpawn != null ? minotaurReinforcementsSpawn.getY() : null)
			.build();
	}

	@Override
	public String toString()
	{
		Gson gson = new GsonBuilder()
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.create();

		return gson.toJson(this);
	}

	public static String csvHeader() {
		return "wave,status,accountName,tag,itemIds,itemNames,quantities,modifierChoice_I,modifierChoice_II,modifierChoice_III,chosenModifier,timeTaken,damageTaken,speedBonus,damageBonus,modifierGlory,completionBonus,waveGlory,totalGlory,serpentShamanSpawnX,serpentShamanSpawnY,javelinColossusSpawnAX,javelinColossusSpawnAY,javelinColossusSpawnBX,javelinColossusSpawnBY,manticoreSpawnAX,manticoreSpawnAY,manticoreSequenceA,manticoreSpawnBX,manticoreSpawnBY,manticoreSequenceB,shockwaveColossusSpawnAX,shockwaveColossusSpawnAY,shockwaveColossusSpawnBX,shockwaveColossusSpawnBY,jaguarWarriorReinfSpawnX,jaguarWarriorReinfSpawnY,serpentShamanReinfSpawnX,serpentShamanReinfSpawnY,minotaurReinfSpawnX,minotaurReinfSpawnY";
	}

	public String toCsvRow() {
		String itemIds = earnedLoot == null || earnedLoot.isEmpty() ? "-1" :
			earnedLoot.stream().map(i -> String.valueOf(i.getItemId())).collect(Collectors.joining("|"));

		String itemNames = earnedLoot == null || earnedLoot.isEmpty() ? "" :
			earnedLoot.stream().map(ItemBundle::getItemName).collect(Collectors.joining("|"));

		String quantities = earnedLoot == null || earnedLoot.isEmpty() ? "0" :
			earnedLoot.stream().map(i -> String.valueOf(i.getQuantity())).collect(Collectors.joining("|"));

		String modStatus = status != null ? status.name() : "UNKNOWN";
		String chosenMod = chosenModifier != null ? chosenModifier.name() : "";

		// Safely extract the 3 choices without throwing IndexOutOfBoundsExceptions
		String mod1 = modifierChoices != null && !modifierChoices.isEmpty() && modifierChoices.get(0) != null ? modifierChoices.get(0).name() : "";
		String mod2 = modifierChoices != null && modifierChoices.size() > 1 && modifierChoices.get(1) != null ? modifierChoices.get(1).name() : "";
		String mod3 = modifierChoices != null && modifierChoices.size() > 2 && modifierChoices.get(2) != null ? modifierChoices.get(2).name() : "";

		return String.join(",",
			String.valueOf(wave),
			modStatus,
			accountName,
			tag,
			itemIds,
			itemNames,
			quantities,
			mod1,
			mod2,
			mod3,
			chosenMod,
			String.format("%.1f", timeTaken),
			String.valueOf(damageTaken),
			String.valueOf(speedBonus),
			String.valueOf(damageBonus),
			String.valueOf(modifierGlory),
			String.valueOf(completionBonus),
			String.valueOf(waveGlory),
			String.valueOf(totalGlory),

			serpentShamanSpawn != null ? String.valueOf(serpentShamanSpawn.getX()) : "",
			serpentShamanSpawn != null ? String.valueOf(serpentShamanSpawn.getY()) : "",

			javelinColossusSpawnA != null ? String.valueOf(javelinColossusSpawnA.getX()) : "",
			javelinColossusSpawnA != null ? String.valueOf(javelinColossusSpawnA.getY()) : "",
			javelinColossusSpawnB != null ? String.valueOf(javelinColossusSpawnB.getX()) : "",
			javelinColossusSpawnB != null ? String.valueOf(javelinColossusSpawnB.getY()) : "",

			manticoreSpawnA != null ? String.valueOf(manticoreSpawnA.getX()) : "",
			manticoreSpawnA != null ? String.valueOf(manticoreSpawnA.getY()) : "",
			manticoreSequenceA != null ? manticoreSequenceA.toCsvFormat() : "",
			manticoreSpawnB != null ? String.valueOf(manticoreSpawnB.getX()) : "",
			manticoreSpawnB != null ? String.valueOf(manticoreSpawnB.getY()) : "",
			manticoreSequenceB != null ? manticoreSequenceB.toCsvFormat() : "",

			shockwaveColossusSpawnA != null ? String.valueOf(shockwaveColossusSpawnA.getX()) : "",
			shockwaveColossusSpawnA != null ? String.valueOf(shockwaveColossusSpawnA.getY()) : "",
			shockwaveColossusSpawnB != null ? String.valueOf(shockwaveColossusSpawnB.getX()) : "",
			shockwaveColossusSpawnB != null ? String.valueOf(shockwaveColossusSpawnB.getY()) : "",

			jaguarWarriorReinforcementsSpawn != null ? String.valueOf(jaguarWarriorReinforcementsSpawn.getX()) : "",
			jaguarWarriorReinforcementsSpawn != null ? String.valueOf(jaguarWarriorReinforcementsSpawn.getY()) : "",
			serpentShamanReinforcementsSpawn != null ? String.valueOf(serpentShamanReinforcementsSpawn.getX()) : "",
			serpentShamanReinforcementsSpawn != null ? String.valueOf(serpentShamanReinforcementsSpawn.getY()) : "",
			minotaurReinforcementsSpawn != null ? String.valueOf(minotaurReinforcementsSpawn.getX()) : "",
			minotaurReinforcementsSpawn != null ? String.valueOf(minotaurReinforcementsSpawn.getY()) : ""
		);
	}
}
