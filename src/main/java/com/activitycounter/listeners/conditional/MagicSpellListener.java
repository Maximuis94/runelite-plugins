package com.activitycounter.listeners.conditional;

import com.activitycounter.ActivityCounterPlugin;
import static com.activitycounter.PluginConstants.EQUIPPED_ITEM_CONTAINER_ID;
import com.activitycounter.events.ExperienceDrop;
import com.activitycounter.listeners.AbstractListenerLeaf;
import com.activitycounter.listeners.core.EquippedItemListener;
import com.activitycounter.listeners.core.PlayerListener;
import com.activitycounter.listeners.core.StatChangeListener;
import com.activitycounter.models.EquipmentWorn;
import com.activitycounter.models.MagicSpell;
import com.activitycounter.models.RuneElement;
import com.activitycounter.models.RuneProvider;
import com.activitycounter.models.RuneStack;
import com.activitycounter.models.SpellType;
import com.activitycounter.models.TrackedActivity;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.Subscribe;

/**
 * Evaluates raw inventory reductions and authorizes them as consumed resources
 * only if they mathematically map to a valid spell cast.
 * Utilizes a 2FA approach: matching physical runes alongside reverse-engineered Base XP.
 */
@Slf4j
@Singleton
public class MagicSpellListener extends AbstractListenerLeaf
{
	@Inject
	private Client client;

	@Inject
	private ActivityCounterPlugin plugin;

	@Inject
	private StatChangeListener StatChangeListener;

	@Inject
	private PlayerListener playerListener;

	@Inject
	private EquippedItemListener equippedItemListener;

	// Hardware Cache
	private final boolean[] infiniteElements = new boolean[RuneElement.values().length];
	private boolean requiresEquipmentUpdate = false;

	// Spell Evaluation Queue
	private int[] pendingConsumedProviders = null;
	private int pendingRuneTick = -1;

	private int thisTick = -1;
	private int magicXpThisTick = 0;
	private int hpXpThisTick = 0;
	private int defenseXpThisTick = 0;
	private int baseMagicXp = 0;

	private boolean isInitialized = false;

	public MagicSpellListener() {}

	public void initializeState()
	{
		if (isInitialized) return;
		isInitialized = true;
		requiresEquipmentUpdate = true;
	}

