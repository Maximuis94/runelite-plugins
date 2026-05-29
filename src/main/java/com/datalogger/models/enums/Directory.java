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

import static com.datalogger.constants.PluginConstants.COLOSSEUM_TRIALS_DIR;
import static com.datalogger.constants.PluginConstants.GRAND_EXCHANGE_DIR;
import static com.datalogger.constants.PluginConstants.INTERNAL_COLOSSEUM_DIR;
import static com.datalogger.constants.PluginConstants.INTERNAL_VAULT_DIR;
import static com.datalogger.constants.PluginConstants.ITEM_VAULT_DIR;
import static com.datalogger.services.FileIOService.INTERNAL_GE_HISTORY_DIR;
import static com.datalogger.services.FileIOService.INTERNAL_GE_OFFERS_DIR;
import java.io.File;
import lombok.Getter;

@Getter
public enum Directory
{
	COLOSSEUM_TRAILS("Colosseum trial log", COLOSSEUM_TRIALS_DIR, false),
	ITEMS("Item log", ITEM_VAULT_DIR, true),
	GRAND_EXCHANGE("Grand Exchange log", GRAND_EXCHANGE_DIR, true),
	COLOSSEUM_TRAILS_INTERNAL("Internal Colosseum trial log", INTERNAL_COLOSSEUM_DIR, false),
	ITEMS_INTERNAL("Internal Item log", INTERNAL_VAULT_DIR, true),
	GRAND_EXCHANGE_INTERNAL("Internal Grand Exchange log", INTERNAL_GE_OFFERS_DIR, false),
	GRAND_EXCHANGE_HISTORY_INTERNAL("Internal Grand Exchange history log", INTERNAL_GE_HISTORY_DIR, false);

	final String label;
	final File directory;
	final boolean accountSpecificDirectories;

	Directory(String label, File directory, boolean accountSpecific)
	{
		this.label = label;
		this.directory = directory;
		this.accountSpecificDirectories = accountSpecific;
	}

	@Override
	public String toString()
	{
		return label;
	}
}