// Herb sack parser - for now commented out due to fundamentally different tracking mechanics
///*
// * Copyright (c) 2026, maximuis94 <https://github.com/maximuis94>
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are met:
// *
// * 1. Redistributions of source code must retain the above copyright notice, this
// *    list of conditions and the following disclaimer.
// * 2. Redistributions in binary form must reproduce the above copyright notice,
// *    this list of conditions and the following disclaimer in the documentation
// *    and/or other materials provided with the distribution.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
// * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//
//package com.datalogger.services.itemvault;
//
//import com.datalogger.events.AccountSessionStarted;
//import com.datalogger.loggers.ItemVaultLogger;
//import com.datalogger.models.enums.VaultType;
//import com.datalogger.models.itemvault.BankedItem;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import javax.inject.Inject;
//import javax.inject.Singleton;
//import lombok.extern.slf4j.Slf4j;
//import net.runelite.api.ChatMessageType;
//import net.runelite.api.Client;
//import net.runelite.api.events.ChatMessage;
//import net.runelite.api.events.GameTick;
//import net.runelite.api.gameval.ItemID;
//import net.runelite.client.eventbus.Subscribe;
//import net.runelite.client.util.Text;
//
//@Slf4j
//@Singleton
//public class HerbSackVaultParser implements VaultParser
//{
//	@Inject private Client client;
//	@Inject private ItemVaultLogger itemVaultLogger;
//
//	// Matches lines like: "10 x Grimy guam leaf"
//	private static final Pattern HERB_LINE_PATTERN = Pattern.compile("^([0-9]+) x (Grimy [a-z ]+)$");
//
//	// Fast lookup map for the 14 specific herbs that can go in the sack
//	private static final Map<String, Integer> HERB_NAME_TO_ID = Map.ofEntries(
//		Map.entry("Grimy guam leaf", ItemID.UNIDENTIFIED_GUAM),
//		Map.entry("Grimy marrentill", ItemID.UNIDENTIFIED_MARENTILL),
//		Map.entry("Grimy tarromin", ItemID.UNIDENTIFIED_TARROMIN),
//		Map.entry("Grimy harralander", ItemID.UNIDENTIFIED_HARRALANDER),
//		Map.entry("Grimy ranarr weed", ItemID.UNIDENTIFIED_RANARR),
//		Map.entry("Grimy toadflax", ItemID.UNIDENTIFIED_TOADFLAX),
//		Map.entry("Grimy irit leaf", ItemID.UNIDENTIFIED_IRIT),
//		Map.entry("Grimy avantoe", ItemID.UNIDENTIFIED_AVANTOE),
//		Map.entry("Grimy kwuarm", ItemID.UNIDENTIFIED_KWUARM),
//		Map.entry("Grimy snapdragon", ItemID.UNIDENTIFIED_SNAPDRAGON),
//		Map.entry("Grimy cadantine", ItemID.UNIDENTIFIED_CADANTINE),
//		Map.entry("Grimy lantadyme", ItemID.UNIDENTIFIED_LANTADYME),
//		Map.entry("Grimy dwarf weed", ItemID.UNIDENTIFIED_DWARF_WEED),
//		Map.entry("Grimy torstol", ItemID.UNIDENTIFIED_TORSTOL)
//	);
//
//	private long accountHash = -1;
//	private String accountName = null;
//
//	// State machine variables
//	private boolean isParsingSack = false;
//	private int ticksSinceLastMessage = 0;
//	private final List<BankedItem> temporaryItemList = new ArrayList<>();
//
//	@Override
//	public VaultType getVaultType()
//	{
//		return VaultType.HERB_SACK; // Ensure HERB_SACK is in your VaultType enum
//	}
//
//	@Subscribe
//	public void onAccountSessionStarted(AccountSessionStarted event)
//	{
//		this.accountHash = event.getAccountHash();
//		this.accountName = event.getAccountName();
//	}
//
//	@Subscribe
//	public void onChatMessage(ChatMessage event)
//	{
//		if (accountHash == -1 || accountName == null) return;
//		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM) return;
//
//		String message = Text.removeTags(event.getMessage());
//
//		// 1. Check if the parser should turn ON
//		if (message.equals("The herb sack contains:"))
//		{
//			isParsingSack = true;
//			ticksSinceLastMessage = 0;
//			temporaryItemList.clear();
//			return;
//		}
//
//		// 2. If ON, parse the subsequent herb lines
//		if (isParsingSack)
//		{
//			Matcher matcher = HERB_LINE_PATTERN.matcher(message);
//			if (matcher.matches())
//			{
//				int quantity = Integer.parseInt(matcher.group(1));
//				String herbName = matcher.group(2);
//
//				Integer itemId = HERB_NAME_TO_ID.get(herbName);
//				if (itemId != null)
//				{
//					temporaryItemList.add(new BankedItem(getVaultType(), accountHash, accountName, itemId, herbName, quantity));
//					ticksSinceLastMessage = 0; // Reset the timeout
//				}
//			}
//		}
//	}
//
//	@Subscribe
//	public void onGameTick(GameTick event)
//	{
//		// 3. Timeout logic to finalize the vault parsing
//		if (isParsingSack)
//		{
//			ticksSinceLastMessage++;
//
//			// If 2 game ticks have passed without a new herb message, assume the output is finished
//			if (ticksSinceLastMessage >= 2)
//			{
//				finalizeParsing();
//			}
//		}
//	}
//
//	private void finalizeParsing()
//	{
//		isParsingSack = false;
//		ticksSinceLastMessage = 0;
//
//		if (!temporaryItemList.isEmpty())
//		{
//			log.debug("Finished parsing Herb Sack. Found {} distinct herbs.", temporaryItemList.size());
//
//			// Send to the logger
//			itemVaultLogger.logVault(accountHash, accountName, getVaultType(), new ArrayList<>(temporaryItemList));
//
//			temporaryItemList.clear();
//		}
//	}
//}