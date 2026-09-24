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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
import net.runelite.client.util.Filepath;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = PLUGIN_NAME,
	internalName = PluginConstants.PLUGIN_DIR_NAME,
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
	private ConfigManager configManager;

	@Inject
	private SessionStorageManager storageManager;

	@Provides
	ActivityCounterConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(ActivityCounterConfig.class);
	}

	public Filepath getDirectory()
	{
		try
		{
			return this.getPluginDirectory();
		}
		catch (IOException e)
		{
			log.error("Failed to get plugin directory", e);
			return null;
		}
	}

	@Getter
	private Session currentSession;
	private ActivityCounterPanel panel;
	private NavigationButton navButton;

	private final List<ActivityData> activityRegistry = new ArrayList<>();

	private boolean trackExperience = false;
	private boolean trackChatMessages = false;
	private boolean trackLevels = false;
	private boolean trackCannonballs = false;
	private boolean trackNightmareZone = false;

	@Getter
	private boolean showSessionDuration = false;
	@Getter
	private boolean confirmSessionTermination = false;
	private boolean requiresSaveAndRefresh = false;

	private int loginTicks = 0;

	public boolean isLoggedIn() {
		return client != null && client.getGameState() == GameState.LOGGED_IN;
	}

	private static final String SUPERIOR_SPAWN_MESSAGE = "A superior foe has appeared...";
	private static final int SUPERIOR_SPAWNS = -7000;

	private static final String BIRD_EGG_OFFERING_MESSAGE = "You offer your bird's egg to the shrine and receive a reward.";
	private static final int BIRD_EGG_OFFERINGS = -7001;

	private static final int CA_TASKS = -7002;
	private static final String CA_PREFIX = "CA_ID:";
