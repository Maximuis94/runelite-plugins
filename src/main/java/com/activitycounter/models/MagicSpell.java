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

import static com.activitycounter.models.RuneElement.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.runelite.api.gameval.ItemID;


@Getter
public enum MagicSpell
{
	// --- STANDARD SPELLBOOK (Strikes) ---
	WIND_STRIKE(TrackedActivity.WIND_STRIKE, SpellBook.REGULAR, SpellType.COMBAT, 1, 5.5f, 220, new RuneStack(MIND, 1), new RuneStack(AIR, 1)),
	WATER_STRIKE(TrackedActivity.WATER_STRIKE, SpellBook.REGULAR, SpellType.COMBAT, 5, 7.5f, 211, new RuneStack(MIND, 1), new RuneStack(WATER, 1), new RuneStack(AIR, 1)),
	EARTH_STRIKE(TrackedActivity.EARTH_STRIKE, SpellBook.REGULAR, SpellType.COMBAT, 9, 9.5f, 132, new RuneStack(MIND, 1), new RuneStack(EARTH, 2), new RuneStack(AIR, 1)),
	FIRE_STRIKE(TrackedActivity.FIRE_STRIKE, SpellBook.REGULAR, SpellType.COMBAT, 13, 11.5f, 160, new RuneStack(MIND, 1), new RuneStack(FIRE, 3), new RuneStack(AIR, 2)),

	// --- STANDARD SPELLBOOK (Bolts) ---
	WIND_BOLT(TrackedActivity.WIND_BOLT, SpellBook.REGULAR, SpellType.COMBAT, 17, 13.5f, 218, new RuneStack(CHAOS, 1), new RuneStack(AIR, 2)),
	WATER_BOLT(TrackedActivity.WATER_BOLT, SpellBook.REGULAR, SpellType.COMBAT, 23, 16.5f, 209, new RuneStack(CHAOS, 1), new RuneStack(WATER, 2), new RuneStack(AIR, 2)),
	EARTH_BOLT(TrackedActivity.EARTH_BOLT, SpellBook.REGULAR, SpellType.COMBAT, 29, 19.5f, 130, new RuneStack(CHAOS, 1), new RuneStack(EARTH, 3), new RuneStack(AIR, 2)),
	FIRE_BOLT(TrackedActivity.FIRE_BOLT, SpellBook.REGULAR, SpellType.COMBAT, 35, 22.5f, 157, new RuneStack(CHAOS, 1), new RuneStack(FIRE, 4), new RuneStack(AIR, 3)),

	// --- STANDARD SPELLBOOK (Blasts) ---
	WIND_BLAST(TrackedActivity.WIND_BLAST, SpellBook.REGULAR, SpellType.COMBAT, 41, 25.5f, 216, new RuneStack(DEATH, 1), new RuneStack(AIR, 3)),
	WATER_BLAST(TrackedActivity.WATER_BLAST, SpellBook.REGULAR, SpellType.COMBAT, 47, 28.5f, 207, new RuneStack(DEATH, 1), new RuneStack(WATER, 3), new RuneStack(AIR, 3)),
	EARTH_BLAST(TrackedActivity.EARTH_BLAST, SpellBook.REGULAR, SpellType.COMBAT, 53, 31.5f, 128, new RuneStack(DEATH, 1), new RuneStack(EARTH, 4), new RuneStack(AIR, 3)),
	FIRE_BLAST(TrackedActivity.FIRE_BLAST, SpellBook.REGULAR, SpellType.COMBAT, 59, 34.5f, 155, new RuneStack(DEATH, 1), new RuneStack(FIRE, 5), new RuneStack(AIR, 4)),

	// --- STANDARD SPELLBOOK (Waves) ---
	WIND_WAVE(TrackedActivity.WIND_WAVE, SpellBook.REGULAR, SpellType.COMBAT, 62, 36.0f, 222, new RuneStack(BLOOD, 1), new RuneStack(AIR, 5)),
	WATER_WAVE(TrackedActivity.WATER_WAVE, SpellBook.REGULAR, SpellType.COMBAT, 65, 37.5f, 213, new RuneStack(BLOOD, 1), new RuneStack(WATER, 7), new RuneStack(AIR, 5)),
	EARTH_WAVE(TrackedActivity.EARTH_WAVE, SpellBook.REGULAR, SpellType.COMBAT, 70, 40.0f, 134, new RuneStack(BLOOD, 1), new RuneStack(EARTH, 7), new RuneStack(AIR, 5)),
	FIRE_WAVE(TrackedActivity.FIRE_WAVE, SpellBook.REGULAR, SpellType.COMBAT, 75, 42.5f, 162, new RuneStack(BLOOD, 1), new RuneStack(FIRE, 7), new RuneStack(AIR, 5)),

	// --- STANDARD SPELLBOOK (Surges) ---
	WIND_SURGE(TrackedActivity.WIND_SURGE, SpellBook.REGULAR, SpellType.COMBAT, 81, 75.0f, -1, new RuneStack[]{new RuneStack(WRATH, 1), new RuneStack(AIR, 7)}, SURGE_SACK),
	WATER_SURGE(TrackedActivity.WATER_SURGE, SpellBook.REGULAR, SpellType.COMBAT, 85, 80.0f, -1, new RuneStack[]{new RuneStack(WRATH, 1), new RuneStack(WATER, 10), new RuneStack(AIR, 7)}, SURGE_SACK),
	EARTH_SURGE(TrackedActivity.EARTH_SURGE, SpellBook.REGULAR, SpellType.COMBAT, 90, 85.0f, -1, new RuneStack[]{new RuneStack(WRATH, 1), new RuneStack(EARTH, 10), new RuneStack(AIR, 7)}, SURGE_SACK),
	FIRE_SURGE(TrackedActivity.FIRE_SURGE, SpellBook.REGULAR, SpellType.COMBAT, 95, 90.0f, -1, new RuneStack[]{new RuneStack(WRATH, 1), new RuneStack(FIRE, 10), new RuneStack(AIR, 7)}, SURGE_SACK),

	// --- STANDARD SPELLBOOK (Curses & Binds - Sack Enabled) ---
	CONFUSE(TrackedActivity.CONFUSE, SpellBook.REGULAR, SpellType.UTILITY, 3, 13.0f, 119, new RuneStack(BODY, 1), new RuneStack(WATER, 3), new RuneStack(EARTH, 2)),
	WEAKEN(TrackedActivity.WEAKEN, SpellBook.REGULAR, SpellType.UTILITY, 11, 21.0f, 215, new RuneStack(BODY, 1), new RuneStack(WATER, 3), new RuneStack(EARTH, 2)),
	CURSE(TrackedActivity.CURSE, SpellBook.REGULAR, SpellType.UTILITY, 19, 29.0f, 127, new RuneStack(BODY, 1), new RuneStack(WATER, 2), new RuneStack(EARTH, 3)),
	BIND(TrackedActivity.BIND, SpellBook.REGULAR, SpellType.COMBAT, 20, 30.0f, 101, new RuneStack[]{new RuneStack(NATURE, 2), new RuneStack(EARTH, 3), new RuneStack(WATER, 3)}, ENTANGLE_SACK),
	SNARE(TrackedActivity.SNARE, SpellBook.REGULAR, SpellType.COMBAT, 50, 60.0f, 186, new RuneStack[]{new RuneStack(NATURE, 3), new RuneStack(EARTH, 4), new RuneStack(WATER, 4)}, ENTANGLE_SACK),
	ENTANGLE(TrackedActivity.ENTANGLE, SpellBook.REGULAR, SpellType.COMBAT, 79, 89.0f, 151, new RuneStack[]{new RuneStack(NATURE, 4), new RuneStack(EARTH, 5), new RuneStack(WATER, 5)}, ENTANGLE_SACK),
	TELE_BLOCK(TrackedActivity.TELE_BLOCK, SpellBook.REGULAR, SpellType.COMBAT, 85, 80.0f, 202, new RuneStack(LAW, 1), new RuneStack(DEATH, 1), new RuneStack(CHAOS, 1)),
	VULNERABILITY(TrackedActivity.VULNERABILITY, SpellBook.REGULAR, SpellType.UTILITY, 66, 76.0f, 3009, new RuneStack(SOUL, 1), new RuneStack(EARTH, 5), new RuneStack(WATER, 5)),
	ENFEEBLE(TrackedActivity.ENFEEBLE, SpellBook.REGULAR, SpellType.UTILITY, 73, 83.0f, 148, new RuneStack(SOUL, 1), new RuneStack(EARTH, 5), new RuneStack(WATER, 5)),
	STUN(TrackedActivity.STUN, SpellBook.REGULAR, SpellType.UTILITY, 80, 90.0f, 3004, new RuneStack(SOUL, 1), new RuneStack(EARTH, 5), new RuneStack(WATER, 5)),

