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
import com.datalogger.events.AccountSessionStarted;
import com.datalogger.services.FileIOService;
import java.io.File;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public abstract class AbstractLogger implements Loggable {
	@Inject protected Client client;
	@Inject protected FileIOService fileIOService;
	@Inject protected DataLoggerConfig config;

	// Currently active account
	private String accountName = "unknown";
	private String accountHashString = "-1";
	private long accountHashLong = -1;

	/**
	 * Logic for getting an account name, which is used as a folder in the file structure
	 */
	protected String getAccountName() {
		return accountName;
	}

	/**
	 * A numerical hash string associated with the OSRS account.
	 */
	protected String getAccountHashString() {
		return accountHashString;
	}

	/**
	 * A numerical hash string associated with the OSRS account.
	 */
	protected long getAccountHashLong() {
		return accountHashLong;
	}

	/**
	 * Centralized write method that every sub-logger will use.
	 * It automatically resolves the target file based on the logger type.
	 */
	protected void logRow(String csvRow) {
		if (!isEnabled() || accountName.equals("unknown")) {
			return;
		}

		File logFile = fileIOService.getTargetFile(getLogType(), accountName);
		fileIOService.atomicWrite(logFile, getCsvHeader(), csvRow);
	}

	/**
	 * Listens for our custom global session event.
	 * This guarantees the hash and name are valid, and it runs on the ClientThread.
	 */
	@Subscribe
	public void onAccountSessionStarted(AccountSessionStarted event) {
		accountName = event.getAccountName();
		accountHashString = event.getAccountHashString();
		accountHashLong = Long.parseLong(accountHashString);

		log.debug("[{}] Session initialized for {} ({})", getLogType(), accountName, accountHashString);

		if (isEnabled()) {
			setup();
		}
	}

	/**
	 * Resets account hash and cache
	 */
	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		if (event.getGameState() == GameState.LOGIN_SCREEN) {
			accountName = "unknown";
			accountHashString = "-1";
		}
	}
}