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

package com.datalogger.constants;

/**
 * Constants related to logging of Item / Grand Exchange offers
 */
public final class Item
{
	/**
	 * Widget child IDs
	 */
	public static class InterfaceID
	{
		public static final int GE_GROUP_ID = 383;
		public static final int GE_HISTORY_CHILD_ID = 3;
		public static final int BANK_GROUP_ID = 12;
		public static final int BANK_CHILD_ID = 12;
		public static final int SEED_VAULT_GROUP_ID = 631;
		public static final int SEED_VAULT_CHILD_ID = 15;
	}

	/**
	 * Fixed values that are related to processing Grand Exchange offers
	 */
	public static class Values
	{
		public static final double TAX_RATE = 0.02;
		public static final double TAX_MULTIPLIER = 1.-TAX_RATE;
		public static final int MAX_ITEM_TAX = 5000000;
		public static final int MAX_TAX_PRICE = (int) (MAX_ITEM_TAX / TAX_RATE);
		public static final int MAX_TAXED_PRICE = MAX_TAX_PRICE - MAX_ITEM_TAX;

	}
}
