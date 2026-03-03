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
import static com.datalogger.constants.Colosseum.Item.SUNFIRE_SPLINTERS_ID;
import static com.datalogger.constants.Colosseum.NPC.BEE_SWARM_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.BOSS_WAVE_BEAM_CRYSTAL;
import static com.datalogger.constants.Colosseum.NPC.FREMENNIK_MAGE_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.FREMENNIK_MELEE_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.FREMENNIK_RANGED_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.HEALING_TOTEM_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.MINIMUS_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.SITTING_SOL_HEREDIT_NPC_ID;
import static com.datalogger.constants.Colosseum.NPC.SOLAR_FLARE_NPC_ID;
import static com.datalogger.constants.Colosseum.RewardsChest.REWARDS_CHEST_GROUP_ID;
import static com.datalogger.constants.Colosseum.RewardsChest.REWARDS_CHEST_MODIFIER_LIST_CONTAINER_ID;
import static com.datalogger.constants.Colosseum.RewardsChest.REWARDS_CHEST_REWARDS_TAB_CHILD_ID;
import static com.datalogger.constants.Colosseum.RewardsChest.REWARDS_CHEST_SUMMARY_TAB_CHILD_ID;
import com.datalogger.models.colosseum.ColosseumState;
import com.datalogger.models.colosseum.IntermissionUI;
import com.datalogger.models.colosseum.enums.ColosseumModifier;
import com.datalogger.models.common.ItemBundle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class ColosseumScanner {

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private DataLoggerConfig config;

	private final ItemBundle swappedDizanasQuiver = new ItemBundle(SUNFIRE_SPLINTERS_ID, "Sunfire splinters", 4000);

	/**
	 * Match a ColosseumModifier instance to the given String and return it
	 */
	private ColosseumModifier matchModifier(String text) {
		switch (text) {
			case "Bees!": return ColosseumModifier.BEES_I;
			case "Bees! (II)": return ColosseumModifier.BEES_II;
			case "Bees! (III)": return ColosseumModifier.BEES_III;

			case "Blasphemy": return ColosseumModifier.BLASPHEMY_I;
			case "Blasphemy (II)": return ColosseumModifier.BLASPHEMY_II;
			case "Blasphemy (III)": return ColosseumModifier.BLASPHEMY_III;

			case "Doom": return ColosseumModifier.DOOM_I;
			case "Doom (II)": return ColosseumModifier.DOOM_II;
			case "Doom (III)": return ColosseumModifier.DOOM_III;

			case "Frailty": return ColosseumModifier.FRAILTY_I;
			case "Frailty (II)": return ColosseumModifier.FRAILTY_II;
			case "Frailty (III)": return ColosseumModifier.FRAILTY_III;

			case "Mantimayhem": return ColosseumModifier.MANTIMAYHEM_I;
			case "Mantimayhem (II)": return ColosseumModifier.MANTIMAYHEM_II;
			case "Mantimayhem (III)": return ColosseumModifier.MANTIMAYHEM_III;

			case "Myopia": return ColosseumModifier.MYOPIA_I;
			case "Myopia (II)": return ColosseumModifier.MYOPIA_II;
			case "Myopia (III)": return ColosseumModifier.MYOPIA_III;

			case "Reentry": return ColosseumModifier.REENTRY_I;
			case "Reentry (II)": return ColosseumModifier.REENTRY_II;
			case "Reentry (III)": return ColosseumModifier.REENTRY_III;

			case "Relentless": return ColosseumModifier.RELENTLESS_I;
			case "Relentless (II)": return ColosseumModifier.RELENTLESS_II;
			case "Relentless (III)": return ColosseumModifier.RELENTLESS_III;

			case "Solarflare": return ColosseumModifier.SOLARFLARE_I;
			case "Solarflare (II)": return ColosseumModifier.SOLARFLARE_II;
			case "Solarflare (III)": return ColosseumModifier.SOLARFLARE_III;

			case "Volatility": return ColosseumModifier.VOLATILITY_I;
			case "Volatility (II)": return ColosseumModifier.VOLATILITY_II;
			case "Volatility (III)": return ColosseumModifier.VOLATILITY_III;

			case "Dynamic Duo": return ColosseumModifier.DYNAMIC_DUO;
			case "Quartet": return ColosseumModifier.QUARTET;
			case "Red Flag": return ColosseumModifier.RED_FLAG;
			case "Totemic": return ColosseumModifier.TOTEMIC;

			default:
				throw new UnsupportedOperationException("Unknown modifier: " + text);
		}
	}

	/**
	 * Handler for scanning rewards chest UI / wave intermission UI
	 */
	public IntermissionUI scanUI(boolean rewardsChestUI)
	{
		return rewardsChestUI ? scanRewardsChestUI() : scanIntermissionUI();
	}

	/**
	 * Extract all values from the wave intermission UI.
	 */
	private IntermissionUI scanIntermissionUI() {

		log.debug("Scanning intermission UI");
		int speedBonusTimeSeconds = -1;
		int damageTakenAmount = -1;

		Widget modContainer = client.getWidget(INTERMISSION_GROUP_ID, INTERMISSION_MODIFIER_LIST_CONTAINER);
		List<ColosseumModifier> activeModifiers = parseActiveModifiers(modContainer);

		Widget lootContainer = client.getWidget(INTERMISSION_GROUP_ID, INTERMISSION_NEXT_LOOT_CONTAINER);
		List<ItemBundle> nextLoot = parseNextLoot(lootContainer);

		Widget stats = client.getWidget(INTERMISSION_GROUP_ID, INTERMISSION_RESULT_CONTAINER);
		if (stats == null) {return null;}
		Function<Integer, String> clean = (id) -> {
			Widget w = stats.getChild(id);
			return w != null ? Text.removeTags(w.getText()) : "";
		};

		Function<String, Integer> parseNum = (s) -> {
			String val = s.replaceAll("[^0-9-]", "");
			return val.isEmpty() ? 0 : Integer.parseInt(val);
		};

		Function<String, Integer> parseTime = (s) -> {
			if (!s.contains(":")) return 0;
			String[] parts = s.split(":");
			return (Integer.parseInt(parts[0].trim()) * 60) + Integer.parseInt(parts[1].trim());
		};

		int waveBonusGlory = parseNum.apply(clean.apply(1));

		String speedText = clean.apply(3);
		int speedBonusGlory = parseNum.apply(speedText.split("\\(")[0]);
		if (speedText.contains("(")) {
			speedBonusTimeSeconds = parseTime.apply(speedText.substring(speedText.indexOf("(") + 1, speedText.indexOf(")")));
		}

		int modChoiceGlory = parseNum.apply(clean.apply(5));

		String dmgText = clean.apply(7);
		int damageTakenGlory = parseNum.apply(dmgText.split("\\(")[0]);
		if (dmgText.contains("(")) {
			damageTakenAmount = parseNum.apply(dmgText.substring(dmgText.indexOf("(") + 1, dmgText.indexOf(")")));
		}

		int totalGlory = parseNum.apply(clean.apply(10));
		int totalTimeSeconds = parseTime.apply(clean.apply(11));

		List<ColosseumModifier> modifierChoices = new ArrayList<>();
		for (int childId : INTERMISSION_MODIFIER_CHOICES) {
			Widget container = client.getWidget(INTERMISSION_GROUP_ID, childId);
			if (container != null) {
				String rawText = Text.removeTags(container.getName());
				ColosseumModifier mod = matchModifier(rawText);
				if (mod != null) {
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
			.waveBonusGlory(waveBonusGlory)
			.speedBonusGlory(speedBonusGlory)
			.speedBonusTimeSeconds(speedBonusTimeSeconds)
			.modChoiceGlory(modChoiceGlory)
			.damageTakenGlory(damageTakenGlory)
			.damageTakenAmount(damageTakenAmount)
			.totalGlory(totalGlory)
			.totalTimeSeconds(totalTimeSeconds)
			.activeModifiers(activeModifiers)
			.build();
	}

	/**
	 * Extract values from the rewards chest UI and return them
	 */
	private IntermissionUI scanRewardsChestUI() {
		log.debug("Scanning rewards chest UI");

		int speedBonusTimeSeconds = -1;
		int damageTakenAmount = -1;

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
		if (stats == null) {return null;}
		Function<Integer, String> clean = (id) -> {
			Widget w = stats.getChild(id);
			return w != null ? Text.removeTags(w.getText()) : "";
		};

		Function<String, Integer> parseNum = (s) -> {
			String val = s.replaceAll("[^0-9-]", "");
			return val.isEmpty() ? 0 : Integer.parseInt(val);
		};

		Function<String, Integer> parseTime = (s) -> {
			if (!s.contains(":")) return 0;
			String[] parts = s.split(":");
			return (Integer.parseInt(parts[0].trim()) * 60) + Integer.parseInt(parts[1].trim());
		};

		int waveBonusGlory = parseNum.apply(clean.apply(1));

		String speedText = clean.apply(3);
		int speedBonusGlory = parseNum.apply(speedText.split("\\(")[0]);
		if (speedText.contains("(")) {
			speedBonusTimeSeconds = parseTime.apply(speedText.substring(speedText.indexOf("(") + 1, speedText.indexOf(")")));
		}

		int modChoiceGlory = parseNum.apply(clean.apply(5));

		String dmgText = clean.apply(7);
		int damageTakenGlory = parseNum.apply(dmgText.split("\\(")[0]);
		if (dmgText.contains("(")) {
			damageTakenAmount = parseNum.apply(dmgText.substring(dmgText.indexOf("(") + 1, dmgText.indexOf(")")));
		}

		int totalGlory = parseNum.apply(clean.apply(10));
		int totalTimeSeconds = parseTime.apply(clean.apply(11));

		Widget modContainer = client.getWidget(REWARDS_CHEST_GROUP_ID, REWARDS_CHEST_MODIFIER_LIST_CONTAINER_ID);
		List<ColosseumModifier> activeModifiers = parseActiveModifiers(modContainer);

		return IntermissionUI.builder()
			.activeModifiers(activeModifiers)
			.potentialLoot(allLoot)
			.damageTakenGlory(damageTakenGlory)
			.speedBonusGlory(speedBonusGlory)
			.speedBonusTimeSeconds(speedBonusTimeSeconds)
			.damageTakenAmount(damageTakenAmount)
			.waveBonusGlory(waveBonusGlory)
			.totalGlory(totalGlory)
			.totalTimeSeconds(totalTimeSeconds)
			.modChoiceGlory(modChoiceGlory)
			.build();
	}

	/**
	 * Scans the immediate environment to capture the current Colosseum state. Executed every game tick during a wave.
	 */
	public ColosseumState scanCurrentState(int currentWave, int waveStart) {
		Player player = client.getLocalPlayer();
		WorldPoint playerLocation = (player != null) ? player.getWorldLocation() : null;

		ArrayList<NPC> detectedNpcs = new ArrayList<>();

		for (NPC npc : client.getTopLevelWorldView().npcs()) {
			if (npc == null || npc.isDead()) {
				continue;
			}

			if (isColosseumEnemy(npc)) {
				detectedNpcs.add(npc);
			}
		}

		return ColosseumState.builder()
			.wave(currentWave)
			.tick(client.getTickCount()-waveStart)
			.playerLocation(playerLocation)
			.npcs(detectedNpcs)
			.build();
	}

	/**
	 * Helper to filter out pets, thralls, or irrelevant NPCs.
	 */
	private boolean isColosseumEnemy(NPC npc) {
		if (npc == null) {return false;}

		int id = npc.getId();
		return id != -1 && id != SOLAR_FLARE_NPC_ID && id != SITTING_SOL_HEREDIT_NPC_ID && id != MINIMUS_NPC_ID && id != FREMENNIK_MAGE_NPC_ID && id != FREMENNIK_MELEE_NPC_ID && id != FREMENNIK_RANGED_NPC_ID && id != BOSS_WAVE_BEAM_CRYSTAL && id != BEE_SWARM_NPC_ID && id != HEALING_TOTEM_NPC_ID;
	}

	private List<ItemBundle> parseNextLoot(Widget lootContainer)
	{
		List<ItemBundle> nextLoot = new ArrayList<>();
		if (lootContainer != null && lootContainer.getChild(0) != null) {
			for (Widget child : lootContainer.getDynamicChildren()) {
				int id = child.getItemId();
				int quantity = child.getItemQuantity();
				ItemBundle nextItem = ItemBundle.fromComp(itemManager.getItemComposition(id), quantity);

				if (config.logQuiverAsSplinters() && nextItem.getItemId() == DIZANAS_QUIVER_UNCHARGED_ID)
					nextLoot.add(swappedDizanasQuiver);
				else
					nextLoot.add(nextItem);
			}
		}
		return nextLoot;
	}

	private List<ColosseumModifier> parseActiveModifiers(Widget modifierContainer)
	{
		List<ColosseumModifier> parsedModifiers = new ArrayList<>();
		if (modifierContainer != null && modifierContainer.getDynamicChildren() != null) {
			for (Widget child : modifierContainer.getDynamicChildren()) {
				String txt = Text.removeTags(child.getText()).trim();
				if (!txt.isEmpty() && !txt.equals("You have no modifiers yet.")) {
					ColosseumModifier mod = matchModifier(txt);
					if (mod != null) parsedModifiers.add(mod);
				}
			}
		}
		return parsedModifiers;
	}
}