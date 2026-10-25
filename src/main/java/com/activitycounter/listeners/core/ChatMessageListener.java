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

package com.activitycounter.listeners.core;

import com.activitycounter.listeners.AbstractListenerNode;
import com.activitycounter.listeners.PreprocessedChatListener;
import com.activitycounter.models.TrackedActivity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

/**
 * Centralized chatmessage listener/router class.
 * Given that chatmessages tend to be preprocessed to make them easier/cheaper to parse, all chatmessages are routed
 * through and preprocessed in this class.
 * Each chat message is assumed to contribute to up to one counter. As such, whenever a match is made, further
 * processing of that message is terminated. To allow for this format, external classes that require chat messages to be
 * parsed ought to register a parser method here using (un)register. The registered method should follow the signature
 * described by the ChatMessageHandler interface.
 */
@Slf4j
@Singleton
public class ChatMessageListener extends AbstractListenerNode
{
	public static final int AMULET_OF_BOUNTY_ITEM_ID = 21160;
	public static final int AMULET_OF_CHEMISTRY_ITEM_ID = 21161;
	public static final int ALCHEMISTS_AMULET_ITEM_ID = 29990;

	public static final int BRACELET_OF_SLAUGHTER_ITEM_ID = 21183;
	public static final int EXPEDITIOUS_BRACELET_ITEM_ID = 21177;

	@Inject
	private ConfigListener configListener;

	@Inject
	private ExperienceDropListener experienceDropListener;

	@Inject
	private SoundEffectListener soundEffectListener;

	@Inject
	private EquippedItemListener equippedItemListener;

	private int tickCountPestControlPoints = -1;
	private static final int PEST_CONTROL_POINTS_COOLDOWN = 100;

	private static final String CANNON_LOST_AND_FOUND_MESSAGE = "The dwarf gives you a new cannon.";
	private static final String SUPERIOR_SPAWN_MESSAGE = "A superior foe has appeared...";
	private static final String BIRD_EGG_OFFERING_MESSAGE = "You offer your bird's egg to the shrine and receive a reward.";
	private static final String BONES_SPARED_MESSAGE = "The Dark Lord spares your sacrifice but still rewards you for your efforts.";
	private static final String CA_PREFIX = "CA_ID:";
	private static final String MUSIC_TRACK_UNLOCK_PREFIX = "You have unlocked a new music track: ";
	private static final String CHESTS_PREFIX = "You have opened ";
	private static final String LARRANS_SMALL_CHEST_PREFIX = "You have opened Larran's small chest ";
	private static final String LARRANS_BIG_CHEST_PREFIX = "You have opened Larran's big chest ";
	private static final String BRIMSTONE_CHEST_PREFIX = "You have opened the Brimstone chest";
	private static final String ZOMBIE_PIRATE_LOCKER_PREFIX = "You've opened the Zombie Pirate's ";
	private static final String ELVEN_CRYSTAL_CHESTS_PREFIX = "You have opened the crystal chest ";
	private static final String NEW_COLLECTION_LOG_PREFIX = "New item added to your collection log:";

	private static final String CRYSTAL_CHESTS_MESSAGE = "You find some treasure in the chest!";
	private static final int MASTERING_MIXOLOGY_REGION_ID = 5521;
	private static final String MIXOLOGY_DIGWEED_PICKED_MESSAGE = "You collect a handful of digweed.";
	private static final String MIXOLOGY_DIGWEED_MATURED_SUFFIX = "has matured...";

	private static final String SNEAKING_SUSPICION_PREFIX = "You have a sneaking suspicion that";
	private static final String SNEAKING_SUSPICION_BEGINNER_AFFIX = " beginner scroll box.";
	private static final String SNEAKING_SUSPICION_EASY_AFFIX = " easy scroll box.";
	private static final String SNEAKING_SUSPICION_MEDIUM_AFFIX = " medium scroll box.";
	private static final String SNEAKING_SUSPICION_HARD_AFFIX = " hard scroll box.";
	private static final String SNEAKING_SUSPICION_ELITE_AFFIX = " elite scroll box.";
	private static final String FARMING_CONTRACT_MESSAGE = "You've completed a Farming Guild Contract. You should return to GuildMaster Jane.";
	private static final String YOU_HAVE_COMPLETED_PREFIX = "You have completed ";

