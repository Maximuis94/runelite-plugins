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

package com.datalogger.services;

import static com.datalogger.constants.Colosseum.LosLink;
import static com.datalogger.constants.Colosseum.LosLink.LOS_LINK_CORRECTION_X;
import static com.datalogger.constants.Colosseum.LosLink.LOS_LINK_CORRECTION_Y;
import static com.datalogger.constants.Colosseum.LosLink.MANTICORE_URL_ID;
import static com.datalogger.constants.Colosseum.LosLink.SERPENT_SHAMAN_REINFORCEMENTS_URL_ID;
import static com.datalogger.constants.Colosseum.LosLink.SERPENT_SHAMAN_URL_ID;
import static com.datalogger.constants.Colosseum.LosLink.STATIC_NPC_URL_IDS;
import static com.datalogger.constants.Colosseum.LosLink.WAVE_START_AFFIX;
import static com.datalogger.constants.Colosseum.STATIC_MANTICORE_AFFIXES;
import com.datalogger.dto.ColosseumStateDTO;
import com.datalogger.models.colosseum.ColosseumNPC;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.gameval.NpcID;

/**
 * Generator class for los.colosim replay links. A timeline json file is attached to the class.
 * Replay links may be generated for waves 1-11, given certain assumptions are met.
 * Class is built / its methods are invoked via sidepanel UI.
 * Key components;
 * - Remapped NPC/player coordinates to replay URL coordinates/IDs
 * - Assign correct manticore orb sequences using all wave states.
 * - Distinguish between reinforcements / initial Serpent shaman
 * Limitations;
 * - Red flag bull (poor thang is simply ignored since it no longer blocks its allies)
 * - At t0 the wave is assumed to start OR all mobs are expected to be able to launch their next attack
 * 		i.e. there is no active cooldown; If t0!=0, every npc can attack immediately, provided the player is within range
 * 		How to communicate this, if at all?
 * - Certain edge cases cannot be fixed; mid-replay reinforcements spawns are ignored as NPC locations are defined at t0.
 * - Wave start flag can only be applied properly if t0=0; if t0=1, it is not possible to factor this in.
 * 		Fix: make it impossible for t0=[1,2]
 * N.B. it seems fair to assume that anyone who takes the effort to generate replays is reviewing/optimizing in detail
 * 	and therefore is likely to be aware of these limitations.
 */
@Slf4j
public class ColosseumReplayLinkGenerator
{
	private final int nWaves;
	private final Gson gson;

	// Lists of states, grouped per wave number
	private final Map<Integer, List<ColosseumStateDTO>> timelineMap = new HashMap<>();

	public ColosseumReplayLinkGenerator(File timelineFile, Gson gson)
	{
		this.gson = gson;
		this.nWaves = parseTimelineFile(timelineFile);
	}

	/**
	 * Generates the actual replay URL for the given waveNumber, ranging from tick t0 to tick t1 in that wave.
	 * Additional reinforcements spawns during a replay are not encoded mid-replay, as npc coordinates are defined in
	 * the initial state.
	 * @param wave The specific wave number to generate the link for
	 * @param startTick   The starting tick
	 * @param duration   Replay duration in ticks
	 * @return Replay URL if parameters check out and a valid URL is generated, else null.
	 */
	public String getReplayUrl(int wave, int startTick, int duration)
	{
		if (wave < 1 || wave > 11) return null;

		List<ColosseumStateDTO> waveTimeline = timelineMap.get(wave);

		if (waveTimeline == null || waveTimeline.isEmpty()) {
			return null;
		}

		Set<Integer> initialNpcIndices = new HashSet<>();
		Set<Integer> reinforcementNpcIndices = new HashSet<>();
		Map<Integer, String> manticoreSequences = new HashMap<>();

		for (ColosseumStateDTO state : waveTimeline) {
			int currentWave = state.getWave();
			if (wave != currentWave) continue;

			int currentTick = state.getTick(); // Since tick is relative, it starts around 0

			for (ColosseumNPC npc : state.getNpcs()) {
				if (currentTick < 66) {
					initialNpcIndices.add(npc.getNpcIndex());
				} else if (!initialNpcIndices.contains(npc.getNpcIndex())) {
					reinforcementNpcIndices.add(npc.getNpcIndex());
				}

				if (currentWave > 3 && npc.getOrbSequence() != null && !npc.getOrbSequence().equalsIgnoreCase("Unknown")) {
					manticoreSequences.put(npc.getNpcIndex(), npc.getOrbSequence());
				}
			}
		}

		StringBuilder urlAffix = new StringBuilder();

		int endTick = startTick + duration;
		for (ColosseumStateDTO state : waveTimeline)
		{
			int curTick = state.getTick();

			if (curTick < startTick) {}

			else if (curTick == startTick)
			{
				urlAffix.append(convertInitialState(state, reinforcementNpcIndices, manticoreSequences));
			}

			else if (curTick <= endTick)
			{
				urlAffix.append(convertSubsequentState(state, curTick == duration));
			}

			else break;
		}

		// add _ws to set wave start flag to true if t0 is first tick
		if (startTick == 0)
			urlAffix.append(WAVE_START_AFFIX);

		return LosLink.URL_PREFIX + urlAffix;
	}

	public int getNWaves() {
		return nWaves;
	}

