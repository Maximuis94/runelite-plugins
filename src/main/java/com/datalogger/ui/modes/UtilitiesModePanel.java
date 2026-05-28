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

package com.datalogger.ui.modes;

import com.datalogger.DataLoggerConfig;
import com.datalogger.constants.PluginConstants;
import static com.datalogger.constants.PluginConstants.COLOSSEUM_ROOT_DIR;
import static com.datalogger.constants.PluginConstants.COLOSSEUM_TRIALS_DIR;
import static com.datalogger.constants.PluginConstants.CONFIG_GROUP;
import static com.datalogger.constants.PluginConstants.GRAND_EXCHANGE_DIR;
import static com.datalogger.constants.PluginConstants.ITEM_VAULT_DIR;
import static com.datalogger.constants.PluginConstants.WEBHOOK_TEST_COOLDOWN_SECONDS_SUCCESS;
import com.datalogger.dto.ColosseumAttemptDTO;
import com.datalogger.dto.ColosseumWaveDTO;
import com.datalogger.framework.LogType;
import com.datalogger.loggers.ItemVaultLogger;
import com.datalogger.models.enums.ColosseumBroadcastMode;
import com.datalogger.models.enums.ColosseumWebhookFormatter;
import com.datalogger.models.enums.UIScrollSpeed;
import com.datalogger.services.DiscordWebhookService;
import com.datalogger.services.FileIOService;
import com.datalogger.services.GrandExchangeExportService;
import com.datalogger.services.itemvault.VaultManager;
import com.datalogger.ui.utils.Components;
import static com.datalogger.ui.utils.Components.createStyledButton;
import static com.datalogger.ui.utils.Components.showConfirmDialog;
import static com.datalogger.ui.utils.Util.openDirectory;
import com.datalogger.utils.migration.MigrationManager;
import com.datalogger.webhook.ColosseumCustomDiscordFormatter;
import com.datalogger.webhook.ColosseumDiscordBroadcaster;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;

@Slf4j
public class UtilitiesModePanel extends JPanel
{

	private final ScheduledExecutorService executor;
	private final DiscordWebhookService discordWebhookService;
	private final ColosseumDiscordBroadcaster colosseumDiscordBroadcaster;
	private final Gson gson;
	private final DataLoggerConfig config;
	private final ConfigManager configManager;
	private final VaultManager vaultManager;
	private final FileIOService fileIOService;
	private final ItemVaultLogger itemVaultLogger;
	private final MigrationManager migrationManager;
	private final GrandExchangeExportService geExportService;

	private Instant waitUntil;
	private UIScrollSpeed scrollSpeed = UIScrollSpeed.MEDIUM;
	protected JScrollBar scrollBar;

	@Inject
	public UtilitiesModePanel(ScheduledExecutorService executor, DiscordWebhookService discordWebhookService,
							  ColosseumDiscordBroadcaster colosseumDiscordBroadcaster, Gson gson,
							  DataLoggerConfig config, ConfigManager configManager, VaultManager vaultManager,
							  FileIOService fileIOService, ItemVaultLogger itemVaultLogger,
							  MigrationManager migrationManager, GrandExchangeExportService geExportService)
	{
		this.executor = executor;
		this.discordWebhookService = discordWebhookService;
		this.colosseumDiscordBroadcaster = colosseumDiscordBroadcaster;
		this.configManager = configManager;
		this.migrationManager = migrationManager;
		this.gson = gson;
		this.config = config;
		this.vaultManager = vaultManager;
		this.fileIOService = fileIOService;
		this.itemVaultLogger = itemVaultLogger;
		this.geExportService = geExportService;
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel contentWrapper = new JPanel();
		contentWrapper.setLayout(new BoxLayout(contentWrapper, BoxLayout.Y_AXIS));
		contentWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		int interPanelVerticalDistance = 20;

		contentWrapper.add(Box.createVerticalStrut(interPanelVerticalDistance));
		contentWrapper.add(buildDirectoriesPanel());
		contentWrapper.add(Box.createVerticalStrut(interPanelVerticalDistance));
		contentWrapper.add(buildExportsPanel());
		contentWrapper.add(Box.createVerticalStrut(interPanelVerticalDistance));
		contentWrapper.add(buildDebugPanel());

		JPanel migrationPanel = buildDataMigrationPanel();
		if (migrationPanel != null)
		{
			contentWrapper.add(Box.createVerticalStrut(interPanelVerticalDistance));
			contentWrapper.add(migrationPanel);
		}

		add(contentWrapper, BorderLayout.NORTH);
	}

