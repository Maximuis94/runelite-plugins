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

package com.activitycounter.ui;

import com.activitycounter.ActivityCounterConfig;
import com.activitycounter.ActivityCounterPlugin;
import com.activitycounter.listeners.core.ConfigListener;
import com.activitycounter.models.Count;
import com.activitycounter.models.Session;
import com.activitycounter.models.TrackedActivity;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

/**
 * Overlay panel that may be used to display a subset of activities ingame
 */
public class ActivityCounterOverlay extends OverlayPanel
{
	private final ActivityCounterPlugin plugin;
	private final ConfigListener configListener;
	private final ActivityCounterConfig config;

	@Inject
	private ActivityCounterOverlay(ActivityCounterPlugin plugin, ConfigListener configListener, ActivityCounterConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.configListener = configListener;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		panelComponent.setPreferredSize(new Dimension(200, 0));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.setPreferredSize(new Dimension(200, 0));

		Session activeSession = plugin.getCurrentSession();
		if (activeSession == null || !activeSession.isInProgress())
		{
			return null;
		}

		int secondsPassed = activeSession.getSecondsPassed();

		// 1. Render Pinned Time Passed (Top)
		if (config.pinTimeOverlay())
		{
			int hours = secondsPassed / 3600;
			int minutes = (secondsPassed % 3600) / 60;
			int seconds = secondsPassed % 60;
			String timeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);

			panelComponent.getChildren().add(LineComponent.builder()
				.left("Time passed")
				.right(timeStr)
				.build());
		}

		// 2. Render Pinned Activities (Middle)
		List<TrackedActivity> pinnedActivities = plugin.getPinnedActivities();
		if (pinnedActivities != null && !pinnedActivities.isEmpty())
		{
			for (TrackedActivity activity : pinnedActivities)
			{
				Count count = activeSession.getKillCount(activity.getId());
				if (count == null || count.getSessionKc() == 0) continue;

				boolean isHourly = configListener.isHourlyRate(activity.getId());
				String formattedValue = ActivityCounterPlugin.formatCountDisplay(
					count.getSessionKc(),
					secondsPassed,
					isHourly
				);

				panelComponent.getChildren().add(LineComponent.builder()
					.left(count.getName())
					.right(formattedValue)
					.rightColor(ColorScheme.BRAND_ORANGE)
					.build());
			}
		}

		// 3. Render Pinned Score (Bottom)
		if (config.pinScoreOverlay())
		{
			float totalScore = 0f;
			for (Count kc : activeSession.getAllKillCounts()) {
				totalScore += kc.getSessionKc() * kc.getScore();
			}

			String scoreStr;
			if (config.scoreHourlyRate())
			{
				if (secondsPassed > 0)
				{
					double hours = secondsPassed / 3600.0;
					double rate = totalScore / hours;

					if (rate >= 100.0) {
						scoreStr = String.format("%,d/hr", Math.round(rate));
					} else {
						scoreStr = String.format("%,.2f/hr", rate);
					}
				}
				else
				{
					scoreStr = "0/hr";
				}
			}
			else
			{
				if (Math.abs(totalScore - Math.round(totalScore)) < 0.0001) {
					scoreStr = String.format("%,d", Math.round(totalScore));
				} else {
					scoreStr = String.format("%,.2f", totalScore);
				}
			}

			panelComponent.getChildren().add(LineComponent.builder()
				.left(config.customScoreLabel())
				.right(scoreStr)
				.rightColor(ColorScheme.BRAND_ORANGE)
				.build());
		}

		return super.render(graphics);
	}
}