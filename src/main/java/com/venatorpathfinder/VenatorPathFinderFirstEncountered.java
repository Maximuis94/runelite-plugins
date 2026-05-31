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

import com.venatorpathfinder.node.VenatorNodeSize1;
import com.venatorpathfinder.node.VenatorNodeSize2;
import com.venatorpathfinder.node.VenatorNodeSize3;
import com.venatorpathfinder.node.VenatorNodeSize4;
import com.venatorpathfinder.node.VenatorNodeSize5;
import com.venatorpathfinder.node.VenatorPathNode;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;

@Singleton
public class VenatorPathFinderFirstEncountered
{
	private final Client client;
	// Using LinkedHashMap to preserve the engine's NPC array order for the third node check
	private final Map<Integer, VenatorPathNode> nodes = new LinkedHashMap<>();

	VenatorPathNode[] predictedPath = new VenatorPathNode[3];

	@Inject
	public VenatorPathFinderFirstEncountered(Client client)
	{
		this.client = client;
	}

	private boolean isAttackable(NPC npc)
	{
		if (npc == null || npc.isDead()) return false;
		if (npc.getCombatLevel() > 0) return true;
		if (npc.getComposition() != null && npc.getComposition().getActions() != null)
		{
			for (String action : npc.getComposition().getActions())
			{
				if ("Attack".equalsIgnoreCase(action)) return true;
			}
		}
		return false;
	}

	/**
	 * Find the path by selecting the very first valid node encountered in the engine array.
	 */
	public VenatorPathNode[] findPath(NPC npc)
	{
		int currentIndex = npc.getIndex();
		VenatorPathNode startNode = getNode(npc);
		predictedPath[0] = startNode;
		nodes.clear();
		nodes.put(currentIndex, startNode);

		predictedPath[1] = findSecondNode(startNode);
		predictedPath[2] = predictedPath[1] == null ? null : findThirdNode(predictedPath[1]);

		return predictedPath;
	}

	/**
	 * Iterate over NPCs in view. Caches all NPCs in range, but stores and returns
	 * the VERY FIRST candidate that passes the bounce check.
	 */
	private VenatorPathNode findSecondNode(VenatorPathNode sender)
	{
		int currentIndex = sender.getIndex();
		VenatorPathNode firstFound = null;

		for (NPC candidate : client.getTopLevelWorldView().npcs())
		{
			if (candidate.getIndex() == currentIndex || !isAttackable(candidate)) continue;

			WorldPoint candidateLocation = candidate.getWorldLocation();
			if (sender.inRange(candidateLocation))
			{
				int npcIndex = candidate.getIndex();
				VenatorPathNode candidateNode = getNode(candidateLocation.getX(), candidateLocation.getY(), candidate);
				nodes.put(npcIndex, candidateNode);

				// If we haven't found a valid bounce yet, check this one
				if (firstFound == null && canBounce(sender, candidateNode))
				{
					firstFound = candidateNode;
				}
			}
		}
		return firstFound;
	}

	/**
	 * Iterates over the cached nodes (in the exact order they were discovered)
	 * and returns the first one that successfully accepts a bounce.
	 */
	private VenatorPathNode findThirdNode(VenatorPathNode sender)
	{
		final int senderIndex = sender.getIndex();

		for (VenatorPathNode candidate : nodes.values())
		{
			if (candidate.getIndex() != senderIndex && canBounce(sender, candidate))
			{
				return candidate;
			}
		}

		return null;
	}

	/**
	 * Centralized bounce check mimicking the accurate geometric verification.
	 */
	private boolean canBounce(VenatorPathNode sender, VenatorPathNode target)
	{
		int sSize = sender.getSize();
		int tSize = target.getSize();

		int tSwX = target.getSouthWestX();
		int tSwY = target.getSouthWestY();
		int tCX = target.getCenterX();
		int tCY = target.getCenterY();
		int tCSwX = target.getCenterSouthWestX();
		int tCSwY = target.getCenterSouthWestY();

		net.runelite.api.WorldView wv = client.getTopLevelWorldView();
		int plane = sender.getNpc().getWorldLocation().getPlane();

		for (int sx : sender.sendsFromX())
		{
			for (int sy : sender.sendsFromY())
			{
				boolean sendPasses = false;
				if (sSize % 2 != 0) {
					sendPasses = finds(sx, sy, tSwX, tSwY) && finds(sx, sy, tCX, tCY);
				} else if (sSize == 2) {
					sendPasses = tSize <= 3 ? finds(sx, sy, tCX, tCY) : finds(sx, sy, tCX, tCY) && finds(sx, sy, tCSwX, tCSwY);
				} else if (sSize == 4) {
					sendPasses = finds(sx, sy, tSwX, tSwY) && finds(sx, sy, tCSwX, tCSwY);
				}

				boolean acceptPasses = false;
				if (tSize == 1 || tSize == 2) {
					acceptPasses = finds(sx, sy, tSwX, tSwY);
				} else if (tSize == 3) {
					if (sSize % 2 != 0) acceptPasses = finds(sx, sy, tCX, tCY) && finds(sx, sy, tSwX, tSwY);
					else if (sSize == 2) acceptPasses = finds(sx, sy, tCX, tCY);
					else if (sSize == 4) acceptPasses = finds(sx, sy, tSwX, tSwY);
				} else if (tSize == 4 || tSize == 5) {
					if (sSize % 2 != 0) acceptPasses = finds(sx, sy, tCX, tCY) && finds(sx, sy, tSwX, tSwY);
					else if (sSize == 2) acceptPasses = finds(sx, sy, tCX, tCY) && finds(sx, sy, tCSwX, tCSwY);
					else if (sSize == 4) acceptPasses = finds(sx, sy, tSwX, tSwY) && finds(sx, sy, tCSwX, tCSwY);
				}

				if (sendPasses && acceptPasses)
				{
					WorldArea simulatedPlayerArea = new WorldArea(sx, sy, 1, 1, plane);
					WorldPoint targetCenter = new WorldPoint(tCX, tCY, plane);

					if (simulatedPlayerArea.hasLineOfSightTo(wv, targetCenter))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean finds(int sX, int sY, int tX, int tY)
	{
		return Math.max(Math.abs(sX - tX), Math.abs(sY - tY)) <= 2;
	}

	/**
	 * Convert npc into a VenatorNode and return it
	 */
	private VenatorPathNode getNode(NPC npc)
	{
		WorldPoint loc = npc.getWorldLocation();
		return getNode(loc.getX(), loc.getY(), npc);
	}

	private VenatorPathNode getNode(int x, int y, NPC npc)
	{
		switch (npc.getComposition().getSize())
		{
			case 1:
				return new VenatorNodeSize1(npc, x, y);
			case 2:
				return new VenatorNodeSize2(npc, x, y);
			case 3:
				return new VenatorNodeSize3(npc, x, y);
			case 4:
				return new VenatorNodeSize4(npc, x, y);
			case 5:
				return new VenatorNodeSize5(npc, x, y);
			default:
				throw new IllegalArgumentException("Invalid npc size: " + npc.getComposition().getSize());
		}
	}
}