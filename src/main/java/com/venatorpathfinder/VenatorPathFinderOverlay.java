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

package com.venatorpathfinder;

import com.venatorpathfinder.node.VenatorPathNode;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldArea;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

@Slf4j
public class VenatorPathFinderOverlay extends Overlay
{
	private final Client client;
	private final VenatorPathFinderConfig config;
	private final ModelOutlineRenderer modelOutlineRenderer;

	private boolean hasEquippedVenatorBow = false;
	private boolean ignoreVenatorBowCondition = false;
	private boolean isInMultiCombat = false;
	private boolean ignoreMultiCombatCondition = false;
	private boolean shouldRenderPath = false;
	private boolean drawOnlyOnePath = false;
	private boolean limitPathSize = false;
	private boolean drawPathOfTargetedNpc = false; // Add this line

	private Color initialTargetOutline;
	private Color firstBounceOutline;
	private Color secondBounceOutline;
	private Color boomerangBounceOutline;

	private boolean lingeringOutline = false;
	private NPC lastSourceNpc = null;

	private int outlineWidth;
	private int outlineFeather;


	// Add this to your class variables
	private final VenatorPathFinderExhaustive pathFinderExhaustive;
	private final VenatorPathFinderLowestIndex pathFinderLowestIndex;

	private final Set<NPC> b1Npcs = new HashSet<>();
	private final Set<NPC> b2Npcs = new HashSet<>();

	// Update your constructor to include the pathFinder
	@Inject
	private VenatorPathFinderOverlay(Client client, VenatorPathFinderConfig config, ModelOutlineRenderer modelOutlineRenderer, VenatorPathFinderExhaustive pathFinder, VenatorPathFinderLowestIndex pathFinderLowestIndex)
	{
		this.client = client;
		this.config = config;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.pathFinderExhaustive = pathFinder;
		this.pathFinderLowestIndex = pathFinderLowestIndex;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		parseConfigs();
	}

	public void setInMultiCombat(boolean inMultiCombat)
	{
		isInMultiCombat = inMultiCombat;
		if (!ignoreMultiCombatCondition) updateShouldRenderPath();
	}

	public void setHasEquippedVenatorBow(boolean equippedVenatorBow)
	{
		hasEquippedVenatorBow = equippedVenatorBow;
		if (!ignoreVenatorBowCondition) updateShouldRenderPath();
	}

	public void parseConfigs()
	{
		initialTargetOutline = config.initialColor();
		firstBounceOutline = config.bounce1Color();
		secondBounceOutline = config.bounce2Color();
		boomerangBounceOutline = config.returnBounceColor();
		outlineFeather = config.outlineFeather();
		outlineWidth = config.outlineWidth();

		ignoreMultiCombatCondition = config.ignoreMultiCombatPrerequisite();
		ignoreVenatorBowCondition = config.ignoreVenatorBowPrerequisite();
		drawOnlyOnePath = config.drawOnlyOnePath();
		limitPathSize = config.limitPathSize();

		boolean screenshotLoggerEnabled = config.debugLoggingScreenshotEnabled();
		drawPathOfTargetedNpc = config.drawPathOfTargetedNpc() || screenshotLoggerEnabled;

		updateShouldRenderPath();
	}

	private void updateShouldRenderPath()
	{
		shouldRenderPath = (ignoreMultiCombatCondition || isInMultiCombat) && (ignoreVenatorBowCondition || hasEquippedVenatorBow);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!shouldRenderPath) return null;

		Player player = client.getLocalPlayer();
		if (player == null) return null;

		WorldArea playerArea = player.getWorldArea();
		if (playerArea == null) return null;

		NPC hoverNpc = null;
		NPC combatNpc = null;
		WorldView worldView = client.getTopLevelWorldView();

		MenuEntry[] menuEntries = client.getMenu().getMenuEntries();
		if (menuEntries.length > 0 && !client.isMenuOpen())
		{
			MenuEntry topEntry = menuEntries[menuEntries.length - 1];
			NPC hoveredNpc = topEntry.getNpc();

			if (isAttackable(hoveredNpc))
			{
				hoverNpc = hoveredNpc;
			}
		}

