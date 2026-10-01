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

package com.activitycounter.listeners;

import com.activitycounter.ActivityCounterPlugin;
import static com.activitycounter.PluginConstants.ActivityID.HUNTER_RUMOURS;
import static com.activitycounter.PluginConstants.ActivityID.MAHOGANY_HOMES;
import static com.activitycounter.PluginConstants.SoundID.STUNNED_SOUND_EFFECT_ID;
import com.activitycounter.models.Count;
import com.activitycounter.models.Session;
import com.activitycounter.models.TrackedActivity;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

/**
 * Listener that processes messages related to specific counters that tend to be updated after parsing a single,
 * specific message that meets certain criteria.
 */
@Slf4j
@Singleton
public class ChatMessageListener {

	@Inject
	private ActivityCounterPlugin plugin;

	@Inject
	private Client client;

	private int tickCountPestControlPoints = -1;
	private static final int PEST_CONTROL_POINTS_COOLDOWN = 100;

	private static final String CANNON_LOST_AND_FOUND_MESSAGE = "The dwarf gives you a new cannon.";
	private static final String SUPERIOR_SPAWN_MESSAGE = "A superior foe has appeared...";
	private static final String BIRD_EGG_OFFERING_MESSAGE = "You offer your bird's egg to the shrine and receive a reward.";
	private static final String BONES_SPARED_MESSAGE = "The Dark Lord spares your sacrifice but still rewards you for your efforts.";
	private static final String CA_PREFIX = "CA_ID:";
//	private static final String QUEST_COMPLETED_PREFIX = "Congratulations, you've completed a quest: ";
	private static final String MUSIC_TRACK_UNLOCK_PREFIX = "You have unlocked a new music track: ";
	private static final String CHESTS_PREFIX = "You have opened ";
	private static final String LARRANS_SMALL_CHEST_PREFIX = "You have opened Larran's small chest ";
	private static final String LARRANS_BIG_CHEST_PREFIX = "You have opened Larran's big chest ";
	private static final String BRIMSTONE_CHEST_PREFIX = "You have opened the Brimstone chest";
	private static final String ZOMBIE_PIRATE_LOCKER_PREFIX = "You've opened the Zombie Pirate's ";
	private static final String SNEAKING_SUSPICION_PREFIX = "You have a sneaking suspicion that";
	private static final String SNEAKING_SUSPICION_BEGINNER_AFFIX = " beginner scroll box.";
	private static final String SNEAKING_SUSPICION_EASY_AFFIX = " easy scroll box.";
	private static final String SNEAKING_SUSPICION_MEDIUM_AFFIX = " medium scroll box.";
	private static final String SNEAKING_SUSPICION_HARD_AFFIX = " hard scroll box.";
	private static final String SNEAKING_SUSPICION_ELITE_AFFIX = " elite scroll box.";
	private static final String FARMING_CONTRACT_MESSAGE = "You've completed a Farming Guild Contract. You should return to GuildMaster Jane.";
	private static final Pattern RUMOUR_PATTERN = Pattern.compile("You have completed ([\\d,]+) rumours for the Hunter Guild\\.");
	private static final String YOU_HAVE_COMPLETED_PREFIX = "You have completed ";
	private static final Pattern MAHOGANY_HOMES_PATTERN = Pattern.compile("You have completed ([\\d,]+) contracts with a total of [\\d,]+ points\\.");
	private static final Pattern LAP_PATTERN = Pattern.compile("Your (.+) count is: ([\\d,]+)\\.");

	private static final String BIRD_HOUSE_MESSAGE = "Your birdhouse trap is now full of seed and will start to catch birds.";

	private static final Pattern PEST_CONTROL_POINTS_PATTERN = Pattern.compile("We've awarded you (\\d+) Void Knight Commendation points\\.");
	private static final String PEST_CONTROL_POINTS_PREFIX = "Squire|Congratulations! You managed to destroy all the portals!";

	private static final String PICKPOCKET_FAILED_PREFIX = "You fail to pick";
	private int pickpocketFailMsgTick = -1;

	private static final String LAP_PREFIX = "Your ";

	private void incrementChatboxActivity(TrackedActivity activity) {
		Session session = plugin.getCurrentSession();
		if (session != null && session.isTracking(activity.getId())) {
			Count kc = session.getKillCount(activity.getId());
			kc.setSessionKc(kc.getSessionKc() + 1);
			plugin.setRequiresSaveAndRefresh(true);
		}
	}

