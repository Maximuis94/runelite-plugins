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
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

/**
 * Class for handling the overlay generation
 */
public class LineOfSightOverlay extends Overlay {

	private final Client client;
	private int activeWeaponRange = 0;

	public void setActiveAttackRange(int range)
	{
		activeWeaponRange = range;
	}

	@Inject
	public LineOfSightOverlay(Client client) {
		this.client = client;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	private static final Color OUTLINE_COLOR = new Color(0, 255, 255, 200);
	private static final Color FILL_COLOR = new Color(0, 255, 255, 30);

	@Override
	public Dimension render(Graphics2D graphics) {
		Player player = client.getLocalPlayer();

		if (player == null || player.getWorldArea() == null) {
			return null;
		}

		WorldPoint playerLocation = player.getWorldLocation();
		WorldArea playerArea = player.getWorldArea();
		WorldView wv = client.getTopLevelWorldView();

		for (int dx = -activeWeaponRange; dx <= activeWeaponRange; dx++) {
			for (int dy = -activeWeaponRange; dy <= activeWeaponRange; dy++) {
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
							OverlayUtil.renderPolygon(
								graphics,
								tilePoly,
								OUTLINE_COLOR,
								FILL_COLOR,
								new BasicStroke(1)
							);
						}
					}
				}
			}
		}
		return null;
	}
}