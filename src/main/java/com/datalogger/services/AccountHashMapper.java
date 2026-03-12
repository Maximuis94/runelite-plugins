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
package com.datalogger.services;

import com.datalogger.events.AccountHashResolved;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
public class AccountHashMapper
{
	private final Map<Long, String> hashToNameCache = new ConcurrentHashMap<>();

	/**
	 * Listens for the custom global session event.
	 * Automatically updates the hash-to-name mapping cache and file.
	 */
	@Subscribe
	public void onAccountHashResolved(AccountHashResolved event)
	{
		long accountHashLong = event.getAccountHash();
		String accountName = event.getAccountName();
		log.debug("AccountHashMapper received resolved session for {} ({})", accountName, accountHashLong);
		updateAccountHashMapping(accountHashLong, accountName);
	}

	/**
	 * Attempts to find the account hash associated with a given account name.
	 * @param accountName The account name to search for
	 * @return The account hash, or -1L if no matching account is found
	 */
	public long getAccountHashByAccountName(String accountName)
	{
		if (accountName == null || accountName.trim().isEmpty()) {
			return -1L;
		}

		for (Map.Entry<Long, String> entry : hashToNameCache.entrySet())
		{
			if (entry.getValue().equalsIgnoreCase(accountName.trim()))
			{
				return entry.getKey();
			}
		}
		return -1L;
	}

	public void loadMappings()
	{
		File mappingFile = FileIOService.ACCOUNT_HASH_MAPPINGS;

		if (mappingFile.exists())
		{
			try (FileInputStream in = new FileInputStream(mappingFile))
			{
				Properties properties = new Properties();
				properties.load(in);

				for (String key : properties.stringPropertyNames())
				{
					try {
						long hash = Long.parseLong(key);
						hashToNameCache.put(hash, properties.getProperty(key));
					} catch (NumberFormatException e) {
						log.warn("Found invalid account hash in mappings file: {}", key);
					}
				}

				log.info("Loaded {} account hash mappings into memory.", hashToNameCache.size());
			}
			catch (Exception e)
			{
				log.error("Failed to read existing account hash mappings", e);
			}
		}
	}

	/**
	 * Get the account name for a given account hash.
	 */
	public String getAccountName(long accountHash)
	{
		return hashToNameCache.getOrDefault(accountHash, "Unknown");
	}

	/**
	 * Update a cached accountHash-accountName mapping file with the given accountHash and accountName,
	 * and update the live memory cache.
	 */
	public void updateAccountHashMapping(long accountHash, String accountName)
	{
		if (accountHash == -1L || accountName == null || accountName.isEmpty())
		{
			return;
		}

		hashToNameCache.put(accountHash, accountName);

		File mappingFile = FileIOService.ACCOUNT_HASH_MAPPINGS;
		File parentDir = mappingFile.getParentFile();
		if (parentDir != null && !parentDir.exists())
		{
			parentDir.mkdirs();
		}

		synchronized (mappingFile.getAbsolutePath().intern())
		{
			Properties properties = new Properties();

			if (mappingFile.exists())
			{
				try (FileInputStream in = new FileInputStream(mappingFile))
				{
					properties.load(in);
				}
				catch (Exception e)
				{
					log.error("Failed to read existing account hash mappings during update", e);
				}
			}
			properties.setProperty(String.valueOf(accountHash), accountName);

			try (FileOutputStream out = new FileOutputStream(mappingFile))
			{
				properties.store(out, "Account Hash to Account Name Mappings");
				log.debug("Successfully updated account hash mapping for: {}", accountName);
			}
			catch (Exception e)
			{
				log.error("Failed to write updated account hash mappings", e);
			}
		}
	}

	/**
	 * Returns a list of all accountHashes registered
	 */
	public List<Long> getAccountHashes()
	{
		return new ArrayList<>(hashToNameCache.keySet());
	}
}