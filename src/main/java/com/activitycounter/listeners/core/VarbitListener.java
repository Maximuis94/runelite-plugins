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
import com.activitycounter.models.Count;
import com.activitycounter.models.Session;
import com.activitycounter.models.TrackedActivity;
import com.activitycounter.models.TrackerType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
public class VarbitListener extends AbstractListenerNode
{
	@Getter
	private final boolean isBusy = false;
	@Getter
	private final boolean isInWilderness = false;

	private static final int MAX_VAR_ID = 20000;

	@SuppressWarnings("unchecked")
	private final List<TrackedActivity>[] varpArray = (List<TrackedActivity>[]) new List[MAX_VAR_ID];

	@SuppressWarnings("unchecked")
	private final List<TrackedActivity>[] varbitArray = (List<TrackedActivity>[]) new List[MAX_VAR_ID];

	private final int[] previousValues;

	public VarbitListener() {
		previousValues = new int[TrackedActivity.MAX_TRACKING_ID + 1];
		Arrays.fill(previousValues, -1);

		for (TrackedActivity act : TrackedActivity.values()) {
			int[] sourceIds = act.getGameSourceIds();
			if (sourceIds == null) continue;

			for (int id : sourceIds) {
				if (id <= 0 || id >= MAX_VAR_ID) continue;

				if (act.getTrackerType() == TrackerType.VARPLAYER_VALUE) {
					if (varpArray[id] == null) varpArray[id] = new ArrayList<>();
					varpArray[id].add(act);
				} else if (act.getTrackerType() == TrackerType.VARBIT_VALUE) {
					if (varbitArray[id] == null) varbitArray[id] = new ArrayList<>();
					varbitArray[id].add(act);
				}
			}
		}
	}

	/**
	 * Calculates the sum of all varp/varbit IDs associated with an activity.
	 * This enables composite counters (e.g., adding 4 different boss KCs together)
	 */
	private int calculateCompositeValue(TrackedActivity act) {
		int total = 0;
		int[] sourceIds = act.getGameSourceIds();
		if (sourceIds == null) return 0;

		for (int id : sourceIds) {
			if (id > 0) {
				total += (act.getTrackerType() == TrackerType.VARPLAYER_VALUE)
					? client.getVarpValue(id)
					: client.getVarbitValue(id);
			}
		}
		return total;
	}

	private Integer initialCount(TrackedActivity activity, int newCount) {
		int id = activity.getId();
		int prev = previousValues[id];
		if (prev == -1 || plugin.getLoginTicks() < 5) {
			previousValues[id] = newCount;
			return null;
		}
		return prev;
	}

	/**
	 * Registers a value update for a TrackedActivity, but only if it's an increase within certain limits.
	 * @param activity The activity the number is related to
	 * @param newValue The new value of the number
	 * @param maxDelta Max increase of this number in a single update. Ignore update if this threshold is exceeded
	 */
	private void updateIncreasing(TrackedActivity activity, int newValue, int maxDelta) {
		Integer prev = initialCount(activity, newValue);
		if (prev == null) return;

		int delta = newValue - prev;
		if (delta > 0 && delta <= maxDelta) {
			plugin.increaseCountByAmount(activity.getId(), delta);
		}
		previousValues[activity.getId()] = newValue;
	}

	/**
	 * Registers a value update for a TrackedActivity, but only if it's a decrease within certain limits.
	 * @param activity The activity the number is related to
	 * @param newValue The new value of the number
	 * @param maxDelta Max decrease of this number in a single update. Ignore update if this threshold is exceeded
	 */
	private void updateDecreasing(TrackedActivity activity, int newValue, int maxDelta) {
		Integer prev = initialCount(activity, newValue);
		if (prev == null) return;

		int delta = prev - newValue;
		if (delta > 0 && delta <= maxDelta) {
			plugin.increaseCountByAmount(activity.getId(), delta);
		}
		previousValues[activity.getId()] = newValue;
	}

	public void resetBaselines() {
		if (client != null && client.getGameState() == GameState.LOGGED_IN) {
			for (TrackedActivity act : TrackedActivity.values()) {
				if (act.getMaxDelta() > 0 || act.isDecreasing()) {
					previousValues[act.getId()] = calculateCompositeValue(act);
				}
			}
		}
	}

	/**
	 * Iterate over all given trackedActivities. Act based on TrackedActivity parameters.
	 */
	private void iterateOverActivities(List<TrackedActivity> trackedActivities)
	{
		if (trackedActivities != null) {
			for (TrackedActivity act : trackedActivities) {
				if (!plugin.isTracking(act.getId())) continue;

				int compositeValue = calculateCompositeValue(act);

				if (act.getMaxDelta() > 0) {
					if (act.isDecreasing()) {
						updateDecreasing(act, compositeValue, act.getMaxDelta());
					} else {
						updateIncreasing(act, compositeValue, act.getMaxDelta());
					}
				}
				else if (act.getTriggerCondition() != null) {
					boolean shouldTrigger = false;
					if (act.getTriggerCondition() == TrackedActivity.TriggerCondition.GREATER_THAN_ZERO && compositeValue > 0) {
						shouldTrigger = true;
					} else if (act.getTriggerCondition() == TrackedActivity.TriggerCondition.EQUALS_ONE && compositeValue == 1) {
						shouldTrigger = true;
					}

					if (shouldTrigger) {
						plugin.increaseCountByOne(act.getId());
					}
				}
				else {
					plugin.processActivityUpdate(act.getId(), compositeValue);
				}
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event) {
		if (!plugin.hasActiveSession()) return;

		int varbitId = event.getVarbitId();
		int varpId = event.getVarpId();

		if (varbitId == VarbitID.DATE_SECONDS_PAST_MINUTE) {
			plugin.tickSessionTime();
		}

		if (varbitId != -1) {
			if (varbitId < MAX_VAR_ID) {
				iterateOverActivities(varbitArray[varbitId]);
			} else {
				log.warn("Varbit value change detected for Varbit with VarbitID={}, exceeding MAX_VAR_ID", varbitId);
			}
		}

		if (varpId != -1) {
			if (varpId < MAX_VAR_ID) {
				iterateOverActivities(varpArray[varpId]);
			} else {
				log.warn("VarPlayer value change detected for Varpbit with VarpID={}, exceeding MAX_VAR_ID", varpId);
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		Session session = plugin.getCurrentSession();
		if (session == null || !session.isInProgress()) return;

		if (plugin.getLoginTicks() < 5) return;

		for (TrackedActivity act : TrackedActivity.values()) {
			if (act.getMaxDelta() > 0 || act.isDecreasing()) {
				if (previousValues[act.getId()] == -1) {
					previousValues[act.getId()] = calculateCompositeValue(act);
				}
			}
		}

		boolean baselinesUpdated = false;

		for (Count count : session.getAllKillCounts()) {
			if (count.getInitialKc() != -1) continue;

			TrackedActivity act = TrackedActivity.getByStorageId(count.getTrackingId());
			if (act == null || act.getGameSourceIds() == null) continue;

			try {
				if (act.getTrackerType() == TrackerType.VARPLAYER_VALUE || act.getTrackerType() == TrackerType.VARBIT_VALUE) {
					count.setInitialKc(calculateCompositeValue(act));
					baselinesUpdated = true;
				}
			} catch (IndexOutOfBoundsException e) {
				log.debug("Invalid Value for TrackerActivity {}", act.getName(), e);
			}
		}

		if (baselinesUpdated) {
			plugin.setRequiresSaveAndRefresh(true);
		}
	}
}