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
import static com.activitycounter.PluginConstants.VARBIT_OFFSET;
import com.activitycounter.models.Count;
import com.activitycounter.models.Session;
import com.activitycounter.models.TrackedActivity;
import com.activitycounter.models.TrackerType;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import static net.runelite.api.gameval.VarbitID.HOSIDIUS_TITHE_REWARDPOINTS;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
public class VarbitListener {

	@Inject
	private Client client;

	@Inject
	private ActivityCounterPlugin plugin;

	private static final int SLAYER_POINTS_VARBITID = VarbitID.SLAYER_POINTS;
	private static final int CANNON_BALLS_VARPLAYERID = VarPlayerID.ROCKTHROWER;
	private static final int NIGHTMARE_ZONE_POINTS_SESSION_VARBITID = VarbitID.NZONE_CURRENTPOINTS;
	private static final int TITHE_FARM_VARBITID = VarbitID.HOSIDIUS_TITHE_REWARDPOINTS;
	private static final int GIANTS_FOUNDRY_POINTS_VARPLAYERID = VarPlayerID.GIANTS_FOUNDRY_REWARD_SHOP_POINTS;

	private final Map<TrackedActivity, Integer> previousValues = new EnumMap<>(TrackedActivity.class);

	/**
	 * Establishes a baseline/reference value that accounts for logging in and hopping.
	 */
	private Integer initialCount(TrackedActivity activity, int newCount)
	{
		Integer prev = previousValues.get(activity);

		if (prev == null || plugin.getLoginTicks() < 5) {
			previousValues.put(activity, newCount);
			return null;
		}

		return prev;
	}

	/**
	 * Updates a varbit/varplayer-based counter that acts as a currency as well (i.e. the value may drop by spending)
	 * maxDelta acts as an upperbound to help detect massive increments
	 */
	private void updateCurrency(Session currentSession, TrackedActivity activity, int newValue, int maxDelta) {
		Integer prev = initialCount(activity, newValue);

		if (prev == null) return;

		int delta = newValue - prev;

		if (delta > 0 && delta <= maxDelta) {
			Count kc = currentSession.getKillCount(activity.getId());
			if (kc != null) {
				if (kc.getInitialKc() == -1) {
					kc.setInitialKc(0);
				}
				kc.setSessionKc(kc.getSessionKc() + delta);
				plugin.setRequiresSaveAndRefresh(true);
			}
		}

		previousValues.put(activity, newValue);
	}

	/**
	 * Tracker for Cannonballs. Value counted is the decrease rather than the increase.
	 */
	private void updateCannonBalls(Session currentSession) {
		int newCannonBalls = client.getVarpValue(CANNON_BALLS_VARPLAYERID);
		Integer prev = initialCount(TrackedActivity.CANNONBALLS_FIRED, newCannonBalls);

		if (prev == null) return;

		int deltaCb = prev - newCannonBalls;

		if (deltaCb > 0 && deltaCb <= 2) {
			Count kc = currentSession.getKillCount(TrackedActivity.CANNONBALLS_FIRED.getId());
			if (kc != null) {
				if (kc.getInitialKc() == -1) kc.setInitialKc(0);
				kc.setSessionKc(kc.getSessionKc() + deltaCb);
				plugin.setRequiresSaveAndRefresh(true);
			}
		}
		previousValues.put(TrackedActivity.CANNONBALLS_FIRED, newCannonBalls);
	}

	/**
	 * Generic processor for monotonically increasing stats (Boss KCs, Clues, Agility laps).
	 */
	private void processActivityUpdate(int trackingId, int currentValue)
	{
		Session session = plugin.getCurrentSession();
		if (session == null) return;
		if (plugin.getLoginTicks() < 5) return;

		Count kc = session.getKillCount(trackingId);
		if (kc == null) return;

		if (kc.getInitialKc() == -1)
		{
			kc.setInitialKc(currentValue);
		}

		int newSessionKc = currentValue - kc.getInitialKc();
		if (newSessionKc > kc.getSessionKc())
		{
			kc.setSessionKc(newSessionKc);
			plugin.setRequiresSaveAndRefresh(true);
		}
	}

