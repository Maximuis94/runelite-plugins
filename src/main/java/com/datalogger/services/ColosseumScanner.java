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

package com.datalogger.services;

import com.datalogger.DataLoggerConfig;
import static com.datalogger.constants.Colosseum.Intermission.INTERMISSION_GROUP_ID;
import static com.datalogger.constants.Colosseum.Intermission.INTERMISSION_MODIFIER_CHOICES;
import static com.datalogger.constants.Colosseum.Intermission.INTERMISSION_MODIFIER_LIST_CONTAINER;
import static com.datalogger.constants.Colosseum.Intermission.INTERMISSION_NEXT_LOOT_CONTAINER;
import static com.datalogger.constants.Colosseum.Intermission.INTERMISSION_RESULT_CONTAINER;
import static com.datalogger.constants.Colosseum.Item.DIZANAS_QUIVER_UNCHARGED_ID;
import static com.datalogger.constants.Colosseum.Item.SWAPPED_DIZANAS_QUIVER_ITEM_BUNDLE;
import static com.datalogger.constants.Colosseum.ManticoreAttack.MAGIC_ORB_ID;
import static com.datalogger.constants.Colosseum.ManticoreAttack.MELEE_ORB_ID;
import static com.datalogger.constants.Colosseum.ManticoreAttack.RANGED_ORB_ID;
import static com.datalogger.constants.Colosseum.NPC.*;
import static com.datalogger.constants.Colosseum.NPC_ALIAS.BOSS_WAVE_BEAM_CRYSTAL_NPC_NAME;
import static com.datalogger.constants.Colosseum.NPC_ALIAS.SOLARFLARE_NPC_NAME;
import static com.datalogger.constants.Colosseum.RewardsChest.REWARDS_CHEST_GROUP_ID;
import static com.datalogger.constants.Colosseum.RewardsChest.REWARDS_CHEST_MODIFIER_LIST_CONTAINER_ID;
import static com.datalogger.constants.Colosseum.RewardsChest.REWARDS_CHEST_REWARDS_TAB_CHILD_ID;
import static com.datalogger.constants.Colosseum.RewardsChest.REWARDS_CHEST_SUMMARY_TAB_CHILD_ID;
import com.datalogger.models.colosseum.ColosseumNPC;
import com.datalogger.models.colosseum.ColosseumState;
import com.datalogger.models.colosseum.IntermissionUI;
import com.datalogger.models.colosseum.ManticoreAttackSequence;
import com.datalogger.models.colosseum.SummaryUI;
import com.datalogger.models.colosseum.enums.ColosseumModifier;
import com.datalogger.models.colosseum.enums.TimestampFormat;
import com.datalogger.models.itemvault.ItemBundle;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ActorSpotAnim;
import net.runelite.api.Client;
import net.runelite.api.IndexedObjectSet;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class ColosseumScanner
{
	private final Client client;

	private final ItemManager itemManager;

	private final DataLoggerConfig config;

	private boolean enabledSwapQuiverLoot;
	private TimestampFormat timestampFormat;

	private boolean scannedManticores;
	private Integer manticoreIndexA = null;
	private Integer manticoreIndexB = null;

	private final Map<Integer, ManticoreAttackSequence> manticoreSequences = new HashMap<>();
	private final Map<Integer, String> manticoreSequenceStrings = new HashMap<>();

	public void setManticoreIndexA(int index) {
		manticoreIndexA = index;
	}

	public ManticoreAttackSequence getManticoreSequenceA()
	{
		return manticoreIndexA != null ? manticoreSequences.get(manticoreIndexA) : null;
	}

	public void setManticoreIndexB(int index) {
		manticoreIndexB = index;
	}

	public ManticoreAttackSequence getManticoreSequenceB()
	{
		return manticoreIndexB != null ? manticoreSequences.get(manticoreIndexB) : null;
	}

	private final Set<Integer> CORE_COLOSSEUM_MOBS = ImmutableSet.of(
		JAVELIN_COLOSSUS_NPC_ID,
		MANTICORE_NPC_ID,
		SHOCKWAVE_COLOSSUS_NPC_ID,
		SOL_HEREDIT_NPC_ID,
		JAGUAR_WARRIOR_NPC_ID,
		SERPENT_SHAMAN_NPC_ID,
		MINOTAUR_NPC_ID,
		MINOTAUR_RED_FLAG_NPC_ID
	);

	private Map<BooleanSupplier, Collection<Integer>> OPTIONAL_NPC_SUPPLIERS;
	private final Set<Integer> trackedNpcIds = new HashSet<>();

	@Inject
	public ColosseumScanner(DataLoggerConfig config, Client client, ItemManager itemManager)
	{
		this.config = config;
		this.client = client;
		this.itemManager = itemManager;
		OPTIONAL_NPC_SUPPLIERS = new HashMap<>();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(DataLoggerConfig.CONFIG_GROUP)) {
			return;
		}

		updateConfigFlags(false);
	}

	/**
	 * Return true if all Manticores have an identified orb sequence, false if not. Cache result until end of wave.
	 */
	public boolean scannedManticoreSequences(int currentWave) {
		if (scannedManticores) return true;

		boolean manticoreADone = getManticoreSequenceA() != null;
		boolean manticoreBDone = getManticoreSequenceB() != null;

		if (currentWave < 4) {
			scannedManticores = true;
		} else if (currentWave <= 8) {
			scannedManticores = manticoreADone;
		} else {
			scannedManticores = manticoreADone && manticoreBDone;
		}

		return scannedManticores;
	}

	/**
	 * Identify the orb order of npc, provided it is a Manticore, it is not already logged and it has three orbs
	 */
	public void parseManticoreAttackSequence(NPC npc) {
		if (npc == null || manticoreSequences.containsKey(npc.getIndex())) return;

		int npcIndex = npc.getIndex();

		Iterable<ActorSpotAnim> anims = npc.getSpotAnims();
		if (anims == null) return;

		List<ActorSpotAnim> activeOrbs = new ArrayList<>(3);

		for (ActorSpotAnim anim : anims) {
			int id = anim.getId();
			if (id == MAGIC_ORB_ID || id == RANGED_ORB_ID || id == MELEE_ORB_ID) {
				activeOrbs.add(anim);
			}
		}

		if (activeOrbs.size() == 3) {
			ManticoreAttackSequence sequence = new ManticoreAttackSequence(activeOrbs);
			manticoreSequences.put(npcIndex, sequence);
			manticoreSequenceStrings.put(npcIndex, sequence.stateString());

			String manticoreTag = manticoreIndexA == npcIndex ? "A" : "B";

			log.info("Manticore {} sequence identified as {} at tick={}", manticoreTag, sequence, client.getTickCount());
		}
	}

	/**
	 * Attempt to assign attack sequences to Manticores if they are not registered yet
	 */
	public void parseManticoreAttackSequences(IndexedObjectSet<? extends NPC> npcs)
	{
		if (manticoreIndexA != null && !manticoreSequences.containsKey(manticoreIndexA)) {
			parseManticoreAttackSequence(npcs.byIndex(manticoreIndexA));
		}

		if (manticoreIndexB != null && !manticoreSequences.containsKey(manticoreIndexB)) {
			parseManticoreAttackSequence(npcs.byIndex(manticoreIndexB));
		}
	}

	/**
	 * Clears existing identified Manticore attack sequences and indices.
	 */
	public void resetManticoreSequences()
	{
		manticoreSequences.clear();
		manticoreSequenceStrings.clear();
		manticoreIndexA = null;
		manticoreIndexB = null;
		scannedManticores = false;
	}

	/**
	 * Match a ColosseumModifier instance to the given String and return it
	 */
	private ColosseumModifier matchModifier(String text)
	{
		switch (text)
		{
			case "Bees!":
				return ColosseumModifier.BEES_I;
			case "Bees! (II)":
				return ColosseumModifier.BEES_II;
			case "Bees! (III)":
				return ColosseumModifier.BEES_III;

			case "Blasphemy":
				return ColosseumModifier.BLASPHEMY_I;
			case "Blasphemy (II)":
				return ColosseumModifier.BLASPHEMY_II;
			case "Blasphemy (III)":
				return ColosseumModifier.BLASPHEMY_III;

			case "Doom":
				return ColosseumModifier.DOOM_I;
			case "Doom (II)":
				return ColosseumModifier.DOOM_II;
			case "Doom (III)":
				return ColosseumModifier.DOOM_III;

			case "Frailty":
				return ColosseumModifier.FRAILTY_I;
			case "Frailty (II)":
				return ColosseumModifier.FRAILTY_II;
			case "Frailty (III)":
				return ColosseumModifier.FRAILTY_III;

			case "Mantimayhem":
				return ColosseumModifier.MANTIMAYHEM_I;
			case "Mantimayhem (II)":
				return ColosseumModifier.MANTIMAYHEM_II;
			case "Mantimayhem (III)":
				return ColosseumModifier.MANTIMAYHEM_III;

			case "Myopia":
				return ColosseumModifier.MYOPIA_I;
			case "Myopia (II)":
				return ColosseumModifier.MYOPIA_II;
			case "Myopia (III)":
				return ColosseumModifier.MYOPIA_III;

			case "Reentry":
				return ColosseumModifier.REENTRY_I;
			case "Reentry (II)":
				return ColosseumModifier.REENTRY_II;
			case "Reentry (III)":
				return ColosseumModifier.REENTRY_III;

			case "Relentless":
				return ColosseumModifier.RELENTLESS_I;
			case "Relentless (II)":
				return ColosseumModifier.RELENTLESS_II;
			case "Relentless (III)":
				return ColosseumModifier.RELENTLESS_III;

			case "Solarflare":
				return ColosseumModifier.SOLARFLARE_I;
			case "Solarflare (II)":
				return ColosseumModifier.SOLARFLARE_II;
			case "Solarflare (III)":
				return ColosseumModifier.SOLARFLARE_III;

			case "Volatility":
				return ColosseumModifier.VOLATILITY_I;
			case "Volatility (II)":
				return ColosseumModifier.VOLATILITY_II;
			case "Volatility (III)":
				return ColosseumModifier.VOLATILITY_III;

			case "Dynamic Duo":
				return ColosseumModifier.DYNAMIC_DUO;
			case "Quartet":
				return ColosseumModifier.QUARTET;
			case "Red Flag":
				return ColosseumModifier.RED_FLAG;
			case "Totemic":
				return ColosseumModifier.TOTEMIC;

			default:
				throw new UnsupportedOperationException("Unknown modifier: " + text);
		}
	}

	/**
	 * Handler for scanning rewards chest UI / wave intermission UI
	 */
	public IntermissionUI scanUI(boolean rewardsChestUI, int currentWave)
	{
		return rewardsChestUI ? scanRewardsChestUI(currentWave) : scanIntermissionUI(currentWave);
	}

	/**
	 * Extract relevant data from the UI that appears in-between waves.
	 */
	private IntermissionUI scanIntermissionUI(int currentWave)
	{
		log.debug("Scanning intermission UI for wave {}", currentWave);

		Widget modContainer = client.getWidget(INTERMISSION_GROUP_ID, INTERMISSION_MODIFIER_LIST_CONTAINER);
		List<ColosseumModifier> activeModifiers = parseActiveModifiers(modContainer);

		Widget lootContainer = client.getWidget(INTERMISSION_GROUP_ID, INTERMISSION_NEXT_LOOT_CONTAINER);
		List<ItemBundle> nextLoot = parseNextLoot(lootContainer);

		SummaryUI summary = parseSummaryUI(client.getWidget(INTERMISSION_GROUP_ID, INTERMISSION_RESULT_CONTAINER));

		List<ColosseumModifier> modifierChoices = new ArrayList<>();
		for (int childId : INTERMISSION_MODIFIER_CHOICES)
		{
			Widget container = client.getWidget(INTERMISSION_GROUP_ID, childId);
			if (container != null)
			{
				String rawText = Text.removeTags(container.getName());
				ColosseumModifier mod = matchModifier(rawText);
				if (mod != null)
				{
					modifierChoices.add(mod);
					log.debug("Parsed Modifier choice: {}", rawText);
				}
				else
					log.error("Parsed unknown modifier choice: {}", rawText);
			}
		}

		return IntermissionUI.builder()
			.potentialLoot(nextLoot)
			.modifierChoices(modifierChoices)
			.activeModifiers(activeModifiers)
			.damageTakenGlory(summary == null ? 0 : summary.getDamageTakenGlory())
			.speedBonusGlory(summary == null ? 0 : summary.getSpeedBonusGlory())
			.speedBonusTimeSeconds(summary == null ? -1 : summary.getSpeedBonusTimeSeconds())
			.damageTakenAmount(summary == null ? -1 : summary.getDamageTakenAmount())
			.waveBonusGlory(summary == null ? 0 : summary.getWaveBonusGlory())
			.waveGlory(summary == null ? 0 : summary.getWaveGlory())
			.totalGlory(summary == null ? 0 : summary.getTotalGlory())
			.totalTimeSeconds(summary == null ? 0 : summary.getTotalTimeSeconds())
			.modChoiceGlory(summary == null ? 0 : summary.getModChoiceGlory())
			.build();
	}

	/**
	 * Extract relevant data from the rewards chest UI and return them
	 */
	private IntermissionUI scanRewardsChestUI(int currentWave)
	{
		log.debug("Scanning rewards chest UI for wave {}", currentWave);
		Widget lootContainer = client.getWidget(REWARDS_CHEST_GROUP_ID, REWARDS_CHEST_REWARDS_TAB_CHILD_ID);
		List<ItemBundle> allLoot = parseNextLoot(lootContainer);
		if (!allLoot.isEmpty())
		{
			log.debug("Scanned {} items from rewards chest:", allLoot.size());
			int i = 0;
			for (ItemBundle loot : allLoot)
			{
				i++;
				log.debug("[{}] item_id={} item_name={} quantity={}", i, loot.getItemId(), loot.getItemName(), loot.getQuantity());
			}
		}
		else
			log.error("Failed to scan nextLoot");

		Widget stats = client.getWidget(REWARDS_CHEST_GROUP_ID, REWARDS_CHEST_SUMMARY_TAB_CHILD_ID);
		SummaryUI ui = parseSummaryUI(stats);

		Widget modContainer = client.getWidget(REWARDS_CHEST_GROUP_ID, REWARDS_CHEST_MODIFIER_LIST_CONTAINER_ID);
		List<ColosseumModifier> activeModifiers = parseActiveModifiers(modContainer);

		return IntermissionUI.builder()
			.activeModifiers(activeModifiers)
			.potentialLoot(allLoot)
			.damageTakenGlory(ui == null ? 0 : ui.getDamageTakenGlory())
			.speedBonusGlory(ui == null ? 0 : ui.getSpeedBonusGlory())
			.speedBonusTimeSeconds(ui == null ? -1 : ui.getSpeedBonusTimeSeconds())
			.damageTakenAmount(ui == null ? -1 : ui.getDamageTakenAmount())
			.waveBonusGlory(ui == null ? 0 : ui.getWaveBonusGlory())
			.waveGlory(ui == null ? 0 : ui.getWaveGlory())
			.totalGlory(ui == null ? 0 : ui.getTotalGlory())
			.totalTimeSeconds(ui == null ? 0 : ui.getTotalTimeSeconds())
			.modChoiceGlory(ui == null ? 0 : ui.getModChoiceGlory())
			.build();
	}

	/**
	 * Generate an NPC that can be inserted into the timeline from the given data.
	 *
	 * @param npc            NPC instance of the NPC
	 * @param npcComposition NPCComposition instance of the NPC
	 * @return Subset of NPC data that can be added to the NPC list of the scanned state
	 */
	private ColosseumNPC generateNpc(NPC npc, NPCComposition npcComposition)
	{
		int id = (npcComposition != null) ? npcComposition.getId() : npc.getId();

		final String name;
		if (id == BOSS_WAVE_BEAM_CRYSTAL)
			name = BOSS_WAVE_BEAM_CRYSTAL_NPC_NAME;
		else if (id == SOLAR_FLARE_NPC_ID)
			name = SOLARFLARE_NPC_NAME;
		else if (npcComposition != null)
			name = npcComposition.getName();
		else if (npc.getName() != null)
			name = npc.getName();
		else
			name = "Unknown";

		int[] stats = npcComposition == null ? new int[0] : npcComposition.getStats();
		int ratio = npc.getHealthRatio();
		int scale = npc.getHealthScale();

		int maxHp = npcComposition == null ? -1 : stats[3];
		final int currentHp;
		if (maxHp != -1 && ratio >= 0 && scale > 0)
			currentHp = (int) Math.ceil((double) ratio * maxHp / scale);
		else
			currentHp = maxHp;

		WorldPoint location = npc.getWorldLocation();
		npc.getHealthScale();

		ColosseumNPC.ColosseumNPCBuilder builder = ColosseumNPC.builder()
			.npcId(id)
			.name(Text.removeTags(name))
			.x(location.getRegionX())
			.y(location.getRegionY())
			.hp(currentHp)
			.maxHp(maxHp);
		if (id == MANTICORE_NPC_ID) {
			int npcIndex = npc.getIndex();
			String sequence = manticoreSequences.containsKey(npcIndex)
				? manticoreSequenceStrings.get(npcIndex)
				: "Unknown";

			builder.orbSequence(sequence);
		}
		return builder.build();
	}

	private static final DateTimeFormatter HHMMSS_FORMATTER = DateTimeFormatter
		.ofPattern("HH:mm:ss.S")
		.withZone(ZoneId.systemDefault());


	/**
	 * Scan the State on a particular tick and return it. The contents of the resulting State is dictated by
	 * user-defined configurations
	 */
	public ColosseumState scanCurrentState(int currentWave, int waveStart)
	{
		Player player = client.getLocalPlayer();
		WorldPoint playerLocation = (player != null) ? player.getWorldLocation() : null;

		ArrayList<ColosseumNPC> detectedNpcs = new ArrayList<>();

		for (NPC npc : client.getTopLevelWorldView().npcs())
		{
			if (npc == null || npc.isDead())
				continue;

			NPCComposition composition = npc.getComposition();
			int npcId = npc.getId();
			if (composition != null)
				npcId = composition.getId();

			if (trackedNpcIds.contains(npcId)) {
				ColosseumNPC colosseumNpc = generateNpc(npc, composition);
				detectedNpcs.add(colosseumNpc);
			}
		}
		int playerHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
		int playerPrayer = client.getBoostedSkillLevel(Skill.PRAYER);

		ColosseumState.ColosseumStateBuilder state = ColosseumState.builder()
			.wave(currentWave)
			.tick(client.getTickCount() - waveStart)
			.playerHp(playerHp)
			.playerPrayer(playerPrayer)
			.playerLocation(playerLocation)
			.npcs(detectedNpcs);

		switch (timestampFormat)
		{
			case UNIX:
				state.timestampUnix(Instant.now().toEpochMilli());
			case HHMMSS_M:
				state.timestampHmsm(HHMMSS_FORMATTER.format(Instant.now()));
			case BOTH:
				state.timestampUnix(Instant.now().toEpochMilli());
				state.timestampHmsm(HHMMSS_FORMATTER.format(Instant.now()));
		}
		return state.build();
	}

	/**
	 * Parse the wave results UI described by stats and return the interpreted values
	 */
	private SummaryUI parseSummaryUI(Widget stats)
	{
		if (stats == null) return null;

		int waveBonusGlory = parseNum(clean(stats, 1));

		String speedText = clean(stats, 3);
		int speedBonusGlory = parseNum(speedText.split("\\(")[0]);
		int speedBonusTimeSeconds = speedText.contains("(") ?
			parseTime(speedText.substring(speedText.indexOf("(") + 1, speedText.indexOf(")"))) : 0;

		int modChoiceGlory = parseNum(clean(stats, 5));

		String dmgText = clean(stats, 7);
		int damageTakenGlory = parseNum(dmgText.split("\\(")[0]);
		int damageTakenAmount = dmgText.contains("(") ?
			parseNum(dmgText.substring(dmgText.indexOf("(") + 1, dmgText.indexOf(")"))) : 0;

		SummaryUI result = SummaryUI.builder()
			.waveBonusGlory(waveBonusGlory)
			.speedBonusGlory(speedBonusGlory)
			.speedBonusTimeSeconds(speedBonusTimeSeconds)
			.modChoiceGlory(modChoiceGlory)
			.damageTakenGlory(damageTakenGlory)
			.damageTakenAmount(damageTakenAmount)
			.waveGlory(waveBonusGlory + modChoiceGlory + damageTakenGlory + speedBonusGlory)
			.totalGlory(parseNum(clean(stats, 10)))
			.totalTimeSeconds(parseTime(clean(stats, 11)))
			.build();

		if (result != null)
		{
			log.info("Parsed the Results from intermissionUI: {}", result);
		}
		else {
			log.info("Failed to parse results from intermission UI");
		}

		return result;
	}

	private String clean(Widget parent, int childId)
	{
		Widget w = parent.getChild(childId);
		return (w != null) ? Text.removeTags(w.getText()) : "";
	}

	private static int parseNum(String s)
	{
		String val = s.replaceAll("[^0-9-]", "");
		return val.isEmpty() ? 0 : Integer.parseInt(val);
	}

	private static int parseTime(String s)
	{
		if (!s.contains(":")) return 0;
		String[] parts = s.split(":");
		return (Integer.parseInt(parts[0].trim()) * 60) + Integer.parseInt(parts[1].trim());
	}

	/**
	 * Iterate over lootContainer elements, attempt to extract a quantity of items from each element and return the extracted data as a List.
	 */
	private List<ItemBundle> parseNextLoot(Widget lootContainer)
	{
		List<ItemBundle> nextLoot = new ArrayList<>();
		if (lootContainer != null && lootContainer.getChild(0) != null)
		{
			for (Widget child : lootContainer.getDynamicChildren())
			{
				int id = child.getItemId();
				int quantity = child.getItemQuantity();
				ItemBundle nextItem = ItemBundle.fromComp(itemManager.getItemComposition(id), quantity);

				if (enabledSwapQuiverLoot && nextItem.getItemId() == DIZANAS_QUIVER_UNCHARGED_ID)
					nextLoot.add(SWAPPED_DIZANAS_QUIVER_ITEM_BUNDLE);
				else
					nextLoot.add(nextItem);
			}
		}
		return nextLoot;
	}

	/**
	 * Extract and return the active modifiers listed in the given modifierContainer
	 */
	private List<ColosseumModifier> parseActiveModifiers(Widget modifierContainer)
	{
		List<ColosseumModifier> parsedModifiers = new ArrayList<>();
		if (modifierContainer != null && modifierContainer.getDynamicChildren() != null)
		{
			for (Widget child : modifierContainer.getDynamicChildren())
			{
				String txt = Text.removeTags(child.getText()).trim();
				if (!txt.isEmpty() && !txt.equals("You have no modifiers yet."))
				{
					ColosseumModifier mod = matchModifier(txt);
					if (mod != null) parsedModifiers.add(mod);
				}
			}
		}
		return parsedModifiers;
	}

	/**
	 * Update flags that are derived from (combinations of) configurations.
	 * @param startUp If true, initializes certain variables during startup
	 */
	public void updateConfigFlags(boolean startUp)
	{
		enabledSwapQuiverLoot = config.logQuiverAsSplinters();
		log.info("enabledSwapQuiverLoot is set to {}", enabledSwapQuiverLoot);
		timestampFormat = config.logTimestamp();
		log.info("timestampFormat is set to {}", timestampFormat.name());

		if (startUp)
		{
			OPTIONAL_NPC_SUPPLIERS = ImmutableMap.<BooleanSupplier, Collection<Integer>>builder()
				.put(config::logBeeSwarm, Collections.singletonList(BEE_SWARM_NPC_ID))
				.put(config::logSolarFlare, Collections.singletonList(SOLAR_FLARE_NPC_ID))
				.put(config::logHealingTotem, Collections.singletonList(HEALING_TOTEM_NPC_ID))
				.put(config::logBeamCrystal, Collections.singletonList(BOSS_WAVE_BEAM_CRYSTAL))
				.put(config::logFremenniks, Arrays.asList(
					FREMENNIK_MAGE_NPC_ID,
					FREMENNIK_MELEE_NPC_ID,
					FREMENNIK_RANGED_NPC_ID
				))
				.build();
		}
		trackedNpcIds.clear();
		trackedNpcIds.addAll(CORE_COLOSSEUM_MOBS);

		for (Map.Entry<BooleanSupplier, Collection<Integer>> entry : OPTIONAL_NPC_SUPPLIERS.entrySet())
		{
			if (entry.getKey().getAsBoolean())
			{
				trackedNpcIds.addAll(entry.getValue());
			}
		}
	}
}