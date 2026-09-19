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
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.util.Filepath;

/**
 * Managing class for IO operations
 */
@Slf4j
@Singleton
public class SessionStorageManager {

	@Inject
	private Gson gson;

	@Inject
	private ActivityCounterPlugin plugin;

	/**
	 * Saves a session to the plugin's directory. Creates said directory if it does not exist.
	 */
	public void saveSession(Session session) {
		Filepath accountDir = plugin.getDirectory().join(String.valueOf(session.getAccountHash()));

		if (!accountDir.exists()) {
			try
			{
				accountDir.createDirectories();
				log.debug("Created account directory '{}'", accountDir.toString());
			}
			catch (IOException e)
			{
				log.error("Failed to create account directory '{}'", accountDir, e);
				return;
			}
		}

		Filepath sessionFile = accountDir.join(session.getId() + ".json");

		try (Writer writer = sessionFile.openBufferedWriter()) {
			gson.toJson(session, writer);
		} catch (Exception e) {
			log.error("Failed to save session to file", e);
		}
	}

	/**
	 * Loads a session from a specified Filepath.
	 */
	public Session loadSession(Filepath file) {
		if (!file.exists()) return null;

		try (Reader reader = file.openBufferedReader()) {
			return gson.fromJson(reader, Session.class);
		} catch (Exception e) {
			log.error("Failed to load session from file", e);
			return null;
		}
	}

	/**
	 * Scans the specific account's directory to find an in-progress session.
	 */
	public Session loadActiveSession(long accountHash) {
		Filepath accountDir = plugin.getDirectory().join(String.valueOf(accountHash));

		if (!accountDir.exists() || !accountDir.isDirectory()) {
			log.debug("No directory exists for accountHash={}", accountHash);
			return null;
		}

		try (Stream<Filepath> files = accountDir.walk(1)) {
			List<Filepath> sessionFiles = files
				.filter(f -> f.isFile() && f.getFileName().endsWith(".json"))
				.collect(Collectors.toList());

			if (sessionFiles.isEmpty()) {
				log.debug("No potential session files exist for accountHash={}", accountHash);
				return null;
			}

			for (Filepath file : sessionFiles) {
				Session session = loadSession(file);
				if (session != null && session.isInProgress()) {
					log.debug("Loading active session {} from file '{}'", session.getId(), file.toString());
					return session;
				}
			}
		} catch (Exception e) {
			log.error("Failed to scan for active sessions", e);
		}

		log.debug("No active sessions found for accountHash={}, though archived sessions exists", accountHash);
		return null;
	}

	/**
	 * Loads all archived (not in-progress) sessions for the current account.
	 */
	public List<Session> loadArchivedSessions(long accountHash) {
		List<Session> archived = new ArrayList<>();
		Filepath accountDir = plugin.getDirectory().join(String.valueOf(accountHash));

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
		Filepath pluginDir = plugin.getDirectory();

		if (!pluginDir.exists() || !pluginDir.isDirectory()) {
			return archived;
		}

		try (Stream<Filepath> dirs = pluginDir.walk(1)) {
			dirs.filter(Filepath::isDirectory)
				.filter(d -> !d.equals(pluginDir))
				.forEach(accountDir -> {
					archived.addAll(getAccountSessions(accountDir));
				});
		} catch (Exception e) {
			log.error("Failed to load all archived sessions", e);
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

		Filepath accountDir = plugin.getDirectory().join(String.valueOf(session.getAccountHash()));
		Filepath sessionFile = accountDir.join(session.getId() + ".json");

		if (sessionFile.exists()) {
			try {
				sessionFile.delete();
				return true;
			} catch (Exception e) {
				log.error("Failed to delete session file", e);
				return false;
			}
		}

		return false;
	}

	/**
	 * Returns a list of all Sessions in the given accountDir.
	 */
	private List<Session> getAccountSessions(Filepath accountDir) {
		List<Session> sessions = new ArrayList<>();

		try (Stream<Filepath> files = accountDir.walk(1)) {
			files.filter(f -> f.isFile() && f.getFileName().endsWith(".json"))
				.forEach(file -> {
					Session session = loadSession(file);
					if (session != null) {
						sessions.add(session);
					}
				});
		} catch (Exception e) {
			log.error("Failed to list account sessions in dir {}", accountDir.toString(), e);
		}

		return sessions;
	}
}