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
import static com.datalogger.constants.Colosseum.Intermission.INTERMISSION_GROUP_ID;
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
import com.datalogger.events.ColosseumAttemptEnded;
import com.datalogger.events.ColosseumWaveEnded;
import com.datalogger.events.ColosseumWaveStarted;
import com.datalogger.framework.AbstractLogger;
import com.datalogger.framework.LogType;
import com.datalogger.models.colosseum.ColosseumAttempt;
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
import net.runelite.api.NPC;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class ColosseumAttemptLogger extends AbstractLogger
{
	private IntermissionUI parsedTransitionUI;

	private String entryTag;
	private String entryAccount;

	private boolean activeTrial;
	private boolean activeWave;
	private boolean inColosseum;

	private boolean enabledLogging;
	private boolean enabledCsvLogging;

	private boolean parsedCurrentWaveIntermission;
	private int currentWave;
	private int completedWave;
//	private int selectedModifierIdx;
	private ColosseumModifier selectedModifier;

	private int attemptStartTick;
	private int attemptEndTick;
	private int waveStartTick;
	private int waveEndTick;

	private ColosseumAttempt currentAttempt;
	private WaveStatus finalStatus;

	private final ColosseumScanner scanner;
	private final Client client;
	private final FileIOService fileIOService;
	private final DataLoggerConfig config;
	private final EventBus eventBus;

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
	public ColosseumAttemptLogger(ColosseumScanner scanner, Client client, FileIOService fileIOService, DataLoggerConfig config, EventBus eventBus) {
		this.scanner = scanner;
		this.client = client;
		this.fileIOService = fileIOService;
		this.config = config;
		this.eventBus = eventBus;

		updateConfigFlags();
	}

	/**
	 * Parse relevant plugin configurations
	 */
	private void updateConfigFlags()
	{
		enabledLogging = config.logColosseum();
		enabledCsvLogging = enabledLogging && config.logColosseumCSV();
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
			log.info("Starting wave {} - setting waveStartTick to {}", currentWave, waveStartTick);
			log.info("Modifier change tick is {}", waveStartTick-5);
			startWave();
			return;
		}

		if (message.startsWith(BOSS_WAVE_START_PREFIX))
		{
			completedWave = 12;
			waveStartTick = client.getTickCount();
			log.info("Starting wave 12 - setting waveStartTick to {}", waveStartTick);
			log.info("Modifier change tick is {}", waveStartTick-5);
			startWave();
			return;
		}

		if (message.startsWith("Wave ") && message.contains("completed! Wave duration:"))
		{
			waveEndTick = client.getTickCount();
			log.info("[WAVE {}] Setting WaveEndTick for wave to {}", currentWave, waveStartTick);
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
					log.info("[WAVE {}] Failed to resolve selected modifier; modifierChoices is null", currentWave);
				}
				else if (modifierChoices.size() != 3)
				{
					log.info("[WAVE {}] Unexpected amount of modifierchoices: {}", currentWave, modifierChoices);
				}
				else
				{
					int selectedModifierIdx = event.getValue()-1;
					selectedModifier = modifierChoices.get(selectedModifierIdx);
					parsedTransitionUI.setSelectedModifier(selectedModifier);
					log.info("[WAVE {}] Varbit {} was changed - new value is {}, set selectedModifier to {}", currentWave, COLOSSEUM_SELECTED_MODIFIER_VARBIT, selectedModifierIdx, selectedModifier);
				}
			}
			else
			{
				log.info("[WAVE {}] Failed to resolve selected modifier; parsedTransitionUI is null", currentWave);
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
	public void onGameTick(GameTick event) {
		if (!enabledLogging || !inColosseum) return;

		if (activeWave && !scanner.scannedManticoreSequences(currentWave)) {
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
				log.info("Player logged out or disconnected in the Colosseum. Run failed.");
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
				log.info("Player left the Colosseum (Death/Teleport/Walked out). Run failed.");
				failWave();
			}
		}
	}

	/**
	 * Initiates a new Attempt and resets all parameters
	 */
	private void startAttempt() {
		log.info("Starting a new Colosseum attempt.");
		attemptStartTick = client.getTickCount();
		setCurrentWave(1);
		activeWave = false;
		currentAttempt = new ColosseumAttempt(attemptStartTick);
		finalStatus = null;
		activeTrial = true;
		waitingForIntermission = true;
		inColosseum = true;
		parsedTransitionUI = null;
		parsedCurrentWaveIntermission = false;

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
	 * Wrap up the ongoing attempt by merging the timeline of states, logging the current attempt and writing the CSV log,
	 * depending on user-defined configurations.
	 */
	private void endAttempt(WaveStatus status)
	{
		attemptEndTick = client.getTickCount();
		double attemptDuration = BigDecimal.valueOf(.6 * (attemptEndTick - attemptStartTick)).setScale(1, RoundingMode.HALF_UP).doubleValue();
		log.info("[Wave {}] ColosseumAttemptEnded posted | startTick={} endTick={} duration={}s", currentWave, attemptStartTick, attemptEndTick, attemptDuration);
		eventBus.post(new ColosseumAttemptEnded(currentAttempt.getStartTime()));
		currentAttempt.setFinalStatus(status);
		fileIOService.logColosseumAttempt(currentAttempt);

		if (enabledCsvLogging)
			fileIOService.writeColosseumCSVLog(entryAccount, currentAttempt.getStartTime(), writeCsvLog());
		currentAttempt = null;
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
			log.debug(completedWave.toString());
			currentAttempt.submitWave(completedWave);
			selectedModifier = null;
		}
		parsedTransitionUI = newUI;

		waitingForIntermission = false;
	}

	/**
	 * Ongoing wave has failed; update it accordingly and submit it. Subsequently, end the ongoing attempt.
	 */
	private void failWave() {
		if (currentAttempt == null || finalStatus != null && finalStatus != WaveStatus.CONFIG_DISABLED) return;
		activeWave = false;
		endWave();
		final WaveStatus submitStatus;
		if (finalStatus == null)
		{
			submitStatus = WaveStatus.FAILED;
			log.debug("[Wave {}] Failed wave during ongoing Colosseum attempt at tick={} finalStatus was set to {}", currentWave, client.getTickCount(), finalStatus);
		}
		else
		{
			log.debug("[Wave {}] Failed wave during ongoing Colosseum attempt at tick={} with finalStatus={}", currentWave, client.getTickCount(), finalStatus);
			submitStatus = finalStatus;
		}

		List<ItemBundle> loot = parsedTransitionUI != null && parsedTransitionUI.getPotentialLoot() != null ? parsedTransitionUI.getPotentialLoot() : new ArrayList<>();

		double timeTaken = getWaveTimeTaken();
		ColosseumWave failedWave = ColosseumWave.builder()
			.wave(currentWave)
			.status(submitStatus)
			.accountName(entryAccount)
			.tag(entryTag)
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
			.serpentShamanSpawn(serpentShamanSpawn)
			.javelinColossusSpawnA(javelinColossusSpawnA)
			.javelinColossusSpawnB(javelinColossusSpawnB)
			.manticoreSpawnA(manticoreSpawnA)
			.manticoreSequenceA(scanner.getManticoreSequenceA())
			.manticoreSpawnB(manticoreSpawnB)
			.manticoreSequenceB(scanner.getManticoreSequenceB())
			.shockwaveColossusSpawnA(shockwaveColossusSpawnA)
			.shockwaveColossusSpawnB(shockwaveColossusSpawnB)
			.jaguarWarriorReinforcementsSpawn(jaguarWarriorReinforcementsSpawn)
			.serpentShamanReinforcementsSpawn(serpentShamanReinforcementsSpawn)
			.minotaurReinforcementsSpawn(minotaurReinforcementsSpawn)
			.build();

		log.debug(failedWave.toString());

		currentAttempt.submitWave(failedWave);
		endAttempt(submitStatus);
	}

	/**
	 * Start a new wave by resetting all wave-specific values and notify that a new wave has started
	 */
	private void startWave() {
		if (activeWave) return;

		waveStartTick = client.getTickCount();
		log.info("[Wave {}] ColosseumWaveStarted event posted at tick {}", currentWave, waveStartTick);
		eventBus.post(new ColosseumWaveStarted(currentAttempt.getStartTime(), currentWave, waveStartTick));
		activeWave = true;
		parsedCurrentWaveIntermission = false;

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

		scanner.resetManticoreSequences();
	}

	/**
	 * Ends the wave; activeWave flag is updated, ColosseumWaveEnded event is broadcast and a log message is produced.
	 */
	public void endWave() {
		activeWave = false;
		waveEndTick = client.getTickCount();
		eventBus.post(new ColosseumWaveEnded(currentAttempt.getStartTime(), currentWave));
		log.info("[Wave {}] ColosseumWaveEnded event posted at tick {}", currentWave, client.getTickCount());
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
	 * Generate a completed wave by combining new data with the previously parsed UI data and return it
	 */
	private ColosseumWave generateCompletedWave(IntermissionUI curUI)
	{
		int endTick = client.getTickCount();
		log.info("Completed wave {} at tick {}", completedWave, endTick);

		List<ItemBundle> loot = parsedTransitionUI != null && parsedTransitionUI.getPotentialLoot() != null
			? parsedTransitionUI.getPotentialLoot()
			: new ArrayList<>();

		double timeTaken = getWaveTimeTaken();
		return ColosseumWave.builder()
			.wave(completedWave)
			.status(WaveStatus.COMPLETED)
			.accountName(entryAccount)
			.tag(entryTag)
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
			.serpentShamanSpawn(serpentShamanSpawn)
			.javelinColossusSpawnA(javelinColossusSpawnA)
			.javelinColossusSpawnB(javelinColossusSpawnB)
			.manticoreSpawnA(manticoreSpawnA)
			.manticoreSequenceA(scanner.getManticoreSequenceA())
			.manticoreSpawnB(manticoreSpawnB)
			.manticoreSequenceB(scanner.getManticoreSequenceB())
			.shockwaveColossusSpawnA(shockwaveColossusSpawnA)
			.shockwaveColossusSpawnB(shockwaveColossusSpawnB)
			.jaguarWarriorReinforcementsSpawn(jaguarWarriorReinforcementsSpawn)
			.serpentShamanReinforcementsSpawn(serpentShamanReinforcementsSpawn)
			.minotaurReinforcementsSpawn(minotaurReinforcementsSpawn)

			.build();
	}

	/**
	 * Generate a cancelled wave and return it. Occurs if the rewards chest is opened before wave 12.
	 */
	private ColosseumWave generateCancelledWave(IntermissionUI curUI)
	{
		int endTick = client.getTickCount();
		log.info("Cancelled wave {} at tick {}", currentWave, endTick);
		List<ItemBundle> loot = parsedTransitionUI != null && parsedTransitionUI.getPotentialLoot() != null
			? parsedTransitionUI.getPotentialLoot()
			: new ArrayList<>();
		return ColosseumWave.builder()
			.wave(completedWave+1)
			.status(WaveStatus.CANCELLED)
			.accountName(entryAccount)
			.tag(entryTag)
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
			.serpentShamanSpawn(null)
			.javelinColossusSpawnA(null)
			.javelinColossusSpawnB(null)
			.manticoreSpawnA(null)
			.manticoreSequenceA(null)
			.manticoreSpawnB(null)
			.manticoreSequenceB(null)
			.shockwaveColossusSpawnA(null)
			.shockwaveColossusSpawnB(null)
			.jaguarWarriorReinforcementsSpawn(null)
			.serpentShamanReinforcementsSpawn(null)
			.minotaurReinforcementsSpawn(null)
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
		return enabledLogging;
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

	/**
	 * Updates the value of currentWave and logs a message
	 */
	private void setCurrentWave(int waveNumber)
	{
		if (currentWave == waveNumber) return;

		log.info("Updated currentWave value from {} to {} at tick={}", currentWave, waveNumber, client.getTickCount());
		currentWave = waveNumber;
	}
}