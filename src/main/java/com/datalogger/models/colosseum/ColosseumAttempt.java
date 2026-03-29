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

import static com.datalogger.constants.Colosseum.Item.DIZANAS_QUIVER_REWARD;
import static com.datalogger.constants.Colosseum.Item.DIZANAS_QUIVER_SWAPPED_REWARD;
import static com.datalogger.constants.PluginConstants.COLOSSEUM_ATTEMPT_DIR;
import com.datalogger.dto.ColosseumAttemptDTO;
import com.datalogger.models.enums.WaveStatus;
import com.datalogger.models.itemvault.ItemBundle;
import com.datalogger.models.supplytracker.TrackedSupplies;
import com.datalogger.models.supplytracker.ValuedItemStack;
import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ColosseumAttempt
{
	private final long id;

	private final String account;

	private final int startTick;

	private final String startTime;

	@Setter
	private WaveStatus finalStatus;

	private int consumedSupplyValue;

	private TrackedSupplies consumedSupplies;

	private int totalRewardsValue;

	private final Map<Integer, Integer> rewards = new HashMap<>();

	private Map<String, ValuedItemStack> namedRewards = null;

	private double totalTimeTaken;

	private final List<ColosseumWave> waves;

	private final String attemptId;

	private final File attemptRoot;

	private final File waveLogJsonFile;

	private final File waveLogCsvFile;

	private final String supplyFileName;


	public ColosseumAttempt(int startTick, String accountName)
	{
		id = System.currentTimeMillis();
		this.startTick = startTick;
		startTime = Instant.ofEpochMilli(id)
			.atZone(ZoneId.systemDefault())
			.format(DateTimeFormatter.ofPattern("yyMMdd_HHmmss"));
		waves = new ArrayList<>();
		finalStatus = WaveStatus.FAILED;
		account = accountName;
		totalTimeTaken = .0;

		attemptId = String.format("%s_%s", account, startTime);
		attemptRoot = new File(COLOSSEUM_ATTEMPT_DIR, attemptId);
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
	 * Add the given wave to the list of waves, provided it has not been submitted yet.
	 * Additionally, add its loot to the Attempt rewards stack.
	 */
	public void submitWave(ColosseumWave wave, boolean swapQuiver)
	{
		if (waves.isEmpty() || waves.get(waves.size() - 1).getWave() != wave.getWave())
		{
			waves.add(wave);
			totalTimeTaken = wave.getTotalTimeTaken();

			if (wave.getStatus() == WaveStatus.COMPLETED)
			{
				ItemBundle bundle = wave.getEarnedLoot();
				rewards.merge(bundle.getItemId(), bundle.getQuantity(), Integer::sum);

				if (wave.getWave() == 12)
				{
					ItemBundle guaranteedReward = swapQuiver ? DIZANAS_QUIVER_SWAPPED_REWARD : DIZANAS_QUIVER_REWARD;
					rewards.merge(guaranteedReward.getItemId(), guaranteedReward.getQuantity(), Integer::sum);
				}
			}
		}
	}

	public ColosseumAttemptDTO toDTO() {
		return ColosseumAttemptDTO.builder()
			.attemptId(id)
			.timestamp(id)
			.result(finalStatus != null ? finalStatus.name() : "UNKNOWN")
			.rewardsValue(totalRewardsValue)
			.rewards(namedRewards)
			.consumedSupplyValue(consumedSupplyValue)
			.consumedSupplies(consumedSupplies != null ? consumedSupplies.toDto(null) : null)
			.totalGlory(waves.isEmpty() ? 0 : waves.get(waves.size() - 1).getTotalGlory())
			.waves(waves.stream()
				.map(ColosseumWave::toDTO)
				.collect(Collectors.toList()))
			.build();
	}
}