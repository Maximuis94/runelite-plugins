package com.datalogger.loggers;

import com.datalogger.DataLoggerConfig;
import static com.datalogger.constants.Colosseum.Intermission.INTERMISSION_GROUP_ID;
import static com.datalogger.constants.Colosseum.Intermission.INTERMISSION_RESULT_CONTAINER;
import static com.datalogger.constants.Colosseum.Message.BOSS_WAVE_START_PREFIX;
import static com.datalogger.constants.Colosseum.Message.DEATH_MESSAGE;
import static com.datalogger.constants.Colosseum.Message.END_ATTEMPT_MESSAGE;
import static com.datalogger.constants.Colosseum.Message.START_ATTEMPT_MESSAGE;
import static com.datalogger.constants.Colosseum.Message.WAVE_START_PREFIX;
import static com.datalogger.constants.Colosseum.Region.COLOSSEUM_REGION_ID;
import static com.datalogger.constants.Colosseum.RewardsChest.REWARDS_CHEST_GROUP_ID;
import static com.datalogger.constants.Colosseum.RewardsChest.REWARDS_CHEST_REWARDS_TAB_CHILD_ID;
import static com.datalogger.constants.Colosseum.Varbit.COLOSSEUM_SELECTED_MODIFIER_VARBIT;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
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
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
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

	private boolean enabledLogging;
	private boolean enabledCsvLogging;
	private boolean enabledTimelineLogging;

	private int currentWave;
	private int selectedModifierIdx;
	private ColosseumModifier selectedModifier;
	private int lastWaveTickCount;

	private int attemptStartTick;
	private int attemptEndTick;
	private int waveStartTick;
	private int waveEndTick;

	private final List<ColosseumState> states = new ArrayList<>();
	private ColosseumAttempt currentAttempt;
	private WaveStatus finalStatus;

	private final ColosseumScanner scanner;
	private final Client client;
	private final FileIOService fileIOService;
	private final DataLoggerConfig config;
	private final ConfigManager configManager;

	private boolean waitingForIntermission;
	private boolean widgetIsOpen;

	@Inject
	public ColosseumAttemptLogger(ColosseumScanner scanner, Client client, FileIOService fileIOService, DataLoggerConfig config, ConfigManager configManager) {
		this.scanner = scanner;
		this.client = client;
		this.fileIOService = fileIOService;
		this.config = config;
		this.configManager = configManager;

		updateConfigFlags();
		startAttempt();
	}

	/**
	 * Update booleans derived from (combinations of) plugin configurations
	 */
	private void updateConfigFlags()
	{
		enabledLogging = config.logColosseum();
		enabledCsvLogging = enabledLogging && config.logColosseumCSV();
		enabledTimelineLogging = enabledLogging && config.logWaveTimeline();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals(DataLoggerConfig.CONFIG_GROUP)) return;

		updateConfigFlags();
		String key = event.getKey();
		if (key.equals("logColosseum")) {
			if ((!enabledLogging && currentAttempt != null) && (activeWave || activeTrial)) {
				log.info("Colosseum logging was disabled mid-run. Cleaning up state.");
				finalStatus = WaveStatus.CONFIG_DISABLED;
				endWave();
				endAttempt();
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!enabledLogging || !inColosseum) return;

		if (event.getType() != ChatMessageType.NPC_SAY && event.getType() != ChatMessageType.GAMEMESSAGE  && event.getType() != ChatMessageType.CONSOLE)
		{
			return;
		}

		String message = Text.removeTags(event.getMessage());

		if (message.startsWith(DEATH_MESSAGE)) {
			waveEndTick = client.getTickCount();
			failWave();
			return;
		}

		if (message.startsWith(START_ATTEMPT_MESSAGE))
		{
			startAttempt();
			return;
		}

		if (currentWave < 11 && message.startsWith(WAVE_START_PREFIX))
		{
			waveStartTick = client.getTickCount();
			currentWave = Integer.parseInt(message.replace("Wave: ", "").trim());
			buildWave();
			return;
		}

		if (currentWave == 11 && message.startsWith(BOSS_WAVE_START_PREFIX))
		{
			waveStartTick = client.getTickCount();
			currentWave = 12;
			buildWave();
			return;
		}

		if (message.startsWith("Wave ") && message.contains("completed! Wave duration:"))
		{
			waveEndTick = client.getTickCount();
			waitingForIntermission = true;
			if (enabledTimelineLogging)
				endWave();
			return;
		}

		if (message.startsWith(END_ATTEMPT_MESSAGE))
		{
			attemptEndTick = client.getTickCount();
			finalStatus = currentWave == 12 ? WaveStatus.COMPLETED : WaveStatus.CLAIMED;
			waitingForIntermission = true;
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event) {
		if (!enabledLogging || !inColosseum) return;
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
		if (!enabledLogging || !inColosseum) return;

		if (widgetIsOpen)
		{
			if (widgetDataIsLoaded())
			{
				widgetIsOpen = false;
				parseUI();
			}
		}

		if (enabledTimelineLogging && activeWave)
		{
			ColosseumState state = scanner.scanCurrentState(currentWave, waveStartTick);
			states.add(state);
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event) {
		if (!inColosseum) return;

		if (isColosseumUI(event)) {
			widgetIsOpen = true;
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		if (!enabledLogging) return;

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
						return;
					}
					log.info("Player left the Colosseum (Death/Teleport/Walked out). Run failed.");
					failWave();
				}
			}
		}
	}

	private void startAttempt() {
		attemptStartTick = client.getTickCount();
		currentWave = 1;
		activeWave = false;
		currentAttempt = new ColosseumAttempt(attemptStartTick);
		finalStatus = null;
		activeTrial = true;
		waitingForIntermission = true;
		parsedTransitionUI = null;

		log.info("Starting a new Colosseum attempt.");
	}

	/**
	 * Wrap up the ongoing attempt by merging the timeline of states, logging the current attempt and writing the CSV log,
	 * depending on user-defined configurations.
	 */
	private void endAttempt()
	{
		currentAttempt.setFinalStatus(finalStatus);
		fileIOService.mergeTimelineFiles(currentAttempt.getStartTime());
		fileIOService.logColosseumAttempt(currentAttempt);

		if (enabledCsvLogging)
			fileIOService.writeColosseumCSVLog(getAccountName(), currentAttempt.getStartTime(), writeCsvLog());
		currentAttempt = null;
	}

	private void parseUI() {
		if (!waitingForIntermission) return;

		IntermissionUI newUI = scanner.scanUI(finalStatus != null);

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

		double timeTaken = getWaveTimeTaken();
		ColosseumWave failedWave = ColosseumWave.builder()
			.wave(currentWave)
			.status(WaveStatus.FAILED)
			.earnedLoot(loot)
			.modifierChoices(parsedTransitionUI != null ? parsedTransitionUI.getModifierChoices() : new ArrayList<>())
			.chosenModifier(selectedModifier)
			.startTick(waveStartTick)
			.endTick(waveEndTick)
			.timeTaken(timeTaken)
			.speedBonus(0)
			.damageTaken(0)
			.damageBonus(0)
			.modifierGlory(0)
			.completionBonus(0)
			.waveGlory(0)
			.totalGlory(parsedTransitionUI != null ? parsedTransitionUI.getTotalGlory() : 0)
			.build();

		log.debug("Failed Colosseum attempt during wave {}", currentWave);
		log.debug(failedWave.toString());

		currentAttempt.submitWave(failedWave);
		endAttempt();
	}

	private void buildWave() {
		if (activeWave) return;

		waveStartTick = client.getTickCount();

		activeWave = true;
		log.debug("Starting Wave {}", currentWave);
	}

	/**
	 * End the wave by submitting the registered WaveState instances to a temporary file.
	 */
	public void endWave() {
		activeWave = false;

		List<ColosseumState> snapshot = new ArrayList<>(this.states);
		int ticksCaptured = snapshot.size();

		if (ticksCaptured == 0) return;

		fileIOService.saveWaveStates(currentAttempt.getStartTime(), currentWave, snapshot);

		this.lastWaveTickCount = ticksCaptured;
		int nTicks = waveEndTick - waveStartTick;
		states.clear();

		if (nTicks == ticksCaptured)
			log.debug("Wave {} ended. Captured {}/{} ticks. Waiting for UI...", currentWave, ticksCaptured, nTicks);
		else
		{
			int nMissed = nTicks - ticksCaptured;
			log.warn("Wave {} ended. Captured {}/{} ticks (missed {} ticks)", currentWave, ticksCaptured, nTicks, nMissed);
		}

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

		double timeTaken = getWaveTimeTaken();
		return ColosseumWave.builder()
			.wave(currentWave)
			.status(WaveStatus.COMPLETED)
			.earnedLoot(loot)
			.modifierChoices(parsedTransitionUI.getModifierChoices())
			.chosenModifier(parsedTransitionUI.getSelectedModifier())
			.startTick(waveStartTick)
			.endTick(waveEndTick)
			.timeTaken(timeTaken)
			.speedBonus(curUI.getSpeedBonusGlory())
			.damageTaken(curUI.getDamageTakenAmount())
			.damageBonus(curUI.getDamageTakenGlory())
			.modifierGlory(curUI.getModChoiceGlory())
			.completionBonus(curUI.getWaveBonusGlory())
			.waveGlory(curUI.getWaveGlory())
			.totalGlory(curUI.getTotalGlory())
			.build();
	}

	private ColosseumWave generateFailedWave()
	{
		List<ItemBundle> loot = parsedTransitionUI != null && parsedTransitionUI.getPotentialLoot() != null
			? parsedTransitionUI.getPotentialLoot()
			: new ArrayList<>();

		double timeTaken = getWaveTimeTaken();
		return ColosseumWave.builder()
			.wave(currentWave)
			.status(WaveStatus.FAILED)
			.earnedLoot(loot)
			.modifierChoices(parsedTransitionUI != null ? parsedTransitionUI.getModifierChoices() : new ArrayList<>())
			.chosenModifier(parsedTransitionUI != null ? parsedTransitionUI.getSelectedModifier() : null)
			.startTick(waveStartTick)
			.endTick(waveEndTick)
			.timeTaken(timeTaken)
			.damageTaken(0)
			.speedBonus(0)
			.damageBonus(0)
			.modifierGlory(0)
			.completionBonus(0)
			.waveGlory(0)
			.totalGlory(parsedTransitionUI != null ? parsedTransitionUI.getWaveGlory() : 0)
			.build();
	}

	private ColosseumWave generateCancelledWave(IntermissionUI curUI)
	{
		List<ItemBundle> loot = parsedTransitionUI != null && parsedTransitionUI.getPotentialLoot() != null
			? parsedTransitionUI.getPotentialLoot()
			: new ArrayList<>();
		int endTick = client.getTickCount();
		return ColosseumWave.builder()
			.wave(currentWave+1)
			.status(WaveStatus.CANCELLED)
			.earnedLoot(loot)
			.modifierChoices(parsedTransitionUI != null ? parsedTransitionUI.getModifierChoices() : new ArrayList<>())
			.chosenModifier(null)
			.startTick(endTick)
			.endTick(endTick)
			.timeTaken(0)
			.speedBonus(0)
			.damageTaken(0)
			.damageBonus(0)
			.modifierGlory(0)
			.completionBonus(0)
			.totalGlory(curUI.getWaveGlory())
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

	/**
	 * Compute the time taken based on wave start and end ticks, return it with up to one decimal in seconds.
	 */
	private double getWaveTimeTaken()
	{
		return BigDecimal.valueOf(.6 * (waveEndTick - waveStartTick)).setScale(1, RoundingMode.HALF_UP).doubleValue();
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