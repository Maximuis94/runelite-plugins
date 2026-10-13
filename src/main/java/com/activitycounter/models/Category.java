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

package com.activitycounter.models;

import lombok.Getter;

@Getter
public enum Category
{
	BOSSES(5856),
	CHESTS(1108),
	OTHER(6390),
	CLUE(5853),
	SLAYER(216),
	AGILITY(204),
	BOLTS(358),
	LEVELS(222),
	EXPERIENCE(1198),
	RAIDS(4288),
	MINI_GAMES(1053),
	MAGIC_SPELLS(202),
	COMBAT(168),
	SUPPLIES(223),
	SKILLING(209),
	ACHIEVEMENTS(1298),
	SAILING(228),
	TERTIARY_DROPS(3018),
	RANDOM_EVENTS(7087);

	private final String header;
	private final int iconId;

	Category(int iconId)
	{
		String[] words = this.name().toLowerCase().split("_");
		StringBuilder sb = new StringBuilder();

		for (String word : words) {
			sb.append(Character.toUpperCase(word.charAt(0)))
				.append(word.substring(1))
				.append(" ");
		}
		this.iconId = iconId;
		this.header = sb.toString().trim().replace("_", " ");
	}
}