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
import com.slayerbanktab.models.LayoutMode;
import java.awt.Point;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SetupGridBuilder {

	public static int[] buildDraftArray(Map<ExtendedEquipmentSlot, Integer> equipment, Integer[] inventory, List<Integer> auxiliary, LayoutMode layoutMode) {
		int[] buffer = new int[MAX_TAB_ITEMS];
		Arrays.fill(buffer, -1);
		int highestIndex = -1;
		int nRows = 0;

		Set<Integer> existingItems = new HashSet<>();

		if (equipment != null) {
			int nEquipment = 0;
			int maxEquipY = -1;

			for (ExtendedEquipmentSlot slot : ExtendedEquipmentSlot.values()) {
				Integer itemId = equipment.get(slot);
				if (itemId != null && itemId > 0) {
					existingItems.add(itemId);
					Point p;
					if (layoutMode == LayoutMode.ZIGZAG) {
						p = getZigZagCoordinate(nEquipment, 0);
					} else {
						p = getEquipmentCoordinate(slot, layoutMode);
					}

					if (p != null) {
						int index = (p.y * 8) + p.x;
						buffer[index] = itemId;
						highestIndex = Math.max(highestIndex, index);
						maxEquipY = Math.max(maxEquipY, p.y);
					}
					nEquipment++;
				}
			}
			nRows = maxEquipY + 1;
		}

		if (inventory != null) {
			int maxInvY = nRows > 0 ? nRows - 1 : 0;
			int zigZagIndex = 0;

			for (int i = 0; i < inventory.length; i++) {
				int itemId = inventory[i] != null ? inventory[i] : -1;
				if (itemId > 0) {
					existingItems.add(itemId);
					Point p;
					if (layoutMode == LayoutMode.ZIGZAG) {
						p = getZigZagCoordinate(zigZagIndex, nRows);
						zigZagIndex++;
					} else {
						p = getInventoryCoordinate(i, layoutMode);
					}

					if (p != null) {
						int index = (p.y * 8) + p.x;
						buffer[index] = itemId;
						highestIndex = Math.max(highestIndex, index);
						maxInvY = Math.max(maxInvY, p.y);
					}
				}
			}
			nRows = maxInvY + 1;
		}

		if (auxiliary != null) {
			int auxIndex = 0;
			for (int i = 0; i < auxiliary.size(); i++) {
				int auxId = auxiliary.get(i);

				if (auxId <= 0 || existingItems.contains(auxId)) {
					continue;
				}
				existingItems.add(auxId);

				Point p;
				if (layoutMode == LayoutMode.ZIGZAG) {
					p = getZigZagCoordinate(auxIndex, nRows);
				} else {
					p = getAuxiliaryCoordinate(auxIndex, layoutMode);
				}

				if (p != null) {
					int index = (p.y * 8) + p.x;
					if (index < buffer.length) {
						buffer[index] = auxId;
						highestIndex = Math.max(highestIndex, index);
					}
				}
				auxIndex++;
			}
		}

		if (highestIndex == -1) return new int[0];
		return Arrays.copyOf(buffer, highestIndex + 1);
	}

	private static Point getEquipmentCoordinate(ExtendedEquipmentSlot slot, LayoutMode mode) {
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

	private static Point getZigZagCoordinate(int i, int startRow) {
		return new Point((i % 14) / 2, startRow + (i % 2) + (i / 14) * 2);
	}

	private static Point getInventoryCoordinate(int invIndex, LayoutMode mode) {
		if (invIndex < 0 || invIndex > 27) return null;
		int xOffset = 4 + (invIndex % 4);
		int yOffset = invIndex / 4;
		return new Point(xOffset, yOffset);
	}

	private static Point getAuxiliaryCoordinate(int auxListIndex, LayoutMode mode) {
		int startingRow = (mode == LayoutMode.ZIGZAG) ? 6 : 8;
		int xOffset = auxListIndex % 8;
		int yOffset = startingRow + (auxListIndex / 8);
		return new Point(xOffset, yOffset);
	}
}