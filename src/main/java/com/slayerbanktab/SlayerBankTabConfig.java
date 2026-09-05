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

package com.slayerbanktab;

import static com.slayerbanktab.PluginConstants.CONFIG_GROUP;
import com.slayerbanktab.models.LayoutMode;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(CONFIG_GROUP)
public interface SlayerBankTabConfig extends Config {

	// --- SECTIONS ---

	@ConfigSection(
		name = "General",
		description = "Core settings for the Slayer Bank Tabs plugin",
		position = 0
	)
	String generalSection = "generalSection";

	@ConfigSection(
		name = "Additional items",
		description = "Section with options that add items from pouches or used to charge items.",
		position = 1
	)
	String additionalItemSection = "additionalItemSection";

	// --- GENERAL SETTINGS ---

	@ConfigItem(
		keyName = "enableInventoryIconMenuOption",
		name = "Add inventory icon menu option",
		description = "Adds a menu option when shift+right-clicking the Inventory icon for registering current setup to the slayer tab",
		position = 2,
		section = generalSection
	)
	default boolean enableInventoryIconMenuOption() {
		return false;
	}

	@ConfigItem(
		keyName = "mergeTuraelTasks",
		name = "Merge Turael task tabs",
		description = "Merges all Turael task tabs into a single slayer task tab to accommodate <br>" +
			"Turael skipping, adding exactly one setup used for all Turael tasks.",
		position = 3,
		section = generalSection
	)
	default boolean mergeTuraelTasks() {
		return false;
	}

	@ConfigItem(
		keyName = "mergeOtherSetups",
		name = "Merge non-Wildy/Konar layouts",
		description = "Groups layouts for standard masters (e.g. Duradel, Nieve, Chaeldar) <br>" +
			" together so you only need one layout per target assigned by any regular Slayer master.",
		position = 4,
		section = generalSection
	)
	default boolean mergeOtherSetups() {
		return true;
	}

	@ConfigItem(
		keyName = "autoAssignUndefinedSetups",
		name = "Auto-assign undefined layouts",
		description =
			"If the current task does not have a configured bank tab, <br>" +
				"inventory+equipment are cached after closing the bank and <br>" +
				"configured automatically as soon as the first task kc is made. <br>" +
				"You will also be notified via a game message, depending on configurations.",
		position = 5,
		section = generalSection
	)
	default boolean autoAssignUndefinedSetups() {
		return true;
	}

	@ConfigItem(
		keyName = "notifyOnCache",
		name = "Notify on layout cache",
		description =
			"If 'Auto-assign undefined layouts' is enabled, you will be <br>" +
				"notified that the current setup will be saved to the current <br>" +
				"task after making the first KC via a game message right after <br>" +
				"closing the bank.",
		position = 6,
		section = generalSection
	)
	default boolean notifyOnCache() {
		return false;
	}

	@ConfigItem(
		keyName = "enableAddItemToSlayTabMenuOption",
		name = "'Add item to tab' menu option",
		description =
			"If enabled, add a menu option to items in the bank <br>" +
				"that may be used to add the right clicked item to <br>" +
				"the currently active slayer tab",
		position = 7,
		section = generalSection
	)
	default boolean enableAddItemToSlayTabMenuOption() {
		return false;
	}

	@ConfigItem(
		keyName = "layoutMode",
		name = "Layout mode",
		description =
			"The layout mode that is to be applied.<br>" +
				"Default shows the equipment worn in the first 3 columns<br>" +
				"and the inventory slots in the final 4 columns, whereas<br>" +
				"the zigzag layout places the equipment items in the first 2 rows <br>" +
				"and the inventory in the subsequent rows.<br>" +
				"Additional rows will behave the same.",
		position = 8,
		section = generalSection
	)
	default LayoutMode layoutMode() {
		return LayoutMode.DEFAULT;
	}

	// --- AUXILIARY TOGGLES ---

	@ConfigItem(
		keyName = "autoAddRunePouchRunes",
		name = "Auto-add rune pouch runes",
		description = "Adds the runes in the rune pouch as additional items to the setup, if applicable.",
		position = 1,
		section = additionalItemSection
	)
	default boolean autoAddRunePouchRunes() {
		return true;
	}

	@ConfigItem(
		keyName = "autoAddBoltPouchBolts",
		name = "Auto-add bolt pouch bolts",
		description = "Adds the bolts in the bolt pouch as additional items to the setup, if applicable.",
		position = 2,
		section = additionalItemSection
	)
	default boolean autoAddBoltPouchBolts() {
		return true;
	}

	@ConfigItem(
		keyName = "autoAddLootingBagContents",
		name = "Auto-add looting bag placeholders",
		description = "Automatically adds the contents of the looting bag as <br>" +
			"additional items, to use as placeholders <br>" +
			"in the looting bag, provided there is a looting bag <br>" +
			"in the inventory",
		position = 3,
		section = additionalItemSection
	)
	default boolean autoAddLootingBagContents() {
		return false;
	}

	@ConfigItem(
		keyName = "autoRefreshAdditionalItems",
		name = "Auto refresh additional items",
		description = "Automatically refresh the additional items upon loading the tab.<br>" +
			"This can be done manually by right-clicking the slayer tab.",
		position = 4,
		section = additionalItemSection
	)
	default boolean autoRefreshAdditionalItems() {
		return true;
	}

	@ConfigItem(
		keyName = "customAdditionalItemMappings",
		name = "Custom additional item mappings",
		description = "Map extra items to a primary weapon or gear piece.<br>" +
			"Format: Primary Item, Item 1, Item 2, <br>" +
			"Alternatively, for mapping two items both ways: Item 1|Item 2, <br>" +
			"Example: Tumeken's shadow, Soul rune, Chaos rune<br>" +
			"        Herb sack|Open herb sack.<br>" +
			"Accepts exact item names or IDs. One mapping per line.",
		position = 5,
		section = additionalItemSection
	)
	default String customAdditionalItemMappings() {
		return "";
	}
}