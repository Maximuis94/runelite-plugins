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
import com.datalogger.models.enums.UIScrollSpeed;
import com.datalogger.models.itemvault.BankedItem;
import com.datalogger.services.AccountHashMapper;
import com.datalogger.ui.utils.Components;
import com.datalogger.ui.utils.Models.AccountItem;
import com.datalogger.ui.utils.table.ItemTable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.QuantityFormatter;

@Slf4j
public class ItemsModePanel extends JPanel
{
	private final AccountHashMapper accountHashMapper;
	private final Gson gson;
	private final ScheduledExecutorService executor;
	private final ItemManager itemManager;
	private final ClientThread clientThread;
	private final DataLoggerConfig config;

	private final JComboBox<AccountItem> accountFilter;
	private final JComboBox<String> sourceFilter;

	private final JTextField accountFilterInput;
	private final JTextField sourceFilterInput;
	private final JTextField itemNameFilterInput;

	private final JPanel statsContainer;
	private final JPanel contentPanel;

	// Global Cache
	private final List<VaultRecord> rawVaultData = new ArrayList<>();
	private final Map<Integer, String> itemNameCache = new HashMap<>();
	private final Map<Integer, Integer> itemPriceCache = new HashMap<>();

	private ItemTable itemTableWrapper;
	private JScrollBar scrollBar;
	private UIScrollSpeed scrollSpeed = UIScrollSpeed.MEDIUM;