	@Override
	public void onRegistered()
	{
		isInitialized = false;
		initializeState();
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Subscribe
	public void onExperienceDrop(ExperienceDrop event)
	{
		int tick = event.getTick();

		if (tick != thisTick)
		{
			thisTick = tick;
			magicXpThisTick = 0;
			hpXpThisTick = 0;
			defenseXpThisTick = 0;
			baseMagicXp = 0;
		}

		Skill skill = event.getSkill();

		switch (skill)
		{
			case MAGIC:
				magicXpThisTick += event.getXpGained();
				break;
			case HITPOINTS:
				hpXpThisTick += event.getXpGained();
				break;
			case DEFENCE:
				defenseXpThisTick += event.getXpGained();
				break;
			default:
				break;
		}

		baseMagicXp = computeBaseMagicExp();
	}

	private int computeBaseMagicExp()
	{
		if (magicXpThisTick == 0)
		{
			return 0;
		}

		float damageMultiplier = (defenseXpThisTick > 0) ? 1.0f : 1.5f;

		return Math.round(magicXpThisTick - (hpXpThisTick * damageMultiplier));
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == EQUIPPED_ITEM_CONTAINER_ID)
		{
			requiresEquipmentUpdate = true;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (requiresEquipmentUpdate)
		{
			Arrays.fill(infiniteElements, false);

			RuneProvider weapon = RuneProvider.getByItemId(equippedItemListener.getWeaponId());
			if (weapon != null && weapon.isInfinite())
			{
				for (RuneElement re : weapon.getProvidedElements())
				{
					infiniteElements[re.ordinal()] = true;
				}
			}

			RuneProvider offhand = RuneProvider.getByItemId(equippedItemListener.getOffhandId());
			if (offhand != null && offhand.isInfinite())
			{
				for (RuneElement re : offhand.getProvidedElements())
				{
					infiniteElements[re.ordinal()] = true;
				}
			}

			requiresEquipmentUpdate = false;
		}

		if (pendingConsumedProviders != null && pendingRuneTick == client.getTickCount()-1)
		{
			evaluateSpellCast();
			pendingConsumedProviders = null;
		}
	}

	/**
	 * Called directly by the InventoryListener whenever finite runes decrease.
	 * Defers actual evaluation to onGameTick so XP drops can be captured.
	 */
	public void processRuneDeltas(int[] consumedProviders)
	{
		if (playerListener.isTransferStateActive())
		{
			return;
		}

		this.pendingConsumedProviders = consumedProviders.clone();
		this.pendingRuneTick = client.getTickCount();
	}

	private void evaluateSpellCast()
	{
		int[] consumedElements = new int[RuneElement.values().length];
		int totalConsumedItems = 0;

		for (RuneProvider rp : RuneProvider.values())
		{
			if (rp.isInfinite()) continue;

			int amt = pendingConsumedProviders[rp.ordinal()];
			if (amt > 0)
			{
				totalConsumedItems += amt;
				for (RuneElement re : rp.getProvidedElements())
				{
					consumedElements[re.ordinal()] += amt;
				}
			}
		}

		if (totalConsumedItems == 0) return;

		boolean hasMagicXp = (thisTick == pendingRuneTick && magicXpThisTick > 0);

		int calculatedBaseXp = hasMagicXp ? baseMagicXp : -1;

		int effectiveMagicLevel = StatChangeListener.getEffectiveLevel(Skill.MAGIC);
		MagicSpell matchedSpell = null;

		MagicSpell[] activeSpells = MagicSpell.getSpellsForBook(playerListener.getActiveSpellBook());
		for (MagicSpell spell : activeSpells)
		{
			if (effectiveMagicLevel >= spell.getMagicLevel() && hasRequiredWeapon(spell) && spellMatches(spell, consumedElements))
			{
				if (hasMagicXp)
				{
					// Fuzzy match within 2 XP to absorb multi-target integer rounding errors
					if (Math.abs(calculatedBaseXp - spell.getExperience()) <= 2.0f)
					{
						matchedSpell = spell;
						break;
					}
					else if (log.isDebugEnabled())
					{
						log.debug("Spell elements matched {}, but Base XP failed. Expected: {}, Calculated: {}",
							spell.name(), spell.getExperience(), calculatedBaseXp);
					}
				}
				else
				{
					// Fallback for players with 200m XP or zero-XP utility spells
					log.debug("Matched {} via strict element signature (No Magic XP registered this tick)", spell.name());
					matchedSpell = spell;
					break;
				}
			}
		}

		if (matchedSpell != null && activeSession != null)
		{
			for (RuneProvider rp : RuneProvider.values())
			{
				int amt = pendingConsumedProviders[rp.ordinal()];
				if (amt > 0 && rp.getTrackedActivity() != null)
				{
					activeSession.processActivityUpdate(rp.getTrackedActivity(), amt);
				}
			}

			if (matchedSpell.getActivity() != null)
			{
				activeSession.processActivityUpdate(matchedSpell.getActivity(), 1);
			}

			if (equippedItemListener.getWeapon() == EquipmentWorn.WILDY_SCEPTRE && matchedSpell.getSpellType() == SpellType.COMBAT)
			{
				activeSession.processActivityUpdate(TrackedActivity.REVENANT_ETHER, 1);
			}

			plugin.setRequiresSaveAndRefresh(true);
		}
	}

	/**
	 * Verifies if the elements lost perfectly match the spell's cost after applying infinite sources.
	 */
	private boolean spellMatches(MagicSpell spell, int[] consumedElements)
	{
		if (spell.getSackConsumed() != null && playerListener.isInWilderness())
		{
			int sackId = spell.getSackConsumed().ordinal();
			if (consumedElements[sackId] == 1)
			{
				for (int i = 0; i < consumedElements.length; i++)
				{
					if (i != sackId && consumedElements[i] > 0) return false;
				}
				return true;
			}
		}

		int[] expectedElements = new int[RuneElement.values().length];
		if (spell.getRunesConsumed() != null)
		{
			for (RuneStack stack : spell.getRunesConsumed())
			{
				if (!infiniteElements[stack.getElement().ordinal()])
				{
					expectedElements[stack.getElement().ordinal()] += stack.getQuantity();
				}
			}
		}

		for (int i = 0; i < consumedElements.length; i++)
		{
			if (consumedElements[i] != expectedElements[i])
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks if the currently equipped weapon satisfies the spell's strict weapon requirement.
	 */
	private boolean hasRequiredWeapon(MagicSpell spell)
	{
		int[] requiredWeapons = spell.getRequiredWeapons();

		// If the spell has no specific weapon requirements, it is always authorized
		if (requiredWeapons == null || requiredWeapons.length == 0)
		{
			return true;
		}

		int currentWeaponId = equippedItemListener.getWeaponId();
		for (int validWeaponId : requiredWeapons)
		{
			if (currentWeaponId == validWeaponId)
			{
				return true;
			}
		}

		return false;
	}
}