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

package com.activitycounter.models;

import net.runelite.api.Client;

public enum TrackerType {

	// --- STATE-DRIVEN TRACKERS (Differential) ---
	VARBIT_VALUE {
		@Override
		public int fetchBaseline(Client client, TrackedActivity activity) {
			int total = 0;
			if (activity.getGameSourceIds() != null) {
				for (int id : activity.getGameSourceIds()) {
					if (id > 0) total += client.getVarbitValue(id);
				}
			}
			return total;
		}

		@Override
		public int calculateNewSessionKc(Count kc, int newClientValue) {
			return newClientValue - kc.getInitialKc();
		}
	},
	VARPLAYER_VALUE {
		@Override
		public int fetchBaseline(Client client, TrackedActivity activity) {
			int total = 0;
			if (activity.getGameSourceIds() != null) {
				for (int id : activity.getGameSourceIds()) {
					if (id > 0) total += client.getVarpValue(id);
				}
			}
			return total;
		}

		@Override
		public int calculateNewSessionKc(Count kc, int newClientValue) {
			return newClientValue - kc.getInitialKc();
		}
	},
	STAT_CHANGE {
		@Override
		public int fetchBaseline(Client client, TrackedActivity activity) {
			// Baseline is handled dynamically in StatChangeListener when stats actually load
			return -1;
		}

		@Override
		public int calculateNewSessionKc(Count kc, int newClientValue) {
			return newClientValue - kc.getInitialKc();
		}
	},

	// --- EVENT-DRIVEN TRACKERS (Cumulative) ---
	CHAT_MESSAGE {
		@Override
		public int fetchBaseline(Client client, TrackedActivity activity) { return 0; }

		@Override
		public int calculateNewSessionKc(Count kc, int eventIncrement) {
			return kc.getSessionKc() + eventIncrement;
		}
	},
	SOUND_EFFECT {
		@Override
		public int fetchBaseline(Client client, TrackedActivity activity) { return 0; }

		@Override
		public int calculateNewSessionKc(Count kc, int eventIncrement) {
			return kc.getSessionKc() + eventIncrement;
		}
	},
	EQUIPMENT_SOUND_EFFECT {
		@Override
		public int fetchBaseline(Client client, TrackedActivity activity) { return 0; }

		@Override
		public int calculateNewSessionKc(Count kc, int eventIncrement) {
			return kc.getSessionKc() + eventIncrement;
		}
	},
	AREA_SOUND {
		@Override
		public int fetchBaseline(Client client, TrackedActivity activity) { return 0; }

		@Override
		public int calculateNewSessionKc(Count kc, int eventIncrement) {
			return kc.getSessionKc() + eventIncrement;
		}
	},
	RESOURCE_CONSUMED {
		@Override
		public int fetchBaseline(Client client, TrackedActivity activity) { return 0; }

		@Override
		public int calculateNewSessionKc(Count kc, int amountConsumed) {
			return kc.getSessionKc() + amountConsumed;
		}
	},
	CUSTOM {
		@Override
		public int fetchBaseline(Client client, TrackedActivity activity) { return 0; }

		@Override
		public int calculateNewSessionKc(Count kc, int eventIncrement) {
			return kc.getSessionKc() + eventIncrement;
		}
	};

	/**
	 * Extracts the starting value for this tracker when a session begins.
	 */
	public abstract int fetchBaseline(Client client, TrackedActivity activity);

	/**
	 * Calculates the new session count based on the tracker's paradigm.
	 * For State-Driven: `value` is the new total Varbit/Varp/Stat.
	 * For Event-Driven: `value` is the incremental amount (usually 1).
	 */
	public abstract int calculateNewSessionKc(Count kc, int value);
}