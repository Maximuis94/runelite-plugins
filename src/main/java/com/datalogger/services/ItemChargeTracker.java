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

/*
 * Copyright (c) 2026, maximuis94 <https://github.com/maximuis94>
 * All rights reserved.
 */

package com.datalogger.services;

import static com.datalogger.constants.PluginConstants.INTERNAL_ITEM_CHARGE_DIR;
import com.datalogger.events.AccountSessionStarted;
import com.datalogger.models.enums.ItemCharge;
import com.datalogger.models.enums.VaultType;
import com.datalogger.models.itemvault.BankedItem;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.util.Text;

/**
 * Tracker for refundable item charges. Tracked item charges are updated sporadically via parsed game messages and are
 * an approximation rather than a very accurate count.
 * Item charge counts are stored per account and are also stored as a separate ItemVault.
 */
@Slf4j
@Singleton
public class ItemChargeTracker
{
	@Inject private FileIOService fileIOService;
	@Inject private ItemManager itemManager;

	private final Map<ItemCharge, Integer> itemCharges = new ConcurrentHashMap<>();

	private long accountHash = -1;
	private String accountName = null;
	private File itemChargesFile = null;
	private boolean isSavingFile = false;
	private Instant nextSaveTime = Instant.now();

	private final int SAVE_FILE_COOLDOWN_SECONDS = 60;

	private final Map<Integer, String> itemIdNameCache = new HashMap<>();

	private static final Pattern CHARGE_CHECK_PATTERN = Pattern.compile("(?i)(?:Your )?(.+?) has ([0-9,]+) charges?(?: remaining)?\\.?");
	private static final Pattern CHARGE_ADD_PATTERN = Pattern.compile("(?i).*It now has ([0-9,]+) charges\\.?");

	private static final Map<String, ItemCharge> CHAT_NAME_MAP = Map.ofEntries(
		Map.entry("scythe of vitur", ItemCharge.SCYTHE_OF_VITUR),
		Map.entry("tumeken's shadow", ItemCharge.TUMEKENS_SHADOW),
		Map.entry("sanguinesti staff", ItemCharge.SANGUINESTI_STAFF),
		Map.entry("amulet of blood fury", ItemCharge.AMULET_OF_BLOOD_FURY),
		Map.entry("toxic trident", ItemCharge.TOXIC_TRIDENT),
		Map.entry("trident of the swamp", ItemCharge.TOXIC_TRIDENT), // Added Swamp mapping
		Map.entry("trident of the seas", ItemCharge.TRIDENT_OF_THE_SEAS),
		Map.entry("venator bow", ItemCharge.VENATOR_BOW)
	);

	@Subscribe
	public void onAccountSessionStarted(AccountSessionStarted event)
	{
		this.accountHash = event.getAccountHash();
		this.accountName = event.getAccountName();
		this.itemChargesFile = new File(INTERNAL_ITEM_CHARGE_DIR, accountHash + ".json");

		loadFromFile();
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (accountHash == -1) return;
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM) return;

		String message = Text.removeTags(event.getMessage());

		Matcher checkMatcher = CHARGE_CHECK_PATTERN.matcher(message);
		if (checkMatcher.find())
		{
			String itemName = checkMatcher.group(1).toLowerCase();
			int charges = Integer.parseInt(checkMatcher.group(2).replace(",", ""));

			ItemCharge chargeEnum = CHAT_NAME_MAP.get(itemName);
			if (chargeEnum != null)
			{
				updateCharges(chargeEnum, charges);
			}
			return;
		}

		Matcher addMatcher = CHARGE_ADD_PATTERN.matcher(message);
		if (addMatcher.find())
		{
			int charges = Integer.parseInt(addMatcher.group(1).replace(",", ""));

			for (Map.Entry<String, ItemCharge> entry : CHAT_NAME_MAP.entrySet())
			{
				if (message.toLowerCase().contains(entry.getKey()))
				{
					updateCharges(entry.getValue(), charges);
					break;
				}
			}
		}
	}

	public void updateCharges(ItemCharge charge, int quantity)
	{
		if (accountHash == -1) return;

		itemCharges.put(charge, quantity);
		log.debug("Updated charges for {}: {} (Account: {})", charge.name(), quantity, accountName);

		saveToFile();
	}

	public int getCharges(ItemCharge charge)
	{
		return itemCharges.getOrDefault(charge, -1);
	}

	/**
	 * Clear the itemCharges map, then fill it with parsed itemCharges File contents.
	 */
	private void loadFromFile()
	{
		if (itemChargesFile == null || !itemChargesFile.exists()) return;

		itemCharges.clear();
		Type type = new TypeToken<Map<ItemCharge, Integer>>() {}.getType();

		Map<ItemCharge, Integer> loadedData = fileIOService.readJson(itemChargesFile, type);

		if (loadedData != null)
		{
			itemCharges.putAll(loadedData);
			log.debug("Successfully loaded local item charges for account {}", accountHash);
		}
	}

	/**
	 * Convert the ItemCharge mapping to an aggregated List of BankedItem instances and return it.
	 */
	private List<BankedItem> exportToItemVault()
	{
		List<BankedItem> results = new ArrayList<>();
		Map<Integer, Long> ingredientCounts = new HashMap<>();

		final VaultType vault = VaultType.ITEM_CHARGES;

		for (Map.Entry<ItemCharge, Integer> entry : itemCharges.entrySet())
		{
			ItemCharge itemCharge = entry.getKey();
			int chargeCount = entry.getValue();

			if (chargeCount <= 0) continue;

			results.add(new BankedItem(
				vault,
				accountHash,
				accountName,
				itemCharge.getBaseId(),
				itemCharge.getFormattedName() + " (Charges)",
				chargeCount
			));

			for (Map.Entry<Integer, Integer> input : itemCharge.getInputItem().entrySet())
			{
				int ingredientId = input.getKey();
				int qtyPerBatch = input.getValue();
				int batchSize = itemCharge.getNCharges();

				long totalIngredientQty = ((long) chargeCount * qtyPerBatch / batchSize);

				if (totalIngredientQty > 0)
				{
					ingredientCounts.merge(ingredientId, totalIngredientQty, Long::sum);
				}
			}
		}

		for (Map.Entry<Integer, Long> entry : ingredientCounts.entrySet())
		{
			int itemId = entry.getKey();
			long quantity = entry.getValue();

			String itemName = itemIdNameCache.computeIfAbsent(itemId, id -> itemManager.getItemComposition(id).getName());

			results.add(new BankedItem(
				vault,
				accountHash,
				accountName,
				itemId,
				itemName,
				quantity
			));
		}

		return results;
	}

	/**
	 * Export the current ItemCharge counts to an item vault export and to a item charge count cache.
	 * TODO: export two files; one for item charge data, and one for item vault data
	 */
	private void saveToFile()
	{
		Instant now = Instant.now();
		if (isSavingFile || accountHash == -1 || now.isBefore(nextSaveTime)) return;

		try
		{
			isSavingFile = true;

			File itemVaultFile = fileIOService.getInternalVaultFile(VaultType.ITEM_CHARGES);
			List<BankedItem> asItemVault = exportToItemVault();
			fileIOService.writeJson(itemVaultFile, asItemVault);

			fileIOService.writeJson(itemChargesFile, itemCharges);

			nextSaveTime = now.plusSeconds(SAVE_FILE_COOLDOWN_SECONDS);
		}
		finally
		{
			isSavingFile = false;
		}
	}
}