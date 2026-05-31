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

package com.venatorpathfinder.node;

import static com.venatorpathfinder.VenatorPathFinderPlugin.IN_RANGE_RADIUS;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;

public class VenatorNodeSize1 implements VenatorPathNode
{
	private final int npcIndex;

	private final NPC npc;

	private final int swX;
	private final int swY;
	private final int cX;
	private final int cY;
	private final int cSwX;
	private final int cSwY;

	private final int[] fromX;
	private final int[] fromY;


	public VenatorNodeSize1(NPC npc, int x, int y)
	{
		this.npcIndex = npc.getIndex();
		this.npc = npc;

		swX = x;
		swY = y;

		cSwX = x;
		cSwY = y;

		cX = x;
		cY = y;

		fromX = new int[]{x};
		fromY = new int[]{y};
	}

	@Override
	public int getIndex()
	{
		return npcIndex;
	}

	@Override
	public NPC getNpc()
	{
		return npc;
	}

	@Override
	public int getSize()
	{
		return 1;
	}

	@Override
	public boolean inRange(WorldPoint wp)
	{
		return Math.max(Math.abs(swX - wp.getX()), Math.abs(swY - wp.getY())) <= IN_RANGE_RADIUS;
	}

	@Override
	public int getSouthWestX()
	{
		return swX;
	}

	@Override
	public int getSouthWestY()
	{
		return swY;
	}

	@Override
	public int getCenterX()
	{
		return cX;
	}

	@Override
	public int getCenterY()
	{
		return cY;
	}

	@Override
	public int getCenterSouthWestX()
	{
		return cSwX;
	}

	@Override
	public int getCenterSouthWestY()
	{
		return cSwY;
	}

	@Override
	public int[] sendsFromX()
	{
		return fromX;
	}

	@Override
	public int[] sendsFromY()
	{
		return fromY;
	}
}