	private JPanel buildDirectoriesPanel()
	{
		JPanel panel = Components.createTitledPanel("Directories", new GridLayout(0, 1, 0, 5));

		JButton colosseumButton = createStyledButton("Colosseum trial log", e -> openDirectory(LogType.COLOSSEUM.getLogDirectory().getAbsolutePath(), executor));
		colosseumButton.setToolTipText("Click to open " + COLOSSEUM_ROOT_DIR.getAbsolutePath());
		panel.add(colosseumButton);

		JButton itemVaultButton = createStyledButton("Item log", e -> openDirectory(LogType.ITEM_VAULT.getLogDirectory().getAbsolutePath(), executor));
		itemVaultButton.setToolTipText("Click to open " + ITEM_VAULT_DIR.getAbsolutePath());
		panel.add(itemVaultButton);

		JButton grandExchangeButton = createStyledButton("Grand Exchange log", e -> openDirectory(LogType.GRAND_EXCHANGE.getLogDirectory().getAbsolutePath(), executor));
		grandExchangeButton.setToolTipText("Click to open " + GRAND_EXCHANGE_DIR.getAbsolutePath());
		panel.add(grandExchangeButton);

		return panel;
	}

	private void exportAggregatedData()
	{
		int result = showConfirmDialog(
			this,"Confirm Export",
			"Are you sure you want to export your item data history?\n" +
				"This may overwrite existing files in .runelite/data-logger/items without warning."
		);

		if (result != JOptionPane.YES_OPTION) return;

		itemVaultLogger.exportAggregatedData(vaultManager.getItemChargeParsers());

		SwingUtilities.invokeLater(() -> {
			JOptionPane.showMessageDialog(this,
				"Successfully merged and exported item data to " + ITEM_VAULT_DIR.getAbsolutePath(),
				"Success", JOptionPane.INFORMATION_MESSAGE);
		});
	}

	private JButton exportAggregatedVaultButton()
	{
		JButton exportVaultSummary = createStyledButton("Export merged item data", e -> exportAggregatedData());
		exportVaultSummary.setToolTipText("Click to export and merge aggregated item data");
		return exportVaultSummary;
	}

	private void mergeWaveLogs()
	{
		executor.submit(() -> {
			this.fileIOService.mergeColosseumWaveLogs();
			SwingUtilities.invokeLater(() -> {
				JOptionPane.showMessageDialog(this,
					"Successfully merged and exported Colosseum trials to " + COLOSSEUM_ROOT_DIR.getAbsolutePath(),
					"Success", JOptionPane.INFORMATION_MESSAGE);
			});
		});
	}

