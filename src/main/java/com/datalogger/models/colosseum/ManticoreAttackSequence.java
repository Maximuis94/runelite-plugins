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

import static com.datalogger.constants.Colosseum.ManticoreAttack.MAGIC_ORB_ID;
import static com.datalogger.constants.Colosseum.ManticoreAttack.MELEE_ORB_ID;
import static com.datalogger.constants.Colosseum.ManticoreAttack.NO_ORB_ID;
import static com.datalogger.constants.Colosseum.ManticoreAttack.RANGED_ORB_ID;
import lombok.Getter;
import net.runelite.api.ActorSpotAnim;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ManticoreAttackSequence {

	@Getter
	public enum ManticoreOrb {
		MAGIC(MAGIC_ORB_ID),
		RANGE(RANGED_ORB_ID),
		MELEE(MELEE_ORB_ID),
		UNKNOWN(NO_ORB_ID);

		private final int id;

		ManticoreOrb(int id) {
			this.id = id;
		}

		public static ManticoreOrb fromId(int id) {
			for (ManticoreOrb orb : values()) {
				if (orb.getId() == id) return orb;
			}
			return UNKNOWN;
		}
	}

	private final List<ManticoreOrb> orbs;

	/**
	 * The constructor takes the raw SpotAnims from the engine,
	 * sorts them by height, and safely maps them to our Enums!
	 */
	public ManticoreAttackSequence(List<ActorSpotAnim> rawSpotAnims) {
		this.orbs = new ArrayList<>();

		// Sort by height ascending (Bottom to Top)
		rawSpotAnims.sort(Comparator.comparingInt(ActorSpotAnim::getHeight));

		for (ActorSpotAnim anim : rawSpotAnims) {
			this.orbs.add(ManticoreOrb.fromId(anim.getId()));
		}
	}

	/**
	 * Convert animation IDs to readable Strings, e.g. MAGIC-RANGE-MELEE
	 */
	public String toCsvFormat() {
		return orbs.stream()
			.map(Enum::name)
			.collect(Collectors.joining("-"));
	}
}