	private void processChatboxUpdate(int activityId, int newTotalCount) {
		Session session = plugin.getCurrentSession();
		if (session == null) return;

		Count kc = session.getKillCount(activityId);

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
	private void pestControlPointsMessage(String message)
	{
		Matcher pcMatcher = PEST_CONTROL_POINTS_PATTERN.matcher(message);
		Session activeSession = plugin.getCurrentSession();
		if (activeSession != null && activeSession.isTracking(TrackedActivity.PEST_CONTROL_POINTS.getId())) {
			if (pcMatcher.find())
			{
				int currentTickCount = client.getTickCount();
				int deltaT = currentTickCount - tickCountPestControlPoints;
				if (deltaT < PEST_CONTROL_POINTS_COOLDOWN)
				{
					return;
				}

				int pointsEarned = Integer.parseInt(pcMatcher.group(1));

				Count kc = activeSession.getKillCount(TrackedActivity.PEST_CONTROL_POINTS.getId());
				int newPoints = kc.getSessionKc() + pointsEarned;
				kc.setSessionKc(newPoints);
				plugin.setRequiresSaveAndRefresh(true);
				tickCountPestControlPoints = currentTickCount;

				log.debug("Earned {} Pest Control points, session total is {}", pointsEarned, newPoints);
			}
		}
	}

	/**
	 * Processes a message of type DIALOG
	 */
	private void processDialogueMessage(String message)
	{
		// Pest Control points
		if (plugin.isKcVisible(TrackedActivity.PEST_CONTROL_POINTS) && message.startsWith(PEST_CONTROL_POINTS_PREFIX)) {
			pestControlPointsMessage(message);
//			return;
		}

	}

	/**
	 * Processes a message of type MESBOX (i.e. statement with 'Click here to continue')
	 */
	private void processMesboxMessage(String message)
	{
		if (plugin.isKcVisible(TrackedActivity.CANNONS_LOST) && message.equals(CANNON_LOST_AND_FOUND_MESSAGE)) {
			incrementChatboxActivity(TrackedActivity.CANNONS_LOST);
//			return;
		}

	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {
		ChatMessageType messageType = event.getType();
		if (!isRelevantMessageType(messageType)) return;

		Session session = plugin.getCurrentSession();
		if (session == null || !session.isInProgress()) {
			return;
		}

		String message = preprocessMessage(event.getMessage());

		if (messageType == ChatMessageType.DIALOG)
		{
			processDialogueMessage(message);
			return;
		}

		if (messageType == ChatMessageType.MESBOX)
		{
			processMesboxMessage(message);
			return;
		}

		if (plugin.isKcVisible(TrackedActivity.SACRIFICES_SPARED) && message.equals(BONES_SPARED_MESSAGE)) {
			incrementChatboxActivity(TrackedActivity.SACRIFICES_SPARED);
			return;
		}

		if (plugin.isKcVisible(TrackedActivity.FARMING_CONTRACTS) && message.equals(FARMING_CONTRACT_MESSAGE)) {
			incrementChatboxActivity(TrackedActivity.FARMING_CONTRACTS);
			return;
		}
		if (plugin.isKcVisible(TrackedActivity.BIRD_HOUSES) && message.equals(BIRD_HOUSE_MESSAGE)) {
			incrementChatboxActivity(TrackedActivity.BIRD_HOUSES);
			return;
		}
		if (plugin.isKcVisible(TrackedActivity.SUPERIOR_SPAWNS) && message.equals(SUPERIOR_SPAWN_MESSAGE)) {
			incrementChatboxActivity(TrackedActivity.SUPERIOR_SPAWNS);
			return;
		}
		if (plugin.isKcVisible(TrackedActivity.BIRD_EGGS_OFFERED) && message.equals(BIRD_EGG_OFFERING_MESSAGE)) {
			incrementChatboxActivity(TrackedActivity.BIRD_EGGS_OFFERED);
			return;
		}
		if (plugin.isKcVisible(TrackedActivity.MUSIC_TRACKS_UNLOCKED) && message.startsWith(MUSIC_TRACK_UNLOCK_PREFIX)) {
			incrementChatboxActivity(TrackedActivity.MUSIC_TRACKS_UNLOCKED);
			return;
		}
//		if (plugin.isKcVisible(TrackedActivity.QUESTS) && message.startsWith(QUEST_COMPLETED_PREFIX)) {
//			incrementChatboxActivity(TrackedActivity.QUESTS);
//			return;
//		}



		if (message.startsWith(CHESTS_PREFIX)) {
			if (message.startsWith(LARRANS_SMALL_CHEST_PREFIX) && plugin.isKcVisible(TrackedActivity.LARRANS_SMALL_CHESTS)) {
				incrementChatboxActivity(TrackedActivity.LARRANS_SMALL_CHESTS);
			} else if (message.startsWith(LARRANS_BIG_CHEST_PREFIX) && plugin.isKcVisible(TrackedActivity.LARRANS_BIG_CHESTS)) {
				incrementChatboxActivity(TrackedActivity.LARRANS_BIG_CHESTS);
			} else if (message.startsWith(BRIMSTONE_CHEST_PREFIX) && plugin.isKcVisible(TrackedActivity.BRIMSTONE_CHESTS)) {
				incrementChatboxActivity(TrackedActivity.BRIMSTONE_CHESTS);
			} else if (message.startsWith(ZOMBIE_PIRATE_LOCKER_PREFIX) && plugin.isKcVisible(TrackedActivity.ZOMBIE_PIRATE_CHESTS)) {
				incrementChatboxActivity(TrackedActivity.ZOMBIE_PIRATE_CHESTS);
			}
			return;
		}

		if (message.startsWith(SNEAKING_SUSPICION_PREFIX)) {
			if (message.endsWith(SNEAKING_SUSPICION_BEGINNER_AFFIX) && plugin.isKcVisible(TrackedActivity.MISSED_BEGINNER_CLUE)) incrementChatboxActivity(TrackedActivity.MISSED_BEGINNER_CLUE);
			else if (message.endsWith(SNEAKING_SUSPICION_EASY_AFFIX) && plugin.isKcVisible(TrackedActivity.MISSED_EASY_CLUE)) incrementChatboxActivity(TrackedActivity.MISSED_EASY_CLUE);
			else if (message.endsWith(SNEAKING_SUSPICION_MEDIUM_AFFIX) && plugin.isKcVisible(TrackedActivity.MISSED_MEDIUM_CLUE)) incrementChatboxActivity(TrackedActivity.MISSED_MEDIUM_CLUE);
			else if (message.endsWith(SNEAKING_SUSPICION_HARD_AFFIX) && plugin.isKcVisible(TrackedActivity.MISSED_HARD_CLUE)) incrementChatboxActivity(TrackedActivity.MISSED_HARD_CLUE);
			else if (message.endsWith(SNEAKING_SUSPICION_ELITE_AFFIX) && plugin.isKcVisible(TrackedActivity.MISSED_ELITE_CLUE)) incrementChatboxActivity(TrackedActivity.MISSED_ELITE_CLUE);
			return;
		}

		if (plugin.isKcVisible(TrackedActivity.CA_DIARY_TASKS) && message.startsWith(CA_PREFIX)) {
			incrementChatboxActivity(TrackedActivity.CA_DIARY_TASKS);
			return;
		}

		if (plugin.isKcVisible(TrackedActivity.PICKPOCKET_FAIL) && message.startsWith(PICKPOCKET_FAILED_PREFIX)) {
			int tick = client.getTickCount();
			if (pickpocketFailMsgTick == tick) incrementChatboxActivity(TrackedActivity.PICKPOCKET_FAIL);
			pickpocketFailMsgTick = client.getTickCount();
		}

		if (message.startsWith(LAP_PREFIX)) {
			Matcher matcher = LAP_PATTERN.matcher(message);
			if (matcher.find()) {
				String courseName = matcher.group(1);
				int newLapCount = Integer.parseInt(matcher.group(2).replace(",", ""));
				TrackedActivity lapActivity = null;

				switch (courseName) {
					case "Gnome Stronghold Agility lap": lapActivity = TrackedActivity.AGILITY_GNOME_STRONGHOLD; break;
					case "Shayzien Basic Agility Course lap": lapActivity = TrackedActivity.AGILITY_SHAYZIEN_LOW; break;
					case "Shayzien Advanced Agility Course lap": lapActivity = TrackedActivity.AGILITY_SHAYZIEN_HIGH; break;
					case "Penguin Agility lap": lapActivity = TrackedActivity.AGILITY_PENGUIN; break;
					case "Barbarian Outpost lap": lapActivity = TrackedActivity.AGILITY_BARBARIAN_OUTPOST; break;
					case "Ape Atoll Agility lap": lapActivity = TrackedActivity.AGILITY_APE_ATOLL; break;
					case "Wilderness Agility lap": lapActivity = TrackedActivity.AGILITY_WILDERNESS; break;
					case "Colossal Wyrm Agility Course (Advanced) lap": lapActivity = TrackedActivity.AGILITY_COLOSSAL_WYRM_ADVANCED; break;
					case "Colossal Wyrm Agility Course (Basic) lap": lapActivity = TrackedActivity.AGILITY_COLOSSAL_WYRM_BASIC; break;
					case "Werewolf Agility lap": lapActivity = TrackedActivity.AGILITY_WEREWOLF; break;
					case "Prifddinas Agility Course lap": lapActivity = TrackedActivity.AGILITY_PRIFDDINAS; break;
					case "Draynor Village Rooftop lap": lapActivity = TrackedActivity.AGILITY_DRAYNOR_ROOFTOP; break;
					case "Al Kharid Rooftop lap": lapActivity = TrackedActivity.AGILITY_AL_KHARID_ROOFTOP; break;
					case "Varrock Rooftop lap": lapActivity = TrackedActivity.AGILITY_VARROCK_ROOFTOP; break;
					case "Canifis Rooftop lap": lapActivity = TrackedActivity.AGILITY_CANIFIS_ROOFTOP; break;
					case "Falador Rooftop lap": lapActivity = TrackedActivity.AGILITY_FALADOR_ROOFTOP; break;
					case "Seers' Village Rooftop lap": lapActivity = TrackedActivity.AGILITY_SEERS_ROOFTOP; break;
					case "Pollnivneach Rooftop lap": lapActivity = TrackedActivity.AGILITY_POLLNIVNEACH_ROOFTOP; break;
					case "Rellekka Rooftop lap": lapActivity = TrackedActivity.AGILITY_RELLEKKA_ROOFTOP; break;
					case "Ardougne Rooftop lap": lapActivity = TrackedActivity.AGILITY_ARDOUGNE_ROOFTOP; break;
					case "Agility Pyramid lap": lapActivity = TrackedActivity.AGILITY_PYRAMID; break;
					case "Dorgesh-Kaan Agility lap": lapActivity = TrackedActivity.AGILITY_DORGESH_KAAN; break;
					case "Agility Arena Total Ticket": lapActivity = TrackedActivity.AGILITY_BRIMHAVEN; break;
				}

				if (lapActivity != null && plugin.isKcVisible(lapActivity) && session.isTracking(lapActivity.getId())) {
					processChatboxUpdate(lapActivity.getId(), newLapCount);
				}
			}
			return;
		}

		if (message.startsWith(YOU_HAVE_COMPLETED_PREFIX)) {
			if (plugin.isKcVisible(TrackedActivity.HUNTER_RUMOURS)) {
				Matcher rumourMatcher = RUMOUR_PATTERN.matcher(message);
				if (rumourMatcher.find()) {
					int newRumourCount = Integer.parseInt(rumourMatcher.group(1).replace(",", ""));
					if (session.isTracking(HUNTER_RUMOURS)) {
						processChatboxUpdate(HUNTER_RUMOURS, newRumourCount);
					}
					return;
				}
			}

			if (plugin.isKcVisible(TrackedActivity.MAHOGANY_HOMES)) {
				Matcher mahoganyMatcher = MAHOGANY_HOMES_PATTERN.matcher(message);
				if (mahoganyMatcher.find()) {
					int newContractCount = Integer.parseInt(mahoganyMatcher.group(1).replace(",", ""));
					if (session.isTracking(MAHOGANY_HOMES)) {
						processChatboxUpdate(MAHOGANY_HOMES, newContractCount);
					}
				}
			}
		}
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


	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event) {
		int tick = client.getTickCount();
		int soundId = event.getSoundId();

		if (plugin.isKcVisible(TrackedActivity.PICKPOCKET_FAIL) && soundId == STUNNED_SOUND_EFFECT_ID) {
			if (pickpocketFailMsgTick == tick)
				incrementChatboxActivity(TrackedActivity.PICKPOCKET_FAIL);
			pickpocketFailMsgTick = client.getTickCount();
		}

	}
}