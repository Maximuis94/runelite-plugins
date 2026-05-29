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

package com.datalogger.services.itemvault.other;

import com.datalogger.models.enums.ItemCharge;
import com.datalogger.models.enums.VaultType;
import com.datalogger.models.itemvault.BankedItem;
import com.datalogger.services.itemvault.AbstractVaultParser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Singleton;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
public class ToxicBlowpipeParser extends AbstractVaultParser
{
	private static final Pattern BLOWPIPE_PATTERN = Pattern.compile("Darts: (.*?) x ([0-9,]+)\\. Scales: ([0-9,]+)");

	private static final Map<String, Integer> DART_ID_MAP = Map.of(
		"Bronze dart", ItemID.BRONZE_DART,
		"Iron dart", ItemID.IRON_DART,
		"Steel dart", ItemID.STEEL_DART,
		"Mithril dart", ItemID.MITHRIL_DART,
		"Adamant dart", ItemID.ADAMANT_DART,
		"Rune dart", ItemID.RUNE_DART,
		"Amethyst dart", ItemID.AMETHYST_DART,
		"Dragon dart", ItemID.DRAGON_DART
	);

	@Data
	private static class BlowpipeState
	{
		private String dartName = "";
		private int dartId = -1;
		private int dartQuantity = 0;
		private int scalesQuantity = 0;
	}

	private final BlowpipeState currentState = new BlowpipeState();
	private boolean pendingSave = false;

	@Override
	public VaultType getVaultType()
	{
		return VaultType.ITEM_CHARGES;
	}

	@Override
	public String getVaultLabel() {
		return ItemCharge.TOXIC_BLOWPIPE.name();
	}

	@Override
	protected void loadSessionData(File cacheFile)
	{
		List<BankedItem> loadedItems = itemVaultLogger.getVault(currentAccountHash, ItemCharge.TOXIC_BLOWPIPE);

		if (loadedItems != null)
		{
			for (BankedItem item : loadedItems)
			{
				if (item.getItemId() == ItemID.SNAKEBOSS_SCALE)
				{
					// It's the scales, restore the scales quantity
					currentState.setScalesQuantity((int) item.getQuantity());
				}
				else
				{
					// If it's not scales, it must be the darts!
					currentState.setDartId(item.getItemId());
					currentState.setDartName(item.getItemName());
					currentState.setDartQuantity((int) item.getQuantity());
				}
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!isEnabled || event.getType() != ChatMessageType.GAMEMESSAGE) return;

		String message = Text.removeTags(event.getMessage());

		if (message.startsWith("Darts: "))
		{
			Matcher matcher = BLOWPIPE_PATTERN.matcher(message);
			if (matcher.find())
			{
				String dartName = matcher.group(1).trim();
				int dartQty = Integer.parseInt(matcher.group(2).replace(",", ""));
				int scalesQty = Integer.parseInt(matcher.group(3).replace(",", ""));

				int dartId = DART_ID_MAP.getOrDefault(dartName, -1);

				if (dartId != -1)
				{
					currentState.setDartName(dartName);
					currentState.setDartId(dartId);
					currentState.setDartQuantity(dartQty);
					currentState.setScalesQuantity(scalesQty);

					pendingSave = true;
					log.debug("Parsed Blowpipe - Darts: {}x {}, Scales: {}", dartQty, dartName, scalesQty);
				}
				else
				{
					log.warn("Unrecognized dart type parsed from Blowpipe: {}", dartName);
				}
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (pendingSave && hasValidAccountHash)
		{
			// Convert the BlowpipeState into a List<BankedItem>
			List<BankedItem> items = parseVault();

			// Save the List directly to the JSON file
//			fileIOService.writeJson(vaultFile, items);
			submitVault(items);
			pendingSave = false;
		}
	}

	@Override
	public List<BankedItem> parseVault()
	{
		List<BankedItem> items = new ArrayList<>();
		if (!isEnabled) return items;

		if (currentState.getDartQuantity() > 0 && currentState.getDartId() != -1)
		{
			items.add(new BankedItem(
				getVaultType(),
				currentAccountHash,
				currentAccountName,
				currentState.getDartId(),
				currentState.getDartName(),
				currentState.getDartQuantity()
			));
		}

		if (currentState.getScalesQuantity() > 0)
		{
			items.add(new BankedItem(
				getVaultType(),
				currentAccountHash,
				currentAccountName,
				ItemID.SNAKEBOSS_SCALE,
				"Zulrah's scales",
				currentState.getScalesQuantity()
			));
		}

		return items;
	}

	@Override
	protected File getInternalVaultFile()
	{
		return itemVaultLogger.getInternalVaultFile(ItemCharge.TOXIC_BLOWPIPE, String.valueOf(currentAccountHash));
	}

	@Override
	public File getInternalVaultFile(long accountHash)
	{
		return itemVaultLogger.getInternalVaultFile(VaultType.ITEM_CHARGES, String.valueOf(accountHash));
	}

	@Override
	protected void submitVault(List<BankedItem> items)
	{
		if (!hasValidAccountHash) return;

		ensureAccountNameIsCached();
		itemVaultLogger.logVault(currentAccountHash, currentAccountName, ItemCharge.TOXIC_BLOWPIPE, items);
	}
}