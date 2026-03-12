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
import com.datalogger.dto.ColosseumAttemptDTO;
import com.datalogger.dto.ColosseumStateDTO;
import com.datalogger.events.ColosseumAttemptStarted;
import com.datalogger.framework.LogType;
import com.datalogger.models.colosseum.ColosseumAttempt;
import com.datalogger.models.colosseum.ColosseumState;
import com.datalogger.models.colosseum.ColosseumWave;
import com.datalogger.models.grandexchange.ActiveGeOffer;
import com.datalogger.models.grandexchange.GeLedgerEntry;
import com.datalogger.models.itemvault.BankedItem;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.Subscribe;

/**
 * Service that handles writing and reading operations of local files.
 */
@Slf4j
@Singleton
public class FileIOService
{
	private final Gson gson;

	private final ScheduledExecutorService executor;

	private final Map<File, Queue<String>> failedWrites = new ConcurrentHashMap<>();

	private final Object mappingsLock = new Object();

	private final ConcurrentHashMap<String, Object> accountLocks = new ConcurrentHashMap<>();

	private final Queue<Future<?>> pendingWrites = new ConcurrentLinkedQueue<>();

	private final DataLoggerConfig config;

	private String account;
	private String attemptRoot;
	private String startTime;

	private final int MAX_BACKLOG_ROWS = 5000;

	@Inject
	private FileIOService(Gson gson, ScheduledExecutorService executor, DataLoggerConfig config) {
		this.executor = executor;
		this.gson = gson.newBuilder()
			.registerTypeAdapter(WorldPoint.class, new WorldPointSerializer())
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.create();
		this.config = config;
	}

	@Subscribe
	public void onColosseumAttemptStarted(ColosseumAttemptStarted event)
	{
		attemptRoot = event.getRoot();
		account = event.getAccountName();
		startTime = event.getStartTime();
		log.info("Initialized Colosseum FileIOService vars with root={}, account={}, startTime={}", attemptRoot, account, startTime);
	}

	public static final File PLUGIN_ROOT = new File(RuneLite.RUNELITE_DIR, "data-logger");
	public static final File INTERNAL_ROOT_DIR = new File(PLUGIN_ROOT, "internal");
	public static final File INTERNAL_GE_DIR = new File(INTERNAL_ROOT_DIR, "ge-history");
	public static final File INTERNAL_VAULT_DIR = new File(INTERNAL_ROOT_DIR, "item-vault");
	public static final File INTERNAL_TEMP_DIR = new File(INTERNAL_ROOT_DIR, "temp");
	public static final File GE_STATE_DIR = new File(INTERNAL_ROOT_DIR, "state");
	public static final File INTERNAL_ACTIVE_OFFERS_DIR = new File(INTERNAL_ROOT_DIR, "active-offers");
	public static final File GRAND_EXCHANGE_DIR = new File(PLUGIN_ROOT, "grand-exchange");
	public static final File ITEM_VAULT_DIR = new File(PLUGIN_ROOT, "item-vault");
	public static final File COLOSSEUM_ROOT_DIR = new File(PLUGIN_ROOT, "colosseum");
	public static final File COLOSSEUM_TIMELINE_DIR = new File(COLOSSEUM_ROOT_DIR, "timeline");
	public static final File COLOSSEUM_LOG_DIR = new File(COLOSSEUM_ROOT_DIR, "log");
	public static final File COLOSSEUM_CSV_DIR = new File(COLOSSEUM_ROOT_DIR, "csv");
	public static final File COLOSSEUM_SCREENSHOT_DIR = new File(COLOSSEUM_ROOT_DIR, "screenshot");

	public static final File ACCOUNT_HASH_MAPPINGS = new File(INTERNAL_ROOT_DIR, "account-hash-mappings.json");
	public static final File AGGREGATED_ITEM_VAULT_JSON = new File(ITEM_VAULT_DIR, "aggregated-wealth-summary.json");
	public static final File AGGREGATED_ITEM_VAULT_CSV = new File(ITEM_VAULT_DIR, "aggregated-wealth-summary.csv");


