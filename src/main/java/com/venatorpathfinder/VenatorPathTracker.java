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
import com.venatorpathfinder.node.VenatorPathNode;
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
	private final VenatorPathFinderLowestIndex pathFinder;
//	private final VenatorPathFinderFirstEncountered pathFinder; // Add this line

	private boolean isTracking = false;
	private int ticksSinceAttack = 0;
	private int expectedPathSize = 1;

	private static final File ROOT = new File(RuneLite.RUNELITE_DIR, "venator-path-finder");
	private static final File JSON_ROOT = new File(ROOT, "json");
	private static final File CSV_FILE = new File(ROOT, "venator-paths.csv");
	private static final File IMG_ROOT = new File(ROOT, "image");

	private final List<NPC> currentPath = new ArrayList<>();

	@Inject
	public VenatorPathTracker(Client client, DrawManager drawManager, Gson gson, VenatorPathFinderLowestIndex pathFinder)
	{
		this.client = client;
		this.drawManager = drawManager;

		this.gson = gson.newBuilder().setPrettyPrinting().create();
		this.pathFinder = pathFinder;
	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		int soundId = event.getSoundId();

		if (soundId == VenatorPathFinderPlugin.VENATOR_ATTACK_SOUND)
		{
			if (isTracking && !currentPath.isEmpty())
			{
				finalizeTrack();
			}

			isTracking = true;
			ticksSinceAttack = 0;
			expectedPathSize = 1; // Base attack is 1 hit
			currentPath.clear();
		}
		else if (isTracking)
		{
			if (soundId == VenatorPathFinderPlugin.VENATOR_BOUNCE_SOUND_1)
			{
				expectedPathSize = Math.max(expectedPathSize, 2);
			}
			else if (soundId == VenatorPathFinderPlugin.VENATOR_BOUNCE_SOUND_2)
			{
				expectedPathSize = Math.max(expectedPathSize, 3);
			}
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

		// --- PREDICTION GENERATION & COMPARISON ---
		NPC initialTarget = currentPath.get(0);
		VenatorPathNode[] predictedNodes = pathFinder.findPath(initialTarget);
		List<NPC> predictedNpcs = new ArrayList<>();

		if (predictedNodes != null)
		{
			for (int i = 0; i < 3; i++)
			{
				if (predictedNodes[i] != null && predictedNodes[i].getNpc() != null)
				{
					predictedNpcs.add(predictedNodes[i].getNpc());
				}
			}
		}

		boolean matchesPrediction = currentPath.size() == predictedNpcs.size();
		if (matchesPrediction)
		{
			for (int i = 0; i < currentPath.size(); i++)
			{
				if (currentPath.get(i).getIndex() != predictedNpcs.get(i).getIndex())
				{
					matchesPrediction = false;
					break;
				}
			}
		}

		// --- PATHLESS SKIP LOGIC ---
		// If both the actual path and the predicted path only contain 1 hit, discard the log entirely.
		if (currentPath.size() == 1 && predictedNpcs.size() == 1)
		{
			isTracking = false;
			currentPath.clear();
			return;
		}
		// ------------------------------------------

		long timestamp = System.currentTimeMillis();

		logCsvData(matchesPrediction);
		exportEnvironmentJson(timestamp);

		if (currentPath.size() == 3)
		{
			takeScreenshot(timestamp);
		}

		isTracking = false;
		currentPath.clear();
	}

	private void logCsvData(boolean matchesPrediction)
	{
		// 1. Check if the file exists BEFORE the FileWriter creates it
		boolean isNewFile = !CSV_FILE.exists();

		StringBuilder sb = new StringBuilder();

		// 2. Append the header if it is a brand new file
		if (isNewFile)
		{
			// Added 'MatchesPrediction' as the first column
			sb.append("MatchesPrediction,ExpectedPathSize,ActualPathSize,")
				.append("Npc1_ID,Npc1_Name,Npc1_Index,Npc1_RegX,Npc1_RegY,")
				.append("Npc2_ID,Npc2_Name,Npc2_Index,Npc2_RegX,Npc2_RegY,")
				.append("Npc3_ID,Npc3_Name,Npc3_Index,Npc3_RegX,Npc3_RegY\n");
		}

		// Append the actual data
		sb.append(matchesPrediction).append(",")
			.append(expectedPathSize).append(",")
			.append(currentPath.size()).append(",");

		for (int i = 0; i < 3; i++)
		{
			if (i < currentPath.size())
			{
				NPC npc = currentPath.get(i);
				sb.append(npc.getId()).append(",")
					.append(npc.getName()).append(",")
					.append(npc.getIndex()).append(",")
					.append(npc.getWorldLocation().getX()).append(",")
					.append(npc.getWorldLocation().getY()).append(",");
			}
			else
			{
				sb.append("-1,NONE,-1,-1,-1,");
			}
		}

		// Remove the trailing comma to keep the CSV clean
		if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',')
		{
			sb.deleteCharAt(sb.length() - 1);
		}

		sb.append("\n");

		// Ensure directory exists, then write to the file
		try
		{
			CSV_FILE.getParentFile().mkdirs();

			try (FileWriter writer = new FileWriter(CSV_FILE, true))
			{
				writer.write(sb.toString());
			}
		}
		catch (Exception e)
		{
			log.error("Failed to write Venator data to CSV file", e);
		}
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
			JSON_ROOT.mkdirs();
			File file = new File(JSON_ROOT, "Path-" + timestamp + ".json");

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
				IMG_ROOT.mkdirs();
				File file = new File(IMG_ROOT, "Path-" + timestamp + ".jpeg");
				ImageIO.write(bufferedImage, "jpg", file);
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