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

import com.datalogger.loggers.ColosseumAttemptLogger;
import com.datalogger.loggers.ColosseumTimelineLogger;
import com.datalogger.loggers.GrandExchangeLogger;
import com.datalogger.loggers.ScreenshotLogger;
import com.datalogger.services.ColosseumScanner;
import com.datalogger.ui.DataLoggerPanel;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Data Logger",
	description = "Logs game data for various activities, like Fortis Colosseum results or the Grand Exchange",
	tags = {
		"logger", "data", "history", "screenshot", "tracker", "csv", "json",

		"ge", "grand exchange",

		"fortis", "colosseum"
	}
)
public class DataLoggerPlugin extends Plugin
{
	@Inject private ClientThread clientThread;

	@Inject private Client client;

	@Inject private ClientToolbar clientToolbar;

	@Inject private EventBus eventBus;

	@Inject private GrandExchangeLogger geLogger;
	@Inject private ColosseumAttemptLogger coloLogger;
	@Inject private ColosseumScanner coloScanner;
	@Inject private ScreenshotLogger screenshotLogger;
	@Inject private ColosseumTimelineLogger timelineLogger;

	@Inject private DataLoggerPanel panel;

	private NavigationButton navButton;

	@Provides
	DataLoggerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DataLoggerConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		eventBus.register(geLogger);
		eventBus.register(coloLogger);
		eventBus.register(coloScanner);
		eventBus.register(screenshotLogger);
		eventBus.register(timelineLogger);
		coloScanner.updateConfigFlags(true);

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
		eventBus.unregister(geLogger);
		eventBus.unregister(coloLogger);
		eventBus.unregister(coloScanner);
		eventBus.unregister(screenshotLogger);
		eventBus.unregister(timelineLogger);
		clientToolbar.removeNavigation(navButton);
	}
}