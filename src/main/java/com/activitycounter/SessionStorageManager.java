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

package com.activitycounter;

import com.activitycounter.models.Session;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Managing class for IO operations
 */
@Slf4j
@Singleton
public class SessionStorageManager {

	@Inject
	private Gson gson;

	/**
	 * Saves a session to the plugin's directory. Create said directory if it does not exist.
	 */
	public void saveSession(Session session) {
		File accountDir = new File(PluginConstants.PLUGIN_DIR, String.valueOf(session.getAccountHash()));
		if (accountDir.mkdirs())
			log.debug("Created account directory '{}'", accountDir.getAbsolutePath());

		File sessionFile = new File(accountDir, session.getId() + ".json");
		try (FileWriter writer = new FileWriter(sessionFile)) {
			gson.toJson(session, writer);
		} catch (IOException e) {
			log.error("Failed to save session to file", e);
		}
	}

	/**
	 * Loads a session from a specified file.
	 */
	public Session loadSession(File file) {
		if (!file.exists()) return null;

		try (FileReader reader = new FileReader(file)) {
			return gson.fromJson(reader, Session.class);
		} catch (IOException e) {
			log.error("Failed to load session from file", e);
			return null;
		}
	}

	/**
	 * Scans the specific account's directory to find an in-progress session.
	 */
	public Session loadActiveSession(long accountHash) {
		File accountDir = new File(PluginConstants.PLUGIN_DIR, String.valueOf(accountHash));
		if (!accountDir.exists() || !accountDir.isDirectory()) {
			log.debug("No directory exists for accountHash={}", accountHash);
			return null;
		}

		File[] files = accountDir.listFiles((dir, name) -> name.endsWith(".json"));
		if (files == null) {
			log.debug("No potential session files exist for accountHash={}", accountHash);
			return null;
		}

		for (File file : files) {
			Session session = loadSession(file);
			if (session != null && session.isInProgress()) {
				log.debug("Loading active session {} from file '{}'", session.getId(), file.getAbsolutePath());
				return session;
			}
		}
		log.debug("No active sessions found for accountHash={}, though archived sessions exists", accountHash);
		return null;
	}

	/**
	 * Loads all archived (not in-progress) sessions for the current account.
	 */
	public List<Session> loadArchivedSessions(long accountHash) {
		List<Session> archived = new ArrayList<>();
		File accountDir = new File(PluginConstants.PLUGIN_DIR, String.valueOf(accountHash));

		if (!accountDir.exists() || !accountDir.isDirectory()) {
			return archived;
		}

		archived.addAll(getAccountSessions(accountDir));
		archived.sort((s1, s2) -> s2.getStartTime().compareTo(s1.getStartTime()));
		return archived;
	}

	/**
	 * Loads all archived sessions across all account hashes.
	 */
	public List<Session> loadAllArchivedSessions() {
		List<Session> archived = new ArrayList<>();
		File pluginDir = PluginConstants.PLUGIN_DIR;

		if (!pluginDir.exists() || !pluginDir.isDirectory()) {
			return archived;
		}

		File[] accountDirs = pluginDir.listFiles(File::isDirectory);
		if (accountDirs != null) {
			for (File accountDir : accountDirs) {
				archived.addAll(getAccountSessions(accountDir));
			}
		}

		archived.sort((s1, s2) -> s2.getStartTime().compareTo(s1.getStartTime()));
		return archived;
	}

	/**
	 * Deletes the specified session file from the disk.
	 */
	public boolean deleteSession(Session session) {
		if (session == null) {
			return false;
		}

		File accountDir = new File(PluginConstants.PLUGIN_DIR, String.valueOf(session.getAccountHash()));
		File sessionFile = new File(accountDir, session.getId() + ".json");

		if (sessionFile.exists()) {
			return sessionFile.delete();
		}

		return false;
	}

	/**
	 * Returns a list of all Sessions in the given accountDir
	 */
	private List<Session> getAccountSessions(File accountDir)
	{
		List<Session> sessions = new ArrayList<>();
		File[] files = accountDir.listFiles((dir, name) -> name.endsWith(".json"));
		if (files != null) {
			for (File file : files) {
				Session session = loadSession(file);
				if (session != null) {
					sessions.add(session);
				}
			}
		}
		return sessions;
	}
}