	// --- STANDARD SPELLBOOK (Enchantments) ---
	LVL_1_ENCHANT(TrackedActivity.LVL_1_ENCHANT, SpellBook.REGULAR, SpellType.UTILITY, 7, 17.5f, -1, new RuneStack(COSMIC, 1), new RuneStack(WATER, 1)),
	LVL_2_ENCHANT(TrackedActivity.LVL_2_ENCHANT, SpellBook.REGULAR, SpellType.UTILITY, 27, 37.0f, -1, new RuneStack(COSMIC, 1), new RuneStack(AIR, 3)),
	LVL_3_ENCHANT(TrackedActivity.LVL_3_ENCHANT, SpellBook.REGULAR, SpellType.UTILITY, 49, 59.0f, -1, new RuneStack(COSMIC, 1), new RuneStack(FIRE, 5)),
	LVL_4_ENCHANT(TrackedActivity.LVL_4_ENCHANT, SpellBook.REGULAR, SpellType.UTILITY, 57, 67.0f, -1, new RuneStack(COSMIC, 1), new RuneStack(EARTH, 10)),
	LVL_5_ENCHANT(TrackedActivity.LVL_5_ENCHANT, SpellBook.REGULAR, SpellType.UTILITY, 68, 78.0f, -1, new RuneStack(COSMIC, 1), new RuneStack(EARTH, 15), new RuneStack(WATER, 15)),
	LVL_6_ENCHANT(TrackedActivity.LVL_6_ENCHANT, SpellBook.REGULAR, SpellType.UTILITY, 87, 97.0f, -1, new RuneStack(COSMIC, 1), new RuneStack(EARTH, 20), new RuneStack(FIRE, 20)),
	LVL_7_ENCHANT(TrackedActivity.LVL_7_ENCHANT, SpellBook.REGULAR, SpellType.UTILITY, 93, 110.0f, -1, new RuneStack(COSMIC, 1), new RuneStack(BLOOD, 20), new RuneStack(SOUL, 20)),

	// --- STANDARD SPELLBOOK (Bolt Enchantments) ---
	ENCHANT_CROSSBOW_BOLT_OPAL(TrackedActivity.ENCHANT_CROSSBOW_BOLT_OPAL, SpellBook.REGULAR, SpellType.UTILITY, 4, 9.0f, -1, new RuneStack(COSMIC, 1), new RuneStack(AIR, 2)),
	ENCHANT_CROSSBOW_BOLT_SAPPHIRE(TrackedActivity.ENCHANT_CROSSBOW_BOLT_SAPPHIRE, SpellBook.REGULAR, SpellType.UTILITY, 7, 17.0f, -1, new RuneStack(COSMIC, 1), new RuneStack(WATER, 1), new RuneStack(MIND, 1)),
	ENCHANT_CROSSBOW_BOLT_JADE(TrackedActivity.ENCHANT_CROSSBOW_BOLT_JADE, SpellBook.REGULAR, SpellType.UTILITY, 14, 19.0f, -1, new RuneStack(COSMIC, 1), new RuneStack(EARTH, 2)),
	ENCHANT_CROSSBOW_BOLT_PEARL(TrackedActivity.ENCHANT_CROSSBOW_BOLT_PEARL, SpellBook.REGULAR, SpellType.UTILITY, 24, 29.0f, -1, new RuneStack(COSMIC, 1), new RuneStack(WATER, 2)),
	ENCHANT_CROSSBOW_BOLT_EMERALD(TrackedActivity.ENCHANT_CROSSBOW_BOLT_EMERALD, SpellBook.REGULAR, SpellType.UTILITY, 27, 37.0f, -1, new RuneStack(COSMIC, 1), new RuneStack(AIR, 3), new RuneStack(NATURE, 1)),
	ENCHANT_CROSSBOW_BOLT_RED_TOPAZ(TrackedActivity.ENCHANT_CROSSBOW_BOLT_RED_TOPAZ, SpellBook.REGULAR, SpellType.UTILITY, 29, 33.0f, -1, new RuneStack(COSMIC, 1), new RuneStack(FIRE, 2)),
	ENCHANT_CROSSBOW_BOLT_RUBY(TrackedActivity.ENCHANT_CROSSBOW_BOLT_RUBY, SpellBook.REGULAR, SpellType.UTILITY, 49, 59.0f, -1, new RuneStack(COSMIC, 1), new RuneStack(FIRE, 5), new RuneStack(BLOOD, 1)),
	ENCHANT_CROSSBOW_BOLT_DIAMOND(TrackedActivity.ENCHANT_CROSSBOW_BOLT_DIAMOND, SpellBook.REGULAR, SpellType.UTILITY, 57, 67.0f, -1, new RuneStack(COSMIC, 1), new RuneStack(EARTH, 10), new RuneStack(LAW, 2)),
	ENCHANT_CROSSBOW_BOLT_DRAGONSTONE(TrackedActivity.ENCHANT_CROSSBOW_BOLT_DRAGONSTONE, SpellBook.REGULAR, SpellType.UTILITY, 68, 78.0f, -1, new RuneStack(COSMIC, 1), new RuneStack(EARTH, 15), new RuneStack(SOUL, 1)),
	ENCHANT_CROSSBOW_BOLT_ONYX(TrackedActivity.ENCHANT_CROSSBOW_BOLT_ONYX, SpellBook.REGULAR, SpellType.UTILITY, 87, 97.0f, -1, new RuneStack(COSMIC, 1), new RuneStack(FIRE, 20), new RuneStack(DEATH, 1)),



