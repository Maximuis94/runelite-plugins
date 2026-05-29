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

import com.datalogger.DataLoggerConfig;
import com.datalogger.constants.PluginConstants;
import com.datalogger.models.enums.ExchangeLoggerCsvFileStrategy;
import com.datalogger.models.enums.ExchangeLoggerJsonFileStrategy;
import com.datalogger.models.grandexchange.GeLedgerEntry;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class GrandExchangeExportService
{
	private final FileIOService fileIOService;
	private final DataLoggerConfig config;
	private final ScheduledExecutorService executor;
	private final Gson gson;

	private static final DateTimeFormatter DAILY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
	private static final DateTimeFormatter WEEKLY_FORMAT = DateTimeFormatter.ofPattern("YYYY-ww").withZone(ZoneId.systemDefault());
	private static final DateTimeFormatter MONTHLY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneId.systemDefault());
	public static final String CSV_HEADER = "ItemId,ItemName,OfferCreationTime,Timestamp,TradeType,Quantity,OfferQuantity,Price,OfferPrice,Value,Tax,AccountName,AccountHash,GeSlot,IsHistoryEntry,IsCancelled";

	private boolean geIncludeItemId = true;
	private boolean geIncludeItemName = true;
	private boolean geIncludeIsBuy = true;
	private boolean geIncludeQuantity = true;
	private boolean geIncludePrice = true;
	private boolean geIncludeValue = true;
	private boolean geIncludeTax = true;
	private boolean geIncludeAccountName = true;
	private boolean geIncludeAccountHash = true;
	private boolean geIncludeGeSlot = true;
	private boolean geIncludeIsCancelled = true;
	private boolean geIncludeOfferCreationTime = true;
	private boolean geIncludeExactTimestamp = true;
	private boolean geIncludeOriginalOfferQuantity = true;
	private boolean geIncludeOriginalOfferPrice = true;
	private ExchangeLoggerJsonFileStrategy jsonStrategy = null;
	private ExchangeLoggerCsvFileStrategy csvStrategy = null;

	@Inject
	public GrandExchangeExportService(FileIOService fileIOService, DataLoggerConfig config, ScheduledExecutorService executor, Gson gson)
	{
		this.fileIOService = fileIOService;
		this.config = config;
		this.executor = executor;
		this.gson = gson;
		updateConfigurations();
	}

	public void updateConfigurations()
	{
//		loggerIsEnabled = config.logGrandExchange() && isOnPermanentWorld();
		jsonStrategy = config.geJsonFileStrategy();
		csvStrategy = config.geCsvFileStrategy();
		geIncludeItemId = config.geIncludeItemId();
		geIncludeItemName = config.geIncludeItemName();
		geIncludeIsBuy = config.geIncludeIsBuy();
		geIncludeQuantity = config.geIncludeQuantity();
		geIncludePrice = config.geIncludePrice();
		geIncludeValue = config.geIncludeValue();
		geIncludeTax = config.geIncludeTax();
		geIncludeAccountName = config.geIncludeAccountName();
		geIncludeAccountHash = config.geIncludeAccountHash();
		geIncludeGeSlot = config.geIncludeGeSlot();
		geIncludeIsCancelled = config.geIncludeIsCancelled();
		geIncludeOfferCreationTime = config.geIncludeOfferCreationTime();
		geIncludeExactTimestamp = config.geIncludeExactTimestamp();
		geIncludeOriginalOfferQuantity = config.geIncludeOriginalOfferQuantity();
		geIncludeOriginalOfferPrice = config.geIncludeOriginalOfferPrice();
	}

	/**
	 * Iterates over all historical JSONL files and exports them asynchronously
	 * based on the active config strategies.
	 */
	public void exportHistoricalData(Runnable onSuccess, Runnable onNoStrategy)
	{
		updateConfigurations();
		if (csvStrategy == ExchangeLoggerCsvFileStrategy.NONE && jsonStrategy == ExchangeLoggerJsonFileStrategy.NONE)
		{
			if (onNoStrategy != null) onNoStrategy.run();
			return;
		}

		executor.submit(() -> {
			File sourceDir = FileIOService.INTERNAL_GE_OFFERS_DIR;
			if (!sourceDir.exists() || !sourceDir.isDirectory())
			{
				log.warn("Internal GE offers directory does not exist.");
				return;
			}

			File[] files = sourceDir.listFiles((dir, name) -> name.endsWith(".jsonl"));
			if (files == null || files.length == 0) return;

			int totalExported = 0;

			for (File file : files)
			{
				List<GeLedgerEntry> entries = fileIOService.readJsonlFile(file, GeLedgerEntry.class);
				if (entries == null || entries.isEmpty()) continue;

				if (csvStrategy != ExchangeLoggerCsvFileStrategy.NONE)
				{
					batchAndWriteCsv(entries, csvStrategy);
				}

				if (jsonStrategy != ExchangeLoggerJsonFileStrategy.NONE)
				{
					batchAndWriteJson(entries, jsonStrategy);
				}

				totalExported += entries.size();
			}

			log.debug("Successfully exported {} historical GE entries.", totalExported);
			if (onSuccess != null)
			{
				onSuccess.run();
			}
		});
	}

	private void batchAndWriteCsv(List<GeLedgerEntry> entries, ExchangeLoggerCsvFileStrategy strategy)
	{
		Map<File, List<GeLedgerEntry>> batches = new HashMap<>();

		for (GeLedgerEntry entry : entries)
		{
			File targetFile = resolveTargetFile(entry, strategy.name().replace("CSV_", ""), ".csv");
			batches.computeIfAbsent(targetFile, k -> new ArrayList<>()).add(entry);
		}

		for (Map.Entry<File, List<GeLedgerEntry>> batch : batches.entrySet())
		{
			File targetFile = batch.getKey();
			targetFile.getParentFile().mkdirs();
			boolean isNew = !targetFile.exists();

			try (BufferedWriter bw = Files.newBufferedWriter(targetFile.toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
				 PrintWriter out = new PrintWriter(bw))
			{
				if (isNew)
				{
					out.println(CSV_HEADER);
				}
				for (GeLedgerEntry entry : batch.getValue())
				{
					out.println(formatCsvRow(entry));
				}
			}
			catch (IOException e)
			{
				log.error("Failed to write CSV batch to {}", targetFile.getName(), e);
			}
		}
	}

	private void batchAndWriteJson(List<GeLedgerEntry> entries, ExchangeLoggerJsonFileStrategy strategy)
	{
		Map<File, List<GeLedgerEntry>> batches = new HashMap<>();

		for (GeLedgerEntry entry : entries)
		{
			String ext = strategy == ExchangeLoggerJsonFileStrategy.JSONLINE ? ".jsonl" : ".json";
			File targetFile = resolveTargetFile(entry, strategy.name().replace("JSON_", ""), ext);
			batches.computeIfAbsent(targetFile, k -> new ArrayList<>()).add(entry);
		}

		for (Map.Entry<File, List<GeLedgerEntry>> batch : batches.entrySet())
		{
			File targetFile = batch.getKey();
			targetFile.getParentFile().mkdirs();

			if (strategy == ExchangeLoggerJsonFileStrategy.JSONLINE)
			{
				try (BufferedWriter bw = Files.newBufferedWriter(targetFile.toPath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND))
				{
					for (GeLedgerEntry entry : batch.getValue())
					{
						bw.write(gson.toJson(buildGeJsonObject(entry)));
						bw.newLine();
					}
				}
				catch (IOException e)
				{
					log.error("Failed to write JSONL batch to {}", targetFile.getName(), e);
				}
			}
			else
			{
				// Read existing array to memory, append batch, write exactly once.
				JsonArray array = new JsonArray();
				if (targetFile.exists() && targetFile.length() > 0)
				{
					try (FileReader reader = new FileReader(targetFile))
					{
						JsonArray existing = gson.fromJson(reader, JsonArray.class);
						if (existing != null) array = existing;
					}
					catch (Exception ignored) {}
				}

				for (GeLedgerEntry entry : batch.getValue())
				{
					array.add(buildGeJsonObject(entry));
				}

				try (FileWriter fw = new FileWriter(targetFile))
				{
					gson.toJson(array, fw);
				}
				catch (IOException e)
				{
					log.error("Failed to write JSON batch to {}", targetFile.getName(), e);
				}
			}
		}
	}

	private File resolveTargetFile(GeLedgerEntry entry, String frequency, String extension)
	{
		String accountLower = entry.getAccountName() != null ? entry.getAccountName().toLowerCase() : "unknown";
		File root = new File(PluginConstants.GRAND_EXCHANGE_DIR, accountLower);

		if (frequency.equals("SINGLE") || frequency.equals("JSONLINE"))
		{
			return new File(root, "exchange-offers" + extension);
		}

		long ts = entry.getExactTimestamp() > 0 ? entry.getExactTimestamp() : entry.getParseTime();
		if (ts <= 0) ts = System.currentTimeMillis();
		Instant instant = Instant.ofEpochMilli(ts);

		String dateSuffix;
		switch (frequency)
		{
			case "DAILY":
				dateSuffix = DAILY_FORMAT.format(instant);
				break;
			case "WEEKLY":
				dateSuffix = WEEKLY_FORMAT.format(instant);
				break;
			case "MONTHLY":
				dateSuffix = MONTHLY_FORMAT.format(instant);
				break;
			default:
				dateSuffix = "";
		}

		// Align with enum naming ("exchange-offers_2026..." for CSV vs "exchange-offers-2026..." for JSON)
		String separator = extension.equals(".csv") ? "_" : "";
		return new File(root, "exchange-offers" + separator + dateSuffix + extension);
	}

	public String formatCsvRow(GeLedgerEntry entry)
	{
		String safeItemName = entry.getItemName() != null ? entry.getItemName() : "Unknown";
		if (safeItemName.contains(",")) safeItemName = "\"" + safeItemName + "\"";

		String creationTimeStr = entry.getOfferCreationTime() > 0 ? Instant.ofEpochMilli(entry.getOfferCreationTime()).toString() : "";
		long mainTimeMillis = entry.getExactTimestamp() > 0 ? entry.getExactTimestamp() : entry.getParseTime();
		String timeStr = mainTimeMillis > 0 ? Instant.ofEpochMilli(mainTimeMillis).toString() : "";
		String tradeType = entry.isBuy() ? "BUY" : "SELL";

		return String.join(",",
			String.valueOf(entry.getItemId()),
			safeItemName, creationTimeStr, timeStr, tradeType,
			String.valueOf(entry.getQuantity()), String.valueOf(entry.getOriginalOfferQuantity()),
			String.valueOf(entry.getPrice()), String.valueOf(entry.getOriginalOfferPrice()),
			String.valueOf(entry.getValue()), String.valueOf(entry.getTax()),
			entry.getAccountName() != null ? entry.getAccountName() : "",
			String.valueOf(entry.getAccountHash()), String.valueOf(entry.getGeSlot()),
			String.valueOf(entry.isHistoryEntry()), String.valueOf(entry.isCancelled())
		);
	}

	/**
	 * Construct a JsonObject based on enabled configurations and return it
	 */
	public JsonObject buildGeJsonObject(GeLedgerEntry ledgerEntry)
	{
		JsonObject jsonObject = new JsonObject();
		if (geIncludeItemId) jsonObject.addProperty("itemId", ledgerEntry.getItemId());
		if (geIncludeItemName) jsonObject.addProperty("itemName", ledgerEntry.getItemName());
		if (geIncludeIsBuy) jsonObject.addProperty("isBuy", ledgerEntry.isBuy());
		if (geIncludeQuantity) jsonObject.addProperty("quantity", ledgerEntry.getQuantity());
		if (geIncludePrice) jsonObject.addProperty("price", ledgerEntry.getPrice());
		if (geIncludeValue) jsonObject.addProperty("value", ledgerEntry.getValue());
		if (geIncludeTax) jsonObject.addProperty("tax", ledgerEntry.getTax());
		if (geIncludeAccountName) jsonObject.addProperty("accountName", ledgerEntry.getAccountName());
		if (geIncludeAccountHash) jsonObject.addProperty("accountHash", ledgerEntry.getAccountHash());
		if (geIncludeGeSlot) jsonObject.addProperty("geSlot", ledgerEntry.getGeSlot());
		if (geIncludeIsCancelled) jsonObject.addProperty("isCancelled", ledgerEntry.isCancelled());
		if (geIncludeOfferCreationTime) jsonObject.addProperty("offerCreationTime", ledgerEntry.getOfferCreationTime());
		if (geIncludeExactTimestamp) jsonObject.addProperty("exactTimestamp", ledgerEntry.getExactTimestamp());
		if (geIncludeOriginalOfferQuantity) jsonObject.addProperty("originalOfferQuantity", ledgerEntry.getOriginalOfferQuantity());
		if (geIncludeOriginalOfferPrice) jsonObject.addProperty("originalOfferPrice", ledgerEntry.getOriginalOfferPrice());
		return jsonObject;
	}
}