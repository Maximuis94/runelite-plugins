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

package com.activitycounter.listeners.conditional;

import com.activitycounter.PluginConstants;
import static com.activitycounter.PluginConstants.CONFIG_KEY_ACTIVITY_GROUP_HERBLORE;
import com.activitycounter.listeners.AbstractListenerLeaf;
import com.activitycounter.listeners.PreprocessedChatListener;
import static com.activitycounter.listeners.core.ChatMessageListener.ALCHEMISTS_AMULET_ITEM_ID;
import static com.activitycounter.listeners.core.ChatMessageListener.AMULET_OF_CHEMISTRY_ITEM_ID;
import com.activitycounter.listeners.core.EquippedItemListener;
import com.activitycounter.listeners.core.ExperienceDropListener;
import com.activitycounter.listeners.core.InventoryListener;
import com.activitycounter.models.BaseRecipe;
import com.activitycounter.models.Potion;
import com.activitycounter.models.RegularRecipe;
import com.activitycounter.models.TrackedActivity;
import com.activitycounter.models.UpgradeRecipe;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.eventbus.Subscribe;

/**
 * Tracks herblore activities: potions mixed and secondary ingredients saved by the prescription goggles, including
 * the batches that draw secondaries from the reagent pouch.
 */
@Slf4j
@Singleton
public class HerbloreListener extends AbstractListenerLeaf implements PreprocessedChatListener
{
	@Inject
	private InventoryListener inventoryListener;

	@Inject
	private ExperienceDropListener experienceDropListener;

	@Inject
	private EquippedItemListener equippedItemListener;

	private static final String ALCHEMIST_AMULET_PROC_PREFIX = "Your Alchemist's amulet helps you create";
	private static final String CHEMISTRY_AMULET_PROC_PREFIX = "Your amulet of chemistry helps you create";

	private static final String POTIONS_MIXED_PREFIX = "You mix";
	private static final String POST_FILL_POUCH_MESSAGE = "You add the items to your pouch.";
	private static final int BOTTOM_RIGHT_INVENTORY_SLOT_INDEX = 27;
	private static final int MAX_INGREDIENT_IN_INVENTORY = 27;

	private static final TrackedActivity[] MANAGED_ACTIVITIES = new TrackedActivity[]{
		TrackedActivity.POTIONS_MIXED,
		TrackedActivity.PRESCRIPTION_GOGGLE_PROCS,
		TrackedActivity.ALCHEMISTS_AMULET_PROCS
	};

	// Micro-batch state tracking
	private int pendingMixes = 0;
	private int waitTicks = 0;

	// Reagent pouch fill detection
	private boolean pouchFillPending = false;

	@Override
	protected String getActivityGroupConfigKey()
	{
		return CONFIG_KEY_ACTIVITY_GROUP_HERBLORE;
	}

	@Override
	protected TrackedActivity[] getManagedActivities()
	{
		return MANAGED_ACTIVITIES;
	}

	@Override
	public boolean isEnabled()
	{
		return config.enableHerbloreListener();
	}

	@Override
	public boolean onPreprocessedChatMessage(String message, ChatMessageType type, int regionId)
	{
		if (message.startsWith(POTIONS_MIXED_PREFIX))
		{
			if (!experienceDropListener.hasSkillGainedExpLastTick(Skill.HERBLORE)) return false;

			plugin.increaseCountByOne(TrackedActivity.POTIONS_MIXED.getId());
			pendingMixes++;
			return true;
		}

		int amuletId = equippedItemListener.getNeckId();
		if ((amuletId == ALCHEMISTS_AMULET_ITEM_ID && message.startsWith(ALCHEMIST_AMULET_PROC_PREFIX)) ||
			(amuletId == AMULET_OF_CHEMISTRY_ITEM_ID && message.startsWith(CHEMISTRY_AMULET_PROC_PREFIX)))
		{
			if (!experienceDropListener.hasSkillGainedExpLastTick(Skill.HERBLORE)) return false;

			plugin.increaseCountByOne(TrackedActivity.ALCHEMISTS_AMULET_PROCS.getId());
			return true;
		}

		if (hasReagentPouch() && hasPrescriptionGoggles() && message.startsWith(POST_FILL_POUCH_MESSAGE))
		{
			pouchFillPending = true;
			return true;
		}
		return false;
	}

	/**
	 * Returns true if there is a Reagent pouch in the inventory
	 */
	private boolean hasReagentPouch()
	{
		return inventoryListener.getItemCount(ItemID.MM_SECONDARY_POUCH) > 0 ||
			inventoryListener.getItemCount(ItemID.MM_SECONDARY_POUCH_OPEN) > 0;
	}