	private static final Pattern RUMOUR_PATTERN = Pattern.compile("([\\d,]+) rumours for the Hunter Guild\\.");
	private static final Pattern MAHOGANY_HOMES_PATTERN = Pattern.compile("([\\d,]+) contracts with a total of [\\d,]+ points\\.");
	private static final Pattern LAP_PATTERN = Pattern.compile("Your (.+) count is: ([\\d,]+)\\.");

	private static final String BIRD_HOUSE_MESSAGE = "Your birdhouse trap is now full of seed and will start to catch birds.";

	private static final Pattern PEST_CONTROL_POINTS_PATTERN = Pattern.compile("We've awarded you (\\d+) Void Knight Commendation points\\.");
	private static final String PEST_CONTROL_POINTS_PREFIX = "Squire|Congratulations! You managed to destroy all the portals!";

	private static final String PICKPOCKET_START_PREFIX = "You attempt to pick";

	private static final String LAP_PREFIX = "Your ";

	private static final String AMULET_OF_BOUNTY_PROC_PREFIX = "Your amulet of bounty saves some seeds for you.";
	private static final String EXPEDITIOUS_BRACELET_PROC_PREFIX = "Your expeditious bracelet";
	private static final String BRACELET_OF_SLAUGHTER_PROC_PREFIX = "Your bracelet of slaughter";

	private static final String DRUNKEN_DWARF_RANDOM_EVENT = "Drunken Dwarf|I 'new it were you matey! I remember your kebab stories! 'Ere, have some ob the good stuff!";
	private static final String GENIE_RANDOM_EVENT_PREFIX = "Genie|Ah, so you are there,";
	private static final String MIME_RANDOM_EVENT_AFFIX = "Yeah, I'd love to do a mime show.";
	private static final String RICK_RANDOM_EVENT_PREFIX = "Rick Turpentine|Today is your lucky day,";

	private static final Map<String, TrackedActivity> AGILITY_COURSES = new HashMap<>();

	static
	{
		AGILITY_COURSES.put("Gnome Stronghold Agility lap", TrackedActivity.AGILITY_GNOME_STRONGHOLD);
		AGILITY_COURSES.put("Shayzien Basic Agility Course lap", TrackedActivity.AGILITY_SHAYZIEN_LOW);
		AGILITY_COURSES.put("Shayzien Advanced Agility Course lap", TrackedActivity.AGILITY_SHAYZIEN_HIGH);
		AGILITY_COURSES.put("Penguin Agility lap", TrackedActivity.AGILITY_PENGUIN);
		AGILITY_COURSES.put("Barbarian Outpost lap", TrackedActivity.AGILITY_BARBARIAN_OUTPOST);
		AGILITY_COURSES.put("Ape Atoll Agility lap", TrackedActivity.AGILITY_APE_ATOLL);
		AGILITY_COURSES.put("Wilderness Agility lap", TrackedActivity.AGILITY_WILDERNESS);
		AGILITY_COURSES.put("Colossal Wyrm Agility Course (Advanced) lap", TrackedActivity.AGILITY_COLOSSAL_WYRM_ADVANCED);
		AGILITY_COURSES.put("Colossal Wyrm Agility Course (Basic) lap", TrackedActivity.AGILITY_COLOSSAL_WYRM_BASIC);
		AGILITY_COURSES.put("Werewolf Agility lap", TrackedActivity.AGILITY_WEREWOLF);
		AGILITY_COURSES.put("Prifddinas Agility Course lap", TrackedActivity.AGILITY_PRIFDDINAS);
		AGILITY_COURSES.put("Draynor Village Rooftop lap", TrackedActivity.AGILITY_DRAYNOR_ROOFTOP);
		AGILITY_COURSES.put("Al Kharid Rooftop lap", TrackedActivity.AGILITY_AL_KHARID_ROOFTOP);
		AGILITY_COURSES.put("Varrock Rooftop lap", TrackedActivity.AGILITY_VARROCK_ROOFTOP);
		AGILITY_COURSES.put("Canifis Rooftop lap", TrackedActivity.AGILITY_CANIFIS_ROOFTOP);
		AGILITY_COURSES.put("Falador Rooftop lap", TrackedActivity.AGILITY_FALADOR_ROOFTOP);
		AGILITY_COURSES.put("Seers' Village Rooftop lap", TrackedActivity.AGILITY_SEERS_ROOFTOP);
		AGILITY_COURSES.put("Pollnivneach Rooftop lap", TrackedActivity.AGILITY_POLLNIVNEACH_ROOFTOP);
		AGILITY_COURSES.put("Rellekka Rooftop lap", TrackedActivity.AGILITY_RELLEKKA_ROOFTOP);
		AGILITY_COURSES.put("Ardougne Rooftop lap", TrackedActivity.AGILITY_ARDOUGNE_ROOFTOP);
		AGILITY_COURSES.put("Agility Pyramid lap", TrackedActivity.AGILITY_PYRAMID);
		AGILITY_COURSES.put("Dorgesh-Kaan Agility lap", TrackedActivity.AGILITY_DORGESH_KAAN);
		AGILITY_COURSES.put("Agility Arena Total Ticket", TrackedActivity.AGILITY_BRIMHAVEN);
	}