	/**
	 * Appends a row to a CSV file. If the file is locked, it queues the row and attempts to flush the queue on the
	 * next write attempt.
	 */
	public void atomicWrite(File file, String header, String row) {
		Future<?> future = executor.submit(() -> {
			Queue<String> backlog = failedWrites.computeIfAbsent(file, k -> new ConcurrentLinkedQueue<>());
			if (backlog.size() >= MAX_BACKLOG_ROWS) {
				log.error("Write backlog full for {}. Dropping row to prevent OOM.", file.getName());
				return;
			}
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

		pendingWrites.add(future);
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
		Future<?> future = executor.submit(() -> {
			File file = getStateFile(accountHash); // Note: getStateFile has a .mkdirs() in it, so it's good this is async now!
			try (FileOutputStream out = new FileOutputStream(file)) {
				props.store(out, "Account-specific ongoing Grand Exchange offers");
			} catch (IOException e) {
				log.error("Could not save state to {}", file.getName(), e);
			}
		});

		pendingWrites.add(future);
	}

	/**
	 * Serializes any object to a JSON file.
	 */
	public void saveJson(File file, Object data) {
		Future<?> future = executor.submit(() -> {
			File directory = file.getParentFile();
			if (!directory.exists() && !directory.mkdirs()) {
				log.error("Could not create directory: {}", directory.getAbsolutePath());
				return;
			}

			String filename = file.getName();
			String cleanName = filename.endsWith(".json") ? filename : filename + ".json";
			File finalFile = new File(directory, cleanName);

			// StandardCharsets.UTF_8 recommended for clean JSON encoding
			try (BufferedWriter writer = Files.newBufferedWriter(finalFile.toPath(), StandardCharsets.UTF_8)) {
				gson.toJson(data, writer);
				log.debug("Successfully integrated data to {}", finalFile.getName());
			} catch (IOException e) {
				log.error("Error saving integrated JSON to {}", finalFile.getName(), e);
			}
		});

		pendingWrites.add(future);
	}

	/**
	 * Convert the given attemptId and waveId to the temporary File that is to be used.
	 */
	private File tmpWaveFile(String attemptId, int waveId)
	{
		return new File(INTERNAL_TEMP_DIR, String.format("%s-%02d.tmp", attemptId, waveId));
	}

	/**
	 * Save the ColosseumStates for a single wave of an ongoing attempt in a temporary file in the temporary directory.
	 */
	public void saveWaveStates(String attemptId, int waveId, List<ColosseumState> liveStates) {
		Future<?> future = executor.submit(() -> {
			// 1. Move the DTO mapping inside the async block to save Client Thread CPU cycles
			List<ColosseumStateDTO> dtos = liveStates.stream()
				.map(ColosseumState::toDTO)
				.collect(Collectors.toList());

			if (!INTERNAL_TEMP_DIR.exists() && !INTERNAL_TEMP_DIR.mkdirs()) {
				log.error("Could not create directory: {}", INTERNAL_TEMP_DIR.getAbsolutePath());
				return;
			}

			File file = tmpWaveFile(attemptId, waveId);

			try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
				gson.toJson(dtos, writer);
				log.info("Successfully saved Colosseum run to: {}", file.getName());
			} catch (IOException e) {
				log.error("Failed to save Colosseum JSON", e);
			}
		});

		pendingWrites.add(future);
	}

	/**
	 * Attempt to load the states from a temporary file and return it. If it fails, return an empty List instead.
	 */
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
	 * Parse all the components that constitute the ongoing attempt and merge them into a single timeline. Save this
	 * timeline and subsequently delete the temporary files.
	 */
	public void mergeTimelineFiles(File outFile, String attemptId) {
		Future<?> future = executor.submit(() -> {
			List<File> toDelete = new ArrayList<>();

			try {
				File parentDir = outFile.getParentFile();
				if (parentDir != null && !parentDir.exists()) {
					parentDir.mkdirs();
				}

				try (JsonWriter writer = new JsonWriter(new FileWriter(outFile))) {
					writer.setIndent("  ");
					writer.beginArray();

					for (int i = 1; i <= 12; i++) {
						File tempFile = tmpWaveFile(attemptId, i);
						if (tempFile.exists()) {
							try (JsonReader reader = new JsonReader(new FileReader(tempFile))) {
								reader.beginArray();

								while (reader.hasNext()) {
									ColosseumStateDTO dto = gson.fromJson(reader, ColosseumStateDTO.class);
									gson.toJson(dto, ColosseumStateDTO.class, writer);
								}

								reader.endArray();
							}
							toDelete.add(tempFile);
						}
					}
					writer.endArray();
				}

				log.info("Successfully merged timeline for attempt {} at {}", attemptId, outFile.getAbsolutePath());

				// Clean up temporary files
				for (File f : toDelete) {
					if (f.delete()) {
						log.debug("Deleted temp file '{}'", f.getName());
					} else {
						log.error("Failed to delete temp file '{}'", f.getName());
					}
				}

			} catch (Exception e) {
				log.error("Failed to merge timeline files for attempt {} at file {}", attemptId, outFile.getAbsolutePath(), e);
			}
		});
		pendingWrites.add(future);
	}

	/**
	 * Logs the given attempt
	 */
	public void logColosseumAttempt(ColosseumAttempt attempt) {
		Future<?> future = executor.submit(() -> {
			// 1. Move disk I/O (directory checks) inside the async block
			if (!COLOSSEUM_LOG_DIR.exists()) {
				COLOSSEUM_LOG_DIR.mkdirs();
			}

			File targetFile = attemptWaveLogFile(attempt.getAccount(), attempt.getStartTime(), "json");

			// 2. Use the DTO to ensure your JSON output is formatted correctly
			ColosseumAttemptDTO attemptDto = attempt.toDTO();

			// Note: FileWriter is fine, but Files.newBufferedWriter is the modern standard
			// for Java 11+ as it lets you explicitly set UTF-8 encoding.
			try (FileWriter writer = new FileWriter(targetFile)) {
				gson.toJson(attemptDto, writer);
				log.info("Successfully saved Colosseum attempt to {}", targetFile.getAbsolutePath());
				log.info("Attempt waves: {} finalStatus: {}", attempt.getWaves().size(), attempt.getFinalStatus());
			} catch (IOException e) {
				log.error("Failed to save Colosseum attempt log!", e);
			}
		});

		pendingWrites.add(future);
	}

	/**
	 * Saves a screenshot to a specified sub-directory with a specific file name.
	 * * @param screenshot   The image to save
	 * @param directory The path relative to the root screenshots folder (e.g., "colosseum/231012_143000")
	 * @param fileName     The name of the file without the extension (e.g., "wave-01")
	 */
	public void saveScreenshot(BufferedImage screenshot, File directory, String fileName) {
		Future<?> future = executor.submit(() -> {
			try {
				if (!directory.exists() && !directory.mkdirs()) {
					log.error("Failed to create screenshot directory: {}", directory.getAbsolutePath());
					return;
				}

				File file = new File(directory, fileName + ".png");
				ImageIO.write(screenshot, "png", file);

				log.info("Saved screenshot to {}", file.getAbsolutePath());
			} catch (IOException e) {
				log.error("Failed to save screenshot", e);
			}
		});

		pendingWrites.add(future);
	}

	public void writeColosseumCSVLog(ColosseumAttempt attempt, String rows) {
		String header = ColosseumWave.csvHeader();
		File outFile = attemptWaveLogFile(attempt.getAccount(), attempt.getStartTime(), "csv");
		Future<?> future = executor.submit(() -> {
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

				log.info("Successfully saved Colosseum CSV log to {}", outFile.getAbsolutePath());
			} catch (IOException e) {
				log.error("Failed to write Colosseum CSV log for attempt {}", outFile.getName(), e);
			}});
		pendingWrites.add(future);
	}

