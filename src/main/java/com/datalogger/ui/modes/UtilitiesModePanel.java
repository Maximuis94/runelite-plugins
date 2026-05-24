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
import static com.datalogger.constants.PluginConstants.WEBHOOK_TEST_COOLDOWN_SECONDS_SUCCESS;
import com.datalogger.dto.ColosseumAttemptDTO;
import com.datalogger.dto.ColosseumWaveDTO;
import com.datalogger.framework.LogType;
import com.datalogger.loggers.ItemVaultLogger;
import com.datalogger.models.enums.ColosseumBroadcastMode;
import com.datalogger.models.enums.ColosseumWebhookFormatter;
import com.datalogger.models.enums.UIScrollSpeed;
import com.datalogger.models.itemvault.ItemBundle;
import com.datalogger.models.supplytracker.ValuedItemStack;
import com.datalogger.services.DiscordWebhookService;
import com.datalogger.services.FileIOService;
import com.datalogger.services.itemvault.VaultManager;
import com.datalogger.ui.utils.Components;
import static com.datalogger.ui.utils.Components.createStyledButton;
import static com.datalogger.ui.utils.Util.openDirectory;
import com.datalogger.utils.migration.MigrationManager;
import com.datalogger.utils.migration.colosseumtrial.ColosseumTrialMigrationCsvV0V1;
import com.datalogger.utils.migration.colosseumtrial.ColosseumTrialMigrationV0V1;
import com.datalogger.utils.migration.colosseumtrial.models.ColosseumAttemptDtoV0;
import com.datalogger.utils.migration.colosseumtrial.models.ColosseumAttemptDtoV1;
import com.datalogger.utils.migration.colosseumtrial.models.ColosseumWaveDtoV0;
import com.datalogger.utils.migration.colosseumtrial.models.ColosseumWaveDtoV1;
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
import java.io.IOException;
import java.io.Reader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;

@Slf4j
public class UtilitiesModePanel extends JPanel {

	private final ScheduledExecutorService executor;
	private final DiscordWebhookService discordWebhookService;
	private final ColosseumDiscordBroadcaster colosseumDiscordBroadcaster;
	private final Gson gson;
	private final DataLoggerConfig config;
	private final VaultManager vaultManager;
	private final ClientThread clientThread;
	private final FileIOService fileIOService;
	private final ItemVaultLogger itemVaultLogger;
	private final ItemManager itemManager;
	private final ColosseumTrialMigrationV0V1 colosseumMigration;
	private final ColosseumTrialMigrationCsvV0V1 colosseumCsvMigration;
	private final MigrationManager migrationManager;

	private Instant waitUntil;
	private UIScrollSpeed scrollSpeed = UIScrollSpeed.MEDIUM;
	protected JScrollBar scrollBar;

	@Inject
	public UtilitiesModePanel(ScheduledExecutorService executor, DiscordWebhookService discordWebhookService,
							  ColosseumDiscordBroadcaster colosseumDiscordBroadcaster, Gson gson,
							  DataLoggerConfig config, VaultManager vaultManager, ClientThread clientThread,
							  FileIOService fileIOService, ItemVaultLogger itemVaultLogger, ItemManager itemManager,
							  ColosseumTrialMigrationV0V1 colosseumMigration, ColosseumTrialMigrationCsvV0V1 colosseumCsvMigration,
							  MigrationManager migrationManager) {
		this.executor = executor;
		this.discordWebhookService = discordWebhookService;
		this.colosseumDiscordBroadcaster = colosseumDiscordBroadcaster;
		this.colosseumMigration = colosseumMigration;
		this.colosseumCsvMigration = colosseumCsvMigration;
		this.migrationManager = migrationManager;
		this.gson = gson;
		this.config = config;
		this.vaultManager = vaultManager;
		this.clientThread = clientThread;
		this.fileIOService = fileIOService;
		this.itemVaultLogger = itemVaultLogger;
		this.itemManager = itemManager;

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
		contentWrapper.add(Box.createVerticalStrut(interPanelVerticalDistance));
		contentWrapper.add(buildDataMigrationPanel());

		add(contentWrapper, BorderLayout.NORTH);
	}

