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

package com.dynamiclos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Area;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class LineOfSightOverlay extends Overlay {

	private final Client client;
	private final DynamicLineOfSightConfig config;
	private int activeWeaponRange = 0;

	public void setActiveAttackRange(int range) {
		activeWeaponRange = range;
	}

	@Inject
	public LineOfSightOverlay(Client client, DynamicLineOfSightConfig config) {
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		Player player = client.getLocalPlayer();

		if (player == null || player.getWorldArea() == null) {
			return null;
		}

		WorldPoint playerLocation = player.getWorldLocation();
		WorldArea playerArea = player.getWorldArea();
		WorldView wv = client.getTopLevelWorldView();

		if (config.drawActiveWeaponRange()) {
			Area activeRangeArea = calculateLineOfSightArea(playerLocation, playerArea, wv, activeWeaponRange);
			if (!activeRangeArea.isEmpty()) {
				graphics.setColor(config.activeWeaponFillColor());
				graphics.fill(activeRangeArea);

				graphics.setColor(config.activeWeaponOutlineColor());
				graphics.setStroke(new BasicStroke(1));
				graphics.draw(activeRangeArea);
			}
		}

		if (config.drawMaxAttackRange()) {
			Area maxRangeArea = calculateLineOfSightArea(playerLocation, playerArea, wv, 10);
			if (!maxRangeArea.isEmpty()) {
				graphics.setColor(config.maxRangeFillColor());
				graphics.fill(maxRangeArea);

				graphics.setColor(config.maxRangeOutlineColor());
				graphics.setStroke(new BasicStroke(1));
				graphics.draw(maxRangeArea);
			}
		}

		boolean highlightActive = config.highlightAttackableEnemies();
		boolean highlightMax = config.highlightEnemiesWithinMaxRange();

		if (highlightActive || highlightMax) {
			for (NPC npc : wv.npcs()) {
				if (npc == null || npc.isDead() || npc.getCombatLevel() == 0) {
					continue;
				}

				WorldArea npcArea = npc.getWorldArea();
				if (npcArea == null) {
					continue;
				}

				int distance = playerArea.distanceTo(npcArea);
				Color enemyHighlightColor = null;

				// --- NEW: Handle Melee Quirk ---
				boolean inActiveRange;
				if (activeWeaponRange == 1) {
					// Safely calculates 1x1 diagonal vs large NPC orthogonal rules
					inActiveRange = playerArea.isInMeleeDistance(npcArea);
				} else {
					inActiveRange = distance <= activeWeaponRange;
				}

				// Check active weapon range first (smaller bubble priority)
				if (highlightActive && inActiveRange) {
					enemyHighlightColor = config.activeWeaponOutlineColor();
				}
				// If not in active range, check max range
				else if (highlightMax && distance <= 10) {
					enemyHighlightColor = config.maxRangeOutlineColor();
				}

				// If the enemy falls into either enabled category, perform the LoS check and draw
				if (enemyHighlightColor != null) {
					if (playerArea.hasLineOfSightTo(wv, npcArea)) {
						Shape npcHull = npc.getConvexHull();
						if (npcHull != null) {
							graphics.setColor(enemyHighlightColor);
							graphics.setStroke(new BasicStroke(2));
							graphics.draw(npcHull);
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Helper method to calculate the line of sight area for a given range.
	 */
	private Area calculateLineOfSightArea(WorldPoint playerLocation, WorldArea playerArea, WorldView wv, int range) {
		Area area = new Area();

		for (int dx = -range; dx <= range; dx++) {
			for (int dy = -range; dy <= range; dy++) {
				if (range == 1 && Math.abs(dx) == 1 && Math.abs(dy) == 1) {
					continue;
				}

				WorldPoint targetPoint = new WorldPoint(
					playerLocation.getX() + dx,
					playerLocation.getY() + dy,
					playerLocation.getPlane()
				);

				WorldArea targetArea = new WorldArea(targetPoint, 1, 1);

				if (playerArea.hasLineOfSightTo(wv, targetArea)) {
					LocalPoint localPoint = LocalPoint.fromWorld(client, targetPoint);

					if (localPoint != null) {
						Polygon tilePoly = Perspective.getCanvasTilePoly(client, localPoint);

						if (tilePoly != null) {
							area.add(new Area(tilePoly));
						}
					}
				}
			}
		}
		return area;
	}
}