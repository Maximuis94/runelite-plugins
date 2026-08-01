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

package com.slayerbanktab.services;

import static com.slayerbanktab.PluginConstants.MAX_TAB_ITEMS;
import com.slayerbanktab.models.ExtendedEquipmentSlot;
import java.awt.Point;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SetupGridBuilder {
	public static int[] buildDraftArray(Map<ExtendedEquipmentSlot, Integer> equipment, Integer[] inventory, List<Integer> auxiliary) {
		int[] buffer = new int[MAX_TAB_ITEMS];
		Arrays.fill(buffer, -1);
		int highestIndex = -1;

		if (equipment != null) {
			for (Map.Entry<ExtendedEquipmentSlot, Integer> entry : equipment.entrySet()) {
				Point p = getEquipmentCoordinate(entry.getKey());
				if (p != null) {
					int index = (p.y * 8) + p.x;
					buffer[index] = entry.getValue();
					highestIndex = Math.max(highestIndex, index);
				}
			}
		}

		if (inventory != null) {
			for (int i = 0; i < inventory.length; i++) {
				if (inventory[i] != null && inventory[i] > 0) {
					Point p = getInventoryCoordinate(i);
					if (p != null) {
						int index = (p.y * 8) + p.x;
						buffer[index] = inventory[i];
						highestIndex = Math.max(highestIndex, index);
					}
				}
			}
		}

		if (auxiliary != null) {
			for (int i = 0; i < auxiliary.size(); i++) {
				Point p = getAuxiliaryCoordinate(i);
				int index = (p.y * 8) + p.x;
				buffer[index] = auxiliary.get(i);
				highestIndex = Math.max(highestIndex, index);
			}
		}

		if (highestIndex == -1) return new int[0];
		return Arrays.copyOf(buffer, highestIndex + 1);
	}

	private static Point getEquipmentCoordinate(ExtendedEquipmentSlot slot) {
		switch (slot) {
			case HEAD:   return new Point(1, 0);
			case CAPE:   return new Point(0, 1);
			case AMULET: return new Point(1, 1);
			case AMMO:   return new Point(2, 1);
			case WEAPON: return new Point(0, 2);
			case BODY:   return new Point(1, 2);
			case SHIELD: return new Point(2, 2);
			case LEGS:   return new Point(1, 3);
			case GLOVES: return new Point(0, 4);
			case BOOTS:  return new Point(1, 4);
			case RING:   return new Point(2, 4);
			case QUIVER_AMMO: return new Point(2, 0);
			default:     return null;
		}
	}

	private static Point getInventoryCoordinate(int invIndex) {
		if (invIndex < 0 || invIndex > 27) return null;
		int xOffset = 4 + (invIndex % 4);
		int yOffset = invIndex / 4;
		return new Point(xOffset, yOffset);
	}

	private static Point getAuxiliaryCoordinate(int auxListIndex) {
		int startingRow = 8;
		int xOffset = auxListIndex % 8;
		int yOffset = startingRow + (auxListIndex / 8);
		return new Point(xOffset, yOffset);
	}
}