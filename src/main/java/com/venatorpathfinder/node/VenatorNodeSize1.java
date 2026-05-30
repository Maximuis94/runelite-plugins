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

public class VenatorNodeSize1 implements VenatorNode
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


	public VenatorNodeSize1(int npcIndex, int x, int y)
	{
		this.npcIndex = npcIndex;
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
	public int getSize()
	{
		return 1;
	}

	// Sends from CENTER tile
	@Override
	public boolean canSend(VenatorNode recipient)
	{
		int tSwX = recipient.getSouthWestX();
		int tSwY = recipient.getSouthWestY();
		int tCX = recipient.getCenterX();
		int tCY = recipient.getCenterY();

		return VenatorPathFinderOverlay.finds(cX, cY, tSwX, tSwY) && VenatorPathFinderOverlay.finds(cX, cY, tCX, tCY);
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
		int senderX;
		int senderY;
		switch (sender.getSize())
		{
			// 1x1, 3x3, and 5x5 sends bounce if it finds targets SW and CENTRE tile from its CENTRE tile.
			case 1:
			case 3:
			case 5:
				senderX = sender.getCenterX();
				senderY = sender.getCenterY();
				return VenatorPathFinderOverlay.finds(senderX, senderY, swX, swY);

				// If it finds targets CENTRE and CENTRE SW tile from any of its tiles (monster is ≥ 4x4).
			case 2:
				senderX = sender.getSouthWestX();
				senderY = sender.getSouthWestY();
				return VenatorPathFinderOverlay.finds(senderX, senderY, cX, cY) ||
					VenatorPathFinderOverlay.finds(senderX+1, senderY, cX, cY) ||
					VenatorPathFinderOverlay.finds(senderX, senderY+1, cX, cY) ||
					VenatorPathFinderOverlay.finds(senderX+1, senderY+1, cX, cY);

				// 4x4 needs to find 5x5's SW and CENTRE SW tiles from any of its middle 2x2 tiles.
			case 4:
				senderX = sender.getCenterSouthWestX();
				senderY = sender.getCenterSouthWestY();
				return VenatorPathFinderOverlay.finds(senderX, senderY, swX, swY) ||
					VenatorPathFinderOverlay.finds(senderX+1, senderY, swX,swY) ||
					VenatorPathFinderOverlay.finds(senderX, senderY+1, swX, swY) ||
					VenatorPathFinderOverlay.finds(senderX+1, senderY+1, swX, swY);
			default:
				return false;
		}
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
