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

package com.datalogger.ui.modes;

import com.datalogger.DataLoggerConfig;
import com.datalogger.constants.PluginConstants;
import static com.datalogger.constants.PluginConstants.WEBHOOK_TEST_COOLDOWN_SECONDS_SUCCESS;
import com.datalogger.dto.ColosseumAttemptDTO;
import com.datalogger.dto.ColosseumWaveDTO;
import com.datalogger.loggers.ItemVaultLogger;
import com.datalogger.services.DiscordWebhookService;
import com.datalogger.services.FileIOService;
import com.datalogger.services.itemvault.VaultManager;
import static com.datalogger.ui.utils.UIUtils.createStyledButton;
import com.datalogger.webhook.ColosseumCustomDiscordFormatter;
import com.datalogger.webhook.ColosseumDiscordBroadcaster;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.ColorScheme;

@Slf4j
public class DebugModePanel extends JPanel {

	private final ScheduledExecutorService executor;
	private final DiscordWebhookService discordWebhookService;
	private final ColosseumDiscordBroadcaster colosseumDiscordBroadcaster;
	private final Gson gson;
	private final DataLoggerConfig config;
	private final VaultManager vaultManager;
	private final ClientThread clientThread;
	private final FileIOService fileIOService;
	private final ItemVaultLogger itemVaultLogger;

	private Instant waitUntil;
	@Inject
	public DebugModePanel(ScheduledExecutorService executor, DiscordWebhookService discordWebhookService,
								 ColosseumDiscordBroadcaster colosseumDiscordBroadcaster, Gson gson,
								 DataLoggerConfig config, VaultManager vaultManager, ClientThread clientThread,
								 FileIOService fileIOService, ItemVaultLogger itemVaultLogger) {
		this.executor = executor;
		this.discordWebhookService = discordWebhookService;
		this.colosseumDiscordBroadcaster = colosseumDiscordBroadcaster;
		this.gson = gson;
		this.config = config;
		this.vaultManager = vaultManager;
		this.clientThread = clientThread;
		this.fileIOService = fileIOService;
		this.itemVaultLogger = itemVaultLogger;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel buttonContainer = new JPanel();
		buttonContainer.setLayout(new GridLayout(8, 1, 0, 8));
		buttonContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		buttonContainer.add(testCustomTemplateButton());
		buttonContainer.add(testDiscordButton());

		add(buttonContainer, BorderLayout.NORTH);
	}

	private JButton testCustomTemplateButton()
	{
		waitUntil = Instant.now();
		JButton uploadTestButton = createStyledButton("Test with Uploaded Log", e -> testCustomTemplate());
		uploadTestButton.setToolTipText("Select a saved .json wave-log to test your custom formatting template.");
		return uploadTestButton;
	}

