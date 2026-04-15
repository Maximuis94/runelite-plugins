/*
 * Copyright (c) 2026, maximuis94 <https://github.com/maximuis94>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
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
package com.datalogger.ui;

import com.datalogger.DataLoggerConfig;
import com.datalogger.constants.PluginConstants;
import static com.datalogger.constants.PluginConstants.WEBHOOK_TEST_COOLDOWN_SECONDS_SUCCESS;
import com.datalogger.dto.ColosseumAttemptDTO;
import com.datalogger.dto.ColosseumWaveDTO;
import com.datalogger.framework.LogType;
import com.datalogger.loggers.ColosseumAttemptLogger;
import com.datalogger.loggers.ItemVaultLogger;
import com.datalogger.services.DiscordWebhookService;
import com.datalogger.services.FileIOService;
import com.datalogger.services.itemvault.VaultManager;
import com.datalogger.webhook.ColosseumConciseDiscordFormatter;
import com.datalogger.webhook.ColosseumCustomDiscordFormatter;
import com.datalogger.webhook.ColosseumDetailedDiscordFormatter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import net.runelite.client.ui.PluginPanel;

@Slf4j
public class DataLoggerPanel extends PluginPanel {
	private final JPanel logContentDisplay = new JPanel();
	private final ScheduledExecutorService executor;
	private final DiscordWebhookService discordWebhookService;
	private final Gson gson;
	private final DataLoggerConfig config;
	private final VaultManager vaultManager;
	private ClientThread clientThread;

	private Instant waitUntil;

	@Inject
	public DataLoggerPanel(ItemVaultLogger itemVaultLogger, ScheduledExecutorService executor, FileIOService fileIOService, DiscordWebhookService discordWebhookService, Gson gson, DataLoggerConfig config, ColosseumAttemptLogger logger, VaultManager vaultManager, ClientThread clientThread) {		this.executor = executor;
		this.discordWebhookService = discordWebhookService;
		this.config = config;
		this.gson = gson;
		this.vaultManager = vaultManager;
		this.clientThread = clientThread;

		this.setBorder(new EmptyBorder(0, 0, 10, 0));
		this.setBackground(ColorScheme.DARK_GRAY_COLOR);
		this.getScrollPane().setBorder(new EmptyBorder(10, 10, 10, 10));
		this.getScrollPane().setBackground(ColorScheme.DARK_GRAY_COLOR);

		logContentDisplay.setLayout(new BorderLayout());
		logContentDisplay.setBackground(ColorScheme.DARK_GRAY_COLOR);
		add(logContentDisplay, BorderLayout.CENTER);

		JPanel buttonContainer = new JPanel();
		buttonContainer.setLayout(new GridLayout(8, 1, 0, 8));
		buttonContainer.setBorder(new EmptyBorder(10, 5, 0, 5));
		buttonContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JButton openLogsBtn = createStyledButton("Colosseum log directory", e -> openDirectory(LogType.COLOSSEUM.getLogDirectory().getAbsolutePath()));
		JButton openDataBtn = createStyledButton("Grand exchange directory", e -> openDirectory(LogType.GRAND_EXCHANGE.getLogDirectory().getAbsolutePath()));
		JButton openItemVaultBtn = createStyledButton("Item Vault directory", e -> openDirectory(LogType.ITEM_VAULT.getLogDirectory().getAbsolutePath()));
		JButton exportVaultBtn = createStyledButton("Export Vault Summary",e -> itemVaultLogger.exportAggregatedData());
		JButton mergeColosseumWaveLog = createStyledButton("Merge colosseum wave logs",e -> fileIOService.mergeColosseumWaveLogs());


		buttonContainer.add(exportVaultBtn);
		buttonContainer.add(mergeColosseumWaveLog);
		buttonContainer.add(openLogsBtn);
		buttonContainer.add(openItemVaultBtn);
		buttonContainer.add(openDataBtn);
		buttonContainer.add(testCustomTemplateButton());
//		buttonContainer.add(testDiscordButton());
		buttonContainer.add(writeMergedLogsButton());

		add(buttonContainer, BorderLayout.SOUTH);
	}

	private JButton writeMergedLogsButton()
	{
		String BUTTON_LABEL = "Write merged ItemVault CSV";
		JButton writeMergedLogsFileButton = createStyledButton(BUTTON_LABEL, null);
		writeMergedLogsFileButton.addActionListener(e -> {
			writeMergedLogsFileButton.setEnabled(false); // Prevent spam clicking
			writeMergedLogsFileButton.setText("Exporting...");

			clientThread.invokeLater(() -> {
				vaultManager.writeMergedCsvFile();

				javax.swing.SwingUtilities.invokeLater(() -> {
					writeMergedLogsFileButton.setEnabled(true);
					writeMergedLogsFileButton.setText(BUTTON_LABEL);
				});
			});
		});
		writeMergedLogsFileButton.setToolTipText("Merge all internal itemvault log files and merge contents into a single CSV file");
		return writeMergedLogsFileButton;
	}

	private void writeMergedCsvFile()
	{
		log.info("Writing merged itemvault CSV file");
		vaultManager.writeMergedCsvFile();
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

			log.info("Selected file for custom template test: {}", fileToParse.getName());

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
						log.info("Successfully parsed attempt {}. Firing Discord webhook with custom template...", parsedDto.getAttemptId());
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

			log.info("Selected file for Discord test: {}", fileToParse.getName());

			executor.submit(() -> {
				try (Reader reader = new FileReader(fileToParse)) {
					ColosseumAttemptDTO parsedDto = gson.fromJson(reader, ColosseumAttemptDTO.class);

					if (parsedDto != null) {
						log.info("Successfully parsed attempt {}. Firing Discord webhook...", parsedDto.getAttemptId());
						String url = config.colosseumDiscordWebhookUrl();
						if (url == null || url.trim().isEmpty())
						{
							log.warn("Cannot broadcast: Webhook URL is empty.");
							return;
						}
						JsonObject payload;
						switch (config.colosseumWebhookFormat())
						{
							case CONCISE:
								payload = ColosseumConciseDiscordFormatter.buildPayload(parsedDto, config);
								break;
							case CUSTOM:
								payload = ColosseumCustomDiscordFormatter.buildPayload(parsedDto, config.colosseumCustomTemplate());
								break;
							default:
								payload = ColosseumDetailedDiscordFormatter.buildPayload(parsedDto, config);

						}
						discordWebhookService.sendWebhook(url, payload);
					} else {
						log.warn("Parsed DTO was null. Ensure the JSON structure matches ColosseumAttemptDTO.");
					}
				} catch (Exception ex) {
					log.error("Failed to read or parse the JSON file for Discord test.", ex);
				}
			});
		}
	}

	/**
	 * Helper method to open a directory in the native OS file explorer.
	 */
	private void openDirectory(String directoryPath) {
		executor.submit(() -> {
			File dir = new File(directoryPath);

			if (!dir.exists()) {
				log.warn("Directory does not exist, attempting to create: {}", directoryPath);
				if (!dir.mkdirs()) {
					log.error("Failed to create directory: {}", directoryPath);
					return;
				}
			}

			try {
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
					Desktop.getDesktop().open(dir);
				} else {
					log.warn("Desktop API is not supported on this platform. Cannot open file explorer.");
				}
			} catch (IOException ex) {
				log.error("Failed to open directory: {}", directoryPath, ex);
			}
		});
	}

	private JButton createStyledButton(String text, ActionListener actionListener) {
		JButton button = new JButton(text);
		button.setFocusable(false);
		button.setPreferredSize(new Dimension(0, 30)); // Standard height for panel buttons

		button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		button.setForeground(Color.WHITE);

		button.setBorder(new EmptyBorder(5, 5, 5, 5));

		button.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				button.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			}
		});

		if (actionListener != null) {
			button.addActionListener(actionListener);
		}

		return button;
	}
}