	public static class WorldPointSerializer implements JsonSerializer<WorldPoint> {
		@Override
		public JsonElement serialize(WorldPoint src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			obj.addProperty("x", src.getRegionX());
			obj.addProperty("y", src.getRegionY());
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

		// Safely retrieve or create a lock specific to this account hash
		Object lock = accountLocks.computeIfAbsent(accountHash, k -> new Object());

		synchronized (lock) {
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
	public void saveInternalGeLedger(String accountName, String accountHash, List<GeLedgerEntry> ledger) {
		if (accountName == null || accountHash == null || accountHash.isEmpty() || ledger == null) {
			return;
		}

		Future<?> future = executor.submit(() -> {
			if (!INTERNAL_GE_DIR.exists() && !INTERNAL_GE_DIR.mkdirs()) {
				log.error("Failed to create internal GE ledger directory: {}", INTERNAL_GE_DIR.getAbsolutePath());
				return;
			}

			File finalFile = new File(INTERNAL_GE_DIR, accountHash + ".json");
			File tempFile = new File(INTERNAL_GE_DIR, accountHash + ".tmp");
			File copyTo = new File(GRAND_EXCHANGE_DIR, accountName + "/grand-exchange.json");

			Object lock = accountLocks.computeIfAbsent(accountHash, k -> new Object());

			synchronized (lock) {
				try {
					try (FileWriter writer = new FileWriter(tempFile)) {
						gson.toJson(ledger, writer);
					}

					Files.move(
						tempFile.toPath(),
						finalFile.toPath(),
						StandardCopyOption.ATOMIC_MOVE,
						StandardCopyOption.REPLACE_EXISTING
					);

					log.debug("Successfully saved internal GE ledger for account hash: {}", accountHash);

					if (config.logGrandExchangeJSON())
					{
						try
						{
							File copyToParent = copyTo.getParentFile();
							if (copyToParent != null && !copyToParent.exists())
							{
								copyToParent.mkdirs();
							}
							Files.copy(
								finalFile.toPath(),
								copyTo.toPath(),
								StandardCopyOption.REPLACE_EXISTING
							);
						}
						catch (Exception ignored)
						{
							log.info("Failed to copy the json to the grand-exchange directory.");
						}
					}

				} catch (Exception e) {
					log.error("Failed to save internal GE ledger for account hash: {}", accountHash, e);
					if (tempFile.exists()) {
						tempFile.delete();
					}
				}
			}
		});
		pendingWrites.add(future);
	}

	/**
	 * Safely loads the active Grand Exchange offers (Wealth Tracker state) for a specific account.
	 */
	public List<ActiveGeOffer> loadActiveGeOffers(String accountHash) {
		if (accountHash == null || accountHash.isEmpty())
			return new ArrayList<>();

		File file = new File(INTERNAL_ACTIVE_OFFERS_DIR, accountHash + ".json");

		Object lock = accountLocks.computeIfAbsent(accountHash, k -> new Object());

		synchronized (lock) {
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

		Future<?> future = executor.submit(() -> {
			if (!INTERNAL_ACTIVE_OFFERS_DIR.exists() && !INTERNAL_ACTIVE_OFFERS_DIR.mkdirs()) {
				log.error("Failed to create internal active GE offers directory: {}", INTERNAL_ACTIVE_OFFERS_DIR.getAbsolutePath());
				return;
			}

			File finalFile = new File(INTERNAL_ACTIVE_OFFERS_DIR, accountHash + ".json");
			File tempFile = new File(INTERNAL_ACTIVE_OFFERS_DIR, accountHash + ".tmp");

			Object lock = accountLocks.computeIfAbsent(accountHash, k -> new Object());

			synchronized (lock) {
				try {
					try (FileWriter writer = new FileWriter(tempFile)) {
						gson.toJson(offers, writer);
					}

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
		pendingWrites.add(future);
	}

	/**
	 * Final attempt to write all rows that previously failed to write.
	 */
	public void flushAll() {
		log.info("Waiting for pending background file writes to complete...");

		Future<?> future;
		while ((future = pendingWrites.poll()) != null) {
			try {
				if (!future.isDone() && !future.isCancelled()) {
					future.get(2, TimeUnit.SECONDS);
				}
			} catch (Exception e) {
				log.warn("A pending file write timed out or was interrupted during shutdown.", e);
			}
		}

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

	/**
	 * Update a cached accountHash-accountName mapping JSON file with the given accountHash and accountName
	 */
	public void updateAccountHashMapping(String accountHash, String accountName) {
		if (accountHash == null || accountHash.isEmpty() || accountName == null || accountName.isEmpty()) {
			return;
		}

		Future<?> future = executor.submit(() -> {
			File parentDir = ACCOUNT_HASH_MAPPINGS.getParentFile();
			if (parentDir != null && !parentDir.exists()) {
				parentDir.mkdirs();
			}

			synchronized (mappingsLock) {
				Properties properties = new Properties();

				if (ACCOUNT_HASH_MAPPINGS.exists()) {
					try (FileInputStream in = new FileInputStream(ACCOUNT_HASH_MAPPINGS)) {
						properties.load(in);
					} catch (Exception e) {
						log.error("Failed to read existing account hash mappings", e);
					}
				}

				properties.setProperty(accountHash, accountName);

				try (FileOutputStream out = new FileOutputStream(ACCOUNT_HASH_MAPPINGS)) {
					properties.store(out, "Account Hash to Account Name Mappings");
					log.debug("Successfully updated account hash mapping for: {}", accountName);
				} catch (Exception e) {
					log.error("Failed to write updated account hash mappings", e);
				}
			}
		});

		pendingWrites.add(future);
	}

	/**
	 * Safely exports Item Vault contents to a CSV file on a background thread.
	 */
	public void writeVaultCsv(File csvFile, List<BankedItem> items, boolean includeMetadata) {
		Future<?> future = executor.submit(() -> {
			File parentDir = csvFile.getParentFile();

			// Fix: Actually create the parent directories if they don't exist!
			if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
				log.error("Failed to create directory for vault CSV: {}", parentDir.getAbsolutePath());
				return;
			}

			try (BufferedWriter bw = Files.newBufferedWriter(csvFile.toPath(), StandardCharsets.UTF_8);
				 PrintWriter out = new PrintWriter(bw)) {

				if (includeMetadata) {
					out.println("AccountName,Vault,ItemID,ItemName,Quantity");
					for (BankedItem item : items) {
						out.printf("%s,%s,%d,%s,%d%n",
							item.getAccountName(),
							item.getVaultType(),
							item.getItemId(),
							item.getItemName(),
							item.getQuantity());
					}
				} else {
					out.println("ItemID,ItemName,Quantity");
					for (BankedItem item : items) {
						out.printf("%d,%s,%d%n",
							item.getItemId(),
							item.getItemName(),
							item.getQuantity());
					}
				}
				log.info("Successfully exported vault to CSV: {}", csvFile.getName());
			} catch (Exception e) {
				log.error("Failed to write vault contents to CSV: {}", csvFile.getName(), e);
			}
		});

		pendingWrites.add(future);
	}

	/**
	 * Reads all vault JSON files from the disk.
	 * Returns a Map where the key is the filename and the value is the array of raw items.
	 */
	public Map<String, BankedItem[]> readAllVaultFilesRaw() {
		File directory = INTERNAL_VAULT_DIR;
		if (!directory.exists() || !directory.isDirectory()) {
			return new HashMap<>(); // Return empty map if directory doesn't exist
		}

		File[] vaultFiles = directory.listFiles((dir, name) -> name.endsWith(".json"));
		if (vaultFiles == null || vaultFiles.length == 0) {
			return new HashMap<>();
		}

		Map<String, BankedItem[]> fileContents = new HashMap<>();
		for (File file : vaultFiles) {
			try (Reader reader = new FileReader(file)) {
				BankedItem[] parsedItems = gson.fromJson(reader, BankedItem[].class);
				if (parsedItems != null) {
					fileContents.put(file.getName(), parsedItems);
				}
			} catch (Exception e) {
				log.error("Failed to read items from vault file: {}", file.getName(), e);
			}
		}
		return fileContents;
	}

	/**
	 * Safely saves an account hash mapping to disk on a background thread.
	 */
	public void saveAccountHashMappingAsync(long accountHash, String accountName)
	{
		Future<?> future = executor.submit(() -> {
			File mappingFile = ACCOUNT_HASH_MAPPINGS;
			File parentDir = mappingFile.getParentFile();

			if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
				log.error("Failed to create directory for account hash mappings.");
				return;
			}

			synchronized (mappingFile.getAbsolutePath().intern()) {
				Properties properties = new Properties();

				if (mappingFile.exists()) {
					try (FileInputStream in = new FileInputStream(mappingFile)) {
						properties.load(in);
					} catch (Exception e) {
						log.error("Failed to read existing account hash mappings during update", e);
					}
				}

				properties.setProperty(String.valueOf(accountHash), accountName);
				try (FileOutputStream out = new FileOutputStream(mappingFile)) {
					properties.store(out, "Account Hash to Account Name Mappings");
					log.debug("Successfully updated account hash mapping for: {}", accountName);
				} catch (Exception e) {
					log.error("Failed to write updated account hash mappings", e);
				}
			}
		});

		pendingWrites.add(future);
	}

	/**
	 * Reads the account hash mappings from the disk.
	 * @return A map of account hashes to account names.
	 */
	public Map<Long, String> loadAccountHashMappingsRaw()
	{
		Map<Long, String> mappings = new HashMap<>();

		if (ACCOUNT_HASH_MAPPINGS.exists())
		{
			try (FileInputStream in = new FileInputStream(ACCOUNT_HASH_MAPPINGS))
			{
				Properties properties = new Properties();
				properties.load(in);

				for (String key : properties.stringPropertyNames())
				{
					try {
						long hash = Long.parseLong(key);
						mappings.put(hash, properties.getProperty(key));
					} catch (NumberFormatException e) {
						log.warn("Found invalid account hash in mappings file: {}", key);
					}
				}
			}
			catch (Exception e)
			{
				log.error("Failed to read existing account hash mappings", e);
			}
		}
		return mappings;
	}

	public File attemptTimelineFile()
	{
		return new File(attemptRoot, String.format("%s_%s_timeline.json", account, startTime));
	}

	public File attemptWaveLogFile(String account, String startTime, String extension)
	{
		String attemptId = String.format("%s_%s", account, startTime);
		return new File(new File(FileIOService.COLOSSEUM_ROOT_DIR, attemptId), String.format("%s_wave-log.%s", attemptId, extension.replace(".","")));	}

	public File attemptWaveLogCsvFile()
	{
		return new File(attemptRoot, String.format("%s_%s_wave-log.csv", account, startTime));
	}
}