	// --- STANDARD SPELLBOOK (Utility) ---
	LOW_LEVEL_ALCHEMY(TrackedActivity.LOW_LEVEL_ALCHEMY, SpellBook.REGULAR, SpellType.UTILITY, 21, 31.0f, 98, new RuneStack(FIRE, 3), new RuneStack(NATURE, 1)),
	HIGH_LEVEL_ALCHEMY(TrackedActivity.HIGH_LEVEL_ALCHEMY, SpellBook.REGULAR, SpellType.UTILITY, 55, 65.0f, 97, new RuneStack(FIRE, 5), new RuneStack(NATURE, 1)),
	CRUMBLE_UNDEAD(TrackedActivity.CRUMBLE_UNDEAD, SpellBook.REGULAR, SpellType.COMBAT, 39, 24.5f, 122, new RuneStack(CHAOS, 1), new RuneStack(EARTH, 2), new RuneStack(AIR, 2)),
	MONSTER_INSPECT(TrackedActivity.MONSTER_INSPECT, SpellBook.REGULAR, SpellType.UTILITY, 42, 30.5f, -1, new RuneStack(BODY, 2), new RuneStack(MIND, 2)),
	SUPERHEAT_ITEM(TrackedActivity.SUPERHEAT_ITEM, SpellBook.REGULAR, SpellType.UTILITY, 43, 53.0f, -1, new RuneStack(NATURE, 1), new RuneStack(FIRE, 4)),
	BONES_TO_BANANAS(TrackedActivity.BONES_TO_BANANAS, SpellBook.REGULAR, SpellType.UTILITY, 15, 25.0f, -1, new RuneStack(NATURE, 1), new RuneStack(WATER, 2), new RuneStack(EARTH, 2)),
	TELEKINETIC_GRAB(TrackedActivity.TELEKINETIC_GRAB, SpellBook.REGULAR, SpellType.UTILITY, 33, 43.0f, -1, new RuneStack(LAW, 1), new RuneStack(AIR, 1)),
	BONES_TO_PEACHES(TrackedActivity.BONES_TO_PEACHES, SpellBook.REGULAR, SpellType.UTILITY, 60, 35.5f, -1, new RuneStack(NATURE, 2), new RuneStack(WATER, 4), new RuneStack(EARTH, 4)),
	CHARGE_WATER_ORB(TrackedActivity.CHARGE_WATER_ORB, SpellBook.REGULAR, SpellType.UTILITY, 56, 76.0f, 118, new RuneStack(COSMIC, 3), new RuneStack(WATER, 30)),
	CHARGE_EARTH_ORB(TrackedActivity.CHARGE_EARTH_ORB, SpellBook.REGULAR, SpellType.UTILITY, 60, 81.0f, 115, new RuneStack(COSMIC, 3), new RuneStack(EARTH, 30)),
	CHARGE_FIRE_ORB(TrackedActivity.CHARGE_FIRE_ORB, SpellBook.REGULAR, SpellType.UTILITY, 63, 73.0f, 117, new RuneStack(COSMIC, 3), new RuneStack(FIRE, 30)),
	CHARGE_AIR_ORB(TrackedActivity.CHARGE_AIR_ORB, SpellBook.REGULAR, SpellType.UTILITY, 66, 76.0f, 116, new RuneStack(COSMIC, 3), new RuneStack(AIR, 30)),
	CHARGE(TrackedActivity.CHARGE, SpellBook.REGULAR, SpellType.UTILITY, 80, 180.0f, -1, new RuneStack(BLOOD, 3), new RuneStack(FIRE, 3), new RuneStack(AIR, 3)),

	// --- STANDARD SPELLBOOK (God Spells & Unique Combat) ---
	MAGIC_DART(TrackedActivity.MAGIC_DART, SpellBook.REGULAR, SpellType.COMBAT, 50, 30.0f, -1, new RuneStack[]{new RuneStack(DEATH, 1), new RuneStack(MIND, 4)}, ItemID.SLAYER_STAFF, ItemID.SLAYER_STAFF_ENCHANTED, ItemID.STAFF_OF_BALANCE, ItemID.STAFF_OF_LIGHT, ItemID.SOTD, ItemID.TOXIC_SOTD, ItemID.TOXIC_SOTD_CHARGED),
	IBAN_BLAST(TrackedActivity.IBAN_BLAST, SpellBook.REGULAR, SpellType.COMBAT, 50, 30.0f, -1, new RuneStack[]{new RuneStack(DEATH, 1), new RuneStack(FIRE, 5)}, ItemID.IBANSTAFF, ItemID.IBAN_STAFF_ORN, ItemID.IBAN_STAFF_UPGRADED_ORN, ItemID.IBANSTAFF_UPGRADED),
	CLAWS_OF_GUTHIX(TrackedActivity.CLAWS_OF_GUTHIX, SpellBook.REGULAR, SpellType.COMBAT, 60, 35.0f, -1, new RuneStack[]{new RuneStack(BLOOD, 2), new RuneStack(FIRE, 1), new RuneStack(AIR, 4)}, ItemID.PEST_VOID_KNIGHT_MACE, ItemID.STAFF_OF_BALANCE, ItemID.GUTHIX_STAFF),
	FLAMES_OF_ZAMORAK(TrackedActivity.FLAMES_OF_ZAMORAK, SpellBook.REGULAR, SpellType.COMBAT, 60, 35.0f, -1, new RuneStack[]{new RuneStack(BLOOD, 2), new RuneStack(FIRE, 4), new RuneStack(AIR, 1)}, ItemID.SOTD, ItemID.TOXIC_SOTD_CHARGED, ItemID.TOXIC_SOTD, ItemID.ZAMORAK_STAFF),
	SARADOMIN_STRIKE(TrackedActivity.SARADOMIN_STRIKE, SpellBook.REGULAR, SpellType.COMBAT, 60, 35.0f, -1, new RuneStack[]{new RuneStack(BLOOD, 2), new RuneStack(FIRE, 2), new RuneStack(AIR, 4)}, ItemID.SARADOMIN_STAFF, ItemID.STAFF_OF_LIGHT),
	TELEBLOCK(TrackedActivity.TELE_BLOCK, SpellBook.REGULAR, SpellType.COMBAT, 85, 83.6f, 197, new RuneStack[]{new RuneStack(CHAOS, 1), new RuneStack(DEATH, 1), new RuneStack(LAW, 1)}, TELEPORT_SPELL_SACK),

	// --- ANCIENT MAGICKS (Ice Spells - Sack Enabled) ---
	ICE_RUSH(TrackedActivity.ICE_RUSH, SpellBook.ANCIENT, SpellType.COMBAT, 58, 34.0f, 173, new RuneStack[]{new RuneStack(DEATH, 2), new RuneStack(CHAOS, 2), new RuneStack(WATER, 2)}, ICE_SACK),
	ICE_BURST(TrackedActivity.ICE_BURST, SpellBook.ANCIENT, SpellType.COMBAT, 70, 40.0f, 170, new RuneStack[]{new RuneStack(DEATH, 2), new RuneStack(CHAOS, 4), new RuneStack(WATER, 4)}, ICE_SACK),
	ICE_BLITZ(TrackedActivity.ICE_BLITZ, SpellBook.ANCIENT, SpellType.COMBAT, 82, 46.0f, 169, new RuneStack[]{new RuneStack(BLOOD, 2), new RuneStack(DEATH, 2), new RuneStack(WATER, 3)}, ICE_SACK),
	ICE_BARRAGE(TrackedActivity.ICE_BARRAGE, SpellBook.ANCIENT, SpellType.COMBAT, 94, 52.0f, 168, new RuneStack[]{new RuneStack(BLOOD, 2), new RuneStack(DEATH, 4), new RuneStack(WATER, 6)}, ICE_SACK),

	// --- ANCIENT MAGICKS (Blood Spells) ---
	BLOOD_RUSH(TrackedActivity.BLOOD_RUSH, SpellBook.ANCIENT, SpellType.COMBAT, 56, 33.0f, 110, new RuneStack(BLOOD, 1), new RuneStack(DEATH, 2), new RuneStack(CHAOS, 2)),
	BLOOD_BURST(TrackedActivity.BLOOD_BURST, SpellBook.ANCIENT, SpellType.COMBAT, 68, 39.0f, 105, new RuneStack(BLOOD, 2), new RuneStack(DEATH, 2), new RuneStack(CHAOS, 4)),
	BLOOD_BLITZ(TrackedActivity.BLOOD_BLITZ, SpellBook.ANCIENT, SpellType.COMBAT, 80, 45.0f, 104, new RuneStack(BLOOD, 4), new RuneStack(DEATH, 2)),
	BLOOD_BARRAGE(TrackedActivity.BLOOD_BARRAGE, SpellBook.ANCIENT, SpellType.COMBAT, 92, 51.0f, 102, new RuneStack(SOUL, 1), new RuneStack(BLOOD, 4), new RuneStack(DEATH, 4)),