	/**
	 * Parses the file into memory, filtering out invalid states,
	 * and returns the highest wave number found.
	 */
	private int parseTimelineFile(File timelineFile)
	{
		if (timelineFile == null || !timelineFile.exists()) {
			return 0;
		}

		int highestWaveFound = 0;

		try (FileReader reader = new FileReader(timelineFile)) {
			JsonElement rootElement = new JsonParser().parse(reader);
			JsonArray jsonArray = rootElement.getAsJsonArray();

			if (jsonArray == null || jsonArray.size() == 0) {
				return 0;
			}

			for (JsonElement stateElement : jsonArray) {
				JsonObject stateObj = stateElement.getAsJsonObject();

				if (!stateObj.has("wave") || !stateObj.has("tick") ||
					!stateObj.has("playerX") || !stateObj.has("playerY")) {
					continue;
				}

				int wave = stateObj.get("wave").getAsInt();
				int tick = stateObj.get("tick").getAsInt();
				int playerX = stateObj.get("playerX").getAsInt();
				int playerY = stateObj.get("playerY").getAsInt();

				if (wave == -1 || tick == -1 || playerX == -1 || playerY == -1) {
					continue;
				}

				highestWaveFound = Math.max(highestWaveFound, wave);

				List<ColosseumNPC> filteredNpcs = new ArrayList<>();
				if (stateObj.has("npcs")) {
					JsonArray npcsArray = stateObj.getAsJsonArray("npcs");

					for (JsonElement npcElement : npcsArray) {
						JsonObject npcObj = npcElement.getAsJsonObject();

						if (shouldIncludeNpc(npcObj)) {
							ColosseumNPC npc = gson.fromJson(npcObj, ColosseumNPC.class);
							filteredNpcs.add(npc);
						}
					}
				}

				ColosseumStateDTO stateDTO = ColosseumStateDTO.builder()
					.wave(wave)
					.tick(tick)
					.playerX(playerX)
					.playerY(playerY)
					.playerHp(stateObj.has("playerHp") ? stateObj.get("playerHp").getAsInt() : -1)
					.playerPrayer(stateObj.has("playerPrayer") ? stateObj.get("playerPrayer").getAsInt() : -1)
					.npcs(filteredNpcs)
					.build();

				timelineMap.computeIfAbsent(wave, k -> new ArrayList<>()).add(stateDTO);
			}
		} catch (Exception e) {log.error("Failed to parse timeline from file: {}", timelineFile.getName(), e);}

		return highestWaveFound;
	}

	private boolean shouldIncludeNpc(JsonObject npcObj) {
		if (!npcObj.has("npcId")) {return false;}

		int npcId = npcObj.get("npcId").getAsInt();
		return LosLink.RELEVANT_NPC_IDS.contains(npcId);
	}

	/**
	 * Converts the initial state into a URL segment. The initial state encodes NPC IDs and locations.
	 */
	private String convertInitialState(ColosseumStateDTO state, Set<Integer> reinforcements, Map<Integer, String> manticoreSequences)
	{
		StringBuilder initialStateString = new StringBuilder();
		for (ColosseumNPC npc : state.getNpcs())
		{
			if (reinforcements.contains(npc.getNpcIndex()))
				npc.setReinforcements(true);

			else if (manticoreSequences.containsKey(npc.getNpcIndex()))
				npc.setSequenceRevealed(npc.getOrbSequence().equals("Unknown"));
				npc.setOrbSequence(manticoreSequences.get(npc.getNpcIndex()));

			initialStateString.append(getNpcUrlSegment(npc));
		}
		initialStateString.append("#");
		return initialStateString.toString();
	}

	/**
	 * Converts state 1-N to a URL segment
	 */
	private String convertSubsequentState(ColosseumStateDTO state, boolean isFinalState)
	{
		return remapPlayerXY(state.getPlayerX(), state.getPlayerY()) + (isFinalState ? "" : ".");
	}

	/**
	 * Return the String segment that encodes the x, y coordinates and the appropriate ID of the given ColosseumNPC.
	 */
	private String getNpcUrlSegment(ColosseumNPC npc)
	{
		return remapNpcX(npc.getX()) + remapNpcY(npc.getY()) + getUrlNpcId(npc) + ".";
	}

	/**
	 * Convert region X to an integer that represents the X-coordinate of an NPC in a replay-url
	 */
	private int remapNpcX(int x)
	{
		return x - LOS_LINK_CORRECTION_X;
	}

	/**
	 * Convert region Y to an integer that represents the Y-coordinate of an NPC in a replay-URL
	 */
	private int remapNpcY(int y)
	{
		return y - LOS_LINK_CORRECTION_Y;
	}

	/**
	 * Convert region x and region y to an integer that represents the player location as part of a replay URL.
	 */
	private String remapPlayerXY(int x, int y)
	{
		return String.valueOf((y - LOS_LINK_CORRECTION_Y) * 256 + x - LOS_LINK_CORRECTION_X);
	}

	/**
	 * Return the appropriate id affix used for the given Manticore in the URL, given its (yet-to-be-identified) sequence.
	 */
	private String getManticoreAffix(ColosseumNPC npc)
	{
		if (npc.isSequenceRevealed())
		{
			return STATIC_MANTICORE_AFFIXES.get(npc.getOrbSequence());
		}
		else
			return "u" + STATIC_MANTICORE_AFFIXES.get(npc.getOrbSequence());
	}

	/**
	 * Return the ID of a particular NPC that is to be used in the replay URL.
	 */
	private String getUrlNpcId(ColosseumNPC npc)
	{
		int npcId = npc.getNpcId();
		if (npcId == NpcID.COLOSSEUM_STANDARD_MAGER)
			return npc.isReinforcements() ? SERPENT_SHAMAN_REINFORCEMENTS_URL_ID : SERPENT_SHAMAN_URL_ID;
		else if (npcId == NpcID.COLOSSEUM_MANTICORE)
		{
			return MANTICORE_URL_ID + getManticoreAffix(npc);
		}
		else
			return STATIC_NPC_URL_IDS.get(npcId);
	}
}