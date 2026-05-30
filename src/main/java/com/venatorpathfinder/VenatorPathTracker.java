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

import com.google.gson.Gson;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.NPC;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.DrawManager;

@Slf4j
@Singleton
public class VenatorPathTracker
{
	private final Client client;
	private final DrawManager drawManager;
	private final Gson gson;

	private static final int VENATOR_ATTACK_SOUND = 4022;
	private static final int VENATOR_BOUNCE_SOUND = 4023;

	private boolean isTracking = false;
	private int ticksSinceAttack = 0;
	private int expectedPathSize = 1;

	private final List<NPC> currentPath = new ArrayList<>();

	@Inject
	public VenatorPathTracker(Client client, DrawManager drawManager, Gson gson)
	{
		this.client = client;
		this.drawManager = drawManager;

		// We configure Gson to pretty-print so it's readable by humans if needed
		this.gson = gson.newBuilder().setPrettyPrinting().create();
	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		int soundId = event.getSoundId();

		if (soundId == VENATOR_ATTACK_SOUND)
		{
			if (isTracking && !currentPath.isEmpty())
			{
				finalizeTrack();
			}

			isTracking = true;
			ticksSinceAttack = 0;
			expectedPathSize = 1;
			currentPath.clear();
		}
		else if (isTracking && soundId == VENATOR_BOUNCE_SOUND)
		{
			expectedPathSize++;
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		if (!isTracking)
		{
			return;
		}

		if (event.getActor() instanceof NPC && event.getHitsplat().isMine())
		{
			NPC target = (NPC) event.getActor();
			currentPath.add(target);

			// SCREENSHOT & JSON TRIGGER: 3rd hitsplat within 4 ticks
			if (currentPath.size() == 3 && ticksSinceAttack <= 4)
			{
				finalizeTrack();
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (isTracking)
		{
			ticksSinceAttack++;

			if (ticksSinceAttack > 4 || currentPath.size() == expectedPathSize)
			{
				finalizeTrack();
			}
		}
	}

	/**
	 * Handles the logic for closing out a track: logs CSV, saves screenshot, and exports JSON environment.
	 */
	private void finalizeTrack()
	{
		if (currentPath.isEmpty())
		{
			isTracking = false;
			return;
		}

		long timestamp = System.currentTimeMillis();

		logCsvData();
		exportEnvironmentJson(timestamp);

		// Only screenshot if it was a full 3-hit path
		if (currentPath.size() == 3)
		{
			takeScreenshot(timestamp);
		}

		isTracking = false;
		currentPath.clear();
	}

	private void logCsvData()
	{
		StringBuilder sb = new StringBuilder("VenatorData,");
		sb.append(expectedPathSize).append(",").append(currentPath.size()).append(",");

		for (int i = 0; i < 3; i++)
		{
			if (i < currentPath.size())
			{
				NPC npc = currentPath.get(i);
				sb.append(npc.getId()).append(",")
					.append(npc.getIndex()).append(",")
					.append(npc.getWorldLocation().getX()).append(",")
					.append(npc.getWorldLocation().getY()).append(",");
			}
			else
			{
				sb.append("NONE,-1,-1,-1,");
			}
		}
		log.info(sb.toString());
	}

	/**
	 * Reconstructs the 15-tile radius environment and serializes it to JSON.
	 */
	private void exportEnvironmentJson(long timestamp)
	{
		try
		{
			WorldView worldView = client.getTopLevelWorldView();
			NPC primaryTarget = currentPath.get(0);
			WorldPoint startLoc = primaryTarget.getWorldLocation();
			int plane = startLoc.getPlane();

			SimulationState state = new SimulationState();
			state.timestamp = timestamp;
			state.plane = plane;

			// Base X and Y are required to map World Coordinates to the 104x104 Collision Array
			state.basePathX = worldView.getBaseX();
			state.basePathY = worldView.getBaseY();

			// 1. Map the actual path taken
			state.actualPath = new ArrayList<>();
			for (NPC npc : currentPath)
			{
				state.actualPath.add(new NpcState(npc));
			}

			// 2. Map all possible targets in the area (15 tile radius)
			state.environmentNpcs = new ArrayList<>();
			for (NPC npc : worldView.npcs())
			{
				if (!npc.isDead() && npc.getWorldLocation().getPlane() == plane)
				{
					if (npc.getWorldLocation().distanceTo(startLoc) <= 15)
					{
						state.environmentNpcs.add(new NpcState(npc));
					}
				}
			}

			// 3. Extract the Collision map for the current plane
			CollisionData[] collisionMaps = worldView.getCollisionMaps();
			if (collisionMaps != null && collisionMaps[plane] != null)
			{
				// getFlags() returns a 104x104 int array representing line of sight/movement blocks
				state.collisionFlags = collisionMaps[plane].getFlags();
			}

			// Write to File
			File dir = new File(RuneLite.RUNELITE_DIR, "screenshots/VenatorTracker");
			dir.mkdirs();
			File file = new File(dir, "Path-" + timestamp + ".json");

			try (FileWriter writer = new FileWriter(file))
			{
				gson.toJson(state, writer);
			}

			log.info("Saved Venator JSON environment to: {}", file.getAbsolutePath());
		}
		catch (Exception e)
		{
			log.error("Failed to export JSON environment", e);
		}
	}

	private void takeScreenshot(long timestamp)
	{
		drawManager.requestNextFrameListener(image -> {
			BufferedImage bufferedImage = (BufferedImage) image;
			try
			{
				File dir = new File(RuneLite.RUNELITE_DIR, "screenshots/VenatorTracker");
				dir.mkdirs();
				File file = new File(dir, "Path-" + timestamp + ".png");
				ImageIO.write(bufferedImage, "png", file);
				log.info("Saved Venator screenshot to: {}", file.getAbsolutePath());
			}
			catch (Exception e)
			{
				log.error("Failed to save Venator path screenshot", e);
			}
		});
	}

	// --- INNER CLASSES FOR GSON SERIALIZATION ---

	private static class SimulationState
	{
		long timestamp;
		int basePathX;
		int basePathY;
		int plane;
		List<NpcState> actualPath;
		List<NpcState> environmentNpcs;
		int[][] collisionFlags;
	}

	private static class NpcState
	{
		int id;
		int index;
		int size;
		int worldX;
		int worldY;

		public NpcState(NPC npc)
		{
			this.id = npc.getId();
			this.index = npc.getIndex();
			this.worldX = npc.getWorldLocation().getX();
			this.worldY = npc.getWorldLocation().getY();
			this.size = (npc.getComposition() != null) ? npc.getComposition().getSize() : 1;
		}
	}
}