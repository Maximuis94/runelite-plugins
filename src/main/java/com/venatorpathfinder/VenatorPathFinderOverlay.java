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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
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

	private Color initialTargetOutline;
	private Color firstBounceOutline;
	private Color secondBounceOutline;
	private Color boomerangBounceOutline;

	private int outlineWidth;
	private int outlineFeather;

	private int plane = 0;

	@Inject
	private VenatorPathFinderOverlay(Client client, VenatorPathFinderConfig config, ModelOutlineRenderer modelOutlineRenderer)
	{
		this.client = client;
		this.config = config;
		this.modelOutlineRenderer = modelOutlineRenderer;
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

		plane = playerArea.getPlane();

		MenuEntry[] menuEntries = client.getMenu().getMenuEntries();
		if (menuEntries.length == 0 || client.isMenuOpen()) return null;

		MenuEntry topEntry = menuEntries[menuEntries.length - 1];
		NPC hoveredNpc = topEntry.getNpc();

		if (!isAttackable(hoveredNpc)) return null;

		DeflectingNpc dHoveredNpc = new DeflectingNpc(hoveredNpc);

		WorldView worldView = client.getTopLevelWorldView();

		List<NPC> validNpcs = new ArrayList<>();
		for (NPC npc : worldView.npcs())
		{
			if (isAttackable(npc))
			{
				if (hoveredNpc.getWorldLocation().distanceTo(npc.getWorldLocation()) <= 15)
				{
					validNpcs.add(npc);
				}
			}
		}

		Set<NPC> bounce1 = new HashSet<>();
		Set<NPC> bounce2 = new HashSet<>();
		boolean returnsToSender = false;

		List<NPC> b1Candidates = new ArrayList<>(validNpcs);
		b1Candidates.sort(this::compareNpcsByCoordinate);

		for (NPC b1 : b1Candidates)
		{
			if (b1 == hoveredNpc) continue;

			DeflectingNpc dB1 = new DeflectingNpc(b1);
			List<Point> hoveredSenderTiles = dHoveredNpc.getSenderTiles();
			if (hoveredSenderTiles.isEmpty())
			{
				continue;
			}

			if (canBounce(worldView, dHoveredNpc, dB1, hoveredSenderTiles) && hasNpcLineOfSight(worldView, hoveredNpc.getWorldArea(), b1.getWorldArea()))
			{
				bounce1.add(b1);

				if (limitPathSize)
				{
					if (drawOnlyOnePath) break;
					else continue;
				}

				List<Point> b1SenderTiles = dB1.getSenderTiles();
				if (b1SenderTiles.isEmpty())
				{
					continue;
				}

				List<NPC> b2Candidates = new ArrayList<>(validNpcs);
				b2Candidates.sort(this::compareNpcsByCoordinate);

				for (NPC b2 : b2Candidates)
				{
					if (b2 == b1) continue;
					DeflectingNpc dB2 = new DeflectingNpc(b2);

					if (canBounce(worldView, dB1, dB2, b1SenderTiles) && hasNpcLineOfSight(worldView, b1.getWorldArea(), b2.getWorldArea()))
					{
						if (b2 == hoveredNpc)
						{
							returnsToSender = true;
						}
						else
						{
							bounce2.add(b2);
						}

						if (drawOnlyOnePath) break;
					}
				}

				if (drawOnlyOnePath) break;
			}
		}

		if (returnsToSender) {
			modelOutlineRenderer.drawOutline(hoveredNpc, outlineWidth+1, boomerangBounceOutline, outlineFeather);
		} else if (!bounce1.isEmpty()) {
			modelOutlineRenderer.drawOutline(hoveredNpc, outlineWidth, initialTargetOutline, outlineFeather);
		}

		for (NPC n : bounce1) modelOutlineRenderer.drawOutline(n, outlineWidth, firstBounceOutline, outlineFeather);
		for (NPC n : bounce2)
		{
			if (!bounce1.contains(n)) {
				modelOutlineRenderer.drawOutline(n, outlineWidth, secondBounceOutline, outlineFeather);
			}
		}

		return null;
	}

	private int compareNpcsByCoordinate(NPC n1, NPC n2)
	{
		int x1 = n1.getWorldLocation().getX();
		int x2 = n2.getWorldLocation().getX();
		if (x1 != x2)
		{
			return Integer.compare(x2, x1);
		}

		int y1 = n1.getWorldLocation().getY();
		int y2 = n2.getWorldLocation().getY();

		return Integer.compare(y2, y1);
	}

	private boolean hasNpcLineOfSight(WorldView wv, WorldArea sourceArea, WorldArea targetArea)
	{
		int nearestX = Math.max(sourceArea.getX(), Math.min(targetArea.getX(), sourceArea.getX() + sourceArea.getWidth() - 1));
		int nearestY = Math.max(sourceArea.getY(), Math.min(targetArea.getY(), sourceArea.getY() + sourceArea.getHeight() - 1));
		WorldArea nearestSourceTile = new WorldArea(nearestX, nearestY, 1, 1, plane);

		return targetArea.hasLineOfSightTo(wv, nearestSourceTile);
	}

	/**
	 * Returns true if the geometry allows a projectile to bounce from sender to target.
	 * Mirrors engine behavior by finding the FIRST geometrically valid tile and short-circuiting
	 * the entire bounce check if that specific tile lacks Line of Sight.
	 */
	private boolean canBounce(WorldView worldView, DeflectingNpc sender, DeflectingNpc target, List<Point> senderTiles)
	{
		for (Point s : senderTiles)
		{
			boolean geometryPasses = canSendGeometry(sender, target, s) && canAcceptGeometry(sender, target, s);

			if (geometryPasses)
			{
				WorldArea simulatedPlayerArea = new WorldArea(s.x, s.y, 1, 1, plane);
				Point tCenter = target.getCenterTile();

				return simulatedPlayerArea.hasLineOfSightTo(worldView, new WorldPoint(tCenter.x, tCenter.y, plane));
			}
		}
		return false;
	}

	/**
	 * Determines if a sender NPC tile geometrically satisfies the requirements to initiate a bounce.
	 */
	private boolean canSendGeometry(DeflectingNpc sender, DeflectingNpc target, Point s)
	{
		int sSize = sender.getSize();
		int tSize = target.getSize();

		int tSwX = target.getSwX();
		int tSwY = target.getSwY();
		int tCX = target.getCX();
		int tCY = target.getCY();
		int tCSwX = target.getCSwX();
		int tCSwY = target.getCSwY();

		if (sSize % 2 != 0) {
			return finds(s.x, s.y, tSwX, tSwY) && finds(s.x, s.y, tCX, tCY);
		} else if (sSize == 2) {
			return tSize <= 3 ?
				finds(s.x, s.y, tCX, tCY) :
				finds(s.x, s.y, tCX, tCY) && finds(s.x, s.y, tCSwX, tCSwY);
		} else if (sSize == 4) {
			return finds(s.x, s.y, tSwX, tSwY) && finds(s.x, s.y, tCSwX, tCSwY);
		}
		return false;
	}

	/**
	 * Determines if a target NPC geometrically satisfies the requirements to accept a bounce.
	 */
	private boolean canAcceptGeometry(DeflectingNpc sender, DeflectingNpc target, Point s)
	{
		int sSize = sender.getSize();
		int tSize = target.getSize();

		int tSwX = target.getSwX();
		int tSwY = target.getSwY();
		int tCX = target.getCX();
		int tCY = target.getCY();
		int tCSwX = target.getCSwX();
		int tCSwY = target.getCSwY();

		if (tSize == 1 || tSize == 2) {
			return finds(s.x, s.y, tSwX, tSwY);
		} else if (tSize == 3) {
			if (sSize % 2 != 0) return finds(s.x, s.y, tCX, tCY) && finds(s.x, s.y, tSwX, tSwY);
			else if (sSize == 2) return finds(s.x, s.y, tCX, tCY);
			else if (sSize == 4) return finds(s.x, s.y, tSwX, tSwY);
		} else if (tSize == 4 || tSize == 5) {
			if (sSize % 2 != 0) return finds(s.x, s.y, tCX, tCY) && finds(s.x, s.y, tSwX, tSwY);
			else if (sSize == 2) return finds(s.x, s.y, tCX, tCY) && finds(s.x, s.y, tCSwX, tCSwY);
			else if (sSize == 4) return finds(s.x, s.y, tSwX, tSwY) && finds(s.x, s.y, tCSwX, tCSwY);
		} else {
			log.warn("Found an unexpected NPC size of {}x{}", tSize, tSize);
		}
		return false;
	}

	/**
	 * Purely geometric distance check: Returns true if the Chebyshev distance is lesser than or equal to 2
	 */
	private boolean finds(int sX, int sY, int tX, int tY)
	{
		return Math.max(Math.abs(sX - tX), Math.abs(sY - tY)) <= 2;
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
}