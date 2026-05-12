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

package com.datalogger.services.itemvault;

import com.datalogger.models.enums.VaultType;
import com.datalogger.models.itemvault.BankedItem;
import java.io.File;
import java.util.List;

/**
 * Overarching VaultParser interface. Each VaultParser has its own VaultType, as well as a parseVault method that
 * returns the current amount of refundable items, derived from the tracked amount of charges related to the subclass.
 */
public interface VaultParser
{
	/**
	 * Identifies which vault this parser handles.
	 */
	VaultType getVaultType();

	/**
	 * Convert the internally tracked amount of charges to one or more BankedItems. That is, one or more quantities of
	 * items that the amount of charges represents.
	 */
	List<BankedItem> parseVault();

	/**
	 * Label used in merged files to distinguish item origins
	 */
	String getVaultLabel();

	/**
	 * Returns the prefix used in the internal output file name.
	 */
	String getFilePrefix();

	/**
	 * Parse the cache file produced by the VaultParser and converts its contents to a List of BankedItem instances.
	 */
	List<BankedItem> parseOfflineFile(long accountHash, File vaultFile);
}