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

import lombok.Getter;
import static net.runelite.api.gameval.ItemID.*;

/**
 * An upgrade recipe: an existing potion is combined with an ingredient to produce a stronger variant with the
 * same amount of doses. The action can be performed on any of the 1-, 2-, 3- or 4-dose equivalents of the base
 * potion, and the ingredient consumption scales with the amount of doses of the base (a 4-dose base consumes
 * four times the per-dose ingredient amount).
 */
@Getter
public enum UpgradeRecipe implements BaseRecipe
{
	SUPER_ANTIFIRE(Potion.ANTIFIRE, CRUSHED_DRAGON_BONES, Potion.SUPER_ANTIFIRE),
	EXTENDED_SUPER_ANTIFIRE(Potion.EXTENDED_ANTIFIRE, CRUSHED_DRAGON_BONES, Potion.EXTENDED_SUPER_ANTIFIRE),
	EXTENDED_SUPER_ANTIFIRE_SHARD(Potion.SUPER_ANTIFIRE, LAVA_SHARD, Potion.EXTENDED_SUPER_ANTIFIRE),
	MIXTURE_STEP_1(Potion.SUPER_RESTORE, UNICORN_HORN_DUST, Potion.MIXTURE_STEP_1),
	SANFEW_SERUM(Potion.MIXTURE_STEP_2, NAIL_BEAST_NAIL, Potion.SANFEW_SERUM),
	STAMINA(Potion.SUPER_ENERGY, AMYLASE, Potion.STAMINA),
	EXTENDED_ANTIFIRE(Potion.ANTIFIRE, LAVA_SHARD, Potion.EXTENDED_ANTIFIRE),
	DIVINE_SUPER_ATTACK(Potion.SUPER_ATTACK, SOTE_CRYSTAL_DUST, Potion.DIVINE_SUPER_ATTACK),
	DIVINE_SUPER_STRENGTH(Potion.SUPER_STRENGTH, SOTE_CRYSTAL_DUST, Potion.DIVINE_SUPER_STRENGTH),
	DIVINE_SUPER_DEFENCE(Potion.SUPER_DEFENCE, SOTE_CRYSTAL_DUST, Potion.DIVINE_SUPER_DEFENCE),
	DIVINE_RANGING(Potion.RANGING, SOTE_CRYSTAL_DUST, Potion.DIVINE_RANGING),
	DIVINE_MAGIC(Potion.MAGIC, SOTE_CRYSTAL_DUST, Potion.DIVINE_MAGIC),
	DIVINE_SUPER_COMBAT(Potion.SUPER_COMBAT, SOTE_CRYSTAL_DUST, Potion.DIVINE_SUPER_COMBAT),
	DIVINE_BASTION(Potion.BASTION, SOTE_CRYSTAL_DUST, Potion.DIVINE_BASTION),
	DIVINE_BATTLEMAGE(Potion.BATTLEMAGE, SOTE_CRYSTAL_DUST, Potion.DIVINE_BATTLEMAGE),
	ANTI_VENOM(Potion.ANTIDOTE_PLUSPLUS, SNAKEBOSS_SCALE, Potion.ANTI_VENOM, 5),
	FORGOTTEN_BREW(Potion.ANCIENT_BREW, ANCIENT_ESSENCE, Potion.FORGOTTEN_BREW, 20),
	EXTENDED_ANTI_VENOM_PLUS(Potion.ANTI_VENOM_PLUS, ARAXYTE_VENOM_SACK, Potion.EXTENDED_ANTI_VENOM_PLUS),
	EXTREME_ENERGY(Potion.SUPER_ENERGY, YELLOW_FIN, Potion.EXTREME_ENERGY),
	EXTENDED_STAMINA(Potion.STAMINA, MARLIN_SCALES, Potion.EXTENDED_STAMINA),
	;

	private final Potion basePotion;
	private final int ingredientItemId;
	private final Potion producedPotion;
	private final int ingredientPerDose;

	UpgradeRecipe(Potion basePotion, int ingredientItemId, Potion producedPotion)
	{
		this(basePotion, ingredientItemId, producedPotion, 1);
	}

	UpgradeRecipe(Potion basePotion, int ingredientItemId, Potion producedPotion, int ingredientPerDose)
	{
		this.basePotion = basePotion;
		this.ingredientItemId = ingredientItemId;
		this.producedPotion = producedPotion;
		this.ingredientPerDose = ingredientPerDose;
	}

	@Override
	public int[] getBaseItemIds()
	{
		return basePotion.getDoseItemIds();
	}

	@Override
	public int getProducedItemId(int baseDoses)
	{
		return producedPotion.getDoseItemId(baseDoses);
	}

	@Override
	public int getIngredientQuantity(int baseDoses)
	{
		return ingredientPerDose * baseDoses;
	}
}