	public void resetBaselines() {
		if (client != null && client.getGameState() == GameState.LOGGED_IN) {
			previousValues.put(TrackedActivity.CANNONBALLS_FIRED, client.getVarpValue(VarPlayerID.ROCKTHROWER));
			previousValues.put(TrackedActivity.NMZ_POINTS, client.getVarbitValue(VarbitID.NZONE_CURRENTPOINTS));
			previousValues.put(TrackedActivity.SLAYER_POINTS, client.getVarbitValue(VarbitID.SLAYER_POINTS));
			previousValues.put(TrackedActivity.TITHE_FARM_POINTS, client.getVarbitValue(HOSIDIUS_TITHE_REWARDPOINTS));
			previousValues.put(TrackedActivity.GIANTS_FOUNDRY_POINTS, client.getVarpValue(VarPlayerID.GIANTS_FOUNDRY_REWARD_SHOP_POINTS));
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event) {
		Session session = plugin.getCurrentSession();
		if (session == null || !session.isInProgress()) return;

		int varbitId = event.getVarbitId();
		int varpId = event.getVarpId();

		if (varbitId == VarbitID.DATE_SECONDS_PAST_MINUTE) {
			session.addSecond();
			if (plugin.isShowSessionDuration()) {
				plugin.updatePanelTime();
			}
			return;
		}

		if (varpId != -1) {
			if (varpId == CANNON_BALLS_VARPLAYERID && session.isTracking(TrackedActivity.CANNONBALLS_FIRED.getId())) {
				updateCannonBalls(session);
			}

			else if (varpId == GIANTS_FOUNDRY_POINTS_VARPLAYERID && session.isTracking(TrackedActivity.GIANTS_FOUNDRY_POINTS.getId())) {
				updateCurrency(session, TrackedActivity.GIANTS_FOUNDRY_POINTS, client.getVarpValue(varpId), 50000);
			}

			else if (session.isTracking(varpId)) {
				processActivityUpdate(varpId, client.getVarpValue(varpId));
			}
		}

		if (varbitId != -1) {
			int trackedVarbitId = varbitId + VARBIT_OFFSET;

			if (varbitId == SLAYER_POINTS_VARBITID && session.isTracking(TrackedActivity.SLAYER_POINTS.getId())) {
				updateCurrency(session, TrackedActivity.SLAYER_POINTS, client.getVarbitValue(varbitId), 1500);
			} else if (varbitId == NIGHTMARE_ZONE_POINTS_SESSION_VARBITID && session.isTracking(TrackedActivity.NMZ_POINTS.getId())) {
				updateCurrency(session, TrackedActivity.NMZ_POINTS, client.getVarbitValue(varbitId), 2000000);
			} else if (varbitId == TITHE_FARM_VARBITID && session.isTracking(TrackedActivity.TITHE_FARM_POINTS.getId())) {
				updateCurrency(session, TrackedActivity.TITHE_FARM_POINTS, client.getVarbitValue(varbitId), 500);
			} else if (session.isTracking(trackedVarbitId)) {
				processActivityUpdate(trackedVarbitId, client.getVarbitValue(varbitId));
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		Session session = plugin.getCurrentSession();
		if (session == null || !session.isInProgress()) return;
		if (plugin.getLoginTicks() < 5) return;

		boolean baselinesUpdated = false;

		for (TrackedActivity act : TrackedActivity.values()) {
			if (session.isTracking(act.getId())) {
				Count kc = session.getKillCount(act.getId());

				if (kc != null && kc.getInitialKc() == -1) {
					if (act.getTrackerType() == TrackerType.VARPLAYER_VALUE) {
						try
						{
							kc.setInitialKc(client.getVarpValue(act.getId()));
							baselinesUpdated = true;
						}
						catch (IndexOutOfBoundsException e)
						{
							log.debug("Invalid Varp Value {} for TrackerActivity {} of type={}", act.getId(), act.getName(), act.getTrackerType(), e);
						}
					} else if (act.getTrackerType() == TrackerType.VARBIT_VALUE) {
						try
						{
							kc.setInitialKc(client.getVarbitValue(act.getId() - VARBIT_OFFSET));
							baselinesUpdated = true;
						}
						catch (IndexOutOfBoundsException e)
						{
							log.debug("Invalid Varbit Value {} for TrackerActivity {} of type={}", act.getId(), act.getName(), act.getTrackerType(), e);
						}
					}
				}
			}
		}

		if (baselinesUpdated) {
			plugin.setRequiresSaveAndRefresh(true);
		}
	}
}