	// --- THE REGISTRY PATTERN ---

	// CopyOnWriteArrayList prevents ConcurrentModificationException
	private final List<PreprocessedChatListener> handlers = new CopyOnWriteArrayList<>();

	public void register(PreprocessedChatListener handler)
	{
		if (!handlers.contains(handler))
		{
			handlers.add(handler);
		}
	}

	public void unregister(PreprocessedChatListener handler)
	{
		handlers.remove(handler);
	}


	/**
	 * Guice executes this automatically after injecting dependencies.
	 * Internal handlers are registered here.
	 */
	@Inject
	public void initializeRegistry()
	{
		register((msg, type, region) -> type == ChatMessageType.DIALOG && processDialogueMessage(msg));
		register((msg, type, region) -> type == ChatMessageType.MESBOX && processMesboxMessage(msg));
		register((msg, type, region) -> processRegionSpecificMessages(msg, region));
		register((msg, type, region) -> processEquipmentChatMessages(msg));
		register((msg, type, region) -> identifyChestOpened(msg));
		register((msg, type, region) -> processGeneralMessages(msg));
	}


	// --- MESSAGE PROCESSORS (All return true if they consume the message) ---

	private boolean pestControlPointsMessage(String message)
	{
		Matcher pcMatcher = PEST_CONTROL_POINTS_PATTERN.matcher(message);
		if (pcMatcher.find())
		{
			int currentTickCount = client.getTickCount();
			int deltaT = currentTickCount - tickCountPestControlPoints;
			if (deltaT < PEST_CONTROL_POINTS_COOLDOWN)
			{
				return true; // Still consumed, just on cooldown
			}

			int pointsEarned = Integer.parseInt(pcMatcher.group(1));
			plugin.increaseCountByAmount(TrackedActivity.PEST_CONTROL_POINTS.getId(), pointsEarned);
			tickCountPestControlPoints = currentTickCount;

			log.debug("Earned {} Pest Control points", pointsEarned);
			return true;
		}
		return false;
	}

