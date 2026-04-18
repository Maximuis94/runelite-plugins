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

/**
 * Configuration that allows one to select specific occasions on which a screenshot is to be attached to a Discord
 * message
 */
public enum BroadcastColosseumScreenshotOption
{
	NEVER(false, false, false),
	SUCCESS(true, false, false),
	CLAIM(false, true, false),
	FAILURE(false, false, true),
	SUCCESS_OR_CLAIM(true, true, false),
	ALWAYS(true, true, true);

	private final boolean onSuccess;
	private final boolean onClaim;
	private final boolean onFailure;
	private final boolean onRewardsUI;

	BroadcastColosseumScreenshotOption(boolean onSuccess, boolean onClaim, boolean onFailure)
	{
		this.onSuccess = onSuccess;
		this.onClaim = onClaim;
		this.onFailure = onFailure;
		this.onRewardsUI = onClaim || onSuccess;
	}

	public boolean isScreenshotOnSuccess() { return onSuccess; }
	public boolean isScreenshotOnClaim() { return onClaim; }
	public boolean isScreenshotOnFailure() { return onFailure; }
	public boolean isScreenshotOnRewardsUI() { return onRewardsUI; }
}
