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
import com.datalogger.framework.DataRow;
import com.datalogger.framework.LogType;
import com.datalogger.models.colosseum.ColosseumAttempt;
import com.datalogger.models.colosseum.ColosseumState;
import com.datalogger.models.colosseum.ColosseumWave;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;

@Slf4j
@Singleton
public class FileIOService
{

	@Inject
	private FileIOService(DataLoggerConfig config, Gson gson) {
		this.gson = gson.newBuilder()
			.setPrettyPrinting()
			.create();
	}

	private final Gson gson;

	public final File PLUGIN_ROOT = new File(RuneLite.RUNELITE_DIR, "data-logger");
	private final File STATE_DIR = new File(PLUGIN_ROOT, "state");
	private final File COLOSSEUM_ROOT_DIR = new File(PLUGIN_ROOT, "colosseum");
	private final File COLOSSEUM_TIMELINE_DIR = new File(COLOSSEUM_ROOT_DIR, "timeline");
	private final File COLOSSEUM_LOG_DIR = new File(COLOSSEUM_ROOT_DIR, "log");
	private final File COLOSSEUM_CSV_DIR = new File(COLOSSEUM_ROOT_DIR, "csv");
	private final File COLOSSEUM_SCREENSHOT_DIR = new File(COLOSSEUM_ROOT_DIR, "screenshot");
	private final File COLOSSEUM_TEMP_DIR = new File(COLOSSEUM_ROOT_DIR, "temp");

	/**
	 * Parse the rows associated with the given LogType and account
	 * @param type The log that is to be parsed
	 * @param account The account of which the entries are to be parsed
	 * @return A List of DataRow instances parsed from the associated log file
	 * @param <T> The DataRow class associated with LogType
	 */
	public <T extends DataRow> List<T> loadLogs(LogType type, String account) {
		File file = getTargetFile(type, account);
		List<T> data = new ArrayList<>();

		if (!file.exists()) return data;

		// The enum now provides the parser!
		Function<String, T> parser = (Function<String, T>) type.getParser();

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			reader.readLine(); // Skip header
			String line;
			while ((line = reader.readLine()) != null) {
				T obj = parser.apply(line);
				if (obj != null) data.add(obj);
			}
		} catch (IOException e) {
			log.error("Failed to read logs for {}", type, e);
		}
		return data;
	}

	/**
	 * Performs an atomic append to a file.
	 * Handles directory creation on-the-fly to prevent crashes if folders are moved/deleted.
	 * @param file The file that is to be modified
	 * @param header The header of the CSV file that will be added if the file does not exist
	 * @param row The content of the line that is to be added to the file
	 */
	public void atomicWrite(File file, String header, String row) throws IOException {
		File parent = file.getParentFile();
		if (parent != null && !parent.exists() && !parent.mkdirs()) {
			throw new IOException("Failed to create directory: " + parent.getAbsolutePath());
		}

		StringBuilder sb = new StringBuilder();
		boolean isNew = !file.exists() || file.length() == 0;

		if (isNew && header != null) {
			sb.append(header).append("\n");
		}
		sb.append(row).append("\n");

		java.nio.file.Files.write(
			file.toPath(),
			sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8),
			java.nio.file.StandardOpenOption.CREATE,
			java.nio.file.StandardOpenOption.APPEND
		);
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
		if (!STATE_DIR.exists()) {
			STATE_DIR.mkdirs();
		}
		return new File(STATE_DIR, "state_" + accountHash + ".properties");
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

		new Thread(() -> {
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
		}).start();
	}

	private List<ColosseumStateDTO> loadWaveStates(File file) {
		try (Reader reader = new FileReader(file)) {
			// 1. Parse directly into an array using the standard .class reference
			ColosseumStateDTO[] dataArray = gson.fromJson(reader, ColosseumStateDTO[].class);

			// 2. Convert the array to a List (or return an empty list if null)
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
		new Thread(() -> {
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

				// Delete the temp directory
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
		}).start();
	}

	/**
	 * Log the given attempt
	 */
	public void logColosseumAttempt(ColosseumAttempt attempt) {
		// 1. Define the directory: .runelite/colosseum-logs/

		// 2. Ensure the directory exists
		if (!COLOSSEUM_LOG_DIR.exists()) {
			COLOSSEUM_LOG_DIR.mkdirs();
		}

		// 3. Create a unique filename using the attempt ID (Timestamp)
		File targetFile = logFile(attempt.getStartTime());

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
}