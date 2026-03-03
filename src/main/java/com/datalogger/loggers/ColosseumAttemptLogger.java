package com.datalogger.loggers;

import static com.datalogger.constants.Colosseum.Intermission.INTERMISSION_GROUP_ID;
import static com.datalogger.constants.Colosseum.Intermission.INTERMISSION_RESULT_CONTAINER;
import static com.datalogger.constants.Colosseum.Message.BOSS_WAVE_START_PREFIX;
import static com.datalogger.constants.Colosseum.Message.DEATH_MESSAGE;
import static com.datalogger.constants.Colosseum.Message.END_ATTEMPT_MESSAGE;
import static com.datalogger.constants.Colosseum.Message.START_ATTEMPT_MESSAGE;
import static com.datalogger.constants.Colosseum.Message.WAVE_START_PREFIX;
import static com.datalogger.constants.Colosseum.RewardsChest.REWARDS_CHEST_GROUP_ID;
import static com.datalogger.constants.Colosseum.RewardsChest.REWARDS_CHEST_REWARDS_TAB_CHILD_ID;
import static com.datalogger.constants.Colosseum.Region.COLOSSEUM_REGION_ID;
import static com.datalogger.constants.Colosseum.Varbit.COLOSSEUM_SELECTED_MODIFIER_VARBIT;
import com.datalogger.DataLoggerConfig;
import com.datalogger.framework.AbstractLogger;
import com.datalogger.framework.LogType;
import com.datalogger.models.colosseum.ColosseumAttempt;
import com.datalogger.models.colosseum.ColosseumState;
import com.datalogger.models.colosseum.ColosseumWave;
import com.datalogger.models.colosseum.IntermissionUI;
import com.datalogger.models.colosseum.enums.ColosseumModifier;
import com.datalogger.models.colosseum.enums.WaveStatus;
import com.datalogger.models.common.ItemBundle;
import com.datalogger.services.ColosseumScanner;
import com.datalogger.services.FileIOService;
import com.google.common.primitives.Ints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.WorldView;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.NotImplementedException;

@Slf4j
@Singleton
public class ColosseumAttemptLogger extends AbstractLogger
{
	private IntermissionUI parsedTransitionUI;

	private boolean activeTrial;
	private boolean activeWave;
	private boolean inColosseum;

	private int currentWave;
	private int selectedModifierIdx;
	private ColosseumModifier selectedModifier;
	private int lastWaveTickCount;
	private int waveStartTick;
	private int waveStartIdx;
	private boolean screenshotUI;

	private final List<ColosseumState> states = new ArrayList<>();
	private ColosseumAttempt currentAttempt;
	private WaveStatus finalStatus;

	private final ColosseumScanner scanner;
	private final Client client;
	private final FileIOService fileIOService;
	private final DataLoggerConfig config;
	private final DrawManager drawManager;
	private final ScheduledExecutorService executor;

	private boolean waitingForIntermission;
	private boolean widgetIsOpen;
	private boolean fullSkip;

	@Inject
	public ColosseumAttemptLogger(ColosseumScanner scanner, Client client, FileIOService fileIOService, DataLoggerConfig config, DrawManager drawManager, ScheduledExecutorService executor) {
		this.scanner = scanner;
		this.client = client;
		this.fileIOService = fileIOService;
		this.config = config;
		this.drawManager = drawManager;
		this.executor = executor;
		updateConfigFlags();
		startAttempt();
	}

