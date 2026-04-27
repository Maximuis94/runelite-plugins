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

package com.datalogger.models.enums;

import javax.annotation.Nullable;
import lombok.Getter;

/**
 * Various ways to broadcast a completed Colosseum trial
 */
@Getter
public enum ColosseumBroadcastMode
{
	SKIP("Don't broadcast", null, false),
	CONCISE("Concise", ColosseumWebhookFormatter.CONCISE, false),
	CONCISE_SCREENSHOT("Concise+screenshot", ColosseumWebhookFormatter.CONCISE, true),
	CUSTOM_TEMPLATE("Custom", ColosseumWebhookFormatter.CUSTOM, false),
	CUSTOM_TEMPLATE_SCREENSHOT("Custom+screenshot", ColosseumWebhookFormatter.CUSTOM, true),
	DETAILED("Detailed", ColosseumWebhookFormatter.DETAILED, false),
	DETAILED_SCREENSHOT("Detailed+screenshot", ColosseumWebhookFormatter.DETAILED, true),
	SCREENSHOT("Screenshot", null, true),
	DETAILED_WATERMARKED_SCREENSHOT("Detailed screenshot", ColosseumWebhookFormatter.SCREENSHOT, true);

	private final String name;

	@Nullable
	private final ColosseumWebhookFormatter formatter;

	private final boolean attachScreenshot;

	ColosseumBroadcastMode(String name, @Nullable ColosseumWebhookFormatter formatter, boolean attachScreenshot)
	{
		this.name = name;
		this.formatter = formatter;
		this.attachScreenshot = attachScreenshot;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
