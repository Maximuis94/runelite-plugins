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

import static com.datalogger.constants.Item.InterfaceID.BANK_CHILD_ID;
import static com.datalogger.constants.Item.InterfaceID.BANK_GROUP_ID;
import static com.datalogger.constants.Item.InterfaceID.SEED_VAULT_CHILD_ID;
import static com.datalogger.constants.Item.InterfaceID.SEED_VAULT_GROUP_ID;
import static com.datalogger.constants.PluginConstants.INTERNAL_VAULT_DIR;
import static com.datalogger.constants.PluginConstants.ITEM_VAULT_DIR;
import java.io.File;
import lombok.Getter;
import net.runelite.api.events.WidgetLoaded;

@Getter
public enum VaultType
{
	BANK(BANK_GROUP_ID, BANK_CHILD_ID),
	SEED_VAULT(SEED_VAULT_GROUP_ID, SEED_VAULT_CHILD_ID),
	GRAND_EXCHANGE(-1, -1),
	VYRE_WELL(-1, -1),
	RUNE_POUCH(-1, -1),
	ITEM_CHARGES(-1, -1),
	MASTER_SCROLL_BOOK(-1, -1),
	QUIVER(-1, -1),
	BOLT_POUCH(-1, -1),
	STASH_UNITS(-1, -1),
	POH_COSTUME_ROOM(-1, -1),
	FARMING_TOOLS(-1, -1),
	TOA_PICKAXE(-1, -1),
	COFFER(-1, -1)


	;


	private final int groupId;
	private final int childId;

	VaultType(int groupId, int childId)
	{
		this.childId = childId;
		this.groupId = groupId;
	}

	/**
	 * The name of the VaultType, lowercased and with hyphens instead of underscores.
	 */
	public String fileNameString()
	{
		return this.name().toLowerCase().replace("_", "-");
	}

	/**
	 * Return true if the given WidgetLoaded instance has a groupId that corresponds to this VaultType
	 */
	public boolean isVaultUI(WidgetLoaded wl)
	{
		return wl.getGroupId() == groupId;
	}

	/**
	 * Return the VaultType that corresponds to the VaultType with the given groupId
	 */
	public static VaultType byGroupId(int groupId)
	{
		switch (groupId)
		{
			case BANK_GROUP_ID:
				return VaultType.BANK;
			case SEED_VAULT_GROUP_ID:
				return VaultType.SEED_VAULT;
			default:
				return null;
		}
	}

	public static File getInternalRoot(long accountHash)
	{
		return new File(INTERNAL_VAULT_DIR, String.valueOf(accountHash));
	}

	public static File getInternalRoot(String accountHash)
	{
		return new File(INTERNAL_VAULT_DIR, accountHash);
	}

	/**
	 * Return the internal json file associated with the given accountHash used to store specific vault data.
	 */
	public File getInternalFile(File parent, long accountHash)
	{
		String hash = String.valueOf(accountHash);
		return new File(parent, fileNameString() + "_" + hash + ".json");
	}

	/**
	 * Return the internal json file associated with the given accountHash.
	 */
	public File getExternalCSVFile(long accountHash)
	{
		return new File(ITEM_VAULT_DIR, fileNameString() + "_" + accountHash + ".csv");
	}

	/**
	 * Return the internal json file associated with the given accountHash.
	 */
	public File getExternalCSVFile(String accountName)
	{
		return new File(ITEM_VAULT_DIR, fileNameString() + "_" + accountName + ".csv");
	}

	/**
	 * Return the internal json file associated with the given accountHash.
	 */
	public File getExternalJSONFile(long accountHash)
	{
		return new File(ITEM_VAULT_DIR, fileNameString() + "_" + accountHash + ".json");
	}

	/**
	 * Return the internal json file associated with the given accountHash.
	 */
	public File getExternalJSONFile(String accountName)
	{
		return new File(ITEM_VAULT_DIR, fileNameString() + "_" + accountName + ".json");
	}

//	/**
//	 * Return the File used to track unlocked vaults for the given accountHash across sessions.
//	 */
//	public File getUnlockedVaultCacheFile(long accountHash)
//	{
//		return new File(INTERNAL_VAULT_DIR, "unlocked-vaults-cache_" + accountHash + ".json");
//	}
}