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

import com.datalogger.constants.PluginConstants;
import com.datalogger.dto.ColosseumAttemptDTO;
import com.datalogger.dto.ColosseumWaveDTO;
import com.datalogger.models.enums.ColosseumModifier;
import com.datalogger.models.itemvault.ItemBundle;
import com.google.gson.Gson;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.QuantityFormatter;

@Slf4j
public class ColosseumStatisticsModePanel extends JPanel
{
	private final Gson gson;
	private final ScheduledExecutorService executor;
	private final SpriteManager spriteManager;
	private final ItemManager itemManager;
	private final ClientThread clientThread;

	private final JPanel statsContainer;
	private final JPanel tableContainer;
	private final JPanel dynamicControlsPanel;
	private final PluginErrorPanel errorPanel;
	private JTable rankingTable;
	private TableMode currentTableMode = TableMode.MODIFIERS;

	private final Map<ColosseumModifier, ImageIcon> modIconCache = new HashMap<>();
	private final Map<Integer, ImageIcon> itemIconCache = new HashMap<>();

	private List<ColosseumAttemptDTO> cachedAttempts = null;
	private final Set<Integer> activeWaves = new HashSet<>();
	private final Set<String> activeResults = new HashSet<>();
	private final javax.swing.JTextField includeTagsInput = new javax.swing.JTextField();
	private final javax.swing.JTextField excludeTagsInput = new javax.swing.JTextField();

	// Field to track the active modifier filter combobox state
	private String requiredActiveModifier = "Any Active Modifier";

	@Inject
	public ColosseumStatisticsModePanel(Gson gson, ScheduledExecutorService executor, SpriteManager spriteManager, ItemManager itemManager, ClientThread clientThread)
	{
		this.gson = gson;
		this.executor = executor;
		this.spriteManager = spriteManager;
		this.itemManager = itemManager;
		this.clientThread = clientThread;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Initialize filter defaults
		activeResults.add("COMPLETED");
		activeResults.add("FAILED");
		activeResults.add("CANCELLED");

		// --- 1. STATS SECTION (Top) ---
		statsContainer = new JPanel(new GridLayout(0, 2, 5, 5));
		statsContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		statsContainer.setBorder(new EmptyBorder(10, 0, 10, 0));
		add(statsContainer, BorderLayout.NORTH);

		// --- 2. CENTER WRAPPER (Holds Controls + Table) ---
		JPanel centerWrapper = new JPanel(new BorderLayout());
		centerWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// 2a. Static Filters (Wave, Result, Tag)
		JPanel tagFiltersPanel = new JPanel(new GridLayout(2, 1, 0, 5));
		tagFiltersPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		tagFiltersPanel.add(buildIncludeTagFilterPanel());
		tagFiltersPanel.add(buildTagFilterPanel());

		JPanel staticFiltersContainer = new JPanel(new BorderLayout(0, 5));
		staticFiltersContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		staticFiltersContainer.add(buildFilterPanel(), BorderLayout.NORTH);
		staticFiltersContainer.add(buildResultFilterPanel(), BorderLayout.CENTER);
		staticFiltersContainer.add(tagFiltersPanel, BorderLayout.SOUTH);

		// 2b. Dynamic Controls (Comboboxes)
		dynamicControlsPanel = new JPanel(new BorderLayout());
		dynamicControlsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Group all controls together
		JPanel allControlsWrapper = new JPanel(new BorderLayout(0, 5));
		allControlsWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		allControlsWrapper.setBorder(new EmptyBorder(0, 0, 10, 0)); // Margin before table
		allControlsWrapper.add(staticFiltersContainer, BorderLayout.NORTH);
		allControlsWrapper.add(dynamicControlsPanel, BorderLayout.SOUTH);

		centerWrapper.add(allControlsWrapper, BorderLayout.NORTH);

		// 2c. Table Container
		tableContainer = new JPanel(new BorderLayout());
		tableContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		centerWrapper.add(tableContainer, BorderLayout.CENTER);

		add(centerWrapper, BorderLayout.CENTER);

		errorPanel = new PluginErrorPanel();

		loadStatisticsFromDisk();
	}