	/**
	 * Prompts the user to select a wave-log JSON file, parses it into a DTO,
	 * and sends it to the Discord Webhook.
	 * A post-submission cooldown is used to prevent spamming.
	 */
	private void testCustomTemplate() {
		Instant now = Instant.now();
		if (now.isBefore(waitUntil))
		{
			long secondsLeft = Duration.between(now, waitUntil).getSeconds();
			JOptionPane.showMessageDialog(this,
				"Please wait " + Math.max(secondsLeft, 1) + " seconds before sending another request",
				"Don't spam :(",
				JOptionPane.WARNING_MESSAGE);
			return;
		}

		JFileChooser fileChooser = new JFileChooser(PluginConstants.COLOSSEUM_ATTEMPT_DIR);
		fileChooser.setDialogTitle("Select a Colosseum wave-log JSON file");
		fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Wave Logs", "json"));

		int userSelection = fileChooser.showOpenDialog(this);
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File fileToParse = fileChooser.getSelectedFile();

			log.debug("Selected file for custom template test: {}", fileToParse.getName());

			executor.submit(() -> {
				try (Reader reader = new FileReader(fileToParse)) {
					ColosseumAttemptDTO parsedDto = gson.fromJson(reader, ColosseumAttemptDTO.class);
					if (parsedDto != null) {
						List<ColosseumWaveDTO> waves = parsedDto.getWaves();
						if (waves == null || waves.isEmpty())
						{
							JOptionPane.showMessageDialog(this,
								"The selected file is not a valid wave log json file.\n" +
									"Please ensure you are selecting a wave log file (ends with '_wave-log.json').",
								"Invalid Log File",
								JOptionPane.ERROR_MESSAGE);
							return;
						}
						log.debug("Successfully parsed attempt {}. Firing Discord webhook with custom template...", parsedDto.getAttemptId());
						String url = config.colosseumDiscordWebhookUrl();
						if (url == null || url.trim().isEmpty())
						{
							log.warn("Cannot broadcast custom test template: Webhook URL is empty.");
							return;
						}

						JsonObject payload = ColosseumCustomDiscordFormatter.buildTestPayload(parsedDto, config.colosseumCustomTemplate());
						discordWebhookService.sendWebhook(url, payload);
						waitUntil = Instant.now().plusSeconds(WEBHOOK_TEST_COOLDOWN_SECONDS_SUCCESS);
					} else {
						log.warn("Failed to parse the DTO. Ensure the JSON structure matches ColosseumAttemptDTO.");
					}
				} catch (Exception ex) {
					log.error("Failed to read or parse the JSON file for Discord test.", ex);
				}
			});
		}
	}

	private JButton testDiscordButton()
	{
		return createStyledButton("Discord test",e -> sendTestDiscordMessage());
	}

	/**
	 * Prompts the user to select a wave-log JSON file, parses it into a DTO,
	 * and sends it to the Discord Webhook.
	 */
	private void sendTestDiscordMessage() {
		JFileChooser fileChooser = new JFileChooser(PluginConstants.COLOSSEUM_ATTEMPT_DIR);
		fileChooser.setDialogTitle("Select Colosseum Attempt JSON");
		fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Wave Logs", "json"));

		int userSelection = fileChooser.showOpenDialog(this);
		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File fileToParse = fileChooser.getSelectedFile();

			log.debug("Selected file for Discord test: {}", fileToParse.getName());

			executor.submit(() -> {
				try (Reader reader = new FileReader(fileToParse)) {
					ColosseumAttemptDTO parsedDto = gson.fromJson(reader, ColosseumAttemptDTO.class);

					if (parsedDto != null) {
						log.debug("Successfully parsed attempt {}. Firing Discord webhook...", parsedDto.getAttemptId());
						String url = config.colosseumDiscordWebhookUrl();
						if (url == null || url.trim().isEmpty())
						{
							log.warn("Cannot broadcast: Webhook URL is empty.");
							return;
						}
						colosseumDiscordBroadcaster.broadcastToDiscord(parsedDto);
//						JsonObject payload;
//						switch (config.colosseumWebhookFormat())
//						{
//							case CONCISE:
//								payload = ColosseumConciseDiscordFormatter.buildPayload(parsedDto, config);
//								discordWebhookService.sendWebhook(url, payload);
//								break;
//							case CUSTOM:
//								payload = ColosseumCustomDiscordFormatter.buildPayload(parsedDto, config.colosseumCustomTemplate());
//								discordWebhookService.sendWebhook(url, payload);
//								break;
//							case SCREENSHOT:
//								colosseumDiscordBroadcaster.broadcastToDiscord(parsedDto);
//								break;
//							default:
//								payload = ColosseumDetailedDiscordFormatter.buildPayload(parsedDto, config);
//								discordWebhookService.sendWebhook(url, payload);
//
//						}
					} else {
						log.warn("Parsed DTO was null. Ensure the JSON structure matches ColosseumAttemptDTO.");
					}
				} catch (Exception ex) {
					log.error("Failed to read or parse the JSON file for Discord test.", ex);
				}
			});
		}
	}
}