	// --- ANCIENT MAGICKS (Smoke Spells) ---
	SMOKE_RUSH(TrackedActivity.SMOKE_RUSH, SpellBook.ANCIENT, SpellType.COMBAT, 50, 30.0f, 185, new RuneStack(DEATH, 2), new RuneStack(CHAOS, 2), new RuneStack(FIRE, 1), new RuneStack(AIR, 1)),
	SMOKE_BURST(TrackedActivity.SMOKE_BURST, SpellBook.ANCIENT, SpellType.COMBAT, 62, 36.0f, 182, new RuneStack(DEATH, 2), new RuneStack(CHAOS, 4), new RuneStack(FIRE, 2), new RuneStack(AIR, 2)),
	SMOKE_BLITZ(TrackedActivity.SMOKE_BLITZ, SpellBook.ANCIENT, SpellType.COMBAT, 74, 42.0f, 181, new RuneStack(BLOOD, 2), new RuneStack(DEATH, 2), new RuneStack(FIRE, 2), new RuneStack(AIR, 2)),
	SMOKE_BARRAGE(TrackedActivity.SMOKE_BARRAGE, SpellBook.ANCIENT, SpellType.COMBAT, 86, 48.0f, 180, new RuneStack(BLOOD, 2), new RuneStack(DEATH, 4), new RuneStack(FIRE, 4), new RuneStack(AIR, 4)),

	// --- ANCIENT MAGICKS (Shadow Spells) ---
	SHADOW_RUSH(TrackedActivity.SHADOW_RUSH, SpellBook.ANCIENT, SpellType.COMBAT, 52, 31.0f, 179, new RuneStack(DEATH, 2), new RuneStack(CHAOS, 2), new RuneStack(SOUL, 1), new RuneStack(AIR, 1)),
	SHADOW_BURST(TrackedActivity.SHADOW_BURST, SpellBook.ANCIENT, SpellType.COMBAT, 64, 37.0f, 177, new RuneStack(DEATH, 2), new RuneStack(CHAOS, 4), new RuneStack(SOUL, 2), new RuneStack(AIR, 2)),
	SHADOW_BLITZ(TrackedActivity.SHADOW_BLITZ, SpellBook.ANCIENT, SpellType.COMBAT, 76, 43.0f, 176, new RuneStack(BLOOD, 2), new RuneStack(DEATH, 2), new RuneStack(SOUL, 2), new RuneStack(AIR, 2)),
	SHADOW_BARRAGE(TrackedActivity.SHADOW_BARRAGE, SpellBook.ANCIENT, SpellType.COMBAT, 88, 49.0f, 175, new RuneStack(BLOOD, 2), new RuneStack(DEATH, 4), new RuneStack(SOUL, 3), new RuneStack(AIR, 4)),

	// --- ANCIENT MAGICKS (Teleports) ---
	PADDEWWA_TELEPORT(TrackedActivity.PADDEWWA_TELEPORT, SpellBook.ANCIENT, SpellType.TELEPORT, 54, 64.0f, -1, new RuneStack(LAW, 2), new RuneStack(FIRE, 1), new RuneStack(AIR, 1)),
	SENNTISTEN_TELEPORT(TrackedActivity.SENNTISTEN_TELEPORT, SpellBook.ANCIENT, SpellType.TELEPORT, 60, 70.0f, -1, new RuneStack(LAW, 2), new RuneStack(SOUL, 1)),
	KHARYRLL_TELEPORT(TrackedActivity.KHARYRLL_TELEPORT, SpellBook.ANCIENT, SpellType.TELEPORT, 66, 76.0f, -1, new RuneStack(LAW, 2), new RuneStack(BLOOD, 1)),
	LASSAR_TELEPORT(TrackedActivity.LASSAR_TELEPORT, SpellBook.ANCIENT, SpellType.TELEPORT, 72, 82.0f, -1, new RuneStack(LAW, 2), new RuneStack(WATER, 4)),
	DAREEYAK_TELEPORT(TrackedActivity.DAREEYAK_TELEPORT, SpellBook.ANCIENT, SpellType.TELEPORT, 78, 88.0f, -1, new RuneStack(LAW, 2), new RuneStack(FIRE, 3), new RuneStack(AIR, 2)),
	CARRALLANGER_TELEPORT(TrackedActivity.CARRALLANGER_TELEPORT, SpellBook.ANCIENT, SpellType.TELEPORT, 84, 94.0f, -1, new RuneStack(LAW, 2), new RuneStack(SOUL, 2)),
	ANCIENT_TELEPORT_TO_TARGET(TrackedActivity.ANCIENT_TELEPORT_TO_TARGET, SpellBook.ANCIENT, SpellType.TELEPORT, 85, 45.0f, -1, new RuneStack(LAW, 1), new RuneStack(DEATH, 1), new RuneStack(CHAOS, 1)),
	ANNAKARL_TELEPORT(TrackedActivity.ANNAKARL_TELEPORT, SpellBook.ANCIENT, SpellType.TELEPORT, 90, 100.0f, -1, new RuneStack(LAW, 2), new RuneStack(BLOOD, 2)),
	GHORROCK_TELEPORT(TrackedActivity.GHORROCK_TELEPORT, SpellBook.ANCIENT, SpellType.TELEPORT, 96, 106.0f, -1, new RuneStack(LAW, 2), new RuneStack(WATER, 8)),

	// --- STANDARD SPELLBOOK (Teleports) ---
	VARROCK_TELEPORT(TrackedActivity.VARROCK_TELEPORT, SpellBook.REGULAR, SpellType.TELEPORT, 25, 35.0f, -1, new RuneStack(LAW, 1), new RuneStack(FIRE, 1), new RuneStack(AIR, 3)),
	LUMBRIDGE_TELEPORT(TrackedActivity.LUMBRIDGE_TELEPORT, SpellBook.REGULAR, SpellType.TELEPORT, 31, 41.0f, -1, new RuneStack(LAW, 1), new RuneStack(EARTH, 1), new RuneStack(AIR, 3)),
	FALADOR_TELEPORT(TrackedActivity.FALADOR_TELEPORT, SpellBook.REGULAR, SpellType.TELEPORT, 37, 48.0f, -1, new RuneStack(LAW, 1), new RuneStack(WATER, 1), new RuneStack(AIR, 3)),
	CAMELOT_TELEPORT(TrackedActivity.CAMELOT_TELEPORT, SpellBook.REGULAR, SpellType.TELEPORT, 45, 55.5f, -1, new RuneStack(LAW, 1), new RuneStack(AIR, 5)),
	ARDOUGNE_TELEPORT(TrackedActivity.ARDOUGNE_TELEPORT, SpellBook.REGULAR, SpellType.TELEPORT, 51, 61.0f, -1, new RuneStack(LAW, 2), new RuneStack(WATER, 2)),
	TELEPORT_TO_HOUSE(TrackedActivity.TELEPORT_TO_HOUSE, SpellBook.REGULAR, SpellType.TELEPORT, 40, 30.0f, -1, new RuneStack(LAW, 1), new RuneStack(AIR, 1), new RuneStack(EARTH, 1)),
	CIVITAS_ILLA_FORTIS_TELEPORT(TrackedActivity.CIVITAS_ILLA_FORTIS_TELEPORT, SpellBook.REGULAR, SpellType.TELEPORT, 54, 64.0f, -1, new RuneStack(LAW, 2), new RuneStack(EARTH, 1), new RuneStack(FIRE, 1)),
	WATCHTOWER_TELEPORT(TrackedActivity.WATCHTOWER_TELEPORT, SpellBook.REGULAR, SpellType.TELEPORT, 58, 68.0f, -1, new RuneStack(LAW, 2), new RuneStack(EARTH, 2)),
	TROLLHEIM_TELEPORT(TrackedActivity.TROLLHEIM_TELEPORT, SpellBook.REGULAR, SpellType.TELEPORT, 61, 68.0f, -1, new RuneStack(LAW, 2), new RuneStack(FIRE, 2)),
	REGULAR_APE_ATOLL_TELEPORT(TrackedActivity.REGULAR_APE_ATOLL_TELEPORT, SpellBook.REGULAR, SpellType.TELEPORT, 64, 74.0f, -1, new RuneStack(LAW, 2), new RuneStack(FIRE, 2), new RuneStack(WATER, 2)),
	KOUREND_CASTLE_TELEPORT(TrackedActivity.KOUREND_CASTLE_TELEPORT, SpellBook.REGULAR, SpellType.TELEPORT, 69, 82.0f, -1, new RuneStack(LAW, 2), new RuneStack(SOUL, 2), new RuneStack(WATER, 4), new RuneStack(FIRE, 5)),
	TELEOTHER_LUMBRIDGE(TrackedActivity.TELEOTHER_LUMBRIDGE, SpellBook.REGULAR, SpellType.TELEPORT, 74, 84.0f, -1, new RuneStack(LAW, 1), new RuneStack(SOUL, 1), new RuneStack(EARTH, 1)),
	TELEOTHER_FALADOR(TrackedActivity.TELEOTHER_FALADOR, SpellBook.REGULAR, SpellType.TELEPORT, 82, 92.0f, -1, new RuneStack(LAW, 1), new RuneStack(SOUL, 1), new RuneStack(WATER, 1)),
	REGULAR_TELEPORT_TO_TARGET(TrackedActivity.REGULAR_TELEPORT_TO_TARGET, SpellBook.REGULAR, SpellType.TELEPORT, 85, 45.0f, -1, new RuneStack(LAW, 1), new RuneStack(DEATH, 1), new RuneStack(CHAOS, 1)),
	TELEOTHER_CAMELOT(TrackedActivity.TELEOTHER_CAMELOT, SpellBook.REGULAR, SpellType.TELEPORT, 90, 100.0f, -1, new RuneStack(LAW, 1), new RuneStack(SOUL, 2)),

