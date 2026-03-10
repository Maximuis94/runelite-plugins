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
package com.datalogger.loggers;

import com.datalogger.DataLoggerConfig;
import com.datalogger.events.ColosseumAttemptEnded;
import com.datalogger.events.ColosseumWaveEnded;
import com.datalogger.events.ColosseumWaveStarted;
import com.datalogger.models.colosseum.ColosseumState;
import com.datalogger.services.ColosseumScanner;
import com.datalogger.services.FileIOService;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

/**
 * Timeline logger of the colosseum logger.
 * The timeline is composed of states that are generated every tick.
 * A state describes player- and NPC data, NPC data is restricted to a subset of NPCs, of which may be enabled via
 * plugin configurations.
 *
 */
@Slf4j
@Singleton
public class ColosseumTimelineLogger {
	@Inject private ColosseumScanner scanner;
	@Inject private FileIOService fileIOService;
	@Inject private DataLoggerConfig config;

	private boolean isRecording = false;
	private int currentWave;
	private int waveStartTick;
	private final List<ColosseumState> states = new ArrayList<>();

	/**
	 * Invoked at wave start by the ColosseumAttemptLogger. Ensures the appropriate flags and basic data are set and
	 * that the states List is empty.
	 */
	@Subscribe
	public void onColosseumWaveStarted(ColosseumWaveStarted event) {
		currentWave = event.getWaveNumber();
		waveStartTick = event.getStartTick();
		states.clear();
		isRecording = true;
	}

	/**
	 * Scan and store game state data on a particular tick
	 */
	@Subscribe
	public void onGameTick(GameTick event) {
		if (!isRecording || !config.logWaveTimeline()) return;

		states.add(scanner.scanCurrentState(currentWave, waveStartTick));
	}

	@Subscribe
	public void onColosseumWaveEnded(ColosseumWaveEnded event) {
		if (!isRecording) return;
		isRecording = false;

		if (states.isEmpty()) return;

		List<ColosseumState> snapshot = new ArrayList<>(states);
		fileIOService.saveWaveStates(event.getAttemptId(), event.getWaveNumber(), snapshot);
		states.clear();
	}

	@Subscribe
	public void onColosseumAttemptEnded(ColosseumAttemptEnded event) {
		if (config.logWaveTimeline()) {
			fileIOService.mergeTimelineFiles(event.getAttemptId());
		}
	}
}