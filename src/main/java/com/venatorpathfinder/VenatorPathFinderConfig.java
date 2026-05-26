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
package com.venatorpathfinder;

import static com.venatorpathfinder.VenatorPathFinderPlugin.CONFIG_GROUP;
import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(CONFIG_GROUP)
public interface VenatorPathFinderConfig extends Config
{
	@Alpha
	@ConfigItem(
		keyName = "initialColor",
		name = "Target Color",
		description = "Color of the NPC you would be directly attacking.",
		position = 0
	)
	default Color initialColor() { return Color.RED; }

	@Alpha
	@ConfigItem(
		keyName = "bounce1Color",
		name = "First Bounce Color",
		description = "Outline color drawn around the NPC(s) that may be targeted by the first bounced projectile.",
		position = 1
	)
	default Color bounce1Color() { return Color.YELLOW; }

	@Alpha
	@ConfigItem(
		keyName = "bounce2Color",
		name = "Second Bounce Color",
		description = "Outline color drawn around the NPC(s) that may be targeted by the second bounced projectile.",
		position = 2
	)
	default Color bounce2Color() { return Color.BLUE; }

	@Alpha
	@ConfigItem(
		keyName = "returnBounceColor",
		name = "Return Bounce Color",
		description = "Outline color of the NPC you would be directly attacking if the projectile may hit it a second time.",
		position = 3
	)
	default Color returnBounceColor() { return Color.MAGENTA; }

	@ConfigItem(
		keyName = "outlineWidth",
		name = "Outline width",
		description = "Width of the outline drawn around affected NPC(s)",
		position = 4
	)
	default int outlineWidth() { return 5; }

	@ConfigItem(
		keyName = "outlineFeather",
		name = "Outline feather",
		description = "Feather size of the outline drawn around NPCs. A higher value increases the blur.",
		position = 5
	)
	default int outlineFeather() { return 2; }

	@ConfigItem(
		keyName = "limitPathSize",
		name = "Limit pathSize to 1 bounce",
		description = "If set, path size is limited to one bounce",
		position = 6
	)
	default boolean limitPathSize() { return false; }

	@ConfigItem(
		keyName = "drawOnlyOnePath",
		name = "Draw at most one path",
		description = "If set, draw at most one path. Note that this is not necessarily the actual path.",
		position = 7
	)
	default boolean drawOnlyOnePath() { return false; }

	@ConfigItem(
		keyName = "ignoreMultiCombatPrerequisite",
		name = "Ignore multicombat prerequisite",
		description = "If set, multicombat areas will not affect whether or not the paths are drawn",
		position = 8
	)
	default boolean ignoreMultiCombatPrerequisite() { return true; }

	@ConfigItem(
		keyName = "ignoreVenatorBowPrerequisite",
		name = "Ignore Venator bow prerequisite",
		description = "If set, having a venator bow equipped will not affect whether or not the paths are drawn",
		position = 9
	)
	default boolean ignoreVenatorBowPrerequisite() { return true; }
}
