package com.activitycounter.listeners.core;

import com.activitycounter.listeners.AbstractListenerNode;
import com.activitycounter.models.GameMode;
import com.activitycounter.models.SpellBook;
import com.activitycounter.models.TrackedActivity;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

/**
 * Centralized listener that actively tracks player status variables and interface states.
 * Provides O(1) lookups for the player's condition (e.g., active spellbook, wilderness status).
 */
@Slf4j
@Singleton
public class PlayerListener extends AbstractListenerNode
{


	@Getter
	private boolean isBusy = false;

	@Getter
	private boolean inWilderness = false;

	@Getter
	private SpellBook activeSpellBook = null;

	@Getter
	private GameMode gameMode = GameMode.NORMAL;

	private boolean isInitialized = false;

	private static final int[] TRANSFER_INTERFACE_IDS = {
		12, 192, 300, 335, 464, 465, 631, 669, 724
	};

	public void initializeState()
	{
		if (isInitialized) return;

		if (client.getGameState() == GameState.LOGGED_IN)
		{
			updateBusy();
			updateWilderness();
			updateSpellbook();
			updateGameMode();
			isInitialized = true;
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			initializeState();
		}
		else
		{
			isInitialized = false;
		}
	}



	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int varbitId = event.getVarbitId();

		if (varbitId > -1)
		{
			switch (varbitId)
			{
				case VarbitID.BUSY:
					updateBusy();
					break;
				case VarbitID.SPELLBOOK:
					updateSpellbook();
					plugin.increaseCountByOne(TrackedActivity.SPELLBOOK_CHANGES.getId());
					break;
				case VarbitID.INSIDE_WILDERNESS:
					updateWilderness();
					break;
			}
		}
	}

	private void updateBusy()
	{
		isBusy = client.getVarbitValue(VarbitID.BUSY) == 1;
	}

	private void updateSpellbook()
	{
		activeSpellBook = SpellBook.getById(client.getVarbitValue(VarbitID.SPELLBOOK));
	}

	private void updateWilderness()
	{
		inWilderness = client.getVarbitValue(VarbitID.INSIDE_WILDERNESS) == 1;
	}

	private void updateGameMode()
	{
		gameMode = GameMode.fromVarbitValue(client.getVarbitValue(VarbitID.IRONMAN));
	}

	/**
	 * Determines if the player is currently in an interface or state where runes
	 * could leave the inventory (e.g., banking, dying) without actually being consumed.
	 */
	public boolean isTransferStateActive()
	{
		if (client.getLocalPlayer() != null && client.getLocalPlayer().isDead())
		{
			return true;
		}

		for (int interfaceId : TRANSFER_INTERFACE_IDS)
		{
			if (client.getWidget(interfaceId, 1) != null)
			{
				return true;
			}
		}

		return false;
	}
}