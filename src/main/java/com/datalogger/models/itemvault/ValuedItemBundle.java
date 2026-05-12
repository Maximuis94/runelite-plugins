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

package com.datalogger.models.itemvault;

import lombok.Value;
import net.runelite.api.ItemComposition;

/**
 * Represents a quantity of a specific item
 */
@Value
public class ValuedItemBundle
{
	int itemId;
	String itemName;
	long quantity;
	long value;

	public ValuedItemBundle(int itemId, String itemName, int quantity, int price) {
		this.itemId = itemId;
		this.itemName = itemName;
		this.quantity = quantity;
		this.value = (long) price * quantity;
	}

	public ValuedItemBundle(int itemId, String itemName, long quantity, int price) {
		this.itemId = itemId;
		this.itemName = itemName;
		this.quantity = quantity;
		this.value = (long) price * quantity;
	}

	/**
	 * Constructs a new ItemBundle using the game's item composition blueprint.
	 *
	 * @param itemComp The ItemComposition fetched from the ItemManager.
	 * @param quantity The amount of this item.
	 */
	public static ValuedItemBundle fromComp(ItemComposition itemComp, int quantity, int price)
	{
		return new ValuedItemBundle(itemComp.getId(), itemComp.getName(), quantity, price);
	}
}