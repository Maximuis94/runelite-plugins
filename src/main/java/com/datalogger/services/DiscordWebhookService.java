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
import com.datalogger.events.DataLoggerConfigChanged;
import com.datalogger.events.ScreenshotFileCreated;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.Subscribe;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Singleton
public class DiscordWebhookService
{
	private boolean isEnabled;
	private String globalWebhookUrl;

	private final OkHttpClient okHttpClient;
	private final DataLoggerConfig config;
	private final Gson gson;

	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	private final Cache<String, QueuedPayload> pendingWebhooks = CacheBuilder.newBuilder()
		.expireAfterWrite(2, TimeUnit.MINUTES)
		.build();

	@Inject
	public DiscordWebhookService(OkHttpClient okHttpClient, DataLoggerConfig config, Gson gson)
	{
		this.okHttpClient = okHttpClient;
		this.config = config;
		this.gson = gson;
	}

	@Subscribe
	public void onDataLoggerConfigChanged(DataLoggerConfigChanged event)
	{
		updateConfigFlags();
	}

	private void updateConfigFlags()
	{
		globalWebhookUrl = config.colosseumDiscordWebhookUrl().trim();
		isEnabled = !globalWebhookUrl.isEmpty() && config.enableWebhookBroadcasting();
	}

	/**
	 * Sends a simple text message to the configured webhook URL.
	 */
	public void broadcastMessage(String message)
	{
		if (!isEnabled) return;

		JsonObject payload = new JsonObject();
		payload.addProperty("content", message);
		sendWebhook(globalWebhookUrl, payload);
	}

	/**
	 * Sends a fully formatted JSON embed payload to the given webhook URL without an image.
	 */
	public void sendWebhook(String webhookUrl, JsonObject payload)
	{
		if (webhookUrl == null || webhookUrl.trim().isEmpty() || payload == null) return;

		HttpUrl url = HttpUrl.parse(webhookUrl.trim());
		if (url == null)
		{
			log.warn("Invalid Webhook URL provided.");
			return;
		}

		Request request = new Request.Builder()
			.url(url)
			.post(RequestBody.create(JSON, gson.toJson(payload)))
			.build();

		okHttpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e)
			{
				log.warn("Failed to send webhook to Discord", e);
			}

			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response)
			{
				response.close();
				if (!response.isSuccessful())
				{
					log.warn("Discord webhook returned an error: {}", response.code());
				}
			}
		});
	}

	/**
	 * Queues a webhook payload waiting for a screenshot to be generated.
	 */
	public void queueWebhook(String webhookUrl, JsonObject payload, File screenshotFile)
	{
		if (webhookUrl == null || webhookUrl.trim().isEmpty() || payload == null || screenshotFile == null) return;

		QueuedPayload queuedPayload = new QueuedPayload(webhookUrl, payload, screenshotFile);

		if (screenshotFile.exists())
		{
			log.info("Screenshot already exists, submitting screenshot and payload");
			screenshotWebhook(queuedPayload);
		}
		else
		{
			String absolutePath = screenshotFile.getAbsolutePath();
			log.debug("Queueing webhook payload for expected screenshot: {}", absolutePath);
			pendingWebhooks.put(absolutePath, queuedPayload);
		}
	}

	/**
	 * Listens for the successful creation of a screenshot and fires off any waiting webhooks.
	 */
	@Subscribe
	public void onScreenshotFileCreated(ScreenshotFileCreated event)
	{
		log.info("Received ScreenshotFileCreated event for path: {}", event.getAbsolutePath());
		String path = event.getAbsolutePath();
		QueuedPayload data = pendingWebhooks.getIfPresent(path);

		if (data == null)
		{
			log.info("Did not find a matching cached payload...");
			return;
		}

		log.debug("Found queued webhook! Sending request to webhook");

		pendingWebhooks.invalidate(path);
		screenshotWebhook(data);
	}

	/**
	 * Internal method to handle the Multipart webhook execution with an attached file.
	 */
	private void screenshotWebhook(QueuedPayload data)
	{
		HttpUrl url = HttpUrl.parse(data.getWebhookUrl().trim());
		if (url == null)
		{
			log.warn("Invalid Webhook URL provided in queue.");
			return;
		}

		RequestBody requestBody;
		File screenshotFile = data.getScreenshotFile();

		if (screenshotFile != null && screenshotFile.exists())
		{
			String mimeType = screenshotFile.getName().toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
			RequestBody fileBody = RequestBody.create(MediaType.parse(mimeType), screenshotFile);

			// Build the multipart payload Discord expects
			requestBody = new MultipartBody.Builder()
				.setType(MultipartBody.FORM)
				.addFormDataPart("file", screenshotFile.getName(), fileBody)
				.addFormDataPart("payload_json", gson.toJson(data.getPayload()))
				.build();
		}
		else
		{
			log.warn("Screenshot file was queued but does not exist on disk. Sending payload without image.");
			requestBody = RequestBody.create(JSON, gson.toJson(data.getPayload()));
		}

		Request request = new Request.Builder()
			.url(url)
			.post(requestBody)
			.build();

		okHttpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e)
			{
				log.warn("Failed to send multipart webhook to Discord", e);
			}

			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response)
			{
				response.close();
				if (!response.isSuccessful())
				{
					log.warn("Discord multipart webhook returned an error: {}", response.code());
				}
			}
		});
	}

	/**
	 * Helper class representing a Discord payload waiting for a file.
	 */
	@Getter
	private static class QueuedPayload
	{
		private final String webhookUrl;
		private final JsonObject payload;
		private final File screenshotFile;

		public QueuedPayload(String webhookUrl, JsonObject payload, File screenshotFile)
		{
			this.webhookUrl = webhookUrl;
			this.payload = payload;
			this.screenshotFile = screenshotFile;
		}

		public boolean fileExists()
		{
			return screenshotFile != null && screenshotFile.exists();
		}
	}
}