	private void exportHistoricalGeData()
	{
		int result = showConfirmDialog(
			this,"Confirm Export",
			"Are you sure you want to export your internal GE history?\n" +
				"This may overwrite existing files in .runelite/data-logger/grand-exchange without warning."
			);

		if (result == JOptionPane.YES_OPTION)
		{
			geExportService.exportHistoricalData(
				() -> SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
					"GE Historical Export completed successfully to " + GRAND_EXCHANGE_DIR.getAbsolutePath(),
					"Success", JOptionPane.INFORMATION_MESSAGE)),
				() -> SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
					"Both GE export strategies are currently set to 'NONE'. Please change them in the plugin configuration.",
					"Export Aborted", JOptionPane.WARNING_MESSAGE))
			);
		}
	}

	private JButton mergedColosseumWaveLogsButton()
	{
		JButton exportWaveLogs = createStyledButton("Export merged wave log data", e -> mergeWaveLogs());
		exportWaveLogs.setToolTipText("Click to merge and export Colosseum Wave log data");
		return exportWaveLogs;
	}

	private JPanel buildExportsPanel()
	{
		JPanel panel = Components.createTitledPanel("Manual Exports", new GridLayout(0, 1, 0, 5));

		panel.add(exportAggregatedVaultButton());
		panel.add(mergedColosseumWaveLogsButton());

		JButton exportHistoricalGeBtn = createStyledButton("Export Historical GE Data", e -> exportHistoricalGeData());
		exportHistoricalGeBtn.setToolTipText("Exports all accounts' internal GE history based on configured File Strategies.");
		panel.add(exportHistoricalGeBtn);

		return panel;
	}

	/**
	 * Identify whether or not data can be migrated.
	 */
	private boolean hasLegacyColosseumTrials()
	{
		File[] files = COLOSSEUM_ROOT_DIR.listFiles();
		if (files == null) return false;

		for (File nextDir : files)
		{
			if (migrationManager.isMigrateableLoggedTrialDir(nextDir))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Panel with buttons to manually migrate legacy data. Buttons only appear if they are relevant, i.e., if legacy data may exist.
	 */
	private JPanel buildDataMigrationPanel()
	{
		if (hasLegacyColosseumTrials())
		{
			JPanel panel;
			JButton convertBtn;
			panel = Components.createTitledPanel("Data migration", new GridLayout(0, 1, 0, 5));
			convertBtn = createStyledButton("Migrate logs", e -> initiateColosseumTrialMigration());
			convertBtn.setToolTipText("Migrate logged Colosseum trials to a newer version, if possible.");
			panel.add(convertBtn);

			return panel;
		}
		return null;
	}

	private JPanel buildDebugPanel()
	{
		JPanel panel = Components.createTitledPanel("Debug & Testing", new BorderLayout(0, 5));

		JPanel controlsPanel = new JPanel(new GridLayout(0, 1, 0, 5));
		controlsPanel.setOpaque(false);

		JLabel instructionsLabel = new JLabel("<html><div style='width: 200px; color: white; padding-bottom: 2px;'>Test various broadcasting options<br>here using existing trial submissions</div></html>");

		JComboBox<String> trialSelector = Components.createComboBox();
		trialSelector.addItem("Select Trial...");
		trialSelector.setToolTipText("Select a logged trial you wish to broadcast");

		executor.submit(() -> {
			File dir = PluginConstants.COLOSSEUM_TRIALS_DIR;
			if (dir.exists() && dir.isDirectory())
			{
				File[] subdirs = dir.listFiles(File::isDirectory);
				if (subdirs != null)
				{
					Arrays.sort(subdirs, Comparator.comparingLong(File::lastModified).reversed());
					SwingUtilities.invokeLater(() -> {
						for (File subdir : subdirs)
						{
							trialSelector.addItem(subdir.getName());
						}
					});
				}
			}
		});

		JComboBox<ColosseumBroadcastMode> modeSelector = Components.createComboBox(ColosseumBroadcastMode.values());
		modeSelector.setToolTipText("Select a broadcast mode you wish to apply");

		controlsPanel.add(trialSelector);
		controlsPanel.add(modeSelector);
		JPanel topWrapper = new JPanel(new BorderLayout(0, 5));
		topWrapper.setOpaque(false);
		topWrapper.add(instructionsLabel, BorderLayout.NORTH);
		topWrapper.add(controlsPanel, BorderLayout.CENTER);
		panel.add(topWrapper, BorderLayout.NORTH);

		JTextArea customFormatInput = new JTextArea(config.colosseumCustomTemplate());
		customFormatInput.setLineWrap(true);
		customFormatInput.setWrapStyleWord(true);
		customFormatInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		customFormatInput.setForeground(Color.WHITE);
		customFormatInput.setCaretColor(Color.WHITE);
		customFormatInput.setBorder(new EmptyBorder(5, 5, 5, 5));
		customFormatInput.setToolTipText("Edit custom formatting here to test changes before saving to config.");

		JPanel inputPanel = new JPanel(new BorderLayout(0, 5));
		inputPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JScrollPane scrollPane = Components.wrapWithRuneLiteScrollbar(customFormatInput);
		scrollPane.setPreferredSize(new Dimension(0, 270));
		scrollBar = scrollPane.getVerticalScrollBar();
		setScrollSpeed(scrollSpeed);
		inputPanel.add(scrollPane, BorderLayout.NORTH);

		// 4. Test Button
		JButton testBtn = createStyledButton("Test Colosseum template", e -> {
			String selectedTrial = (String) trialSelector.getSelectedItem();
			if (selectedTrial == null || selectedTrial.equals("Select Trial..."))
			{
				JOptionPane.showMessageDialog(this, "Please select a Colosseum trial first.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			ColosseumBroadcastMode selectedMode = (ColosseumBroadcastMode) modeSelector.getSelectedItem();

			if (selectedMode == null)
			{
				log.debug("Unable to extract selectedMode from dropdown menu...");
			}
			else if (selectedMode == ColosseumBroadcastMode.SKIP)
			{
				log.debug("Broadcasting a Colosseum run on the don't broadcast mode-- i.e., aborting...");
			}
			else
			{
				log.debug("Broadcasting a debug Colosseum run with mode={}", selectedMode.getName());
				String templateText = customFormatInput.getText();
				sendTestDiscordMessage(selectedTrial, selectedMode, templateText);
			}

		});
		testBtn.setBorder(new EmptyBorder(10, 5, 10, 5));
		testBtn.setToolTipText("Click to submit a test using the configurations above. The header and footer will indicate it is a test submission.");

		panel.add(inputPanel, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel(new GridLayout(3, 0, 12, 5));
		buttonPanel.add(testBtn, BorderLayout.NORTH);
		JButton saveTemplateBtn = createStyledButton("Save template", e -> {
			String customTemplateText = customFormatInput.getText().strip();
			configManager.setConfiguration(CONFIG_GROUP, "colosseumCustomTemplate", customTemplateText);
			SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
				"Saved custom template to configurations",
				"Success", JOptionPane.INFORMATION_MESSAGE));
		});
		saveTemplateBtn.setToolTipText("Save the content of the custom textfield to the plugin configurations ");
		buttonPanel.add(saveTemplateBtn, BorderLayout.CENTER);

		JButton resetTemplateBtn = createStyledButton("Reset template", e -> {
			customFormatInput.setText(config.colosseumCustomTemplate());
		});
		resetTemplateBtn.setBorder(new EmptyBorder(5, 15, 5, 5));
		resetTemplateBtn.setToolTipText("Resets the content of the textfield to the current configuration");
		buttonPanel.add(resetTemplateBtn, BorderLayout.SOUTH);

		buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
//		customFormatInput.setForeground(Color.WHITE);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * Invokes the migrateColosseumTrialsV0V1 of the MigrationManager
	 */
	private void initiateColosseumTrialMigration()
	{
		executor.submit(() -> {
			try
			{
				int nMigrated = migrationManager.migrateColosseumTrialsV0V1();
				if (nMigrated > 0)
				{
					SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(this,
							"Converted a total of " + nMigrated + " trials. The converted trials were transferred to " + COLOSSEUM_TRIALS_DIR.getAbsolutePath(),
							"Success", JOptionPane.INFORMATION_MESSAGE);
					});
				}
				else {
					SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(this,
							"Did not encounter any new trials.",
							"Success", JOptionPane.INFORMATION_MESSAGE);
					});
				}
			}
			catch (Exception e)
			{
				log.error("Error occurred while initiating Colosseum Trial Migration", e);
				SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(this,
						"An error occurred during migration. Check your RuneLite logs.",
						"Error", JOptionPane.ERROR_MESSAGE);
				});
			}
		});
	}

	private void sendTestDiscordMessage(String trialId, ColosseumBroadcastMode mode, String templateText)
	{
		Instant now = Instant.now();
		if (waitUntil != null && now.isBefore(waitUntil))
		{
			long secondsLeft = Duration.between(now, waitUntil).getSeconds();
			JOptionPane.showMessageDialog(this, "Please wait " + Math.max(secondsLeft, 1) + " seconds.", "Cooldown", JOptionPane.WARNING_MESSAGE);
			return;
		}

		File trialDir = new File(PluginConstants.COLOSSEUM_TRIALS_DIR, trialId);
		File[] jsonFiles = trialDir.listFiles((d, name) -> name.endsWith(".json"));

		if (jsonFiles == null || jsonFiles.length == 0)
		{
			JOptionPane.showMessageDialog(this, "No valid wave-log JSON found for this trial.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		File fileToParse = jsonFiles[0];
		log.debug("Selected file for Discord test: {}", fileToParse.getName());

		executor.submit(() -> {
			try (Reader reader = new FileReader(fileToParse))
			{
				ColosseumAttemptDTO parsedDto = gson.fromJson(reader, ColosseumAttemptDTO.class);

				if (parsedDto != null)
				{
					log.debug("Successfully parsed attempt {}. Firing Discord webhook...", parsedDto.getAttemptId());
					String url = config.colosseumDiscordWebhookUrl();
					if (url == null || url.trim().isEmpty())
					{
						log.warn("Cannot broadcast: Webhook URL is empty.");
						return;
					}

					if (mode.getFormatter() == ColosseumWebhookFormatter.CUSTOM)
					{

						JsonObject payload = ColosseumCustomDiscordFormatter.buildPayload(parsedDto, templateText, true);

						if (mode.isAttachScreenshot())
						{
							List<ColosseumWaveDTO> waves = parsedDto.getWaves();
							ColosseumWaveDTO finalWave = waves.get(waves.size() - 1);

							File screenshotFile = colosseumDiscordBroadcaster.getBroadcastScreenshotFile(finalWave, parsedDto.getAttemptId());

							discordWebhookService.queueWebhook(url, payload, screenshotFile);
						}
						else
						{
							discordWebhookService.sendWebhook(url, payload);
						}

					}
					else
					{
						colosseumDiscordBroadcaster.broadcastToDiscordDebug(parsedDto, mode);
					}

					waitUntil = Instant.now().plusSeconds(WEBHOOK_TEST_COOLDOWN_SECONDS_SUCCESS);
				}
				else
				{
					log.warn("Parsed DTO was null. Ensure the JSON structure matches ColosseumAttemptDTO.");
				}
			}
			catch (Exception ex)
			{
				log.error("Failed to read or parse the JSON file for Discord test.", ex);
			}
		});
	}

	/**
	 * Set the scroll speed of the inner scrollbar of this panel to a value that corresponds with the given speed
	 */
	public void setScrollSpeed(UIScrollSpeed scrollSpeed)
	{
		this.scrollSpeed = scrollSpeed;
		switch (scrollSpeed)
		{
			case LOW:
				scrollBar.setUnitIncrement(30);
				break;
			case MEDIUM:
				scrollBar.setUnitIncrement(60);
				break;
			case HIGH:
				scrollBar.setUnitIncrement(120);
				break;
		}
	}
}