	private boolean processDialogueMessage(String message)
	{
		if (message.startsWith(PEST_CONTROL_POINTS_PREFIX))
		{
			return pestControlPointsMessage(message);
		}
		if (message.startsWith(DRUNKEN_DWARF_RANDOM_EVENT))
		{
			plugin.increaseCountByOne(TrackedActivity.DRUNKEN_DWARF.getId());
			return true;
		}
		if (message.startsWith(GENIE_RANDOM_EVENT_PREFIX))
		{
			plugin.increaseCountByOne(TrackedActivity.GENIE.getId());
			return true;
		}

//		Chat message type DIALOG: Dogadon|Yeah, I'd love to do a mime show.
//		Chat message type GAMEMESSAGE: You need to copy the mime's performance, then you'll return to where you were.
//		Chat message type DIALOG: Mysterious Old Man|You need to copy the mime's performance, then you'll be returned to where you were.
		if (message.endsWith(MIME_RANDOM_EVENT_AFFIX))
		{
			plugin.increaseCountByOne(TrackedActivity.MIME.getId());
			return true;
		}

		if (message.startsWith(RICK_RANDOM_EVENT_PREFIX))
		{
			plugin.increaseCountByOne(TrackedActivity.RICK_TURPENTINE.getId());
		}

//		2026-10-17 00:53:45 CEST [Client] DEBUG injected-client - Chat message type GAMEMESSAGE: Your reward is: <col=ff0000>480</col> x <col=ff0000>Coins</col>.
//		2026-10-17 00:53:45 CEST [Client] DEBUG injected-client - Chat message type DIALOG: Rick Turpentine|Today is your lucky day, milady! I am donating to the victims of crime to atone for my past actions!


		// 2026-10-18 20:32:48 CEST [Client] DEBUG injected-client - Chat message type DIALOG: Mysterious Old Man|Ah, so you are there. I hoped you would talk to me, I get so lonely. Here, have a present! I must be going now though.
		return false;
	}

	private boolean processMesboxMessage(String message)
	{
		if (message.equals(CANNON_LOST_AND_FOUND_MESSAGE))
		{
			plugin.increaseCountByOne(TrackedActivity.CANNONS_LOST.getId());
			return true;
		}
		return false;
	}

	private boolean processRegionSpecificMessages(String message, int regionId)
	{
		if (regionId == MASTERING_MIXOLOGY_REGION_ID)
		{
			if (message.equals(MIXOLOGY_DIGWEED_PICKED_MESSAGE))
			{
				plugin.increaseCountByOne(TrackedActivity.MIXOLOGY_DIGWEED_PICKED.getId());
				return true;
			}
			if (message.endsWith(MIXOLOGY_DIGWEED_MATURED_SUFFIX))
			{
				plugin.increaseCountByOne(TrackedActivity.MIXOLOGY_DIGWEED_MATURED.getId());
				return true;
			}
		}
		return false;
	}

	private boolean identifyChestOpened(String message)
	{
		if (message.startsWith(CHESTS_PREFIX))
		{
			if (message.startsWith(LARRANS_SMALL_CHEST_PREFIX))
			{
				plugin.increaseCountByOne(TrackedActivity.LARRANS_SMALL_CHESTS.getId());
			}
			else if (message.startsWith(LARRANS_BIG_CHEST_PREFIX))
			{
				plugin.increaseCountByOne(TrackedActivity.LARRANS_BIG_CHESTS.getId());
			}
			else if (message.startsWith(BRIMSTONE_CHEST_PREFIX))
			{
				plugin.increaseCountByOne(TrackedActivity.BRIMSTONE_CHESTS.getId());
			}
			else if (message.startsWith(ZOMBIE_PIRATE_LOCKER_PREFIX))
			{
				plugin.increaseCountByOne(TrackedActivity.ZOMBIE_PIRATE_CHESTS.getId());
			}
			else if (message.startsWith(ELVEN_CRYSTAL_CHESTS_PREFIX))
			{
				plugin.increaseCountByOne(TrackedActivity.ELVEN_CRYSTAL_CHESTS.getId());
			}
			else
				log.warn("Encountered a message with CHESTS_PREFIX='{}', but was unable to match it with a chest. Message={}", CHESTS_PREFIX, message);
			return true;
		}
		return false;
	}

