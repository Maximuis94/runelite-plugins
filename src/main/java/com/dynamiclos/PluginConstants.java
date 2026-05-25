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

package com.dynamiclos;

import java.awt.Color;
import net.runelite.api.gameval.NpcID;

/**
 * Static values used by the plugin
 */
public final class PluginConstants
{
	private PluginConstants() {throw new UnsupportedOperationException("Utility class cannot be instantiated");}

	public static final String PLUGIN_CONFIG_GROUP = "dynamiclineofsight";

	public static final int STAFF_EQUIPMENT_TYPE_ID = 18;
	public static final int STAFF_AUTOCAST_STYLE_INDEX = 3;
	public static final int STAFF_DEFENSIVE_AUTOCAST_STYLE_INDEX = 4;
	public static final int SPELL_CAST_ATTACK_RANGE = 10;

	public static final float DEFAULT_LINE_WIDTH = 1.f;

	public static final int DEFAULT_OUTLINE_ALPHA = 200;
	public static final int DEFAULT_FILL_ALPHA = 15;

//	public static final Color DEFAULT_COLOR_MELEE = new Color(255, 0, 0);
	public static final Color DEFAULT_COLOR_MELEE_OUTLINE = new Color(255, 0, 0, DEFAULT_OUTLINE_ALPHA);
	public static final Color DEFAULT_COLOR_MELEE_FILL = new Color(255, 0, 0, DEFAULT_FILL_ALPHA);
	
//	public static final Color DEFAULT_COLOR_RANGED = new Color(0, 255, 0);
	public static final Color DEFAULT_COLOR_RANGED_OUTLINE = new Color(0, 255, 0, DEFAULT_OUTLINE_ALPHA);
	public static final Color DEFAULT_COLOR_RANGED_FILL = new Color(0, 255, 0, DEFAULT_FILL_ALPHA);
	
//	public static final Color DEFAULT_COLOR_MAGIC = new Color(0, 0, 255);
	public static final Color DEFAULT_COLOR_MAGIC_OUTLINE = new Color(0,0, 255, DEFAULT_OUTLINE_ALPHA);
	public static final Color DEFAULT_COLOR_MAGIC_FILL = new Color(0, 0, 255, DEFAULT_FILL_ALPHA);

//	public static final Color DEFAULT_COLOR_OTHER = new Color(155, 0, 155);
	public static final Color DEFAULT_COLOR_OTHER_OUTLINE = new Color(155, 0, 155, DEFAULT_OUTLINE_ALPHA);
	public static final Color DEFAULT_COLOR_OTHER_FILL = new Color(155, 0, 155, DEFAULT_FILL_ALPHA);

//	public static final Color DEFAULT_COLOR_ACTIVE_WEAPON = new Color(255, 0, 125);
	public static final Color DEFAULT_COLOR_ACTIVE_WEAPON_OUTLINE = new Color(255, 0, 125, DEFAULT_OUTLINE_ALPHA);
	public static final Color DEFAULT_COLOR_ACTIVE_WEAPON_FILL = new Color(255, 0, 125, DEFAULT_FILL_ALPHA);
}