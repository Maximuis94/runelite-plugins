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
import com.activitycounter.listeners.BoltProcListener;
import com.activitycounter.listeners.ChatMessageListener;
import com.activitycounter.listeners.ConfigListener;
import com.activitycounter.listeners.ExperienceDropListener;
import com.activitycounter.listeners.FarmingListener;
import com.activitycounter.listeners.SoundEffectListener;
import com.activitycounter.listeners.StatChangeListener;
import com.activitycounter.listeners.VarbitListener;
import com.activitycounter.models.Category;
import com.activitycounter.models.Count;
import com.activitycounter.models.Session;
import com.activitycounter.models.TrackedActivity;
import com.activitycounter.models.TrackerType;
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
import net.runelite.client.util.Filepath;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = PLUGIN_NAME,
	internalName = PluginConstants.PLUGIN_DIR_NAME,
	description = "Highly customizable plugin that counts bosses/laps/tablets/bolt procs/sneaky suspicions and much more per manually defined session.",
	tags = {"session","tracker","counter","kc","killcount","activity","lap","minigame","supplies","stats","skilling","sneaky","suspicion"}
)
public class ActivityCounterPlugin extends Plugin
{
	@Getter
	private int loadedRegionId;

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
	private boolean hasRegisteredBoltProcListener = false;

	@Inject
	private FarmingListener farmingListener;

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
	private SessionStorageManager storageManager;

	@Inject
	private ConfigListener configListener;

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
		eventBus.register(configListener);
		configListener.buildAllCaches();

		eventBus.register(chatMessageListener);
		eventBus.register(statChangeListener);
		eventBus.register(varbitListener);
		eventBus.register(soundEffectListener);
		soundEffectListener.initializeRoutingCaches();

		eventBus.register(experienceDropListener);

		registerFarmingListener();

		if (config.enableBoltProcListener())
		{
			registerBoltProcListener();
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
	}

	@Override
	protected void shutDown()
	{
		eventBus.unregister(configListener);
		eventBus.unregister(chatMessageListener);
		eventBus.unregister(statChangeListener);
		eventBus.unregister(varbitListener);
		eventBus.unregister(soundEffectListener);
		eventBus.unregister(experienceDropListener);

		unregisterFarmingListener();
		unregisterBoltProcListener();

		clientToolbar.removeNavigation(navButton);
		if (currentSession != null)
		{
			storageManager.saveSession(currentSession);
		}
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
	 * Starts a new session
	 */
	public void startSession()
	{
		clientThread.invokeLater(() -> {
			if (client == null || client.getGameState() != GameState.LOGGED_IN)
			{
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
		clientThread.invokeLater(() -> {
			if (currentSession != null) {
				currentSession.setInProgress(false);
				currentSession.setEndTime(Instant.now());
				storageManager.saveSession(currentSession);
				log.debug("Session closed and archived.");
				currentSession = null;
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

		if (panel != null) {
			panel.refreshKcContainer();
		}

		if (eventKey.equals("enableBoltProcListener"))
		{
			if (config.enableBoltProcListener()) registerBoltProcListener();
			else unregisterBoltProcListener();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
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

				if (isManualStart && client != null && client.getGameState() == GameState.LOGGED_IN) {
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
	 * Registers the boltProcListener and updates the corresponding flag.
	 */
	public void registerBoltProcListener()
	{
		if (!hasRegisteredBoltProcListener)
		{
			eventBus.register(boltProcListener);
			hasRegisteredBoltProcListener = true;

			clientThread.invokeLater(() -> boltProcListener.initializeState());
		}
		else {
			log.warn("Unable to register Bolt Proc Listener; it has already been registered");
		}
	}

	/**
	 * Unregisters the boltProcListener and updates the corresponding flag.
	 */
	public void unregisterBoltProcListener()
	{
		if (hasRegisteredBoltProcListener)
		{
			eventBus.unregister(boltProcListener);
			hasRegisteredBoltProcListener = false;
		}
		else {
			log.warn("Unable to unregister Bolt Proc Listener; it has not been registered");
		}
	}

	/**
	 * Registers the farmingListener and updates the corresponding flag.
	 */
	public void registerFarmingListener()
	{
		if (!farmingListener.isRegistered())
		{
			eventBus.register(farmingListener);
			farmingListener.setRegistered(true);

			clientThread.invokeLater(() -> boltProcListener.initializeState());
		}
		else {
			log.warn("Unable to register FarmingListener; it has already been registered");
		}
	}

	/**
	 * Unregisters the farmingListener and updates the corresponding flag.
	 */
	public void unregisterFarmingListener()
	{
		if (farmingListener.isRegistered())
		{
			eventBus.unregister(farmingListener);
			farmingListener.setRegistered(false);
		}
		else {
			log.warn("Unable to unregister FarmingListener; it has not been registered");
		}
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
}