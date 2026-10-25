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

	@Getter
	private boolean ableToFullyAccessGE = false;

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
				case VarbitID.IRONMAN:
					updateGameMode();
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
		ableToFullyAccessGE = gameMode != GameMode.NORMAL;
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