	private JPanel buildResultFilterPanel()
	{
		JPanel panel = new JPanel(new GridLayout(1, 3, 5, 2));
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		panel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR),
			"Filter Result",
			TitledBorder.LEFT, TitledBorder.TOP,
			FontManager.getRunescapeSmallFont(), Color.LIGHT_GRAY
		));

		String[] results = {"COMPLETED", "FAILED", "CANCELLED"};
		for (String result : results)
		{
			JButton btn = new JButton(result);
			btn.setFocusable(false);
			btn.setFont(FontManager.getRunescapeSmallFont());
			btn.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
			btn.setForeground(Color.WHITE);
			btn.setBorder(BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR, 1));

			btn.addActionListener(e -> {
				if (activeResults.contains(result)) {
					activeResults.remove(result);
					btn.setBackground(ColorScheme.DARK_GRAY_COLOR);
					btn.setForeground(Color.GRAY);
				} else {
					activeResults.add(result);
					btn.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
					btn.setForeground(Color.WHITE);
				}
				recalculateStatsFromCache();
			});
			panel.add(btn);
		}
		return panel;
	}

	private JPanel buildIncludeTagFilterPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		panel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR),
			"Include Tags (comma-separated)",
			TitledBorder.LEFT, TitledBorder.TOP,
			FontManager.getRunescapeSmallFont(), Color.LIGHT_GRAY
		));

		includeTagsInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		includeTagsInput.setForeground(Color.WHITE);
		includeTagsInput.setCaretColor(Color.WHITE);
		includeTagsInput.setBorder(new EmptyBorder(5, 5, 5, 5));
		StringBuilder tooltipText = new StringBuilder("<html><body style='padding: 2px; max-width: 250px;'>");
		tooltipText.append("If set, ONLY include waves that have at least one of the tags specified.<br>" +
			"Multiple tags can be set by separating each tag with '|'; e.g. TAG_A|TAG_B");
		tooltipText.append("</body></html>");
		includeTagsInput.setToolTipText(tooltipText.toString());

		includeTagsInput.addActionListener(e -> recalculateStatsFromCache());
		includeTagsInput.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusLost(java.awt.event.FocusEvent e) {
				recalculateStatsFromCache();
			}
		});

		panel.add(includeTagsInput, BorderLayout.CENTER);
		return panel;
	}

	private JPanel buildTagFilterPanel()
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		panel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR),
			"Exclude Tags (comma-separated)",
			TitledBorder.LEFT, TitledBorder.TOP,
			FontManager.getRunescapeSmallFont(), Color.LIGHT_GRAY
		));

		excludeTagsInput.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		excludeTagsInput.setForeground(Color.WHITE);
		excludeTagsInput.setCaretColor(Color.WHITE);
		excludeTagsInput.setBorder(new EmptyBorder(5, 5, 5, 5));
		StringBuilder tooltipText = new StringBuilder("<html><body style='padding: 2px; max-width: 250px;'>");
		tooltipText.append("If set, exclude waves that have any of the tags specified.<br>" +
			"Multiple tags can be set by separating each tag with '|', e.g. TAG_A|TAG_B");
		tooltipText.append("</body></html>");
		excludeTagsInput.setToolTipText(tooltipText.toString());

		// Recalculate when the user presses Enter or clicks away
		excludeTagsInput.addActionListener(e -> recalculateStatsFromCache());
		excludeTagsInput.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusLost(java.awt.event.FocusEvent e) {
				recalculateStatsFromCache();
			}
		});

		panel.add(excludeTagsInput, BorderLayout.CENTER);
		return panel;
	}

	private JPanel buildFilterPanel()
	{
		JPanel panel = new JPanel(new GridLayout(2, 6, 2, 2));
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		panel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR),
			"Filter Waves",
			TitledBorder.LEFT, TitledBorder.TOP,
			FontManager.getRunescapeSmallFont(), Color.LIGHT_GRAY
		));

		for (int i = 1; i <= 12; i++)
		{
			final int waveNum = i;
			activeWaves.add(waveNum);

			JButton waveButton = new JButton(String.valueOf(waveNum));
			waveButton.setFocusable(false);
			waveButton.setFont(FontManager.getRunescapeSmallFont());
			waveButton.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
			waveButton.setForeground(Color.WHITE);
			waveButton.setBorder(BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR, 1));

			waveButton.addActionListener(e -> {
				if (activeWaves.contains(waveNum)) {
					activeWaves.remove(waveNum);
					waveButton.setBackground(ColorScheme.DARK_GRAY_COLOR);
					waveButton.setForeground(Color.GRAY);
				} else {
					activeWaves.add(waveNum);
					waveButton.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
					waveButton.setForeground(Color.WHITE);
				}
				recalculateStatsFromCache();
			});
			panel.add(waveButton);
		}
		return panel;
	}

	private void loadStatisticsFromDisk()
	{
		SwingUtilities.invokeLater(() -> {
			statsContainer.removeAll();
			statsContainer.setLayout(new BorderLayout());
			statsContainer.add(errorPanel, BorderLayout.CENTER);
			errorPanel.setContent("Loading Data...", "Reading Colosseum history.");
			revalidate();
			repaint();
		});

		executor.submit(() -> {
			List<ColosseumAttemptDTO> loadedData = new ArrayList<>();
			File historyFile = PluginConstants.INTERNAL_COLOSSEUM_ATTEMPT_HISTORY;

			if (historyFile.exists())
			{
				try (BufferedReader reader = new BufferedReader(new FileReader(historyFile)))
				{
					String line;
					while ((line = reader.readLine()) != null)
					{
						if (line.trim().isEmpty()) continue;

						// Wrap the parsing of the individual line
						try
						{
							ColosseumAttemptDTO dto = gson.fromJson(line, ColosseumAttemptDTO.class);
							if (dto != null) loadedData.add(dto);
						}
						catch (Exception parseException)
						{
							log.warn("Skipping malformed attempt line in history file: {}", parseException.getMessage());
						}
					}
				}
				catch (Exception e) { log.error("Failed to read Colosseum history file", e); }
			}
			this.cachedAttempts = loadedData;
			SwingUtilities.invokeLater(this::recalculateStatsFromCache);
		});
	}

	private void recalculateStatsFromCache()
	{
		if (cachedAttempts == null) return;

		// Parse the included tags from the text field
		String rawIncludeTags = includeTagsInput.getText();
		Set<String> includedTags = new HashSet<>();
		if (rawIncludeTags != null && !rawIncludeTags.trim().isEmpty()) {
			for (String tag : rawIncludeTags.split(",")) {
				if (!tag.trim().isEmpty()) {
					includedTags.add(tag.trim().toLowerCase()); // Lowercase for case-insensitive matching
				}
			}
		}

		// Parse the excluded tags from the text field
		String rawExcludeTags = excludeTagsInput.getText();
		Set<String> excludedTags = new HashSet<>();
		if (rawExcludeTags != null && !rawExcludeTags.trim().isEmpty()) {
			for (String tag : rawExcludeTags.split(",")) {
				if (!tag.trim().isEmpty()) {
					excludedTags.add(tag.trim().toLowerCase()); // Lowercase for case-insensitive matching
				}
			}
		}

		executor.submit(() -> {
			AggregateStats stats = new AggregateStats();
			for (ColosseumAttemptDTO attempt : cachedAttempts)
			{
				// Pass the included/excluded tags and the required active modifier down to the processor
				stats.processAttempt(attempt, activeWaves, activeResults, excludedTags, includedTags, requiredActiveModifier);
			}

			// Perform price calculation on the Client Thread
			clientThread.invoke(() -> {
				long totalValue = 0;
				for (Map.Entry<String, Integer> entry : stats.getRewardCounts().entrySet())
				{
					ItemBundle bundle = stats.getRewardBundles().get(entry.getKey());
					int frequency = entry.getValue();

					int price = itemManager.getItemPrice(bundle.getItemId());
					totalValue += (long) price * bundle.getQuantity() * frequency;
				}
				stats.setTotalGpValue(totalValue);

				// Once calculation is done, hop back to the UI thread to rebuild
				SwingUtilities.invokeLater(() -> rebuildUi(stats));
			});
		});
	}

	private void rebuildUi(AggregateStats stats)
	{
		statsContainer.removeAll();
		dynamicControlsPanel.removeAll();
		tableContainer.removeAll();
		statsContainer.setLayout(new GridLayout(0, 2, 5, 5));

		if (stats.getTotalAttempts() == 0)
		{
			statsContainer.setLayout(new BorderLayout());
			errorPanel.setContent("No Data", "Complete a Colosseum run to see statistics.");
			statsContainer.add(errorPanel, BorderLayout.CENTER);
		}
		else
		{
			// --- 1. POPULATE STAT CARDS ---

			// Build tooltip for N Trials
			StringBuilder trialsTooltip = new StringBuilder("<html><body style='padding: 2px; max-width: 250px;'>");
			trialsTooltip.append("The number of trials that have one or more waves included in the data subset.<br><br>");
			int completed = stats.getAttemptResultCounts().getOrDefault("COMPLETED", 0);
			int cancelled = stats.getAttemptResultCounts().getOrDefault("CANCELLED", 0);
			int failed = stats.getAttemptResultCounts().getOrDefault("FAILED", 0);
			trialsTooltip.append("<span style='color: #a5a5a5;'>Completed:</span> <span style='color: white;'>").append(completed).append("</span><br>");
			trialsTooltip.append("<span style='color: #a5a5a5;'>Cancelled:</span> <span style='color: white;'>").append(cancelled).append("</span><br>");
			trialsTooltip.append("<span style='color: #a5a5a5;'>Failed:</span> <span style='color: white;'>").append(failed).append("</span>");
			trialsTooltip.append("</body></html>");

			statsContainer.add(buildStatCard("N trials", String.valueOf(stats.getTotalAttempts()), trialsTooltip.toString()));

			double avgWave = stats.getTotalAttempts() > 0 ? (double) stats.getTotalWavesReached() / stats.getTotalAttempts() : 0;
			statsContainer.add(buildStatCard("Avg Wave", String.format("%.1f", avgWave)));

			// Dynamically build an HTML tooltip for the wave breakdown
			StringBuilder waveBreakdown = new StringBuilder("<html><body style='padding: 2px;'>");
			waveBreakdown.append("<b style='color: white;'>Wave Breakdown:</b><br>");

			boolean hasWaves = false;
			for (int i = 1; i <= 12; i++)
			{
				int count = stats.getWaveCompletionCounts().getOrDefault(i, 0);
				if (count > 0)
				{
					waveBreakdown.append("<span style='color: #a5a5a5;'>Wave ").append(i).append(":</span> ")
						.append("<span style='color: white;'>").append(count).append("</span><br>");
					hasWaves = true;
				}
			}
			waveBreakdown.append("</body></html>");

			// If no waves are included, just show a standard tooltip
			String tooltipText = hasWaves ? waveBreakdown.toString() : "No waves matched the current filters.";
			statsContainer.add(buildStatCard("Waves Included", String.format("%,d", stats.getWavesIncluded()), tooltipText));

			if (currentTableMode == TableMode.MODIFIERS)
			{
				ColosseumModifier favMod = stats.getMostCommonModifier();
				statsContainer.add(buildStatCard("Fav Modifier", favMod != null ? favMod.getUiLabel() : "N/A"));
			}
			else
			{
				double avgValue = stats.getTotalAttempts() > 0 ? (double) stats.getTotalGpValue() / stats.getTotalAttempts() : 0;
				String formattedTotal = QuantityFormatter.quantityToStackSize(stats.getTotalGpValue()) + " gp";
				String rewardsTooltip = "<html><body style='padding: 2px; max-width: 250px;'>" + "Average reward value per trial, summed across all of its waves that have not been filtered out<br>" +
					"<b style='color: white;'>Total gp:</b> " + formattedTotal + "<br>";
				statsContainer.add(buildStatCard("Avg Value/trial", String.format("%,.0f gp", avgValue), rewardsTooltip));
			}

			// --- 2. POPULATE DYNAMIC COMBOBOXES ---
			JPanel selectorsPanel = new JPanel(new GridLayout(0, 1, 0, 5));
			selectorsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

			JComboBox<TableMode> tableModeSelector = new JComboBox<>(TableMode.values());
			tableModeSelector.setSelectedItem(currentTableMode);
			tableModeSelector.setFocusable(false);
			tableModeSelector.addActionListener(e -> {
				currentTableMode = (TableMode) tableModeSelector.getSelectedItem();
				requiredActiveModifier = "Any Active Modifier"; // Reset to avoid hidden filter state
				recalculateStatsFromCache();
			});
			selectorsPanel.add(tableModeSelector);

			// Active Modifier condition dropdown (Only visible in Modifiers mode)
			if (currentTableMode == TableMode.MODIFIERS)
			{
				JComboBox<String> activeModifierFilterBox = new JComboBox<>();

				// Build dynamic tooltip for the Combobox to include wave counts
				StringBuilder modTooltip = new StringBuilder("<html><body style='padding: 2px; max-width: 250px;'>");
				modTooltip.append("If set, only include waves in which the selected modifier (but not a higher tier) was selected at some point before the wave.<br><br>");
				modTooltip.append("<b style='color: white;'>Matching Waves:</b><br>");

				boolean foundModWaves = false;
				for (int i = 1; i <= 12; i++)
				{
					int count = stats.getWaveCompletionCounts().getOrDefault(i, 0);
					if (count > 0)
					{
						modTooltip.append("<span style='color: #a5a5a5;'>Wave ").append(i).append(":</span> ")
							.append("<span style='color: white;'>").append(count).append("</span><br>");
						foundModWaves = true;
					}
				}

				if (!foundModWaves)
				{
					modTooltip.append("<span style='color: #a5a5a5;'>No waves matched.</span>");
				}
				modTooltip.append("</body></html>");

				activeModifierFilterBox.setToolTipText(modTooltip.toString());

				activeModifierFilterBox.addItem("Any Active Modifier");
				for (ColosseumModifier mod : ColosseumModifier.values())
				{
					activeModifierFilterBox.addItem(mod.name());
				}
				activeModifierFilterBox.setSelectedItem(requiredActiveModifier);
				activeModifierFilterBox.setFocusable(false);
				activeModifierFilterBox.addActionListener(e -> {
					String selected = (String) activeModifierFilterBox.getSelectedItem();
					if (!requiredActiveModifier.equals(selected)) {
						requiredActiveModifier = selected;
						recalculateStatsFromCache();
					}
				});

				selectorsPanel.add(activeModifierFilterBox);
			}

			dynamicControlsPanel.add(selectorsPanel, BorderLayout.CENTER);

			// --- 3. POPULATE DATA TABLE ---
			DefaultTableModel tableModel;
			if (currentTableMode == TableMode.MODIFIERS)
			{
				tableModel = buildModifierTable(stats);
				rankingTable = new JTable(tableModel) {
					@Override
					public Dimension getPreferredScrollableViewportSize() {
						return getPreferredSize();
					}
				};
				rankingTable.setToolTipText(new StringBuilder("<html><body style='padding: 2px; max-width: 250px;'>" +
					"Wave: Wave number of this row<br>" +
					"Seen: Number of times this modifier was among the choices for the corresponding wave<br>" +
					"Picked: Number of times this modifier was selected<br>" +
					"Total: Total number of waves with this particular wave number<br>" +
					"Seen/picked/total are counted across all of the filtered waves<br>" +
					"</body></html>").toString());

				rankingTable.getColumnModel().getColumn(0).setCellRenderer(new ModifierIconCellRenderer());

				DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
				centerRenderer.setHorizontalAlignment(JLabel.CENTER);

				rankingTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // Wave
				rankingTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer); // Seen
				rankingTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer); // Picked
				rankingTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer); // Total (Replaced Rate %)

				rankingTable.setAutoCreateRowSorter(true);
				rankingTable.getRowSorter().setSortKeys(Collections.singletonList(
					new javax.swing.RowSorter.SortKey(1, javax.swing.SortOrder.ASCENDING)
				));

				// Shift column widths slightly to fit the new Wave column
				rankingTable.getColumnModel().getColumn(0).setPreferredWidth(40);
				rankingTable.getColumnModel().getColumn(1).setPreferredWidth(40);
				rankingTable.getColumnModel().getColumn(2).setPreferredWidth(40);
				rankingTable.getColumnModel().getColumn(3).setPreferredWidth(40);
				rankingTable.getColumnModel().getColumn(4).setPreferredWidth(60);
			}
			else
			{
				tableModel = buildRewardsTable(stats);
				rankingTable = new JTable(tableModel) {
					@Override
					public Dimension getPreferredScrollableViewportSize() {
						return getPreferredSize();
					}
				};

				rankingTable.getColumnModel().getColumn(0).setCellRenderer(new ItemIconCellRenderer());

				DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
				centerRenderer.setHorizontalAlignment(JLabel.CENTER);

				// 1: Wave (Centered)
				rankingTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
				// 2: Count (Centered)
				rankingTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

				// 3: Rate % Renderer
				rankingTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
					@Override
					protected void setValue(Object value) {
						setText(value instanceof Double ? String.format("%.1f%%", (Double) value) : "");
						setHorizontalAlignment(JLabel.CENTER);
					}
				});

				// Update column widths
				rankingTable.getColumnModel().getColumn(0).setPreferredWidth(50);
				rankingTable.getColumnModel().getColumn(1).setPreferredWidth(40); // Wave width
				rankingTable.getColumnModel().getColumn(2).setPreferredWidth(50); // Count width
				rankingTable.getColumnModel().getColumn(3).setPreferredWidth(60); // Rate width

				rankingTable.setAutoCreateRowSorter(true);
				rankingTable.getRowSorter().setSortKeys(Collections.singletonList(
					new javax.swing.RowSorter.SortKey(1, javax.swing.SortOrder.ASCENDING)
				));
			}

			rankingTable.setBackground(ColorScheme.DARK_GRAY_COLOR);
			rankingTable.setForeground(Color.WHITE);
			rankingTable.setGridColor(ColorScheme.DARKER_GRAY_COLOR);
			rankingTable.getTableHeader().setBackground(ColorScheme.DARKER_GRAY_COLOR);
			rankingTable.getTableHeader().setForeground(Color.WHITE);
			rankingTable.setRowHeight(32);

			JScrollPane tableScrollPane = new JScrollPane(rankingTable);
			tableScrollPane.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
			tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
			tableScrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
			tableContainer.add(tableScrollPane, BorderLayout.CENTER);
		}

		revalidate();
		repaint();
	}

	private DefaultTableModel buildRewardsTable(AggregateStats stats)
	{
		String[] columnNames = {"Item", "Wave", "Count", "Rate %"};
		DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0)
		{
			@Override public boolean isCellEditable(int row, int col) { return false; }
			@Override public Class<?> getColumnClass(int col)
			{
				if (col == 0) return ItemBundle.class;
				if (col == 3) return Double.class;
				return Integer.class;
			}
		};

		stats.getRewardBundles().forEach((key, bundle) -> {
			Integer wave = stats.getRewardWaves().get(key);
			Integer frequency = stats.getRewardCounts().getOrDefault(key, 0);

			// Get total completions for THIS specific wave
			int specificWaveTotal = stats.getWaveCompletionCounts().getOrDefault(wave, 0);

			// Calculate rate against the specific wave total
			double rate = specificWaveTotal > 0 ? (double) frequency / specificWaveTotal * 100.0 : 0.0;

			tableModel.addRow(new Object[]{bundle, wave, frequency, rate});
		});

		return tableModel;
	}

	private DefaultTableModel buildModifierTable(AggregateStats stats)
	{
		String[] columnNames = {"", "Wave", "Seen", "Picked", "Total"}; // Changed Rate % to Total
		DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0)
		{
			@Override public boolean isCellEditable(int row, int col) { return false; }
			@Override public Class<?> getColumnClass(int col)
			{
				if (col == 0) return ColosseumModifier.class;
				return Integer.class; // All other columns (Wave, Seen, Picked, Total) are Integers
			}
		};

		for (ModifierStat stat : stats.getRankedModifiers())
		{
			// Fetch the total completion count for the wave associated with this modifier stat
			int waveTotal = stats.getWaveCompletionCounts().getOrDefault(stat.getWave(), 0);

			tableModel.addRow(new Object[]{
				stat.getModifier(),
				stat.getWave(), // Pass wave number
				stat.getAppearedCount(),
				stat.getChosenCount(),
				waveTotal // Output the total occurrences of this wave
			});
		}
		return tableModel;
	}

	private JPanel buildStatCard(String title, String value)
	{
		return buildStatCard(title, value, null);
	}

	// The new master method that handles tooltips
	private JPanel buildStatCard(String title, String value, String tooltip)
	{
		JPanel card = new JPanel(new BorderLayout());
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_HOVER_COLOR, 1),
			new EmptyBorder(8, 8, 8, 8)
		));

		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(FontManager.getRunescapeSmallFont());
		titleLabel.setForeground(Color.LIGHT_GRAY);
		titleLabel.setHorizontalAlignment(JLabel.CENTER);

		JLabel valueLabel = new JLabel(value);
		valueLabel.setFont(FontManager.getRunescapeBoldFont());
		valueLabel.setForeground(Color.WHITE);
		valueLabel.setHorizontalAlignment(JLabel.CENTER);

		card.add(titleLabel, BorderLayout.NORTH);
		card.add(valueLabel, BorderLayout.CENTER);

		// Apply the tooltip if one was provided
		if (tooltip != null && !tooltip.isEmpty())
		{
			card.setToolTipText(tooltip);
			// Apply to labels as well, otherwise hovering directly over the text might hide the tooltip
			titleLabel.setToolTipText(tooltip);
			valueLabel.setToolTipText(tooltip);
		}

		return card;
	}

	private ImageIcon getIconForItem(int itemId, int quantity)
	{
		if (itemId <= 0) return null;
		AsyncBufferedImage image = itemManager.getImage(itemId, quantity, true);
		if (image == null) return null;

		image.onLoaded(() -> SwingUtilities.invokeLater(() -> {
			if (rankingTable != null) rankingTable.repaint();
		}));

		return new ImageIcon(image);
	}

	private ImageIcon getIconForModifier(ColosseumModifier mod)
	{
		if (modIconCache.containsKey(mod)) return modIconCache.get(mod);
		if (mod.getSpriteId() <= 0) return null;

		modIconCache.put(mod, null);
		spriteManager.getSpriteAsync(mod.getSpriteId(), 0, loadedImg -> {
			SwingUtilities.invokeLater(() -> {
				modIconCache.put(mod, new ImageIcon(loadedImg));
				if (rankingTable != null) rankingTable.repaint();
			});
		});
		return null;
	}

	private class ModifierIconCellRenderer extends DefaultTableCellRenderer
	{
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value instanceof ColosseumModifier)
			{
				ColosseumModifier mod = (ColosseumModifier) value;
				setText(null);
				setToolTipText(mod.getUiLabel());
				setHorizontalAlignment(JLabel.CENTER);
				setIcon(getIconForModifier(mod));
			}
			return this;
		}
	}

	private class ItemIconCellRenderer extends DefaultTableCellRenderer
	{
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (value instanceof ItemBundle)
			{
				ItemBundle bundle = (ItemBundle) value;
				setText(null);
				setIcon(getIconForItem(bundle.getItemId(), bundle.getQuantity()));
				setToolTipText(bundle.getItemName());
				setHorizontalAlignment(JLabel.CENTER);
			}
			return this;
		}
	}

	@Getter
	private static class ModifierStat implements Comparable<ModifierStat>
	{
		private final ColosseumModifier modifier;
		private final int wave; // NEW: Track the wave
		private int appearedCount = 0;
		private int chosenCount = 0;

		public ModifierStat(ColosseumModifier modifier, int wave) {
			this.modifier = modifier;
			this.wave = wave;
		}

		public void incrementAppeared() { appearedCount++; }
		public void incrementChosen() { chosenCount++; }
		public double getPickRate() { return appearedCount == 0 ? 0.0 : ((double) chosenCount / appearedCount) * 100.0; }

		@Override
		public int compareTo(ModifierStat other)
		{
			// Sort by Rate first, then by chosen count
			int rateCompare = Double.compare(other.getPickRate(), this.getPickRate());
			return rateCompare != 0 ? rateCompare : Integer.compare(other.chosenCount, this.chosenCount);
		}
	}

	@Getter
	private static class AggregateStats
	{
		private int totalAttempts = 0;
		private int totalWavesReached = 0;
		private int wavesIncluded = 0;
		@Setter
		private long totalGpValue = 0;
		private final Map<Integer, Integer> waveCompletionCounts = new HashMap<>();

		private final Map<String, ModifierStat> modifierStats = new LinkedHashMap<>();
		private final Map<String, Integer> rewardCounts = new HashMap<>();
		private final Map<String, ItemBundle> rewardBundles = new HashMap<>();
		private final Map<String, Integer> rewardWaves = new HashMap<>();

		// Map to track the result counts for included attempts
		private final Map<String, Integer> attemptResultCounts = new HashMap<>();

		public void processAttempt(ColosseumAttemptDTO attempt, Set<Integer> activeWaves, Set<String> activeResults, Set<String> excludedTags, Set<String> includedTags, String requiredActiveModifier)
		{
			if (attempt.getResult() == null || !activeResults.contains(attempt.getResult())) return;
			if (attempt.getWaves() == null || attempt.getWaves().isEmpty()) return;

			boolean attemptIncluded = false;

			// Track active modifiers dynamically across the attempt's waves
			Map<String, String> activeModsByBase = new HashMap<>();

			for (int i = 0; i < attempt.getWaves().size(); i++)
			{
				int waveNumber = i + 1;
				ColosseumWaveDTO wave = attempt.getWaves().get(i);

				// 1. Evaluate if the required modifier is currently active (chosen in a previous wave)
				boolean hasRequiredMod = false;
				if (requiredActiveModifier == null || requiredActiveModifier.equals("Any Active Modifier")) {
					hasRequiredMod = true;
				} else {
					if (activeModsByBase.containsValue(requiredActiveModifier)) {
						hasRequiredMod = true;
					}
				}

				// 2. Update the running active modifiers state for the NEXT wave
				String chosen = wave.getChosenModifier();
				if (chosen != null) {
					String baseName = getBaseModifierName(chosen);
					if (baseName != null) {
						// This perfectly overwrites BLASPHEMY_I with BLASPHEMY_II
						activeModsByBase.put(baseName, chosen);
					}
				}

				// 3. Apply Filters
				if (!hasRequiredMod) continue;
				if (!activeWaves.contains(waveNumber)) continue;

				// Included Tags Check
				if (!includedTags.isEmpty())
				{
					boolean hasIncludedTag = false;
					if (wave.getTag() != null)
					{
						for (String tag : wave.getTag().split(","))
						{
							if (includedTags.contains(tag.trim().toLowerCase()))
							{
								hasIncludedTag = true;
								break;
							}
						}
					}
					if (!hasIncludedTag) continue; // Skip wave if it lacks the required included tags
				}

				// Excluded Tags Check
				if (!excludedTags.isEmpty() && wave.getTag() != null)
				{
					boolean hasExcludedTag = false;
					for (String tag : wave.getTag().split(","))
					{
						if (excludedTags.contains(tag.trim().toLowerCase()))
						{
							hasExcludedTag = true;
							break;
						}
					}
					if (hasExcludedTag) continue;
				}

				// If we make it here, at least one wave from this attempt is passing the filters!
				attemptIncluded = true;
				log.debug("{}", wave.getActiveModifiers());

				wavesIncluded++;
				waveCompletionCounts.put(waveNumber, waveCompletionCounts.getOrDefault(waveNumber, 0) + 1);

				// Modifier Tracking
				if (wave.getModifierChoices() != null)
				{
					for (String choice : wave.getModifierChoices())
					{
						ColosseumModifier mod = parseModifierSafe(choice);
						if (mod != null) {
							String key = waveNumber + ":" + choice;
							ModifierStat stat = modifierStats.computeIfAbsent(key, k -> new ModifierStat(mod, waveNumber));
							stat.incrementAppeared();
						}
					}
				}

				if (chosen != null)
				{
					ColosseumModifier mod = parseModifierSafe(chosen);
					if (mod != null) {
						String key = waveNumber + ":" + chosen;
						ModifierStat stat = modifierStats.computeIfAbsent(key, k -> new ModifierStat(mod, waveNumber));
						stat.incrementChosen();
					}
				}

				ItemBundle reward = wave.getEarnedLoot();
				if (reward != null)
				{
					String key = waveNumber + ":" + reward.getItemId() + ":" + reward.getQuantity();
					rewardCounts.put(key, rewardCounts.getOrDefault(key, 0) + 1);
					rewardBundles.putIfAbsent(key, reward);
					rewardWaves.putIfAbsent(key, waveNumber);
				}
			}

			// Only add to our attempt stats if it actually contributed data
			if (attemptIncluded)
			{
				totalAttempts++;
				totalWavesReached += attempt.getWaves().size();

				// Track the result of this valid attempt for the tooltip breakdown
				if (attempt.getResult() != null)
				{
					attemptResultCounts.put(attempt.getResult(), attemptResultCounts.getOrDefault(attempt.getResult(), 0) + 1);
				}
			}
		}

		public List<ModifierStat> getRankedModifiers()
		{
			List<ModifierStat> list = new ArrayList<>(modifierStats.values());
			Collections.sort(list);
			return list;
		}

		private ColosseumModifier parseModifierSafe(String name) {
			for (ColosseumModifier m : ColosseumModifier.values()) {
				if (m.name().equals(name)) return m;
			}
			return null;
		}

		// Helper to group tiers (e.g. BLASPHEMY_II -> BLASPHEMY)
		private String getBaseModifierName(String modName) {
			if (modName == null) return null;
			if (modName.endsWith("_III")) return modName.substring(0, modName.length() - 4);
			if (modName.endsWith("_II")) return modName.substring(0, modName.length() - 3);
			if (modName.endsWith("_I")) return modName.substring(0, modName.length() - 2);
			return modName;
		}

		public ColosseumModifier getMostCommonModifier()
		{
			Map<ColosseumModifier, Integer> globalCounts = new HashMap<>();
			for (ModifierStat stat : modifierStats.values()) {
				globalCounts.put(stat.getModifier(), globalCounts.getOrDefault(stat.getModifier(), 0) + stat.getChosenCount());
			}

			return globalCounts.entrySet().stream()
				.max(Map.Entry.comparingByValue())
				.filter(e -> e.getValue() > 0)
				.map(Map.Entry::getKey).orElse(null);
		}

	}

	private enum TableMode
	{
		MODIFIERS("Modifiers"),
		REWARDS("Rewards");
		private final String name;
		TableMode(String name) { this.name = name; }
		@Override public String toString() { return name; }
	}
}