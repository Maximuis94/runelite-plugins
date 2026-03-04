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

import com.datalogger.dto.ColosseumStateDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;

/**
 * The state of the Colosseum at a particular tick during a particular wave. It encodes the coordinates of the player
 * and the NPCs, as well as the wave id and tick number
 */
@Data
@Builder
public class ColosseumState
{
	private int wave;
	private int tick;
	private WorldPoint playerLocation;
	private List<ColosseumNPC> npcs; // Changed to List for better API practice

	/**
	 * Converts this live engine state into a static DTO for saving.
	 */
	public ColosseumStateDTO toDTO() {
		return ColosseumStateDTO.builder()
			.wave(this.wave)
			.tick(this.tick)
			.playerX(this.playerLocation != null ? this.playerLocation.getX() : 0)
			.playerY(this.playerLocation != null ? this.playerLocation.getY() : 0)
			.npcs(this.npcs)
			.build();
	}
}