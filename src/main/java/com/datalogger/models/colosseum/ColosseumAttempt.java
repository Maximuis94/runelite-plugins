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

import com.datalogger.dto.ColosseumAttemptDTO;
import com.datalogger.models.colosseum.enums.WaveStatus;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ColosseumAttempt
{
	private final long id;

	private final String startTime;

	@Setter
	private WaveStatus finalStatus;

	private final List<ColosseumWave> waves;


	public ColosseumAttempt()
	{
		id = System.currentTimeMillis();
		startTime = Instant.ofEpochMilli(id)
			.atZone(ZoneId.systemDefault())
			.format(DateTimeFormatter.ofPattern("yyMMdd_HHmmss"));
		waves = new ArrayList<>();
		finalStatus = WaveStatus.FAILED;
	}

	public ColosseumAttemptDTO toDTO() {
		return ColosseumAttemptDTO.builder()
			.attemptId(this.id)
			.timestamp(this.id)
			.result(this.finalStatus != null ? this.finalStatus.name() : "UNKNOWN")
			.totalGlory(this.waves.isEmpty() ? 0 : this.waves.get(this.waves.size() - 1).getTotalGlory())
			.waves(this.waves.stream()
				.map(ColosseumWave::toDTO)
				.collect(Collectors.toList()))
			.build();
	}

	/**
	 * Add the given wave to the list of waves, provided it has not been submitted yet.
	 */
	public void submitWave(ColosseumWave wave)
	{
		if (waves.isEmpty())
		{
			this.waves.add(wave);
		}
		else
		{
			ColosseumWave lastLogged = waves.get(waves.size() - 1);
			if (lastLogged.getWave() != wave.getWave())
				waves.add(wave);
		}
	}

}