	private JPanel buildDirectoriesPanel()
	{
		JPanel panel = Components.createTitledPanel("Directories", new GridLayout(0, 1, 0, 5));

		JButton colosseumButton = createStyledButton("Colosseum log directory", e -> openDirectory(LogType.COLOSSEUM.getLogDirectory().getAbsolutePath(), executor));
		colosseumButton.setToolTipText("Click to open the Colosseum log output directory");
		panel.add(colosseumButton);

		JButton itemVaultButton = createStyledButton("Item Vault directory", e -> openDirectory(LogType.ITEM_VAULT.getLogDirectory().getAbsolutePath(), executor));
		itemVaultButton.setToolTipText("Click to open the Item Vault output directory");
		panel.add(itemVaultButton);

		JButton grandExchangeButton = createStyledButton("Grand Exchange directory", e -> openDirectory(LogType.GRAND_EXCHANGE.getLogDirectory().getAbsolutePath(), executor));
		grandExchangeButton.setToolTipText("Click to open the Grand Exchange output directory");
		panel.add(grandExchangeButton);

		return panel;
	}

	private JButton exportAggregatedVaultButton()
	{
		JButton exportVaultSummary = createStyledButton("Export merged item data", e -> vaultManager.writeMergedCsvFile());
		exportVaultSummary.setToolTipText("Click to export and merge aggregated item data");
		return exportVaultSummary;
	}

	private JButton mergedColosseumWaveLogsButton()
	{
		JButton exportWaveLogs = createStyledButton("Export merged wave log data", e -> this.fileIOService.mergeColosseumWaveLogs());
		exportWaveLogs.setToolTipText("Click to merge and export Colosseum Wave log data");
		return exportWaveLogs;
	}

	private JPanel buildExportsPanel()
	{
		JPanel panel = Components.createTitledPanel("Manual Exports", new GridLayout(0, 1, 0, 5));

		panel.add(exportAggregatedVaultButton());
		panel.add(mergedColosseumWaveLogsButton());

		return panel;
	}

