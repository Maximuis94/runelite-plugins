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
import com.datalogger.constants.PluginConstants;
import static com.datalogger.constants.PluginConstants.INTERNAL_COLOSSEUM_TRIAL_HISTORY;
import static com.datalogger.constants.PluginConstants.INTERNAL_VAULT_DIR;
import com.datalogger.dto.ColosseumAttemptDTO;
import com.datalogger.dto.ColosseumStateDTO;
import com.datalogger.dto.TrackedSuppliesDTO;
import com.datalogger.events.AccountSessionStarted;
import com.datalogger.events.ColosseumAttemptStarted;
import com.datalogger.events.DataLoggerConfigChanged;
import com.datalogger.events.ScreenshotFileCreated;
import com.datalogger.framework.LogType;
import com.datalogger.models.colosseum.ColosseumAttempt;
import com.datalogger.models.colosseum.ColosseumState;
import com.datalogger.models.colosseum.ColosseumWave;
import com.datalogger.models.enums.ItemCharge;
import com.datalogger.models.enums.ScreenshotFormat;
import com.datalogger.models.enums.VaultType;
import static com.datalogger.models.enums.VaultType.getInternalRoot;
import com.datalogger.models.grandexchange.ActiveGeOffer;
import com.datalogger.models.grandexchange.GeLedgerEntry;
import com.datalogger.models.itemvault.BankedItem;
import com.datalogger.models.itemvault.ValuedItemBundle;
import com.datalogger.models.supplytracker.ValuedItemStack;
import com.datalogger.services.itemvault.VaultParser;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
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
import java.time.Instant;
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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

/**
 * Service that handles writing and reading operations of local files.
 */
@Slf4j
@Singleton
public class FileIOService
{
	public static final File ACCOUNT_HASH_MAPPINGS = new File(PluginConstants.INTERNAL_ROOT_DIR, "account-hash-mappings.properties");
	public static final File INTERNAL_ACTIVE_OFFERS_DIR = new File(PluginConstants.INTERNAL_ROOT_DIR, "active-offers");
	public static final File GE_STATE_DIR = new File(PluginConstants.INTERNAL_ROOT_DIR, "state");
	public static final File INTERNAL_TEMP_DIR = new File(PluginConstants.INTERNAL_ROOT_DIR, "temp");
	public static final File INTERNAL_GE_DIR = new File(PluginConstants.INTERNAL_ROOT_DIR, "grand-exchange");
	public static final File INTERNAL_GE_OFFERS_DIR = new File(INTERNAL_GE_DIR, "completed");
	public static final File INTERNAL_GE_HISTORY_DIR = new File(INTERNAL_GE_DIR, "history");
	public static final File DEBUG_DIR = new File(PluginConstants.PLUGIN_ROOT, "debug");
	public static final File COLOSSEUM_WAVE_LOG_MERGED_CSV = new File(PluginConstants.COLOSSEUM_ROOT_DIR, "colosseum-waves-merged.csv");
	public static final File COLOSSEUM_WAVE_LOG_MERGED_JSON = new File(PluginConstants.COLOSSEUM_ROOT_DIR, "colosseum-waves-merged.json");
	private final Gson gson;

	private final ScheduledExecutorService executor;

	private final Map<File, Queue<String>> failedWrites = new ConcurrentHashMap<>();

	private final Object mappingsLock = new Object();

	private final ConcurrentHashMap<String, Object> accountLocks = new ConcurrentHashMap<>();

	private final Queue<Future<?>> pendingWrites = new ConcurrentLinkedQueue<>();

	private final DataLoggerConfig config;
	private final EventBus eventBus;

	private boolean isTemporaryGameMode = false;
	private String accountName = "";
	private String accountHashString = "";
	@Getter
	private File internalVaultRoot = null;

	@Getter
	private boolean hasValidAccountInfo = false;
	@Getter
	private boolean hasExpiredAccountInfo = false;

	private String attemptRoot;
	private File attemptRootFile;
	private String startTime;

	private static final Map<String, File> INTERNAL_LEDGER_CACHE = new ConcurrentHashMap<>();

	private final int MAX_BACKLOG_ROWS = 5000;
	private final int MAX_FLUSH_TIME_MS = 3000;

	@Inject
	private FileIOService(Gson gson, ScheduledExecutorService executor, DataLoggerConfig config, EventBus eventBus) {
		this.executor = executor;
		this.gson = gson.newBuilder()
			.registerTypeAdapter(WorldPoint.class, new WorldPointSerializer())
			.disableHtmlEscaping()
			.create();
		this.config = config;
		this.eventBus = eventBus;
	}

	/**
	 * Helper method to safely create directories and report exactly why they failed.
	 */
	private boolean ensureDirectoryExists(File dir) {
		if (dir != null && !dir.exists()) {
			try {
				java.nio.file.Files.createDirectories(dir.toPath());
			} catch (IOException e) {
				log.error("Failed to create directory: {}", dir.getAbsolutePath(), e);
				return false;
			}
		}
		return true;
	}

	@Subscribe
	public void onColosseumAttemptStarted(ColosseumAttemptStarted event)
	{
		attemptRoot = event.getRoot();
		startTime = event.getStartTime();

		attemptRootFile = new File(attemptRoot);

		if (ensureDirectoryExists(attemptRootFile))
		{
			log.debug("Successfully verified Colosseum attempt directory at: {}", attemptRoot);
		}
		else
		{
			log.warn("Failed to create Colosseum attempt directory at: {}. Logs/screenshots may fail to save.", attemptRoot);
		}

		log.debug("Initialized Colosseum FileIOService vars with root={}, account={}, startTime={}", attemptRoot, accountName, startTime);
	}

