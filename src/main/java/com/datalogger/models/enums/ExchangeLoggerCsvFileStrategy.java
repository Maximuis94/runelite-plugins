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

package com.datalogger.models.enums;

import com.datalogger.constants.PluginConstants;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ExchangeLoggerCsvFileStrategy
{
	NONE("No CSV"),
	CSV_SINGLE("Single CSV File"),
	CSV_DAILY("One file/day"),
	CSV_WEEKLY("One file/week"),
	CSV_MONTHLY("One file/month");

	private final String displayName;

	ExchangeLoggerCsvFileStrategy(String displayName)
	{
		this.displayName = displayName;
	}

	private static final Map<String, File> FILE_CACHE = new ConcurrentHashMap<>();

	private static final DateTimeFormatter DAILY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter WEEKLY_FORMAT = DateTimeFormatter.ofPattern("YYYY-ww");
	private static final DateTimeFormatter MONTHLY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

	/**
	 * Return the relevant jsonfile tied to the given strategy and accountName
	 */
	public File getCsvFile(String account)
	{
		String fileName;
		LocalDate now = LocalDate.now();

		switch (this)
		{
			case NONE:
				return null;
			case CSV_DAILY:
				fileName = "exchange-offers_" + now.format(DAILY_FORMAT) + ".json";
				break;
			case CSV_WEEKLY:
				fileName = "exchange-offers_" + now.format(WEEKLY_FORMAT) + ".json";
				break;
			case CSV_MONTHLY:
				fileName = "exchange-offers_" + now.format(MONTHLY_FORMAT) + ".json";
				break;
			case CSV_SINGLE:
			default:
				fileName = "exchange-offers.csv";
		}

		String accountLower = account.toLowerCase();

		String cacheKey = accountLower + "_" + fileName;

		return FILE_CACHE.computeIfAbsent(cacheKey, key -> {
			File root = new File(PluginConstants.GRAND_EXCHANGE_DIR, accountLower);
			return new File(root, fileName);
		});
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}