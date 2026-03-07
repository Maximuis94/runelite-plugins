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

package com.datalogger.services;

import com.datalogger.models.grandexchange.GeLedgerEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GeHistoryReconciler {

	public List<GeLedgerEntry> weaveLedgers(List<GeLedgerEntry> masterLedger, List<GeLedgerEntry> scrapedHistory) {
		List<GeLedgerEntry> updatedLedger = new ArrayList<>(masterLedger);
		Set<Integer> consumedIndices = new HashSet<>();

		int j = Math.max(0, updatedLedger.size() - 200);

		for (GeLedgerEntry scraped : scrapedHistory) {
			boolean foundMatch = false;

			for (int i = j; i < updatedLedger.size(); i++) {
				if (consumedIndices.contains(i)) {
					continue;
				}

				GeLedgerEntry existing = updatedLedger.get(i);

				if (isExactMatch(existing, scraped)) {
					foundMatch = true;
					consumedIndices.add(i);

					if (!existing.isHistoryEntry()) {
						existing.setTax(scraped.getTax());
						existing.setPrice(scraped.getPrice());
						existing.setValue(scraped.getValue());
					}

					j = i + 1;
					break;
				}
			}

			if (!foundMatch) {
				updatedLedger.add(scraped);
				j = updatedLedger.size();
			}
		}

		return updatedLedger;
	}

	private boolean isExactMatch(GeLedgerEntry existing, GeLedgerEntry scraped) {
		return existing.getItemId() == scraped.getItemId() &&
			existing.isBuy() == scraped.isBuy() &&
			existing.getQuantity() == scraped.getQuantity() &&
			existing.getValue() == scraped.getValue();
	}
}