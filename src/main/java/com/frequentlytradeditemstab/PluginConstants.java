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
package com.frequentlytradeditemstab;

import java.io.File;
import static net.runelite.api.gameval.InterfaceID.GE_HISTORY;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.RuneLite;

/**
 * Global constants used throughout the Frequently Traded Items Tab plugin.
 */
public final class PluginConstants {

	// Prevent instantiation
	private PluginConstants() {}

	// --- General Plugin Constants ---
	public static final String PLUGIN_NAME = "Frequently Traded Items Tab";
	public static final String CONFIG_GROUP = "frequentlytradeditemstab";
	public static final String PLUGIN_DIR_NAME = "frequently-traded-items-tab";
	public static final File PLUGIN_DIR = new File(RuneLite.RUNELITE_DIR, PLUGIN_DIR_NAME);

	public static final class Cache {
		private Cache() {}
		public static final String TRADES_FILE_PREFIX = "trades-";
		public static final String HISTORY_FILE_PREFIX = "history-";
		public static final String FILE_EXTENSION = ".json";
	}

	public static final class BankUI {
		private BankUI() {}

		public static final int ITEMS_PER_ROW = 8;
		public static final int ITEM_WIDTH = 38;
		public static final int ITEM_HEIGHT = 34;
		public static final int ITEM_START_X = 43;
		public static final int ITEM_START_Y = 6;
		public static final int MAX_ITEM_ID = 35000;

		public static final int BTN_ORIGINAL_X = 420;
		public static final int BTN_ORIGINAL_Y = 420;
		public static final int BTN_ORIGINAL_WIDTH = 30;
		public static final int BTN_ORIGINAL_HEIGHT = 30;

		public static final int BANK_MARGIN_PADDING = 6;
		public static final int SCROLL_BAR_WIDTH = 16;

		public static final int ITEM_CONTAINER_ID = (InterfaceID.BANKMAIN << 16) | 12;
		public static final int BOTTOM_BAR_CHILD_ID = 16;
		public static final int SCROLLBAR_ID = (InterfaceID.BANKMAIN << 16) | 13;

		public static final int BANK_TITLE_BAR_ID = (InterfaceID.BANKMAIN << 16) | 15;
		public static final int FREQUENT_ITEMS_BUTTON_ID = 555;
	}

	public static final class GeHistory {
		private GeHistory() {}

		public static final int GE_HISTORY_CHILD_CONTAINER_ID = 3;

		public static final int PACKED_CONTAINER_ID = (GE_HISTORY << 16) | GE_HISTORY_CHILD_CONTAINER_ID;

		public static final int ELEMENTS_PER_ROW = 6;
		public static final long PARSE_COOLDOWN_MS = 5 * 60 * 1000;
	}

	public static final class Analysis {
		private Analysis() {}
		public static final int DEFAULT_FREQUENCY_THRESHOLD = 5;
	}
}
