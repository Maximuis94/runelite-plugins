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

package com.datalogger.models.colosseum;

import static com.datalogger.constants.Colosseum.COLOSSEUM_TRIAL_TIMESTAMP_FORMATTER;
import static com.datalogger.constants.PluginConstants.COLOSSEUM_TRIALS_DIR;
import com.datalogger.dto.ColosseumAttemptDTO;
import com.datalogger.models.enums.ColosseumModifier;
import com.datalogger.models.enums.WaveStatus;
import com.datalogger.models.supplytracker.TrackedSupplies;
import com.datalogger.models.supplytracker.ValuedItemStack;
import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ColosseumAttempt
{
	private final long id;

	private final String accountName;

	private final int startTick;

	private final String startTime;

	@Setter
	private WaveStatus finalStatus;

	private final Map<Integer, ColosseumModifier> activeModifiers = new LinkedHashMap<>();

	private int consumedSupplyValue;

	private TrackedSupplies consumedSupplies;

	private int totalRewardsValue;

	private final Map<Integer, Integer> rewards = new HashMap<>();

	private Map<String, ValuedItemStack> namedRewards = null;

	@Setter
	private double totalTimeTaken;

	private final List<ColosseumWave> waves;

	private final String attemptId;

	private final File attemptRoot;

	private final File waveLogJsonFile;

	private final File waveLogCsvFile;

	private final String supplyFileName;


	public ColosseumAttempt(int startTick, String accountName)
	{
		long now = System.currentTimeMillis();
		id = now - (now % 1000L);
		this.startTick = startTick;
		startTime = Instant.ofEpochMilli(id)
			.atZone(ZoneId.systemDefault())
			.format(COLOSSEUM_TRIAL_TIMESTAMP_FORMATTER);
		waves = new ArrayList<>();
		finalStatus = WaveStatus.FAILED;
		this.accountName = accountName == null ? "N/A" : accountName;
		totalTimeTaken = .0;

		attemptId = String.format("%s_%s", this.accountName, startTime);
		attemptRoot = new File(COLOSSEUM_TRIALS_DIR, attemptId);
		waveLogJsonFile =  new File(attemptRoot, attemptId+"_wave-log.json");
		waveLogCsvFile =  new File(attemptRoot, attemptId+"_wave-log.csv");
		supplyFileName = attemptId + "_supply-log";
		consumedSupplyValue = 0;
	}

	/**
	 * Define the namedRewards and the summed rewards attributes that are to be used in the DTO
	 */
	public void setNamedRewards(Map<String, ValuedItemStack> namedRewards, int totalValue)
	{
		this.namedRewards = namedRewards;
		this.totalRewardsValue = totalValue;
	}

	/**
	 * Set the consumedSupplies and the consumedSupplyValue attributes
	 */
	public void setConsumedSupplies(TrackedSupplies supplies)
	{
		consumedSupplyValue = supplies.getTotalValue();
		consumedSupplies = supplies;
	}

	/**
	 * Convert the active modifiers to an ordered List of Strings
	 */
	private List<String> getActiveModifiersDTO()
	{
		Collection<ColosseumModifier> modifiers = activeModifiers.values();

		return modifiers.stream()
			.map(ColosseumModifier::name)
			.collect(Collectors.toList());
	}

	public ColosseumAttemptDTO toDTO() {
		return ColosseumAttemptDTO.builder()
			.attemptId(attemptId)
			.timestamp(id)
			.accountName(accountName)
			.result(finalStatus != null ? finalStatus.name() : "UNKNOWN")
			.rewardsValue(totalRewardsValue)
			.rewards(namedRewards)
			.consumedSupplyValue(consumedSupplyValue)
			.consumedSupplies(consumedSupplies != null ? consumedSupplies.toDto(null) : null)
			.totalGlory(waves.isEmpty() ? 0 : waves.get(waves.size() - 1).getTotalGlory())
			.totalTime(totalTimeTaken)
			.activeModifiers(getActiveModifiersDTO())
			.waves(waves.stream()
				.map(ColosseumWave::toDTO)
				.collect(Collectors.toList()))
			.build();
	}
}