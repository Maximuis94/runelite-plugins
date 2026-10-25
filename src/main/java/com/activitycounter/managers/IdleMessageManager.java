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

package com.activitycounter.managers;

import com.activitycounter.ActivityCounterConfig;
import com.activitycounter.listeners.core.ConfigListener;
import com.activitycounter.listeners.core.SoundEffectListener;
import com.activitycounter.models.TrackedActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class IdleMessageManager
{
	private final ActivityCounterConfig config;
	private final ConfigListener configListener;
	private final SoundEffectListener soundEffectListener;

	private final Random random = new Random();
	private String currentTip = "";

	// Add as many tips here as you want.
	private static final List<String> TIPS = List.of(
		"Right-click any counter to swap it to a /hr display mode.",
		"You can bind specific configurations directly to a session via the plugin settings.",
		"Pin your most important activities to the in-game overlay by right-clicking them.",
		"Custom scoring allows you to assign weighted values to activities to track overall profit or points.",
		"Check the 'Help' tab for mini-guides on how to utilize advanced tracking features."
	);

	@Inject
	public IdleMessageManager(
		ActivityCounterConfig config,
		ConfigListener configListener,
		SoundEffectListener soundEffectListener)
	{
		this.config = config;
		this.configListener = configListener;
		this.soundEffectListener = soundEffectListener;
		rollNewTip();
	}

	/**
	 * Selects a new tip. Call this when a new session starts so the text
	 * remains stable during the session's idle phase.
	 */
	public void rollNewTip()
	{
		currentTip = TIPS.get(random.nextInt(TIPS.size()));
	}

	/**
	 * Evaluates all plugin conditions and generates a unified HTML idle message.
	 */
	public String getIdleMessage()
	{
		List<String> warnings = getActiveWarnings();

		StringBuilder sb = new StringBuilder();
		sb.append("<html><div style='margin-top: 5px; margin-bottom: 5px;'>");

		// 1. Render Warnings (if any exist)
		if (!warnings.isEmpty())
		{
			sb.append("<b style='color: #ff6666;'>Counter is partially active!</b><br>");
			for (String warning : warnings)
			{
				sb.append("• ").append(warning).append("<br>");
			}
			sb.append("<br>");
		}
		else
		{
			sb.append("<b style='color: #40ff40;'>Counter is fully active and waiting!</b><br><br>");
		}

		// 2. Render Tip of the Session
		sb.append("<b style='color: #ff9933;'>Did you know?</b><br>");
		sb.append(currentTip);

		sb.append("</div></html>");

		return sb.toString();
	}

	/**
	 * Scalable helper method to process all system states into readable warnings.
	 */
	private List<String> getActiveWarnings()
	{
		List<String> warnings = new ArrayList<>();

		if (!soundEffectListener.isHasSoundEffects())
		{
			warnings.add("Game sounds are muted. Audio-based counters (e.g., teletabs) may not track.");
		}

		if (!config.enableBoltProcListener())
		{
			warnings.add("Bolt proc counters are disabled in 'Group filters'.");
		}

		int hiddenCount = 0;
		for (TrackedActivity act : TrackedActivity.values())
		{
			if (!configListener.isKcVisible(act)) hiddenCount++;
		}
		if (hiddenCount > 0)
		{
			warnings.add(hiddenCount + " individual activities are currently hidden via settings.");
		}

		// Simply add more 'if' blocks here as the plugin expands

		return warnings;
	}
}