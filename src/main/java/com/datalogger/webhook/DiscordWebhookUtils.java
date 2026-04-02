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

package com.datalogger.webhook;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.awt.Color;
import javax.annotation.Nonnull;
import lombok.NonNull;

/**
 * Utility class for constructing standardized Discord webhook JSON structures.
 */
public final class DiscordWebhookUtils {
	private DiscordWebhookUtils() {}

	/**
	 * Helper to wrap a built embed object into the standard Discord payload structure.
	 */
	public static @NonNull JsonObject wrapEmbedIntoPayload(JsonObject embed) {
		JsonObject payload = new JsonObject();
		JsonArray embeds = new JsonArray();
		embeds.add(embed);
		payload.add("embeds", embeds);
		return payload;
	}

	/**
	 * Creates a footer JSON object and attaches it directly to the provided embed.
	 * @param embed The main embed JsonObject to attach the footer to.
	 * @param text  The text to display in the footer.
	 */
	public static void addFooter(@Nonnull JsonObject embed, @Nonnull String text) {
		JsonObject footer = new JsonObject();
		footer.addProperty("text", text);
		embed.add("footer", footer);
	}

	/**
	 * Create a field with the given parameters and add it to fields
	 */
	public static void addField(@Nonnull JsonArray fields, @Nonnull String name, @Nonnull String value, boolean inline) {
		JsonObject field = new JsonObject();
		field.addProperty("name", name);
		field.addProperty("value", value);
		field.addProperty("inline", inline);
		fields.add(field);
	}

	/**
	 * Sets the color of the embed using a standard Java Color object.
	 * * @param embed The main embed JsonObject.
	 * @param color The Color object to apply (e.g., Color.RED).
	 */
	public static void setColor(@Nonnull JsonObject embed, @Nonnull Color color) {
		int decimalColor = color.getRGB() & 0xFFFFFF;
		embed.addProperty("color", decimalColor);
	}

	/**
	 * Sets the color of the embed using a raw decimal/hexadecimal integer.
	 * @param embed The main embed JsonObject.
	 * @param color The integer color code (e.g., 0xFF0000 for red or 16711680).
	 */
	public static void setColor(@Nonnull JsonObject embed, int color) {
		embed.addProperty("color", color);
	}

	/**
	 * Sets the color of the given embed using red, green and blue integers, each ranging from 0-255. If either color
	 * falls outside this range, the color is not updated.
	 * @param embed The main embed JsonObject.
	 * @param red The red color code (0-255)
	 * @param green The green color code (0-255)
	 * @param blue The blue color code (0-255)
	 */
	public static void setColor(@Nonnull JsonObject embed, int red, int green, int blue) {
		try
		{
			Color color = new Color(red, green, blue);
			embed.addProperty("color", color.getRGB());
		}
		catch (IllegalArgumentException ignored) {}
	}
}