//	private static final Pattern COMBAT_TASK_PATTERN = Pattern.compile("^Congratulations, you've completed an? (easy|medium|hard|elite|master|grandmaster) combat task: (.+) \\((\\d+) points?\\)\\.?$");

	private static final int QUESTS_COMPLETED = -7003;
	private static final String QUEST_COMPLETED_PREFIX = "Congratulations, you've completed a quest: ";

	private static final int MUSIC_TRACK_UNLOCKS = -7004;
	private static final String MUSIC_TRACK_UNLOCK_PREFIX = "You have unlocked a new music track: ";

	private static final String LARRANS_PREFIX = "You have opened Larran's ";
	private static final int LARRANS_SMALL_CHESTS = -7005;
	private static final String LARRANS_SMALL_CHEST_PREFIX = "You have opened Larran's small chest ";

	private static final int LARRANS_BIG_CHESTS = -7006;
	private static final String LARRANS_BIG_CHEST_PREFIX = "You have opened Larran's big chest ";

	private static final String SNEAKING_SUSPICION_PREFIX = "You have a sneaking suspicion that";

	private static final String SNEAKING_SUSPICION_BEGINNER_AFFIX = " beginner scroll box.";
	private static final int MISSED_BEGINNER_CLUES = -7010;

	private static final String SNEAKING_SUSPICION_EASY_AFFIX = " easy scroll box.";
	private static final int MISSED_EASY_CLUES = -7011;

	private static final String SNEAKING_SUSPICION_MEDIUM_AFFIX = " medium scroll box.";
	private static final int MISSED_MEDIUM_CLUES = -7012;

	private static final String SNEAKING_SUSPICION_HARD_AFFIX = " hard scroll box.";
	private static final int MISSED_HARD_CLUES = -7013;

	private static final String SNEAKING_SUSPICION_ELITE_AFFIX = " elite scroll box.";
	private static final int MISSED_ELITE_CLUES = -7014;

	private int cannonBallsLoaded = 0;
	private static final int CANNON_BALLS_VARPLAYERID = VarPlayerID.ROCKTHROWER;
	private static final int CANNON_BALLS = -7015;

	private int nmzPoints = 0;
	private static final int NIGHTMARE_ZONE_POINTS_SESSION_VARBITID = VarbitID.NZONE_CURRENTPOINTS;
	private static final int NMZ_POINTS = -7016;


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
	private static final int XP_SAILING = -5024;

	// Pseudo-IDs for Skill Levels (Negative to prevent collisions)
	private static final int LVL_TOTAL = -6000;
	private static final int LVL_ATTACK = -6001;
	private static final int LVL_DEFENCE = -6002;
	private static final int LVL_STRENGTH = -6003;
	private static final int LVL_HITPOINTS = -6004;
	private static final int LVL_RANGED = -6005;
	private static final int LVL_PRAYER = -6006;
	private static final int LVL_MAGIC = -6007;
	private static final int LVL_COOKING = -6008;
	private static final int LVL_WOODCUTTING = -6009;
	private static final int LVL_FLETCHING = -6010;
	private static final int LVL_FISHING = -6011;
	private static final int LVL_FIREMAKING = -6012;
	private static final int LVL_CRAFTING = -6013;
	private static final int LVL_SMITHING = -6014;
	private static final int LVL_MINING = -6015;
	private static final int LVL_HERBLORE = -6016;
	private static final int LVL_AGILITY = -6017;
	private static final int LVL_THIEVING = -6018;
	private static final int LVL_SLAYER = -6019;
	private static final int LVL_FARMING = -6020;
	private static final int LVL_RUNECRAFTING = -6021;
	private static final int LVL_HUNTER = -6022;
	private static final int LVL_CONSTRUCTION = -6023;
	private static final int LVL_SAILING = -6024;

	@AllArgsConstructor
	private static class ActivityData
	{
		String name;
		int trackingId;
		Category category;
		BooleanSupplier isVisible;
		String configKey;
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
			config.trackConstructionXp() || config.trackHunterXp() || config.trackSailingXp();

		trackLevels = config.trackTotalLevel() || config.trackAttackLevel() || config.trackStrengthLevel() ||
			config.trackDefenceLevel() || config.trackRangedLevel() || config.trackPrayerLevel() || config.trackMagicLevel() ||
			config.trackRunecraftLevel() || config.trackHitpointsLevel() || config.trackCraftingLevel() ||
			config.trackMiningLevel() || config.trackSmithingLevel() || config.trackFishingLevel() ||
			config.trackCookingLevel() || config.trackFiremakingLevel() || config.trackWoodcuttingLevel() ||
			config.trackAgilityLevel() || config.trackHerbloreLevel() || config.trackThievingLevel() ||
			config.trackFletchingLevel() || config.trackSlayerLevel() || config.trackFarmingLevel() ||
			config.trackConstructionLevel() || config.trackHunterLevel() || config.trackSailingLevel();

		trackChatMessages = config.trackSuperiorSpawns() || config.trackAgilityLaps() ||
			config.trackHunterRumours() || config.trackFarmingContracts() ||
			config.trackMahoganyHomesContracts() || config.trackMusicUnlocked() ||
			config.trackClueScrolls() || config.trackLarransChests() ||
			config.trackQuests() || config.trackCombatAchievements() ||
			config.trackBirdEggOfferings();

		showSessionDuration = config.showSessionDuration();
		confirmSessionTermination = config.confirmTerminateSession();
		trackCannonballs = config.trackCannonballs();
		trackNightmareZone = config.trackNightmareZonePoints();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		GameState state = event.getGameState();

		if (state == GameState.LOGGED_IN) {
			loginTicks = 0;

			long accountHash = client.getAccountHash();
			if (accountHash == -1) return;

			if (currentSession == null) {
				Session activeSession = storageManager.loadActiveSession(accountHash);
				if (activeSession != null) {
					currentSession = activeSession;
					initializeKillCounts(false);
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

	/**
	 * Starts a new session
	 */
	public void startSession() {
		clientThread.invokeLater(() -> {
			if (client == null || client.getGameState() != GameState.LOGGED_IN) {
				return;
			}

			String accountName = client.getLocalPlayer() != null
				? client.getLocalPlayer().getName()
				: "Unknown";

			currentSession = new Session(client.getAccountHash(), accountName);
			initializeKillCounts(true);
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

	public int getCategorySortOrder(Category category) {
		switch (category) {
			case BOSSES: return config.sortBosses();
			case CHESTS: return config.sortChests();
			case CLUE: return config.sortClue();
			case SLAYER: return config.sortSlayer();
			case AGILITY: return config.sortAgility();
			case EXPERIENCE: return config.sortExperience();
			case LEVELS: return config.sortLevels();
			case OTHER: return config.sortOther();
			default: return 99;
		}
	}

	public String getActivityConfigKey(int trackingId) {
		for (ActivityData act : activityRegistry) {
			if (act.trackingId == trackingId) {
				return act.configKey;
			}
		}
		return null;
	}

	public void disableActivity(String configKey) {
		if (configKey != null) {
			configManager.setConfiguration(PluginConstants.CONFIG_GROUP, configKey, false);
		}
	}

	/**
	 * Returns a processed list of counts to display in the UI, applying merges if configured.
	 */
	public List<Count> getDisplayKcs(Session session) {
		if (session == null) return new ArrayList<>();

		List<Count> visibleKcs = session.getAllKillCounts().stream()
			.filter(kc -> kc.getSessionKc() > 0 && isKcVisible(kc.getVarPlayerId()))
			.collect(Collectors.toList());

		if (config.mergeSlayerTaskCounts()) {
			int otherId = VarbitID.SLAYER_TASKS_COMPLETED + VARBIT_OFFSET;
			int wildyId = VarbitID.SLAYER_WILDERNESS_TASKS_COMPLETED + VARBIT_OFFSET;
			int mortimerId = VarPlayerID.SLAYER_MORTIMER_TASKS_COMPLETED;

			int mergedSessionKc = 0;

			java.util.Iterator<Count> iterator = visibleKcs.iterator();
			while (iterator.hasNext()) {
				Count kc = iterator.next();
				if (kc.getVarPlayerId() == otherId || kc.getVarPlayerId() == wildyId || kc.getVarPlayerId() == mortimerId) {
					mergedSessionKc += kc.getSessionKc();
					iterator.remove();
				}
			}

			if (mergedSessionKc > 0) {
				Count mergedCount = new Count("Slayer tasks", otherId);
				mergedCount.setSessionKc(mergedSessionKc);
				visibleKcs.add(mergedCount);
			}
		}

		return visibleKcs;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals(CONFIG_GROUP)) return;
		updateConfigFlags();
		panel.refreshKcContainer();
	}

	/**
	 * Updates cannonballs, provided the previous count is higher and the difference is minimal
	 */
	private void updateCannonBalls()
	{
		int newCannonBalls = client.getVarpValue(CANNON_BALLS_VARPLAYERID);
		int deltaCb = cannonBallsLoaded-newCannonBalls;
		if (deltaCb > 0 && deltaCb < 3)
		{
			Count kc = currentSession.getKillCount(CANNON_BALLS);
			kc.setSessionKc(kc.getSessionKc() + deltaCb);
			currentSession.addKillCount(kc);
		}
		cannonBallsLoaded = newCannonBalls;
	}

	/**
	 * Updates Nightmare zone points
	 */
	private void updateNmzPoints()
	{
		int newNmzPoints = client.getVarbitValue(NIGHTMARE_ZONE_POINTS_SESSION_VARBITID);
		int deltaNmz = newNmzPoints - nmzPoints;
		if (deltaNmz > 0)
		{
			Count kc = currentSession.getKillCount(NMZ_POINTS);
			kc.setSessionKc(kc.getSessionKc() + deltaNmz);
			currentSession.addKillCount(kc);
		}
		nmzPoints = newNmzPoints;
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
		else if (varpId == CANNON_BALLS_VARPLAYERID)
		{
			if (trackCannonballs) updateCannonBalls();
			return;
		}

		// 2. Process Varbit-based changes safely using the offset
		if (varbitId != -1) {
			int trackedVarbitId = varbitId + VARBIT_OFFSET;
			if (currentSession.isTracking(trackedVarbitId)) {
				processActivityUpdate(trackedVarbitId, client.getVarbitValue(varbitId));
			}
			else if (varbitId == NIGHTMARE_ZONE_POINTS_SESSION_VARBITID)
			{
				if (trackNightmareZone) updateNmzPoints();
				return;
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
			requiresSaveAndRefresh = true;
		}
	}

	/**
	 * Activity counter that adds 1 to the Count associated with the given trackingId, provided it's tracked
	 */
	private void incrementChatboxActivity(int trackingId) {
		if (currentSession.isTracking(trackingId)) {
			Count kc = currentSession.getKillCount(trackingId);
			kc.setSessionKc(kc.getSessionKc() + 1);
			requiresSaveAndRefresh = true;
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

		if (config.trackFarmingContracts() && message.equals(FARMING_CONTRACT_MESSAGE)) {
			incrementChatboxActivity(FARMING_CONTRACTS);
			return;
		}
		if (config.trackSuperiorSpawns() && message.equals(SUPERIOR_SPAWN_MESSAGE)) {
			incrementChatboxActivity(SUPERIOR_SPAWNS);
			return;
		}
		if (config.trackBirdEggOfferings() && message.equals(BIRD_EGG_OFFERING_MESSAGE)) {
			incrementChatboxActivity(BIRD_EGG_OFFERINGS);
			return;
		}

		if (config.trackMusicUnlocked() && message.startsWith(MUSIC_TRACK_UNLOCK_PREFIX)) {
			incrementChatboxActivity(MUSIC_TRACK_UNLOCKS);
			return;
		}

		if (config.trackQuests() && message.startsWith(QUEST_COMPLETED_PREFIX)) {
			incrementChatboxActivity(QUESTS_COMPLETED);
			return;
		}

		if (config.trackLarransChests() && message.startsWith(LARRANS_PREFIX)) {
			if (message.startsWith(LARRANS_SMALL_CHEST_PREFIX)) {
				incrementChatboxActivity(LARRANS_SMALL_CHESTS);
			} else if (message.startsWith(LARRANS_BIG_CHEST_PREFIX)) {
				incrementChatboxActivity(LARRANS_BIG_CHESTS);
			}
			return;
		}

		if (config.trackClueScrolls() && message.startsWith(SNEAKING_SUSPICION_PREFIX)) {
			if (message.endsWith(SNEAKING_SUSPICION_BEGINNER_AFFIX)) incrementChatboxActivity(MISSED_BEGINNER_CLUES);
			else if (message.endsWith(SNEAKING_SUSPICION_EASY_AFFIX)) incrementChatboxActivity(MISSED_EASY_CLUES);
			else if (message.endsWith(SNEAKING_SUSPICION_MEDIUM_AFFIX)) incrementChatboxActivity(MISSED_MEDIUM_CLUES);
			else if (message.endsWith(SNEAKING_SUSPICION_HARD_AFFIX)) incrementChatboxActivity(MISSED_HARD_CLUES);
			else if (message.endsWith(SNEAKING_SUSPICION_ELITE_AFFIX)) incrementChatboxActivity(MISSED_ELITE_CLUES);
			return;
		}

		if (config.trackCombatAchievements() && message.startsWith(CA_PREFIX)) {
			incrementChatboxActivity(CA_TASKS);
			return;
		}

		if (config.trackAgilityLaps() && message.startsWith(LAP_PREFIX)) {
			Matcher matcher = LAP_PATTERN.matcher(message);
			if (matcher.find()) {
				String courseName = matcher.group(1);
				int newLapCount = Integer.parseInt(matcher.group(2).replace(",", ""));
				int pseudoId = -1;

				switch (courseName) {
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

				if (pseudoId != -1 && currentSession.isTracking(pseudoId)) {
					processChatboxUpdate(pseudoId, newLapCount);
				}
			}
			return;
		}

		if (message.startsWith(COMPLETED_PREFIX)) {
			if (config.trackHunterRumours()) {
				Matcher rumourMatcher = RUMOUR_PATTERN.matcher(message);
				if (rumourMatcher.find()) {
					int newRumourCount = Integer.parseInt(rumourMatcher.group(1).replace(",", ""));
					if (currentSession.isTracking(HUNTER_RUMOURS)) {
						processChatboxUpdate(HUNTER_RUMOURS, newRumourCount);
					}
					return;
				}
			}

			if (config.trackMahoganyHomesContracts()) {
				Matcher mahoganyMatcher = MAHOGANY_HOMES_PATTERN.matcher(message);
				if (mahoganyMatcher.find()) {
					int newContractCount = Integer.parseInt(mahoganyMatcher.group(1).replace(",", ""));
					if (currentSession.isTracking(MAHOGANY_HOMES)) {
						processChatboxUpdate(MAHOGANY_HOMES, newContractCount);
					}
				}
			}
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event) {
		if (!trackExperience && !trackLevels) return;

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

		if (trackLevels) {
			int lvlTrackingId = getSkillLevelTrackingId(event.getSkill());
			if (currentSession.isTracking(lvlTrackingId)) {
				processActivityUpdate(lvlTrackingId, client.getRealSkillLevel(event.getSkill()));
			}
			if (currentSession.isTracking(LVL_TOTAL)) {
				processActivityUpdate(LVL_TOTAL, client.getTotalLevel());
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (currentSession != null && currentSession.isInProgress()) {

			if (loginTicks < 5) {
				loginTicks++;
			} else {
				boolean baselinesUpdated = false;

				for (ActivityData act : activityRegistry) {
					if (currentSession.isTracking(act.trackingId)) {
						Count kc = currentSession.getKillCount(act.trackingId);
						if (kc.getInitialKc() == -1) {
							if (act.trackingId >= VARBIT_OFFSET) {
								kc.setInitialKc(client.getVarbitValue(act.trackingId - VARBIT_OFFSET));
								baselinesUpdated = true;
							} else if (act.trackingId > 0) {
								kc.setInitialKc(client.getVarpValue(act.trackingId));
								baselinesUpdated = true;
							}
						}
					}
				}

				for (Skill skill : Skill.values()) {
					int xpTrackingId = getSkillTrackingId(skill);
					if (currentSession.isTracking(xpTrackingId)) {
						Count kc = currentSession.getKillCount(xpTrackingId);
						if (kc.getInitialKc() == -1) {
							kc.setInitialKc(client.getSkillExperience(skill));
							currentSession.initializeSkill(skill, client.getSkillExperience(skill));
							baselinesUpdated = true;
						}
					}

					int lvlTrackingId = getSkillLevelTrackingId(skill);
					if (currentSession.isTracking(lvlTrackingId)) {
						Count kc = currentSession.getKillCount(lvlTrackingId);
						if (kc.getInitialKc() == -1) {
							kc.setInitialKc(client.getRealSkillLevel(skill));
							baselinesUpdated = true;
						}
					}
				}

				if (currentSession.isTracking(LVL_TOTAL)) {
					Count kc = currentSession.getKillCount(LVL_TOTAL);
					if (kc.getInitialKc() == -1) {
						kc.setInitialKc(client.getTotalLevel());
						baselinesUpdated = true;
					}
				}

				if (baselinesUpdated) {
					requiresSaveAndRefresh = true;
				}
			}
		}

		if (requiresSaveAndRefresh && currentSession != null) {
			storageManager.saveSession(currentSession);
			panel.refreshKcContainer();
			requiresSaveAndRefresh = false;
		}
	}

	public void renameSession(Session sessionToRename, String newName) {
		if (sessionToRename != null && newName != null && !newName.trim().isEmpty()) {
			String trimmedName = newName.trim();

			sessionToRename.setSessionName(trimmedName);
			storageManager.saveSession(sessionToRename);

			if (currentSession != null && currentSession.getId().equals(sessionToRename.getId())) {
				currentSession.setSessionName(trimmedName);
			}

			panel.reloadComboBox();
			log.debug("Renamed session to: {}", trimmedName);
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

	private void initializeKillCounts(boolean isManualStart) {
		if (currentSession == null) return;

		nmzPoints = 0;

		for (ActivityData act : activityRegistry) {
			if (!currentSession.isTracking(act.trackingId)) {
				Count kc = new Count(act.name, act.trackingId);

				kc.setInitialKc(-1);
				kc.setSessionKc(0);

				if (isManualStart && client != null && client.getGameState() == GameState.LOGGED_IN) {
					if (act.trackingId >= VARBIT_OFFSET) {
						kc.setInitialKc(client.getVarbitValue(act.trackingId - VARBIT_OFFSET));
					} else if (act.trackingId > 0) {
						kc.setInitialKc(client.getVarpValue(act.trackingId));
					}
				}

				currentSession.addKillCount(kc);
			}
		}

		if (isManualStart && client != null && client.getGameState() == GameState.LOGGED_IN) {
			for (Skill skill : Skill.values()) {
				int xpTrackingId = getSkillTrackingId(skill);
				if (currentSession.isTracking(xpTrackingId)) {
					Count kc = currentSession.getKillCount(xpTrackingId);
					if (kc.getInitialKc() == -1) {
						kc.setInitialKc(client.getSkillExperience(skill));
						currentSession.initializeSkill(skill, client.getSkillExperience(skill));
					}
				}

				int lvlTrackingId = getSkillLevelTrackingId(skill);
				if (currentSession.isTracking(lvlTrackingId)) {
					Count kc = currentSession.getKillCount(lvlTrackingId);
					if (kc.getInitialKc() == -1) {
						kc.setInitialKc(client.getRealSkillLevel(skill));
					}
				}
			}

			if (currentSession.isTracking(LVL_TOTAL)) {
				Count kc = currentSession.getKillCount(LVL_TOTAL);
				if (kc.getInitialKc() == -1) {
					kc.setInitialKc(client.getTotalLevel());
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
			case SAILING: return XP_SAILING;
			default: return -1;
		}
	}

	private int getSkillLevelTrackingId(Skill skill) {
		switch (skill) {
			case ATTACK: return LVL_ATTACK;
			case DEFENCE: return LVL_DEFENCE;
			case STRENGTH: return LVL_STRENGTH;
			case HITPOINTS: return LVL_HITPOINTS;
			case RANGED: return LVL_RANGED;
			case PRAYER: return LVL_PRAYER;
			case MAGIC: return LVL_MAGIC;
			case COOKING: return LVL_COOKING;
			case WOODCUTTING: return LVL_WOODCUTTING;
			case FLETCHING: return LVL_FLETCHING;
			case FISHING: return LVL_FISHING;
			case FIREMAKING: return LVL_FIREMAKING;
			case CRAFTING: return LVL_CRAFTING;
			case SMITHING: return LVL_SMITHING;
			case MINING: return LVL_MINING;
			case HERBLORE: return LVL_HERBLORE;
			case AGILITY: return LVL_AGILITY;
			case THIEVING: return LVL_THIEVING;
			case SLAYER: return LVL_SLAYER;
			case FARMING: return LVL_FARMING;
			case RUNECRAFT: return LVL_RUNECRAFTING;
			case HUNTER: return LVL_HUNTER;
			case CONSTRUCTION: return LVL_CONSTRUCTION;
			case SAILING: return LVL_SAILING;
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
		activityRegistry.add(new ActivityData("Brutus", VarPlayerID.TOTAL_COWBOSS_KILLS, Category.BOSSES, config::trackCowBoss, "trackCowBoss"));
		activityRegistry.add(new ActivityData("Obor", VarPlayerID.TOTAL_HILLGIANT_BOSS_KILLS, Category.BOSSES, config::trackObor, "trackObor"));
		activityRegistry.add(new ActivityData("Bryophyta", VarPlayerID.TOTAL_BRYOPHYTA_KILLS, Category.BOSSES, config::trackBryophyta, "trackBryophyta"));
		activityRegistry.add(new ActivityData("Scurrius", VarPlayerID.TOTAL_RAT_BOSS_KILLS, Category.BOSSES, config::trackScurrius, "trackScurrius"));
		activityRegistry.add(new ActivityData("Chaos Fanatic", VarPlayerID.TOTAL_CHAOSFANATIC_KILLS, Category.BOSSES, config::trackChaosFanatic, "trackChaosFanatic"));
		activityRegistry.add(new ActivityData("Deranged Archaeologist", VarPlayerID.TOTAL_DERANGEDARCHAEOLOGIST_KILLS, Category.BOSSES, config::trackDerangedArchaeologist, "trackDerangedArchaeologist"));
		activityRegistry.add(new ActivityData("Crazy Archaeologist", VarPlayerID.TOTAL_CRAZYARCHAEOLOGIST_KILLS, Category.BOSSES, config::trackCrazyArchaeologist, "trackCrazyArchaeologist"));
		activityRegistry.add(new ActivityData("Scorpia", VarPlayerID.TOTAL_SCORPIA_KILLS, Category.BOSSES, config::trackScorpia, "trackScorpia"));
		activityRegistry.add(new ActivityData("Giant Mole", VarPlayerID.TOTAL_MOLE_KILLS, Category.BOSSES, config::trackGiantMole, "trackGiantMole"));
		activityRegistry.add(new ActivityData("Shellbane Gryphon", VarPlayerID.TOTAL_GRYPHON_BOSS_KILLS, Category.BOSSES, config::trackGryphonBoss, "trackGryphonBoss"));
		activityRegistry.add(new ActivityData("Grotesque Guardians", VarPlayerID.TOTAL_GARGBOSS_KILLS, Category.BOSSES, config::trackGrotesqueGuardians, "trackGrotesqueGuardians"));
		activityRegistry.add(new ActivityData("Amoxliatl", VarPlayerID.TOTAL_AMOXLIATL_KILLS, Category.BOSSES, config::trackAmoxliatl, "trackAmoxliatl"));
		activityRegistry.add(new ActivityData("Calvar'ion", VarPlayerID.TOTAL_CALVARION_KILLS, Category.BOSSES, config::trackCalvarion, "trackCalvarion"));
		activityRegistry.add(new ActivityData("King Black Dragon", VarPlayerID.TOTAL_KBD_KILLS, Category.BOSSES, config::trackKingBlackDragon, "trackKingBlackDragon"));
		activityRegistry.add(new ActivityData("Hespori", VarPlayerID.TOTAL_HESPORI_KILLS, Category.BOSSES, config::trackHespori, "trackHespori"));
		activityRegistry.add(new ActivityData("Kraken", VarPlayerID.TOTAL_KRAKEN_BOSS_KILLS, Category.BOSSES, config::trackKraken, "trackKraken"));
		activityRegistry.add(new ActivityData("Thermonuclear Smoke Devil", VarPlayerID.TOTAL_THERMY_KILLS, Category.BOSSES, config::trackThermonuclearSmokeDevil, "trackThermonuclearSmokeDevil"));
		activityRegistry.add(new ActivityData("Spindel", VarPlayerID.TOTAL_SPINDEL_KILLS, Category.BOSSES, config::trackSpindel, "trackSpindel"));
		activityRegistry.add(new ActivityData("Dagannoth Prime", VarPlayerID.TOTAL_PRIME_KILLS, Category.BOSSES, config::trackDagannothPrime, "trackDagannothPrime"));
		activityRegistry.add(new ActivityData("Dagannoth Rex", VarPlayerID.TOTAL_REX_KILLS, Category.BOSSES, config::trackDagannothRex, "trackDagannothRex"));
		activityRegistry.add(new ActivityData("Dagannoth Supreme", VarPlayerID.TOTAL_SUPREME_KILLS, Category.BOSSES, config::trackDagannothSupreme, "trackDagannothSupreme"));
		activityRegistry.add(new ActivityData("Cerberus", VarPlayerID.TOTAL_CERBERUS_KILLS, Category.BOSSES, config::trackCerberus, "trackCerberus"));
		activityRegistry.add(new ActivityData("Sarachnis", VarPlayerID.TOTAL_SARACHNIS_KILLS, Category.BOSSES, config::trackSarachnis, "trackSarachnis"));
		activityRegistry.add(new ActivityData("Skotizo", VarPlayerID.TOTAL_CATA_BOSS_KILLS, Category.BOSSES, config::trackSkotizo, "trackSkotizo"));
		activityRegistry.add(new ActivityData("Artio", VarPlayerID.TOTAL_ARTIO_KILLS, Category.BOSSES, config::trackArtio, "trackArtio"));
		activityRegistry.add(new ActivityData("Kalphite Queen", VarPlayerID.TOTAL_KALPHITE_KILLS, Category.BOSSES, config::trackKalphiteQueen, "trackKalphiteQueen"));
		activityRegistry.add(new ActivityData("Abyssal Sire", VarPlayerID.TOTAL_ABYSSALSIRE_KILLS, Category.BOSSES, config::trackAbyssalSire, "trackAbyssalSire"));
		activityRegistry.add(new ActivityData("Royal Titans", VarPlayerID.TOTAL_ROYAL_TITAN_KILLS, Category.BOSSES, config::trackRoyalTitan, "trackRoyalTitan"));
		activityRegistry.add(new ActivityData("Alchemical Hydra", VarPlayerID.TOTAL_HYDRABOSS_KILLS, Category.BOSSES, config::trackAlchemicalHydra, "trackAlchemicalHydra"));
		activityRegistry.add(new ActivityData("Vet'ion", VarPlayerID.TOTAL_VETION_KILLS, Category.BOSSES, config::trackVetion, "trackVetion"));
		activityRegistry.add(new ActivityData("Venenatis", VarPlayerID.TOTAL_VENENATIS_KILLS, Category.BOSSES, config::trackVenenatis, "trackVenenatis"));
		activityRegistry.add(new ActivityData("Callisto", VarPlayerID.TOTAL_CALLISTO_KILLS, Category.BOSSES, config::trackCallisto, "trackCallisto"));
		activityRegistry.add(new ActivityData("Chaos Elemental", VarPlayerID.TOTAL_CHAOSELE_KILLS, Category.BOSSES, config::trackChaosElemental, "trackChaosElemental"));
		activityRegistry.add(new ActivityData("Kree'arra", VarPlayerID.TOTAL_ARMADYL_KILLS, Category.BOSSES, config::trackArmadyl, "trackArmadyl"));
		activityRegistry.add(new ActivityData("Mad Angel", VarPlayerID.TOTAL_MAD_ANGEL_KILLS, Category.BOSSES, config::trackMadAngel, "trackMadAngel"));
		activityRegistry.add(new ActivityData("Commander Zilyana", VarPlayerID.TOTAL_SARADOMIN_KILLS, Category.BOSSES, config::trackSaradomin, "trackSaradomin"));
		activityRegistry.add(new ActivityData("General Graardor", VarPlayerID.TOTAL_BANDOS_KILLS, Category.BOSSES, config::trackBandos, "trackBandos"));
		activityRegistry.add(new ActivityData("The Hueycoatl", VarPlayerID.TOTAL_HUEY_KILLS, Category.BOSSES, config::trackTheHueycoatl, "trackTheHueycoatl"));
		activityRegistry.add(new ActivityData("K'ril Tsutsaroth", VarPlayerID.TOTAL_ZAMORAK_KILLS, Category.BOSSES, config::trackZamorak, "trackZamorak"));
		activityRegistry.add(new ActivityData("TzTok-Jad", VarPlayerID.TOTAL_JAD_KILLS, Category.BOSSES, config::trackTzTokJad, "trackTzTokJad"));
		activityRegistry.add(new ActivityData("Zulrah", VarPlayerID.TOTAL_SNAKEBOSS_KILLS, Category.BOSSES, config::trackZulrah, "trackZulrah"));
		activityRegistry.add(new ActivityData("Vorkath", VarPlayerID.TOTAL_VORKATH_KILLS, Category.BOSSES, config::trackVorkath, "trackVorkath"));
		activityRegistry.add(new ActivityData("Phantom Muspah", VarPlayerID.TOTAL_MUSPAH_KILLS, Category.BOSSES, config::trackPhantomMuspah, "trackPhantomMuspah"));
		activityRegistry.add(new ActivityData("Maggot King", VarPlayerID.TOTAL_MAGGOT_KING_KILLS, Category.BOSSES, config::trackMaggotKing, "trackMaggotKing"));
		activityRegistry.add(new ActivityData("Duke Sucellus", VarPlayerID.TOTAL_DUKE_SUCELLUS_KILLS, Category.BOSSES, config::trackDukeSucellus, "trackDukeSucellus"));
		activityRegistry.add(new ActivityData("Vardorvis", VarPlayerID.TOTAL_VARDORVIS_KILLS, Category.BOSSES, config::trackVardorvis, "trackVardorvis"));
		activityRegistry.add(new ActivityData("Corporeal Beast", VarPlayerID.TOTAL_CORP_KILLS, Category.BOSSES, config::trackCorporealBeast, "trackCorporealBeast"));
		activityRegistry.add(new ActivityData("The Whisperer", VarPlayerID.TOTAL_WHISPERER_KILLS, Category.BOSSES, config::trackTheWhisperer, "trackTheWhisperer"));
		activityRegistry.add(new ActivityData("The Leviathan", VarPlayerID.TOTAL_LEVIATHAN_KILLS, Category.BOSSES, config::trackTheLeviathan, "trackTheLeviathan"));
		activityRegistry.add(new ActivityData("The Nightmare", VarPlayerID.TOTAL_NIGHTMARE_KILLS, Category.BOSSES, config::trackTheNightmare, "trackTheNightmare"));
		activityRegistry.add(new ActivityData("Araxxor", VarPlayerID.TOTAL_ARAXXOR_KILLS, Category.BOSSES, config::trackAraxxor, "trackAraxxor"));
		activityRegistry.add(new ActivityData("Nex", VarPlayerID.TOTAL_NEX_KILLS, Category.BOSSES, config::trackNex, "trackNex"));
		activityRegistry.add(new ActivityData("Phosani's Nightmare", VarPlayerID.TOTAL_NIGHTMARE_CHALLENGE_KILLS, Category.BOSSES, config::trackPhosanisNightmare, "trackPhosanisNightmare"));
		activityRegistry.add(new ActivityData("Duke Sucellus (Awakened)", VarPlayerID.TOTAL_DUKE_SUCELLUS_AWAKENED_KILLS, Category.BOSSES, config::trackDukeSucellus, "trackDukeSucellus"));
		activityRegistry.add(new ActivityData("Vardorvis (Awakened)", VarPlayerID.TOTAL_VARDORVIS_AWAKENED_KILLS, Category.BOSSES, config::trackVardorvis, "trackVardorvis"));
		activityRegistry.add(new ActivityData("The Whisperer (Awakened)", VarPlayerID.TOTAL_WHISPERER_AWAKENED_KILLS, Category.BOSSES, config::trackTheWhisperer, "trackTheWhisperer"));
		activityRegistry.add(new ActivityData("The Leviathan (Awakened)", VarPlayerID.TOTAL_LEVIATHAN_AWAKENED_KILLS, Category.BOSSES, config::trackTheLeviathan, "trackTheLeviathan"));
		activityRegistry.add(new ActivityData("Demonic Brutus", VarPlayerID.TOTAL_COWBOSS_HARDMODE_KILLS, Category.BOSSES, config::trackCowBossHardMode, "trackCowBossHardMode"));
		activityRegistry.add(new ActivityData("Yama", VarPlayerID.TOTAL_YAMA_KILLS, Category.BOSSES, config::trackYama, "trackYama"));
		activityRegistry.add(new ActivityData("TzKal-Zuk", VarPlayerID.TOTAL_ZUK_KILLS, Category.BOSSES, config::trackTzKalZuk, "trackTzKalZuk"));
		activityRegistry.add(new ActivityData("Sol Heredit", VarPlayerID.TOTAL_SOL_KILLS, Category.BOSSES, config::trackColosseum, "trackColosseum"));
		activityRegistry.add(new ActivityData("Doom of Mokhaiotl levels", VarPlayerID.TOTAL_DOM_LEVELS, Category.BOSSES, config::trackDomLevels, "trackDomLevels"));

		// Chests Looted
		activityRegistry.add(new ActivityData("Barrows Chests", VarPlayerID.TOTAL_BARROWS_CHESTS, Category.CHESTS, config::trackBarrowsChests, "trackBarrowsChests"));
		activityRegistry.add(new ActivityData("Chambers of Xeric", VarPlayerID.TOTAL_COMPLETED_XERICCHAMBERS, Category.CHESTS, config::trackChambersOfXeric, "trackChambersOfXeric"));
		activityRegistry.add(new ActivityData("Chambers of Xeric: Challenge Mode", VarPlayerID.TOTAL_COMPLETED_XERICCHAMBERS_CHALLENGE, Category.CHESTS, config::trackChambersOfXeric, "trackChambersOfXeric"));
		activityRegistry.add(new ActivityData("Theatre of Blood", VarPlayerID.TOTAL_COMPLETED_THEATREOFBLOOD, Category.CHESTS, config::trackTheatreOfBlood, "trackTheatreOfBlood"));
		activityRegistry.add(new ActivityData("Theatre of Blood: Story Mode", VarPlayerID.TOTAL_COMPLETED_THEATREOFBLOOD_STORY, Category.CHESTS, config::trackTheatreOfBlood, "trackTheatreOfBlood"));
		activityRegistry.add(new ActivityData("Theatre of Blood: Hard Mode", VarPlayerID.TOTAL_COMPLETED_THEATREOFBLOOD_HARD, Category.CHESTS, config::trackTheatreOfBlood, "trackTheatreOfBlood"));
		activityRegistry.add(new ActivityData("The Gauntlet", VarPlayerID.TOTAL_COMPLETED_GAUNTLET, Category.CHESTS, config::trackTheGauntlet, "trackTheGauntlet"));
		activityRegistry.add(new ActivityData("The Corrupted Gauntlet", VarPlayerID.TOTAL_COMPLETED_GAUNTLET_HM, Category.CHESTS, config::trackTheGauntlet, "trackTheGauntlet"));
		activityRegistry.add(new ActivityData("Tombs of Amascut", VarPlayerID.TOTAL_COMPLETED_TOMBSOFAMASCUT, Category.CHESTS, config::trackTombsOfAmascut, "trackTombsOfAmascut"));
		activityRegistry.add(new ActivityData("Tombs of Amascut: Entry Mode", VarPlayerID.TOTAL_COMPLETED_TOMBSOFAMASCUT_ENTRY, Category.CHESTS, config::trackTombsOfAmascut, "trackTombsOfAmascut"));
		activityRegistry.add(new ActivityData("Tombs of Amascut: Expert Mode", VarPlayerID.TOTAL_COMPLETED_TOMBSOFAMASCUT_EXPERT, Category.CHESTS, config::trackTombsOfAmascut, "trackTombsOfAmascut"));
		activityRegistry.add(new ActivityData("Perilous Moons Chests", VarPlayerID.TOTAL_PMOON_CHESTS, Category.CHESTS, config::trackPerilousMoonsChests, "trackPerilousMoonsChests"));

		// Other (Minigames & Misc)
		activityRegistry.add(new ActivityData("Wintertodt", VarPlayerID.TOTAL_WINTERTODT_KILLS, Category.OTHER, config::trackWintertodt, "trackWintertodt"));
		activityRegistry.add(new ActivityData("Zalcano", VarPlayerID.TOTAL_ZALCANO_KILLS, Category.OTHER, config::trackZalcano, "trackZalcano"));
		activityRegistry.add(new ActivityData("Tempoross", VarPlayerID.TOTAL_TEMPOROSS_KILLS, Category.OTHER, config::trackTempoross, "trackTempoross"));
		activityRegistry.add(new ActivityData("Guardians of the Rift", VarPlayerID.TOTAL_GOTR_KILLS, Category.OTHER, config::trackGuardiansOfTheRift, "trackGuardiansOfTheRift"));
		activityRegistry.add(new ActivityData("Jad Challenge 1", VarPlayerID.JAD_CHALLENGE_1_COMPLETIONS, Category.OTHER, config::trackJadChallenges, "trackJadChallenges"));
		activityRegistry.add(new ActivityData("Jad Challenge 2", VarPlayerID.JAD_CHALLENGE_2_COMPLETIONS, Category.OTHER, config::trackJadChallenges, "trackJadChallenges"));
		activityRegistry.add(new ActivityData("Jad Challenge 3", VarPlayerID.JAD_CHALLENGE_3_COMPLETIONS, Category.OTHER, config::trackJadChallenges, "trackJadChallenges"));
		activityRegistry.add(new ActivityData("Jad Challenge 4", VarPlayerID.JAD_CHALLENGE_4_COMPLETIONS, Category.OTHER, config::trackJadChallenges, "trackJadChallenges"));
		activityRegistry.add(new ActivityData("Jad Challenge 5", VarPlayerID.JAD_CHALLENGE_5_COMPLETIONS, Category.OTHER, config::trackJadChallenges, "trackJadChallenges"));
		activityRegistry.add(new ActivityData("Jad Challenge 6", VarPlayerID.JAD_CHALLENGE_6_COMPLETIONS, Category.OTHER, config::trackJadChallenges, "trackJadChallenges"));
		activityRegistry.add(new ActivityData("Colosseum waves", VarPlayerID.TOTAL_COLOSSEUM_WAVES_COMPLETED, Category.OTHER, config::trackColosseum, "trackColosseum"));
		activityRegistry.add(new ActivityData("Gemstone Crab", VarPlayerID.TOTAL_GEMSTONE_CRAB_KILLS, Category.OTHER, config::trackGemstoneCrab, "trackGemstoneCrab"));
		activityRegistry.add(new ActivityData("Soul Wars wins", VarPlayerID.SOUL_WARS_TOTAL_WINS, Category.OTHER, config::trackSoulWars, "trackSoulWars"));
		activityRegistry.add(new ActivityData("Soul Wars games", VarPlayerID.SOUL_WARS_TOTAL_GAMES, Category.OTHER, config::trackSoulWars, "trackSoulWars"));
		activityRegistry.add(new ActivityData("Hunter Rumours", HUNTER_RUMOURS, Category.OTHER, config::trackHunterRumours, "trackHunterRumours"));
		activityRegistry.add(new ActivityData("Farming Contracts", FARMING_CONTRACTS, Category.OTHER, config::trackFarmingContracts, "trackFarmingContracts"));
		activityRegistry.add(new ActivityData("Mahogany Homes", MAHOGANY_HOMES, Category.OTHER, config::trackMahoganyHomesContracts, "trackMahoganyHomesContracts"));
		activityRegistry.add(new ActivityData("New collections logged", VarPlayerID.COLLECTION_COUNT, Category.OTHER, config::trackCollectionsLogged, "trackCollectionsLogged"));
		activityRegistry.add(new ActivityData("Bird eggs offered", BIRD_EGG_OFFERINGS, Category.OTHER, config::trackBirdEggOfferings, "trackBirdEggOfferings"));
		activityRegistry.add(new ActivityData("Player deaths", VarPlayerID.TRACKING_DEATHS, Category.OTHER, config::trackPlayerDeaths, "trackPlayerDeaths"));
		activityRegistry.add(new ActivityData("Player kills", VarPlayerID.TRACKING_PLAYERS_KILLED, Category.OTHER, config::trackPlayerKills, "trackPlayerKills"));
		activityRegistry.add(new ActivityData("Monster kills", VarPlayerID.TRACKING_MONSTERS_KILLED, Category.OTHER, config::trackMonsterKills, "trackMonsterKills"));
		activityRegistry.add(new ActivityData("Quests", QUESTS_COMPLETED, Category.OTHER, config::trackQuests, "trackQuests"));
		activityRegistry.add(new ActivityData("Quest points", VarPlayerID.QP, Category.OTHER, config::trackQuests, "trackQuests"));
		activityRegistry.add(new ActivityData("CA Diary tasks", CA_TASKS, Category.OTHER, config::trackCombatAchievements, "trackCombatAchievements"));
		activityRegistry.add(new ActivityData("CA Diary points", VarPlayerID.CA_GENERAL3, Category.OTHER, config::trackCombatAchievements, "trackCombatAchievements"));
		activityRegistry.add(new ActivityData("Mixology orders", VarPlayerID.TOTAL_MIXOLOGY_ORDERS, Category.OTHER, config::trackMixologyOrders, "trackMixologyOrders"));
		activityRegistry.add(new ActivityData("Music tracks unlocked", MUSIC_TRACK_UNLOCKS, Category.OTHER, config::trackMusicUnlocked, "trackMusicUnlocked"));
		activityRegistry.add(new ActivityData("Larran's small chests", LARRANS_SMALL_CHESTS, Category.OTHER, config::trackLarransChests, "trackLarransChests"));
		activityRegistry.add(new ActivityData("Larran's big chests", LARRANS_BIG_CHESTS, Category.OTHER, config::trackLarransChests, "trackLarransChests"));
		activityRegistry.add(new ActivityData("Damage dealt to NPCs", VarPlayerID.TRACKING_DAMAGE_DEALT_TO_NPCS, Category.OTHER, config::trackNpcDamage, "trackNpcDamage"));
		activityRegistry.add(new ActivityData("Special attacks used", VarPlayerID.TRACKING_SPECIAL_ATTACKS_USED, Category.OTHER, config::trackSpecialAttacks, "trackSpecialAttacks"));
		activityRegistry.add(new ActivityData("Damage taken from NPCs", VarPlayerID.TRACKING_DAMAGE_TAKEN_FROM_NPCS, Category.OTHER, config::trackNpcDamage, "trackNpcDamage"));
		activityRegistry.add(new ActivityData("Fish caught", VarPlayerID.TRACKING_FISH_CAUGHT, Category.OTHER, config::trackResourcesGathered, "trackResourcesGathered"));
		activityRegistry.add(new ActivityData("Logs chopped", VarPlayerID.TRACKING_LOGS_CHOPPED, Category.OTHER, config::trackResourcesGathered, "trackResourcesGathered"));
		activityRegistry.add(new ActivityData("Ore mined", VarPlayerID.TRACKING_ORE_MINED, Category.OTHER, config::trackResourcesGathered, "trackResourcesGathered"));
		activityRegistry.add(new ActivityData("Potions sipped", VarPlayerID.TRACKING_POTIONS_SIPPED, Category.OTHER, config::trackSuppliesConsumed, "trackSuppliesConsumed"));
		activityRegistry.add(new ActivityData("Food eaten", VarPlayerID.TRACKING_FOOD_EATEN, Category.OTHER, config::trackSuppliesConsumed, "trackSuppliesConsumed"));
		activityRegistry.add(new ActivityData("Coins gained", VarPlayerID.TRACKING_COINS_GAINED, Category.OTHER, config::trackCoins, "trackCoins"));
		activityRegistry.add(new ActivityData("Coins lost", VarPlayerID.TRACKING_COINS_LOST, Category.OTHER, config::trackCoins, "trackCoins"));
		activityRegistry.add(new ActivityData("Cannonballs fired", CANNON_BALLS, Category.OTHER, config::trackCannonballs, "trackCannonballs"));
		activityRegistry.add(new ActivityData("NMZ points", NMZ_POINTS, Category.OTHER, config::trackNightmareZonePoints, "trackNightmareZonePoints"));


		// Clue Scrolls
		activityRegistry.add(new ActivityData("Completed beginner clue", VarPlayerID.COMPLETED_CLUES5, Category.CLUE, config::trackClueScrolls, "trackClueScrolls"));
		activityRegistry.add(new ActivityData("Missed beginner clue", MISSED_BEGINNER_CLUES, Category.CLUE, config::trackClueScrolls, "trackClueScrolls"));
		activityRegistry.add(new ActivityData("Completed easy clue", VarPlayerID.COMPLETED_CLUES, Category.CLUE, config::trackClueScrolls, "trackClueScrolls"));
		activityRegistry.add(new ActivityData("Missed easy clue", MISSED_EASY_CLUES, Category.CLUE, config::trackClueScrolls, "trackClueScrolls"));
		activityRegistry.add(new ActivityData("Completed medium clue", VarPlayerID.COMPLETED_CLUES1, Category.CLUE, config::trackClueScrolls, "trackClueScrolls"));
		activityRegistry.add(new ActivityData("Missed medium clue", MISSED_MEDIUM_CLUES, Category.CLUE, config::trackClueScrolls, "trackClueScrolls"));
		activityRegistry.add(new ActivityData("Completed hard clue", VarPlayerID.COMPLETED_CLUES2, Category.CLUE, config::trackClueScrolls, "trackClueScrolls"));
		activityRegistry.add(new ActivityData("Missed hard clue", MISSED_HARD_CLUES, Category.CLUE, config::trackClueScrolls, "trackClueScrolls"));
		activityRegistry.add(new ActivityData("Completed elite clue", VarPlayerID.COMPLETED_CLUES3, Category.CLUE, config::trackClueScrolls, "trackClueScrolls"));
		activityRegistry.add(new ActivityData("Missed elite clue", MISSED_ELITE_CLUES, Category.CLUE, config::trackClueScrolls, "trackClueScrolls"));
		activityRegistry.add(new ActivityData("Completed master clue", VarPlayerID.COMPLETED_CLUES4, Category.CLUE, config::trackClueScrolls, "trackClueScrolls"));
		activityRegistry.add(new ActivityData("Mimic", VarPlayerID.TOTAL_MIMIC_KILLS, Category.CLUE, config::trackMimic, "trackMimic"));

		// Slayer Tasks
		activityRegistry.add(new ActivityData("Slayer tasks (Other)", VarbitID.SLAYER_TASKS_COMPLETED + VARBIT_OFFSET, Category.SLAYER, config::trackSlayerTasks, "trackSlayerTasks"));
		activityRegistry.add(new ActivityData("Slayer tasks (Wilderness)", VarbitID.SLAYER_WILDERNESS_TASKS_COMPLETED + VARBIT_OFFSET, Category.SLAYER, config::trackSlayerTasks, "trackSlayerTasks"));
		activityRegistry.add(new ActivityData("Slayer tasks (Mortimer)", VarPlayerID.SLAYER_MORTIMER_TASKS_COMPLETED, Category.SLAYER, config::trackSlayerTasks, "trackSlayerTasks"));
		activityRegistry.add(new ActivityData("Superior spawns", SUPERIOR_SPAWNS, Category.SLAYER, config::trackSuperiorSpawns, "trackSuperiorSpawns"));

		// Agility Courses
		activityRegistry.add(new ActivityData("Gnome Stronghold Laps", AGILITY_GNOME_STRONGHOLD, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Shayzien Laps (Basic)", AGILITY_SHAYZIEN_LOW, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Shayzien Laps (Advanced)", AGILITY_SHAYZIEN_HIGH, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Penguin Laps", AGILITY_PENGUIN, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Barbarian Outpost Laps", AGILITY_BARBARIAN_OUTPOST, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Ape Atoll Laps", AGILITY_APE_ATOLL, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Wilderness Laps", AGILITY_WILDERNESS, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Colossal Wyrm Laps (Advanced)", AGILITY_COLOSSAL_WYRM_ADVANCED, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Colossal Wyrm Laps (Basic)", AGILITY_COLOSSAL_WYRM_BASIC, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Werewolf Laps", AGILITY_WEREWOLF, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Prifddinas Laps", AGILITY_PRIFDDINAS, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Draynor Village Rooftop Laps", AGILITY_DRAYNOR_ROOFTOP, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Al Kharid Rooftop Laps", AGILITY_AL_KHARID_ROOFTOP, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Varrock Rooftop Laps", AGILITY_VARROCK_ROOFTOP, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Canifis Rooftop Laps", AGILITY_CANIFIS_ROOFTOP, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Falador Rooftop Laps", AGILITY_FALADOR_ROOFTOP, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Seers' Village Rooftop Laps", AGILITY_SEERS_ROOFTOP, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Pollnivneach Rooftop Laps", AGILITY_POLLNIVNEACH_ROOFTOP, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Rellekka Rooftop Laps", AGILITY_RELLEKKA_ROOFTOP, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Ardougne Rooftop Laps", AGILITY_ARDOUGNE_ROOFTOP, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Agility Pyramid Laps", AGILITY_PYRAMID, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Dorgesh-Kaan Laps", AGILITY_DORGESH_KAAN, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));
		activityRegistry.add(new ActivityData("Brimhaven Agility Tickets", AGILITY_BRIMHAVEN, Category.AGILITY, config::trackAgilityLaps, "trackAgilityLaps"));

		// Experience Tracking
		activityRegistry.add(new ActivityData("Total XP", XP_TOTAL, Category.EXPERIENCE, config::trackTotalXp, "trackTotalXp"));
		activityRegistry.add(new ActivityData("Attack XP", XP_ATTACK, Category.EXPERIENCE, config::trackAttackXp, "trackAttackXp"));
		activityRegistry.add(new ActivityData("Defence XP", XP_DEFENCE, Category.EXPERIENCE, config::trackDefenceXp, "trackDefenceXp"));
		activityRegistry.add(new ActivityData("Strength XP", XP_STRENGTH, Category.EXPERIENCE, config::trackStrengthXp, "trackStrengthXp"));
		activityRegistry.add(new ActivityData("Hitpoints XP", XP_HITPOINTS, Category.EXPERIENCE, config::trackHitpointsXp, "trackHitpointsXp"));
		activityRegistry.add(new ActivityData("Ranged XP", XP_RANGED, Category.EXPERIENCE, config::trackRangedXp, "trackRangedXp"));
		activityRegistry.add(new ActivityData("Prayer XP", XP_PRAYER, Category.EXPERIENCE, config::trackPrayerXp, "trackPrayerXp"));
		activityRegistry.add(new ActivityData("Magic XP", XP_MAGIC, Category.EXPERIENCE, config::trackMagicXp, "trackMagicXp"));
		activityRegistry.add(new ActivityData("Cooking XP", XP_COOKING, Category.EXPERIENCE, config::trackCookingXp, "trackCookingXp"));
		activityRegistry.add(new ActivityData("Woodcutting XP", XP_WOODCUTTING, Category.EXPERIENCE, config::trackWoodcuttingXp, "trackWoodcuttingXp"));
		activityRegistry.add(new ActivityData("Fletching XP", XP_FLETCHING, Category.EXPERIENCE, config::trackFletchingXp, "trackFletchingXp"));
		activityRegistry.add(new ActivityData("Fishing XP", XP_FISHING, Category.EXPERIENCE, config::trackFishingXp, "trackFishingXp"));
		activityRegistry.add(new ActivityData("Firemaking XP", XP_FIREMAKING, Category.EXPERIENCE, config::trackFiremakingXp, "trackFiremakingXp"));
		activityRegistry.add(new ActivityData("Crafting XP", XP_CRAFTING, Category.EXPERIENCE, config::trackCraftingXp, "trackCraftingXp"));
		activityRegistry.add(new ActivityData("Smithing XP", XP_SMITHING, Category.EXPERIENCE, config::trackSmithingXp, "trackSmithingXp"));
		activityRegistry.add(new ActivityData("Mining XP", XP_MINING, Category.EXPERIENCE, config::trackMiningXp, "trackMiningXp"));
		activityRegistry.add(new ActivityData("Herblore XP", XP_HERBLORE, Category.EXPERIENCE, config::trackHerbloreXp, "trackHerbloreXp"));
		activityRegistry.add(new ActivityData("Agility XP", XP_AGILITY, Category.EXPERIENCE, config::trackAgilityXp, "trackAgilityXp"));
		activityRegistry.add(new ActivityData("Thieving XP", XP_THIEVING, Category.EXPERIENCE, config::trackThievingXp, "trackThievingXp"));
		activityRegistry.add(new ActivityData("Slayer XP", XP_SLAYER, Category.EXPERIENCE, config::trackSlayerXp, "trackSlayerXp"));
		activityRegistry.add(new ActivityData("Farming XP", XP_FARMING, Category.EXPERIENCE, config::trackFarmingXp, "trackFarmingXp"));
		activityRegistry.add(new ActivityData("Runecraft XP", XP_RUNECRAFT, Category.EXPERIENCE, config::trackRunecraftXp, "trackRunecraftXp"));
		activityRegistry.add(new ActivityData("Hunter XP", XP_HUNTER, Category.EXPERIENCE, config::trackHunterXp, "trackHunterXp"));
		activityRegistry.add(new ActivityData("Construction XP", XP_CONSTRUCTION, Category.EXPERIENCE, config::trackConstructionXp, "trackConstructionXp"));
		activityRegistry.add(new ActivityData("Sailing XP", XP_SAILING, Category.EXPERIENCE, config::trackSailingXp, "trackSailingXp"));

		// Level Tracking
		activityRegistry.add(new ActivityData("Total Level", LVL_TOTAL, Category.LEVELS, config::trackTotalLevel, "trackTotalLevel"));
		activityRegistry.add(new ActivityData("Attack Level", LVL_ATTACK, Category.LEVELS, config::trackAttackLevel, "trackAttackLevel"));
		activityRegistry.add(new ActivityData("Defence Level", LVL_DEFENCE, Category.LEVELS, config::trackDefenceLevel, "trackDefenceLevel"));
		activityRegistry.add(new ActivityData("Strength Level", LVL_STRENGTH, Category.LEVELS, config::trackStrengthLevel, "trackStrengthLevel"));
		activityRegistry.add(new ActivityData("Hitpoints Level", LVL_HITPOINTS, Category.LEVELS, config::trackHitpointsLevel, "trackHitpointsLevel"));
		activityRegistry.add(new ActivityData("Ranged Level", LVL_RANGED, Category.LEVELS, config::trackRangedLevel, "trackRangedLevel"));
		activityRegistry.add(new ActivityData("Prayer Level", LVL_PRAYER, Category.LEVELS, config::trackPrayerLevel, "trackPrayerLevel"));
		activityRegistry.add(new ActivityData("Magic Level", LVL_MAGIC, Category.LEVELS, config::trackMagicLevel, "trackMagicLevel"));
		activityRegistry.add(new ActivityData("Cooking Level", LVL_COOKING, Category.LEVELS, config::trackCookingLevel, "trackCookingLevel"));
		activityRegistry.add(new ActivityData("Woodcutting Level", LVL_WOODCUTTING, Category.LEVELS, config::trackWoodcuttingLevel, "trackWoodcuttingLevel"));
		activityRegistry.add(new ActivityData("Fletching Level", LVL_FLETCHING, Category.LEVELS, config::trackFletchingLevel, "trackFletchingLevel"));
		activityRegistry.add(new ActivityData("Fishing Level", LVL_FISHING, Category.LEVELS, config::trackFishingLevel, "trackFishingLevel"));
		activityRegistry.add(new ActivityData("Firemaking Level", LVL_FIREMAKING, Category.LEVELS, config::trackFiremakingLevel, "trackFiremakingLevel"));
		activityRegistry.add(new ActivityData("Crafting Level", LVL_CRAFTING, Category.LEVELS, config::trackCraftingLevel, "trackCraftingLevel"));
		activityRegistry.add(new ActivityData("Smithing Level", LVL_SMITHING, Category.LEVELS, config::trackSmithingLevel, "trackSmithingLevel"));
		activityRegistry.add(new ActivityData("Mining Level", LVL_MINING, Category.LEVELS, config::trackMiningLevel, "trackMiningLevel"));
		activityRegistry.add(new ActivityData("Herblore Level", LVL_HERBLORE, Category.LEVELS, config::trackHerbloreLevel, "trackHerbloreLevel"));
		activityRegistry.add(new ActivityData("Agility Level", LVL_AGILITY, Category.LEVELS, config::trackAgilityLevel, "trackAgilityLevel"));
		activityRegistry.add(new ActivityData("Thieving Level", LVL_THIEVING, Category.LEVELS, config::trackThievingLevel, "trackThievingLevel"));
		activityRegistry.add(new ActivityData("Slayer Level", LVL_SLAYER, Category.LEVELS, config::trackSlayerLevel, "trackSlayerLevel"));
		activityRegistry.add(new ActivityData("Farming Level", LVL_FARMING, Category.LEVELS, config::trackFarmingLevel, "trackFarmingLevel"));
		activityRegistry.add(new ActivityData("Runecraft Level", LVL_RUNECRAFTING, Category.LEVELS, config::trackRunecraftLevel, "trackRunecraftLevel"));
		activityRegistry.add(new ActivityData("Hunter Level", LVL_HUNTER, Category.LEVELS, config::trackHunterLevel, "trackHunterLevel"));
		activityRegistry.add(new ActivityData("Construction Level", LVL_CONSTRUCTION, Category.LEVELS, config::trackConstructionLevel, "trackConstructionLevel"));
		activityRegistry.add(new ActivityData("Sailing Level", LVL_SAILING, Category.LEVELS, config::trackSailingLevel, "trackSailingLevel"));
	}
}