	@Subscribe
	public void onDataLoggerConfigChanged(DataLoggerConfigChanged event)
	{
		updateConfigFlags();
	}

	@Subscribe
	public void onAccountSessionStarted(AccountSessionStarted event)
	{
		updateAccountInfo(event.getAccountName(), event.getAccountHashString(), event.isOnPermanentWorld());
	}

	private boolean isValidAccountString(String accountString)
	{
		return accountString != null && !accountString.isEmpty();
	}

	/**
	 * Update account info with the given accountName and accountHash Strings. If either one is not valid, indicate this
	 * via the hasValidAccountInfo flag, but keep the previous name and/or hash cached.
	 */
	private void updateAccountInfo(String accountName, String accountHashString, boolean isTemporaryGameMode)
	{
		boolean isValidName = isValidAccountString(accountName);
		boolean isValidHash = isValidAccountString(accountHashString);
		if (isValidName && isValidHash)
		{
			this.accountName = accountName;
			this.accountHashString = accountHashString;
			internalVaultRoot = getInternalRoot(accountHashString);
			hasValidAccountInfo = true;
			this.isTemporaryGameMode = isTemporaryGameMode;
		}
		else if (hasValidAccountInfo)
		{
			log.debug("AccountHash info has expired and does not match the currently active session.");
			hasExpiredAccountInfo = true;
		}
	}

