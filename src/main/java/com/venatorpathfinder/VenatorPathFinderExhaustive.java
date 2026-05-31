/*
 * Copyright (c) 2026, maximuis94 <https://github.com/maximuis94>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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
import com.venatorpathfinder.node.VenatorNodeSize1;
import com.venatorpathfinder.node.VenatorNodeSize2;
import com.venatorpathfinder.node.VenatorNodeSize3;
import com.venatorpathfinder.node.VenatorNodeSize4;
import com.venatorpathfinder.node.VenatorNodeSize5;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.IndexedObjectSet;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;

@Singleton
public class VenatorPathFinderExhaustive
{
	private final Client client;
	private final Map<Integer, VenatorPathNode> nodes = new HashMap<>();
	private VenatorPathNode start;

	@Inject
	public VenatorPathFinderExhaustive(Client client)
	{
		this.client = client;
	}

	/**
	 * Initializes the nodes map with nearby targets and calculates all valid bounce paths.
	 */
	public List<VenatorPathNode[]> getPaths(NPC startNpc)
	{
		List<VenatorPathNode[]> allPaths = new ArrayList<>();

		if (startNpc == null)
		{
			return allPaths;
		}

		// 1. Gather all potential NPCs within 15 tiles into the nodes map
		updateNodes(startNpc);

		if (this.start == null)
		{
			return allPaths; // Start NPC wasn't valid or wasn't added
		}

		// 2. Initialize the base path
		List<VenatorPathNode> currentPath = new ArrayList<>();
		currentPath.add(this.start);

		// 3. Calculate paths recursively (Max 3 nodes per path: Start + 2 bounces)
		findPathsDFS(this.start, currentPath, allPaths);

		return allPaths;
	}

	/**
	 * Recursive Depth-First Search to find all valid Venator bow bounce combinations.
	 * Only records terminal/maximal paths to prevent returning redundant sub-paths.
	 */
	private void findPathsDFS(VenatorPathNode current, List<VenatorPathNode> currentPath, List<VenatorPathNode[]> allPaths)
	{
		// Base case: If we've reached a full 3-hit path, add it and terminate this branch
		if (currentPath.size() == 3)
		{
			allPaths.add(currentPath.toArray(new VenatorPathNode[0]));
			return;
		}

		boolean foundBounce = false;

		for (VenatorPathNode neighbor : nodes.values())
		{
			// Venator arrows cannot bounce to the same target twice in a row
			if (neighbor.getIndex() == current.getIndex())
			{
				continue;
			}

			// Using the mathematically accurate unified check
			if (canBounce(current, neighbor))
			{
				foundBounce = true;
				currentPath.add(neighbor);
				findPathsDFS(neighbor, currentPath, allPaths);
				currentPath.remove(currentPath.size() - 1); // Backtrack
			}
		}

		// If no further bounces were found, but we achieved at least a 2-hit path, save it
		if (!foundBounce && currentPath.size() == 2)
		{
			allPaths.add(currentPath.toArray(new VenatorPathNode[0]));
		}
	}

	/**
	 * Centralized bounce check mimicking the old geometric verification exactly.
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
				// 1. Can Send Geometry
				boolean sendPasses = false;
				if (sSize % 2 != 0) {
					sendPasses = finds(sx, sy, tSwX, tSwY) && finds(sx, sy, tCX, tCY);
				} else if (sSize == 2) {
					sendPasses = tSize <= 3 ? finds(sx, sy, tCX, tCY) : finds(sx, sy, tCX, tCY) && finds(sx, sy, tCSwX, tCSwY);
				} else if (sSize == 4) {
					sendPasses = finds(sx, sy, tSwX, tSwY) && finds(sx, sy, tCSwX, tCSwY);
				}

				// 2. Can Accept Geometry
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

				// 3. Line of Sight (Only evaluated if the EXACT tile passes both geometries)
				if (sendPasses && acceptPasses)
				{
					net.runelite.api.coords.WorldArea simulatedPlayerArea = new net.runelite.api.coords.WorldArea(sx, sy, 1, 1, plane);
					net.runelite.api.coords.WorldPoint targetCenter = new net.runelite.api.coords.WorldPoint(tCX, tCY, plane);

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
	 * Iterate over all NPCs; adds the NPCs within 15 tiles of the start NPC to the hashmap.
	 */
	private void updateNodes(NPC startNpc)
	{
		clearNodes();

		WorldPoint startLoc = startNpc.getWorldLocation();
		int plane = startLoc.getPlane();

		IndexedObjectSet<? extends NPC> npcs = client.getTopLevelWorldView().npcs();

		for (NPC npc : npcs)
		{
			// Add basic isAttackable safety check here if needed, or rely on dead/composition checks
			if (npc.isDead() || npc.getComposition() == null)
			{
				continue;
			}

			WorldPoint npcLoc = npc.getWorldLocation();

			if (npcLoc.getPlane() == plane && npcLoc.distanceTo(startLoc) <= 15)
			{
				VenatorPathNode node = getNode(npc);
				nodes.put(npc.getIndex(), node);
			}
		}

		this.start = nodes.get(startNpc.getIndex());
	}

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

	public void clearNode(int npcIndex)
	{
		nodes.remove(npcIndex);
	}

	public void clearNodes()
	{
		nodes.clear();
	}
}