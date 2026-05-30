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

import com.venatorpathfinder.node.VenatorNode;
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
public class VenatorPathFinder
{
	private final Client client;
	private final Map<Integer, VenatorNode> nodes = new HashMap<>();
	private VenatorNode start;

	@Inject
	public VenatorPathFinder(Client client)
	{
		this.client = client;
	}

	/**
	 * Initializes the nodes map with nearby targets and calculates all valid bounce paths.
	 */
	public List<VenatorNode[]> getPaths(NPC startNpc)
	{
		List<VenatorNode[]> allPaths = new ArrayList<>();

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
		List<VenatorNode> currentPath = new ArrayList<>();
		currentPath.add(this.start);

		// 3. Calculate paths recursively (Max 3 nodes per path: Start + 2 bounces)
		findPathsDFS(this.start, currentPath, allPaths);

		return allPaths;
	}

	/**
	 * Recursive Depth-First Search to find all valid Venator bow bounce combinations.
	 */
	private void findPathsDFS(VenatorNode current, List<VenatorNode> currentPath, List<VenatorNode[]> allPaths)
	{
		if (currentPath.size() > 1)
		{
			allPaths.add(currentPath.toArray(new VenatorNode[0]));
		}

		if (currentPath.size() >= 3)
		{
			return;
		}

		for (VenatorNode neighbor : nodes.values())
		{
			if (neighbor.getIndex() == current.getIndex())
			{
				continue;
			}

			if (current.canSend(neighbor) && neighbor.canAccept(current))
			{
				currentPath.add(neighbor);
				findPathsDFS(neighbor, currentPath, allPaths);
				currentPath.remove(currentPath.size() - 1);
			}
		}
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
			if (npc.isDead() || npc.getComposition() == null)
			{
				continue;
			}

			WorldPoint npcLoc = npc.getWorldLocation();

			if (npcLoc.getPlane() == plane && npcLoc.distanceTo(startLoc) <= 15)
			{
				VenatorNode node = getNode(npc);
				nodes.put(npc.getIndex(), node);
			}
		}

		this.start = nodes.get(startNpc.getIndex());
	}

	private VenatorNode getNode(NPC npc)
	{
		WorldPoint loc = npc.getWorldLocation();
		int npcIndex = npc.getIndex();
		int x = loc.getX();
		int y = loc.getY();

		switch (npc.getComposition().getSize())
		{
			case 1:
				return new VenatorNodeSize1(npcIndex, x, y);
			case 2:
				return new VenatorNodeSize2(npcIndex, x, y);
			case 3:
				return new VenatorNodeSize3(npcIndex, x, y);
			case 4:
				return new VenatorNodeSize4(npcIndex, x, y);
			case 5:
				return new VenatorNodeSize5(npcIndex, x, y);
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