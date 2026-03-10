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

package com.datalogger.models.grandexchange;

import lombok.Builder;
import lombok.Data;
import net.runelite.api.GrandExchangeOfferState;

@Data
@Builder
public class ActiveGeOffer {
	private String accountName;
	private int slot;
	private int itemId;
	private String itemName;
	private boolean isBuy;
	private GrandExchangeOfferState state;

	private int totalQuantity;
	private int quantitySold;
	private int offerPrice;
	private int spent;

	/**
	 * GP from incomplete buy offers that has not yet been spent
	 */
	public int getStuckGP() {
		if (isBuy && state != GrandExchangeOfferState.EMPTY) {
			int totalReservedCash = totalQuantity * offerPrice;
			return totalReservedCash - spent;
		}
		return 0;
	}

	/**
	 * Items held by the GE that have not yet been sold.
	 * (Only applies to incomplete SELL offers).
	 */
	public int getStuckItems() {
		if (!isBuy && state != GrandExchangeOfferState.EMPTY) {
			return totalQuantity - quantitySold;
		}
		return 0;
	}
}