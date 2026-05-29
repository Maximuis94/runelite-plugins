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

import static com.dynamiclos.PluginConstants.PLUGIN_CONFIG_GROUP;
import static com.dynamiclos.PluginConstants.SPELL_CAST_ATTACK_RANGE;
import static com.dynamiclos.PluginConstants.STAFF_AUTOCAST_STYLE_INDEX;
import static com.dynamiclos.PluginConstants.STAFF_DEFENSIVE_AUTOCAST_STYLE_INDEX;
import static com.dynamiclos.PluginConstants.STAFF_EQUIPMENT_TYPE_ID;
import com.google.inject.Provides;
import java.awt.Color;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.KeyCode;
import net.runelite.api.Menu;
import net.runelite.api.WorldView;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.HotkeyListener;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
	name = "Dynamic line of sight",
	description = "Adds customizable player and NPC Lines of Sight. Uses current weapon and Myopia tier. NPC and player LoS can also be drawn under cursor.",
	tags = {"combat","weapon", "attack", "range", "line", "sight", "los", "myopia", "fortis", "colosseum", "fight", "caves", "inferno", "safespot", "safe", "spot", "npc"}
)
public class DynamicLineOfSightPlugin extends Plugin
{

	@Provides
	DynamicLineOfSightConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DynamicLineOfSightConfig.class);
	}

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DynamicLineOfSightConfig config;

	@Inject
	private LineOfSightOverlay overlay;

	@Inject
	private KeyManager keyManager;

	@Inject
	private ChatboxPanelManager chatboxPanelManager;

	@Inject
	private ConfigManager configManager;

	@Getter
	private int cachedAttackRange = 1;
	private boolean configUpdatePending = false;

	private static final int COLOSSEUM_REGION_ID = 7175;
	private static final int MYOPIA_VARBIT_ID = VarbitID.COLOSSEUM_MODIFIER_MYOPIA_STACKS_CLIENT;
	private static final int WORN_EQUIPMENT_ID = InventoryID.WORN;
	private static final int ATTACK_STYLE_VARP_ID = VarPlayerID.COM_MODE;
	private static final int EQUIPPED_WEAPON_TYPE_VARBIT_ID = VarbitID.COMBAT_WEAPON_CATEGORY;

	private final static int MIN_PLAYER_ATTACK_RANGE = 1;
	private final static int MAX_PLAYER_ATTACK_RANGE = 10;
	private final static int WEAPON_SLOT_INDEX = EquipmentInventorySlot.WEAPON.getSlotIdx();
	private static final int LONGRANGE_STYLE_INDEX = 3;
	private static final int LONGRANGE_EXTENSION = 2;
	private static final Set<Integer> LONGRANGE_STYLE_IDS = Set.of(3, 5, 7, 19, 24);


	private int equippedWeaponId = -1;
	private int equippedWeaponType = -1;
	private int attackStyleIndex = -1;
	private boolean hasLongRangeWeapon = false;
	private int longRangeBonus = 0;

	private boolean isInColosseum = false;
	private int myopiaTier = 0;
	private int myopiaReduction = 0;
	private boolean isAffectedByMyopia = false;

	private boolean showMenuEntry = false;
	private boolean showGameMessage = false;
	private int MIN_CHAT_RANGE = 0;
	private int MAX_CHAT_RANGE = 20;

	private Color meleeColor;
	private Color rangedColor;
	private Color magicColor;
	private Color hybridColor;

	private final Map<CombatStyle, Map<String, Integer>> npcDistancesByName = new EnumMap<>(CombatStyle.class);
	private final Map<CombatStyle, Map<Integer, Integer>> npcDistancesById = new EnumMap<>(CombatStyle.class);

	@Override
	protected void startUp() throws Exception
	{
		overlay.parseConfigs();
		parseConfigColors();
		overlayManager.add(overlay);
		clientThread.invokeLater(this::initialize);
		keyManager.registerKeyListener(losHotkeyListener);
		keyManager.registerKeyListener(virtualPlayerLosHotkeyListener);
		keyManager.registerKeyListener(togglePlayerLosHotkeyListener);
		keyManager.registerKeyListener(toggleNpcLosHotkeyListener);
		keyManager.registerKeyListener(virtualNpcLosHotkeyListener);
	}

	@Override
	protected void shutDown() throws Exception
	{
		keyManager.unregisterKeyListener(losHotkeyListener);
		keyManager.unregisterKeyListener(virtualPlayerLosHotkeyListener);
		keyManager.unregisterKeyListener(togglePlayerLosHotkeyListener);
		keyManager.unregisterKeyListener(toggleNpcLosHotkeyListener);
		keyManager.unregisterKeyListener(virtualNpcLosHotkeyListener);
		overlayManager.remove(overlay);
	}

	/**
	 * Return true if the given ConfigChanged event is relevant for this plugin
	 */
	private static boolean isPluginConfig(ConfigChanged event)
	{
		return event.getGroup().equals(PLUGIN_CONFIG_GROUP);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!isPluginConfig(event)) return;

		if (!configUpdatePending)
		{
			configUpdatePending = true;
			clientThread.invokeLater(() ->
			{
				configUpdatePending = false;
				overlay.parseConfigs();
				updateCachedRange();
				parseConfigColors();
			});
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == WORN_EQUIPMENT_ID)
		{
			Item weapon = event.getItemContainer().getItem(WEAPON_SLOT_INDEX);
			if (weapon == null) return;

			int weaponId = weapon.getId();
			if (weaponId != equippedWeaponId)
			{
				log.debug("Equipped weapon changed from {} to {}", equippedWeaponId, weaponId);
				equippedWeaponId = weaponId;
				updateCachedRange();
			}
		}
	}

	private Color getColorFromConfig(CombatStyle combatStyle)
	{

		Color color = Color.decode(configManager.getConfiguration(PLUGIN_CONFIG_GROUP, combatStyle.npcOutlineColorKey));
		if (color.getAlpha() == 0) color = Color.decode(configManager.getConfiguration(PLUGIN_CONFIG_GROUP, combatStyle.npcFillColorKey));
		return new Color(color.getRed(), color.getGreen(), color.getBlue());
	}

	private void parseConfigColors()
	{
		meleeColor = getColorFromConfig(CombatStyle.MELEE);
		rangedColor = getColorFromConfig(CombatStyle.RANGED);
		magicColor = getColorFromConfig(CombatStyle.MAGIC);
		hybridColor = getColorFromConfig(CombatStyle.OTHER);

		parseNpcDistances(CombatStyle.MELEE, config.meleeNpcDefs());
		parseNpcDistances(CombatStyle.RANGED, config.rangedNpcDefs());
		parseNpcDistances(CombatStyle.MAGIC, config.magicNpcDefs());
		parseNpcDistances(CombatStyle.OTHER, config.otherNpcDefs());

		showMenuEntry = config.enableNpcLosMenu();
		showGameMessage = config.broadcastGameMessageNpcLos();
	}

	private void parseNpcDistances(CombatStyle style, String configValue)
	{
		Map<String, Integer> byName = new HashMap<>();
		Map<Integer, Integer> byId = new HashMap<>();

		if (configValue != null && !configValue.trim().isEmpty()) {
			String normalizedConfig = configValue.replace("\r\n", "\n").replace(",", "\n");
			String[] entries = normalizedConfig.split("\n");

			for (String entry : entries) {
				if (entry.trim().isEmpty()) continue;
				String[] parts = entry.split("\\|");
				if (parts.length != 2) continue;

				String identifier = parts[0].trim().toLowerCase();
				try {
					String rangeStr = parts[1].trim();
					if (rangeStr.endsWith("*")) {
						rangeStr = rangeStr.substring(0, rangeStr.length() - 1); // remove diagonal indicator if present
					}
					int range = Integer.parseInt(rangeStr);

					try {
						int id = Integer.parseInt(identifier);
						byId.put(id, range);
					} catch (NumberFormatException e) {
						byName.put(identifier, range);
					}
				} catch (NumberFormatException e) { }
			}
		}
		npcDistancesByName.put(style, byName);
		npcDistancesById.put(style, byId);
	}

	private String getMenuOptionText(String baseText, Color color, NPC npc, CombatStyle style)
	{
		Integer dist = null;
		if (npc != null) {
			Map<Integer, Integer> byId = npcDistancesById.get(style);
			if (byId != null && byId.containsKey(npc.getId())) {
				dist = byId.get(npc.getId());
			} else {
				Map<String, Integer> byName = npcDistancesByName.get(style);
				if (byName != null && npc.getName() != null) {
					dist = byName.get(npc.getName().toLowerCase());
				}
			}
		}

		String label = baseText;
		if (dist != null) {
			label += " (" + dist + ")";
		}
		return ColorUtil.wrapWithColorTag(label, color);
	}

	private void updateInColosseum()
	{
		WorldView topLevelWorldView = client.getTopLevelWorldView();

		if (topLevelWorldView != null)
		{
			int[] mapRegions = topLevelWorldView.getMapRegions();

			if (mapRegions != null)
			{
				for (int region : mapRegions)
				{
					if (region == COLOSSEUM_REGION_ID)
					{
						log.debug("Player has entered the Colosseum");
						isInColosseum = true;
						updateMyopiaReduction();
						return;
					}
				}
				if (myopiaTier > 0)
				{
					resetInColosseum();
					updateCachedRange();
				}
			}
		}
	}

	private void resetInColosseum()
	{
		log.debug("Resetting Colosseum-related values");
		myopiaTier = 0;
		isInColosseum = false;
		updateMyopiaReduction();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();

		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING || state == GameState.CONNECTION_LOST)
		{
			resetInColosseum();
			return;
		}

		if (state == GameState.LOGGED_IN)
		{
			updateInColosseum();
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int vId = event.getVarbitId();
		if (vId == EQUIPPED_WEAPON_TYPE_VARBIT_ID)
		{
			int newEquippedWeaponType = client.getVarbitValue(EQUIPPED_WEAPON_TYPE_VARBIT_ID);
			hasLongRangeWeapon = LONGRANGE_STYLE_IDS.contains(newEquippedWeaponType);
			log.debug("Equipped Weapon Type was changed from {} to {}", equippedWeaponType, newEquippedWeaponType);
			equippedWeaponType = newEquippedWeaponType;
			updateActiveLongRangeBonus();
			updateCachedRange();
			return;
		}

		else if (vId == MYOPIA_VARBIT_ID)
		{
			myopiaTier = client.getVarbitValue(MYOPIA_VARBIT_ID);
			isInColosseum = true;
			updateMyopiaReduction();
			updateCachedRange();
			return;
		}

		int pId = event.getVarpId();
		if (pId == ATTACK_STYLE_VARP_ID)
		{
			int newIndex = client.getVarpValue(ATTACK_STYLE_VARP_ID);
			log.debug("AttackStyleIndex was changed from {} to {}", attackStyleIndex, newIndex);
			attackStyleIndex = client.getVarpValue(ATTACK_STYLE_VARP_ID);
			updateActiveLongRangeBonus();
			updateCachedRange();
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (!showMenuEntry || !client.isKeyPressed(KeyCode.KC_SHIFT))
		{
			return;
		}

		if ("Examine".equals(event.getOption()) && event.getMenuEntry().getNpc() != null)
		{
			NPC npc = event.getMenuEntry().getNpc();

			if (npc.getComposition() == null || npc.getComposition().getActions() == null)
			{
				return;
			}

			boolean isAttackable = false;
			for (String action : npc.getComposition().getActions())
			{
				if (action != null && action.equalsIgnoreCase("Attack"))
				{
					isAttackable = true;
					break;
				}
			}

			if (!isAttackable)
			{
				return;
			}

			String target = event.getTarget();

			MenuEntry parent = client.getMenu().createMenuEntry(-1)
				.setOption("Set/clear los")
				.setTarget(target)
				.setType(MenuAction.RUNELITE);

			Menu submenu = parent.createSubMenu();

			// Conditionally add the "Clear all" option
			if (hasAnyLosConfigured(npc))
			{
				submenu.createMenuEntry(-1)
					.setOption("Clear all")
					.setType(MenuAction.RUNELITE)
					.onClick(e -> clearAllLos(npc));
			}

			submenu.createMenuEntry(-1)
				.setOption(getMenuOptionText("Other", hybridColor, npc, CombatStyle.OTHER))
				.setType(MenuAction.RUNELITE)
				.onClick(e -> handleLosConfiguration(npc, CombatStyle.OTHER, hybridColor));

			submenu.createMenuEntry(-1)
				.setOption(getMenuOptionText("Magic", magicColor, npc, CombatStyle.MAGIC))
				.setType(MenuAction.RUNELITE)
				.onClick(e -> handleLosConfiguration(npc, CombatStyle.MAGIC, magicColor));

			submenu.createMenuEntry(-1)
				.setOption(getMenuOptionText("Ranged", rangedColor, npc, CombatStyle.RANGED))
				.setType(MenuAction.RUNELITE)
				.onClick(e -> handleLosConfiguration(npc, CombatStyle.RANGED, rangedColor));

			submenu.createMenuEntry(-1)
				.setOption(getMenuOptionText("Melee", meleeColor, npc, CombatStyle.MELEE))
				.setType(MenuAction.RUNELITE)
				.onClick(e -> handleLosConfiguration(npc, CombatStyle.MELEE, meleeColor));
		}
	}

	private boolean hasAnyLosConfigured(NPC npc)
	{
		if (npc == null) return false;

		String name = npc.getName() != null ? npc.getName().toLowerCase() : null;
		int id = npc.getId();

		for (CombatStyle style : CombatStyle.values())
		{
			Map<Integer, Integer> byId = npcDistancesById.get(style);
			if (byId != null && byId.containsKey(id))
			{
				return true;
			}

			Map<String, Integer> byName = npcDistancesByName.get(style);
			if (byName != null && name != null && byName.containsKey(name))
			{
				return true;
			}
		}
		return false;
	}

	private boolean hasLosConfigured(NPC npc, CombatStyle combatStyle)
	{
		if (npc == null) return false;

		String name = npc.getName() != null ? npc.getName().toLowerCase() : null;
		int id = npc.getId();

		Map<Integer, Integer> byId = npcDistancesById.get(combatStyle);
		if (byId != null && byId.containsKey(id))
		{
			return true;
		}

		Map<String, Integer> byName = npcDistancesByName.get(combatStyle);
		if (byName != null && name != null && byName.containsKey(name))
		{
			return true;
		}

		return false;
	}

	private void clearAllLos(NPC npc)
	{
		String npcName = npc.getName();
		if (npcName == null) return;

		boolean removedAny = false;
		for (CombatStyle style : CombatStyle.values())
		{
			String configKey = getConfigKeyForStyle(style);
			String currentConfig = configManager.getConfiguration(PluginConstants.PLUGIN_CONFIG_GROUP, configKey);
			if (currentConfig == null) currentConfig = "";

			if (isNpcInConfig(npcName, currentConfig))
			{
				String newConfig = removeNpcFromConfig(npcName, currentConfig);
				configManager.setConfiguration(PluginConstants.PLUGIN_CONFIG_GROUP, configKey, newConfig);
				removedAny = true;
			}
		}

		if (removedAny)
		{
			sendGameMessage("Cleared all LoS configurations for " + ColorUtil.wrapWithColorTag(npcName, Color.RED) + ".");
		}
	}

	/**
	 * Sends a specific message into the chatbox, but only if the configurations allow for it
	 */
	private void sendGameMessage(String message)
	{
		if (showGameMessage)
		{
			clientThread.invokeLater(() -> {
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, "");
			});
		}
	}

	private String inputTextWithNothing(String combatStyleFormatted, String npcName)
	{
		return "Enter " + combatStyleFormatted + " LoS distance for NPC " + npcName + " (Set to 1-20, 1*, or nothing)";
	}

	private String inputTextWithoutNothing(String combatStyleFormatted, String npcName)
	{
		return "Enter " + combatStyleFormatted + " LoS distance for NPC " + npcName + " (Set to 1-20, 1*)";
	}

	private void handleLosConfiguration(NPC npc, CombatStyle style, Color color)
	{
		String npcName = npc.getName();
		if (npcName == null) return;

		String configKey = getConfigKeyForStyle(style);
		String currentConfig = configManager.getConfiguration(PluginConstants.PLUGIN_CONFIG_GROUP, configKey);
		if (currentConfig == null) currentConfig = "";

		final boolean hasLos = hasLosConfigured(npc, style);

		String styleFormatted = ColorUtil.wrapWithColorTag(style.name().toLowerCase(), color);

		String textInputText = hasLos ? inputTextWithNothing(styleFormatted, npcName) : inputTextWithoutNothing(styleFormatted, npcName);

		final String existingConfig = currentConfig;
		chatboxPanelManager.openTextInput(textInputText)
			.onDone((input) -> {
				try
				{
					String newEntry;
					String newConfig;
					String range;
					String i = input.strip();

					if (i.isEmpty())
					{
						if (hasLos)
						{
							newConfig = removeNpcFromConfig(npcName, existingConfig);
							configManager.setConfiguration(PluginConstants.PLUGIN_CONFIG_GROUP, configKey, newConfig);
							sendGameMessage("Removed " + styleFormatted + " line of sight for " + npcName);
						}
						return;
					}
					else if (i.equals("1*"))
					{
						newEntry = npcName + "|" + i;
						range = i;
					}
					else
					{
						int rangeInt = Integer.parseInt(i);
						range =  String.valueOf(rangeInt);

						if (rangeInt < MIN_CHAT_RANGE || rangeInt > MAX_CHAT_RANGE)
						{
							sendGameMessage("Range of " + range + " is outside of the range of " + MIN_CHAT_RANGE + "-" + MAX_CHAT_RANGE + ". For a value larger than " + MAX_CHAT_RANGE + ", set it manually in config.");
							return;
						}

						newEntry = npcName + "|" + range;
					}
					newConfig = existingConfig.isEmpty() ? newEntry : existingConfig + "\n" + newEntry;
					configManager.setConfiguration(PluginConstants.PLUGIN_CONFIG_GROUP, configKey, newConfig);
					sendGameMessage("Set " + styleFormatted + " line of sight range for NPC " + npcName + " to " + range);
				}
				catch (NumberFormatException e)
				{
					log.warn("Invalid range input provided for NPC LoS: {}", input);
				}
			})
			.build();

	}

	private String getConfigKeyForStyle(CombatStyle style)
	{
		switch (style)
		{
			case MELEE: return "meleeNpcDefs";
			case RANGED: return "rangedNpcDefs";
			case MAGIC: return "magicNpcDefs";
			case OTHER: return "otherNpcDefs";
			default: return "";
		}
	}

	private boolean isNpcInConfig(String npcName, String currentConfig)
	{
		String searchStr = npcName.toLowerCase() + "|";
		return Arrays.stream(currentConfig.split("\n"))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.anyMatch(s -> s.toLowerCase().startsWith(searchStr));
	}

	private String removeNpcFromConfig(String npcName, String currentConfig)
	{
		String searchStr = npcName.toLowerCase() + "|";
		return Arrays.stream(currentConfig.split("\n"))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.filter(s -> !s.toLowerCase().startsWith(searchStr))
			.collect(Collectors.joining("\n"));
	}

	@Getter
	private boolean isHotkeyHeld = false;

	private final HotkeyListener losHotkeyListener = new HotkeyListener(() -> config.npcLosHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			isHotkeyHeld = true;
		}

		@Override
		public void hotkeyReleased()
		{
			isHotkeyHeld = false;
		}
	};

	@Getter
	private boolean isVirtualPlayerLosHotkeyHeld = false;

	private final HotkeyListener virtualPlayerLosHotkeyListener = new HotkeyListener(() -> config.virtualPlayerLosHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			isVirtualPlayerLosHotkeyHeld = true;
		}

		@Override
		public void hotkeyReleased()
		{
			isVirtualPlayerLosHotkeyHeld = false;
		}
	};

	@Getter
	private boolean playerLosToggledOff = false;

	private final HotkeyListener togglePlayerLosHotkeyListener = new HotkeyListener(() -> config.togglePlayerLosHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			playerLosToggledOff = !playerLosToggledOff;
		}
	};

	@Getter
	private boolean npcLosToggledOff = false;

	private final HotkeyListener toggleNpcLosHotkeyListener = new HotkeyListener(() -> config.toggleNpcLosHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			npcLosToggledOff = !npcLosToggledOff;
		}
	};

	@Getter
	private boolean isVirtualNpcLosHotkeyHeld = false;

	private final HotkeyListener virtualNpcLosHotkeyListener = new HotkeyListener(() -> config.virtualNpcLosHotkey())
	{
		@Override
		public void hotkeyPressed()
		{
			isVirtualNpcLosHotkeyHeld = true;
		}

		@Override
		public void hotkeyReleased()
		{
			isVirtualNpcLosHotkeyHeld = false;
		}
	};

	/**
	 * Initialization method that attempts to determine all dynamic values that may affect range.
	 */
	private void initialize()
	{
		int equippedWeaponType = client.getVarbitValue(EQUIPPED_WEAPON_TYPE_VARBIT_ID);
		hasLongRangeWeapon = LONGRANGE_STYLE_IDS.contains(equippedWeaponType);
		attackStyleIndex = client.getVarpValue(ATTACK_STYLE_VARP_ID);

		updateInColosseum();
		updateCachedRange();
	}

	/**
	 * Updates the value of myopiaReduction and logs changes
	 */
	private void updateMyopiaReduction()
	{
		if (!isInColosseum)
		{
			log.debug("Player is not in Colosseum - updated myopiaReduction from {} to 0", myopiaReduction);
			myopiaTier = 0;
		}
		int newValue = myopiaTier * 2;
		if (newValue != myopiaReduction)
		{
			log.debug("Updated myopiaReduction from {} to {}", myopiaReduction, newValue);
			myopiaReduction = newValue;
			overlay.setMyopiaReduction(myopiaReduction);
		}
	}

	/**
	 * Updates the active long range bonus based on weapon and attack style index.
	 */
	private void updateActiveLongRangeBonus()
	{
		longRangeBonus = hasLongRangeWeapon && attackStyleIndex == LONGRANGE_STYLE_INDEX ? LONGRANGE_EXTENSION : 0;
		log.debug("Longrange bonus is {}", longRangeBonus == LONGRANGE_EXTENSION ? "active" : "inactive");
	}

	/**
	 * Return true if the equippedWeaponType and attackStyleIndex suggest the Player is autocasting spells
	 */
	private boolean isAutocasting()
	{
		return equippedWeaponType == STAFF_EQUIPMENT_TYPE_ID && attackStyleIndex == STAFF_AUTOCAST_STYLE_INDEX || attackStyleIndex == STAFF_DEFENSIVE_AUTOCAST_STYLE_INDEX;
	}

	/**
	 * Updates the cached range by checking the weapon and current attack style.
	 */
	private void updateCachedRange() {
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}

		int baseRange = isAutocasting() ? SPELL_CAST_ATTACK_RANGE : WeaponRangeConstants.getBaseRange(equippedWeaponId);

		int computedRange = Math.min(baseRange + longRangeBonus, MAX_PLAYER_ATTACK_RANGE) - myopiaReduction;
		int newRange = Math.max(computedRange, MIN_PLAYER_ATTACK_RANGE);

		boolean newIsAffectedByMyopia = baseRange > 1 && myopiaReduction > 0;

		if (newRange != cachedAttackRange || newIsAffectedByMyopia != isAffectedByMyopia)
		{
			log.debug("Updated player attack range parameters from {} to {}", cachedAttackRange, newRange);
			cachedAttackRange = newRange;
			isAffectedByMyopia = newIsAffectedByMyopia;
			overlay.setActiveAttackRange(cachedAttackRange, isAffectedByMyopia);
		}
	}
}