	/**
	 * Parse relevant configurations and load the values into associated variables
	 */
	private void updateConfigFlags()
	{
	}


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
				if (!ensureDirectoryExists(parentDir)) {
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

					if (backlog.isEmpty()) {
						failedWrites.remove(file);
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
	 * @return File reference based on inputs provided and strategy selected
	 */
	public File getTargetFile(LogType type) {
		String date = java.time.LocalDate.now().toString();
		File accountDir = new File(type.getLogDirectory(), accountName.toLowerCase());

		return new File(accountDir, type.getDirectoryName() + "_" + date + ".csv");
	}

	/**
	 * Return the internal vault json file relevant for the account that is logged in associated with the given VaultType
	 */
	public File getInternalVaultFile(VaultType vaultType)
	{
		return new File(internalVaultRoot, vaultType.fileNameString() + "_" + accountHashString + ".json");
	}

	/**
	 * Return the internal vault json file relevant for the account that is logged in associated with the given VaultType
	 */
	public File getInternalVaultFile(VaultType vaultType, String accountHashString)
	{
		File internalVaultRoot = getInternalRoot(accountHashString);
		return new File(internalVaultRoot, vaultType.fileNameString() + "_" + accountHashString + ".json");
	}

	/**
	 * Return the internal vault json file relevant for the account that is logged in associated with the given VaultType
	 */
	public File getInternalVaultFile(ItemCharge itemCharge)
	{
		return new File(internalVaultRoot, itemCharge.fileNameString() + "_" + accountHashString + ".json");
	}

	/**
	 * Return the internal vault json file relevant for the account that is logged in associated with the given VaultType
	 */
	public File getInternalVaultFile(ItemCharge itemCharge, String accountHashString)
	{
		return new File(new File(INTERNAL_VAULT_DIR, accountHashString), itemCharge.fileNameString() + "_" + accountHashString + ".json");
	}

	/**
	 * Ensures the state directory exists and returns the file path for an account's state
	 */
	private File getStateFile() {
		ensureDirectoryExists(GE_STATE_DIR);
		return new File(GE_STATE_DIR, "state_" + accountHashString + ".properties");
	}

	/**
	 * Parse the properties file associated with the given account hash
	 * @return The contents of the parsed properties file
	 */
	public Properties getAccountState() {
		Properties props = new Properties();
		File file = getStateFile();

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
	 * @param props Properties that are to be saved.
	 */
	public void saveAccountState(Properties props) {
		Future<?> future = executor.submit(() -> {
			File file = getStateFile();
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
			if (!ensureDirectoryExists(directory)) {
				return;
			}

			String filename = file.getName();
			String cleanName = filename.endsWith(".json") ? filename : filename + ".json";
			File finalFile = new File(directory, cleanName);

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
			List<ColosseumStateDTO> dtos = liveStates.stream()
				.map(ColosseumState::toDTO)
				.collect(Collectors.toList());

			if (!ensureDirectoryExists(INTERNAL_TEMP_DIR)) {
				return;
			}

			File file = tmpWaveFile(attemptId, waveId);

			try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
				gson.toJson(dtos, writer);
				log.debug("Successfully saved Colosseum run to: {}", file.getName());
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
				ensureDirectoryExists(outFile.getParentFile());

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

				log.debug("Successfully merged timeline for attempt {} at {}", attemptId, outFile.getAbsolutePath());

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

	public void logColosseumAttempt(ColosseumAttemptDTO attemptDto, File attemptLogJsonFile) {
		Future<?> futureInternal = executor.submit(() -> {
			ensureDirectoryExists(INTERNAL_COLOSSEUM_TRIAL_HISTORY.getParentFile());

			Gson flatGson = new Gson();
			String jsonLine = flatGson.toJson(attemptDto);

			try (FileWriter fw = new FileWriter(INTERNAL_COLOSSEUM_TRIAL_HISTORY, true);
				 BufferedWriter bw = new BufferedWriter(fw);
				 PrintWriter out = new PrintWriter(bw)) {

				out.println(jsonLine);
				log.debug("Successfully appended attempt to internal history.");

			} catch (IOException e) {
				log.error("Failed to append to internal Colosseum history!", e);
			}
		});
		pendingWrites.add(futureInternal);

		Future<?> futureExternal = executor.submit(() -> {
			ensureDirectoryExists(attemptLogJsonFile.getParentFile());

			try (FileWriter writer = new FileWriter(attemptLogJsonFile)) {
				gson.toJson(attemptDto, writer);
				log.debug("Successfully saved Colosseum attempt to {}", attemptLogJsonFile.getAbsolutePath());
			} catch (IOException e) {
				log.error("Failed to save Colosseum attempt log!", e);
			}
		});
		pendingWrites.add(futureExternal);
	}

	/**
	 * Saves a screenshot to a specified sub-directory with a specific file name.
	 * * @param screenshot   The image to save
	 * @param rootDir The path relative to the root screenshots folder (e.g., "colosseum/231012_143000")
	 * @param fileName     The name of the file without the extension (e.g., "wave-01")
	 * @param format The format of the image file
	 */
	public void saveScreenshot(BufferedImage image, File rootDir, String fileName, ScreenshotFormat format, boolean shouldBroadcast) throws IOException {
		log.debug("Attempting to save screenshot at directory {} with fileName {}", rootDir, fileName);

		if (!ensureDirectoryExists(rootDir)) {
			return;
		}

		File outputFile = new File(rootDir, fileName + "." + format.getExtension());

		if (format == ScreenshotFormat.JPEG) {
			BufferedImage rgbImage = new BufferedImage(
				image.getWidth(),
				image.getHeight(),
				BufferedImage.TYPE_INT_RGB
			);
			rgbImage.createGraphics().drawImage(image, 0, 0, null);
			ImageIO.write(rgbImage, "jpg", outputFile);
		} else {
			ImageIO.write(image, "png", outputFile);
		}
		String absolutePath = outputFile.getAbsolutePath();
		log.debug("Posting ScreenshotFileCreated event for {} shouldBroadcast={}", absolutePath, shouldBroadcast);
		eventBus.post(new ScreenshotFileCreated(absolutePath));

	}

	public void writeColosseumCSVLog(ColosseumAttempt attempt, String rows) {
		String header = ColosseumWave.csvHeader();
		File outFile = attempt.getWaveLogCsvFile();
		Future<?> future = executor.submit(() -> {
			try {
				ensureDirectoryExists(outFile.getParentFile());

				String content = header + "\n" + rows.trim() + "\n";

				java.nio.file.Files.write(
					outFile.toPath(),
					content.getBytes(java.nio.charset.StandardCharsets.UTF_8),
					java.nio.file.StandardOpenOption.CREATE,
					java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
				);

				log.debug("Successfully saved Colosseum CSV log to {}", outFile.getAbsolutePath());
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
	 * Helper method to seamlessly convert an existing JSON array file to a JSON Lines file.
	 */
	private void migrateLedgerToJsonl(File oldFile, File newFile)
	{
		log.info("Migrating internal GE ledger from JSON array to JSON Lines for: {}", oldFile.getName());
		try
		{
			if (oldFile.length() > 0)
			{
				try (FileReader reader = new FileReader(oldFile);
					 FileWriter fw = new FileWriter(newFile, true);
					 BufferedWriter bw = new BufferedWriter(fw))
				{
					// Parse the old array
					JsonArray array = gson.fromJson(reader, JsonArray.class);
					if (array != null)
					{
						// Write each element as a new line
						for (JsonElement element : array)
						{
							bw.write(gson.toJson(element));
							bw.newLine();
						}
					}
				}
			}

			if (oldFile.delete())
			{
				log.info("Successfully migrated and deleted old JSON ledger.");
			}
			else
			{
				log.warn("Migration succeeded, but failed to delete old file: {}", oldFile.getAbsolutePath());
			}
		}
		catch (Exception e)
		{
			log.error("Failed to migrate internal GE ledger. New entries will still be appended to .jsonl.", e);
		}
	}

	/**
	 * Adds entry to the internal GE ledger
	 */
	public void extendInternalGeLedger(GeLedgerEntry entry)
	{
		if (!isTemporaryGameMode) return;
		String accountHash = String.valueOf(entry.getAccountHash());

		File jsonlFile = INTERNAL_LEDGER_CACHE.computeIfAbsent(accountHash, hash -> {
			File dir = INTERNAL_GE_OFFERS_DIR;
			if (!dir.exists())
			{
				dir.mkdirs();
			}
			return new File(dir, hash + ".jsonl");
		});

		File oldJsonFile = new File(INTERNAL_GE_DIR, accountHash + ".json");
		if (oldJsonFile.exists())
		{
			migrateLedgerToJsonl(oldJsonFile, jsonlFile);
		}

		try (FileWriter fw = new FileWriter(jsonlFile, true);
			 BufferedWriter bw = new BufferedWriter(fw))
		{
			bw.write(gson.toJson(entry));
			bw.newLine();
		}
		catch (IOException e)
		{
			log.error("Failed to append to internal GE ledger (.jsonl): {}", jsonlFile.getAbsolutePath(), e);
		}
	}

	public List<GeLedgerEntry> loadInternalGeLedger(String accountHash) {
		if (accountHash == null || accountHash.isEmpty()) {
			return new ArrayList<>();
		}

		File file = new File(INTERNAL_GE_DIR, accountHash + ".json");
		Object lock = accountLocks.computeIfAbsent(accountHash, k -> new Object());

		synchronized (lock) {
			if (!file.exists()) {
				return new ArrayList<>();
			}

			try (Reader reader = new FileReader(file)) {
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
	 * @param ledger The full merged list of ledger entries to save
	 */
	public void saveInternalGeLedger(List<GeLedgerEntry> ledger) {
		if (!hasValidAccountInfo || ledger == null) {
			return;
		}

		Future<?> future = executor.submit(() -> {
			if (!ensureDirectoryExists(INTERNAL_GE_DIR)) {
				return;
			}

			File finalFile = new File(INTERNAL_GE_DIR, accountHashString + ".json");
			File tempFile = new File(INTERNAL_GE_DIR, accountHashString + ".tmp");
			File copyTo = new File(PluginConstants.GRAND_EXCHANGE_DIR, accountName + "/grand-exchange.json");

			Object lock = accountLocks.computeIfAbsent(accountHashString, k -> new Object());

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

					log.debug("Successfully saved internal GE ledger for account hash: {}", accountHashString);

				} catch (Exception e) {
					log.error("Failed to save internal GE ledger for account hash: {}", accountHashString, e);
					if (tempFile.exists()) {
						tempFile.delete();
					}
				}
			}
		});
		pendingWrites.add(future);
	}

	/**
	 * Appends a Grand Exchange JSON object to the specified file based on the logging strategy.
	 */
	public synchronized void appendGeJsonLog(File file, JsonObject newEntry)
	{
		File parent = file.getParentFile();
		if (parent != null && !parent.exists())
		{
			parent.mkdirs();
		}

		JsonArray array = new JsonArray();

		if (file.exists() && file.length() > 0)
		{
			try (FileReader reader = new FileReader(file))
			{
				JsonArray parsedArray = gson.fromJson(reader, JsonArray.class);

				if (parsedArray != null)
				{
					array = parsedArray;
				}
			}
			catch (Exception e)
			{
				log.warn("Failed to read existing GE JSON Array at {}. Overwriting with a fresh array.", file.getAbsolutePath(), e);
			}
		}

		array.add(newEntry);

		try (FileWriter fw = new FileWriter(file))
		{
			gson.toJson(array, fw);
		}
		catch (IOException e)
		{
			log.error("Failed to write GE JSON Array to {}", file.getAbsolutePath(), e);
		}
	}

	/**
	 * Appends a single JsonObject as a new line to a .jsonl file.
	 * Highly efficient as it does not require reading the existing file into memory.
	 */
	public synchronized void appendJsonlLog(File file, JsonObject newEntry)
	{
		if (file == null || newEntry == null) return;

		File parent = file.getParentFile();
		if (parent != null && !parent.exists())
		{
			parent.mkdirs();
		}

		// The 'true' flag in FileWriter enables append mode
		try (FileWriter fw = new FileWriter(file, true);
			 BufferedWriter bw = new BufferedWriter(fw))
		{
			bw.write(newEntry.toString());
			bw.newLine();
		}
		catch (IOException e)
		{
			log.error("Failed to append to JSONL file at {}", file.getAbsolutePath(), e);
		}
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

	public List<ActiveGeOffer> loadActiveGeOffers()
	{
		if(!hasValidAccountInfo)
		{
			log.error("Unable to load active GE offers for current accountHash as it is null or empty...");
			return new ArrayList<>();
		}

		log.debug("Loading active GE offers for account {} with hash {}...", accountName, accountHashString);
		return loadActiveGeOffers(accountHashString);
	}

	/**
	 * Safely saves the active Grand Exchange offers (Wealth Tracker state) for a specific account,
	 * and exports a JSON copy and a CSV file to the public grand exchange directory.
	 */
	public void saveActiveGeOffers(String accountHash, List<ActiveGeOffer> offers) {
		if (!hasValidAccountInfo || offers == null) {
			log.debug("Unable to save active GE offers for account hash: {}", accountHash);
			return;
		}

		Future<?> future = executor.submit(() -> {
			if (!ensureDirectoryExists(INTERNAL_ACTIVE_OFFERS_DIR)) {
				return;
			}
			File finalFile = new File(INTERNAL_ACTIVE_OFFERS_DIR, accountHash + ".json");
			File tempFile = new File(INTERNAL_ACTIVE_OFFERS_DIR, accountHash + ".tmp");

			File root = new File(PluginConstants.ITEM_VAULT_DIR, accountName);
			File publicJsonFile = new File(root, "active-offers_" + accountName + ".json");
			File publicCsvFile = new File(root, "active-offers_" + accountName + ".csv");

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

					if (!ensureDirectoryExists(PluginConstants.ITEM_VAULT_DIR)) {
						log.error("Failed to create public active GE offers directory: {}", PluginConstants.ITEM_VAULT_DIR.getAbsolutePath());
					} else {
						Files.copy(
							finalFile.toPath(),
							publicJsonFile.toPath(),
							StandardCopyOption.REPLACE_EXISTING
						);

						try (BufferedWriter bw = Files.newBufferedWriter(publicCsvFile.toPath(), StandardCharsets.UTF_8);
							 PrintWriter out = new PrintWriter(bw)) {

							out.println("slot,itemId,itemName,isBuy,state,totalQuantity,quantitySold,offerPrice,spent");

							for (ActiveGeOffer offer : offers) {
								String itemName = offer.getItemName() != null ? offer.getItemName() : "";
								if (itemName.contains(",")) {
									itemName = "\"" + itemName + "\"";
								}

								out.printf("%d,%d,%s,%b,%s,%d,%d,%d,%d%n",
									offer.getSlot(),
									offer.getItemId(),
									itemName,
									offer.isBuy(),
									offer.getState(),
									offer.getTotalQuantity(),
									offer.getQuantitySold(),
									offer.getOfferPrice(),
									offer.getSpent()
								);
							}
						}
						log.debug("Successfully exported public active GE offers (JSON/CSV) for account hash: {}", accountHash);
					}

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

	public void saveActiveGeOffers(List<ActiveGeOffer> offers)
	{
		log.debug("Saving active GE offers for account {}", accountName);
		saveActiveGeOffers(accountHashString, offers);
	}

	/**
	 * Final attempt to write all rows that previously failed to write.
	 */
	public void flushAll() {
		log.debug("Waiting for pending background file writes to complete...");

		Instant timeLimit = Instant.now().plusMillis(MAX_FLUSH_TIME_MS);

		Future<?> future;
		while ((future = pendingWrites.poll()) != null) {
			try {
				if (!future.isDone() && !future.isCancelled()) {
					long millisLeft = java.time.Duration.between(Instant.now(), timeLimit).toMillis();

					if (millisLeft <= 0) {
						log.warn("Global flush timeout reached. Cancelling remaining pending writes.");
						future.cancel(true);
						break;
					}
					future.get(millisLeft, TimeUnit.MILLISECONDS);
				}
			} catch (Exception e) {
				log.warn("A pending file write timed out or was interrupted during shutdown.", e);
			}
		}

		for (Map.Entry<File, Queue<String>> entry : failedWrites.entrySet()) {
			if (Instant.now().isAfter(timeLimit)) {
				log.warn("Global flush timeout reached before backlog could be fully flushed.");
				break;
			}

			File file = entry.getKey();
			Queue<String> backlog = entry.getValue();

			if (backlog.isEmpty()) continue;

			log.debug("Attempting final flush of {} rows to {} before shutdown...", backlog.size(), file.getName());

			try (FileWriter fw = new FileWriter(file, true);
				 BufferedWriter bw = new BufferedWriter(fw);
				 PrintWriter out = new PrintWriter(bw)) {

				String queuedRow;
				while ((queuedRow = backlog.poll()) != null) {
					out.println(queuedRow);
				}

				if (backlog.isEmpty()) {
					failedWrites.remove(file);
				}

				log.debug("Successfully flushed remaining backlog for {}", file.getName());

			} catch (IOException e) {
				log.error(
					"Failed to write rows to file {} due to lock during shutdown! {} rows were not written to CSV.",
					file.getName(), backlog.size());
			}
		}
	}

	/**
	 * Safely exports Item Vault contents to a CSV file on a background thread.
	 */
	public void writeVaultCsv(File csvFile, List<BankedItem> items, boolean includeMetadata) {
		Future<?> future = executor.submit(() -> {
			File parentDir = csvFile.getParentFile();

			if (!ensureDirectoryExists(parentDir)) {
				return;
			}

			try (BufferedWriter bw = Files.newBufferedWriter(csvFile.toPath(), StandardCharsets.UTF_8);
				 PrintWriter out = new PrintWriter(bw)) {

				if (includeMetadata) {
					out.println("AccountName,AccountHash,Source,ItemID,ItemName,Quantity");
					for (BankedItem item : items) {
						out.printf("%s,%d,%s,%d,%s,%d%n",
							item.getAccountName(),
							item.getAccountHash(),
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
				log.debug("Successfully exported vault to CSV: {}", csvFile.getName());
			} catch (Exception e) {
				log.error("Failed to write vault contents to CSV: {}", csvFile.getName(), e);
			}
		});

		pendingWrites.add(future);
	}

	/**
	 * Writes merged aggregated vault data to csvFile (i.e., each item, if present, has exactly one row)
	 * @param csvFile Output File
	 * @param items Row data
	 */
	public void writeVaultCsv(File csvFile, List<ValuedItemBundle> items) {
		Future<?> future = executor.submit(() -> {
			File parentDir = csvFile.getParentFile();

			if (!ensureDirectoryExists(parentDir)) {
				return;
			}

			try (BufferedWriter bw = Files.newBufferedWriter(csvFile.toPath(), StandardCharsets.UTF_8);
				 PrintWriter out = new PrintWriter(bw)) {

				out.println("ItemID,ItemName,Quantity,Value");
				for (ValuedItemBundle item : items) {
					out.printf("%d,%s,%d,%d%n",
						item.getItemId(),
						item.getItemName(),
						item.getQuantity(),
						item.getValue());
				}
				log.debug("Successfully exported vault to CSV: {}", csvFile.getName());
			} catch (Exception e) {
				log.error("Failed to write vault contents to CSV: {}", csvFile.getName(), e);
			}
		});

		pendingWrites.add(future);
	}

	public void exportAggregatedItemCharges(long accountHash, String accountName, List<VaultParser> vaultParsers) {
		executor.submit(() -> {
			List<BankedItem> aggregatedCharges = new ArrayList<>();
			for (VaultParser parser : vaultParsers) {
				File vaultFile = parser.getInternalVaultFile(accountHash);

				if (!vaultFile.exists())
				{
					continue;
				}
				log.debug("Exporting vault file {}", vaultFile);
				List<BankedItem> parsedCharges = parser.parseOfflineFile(accountHash, vaultFile);
				if (parsedCharges != null && !parsedCharges.isEmpty()) {
					aggregatedCharges.addAll(parsedCharges);
				}
			}

			if (aggregatedCharges.isEmpty()) {
				log.debug("No item charges found to export for account {}", accountName);
				return;
			}
			else
			{
				log.debug("A total of {} items found to export for account {}", aggregatedCharges.size(), accountName);
			}

			VaultType vaultType = VaultType.ITEM_CHARGES;

			if (config.logItemVaultJSON()) {
				File externalJson = vaultType.getExternalJSONFile(accountName);
				saveJson(externalJson, aggregatedCharges);
			}

			if (config.logItemVaultCSV()) {
				File csvFile = vaultType.getExternalCSVFile(accountName);
				writeVaultCsv(csvFile, aggregatedCharges, true);
			}
		});
	}

	/**
	 * Reads all vault JSON files from the disk.
	 * Returns a Map where the key is the filename and the value is the array of raw items.
	 */
	public Map<Long, Map<VaultType, List<BankedItem>>> readAllVaultFilesRaw()
	{
		Map<Long, Map<VaultType, List<BankedItem>>> allData = new HashMap<>();
		File directory = PluginConstants.INTERNAL_VAULT_DIR;

		if (!directory.exists() || !directory.isDirectory())
		{
			return allData;
		}

		File[] accountDirs = directory.listFiles(File::isDirectory);
		if (accountDirs == null) return allData;

		for (File accountDir : accountDirs)
		{
			long accountHash;
			try
			{
				accountHash = Long.parseLong(accountDir.getName());
			}
			catch (NumberFormatException e)
			{
				continue;
			}

			File[] vaultFiles = accountDir.listFiles((dir, name) -> name.endsWith(".json"));
			if (vaultFiles == null) continue;

			Map<VaultType, List<BankedItem>> accountVaults = new HashMap<>();
			for (File file : vaultFiles)
			{
				try (Reader reader = new FileReader(file))
				{
					String typeName = file.getName().replace(".json", "").toUpperCase().replace("-", "_");
					VaultType vaultType = VaultType.valueOf(typeName);

					List<BankedItem> parsedItems = gson.fromJson(reader, BankedItem.LIST_TYPE);
					if (parsedItems != null && !parsedItems.isEmpty())
					{
						accountVaults.put(vaultType, parsedItems);
					}
				}
				catch (Exception e)
				{
					log.error("Failed to read items from vault file: {}", file.getName(), e);
				}
			}

			if (!accountVaults.isEmpty())
			{
				allData.put(accountHash, accountVaults);
			}
		}
		return allData;
	}

	/**
	 * Safely saves an account hash mapping to disk on a background thread.
	 */
	public void saveAccountHashMappingAsync(long accountHash, String accountName)
	{
		Future<?> future = executor.submit(() -> {
			File mappingFile = ACCOUNT_HASH_MAPPINGS;

			if (!ensureDirectoryExists(mappingFile.getParentFile())) {
				return;
			}

			synchronized (mappingsLock) {
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

				for (String accountHash : properties.stringPropertyNames())
				{
					try {
						long hash = Long.parseLong(accountHash);
						String name = properties.getProperty(accountHash);
						mappings.put(hash, name);
					} catch (NumberFormatException e) {
						log.warn("Found invalid account hash in mappings file: {}", accountHash);
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

	/**
	 * Merge CSV and JSON wave log files
	 */
	public void mergeColosseumWaveLogs()
	{
		mergeAllWaveLogsJson();
		mergeAllWaveLogsCsv();
	}

	/**
	 * Merges all Colosseum wave log json files into a single csv file
	 */
	private void mergeAllWaveLogsJson() {
		Future<?> future = executor.submit(() -> {
			if (!PluginConstants.COLOSSEUM_TRIALS_DIR.exists() || !PluginConstants.COLOSSEUM_TRIALS_DIR.isDirectory()) {
				log.debug("Colosseum root directory does not exist. Skipping merge.");
				return;
			}
			log.debug("Attempting to merge all Colosseum wave json logs into {}", COLOSSEUM_WAVE_LOG_MERGED_JSON);

			File[] attemptDirs = PluginConstants.COLOSSEUM_TRIALS_DIR.listFiles(File::isDirectory);
			if (attemptDirs == null || attemptDirs.length == 0) {
				return;
			}

			if (!ensureDirectoryExists(COLOSSEUM_WAVE_LOG_MERGED_JSON.getParentFile())) {
				return;
			}

			int nWaves = 0;
			int nLogs = 0;

			try (BufferedWriter bw = Files.newBufferedWriter(COLOSSEUM_WAVE_LOG_MERGED_JSON.toPath(), StandardCharsets.UTF_8);
				 JsonWriter writer = new JsonWriter(bw)) {

				writer.setIndent("  ");
				writer.beginArray();

				for (File dir : attemptDirs) {
					String attemptId = dir.getName();
					File logFile = new File(dir, attemptId + "_wave-log.json");

					if (!logFile.exists() || !logFile.isFile()) {
						continue;
					}

					try (Reader reader = new FileReader(logFile)) {
						JsonObject attemptJson = gson.fromJson(reader, JsonObject.class);

						if (attemptJson != null && attemptJson.has("waves") && attemptJson.has("timestamp")) {
							long timestamp = attemptJson.get("timestamp").getAsLong();
							String attemptResult = attemptJson.get("result").getAsString();
							JsonArray waves = attemptJson.getAsJsonArray("waves");
							boolean addedWave = false;
							for (JsonElement waveElement : waves) {
								if (waveElement.isJsonObject()) {
									JsonObject waveObj = waveElement.getAsJsonObject();
									waveObj.addProperty("attemptId", attemptId);
									waveObj.addProperty("attemptTimestamp", timestamp);
									waveObj.addProperty("attemptResult", attemptResult);

									gson.toJson(waveObj, writer);

									nWaves++;
									if (!addedWave) {
										nLogs++;
										addedWave = true;
									}
								}
							}
						}
					} catch (Exception e) {
						log.error("Failed to parse wave log for attempt: {}", attemptId, e);
					}
				}

				writer.endArray();
				log.debug("Successfully merged data of {} waves from {} log files into {}", nWaves, nLogs, COLOSSEUM_WAVE_LOG_MERGED_JSON.getName());

			} catch (IOException e) {
				log.error("Failed to write merged Colosseum wave logs to {}", COLOSSEUM_WAVE_LOG_MERGED_JSON.getName(), e);
			}
		});
		pendingWrites.add(future);
	}

	/**
	 * Merges all Colosseum wave log csv files into a single csv file
	 */
	private void mergeAllWaveLogsCsv() {
		Future<?> future = executor.submit(() -> {
			if (!PluginConstants.COLOSSEUM_ROOT_DIR.exists() || !PluginConstants.COLOSSEUM_ROOT_DIR.isDirectory()) {
				log.debug("Colosseum root directory does not exist. Skipping CSV merge.");
				return;
			}
			log.debug("Attempting to merge all Colosseum wave csv logs...");

			File[] attemptDirs = PluginConstants.COLOSSEUM_TRIALS_DIR.listFiles(File::isDirectory);
			if (attemptDirs == null || attemptDirs.length == 0) {
				return;
			}

			if (!ensureDirectoryExists(COLOSSEUM_WAVE_LOG_MERGED_CSV.getParentFile())) {
				return;
			}

			final String[] CSV_HEADER = "wave,status,attemptResult,accountName,tag,itemId,itemName,quantity,modifierChoice_I,modifierChoice_II,modifierChoice_III,chosenModifier,activeModifiers,timeTaken,damageTaken,speedBonus,damageBonus,modifierGlory,completionBonus,waveGlory,totalGlory,totalTimeTaken,serpentShamanSpawnX,serpentShamanSpawnY,javelinColossusSpawnAX,javelinColossusSpawnAY,javelinColossusSpawnBX,javelinColossusSpawnBY,manticoreSpawnAX,manticoreSpawnAY,manticoreSequenceA,manticoreSpawnBX,manticoreSpawnBY,manticoreSequenceB,shockwaveColossusSpawnAX,shockwaveColossusSpawnAY,shockwaveColossusSpawnBX,shockwaveColossusSpawnBY,jaguarWarriorReinfSpawnX,jaguarWarriorReinfSpawnY,serpentShamanReinfSpawnX,serpentShamanReinfSpawnY,minotaurReinfSpawnX,minotaurReinfSpawnY".split(",");

			int masterAttemptResultIndex = java.util.Arrays.asList(CSV_HEADER).indexOf("attemptResult");
			int nLogs = 0;
			int nWaves = 0;

			try (BufferedWriter bw = Files.newBufferedWriter(COLOSSEUM_WAVE_LOG_MERGED_CSV.toPath(), StandardCharsets.UTF_8);
				 PrintWriter out = new PrintWriter(bw)) {

				out.print("attemptId,attemptTimestamp,");
				out.println(String.join(",", CSV_HEADER));

				for (File dir : attemptDirs) {
					String attemptId = dir.getName();

					int firstUnderscore = attemptId.indexOf('_');
					String attemptTimestamp = firstUnderscore != -1 ? attemptId.substring(firstUnderscore + 1) : attemptId;

					File csvFile = new File(dir, attemptId + "_wave-log.csv");

					if (!csvFile.exists() || !csvFile.isFile()) {
						continue;
					}

					try (BufferedReader reader = Files.newBufferedReader(csvFile.toPath(), StandardCharsets.UTF_8)) {
						String headerLine = reader.readLine();
						if (headerLine == null) continue;

						String[] localHeader = headerLine.split(",", -1);
						int[] headerMap = new int[localHeader.length];
						int localStatusIndex = -1;

						for (int i = 0; i < localHeader.length; i++) {
							headerMap[i] = -1;

							if (localHeader[i].trim().equalsIgnoreCase("status")) {
								localStatusIndex = i;
							}

							for (int j = 0; j < CSV_HEADER.length; j++) {
								if (localHeader[i].trim().equalsIgnoreCase(CSV_HEADER[j])) {
									headerMap[i] = j;
									break;
								}
							}
						}

						java.util.List<String> lines = new java.util.ArrayList<>();
						String line;
						while ((line = reader.readLine()) != null) {
							if (line.trim().isEmpty()) continue;
							lines.add(line);
						}

						if (lines.isEmpty()) continue;

						nLogs++;
						nWaves += lines.size();
						String attemptResult = "";
						if (localStatusIndex != -1) {
							String[] finalRowValues = lines.get(lines.size() - 1).split(",", -1);
							if (finalRowValues.length > localStatusIndex) {
								attemptResult = finalRowValues[localStatusIndex];
							}
						}

						for (String currentLine : lines) {
							String[] localValues = currentLine.split(",", -1);
							String[] alignedValues = new String[CSV_HEADER.length];

							Arrays.fill(alignedValues, "");

							for (int i = 0; i < localValues.length; i++) {
								int masterIndex = headerMap[i];
								if (masterIndex != -1) {
									alignedValues[masterIndex] = localValues[i];
								}
							}

							if (masterAttemptResultIndex != -1) {
								alignedValues[masterAttemptResultIndex] = attemptResult;
							}

							out.print(attemptId);
							out.print(',');
							out.print(attemptTimestamp);
							out.print(',');
							out.println(String.join(",", alignedValues));
						}
					} catch (Exception e) {
						log.error("Failed to parse CSV wave log for attempt: {}", attemptId, e);
					}
				}
				log.debug("Successfully merged all {} waves from {} Colosseum CSV wave logs into {}", nWaves, nLogs, COLOSSEUM_WAVE_LOG_MERGED_CSV.getName());
			} catch (IOException e) {
				log.error("Failed to write merged Colosseum CSV file", e);
			}
		});
		pendingWrites.add(future);
	}

	/**
	 * Serializes the TrackedSupplies object into a formatted JSON file.
	 */
	public void exportToJson(File file, TrackedSuppliesDTO supplies)
	{
		try (FileWriter writer = new FileWriter(file))
		{
			gson.toJson(supplies, writer);
			log.debug("Successfully exported supplies to JSON: {}", file.getAbsolutePath());
		}
		catch (IOException e)
		{
			log.error("Failed to write JSON export.", e);
		}
	}

	/**
	 * Flattens the TrackedSupplies object into a CSV format.
	 * Format: Category, Identifier, Quantity
	 */
	public void exportToCsv(File file, TrackedSuppliesDTO supplies)
	{
		try (PrintWriter writer = new PrintWriter(new FileWriter(file)))
		{
			int totalGp = 0;
			writer.println("Category,Identifier,Quantity,Value (GP)");

			if (supplies.getConsumedItems() != null)
			{
				for (Map.Entry<String, ValuedItemStack> entry : supplies.getConsumedItems().entrySet())
				{
					writer.printf("Item,%s,%d,%d%n", entry.getKey(), entry.getValue().getCount(), entry.getValue().getTotalValueInGp());
					totalGp += entry.getValue().getTotalValueInGp();
				}
			}

			if (supplies.getConsumedCharges() != null)
			{
				for (Map.Entry<String, ValuedItemStack> entry : supplies.getConsumedCharges().entrySet())
				{
					writer.printf("Charge,%s,%d,%d%n", entry.getKey(), entry.getValue().getCount(), entry.getValue().getTotalValueInGp());
					totalGp += entry.getValue().getTotalValueInGp();
				}
			}

			if (supplies.getConsumedDoses() != null)
			{
				for (Map.Entry<String, ValuedItemStack> entry : supplies.getConsumedDoses().entrySet())
				{
					writer.printf("Dose,%s,%d,%d%n", entry.getKey(), entry.getValue().getCount(), entry.getValue().getTotalValueInGp());
					totalGp += entry.getValue().getTotalValueInGp();
				}
			}

			writer.printf("Total,,,%d%n", totalGp);

			log.debug("Successfully exported supplies to CSV: {}", file.getAbsolutePath());
		}
		catch (IOException e)
		{
			log.error("Failed to write CSV export.", e);
		}
	}

	/**
	 * Writes any Java object to a specified file as JSON.
	 */
	public void writeJson(File file, Object data)
	{
		Future<?> future = executor.submit(() -> {
			if (file.getParentFile() != null && !file.getParentFile().exists())
			{
				file.getParentFile().mkdirs();
			}

			try (FileWriter writer = new FileWriter(file))
			{
				gson.toJson(data, writer);
			}
			catch (IOException e)
			{
				log.error("Failed to write JSON to file: {}", file.getName(), e);
			}
		});
		pendingWrites.add(future);
	}

	/**
	 * Reads JSON from a file and parses it into the specified Generic Type.
	 */
	public <T> T readJson(File file, Type typeOfT)
	{
		if (!file.exists()) return null;

		try (FileReader reader = new FileReader(file))
		{
			return gson.fromJson(reader, typeOfT);
		}
		catch (Exception e)
		{
			log.error("Failed to read JSON from file: {}", file.getName(), e);
			return null;
		}
	}

	/**
	 * Reads a JSON Lines (.jsonl) file and parses it into a list of the specified class.
	 */
	public <T> List<T> readJsonlFile(File file, Class<T> classOfT)
	{
		List<T> entries = new ArrayList<>();
		if (file == null || !file.exists()) return entries;

		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				T entry = gson.fromJson(line, classOfT);
				if (entry != null)
				{
					entries.add(entry);
				}
			}
		}
		catch (Exception e)
		{
			log.error("Failed to read JSONL file: {}", file.getAbsolutePath(), e);
		}

		return entries;
	}
}