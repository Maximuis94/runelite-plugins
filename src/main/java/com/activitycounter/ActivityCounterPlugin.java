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

package com.activitycounter;

import static com.activitycounter.PluginConstants.CONFIG_GROUP;
import static com.activitycounter.PluginConstants.PLUGIN_NAME;
import com.activitycounter.models.Category;
import com.activitycounter.models.Count;
import com.activitycounter.models.Session;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = PLUGIN_NAME,
	description = "Plugin that counts activities (e.g. bosses/agility laps) per manually defined session",
	tags = {"session", "tracker", "counter", "kc", "killcount", "activity", "count", "lap"}
)
public class ActivityCounterPlugin extends Plugin {

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ActivityCounterConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private SessionStorageManager storageManager;

	@Provides
	ActivityCounterConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(ActivityCounterConfig.class);
	}

	@Getter
	private Session currentSession;
	private ActivityCounterPanel panel;
	private NavigationButton navButton;

	private final List<ActivityData> activityRegistry = new ArrayList<>();

	private boolean trackExperience = false;
	private boolean trackChatMessages = false;

	@Getter
	private boolean showSessionDuration = false;
	private boolean requiresSaveAndRefresh = false;

	private static final String FARMING_CONTRACT_MESSAGE = "You've completed a Farming Guild Contract. You should return to GuildMaster Jane.";
	private static final int FARMING_CONTRACTS = -3000;

	private static final Pattern RUMOUR_PATTERN = Pattern.compile("You have completed ([\\d,]+) rumours for the Hunter Guild\\.");
	private static final String COMPLETED_PREFIX = "You have completed ";
	private static final int HUNTER_RUMOURS = -2000;

	private static final Pattern MAHOGANY_HOMES_PATTERN = Pattern.compile("You have completed ([\\d,]+) contracts with a total of [\\d,]+ points\\.");
	private static final int MAHOGANY_HOMES = -4000;

	private static final Pattern LAP_PATTERN = Pattern.compile("Your (.+) count is: ([\\d,]+)\\.");
	private static final String LAP_PREFIX = "Your ";

	// Offset used to prevent Varbit IDs from colliding with VarPlayer IDs in the tracker map
	private static final int VARBIT_OFFSET = 100000;

	// Pseudo-IDs for Agility Courses (Negative to prevent collisions)
	private static final int AGILITY_GNOME_STRONGHOLD = -1001;
	private static final int AGILITY_PENGUIN = -1003;
	private static final int AGILITY_BARBARIAN_OUTPOST = -1004;
	private static final int AGILITY_APE_ATOLL = -1005;
	private static final int AGILITY_WILDERNESS = -1006;
	private static final int AGILITY_COLOSSAL_WYRM_ADVANCED = -1007;
	private static final int AGILITY_COLOSSAL_WYRM_BASIC = -1024;
	private static final int AGILITY_WEREWOLF = -1008;
	private static final int AGILITY_PRIFDDINAS = -1009;
	private static final int AGILITY_DRAYNOR_ROOFTOP = -1010;
	private static final int AGILITY_AL_KHARID_ROOFTOP = -1011;
	private static final int AGILITY_VARROCK_ROOFTOP = -1012;
	private static final int AGILITY_CANIFIS_ROOFTOP = -1013;
	private static final int AGILITY_FALADOR_ROOFTOP = -1014;
	private static final int AGILITY_SEERS_ROOFTOP = -1015;
	private static final int AGILITY_POLLNIVNEACH_ROOFTOP = -1016;
	private static final int AGILITY_RELLEKKA_ROOFTOP = -1017;
	private static final int AGILITY_ARDOUGNE_ROOFTOP = -1018;
	private static final int AGILITY_PYRAMID = -1019;
	private static final int AGILITY_DORGESH_KAAN = -1020;
	private static final int AGILITY_BRIMHAVEN = -1021;
	private static final int AGILITY_SHAYZIEN_LOW = -1022;
	private static final int AGILITY_SHAYZIEN_HIGH = -1023;

	// Pseudo-IDs for Skill Experience (Negative to prevent collisions)
	private static final int XP_ATTACK = -5000;
	private static final int XP_DEFENCE = -5001;
	private static final int XP_STRENGTH = -5002;
	private static final int XP_HITPOINTS = -5003;
	private static final int XP_RANGED = -5004;
	private static final int XP_PRAYER = -5005;
	private static final int XP_MAGIC = -5006;
	private static final int XP_COOKING = -5007;
	private static final int XP_WOODCUTTING = -5008;
	private static final int XP_FLETCHING = -5009;
	private static final int XP_FISHING = -5010;
	private static final int XP_FIREMAKING = -5011;
	private static final int XP_CRAFTING = -5012;
	private static final int XP_SMITHING = -5013;
	private static final int XP_MINING = -5014;
	private static final int XP_HERBLORE = -5015;
	private static final int XP_AGILITY = -5016;
	private static final int XP_THIEVING = -5017;
	private static final int XP_SLAYER = -5018;
	private static final int XP_FARMING = -5019;
	private static final int XP_RUNECRAFT = -5020;
	private static final int XP_HUNTER = -5021;
	private static final int XP_CONSTRUCTION = -5022;
	private static final int XP_TOTAL = -5023;

	@AllArgsConstructor
	private static class ActivityData
	{
		String name;
		int trackingId;
		Category category;
		BooleanSupplier isVisible;
	}

	@Override
	protected void startUp() {
		buildActivityRegistry();

		panel = new ActivityCounterPanel(this);
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");
		navButton = NavigationButton.builder()
			.tooltip(PLUGIN_NAME)
			.icon(icon)
			.priority(10)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		updateConfigFlags();
		panel.reloadComboBox();
	}

	@Override
	protected void shutDown() {
		clientToolbar.removeNavigation(navButton);
		if (currentSession != null) {
			storageManager.saveSession(currentSession);
		}
	}

	private void updateConfigFlags()
	{
		trackExperience = config.trackTotalXp() || config.trackAttackXp() || config.trackStrengthXp() ||
			config.trackDefenceXp() || config.trackRangedXp() || config.trackPrayerXp() || config.trackMagicXp() ||
			config.trackRunecraftXp() || config.trackHitpointsXp() || config.trackCraftingXp() ||
			config.trackMiningXp() || config.trackSmithingXp() || config.trackFishingXp() ||
			config.trackCookingXp() || config.trackFiremakingXp() || config.trackWoodcuttingXp() ||
			config.trackAgilityXp() || config.trackHerbloreXp() || config.trackThievingXp() ||
			config.trackFletchingXp() || config.trackSlayerXp() || config.trackFarmingXp() ||
			config.trackConstructionXp() || config.trackHunterXp();
		trackChatMessages = config.trackAgilityLaps() || config.trackHunterRumours() || config.trackFarmingContracts() || config.trackMahoganyHomesContracts();
		showSessionDuration = config.showSessionDuration();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		GameState state = event.getGameState();

		if (state == GameState.LOGGED_IN) {
			long accountHash = client.getAccountHash();
			if (accountHash == -1) return;

			if (currentSession == null) {
				Session activeSession = storageManager.loadActiveSession(accountHash);
				if (activeSession != null) {
					currentSession = activeSession;
					initializeKillCounts();
					log.debug("Resumed active session: {}", currentSession.getId());

					panel.forceActiveSessionSelection();
				}
			}

			updateConfigFlags();
			panel.reloadComboBox();

		} else if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING) {
			if (currentSession != null && state == GameState.LOGIN_SCREEN) {
				storageManager.saveSession(currentSession);
				currentSession = null;
			}

			panel.reloadComboBox();
		}
	}

	public void startSession() {
		clientThread.invokeLater(() -> {
			if (client == null || client.getGameState() != GameState.LOGGED_IN) {
				return;
			}

			String accountName = client.getLocalPlayer() != null
				? client.getLocalPlayer().getName()
				: "Unknown";

			currentSession = new Session(client.getAccountHash(), accountName);
			initializeKillCounts();
			storageManager.saveSession(currentSession);
			log.debug("Session started.");

			panel.reloadComboBox();
		});
	}

	public void closeSession() {
		if (currentSession != null) {
			currentSession.setInProgress(false);
			storageManager.saveSession(currentSession);
			log.debug("Session closed and archived.");
			currentSession = null;

			panel.reloadComboBox();
		}
	}

	public List<Session> getArchivedSessions() {
		if (client == null || client.getGameState() != GameState.LOGGED_IN) {
			return storageManager.loadAllArchivedSessions();
		}
		return storageManager.loadArchivedSessions(client.getAccountHash());
	}

	public void viewArchivedSession(File sessionFile) {
		Session loadedSession = storageManager.loadSession(sessionFile);
		if (loadedSession != null) {
			this.currentSession = loadedSession;
			panel.update(currentSession);
			log.debug("Loaded session: {}", loadedSession.getId());
		}
	}

	public void deleteArchivedSession(Session session) {
		if (session == null || (currentSession != null && currentSession.getId().equals(session.getId()))) {
			return;
		}

		boolean deleted = storageManager.deleteSession(session);
		if (deleted) {
			log.debug("Deleted archived session: {}", session.getId());
			panel.forceActiveSessionSelection();
			panel.reloadComboBox();
		} else {
			log.warn("Failed to delete session file for ID: {}", session.getId());
		}
	}

	/**
	 * Retrieves the Category associated with a specific tracking ID.
	 */
	public Category getActivityCategory(int trackingId) {
		for (ActivityData act : activityRegistry) {
			if (act.trackingId == trackingId) {
				return act.category;
			}
		}
		return Category.OTHER;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals(CONFIG_GROUP)) return;
		updateConfigFlags();
		panel.refreshKcContainer();
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event) {
		if (currentSession == null || !currentSession.isInProgress()) return;

		int varbitId = event.getVarbitId();
		if (varbitId == VarbitID.DATE_SECONDS_PAST_MINUTE) {
			currentSession.addSecond();

			if (showSessionDuration) {
				panel.updateTime();
			}
			return;
		}

		// 1. Process Varp-based changes
		int varpId = event.getVarpId();
		if (currentSession.isTracking(varpId)) {
			processActivityUpdate(varpId, event.getValue());
		}

		// 2. Process Varbit-based changes safely using the offset
		if (varbitId != -1) {
			int trackedVarbitId = varbitId + VARBIT_OFFSET;
			if (currentSession.isTracking(trackedVarbitId)) {
				processActivityUpdate(trackedVarbitId, client.getVarbitValue(varbitId));
			}
		}
	}

	private void processActivityUpdate(int trackingId, int currentValue) {
		Count kc = currentSession.getKillCount(trackingId);

		if (kc.getInitialKc() == -1) {
			kc.setInitialKc(currentValue);
		}

		int newSessionKc = currentValue - kc.getInitialKc();

		if (newSessionKc > kc.getSessionKc()) {
			kc.setSessionKc(newSessionKc);
			requiresSaveAndRefresh = true; // Batched onto the GameTick for performance
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if (!trackChatMessages) return;

		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM) {
			return;
		}

		if (currentSession == null || !currentSession.isInProgress()) {
			return;
		}

		String message = Text.removeTags(event.getMessage());

		if (message.startsWith(LAP_PREFIX))
		{
			Matcher matcher = LAP_PATTERN.matcher(message);

			if (matcher.find())
			{
				String courseName = matcher.group(1);
				int newLapCount = Integer.parseInt(matcher.group(2).replace(",", ""));

				int pseudoId = -1;
				switch (courseName)
				{
					case "Gnome Stronghold Agility lap": pseudoId = AGILITY_GNOME_STRONGHOLD; break;
					case "Shayzien Basic Agility Course lap": pseudoId = AGILITY_SHAYZIEN_LOW; break;
					case "Shayzien Advanced Agility Course lap": pseudoId = AGILITY_SHAYZIEN_HIGH; break;
					case "Penguin Agility lap": pseudoId = AGILITY_PENGUIN; break;
					case "Barbarian Outpost lap": pseudoId = AGILITY_BARBARIAN_OUTPOST; break;
					case "Ape Atoll Agility lap": pseudoId = AGILITY_APE_ATOLL; break;
					case "Wilderness Agility lap": pseudoId = AGILITY_WILDERNESS; break;
					case "Colossal Wyrm Agility Course (Advanced) lap": pseudoId = AGILITY_COLOSSAL_WYRM_ADVANCED; break;
					case "Colossal Wyrm Agility Course (Basic) lap": pseudoId = AGILITY_COLOSSAL_WYRM_BASIC; break;
					case "Werewolf Agility lap": pseudoId = AGILITY_WEREWOLF; break;
					case "Prifddinas Agility Course lap": pseudoId = AGILITY_PRIFDDINAS; break;
					case "Draynor Village Rooftop lap": pseudoId = AGILITY_DRAYNOR_ROOFTOP; break;
					case "Al Kharid Rooftop lap": pseudoId = AGILITY_AL_KHARID_ROOFTOP; break;
					case "Varrock Rooftop lap": pseudoId = AGILITY_VARROCK_ROOFTOP; break;
					case "Canifis Rooftop lap": pseudoId = AGILITY_CANIFIS_ROOFTOP; break;
					case "Falador Rooftop lap": pseudoId = AGILITY_FALADOR_ROOFTOP; break;
					case "Seers' Village Rooftop lap": pseudoId = AGILITY_SEERS_ROOFTOP; break;
					case "Pollnivneach Rooftop lap": pseudoId = AGILITY_POLLNIVNEACH_ROOFTOP; break;
					case "Rellekka Rooftop lap": pseudoId = AGILITY_RELLEKKA_ROOFTOP; break;
					case "Ardougne Rooftop lap": pseudoId = AGILITY_ARDOUGNE_ROOFTOP; break;
					case "Agility Pyramid lap": pseudoId = AGILITY_PYRAMID; break;
					case "Dorgesh-Kaan Agility lap": pseudoId = AGILITY_DORGESH_KAAN; break;
					case "Agility Arena Total Ticket": pseudoId = AGILITY_BRIMHAVEN; break;
				}

				if (pseudoId != -1 && currentSession.isTracking(pseudoId))
				{
					processChatboxUpdate(pseudoId, newLapCount);
				}
			}
		}

		else if (message.startsWith(COMPLETED_PREFIX)) {
			Matcher rumourMatcher = RUMOUR_PATTERN.matcher(message);
			if (rumourMatcher.find()) {
				int newRumourCount = Integer.parseInt(rumourMatcher.group(1).replace(",", ""));
				if (currentSession.isTracking(HUNTER_RUMOURS)) {
					processChatboxUpdate(HUNTER_RUMOURS, newRumourCount);
				}
				return;
			}

			Matcher mahoganyMatcher = MAHOGANY_HOMES_PATTERN.matcher(message);
			if (mahoganyMatcher.find()) {
				int newContractCount = Integer.parseInt(mahoganyMatcher.group(1).replace(",", ""));
				if (currentSession.isTracking(MAHOGANY_HOMES)) {
					processChatboxUpdate(MAHOGANY_HOMES, newContractCount);
				}
			}
		}

		else if (message.equals(FARMING_CONTRACT_MESSAGE)) {
			if (currentSession.isTracking(FARMING_CONTRACTS)) {
				Count kc = currentSession.getKillCount(FARMING_CONTRACTS);
				kc.setSessionKc(kc.getSessionKc() + 1);
				requiresSaveAndRefresh = true;
			}
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event) {
		if (!trackExperience) return;

		if (currentSession == null || !currentSession.isInProgress()) return;

		boolean isXpUpdated = false;

		if (currentSession.updateSkillXp(event.getSkill(), event.getXp())) {
			requiresSaveAndRefresh = true;
			isXpUpdated = true;
		}

		int trackingId = getSkillTrackingId(event.getSkill());
		if (currentSession.isTracking(trackingId)) {
			processActivityUpdate(trackingId, event.getXp());
			isXpUpdated = true;
		}

		if (isXpUpdated && currentSession.isTracking(XP_TOTAL)) {
			int totalGained = currentSession.getGainedXpMap().values().stream()
				.mapToInt(Integer::intValue)
				.sum();

			currentSession.getKillCount(XP_TOTAL).setSessionKc(totalGained);
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (requiresSaveAndRefresh && currentSession != null) {
			storageManager.saveSession(currentSession);
			panel.refreshKcContainer();
			requiresSaveAndRefresh = false;
		}
	}

	private void processChatboxUpdate(int trackingId, int newTotalCount) {
		Count kc = currentSession.getKillCount(trackingId);

		if (kc.getInitialKc() == -1) {
			kc.setInitialKc(newTotalCount - 1);
		}

		int newSessionKc = newTotalCount - kc.getInitialKc();

		if (newSessionKc > kc.getSessionKc()) {
			kc.setSessionKc(newSessionKc);
			requiresSaveAndRefresh = true;
			log.debug("Updated session KC for {}: {}", kc.getName(), newSessionKc);
		}
	}

	private void initializeKillCounts() {
		if (currentSession == null) return;

		for (ActivityData act : activityRegistry) {
			if (!currentSession.isTracking(act.trackingId)) {
				Count kc = new Count(act.name, act.trackingId);

				if (client != null && client.getGameState() == GameState.LOGGED_IN) {
					if (act.trackingId >= VARBIT_OFFSET) {
						kc.setInitialKc(client.getVarbitValue(act.trackingId - VARBIT_OFFSET));
					} else if (act.trackingId > 0) {
						kc.setInitialKc(client.getVarpValue(act.trackingId));
					}
				}

				kc.setSessionKc(0);
				currentSession.addKillCount(kc);
			}
		}

		if (client != null && client.getGameState() == GameState.LOGGED_IN) {
			for (Skill skill : Skill.values()) {
				currentSession.initializeSkill(skill, client.getSkillExperience(skill));

				// Set the baseline KC for the new XP ActivityData entries
				int trackingId = getSkillTrackingId(skill);
				if (currentSession.isTracking(trackingId)) {
					currentSession.getKillCount(trackingId).setInitialKc(client.getSkillExperience(skill));
				}
			}
		}
	}

	public boolean isKcVisible(int trackingId) {
		for (ActivityData act : activityRegistry) {
			if (act.trackingId == trackingId) {
				return act.isVisible.getAsBoolean();
			}
		}
		return false;
	}

	public boolean isSkillVisible(Skill skill) {
		switch (skill) {
			case ATTACK: return config.trackAttackXp();
			case DEFENCE: return config.trackDefenceXp();
			case STRENGTH: return config.trackStrengthXp();
			case HITPOINTS: return config.trackHitpointsXp();
			case RANGED: return config.trackRangedXp();
			case PRAYER: return config.trackPrayerXp();
			case MAGIC: return config.trackMagicXp();
			case COOKING: return config.trackCookingXp();
			case WOODCUTTING: return config.trackWoodcuttingXp();
			case FLETCHING: return config.trackFletchingXp();
			case FISHING: return config.trackFishingXp();
			case FIREMAKING: return config.trackFiremakingXp();
			case CRAFTING: return config.trackCraftingXp();
			case SMITHING: return config.trackSmithingXp();
			case MINING: return config.trackMiningXp();
			case HERBLORE: return config.trackHerbloreXp();
			case AGILITY: return config.trackAgilityXp();
			case THIEVING: return config.trackThievingXp();
			case SLAYER: return config.trackSlayerXp();
			case FARMING: return config.trackFarmingXp();
			case RUNECRAFT: return config.trackRunecraftXp();
			case HUNTER: return config.trackHunterXp();
			case CONSTRUCTION: return config.trackConstructionXp();
			default: return false;
		}
	}

	private int getSkillTrackingId(Skill skill) {
		switch (skill) {
			case ATTACK: return XP_ATTACK;
			case DEFENCE: return XP_DEFENCE;
			case STRENGTH: return XP_STRENGTH;
			case HITPOINTS: return XP_HITPOINTS;
			case RANGED: return XP_RANGED;
			case PRAYER: return XP_PRAYER;
			case MAGIC: return XP_MAGIC;
			case COOKING: return XP_COOKING;
			case WOODCUTTING: return XP_WOODCUTTING;
			case FLETCHING: return XP_FLETCHING;
			case FISHING: return XP_FISHING;
			case FIREMAKING: return XP_FIREMAKING;
			case CRAFTING: return XP_CRAFTING;
			case SMITHING: return XP_SMITHING;
			case MINING: return XP_MINING;
			case HERBLORE: return XP_HERBLORE;
			case AGILITY: return XP_AGILITY;
			case THIEVING: return XP_THIEVING;
			case SLAYER: return XP_SLAYER;
			case FARMING: return XP_FARMING;
			case RUNECRAFT: return XP_RUNECRAFT;
			case HUNTER: return XP_HUNTER;
			case CONSTRUCTION: return XP_CONSTRUCTION;
			default: return -1;
		}
	}

	/**
	 * Retrieves the exact insertion order of an activity from the registry.
	 */
	public int getActivityOrder(int trackingId) {
		for (int i = 0; i < activityRegistry.size(); i++) {
			if (activityRegistry.get(i).trackingId == trackingId) {
				return i;
			}
		}
		return Integer.MAX_VALUE;
	}

	private void buildActivityRegistry() {
		// Bosses Killed
		activityRegistry.add(new ActivityData("Brutus", VarPlayerID.TOTAL_COWBOSS_KILLS, Category.BOSSES, config::trackCowBoss));
		activityRegistry.add(new ActivityData("Obor", VarPlayerID.TOTAL_HILLGIANT_BOSS_KILLS, Category.BOSSES, config::trackObor));
		activityRegistry.add(new ActivityData("Bryophyta", VarPlayerID.TOTAL_BRYOPHYTA_KILLS, Category.BOSSES, config::trackBryophyta));
		activityRegistry.add(new ActivityData("Mimic", VarPlayerID.TOTAL_MIMIC_KILLS, Category.BOSSES, config::trackMimic));
		activityRegistry.add(new ActivityData("Scurrius", VarPlayerID.TOTAL_RAT_BOSS_KILLS, Category.BOSSES, config::trackScurrius));
		activityRegistry.add(new ActivityData("Chaos Fanatic", VarPlayerID.TOTAL_CHAOSFANATIC_KILLS, Category.BOSSES, config::trackChaosFanatic));
		activityRegistry.add(new ActivityData("Crazy Archaeologist", VarPlayerID.TOTAL_CRAZYARCHAEOLOGIST_KILLS, Category.BOSSES, config::trackCrazyArchaeologist));
		activityRegistry.add(new ActivityData("Scorpia", VarPlayerID.TOTAL_SCORPIA_KILLS, Category.BOSSES, config::trackScorpia));
		activityRegistry.add(new ActivityData("Giant Mole", VarPlayerID.TOTAL_MOLE_KILLS, Category.BOSSES, config::trackGiantMole));
		activityRegistry.add(new ActivityData("Shellbane Gryphon", VarPlayerID.TOTAL_GRYPHON_BOSS_KILLS, Category.BOSSES, config::trackGryphonBoss));
		activityRegistry.add(new ActivityData("Grotesque Guardians", VarPlayerID.TOTAL_GARGBOSS_KILLS, Category.BOSSES, config::trackGrotesqueGuardians));
		activityRegistry.add(new ActivityData("Amoxliatl", VarPlayerID.TOTAL_AMOXLIATL_KILLS, Category.BOSSES, config::trackAmoxliatl));
		activityRegistry.add(new ActivityData("Calvar'ion", VarPlayerID.TOTAL_CALVARION_KILLS, Category.BOSSES, config::trackCalvarion));
		activityRegistry.add(new ActivityData("King Black Dragon", VarPlayerID.TOTAL_KBD_KILLS, Category.BOSSES, config::trackKingBlackDragon));
		activityRegistry.add(new ActivityData("Hespori", VarPlayerID.TOTAL_HESPORI_KILLS, Category.BOSSES, config::trackHespori));
		activityRegistry.add(new ActivityData("Kraken", VarPlayerID.TOTAL_KRAKEN_BOSS_KILLS, Category.BOSSES, config::trackKraken));
		activityRegistry.add(new ActivityData("Thermonuclear Smoke Devil", VarPlayerID.TOTAL_THERMY_KILLS, Category.BOSSES, config::trackThermonuclearSmokeDevil));
		activityRegistry.add(new ActivityData("Spindel", VarPlayerID.TOTAL_SPINDEL_KILLS, Category.BOSSES, config::trackSpindel));
		activityRegistry.add(new ActivityData("Dagannoth Prime", VarPlayerID.TOTAL_PRIME_KILLS, Category.BOSSES, config::trackDagannothPrime));
		activityRegistry.add(new ActivityData("Dagannoth Rex", VarPlayerID.TOTAL_REX_KILLS, Category.BOSSES, config::trackDagannothRex));
		activityRegistry.add(new ActivityData("Dagannoth Supreme", VarPlayerID.TOTAL_SUPREME_KILLS, Category.BOSSES, config::trackDagannothSupreme));
		activityRegistry.add(new ActivityData("Cerberus", VarPlayerID.TOTAL_CERBERUS_KILLS, Category.BOSSES, config::trackCerberus));
		activityRegistry.add(new ActivityData("Sarachnis", VarPlayerID.TOTAL_SARACHNIS_KILLS, Category.BOSSES, config::trackSarachnis));
		activityRegistry.add(new ActivityData("Skotizo", VarPlayerID.TOTAL_CATA_BOSS_KILLS, Category.BOSSES, config::trackSkotizo));
		activityRegistry.add(new ActivityData("Artio", VarPlayerID.TOTAL_ARTIO_KILLS, Category.BOSSES, config::trackArtio));
		activityRegistry.add(new ActivityData("Kalphite Queen", VarPlayerID.TOTAL_KALPHITE_KILLS, Category.BOSSES, config::trackKalphiteQueen));
		activityRegistry.add(new ActivityData("Abyssal Sire", VarPlayerID.TOTAL_ABYSSALSIRE_KILLS, Category.BOSSES, config::trackAbyssalSire));
		activityRegistry.add(new ActivityData("Royal Titans", VarPlayerID.TOTAL_ROYAL_TITAN_KILLS, Category.BOSSES, config::trackRoyalTitan));
		activityRegistry.add(new ActivityData("Alchemical Hydra", VarPlayerID.TOTAL_HYDRABOSS_KILLS, Category.BOSSES, config::trackAlchemicalHydra));
		activityRegistry.add(new ActivityData("Vet'ion", VarPlayerID.TOTAL_VETION_KILLS, Category.BOSSES, config::trackVetion));
		activityRegistry.add(new ActivityData("Venenatis", VarPlayerID.TOTAL_VENENATIS_KILLS, Category.BOSSES, config::trackVenenatis));
		activityRegistry.add(new ActivityData("Callisto", VarPlayerID.TOTAL_CALLISTO_KILLS, Category.BOSSES, config::trackCallisto));
		activityRegistry.add(new ActivityData("Chaos Elemental", VarPlayerID.TOTAL_CHAOSELE_KILLS, Category.BOSSES, config::trackChaosElemental));
		activityRegistry.add(new ActivityData("Kree'arra", VarPlayerID.TOTAL_ARMADYL_KILLS, Category.BOSSES, config::trackArmadyl));
		activityRegistry.add(new ActivityData("Mad Angel", VarPlayerID.TOTAL_MAD_ANGEL_KILLS, Category.BOSSES, config::trackMadAngel));
		activityRegistry.add(new ActivityData("Commander Zilyana", VarPlayerID.TOTAL_SARADOMIN_KILLS, Category.BOSSES, config::trackSaradomin));
		activityRegistry.add(new ActivityData("General Graardor", VarPlayerID.TOTAL_BANDOS_KILLS, Category.BOSSES, config::trackBandos));
		activityRegistry.add(new ActivityData("The Hueycoatl", VarPlayerID.TOTAL_HUEY_KILLS, Category.BOSSES, config::trackTheHueycoatl));
		activityRegistry.add(new ActivityData("K'ril Tsutsaroth", VarPlayerID.TOTAL_ZAMORAK_KILLS, Category.BOSSES, config::trackZamorak));
		activityRegistry.add(new ActivityData("TzTok-Jad", VarPlayerID.TOTAL_JAD_KILLS, Category.BOSSES, config::trackTzTokJad));
		activityRegistry.add(new ActivityData("Zulrah", VarPlayerID.TOTAL_SNAKEBOSS_KILLS, Category.BOSSES, config::trackZulrah));
		activityRegistry.add(new ActivityData("Vorkath", VarPlayerID.TOTAL_VORKATH_KILLS, Category.BOSSES, config::trackVorkath));
		activityRegistry.add(new ActivityData("Phantom Muspah", VarPlayerID.TOTAL_MUSPAH_KILLS, Category.BOSSES, config::trackPhantomMuspah));
		activityRegistry.add(new ActivityData("Maggot King", VarPlayerID.TOTAL_MAGGOT_KING_KILLS, Category.BOSSES, config::trackMaggotKing));
		activityRegistry.add(new ActivityData("Duke Sucellus", VarPlayerID.TOTAL_DUKE_SUCELLUS_KILLS, Category.BOSSES, config::trackDukeSucellus));
		activityRegistry.add(new ActivityData("Vardorvis", VarPlayerID.TOTAL_VARDORVIS_KILLS, Category.BOSSES, config::trackVardorvis));
		activityRegistry.add(new ActivityData("Corporeal Beast", VarPlayerID.TOTAL_CORP_KILLS, Category.BOSSES, config::trackCorporealBeast));
		activityRegistry.add(new ActivityData("The Whisperer", VarPlayerID.TOTAL_WHISPERER_KILLS, Category.BOSSES, config::trackTheWhisperer));
		activityRegistry.add(new ActivityData("The Leviathan", VarPlayerID.TOTAL_LEVIATHAN_KILLS, Category.BOSSES, config::trackTheLeviathan));
		activityRegistry.add(new ActivityData("The Nightmare", VarPlayerID.TOTAL_NIGHTMARE_KILLS, Category.BOSSES, config::trackTheNightmare));
		activityRegistry.add(new ActivityData("Araxxor", VarPlayerID.TOTAL_ARAXXOR_KILLS, Category.BOSSES, config::trackAraxxor));
		activityRegistry.add(new ActivityData("Nex", VarPlayerID.TOTAL_NEX_KILLS, Category.BOSSES, config::trackNex));
		activityRegistry.add(new ActivityData("Phosani's Nightmare", VarPlayerID.TOTAL_NIGHTMARE_CHALLENGE_KILLS, Category.BOSSES, config::trackPhosanisNightmare));
		activityRegistry.add(new ActivityData("Duke Sucellus (Awakened)", VarPlayerID.TOTAL_DUKE_SUCELLUS_AWAKENED_KILLS, Category.BOSSES, config::trackDukeSucellus));
		activityRegistry.add(new ActivityData("Vardorvis (Awakened)", VarPlayerID.TOTAL_VARDORVIS_AWAKENED_KILLS, Category.BOSSES, config::trackVardorvis));
		activityRegistry.add(new ActivityData("The Whisperer (Awakened)", VarPlayerID.TOTAL_WHISPERER_AWAKENED_KILLS, Category.BOSSES, config::trackTheWhisperer));
		activityRegistry.add(new ActivityData("The Leviathan (Awakened)", VarPlayerID.TOTAL_LEVIATHAN_AWAKENED_KILLS, Category.BOSSES, config::trackTheLeviathan));
		activityRegistry.add(new ActivityData("Demonic Brutus", VarPlayerID.TOTAL_COWBOSS_HARDMODE_KILLS, Category.BOSSES, config::trackCowBossHardMode));
		activityRegistry.add(new ActivityData("Yama", VarPlayerID.TOTAL_YAMA_KILLS, Category.BOSSES, config::trackYama));
		activityRegistry.add(new ActivityData("TzKal-Zuk", VarPlayerID.TOTAL_ZUK_KILLS, Category.BOSSES, config::trackTzKalZuk));
		activityRegistry.add(new ActivityData("Sol Heredit", VarPlayerID.TOTAL_SOL_KILLS, Category.BOSSES, config::trackColosseum));
		activityRegistry.add(new ActivityData("Doom of Mokhaiotl levels", VarPlayerID.TOTAL_DOM_LEVELS, Category.BOSSES, config::trackDomLevels));

		// Chests Looted
		activityRegistry.add(new ActivityData("Barrows Chests", VarPlayerID.TOTAL_BARROWS_CHESTS, Category.CHESTS, config::trackBarrowsChests));
		activityRegistry.add(new ActivityData("Chambers of Xeric", VarPlayerID.TOTAL_COMPLETED_XERICCHAMBERS, Category.CHESTS, config::trackChambersOfXeric));
		activityRegistry.add(new ActivityData("Chambers of Xeric: Challenge Mode", VarPlayerID.TOTAL_COMPLETED_XERICCHAMBERS_CHALLENGE, Category.CHESTS, config::trackChambersOfXeric));
		activityRegistry.add(new ActivityData("Theatre of Blood", VarPlayerID.TOTAL_COMPLETED_THEATREOFBLOOD, Category.CHESTS, config::trackTheatreOfBlood));
		activityRegistry.add(new ActivityData("Theatre of Blood: Story Mode", VarPlayerID.TOTAL_COMPLETED_THEATREOFBLOOD_STORY, Category.CHESTS, config::trackTheatreOfBlood));
		activityRegistry.add(new ActivityData("Theatre of Blood: Hard Mode", VarPlayerID.TOTAL_COMPLETED_THEATREOFBLOOD_HARD, Category.CHESTS, config::trackTheatreOfBlood));
		activityRegistry.add(new ActivityData("The Gauntlet", VarPlayerID.TOTAL_COMPLETED_GAUNTLET, Category.CHESTS, config::trackTheGauntlet));
		activityRegistry.add(new ActivityData("The Corrupted Gauntlet", VarPlayerID.TOTAL_COMPLETED_GAUNTLET_HM, Category.CHESTS, config::trackTheGauntlet));
		activityRegistry.add(new ActivityData("Tombs of Amascut", VarPlayerID.TOTAL_COMPLETED_TOMBSOFAMASCUT, Category.CHESTS, config::trackTombsOfAmascut));
		activityRegistry.add(new ActivityData("Tombs of Amascut: Entry Mode", VarPlayerID.TOTAL_COMPLETED_TOMBSOFAMASCUT_ENTRY, Category.CHESTS, config::trackTombsOfAmascut));
		activityRegistry.add(new ActivityData("Tombs of Amascut: Expert Mode", VarPlayerID.TOTAL_COMPLETED_TOMBSOFAMASCUT_EXPERT, Category.CHESTS, config::trackTombsOfAmascut));
		activityRegistry.add(new ActivityData("Perilous Moons Chests", VarPlayerID.TOTAL_PMOON_CHESTS, Category.CHESTS, config::trackPerilousMoonsChests));

		// Other (Minigames & Misc)
		activityRegistry.add(new ActivityData("Wintertodt", VarPlayerID.TOTAL_WINTERTODT_KILLS, Category.OTHER, config::trackWintertodt));
		activityRegistry.add(new ActivityData("Zalcano", VarPlayerID.TOTAL_ZALCANO_KILLS, Category.OTHER, config::trackZalcano));
		activityRegistry.add(new ActivityData("Tempoross", VarPlayerID.TOTAL_TEMPOROSS_KILLS, Category.OTHER, config::trackTempoross));
		activityRegistry.add(new ActivityData("Guardians of the Rift", VarPlayerID.TOTAL_GOTR_KILLS, Category.OTHER, config::trackGuardiansOfTheRift));
		activityRegistry.add(new ActivityData("Jad Challenge 1", VarPlayerID.JAD_CHALLENGE_1_COMPLETIONS, Category.OTHER, config::trackJadChallenges));
		activityRegistry.add(new ActivityData("Jad Challenge 2", VarPlayerID.JAD_CHALLENGE_2_COMPLETIONS, Category.OTHER, config::trackJadChallenges));
		activityRegistry.add(new ActivityData("Jad Challenge 3", VarPlayerID.JAD_CHALLENGE_3_COMPLETIONS, Category.OTHER, config::trackJadChallenges));
		activityRegistry.add(new ActivityData("Jad Challenge 4", VarPlayerID.JAD_CHALLENGE_4_COMPLETIONS, Category.OTHER, config::trackJadChallenges));
		activityRegistry.add(new ActivityData("Jad Challenge 5", VarPlayerID.JAD_CHALLENGE_5_COMPLETIONS, Category.OTHER, config::trackJadChallenges));
		activityRegistry.add(new ActivityData("Jad Challenge 6", VarPlayerID.JAD_CHALLENGE_6_COMPLETIONS, Category.OTHER, config::trackJadChallenges));
		activityRegistry.add(new ActivityData("Colosseum waves", VarPlayerID.TOTAL_COLOSSEUM_WAVES_COMPLETED, Category.OTHER, config::trackColosseum));
		activityRegistry.add(new ActivityData("Gemstone Crab", VarPlayerID.TOTAL_GEMSTONE_CRAB_KILLS, Category.OTHER, config::trackGemstoneCrab));
		activityRegistry.add(new ActivityData("Soul Wars wins", VarPlayerID.SOUL_WARS_TOTAL_WINS, Category.OTHER, config::trackSoulWars));
		activityRegistry.add(new ActivityData("Soul Wars games", VarPlayerID.SOUL_WARS_TOTAL_GAMES, Category.OTHER, config::trackSoulWars));
		activityRegistry.add(new ActivityData("Hunter Rumours", HUNTER_RUMOURS, Category.OTHER, config::trackHunterRumours));
		activityRegistry.add(new ActivityData("Farming Contracts", FARMING_CONTRACTS, Category.OTHER, config::trackFarmingContracts));
		activityRegistry.add(new ActivityData("Mahogany Homes", MAHOGANY_HOMES, Category.OTHER, config::trackMahoganyHomesContracts));

		// Clue Scrolls
		activityRegistry.add(new ActivityData("Clue scroll (beginner)", VarPlayerID.COMPLETED_CLUES5, Category.CLUE, config::trackClueScrolls));
		activityRegistry.add(new ActivityData("Clue scroll (easy)", VarPlayerID.COMPLETED_CLUES, Category.CLUE, config::trackClueScrolls));
		activityRegistry.add(new ActivityData("Clue scroll (medium)", VarPlayerID.COMPLETED_CLUES1, Category.CLUE, config::trackClueScrolls));
		activityRegistry.add(new ActivityData("Clue scroll (hard)", VarPlayerID.COMPLETED_CLUES2, Category.CLUE, config::trackClueScrolls));
		activityRegistry.add(new ActivityData("Clue scroll (elite)", VarPlayerID.COMPLETED_CLUES3, Category.CLUE, config::trackClueScrolls));
		activityRegistry.add(new ActivityData("Clue scroll (master)", VarPlayerID.COMPLETED_CLUES4, Category.CLUE, config::trackClueScrolls));

		// Slayer Tasks
		activityRegistry.add(new ActivityData("Slayer tasks", VarbitID.SLAYER_TASKS_COMPLETED + VARBIT_OFFSET, Category.SLAYER, config::trackSlayerTasks));
		activityRegistry.add(new ActivityData("Slayer tasks (Wilderness)", VarbitID.SLAYER_WILDERNESS_TASKS_COMPLETED + VARBIT_OFFSET, Category.SLAYER, config::trackSlayerTasks));
		activityRegistry.add(new ActivityData("Slayer tasks (Mortimer)", VarPlayerID.SLAYER_MORTIMER_TASKS_COMPLETED, Category.SLAYER, config::trackSlayerTasks));

		// Agility Courses
		activityRegistry.add(new ActivityData("Gnome Stronghold Laps", AGILITY_GNOME_STRONGHOLD, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Shayzien Laps (Basic)", AGILITY_SHAYZIEN_LOW, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Shayzien Laps (Advanced)", AGILITY_SHAYZIEN_HIGH, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Penguin Laps", AGILITY_PENGUIN, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Barbarian Outpost Laps", AGILITY_BARBARIAN_OUTPOST, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Ape Atoll Laps", AGILITY_APE_ATOLL, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Wilderness Laps", AGILITY_WILDERNESS, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Colossal Wyrm Laps (Advanced)", AGILITY_COLOSSAL_WYRM_ADVANCED, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Colossal Wyrm Laps (Basic)", AGILITY_COLOSSAL_WYRM_BASIC, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Werewolf Laps", AGILITY_WEREWOLF, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Prifddinas Laps", AGILITY_PRIFDDINAS, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Draynor Village Rooftop Laps", AGILITY_DRAYNOR_ROOFTOP, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Al Kharid Rooftop Laps", AGILITY_AL_KHARID_ROOFTOP, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Varrock Rooftop Laps", AGILITY_VARROCK_ROOFTOP, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Canifis Rooftop Laps", AGILITY_CANIFIS_ROOFTOP, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Falador Rooftop Laps", AGILITY_FALADOR_ROOFTOP, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Seers' Village Rooftop Laps", AGILITY_SEERS_ROOFTOP, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Pollnivneach Rooftop Laps", AGILITY_POLLNIVNEACH_ROOFTOP, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Rellekka Rooftop Laps", AGILITY_RELLEKKA_ROOFTOP, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Ardougne Rooftop Laps", AGILITY_ARDOUGNE_ROOFTOP, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Agility Pyramid Laps", AGILITY_PYRAMID, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Dorgesh-Kaan Laps", AGILITY_DORGESH_KAAN, Category.AGILITY, config::trackAgilityLaps));
		activityRegistry.add(new ActivityData("Brimhaven Agility Tickets", AGILITY_BRIMHAVEN, Category.AGILITY, config::trackAgilityLaps));

		// Experience Tracking
		activityRegistry.add(new ActivityData("Total XP", XP_TOTAL, Category.EXPERIENCE, config::trackTotalXp));
		activityRegistry.add(new ActivityData("Attack XP", XP_ATTACK, Category.EXPERIENCE, config::trackAttackXp));
		activityRegistry.add(new ActivityData("Defence XP", XP_DEFENCE, Category.EXPERIENCE, config::trackDefenceXp));
		activityRegistry.add(new ActivityData("Strength XP", XP_STRENGTH, Category.EXPERIENCE, config::trackStrengthXp));
		activityRegistry.add(new ActivityData("Hitpoints XP", XP_HITPOINTS, Category.EXPERIENCE, config::trackHitpointsXp));
		activityRegistry.add(new ActivityData("Ranged XP", XP_RANGED, Category.EXPERIENCE, config::trackRangedXp));
		activityRegistry.add(new ActivityData("Prayer XP", XP_PRAYER, Category.EXPERIENCE, config::trackPrayerXp));
		activityRegistry.add(new ActivityData("Magic XP", XP_MAGIC, Category.EXPERIENCE, config::trackMagicXp));
		activityRegistry.add(new ActivityData("Cooking XP", XP_COOKING, Category.EXPERIENCE, config::trackCookingXp));
		activityRegistry.add(new ActivityData("Woodcutting XP", XP_WOODCUTTING, Category.EXPERIENCE, config::trackWoodcuttingXp));
		activityRegistry.add(new ActivityData("Fletching XP", XP_FLETCHING, Category.EXPERIENCE, config::trackFletchingXp));
		activityRegistry.add(new ActivityData("Fishing XP", XP_FISHING, Category.EXPERIENCE, config::trackFishingXp));
		activityRegistry.add(new ActivityData("Firemaking XP", XP_FIREMAKING, Category.EXPERIENCE, config::trackFiremakingXp));
		activityRegistry.add(new ActivityData("Crafting XP", XP_CRAFTING, Category.EXPERIENCE, config::trackCraftingXp));
		activityRegistry.add(new ActivityData("Smithing XP", XP_SMITHING, Category.EXPERIENCE, config::trackSmithingXp));
		activityRegistry.add(new ActivityData("Mining XP", XP_MINING, Category.EXPERIENCE, config::trackMiningXp));
		activityRegistry.add(new ActivityData("Herblore XP", XP_HERBLORE, Category.EXPERIENCE, config::trackHerbloreXp));
		activityRegistry.add(new ActivityData("Agility XP", XP_AGILITY, Category.EXPERIENCE, config::trackAgilityXp));
		activityRegistry.add(new ActivityData("Thieving XP", XP_THIEVING, Category.EXPERIENCE, config::trackThievingXp));
		activityRegistry.add(new ActivityData("Slayer XP", XP_SLAYER, Category.EXPERIENCE, config::trackSlayerXp));
		activityRegistry.add(new ActivityData("Farming XP", XP_FARMING, Category.EXPERIENCE, config::trackFarmingXp));
		activityRegistry.add(new ActivityData("Runecraft XP", XP_RUNECRAFT, Category.EXPERIENCE, config::trackRunecraftXp));
		activityRegistry.add(new ActivityData("Hunter XP", XP_HUNTER, Category.EXPERIENCE, config::trackHunterXp));
		activityRegistry.add(new ActivityData("Construction XP", XP_CONSTRUCTION, Category.EXPERIENCE, config::trackConstructionXp));
	}
}