	private boolean processEquipmentChatMessages(String message)
	{
		switch (equippedItemListener.getNeckId())
		{
			case AMULET_OF_BOUNTY_ITEM_ID:
				if (chatMessageStartsWithIncreaseOne(message, AMULET_OF_BOUNTY_PROC_PREFIX, TrackedActivity.AMULET_OF_BOUNTY_PROCS))
					return true;
				break;
			default:
				break;
		}

		switch (equippedItemListener.getGlovesId())
		{
			case BRACELET_OF_SLAUGHTER_ITEM_ID:
				if (chatMessageStartsWithIncreaseOne(message, BRACELET_OF_SLAUGHTER_PROC_PREFIX, TrackedActivity.SLAUGHTER_BRACELET_PROCS))
					return true;
				break;
			case EXPEDITIOUS_BRACELET_ITEM_ID:
				if (chatMessageStartsWithIncreaseOne(message, EXPEDITIOUS_BRACELET_PROC_PREFIX, TrackedActivity.EXPEDITIOUS_BRACELET_PROCS))
					return true;
				break;
			default:
				break;
		}

		return false;
	}

	private boolean processGeneralMessages(String message)
	{
		if (message.equals(BONES_SPARED_MESSAGE))
		{
			plugin.increaseCountByOne(TrackedActivity.SACRIFICES_SPARED.getId());
			return true;
		}
		if (message.equals(FARMING_CONTRACT_MESSAGE))
		{
			plugin.increaseCountByOne(TrackedActivity.FARMING_CONTRACTS.getId());
			return true;
		}
		if (message.equals(BIRD_HOUSE_MESSAGE))
		{
			plugin.increaseCountByOne(TrackedActivity.BIRD_HOUSES.getId());
			return true;
		}
		if (message.equals(SUPERIOR_SPAWN_MESSAGE))
		{
			plugin.increaseCountByOne(TrackedActivity.SUPERIOR_SPAWNS.getId());
			return true;
		}
		if (message.equals(BIRD_EGG_OFFERING_MESSAGE))
		{
			plugin.increaseCountByOne(TrackedActivity.BIRD_EGGS_OFFERED.getId());
			return true;
		}
		if (message.startsWith(MUSIC_TRACK_UNLOCK_PREFIX))
		{
			plugin.increaseCountByOne(TrackedActivity.MUSIC_TRACKS_UNLOCKED.getId());
			return true;
		}
		if (message.startsWith(NEW_COLLECTION_LOG_PREFIX))
		{
			plugin.increaseCountByOne(TrackedActivity.NEW_COLLECTIONS_LOGGED.getId());
			return true;
		}
		if (message.startsWith(SNEAKING_SUSPICION_PREFIX))
		{
			if (message.endsWith(SNEAKING_SUSPICION_BEGINNER_AFFIX))
				plugin.increaseCountByOne(TrackedActivity.MISSED_BEGINNER_CLUE.getId());
			else if (message.endsWith(SNEAKING_SUSPICION_EASY_AFFIX))
				plugin.increaseCountByOne(TrackedActivity.MISSED_EASY_CLUE.getId());
			else if (message.endsWith(SNEAKING_SUSPICION_MEDIUM_AFFIX))
				plugin.increaseCountByOne(TrackedActivity.MISSED_MEDIUM_CLUE.getId());
			else if (message.endsWith(SNEAKING_SUSPICION_HARD_AFFIX))
				plugin.increaseCountByOne(TrackedActivity.MISSED_HARD_CLUE.getId());
			else if (message.endsWith(SNEAKING_SUSPICION_ELITE_AFFIX))
				plugin.increaseCountByOne(TrackedActivity.MISSED_ELITE_CLUE.getId());
			return true;
		}
		if (message.startsWith(CA_PREFIX))
		{
			plugin.increaseCountByOne(TrackedActivity.CA_DIARY_TASKS.getId());
			return true;
		}
		if (message.startsWith(PICKPOCKET_START_PREFIX))
		{
			soundEffectListener.startPickpocketing();
			return true;
		}
		if (message.startsWith(LAP_PREFIX))
		{
			Matcher matcher = LAP_PATTERN.matcher(message);
			if (matcher.find())
			{
				String courseName = matcher.group(1);
				TrackedActivity lapActivity = AGILITY_COURSES.get(courseName);

				if (lapActivity != null)
				{
					plugin.increaseCountByOne(lapActivity.getId());
				}
				else
				{
					log.warn("Failed to recognize a suspected agility lap completion (preprocessed msg='{}') ", message);
				}
				return true;
			}
		}
		if (message.startsWith(YOU_HAVE_COMPLETED_PREFIX))
		{
			Matcher rumourMatcher = RUMOUR_PATTERN.matcher(message);
			if (rumourMatcher.find())
			{
				int newRumourCount = Integer.parseInt(rumourMatcher.group(1).replace(",", ""));
				plugin.processActivityUpdate(TrackedActivity.HUNTER_RUMOURS.getId(), newRumourCount);
				return true;
			}

			Matcher mahoganyMatcher = MAHOGANY_HOMES_PATTERN.matcher(message);
			if (mahoganyMatcher.find())
			{
				int newContractCount = Integer.parseInt(mahoganyMatcher.group(1).replace(",", ""));
				plugin.processActivityUpdate(TrackedActivity.MAHOGANY_HOMES.getId(), newContractCount);
				return true;
			}
		}
		return false;
	}

