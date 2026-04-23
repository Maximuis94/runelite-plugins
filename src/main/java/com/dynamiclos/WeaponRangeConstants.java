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

package com.dynamiclos;

import java.util.Arrays;
import net.runelite.api.gameval.ItemID;

/**
 * Utility class that provides a mapping for base weapon range per item ID
 */
public final class WeaponRangeConstants {

	private WeaponRangeConstants() {
		throw new UnsupportedOperationException("Utility class cannot be instantiated");
	}

	private static final int[] WEAPON_RANGES = new int[40000];

	static {
		Arrays.fill(WEAPON_RANGES, 1);

		WEAPON_RANGES[ItemID.DRAGON_HALBERD] = 2;
		WEAPON_RANGES[ItemID.TWISTED_BOW] = 10;
	}

	/**
	 * Static method that can be called from anywhere without injecting the class.
	 */
	public static int getBaseRange(int weaponId) {
		if (weaponId < 0 || weaponId >= WEAPON_RANGES.length) {
			return 1;
		}
		return WEAPON_RANGES[weaponId];
	}
}