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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.slayerbanktab.PluginConstants;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.client.config.ConfigManager;

@Slf4j
@Singleton
public class AccountHashManager {

	private static final String CONFIG_KEY = "account_hash_names";

	private final Client client;
	private final ConfigManager configManager;
	private final Gson gson;

	private final Map<Long, String> accountMap = new HashMap<>();
	private boolean currentSessionCaptured = false;

	@Inject
	public AccountHashManager(Client client, ConfigManager configManager, Gson gson) {
		this.client = client;
		this.configManager = configManager;
		this.gson = gson;
	}

	public void load() {
		accountMap.clear();
		String json = configManager.getConfiguration(PluginConstants.CONFIG_GROUP, CONFIG_KEY);
		if (json != null && !json.trim().isEmpty()) {
			try {
				Type type = new TypeToken<Map<Long, String>>(){}.getType();
				Map<Long, String> loaded = gson.fromJson(json, type);
				if (loaded != null) {
					accountMap.putAll(loaded);
				}
			} catch (Exception e) {
				log.error("Failed to deserialize account hash mapping", e);
			}
		}
	}

	private void save() {
		try {
			String json = gson.toJson(accountMap);
			configManager.setConfiguration(PluginConstants.CONFIG_GROUP, CONFIG_KEY, json);
		} catch (Exception e) {
			log.error("Failed to serialize account hash mapping", e);
		}
	}

	/**
	 * Resets the session lock when the user drops to the login screen.
	 */
	public void onGameStateChanged(GameState gameState) {
		if (gameState == GameState.LOGIN_SCREEN || gameState == GameState.LOGGING_IN) {
			this.currentSessionCaptured = false;
		}
	}

	/**
	 * Designed to be called on ClientTick. Cost is ~0.0001ms once session is captured.
	 * Returns TRUE only on the exact tick a name is newly acquired or updated.
	 */
	public boolean checkTickRegistration() {
		if (currentSessionCaptured || client.getGameState() != GameState.LOGGED_IN) {
			return false;
		}

		Player localPlayer = client.getLocalPlayer();
		if (localPlayer == null || localPlayer.getName() == null) {
			return false;
		}

		long hash = client.getAccountHash();
		if (hash == -1) return false;

		String loggedInName = localPlayer.getName();
		this.currentSessionCaptured = true;

		if (loggedInName.equals(accountMap.get(hash))) {
			return false;
		}

		accountMap.put(hash, loggedInName);
		save();
		log.debug("Mapped Account Hash {} to Name: '{}'", hash, loggedInName);
		return true;
	}

	/**
	 * Returns the player's real name if known, otherwise a clean fallback like "Account (..3255)"
	 */
	public String getAccountDisplayName(long hash) {
		String knownName = accountMap.get(hash);
		if (knownName != null) return knownName;

		String raw = String.valueOf(hash);
		return raw.length() <= 4 ? ("Account #" + raw) : ("Account (.." + raw.substring(raw.length() - 4) + ")");
	}

	public Map<Long, String> getSnapshot() {
		return Collections.unmodifiableMap(new HashMap<>(accountMap));
	}
}