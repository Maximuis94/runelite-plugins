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
import static com.datalogger.constants.Colosseum.Intermission.INTERMISSION_RESULT_CONTAINER;
import static com.datalogger.constants.Colosseum.Message.BOSS_WAVE_START_PREFIX;
import static com.datalogger.constants.Colosseum.Message.DEATH_MESSAGE;
import static com.datalogger.constants.Colosseum.Message.END_ATTEMPT_MESSAGE;
import static com.datalogger.constants.Colosseum.Message.START_ATTEMPT_MESSAGE;
import static com.datalogger.constants.Colosseum.Message.WAVE_START_PREFIX;
import static com.datalogger.constants.Colosseum.NPC.JAGUAR_WARRIOR_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.JAVELIN_COLOSSUS_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.MANTICORE_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.MINOTAUR_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.MINOTAUR_RED_FLAG_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.SERPENT_SHAMAN_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.SHOCKWAVE_COLOSSUS_NPC_ID;
import static com.datalogger.constants.Colosseum.Region.COLOSSEUM_REGION_ID;
import static com.datalogger.constants.Colosseum.RewardsChest.REWARDS_CHEST_GROUP_ID;
import static com.datalogger.constants.Colosseum.RewardsChest.REWARDS_CHEST_REWARDS_TAB_CHILD_ID;
import static com.datalogger.constants.Colosseum.Script.POPULATE_INTERMISSION_UI_SCRIPT_ID;
import static com.datalogger.constants.Colosseum.Script.POPULATE_REWARDS_CHEST_UI_SCRIPT_ID;
import static com.datalogger.constants.Colosseum.Varbit.COLOSSEUM_SELECTED_MODIFIER_VARBIT;
import com.datalogger.events.ColosseumAttemptEnded;
import com.datalogger.events.ColosseumAttemptStarted;
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
import org.apache.commons.lang3.NotImplementedException;

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

	private int currentWave;
	private int selectedModifierIdx;
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
	private boolean widgetIsOpen;

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
		startAttempt();
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
				endWave();
				failWave();
				endAttempt();
			}
		}
	}

	/**
	 * Return true if the UI intermission/rewards chest UI is ready to be parsed
	 */
	private boolean isPostWaveUI(int scriptId) {
		return scriptId == POPULATE_INTERMISSION_UI_SCRIPT_ID || scriptId == POPULATE_REWARDS_CHEST_UI_SCRIPT_ID;
	}

	/**
	 * Event used to identify a post-wave intermission or rewards chest UI
	 */
	@Subscribe
	public void onScriptPostFired(ScriptPostFired event) {
		if (!enabledLogging || !inColosseum) return;

		if (!widgetIsOpen && isPostWaveUI(event.getScriptId())) {
			widgetIsOpen = true;
		}
	}

	/**
	 * Return true if the given ChatMessageType is used to detect wave transitions or the start/end of an attempt
	 */
	private boolean isRelevantChatMessageType(ChatMessageType chatMessageType) {
		return chatMessageType != ChatMessageType.NPC_SAY &&
			chatMessageType != ChatMessageType.GAMEMESSAGE &&
			chatMessageType != ChatMessageType.CONSOLE;
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if (!enabledLogging || !inColosseum || !isRelevantChatMessageType(event.getType())) return;

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
				int selectedModifierIdx = event.getValue()-1;
				selectedModifier = parsedTransitionUI.getModifierChoices().get(selectedModifierIdx);
				parsedTransitionUI.setSelectedModifier(selectedModifier);
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		if (!enabledLogging || !inColosseum) return;

		if (!scanner.scannedManticoreSequences(currentWave))
		{
			for (NPC npc : client.getTopLevelWorldView().npcs())
			{
				if (npc != null)
					scanner.parseManticoreAttackSequence(npc);
			}
		}

		if (widgetIsOpen && !waitingForIntermission)
		{
			widgetIsOpen = false;
			parseUI();
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
					if (manticoreSpawnA == null)
					{
						manticoreSpawnA = loc;
						scanner.setManticoreIndexA(npc.getIndex());
					}
					else
					{
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

	/**
	 * Reset all variables relevant for tracking an attempt to the initial state
	 */
	private void startAttempt() {
		attemptStartTick = client.getTickCount();
		currentWave = 1;
		activeWave = false;
		currentAttempt = new ColosseumAttempt(attemptStartTick);
		finalStatus = null;
		activeTrial = true;
		waitingForIntermission = true;
		parsedTransitionUI = null;
		entryAccount = getAccountName();

		try
		{
			entryTag = config.colosseumTag();
		}
		catch (NullPointerException e)
		{
			entryTag = "";
		}

		eventBus.post(new ColosseumAttemptStarted(currentAttempt.getStartTime()));
		log.info("Starting a new Colosseum attempt.");
	}

	/**
	 * Wrap up the ongoing attempt by merging the timeline of states, logging the current attempt and writing the CSV log,
	 * depending on user-defined configurations.
	 */
	private void endAttempt() {
		eventBus.post(new ColosseumAttemptEnded(currentAttempt.getStartTime()));
		currentAttempt.setFinalStatus(finalStatus);
		fileIOService.logColosseumAttempt(currentAttempt);

		if (enabledCsvLogging)
			fileIOService.writeColosseumCSVLog(entryAccount, currentAttempt.getStartTime(), writeCsvLog());
		currentAttempt = null;
	}

	private void parseUI() {
		if (!widgetIsOpen) return;

		IntermissionUI newUI = scanner.scanUI(finalStatus != null, currentWave);

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

	/**
	 * Ongoing wave has failed. Submit data extracted from pre-wave UI
	 */
	private void failWave() {
		if (currentAttempt == null || finalStatus != null && finalStatus != WaveStatus.CONFIG_DISABLED) return;
		log.info("Failed attempt at wave {}", currentWave);

		activeWave = false;

		if (finalStatus == null)
			finalStatus = WaveStatus.FAILED;

		endWave();

		List<ItemBundle> loot = parsedTransitionUI != null && parsedTransitionUI.getPotentialLoot() != null ? parsedTransitionUI.getPotentialLoot() : new ArrayList<>();

		double timeTaken = getWaveTimeTaken();
		ColosseumWave failedWave = ColosseumWave.builder()
			.wave(currentWave)
			.status(finalStatus)
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

		log.debug("Failed Colosseum attempt during wave {}", currentWave);
		log.debug(failedWave.toString());


		currentAttempt.submitWave(failedWave);

		endAttempt();
	}

	/**
	 * Reset all wave-specific values and notify that a new wave has started
	 */
	private void buildWave() {
		if (activeWave) return;

		waveStartTick = client.getTickCount();
		eventBus.post(new ColosseumWaveStarted(currentAttempt.getStartTime(), currentWave, waveStartTick));
		activeWave = true;
		scanner.resetManticoreSequences();

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

		log.debug("Starting wave {}", currentWave);


	}

	/**
	 * Ends the wave; flag is updated, the eventBus is updated and a log message is produced.
	 */
	public void endWave() {
		activeWave = false;
		eventBus.post(new ColosseumWaveEnded(currentAttempt.getStartTime(), currentWave));
		log.debug("Wave {} ended", currentWave);
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
	 * Generate a cancelled wave and return it. Occurs after parsing the rewards chest UI before wave 12.
	 */
	private ColosseumWave generateCancelledWave(IntermissionUI curUI)
	{
		List<ItemBundle> loot = parsedTransitionUI != null && parsedTransitionUI.getPotentialLoot() != null
			? parsedTransitionUI.getPotentialLoot()
			: new ArrayList<>();
		int endTick = client.getTickCount();
		return ColosseumWave.builder()
			.wave(currentWave+1)
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

	private boolean isDeathMessage(String message)
	{
		return message.startsWith(DEATH_MESSAGE);
	}

	private boolean isWaveStartedMessage(String message)
	{
		return message.startsWith(START_ATTEMPT_MESSAGE);
	}

	private boolean isStartAttemptMessage(String message)
	{
		return currentWave < 11 && message.startsWith(WAVE_START_PREFIX);
	}

	private boolean isBossWaveStartedMessage(String message)
	{
		return currentWave == 11 && message.startsWith(BOSS_WAVE_START_PREFIX);
	}

	public static boolean isWaveCompletedMessage(String message)
	{
		return message.startsWith("Wave ") && message.contains("completed! Wave duration:");
	}

	public static boolean isAttemptCompletedMessage(String message)
	{
		return message.startsWith(END_ATTEMPT_MESSAGE);
	}

}