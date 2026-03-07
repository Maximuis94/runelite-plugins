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
package com.datalogger.services;

import com.datalogger.DataLoggerConfig;
import com.datalogger.dto.ColosseumStateDTO;
import com.datalogger.framework.LogType;
import com.datalogger.models.colosseum.ColosseumAttempt;
import com.datalogger.models.colosseum.ColosseumState;
import com.datalogger.models.colosseum.ColosseumWave;
import com.datalogger.models.grandexchange.ActiveGeOffer;
import com.datalogger.models.grandexchange.GeLedgerEntry;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.RuneLite;

@Slf4j
@Singleton
public class FileIOService
{
	private final Gson gson;

	private final ScheduledExecutorService executor;

	private final Map<File, Queue<String>> failedWrites = new ConcurrentHashMap<>();

	@Inject
	private FileIOService(DataLoggerConfig config, Gson gson, ScheduledExecutorService executor) {
		this.executor = executor;
		this.gson = gson.newBuilder()
			.registerTypeAdapter(WorldPoint.class, new WorldPointSerializer())
			.setPrettyPrinting()
			.create();
	}


	public final File PLUGIN_ROOT = new File(RuneLite.RUNELITE_DIR, "data-logger");
	private final File INTERNAL_ROOT_DIR = new File(PLUGIN_ROOT, "internal");
	private final File INTERNAL_GE_DIR = new File(INTERNAL_ROOT_DIR, "ge-history");
	private final File GE_STATE_DIR = new File(INTERNAL_ROOT_DIR, "state");
	private final File INTERNAL_ACTIVE_OFFERS_DIR = new File(INTERNAL_ROOT_DIR, "active-offers");
	private final File COLOSSEUM_ROOT_DIR = new File(PLUGIN_ROOT, "colosseum");
	private final File COLOSSEUM_TIMELINE_DIR = new File(COLOSSEUM_ROOT_DIR, "timeline");
	private final File COLOSSEUM_LOG_DIR = new File(COLOSSEUM_ROOT_DIR, "log");
	private final File COLOSSEUM_CSV_DIR = new File(COLOSSEUM_ROOT_DIR, "csv");
	private final File COLOSSEUM_TEMP_DIR = new File(COLOSSEUM_ROOT_DIR, "temp");
	private final File SCREENSHOT_DIR = new File(PLUGIN_ROOT, "screenshot");
	private final File COLOSSEUM_SCREENSHOT_DIR = new File(COLOSSEUM_ROOT_DIR, "screenshot");

