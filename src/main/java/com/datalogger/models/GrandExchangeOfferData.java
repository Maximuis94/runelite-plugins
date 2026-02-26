/*
 * Copyright (c) 2026, maximuis94 <https://github.com/maximuis94>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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
package com.datalogger.models;

import com.datalogger.framework.DataRow;
import lombok.Value;

@Value
public class GrandExchangeOfferData implements DataRow {
	int itemId;
	String timestamp;
	String timestampCreated;
	boolean isBuy;
	int quantity;
	int offerQuantity;
	long price;
	long offerPrice;
	long value;
	String account;
	int slot;

	public static GrandExchangeOfferData fromCsv(String csvLine) {
		try {
			String[] parts = csvLine.split(",");
			return new GrandExchangeOfferData(
				Integer.parseInt(parts[0]), // itemId
				parts[1],                   // timestamp
				parts[2],                   // timestampCreated
				Boolean.parseBoolean(parts[3]), // isBuy
				Integer.parseInt(parts[4]), // quantity
				Integer.parseInt(parts[5]), // totalQuantity
				Long.parseLong(parts[6]),    // price
				Long.parseLong(parts[7]),    // offerPrice
				Long.parseLong(parts[8]),    // value
				parts[9],                   // account
				Integer.parseInt(parts[10]) // slot
			);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String toCsvRow() {
		return String.format("%d,%s,%s,%b,%d,%d,%d,%d,%d,%s,%d",
			itemId, timestamp, timestampCreated, isBuy, quantity,
			offerQuantity, price, offerPrice, value, account, slot);
	}
}
