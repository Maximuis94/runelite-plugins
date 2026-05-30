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

import com.venatorpathfinder.VenatorPathFinderOverlay;

public class VenatorNodeSize2 implements VenatorNode
{
	private final int npcIndex;

	private final int swX;
	private final int swY;
	private final int cX;
	private final int cY;
	private final int cSwX;
	private final int cSwY;

	private final int[] fromX;
	private final int[] fromY;

	public VenatorNodeSize2(int npcIndex, int x, int y)
	{
		this.npcIndex = npcIndex;
		swX = x;
		swY = y;

		cSwX = x;
		cSwY = y;

		cX = x;
		cY = y;

		fromX = new int[]{swX, cX};
		fromY = new int[]{swY, cY};
	}

	@Override
	public int getIndex()
	{
		return npcIndex;
	}

	@Override
	public int getSize()
	{
		return 2;
	}

	// Sends from ANY tile
	@Override
	public boolean canSend(VenatorNode recipient)
	{
		int cX = recipient.getCenterX();
		int cY = recipient.getCenterY();

		switch (recipient.getSize())
		{
			case 1:
			case 2:
			case 3:
				for (int x : fromX)
					for (int y : fromY)
						if (VenatorPathFinderOverlay.finds(x, y, cX, cY))
							return true;
			case 4:
			case 5:
				int cSwX = recipient.getCenterSouthWestX();
				int cSwY = recipient.getCenterSouthWestY();
				for (int x : fromX)
					for (int y : fromY)
						if (VenatorPathFinderOverlay.finds(x, y, cX, cY) && VenatorPathFinderOverlay.finds(x, y, cSwX, cSwY))
							return true;
		}
		return false;
	}

	/**
	 * 5x5 accepts bounce to it, if targeting monster passes these rules.
	 * Odd sized (1x1,3x3,5x5) needs to find 5x5's CENTRE and SW tiles.
	 * 2x2 needs to find 5x5's CENTRE and CENTRE SW tiles.
	 * 4x4 needs to find 5x5's SW and CENTRE SW tiles.
	 */
	@Override
	public boolean canAccept(VenatorNode sender)
	{
		for (int x: sender.sendsFromX())
			for (int y: sender.sendsFromY())
				if (VenatorPathFinderOverlay.finds(x, y, swX, swY))
					return true;

		return false;
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