	/**
	 * Appends a row to a CSV file. If the file is locked, it queues the row and attempts to flush the queue on the
	 * next write attempt.
	 */
	public void atomicWrite(File file, String header, String row) {
		executor.submit(() -> {
			Queue<String> backlog = failedWrites.computeIfAbsent(file, k -> new ConcurrentLinkedQueue<>());
			backlog.add(row);

			try {
				File parentDir = file.getParentFile();
				if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
					log.error("Failed to create log directory: {}", parentDir.getAbsolutePath());
					return;
				}

				boolean isNewFile = !file.exists();

				try (FileWriter fw = new FileWriter(file, true);
					 BufferedWriter bw = new BufferedWriter(fw);
					 PrintWriter out = new PrintWriter(bw)) {

					if (isNewFile && header != null) {
						out.println(header);
					}

					String queuedRow;
					while ((queuedRow = backlog.poll()) != null) {
						out.println(queuedRow);
					}

					log.debug("Successfully wrote to {}. Remaining backlog: {}", file.getName(), backlog.size());
				}
			} catch (IOException e) {
				log.warn("File {} is currently locked or inaccessible. Queued {} rows for next attempt.",
					file.getName(), backlog.size());
			}
		});
	}

	/**
	 * Returns a File reference based on the input provided and the plugin configurations
	 * @param type The data that is to be logged
	 * @param account The account that the data relates to
	 * @return File reference based on inputs provided and strategy selected
	 */
	public File getTargetFile(LogType type, String account) {
		String date = java.time.LocalDate.now().toString();
		File accountDir = new File(type.getLogDirectory(), account.toLowerCase());

		// Ensure the directory exists
		if (!accountDir.exists()) accountDir.mkdirs();

		return new File(accountDir, type.getDirectoryName() + "_" + date + ".csv");
	}

	/**
	 * Ensures the state directory exists and returns the file path for an account's state
	 */
	private File getStateFile(String accountHash) {
		if (!GE_STATE_DIR.exists()) {
			GE_STATE_DIR.mkdirs();
		}
		return new File(GE_STATE_DIR, "state_" + accountHash + ".properties");
	}

	/**
	 * Parse the properties file associated with the given account hash
	 * @param accountHash The account hash that indicates which file is to be parsed
	 * @return The contents of the parsed properties file
	 */
	public Properties getAccountState(String accountHash) {
		Properties props = new Properties();
		File file = getStateFile(accountHash);

		if (file.exists()) {
			try (FileInputStream in = new FileInputStream(file)) {
				props.load(in);
			} catch (IOException e) {
				log.error("Could not load state from {}", file.getName(), e);
			}
		}
		return props;
	}

	/**
	 * Export properties associated with accountHash to its corresponding file
	 * @param accountHash Account hash String
	 * @param props Properties that are to be saved.
	 */
	public void saveAccountState(String accountHash, Properties props) {
		File file = getStateFile(accountHash);
		try (FileOutputStream out = new FileOutputStream(file)) {
			props.store(out, "Account-specific ongoing Grand Exchange offers");
		} catch (IOException e) {
			log.error("Could not save state to {}", file.getName(), e);
		}
	}

	/**
	 * Returns the File associated with timelineId
	 */
	private File getColosseumTimeLineFile(String timelineId)
	{
		return new File(COLOSSEUM_TIMELINE_DIR, String.format("timeline_%s.json", timelineId));
	}

	public void submitColosseumTimeline(String timelineId, List<ColosseumState> timeline){
		File file = getColosseumTimeLineFile(timelineId);
		saveJson(file, timeline);
	}

	/**
	 * Serializes any object to a JSON file.
	 */
	public void saveJson(File file, Object data) {
		File directory = file.getParentFile();
		if (!directory.exists() && !directory.mkdirs()) {
			log.error("Could not create directory: {}", directory.getAbsolutePath());
			return;
		}
		String filename = file.getName();
		String cleanName = filename.endsWith(".json") ? filename : filename + ".json";
		file = new File(directory, cleanName);

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			gson.toJson(data, writer);
			log.debug("Successfully integrated data to {}", file.getName());
		} catch (IOException e) {
			log.error("Error saving integrated JSON to {}", file.getName(), e);
		}
	}

	/**
	 * Convert the given attemptId and waveId to the temporary File that is to be used.
	 */
	private File tmpWaveFile(String attemptId, int waveId)
	{
		return new File(COLOSSEUM_TEMP_DIR, String.format("%s-%02d.tmp", attemptId, waveId));
	}

	/**
	 * Convert the given attemptId to the log file that is to be used
	 */
	private File logFile(String attemptId)
	{
		return new File(COLOSSEUM_LOG_DIR, String.format("log-%s.json", attemptId));
	}

	/**
	 * Save the ColosseumStates for a single wave of an ongoing attempt in the temporary directory.
	 */
	public void saveWaveStates(String attemptId, int waveId, List<ColosseumState> liveStates) {
		List<ColosseumStateDTO> dtos = liveStates.stream()
			.map(ColosseumState::toDTO)
			.collect(Collectors.toList());

		executor.submit(() -> {
			if (!COLOSSEUM_TEMP_DIR.exists() && !COLOSSEUM_TEMP_DIR.mkdirs()) {
				log.error("Could not create directory: {}", COLOSSEUM_TEMP_DIR.getAbsolutePath());
				return;
			}

			File file = tmpWaveFile(attemptId, waveId);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				gson.toJson(dtos, writer);
				log.info("Successfully saved Colosseum run to: {}", file.getName());
			} catch (IOException e) {
				log.error("Failed to save Colosseum JSON", e);
			}
		});
	}

	private List<ColosseumStateDTO> loadWaveStates(File file) {
		try (Reader reader = new FileReader(file)) {
			ColosseumStateDTO[] dataArray = gson.fromJson(reader, ColosseumStateDTO[].class);

			if (dataArray != null) {
				return new ArrayList<>(Arrays.asList(dataArray));
			}

			return new ArrayList<>();
		} catch (IOException e) {
			log.error("Could not read temporary Colosseum file: {}", file.getName(), e);
			return new ArrayList<>();
		}
	}

	/**
	 * Parse all the components that constitute the ongoing attempt and merge them into log entries. Add these log
	 * entries to the log. Remove source files upon completion.
	 */
	public void mergeTimelineFiles(String attemptId) {
		executor.submit(() -> {
			try
			{
				List<ColosseumStateDTO> fullRunData = new ArrayList<>();
				List<File> toDelete = new ArrayList<>();

				for (int i = 1; i <= 12; i++)
				{
					File tempFile = tmpWaveFile(attemptId, i);
					if (tempFile.exists())
					{
						List<ColosseumStateDTO> waveData = loadWaveStates(tempFile);
						fullRunData.addAll(waveData);
						toDelete.add(tempFile);
					}
				}

				File finalFile = new File(COLOSSEUM_TIMELINE_DIR, String.format("timeline-%s.json", attemptId));
				saveJson(finalFile, fullRunData);

				for (File f : toDelete)
				{
					if (f.delete())
						log.debug("Deleted temp file '{}'", f.getName());
					else log.error("Failed to delete temp file '{}'", f.getName());
				}
			} catch (Exception e)
			{
				log.error("Failed to merge timeline files for attempt {}", attemptId);
			}
		});
	}

	/**
	 * Log the given attempt
	 */
	public void logColosseumAttempt(ColosseumAttempt attempt) {
		if (!COLOSSEUM_LOG_DIR.exists()) {
			COLOSSEUM_LOG_DIR.mkdirs();
		}

		File targetFile = logFile(attempt.getStartTime());

		try (FileWriter writer = new FileWriter(targetFile)) {
			gson.toJson(attempt, writer);
			log.info("Successfully saved Colosseum attempt to {}", targetFile.getAbsolutePath());
		} catch (IOException e) {
			log.error("Failed to save Colosseum attempt log!", e);
		}
	}

	/**
	 * Save the given screenshot in the appropriate folder.
	 */
	public void logColosseumScreenshot(BufferedImage screenshot, String attemptId, int waveId)
	{
		try {
			File dir = new File(COLOSSEUM_SCREENSHOT_DIR, attemptId);
			if (!dir.exists())
			{
				if (dir.mkdirs())
					log.debug("Created screenshot directory: {}", dir.getAbsolutePath());
				else
					log.error("Failed to create screenshot directory: {}", dir.getAbsolutePath());
			}

			File file = new File(dir, String.format("wave-%02d.png", waveId));
			ImageIO.write(screenshot, "png", file);

			log.info("Saved wave completion screenshot of Wave {} to {}", waveId, file.getName());
		} catch (IOException e) {
			log.error("Failed to save Colosseum screenshot", e);
		}

	}

	/**
	 * Saves a screenshot to a specified sub-directory with a specific file name.
	 * * @param screenshot   The image to save
	 * @param subDirectory The path relative to the root screenshots folder (e.g., "colosseum/231012_143000")
	 * @param fileName     The name of the file without the extension (e.g., "wave-01")
	 */
	public void saveScreenshot(BufferedImage screenshot, String subDirectory, String fileName) {
		try {
			File dir = new File(SCREENSHOT_DIR, subDirectory);
			if (!dir.exists()) {
				if (dir.mkdirs()) {
					log.debug("Created screenshot directory: {}", dir.getAbsolutePath());
				} else {
					log.error("Failed to create screenshot directory: {}", dir.getAbsolutePath());
					return;
				}
			}

			File file = new File(dir, fileName + ".png");
			ImageIO.write(screenshot, "png", file);

			log.info("Saved screenshot to {}", file.getAbsolutePath());
		} catch (IOException e) {
			log.error("Failed to save screenshot", e);
		}
	}

	public void writeColosseumCSVLog(String account, String attemptId, String rows) {
		File outFile = new File(COLOSSEUM_CSV_DIR, String.format("Colosseum-waves-%s-%s.csv", account, attemptId));
		String header = ColosseumWave.csvHeader();

		try {
			File parent = outFile.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
			String content = header + "\n" + rows.trim() + "\n";

			java.nio.file.Files.write(
				outFile.toPath(),
				content.getBytes(java.nio.charset.StandardCharsets.UTF_8),
				java.nio.file.StandardOpenOption.CREATE,
				java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
			);

			log.info("Successfully saved Colosseum CSV log to {}", outFile.getName());
		} catch (IOException e) {
			log.error("Failed to write Colosseum CSV log for attempt {}", attemptId, e);
		}
	}

	public static class WorldPointSerializer implements JsonSerializer<WorldPoint> {
		@Override
		public JsonElement serialize(WorldPoint src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			obj.addProperty("x", src.getX());
			obj.addProperty("y", src.getY());
			return obj;
		}
	}

	/**
	 * Safely loads the internal Grand Exchange ledger for a specific account.
	 * Uses String interning to prevent reading while the file is actively being saved.
	 *
	 * @param accountHash The unique hash of the logged-in account
	 * @return A list of ledger entries, or an empty list if no file exists
	 */
	public List<GeLedgerEntry> loadInternalGeLedger(String accountHash) {
		if (accountHash == null || accountHash.isEmpty()) {
			return new ArrayList<>();
		}

		File file = new File(INTERNAL_GE_DIR, accountHash + ".json");

		// Synchronize on the interned account hash so we don't read halfway through a write
		synchronized (accountHash.intern()) {
			if (!file.exists()) {
				return new ArrayList<>();
			}

			try (Reader reader = new FileReader(file)) {
				// Read directly into an array to avoid Gson TypeToken generic erasure
				GeLedgerEntry[] arr = gson.fromJson(reader, GeLedgerEntry[].class);
				return arr != null ? new ArrayList<>(Arrays.asList(arr)) : new ArrayList<>();
			} catch (Exception e) {
				log.error("Failed to load internal GE ledger for account hash: {}", accountHash, e);
				return new ArrayList<>();
			}
		}
	}

	/**
	 * Safely saves the internal Grand Exchange ledger for a specific account.
	 * Offloads to a background thread and uses an atomic file swap to prevent data corruption.
	 *
	 * @param accountHash The unique hash of the logged-in account
	 * @param ledger The full merged list of ledger entries to save
	 */
	public void saveInternalGeLedger(String accountHash, List<GeLedgerEntry> ledger) {
		if (accountHash == null || accountHash.isEmpty() || ledger == null) {
			return;
		}

		// Push the heavy disk write to the background executor to prevent freezing the game client
		executor.submit(() -> {
			if (!INTERNAL_GE_DIR.exists() && !INTERNAL_GE_DIR.mkdirs()) {
				log.error("Failed to create internal GE ledger directory: {}", INTERNAL_GE_DIR.getAbsolutePath());
				return;
			}

			File finalFile = new File(INTERNAL_GE_DIR, accountHash + ".json");
			File tempFile = new File(INTERNAL_GE_DIR, accountHash + ".tmp");

			// Synchronize on the interned hash to prevent two rapid GE updates from writing simultaneously
			synchronized (accountHash.intern()) {
				try {
					// 1. Write the full JSON to a temporary file first
					try (FileWriter writer = new FileWriter(tempFile)) {
						gson.toJson(ledger, writer);
					}

					// 2. Atomic Swap: Instantly overwrite the real file with the temp file.
					// If RuneLite crashes during step 1, the real JSON file remains perfectly safe!
					Files.move(
						tempFile.toPath(),
						finalFile.toPath(),
						StandardCopyOption.ATOMIC_MOVE,
						StandardCopyOption.REPLACE_EXISTING
					);

					log.debug("Successfully saved internal GE ledger for account hash: {}", accountHash);

				} catch (Exception e) {
					log.error("Failed to save internal GE ledger for account hash: {}", accountHash, e);
					// Clean up the temp file if something went catastrophically wrong
					if (tempFile.exists()) {
						tempFile.delete();
					}
				}
			}
		});
	}

	/**
	 * Safely loads the active Grand Exchange offers (Wealth Tracker state) for a specific account.
	 */
	public List<ActiveGeOffer> loadActiveGeOffers(String accountHash) {
		if (accountHash == null || accountHash.isEmpty()) {
			return new ArrayList<>();
		}

		File file = new File(INTERNAL_ACTIVE_OFFERS_DIR, accountHash + ".json");

		synchronized (accountHash.intern()) {
			if (!file.exists()) {
				return new ArrayList<>();
			}

			try (Reader reader = new FileReader(file)) {
				ActiveGeOffer[] arr = gson.fromJson(reader, ActiveGeOffer[].class);
				return arr != null ? new ArrayList<>(Arrays.asList(arr)) : new ArrayList<>();
			} catch (Exception e) {
				log.error("Failed to load active GE offers for account hash: {}", accountHash, e);
				return new ArrayList<>();
			}
		}
	}

	/**
	 * Safely saves the active Grand Exchange offers (Wealth Tracker state) for a specific account.
	 */
	public void saveActiveGeOffers(String accountHash, List<ActiveGeOffer> offers) {
		if (accountHash == null || accountHash.isEmpty() || offers == null) {
			log.debug("Unable to save active GE offers for account hash: {}", accountHash);
			return;
		}

		executor.submit(() -> {
			if (!INTERNAL_ACTIVE_OFFERS_DIR.exists() && !INTERNAL_ACTIVE_OFFERS_DIR.mkdirs()) {
				log.error("Failed to create internal active GE offers directory: {}", INTERNAL_ACTIVE_OFFERS_DIR.getAbsolutePath());
				return;
			}

			File finalFile = new File(INTERNAL_ACTIVE_OFFERS_DIR, accountHash + ".json");
			File tempFile = new File(INTERNAL_ACTIVE_OFFERS_DIR, accountHash + ".tmp");

			synchronized (accountHash.intern()) {
				try {
					// Write to temp file first
					try (FileWriter writer = new FileWriter(tempFile)) {
						gson.toJson(offers, writer);
					}

					// Atomic swap
					Files.move(
						tempFile.toPath(),
						finalFile.toPath(),
						StandardCopyOption.ATOMIC_MOVE,
						StandardCopyOption.REPLACE_EXISTING
					);

				} catch (Exception e) {
					log.error("Failed to save active GE offers for account hash: {}", accountHash, e);
					if (tempFile.exists()) {
						tempFile.delete();
					}
				}
			}
		});
	}

	/**
	 * Final attempt to write all rows that previously failed to write.
	 */
	public void flushAll() {
		for (Map.Entry<File, Queue<String>> entry : failedWrites.entrySet()) {
			File file = entry.getKey();
			Queue<String> backlog = entry.getValue();

			if (backlog.isEmpty()) continue;

			log.info("Attempting final flush of {} rows to {} before shutdown...", backlog.size(), file.getName());

			try (FileWriter fw = new FileWriter(file, true);
				 BufferedWriter bw = new BufferedWriter(fw);
				 PrintWriter out = new PrintWriter(bw)) {

				String queuedRow;
				while ((queuedRow = backlog.poll()) != null) {
					out.println(queuedRow);
				}
				log.info("Successfully flushed remaining backlog for {}", file.getName());

			} catch (IOException e) {
				log.error(
					"Failed to write rows to file {} due to lock during shutdown! {} rows were not written to CSV.",
					file.getName(), backlog.size());
			}
		}
	}


}