	private void updateConfigFlags()
	{
		fullSkip = !config.screenshotBetweenWaves() && !config.logColosseum();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals(DataLoggerConfig.CONFIG_GROUP)) {
			return;
		}
		if (event.getKey().equals("logColosseum")) {
			if (!config.logColosseum() && (activeWave || activeTrial)) {
				log.info("Colosseum logging was disabled mid-run. Cleaning up state.");
				startAttempt();
			}
		}
		updateConfigFlags();
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!inColosseum || !config.logColosseum()) return;

		if (event.getType() != ChatMessageType.NPC_SAY && event.getType() != ChatMessageType.GAMEMESSAGE  && event.getType() != ChatMessageType.CONSOLE)
		{
			return;
		}

		String message = Text.removeTags(event.getMessage());

		if (message.startsWith(DEATH_MESSAGE)) {
			failWave();
			return;
		}

		if (message.startsWith(START_ATTEMPT_MESSAGE))
		{
			startAttempt();
			return;
		}

		if (message.startsWith(WAVE_START_PREFIX))
		{
			currentWave = Integer.parseInt(message.replace("Wave: ", "").trim());
			buildWave();
		}

		if (message.startsWith(BOSS_WAVE_START_PREFIX))
		{
			currentWave = 12;
			buildWave();
		}

		if (message.startsWith("Wave ") && message.contains("completed! Wave duration:"))
		{
			waitingForIntermission = true;
			endWave();
			return;
		}

		if (message.startsWith(END_ATTEMPT_MESSAGE))
		{
			finalStatus = currentWave == 12 ? WaveStatus.COMPLETED : WaveStatus.CLAIMED;
			waitingForIntermission = true;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event) {
		if (!inColosseum || !config.logColosseum()) return;
		if (event.getVarbitId() == COLOSSEUM_SELECTED_MODIFIER_VARBIT) {
			if (event.getValue() > 0) {
				selectedModifierIdx = event.getValue()-1;
				selectedModifier = parsedTransitionUI.getModifierChoices().get(selectedModifierIdx);
				parsedTransitionUI.setSelectedModifier(selectedModifier);
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (!config.logColosseum() || !inColosseum) return;

		if (widgetIsOpen)
		{
			if (widgetDataIsLoaded())
			{
				widgetIsOpen = false;
				parseUI();
			}
		}

		if (activeWave)
		{
			ColosseumState state = scanner.scanCurrentState(currentWave, waveStartTick);
			states.add(state);
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event) {
		if (fullSkip || !inColosseum) return;

		if (isColosseumUI(event)) {
			takeColosseumScreenshot();
			widgetIsOpen = true;
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		if (!config.logColosseum()) return;

		GameState state = event.getGameState();

		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING) {
			if (inColosseum && currentAttempt != null) {
				log.info("Player logged out or disconnected in the Colosseum. Run failed.");
				failWave();
				inColosseum = false;
			}
			return;
		}

		if (state == GameState.LOGGED_IN) {
			boolean wasInColosseum = inColosseum;
			updateInColosseum();

			if (wasInColosseum && !inColosseum) {
				if (currentAttempt != null) {
					if (currentWave < 2)
					{
						startAttempt();
						return;
					}
					log.info("Player left the Colosseum (Death/Teleport/Walked out). Run failed.");
					failWave();
				}
			}
		}
	}

	private void startAttempt() {
		currentWave = 1;
		screenshotUI = true;
		activeWave = false;
		currentAttempt = new ColosseumAttempt();
		finalStatus = null;
		activeTrial = true;
		waitingForIntermission = true;
		parsedTransitionUI = null;

		log.info("Starting a new Colosseum attempt.");
	}

	private void endAttempt()
	{
		currentAttempt.setFinalStatus(finalStatus);
		fileIOService.mergeTimelineFiles(currentAttempt.getStartTime());
		fileIOService.logColosseumAttempt(currentAttempt);

		if (config.logColosseumCSV())
			fileIOService.writeColosseumCSVLog(getAccountName(), currentAttempt.getStartTime(), writeCsvLog());
	}

	private void parseUI() {
		if (!waitingForIntermission) return;

		IntermissionUI newUI = scanner.scanUI(finalStatus != null);
		selectedModifierIdx = -1;

		if (parsedTransitionUI != null && (finalStatus == null || finalStatus == WaveStatus.COMPLETED || finalStatus == WaveStatus.CLAIMED))
		{
			log.debug("Completing ColosseumWave for wave {}", currentWave);
			ColosseumWave completedWave = completeWave(newUI);
			log.debug(completedWave.toString());
			currentAttempt.submitWave(completedWave);
			selectedModifier = null;
		}
		parsedTransitionUI = newUI;

		if (finalStatus != null)
		{
			log.debug("Final status is {} ending attempt...", finalStatus.name());
			endAttempt();
		}
		waitingForIntermission = false;
	}

	private void failWave() {
		if (currentAttempt == null || finalStatus != null) return;

		activeWave = false;
		finalStatus = WaveStatus.FAILED;

		endWave();

		List<ItemBundle> loot = parsedTransitionUI != null && parsedTransitionUI.getPotentialLoot() != null ? parsedTransitionUI.getPotentialLoot() : new ArrayList<>();

		ColosseumWave failedWave = ColosseumWave.builder()
			.wave(currentWave)
			.status(WaveStatus.FAILED)
			.earnedLoot(loot)
			.modifierChoices(parsedTransitionUI != null ? parsedTransitionUI.getModifierChoices() : new ArrayList<>())
			.chosenModifier(selectedModifier) // Fix: Safely uses the cached variable rather than risking an out-of-bounds index
			.timeTaken(0)
			.speedBonus(0)
			.damageTaken(0)
			.damageBonus(0)
			.modifierGlory(0)
			.completionBonus(0)
			.totalGlory(parsedTransitionUI != null ? parsedTransitionUI.getTotalGlory() : 0)
			.startIdx(-1)
			.endIdx(-1)
			.build();

		log.debug("Failed Colosseum attempt during wave {}", currentWave);
		log.debug(failedWave.toString());

		currentAttempt.submitWave(failedWave);
		endAttempt();
	}

	private void buildWave() {
		if (activeWave) return;

		waveStartIdx = states.size();
		waveStartTick = client.getTickCount();
		screenshotUI = true;

		activeWave = true;
		log.debug("Starting Wave {}. Timeline Start Index: {}", currentWave, waveStartIdx);
	}

	public void endWave() {
		activeWave = false;

		List<ColosseumState> snapshot = new ArrayList<>(this.states);
		int ticksCaptured = snapshot.size();

		fileIOService.saveWaveStates(currentAttempt.getStartTime(), currentWave, snapshot);

		this.lastWaveTickCount = ticksCaptured;
		states.clear();

		log.debug("Wave {} ended. Captured {} ticks. Waiting for UI...", currentWave, ticksCaptured);
	}

	/**
	 * Take a screenshot if the appropriate conditions are met
	 */
	private void takeColosseumScreenshot() {
		if (!config.screenshotBetweenWaves()) return;
		if (!screenshotUI) return;

		drawManager.requestNextFrameListener(image -> {
			executor.submit(() -> {
				fileIOService.logColosseumScreenshot((BufferedImage) image, currentAttempt.getStartTime(), currentWave);
			});
		});
		screenshotUI = false;
	}

	/**
	 * Return true if event is an intermission/rewards chest widget, or false if not
	 */
	private boolean isColosseumUI(WidgetLoaded event) {
		int groupId = event.getGroupId();
		return groupId == INTERMISSION_GROUP_ID || groupId == REWARDS_CHEST_GROUP_ID;
	}

	/**
	 * Update the inColosseum flag, which is derived from the region the player is located in.
	 */
	private void updateInColosseum()
	{
		WorldView wv = client.getTopLevelWorldView();
		int[] regions = (wv != null) ? wv.getMapRegions() : null;
		inColosseum = regions != null && Ints.contains(regions, COLOSSEUM_REGION_ID);
	}

	private boolean widgetDataIsLoaded()
	{
		Widget w = finalStatus == null ? client.getWidget(INTERMISSION_GROUP_ID, INTERMISSION_RESULT_CONTAINER) : client.getWidget(REWARDS_CHEST_GROUP_ID, REWARDS_CHEST_REWARDS_TAB_CHILD_ID);
		return w != null && w.getDynamicChildren().length > 0;
	}

	private ColosseumWave completeWave(IntermissionUI curUI)
	{
		if (finalStatus == null)
		{
			return generateCompletedWave(curUI);
		}
		switch (finalStatus)
		{
			case COMPLETED:
				return generateCompletedWave(curUI);
			case FAILED:
				return generateFailedWave();
			case CLAIMED:
				return generateCancelledWave(curUI);
		}
		throw new NotImplementedException("Unexpected finalStatus: " + finalStatus);
	}

	private ColosseumWave generateCompletedWave(IntermissionUI curUI)
	{
		List<ItemBundle> loot = parsedTransitionUI != null && parsedTransitionUI.getPotentialLoot() != null
			? parsedTransitionUI.getPotentialLoot()
			: new ArrayList<>();

		return ColosseumWave.builder()
			.wave(currentWave)
			.status(WaveStatus.COMPLETED)
			.earnedLoot(loot)
			.modifierChoices(parsedTransitionUI.getModifierChoices())
			.chosenModifier(parsedTransitionUI.getSelectedModifier())
			.timeTaken(curUI.getTotalTimeSeconds())
			.speedBonus(curUI.getSpeedBonusGlory())
			.damageTaken(curUI.getDamageTakenAmount())
			.damageBonus(curUI.getDamageTakenGlory())
			.modifierGlory(curUI.getModChoiceGlory())
			.completionBonus(curUI.getWaveBonusGlory())
			.totalGlory(curUI.getTotalGlory())
			.startIdx(waveStartIdx)
			.endIdx(waveStartIdx + lastWaveTickCount - 1)
			.build();
	}

	private ColosseumWave generateFailedWave()
	{
		List<ItemBundle> loot = parsedTransitionUI != null && parsedTransitionUI.getPotentialLoot() != null
			? parsedTransitionUI.getPotentialLoot()
			: new ArrayList<>();

		return ColosseumWave.builder()
			.wave(currentWave)
			.status(WaveStatus.FAILED)
			.earnedLoot(loot)
			.modifierChoices(parsedTransitionUI != null ? parsedTransitionUI.getModifierChoices() : new ArrayList<>())
			.chosenModifier(parsedTransitionUI != null ? parsedTransitionUI.getSelectedModifier() : null)
			.timeTaken(0)
			.speedBonus(0)
			.damageTaken(0)
			.damageBonus(0)
			.modifierGlory(0)
			.completionBonus(0)
			.totalGlory(parsedTransitionUI != null ? parsedTransitionUI.getTotalGlory() : 0)
			.startIdx(-1)
			.endIdx(-1)
			.build();
	}

	private ColosseumWave generateCancelledWave(IntermissionUI curUI)
	{
		List<ItemBundle> loot = parsedTransitionUI != null && parsedTransitionUI.getPotentialLoot() != null
			? parsedTransitionUI.getPotentialLoot()
			: new ArrayList<>();

		return ColosseumWave.builder()
			.wave(currentWave+1)
			.status(WaveStatus.CANCELLED)
			.earnedLoot(loot)
			.modifierChoices(parsedTransitionUI != null ? parsedTransitionUI.getModifierChoices() : new ArrayList<>())
			.chosenModifier(null)
			.timeTaken(0)
			.speedBonus(0)
			.damageTaken(0)
			.damageBonus(0)
			.modifierGlory(0)
			.completionBonus(0)
			.totalGlory(curUI.getTotalGlory())
			.startIdx(-1)
			.endIdx(-1)
			.build();
	}

	@Override
	public LogType getLogType()
	{
		return LogType.COLOSSEUM;
	}

	@Override
	public String getCsvHeader()
	{
		return ColosseumWave.csvHeader();
	}

	@Override
	public boolean isEnabled()
	{
		return config.logColosseum();
	}

	public String writeCsvLog() {
		if (currentAttempt == null || currentAttempt.getWaves() == null || currentAttempt.getWaves().isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (ColosseumWave wave : currentAttempt.getWaves()) {
			sb.append(wave.toCsvRow()).append("\n");
		}

		return sb.toString();
	}
}