	/**
	 * Returns true if the prescription goggles are worn in the head slot
	 */
	private boolean hasPrescriptionGoggles()
	{
		int headSlotEquipItemId = equippedItemListener.getHeadId();
		return headSlotEquipItemId == ItemID.MM_ALCHEMIST_HAT || headSlotEquipItemId == ItemID.MM_ALCHEMIST_HAT_ALT;
	}

	@Subscribe(priority = -1)
	public void onGameTick(GameTick event)
	{
		if (pouchFillPending)
		{
			pouchFillPending = false;

			// Only a fill right after a mixing batch has leftover secondaries to count; the goggles must have been
			// worn while mixing, so require herblore experience on the previous tick
			if (experienceDropListener.getSkillsGainedLastTick().contains(Skill.HERBLORE))
			{
				trackPouchFill();
			}
		}

		if (pendingMixes <= 0)
		{
			waitTicks = 0;
			return;
		}

		Map<Integer, Integer> consumed = new HashMap<>();
		Map<Integer, Integer> produced = new HashMap<>();
		int deltaSum = snapshotDeltas(consumed, produced);

		if (!produced.isEmpty())
		{
			boolean pouchPresent = hasReagentPouch();
			MixMatch match;
			while (pendingMixes > 0 && (match = findMatchingRecipe(consumed, produced)) != null)
			{
				deltaSum = applyMatch(match, consumed, produced, deltaSum, pouchPresent);
				pendingMixes--;
			}
		}

		if (pendingMixes > 0)
		{
			waitTicks++;
			if (waitTicks > 2)
			{
				// Failsafe: drop unresolvable mixes to prevent desyncs
				pendingMixes = 0;
				waitTicks = 0;
			}
		}
		else
		{
			waitTicks = 0;
		}
	}

	/**
	 * Collects this tick's inventory deltas. Negative deltas are stored as consumed amounts, positive deltas as
	 * produced amounts. Returns the sum of all inventory deltas of this tick.
	 */
	private int snapshotDeltas(Map<Integer, Integer> consumed, Map<Integer, Integer> produced)
	{
		int deltaSum = 0;
		for (int itemId = 0; itemId < PluginConstants.MAX_ITEM_ID; itemId++)
		{
			int delta = inventoryListener.getItemDelta(itemId);
			deltaSum += delta;
			if (delta < 0)
			{
				consumed.put(itemId, -delta);
			}
			else if (delta > 0)
			{
				produced.put(itemId, delta);
			}
		}
		return deltaSum;
	}

	/**
	 * Counts the prescription goggle procs of a finished mixing batch. While mixing near a bank the pouch supplies
	 * every secondary, so the saved secondaries accumulate inside it; on the fill that tops the pouch back up they
	 * surface in the inventory. Everything of the locked ingredient beyond the single unit that starts the make-x
	 * action is therefore a proc, and with a full inventory-wide stack none were saved.
	 */
	private void trackPouchFill()
	{
		ItemContainer itemContainer = client.getItemContainer(PluginConstants.INVENTORY_ID);
		if (itemContainer == null) return;

		Item[] items = itemContainer.getItems();
		Item bottomRight = items[BOTTOM_RIGHT_INVENTORY_SLOT_INDEX];
		if (bottomRight == null) return;

		int ingredientCount = itemContainer.count(bottomRight.getId());
		if (ingredientCount >= MAX_INGREDIENT_IN_INVENTORY) return;

		// Everything beyond the locked unit that starts the make-x action was saved by the goggles
		int nProcs = ingredientCount - 1;
		if (nProcs <= 0) return;

		plugin.increaseCountByAmount(TrackedActivity.PRESCRIPTION_GOGGLE_PROCS.getId(), nProcs);
	}

	/**
	 * Finds the recipe whose produced item appeared this tick. Regular recipes are identified by the produced item
	 * alone: the base item or the ingredient may be absent from the consumed set when the goggles save them. Upgrade
	 * recipes are identified by the consumed base potion, which is always consumed, so that an amulet of chemistry
	 * proc (which produces the dose+1 variant of the product) is matched with the ingredient quantity of the base
	 * that was actually consumed instead of registering as a prescription goggle proc.
	 * Returns null if no known recipe matches the observed inventory changes.
	 */
	private MixMatch findMatchingRecipe(Map<Integer, Integer> consumed, Map<Integer, Integer> produced)
	{
		for (RegularRecipe recipe : RegularRecipe.values())
		{
			int producedItemId = recipe.getProducedItemId(Potion.MIN_DOSES);
			if (produced.containsKey(producedItemId))
			{
				return new MixMatch(recipe, producedItemId, Potion.MIN_DOSES);
			}

			// An amulet of chemistry proc creates the 4-dose variant instead of the regular product
			int amuletVariantId = getAmuletVariant(producedItemId);
			if (amuletVariantId != producedItemId && produced.containsKey(amuletVariantId))
			{
				return new MixMatch(recipe, amuletVariantId, Potion.MIN_DOSES);
			}
		}

		for (UpgradeRecipe recipe : UpgradeRecipe.values())
		{
			for (int baseItemId : recipe.getBaseItemIds())
			{
				if (!consumed.containsKey(baseItemId))
				{
					continue;
				}
				int baseDoses = recipe.getBasePotion().getDoses(baseItemId);
				int producedItemId = recipe.getProducedItemId(baseDoses);
				if (producedItemId > 0 && produced.containsKey(producedItemId))
				{
					return new MixMatch(recipe, producedItemId, baseDoses);
				}

				// An amulet of chemistry proc creates the dose+1 variant of the product instead
				int amuletVariantId = recipe.getProducedItemId(baseDoses + 1);
				if (amuletVariantId > 0 && produced.containsKey(amuletVariantId))
				{
					return new MixMatch(recipe, amuletVariantId, baseDoses);
				}
			}
		}
		return null;
	}

