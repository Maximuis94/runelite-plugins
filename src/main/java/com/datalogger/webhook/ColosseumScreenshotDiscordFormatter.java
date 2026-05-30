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

import com.datalogger.DataLoggerConfig;
import com.datalogger.dto.ColosseumAttemptDTO;
import com.datalogger.dto.ColosseumWaveDTO;
import com.datalogger.models.enums.WaveStatus;
import com.datalogger.utils.ImageUtil;
import static com.datalogger.webhook.ColosseumDiscordBroadcaster.generateMinimalEmbed;
import static com.datalogger.webhook.ColosseumDiscordBroadcaster.generateMinimalTestEmbed;
import static com.datalogger.webhook.DiscordWebhookUtils.wrapEmbedIntoPayload;
import static com.datalogger.webhook.WebhookFormatUtils.formatActiveModifiers;
import static com.datalogger.webhook.WebhookFormatUtils.formatColosseumWaveLine;
import static com.datalogger.webhook.WebhookFormatUtils.formatSeconds;
import static com.datalogger.webhook.WebhookFormatUtils.getWavesCompleted;
import com.google.gson.JsonObject;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import net.runelite.client.ui.FontManager;

/**
 * Static utility class for handling Colosseum Discord payload formatting and projecting wave data directly onto
 * screenshots.
 * Provides the same information as the detailed format, but via a less spacious screenshot
 */
public final class ColosseumScreenshotDiscordFormatter
{
//	private static final Font DEFAULT_FONT = new Font("Runescape Small", Font.BOLD, 24);
	private static final Font DEFAULT_FONT = FontManager.getRunescapeBoldFont();
	private static final int MIN_COLUMN_WIDTH = 800;
	private static final int LINE_HEIGHT = 20;
	private static final int LEFT_MARGIN = 20;

	private ColosseumScreenshotDiscordFormatter() {}

	/**
	 * Builds a minimal Discord payload with only the footer
	 */
	@Nonnull
	public static JsonObject buildPayload(boolean isTest) {
		JsonObject embed = isTest ? generateMinimalTestEmbed() : generateMinimalEmbed();
		return wrapEmbedIntoPayload(embed);
	}

	public static BufferedImage projectWaveDataOntoScreenshot(BufferedImage screenshot, ColosseumAttemptDTO dto, DataLoggerConfig config) {
		List<String> descriptionLines = generateDescriptionLines(dto, config);
		List<String> waveLines = new ArrayList<>();

		if (dto.getWaves() != null) {
			int nWaves = dto.getWaves().size();

			for (ColosseumWaveDTO wave : dto.getWaves()) {
				WaveStatus status = wave.getWave() < nWaves ? WaveStatus.COMPLETED : WaveStatus.fromString(wave.getStatus());

				String waveSummary = formatColosseumWaveLine(wave, config);
				StringBuilder line = new StringBuilder("Wave " + wave.getWave() + " | ");

				if (status != WaveStatus.COMPLETED) {
					line.append(status.toString().toLowerCase()).append(" | ");
				}

				if (config.includeTime() && status != WaveStatus.CANCELLED) {
					line.append(formatSeconds(wave.getTimeTaken())).append(" - ");
				}

				line.append(waveSummary);
				waveLines.add(line.toString());
			}
		}

		if (waveLines.isEmpty() && descriptionLines.isEmpty()) {
			return screenshot;
		}

		int numColumns = screenshot.getWidth() >= (MIN_COLUMN_WIDTH * 2) ? 2 : 1;
		int columnWidth = screenshot.getWidth() / numColumns;

		int nWaves = waveLines.size();
		int rows = (int) Math.ceil((double) nWaves / numColumns);

		int padding = 15;

		int barHeight = ((numColumns == 1 ? descriptionLines.size() : 1) * LINE_HEIGHT) + (rows * LINE_HEIGHT) + (padding * 3);

		BufferedImage expandedImage = ImageUtil.appendDataBarToScreenshot(screenshot, barHeight);

		Graphics2D g = expandedImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		Font baseRsFont = FontManager.getRunescapeFont();
		int currentY = screenshot.getHeight() + padding + 12;

		g.setFont(baseRsFont.deriveFont(24f));
		g.setColor(Color.BLUE);

		if (numColumns == 1)
		{
			for (String descLine : descriptionLines)
			{
				g.drawString(descLine, LEFT_MARGIN, currentY);
				currentY += LINE_HEIGHT;
			}
		}
		else
		{
			String mergedDescription = String.join(" | ", descriptionLines);
			g.drawString(mergedDescription, LEFT_MARGIN, currentY);
			currentY += LINE_HEIGHT;
		}

		currentY += 10;
		g.setFont(baseRsFont.deriveFont(16f));

		for (int waveId = 0; waveId < nWaves; waveId++) {
			int col = waveId / rows;
			int row = waveId % rows;

			int x = LEFT_MARGIN + (col * columnWidth);
			int y = currentY + (row * LINE_HEIGHT);

			g.drawString(waveLines.get(waveId), x, y);
		}

		g.dispose();
		return expandedImage;
	}

	/**
	 * Takes the original screenshot, projects the wave data onto it, and saves it
	 * as a new file specifically for Discord broadcasting.
	 */
	public static File createProjectedScreenshotFile(File originalFile, ColosseumAttemptDTO dto, DataLoggerConfig config)
	{
		if (originalFile == null || !originalFile.exists())
		{
			return null;
		}

		try
		{
			BufferedImage originalImage = ImageIO.read(originalFile);

			BufferedImage projectedImage = projectWaveDataOntoScreenshot(originalImage, dto, config);

			String extension = config.screenshotFormat().getExtension();
			File outputFile = new File(originalFile.getParentFile(), "discord_broadcast." + extension);
			ImageIO.write(projectedImage, extension, outputFile);

			return outputFile;
		}
		catch (Exception e)
		{
			return originalFile;
		}
	}

	public static List<String> generateDescriptionLines(@Nonnull ColosseumAttemptDTO dto, @Nonnull DataLoggerConfig config)
	{
		List<String> lines = new ArrayList<>();
		String sep = " | ";

		List<String> topStats = new ArrayList<>();
		if (config.includeStatus()) {
			topStats.add(String.format("Status: %s | %s/12 completed",
				dto.getResult().toLowerCase(),
				getWavesCompleted(dto.getWaves())));
		}
		if (config.includeTime()) {
			topStats.add(String.format("Time: %s", formatSeconds(dto.getTotalTime())));
		}
		if (config.includeGlory()) {
			topStats.add(String.format("Glory: %,d", dto.getTotalGlory()));
		}

		if (!topStats.isEmpty()) {
			lines.add(String.join(sep, topStats));
		}

		if (config.includeModifiers()) {
			String activeModifiers = formatActiveModifiers(dto.getActiveModifiers(), true);
			lines.add(String.format("Modifiers: [ %s ]", activeModifiers));
		}

		List<String> econStats = new ArrayList<>();
		if (config.includeRewardValue()) {
			econStats.add(String.format("Reward: %,d gp", dto.getRewardsValue()));
		}
		if (config.includeSupplyValue()) {
			econStats.add(String.format("Supplies: %,d gp", dto.getConsumedSupplyValue()));
		}

		if (!econStats.isEmpty()) {
			lines.add(String.join(sep, econStats));
		}

		return lines;
	}
}