	// --- LUNAR SPELLBOOK (Teleports) ---
	MOONCLAN_TELEPORT(TrackedActivity.MOONCLAN_TELEPORT, SpellBook.LUNAR, SpellType.TELEPORT, 69, 66.0f, -1, new RuneStack(EARTH, 2), new RuneStack(ASTRAL, 2), new RuneStack(LAW, 1)),
	TELE_GROUP_MOONCLAN(TrackedActivity.TELE_GROUP_MOONCLAN, SpellBook.LUNAR, SpellType.TELEPORT, 70, 67.0f, -1, new RuneStack(EARTH, 4), new RuneStack(ASTRAL, 2), new RuneStack(LAW, 1)),
	OURANIA_TELEPORT(TrackedActivity.OURANIA_TELEPORT, SpellBook.LUNAR, SpellType.TELEPORT, 71, 69.0f, -1, new RuneStack(EARTH, 6), new RuneStack(ASTRAL, 2), new RuneStack(LAW, 1)),
	WATERBIRTH_TELEPORT(TrackedActivity.WATERBIRTH_TELEPORT, SpellBook.LUNAR, SpellType.TELEPORT, 72, 71.0f, -1, new RuneStack(WATER, 1), new RuneStack(ASTRAL, 2), new RuneStack(LAW, 1)),
	TELE_GROUP_WATERBIRTH(TrackedActivity.TELE_GROUP_WATERBIRTH, SpellBook.LUNAR, SpellType.TELEPORT, 73, 72.0f, -1, new RuneStack(WATER, 5), new RuneStack(ASTRAL, 2), new RuneStack(LAW, 1)),
	BARBARIAN_TELEPORT(TrackedActivity.BARBARIAN_TELEPORT, SpellBook.LUNAR, SpellType.TELEPORT, 75, 76.0f, -1, new RuneStack(FIRE, 3), new RuneStack(ASTRAL, 2), new RuneStack(LAW, 2)),
	TELE_GROUP_BARBARIAN(TrackedActivity.TELE_GROUP_BARBARIAN, SpellBook.LUNAR, SpellType.TELEPORT, 76, 77.0f, -1, new RuneStack(FIRE, 6), new RuneStack(ASTRAL, 2), new RuneStack(LAW, 2)),
	KHAZARD_TELEPORT(TrackedActivity.KHAZARD_TELEPORT, SpellBook.LUNAR, SpellType.TELEPORT, 78, 80.0f, -1, new RuneStack(WATER, 4), new RuneStack(ASTRAL, 2), new RuneStack(LAW, 2)),
	TELE_GROUP_KHAZARD(TrackedActivity.TELE_GROUP_KHAZARD, SpellBook.LUNAR, SpellType.TELEPORT, 79, 81.0f, -1, new RuneStack(WATER, 8), new RuneStack(ASTRAL, 2), new RuneStack(LAW, 2)),
	FISHING_GUILD_TELEPORT(TrackedActivity.FISHING_GUILD_TELEPORT, SpellBook.LUNAR, SpellType.TELEPORT, 85, 89.0f, -1, new RuneStack(WATER, 10), new RuneStack(ASTRAL, 3), new RuneStack(LAW, 3)),
	LUNAR_TELEPORT_TO_TARGET(TrackedActivity.LUNAR_TELEPORT_TO_TARGET, SpellBook.LUNAR, SpellType.TELEPORT, 85, 45.0f, -1, new RuneStack(CHAOS, 1), new RuneStack(DEATH, 1), new RuneStack(LAW, 1)),
	TELE_GROUP_FISHING_GUILD(TrackedActivity.TELE_GROUP_FISHING_GUILD, SpellBook.LUNAR, SpellType.TELEPORT, 86, 90.0f, -1, new RuneStack(WATER, 14), new RuneStack(ASTRAL, 3), new RuneStack(LAW, 3)),
	CATHERBY_TELEPORT(TrackedActivity.CATHERBY_TELEPORT, SpellBook.LUNAR, SpellType.TELEPORT, 87, 92.0f, -1, new RuneStack(WATER, 10), new RuneStack(ASTRAL, 3), new RuneStack(LAW, 3)),
	TELE_GROUP_CATHERBY(TrackedActivity.TELE_GROUP_CATHERBY, SpellBook.LUNAR, SpellType.TELEPORT, 88, 93.0f, -1, new RuneStack(WATER, 15), new RuneStack(ASTRAL, 3), new RuneStack(LAW, 3)),
	ICE_PLATEAU_TELEPORT(TrackedActivity.ICE_PLATEAU_TELEPORT, SpellBook.LUNAR, SpellType.TELEPORT, 89, 96.0f, -1, new RuneStack(WATER, 8), new RuneStack(ASTRAL, 3), new RuneStack(LAW, 3)),
	TELE_GROUP_ICE_PLATEAU(TrackedActivity.TELE_GROUP_ICE_PLATEAU, SpellBook.LUNAR, SpellType.TELEPORT, 90, 99.0f, -1, new RuneStack(WATER, 16), new RuneStack(ASTRAL, 3), new RuneStack(LAW, 3)),

