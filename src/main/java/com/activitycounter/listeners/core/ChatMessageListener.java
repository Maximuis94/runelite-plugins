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

import static com.activitycounter.PluginConstants.RegionID.CRYSTAL_CHEST_REGION_ID;
import com.activitycounter.listeners.AbstractListenerNode;
import com.activitycounter.listeners.conditional.FarmingListener;
import com.activitycounter.models.Count;
import com.activitycounter.models.Session;
import com.activitycounter.models.TrackedActivity;
import java.util.HashMap;
import java.util.Map;
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
 * Listener that processes messages related to specific counters that tend to be updated after parsing a single,
 * specific message that meets certain criteria.
 * Due to preprocessing, this is the only class with the onChatMessageListener.
 */
@Slf4j
@Singleton
public class ChatMessageListener extends AbstractListenerNode
{
	public static final int AMULET_OF_BOUNTY_ITEM_ID = 21160;
	public static final int AMULET_OF_CHEMISTRY_ITEM_ID = 21161;
	public static final int ALCHEMISTS_AMULET_ITEM_ID = 29988;

	public static final int BRACELET_OF_SLAUGHTER_ITEM_ID = 21183;
	public static final int EXPEDITIOUS_BRACELET_ITEM_ID = 21177;

	@Inject
	private ConfigListener configListener;

	@Inject
	private ExperienceDropListener experienceDropListener;

	@Inject
	private SoundEffectListener soundEffectListener;

	@Inject
	private FarmingListener farmingListener;

	@Inject
	private EquippedItemListener equippedItemListener;

	private ChatMessageType messageType = null;
	private String message = null;
	private int regionId = -1;


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

	// Pre-trimmed regex patterns
	private static final Pattern RUMOUR_PATTERN = Pattern.compile("([\\d,]+) rumours for the Hunter Guild\\.");
	private static final Pattern MAHOGANY_HOMES_PATTERN = Pattern.compile("([\\d,]+) contracts with a total of [\\d,]+ points\\.");
	private static final Pattern LAP_PATTERN = Pattern.compile("Your (.+) count is: ([\\d,]+)\\.");

	private static final String BIRD_HOUSE_MESSAGE = "Your birdhouse trap is now full of seed and will start to catch birds.";

	private static final Pattern PEST_CONTROL_POINTS_PATTERN = Pattern.compile("We've awarded you (\\d+) Void Knight Commendation points\\.");
	private static final String PEST_CONTROL_POINTS_PREFIX = "Squire|Congratulations! You managed to destroy all the portals!";

	private static final String PICKPOCKET_START_PREFIX = "You attempt to pick";
	private static final String PICKPOCKET_FAILED_PREFIX = "You fail to pick";

	private static final String LAP_PREFIX = "Your ";

	private static final String POTIONS_MIXED_PREFIX = "You mix";
	private static final String ALCHEMIST_AMULET_PROC_PREFIX = "Your Alchemist's amulet helps you create";
	private static final String CHEMISTRY_AMULET_PROC_PREFIX = "Your amulet of chemistry helps you create";
	private static final String AMULET_OF_BOUNTY_PROC_PREFIX = "Your amulet of bounty saves some seeds for you.";
	private static final String EXPEDITIOUS_BRACELET_PROC_PREFIX = "Your expeditious bracelet";
	private static final String BRACELET_OF_SLAUGHTER_PROC_PREFIX = "Your bracelet of slaughter";

	private static final String DRUNKEN_DWARF_RANDOM_EVENT = "Drunken Dwarf|I 'new it were you matey! I remember your kebab stories! 'Ere, have some ob the good stuff!";
	private static final String GENIE_RANDOM_EVENT_PREFIX = "Genie|Ah, so you are there,";

	private static final Map<String, TrackedActivity> AGILITY_COURSES = new HashMap<>();

