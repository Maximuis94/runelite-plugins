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
import com.activitycounter.listeners.AbstractListenerLeaf;
import com.activitycounter.listeners.AbstractListenerNode;
import com.activitycounter.listeners.PreprocessedChatListener;
import com.activitycounter.listeners.conditional.BoltProcListener;
import com.activitycounter.listeners.conditional.FarmingListener;
import com.activitycounter.listeners.conditional.GrandExchangeListener;
import com.activitycounter.listeners.conditional.HerbloreListener;
import com.activitycounter.listeners.conditional.MagicSpellListener;
import com.activitycounter.listeners.core.ChatMessageListener;
import com.activitycounter.listeners.core.ConfigListener;
import com.activitycounter.listeners.core.EquippedItemListener;
import com.activitycounter.listeners.core.ExperienceDropListener;
import com.activitycounter.listeners.core.InventoryListener;
import com.activitycounter.listeners.core.PlayerListener;
import com.activitycounter.listeners.core.SoundEffectListener;
import com.activitycounter.listeners.core.StatChangeListener;
import com.activitycounter.listeners.core.VarbitListener;
import com.activitycounter.managers.IdleMessageManager;
import com.activitycounter.managers.PresetManager;
import com.activitycounter.managers.SessionStorageManager;
import com.activitycounter.models.Category;
import com.activitycounter.models.ConfigPreset;
import com.activitycounter.models.Count;
import com.activitycounter.models.Session;
import com.activitycounter.models.TrackedActivity;
import com.activitycounter.models.TrackerType;
import com.activitycounter.ui.ActivityCounterOverlay;
import com.activitycounter.ui.ActivityCounterPanel;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Filepath;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = PLUGIN_NAME,
	internalName = PluginConstants.PLUGIN_DIR_NAME,
	description = "Highly customizable plugin that counts spells/laps/tablets/bolt procs/sneaky suspicions/tax and more per manually defined session.",
	tags = {"session","tracker","counter","kc","killcount","activity","lap","minigame","runes","spells","supplies","stats","skilling","sneaky","suspicion"}
)
public class ActivityCounterPlugin extends Plugin
{
	@Getter
	private int loadedRegionId;

	@Getter
	private final List<TrackedActivity> pinnedActivities = new ArrayList<>();

	// Safely tracks which conditional listeners are actively registered to the EventBus
	private final Set<AbstractListenerLeaf> dynamicallyRegisteredLeaves = new HashSet<>();

	public void togglePinnedActivity(TrackedActivity activity)
	{
		if (pinnedActivities.contains(activity))
		{
			pinnedActivities.remove(activity);
		}
		else
		{
			pinnedActivities.add(activity);
		}
	}

	public void movePinnedActivityToTop(TrackedActivity activity)
	{
		pinnedActivities.remove(activity);
		pinnedActivities.add(0, activity);
	}

	public boolean isActivityPinned(TrackedActivity activity)
	{
		return pinnedActivities.contains(activity);
	}

	@Inject
	private EventBus eventBus;

	@Inject
	private ChatMessageListener chatMessageListener;

	@Inject
	private StatChangeListener statChangeListener;

	@Inject
	private VarbitListener varbitListener;

	@Inject
	private SoundEffectListener soundEffectListener;

	@Inject
	private ExperienceDropListener experienceDropListener;

	@Inject
	private BoltProcListener boltProcListener;

	@Inject
	private FarmingListener farmingListener;

	@Inject
	private EquippedItemListener equippedItemListener;

	@Inject
	private InventoryListener inventoryListener;

	@Getter
	private int loginTicks = 0;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Getter
	@Inject
	private ActivityCounterConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ActivityCounterOverlay activityOverlay;

	@Inject
	private SessionStorageManager storageManager;

	@Inject
	private PresetManager presetManager;

	@Inject
	private ConfigListener configListener;

	@Inject
	private ConfigManager configManager;

