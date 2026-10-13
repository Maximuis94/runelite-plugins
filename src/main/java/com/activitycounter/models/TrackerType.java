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