	private JPanel buildDataMigrationPanel()
	{
		JPanel panel = Components.createTitledPanel("Data migration", new GridLayout(0, 1, 0, 5));

		JButton convertBtn = createStyledButton("Migrate logs", e -> initiateColosseumTrialMigration());
		convertBtn.setToolTipText("Migrate logged Colosseum trials to a newer version, if possible.");
		panel.add(convertBtn);

		return panel;
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
			if (dir.exists() && dir.isDirectory()) {
				File[] subdirs = dir.listFiles(File::isDirectory);
				if (subdirs != null) {
					Arrays.sort(subdirs, Comparator.comparingLong(File::lastModified).reversed());
					SwingUtilities.invokeLater(() -> {
						for (File subdir : subdirs) {
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

		JScrollPane scrollPane = Components.createScrollPane(customFormatInput);
		scrollPane.setPreferredSize(new Dimension(0, 250));
		scrollBar = scrollPane.getVerticalScrollBar();
		setScrollSpeed(scrollSpeed);

		panel.add(scrollPane, BorderLayout.CENTER);

		// 4. Test Button
		JButton testBtn = createStyledButton("Test Colosseum Discord webhook", e -> {
			String selectedTrial = (String) trialSelector.getSelectedItem();
			if (selectedTrial == null || selectedTrial.equals("Select Trial...")) {
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
		testBtn.setToolTipText("Click to submit a test using the configurations above. The header and footer will indicate it is a test submission.");
		panel.add(testBtn, BorderLayout.SOUTH);

		return panel;
	}

	private ColosseumAttemptDtoV1 performMigration(File file) throws IOException
	{
		// 1. Parse V0 file
		ColosseumAttemptDtoV0 v0;
		try (FileReader reader = new FileReader(file)) {
			v0 = gson.fromJson(reader, ColosseumAttemptDtoV0.class);
		}

		if (v0 == null) return null;

		final Map<Integer, Integer> priceCache = new HashMap<>();
		final Set<Integer> itemIds = new HashSet<>();

		// Collect IDs
		if (v0.getWaves() != null) {
			for (ColosseumWaveDtoV0 wave : v0.getWaves()) {
				if (wave.getEarnedLoot() != null) {
					for (ItemBundle bundle : wave.getEarnedLoot()) {
						itemIds.add(bundle.getItemId());
					}
				}
			}
		}

		for (Integer itemId : itemIds) {
			CompletableFuture<Integer> futurePrice = new CompletableFuture<>();
			clientThread.invoke(() -> {
				try {
					futurePrice.complete(itemManager.getItemPrice(itemId));
				} catch (Exception e) {
					futurePrice.completeExceptionally(e);
				}
			});

			try {
				priceCache.put(itemId, futurePrice.join());
			} catch (Exception e) {
				log.error("Failed to fetch price for item {}", itemId, e);
				priceCache.put(itemId, 0);
			}
		}

		// 2. Setup accumulators for aggregate rewards
		Map<String, ValuedItemStack> totalRewardsMap = new LinkedHashMap<>();
		int totalRewardsValue = 0;
		List<ColosseumWaveDtoV1> newWaves = new ArrayList<>();

		// Track active modifiers as a list, ordered by selection
		List<String> activeModifiers = new ArrayList<>();

		if (v0.getWaves() != null) {
			for (ColosseumWaveDtoV0 waveV0 : v0.getWaves()) {

				// --- Loot Aggregation Logic ---
				ItemBundle singleLoot = null;
				if (waveV0.getEarnedLoot() != null && !waveV0.getEarnedLoot().isEmpty()) {
					// Waves 1-11: Take the first entry. Wave 12: Sum all items.
					if (waveV0.getWave() < 12) {
						singleLoot = waveV0.getEarnedLoot().get(0);
						aggregateReward(totalRewardsMap, singleLoot, priceCache);
					} else {
						for (ItemBundle bundle : waveV0.getEarnedLoot()) {
							aggregateReward(totalRewardsMap, bundle, priceCache);
						}
					}
				}

				// --- Modifier Tracking ---
				if (waveV0.getChosenModifier() != null && !waveV0.getChosenModifier().isEmpty()) {
					activeModifiers.add(waveV0.getChosenModifier());
				}

				// --- Convert Wave (V0 to V1) ---
				newWaves.add(convertWave(waveV0, activeModifiers, singleLoot));
			}
		}

		// 3. Build Final V1 DTO
		return ColosseumAttemptDtoV1.builder()
			.attemptId(String.valueOf(v0.getAttemptId()))
			.timestamp(v0.getTimestamp())
			.accountName(v0.getWaves().get(0).getAccountName()) // Fallback to first wave account
			.result(v0.getResult())
			.rewardsValue(totalRewardsValue)
			.rewards(totalRewardsMap)
			.totalGlory(v0.getTotalGlory())
			.totalTime(calculateTotalTime(v0))
			.activeModifiers(activeModifiers)
			.waves(newWaves)
			.build();
	}

	private void aggregateReward(
		Map<String, ValuedItemStack> totalRewardsMap,
		ItemBundle bundle,
		Map<Integer, Integer> priceCache
	) {
		String name = bundle.getItemName();
		int qty = bundle.getQuantity();

		// Retrieve pre-fetched price from the map, default to 0 if not found
		int price = priceCache.getOrDefault(bundle.getItemId(), 0);
		int totalValue = price * qty;

		ValuedItemStack existing = totalRewardsMap.getOrDefault(name, new ValuedItemStack(0, 0));
		totalRewardsMap.put(name, new ValuedItemStack(
			existing.getCount() + qty,
			existing.getTotalValueInGp() + totalValue
		));
	}

	public long calculateLootValue(List<ItemBundle> lootList, ItemManager itemManager) {
		if (lootList == null || lootList.isEmpty()) {
			return 0L;
		}

		long totalValue = 0L;
		for (ItemBundle item : lootList) {
			// getItemPrice handles the GE cache lookup efficiently
			int price = itemManager.getItemPrice(item.getItemId());
			totalValue += (long) price * item.getQuantity();
		}
		return totalValue;
	}

	private ColosseumWaveDtoV1 convertWave(ColosseumWaveDtoV0 waveV0, List<String> activeModifiers, ItemBundle v1Loot)
	{
		return ColosseumWaveDtoV1.builder()
			.wave(waveV0.getWave())
			.status(waveV0.getStatus())
			.accountName(waveV0.getAccountName())
			.tag(waveV0.getTag())
			.earnedLoot(v1Loot)
			.lootValue(0) // Default for migrated waves
			.modifierChoices(waveV0.getModifierChoices() != null ? new ArrayList<>(waveV0.getModifierChoices()) : new ArrayList<>())
			.chosenModifier(waveV0.getChosenModifier())
			.activeModifiers(activeModifiers != null ? activeModifiers : Collections.emptyList())
			.timeTaken(formatTime(waveV0.getTimeTaken()))
			.speedBonus(waveV0.getSpeedBonus())
			.damageTaken(waveV0.getDamageTaken())
			.damageBonus(waveV0.getDamageBonus())
			.modifierGlory(waveV0.getModifierGlory())
			.completionBonus(waveV0.getCompletionBonus())
			.waveGlory(waveV0.getWaveGlory())
			.totalGlory(waveV0.getTotalGlory())
			.totalTimeTaken(formatTime(waveV0.getTotalTimeTaken()))

			// Transfer map coordinates directly
			.serpentShamanSpawnX(waveV0.getSerpentShamanSpawnX())
			.serpentShamanSpawnY(waveV0.getSerpentShamanSpawnY())
			.javelinColossusSpawnAX(waveV0.getJavelinColossusSpawnAX())
			.javelinColossusSpawnAY(waveV0.getJavelinColossusSpawnAY())
			.javelinColossusSpawnBX(waveV0.getJavelinColossusSpawnBX())
			.javelinColossusSpawnBY(waveV0.getJavelinColossusSpawnBY())
			.manticoreSpawnAX(waveV0.getManticoreSpawnAX())
			.manticoreSpawnAY(waveV0.getManticoreSpawnAY())
			.manticoreSequenceA(waveV0.getManticoreSequenceA())
			.manticoreSpawnBX(waveV0.getManticoreSpawnBX())
			.manticoreSpawnBY(waveV0.getManticoreSpawnBY())
			.manticoreSequenceB(waveV0.getManticoreSequenceB())
			.shockwaveColossusSpawnAX(waveV0.getShockwaveColossusSpawnAX())
			.shockwaveColossusSpawnAY(waveV0.getShockwaveColossusSpawnAY())
			.shockwaveColossusSpawnBX(waveV0.getShockwaveColossusSpawnBX())
			.shockwaveColossusSpawnBY(waveV0.getShockwaveColossusSpawnBY())
			.jaguarWarriorReinforcementsSpawnX(waveV0.getJaguarWarriorReinforcementsSpawnX())
			.jaguarWarriorReinforcementsSpawnY(waveV0.getJaguarWarriorReinforcementsSpawnY())
			.serpentShamanReinforcementsSpawnX(waveV0.getSerpentShamanReinforcementsSpawnX())
			.serpentShamanReinforcementsSpawnY(waveV0.getSerpentShamanReinforcementsSpawnY())
			.minotaurReinforcementsSpawnX(waveV0.getMinotaurReinforcementsSpawnX())
			.minotaurReinforcementsSpawnY(waveV0.getMinotaurReinforcementsSpawnY())
			.build();
	}

	private double calculateTotalTime(ColosseumAttemptDtoV0 v0) {
		return v0.getWaves().stream()
			.mapToDouble(ColosseumWaveDtoV0::getTimeTaken)
			.sum();
	}

	/**
	 * Invokes the migrateColosseumTrialsV0V1 of the MigrationManager
	 */
	private void initiateColosseumTrialMigration()
	{
		executor.submit(() -> {
			try {
				migrationManager.migrateColosseumTrialsV0V1();
				SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(this,
						"Migration process completed successfully.",
						"Success", JOptionPane.INFORMATION_MESSAGE);
				});
			} catch (Exception e) {
				log.error("Error occurred while initiating Colosseum Trial Migration", e);
				SwingUtilities.invokeLater(() -> {
					JOptionPane.showMessageDialog(this,
						"An error occurred during migration. Check your RuneLite logs.",
						"Error", JOptionPane.ERROR_MESSAGE);
				});
			}
		});
	}

	private void selectAndConvertFile() {
		JFileChooser fileChooser = new JFileChooser(PluginConstants.COLOSSEUM_ROOT_DIR);
		fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();

			// Offload the heavy work and I/O to the background executor
			executor.submit(() -> {
//				boolean success = colosseumMigration.migrate(selectedFile);
				SwingUtilities.invokeLater(() -> {
					boolean success = colosseumCsvMigration.migrate(selectedFile);

					// Return to the Event Dispatch Thread to update the UI
					if (success) {
						JOptionPane.showMessageDialog(this,
							"File migrated successfully: " + selectedFile.getName(),
							"Success", JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(this,
							"Migration failed. Check the RuneLite client logs.",
							"Error", JOptionPane.ERROR_MESSAGE);
					}
				});
			});
		}
	}

	private double formatTime(double time) {
		return Math.round(time * 10.0) / 10.0;
	}

	private void sendTestDiscordMessage(String trialId, ColosseumBroadcastMode mode, String templateText)
	{
		Instant now = Instant.now();
		if (waitUntil != null && now.isBefore(waitUntil)) {
			long secondsLeft = Duration.between(now, waitUntil).getSeconds();
			JOptionPane.showMessageDialog(this, "Please wait " + Math.max(secondsLeft, 1) + " seconds.", "Cooldown", JOptionPane.WARNING_MESSAGE);
			return;
		}

		File trialDir = new File(PluginConstants.COLOSSEUM_TRIALS_DIR, trialId);
		File[] jsonFiles = trialDir.listFiles((d, name) -> name.endsWith(".json"));

		if (jsonFiles == null || jsonFiles.length == 0) {
			JOptionPane.showMessageDialog(this, "No valid wave-log JSON found for this trial.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		File fileToParse = jsonFiles[0];
		log.debug("Selected file for Discord test: {}", fileToParse.getName());

		executor.submit(() -> {
			try (Reader reader = new FileReader(fileToParse)) {
				ColosseumAttemptDTO parsedDto = gson.fromJson(reader, ColosseumAttemptDTO.class);

				if (parsedDto != null) {
					log.debug("Successfully parsed attempt {}. Firing Discord webhook...", parsedDto.getAttemptId());
					String url = config.colosseumDiscordWebhookUrl();
					if (url == null || url.trim().isEmpty()) {
						log.warn("Cannot broadcast: Webhook URL is empty.");
						return;
					}

					if (mode.getFormatter() == ColosseumWebhookFormatter.CUSTOM) {

						JsonObject payload = ColosseumCustomDiscordFormatter.buildPayload(parsedDto, templateText, true);

						if (mode.isAttachScreenshot()) {
							List<ColosseumWaveDTO> waves = parsedDto.getWaves();
							ColosseumWaveDTO finalWave = waves.get(waves.size() - 1);

							File screenshotFile = colosseumDiscordBroadcaster.getBroadcastScreenshotFile(finalWave, parsedDto.getAttemptId());

							discordWebhookService.queueWebhook(url, payload, screenshotFile);
						} else {
							discordWebhookService.sendWebhook(url, payload);
						}

					} else {
						colosseumDiscordBroadcaster.broadcastToDiscordDebug(parsedDto, mode);
					}

					waitUntil = Instant.now().plusSeconds(WEBHOOK_TEST_COOLDOWN_SECONDS_SUCCESS);
				} else {
					log.warn("Parsed DTO was null. Ensure the JSON structure matches ColosseumAttemptDTO.");
				}
			} catch (Exception ex) {
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
				scrollBar.setUnitIncrement(8);
				break;
			case MEDIUM:
				scrollBar.setUnitIncrement(16);
				break;
			case HIGH:
				scrollBar.setUnitIncrement(32);
				break;
		}
	}
}