	@Inject
	private IdleMessageManager idleMessageManager;

	@Inject
	private PlayerListener playerListener;

	@Inject
	private MagicSpellListener magicSpellListener;

	@Inject
	private GrandExchangeListener grandExchangeTaxListener;

	@Inject
	private HerbloreListener herbloreListener;

	@Getter
	@Inject
	private SpriteManager spriteManager;

	@Getter
	private boolean sessionPaused = false;

	public void toggleSessionPaused() {
		this.sessionPaused = !this.sessionPaused;
		if (panel != null) {
			panel.updatePauseButtonState();
		}
	}



	/**
	 * Listener classes that other classes depend on in some way
	 */
	public AbstractListenerNode[] getCoreListeners() {
		return new AbstractListenerNode[] {
			configListener,
			chatMessageListener,
			statChangeListener,
			varbitListener,
			equippedItemListener,
			soundEffectListener,
			experienceDropListener,
			playerListener,
			inventoryListener,
		};
	}

	/**
	 * Listener classes that only listen and count
	 */
	public AbstractListenerLeaf[] getConditionalListeners() {
		return new AbstractListenerLeaf[] {
			boltProcListener,
			farmingListener,
			herbloreListener,
			magicSpellListener,
			grandExchangeTaxListener,
		};
	}

