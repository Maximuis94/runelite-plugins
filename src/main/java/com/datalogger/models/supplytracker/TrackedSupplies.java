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

package com.datalogger.models.supplytracker;

import com.datalogger.dto.TrackedSuppliesDTO;
import com.datalogger.models.enums.ConsumableItemGroup;
import java.util.HashMap;
import java.util.Map;
import lombok.Value;
import net.runelite.client.game.ItemManager;

@Value
public class TrackedSupplies
{
	Map<Integer, Integer> consumedItems;
	Map<ConsumableItemGroup, Integer> doses;

	int scytheAttacks;
	int shadowAttacks;

	public TrackedSuppliesDTO toDto(ItemManager itemManager)
	{
		Map<String, Integer> namedItems = new HashMap<>();
		if (consumedItems != null)
		{
			consumedItems.forEach((id, qty) -> {
				String itemName = itemManager.getItemComposition(id).getName();
				namedItems.put(itemName, qty);
			});
		}

		Map<String, Integer> namedDoses = new HashMap<>();
		if (doses != null)
		{
			doses.forEach((group, nDoses) -> {
				namedDoses.put(group.getBaseItemName(), nDoses);
			});
		}

		return TrackedSuppliesDTO.builder()
			.consumedItems(namedItems)
			.consumedDoses(namedDoses)
			.scytheAttacks(scytheAttacks)
			.shadowAttacks(shadowAttacks)
			.build();
	}
}
