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

import com.datalogger.events.AccountSessionStarted;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.Subscribe;

/**
 * Service class that manages accountHash/accountName info
 */
@Slf4j
@Singleton
public class AccountHashMapper
{
	private final Map<Long, String> hashToNameCache = new ConcurrentHashMap<>();

	private final FileIOService fileIOService;
	private final ScheduledExecutorService executor;

	@Inject
	private AccountHashMapper(FileIOService fileIOService, ScheduledExecutorService executor)
	{
		this.fileIOService = fileIOService;
		this.executor = executor;
	}


	/**
	 * Listens for the custom global session event.
	 * Automatically updates the hash-to-name mapping cache and file.
	 */
	@Subscribe
	public void onAccountSessionStarted(AccountSessionStarted event)
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
		executor.submit(() -> {
			Map<Long, String> diskMappings = fileIOService.loadAccountHashMappingsRaw();

			if (!diskMappings.isEmpty()) {
				hashToNameCache.putAll(diskMappings);
			}

			log.debug("Loaded {} account hash mappings into memory.", hashToNameCache.size());
		});
	}

	/**
	 * Get the account name for a given account hash. Return the hash as String if there is no matching accountName
	 */
	public String getAccountName(long accountHash)
	{
		return hashToNameCache.getOrDefault(accountHash, String.valueOf(accountHash));
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
		fileIOService.saveAccountHashMappingAsync(accountHash, accountName);
	}

	/**
	 * Returns a list of all accountHashes registered
	 */
	public List<Long> getAccountHashes()
	{
		return new ArrayList<>(hashToNameCache.keySet());
	}
}