	/**
	 * Counts the items the prescription goggles saved on a matched mix. A mix consumes the base item and the
	 * recipe's ingredient quantity and produces a single item, so the inventory deltas of the involved items sum
	 * to -ingredientQuantity (e.g. -1 for an attack potion: -1 guam potion (unf), -1 eye of newt, +1 attack
	 * potion). Every saved item raises that sum by one (0 for an attack potion proc: the eye of newt is not
	 * consumed).
	 * Returns the delta sum with this mix's items accounted for, so additional mixes in the same tick can be
	 * attributed independently.
	 */
	private int applyMatch(MixMatch match, Map<Integer, Integer> consumed, Map<Integer, Integer> produced, int deltaSum, boolean pouchPresent)
	{
		BaseRecipe recipe = match.recipe;
		int ingredientQuantity = recipe.getIngredientQuantity(match.baseDoses);

		int saved = Math.max(0, deltaSum + ingredientQuantity);
		if (saved > 0)
		{
			// With a reagent pouch present a missing ingredient may have been drawn from the pouch rather than saved
			if (pouchPresent)
			{
				log.debug("Herblore: ignored {} saved item(s) for {}: reagent pouch present", saved, recipe);
			}
			else
			{
				log.debug("Herblore: {}: delta sum {} instead of {} -> {} item(s) saved",
					recipe, deltaSum, -ingredientQuantity, saved);
				plugin.increaseCountByAmount(TrackedActivity.PRESCRIPTION_GOGGLE_PROCS.getId(), saved);
			}
		}
		else
		{
			log.debug("Herblore: {}: delta sum {}, no items saved", recipe, deltaSum);
		}

		// Remove the produced item, the consumed base and the consumed ingredients from the delta maps
		decrement(produced, match.producedItemId);
		int accounted = 1;
		for (int baseItemId : recipe.getBaseItemIds())
		{
			if (consumed.containsKey(baseItemId))
			{
				decrement(consumed, baseItemId);
				accounted -= 1;
				break;
			}
		}
		int ingredientConsumed = Math.min(consumed.getOrDefault(recipe.getIngredientItemId(), 0), ingredientQuantity);
		decrement(consumed, recipe.getIngredientItemId(), ingredientConsumed);
		accounted -= ingredientConsumed;

		return deltaSum - accounted;
	}

	/**
	 * Returns the item ID an amulet of chemistry would create instead of the given product: the 4-dose variant of
	 * the same potion, or the given product itself if it is not a dose-able potion.
	 */
	private static int getAmuletVariant(int producedItemId)
	{
		Potion potion = Potion.getByItemId(producedItemId);
		return potion != null ? potion.getDoseItemId(Potion.MAX_DOSES) : producedItemId;
	}

	private static void decrement(Map<Integer, Integer> amounts, int itemId)
	{
		decrement(amounts, itemId, 1);
	}

	private static void decrement(Map<Integer, Integer> amounts, int itemId, int amount)
	{
		Integer count = amounts.get(itemId);
		if (count == null) return;
		if (count <= amount)
		{
			amounts.remove(itemId);
		}
		else
		{
			amounts.put(itemId, count - amount);
		}
	}

	/**
	 * A single mix attributed to a recipe: which product appeared, and the amount of doses of the consumed base
	 * (used to scale the ingredient quantity of upgrade recipes).
	 */
	private static final class MixMatch
	{
		private final BaseRecipe recipe;
		private final int producedItemId;
		private final int baseDoses;

		private MixMatch(BaseRecipe recipe, int producedItemId, int baseDoses)
		{
			this.recipe = recipe;
			this.producedItemId = producedItemId;
			this.baseDoses = baseDoses;
		}
	}
}
