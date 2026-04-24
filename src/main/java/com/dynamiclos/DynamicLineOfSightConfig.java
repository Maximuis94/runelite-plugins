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

package com.dynamiclos;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(DynamicLineOfSightConfig.PLUGIN_CONFIG_GROUP)
public interface DynamicLineOfSightConfig extends Config
{
	String PLUGIN_CONFIG_GROUP = "dynamiclineofsight";
	int DEFAULT_OUTLINE_ALPHA = 200;
	int DEFAULT_FILL_ALPHA = 15;

	@ConfigSection(
		name = "Active Weapon Range",
		description = "Settings for the currently equipped weapon's line of sight",
		position = 1
	)
	String activeWeaponSection = "activeWeaponSection";

	@ConfigSection(
		name = "Max Attack Range",
		description = "Settings for the maximum possible attack range (10 tiles)",
		position = 2
	)
	String maxRangeSection = "maxRangeSection";

	@ConfigItem(
		keyName = "drawActiveWeaponRange",
		name = "Draw active weapon outline",
		description = "Draws the outline of the line of sight based on your currently equipped weapon",
		position = 0,
		section = activeWeaponSection
	)
	default boolean drawActiveWeaponRange()
	{
		return true; // Defaulting to true so it shows up immediately when installed
	}

	@ConfigItem(
		keyName = "highlightAttackableEnemies",
		name = "Highlight active weapon range enemies",
		description = "If checked, enemies that can be directly attacked are given an outline",
		position = 1,
		section = activeWeaponSection
	)
	default boolean highlightAttackableEnemies() {return false;}

	@Alpha
	@ConfigItem(
		keyName = "activeWeaponOutlineColor",
		name = "Active weapon attack range outline color",
		description = "The color of the outline for the active weapon line of sight bubble",
		position = 2,
		section = activeWeaponSection
	)
	default Color activeWeaponOutlineColor()
	{
		return new Color(255, 0, 125, DEFAULT_OUTLINE_ALPHA);
	}

	@Alpha
	@ConfigItem(
		keyName = "activeWeaponFillColor",
		name = "Active weapon attack range fill color",
		description = "The fill color for the active weapon line of sight bubble",
		position = 3,
		section = activeWeaponSection
	)
	default Color activeWeaponFillColor()
	{
		return new Color(255, 0, 255, DEFAULT_FILL_ALPHA);
	}

	@ConfigItem(
		keyName = "drawMaxAttackRange",
		name = "Draw max range outline",
		description = "Draws an outline at the maximum possible attack range (10 tiles)",
		position = 1,
		section = maxRangeSection
	)
	default boolean drawMaxAttackRange()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlightEnemiesWithinMaxRange",
		name = "Highlight max range enemies",
		description = "If checked, enemies within 10 tiles or less range are highlighted",
		position = 2,
		section = maxRangeSection
	)
	default boolean highlightEnemiesWithinMaxRange() {return false;}

	@Alpha
	@ConfigItem(
		keyName = "maxRangeOutlineColor",
		name = "Max attack range outline color",
		description = "The color that is to be used for the outline of the max attack range",
		position = 3,
		section = maxRangeSection
	)
	default Color maxRangeOutlineColor()
	{
		return new Color(0, 0, 255, DEFAULT_OUTLINE_ALPHA);
	}

	@Alpha
	@ConfigItem(
		keyName = "maxRangeFillColor",
		name = "Max attack range fill color",
		description = "The fill color that is to be used for the outline of the max attack range",
		position = 4,
		section = maxRangeSection
	)
	default Color maxRangeFillColor()
	{
		return new Color(0, 0, 255, DEFAULT_FILL_ALPHA);
	}
}