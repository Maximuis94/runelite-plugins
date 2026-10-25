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

/**
 * A herblore recipe: a base item, an ingredient that is added to it, and the item that results from combining
 * the two. Implementations define how ingredient consumption and the produced item depend on the consumed base.
 */
public interface BaseRecipe
{
	/**
	 * Returns the item IDs that can be consumed as the base of this recipe. Regular recipes have a single base
	 * item; upgrade recipes accept any of the four dose variants of their base potion.
	 */
	int[] getBaseItemIds();

	/**
	 * Returns the item ID of the ingredient that is added to the base.
	 */
	int getIngredientItemId();

	/**
	 * Returns the item ID of the produced item given the amount of doses of the consumed base. Regular recipes
	 * always produce the same item and ignore this parameter; upgrade recipes produce the matching dose variant
	 * of their produced potion.
	 */
	int getProducedItemId(int baseDoses);

	/**
	 * Returns the amount of ingredient items a single action consumes. Regular recipes consume a single ingredient;
	 * upgrade recipes scale with the amount of doses of the base potion (a 4-dose base consumes four times the
	 * per-dose ingredient amount).
	 */
	default int getIngredientQuantity(int baseDoses)
	{
		return 1;
	}
}
