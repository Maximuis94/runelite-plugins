package com.slayerbanktab;

import static com.slayerbanktab.PluginConstants.CONFIG_GROUP;
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

	// --- AUXILIARY TOGGLES ---

	@ConfigItem(
		keyName = "customAdditionalItemMappings",
		name = "Custom additional item mappings",
		description = "Map extra items to a primary weapon or gear piece.<br>" +
			"Format: Primary Item, Item 1, Item 2, <br>" +
			"Alternatively, for mapping two items both ways: Item 1|Item 2, <br>" +
			"Example: Tumeken's shadow, Soul rune, Chaos rune<br>" +
			"        Herb sack|Open herb sack.<br>" +
			"Accepts exact item names or IDs. One mapping per line.",
		position = 4,
		section = additionalItemSection
	)
	default String customAdditionalItemMappings() {
		return "";
	}

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
}