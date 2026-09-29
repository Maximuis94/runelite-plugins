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
import com.activitycounter.models.Count;
import com.activitycounter.models.Session;
import com.activitycounter.models.TrackedActivity;
import com.activitycounter.models.TrackerType;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
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

	private final Map<Integer, List<TrackedActivity>> varpMap = new HashMap<>();
	private final Map<Integer, List<TrackedActivity>> varbitMap = new HashMap<>();

	public VarbitListener() {
		for (TrackedActivity act : TrackedActivity.values()) {
			if (act.getGameSourceId() <= 0) continue;

			if (act.getTrackerType() == TrackerType.VARPLAYER_VALUE) {
				varpMap.computeIfAbsent(act.getGameSourceId(), k -> new ArrayList<>()).add(act);
			} else if (act.getTrackerType() == TrackerType.VARBIT_VALUE) {
				varbitMap.computeIfAbsent(act.getGameSourceId(), k -> new ArrayList<>()).add(act);
			}
		}
	}

	private Integer initialCount(TrackedActivity activity, int newCount) {
		Integer prev = previousValues.get(activity);
		if (prev == null || plugin.getLoginTicks() < 5) {
			previousValues.put(activity, newCount);
			return null;
		}
		return prev;
	}

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

	private void processActivityUpdate(int storageId, int currentValue) {
		Session session = plugin.getCurrentSession();
		if (session == null) return;
		if (plugin.getLoginTicks() < 5) return;

		Count kc = session.getKillCount(storageId);
		if (kc == null) return;

		if (kc.getInitialKc() == -1) {
			kc.setInitialKc(currentValue);
		}

		int newSessionKc = currentValue - kc.getInitialKc();
		if (newSessionKc > kc.getSessionKc()) {
			kc.setSessionKc(newSessionKc);
			plugin.setRequiresSaveAndRefresh(true);
		}
	}

	public void resetBaselines() {
		if (client != null && client.getGameState() == GameState.LOGGED_IN) {
			previousValues.put(TrackedActivity.CANNONBALLS_FIRED, client.getVarpValue(VarPlayerID.ROCKTHROWER));
			previousValues.put(TrackedActivity.NMZ_POINTS, client.getVarbitValue(VarbitID.NZONE_CURRENTPOINTS));
			previousValues.put(TrackedActivity.SLAYER_POINTS, client.getVarbitValue(VarbitID.SLAYER_POINTS));
			previousValues.put(TrackedActivity.TITHE_FARM_POINTS, client.getVarbitValue(TITHE_FARM_VARBITID));
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
			List<TrackedActivity> varpActivities = varpMap.get(varpId);
			if (varpActivities != null) {
				for (TrackedActivity act : varpActivities) {
					if (!session.isTracking(act.getId())) continue;

					if (varpId == GIANTS_FOUNDRY_POINTS_VARPLAYERID) {
						updateCurrency(session, act, client.getVarpValue(varpId), 50000);
					} else {
						processActivityUpdate(act.getId(), client.getVarpValue(varpId));
					}
				}
			}

			if (varpId == CANNON_BALLS_VARPLAYERID && session.isTracking(TrackedActivity.CANNONBALLS_FIRED.getId())) {
				updateCannonBalls(session);
			}
		}

		if (varbitId != -1) {
			List<TrackedActivity> varbitActivities = varbitMap.get(varbitId);
			if (varbitActivities != null) {
				for (TrackedActivity act : varbitActivities) {
					if (!session.isTracking(act.getId())) continue;

					if (varbitId == SLAYER_POINTS_VARBITID) {
						updateCurrency(session, act, client.getVarbitValue(varbitId), 1500);
					} else if (varbitId == NIGHTMARE_ZONE_POINTS_SESSION_VARBITID) {
						updateCurrency(session, act, client.getVarbitValue(varbitId), 2000000);
					} else if (varbitId == TITHE_FARM_VARBITID) {
						updateCurrency(session, act, client.getVarbitValue(varbitId), 500);
					} else {
						processActivityUpdate(act.getId(), client.getVarbitValue(varbitId));
					}
				}
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		Session session = plugin.getCurrentSession();
		if (session == null || !session.isInProgress()) return;
		if (plugin.getLoginTicks() < 5) return;

		boolean baselinesUpdated = false;

		for (Count count : session.getAllKillCounts()) {
			if (count.getInitialKc() != -1) continue;

			TrackedActivity act = TrackedActivity.getByStorageId(count.getTrackingId());
			if (act == null || act.getGameSourceId() <= 0) continue;

			try {
				if (act.getTrackerType() == TrackerType.VARPLAYER_VALUE) {
					count.setInitialKc(client.getVarpValue(act.getGameSourceId()));
					baselinesUpdated = true;
				} else if (act.getTrackerType() == TrackerType.VARBIT_VALUE) {
					count.setInitialKc(client.getVarbitValue(act.getGameSourceId()));
					baselinesUpdated = true;
				}
			} catch (IndexOutOfBoundsException e) {
				log.debug("Invalid Value {} for TrackerActivity {}", act.getGameSourceId(), act.getName(), e);
			}
		}

		if (baselinesUpdated) {
			plugin.setRequiresSaveAndRefresh(true);
		}
	}
}