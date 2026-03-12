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
package com.datalogger;

import com.datalogger.events.AccountSessionStarted;
import com.datalogger.loggers.ColosseumAttemptLogger;
import com.datalogger.loggers.ColosseumTimelineLogger;
import com.datalogger.loggers.GrandExchangeLogger;
import com.datalogger.loggers.ItemVaultLogger;
import com.datalogger.loggers.ScreenshotLogger;
import com.datalogger.services.AccountHashMapper;
import com.datalogger.services.ColosseumScanner;
import com.datalogger.services.FileIOService;
import com.datalogger.services.GrandExchangeHistoryParser;
import com.datalogger.services.ItemVaultParser;
import com.datalogger.ui.DataLoggerPanel;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.WorldType;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Data Logger",
	description = "Logs game data for various activities, like Fortis Colosseum results or the Grand Exchange",
	tags = {"logger", "data", "history", "screenshot", "tracker", "csv", "json", "ge", "grand exchange", "bank", "item", "fortis", "colosseum"}
)
public class DataLoggerPlugin extends Plugin
{
	@Inject private ClientThread clientThread;
	@Inject private Client client;
	@Inject private ClientToolbar clientToolbar;
	@Inject private EventBus eventBus;
	@Inject private DataLoggerConfig config;

	@Inject private AccountHashMapper accountHashMapper;
	@Inject private FileIOService fileIOService;
	@Inject private DataLoggerPanel panel;
	@Inject private ItemVaultLogger itemVaultLogger;

	@Inject private ItemVaultParser itemVaultParser;
	@Inject private GrandExchangeLogger geLogger;
	@Inject private GrandExchangeHistoryParser grandExchangeHistoryParser;
	@Inject private ColosseumAttemptLogger coloLogger;
	@Inject private ColosseumScanner coloScanner;
	@Inject private ColosseumTimelineLogger timelineLogger;
	@Inject private ScreenshotLogger screenshotLogger;

	private NavigationButton navButton;
	private boolean sessionInitialized = false;

	// Registration State Trackers
	private boolean isItemVaultRegistered = false;
	private boolean isGeRegistered = false;
	private boolean isColosseumRegistered = false;
	private boolean isTimelineRegistered = false;
	private boolean isScreenshotRegistered = false;

	@Provides
	DataLoggerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DataLoggerConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		log.info("Data Logger starting up...");

		eventBus.register(accountHashMapper);
		accountHashMapper.loadMappings();
		eventBus.register(fileIOService);
		eventBus.register(itemVaultLogger);
		itemVaultLogger.updateIgnoredAccountHashes();

		toggleItemVault(config.logItemVault());
		toggleGrandExchange(config.logGrandExchange());
		toggleColosseum(config.logColosseum());
		toggleTimeline(config.logWaveTimeline());
		toggleScreenshots(config.screenshotBetweenWaves());

		// 3. Setup UI
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");
		navButton = NavigationButton.builder()
			.tooltip("Data Logger Viewer")
			.icon(icon)
			.priority(5)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown()
	{
		log.info("Data Logger shutting down...");

		if (fileIOService != null) {
			fileIOService.flushAll();
		}

		clientToolbar.removeNavigation(navButton);

		eventBus.unregister(accountHashMapper);
		eventBus.unregister(fileIOService);

		toggleItemVault(false);
		toggleGrandExchange(false);
		toggleColosseum(false);
		toggleTimeline(false);
		toggleScreenshots(false);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(DataLoggerConfig.CONFIG_GROUP))
		{
			return;
		}

		switch (event.getKey())
		{
			case "logItemVault":
				toggleItemVault(config.logItemVault());
				break;
			case "logGrandExchange":
				toggleGrandExchange(config.logGrandExchange());
				break;
			case "logColosseum":
				toggleColosseum(config.logColosseum());
				break;
			case "logWaveTimeline":
				toggleTimeline(config.logWaveTimeline());
				break;
			case "screenshotBetweenWaves":
				toggleScreenshots(config.screenshotBetweenWaves());
				break;
		}
	}

	private void toggleItemVault(boolean enable)
	{
		if (enable && !isItemVaultRegistered) {
			eventBus.register(itemVaultParser);
			isItemVaultRegistered = true;
			log.debug("Item Vault tracking enabled.");
		} else if (!enable && isItemVaultRegistered) {
			eventBus.unregister(itemVaultParser);
			isItemVaultRegistered = false;
			log.debug("Item Vault tracking disabled.");
		}
	}

	private void toggleGrandExchange(boolean enable)
	{
		if (enable && !isGeRegistered) {
			eventBus.register(grandExchangeHistoryParser);
			eventBus.register(geLogger);
			isGeRegistered = true;
			log.debug("Grand Exchange tracking enabled.");
		} else if (!enable && isGeRegistered) {
			eventBus.unregister(grandExchangeHistoryParser);
			eventBus.unregister(geLogger);
			isGeRegistered = false;
			log.debug("Grand Exchange tracking disabled.");
		}
	}

	private void toggleColosseum(boolean enable)
	{
		if (enable && !isColosseumRegistered) {
			eventBus.register(coloLogger);
			eventBus.register(coloScanner);
			coloScanner.updateConfigFlags(true);
			isColosseumRegistered = true;
			log.debug("Colosseum tracking enabled.");
			toggleTimeline(config.logWaveTimeline());

		} else if (!enable && isColosseumRegistered) {
			toggleTimeline(false);

			eventBus.unregister(coloLogger);
			eventBus.unregister(coloScanner);
			coloScanner.updateConfigFlags(false);
			isColosseumRegistered = false;
			log.debug("Colosseum tracking disabled.");
		}
	}

	private void toggleTimeline(boolean enable)
	{
		boolean shouldEnable = enable && isColosseumRegistered;

		if (shouldEnable && !isTimelineRegistered) {
			eventBus.register(timelineLogger);
			isTimelineRegistered = true;
			log.debug("Colosseum Timeline tracking enabled.");
		}
		else if (!shouldEnable && isTimelineRegistered) {
			eventBus.unregister(timelineLogger);
			isTimelineRegistered = false;
			log.debug("Colosseum Timeline tracking disabled.");
		}

		if (enable && !isColosseumRegistered) {
			log.warn("Ignored request to enable Colosseum Timeline: Base Colosseum logging is currently disabled.");
		}
	}

	private void toggleScreenshots(boolean enable)
	{
		if (enable && !isScreenshotRegistered) {
			eventBus.register(screenshotLogger);
			isScreenshotRegistered = true;
			log.debug("Screenshot tracking enabled.");
		} else if (!enable && isScreenshotRegistered) {
			eventBus.unregister(screenshotLogger);
			isScreenshotRegistered = false;
			log.debug("Screenshot tracking disabled.");
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			sessionInitialized = false;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!sessionInitialized && client.getGameState() == GameState.LOGGED_IN)
		{
			long currentHash = client.getAccountHash();

			if (currentHash != -1 && client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null)
			{
				sessionInitialized = true;

				String hashString = String.valueOf(currentHash);
				String accountName = client.getLocalPlayer().getName();
				boolean isMembers = client.getWorldType().contains(WorldType.MEMBERS);

				log.debug("Session ready! Hash: {}, Name: {}. Broadcasting event.", hashString, accountName);
				eventBus.post(new AccountSessionStarted(hashString, currentHash, accountName, isMembers));

				fileIOService.updateAccountHashMapping(hashString, accountName);
			}
		}
	}
}