	static {
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

	private void incrementChatboxActivity(Session session, TrackedActivity activity) {
		if (configListener.isKcVisible(activity) && session.isTracking(activity.getId())) {
			Count kc = session.getKillCount(activity.getId());
			kc.setSessionKc(kc.getSessionKc() + 1);
			plugin.setRequiresSaveAndRefresh(true);
		}
	}

	private void processChatboxUpdate(Session session, TrackedActivity activity, int newTotalCount) {
		if (!configListener.isKcVisible(activity) || !session.isTracking(activity.getId())) return;

		Count kc = session.getKillCount(activity.getId());

		if (kc.getInitialKc() == -1) {
			kc.setInitialKc(newTotalCount - 1);
		}

		int newSessionKc = newTotalCount - kc.getInitialKc();

		if (newSessionKc > kc.getSessionKc()) {
			kc.setSessionKc(newSessionKc);
			plugin.setRequiresSaveAndRefresh(true);
			log.debug("Updated session KC for {}: {}", kc.getName(), newSessionKc);
		}
	}

	/**
	 * Extract the points earned from a message that is known to be from Pest Control and update the count accordingly.
	 * Maintains some cooldown between counts.
	 */
	private void pestControlPointsMessage(Session session, String message)
	{
		if (!configListener.isKcVisible(TrackedActivity.PEST_CONTROL_POINTS) || !session.isTracking(TrackedActivity.PEST_CONTROL_POINTS.getId())) return;

		Matcher pcMatcher = PEST_CONTROL_POINTS_PATTERN.matcher(message);
		if (pcMatcher.find())
		{
			int currentTickCount = client.getTickCount();
			int deltaT = currentTickCount - tickCountPestControlPoints;
			if (deltaT < PEST_CONTROL_POINTS_COOLDOWN)
			{
				return;
			}

			int pointsEarned = Integer.parseInt(pcMatcher.group(1));

			Count kc = session.getKillCount(TrackedActivity.PEST_CONTROL_POINTS.getId());
			int newPoints = kc.getSessionKc() + pointsEarned;
			kc.setSessionKc(newPoints);
			plugin.setRequiresSaveAndRefresh(true);
			tickCountPestControlPoints = currentTickCount;

			log.debug("Earned {} Pest Control points, session total is {}", pointsEarned, newPoints);
		}
	}

	/**
	 * Processes a message of type DIALOG
	 */
	private void processDialogueMessage(Session session, String message)
	{
		// Pest Control points
		if (message.startsWith(PEST_CONTROL_POINTS_PREFIX)) {
			pestControlPointsMessage(session, message);
			return;
		}

		if (message.startsWith(DRUNKEN_DWARF_RANDOM_EVENT))
		{
			incrementChatboxActivity(session, TrackedActivity.DRUNKEN_DWARF);
			return;
		}

		if (message.startsWith(GENIE_RANDOM_EVENT_PREFIX))
		{
			incrementChatboxActivity(session, TrackedActivity.GENIE);
			return;
		}
	}

	/**
	 * Processes a message of type MESBOX (i.e. statement with 'Click here to continue')
	 */
	private void processMesboxMessage(Session session, String message)
	{
		if (message.equals(CANNON_LOST_AND_FOUND_MESSAGE)) {
			incrementChatboxActivity(session, TrackedActivity.CANNONS_LOST);
		}
	}

	/**
	 * Checks for herblore-related messages using the most recent exp drop as gatekeeper.
	 */
	private boolean processHerbloreActivity(Session session, String message)
	{
		if (!experienceDropListener.hasSkillGainedExpLastTick(Skill.HERBLORE)) return false;
		int amuletId = equippedItemListener.getNeckId();
		if ((amuletId == ALCHEMISTS_AMULET_ITEM_ID && message.startsWith(ALCHEMIST_AMULET_PROC_PREFIX)) ||
			(amuletId == AMULET_OF_CHEMISTRY_ITEM_ID && message.startsWith(CHEMISTRY_AMULET_PROC_PREFIX)))
		{
			incrementChatboxActivity(session, TrackedActivity.ALCHEMISTS_AMULET_PROCS);
			return true;
		}
		if (message.startsWith(POTIONS_MIXED_PREFIX))
		{
			incrementChatboxActivity(session, TrackedActivity.POTIONS_MIXED);
			return true;
		}
		return false;
	}

	/**
	 * Processes the message if it starts with prefix "You have opened " and returns true. Otherwise it returns false.
	 */
	private boolean identifyChestOpened(Session session, String message)
	{
		if (message.startsWith(CHESTS_PREFIX)) {
			if (message.startsWith(LARRANS_SMALL_CHEST_PREFIX)) {
				incrementChatboxActivity(session, TrackedActivity.LARRANS_SMALL_CHESTS);
			} else if (message.startsWith(LARRANS_BIG_CHEST_PREFIX)) {
				incrementChatboxActivity(session, TrackedActivity.LARRANS_BIG_CHESTS);
			} else if (message.startsWith(BRIMSTONE_CHEST_PREFIX)) {
				incrementChatboxActivity(session, TrackedActivity.BRIMSTONE_CHESTS);
			} else if (message.startsWith(ZOMBIE_PIRATE_LOCKER_PREFIX)) {
				incrementChatboxActivity(session, TrackedActivity.ZOMBIE_PIRATE_CHESTS);
			} else if (message.startsWith(ELVEN_CRYSTAL_CHESTS_PREFIX)) {
				incrementChatboxActivity(session, TrackedActivity.ELVEN_CRYSTAL_CHESTS);
			}
			else log.warn("Encountered a message with CHESTS_PREFIX='{}', but was unable to match it with a chest. Message={}", CHESTS_PREFIX, message);
			return true;
		}
		return false;
	}

	private boolean identifyEqualMessageWithRegionId(Session session, String message, TrackedActivity trackedActivity, String compareTo, int regionId)
	{
		if (regionId == CRYSTAL_CHEST_REGION_ID && message.equals(compareTo)) {
			incrementChatboxActivity(session, trackedActivity);
			return true;
		}
		return false;
	}

	/**
	 * Method for processing all chat messages that are related to equipment charge drains. First, check if the item is
	 * equipped, then check if the message indicates an item charge update. If the message is associated with an
	 * activity, return true, otherwise false.
	 */
	private boolean processEquipmentChatMessages(Session session, String message)
	{
		switch (equippedItemListener.getNeckId())
		{
			case AMULET_OF_BOUNTY_ITEM_ID:
				if (chatMessageStartsWithIncreaseOne(session, message, AMULET_OF_BOUNTY_PROC_PREFIX, TrackedActivity.AMULET_OF_BOUNTY_PROCS)) return true;
				break;

			default:
				break;
		}

		switch (equippedItemListener.getGlovesId())
		{
			case BRACELET_OF_SLAUGHTER_ITEM_ID:
				if (chatMessageStartsWithIncreaseOne(session, message, BRACELET_OF_SLAUGHTER_PROC_PREFIX, TrackedActivity.SLAUGHTER_BRACELET_PROCS)) return true;
				break;
			case EXPEDITIOUS_BRACELET_ITEM_ID:
				if (chatMessageStartsWithIncreaseOne(session, message, EXPEDITIOUS_BRACELET_PROC_PREFIX, TrackedActivity.EXPEDITIOUS_BRACELET_PROCS)) return true;
				break;

			default:
				break;
		}

		return false;
	}

	/**
	 * Returns true if this message has a type that is covered by at least one of the listeners.
	 */
	private static boolean isRelevantMessageType(ChatMessageType messageType) {
		return messageType == ChatMessageType.GAMEMESSAGE || messageType == ChatMessageType.SPAM || messageType == ChatMessageType.DIALOG || messageType == ChatMessageType.MESBOX;
	}

	/**
	 * Return the message without the styling tags like <></> and @mes_hl_red@.
	 */
	public static String preprocessMessage(String message) {
		if (message == null) {
			return null;
		}

		String tagsRemoved = Text.removeTags(message);

		if (tagsRemoved.indexOf('@') == -1) {
			return tagsRemoved;
		}

		StringBuilder sb = new StringBuilder(tagsRemoved.length());
		int currentIndex = 0;

		while (currentIndex < tagsRemoved.length()) {
			int start = tagsRemoved.indexOf('@', currentIndex);
			if (start == -1) {
				sb.append(tagsRemoved, currentIndex, tagsRemoved.length());
				break;
			}

			int end = tagsRemoved.indexOf('@', start + 1);
			if (end == -1) {
				sb.append(tagsRemoved, currentIndex, tagsRemoved.length());
				break;
			}

			boolean isTag = (end > start + 1);
			for (int i = start + 1; i < end; i++) {
				char c = tagsRemoved.charAt(i);
				if (!Character.isLetterOrDigit(c) && c != '_') {
					isTag = false;
					break;
				}
			}

			if (isTag) {
				sb.append(tagsRemoved, currentIndex, start);
				currentIndex = end + 1;
			} else {
				sb.append(tagsRemoved, currentIndex, start + 1);
				currentIndex = start + 1;
			}
		}
		return sb.toString();
	}

	private boolean setMessage(ChatMessage event)
	{
		ChatMessageType type = event.getType();
		if (isRelevantMessageType(type))
		{
			messageType = type;
			message = preprocessMessage(event.getMessage());
			regionId = client.getLocalPlayer().getWorldLocation().getRegionID();
			return true;
		}
		return false;
	}

	/**
	 *  Clears the message variables
	 */
	private void resetMessage()
	{
		messageType = null;
		message = null;
		regionId = -1;
	}

	/**
	 * Preprocesses a chatmessage and forwards it to other subscribers.
	 * NB In order to prevent preprocessing the same message multiple times, this should be the only onChatMessage
	 * subscriber.
	 */
	@Subscribe
	public void onChatMessage(ChatMessage event) {
		if (!setMessage(event)) return;

		Session session = plugin.getCurrentSession();
		if (session == null || !session.isInProgress()) {
			return;
		}

		String message = preprocessMessage(event.getMessage());

		if (messageType == ChatMessageType.DIALOG)
		{
			processDialogueMessage(session, message);
			return;
		}

		if (messageType == ChatMessageType.MESBOX)
		{
			processMesboxMessage(session, message);
			return;
		}

		if (farmingListener.onChatMessage(message, messageType)) return;

		int regionId = client.getLocalPlayer().getWorldLocation().getRegionID();



		// TODO: Add regionId
		if (message.equals(BONES_SPARED_MESSAGE)) {
			incrementChatboxActivity(session, TrackedActivity.SACRIFICES_SPARED);
			return;
		}

		// TODO: Add regionId
		if (message.equals(FARMING_CONTRACT_MESSAGE)) {
			incrementChatboxActivity(session, TrackedActivity.FARMING_CONTRACTS);
			return;
		}
		if (message.equals(BIRD_HOUSE_MESSAGE)) {
			incrementChatboxActivity(session, TrackedActivity.BIRD_HOUSES);
			return;
		}
		if (message.equals(SUPERIOR_SPAWN_MESSAGE)) {
			incrementChatboxActivity(session, TrackedActivity.SUPERIOR_SPAWNS);
			return;
		}
		// TODO: Add regionId
		if (message.equals(BIRD_EGG_OFFERING_MESSAGE)) {
			incrementChatboxActivity(session, TrackedActivity.BIRD_EGGS_OFFERED);
			return;
		}

		if (message.startsWith(MUSIC_TRACK_UNLOCK_PREFIX)) {
			incrementChatboxActivity(session, TrackedActivity.MUSIC_TRACKS_UNLOCKED);
			return;
		}

		if (regionId == MASTERING_MIXOLOGY_REGION_ID)
		{
			if (message.equals(MIXOLOGY_DIGWEED_PICKED_MESSAGE))
			{
				incrementChatboxActivity(session, TrackedActivity.MIXOLOGY_DIGWEED_PICKED);
				return;
			}
			if (message.endsWith(MIXOLOGY_DIGWEED_MATURED_SUFFIX))
			{
				incrementChatboxActivity(session, TrackedActivity.MIXOLOGY_DIGWEED_MATURED);
				return;
			}
		}

		if (processEquipmentChatMessages(session, message)) return;

		if (processHerbloreActivity(session, message)) return;

		if (identifyChestOpened(session, message)) return;


		if (message.startsWith(NEW_COLLECTION_LOG_PREFIX)) {
			incrementChatboxActivity(session, TrackedActivity.NEW_COLLECTIONS_LOGGED);
			return;
		}

		if (message.startsWith(SNEAKING_SUSPICION_PREFIX)) {
			if (message.endsWith(SNEAKING_SUSPICION_BEGINNER_AFFIX)) incrementChatboxActivity(session, TrackedActivity.MISSED_BEGINNER_CLUE);
			else if (message.endsWith(SNEAKING_SUSPICION_EASY_AFFIX)) incrementChatboxActivity(session, TrackedActivity.MISSED_EASY_CLUE);
			else if (message.endsWith(SNEAKING_SUSPICION_MEDIUM_AFFIX)) incrementChatboxActivity(session, TrackedActivity.MISSED_MEDIUM_CLUE);
			else if (message.endsWith(SNEAKING_SUSPICION_HARD_AFFIX)) incrementChatboxActivity(session, TrackedActivity.MISSED_HARD_CLUE);
			else if (message.endsWith(SNEAKING_SUSPICION_ELITE_AFFIX)) incrementChatboxActivity(session, TrackedActivity.MISSED_ELITE_CLUE);
			return;
		}

		if (message.startsWith(CA_PREFIX)) {
			incrementChatboxActivity(session, TrackedActivity.CA_DIARY_TASKS);
			return;
		}

		if (message.startsWith(PICKPOCKET_START_PREFIX))
		{
			soundEffectListener.startPickpocketing();
			return;
		}

		if (message.startsWith(LAP_PREFIX)) {
			Matcher matcher = LAP_PATTERN.matcher(message);
			if (matcher.find()) {
				String courseName = matcher.group(1);
				TrackedActivity lapActivity = AGILITY_COURSES.get(courseName);

				if (lapActivity != null) {
					incrementChatboxActivity(session, lapActivity);
				}
				else {
					log.warn("Failed to recognize a suspected agility lap completion (preprocessed msg='{}') ", message);
				}
			}
			return;
		}

		if (message.startsWith(YOU_HAVE_COMPLETED_PREFIX)) {
			Matcher rumourMatcher = RUMOUR_PATTERN.matcher(message);
			if (rumourMatcher.find()) {
				int newRumourCount = Integer.parseInt(rumourMatcher.group(1).replace(",", ""));
				processChatboxUpdate(session, TrackedActivity.HUNTER_RUMOURS, newRumourCount);
				return;
			}

			Matcher mahoganyMatcher = MAHOGANY_HOMES_PATTERN.matcher(message);
			if (mahoganyMatcher.find()) {
				int newContractCount = Integer.parseInt(mahoganyMatcher.group(1).replace(",", ""));
				processChatboxUpdate(session, TrackedActivity.MAHOGANY_HOMES, newContractCount);
			}
		}
	}

	/**
	 * Check if chatMessage starts with prefix, if so, increase the count for trackedActivity by one and return true. If
	 * not, return false.
	 */
	private boolean chatMessageStartsWithIncreaseOne(Session session, String chatMessage, String prefix, TrackedActivity trackedActivity)
	{
		if (chatMessage.startsWith(prefix)) {
			incrementChatboxActivity(session, trackedActivity);
			return true;
		}
		return false;
	}
}