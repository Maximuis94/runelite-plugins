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
import com.activitycounter.listeners.conditional.BoltProcListener;
import com.activitycounter.listeners.conditional.FarmingListener;
import com.activitycounter.listeners.conditional.GrandExchangeTaxListener;
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
import com.activitycounter.models.Category;
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
import java.util.Iterator;
import java.util.List;
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
	private final List<Integer> pinnedActivityIds = new ArrayList<>();

	public void togglePinnedActivity(TrackedActivity activity)
	{
		int id = activity.getId();
		if (pinnedActivityIds.contains(id))
		{
			pinnedActivityIds.remove((Integer) id);
		}
		else
		{
			pinnedActivityIds.add(id);
		}
	}

	public void movePinnedActivityToTop(TrackedActivity activity)
	{
		int id = activity.getId();
		pinnedActivityIds.remove((Integer) id);
		pinnedActivityIds.add(0, id);
	}

	public boolean isActivityPinned(TrackedActivity activity)
	{
		return pinnedActivityIds.contains(activity.getId());
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
	private ConfigListener configListener;

	@Inject
	private PlayerListener playerListener;

	@Inject
	private MagicSpellListener magicSpellListener;

	@Inject
	private GrandExchangeTaxListener grandExchangeTaxListener;


	/**
	 * Listener classes that other classes depend on in some way
	 */
	private AbstractListenerNode[] getCoreListeners() {
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
	private AbstractListenerLeaf[] getConditionalListeners() {
		return new AbstractListenerLeaf[] {
			boltProcListener,
			farmingListener,
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
	protected void startUp()
	{
		for (AbstractListenerNode listener : getCoreListeners()) {
			listener.register();
		}
		configListener.buildAllCaches();
		soundEffectListener.initializeRoutingCaches();


		for (AbstractListenerLeaf listener : getConditionalListeners()) {
			listener.register();
		}

		panel = new ActivityCounterPanel(this, soundEffectListener, configListener);
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");
		navButton = NavigationButton.builder()
			.tooltip(PLUGIN_NAME)
			.icon(icon)
			.priority(10)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
		panel.reloadComboBox();
		overlayManager.add(activityOverlay);
	}

	@Override
	protected void shutDown()
	{
		for (AbstractListenerNode listener : getCoreListeners()) {
			listener.unregister();
		}

		for (AbstractListenerLeaf listener : getConditionalListeners()) {
			listener.unregister();
		}
		clientToolbar.removeNavigation(navButton);
		if (currentSession != null)
		{
			storageManager.saveSession(currentSession);
		}
		overlayManager.remove(activityOverlay);
	}

	public void saveSession(Session session)
	{
		if (session != null)
		{
			storageManager.saveSession(session);
		}
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

	/**
	 * Starts a new session and passes it on to all of the active Listener classes
	 */
	public void startSession()
	{
		clientThread.invokeLater(() ->
		{
			if (client == null || client.getGameState() != GameState.LOGGED_IN)
			{
				return;
			}

			String accountName = client.getLocalPlayer() != null ? client.getLocalPlayer().getName() : "Unknown";
			currentSession = new Session(client.getAccountHash(), accountName);
			initializeKillCounts(true);

			syncActiveSessionScores();
			storageManager.saveSession(currentSession);

			for (AbstractListenerNode listener : getCoreListeners())
			{
				listener.onSessionStarted(currentSession);
			}
			for (AbstractListenerLeaf listener : getConditionalListeners())
			{
				listener.onSessionStarted(currentSession);
			}

			log.debug("Session started.");
			panel.reloadComboBox();
		});
	}

	/**
	 * Closes an ongoing session and removes it from all registered listening classes.
	 */
	public void closeSession()
	{
		clientThread.invokeLater(() ->
		{
			if (currentSession != null)
			{
				currentSession.setInProgress(false);
				currentSession.setEndTime(Instant.now());
				storageManager.saveSession(currentSession);

				Session closedSession = currentSession;
				currentSession = null;

				// --- NEW: Notify all listeners directly ---
				for (AbstractListenerNode listener : getCoreListeners())
				{
					listener.onSessionStopped(closedSession);
				}
				for (AbstractListenerLeaf listener : getConditionalListeners())
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

	/**
	 * Hides an activity by delegating to ConfigListener and refreshing the UI.
	 */
	public void disableActivity(TrackedActivity activity) {
		configListener.disableActivity(activity);
		if (panel != null) {
			panel.refreshKcContainer();
		}
	}

	/**
	 * Safely toggles a conditional listener on or off, ensuring it receives the
	 * active session state if a session is currently in progress.
	 */
	private void toggleConditionalListener(AbstractListenerLeaf listener, boolean isEnabled)
	{
		if (isEnabled)
		{
			listener.register();
			if (currentSession != null && currentSession.isInProgress())
			{
				listener.onSessionStarted(currentSession);
			}
		}
		else
		{
			if (currentSession != null)
			{
				listener.onSessionStopped(currentSession);
			}
			listener.unregister();
		}
	}

	/**
	 * Returns a processed list of counts to display in the UI, applying merges if configured.
	 */
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

		// Update active session instantly when a new score is set in the context menu
		if (eventKey.equals("activityScores")) {
			syncActiveSessionScores();
			setRequiresSaveAndRefresh(true);
		}

		switch (eventKey)
		{
			case "enableBoltProcListener":
				toggleConditionalListener(boltProcListener, config.enableBoltProcListener());
				break;
			case "enableMagicSpellListener":
				toggleConditionalListener(magicSpellListener, config.enableMagicSpellListener());
				break;
			case "enableFarmingListener":
				toggleConditionalListener(farmingListener, config.enableFarmingListener());
				break;
		}
		clientThread.invokeLater(() -> {
			if (panel != null)
			{
				panel.refreshKcContainer();
			}
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
				if (activeSession != null)
				{
					currentSession = activeSession;
					initializeKillCounts(false);
					syncActiveSessionScores();

					for (AbstractListenerNode listener : getCoreListeners())
					{
						listener.onSessionStarted(currentSession);
					}
					for (AbstractListenerLeaf listener : getConditionalListeners())
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

	/**
	 * Initializes all counts appropriately.
	 */
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

				// KCs that are typically incremented by 1 whenever they occur
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

		// Initialize stat baselines
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

			if (currentSession.isTracking(PluginConstants.ActivityID.LVL_TOTAL))
			{
				Count kc = currentSession.getKillCount(PluginConstants.ActivityID.LVL_TOTAL);
				if (kc.getInitialKc() == -1)
				{
					kc.setInitialKc(client.getTotalLevel());
				}
			}
		}
	}

	/**
	 * Helper method to map a raw tracking ID back to its Enum instance.
	 */
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

	/**
	 * Increases the count of the specified counter by one and set a flag for an update/refresh
	 */
	public void increaseCountByOne(int activityId)
	{
		Session session = getCurrentSession();
		if (session != null && session.isTracking(activityId)) {
			Count kc = session.getKillCount(activityId);
			kc.setSessionKc(kc.getSessionKc() + 1);
			setRequiresSaveAndRefresh(true);
		}
	}

	public void setActivityScore(TrackedActivity activity, float score) {
		configListener.setActivityScore(activity, score);
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
			pinnedActivityIds.add(activity.getId());
		}
		// If saving to config, call your save method once here
	}

	public void unpinAllActivities(Iterable<TrackedActivity> activities)
	{
		for (TrackedActivity activity : activities)
		{
			pinnedActivityIds.remove(activity.getId());
		}
		// If saving to config, call your save method once here
	}

	/**
	 * Completely resets a specific activity count and re-samples its baseline
	 * from the game state to ensure it starts counting from 0 cleanly.
	 */
	public void resetActivityCount(Session targetSession, TrackedActivity activity)
	{
		if (targetSession == null || activity == null) return;

		// If it is an archived session, simply zero the count and save
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

	/**
	 * Completely resets ALL activity counts in the targeted session.
	 */
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
					if (activity.getId() == PluginConstants.ActivityID.LVL_TOTAL)
					{
						kc.setInitialKc(client.getTotalLevel());
					}
					else
					{
						// Reset underlying skill baseline in the session's internal memory
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
}