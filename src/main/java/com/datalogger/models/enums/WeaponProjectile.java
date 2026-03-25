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

import lombok.Getter;
import net.runelite.api.gameval.ItemID;
import static net.runelite.api.gameval.SpotanimID.ARROW_VENATOR01_TRAVEL01;
import static net.runelite.api.gameval.SpotanimID.SPELLS_THAMMARON01_TRAVEL01;
import static net.runelite.api.gameval.SpotanimID.SP_ATTACK_GLOW_ARROW_TRAVEL;
import static net.runelite.api.gameval.SpotanimID.TUMEKENS_SHADOW_TRAVEL;
import static net.runelite.api.gameval.SpotanimID.WILD_CAVE_BOW_ARROW_TRAVEL;
import static net.runelite.api.gameval.SpotanimID.WILD_CAVE_BOW_ARROW_TRAVEL02;

/**
 * Enum with various projectiles that can be launched from a weapon, used to determine if a projectile is fired by the Player.
 * This class is restricted to projectiles mapped to a specific weapon, like a powered staff / Craw's bow.
 *
 */
@Getter
public enum WeaponProjectile
{

	TUMEKENS_SHADOW("Tumeken's shadow", ItemID.TUMEKENS_SHADOW, 2126, 5, false),
	SANGUINESTI_STAFF("Sanguinesti staff", ItemID.SANGUINESTI_STAFF, 1291, 4, false),
	BOW_OF_FAERDHINEN("Bow of faerdhinen", ItemID.BOW_OF_FAERDHINEN, 1938, 5, true),

	TRIDENT_OF_THE_SWAMP("Trident of the swamp", ItemID.TOXIC_TOTS_CHARGED, 1043, 4, false),
	TRIDENT_OF_THE_SEAS("Trident of the seas", ItemID.TOTS, 970, 4, false),
	WARPED_SCEPTRE("Warped sceptre", ItemID.WARPED_SCEPTRE, 2544, 4, false),

	WEBWEAVER_BOW("Webweaver bow", ItemID.WILD_CAVE_WEBWEAVER_CHARGED, WILD_CAVE_BOW_ARROW_TRAVEL02, 4, true),
	CRAWS_BOW("Craw's bow", ItemID.WILD_CAVE_BOW_CHARGED, WILD_CAVE_BOW_ARROW_TRAVEL, 4, true),
	ACCURSED_SCEPTRE("Accursed sceptre", ItemID.WILD_CAVE_ACCURSED_CHARGED, 2339, 4, false),
	THAMMARONS_SCEPTRE("Thammaron's sceptre", ItemID.WILD_CAVE_SCEPTRE_CHARGED, SPELLS_THAMMARON01_TRAVEL01, 4, false),

	CRYSTAL_BOW("Crystal bow", ItemID.CRYSTAL_BOW, SP_ATTACK_GLOW_ARROW_TRAVEL, 5, true),
	VENATOR_BOW("Venator bow", ItemID.VENATOR_BOW, ARROW_VENATOR01_TRAVEL01, 5, true);



	private final String name;
	private final int itemId;
	private final int projectileId;
	private final int attackSpeed;
	private final boolean hasRapidStyle;

	WeaponProjectile(String name, int itemId, int projectileId, int attackSpeed, boolean hasRapidStyle)
	{
		this.name = name;
		this.itemId = itemId;
		this.attackSpeed = attackSpeed;
		this.projectileId = projectileId;
		this.hasRapidStyle = hasRapidStyle;
	}

}