	// --- ARCEUUS SPELLBOOK (Teleports) ---
	ARCEUUS_LIBRARY_TELEPORT(TrackedActivity.ARCEUUS_LIBRARY_TELEPORT, SpellBook.ARCEEUS, SpellType.TELEPORT, 9, 17.0f, -1, new RuneStack(EARTH, 2), new RuneStack(LAW, 1)),
	DRAYNOR_MANOR_TELEPORT(TrackedActivity.DRAYNOR_MANOR_TELEPORT, SpellBook.ARCEEUS, SpellType.TELEPORT, 17, 16.0f, -1, new RuneStack(LAW, 1), new RuneStack(WATER, 1), new RuneStack(EARTH, 1)),
	BATTLEFRONT_TELEPORT(TrackedActivity.BATTLEFRONT_TELEPORT, SpellBook.ARCEEUS, SpellType.TELEPORT, 23, 19.0f, -1, new RuneStack(LAW, 1), new RuneStack(EARTH, 1), new RuneStack(FIRE, 1)),
	MIND_ALTAR_TELEPORT(TrackedActivity.MIND_ALTAR_TELEPORT, SpellBook.ARCEEUS, SpellType.TELEPORT, 28, 22.0f, -1, new RuneStack(LAW, 1), new RuneStack(MIND, 2)),
	RESPAWN_TELEPORT(TrackedActivity.RESPAWN_TELEPORT, SpellBook.ARCEEUS, SpellType.TELEPORT, 34, 27.0f, -1, new RuneStack(LAW, 1), new RuneStack(SOUL, 1)),
	SALVE_GRAVEYARD_TELEPORT(TrackedActivity.SALVE_GRAVEYARD_TELEPORT, SpellBook.ARCEEUS, SpellType.TELEPORT, 40, 30.0f, -1, new RuneStack(LAW, 1), new RuneStack(SOUL, 2)),
	FENKENSTRAIN_S_CASTLE_TELEPORT(TrackedActivity.FENKENSTRAIN_S_CASTLE_TELEPORT, SpellBook.ARCEEUS, SpellType.TELEPORT, 48, 50.0f, -1, new RuneStack(LAW, 1), new RuneStack(SOUL, 1), new RuneStack(EARTH, 1)),
	WEST_ARDOUGNE_TELEPORT(TrackedActivity.WEST_ARDOUGNE_TELEPORT, SpellBook.ARCEEUS, SpellType.TELEPORT, 61, 68.0f, -1, new RuneStack(LAW, 2), new RuneStack(SOUL, 2)),
	HARMONY_ISLAND_TELEPORT(TrackedActivity.HARMONY_ISLAND_TELEPORT, SpellBook.ARCEEUS, SpellType.TELEPORT, 65, 74.0f, -1, new RuneStack(LAW, 1), new RuneStack(SOUL, 1), new RuneStack(NATURE, 1)),
	CEMETERY_TELEPORT(TrackedActivity.CEMETERY_TELEPORT, SpellBook.ARCEEUS, SpellType.TELEPORT, 71, 82.0f, -1, new RuneStack(LAW, 1), new RuneStack(SOUL, 1), new RuneStack(BLOOD, 1)),
	BARROWS_TELEPORT(TrackedActivity.BARROWS_TELEPORT, SpellBook.ARCEEUS, SpellType.TELEPORT, 83, 90.0f, -1, new RuneStack(LAW, 2), new RuneStack(SOUL, 2), new RuneStack(BLOOD, 1)),
	ARCEUUS_TELEPORT_TO_TARGET(TrackedActivity.ARCEUUS_TELEPORT_TO_TARGET, SpellBook.ARCEEUS, SpellType.TELEPORT, 85, 45.0f, -1, new RuneStack(LAW, 1), new RuneStack(DEATH, 1), new RuneStack(CHAOS, 1)),
	ARCEUUS_APE_ATOLL_TELEPORT(TrackedActivity.ARCEUUS_APE_ATOLL_TELEPORT, SpellBook.ARCEEUS, SpellType.TELEPORT, 90, 100.0f, -1, new RuneStack(LAW, 2), new RuneStack(SOUL, 2), new RuneStack(BLOOD, 2)),

	// --- LUNAR SPELLBOOK (Utility & Skilling) ---
	BAKE_PIE(TrackedActivity.BAKE_PIE, SpellBook.LUNAR, SpellType.UTILITY, 65, 60.0f, -1, new RuneStack(FIRE, 5), new RuneStack(WATER, 4), new RuneStack(ASTRAL, 1)),
	GEOMANCY(TrackedActivity.GEOMANCY, SpellBook.LUNAR, SpellType.UTILITY, 65, 60.0f, -1, new RuneStack(EARTH, 8), new RuneStack(ASTRAL, 3), new RuneStack(NATURE, 3)),
	CURE_PLANT(TrackedActivity.CURE_PLANT, SpellBook.LUNAR, SpellType.UTILITY, 66, 60.0f, -1, new RuneStack(EARTH, 8), new RuneStack(ASTRAL, 1)),
	MONSTER_EXAMINE(TrackedActivity.MONSTER_EXAMINE, SpellBook.LUNAR, SpellType.UTILITY, 66, 61.0f, -1, new RuneStack(ASTRAL, 1), new RuneStack(COSMIC, 1), new RuneStack(MIND, 1)),
	ASTRAL_CONTACT(TrackedActivity.ASTRAL_CONTACT, SpellBook.LUNAR, SpellType.UTILITY, 67, 63.0f, -1, new RuneStack(AIR, 2), new RuneStack(ASTRAL, 1), new RuneStack(COSMIC, 1)),
	CURE_OTHER(TrackedActivity.CURE_OTHER, SpellBook.LUNAR, SpellType.UTILITY, 68, 65.0f, -1, new RuneStack(EARTH, 10), new RuneStack(ASTRAL, 1), new RuneStack(LAW, 1)),
	HUMIDIFY(TrackedActivity.HUMIDIFY, SpellBook.LUNAR, SpellType.UTILITY, 68, 65.0f, -1, new RuneStack(FIRE, 1), new RuneStack(WATER, 3), new RuneStack(ASTRAL, 1)),
	HUNTER_KIT(TrackedActivity.HUNTER_KIT, SpellBook.LUNAR, SpellType.UTILITY, 71, 70.0f, -1, new RuneStack(EARTH, 2), new RuneStack(ASTRAL, 2)),
	CURE_ME(TrackedActivity.CURE_ME, SpellBook.LUNAR, SpellType.UTILITY, 71, 65.0f, -1, new RuneStack(ASTRAL, 2), new RuneStack(COSMIC, 2), new RuneStack(LAW, 1)),
	CURE_GROUP(TrackedActivity.CURE_GROUP, SpellBook.LUNAR, SpellType.UTILITY, 74, 74.0f, -1, new RuneStack(ASTRAL, 2), new RuneStack(COSMIC, 2), new RuneStack(LAW, 2)),
	STAT_SPY(TrackedActivity.STAT_SPY, SpellBook.LUNAR, SpellType.UTILITY, 75, 76.0f, -1, new RuneStack(ASTRAL, 2), new RuneStack(BODY, 5), new RuneStack(COSMIC, 1)),
	SPIN_FLAX(TrackedActivity.SPIN_FLAX, SpellBook.LUNAR, SpellType.UTILITY, 76, 75.0f, -1, new RuneStack(AIR, 5), new RuneStack(ASTRAL, 1), new RuneStack(NATURE, 2)),
	SUPERGLASS_MAKE(TrackedActivity.SUPERGLASS_MAKE, SpellBook.LUNAR, SpellType.UTILITY, 77, 78.0f, -1, new RuneStack(AIR, 10), new RuneStack(FIRE, 6), new RuneStack(ASTRAL, 2)),
	TAN_LEATHER(TrackedActivity.TAN_LEATHER, SpellBook.LUNAR, SpellType.UTILITY, 78, 81.0f, -1, new RuneStack(FIRE, 5), new RuneStack(ASTRAL, 2), new RuneStack(NATURE, 1)),
	DREAM(TrackedActivity.DREAM, SpellBook.LUNAR, SpellType.UTILITY, 79, 82.0f, -1, new RuneStack(ASTRAL, 2), new RuneStack(BODY, 5), new RuneStack(COSMIC, 1)),
	STRING_JEWELLERY(TrackedActivity.STRING_JEWELLERY, SpellBook.LUNAR, SpellType.UTILITY, 80, 83.0f, -1, new RuneStack(EARTH, 10), new RuneStack(WATER, 5), new RuneStack(ASTRAL, 2)),
	STAT_RESTORE_POT_SHARE(TrackedActivity.STAT_RESTORE_POT_SHARE, SpellBook.LUNAR, SpellType.UTILITY, 81, 84.0f, -1, new RuneStack(EARTH, 10), new RuneStack(WATER, 10), new RuneStack(ASTRAL, 2)),
	MAGIC_IMBUE(TrackedActivity.MAGIC_IMBUE, SpellBook.LUNAR, SpellType.UTILITY, 82, 86.0f, -1, new RuneStack(FIRE, 7), new RuneStack(WATER, 7), new RuneStack(ASTRAL, 2)),
	FERTILE_SOIL(TrackedActivity.FERTILE_SOIL, SpellBook.LUNAR, SpellType.UTILITY, 83, 87.0f, -1, new RuneStack(EARTH, 15), new RuneStack(ASTRAL, 3), new RuneStack(NATURE, 2)),
	BOOST_POTION_SHARE(TrackedActivity.BOOST_POTION_SHARE, SpellBook.LUNAR, SpellType.UTILITY, 84, 88.0f, -1, new RuneStack(EARTH, 12), new RuneStack(WATER, 10), new RuneStack(ASTRAL, 3)),
	PLANK_MAKE(TrackedActivity.PLANK_MAKE, SpellBook.LUNAR, SpellType.UTILITY, 86, 90.0f, -1, new RuneStack(EARTH, 15), new RuneStack(ASTRAL, 2), new RuneStack(NATURE, 1)),
	RECHARGE_DRAGONSTONE(TrackedActivity.RECHARGE_DRAGONSTONE, SpellBook.LUNAR, SpellType.UTILITY, 89, 97.5f, -1, new RuneStack(WATER, 4), new RuneStack(ASTRAL, 1), new RuneStack(SOUL, 1)),
	ENERGY_TRANSFER(TrackedActivity.ENERGY_TRANSFER, SpellBook.LUNAR, SpellType.UTILITY, 91, 100.0f, -1, new RuneStack(ASTRAL, 3), new RuneStack(LAW, 2), new RuneStack(NATURE, 1)),
	HEAL_OTHER(TrackedActivity.HEAL_OTHER, SpellBook.LUNAR, SpellType.UTILITY, 92, 101.0f, -1, new RuneStack(ASTRAL, 3), new RuneStack(BLOOD, 1), new RuneStack(LAW, 3)),
	HEAL_GROUP(TrackedActivity.HEAL_GROUP, SpellBook.LUNAR, SpellType.UTILITY, 95, 104.0f, -1, new RuneStack(ASTRAL, 4), new RuneStack(BLOOD, 3), new RuneStack(LAW, 6)),
	SPELLBOOK_SWAP(TrackedActivity.SPELLBOOK_SWAP, SpellBook.LUNAR, SpellType.UTILITY, 96, 130.0f, -1, new RuneStack(ASTRAL, 3), new RuneStack(COSMIC, 2), new RuneStack(LAW, 1)),

