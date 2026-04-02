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

package com.placeholderprices;

import javax.inject.Inject;
import lombok.Getter;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Bank Placeholder Prices",
	description = "Adds GE and HA price tooltips to unowned bank placeholders",
	tags = {"bank", "placeholder", "price", "value", "tooltip", "ge", "grand exchange", "ha", "alchemy"}
)
public class PlaceholderPricesPlugin extends Plugin
{
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private PlaceholderPricesOverlay overlay;

	@Getter
	private boolean showGEPrice;

	@Getter
	private boolean showHAValue;

	@Override
	protected void startUp()
	{
		showGEPrice = getBooleanConfig("showGEPrice", true);
		showHAValue = getBooleanConfig("showHAValue", false);

		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!"itemprices".equals(event.getGroup()))
		{
			return;
		}

		String key = event.getKey();
		if ("showGEPrice".equals(key))
		{
			showGEPrice = Boolean.parseBoolean(event.getNewValue());
		}
		else if ("showHAValue".equals(key))
		{
			showHAValue = Boolean.parseBoolean(event.getNewValue());
		}
	}

	/**
	 * Helper method to safely pull boolean configs during startup
	 */
	private boolean getBooleanConfig(String key, boolean defaultValue)
	{
		String val = configManager.getConfiguration("itemprices", key);
		if (val == null)
		{
			return defaultValue;
		}
		return Boolean.parseBoolean(val);
	}
}