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

import com.datalogger.framework.DataRow;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An entry of the Grand Exchange History.
 * Exact temporal data is not known from the UI, so it is represented as a timespan
 * deduced from surrounding known transactions. If the exact time is known,
 * earliestTime and latestTime will be identical.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrandExchangeHistoryEntry implements DataRow {
	private int itemId;
	private boolean isBuy;
	private int quantity;
	private int price;
	private String account;

	// The Timespan Bounds
	private Instant earliestTime;
	private Instant latestTime;

	/**
	 * Converts the entry into a CSV-friendly string for your FileIOService.
	 */
	@Override
	public String toCsvRow() {
		// Handle null bounds gracefully (e.g., if earliestTime couldn't be deduced)
		String earliestStr = earliestTime != null ? earliestTime.toString() : "";
		String latestStr = latestTime != null ? latestTime.toString() : "";

		// Format: itemId, isBuy, quantity, price, account, earliestTime, latestTime
		return String.format("%d,%b,%d,%d,%s,%s,%s",
			itemId,
			isBuy,
			quantity,
			price,
			account,
			earliestStr,
			latestStr
		);
	}

	/**
	 * Generates a unique fingerprint for this transaction based on its raw UI data.
	 * Used for reconciling and preventing duplicate logs.
	 */
	public String getFingerprint() {
		return itemId + "_" + (isBuy ? "BUY" : "SELL") + "_" + quantity + "_" + price;
	}

	/**
	 * Rebuilds an entry from a raw CSV string.
	 * Expected format: itemId,isBuy,quantity,price,account,earliestTime,latestTime
	 */
	public static GrandExchangeHistoryEntry fromCsvRow(String csvLine) {
		if (csvLine == null || csvLine.trim().isEmpty()) return null;

		String[] parts = csvLine.split(",", -1); // -1 keeps trailing empty strings
		if (parts.length < 7) return null;

		try {
			GrandExchangeHistoryEntry entry = GrandExchangeHistoryEntry.builder()
				.itemId(Integer.parseInt(parts[0]))
				.isBuy(Boolean.parseBoolean(parts[1]))
				.quantity(Integer.parseInt(parts[2]))
				.price(Integer.parseInt(parts[3]))
				.account(parts[4])
				.build();

			// Safely parse the optional timestamps
			if (!parts[5].isEmpty()) entry.setEarliestTime(Instant.parse(parts[5]));
			if (!parts[6].isEmpty()) entry.setLatestTime(Instant.parse(parts[6]));

			return entry;
		} catch (Exception e) {
			return null;
		}
	}
}