	@Inject
	public ItemsModePanel(AccountHashMapper accountHashMapper, Gson gson, ScheduledExecutorService executor, ItemManager itemManager, ClientThread clientThread, DataLoggerConfig config)
	{
		this.accountHashMapper = accountHashMapper;
		this.gson = gson;
		this.executor = executor;
		this.itemManager = itemManager;
		this.clientThread = clientThread;
		this.config = config;

		setLayout(new BorderLayout(0, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(new EmptyBorder(10, 10, 10, 10));

		contentPanel = new JPanel(new BorderLayout());
		contentPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		add(contentPanel, BorderLayout.CENTER);

		JPanel northWrapper = new JPanel(new BorderLayout(0, 10));
		northWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel targetPanel = Components.createTitledPanel("Filter Data", new GridLayout(0, 1, 5, 5));

		accountFilter = Components.createComboBox();
		sourceFilter = Components.createComboBox();

		// Refresh names dynamically when user opens the dropdown in case the mapper resolved names late
		accountFilter.addPopupMenuListener(new PopupMenuListener() {
			@Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) { populateAccounts(); }
			@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
			@Override public void popupMenuCanceled(PopupMenuEvent e) {}
		});

		accountFilterInput = Components.createStatefulTextField(text -> applyFilters(),
			"<html><body style='padding: 2px;'>Filter items by partial account name.<br>Separate multiple with commas.</body></html>");
		sourceFilterInput = Components.createStatefulTextField(text -> applyFilters(),
			"<html><body style='padding: 2px;'>Filter items by partial source name.<br>Separate multiple with commas.</body></html>");
		itemNameFilterInput = Components.createStatefulTextField(text -> applyFilters(),
			"<html><body style='padding: 2px; max-width: 250px;'>Filter items by partial item name.<br>Separate multiple with commas (e.g., <b>rune, blood, sword</b>).</body></html>");

		// Build structured layout for each combobox-textfield pairing
		JPanel accountPanel = new JPanel(new BorderLayout(0, 2));
		accountPanel.setOpaque(false);
		accountPanel.add(accountFilter, BorderLayout.NORTH);
		accountPanel.add(accountFilterInput, BorderLayout.CENTER);

		JPanel sourcePanel = new JPanel(new BorderLayout(0, 2));
		sourcePanel.setOpaque(false);
		sourcePanel.add(sourceFilter, BorderLayout.NORTH);
		sourcePanel.add(sourceFilterInput, BorderLayout.CENTER);

		JPanel itemPanel = new JPanel(new BorderLayout(0, 2));
		itemPanel.setOpaque(false);
		itemPanel.add(itemNameFilterInput, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
		buttonPanel.setOpaque(false);

		Dimension buttonSize = new Dimension(0, 30);

		JButton refreshBtn = new JButton("Refresh");
		refreshBtn.setFocusable(false);
		refreshBtn.setPreferredSize(buttonSize);
		refreshBtn.addActionListener(e -> loadAllDataFromDisk());

		JButton clearBtn = new JButton("Clear");
		clearBtn.setFocusable(false);
		clearBtn.setPreferredSize(buttonSize);
		clearBtn.addActionListener(e -> {
			accountFilterInput.setText("");
			sourceFilterInput.setText("");
			itemNameFilterInput.setText("");
			if (accountFilter.getItemCount() > 0) accountFilter.setSelectedIndex(0);
			if (sourceFilter.getItemCount() > 0) sourceFilter.setSelectedIndex(0);
			applyFilters();
		});

		buttonPanel.add(refreshBtn);
		buttonPanel.add(clearBtn);

		JPanel buttonWrapper = new JPanel(new BorderLayout());
		buttonWrapper.setOpaque(false);
		buttonWrapper.add(buttonPanel, BorderLayout.SOUTH);

		targetPanel.add(accountPanel);
		targetPanel.add(sourcePanel);
		targetPanel.add(itemPanel);
		targetPanel.add(buttonWrapper); // Add the wrapper instead of the raw buttonPanel

		statsContainer = new JPanel(new GridLayout(0, 2, 5, 5));
		statsContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		statsContainer.setBorder(new EmptyBorder(5, 0, 0, 0));

		northWrapper.add(targetPanel, BorderLayout.NORTH);
		northWrapper.add(statsContainer, BorderLayout.SOUTH);
		add(northWrapper, BorderLayout.NORTH);

		loadAllDataFromDisk();
	}

	private void loadAllDataFromDisk()
	{
		contentPanel.removeAll();
		statsContainer.removeAll();

		JLabel loadingLabel = new JLabel("Aggregating vault data...", SwingConstants.CENTER);
		loadingLabel.setForeground(Color.GRAY);
		contentPanel.add(loadingLabel, BorderLayout.CENTER);
		contentPanel.revalidate();
		contentPanel.repaint();

		executor.submit(() -> {
			List<VaultRecord> tempRawData = new ArrayList<>();
			Set<Integer> uniqueIds = new HashSet<>();

			File vaultRoot = PluginConstants.INTERNAL_VAULT_DIR;
			if (vaultRoot.exists() && vaultRoot.isDirectory())
			{
				File[] accDirs = vaultRoot.listFiles(File::isDirectory);
				if (accDirs != null)
				{
					for (File accountDir : accDirs)
					{
						long hash;
						try { hash = Long.parseLong(accountDir.getName()); }
						catch (NumberFormatException e) { continue; }

						File[] vaultFiles = accountDir.listFiles((d, n) -> n.endsWith(".json"));
						if (vaultFiles != null)
						{
							for (File vaultFile : vaultFiles)
							{
								String sourceName = vaultFile.getName().replace(".json", "");
								int dotIndex = sourceName.lastIndexOf('_');
								if (dotIndex > 0) sourceName = sourceName.substring(0, dotIndex);

								try (FileReader reader = new FileReader(vaultFile))
								{
									Type listType = new TypeToken<ArrayList<BankedItem>>(){}.getType();
									List<BankedItem> parsed = gson.fromJson(reader, listType);

									if (parsed != null)
									{
										for (BankedItem item : parsed)
										{
											if (item.getQuantity() > 0)
											{
												VaultRecord vaultRecord = new VaultRecord(item.getItemId(), hash, sourceName, item.getQuantity());
												tempRawData.add(vaultRecord);
												uniqueIds.add(item.getItemId());
											}
										}
									}
								}
								catch (Exception ex) {
									log.warn("Failed to parse vaultFile: {}", vaultFile, ex);
								}
							}
						}
					}
				}
			}

			clientThread.invokeLater(() -> {
				for (Integer id : uniqueIds) {
					try {
						itemNameCache.put(id, itemManager.getItemComposition(id).getName());
						itemPriceCache.put(id, itemManager.getItemPrice(id));
					} catch (Exception ignored) {
						itemNameCache.put(id, "Unknown Item");
						itemPriceCache.put(id, 0);
					}
				}

				rawVaultData.clear();
				rawVaultData.addAll(tempRawData);

				SwingUtilities.invokeLater(this::buildBaseUi);
			});
		});
	}

	private void buildBaseUi()
	{
		contentPanel.removeAll();

		if (rawVaultData.isEmpty()) {
			JLabel emptyLabel = new JLabel("No items found in any vault.", SwingConstants.CENTER);
			emptyLabel.setForeground(Color.GRAY);
			contentPanel.add(emptyLabel, BorderLayout.CENTER);
			contentPanel.revalidate();
			contentPanel.repaint();
			return;
		}

		itemTableWrapper = new ItemTable(itemManager);

		JPanel sortButtonHeader = new JPanel(new GridLayout(1, 0, 4, 0));
		sortButtonHeader.setBackground(ColorScheme.DARK_GRAY_COLOR);
		sortButtonHeader.setBorder(new EmptyBorder(0, 0, 5, 0));

		for (ItemTable.ItemSortType type : ItemTable.ItemSortType.values())
		{
			JButton sortBtn = new JButton(type.toString());
			sortBtn.setFont(FontManager.getRunescapeSmallFont());
			sortBtn.setForeground(Color.GRAY);
			sortBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			sortBtn.setFocusable(false);
			sortBtn.setBorder(javax.swing.BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_HOVER_COLOR, 1));

			sortBtn.addActionListener(e -> itemTableWrapper.toggleSortType(type));
			sortButtonHeader.add(sortBtn);
		}

		JTable customTable = itemTableWrapper.getTable();
		customTable.setPreferredScrollableViewportSize(new Dimension(customTable.getPreferredSize().width, 36 * 15));

		JScrollPane scrollPane = Components.createScrollPane(customTable);
		scrollBar = scrollPane.getVerticalScrollBar();
		scrollBar.setUnitIncrement(16);

		JPanel tableContainer = new JPanel(new BorderLayout());
		tableContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		tableContainer.add(sortButtonHeader, BorderLayout.NORTH);
		tableContainer.add(scrollPane, BorderLayout.CENTER);

		contentPanel.add(tableContainer, BorderLayout.CENTER);

		populateAccounts();
		populateSources();

		// Attach listeners to append choices to the text fields
		for (java.awt.event.ActionListener al : accountFilter.getActionListeners()) accountFilter.removeActionListener(al);
		for (java.awt.event.ActionListener al : sourceFilter.getActionListeners()) sourceFilter.removeActionListener(al);

		accountFilter.addActionListener(e -> {
			AccountItem selected = (AccountItem) accountFilter.getSelectedItem();
			if (selected != null && selected.getHash() != -1L) {
				String currentText = accountFilterInput.getText().trim();
				if (currentText.isEmpty() || currentText.endsWith(",")) {
					accountFilterInput.setText(currentText + (currentText.isEmpty() ? "" : " ") + selected.getName());
				} else {
					accountFilterInput.setText(currentText + ", " + selected.getName());
				}
				accountFilter.setSelectedIndex(0);
				applyFilters();
			}
		});

		sourceFilter.addActionListener(e -> {
			String selected = (String) sourceFilter.getSelectedItem();
			if (selected != null && !selected.equals("Select Source...")) {
				String currentText = sourceFilterInput.getText().trim();
				if (currentText.isEmpty() || currentText.endsWith(",")) {
					sourceFilterInput.setText(currentText + (currentText.isEmpty() ? "" : " ") + selected);
				} else {
					sourceFilterInput.setText(currentText + ", " + selected);
				}
				sourceFilter.setSelectedIndex(0);
				applyFilters();
			}
		});

		applyFilters();
	}

	/**
	 * Set the scroll speed of the inner scrollbar of this panel to a value that corresponds with the given speed
	 */
	public void setScrollSpeed(UIScrollSpeed scrollSpeed)
	{
		this.scrollSpeed = scrollSpeed;
		switch (scrollSpeed){
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

	private void populateAccounts()
	{
		accountFilter.removeAllItems();
		accountFilter.addItem(new AccountItem("Select Account...", -1L));

		Set<Long> loadedHashes = new HashSet<>();
		for (VaultRecord r : rawVaultData) loadedHashes.add(r.accountHash);

		List<AccountItem> items = new ArrayList<>();
		for (Long hash : loadedHashes) {
			String name = accountHashMapper.getAccountName(hash);
			if (name == null || name.isEmpty()) name = String.valueOf(hash);
			items.add(new AccountItem(name, hash));
		}

		items.sort(Comparator.comparing(AccountItem::getName, String.CASE_INSENSITIVE_ORDER));
		for (AccountItem item : items) {
			accountFilter.addItem(item);
		}
	}

	private void populateSources()
	{
		sourceFilter.removeAllItems();
		sourceFilter.addItem("Select Source...");

		Set<String> uniqueSources = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		for (VaultRecord r : rawVaultData) uniqueSources.add(r.source);

		for (String src : uniqueSources) {
			sourceFilter.addItem(src);
		}
	}

	public void applyFilters()
	{
		if (itemTableWrapper == null) return;

		// Parse Accounts Filter
		String rawAccText = accountFilterInput.getText();
		List<String> targetAccounts = new ArrayList<>();
		if (rawAccText != null && !rawAccText.trim().isEmpty()) {
			for (String term : rawAccText.split(",")) {
				if (!term.trim().isEmpty()) targetAccounts.add(term.trim().toLowerCase());
			}
		}

		// Parse Sources Filter
		String rawSrcText = sourceFilterInput.getText();
		List<String> targetSources = new ArrayList<>();
		if (rawSrcText != null && !rawSrcText.trim().isEmpty()) {
			for (String term : rawSrcText.split(",")) {
				if (!term.trim().isEmpty()) targetSources.add(term.trim().toLowerCase());
			}
		}

		// Parse Item Name Filter
		String rawItemText = itemNameFilterInput.getText();
		List<String> targetItems = new ArrayList<>();
		if (rawItemText != null && !rawItemText.trim().isEmpty()) {
			for (String term : rawItemText.split(",")) {
				if (!term.trim().isEmpty()) targetItems.add(term.trim().toLowerCase());
			}
		}

		Map<Integer, ItemStat> currentView = new HashMap<>();

		int uniqueItems = 0;
		long totalQuantity = 0;
		long totalValue = 0;
		boolean hideZeroPriceItems = config.hideZeroPriceItems();

		// Track unique accounts and sources in current filtered view
		Set<String> filteredAccounts = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		Set<String> filteredSources = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

		for (VaultRecord record : rawVaultData)
		{
			int price = itemPriceCache.getOrDefault(record.itemId, 0);
			if (hideZeroPriceItems && price <= 0) {
				continue;
			}

			String resolvedAccName = accountHashMapper.getAccountName(record.accountHash);
			if (resolvedAccName == null || resolvedAccName.isEmpty()) resolvedAccName = String.valueOf(record.accountHash);

			boolean matchAcc = targetAccounts.isEmpty();
			if (!matchAcc) {
				String accLower = resolvedAccName.toLowerCase();
				for (String term : targetAccounts) {
					if (accLower.contains(term)) {
						matchAcc = true;
						break;
					}
				}
			}

			boolean matchSrc = targetSources.isEmpty();
			if (!matchSrc) {
				String srcLower = record.source.toLowerCase();
				for (String term : targetSources) {
					if (srcLower.contains(term)) {
						matchSrc = true;
						break;
					}
				}
			}

			boolean matchName = targetItems.isEmpty();
			if (!matchName) {
				String itemName = itemNameCache.getOrDefault(record.itemId, "").toLowerCase();
				for (String term : targetItems) {
					if (itemName.contains(term)) {
						matchName = true;
						break;
					}
				}
			}

			if (matchAcc && matchSrc && matchName) {
				ItemStat stat = currentView.computeIfAbsent(record.itemId, k ->
					new ItemStat(record.itemId, itemNameCache.get(record.itemId), price));

				stat.addQuantity(record.quantity);
				stat.addAccount(resolvedAccName);
				stat.addSource(record.source);

				// Add to our top-level unique sets
				filteredAccounts.add(resolvedAccName);
				filteredSources.add(record.source);
			}
		}

		DefaultTableModel dataModel = new DefaultTableModel(new String[]{"Item Data"}, 0);
		for (ItemStat stat : currentView.values()) {
			dataModel.addRow(new Object[]{ stat });

			uniqueItems++;
			totalQuantity += stat.getQuantity();
			totalValue += stat.getStackValue();
		}

		updateStatsUi(uniqueItems, totalQuantity, totalValue, filteredAccounts, filteredSources);
		itemTableWrapper.updateData(dataModel);

		contentPanel.revalidate();
		contentPanel.repaint();
	}

	private void updateStatsUi(int uniqueItems, long totalQuantity, long totalValue, Set<String> accounts, Set<String> sources)
	{
		statsContainer.removeAll();

		// Changed to a 2x2 grid so all four cards have enough space on the side panel
		statsContainer.setLayout(new GridLayout(2, 2, 5, 5));

		statsContainer.add(Components.createStatCard(
			"Unique Items",
			String.format("%,d", uniqueItems),
			"Total number of distinct items in this subset"
		));

		statsContainer.add(Components.createStatCard(
			"Est. Value",
			QuantityFormatter.quantityToStackSize(totalValue) + " gp",
			"Total estimated Grand Exchange value"
		));

		statsContainer.add(Components.createStatCard(
			"Accounts",
			String.valueOf(accounts.size()),
			"<html>A total of " + accounts.size() + " accounts were found;<br>" +
				(accounts.isEmpty() ? "None" : String.join("<br>", accounts)) + "</html>"
		));

		statsContainer.add(Components.createStatCard(
			"Sources",
			String.valueOf(sources.size()),
			"<html>A total of " + accounts.size() + " sources were found;<br>" +
				(sources.isEmpty() ? "None" : String.join("<br>", sources)) + "</html>"
		));

		statsContainer.revalidate();
		statsContainer.repaint();
	}

	private static class VaultRecord
	{
		final int itemId;
		final long accountHash;
		final String source;
		final long quantity;

		VaultRecord(int itemId, long accountHash, String source, long quantity) {
			this.itemId = itemId;
			this.accountHash = accountHash;
			this.source = source;
			this.quantity = quantity;
		}
	}

	@Getter
	public static class ItemStat
	{
		private final int itemId;
		private final String name;
		private final int gePrice;
		private long quantity = 0;
		private long stackValue = 0;

		private final Set<String> accounts = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		private final Set<String> sources = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

		public ItemStat(int itemId, String name, int gePrice) {
			this.itemId = itemId;
			this.name = name;
			this.gePrice = gePrice;
		}

		public void addQuantity(long qty) {
			this.quantity += qty;
			this.stackValue = this.quantity * this.gePrice;
		}

		public void addAccount(String acc) { this.accounts.add(acc); }
		public void addSource(String src) { this.sources.add(src); }
	}
}