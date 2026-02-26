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
package com.datalogger.framework;

import com.datalogger.DataLoggerConfig;
import com.datalogger.services.FileIOService;
import java.io.File;
import java.io.IOException;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;

@Slf4j
public abstract class AbstractLogger implements Loggable {
	@Inject protected Client client;
	@Inject protected FileIOService utils;
	@Inject protected DataLoggerConfig config;

	private String accountNameCache;

	/**
	 * Logic for getting an account name, which is used as a folder in the file structure
	 */
	protected String getAccountName() {
		if (accountNameCache != null && !accountNameCache.equals("unknown")) {
			return accountNameCache;
		}
		Player player = client.getLocalPlayer();
		accountNameCache = (player != null && player.getName() != null) ?
			player.getName() : "unknown";
		return accountNameCache;
	}

	public void setAccountName(String name) {
		this.accountNameCache = name;
	}

	/**
	 * Centralized write method that every sub-logger will use.
	 * It automatically resolves the target file based on the logger type.
	 */
	protected void logRow(String csvRow) {
		String currentAccount = getAccountName();
		// Uses the utility to determine the file path: root/type/account/date.csv
		File logFile = utils.getTargetFile(getLogType(), currentAccount);

		try {
			utils.atomicWrite(logFile, getCsvHeader(), csvRow);
		} catch (IOException e) {
			log.error("Error writing {} log for {}", getLogType(), currentAccount, e);
		}
	}

	/**
	 * A numerical hash string associated with the OSRS account. Uses account name as fallback.
	 */
	protected String getAccountHashString() {
		try {
			return String.valueOf(client.getClass().getMethod("getAccountHash").invoke(client));
		} catch (Exception e) {
			return getAccountName().toLowerCase().replace(" ", "_");
		}
	}
}