		if (drawPathOfTargetedNpc)
		{
			Actor interacting = player.getInteracting();
			if (interacting instanceof NPC)
			{
				NPC interactingNpc = (NPC) interacting;
				if (isAttackable(interactingNpc))
				{
					combatNpc = interactingNpc;
				}
			}

			if (combatNpc == null && soundTargetNpc != null && (client.getTickCount() - lastAttackTick <= 4))
			{
				for (NPC npc : worldView.npcs())
				{
					if (npc == soundTargetNpc)
					{
						combatNpc = soundTargetNpc;
						break;
					}
				}
			}

			if (combatNpc != null)
			{
				lastSourceNpc = combatNpc;
			}
			else if (lastSourceNpc != null)
			{
				boolean isStillVisible = false;
				for (NPC npc : worldView.npcs())
				{
					if (npc == lastSourceNpc)
					{
						isStillVisible = true;
						break;
					}
				}

				if (isStillVisible)
				{
					combatNpc = lastSourceNpc;
				}
				else
				{
					lastSourceNpc = null;
				}
			}
		}
		else
		{
			lastSourceNpc = null;
		}

		NPC sourceNpc = hoverNpc != null ? hoverNpc : combatNpc;

		if (sourceNpc == null) return null;




		if (drawOnlyOnePath)
		{
			VenatorPathNode[] singlePath = pathFinderLowestIndex.findPath(sourceNpc);
			if (singlePath == null || singlePath.length < 2) return null;

			NPC first = singlePath[1] != null ? singlePath[1].getNpc() : null;

			if (first == null) return null;

			NPC second = null;
			// Apply limitPathSize filter to the single path view as well
			if (!limitPathSize && singlePath.length > 2 && singlePath[2] != null)
			{
				second = singlePath[2].getNpc();
			}

			boolean returnsToSender = second != null && sourceNpc.getIndex() == second.getIndex();

			if (returnsToSender) {
				modelOutlineRenderer.drawOutline(sourceNpc, outlineWidth+1, boomerangBounceOutline, outlineFeather);
			} else {
				modelOutlineRenderer.drawOutline(sourceNpc, outlineWidth, initialTargetOutline, outlineFeather);

				if (second != null)
					modelOutlineRenderer.drawOutline(second, outlineWidth, secondBounceOutline, outlineFeather);
			}
			modelOutlineRenderer.drawOutline(first, outlineWidth, firstBounceOutline, outlineFeather);
		}
		else
		{
			b1Npcs.clear();
			b2Npcs.clear();
			boolean returnsToSender = false;

			List<VenatorPathNode[]> allPaths = pathFinderExhaustive.getPaths(sourceNpc);

			for (VenatorPathNode[] path : allPaths)
			{
				if (path.length > 1 && path[1] != null)
				{
					b1Npcs.add(path[1].getNpc());
				}

				if (!limitPathSize && path.length > 2 && path[2] != null)
				{
					NPC b2 = path[2].getNpc();
					if (b2.getIndex() == sourceNpc.getIndex())
					{
						returnsToSender = true; // Boomerang detected
					}
					else
					{
						b2Npcs.add(b2);
					}
				}
			}

			if (returnsToSender) {
				modelOutlineRenderer.drawOutline(sourceNpc, outlineWidth+1, boomerangBounceOutline, outlineFeather);
			} else if (!b1Npcs.isEmpty()) {
				modelOutlineRenderer.drawOutline(sourceNpc, outlineWidth, initialTargetOutline, outlineFeather);
			}

			for (NPC n : b1Npcs) modelOutlineRenderer.drawOutline(n, outlineWidth, firstBounceOutline, outlineFeather);

			for (NPC n : b2Npcs)
			{
				if (!b1Npcs.contains(n)) {
					modelOutlineRenderer.drawOutline(n, outlineWidth, secondBounceOutline, outlineFeather);
				}
			}
		}

		return null;
	}

	private boolean isAttackable(NPC npc)
	{
		if (npc == null || npc.isDead())
		{
			return false;
		}

		if (npc.getCombatLevel() > 0)
		{
			return true;
		}

		if (npc.getComposition() != null && npc.getComposition().getActions() != null)
		{
			for (String action : npc.getComposition().getActions())
			{
				if ("Attack".equalsIgnoreCase(action))
				{
					return true;
				}
			}
		}

		return false;
	}

	private int lastAttackTick = -1;
	private NPC soundTargetNpc = null;

	public void notifyAttack(NPC target, int currentTick)
	{
		this.soundTargetNpc = target;
		this.lastAttackTick = currentTick;
	}

	/**
	 * Purely geometric distance check used by VenatorNodes.
	 */
	public static boolean finds(int sX, int sY, int tX, int tY)
	{
		return Math.max(Math.abs(sX - tX), Math.abs(sY - tY)) <= 2;
	}
}