	private boolean chatMessageStartsWithIncreaseOne(String chatMessage, String prefix, TrackedActivity trackedActivity)
	{
		if (chatMessage.startsWith(prefix))
		{
			plugin.increaseCountByOne(trackedActivity.getId());
			return true;
		}
		return false;
	}

	// --- UTILITIES & EVENT SUBSCRIPTION ---

	/**
	 * Used as initial gatekeeper
	 */
	private static boolean isRelevantMessageType(ChatMessageType messageType)
	{
		return messageType == ChatMessageType.GAMEMESSAGE || messageType == ChatMessageType.SPAM || messageType == ChatMessageType.DIALOG || messageType == ChatMessageType.MESBOX;
	}

	/**
	 * Returns the given message, minus any styling tags and such. Assume that every message that is further processed
	 * throughout the codebase has passed through this method exactly once.
	 */
	private static String preprocessMessage(String message)
	{
		if (message == null)
		{
			return null;
		}

		String tagsRemoved = Text.removeTags(message);

		if (tagsRemoved.indexOf('@') == -1)
		{
			return tagsRemoved;
		}

		StringBuilder sb = new StringBuilder(tagsRemoved.length());
		int currentIndex = 0;

		while (currentIndex < tagsRemoved.length())
		{
			int start = tagsRemoved.indexOf('@', currentIndex);
			if (start == -1)
			{
				sb.append(tagsRemoved, currentIndex, tagsRemoved.length());
				break;
			}

			int end = tagsRemoved.indexOf('@', start + 1);
			if (end == -1)
			{
				sb.append(tagsRemoved, currentIndex, tagsRemoved.length());
				break;
			}

			boolean isTag = (end > start + 1);
			for (int i = start + 1; i < end; i++)
			{
				char c = tagsRemoved.charAt(i);
				if (!Character.isLetterOrDigit(c) && c != '_')
				{
					isTag = false;
					break;
				}
			}

			if (isTag)
			{
				sb.append(tagsRemoved, currentIndex, start);
				currentIndex = end + 1;
			}
			else
			{
				sb.append(tagsRemoved, currentIndex, start + 1);
				currentIndex = start + 1;
			}
		}
		return sb.toString();
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		ChatMessageType messageType = event.getType();
		if (!isRelevantMessageType(messageType) || !plugin.hasActiveSession())
		{
			return;
		}

		String message = preprocessMessage(event.getMessage());
		int regionId = client.getLocalPlayer() != null ? client.getLocalPlayer().getWorldLocation().getRegionID() : -1;

		for (PreprocessedChatListener handler : handlers)
		{
			if (handler.onPreprocessedChatMessage(message, messageType, regionId))
			{
				return;
			}
		}
	}
}