	// --- LUNAR SPELLBOOK (Sack Enabled) ---
	VENGEANCE(TrackedActivity.VENGEANCE, SpellBook.LUNAR, SpellType.UTILITY, 94, 112.0f, -1, new RuneStack[]{new RuneStack(EARTH, 10), new RuneStack(ASTRAL, 4), new RuneStack(DEATH, 2)}, VENGEANCE_SACK),
	VENGEANCE_OTHER(TrackedActivity.VENGEANCE_OTHER, SpellBook.LUNAR, SpellType.UTILITY, 93, 108.0f, -1, new RuneStack[]{new RuneStack(EARTH, 10), new RuneStack(ASTRAL, 3), new RuneStack(DEATH, 2)}, VENGEANCE_SACK),

	// --- ARCEUUS SPELLBOOK (Combat & Reanimation) ---
	BASIC_REANIMATION(TrackedActivity.BASIC_REANIMATION, SpellBook.ARCEEUS, SpellType.UTILITY, 16, 32.0f, -1, new RuneStack(BODY, 4), new RuneStack(NATURE, 2)),
	GHOSTLY_GRASP(TrackedActivity.GHOSTLY_GRASP, SpellBook.ARCEEUS, SpellType.COMBAT, 35, 22.5f, -1, new RuneStack(AIR, 4), new RuneStack(CHAOS, 1)),
	RESURRECT_LESSER_ZOMBIE(TrackedActivity.RESURRECT_LESSER_ZOMBIE, SpellBook.ARCEEUS, SpellType.UTILITY, 38, 55.0f, -1, new RuneStack(AIR, 10), new RuneStack(COSMIC, 1), new RuneStack(MIND, 5)),
	RESURRECT_LESSER_SKELETON(TrackedActivity.RESURRECT_LESSER_SKELETON, SpellBook.ARCEEUS, SpellType.UTILITY, 38, 55.0f, -1, new RuneStack(AIR, 10), new RuneStack(COSMIC, 1), new RuneStack(MIND, 5)),
	RESURRECT_LESSER_GHOST(TrackedActivity.RESURRECT_LESSER_GHOST, SpellBook.ARCEEUS, SpellType.UTILITY, 38, 55.0f, -1, new RuneStack(AIR, 10), new RuneStack(COSMIC, 1), new RuneStack(MIND, 5)),
	ADEPT_REANIMATION(TrackedActivity.ADEPT_REANIMATION, SpellBook.ARCEEUS, SpellType.UTILITY, 41, 80.0f, -1, new RuneStack(BODY, 4), new RuneStack(NATURE, 3), new RuneStack(SOUL, 1)),
	INFERIOR_DEMONBANE(TrackedActivity.INFERIOR_DEMONBANE, SpellBook.ARCEEUS, SpellType.COMBAT, 44, 27.0f, -1, new RuneStack(FIRE, 3), new RuneStack(CHAOS, 1)),
	SHADOW_VEIL(TrackedActivity.SHADOW_VEIL, SpellBook.ARCEEUS, SpellType.UTILITY, 47, 58.0f, -1, new RuneStack(EARTH, 5), new RuneStack(FIRE, 5), new RuneStack(COSMIC, 5)),
	DARK_LURE(TrackedActivity.DARK_LURE, SpellBook.ARCEEUS, SpellType.UTILITY, 50, 60.0f, -1, new RuneStack(DEATH, 1), new RuneStack(NATURE, 1)),
	SKELETAL_GRASP(TrackedActivity.SKELETAL_GRASP, SpellBook.ARCEEUS, SpellType.COMBAT, 56, 33.0f, -1, new RuneStack(EARTH, 8), new RuneStack(DEATH, 1)),
	RESURRECT_SUPERIOR_GHOST(TrackedActivity.RESURRECT_SUPERIOR_GHOST, SpellBook.ARCEEUS, SpellType.UTILITY, 57, 70.0f, -1, new RuneStack(EARTH, 10), new RuneStack(COSMIC, 1), new RuneStack(DEATH, 5)),
	RESURRECT_SUPERIOR_SKELETON(TrackedActivity.RESURRECT_SUPERIOR_SKELETON, SpellBook.ARCEEUS, SpellType.UTILITY, 57, 70.0f, -1, new RuneStack(EARTH, 10), new RuneStack(COSMIC, 1), new RuneStack(DEATH, 5)),
	RESURRECT_SUPERIOR_ZOMBIE(TrackedActivity.RESURRECT_SUPERIOR_ZOMBIE, SpellBook.ARCEEUS, SpellType.UTILITY, 57, 70.0f, -1, new RuneStack(EARTH, 10), new RuneStack(COSMIC, 1), new RuneStack(DEATH, 5)),
	MARK_OF_DARKNESS(TrackedActivity.MARK_OF_DARKNESS, SpellBook.ARCEEUS, SpellType.UTILITY, 59, 70.0f, -1, new RuneStack(COSMIC, 1), new RuneStack(SOUL, 1)),
	SUPERIOR_DEMONBANE(TrackedActivity.SUPERIOR_DEMONBANE, SpellBook.ARCEEUS, SpellType.COMBAT, 62, 36.0f, -1, new RuneStack(FIRE, 5), new RuneStack(SOUL, 1)),
	LESSER_CORRUPTION(TrackedActivity.LESSER_CORRUPTION, SpellBook.ARCEEUS, SpellType.UTILITY, 64, 75.0f, -1, new RuneStack(DEATH, 1), new RuneStack(SOUL, 2)),
	VILE_VIGOUR(TrackedActivity.VILE_VIGOUR, SpellBook.ARCEEUS, SpellType.UTILITY, 66, 76.0f, -1, new RuneStack(AIR, 3), new RuneStack(SOUL, 1)),
	DEGRIME(TrackedActivity.DEGRIME, SpellBook.ARCEEUS, SpellType.UTILITY, 71, 83.0f, -1, new RuneStack(NATURE, 2), new RuneStack(EARTH, 4)),
	EXPERT_REANIMATION(TrackedActivity.EXPERT_REANIMATION, SpellBook.ARCEEUS, SpellType.UTILITY, 72, 138.0f, -1, new RuneStack(BLOOD, 1), new RuneStack(NATURE, 3), new RuneStack(SOUL, 2)),
	WARD_OF_ARCEUUS(TrackedActivity.WARD_OF_ARCEUUS, SpellBook.ARCEEUS, SpellType.UTILITY, 73, 83.0f, -1, new RuneStack(COSMIC, 1), new RuneStack(NATURE, 2), new RuneStack(SOUL, 4)),
	RESURRECT_GREATER_SKELETON(TrackedActivity.RESURRECT_GREATER_SKELETON, SpellBook.ARCEEUS, SpellType.UTILITY, 76, 88.0f, -1, new RuneStack(FIRE, 10), new RuneStack(BLOOD, 5), new RuneStack(COSMIC, 1)),
	RESURRECT_GREATER_ZOMBIE(TrackedActivity.RESURRECT_GREATER_ZOMBIE, SpellBook.ARCEEUS, SpellType.UTILITY, 76, 88.0f, -1, new RuneStack(FIRE, 10), new RuneStack(BLOOD, 5), new RuneStack(COSMIC, 1)),
	RESURRECT_GREATER_GHOST(TrackedActivity.RESURRECT_GREATER_GHOST, SpellBook.ARCEEUS, SpellType.UTILITY, 76, 88.0f, -1, new RuneStack(FIRE, 10), new RuneStack(BLOOD, 5), new RuneStack(COSMIC, 1)),
	RESURRECT_CROPS(TrackedActivity.RESURRECT_CROPS, SpellBook.ARCEEUS, SpellType.UTILITY, 78, 90.0f, -1, new RuneStack(EARTH, 25), new RuneStack(BLOOD, 8), new RuneStack(NATURE, 12), new RuneStack(SOUL, 8)),
	UNDEAD_GRASP(TrackedActivity.UNDEAD_GRASP, SpellBook.ARCEEUS, SpellType.COMBAT, 79, 46.5f, -1, new RuneStack(FIRE, 12), new RuneStack(BLOOD, 1)),
	DEATH_CHARGE(TrackedActivity.DEATH_CHARGE, SpellBook.ARCEEUS, SpellType.UTILITY, 82, 90.0f, -1, new RuneStack(DEATH, 1), new RuneStack(BLOOD, 1), new RuneStack(SOUL, 1)),
	DARK_DEMONBANE(TrackedActivity.DARK_DEMONBANE, SpellBook.ARCEEUS, SpellType.COMBAT, 82, 43.5f, -1, new RuneStack(FIRE, 7), new RuneStack(SOUL, 2)),
	DEMONIC_OFFERING(TrackedActivity.DEMONIC_OFFERING, SpellBook.ARCEEUS, SpellType.UTILITY, 84, 86.0f, -1, new RuneStack(SOUL, 1), new RuneStack(WRATH, 1)),
	GREATER_CORRUPTION(TrackedActivity.GREATER_CORRUPTION, SpellBook.ARCEEUS, SpellType.UTILITY, 85, 85.0f, -1, new RuneStack(BLOOD, 1), new RuneStack(SOUL, 3)),
	MASTER_REANIMATION(TrackedActivity.MASTER_REANIMATION, SpellBook.ARCEEUS, SpellType.UTILITY, 93, 170.0f, -1, new RuneStack(BLOOD, 2), new RuneStack(NATURE, 4), new RuneStack(SOUL, 4)),
	SINISTER_OFFERING(TrackedActivity.SINISTER_OFFERING, SpellBook.ARCEEUS, SpellType.UTILITY, 92, 98.0f, -1, new RuneStack(BLOOD, 1), new RuneStack(WRATH, 1));

