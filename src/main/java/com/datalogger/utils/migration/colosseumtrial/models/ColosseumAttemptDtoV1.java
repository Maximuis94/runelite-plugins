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

package com.datalogger.utils.migration.colosseumtrial.models;

import com.datalogger.dto.ColosseumWaveDTO;
import com.datalogger.dto.TrackedSuppliesDTO;
import com.datalogger.models.supplytracker.ValuedItemStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class ColosseumAttemptDtoV1 {
	@NonNull
	private String attemptId;
	private long timestamp;
	@NonNull
	private String accountName;
	private String tag;
	@NonNull
	private String result;
	private int rewardsValue;

	@Builder.Default
	private Map<String, ValuedItemStack> rewards = new LinkedHashMap<>();

	private int consumedSupplyValue;
	private TrackedSuppliesDTO consumedSupplies;
	private int totalGlory;
	private double totalTime;

	@Builder.Default
	private List<String> activeModifiers = new ArrayList<>();

	@Builder.Default
	private List<ColosseumWaveDtoV1> waves = new ArrayList<>();

	@Builder.Default
	private int version = 1;
}