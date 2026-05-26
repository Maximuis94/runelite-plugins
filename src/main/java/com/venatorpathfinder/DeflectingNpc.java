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

import java.awt.Point;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

@Slf4j
public class DeflectingNpc
{
	@Getter
	private final int size;

	@Getter
	private final WorldArea area;

	@Getter
	private final int swX;
	@Getter
	private final int swY;

	private final int deltaC;

	private final int deltaCSw;

	public DeflectingNpc(NPC npc)
	{
		size = npc.getComposition().getSize();
		area = npc.getWorldArea();

		WorldPoint swTile = npc.getWorldLocation();
		swX = swTile.getX();
		swY = swTile.getY();

		switch (size) {
			case 1:
			case 2:
				deltaC = 0;
				deltaCSw = 0;
				break;
			case 3:
				deltaC = 1;
				deltaCSw = 0;
				break;
			case 4:
			case 5:
				deltaC = 2;
				deltaCSw = 1;
				break;
			default:
				deltaC = size / 2;
				deltaCSw = Math.max(0, deltaC - 1);
				break;
		}
	}

	public int getCX() {
		return swX+deltaC;
	}

	public int getCY() {
		return swY+deltaC;
	}

	public Point getCenterTile()
	{
		return new Point(getCX(), getCY());
	}

	public int getCSwX() {
		return swX+deltaCSw;
	}

	public int getCSwY() {
		return swY+deltaCSw;
	}

	public Point getCenterSouthWestTile()
	{
		return new Point(getCSwX(), getCSwY());
	}

	/**
	 * Return a List of points that constitute the NPC's sender tiles. The exact sender tiles depend on the size.
	 * If size exceeds expected sizes, return an empty list instead.
	 */
	public List<Point> getSenderTiles()
	{
		switch (size)
		{
			case 1:
			case 3:
			case 5:
				return List.of(getCenterTile());

			case 2:
				return List.of(
					new Point(swX, swY),
					new Point(swX + 1, swY),
					new Point(swX, swY + 1),
					new Point(swX + 1, swY + 1)
				);

			case 4:
				return List.of(
					getCenterTile(),
					getCenterSouthWestTile()
				);

			default:
				log.warn("Unexpected NPC of size {}x{} encountered", size, size);
				return List.of();
		}
	}
}