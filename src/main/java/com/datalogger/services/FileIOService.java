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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;

@Slf4j
@Singleton
public class FileIOService
{
	private final DataLoggerConfig config;

	@Inject
	private FileIOService(DataLoggerConfig config) {
		this.config = config;
	}

	public final File PLUGIN_ROOT = new File(RuneLite.RUNELITE_DIR, "data-logger");
	private final File STATE_DIR = new File(PLUGIN_ROOT, "state");

	/**
	 * Parse registered logs of the file and return them
	 * @param file The log file
	 * @param parser The parser method used to parse the CSV rows
	 * @return A List of DataRow instances
	 * @param <T> DataRow class that represents one logged row
	 */
	public <T> List<T> loadLogs(File file, Function<String, T> parser) {
		List<T> data = new ArrayList<>();
		if (!file.exists()) return data;

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			boolean isHeader = true;
			while ((line = reader.readLine()) != null) {
				if (isHeader) { // Skip the CSV_HEADER
					isHeader = false;
					continue;
				}
				T obj = parser.apply(line);
				if (obj != null) data.add(obj);
			}
		} catch (IOException e) {
			log.error("Failed to read log file: {}", file.getName(), e);
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
	 * @param dataType The data that is to be logged
	 * @param account The account that the data relates to
	 * @return File reference based on inputs provided and strategy selected
	 */
	public File getTargetFile(String dataType, String account) {
		if (config == null) {
			log.error("Config is NULL in DataLoggerUtils!");
			return PLUGIN_ROOT;
		}
		String safeAccount = account.toLowerCase();
		String date = java.time.LocalDate.now().toString();

		File path = new File(new File(PLUGIN_ROOT, dataType), safeAccount);
		String fileName = String.format("%s_%s_%s.csv", dataType, safeAccount, date);
		return new File(path, fileName);
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
}