	// --- FALLBACKS ---
//	SPLASH_CAST(TrackedActivity.SPLASH_CAST, SpellType.COMBAT, 1, 0.0f, 227, );

	final TrackedActivity activity;
	final SpellBook spellbook;
	final SpellType spellType;
	final int magicLevel;
	final float experience;
	final int soundId;

	final RuneStack[] runesConsumed;
	final RuneElement sackConsumed;
	final int[] requiredWeapons;

	MagicSpell(TrackedActivity activity, SpellBook spellBook, SpellType spellType, int magicLevel, float experience, int soundId, RuneStack... runes)
	{
		this.activity = activity;
		this.spellbook = spellBook;
		this.spellType = spellType;
		this.magicLevel = magicLevel;
		this.experience = experience;
		this.soundId = soundId;
		this.runesConsumed = runes;
		this.sackConsumed = null;
		this.requiredWeapons = new int[]{};
	}

	MagicSpell( TrackedActivity activity, SpellBook spellBook, SpellType spellType, int magicLevel, float experience, int soundId, RuneStack[] runes, RuneElement sack)
	{
		this.activity = activity;
		this.spellbook = spellBook;
		this.spellType = spellType;
		this.magicLevel = magicLevel;
		this.experience = experience;
		this.soundId = soundId;
		this.runesConsumed = runes;

		assert sack != null && sack.name().endsWith("_SACK");
		this.sackConsumed = sack;
		this.requiredWeapons = new int[]{};
	}

	MagicSpell( TrackedActivity activity, SpellBook spellBook, SpellType spellType, int magicLevel, float experience, int soundId, RuneStack[] runes, int... weaponsRequired)
	{
		this.activity = activity;
		this.spellbook = spellBook;
		this.spellType = spellType;
		this.magicLevel = magicLevel;
		this.experience = experience;
		this.soundId = soundId;
		this.runesConsumed = runes;

		this.sackConsumed = null;
		this.requiredWeapons = weaponsRequired;
	}

	private static final Map<SpellBook, MagicSpell[]> SPELLS_BY_BOOK = new EnumMap<>(SpellBook.class);

	static
	{
		Map<SpellBook, List<MagicSpell>> tempMap = new EnumMap<>(SpellBook.class);

		for (SpellBook book : SpellBook.values())
		{
			tempMap.put(book, new ArrayList<>());
		}

		for (MagicSpell spell : values())
		{
			if (spell.spellbook != null)
			{
				tempMap.get(spell.spellbook).add(spell);
			}
		}

		for (Map.Entry<SpellBook, List<MagicSpell>> entry : tempMap.entrySet())
		{
			Collections.reverse(entry.getValue());
			SPELLS_BY_BOOK.put(entry.getKey(), entry.getValue().toArray(new MagicSpell[0]));
		}
	}

	/**
	 * Returns an O(1) cached array of only the spells available in the specified spellbook.
	 */
	public static MagicSpell[] getSpellsForBook(SpellBook book)
	{
		if (book == null) return new MagicSpell[0];
		return SPELLS_BY_BOOK.getOrDefault(book, new MagicSpell[0]);
	}
}


// TrackedActivities