	@Provides
	ActivityCounterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ActivityCounterConfig.class);
	}

	public Filepath getDirectory()
	{
		try
		{
			return getPluginDirectory();
		}
		catch (IOException e)
		{
			log.error("Failed to get plugin directory", e);
			return null;
		}
	}

	@Getter
	private Session currentSession = null;
	private boolean pendingSessionResume = false;
	@Getter
	private ActivityCounterPanel panel;
	private NavigationButton navButton;

	@Setter
	private boolean requiresSaveAndRefresh = false;

	public boolean isShowSessionDuration() {
		return config.showSessionDuration();
	}

	public boolean isConfirmSessionTermination() {
		return config.confirmTerminateSession();
	}

	public boolean isLoggedIn()
	{
		return client != null && client.getGameState() == GameState.LOGGED_IN;
	}

	@Override
	protected void startUp() {
		for (AbstractListenerNode listener : getCoreListeners()) {
			listener.register();
		}
		configListener.buildAllCaches();
		soundEffectListener.initializeRoutingCaches();

		// Ensure default exists, then load the last used preset (defaults to "")
		presetManager.ensureDefaultPresetExists();
		presetManager.loadPresetByName(config.lastPresetUsed());

		// Initial evaluation of all conditional toggles
		evaluateConditionalListeners();

		panel = new ActivityCounterPanel(this, soundEffectListener, configListener, presetManager, idleMessageManager);
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");
		navButton = NavigationButton.builder()
			.tooltip(PLUGIN_NAME)
			.icon(icon)
			.priority(10)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		if (panel != null) {
			panel.reloadComboBox();
			panel.refreshKcContainer();
		}
		overlayManager.add(activityOverlay);
	}

	@Override
	protected void shutDown() {
		presetManager.saveDefaultPreset();

		for (AbstractListenerNode listener : getCoreListeners()) {
			listener.unregister();
		}

		for (AbstractListenerLeaf listener : dynamicallyRegisteredLeaves) {
			listener.unregister();
		}
		dynamicallyRegisteredLeaves.clear();

		clientToolbar.removeNavigation(navButton);

		if (currentSession != null)
		{
			saveSession(currentSession);
		}

		overlayManager.remove(activityOverlay);
	}

	/**
	 * Iterates over all conditional listeners and mounts/unmounts them based
	 * entirely on the logic defined in their respective isEnabled() methods.
	 */
	public void evaluateConditionalListeners()
	{
		for (AbstractListenerLeaf listener : getConditionalListeners())
		{
			boolean needed = listener.isEnabled();
			boolean registered = dynamicallyRegisteredLeaves.contains(listener);

			if (needed && !registered)
			{
				listener.register();

				if (listener instanceof PreprocessedChatListener) {
					chatMessageListener.register((PreprocessedChatListener) listener);
				}

				if (currentSession != null && currentSession.isInProgress())
				{
					listener.onSessionStarted(currentSession);
				}
				dynamicallyRegisteredLeaves.add(listener);
				log.debug("Dynamically registered {}", listener.getClass().getSimpleName());
			}
			else if (!needed && registered)
			{
				if (currentSession != null)
				{
					listener.onSessionStopped(currentSession);
				}

				if (listener instanceof PreprocessedChatListener) {
					chatMessageListener.unregister((PreprocessedChatListener) listener);
				}

				listener.unregister();
				dynamicallyRegisteredLeaves.remove(listener);
				log.debug("Dynamically unregistered {}", listener.getClass().getSimpleName());
			}
		}
	}

	public void saveSession(Session session)
	{
		if (session == null) return;

		if (config.enableSessionConfigurations() && session.isInProgress())
		{
			if (session.getSettings() == null)
			{
				session.setSettings(new HashMap<>());
			}

			session.getSettings().clear();
			for (String key : PresetManager.PRESET_KEYS)
			{
				String value = configManager.getConfiguration(PluginConstants.CONFIG_GROUP, key);
				if (value != null)
				{
					session.getSettings().put(key, value);
				}
			}
		}
		storageManager.saveSession(session);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOGGED_IN)
		{
			loginTicks = 0;
			pendingSessionResume = true;
		}
		else if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
		{
			if (currentSession != null && state == GameState.LOGIN_SCREEN)
			{
				storageManager.saveSession(currentSession);
			}
			currentSession = null;
			pendingSessionResume = false;
			panel.reloadComboBox();
			loadedRegionId = -1;
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		Hitsplat hitsplat = event.getHitsplat();
		int type = hitsplat.getHitsplatType();
		if (configListener.isKcVisible(TrackedActivity.MAX_HITS) && type == HitsplatID.DAMAGE_MAX_ME)
		{
			increaseCountByOne(TrackedActivity.MAX_HITS.getId());
		}
	}

	public void startSession()
	{
		clientThread.invokeLater(() ->
		{
			if (client == null || client.getGameState() != GameState.LOGGED_IN)
			{
				return;
			}
			sessionPaused = false;

			String accountName = client.getLocalPlayer() != null ? client.getLocalPlayer().getName() : "Unknown";
			currentSession = new Session(client.getAccountHash(), accountName);
			initializeKillCounts(true);

			syncActiveSessionScores();
			storageManager.saveSession(currentSession);

			for (AbstractListenerNode listener : getCoreListeners())
			{
				listener.onSessionStarted(currentSession);
			}
			// Only push session state to actively registered leaves
			for (AbstractListenerLeaf listener : dynamicallyRegisteredLeaves)
			{
				listener.onSessionStarted(currentSession);
			}
			idleMessageManager.rollNewTip();
			log.debug("Session started.");
			panel.reloadComboBox();
		});
	}

	public void closeSession()
	{
		clientThread.invokeLater(() ->
		{
			if (currentSession != null)
			{
				sessionPaused = false;
				currentSession.setInProgress(false);
				currentSession.setEndTime(Instant.now());
				storageManager.saveSession(currentSession);

				Session closedSession = currentSession;
				currentSession = null;

				for (AbstractListenerNode listener : getCoreListeners())
				{
					listener.onSessionStopped(closedSession);
				}
				for (AbstractListenerLeaf listener : dynamicallyRegisteredLeaves)
				{
					listener.onSessionStopped(closedSession);
				}

				log.debug("Session closed and archived.");
				panel.reloadComboBox();
			}
		});
	}

	public List<Session> getArchivedSessions()
	{
		if (client == null || client.getGameState() != GameState.LOGGED_IN)
		{
			return storageManager.loadAllArchivedSessions();
		}
		return storageManager.loadArchivedSessions(client.getAccountHash());
	}

	public void deleteArchivedSession(Session session)
	{
		if (session == null || (currentSession != null && currentSession.getId().equals(session.getId())))
		{
			return;
		}

		boolean deleted = storageManager.deleteSession(session);
		if (deleted)
		{
			log.debug("Deleted archived session: {}", session.getId());
			panel.forceActiveSessionSelection();
			panel.reloadComboBox();
		}
		else
		{
			log.warn("Failed to delete session file for ID: {}", session.getId());
		}
	}

	public void updatePanelTime()
	{
		if (panel != null)
		{
			panel.updateTime();
		}
	}

	public void disableActivity(TrackedActivity activity) {
		configListener.disableActivity(activity);
		if (panel != null) {
			panel.refreshKcContainer();
		}
	}

	public List<Count> getDisplayKcs(Session session)
	{
		if (session == null) return new ArrayList<>();

		List<Count> visibleKcs = session.getAllKillCounts().stream()
			.filter(kc -> kc.getSessionKc() > 0 && configListener.isKcVisible(kc.getTrackingId()))
			.collect(Collectors.toList());

		if (config.mergeSlayerTaskCounts())
		{
			int otherId = VarbitID.SLAYER_TASKS_COMPLETED;
			int wildyId = VarbitID.SLAYER_WILDERNESS_TASKS_COMPLETED;
			int mortimerId = VarPlayerID.SLAYER_MORTIMER_TASKS_COMPLETED;

			int mergedSessionKc = 0;

			Iterator<Count> iterator = visibleKcs.iterator();
			while (iterator.hasNext())
			{
				Count kc = iterator.next();
				int trackingId = kc.getTrackingId();
				if (trackingId == otherId || trackingId == wildyId || trackingId == mortimerId)
				{
					mergedSessionKc += kc.getSessionKc();
					iterator.remove();
				}
			}

			if (mergedSessionKc > 0)
			{
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

		String eventKey = event.getKey();
		if (eventKey.equals("activityScores")) {
			syncActiveSessionScores();
			setRequiresSaveAndRefresh(true);
		} else if (eventKey.equals("showPresetsTab")) {
			clientThread.invokeLater(() -> {
				if (panel != null) {
					panel.updatePresetsTabVisibility();
				}
			});
		}

		evaluateConditionalListeners();

		clientThread.invokeLater(() -> {
			if (panel != null) panel.refreshKcContainer();
		});
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (pendingSessionResume && client.getAccountHash() != -1)
		{
			pendingSessionResume = false;
			long accountHash = client.getAccountHash();

			if (currentSession == null)
			{
				Session activeSession = storageManager.loadActiveSession(accountHash);
				if (activeSession != null) {
					currentSession = activeSession;

					// Load attached preset if it exists
					if (currentSession.getAttachedPresetName() != null) {
						ConfigPreset attached = presetManager.getPresetByName(currentSession.getAttachedPresetName());
						if (attached != null) {
							presetManager.applyPreset(attached);
						} else {
							log.warn("Attached preset '{}' not found.", currentSession.getAttachedPresetName());
						}
					} else if (config.enableSessionConfigurations() && currentSession.getSettings() != null) {
						for (Map.Entry<String, String> entry : currentSession.getSettings().entrySet()) {
							configManager.setConfiguration(PluginConstants.CONFIG_GROUP, entry.getKey(), entry.getValue());
						}
					}

					initializeKillCounts(false);
					syncActiveSessionScores();

					for (AbstractListenerNode listener : getCoreListeners())
					{
						listener.onSessionStarted(currentSession);
					}
					for (AbstractListenerLeaf listener : dynamicallyRegisteredLeaves)
					{
						listener.onSessionStarted(currentSession);
					}

					log.debug("Resumed active session: {}", currentSession.getId());
					panel.forceActiveSessionSelection();
					panel.reloadComboBox();
				}
			}
		}

		if (currentSession != null && currentSession.isInProgress() && loginTicks < 5)
		{
			loginTicks++;
		}

		if (requiresSaveAndRefresh && currentSession != null)
		{
			storageManager.saveSession(currentSession);
			if (panel != null)
			{
				panel.refreshKcContainer();
			}
			requiresSaveAndRefresh = false;
		}
	}

	public void renameSession(Session sessionToRename, String newName)
	{
		if (sessionToRename != null && newName != null && !newName.trim().isEmpty())
		{
			String trimmedName = newName.trim();

			sessionToRename.setSessionName(trimmedName);
			storageManager.saveSession(sessionToRename);

			if (currentSession != null && currentSession.getId().equals(sessionToRename.getId()))
			{
				currentSession.setSessionName(trimmedName);
			}

			panel.reloadComboBox();
			log.debug("Renamed session to: {}", trimmedName);
		}
	}

	private void initializeKillCounts(boolean isManualStart)
	{
		if (currentSession == null) return;

		if (isManualStart)
		{
			varbitListener.resetBaselines();
		}

		for (TrackedActivity act : TrackedActivity.values())
		{
			if (!currentSession.isTracking(act.getId()))
			{
				Count kc = new Count(act.getName(), act.getId());
				kc.setSessionKc(0);
				kc.setInitialKc(-1);

				if (act.getCategory() == Category.AGILITY)
				{
					kc.setInitialKc(-1);
				}
				else if (isManualStart && client != null && client.getGameState() == GameState.LOGGED_IN) {
					try
					{
						switch (act.getTrackerType())
						{
							case VARPLAYER_VALUE:
							case VARBIT_VALUE:
								int initialBaseline = 0;
								int[] sourceIds = act.getGameSourceIds();

								if (sourceIds != null) {
									for (int sourceId : sourceIds) {
										if (sourceId > 0) {
											initialBaseline += (act.getTrackerType() == TrackerType.VARPLAYER_VALUE)
												? client.getVarpValue(sourceId)
												: client.getVarbitValue(sourceId);
										}
									}
								}
								kc.setInitialKc(initialBaseline);
								break;

							case CHAT_MESSAGE:
							case SOUND_EFFECT:
							case AREA_SOUND:
							case CUSTOM:
								kc.setInitialKc(0);
								break;
							case STAT_CHANGE:
								break;
						}
					} catch (IndexOutOfBoundsException e) {
						log.error("{} for TrackedActivity {} {} {}", e.getClass(), act.getName(), act.getId(), act.getConfigId());
					}
				}
				else if (act.getTrackerType() == TrackerType.CHAT_MESSAGE ||
					act.getTrackerType() == TrackerType.CUSTOM ||
					act.getTrackerType() == TrackerType.SOUND_EFFECT ||
					act.getTrackerType() == TrackerType.AREA_SOUND)
				{
					kc.setInitialKc(0);
				}

				currentSession.addKillCount(kc);
			}
		}

		if (isManualStart && client != null && client.getGameState() == GameState.LOGGED_IN)
		{
			for (Skill skill : Skill.values())
			{
				int xpTrackingId = statChangeListener.getSkillTrackingId(skill);
				if (currentSession.isTracking(xpTrackingId))
				{
					Count kc = currentSession.getKillCount(xpTrackingId);
					if (kc.getInitialKc() == -1)
					{
						kc.setInitialKc(client.getSkillExperience(skill));
						currentSession.initializeSkill(skill, client.getSkillExperience(skill));
					}
				}

				int lvlTrackingId = statChangeListener.getSkillLevelTrackingId(skill);
				if (currentSession.isTracking(lvlTrackingId))
				{
					Count kc = currentSession.getKillCount(lvlTrackingId);
					if (kc.getInitialKc() == -1)
					{
						kc.setInitialKc(client.getRealSkillLevel(skill));
					}
				}
			}

			int totalLvlId = TrackedActivity.LVL_TOTAL.getId();
			if (currentSession.isTracking(totalLvlId))
			{
				Count kc = currentSession.getKillCount(totalLvlId);
				if (kc.getInitialKc() == -1)
				{
					kc.setInitialKc(client.getTotalLevel());
				}
			}
		}
	}

	public TrackedActivity getActivityById(int trackingId) {
		for (TrackedActivity act : TrackedActivity.values()) {
			if (act.getId() == trackingId) {
				return act;
			}
		}
		return null;
	}

	public Category getActivityCategory(int trackingId) {
		TrackedActivity act = getActivityById(trackingId);
		return act != null ? act.getCategory() : Category.OTHER;
	}

	public int getActivityOrder(int trackingId) {
		TrackedActivity act = getActivityById(trackingId);
		return act != null ? act.ordinal() : Integer.MAX_VALUE;
	}

	public boolean hasActiveSession() {
		return currentSession != null && currentSession.isInProgress() && !sessionPaused;
	}

	public boolean isTracking(int activityId) {
		return hasActiveSession() && currentSession.isTracking(activityId);
	}

	public void tickSessionTime() {
		if (hasActiveSession()) {
			currentSession.addSecond();
			if (isShowSessionDuration()) {
				updatePanelTime();
			}
		}
	}

	public void increaseCountByOne(int activityId)
	{
		if (!hasActiveSession()) return;
		if (currentSession.isTracking(activityId)) {
			Count kc = currentSession.getKillCount(activityId);
			if (kc != null) {
				if (kc.getInitialKc() == -1) {
					kc.setInitialKc(0);
				}
				kc.setSessionKc(kc.getSessionKc() + 1);
				setRequiresSaveAndRefresh(true);
			}
		}
	}

	public void increaseCountByAmount(int activityId, int amount)
	{
		if (!hasActiveSession()) return;
		if (currentSession.isTracking(activityId)) {
			Count kc = currentSession.getKillCount(activityId);
			if (kc != null) {
				if (kc.getInitialKc() == -1) {
					kc.setInitialKc(0);
				}
				kc.setSessionKc(kc.getSessionKc() + amount);
				setRequiresSaveAndRefresh(true);
			}
		}
	}

	public void processActivityUpdate(int activityId, int currentValue)
	{
		if (!hasActiveSession() || getLoginTicks() < 5) return;
		Session session = getCurrentSession();
		if (session == null) return;

		Count kc = session.getKillCount(activityId);
		if (kc != null) {
			if (kc.getInitialKc() == -1) {
				kc.setInitialKc(currentValue);
			}

			int newSessionKc = currentValue - kc.getInitialKc();
			if (newSessionKc > kc.getSessionKc()) {
				kc.setSessionKc(newSessionKc);
				setRequiresSaveAndRefresh(true);
			}
		}
	}

	public void setActivityScore(TrackedActivity activity, float score) {
		configListener.setActivityScore(activity, score);
	}

	public void setCountAbsolute(int activityId, int absoluteValue) {
		if (!hasActiveSession()) return;
		if (currentSession.isTracking(activityId)) {
			Count kc = currentSession.getKillCount(activityId);
			if (kc != null) {
				kc.setSessionKc(absoluteValue);
				setRequiresSaveAndRefresh(true);
			}
		}
	}

	public void syncActiveSessionScores() {
		if (currentSession != null && currentSession.isInProgress()) {
			for (Count kc : currentSession.getAllKillCounts()) {
				kc.setScore(configListener.getScore(kc.getTrackingId()));
			}
		}
	}

	public void pinAllActivities(Iterable<TrackedActivity> activities)
	{
		for (TrackedActivity activity : activities)
		{
			pinnedActivities.add(activity);
		}
	}

	public void unpinAllActivities(Iterable<TrackedActivity> activities)
	{
		for (TrackedActivity activity : activities)
		{
			pinnedActivities.remove(activity.getId());
		}
	}

	public void resetActivityCount(Session targetSession, TrackedActivity activity)
	{
		if (targetSession == null || activity == null) return;

		if (targetSession != currentSession || !targetSession.isInProgress())
		{
			Count kc = targetSession.getKillCount(activity.getId());
			if (kc != null)
			{
				kc.setSessionKc(0);
				saveSession(targetSession);
				if (panel != null) panel.refreshKcContainer();
			}
			return;
		}

		clientThread.invokeLater(() ->
		{
			Count kc = currentSession.getKillCount(activity.getId());
			if (kc != null)
			{
				resetActivityCountSync(kc, activity);
				setRequiresSaveAndRefresh(true);
			}
		});
	}

	public void resetAllActivityCounts(Session targetSession)
	{
		if (targetSession == null) return;

		if (targetSession != currentSession || !targetSession.isInProgress())
		{
			for (Count kc : targetSession.getAllKillCounts())
			{
				kc.setSessionKc(0);
			}
			saveSession(targetSession);
			if (panel != null) panel.refreshKcContainer();
			return;
		}

		clientThread.invokeLater(() ->
		{
			for (Count kc : currentSession.getAllKillCounts())
			{
				TrackedActivity activity = getActivityById(kc.getTrackingId());
				if (activity != null)
				{
					resetActivityCountSync(kc, activity);
				}
				else
				{
					kc.setSessionKc(0);
				}
			}
			setRequiresSaveAndRefresh(true);
		});
	}

	private void resetActivityCountSync(Count kc, TrackedActivity activity)
	{
		kc.setSessionKc(0);

		if (client != null && client.getGameState() == GameState.LOGGED_IN)
		{
			switch (activity.getTrackerType())
			{
				case VARPLAYER_VALUE:
				case VARBIT_VALUE:
					int initialBaseline = 0;
					int[] sourceIds = activity.getGameSourceIds();
					if (sourceIds != null)
					{
						for (int sourceId : sourceIds)
						{
							if (sourceId > 0)
							{
								initialBaseline += (activity.getTrackerType() == TrackerType.VARPLAYER_VALUE)
									? client.getVarpValue(sourceId)
									: client.getVarbitValue(sourceId);
							}
						}
					}
					kc.setInitialKc(initialBaseline);
					break;

				case STAT_CHANGE:
					if (activity.getId() == TrackedActivity.LVL_TOTAL.getId())
					{
						kc.setInitialKc(client.getTotalLevel());
					}
					else
					{
						for (Skill skill : Skill.values())
						{
							if (statChangeListener.getSkillTrackingId(skill) == activity.getId())
							{
								int currentXp = client.getSkillExperience(skill);
								kc.setInitialKc(currentXp);
								currentSession.initializeSkill(skill, currentXp);
								break;
							}
							if (statChangeListener.getSkillLevelTrackingId(skill) == activity.getId())
							{
								kc.setInitialKc(client.getRealSkillLevel(skill));
								break;
							}
						}
					}
					break;

				case CHAT_MESSAGE:
				case SOUND_EFFECT:
				case AREA_SOUND:
				case CUSTOM:
					kc.setInitialKc(0);
					break;
			}
		}
		else
		{
			kc.setInitialKc(-1);
		}
	}

	public static String formatCountDisplay(int count, int secondsPassed, boolean isHourly)
	{
		if (isHourly)
		{
			if (secondsPassed > 0)
			{
				double hours = secondsPassed / 3600.0;
				double rate = count / hours;

				if (rate >= 100.0)
				{
					return String.format("%,d/hr", Math.round(rate));
				}
				else
				{
					return String.format("%,.1f/hr", rate);
				}
			}
			return "0/hr";
		}

		return String.format("%,d", count);
	}
}