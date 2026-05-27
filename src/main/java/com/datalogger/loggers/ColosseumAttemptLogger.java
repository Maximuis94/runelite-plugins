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
package com.datalogger.loggers;

import com.datalogger.DataLoggerConfig;
import static com.datalogger.constants.Colosseum.COLOSSEUM_TRIAL_TIMESTAMP_FORMATTER;
import static com.datalogger.constants.Colosseum.Intermission.INTERMISSION_GROUP_ID;
import static com.datalogger.constants.Colosseum.Item.DIZANAS_QUIVER_REWARD;
import static com.datalogger.constants.Colosseum.Item.DIZANAS_QUIVER_SWAPPED_REWARD;
import static com.datalogger.constants.Colosseum.Message.BOSS_MESSAGE_PREFIX;
import static com.datalogger.constants.Colosseum.Message.BOSS_WAVE_START_PREFIX;
import static com.datalogger.constants.Colosseum.Message.DEATH_MESSAGE;
import static com.datalogger.constants.Colosseum.Message.END_ATTEMPT_MESSAGE;
import static com.datalogger.constants.Colosseum.Message.START_ATTEMPT_MESSAGE;
import static com.datalogger.constants.Colosseum.Message.WAVE_1_HEADER;
import static com.datalogger.constants.Colosseum.Message.WAVE_START_PREFIX;
import static com.datalogger.constants.Colosseum.NPC.JAGUAR_WARRIOR_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.JAVELIN_COLOSSUS_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.MANTICORE_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.MINOTAUR_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.MINOTAUR_RED_FLAG_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.SERPENT_SHAMAN_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.SHOCKWAVE_COLOSSUS_NPC_ID;
import static com.datalogger.constants.Colosseum.Region.COLOSSEUM_REGION_ID;
import static com.datalogger.constants.Colosseum.Script.POPULATE_INTERMISSION_UI_SCRIPT_ID;
import static com.datalogger.constants.Colosseum.Script.POPULATE_REWARDS_CHEST_UI_SCRIPT_ID;
import static com.datalogger.constants.Colosseum.Varbit.COLOSSEUM_SELECTED_MODIFIER_VARBIT;
import com.datalogger.dto.ColosseumAttemptDTO;
import com.datalogger.events.ColosseumAttemptEnded;
import com.datalogger.events.ColosseumAttemptStarted;
import com.datalogger.events.ColosseumWaveEnded;
import com.datalogger.events.ColosseumWaveStarted;
import com.datalogger.events.DataLoggerConfigChanged;
import com.datalogger.events.PlayerDied;
import com.datalogger.framework.AbstractLogger;
import com.datalogger.framework.LogType;
import com.datalogger.models.colosseum.ColosseumAttempt;
import com.datalogger.models.colosseum.ColosseumWave;
import com.datalogger.models.colosseum.IntermissionUI;
import com.datalogger.models.enums.ColosseumModifier;
import com.datalogger.models.enums.WaveStatus;
import com.datalogger.models.itemvault.ItemBundle;
import com.datalogger.models.supplytracker.TrackedSupplies;
import com.datalogger.models.supplytracker.ValuedItemStack;
import com.datalogger.services.ColosseumScanner;
import com.datalogger.services.FileIOService;
import com.datalogger.services.SupplyTracker;
import com.datalogger.webhook.ColosseumDiscordBroadcaster;
import com.google.common.primitives.Ints;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemComposition;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class ColosseumAttemptLogger extends AbstractLogger
{
	private IntermissionUI parsedTransitionUI;

	private String entryTag;
	private String entryAccount;
	private String startTime;

	private boolean activeTrial;
	private boolean activeWave;
	private boolean inColosseum;
	private boolean trackSupplies;
	private boolean enabledSwapQuiverLoot;

	private boolean parsedCurrentWaveIntermission;
	private int currentWave;
	private int completedWave;
	//	private int selectedModifierIdx;
	private ColosseumModifier selectedModifier;
	private final List<String> activeModifiers = new ArrayList<>();

	private int bossWavePhase;
	private final int[] bossWavePhaseTickCounts = new int[7];
	private static final List<Integer> BOSS_HP_AT_PHASE = List.of(1500, 1350, 1125, 750, 375, 150, 0);

	private int attemptStartTick;
	private int attemptEndTick;
	private int waveStartTick;
	private int waveEndTick;
	private double totalTimeTaken;

	private ColosseumAttempt currentAttempt;
	private WaveStatus finalStatus;

	private final ColosseumScanner scanner;
	private final Client client;
	private final FileIOService fileIOService;
	private final EventBus eventBus;
	private final SupplyTracker supplyTracker;
	private final ItemManager itemManager;
	private final ColosseumDiscordBroadcaster colosseumDiscordBroadcaster;

	private final DataLoggerConfig config;

	@Setter
	private boolean enabledLogging;
	private boolean enabledCsvLogging;
	private boolean autoMergeWaveFiles;

	private boolean waitingForIntermission;

	private WorldPoint serpentShamanSpawn;
	private WorldPoint javelinColossusSpawnA;
	private WorldPoint javelinColossusSpawnB;
	private WorldPoint manticoreSpawnA;
	private WorldPoint manticoreSpawnB;
	private WorldPoint shockwaveColossusSpawnA;
	private WorldPoint shockwaveColossusSpawnB;

	private WorldPoint jaguarWarriorReinforcementsSpawn;
	private WorldPoint serpentShamanReinforcementsSpawn;
	private WorldPoint minotaurReinforcementsSpawn;

	@Inject
	public ColosseumAttemptLogger(ColosseumScanner scanner, Client client, FileIOService fileIOService, DataLoggerConfig config, EventBus eventBus, SupplyTracker supplyTracker, ItemManager itemManager, ColosseumDiscordBroadcaster colosseumDiscordBroadcaster) {
		this.scanner = scanner;
		this.client = client;
		this.fileIOService = fileIOService;
		this.config = config;
		this.eventBus = eventBus;
		this.supplyTracker = supplyTracker;
		this.itemManager = itemManager;
		this.colosseumDiscordBroadcaster = colosseumDiscordBroadcaster;
		updateConfigFlags();
	}

	/**
	 * Parse relevant plugin configurations
	 */
	private void updateConfigFlags()
	{
		enabledLogging = config.logColosseum();
		enabledCsvLogging = enabledLogging && config.logColosseumCSV();
		autoMergeWaveFiles = config.autoMergeWaveLogs();
		trackSupplies = config.trackSupplies();
		enabledSwapQuiverLoot = config.logQuiverAsSplinters();

	}

	@Subscribe
	public void onDataLoggerConfigChanged(DataLoggerConfigChanged event) {

		updateConfigFlags();
		String key = event.getKey();
		if (key.equals("logColosseum")) {
			if ((!enabledLogging && currentAttempt != null) && (activeWave || activeTrial)) {
				log.debug("Colosseum logging was disabled mid-run. Cleaning up state.");
				finalStatus = WaveStatus.CONFIG_DISABLED;
				failWave();
				endAttempt(WaveStatus.CONFIG_DISABLED);
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

		if (currentWave == 12 && message.startsWith(BOSS_MESSAGE_PREFIX) && client.getBoostedSkillLevel(Skill.HITPOINTS)>0)
		{
			int tickCount = client.getTickCount();
			bossWavePhaseTickCounts[bossWavePhase] = tickCount;
			log.debug("Registered tickCount = {} for Sol heredit phase {}", tickCount, bossWavePhase);
			bossWavePhase++;
			return;
		}


		if (message.startsWith(START_ATTEMPT_MESSAGE))
		{
			startAttempt();
			return;
		}

		if (currentWave < 12 && message.startsWith(WAVE_START_PREFIX))
		{
			waveStartTick = client.getTickCount();
			setCurrentWave(Integer.parseInt(message.split(" ")[1]));
			completedWave = currentWave;
			log.debug("Starting wave {} - setting waveStartTick to {}", currentWave, waveStartTick);
			startWave();
			return;
		}

		if (message.startsWith(BOSS_WAVE_START_PREFIX))
		{
			completedWave = 12;
			waveStartTick = client.getTickCount() - 1;
			log.debug("Starting wave 12 - setting waveStartTick to {}", waveStartTick);
			startWave();
			return;
		}

		if (message.startsWith("Wave ") && message.contains("completed! Wave duration:"))
		{
			waveEndTick = client.getTickCount();
			log.debug("[WAVE {}] Setting WaveEndTick for wave to {}", currentWave, waveEndTick);
			waitingForIntermission = true;

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

		if (event.getVarbitId() == COLOSSEUM_SELECTED_MODIFIER_VARBIT && event.getValue() > 0) {
			if (parsedTransitionUI != null)
			{
				List<ColosseumModifier> modifierChoices = parsedTransitionUI.getModifierChoices();
				if (modifierChoices == null)
				{
					log.debug("[WAVE {}] Failed to resolve selected modifier; modifierChoices is null", currentWave);
				}
				else if (modifierChoices.size() != 3)
				{
					log.debug("[WAVE {}] Unexpected amount of modifierchoices: {}", currentWave, modifierChoices);
				}
				else
				{
					int selectedModifierIdx = event.getValue()-1;
					selectedModifier = modifierChoices.get(selectedModifierIdx);
					activeModifiers.add(selectedModifier.name());
					parsedTransitionUI.setSelectedModifier(selectedModifier);
					log.debug("[WAVE {}] Varbit {} was changed - new value is {}, set selectedModifier to {}", currentWave, COLOSSEUM_SELECTED_MODIFIER_VARBIT, selectedModifierIdx, selectedModifier);
				}
			}
			else
			{
				log.debug("[WAVE {}] Failed to resolve selected modifier; parsedTransitionUI is null", currentWave);
				selectedModifier = null;
			}
		}
	}

	private void onIntermissionScriptFired()
	{
		Widget w = client.getWidget(INTERMISSION_GROUP_ID, 2);
		if (w == null) return;

		Widget c = w.getChild(1);
		String headerText = (c != null) ? Text.removeTags(c.getText()) : null;
		if (headerText == null) return;

		if (headerText.startsWith(WAVE_1_HEADER)) {
			setCurrentWave(1);
		}

		else if (headerText.startsWith("W")) {
			setCurrentWave(Integer.parseInt(headerText.split(" ")[1]) + 1);
		}

		else {
			log.error("Unknown header text detected: {}", headerText);
		}
		waitingForIntermission = true;
		parseUI();
		parsedCurrentWaveIntermission = true;
	}

	/**
	 * Executed if a rewards chest UI has been populated. It is the final step before submitting a completed/claimed
	 * attempt.
	 */
	private void onRewardsChestScriptFired()
	{
		if (currentAttempt == null) return;
		finalStatus = currentWave == 12 ? WaveStatus.COMPLETED : WaveStatus.CLAIMED;
		waitingForIntermission = true;
		parseUI();
		log.debug("[Wave {}] Ending ongoing Colosseum attempt at tick={}, result={}", currentWave, client.getTickCount(), finalStatus.name());
		endAttempt(currentWave == 12 ? WaveStatus.COMPLETED : WaveStatus.CLAIMED);
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		int scriptId = event.getScriptId();

		if (!parsedCurrentWaveIntermission && scriptId == POPULATE_INTERMISSION_UI_SCRIPT_ID)
			onIntermissionScriptFired();

		if (scriptId == POPULATE_REWARDS_CHEST_UI_SCRIPT_ID)
			onRewardsChestScriptFired();
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if (supplyTracker.isTracking() && event.getActor() == client.getLocalPlayer())
		{
			eventBus.post(new PlayerDied(client.getTickCount(), LogType.COLOSSEUM));
			log.debug("Player died at wave {}. Stopping tracker before respawn.", currentWave);

			supplyTracker.stopTracking(currentAttempt.getAttemptRoot(), currentAttempt.getSupplyFileName(),true, enabledCsvLogging);
			activeWave = false;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (!enabledLogging || !inColosseum || !activeWave) return;

		if (!scanner.scannedManticoreSequences(currentWave)) {
			WorldView wv = client.getTopLevelWorldView();
			if (wv != null) {
				scanner.parseManticoreAttackSequences(wv.npcs());
			}
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event) {
		if (!activeWave || !inColosseum) return;

		NPC npc = event.getNpc();
		int id = npc.getId();
		WorldPoint loc = npc.getWorldLocation();

		int relativeTick = client.getTickCount() - waveStartTick;

		if (relativeTick > 30) {
			switch (id) {
				case JAGUAR_WARRIOR_NPC_ID:
					jaguarWarriorReinforcementsSpawn = loc;
					break;
				case MINOTAUR_RED_FLAG_NPC_ID:
				case MINOTAUR_NPC_ID:
					minotaurReinforcementsSpawn = loc;
					break;
				case SERPENT_SHAMAN_NPC_ID:
					serpentShamanReinforcementsSpawn = loc;
					break;
			}
		}
		else {
			switch (id) {
				case SERPENT_SHAMAN_NPC_ID:
					serpentShamanSpawn = loc;
					break;
				case JAVELIN_COLOSSUS_NPC_ID:
					if (javelinColossusSpawnA == null) javelinColossusSpawnA = loc;
					else javelinColossusSpawnB = loc;
					break;
				case MANTICORE_NPC_ID:
					if (manticoreSpawnA == null) {
						manticoreSpawnA = loc;
						scanner.setManticoreIndexA(npc.getIndex());
					}
					else {
						manticoreSpawnB = loc;
						scanner.setManticoreIndexB(npc.getIndex());
					}
					break;
				case SHOCKWAVE_COLOSSUS_NPC_ID:
					if (shockwaveColossusSpawnA == null) shockwaveColossusSpawnA = loc;
					else shockwaveColossusSpawnB = loc;
					break;
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		if (!enabledLogging) return;

		GameState state = event.getGameState();

		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING) {
			if (inColosseum && currentAttempt != null) {
				log.debug("Player logged out or disconnected in the Colosseum. Run failed.");
				finalStatus = WaveStatus.LOGGED_OUT;
				failWave();
				inColosseum = false;
			}
			return;
		}

		updateInColosseum();
		if (currentAttempt != null && state == GameState.LOGGED_IN)
		{
			if (!inColosseum)
			{
				if (currentWave < 2)
				{
					return;
				}
				log.debug("Player left the Colosseum (Death/Teleport/Walked out). Run failed.");
				failWave();
			}
		}
	}

	/**
	 * Initiates a new Attempt and resets all parameters
	 */
	private void startAttempt() {
		log.debug("Starting a new Colosseum attempt.");
		attemptStartTick = client.getTickCount();
		setCurrentWave(1);
		activeModifiers.clear();
		activeWave = false;
		startTime = Instant.ofEpochMilli(System.currentTimeMillis())
			.atZone(ZoneId.systemDefault())
			.format(COLOSSEUM_TRIAL_TIMESTAMP_FORMATTER);
		totalTimeTaken = .0;
		currentAttempt = new ColosseumAttempt(attemptStartTick, getAccountName());
		finalStatus = null;
		activeTrial = true;
		waitingForIntermission = true;
		inColosseum = true;
		parsedTransitionUI = null;
		parsedCurrentWaveIntermission = false;

		if (trackSupplies)
		{
			supplyTracker.startTracking();
			supplyTracker.setTag(currentAttempt.getAttemptId());
		}

		bossWavePhase = 0;
		Arrays.fill(bossWavePhaseTickCounts, -1);

		eventBus.post(new ColosseumAttemptStarted(currentAttempt.getStartTime(), getAccountName(), currentAttempt.getAttemptRoot().getAbsolutePath()));
		log.debug("Starting a new ColosseumAttempt for account {} - Data will be saved in {}", getAccountName(), currentAttempt.getAttemptRoot().getAbsolutePath());

		entryTag = config.colosseumTag();
		try
		{
			entryAccount = getAccountName();
		}
		catch (NullPointerException e)
		{
			entryAccount = "";
		}
	}

	/**
	 * Preprocess the rewards attributes of the ongoing attempt and set them in the ColosseumAttempt instance.
	 */
	private void preprocessAttemptRewards()
	{
		int[] totalValue = {0};

		Map<Integer, Integer> rewards = currentAttempt.getRewards();
		Map<String, ValuedItemStack> namedRewards = new HashMap<>();
		if (rewards != null) {
			rewards.keySet().forEach(itemId -> {
				ItemComposition comp = itemManager.getItemComposition(itemId);
				int price = itemManager.getItemPrice(itemId);
				int qty = rewards.getOrDefault(itemId, 0);
				int value = price * qty;

				totalValue[0] += value;
				namedRewards.put(comp.getName(), new ValuedItemStack(qty, value));
			});
		}
		currentAttempt.setNamedRewards(namedRewards, totalValue[0]);
	}

	/**
	 * Update properties needed to generate the dto, then generate+return the ColosseumAttemptDto
	 */
	private ColosseumAttemptDTO generateColosseumAttemptDto()
	{
		preprocessAttemptRewards();
		return currentAttempt.toDTO();
	}

	/**
	 * Wrap up the ongoing attempt by merging the timeline of states, logging the current attempt and writing the CSV log,
	 * depending on user-defined configurations.
	 */
	private void endAttempt(WaveStatus status)
	{
		attemptEndTick = client.getTickCount();
		double attemptDuration = BigDecimal.valueOf(.6 * (attemptEndTick - attemptStartTick)).setScale(1, RoundingMode.HALF_UP).doubleValue();
		log.debug("[Wave {}] ColosseumAttemptEnded posted | startTick={} endTick={} duration={}s", currentWave, attemptStartTick, attemptEndTick, attemptDuration);
		eventBus.post(new ColosseumAttemptEnded(currentAttempt.getStartTime()));
		currentAttempt.setFinalStatus(status);

		TrackedSupplies supplies = supplyTracker.getConsumedItems();
		currentAttempt.setConsumedSupplies(supplies);

		File attemptJsonFile = currentAttempt.getWaveLogJsonFile();
		ColosseumAttemptDTO dto = generateColosseumAttemptDto();
		fileIOService.logColosseumAttempt(dto, attemptJsonFile);

		if (colosseumDiscordBroadcaster.isEnabledDiscordBroadcasting())
		{
			log.debug("Broadcasting colosseum trial info...");
			colosseumDiscordBroadcaster.broadcastToDiscord(dto, false);
		}

		if (currentWave == 12 && bossWavePhaseTickCounts[0] != -1 && bossWavePhaseTickCounts[1] != -1)
		{
			double totalTimeTaken = 0;
			log.debug("Time taken per boss phase");
			for (int phase = 1; phase < bossWavePhaseTickCounts.length; phase++)
			{
				double phaseTimeTaken = BigDecimal.valueOf(.6*(bossWavePhaseTickCounts[phase]- bossWavePhaseTickCounts[phase-1])).setScale(1, RoundingMode.HALF_UP).doubleValue();
				totalTimeTaken += phaseTimeTaken;
				log.debug("Phase {}: [{}-{}] HP {} seconds (total: {})", phase, BOSS_HP_AT_PHASE.get(phase-1), BOSS_HP_AT_PHASE.get(phase), phaseTimeTaken, totalTimeTaken);
			}
		}

		if (enabledCsvLogging)
		{
			fileIOService.writeColosseumCSVLog(currentAttempt, writeCsvLog());
		}
		if (trackSupplies)
			supplyTracker.stopTracking(currentAttempt.getAttemptRoot(), currentAttempt.getSupplyFileName(), true, enabledCsvLogging);

		currentAttempt = null;

		if (autoMergeWaveFiles)
		{
			fileIOService.mergeColosseumWaveLogs();
		}
	}

	/**
	 * Parse the user interface and extract the relevant values from it.
	 */
	private void parseUI() {
		if (!waitingForIntermission || currentAttempt == null) return;

		IntermissionUI newUI = scanner.scanUI( finalStatus != null, currentWave);

		if (parsedTransitionUI != null && (finalStatus == null || finalStatus == WaveStatus.COMPLETED || finalStatus == WaveStatus.CLAIMED))
		{
			log.debug("Completing ColosseumWave for wave {}", currentWave);
			ColosseumWave completedWave = completeWave(newUI);
			log.debug("{}", completedWave.toDTO());
			submitWave(completedWave);
			selectedModifier = null;
		}
		parsedTransitionUI = newUI;

		waitingForIntermission = false;
	}

	/**
	 * Sets tracked NPC data to null
	 */
	private void resetNpcData()
	{
		serpentShamanSpawn = null;
		javelinColossusSpawnA = null;
		javelinColossusSpawnB = null;
		manticoreSpawnA = null;
		manticoreSpawnB = null;
		shockwaveColossusSpawnA = null;
		shockwaveColossusSpawnB = null;
		jaguarWarriorReinforcementsSpawn = null;
		serpentShamanReinforcementsSpawn = null;
		minotaurReinforcementsSpawn = null;

		if (scanner != null) scanner.resetManticoreSequences();
	}

	/**
	 * Start a new wave by resetting all wave-specific values and notify that a new wave has started
	 */
	private void startWave() {
		if (activeWave) return;
		log.debug("[Wave {}] ColosseumWaveStarted event posted at tick {}", currentWave, waveStartTick);
		eventBus.post(new ColosseumWaveStarted(currentAttempt.getStartTime(), currentWave, waveStartTick));
		activeWave = true;
		parsedCurrentWaveIntermission = false;
		resetNpcData();
	}

	/**
	 * Ends the wave; activeWave flag is updated, ColosseumWaveEnded event is broadcast and a log message is produced.
	 */
	public void endWave() {
		activeWave = false;
		waveEndTick = client.getTickCount();
		eventBus.post(new ColosseumWaveEnded(currentAttempt.getStartTime(), currentWave));
		log.debug("[Wave {}] ColosseumWaveEnded event posted at tick {}", currentWave, client.getTickCount());
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

	/**
	 * Complete the wave using parsed UI data as described by curUI, relay data to appropriate method
	 */
	private ColosseumWave completeWave(IntermissionUI curUI)
	{

		if (finalStatus == null) {
			return generateCompletedWave(curUI);
		}
		switch (finalStatus)
		{
			case COMPLETED:
				return generateCompletedWave(curUI);
			case CLAIMED:
				return generateCancelledWave(curUI);
		}
		throw new IllegalStateException("Unexpected finalStatus in completeWave(): " + finalStatus);
	}

	/**
	 * Creates a pre-populated builder for a ColosseumWave with all the common
	 * state tracking variables shared across different wave outcomes.
	 */
	private ColosseumWave.ColosseumWaveBuilder buildBaseWave() {
		ItemBundle potentialLoot = getPotentialLoot();
		int value;
		if (potentialLoot != null) {
			int itemId = potentialLoot.getItemId();
			int price = itemManager.getItemPrice(itemId);
			value = price * potentialLoot.getQuantity();

		}
		else
			value = 0;

		return ColosseumWave.builder()
			.accountName(entryAccount)
			.tag(entryTag)
			.earnedLoot(getPotentialLoot())
			.lootValue(value)
			.activeModifiers(getActiveModifiersList())
			.modifierChoices(parsedTransitionUI != null ? parsedTransitionUI.getModifierChoices() : new ArrayList<>())
			.startTick(waveStartTick)
			.endTick(waveEndTick)
			.serpentShamanSpawn(serpentShamanSpawn)
			.javelinColossusSpawnA(javelinColossusSpawnA)
			.javelinColossusSpawnB(javelinColossusSpawnB)
			.manticoreSpawnA(manticoreSpawnA)
			.manticoreSequenceA(scanner != null ? scanner.getManticoreSequenceA() : null)
			.manticoreSpawnB(manticoreSpawnB)
			.manticoreSequenceB(scanner != null ? scanner.getManticoreSequenceB() : null)
			.shockwaveColossusSpawnA(shockwaveColossusSpawnA)
			.shockwaveColossusSpawnB(shockwaveColossusSpawnB)
			.jaguarWarriorReinforcementsSpawn(jaguarWarriorReinforcementsSpawn)
			.serpentShamanReinforcementsSpawn(serpentShamanReinforcementsSpawn)
			.minotaurReinforcementsSpawn(minotaurReinforcementsSpawn);
	}

	/**
	 * Generate a completed wave by combining new data with the previously parsed UI data and return it
	 */
	private ColosseumWave generateCompletedWave(IntermissionUI curUI) {
		int endTick = client.getTickCount();
		log.debug("Completed wave {} at tick {}", completedWave, endTick);

		double timeTaken = getWaveTimeTaken();
		totalTimeTaken = BigDecimal.valueOf(totalTimeTaken + timeTaken).setScale(1, RoundingMode.HALF_UP).doubleValue();

		return buildBaseWave()
			.wave(completedWave)
			.status(WaveStatus.COMPLETED)
			.chosenModifier(parsedTransitionUI != null ? parsedTransitionUI.getSelectedModifier() : null)
			.timeTaken(timeTaken)
			.totalTimeTaken(totalTimeTaken)
			.speedBonus(curUI.getSpeedBonusGlory())
			.damageTaken(curUI.getDamageTakenAmount())
			.damageBonus(curUI.getDamageTakenGlory())
			.modifierGlory(curUI.getModChoiceGlory())
			.completionBonus(curUI.getWaveBonusGlory())
			.waveGlory(curUI.getWaveGlory())
			.totalGlory(curUI.getTotalGlory())
			.build();
	}

	/**
	 * Ongoing wave has failed; update it accordingly and submit it. Subsequently, end the ongoing attempt.
	 */
	private void failWave() {
		if (currentAttempt == null || finalStatus != null && finalStatus != WaveStatus.CONFIG_DISABLED && finalStatus != WaveStatus.LOGGED_OUT) return;

		activeWave = false;
		endWave();

		final WaveStatus submitStatus;
		if (finalStatus == null) {
			submitStatus = WaveStatus.FAILED;
			log.debug("[Wave {}] Failed wave during ongoing Colosseum attempt at tick={} finalStatus was set to {}", currentWave, client.getTickCount(), finalStatus);
		} else {
			log.debug("[Wave {}] Failed wave during ongoing Colosseum attempt at tick={} with finalStatus={}", currentWave, client.getTickCount(), finalStatus);
			submitStatus = finalStatus;
		}

		double timeTaken = getWaveTimeTaken();
		totalTimeTaken = BigDecimal.valueOf(totalTimeTaken + timeTaken).setScale(1, RoundingMode.HALF_UP).doubleValue();

		ColosseumWave failedWave = buildBaseWave()
			.wave(currentWave)
			.status(submitStatus)
			.chosenModifier(selectedModifier)
			.timeTaken(timeTaken)
			.totalTimeTaken(totalTimeTaken)
			.totalGlory(parsedTransitionUI != null ? parsedTransitionUI.getTotalGlory() : 0)
			.speedBonus(0)
			.damageTaken(0)
			.damageBonus(0)
			.modifierGlory(0)
			.completionBonus(0)
			.waveGlory(0)
			.build();

		log.debug(failedWave.toString());
		submitWave(failedWave);
		endAttempt(submitStatus);
	}

	/**
	 * Generate a cancelled wave and return it. Occurs if the rewards chest is opened before wave 12.
	 */
	private ColosseumWave generateCancelledWave(IntermissionUI curUI) {
		int endTick = client.getTickCount();
		log.debug("Cancelled wave {} at tick {}", currentWave, endTick);
		resetNpcData();
		return buildBaseWave()
			.wave(completedWave + 1)
			.status(WaveStatus.CANCELLED)
			.chosenModifier(null)
			.timeTaken(0)
			.totalTimeTaken(totalTimeTaken)
			.totalGlory(curUI.getTotalGlory())
			.startTick(endTick)
			.endTick(endTick)
			.speedBonus(0)
			.damageTaken(0)
			.damageBonus(0)
			.modifierGlory(0)
			.completionBonus(0)
			.waveGlory(0)
			.build();
	}

	/**
	 * Processes a completed or failed wave and updates the current attempt's state.
	 * @param wave The ColosseumWave object that was just finished/failed.
	 */
	private void submitWave(ColosseumWave wave) {
		if (currentAttempt == null) {
			return;
		}

		List<ColosseumWave> waves = currentAttempt.getWaves();

		if (waves.isEmpty() || waves.get(waves.size() - 1).getWave() != wave.getWave()) {
			waves.add(wave);
			currentAttempt.setTotalTimeTaken(wave.getTotalTimeTaken());

			ColosseumModifier chosenMod = wave.getChosenModifier();
			if (chosenMod != null) {
				currentAttempt.getActiveModifiers().put(chosenMod.getId(), chosenMod);
			}

			if (wave.getStatus() == WaveStatus.COMPLETED) {
				Map<Integer, Integer> rewards = currentAttempt.getRewards();
				ItemBundle bundle = wave.getEarnedLoot();

				if (bundle != null) {
					rewards.merge(bundle.getItemId(), bundle.getQuantity(), Integer::sum);
				}

				if (wave.getWave() == 12) {
					ItemBundle guaranteedReward = enabledSwapQuiverLoot
						? DIZANAS_QUIVER_SWAPPED_REWARD
						: DIZANAS_QUIVER_REWARD;

					rewards.merge(guaranteedReward.getItemId(), guaranteedReward.getQuantity(), Integer::sum);
				}
			}
		}
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
		return enabledLogging;
	}

	/**
	 * Compute the time taken based on wave start and end ticks, return it with up to one decimal in seconds.
	 */
	private double getWaveTimeTaken()
	{
		return BigDecimal.valueOf(.6 * (waveEndTick - waveStartTick)).setScale(1, RoundingMode.HALF_UP).doubleValue();
	}

	/**
	 * Generate and return a String that is to be written into the CSV log
	 */
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

	/**
	 * Updates the value of currentWave and logs a message
	 */
	private void setCurrentWave(int waveNumber)
	{
		if (currentWave == waveNumber) return;

		log.debug("Updated currentWave value from {} to {} at tick={}", currentWave, waveNumber, client.getTickCount());
		currentWave = waveNumber;
	}
	/**
	 * Return the potential loot of the most recently scanned UI
	 */
	private ItemBundle getPotentialLoot()
	{
		return parsedTransitionUI != null ? parsedTransitionUI.getPotentialLoot() : null;
	}

	// Update the return type and stream collector
	private List<String> getActiveModifiersList() {
		if (activeModifiers.isEmpty()) {
			return Collections.emptyList();
		}

		Map<Integer, ColosseumModifier> highestTierMap = new LinkedHashMap<>();
		for (String modName : activeModifiers) {
			if (modName == null || modName.trim().isEmpty()) {
				continue;
			}
			try {
				ColosseumModifier modifier = ColosseumModifier.valueOf(modName);
				highestTierMap.put(modifier.getId(), modifier);
			} catch (IllegalArgumentException e) {
				log.debug("Unknown modifier {} detected", modName);
			}
		}

		// Return a list instead of Collectors.joining("|")
		return highestTierMap.values().stream()
			.map(ColosseumModifier::name)
			.collect(Collectors.toList());
	}

	/**
	 *
	 */
	private String getActiveModifiersString() {
		if (activeModifiers.isEmpty()) {
			return "";
		}
		Map<Integer, ColosseumModifier> highestTierMap = new LinkedHashMap<>();

		for (String modName : activeModifiers) {
			if (modName == null || modName.trim().isEmpty()) {
				continue;
			}

			try {
				ColosseumModifier modifier = ColosseumModifier.valueOf(modName);
				highestTierMap.put(modifier.getId(), modifier);

			} catch (IllegalArgumentException e) {
				log.debug("Unknown modifier {} detected", modName);
			}
		}
		return highestTierMap.values().stream()
			.map(ColosseumModifier::name)
			.collect(Collectors.joining("|"));
	}
}