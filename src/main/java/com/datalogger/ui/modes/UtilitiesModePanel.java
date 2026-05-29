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

import static com.datalogger.constants.PluginConstants.COLOSSEUM_ROOT_DIR;
import static com.datalogger.constants.PluginConstants.COLOSSEUM_TRIALS_DIR;
import static com.datalogger.constants.PluginConstants.GRAND_EXCHANGE_DIR;
import static com.datalogger.constants.PluginConstants.ITEM_VAULT_DIR;
import com.datalogger.loggers.ItemVaultLogger;
import com.datalogger.models.enums.Directory;
import com.datalogger.services.AccountHashMapper;
import com.datalogger.services.FileIOService;
import com.datalogger.services.GrandExchangeExportService;
import com.datalogger.services.itemvault.VaultManager;
import com.datalogger.ui.utils.Components;
import static com.datalogger.ui.utils.Components.createStyledButton;
import static com.datalogger.ui.utils.Components.showConfirmDialog;
import com.datalogger.ui.utils.Models.AccountItem;
import static com.datalogger.ui.utils.Util.openDirectory;
import com.datalogger.utils.migration.MigrationManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;

@Slf4j
public class UtilitiesModePanel extends JPanel
{
	private final ScheduledExecutorService executor;
	private final VaultManager vaultManager;
	private final FileIOService fileIOService;
	private final ItemVaultLogger itemVaultLogger;
	private final MigrationManager migrationManager;
	private final GrandExchangeExportService geExportService;
	private final AccountHashMapper accountHashMapper;

	@Inject
	public UtilitiesModePanel(ScheduledExecutorService executor, VaultManager vaultManager,
							  FileIOService fileIOService, ItemVaultLogger itemVaultLogger,
							  MigrationManager migrationManager, GrandExchangeExportService geExportService, AccountHashMapper accountHashMapper)
	{
		this.executor = executor;
		this.migrationManager = migrationManager;
		this.vaultManager = vaultManager;
		this.fileIOService = fileIOService;
		this.itemVaultLogger = itemVaultLogger;
		this.geExportService = geExportService;
		this.accountHashMapper = accountHashMapper;

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
		JPanel panel = Components.createTitledPanel("Directories", new BorderLayout(0, 5));

		JPanel controlsPanel = new JPanel(new GridLayout(0, 1, 0, 5));
		controlsPanel.setOpaque(false);

		JComboBox<Directory> directorySelector = Components.createComboBox(Directory.values());
		directorySelector.setToolTipText("Select the type of directory you want to open.");

		executor.schedule(() -> {

			JComboBox<AccountItem> accountSelector = Components.createComboBox();
			accountSelector.setToolTipText("Select an account to open its specific directory.");
			accountSelector.addItem(new AccountItem("Select Account...", -1L));

			for (String accountName : accountHashMapper.getAccountNames())
				accountSelector.addItem(new AccountItem(accountName, -1L));

			directorySelector.addActionListener(e -> {
				Directory selected = (Directory) directorySelector.getSelectedItem();
				if (selected != null) {
					accountSelector.setEnabled(selected.isAccountSpecificDirectories());
				}
			});

			controlsPanel.add(directorySelector);
			controlsPanel.add(accountSelector);
			log.debug("Accounts: {}", accountHashMapper.getAccountNames());
			JButton openBtn = createStyledButton("Open directory", e -> {
				Directory selectedDir = (Directory) directorySelector.getSelectedItem();
				if (selectedDir != null)
				{
					boolean isInternal = selectedDir.toString().startsWith("Internal");
					File targetDirectory = selectedDir.getDirectory();

					// Append the account-specific subfolder if required
					if (selectedDir.isAccountSpecificDirectories())
					{
						AccountItem selectedAccount = (AccountItem) accountSelector.getSelectedItem();

						if (selectedAccount != null)
						{
							long selectedHash = accountHashMapper.getAccountHashByAccountName(selectedAccount.toString());
							log.debug("Selected account: {} hash={} Internal={} directory={}", selectedAccount, selectedHash, isInternal, selectedDir);
							if (isInternal) {
								// Internal folders use the Account Hash
								targetDirectory = new File(targetDirectory, String.valueOf(selectedHash));
							} else {
								// External folders use the Account Name
								targetDirectory = new File(targetDirectory, selectedAccount.toString().toLowerCase());
							}
						}
						log.debug("targetDirectory={}", targetDirectory);
					}

					// Open the specific account folder if it exists, otherwise open the parent folder
					if (targetDirectory != null && targetDirectory.exists()) {
						openDirectory(targetDirectory.getAbsolutePath(), executor);
					} else if (targetDirectory != null) {
						File parentDir = selectedDir.getDirectory();
						openDirectory(parentDir.getAbsolutePath(), executor);
					}
				}
			});


			panel.add(controlsPanel, BorderLayout.CENTER);
			panel.add(openBtn, BorderLayout.SOUTH);
		}, 5000, TimeUnit.MILLISECONDS);

		return panel;
	}

	private void populateAccounts(JComboBox<AccountItem> accountSelector)
	{
		accountSelector.removeAllItems();
		accountSelector.addItem(new AccountItem("Select Account...", -1L));

		List<AccountItem> items = new ArrayList<>();

		// Fetch all known hashes directly from your mapper
		for (Long hash : accountHashMapper.getAccountHashes()) {
			String name = accountHashMapper.getAccountName(hash);
			if (name == null || name.isEmpty()) {
				name = String.valueOf(hash);
			}
			items.add(new AccountItem(name, hash));
		}

		// Sort alphabetically and add to combobox
		items.sort(Comparator.comparing(AccountItem::getName, String.CASE_INSENSITIVE_ORDER));
		for (AccountItem item : items) {
			accountSelector.addItem(item);
		}
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
}