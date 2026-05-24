/*
 * Copyright (c) 2026, maximuis94 <https://github.com/maximuis94>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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

import static com.dynamiclos.PluginConstants.*;
import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;

@ConfigGroup(PLUGIN_CONFIG_GROUP)
public interface DynamicLineOfSightConfig extends Config
{
	@ConfigSection(
		name = "Toggles & Keybinds",
		description = "Enable/disable specific components and configure their keybinds",
		position = 0,
		closedByDefault = true
	)
	String togglesSection = "togglesSection";

	@ConfigSection(
		name = "NPC LoS Styling",
		description = "Section with various outline, fill colors, and line widths to assign to particular NPC groups",
		position = 1,
		closedByDefault = true
	)
	String npcStylingSection = "npcStylingSection";

	@ConfigSection(
		name = "Virtual NPC LoS",
		description = "Settings for the Virtual NPC Line of Sight feature",
		position = 2,
		closedByDefault = true
	)
	String virtualNpcSection = "virtualNpcSection";

	@ConfigSection(
		name = "Active Weapon Range",
		description = "Settings for the currently equipped weapon's line of sight",
		position = 4,
		closedByDefault = true
	)
	String activeWeaponSection = "activeWeaponSection";

	@ConfigSection(
		name = "Max Attack Range",
		description = "Settings for the maximum possible attack range (10 tiles), unaffected by Myopia.",
		position = 5,
		closedByDefault = true
	)
	String maxRangeSection = "maxRangeSection";

	@ConfigSection(
		name = "Fixed Range 1",
		description = "Settings for configurable fixed distance line of sight 1",
		position = 6,
		closedByDefault = true
	)
	String fixedRange1Section = "fixedRange1Section";

	@ConfigSection(
		name = "Fixed Range 2",
		description = "Settings for configurable fixed distance line of sight 2",
		position = 7,
		closedByDefault = true
	)
	String fixedRange2Section = "fixedRange2Section";

	@ConfigSection(
		name = "Fixed Range 3",
		description = "Settings for configurable fixed distance line of sight 3",
		position = 8,
		closedByDefault = true
	)
	String fixedRange3Section = "fixedRange3Section";

	@ConfigSection(
		name = "Fixed Range 4",
		description = "Settings for configurable fixed distance line of sight 4",
		position = 9,
		closedByDefault = true
	)
	String fixedRange4Section = "fixedRange4Section";

	@ConfigSection(
		name = "Fixed Range 5",
		description = "Settings for configurable fixed distance line of sight 5",
		position = 10,
		closedByDefault = true
	)
	String fixedRange5Section = "fixedRange5Section";

	// =========================================
	// TOGGLES & KEYBINDS
	// =========================================

	@ConfigItem(
		keyName = "mutualExclusivePlayerNpcLos",
		name = "Mutually exclusive player/NPC LoS",
		description = "If checked, the player line of sight is hidden while the NPC line of sight is shown.",
		position = 0,
		section = togglesSection
	)
	default boolean mutualExclusivePlayerNpcLos() { return true; }

	@ConfigItem(
		keyName = "togglePlayerLosHotkey",
		name = "Toggle player LoS",
		description = "Press this key to quickly enable/disable all player lines of sight",
		position = 1,
		section = togglesSection
	)
	default Keybind togglePlayerLosHotkey() { return Keybind.NOT_SET; }

	@ConfigItem(
		keyName = "virtualPlayerLosHotkey",
		name = "Virtual player LoS Hotkey",
		description = "Hold this key to draw the player line of sight the tile underneath the cursor while the key is pressed.",
		position = 2,
		section = togglesSection
	)
	default Keybind virtualPlayerLosHotkey() { return Keybind.NOT_SET; }

	@ConfigItem(
		keyName = "enableNpcLos",
		name = "Enable NPC LoS",
		description = "If checked, allows rendering the NPC line of sight below the cursor for specified NPCs.",
		position = 3,
		section = togglesSection
	)
	default boolean enableNpcLos() { return false; }

	@ConfigItem(
		keyName = "npcLosHotkey",
		name = "NPC LoS Hotkey",
		description = "Hold this key to show the line of sight of the hovered NPC while the key is pressed.",
		position = 4,
		section = togglesSection
	)
	default Keybind npcLosHotkey() { return Keybind.NOT_SET; }

	@ConfigItem(
		keyName = "toggleNpcLosHotkey",
		name = "Toggle NPC LoS",
		description = "Press this key to quickly enable/disable all NPC lines of sight",
		position = 5,
		section = togglesSection
	)
	default Keybind toggleNpcLosHotkey() { return Keybind.NOT_SET; }

	@ConfigItem(
		keyName = "enableVirtualNpcLos",
		name = "Enable Virtual NPC LoS",
		description = "If checked, allows drawing a virtual NPC line of sight at the cursor's location.",
		position = 6,
		section = togglesSection
	)
	default boolean enableVirtualNpcLos() { return false; }

	@ConfigItem(
		keyName = "virtualNpcLosHotkey",
		name = "Virtual NPC LoS Hotkey",
		description = "Hold this key to draw a virtual NPC's line of sight at the cursor's location.",
		position = 7,
		section = togglesSection
	)
	default Keybind virtualNpcLosHotkey() { return Keybind.NOT_SET; }

	@ConfigItem(
		keyName = "drawActiveWeaponRange",
		name = "Enable active weapon LoS",
		description = "Draws the outline of the line of sight based on your currently equipped weapon",
		position = 8,
		section = togglesSection
	)
	default boolean drawActiveWeaponRange() { return true; }

	@ConfigItem(
		keyName = "highlightAttackableEnemies",
		name = "Highlight active weapon range enemies",
		description = "If checked, enemies that can be directly attacked are given an outline",
		position = 9,
		section = togglesSection
	)
	default boolean highlightAttackableEnemies() { return false; }

	@ConfigItem(
		keyName = "drawMaxAttackRange",
		name = "Enable max range LoS",
		description = "Draws an outline at the maximum possible attack range (10 tiles)",
		position = 10,
		section = togglesSection
	)
	default boolean drawMaxAttackRange() { return false; }

	@ConfigItem(
		keyName = "highlightEnemiesWithinMaxRange",
		name = "Highlight max range enemies",
		description = "If checked, enemies within 10 tiles or less range are highlighted",
		position = 11,
		section = togglesSection
	)
	default boolean highlightEnemiesWithinMaxRange() { return false; }

	@ConfigItem(
		keyName = "drawFixedRange1",
		name = "Enable range 1 LoS",
		description = "Enables or disables drawing line of sight using attack range 1 configs",
		position = 12,
		section = togglesSection
	)
	default boolean drawFixedRange1() { return false; }

	@ConfigItem(
		keyName = "highlightEnemiesWithinFixedRange1",
		name = "Highlight enemies in range 1",
		description = "If checked, highlight enemies in attack range of the configured distance.",
		position = 13,
		section = togglesSection
	)
	default boolean highlightEnemiesWithinFixedRange1() { return false; }

	@ConfigItem(
		keyName = "drawFixedRange2",
		name = "Enable range 2 LoS",
		description = "Enables or disables drawing line of sight using attack range 2 configs",
		position = 14,
		section = togglesSection
	)
	default boolean drawFixedRange2() { return false; }

	@ConfigItem(
		keyName = "highlightEnemiesWithinFixedRange2",
		name = "Highlight enemies in range 2",
		description = "If checked, highlight enemies in attack range of the configured distance.",
		position = 15,
		section = togglesSection
	)
	default boolean highlightEnemiesWithinFixedRange2() { return false; }

	@ConfigItem(
		keyName = "drawFixedRange3",
		name = "Enable range 3 LoS",
		description = "Enables or disables drawing line of sight using attack range 3 configs",
		position = 16,
		section = togglesSection
	)
	default boolean drawFixedRange3() { return false; }

	@ConfigItem(
		keyName = "highlightEnemiesWithinFixedRange3",
		name = "Highlight enemies in range 3",
		description = "If checked, highlight enemies in attack range of the configured distance.",
		position = 17,
		section = togglesSection
	)
	default boolean highlightEnemiesWithinFixedRange3() { return false; }

	@ConfigItem(
		keyName = "drawFixedRange4",
		name = "Enable range 4 LoS",
		description = "Enables or disables drawing line of sight using attack range 4 configs",
		position = 18,
		section = togglesSection
	)
	default boolean drawFixedRange4() { return false; }

	@ConfigItem(
		keyName = "highlightEnemiesWithinFixedRange4",
		name = "Highlight enemies in range 4",
		description = "If checked, highlight enemies in attack range of the configured distance.",
		position = 19,
		section = togglesSection
	)
	default boolean highlightEnemiesWithinFixedRange4() { return false; }

	@ConfigItem(
		keyName = "drawFixedRange5",
		name = "Enable range 5 LoS",
		description = "Enables or disables drawing line of sight using attack range 5 configs",
		position = 20,
		section = togglesSection
	)
	default boolean drawFixedRange5() { return false; }

	@ConfigItem(
		keyName = "highlightEnemiesWithinFixedRange5",
		name = "Highlight enemies in range 5",
		description = "If checked, highlight enemies in attack range of the configured distance.",
		position = 21,
		section = togglesSection
	)
	default boolean highlightEnemiesWithinFixedRange5() { return false; }


	// =========================================
	// NPC LOS STYLING & DEFINITIONS
	// =========================================

	@Alpha
	@ConfigItem(
		keyName = "meleeNpcOutlineColor",
		name = "Melee outline color",
		description = "The outline color for melee NPC lines of sight",
		position = 0,
		section = npcStylingSection
	)
	default Color meleeNpcOutlineColor() { return DEFAULT_COLOR_MELEE_OUTLINE; }

	@Alpha
	@ConfigItem(
		keyName = "meleeNpcFillColor",
		name = "Melee fill color",
		description = "The fill color for melee NPC lines of sight",
		position = 1,
		section = npcStylingSection
	)
	default Color meleeNpcFillColor() { return DEFAULT_COLOR_MELEE_FILL; }

	@ConfigItem(
		keyName = "meleeNpcLineWidth",
		name = "Melee line width",
		description = "The thickness of the outline for melee NPC lines of sight",
		position = 2,
		section = npcStylingSection
	)
	default double meleeNpcLineWidth() { return DEFAULT_LINE_WIDTH; }

	@ConfigItem(
		keyName = "meleeNpcDefs",
		name = "Melee NPCs",
		description = "Format: NPC_NAME|RANGE. Newline separated. Can also use NPC ID.",
		position = 3,
		section = npcStylingSection
	)
	default String meleeNpcDefs() {
		return "Jaguar warrior|1\n" +
			"Minotaur|1\n" +
			"Tz-Kih|1\n" +
			"Tz-Kek|1\n" +
			"Yt-MejKot|1\n" +
			"Tok-Xil|1*\n" +
			"Ket-Zek|1*\n" +
			"Yt-HurKot|1\n" +
			"Jal-Nib|1\n" +
			"Jal-Ak|1*\n" +
			"Jal-Akrek-Ket|1\n" +
			"Jal-ImKot|1\n" +
			"Jal-Yt-HurKot|1\n" +
			"Jal-Xil|1*\n" +
			"Jal-Zek|1*\n" +
			"JalTok-Jad|1*";
	}

	@Alpha
	@ConfigItem(
		keyName = "rangedNpcOutlineColor",
		name = "Ranged outline color",
		description = "The outline color for ranged NPC lines of sight",
		position = 4,
		section = npcStylingSection
	)
	default Color rangedNpcOutlineColor() { return DEFAULT_COLOR_RANGED_OUTLINE; }

	@Alpha
	@ConfigItem(
		keyName = "rangedNpcFillColor",
		name = "Ranged fill color",
		description = "The fill color for ranged NPC lines of sight",
		position = 5,
		section = npcStylingSection
	)
	default Color rangedNpcFillColor() { return DEFAULT_COLOR_RANGED_FILL; }

	@ConfigItem(
		keyName = "rangedNpcLineWidth",
		name = "Ranged line width",
		description = "The thickness of the outline for ranged NPC lines of sight",
		position = 6,
		section = npcStylingSection
	)
	default double rangedNpcLineWidth() { return DEFAULT_LINE_WIDTH; }

	@ConfigItem(
		keyName = "rangedNpcDefs",
		name = "Ranged NPCs",
		description = "Format: NPC_NAME|RANGE. Newline separated. Can also use NPC ID.",
		position = 7,
		section = npcStylingSection
	)
	default String rangedNpcDefs() {
		return "Javelin colossus|15\n" +
			"Tok-Xil|15\n" +
			"Jal-MejRah|4\n" +
			"Jal-Akrek-Xil|15\n" +
			"Jal-Xil|15";
	}

	@Alpha
	@ConfigItem(
		keyName = "magicNpcOutlineColor",
		name = "Magic outline color",
		description = "The outline color for magic NPC lines of sight",
		position = 8,
		section = npcStylingSection
	)
	default Color magicNpcOutlineColor() { return DEFAULT_COLOR_MAGIC_OUTLINE; }

	@Alpha
	@ConfigItem(
		keyName = "magicNpcFillColor",
		name = "Magic fill color",
		description = "The fill color for magic NPC lines of sight",
		position = 9,
		section = npcStylingSection
	)
	default Color magicNpcFillColor() { return DEFAULT_COLOR_MAGIC_FILL; }

	@ConfigItem(
		keyName = "magicNpcLineWidth",
		name = "Magic line width",
		description = "The thickness of the outline for magic NPC lines of sight",
		position = 10,
		section = npcStylingSection
	)
	default double magicNpcLineWidth() { return DEFAULT_LINE_WIDTH; }

	@ConfigItem(
		keyName = "magicNpcDefs",
		name = "Magic NPCs",
		description = "Format: NPC_NAME|RANGE. Newline separated. Can also use NPC ID.",
		position = 11,
		section = npcStylingSection
	)
	default String magicNpcDefs() {
		return "Serpent shaman|10\n" +
			"Shockwave colossus|15\n" +
			"Ket-Zek|15\n" +
			"Jal-Akrek-Mej|15\n" +
			"Jal-Zek|15";
	}

	@Alpha
	@ConfigItem(
		keyName = "otherNpcOutlineColor",
		name = "Hybrid outline color",
		description = "The outline color for hybrid/typeless NPC lines of sight",
		position = 12,
		section = npcStylingSection
	)
	default Color otherNpcOutlineColor() { return DEFAULT_COLOR_OTHER_OUTLINE; }

	@Alpha
	@ConfigItem(
		keyName = "otherNpcFillColor",
		name = "Hybrid fill color",
		description = "The fill color for hybrid/typeless NPC lines of sight",
		position = 13,
		section = npcStylingSection
	)
	default Color otherNpcFillColor() { return DEFAULT_COLOR_OTHER_FILL; }

	@ConfigItem(
		keyName = "otherNpcLineWidth",
		name = "Hybrid line width",
		description = "The thickness of the outline for hybrid/typeless NPC lines of sight",
		position = 14,
		section = npcStylingSection
	)
	default double otherNpcLineWidth() { return DEFAULT_LINE_WIDTH; }

	@ConfigItem(
		keyName = "otherNpcDefs",
		name = "Hybrid/Typeless NPCs",
		description = "Format: NPC_NAME|RANGE. Newline separated. Can also use NPC ID.",
		position = 15,
		section = npcStylingSection
	)
	default String otherNpcDefs() {
		return "Manticore|15\n" +
			"Sol Heredit|1\n" +
			"Healing totem|20\n" +
			"TzTok-Jad|15\n" +
			"Jal-Ak|15\n" +
			"JalTok-Jad|15";
	}


	// =========================================
	// VIRTUAL NPC LOS
	// =========================================

	@Range(min = 1)
	@ConfigItem(
		keyName = "virtualNpcSize",
		name = "Virtual NPC Size",
		description = "The tile size of the virtual NPC (e.g., 1 for 1x1, 2 for 2x2).",
		position = 0,
		section = virtualNpcSection
	)
	default int virtualNpcSize() { return 1; }

	@Range(min = 0)
	@ConfigItem(
		keyName = "virtualNpcMeleeRange",
		name = "Melee Attack Range",
		description = "The melee attack range of the virtual NPC. 0 to disable.",
		position = 1,
		section = virtualNpcSection
	)
	default int virtualNpcMeleeRange() { return 0; }

	@Range(min = 0)
	@ConfigItem(
		keyName = "virtualNpcRangedRange",
		name = "Ranged Attack Range",
		description = "The ranged attack range of the virtual NPC. 0 to disable.",
		position = 2,
		section = virtualNpcSection
	)
	default int virtualNpcRangedRange() { return 0; }

	@Range(min = 0)
	@ConfigItem(
		keyName = "virtualNpcMagicRange",
		name = "Magic Attack Range",
		description = "The magic attack range of the virtual NPC. 0 to disable.",
		position = 3,
		section = virtualNpcSection
	)
	default int virtualNpcMagicRange() { return 0; }

	@Range(min = 0)
	@ConfigItem(
		keyName = "virtualNpcOtherRange",
		name = "Hybrid/Typeless Range",
		description = "The hybrid/typeless attack range of the virtual NPC. 0 to disable.",
		position = 4,
		section = virtualNpcSection
	)
	default int virtualNpcOtherRange() { return 0; }

	@Alpha
	@ConfigItem(
		keyName = "virtualNpcOutlineColor",
		name = "Virtual NPC outline color",
		description = "The outline color for the virtual NPC",
		position = 5,
		section = virtualNpcSection
	)
	default Color virtualNpcOutlineColor() { return DEFAULT_COLOR_OTHER_OUTLINE; }

	@Alpha
	@ConfigItem(
		keyName = "virtualNpcFillColor",
		name = "Virtual NPC fill color",
		description = "The fill color for the virtual NPC",
		position = 6,
		section = virtualNpcSection
	)
	default Color virtualNpcFillColor() { return DEFAULT_COLOR_OTHER_FILL; }

	@ConfigItem(
		keyName = "virtualNpcLineWidth",
		name = "Virtual NPC line width",
		description = "The thickness of the outline for the virtual NPC",
		position = 7,
		section = virtualNpcSection
	)
	default double virtualNpcLineWidth() { return DEFAULT_LINE_WIDTH; }


	// =========================================
	// ACTIVE WEAPON RANGE ITEMS
	// =========================================

	@Alpha
	@ConfigItem(
		keyName = "activeWeaponOutlineColor",
		name = "Outline color",
		description = "The color of the outline for the active weapon line of sight bubble",
		position = 1,
		section = activeWeaponSection
	)
	default Color activeWeaponOutlineColor() { return DEFAULT_COLOR_ACTIVE_WEAPON_OUTLINE; }

	@Alpha
	@ConfigItem(
		keyName = "activeWeaponFillColor",
		name = "Fill color",
		description = "The fill color for the active weapon line of sight bubble",
		position = 2,
		section = activeWeaponSection
	)
	default Color activeWeaponFillColor() { return DEFAULT_COLOR_ACTIVE_WEAPON_FILL; }

	@ConfigItem(
		keyName = "activeWeaponLineWidth",
		name = "Line width",
		description = "The thickness of the outline for the active weapon range",
		position = 3,
		section = activeWeaponSection
	)
	default double activeWeaponLineWidth() { return DEFAULT_LINE_WIDTH; }


	// =========================================
	// MAX ATTACK RANGE ITEMS
	// =========================================

	@Alpha
	@ConfigItem(
		keyName = "maxRangeOutlineColor",
		name = "Outline color",
		description = "The color that is to be used for the outline of the max attack range",
		position = 1,
		section = maxRangeSection
	)
	default Color maxRangeOutlineColor() { return DEFAULT_COLOR_MAGIC_OUTLINE; }

	@Alpha
	@ConfigItem(
		keyName = "maxRangeFillColor",
		name = "Fill color",
		description = "The fill color that is to be used for the outline of the max attack range",
		position = 2,
		section = maxRangeSection
	)
	default Color maxRangeFillColor() { return DEFAULT_COLOR_MAGIC_FILL; }

	@ConfigItem(
		keyName = "maxRangeLineWidth",
		name = "Line width",
		description = "The thickness of the outline for the max attack range",
		position = 3,
		section = maxRangeSection
	)
	default double maxRangeLineWidth() { return DEFAULT_LINE_WIDTH; }


	// =========================================
	// FIXED RANGE 1 ITEMS
	// =========================================

	@Range(min = 1)
	@ConfigItem(
		keyName = "fixedRange1Distance",
		name = "Attack range",
		description = "The specific distance (in tiles) for this fixed line of sight",
		position = 1,
		section = fixedRange1Section
	)
	default int fixedRange1Distance() { return 5; }

	@Alpha
	@ConfigItem(
		keyName = "fixedRange1OutlineColor",
		name = "Outline color",
		description = "The color of the outline for this fixed range. Setting alpha to 0 disables it.",
		position = 2,
		section = fixedRange1Section
	)
	default Color fixedRange1OutlineColor() { return PluginConstants.DEFAULT_COLOR_OTHER_OUTLINE; }

	@Alpha
	@ConfigItem(
		keyName = "fixedRange1FillColor",
		name = "Fill color",
		description = "The fill color for this fixed range. Setting alpha to 0 disables it.",
		position = 3,
		section = fixedRange1Section
	)
	default Color fixedRange1FillColor() { return PluginConstants.DEFAULT_COLOR_OTHER_FILL; }

	@ConfigItem(
		keyName = "fixedRange1LineWidth",
		name = "Line width",
		description = "The thickness of the outline for this fixed range",
		position = 4,
		section = fixedRange1Section
	)
	default double fixedRange1LineWidth() { return DEFAULT_LINE_WIDTH; }


	// =========================================
	// FIXED RANGE 2 ITEMS
	// =========================================

	@Range(min = 1)
	@ConfigItem(
		keyName = "fixedRange2Distance",
		name = "Attack range",
		description = "The specific distance (in tiles) for this fixed line of sight",
		position = 1,
		section = fixedRange2Section
	)
	default int fixedRange2Distance() { return 5; }

	@Alpha
	@ConfigItem(
		keyName = "fixedRange2OutlineColor",
		name = "Outline color",
		description = "The color of the outline for this fixed range. Setting alpha to 0 disables it.",
		position = 2,
		section = fixedRange2Section
	)
	default Color fixedRange2OutlineColor() { return PluginConstants.DEFAULT_COLOR_OTHER_OUTLINE; }

	@Alpha
	@ConfigItem(
		keyName = "fixedRange2FillColor",
		name = "Fill color",
		description = "The fill color for this fixed range. Setting alpha to 0 disables it.",
		position = 3,
		section = fixedRange2Section
	)
	default Color fixedRange2FillColor() { return PluginConstants.DEFAULT_COLOR_OTHER_FILL; }

	@ConfigItem(
		keyName = "fixedRange2LineWidth",
		name = "Line width",
		description = "The thickness of the outline for this fixed range",
		position = 4,
		section = fixedRange2Section
	)
	default double fixedRange2LineWidth() { return DEFAULT_LINE_WIDTH; }


	// =========================================
	// FIXED RANGE 3 ITEMS
	// =========================================

	@Range(min = 1)
	@ConfigItem(
		keyName = "fixedRange3Distance",
		name = "Attack range",
		description = "The specific distance (in tiles) for this fixed line of sight",
		position = 1,
		section = fixedRange3Section
	)
	default int fixedRange3Distance() { return 5; }

	@Alpha
	@ConfigItem(
		keyName = "fixedRange3OutlineColor",
		name = "Outline color",
		description = "The color of the outline for this fixed range. Setting alpha to 0 disables it.",
		position = 2,
		section = fixedRange3Section
	)
	default Color fixedRange3OutlineColor() { return PluginConstants.DEFAULT_COLOR_OTHER_OUTLINE; }

	@Alpha
	@ConfigItem(
		keyName = "fixedRange3FillColor",
		name = "Fill color",
		description = "The fill color for this fixed range. Setting alpha to 0 disables it.",
		position = 3,
		section = fixedRange3Section
	)
	default Color fixedRange3FillColor() { return PluginConstants.DEFAULT_COLOR_OTHER_FILL; }

	@ConfigItem(
		keyName = "fixedRange3LineWidth",
		name = "Line width",
		description = "The thickness of the outline for this fixed range",
		position = 4,
		section = fixedRange3Section
	)
	default double fixedRange3LineWidth() { return DEFAULT_LINE_WIDTH; }


	// =========================================
	// FIXED RANGE 4 ITEMS
	// =========================================

	@Range(min = 1)
	@ConfigItem(
		keyName = "fixedRange4Distance",
		name = "Attack range",
		description = "The specific distance (in tiles) for this fixed line of sight",
		position = 1,
		section = fixedRange4Section
	)
	default int fixedRange4Distance() { return 5; }

	@Alpha
	@ConfigItem(
		keyName = "fixedRange4OutlineColor",
		name = "Outline color",
		description = "The color of the outline for this fixed range. Setting alpha to 0 disables it.",
		position = 2,
		section = fixedRange4Section
	)
	default Color fixedRange4OutlineColor() { return PluginConstants.DEFAULT_COLOR_OTHER_OUTLINE; }

	@Alpha
	@ConfigItem(
		keyName = "fixedRange4FillColor",
		name = "Fill color",
		description = "The fill color for this fixed range. Setting alpha to 0 disables it.",
		position = 3,
		section = fixedRange4Section
	)
	default Color fixedRange4FillColor() { return PluginConstants.DEFAULT_COLOR_OTHER_FILL; }

	@ConfigItem(
		keyName = "fixedRange4LineWidth",
		name = "Line width",
		description = "The thickness of the outline for this fixed range",
		position = 4,
		section = fixedRange4Section
	)
	default double fixedRange4LineWidth() { return DEFAULT_LINE_WIDTH; }


	// =========================================
	// FIXED RANGE 5 ITEMS
	// =========================================

	@Range(min = 1)
	@ConfigItem(
		keyName = "fixedRange5Distance",
		name = "Attack range",
		description = "The specific distance (in tiles) for this fixed line of sight",
		position = 1,
		section = fixedRange5Section
	)
	default int fixedRange5Distance() { return 5; }

	@Alpha
	@ConfigItem(
		keyName = "fixedRange5OutlineColor",
		name = "Outline color",
		description = "The color of the outline for this fixed range. Setting alpha to 0 disables it.",
		position = 2,
		section = fixedRange5Section
	)
	default Color fixedRange5OutlineColor() { return PluginConstants.DEFAULT_COLOR_OTHER_OUTLINE; }

	@Alpha
	@ConfigItem(
		keyName = "fixedRange5FillColor",
		name = "Fill color",
		description = "The fill color for this fixed range. Setting alpha to 0 disables it.",
		position = 3,
		section = fixedRange5Section
	)
	default Color fixedRange5FillColor() { return PluginConstants.DEFAULT_COLOR_OTHER_FILL; }

	@ConfigItem(
		keyName = "fixedRange5LineWidth",
		name = "Line width",
		description = "The thickness of the outline for this fixed range",
		position = 4,
		section = fixedRange5Section
	)
	default double fixedRange5LineWidth() { return DEFAULT_LINE_WIDTH; }
}