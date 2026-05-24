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

import static com.datalogger.constants.PluginConstants.COLOSSEUM_TRIALS_DIR;
import com.datalogger.models.enums.UIScrollSpeed;
import static com.datalogger.ui.utils.Util.openDirectory;

import com.datalogger.constants.PluginConstants;
import com.datalogger.dto.ColosseumAttemptDTO;
import com.datalogger.dto.ColosseumWaveDTO;
import com.datalogger.models.enums.ColosseumModifier;
import com.datalogger.models.itemvault.ItemBundle;
import com.datalogger.ui.utils.Components;
import com.datalogger.ui.utils.table.ColosseumModifierTable;
import com.google.gson.Gson;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
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
	public enum SortType
	{
		DATE("Date"),
		WAVES("Waves"),
		TIME("Time"),
		GLORY("Glory"),
		REWARD("Reward"),
		SUPPLIES("Supply");

		private final String name;
		SortType(String name) { this.name = name; }
		@Override public String toString() { return name; }
	}

	private final Gson gson;
	private final ScheduledExecutorService executor;
	private final SpriteManager spriteManager;
	private final ItemManager itemManager;
	private final ClientThread clientThread;

	private final JPanel statsContainer;
	private final JPanel tableContainer;
	private final JPanel dynamicControlsPanel;
	private final PluginErrorPanel errorPanel;

	private ColosseumModifierTable modifierTableWrapper;
	private JTable rankingTable;
	private TableMode currentTableMode = TableMode.MODIFIERS;
	private final Map<Integer, ImageIcon> itemIconCache = new HashMap<>();
	private final Map<Integer, ImageIcon> modifierIconCache = new ConcurrentHashMap<>();
	private final Map<String, JPanel> attemptCardCache = new ConcurrentHashMap<>();

	private static final String BASE_MODIFIER_TOOLTIP = "<html><body style='padding: 2px; max-width: 250px;'>" +
		"If set, ONLY include waves that match your active modifier constraints.<br><br>" +
		"- Modifiers are evaluated by their exact tier state (e.g., <b>BLASPHEMY_0</b>, <b>TOTEMIC_I</b>, <b>DOOM_II</b>).<br>" +
		"- To require a specific state, type its exact name.<br>" +
		"- To exclude a specific state, prefix it with an exclamation mark (e.g., <b>!BLASPHEMY_0</b>).<br><br>" +
		"Separate multiple conditions with commas. Unrecognized states are ignored.";
	private static Dimension MODIFIER_SIZE = new Dimension(200, 35);
	private final static Dimension SCROLLPANE_PREFERRED_SIZE = new Dimension(250, 800);
	private final static Dimension SCROLLPANE_MAXIMUM_SIZE = new Dimension(Integer.MAX_VALUE, 800);
	private final static String[] TRIAL_RESULTS = {"COMPLETED", "FAILED", "CANCELLED"};
	private UIScrollSpeed scrollSpeed = UIScrollSpeed.MEDIUM;
	private JScrollBar scrollBar;

	private List<ColosseumAttemptDTO> cachedAttempts = null;
	private AggregateStats currentStats = null; // Caches the latest filtered stats state

	private final Set<Integer> activeWaves = new HashSet<>();
	private final Set<String> activeResults = new HashSet<>(List.of(TRIAL_RESULTS));

	// History Sorting State
	private SortType currentHistorySortType = SortType.DATE;
	private boolean isHistoryAscending = false;

	// Unified Stateful Text Fields
	private final JTextField includeTagsInput;
	private final JTextField excludeTagsInput;
	private final JTextField requiredModifiersInput;

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

		String includeTooltip = "<html><body style='padding: 2px; max-width: 250px;'>" +
			"If set, ONLY include waves that have at least one of the tags specified.<br>" +
			"Multiple tags can be set by separating each tag with ','; e.g. TAG_A,TAG_B</body></html>";
		includeTagsInput = Components.createStatefulTextField(text -> recalculateStatsFromCache(), includeTooltip);

		String excludeTooltip = "<html><body style='padding: 2px; max-width: 250px;'>" +
			"If set, exclude waves that have any of the tags specified.<br>" +
			"Multiple tags can be set by separating each tag with ',', e.g. TAG_A,TAG_B</body></html>";
		excludeTagsInput = Components.createStatefulTextField(text -> recalculateStatsFromCache(), excludeTooltip);

		requiredModifiersInput = Components.createStatefulTextField(text -> recalculateStatsFromCache(), BASE_MODIFIER_TOOLTIP + "</body></html>");

		statsContainer = new JPanel(new GridLayout(0, 2, 5, 5));
		statsContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		statsContainer.setBorder(new EmptyBorder(10, 0, 10, 0));
		add(statsContainer, BorderLayout.NORTH);

		JPanel centerWrapper = new JPanel(new BorderLayout());
		centerWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel tagFiltersPanel = new JPanel(new GridLayout(2, 1, 0, 5));
		tagFiltersPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		tagFiltersPanel.add(buildIncludeTagFilterPanel());
		tagFiltersPanel.add(buildExcludeTagFilterPanel());

		JPanel staticFiltersContainer = new JPanel(new BorderLayout(0, 5));
		staticFiltersContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		staticFiltersContainer.add(buildFilterPanel(), BorderLayout.NORTH);
		staticFiltersContainer.add(buildResultFilterPanel(), BorderLayout.CENTER);
		staticFiltersContainer.add(tagFiltersPanel, BorderLayout.SOUTH);

		dynamicControlsPanel = new JPanel(new BorderLayout());
		dynamicControlsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel allControlsWrapper = new JPanel(new BorderLayout(0, 5));
		allControlsWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		allControlsWrapper.setBorder(new EmptyBorder(0, 0, 10, 0)); // Margin before table
		allControlsWrapper.add(staticFiltersContainer, BorderLayout.NORTH);
		allControlsWrapper.add(dynamicControlsPanel, BorderLayout.SOUTH);

		centerWrapper.add(allControlsWrapper, BorderLayout.NORTH);

		tableContainer = new JPanel(new BorderLayout());
		tableContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		centerWrapper.add(tableContainer, BorderLayout.CENTER);

		add(centerWrapper, BorderLayout.CENTER);

		errorPanel = new PluginErrorPanel();

		modifierTableWrapper = new ColosseumModifierTable(spriteManager);

		loadStatisticsFromDisk();
	}

	private JPanel buildResultFilterPanel()
	{
		JPanel panel = Components.createTitledPanel("Filter Result", new GridLayout(1, 3, 5, 2));


		for (String result : TRIAL_RESULTS)
		{
			JButton btn = new JButton(result);
			btn.setFocusable(false);
			btn.setFont(FontManager.getRunescapeSmallFont());
			btn.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
			btn.setForeground(Color.WHITE);
			btn.setBorder(BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR, 1));
			btn.setToolTipText("Toggle to include/exclude " + result.toLowerCase() + " trials");

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
		JPanel panel = Components.createTitledPanel("Include Tags (comma-separated)", new BorderLayout());
		panel.add(includeTagsInput, BorderLayout.CENTER);
		return panel;
	}

	private JPanel buildExcludeTagFilterPanel()
	{
		JPanel panel = Components.createTitledPanel("Exclude Tags (comma-separated)", new BorderLayout());
		panel.add(excludeTagsInput, BorderLayout.CENTER);
		return panel;
	}

	private JPanel buildFilterPanel()
	{
		JPanel mainPanel = Components.createTitledPanel("Filter Waves", new BorderLayout(0, 5));

		JPanel wavesPanel = new JPanel(new GridLayout(2, 6, 2, 2));
		wavesPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		List<JButton> waveButtons = new ArrayList<>();

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
			waveButton.setToolTipText("Toggle inclusion of wave " + i);

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

			waveButtons.add(waveButton);
			wavesPanel.add(waveButton);
		}

		JPanel actionsPanel = new JPanel(new GridLayout(1, 2, 5, 0));
		actionsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JButton includeAllBtn = new JButton("Include All");
		includeAllBtn.setFocusable(false);
		includeAllBtn.setFont(FontManager.getRunescapeSmallFont());
		includeAllBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		includeAllBtn.setForeground(Color.WHITE);
		includeAllBtn.setBorder(BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR, 1));
		includeAllBtn.setToolTipText("Click to include all wave numbers");
		includeAllBtn.addActionListener(e -> {
			for (int i = 1; i <= 12; i++) {
				activeWaves.add(i);
				JButton btn = waveButtons.get(i - 1);
				btn.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
				btn.setForeground(Color.WHITE);
			}
			recalculateStatsFromCache();
		});

		JButton excludeAllBtn = new JButton("Exclude All");
		excludeAllBtn.setFocusable(false);
		excludeAllBtn.setFont(FontManager.getRunescapeSmallFont());
		excludeAllBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		excludeAllBtn.setForeground(Color.WHITE);
		excludeAllBtn.setBorder(BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR, 1));
		excludeAllBtn.setToolTipText("Click to exclude all wave numbers");
		excludeAllBtn.addActionListener(e -> {
			activeWaves.clear();
			for (JButton btn : waveButtons) {
				btn.setBackground(ColorScheme.DARK_GRAY_COLOR);
				btn.setForeground(Color.GRAY);
			}
			recalculateStatsFromCache();
		});

		actionsPanel.add(includeAllBtn);
		actionsPanel.add(excludeAllBtn);

		mainPanel.add(wavesPanel, BorderLayout.CENTER);
		mainPanel.add(actionsPanel, BorderLayout.SOUTH);

		return mainPanel;
	}

	/**
	 * Load registered trial data by parsing the internal history json file
	 */
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
			File historyFile = PluginConstants.INTERNAL_COLOSSEUM_TRIAL_HISTORY;

			if (historyFile.exists())
			{
				try (BufferedReader reader = new BufferedReader(new FileReader(historyFile)))
				{
					String line;
					while ((line = reader.readLine()) != null)
					{
						if (line.trim().isEmpty()) continue;

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

	/**
	 *
	 */
	private void recalculateStatsFromCache()
	{
		if (cachedAttempts == null) return;

		String rawIncludeTags = includeTagsInput.getText();
		Set<String> includedTags = new HashSet<>();
		if (rawIncludeTags != null && !rawIncludeTags.trim().isEmpty()) {
			for (String tag : rawIncludeTags.split(",")) {
				if (!tag.trim().isEmpty()) {
					includedTags.add(tag.trim().toLowerCase());
				}
			}
		}

		String rawExcludeTags = excludeTagsInput.getText();
		Set<String> excludedTags = new HashSet<>();
		if (rawExcludeTags != null && !rawExcludeTags.trim().isEmpty()) {
			for (String tag : rawExcludeTags.split(",")) {
				if (!tag.trim().isEmpty()) {
					excludedTags.add(tag.trim().toLowerCase());
				}
			}
		}

		String rawRequiredMods = requiredModifiersInput.getText();
		Set<String> requiredMods = new HashSet<>();
		Set<String> excludedMods = new HashSet<>();

		List<String> validStates = AggregateStats.getAllModifierStates();

		if (rawRequiredMods != null && !rawRequiredMods.trim().isEmpty()) {
			for (String mod : rawRequiredMods.split(",")) {
				String trimmed = mod.trim().toUpperCase();
				if (!trimmed.isEmpty()) {
					if (trimmed.startsWith("!")) {
						String stripped = trimmed.substring(1).trim();
						if (validStates.contains(stripped)) {
							// FIX: Map tierless inputs to their _I internal equivalent
							excludedMods.add(addTier(stripped));
						}
					} else {
						if (validStates.contains(trimmed)) {
							// FIX: Map tierless inputs to their _I internal equivalent
							requiredMods.add(addTier(trimmed));
						}
					}
				}
			}
		}

		updateModifierTooltip(requiredMods, excludedMods);

		executor.submit(() -> {
			AggregateStats stats = new AggregateStats();
			for (ColosseumAttemptDTO attempt : cachedAttempts)
			{
				stats.processAttempt(attempt, activeWaves, activeResults, excludedTags, includedTags, requiredMods, excludedMods);
			}
			for (WaveStat wStat : stats.getWaveStats().values()) {
				wStat.sortLists();
			}

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
				this.currentStats = stats;

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

		JPanel selectorsPanel = Components.createTitledPanel("Data type", new BorderLayout(0, 5));
		selectorsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JComboBox<TableMode> tableModeSelector = Components.createComboBox(TableMode.values());
		tableModeSelector.setSelectedItem(currentTableMode);
		tableModeSelector.setToolTipText("Select the type of data to fill the table below with");
		tableModeSelector.addActionListener(e -> {
			currentTableMode = (TableMode) tableModeSelector.getSelectedItem();
			recalculateStatsFromCache();
		});
		selectorsPanel.add(tableModeSelector, BorderLayout.NORTH);

		JPanel activeModPanel = Components.createTitledPanel("Modifier prerequisites", new BorderLayout(0, 5));
		List<String> allStates = AggregateStats.getAllModifierStates();

		JComboBox<String> requireHelperBox = Components.createComboBox();
		requireHelperBox.addItem("Require...");
		for (String state : allStates) requireHelperBox.addItem(state);
		requireHelperBox.setToolTipText("Select a required modifier to add to the textfield below");

		JComboBox<String> excludeHelperBox = Components.createComboBox();
		excludeHelperBox.addItem("Exclude...");
		for (String state : allStates) excludeHelperBox.addItem("!" + state);
		excludeHelperBox.setToolTipText("Select an excluded modifier to add to the textfield below");

		ActionListener helperAction = e -> {
			@SuppressWarnings("unchecked")
			JComboBox<String> source = (JComboBox<String>) e.getSource();
			String selected = (String) source.getSelectedItem();

			if (selected != null && !selected.startsWith("---") && !selected.equals("Require...") && !selected.equals("Exclude...")) {
				String currentText = requiredModifiersInput.getText().trim();

				if (currentText.isEmpty() || currentText.endsWith(",")) {
					requiredModifiersInput.setText(currentText + (currentText.isEmpty() ? "" : " ") + selected);
				} else {
					requiredModifiersInput.setText(currentText + ", " + selected);
				}

				source.setSelectedIndex(0);
				recalculateStatsFromCache();
			}
		};

		requireHelperBox.addActionListener(helperAction);
		excludeHelperBox.addActionListener(helperAction);

		JPanel comboContainer = new JPanel(new GridLayout(1, 2, 5, 0));
		comboContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		comboContainer.add(requireHelperBox);
		comboContainer.add(excludeHelperBox);

		activeModPanel.add(comboContainer, BorderLayout.NORTH);
		activeModPanel.add(requiredModifiersInput, BorderLayout.CENTER);
//		selectorsPanel.add(activeModPanel, BorderLayout.CENTER);

		dynamicControlsPanel.add(selectorsPanel, BorderLayout.NORTH);
		dynamicControlsPanel.add(activeModPanel, BorderLayout.CENTER);

		if (stats.getTotalAttempts() == 0)
		{
			statsContainer.setLayout(new BorderLayout());
			errorPanel.setContent("No Data", "No runs match your current filters. Adjust them to see data.");
			statsContainer.add(errorPanel, BorderLayout.CENTER);
		}
		else
		{
			StringBuilder trialsTooltip = new StringBuilder("<html><body style='padding: 2px; max-width: 250px;'>");
			trialsTooltip.append("Number of trials with one or more waves included.<br><br>");
			int completed = stats.getAttemptResultCounts().getOrDefault("COMPLETED", 0);
			int cancelled = stats.getAttemptResultCounts().getOrDefault("CANCELLED", 0);
			int failed = stats.getAttemptResultCounts().getOrDefault("FAILED", 0);
			trialsTooltip.append("<span style='color: #a5a5a5;'>Completed:</span> <span style='color: white;'>").append(completed).append("</span><br>");
			trialsTooltip.append("<span style='color: #a5a5a5;'>Cancelled:</span> <span style='color: white;'>").append(cancelled).append("</span><br>");
			trialsTooltip.append("<span style='color: #a5a5a5;'>Failed:</span> <span style='color: white;'>").append(failed).append("</span>");
			trialsTooltip.append("</body></html>");

			statsContainer.add(Components.createStatCard("N trials", String.valueOf(stats.getTotalAttempts()), trialsTooltip.toString()));

			double avgWave = stats.getTotalAttempts() > 0 ? (double) stats.getTotalWavesReached() / stats.getTotalAttempts() : 0;
			statsContainer.add(Components.createStatCard("Avg Wave", String.format("%.1f", avgWave), null));

			StringBuilder waveBreakdown = new StringBuilder("<html><body style='padding: 2px;'>");
			waveBreakdown.append("<b style='color: white;'>Wave Breakdown:</b><br>");


			boolean hasWaves = false;
			int total = 0;
			for (int i = 1; i <= 12; i++)
			{
				int count = stats.getWaveCompletionCounts().getOrDefault(i, 0);
				total += count;
				if (count > 0)
				{
					waveBreakdown.append("<span style='color: #a5a5a5;'>Wave ").append(i).append(":</span> ")
						.append("<span style='color: white;'>").append(count).append("</span><br>");
					hasWaves = true;
				}
			}
			waveBreakdown.append("<span style='color: #a5a5a5;'>Total:</span> ")
				.append("<span style='color: white;'>").append(total).append("</span><br></body></html>");

			String tooltipText = hasWaves ? waveBreakdown.toString() : "No waves matched the current filters.";
			statsContainer.add(Components.createStatCard("Waves Included", String.format("%,d", stats.getWavesIncluded()), tooltipText));

			if (currentTableMode == TableMode.MODIFIERS)
			{
				ColosseumModifier favMod = stats.getMostCommonModifier();
				statsContainer.add(Components.createStatCard("Fav Modifier", favMod != null ? favMod.getUiLabel() : "N/A", null));
			}
			else
			{
				double avgValue = stats.getTotalAttempts() > 0 ? (double) stats.getTotalGpValue() / stats.getTotalAttempts() : 0;
				String formattedTotal = QuantityFormatter.quantityToStackSize(stats.getTotalGpValue()) + " gp";
				String rewardsTooltip = "<html><body style='padding: 2px; max-width: 250px;'>" + "Average reward value per trial, summed across<br>all of the waves that have not been filtered out<br>" +
					"<b style='color: white;'>Total gp:</b> " + formattedTotal + "<br>";
				statsContainer.add(Components.createStatCard("Avg Value/trial", String.format("%,.0f gp", avgValue), rewardsTooltip));
			}

			switch (currentTableMode) {
				case MODIFIERS:
					buildModifiersView(stats);
					break;
				case HISTORY:
					buildHistoryView(stats);
					break;
				case REWARDS:
					buildRewardsView(stats);
					break;
				case WAVES:
					buildWavesView(stats);
					break;
			}
		}

		revalidate();
		repaint();
	}

	private void buildModifiersView(AggregateStats stats)
	{
		tableContainer.removeAll();

		DefaultTableModel statsModel = buildModifierTable(stats);
		modifierTableWrapper.updateData(statsModel);

		rankingTable = modifierTableWrapper.getTable();
		rankingTable.putClientProperty("waveCompletionCounts", stats.getWaveCompletionCounts());

		JPanel sortButtonHeader = new JPanel(new java.awt.GridLayout(1, 0, 4, 0));
		sortButtonHeader.setBackground(ColorScheme.DARK_GRAY_COLOR);
		sortButtonHeader.setBorder(new javax.swing.border.EmptyBorder(5, 5, 5, 5));

		for (ColosseumModifierTable.ModifierSortType type : ColosseumModifierTable.ModifierSortType.values())
		{
			JButton sortBtn = new JButton(type.toString());
			sortBtn.setFont(FontManager.getRunescapeSmallFont());
			sortBtn.setForeground(java.awt.Color.GRAY);
			sortBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			sortBtn.setFocusable(false);
			sortBtn.setBorder(javax.swing.BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_HOVER_COLOR, 1));
			sortBtn.addActionListener(e -> modifierTableWrapper.toggleSortType(type));

			sortButtonHeader.add(sortBtn);
		}

		JScrollPane scrollPane = Components.wrapWithRuneLiteScrollbar(rankingTable);

		scrollPane.setPreferredSize(SCROLLPANE_PREFERRED_SIZE);
		scrollPane.setMaximumSize(SCROLLPANE_MAXIMUM_SIZE);
		scrollBar = scrollPane.getVerticalScrollBar();
		setScrollSpeed(scrollSpeed);

		tableContainer.setLayout(new BoxLayout(tableContainer, BoxLayout.Y_AXIS));

		sortButtonHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, sortButtonHeader.getPreferredSize().height));

		sortButtonHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

		tableContainer.add(sortButtonHeader);
		tableContainer.add(scrollPane);

		tableContainer.revalidate();
		tableContainer.repaint();
	}

	private void buildRewardsView(AggregateStats stats)
	{
		tableContainer.removeAll();

		DefaultTableModel tableModel = buildRewardsTable(stats);
		rankingTable = new JTable(tableModel);

		rankingTable.getColumnModel().getColumn(0).setCellRenderer(new ItemIconCellRenderer());

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		rankingTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
		rankingTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

		rankingTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			protected void setValue(Object value) {
				setText(value instanceof Double ? String.format("%.1f%%", (Double) value) : "");
				setHorizontalAlignment(JLabel.CENTER);
			}
		});

		rankingTable.getColumnModel().getColumn(0).setPreferredWidth(50);
		rankingTable.getColumnModel().getColumn(1).setPreferredWidth(40);
		rankingTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		rankingTable.getColumnModel().getColumn(3).setPreferredWidth(60);

		rankingTable.setAutoCreateRowSorter(true);
		rankingTable.getRowSorter().setSortKeys(Collections.singletonList(
			new javax.swing.RowSorter.SortKey(1, javax.swing.SortOrder.ASCENDING)
		));

		rankingTable.setBackground(ColorScheme.DARK_GRAY_COLOR);
		rankingTable.setForeground(Color.WHITE);
		rankingTable.setGridColor(ColorScheme.DARKER_GRAY_COLOR);
		rankingTable.getTableHeader().setBackground(ColorScheme.DARKER_GRAY_COLOR);
		rankingTable.getTableHeader().setForeground(Color.WHITE);
		rankingTable.setRowHeight(42);

		JScrollPane scrollPane = Components.wrapWithRuneLiteScrollbar(rankingTable);
		scrollPane.setPreferredSize(SCROLLPANE_PREFERRED_SIZE);
		scrollPane.setMaximumSize(SCROLLPANE_MAXIMUM_SIZE);
		tableContainer.setLayout(new BoxLayout(tableContainer, BoxLayout.Y_AXIS));
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollBar = scrollPane.getVerticalScrollBar();
		setScrollSpeed(scrollSpeed);

		tableContainer.add(scrollPane);

		tableContainer.revalidate();
		tableContainer.repaint();
	}

	private void buildHistoryView(AggregateStats stats)
	{
		tableContainer.removeAll();

		JPanel sortButtonHeader = new JPanel(new GridLayout(2, 3, 4, 4));
		sortButtonHeader.setBackground(ColorScheme.DARK_GRAY_COLOR);
		sortButtonHeader.setBorder(new EmptyBorder(5, 5, 5, 5));

		for (SortType type : SortType.values())
		{
			JButton sortBtn = new JButton(type.toString());
			sortBtn.setFont(FontManager.getRunescapeSmallFont());
			sortBtn.setFocusable(false);
			sortBtn.setBorder(BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_HOVER_COLOR, 1));

			if (type == currentHistorySortType) {
				sortBtn.setText(type.toString() + (isHistoryAscending ? " \u25B2" : " \u25BC"));
				sortBtn.setForeground(Color.WHITE);
				sortBtn.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
			} else {
				sortBtn.setForeground(Color.GRAY);
				sortBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			}

			sortBtn.addActionListener(e -> {
				if (currentHistorySortType == type) {
					isHistoryAscending = !isHistoryAscending;
				} else {
					currentHistorySortType = type;
					isHistoryAscending = false;
				}
				if (currentStats != null) {
					buildHistoryView(currentStats);
				}
			});

			sortButtonHeader.add(sortBtn);
		}

		JPanel listContainer = new JPanel();
		listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
		listContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel loadingLabel = new JLabel("Sorting history...");
		loadingLabel.setForeground(Color.GRAY);
		loadingLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
		listContainer.add(loadingLabel);

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wrapper.add(listContainer, BorderLayout.NORTH);

		JScrollPane scrollPane = Components.wrapWithRuneLiteScrollbar(wrapper);
		scrollPane.setPreferredSize(SCROLLPANE_PREFERRED_SIZE);
		scrollPane.setMaximumSize(SCROLLPANE_MAXIMUM_SIZE);

		tableContainer.setLayout(new BoxLayout(tableContainer, BoxLayout.Y_AXIS));

		sortButtonHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, sortButtonHeader.getPreferredSize().height));

		sortButtonHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

		scrollBar = scrollPane.getVerticalScrollBar();
		setScrollSpeed(scrollSpeed);

		JLabel onClickInstructionLabel = new JLabel("Click an entry to open its directory");
		tableContainer.add(onClickInstructionLabel);
		tableContainer.add(sortButtonHeader);
		tableContainer.add(scrollPane);

		tableContainer.revalidate();
		tableContainer.repaint();

		executor.submit(() -> {
			List<ColosseumAttemptDTO> attempts = new ArrayList<>(stats.getIncludedAttempts());

			Comparator<ColosseumAttemptDTO> comp;
			switch (currentHistorySortType) {
				case WAVES:
					comp = Comparator.comparingInt(a -> a.getWaves() != null ? a.getWaves().size() : 0);
					break;
				case TIME:
					comp = Comparator.comparingDouble(ColosseumAttemptDTO::getTotalTime);
					break;
				case GLORY:
					comp = Comparator.comparingInt(ColosseumAttemptDTO::getTotalGlory);
					break;
				case REWARD:
					comp = Comparator.comparingInt(ColosseumAttemptDTO::getRewardsValue);
					break;
				case SUPPLIES:
					comp = Comparator.comparingInt(ColosseumAttemptDTO::getConsumedSupplyValue);
					break;
				case DATE:
				default:
					comp = Comparator.comparingLong(ColosseumAttemptDTO::getTimestamp);
					break;
			}

			if (!isHistoryAscending) comp = comp.reversed();
			attempts.sort(comp);

			final int maxCardsToDisplay = 100;
			List<ColosseumAttemptDTO> topAttempts = attempts.size() > maxCardsToDisplay
				? attempts.subList(0, maxCardsToDisplay)
				: attempts;
			boolean wasLimited = attempts.size() > maxCardsToDisplay;

			SwingUtilities.invokeLater(() -> {
				listContainer.removeAll();

				if (topAttempts.isEmpty())
				{
					PluginErrorPanel emptyPanel = new PluginErrorPanel();
					emptyPanel.setContent("No History Found", "No attempts match the current filters.");
					listContainer.add(emptyPanel);
				}
				else
				{
					for (ColosseumAttemptDTO attempt : topAttempts)
					{
						JPanel card = buildAttemptCard(attempt);
						if (card != null) listContainer.add(card);
					}

					if (wasLimited)
					{
						JLabel limitLabel = new JLabel(String.format("Showing top %d results. Use filters to narrow down.", maxCardsToDisplay));
						limitLabel.setFont(FontManager.getRunescapeSmallFont());
						limitLabel.setForeground(Color.GRAY);
						limitLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
						limitLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
						listContainer.add(limitLabel);
					}
				}

				listContainer.revalidate();
				listContainer.repaint();
			});
		});
	}

	private void buildWavesView(AggregateStats stats)
	{
		tableContainer.removeAll();

		JPanel listContainer = new JPanel();
		listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
		listContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		boolean hasData = false;

		for (int i = 1; i <= 12; i++)
		{
			WaveStat wStat = stats.getWaveStats().get(i);

			if (wStat != null && wStat.getTimesReached() > 0)
			{
				JPanel card = buildWaveCard(wStat);
				listContainer.add(card);
				hasData = true;
			}
		}

		if (!hasData)
		{
			PluginErrorPanel emptyPanel = new PluginErrorPanel();
			emptyPanel.setContent("No Data", "No wave data matches the current filters.");
			listContainer.add(emptyPanel);
		}

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wrapper.add(listContainer, BorderLayout.NORTH);

		JScrollPane scrollPane = Components.wrapWithRuneLiteScrollbar(wrapper);
		scrollPane.setPreferredSize(SCROLLPANE_PREFERRED_SIZE);
		scrollPane.setMaximumSize(SCROLLPANE_MAXIMUM_SIZE);
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

		scrollBar = scrollPane.getVerticalScrollBar();
		setScrollSpeed(scrollSpeed);

		tableContainer.setLayout(new BoxLayout(tableContainer, BoxLayout.Y_AXIS));
		tableContainer.add(scrollPane);

		tableContainer.revalidate();
		tableContainer.repaint();
	}

	private JPanel buildWaveCard(WaveStat waveStat)
	{
		JPanel card = new JPanel(new BorderLayout(5, 5));
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(new EmptyBorder(8, 8, 8, 8));

		JPanel marginWrapper = new JPanel(new BorderLayout());
		marginWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		marginWrapper.setBorder(new EmptyBorder(0, 0, 8, 0));
		marginWrapper.add(card, BorderLayout.CENTER);

		JLabel titleLabel = new JLabel("Wave " + waveStat.getWaveNumber());
		titleLabel.setFont(FontManager.getRunescapeBoldFont());
		titleLabel.setForeground(Color.WHITE);

		// --- Core Counts & Percentages ---
		int reached = waveStat.getTimesReached();

		// Safely calculate percentages (avoiding division by zero just in case)
		double completedPct = reached > 0 ? (waveStat.getTimesCompleted() / (double) reached) * 100.0 : 0.0;
		double failedPct = reached > 0 ? (waveStat.getTimesFailed() / (double) reached) * 100.0 : 0.0;
		double cancelledPct = reached > 0 ? (waveStat.getTimesCancelled() / (double) reached) * 100.0 : 0.0;
		double hitlessPct = reached > 0 ? (waveStat.getHitlessRuns() / (double) reached) * 100.0 : 0.0;

		JLabel reachedLabel = new JLabel("Times Reached: " + reached);
		reachedLabel.setForeground(Color.LIGHT_GRAY);

		JLabel completedLabel = new JLabel(String.format("Completed: %d (%.1f%%)", waveStat.getTimesCompleted(), completedPct));
		completedLabel.setForeground(Color.WHITE);

		JLabel failedLabel = new JLabel(String.format("Failed: %d (%.1f%%)", waveStat.getTimesFailed(), failedPct));
		failedLabel.setForeground(Color.WHITE);

		JLabel cancelledLabel = new JLabel(String.format("Cancelled: %d (%.1f%%)", waveStat.getTimesCancelled(), cancelledPct));
		cancelledLabel.setForeground(Color.WHITE);

		JLabel hitlessLabel = new JLabel(String.format("Hitless Runs: %d (%.1f%%)", waveStat.getHitlessRuns(), hitlessPct));
		hitlessLabel.setForeground(Color.WHITE);

		double avgTime = waveStat.getTimeTaken().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
		int m = (int) (avgTime / 60);
		int s = (int) (avgTime % 60);
		int ms = (int) Math.round((avgTime % 1) * 10);
		if (ms == 10) { ms = 0; s++; if (s == 60) { s = 0; m++; } }
		String formattedTime = String.format("%02d:%02d.%d", m, s, ms);
		JLabel avgTimeLabel = new JLabel("Avg Time: " + formattedTime);
		avgTimeLabel.setForeground(Color.WHITE);

		// Glory
		int avgModGlory = (int) waveStat.getModifierGlory().stream().mapToInt(Integer::intValue).average().orElse(0);
		JLabel avgModGloryLabel = new JLabel("Avg Modifier Glory: " + avgModGlory);
		avgModGloryLabel.setForeground(Color.WHITE);

		int avgTotalGlory = (int) waveStat.getTotalGlory().stream().mapToInt(Integer::intValue).average().orElse(0);
		JLabel avgTotalGloryLabel = new JLabel("Avg Total Glory: " + avgTotalGlory);
		avgTotalGloryLabel.setForeground(Color.WHITE);

		// Rewards
		int avgReward = (int) waveStat.getRewardValue().stream().mapToInt(Integer::intValue).average().orElse(0);
		JLabel avgRewardLabel = new JLabel("Avg Reward: " + QuantityFormatter.quantityToStackSize(avgReward));
		avgRewardLabel.setForeground(Color.WHITE);

		// --- Alignments ---
		titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		reachedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		completedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		failedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		cancelledLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		hitlessLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		avgTimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		avgModGloryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		avgTotalGloryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		avgRewardLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		// --- Panel Assembly ---
		JPanel centerPanel = new JPanel();
		centerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

		centerPanel.add(titleLabel);
		centerPanel.add(Box.createVerticalStrut(4));

		centerPanel.add(reachedLabel);
		centerPanel.add(Box.createVerticalStrut(2));
		centerPanel.add(completedLabel);
		centerPanel.add(Box.createVerticalStrut(2));
		centerPanel.add(failedLabel);
		centerPanel.add(Box.createVerticalStrut(2));
		centerPanel.add(cancelledLabel);
		centerPanel.add(Box.createVerticalStrut(2));
		centerPanel.add(hitlessLabel);

		centerPanel.add(Box.createVerticalStrut(6)); // Slightly larger gap to separate counts from averages

		centerPanel.add(avgTimeLabel);
		centerPanel.add(Box.createVerticalStrut(2));
		centerPanel.add(avgModGloryLabel);
		centerPanel.add(Box.createVerticalStrut(2));
		centerPanel.add(avgTotalGloryLabel);
		centerPanel.add(Box.createVerticalStrut(2));
		centerPanel.add(avgRewardLabel);

		card.add(centerPanel, BorderLayout.CENTER);

		return marginWrapper;
	}

	private JPanel buildAttemptCard(ColosseumAttemptDTO attempt)
	{
		int waves = attempt.getWaves() != null ? attempt.getWaves().size() : 0;
		if (waves == 0) return null;

		String attemptId = attempt.getAttemptId();
		if (attemptCardCache.containsKey(attemptId)) {
			return attemptCardCache.get(attemptId);
		}

		JPanel card = new JPanel(new BorderLayout(5, 5));
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(new EmptyBorder(2, 2, 2, 8));

		JPanel marginWrapper = new JPanel(new BorderLayout());
		marginWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		marginWrapper.setBorder(new EmptyBorder(2, 0, 2, 0));
		marginWrapper.add(card, BorderLayout.CENTER);

		long timestamp = attempt.getTimestamp();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
		String formattedDate = formatter.format(Instant.ofEpochMilli(timestamp));

		String accName = attempt.getAccountName();
		JLabel dateLabel = new JLabel(String.format("%s | %s", accName, formattedDate));
		dateLabel.setForeground(Color.WHITE);

		String status = attempt.getResult();
		JLabel wavesLabel = new JLabel(String.format("Waves: %s | Status: %s", waves, status));
		wavesLabel.setForeground(Color.WHITE);

		int rewardValue = attempt.getRewardsValue();
		JLabel rewardsLabel = new JLabel(String.format("Reward value: %s", QuantityFormatter.quantityToStackSize(rewardValue)));
		rewardsLabel.setForeground(Color.WHITE);

		int supplyValue = attempt.getConsumedSupplyValue();
		String supplyLabelText = String.format("Supply value: %s", supplyValue > 0 ? QuantityFormatter.quantityToStackSize(supplyValue) : "NA");
		JLabel supplyValueLabel = new JLabel(supplyLabelText);
		supplyValueLabel.setForeground(Color.WHITE);
		supplyValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		double timeTaken = attempt.getTotalTime();
		int m = (int) (timeTaken / 60);
		int s = (int) (timeTaken % 60);
		int ms = (int) Math.round((timeTaken % 1) * 10);
		if (ms == 10) { ms = 0; s++; if (s == 60) { s = 0; m++; } }
		String formattedTime = String.format("%02d:%02d.%d", m, s, ms);

		JLabel timeLabel = new JLabel(String.format("Time taken: %s", formattedTime));
		timeLabel.setForeground(Color.WHITE);

		JLabel gloryLabel = new JLabel(String.format("Total glory: %d", attempt.getTotalGlory()));
		gloryLabel.setForeground(Color.WHITE);



		JPanel modifierPanel = generateModifierPanel(attempt);

		dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		wavesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		rewardsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		gloryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		modifierPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel centerPanel = new JPanel();
		centerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		Component vStrut = Box.createVerticalStrut(2);

		centerPanel.add(dateLabel);
		centerPanel.add(vStrut);
		centerPanel.add(wavesLabel);

		String tag = attempt.getTag();
		if (tag != null)
		{
			JLabel tagLabel = new JLabel(String.format("Tag: %s", tag));
			tagLabel.setForeground(Color.WHITE);
			tagLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			centerPanel.add(vStrut);
			centerPanel.add(tagLabel);
		}

		centerPanel.add(vStrut);
		centerPanel.add(timeLabel);
		centerPanel.add(vStrut);
		centerPanel.add(gloryLabel);
		centerPanel.add(vStrut);
		centerPanel.add(rewardsLabel);
		centerPanel.add(vStrut);
		centerPanel.add(supplyValueLabel);
		centerPanel.add(vStrut);

		centerPanel.add(modifierPanel);
		centerPanel.add(vStrut);

		card.add(centerPanel, BorderLayout.CENTER);

		File attemptDir = new File(COLOSSEUM_TRIALS_DIR, attempt.getAttemptId());

		card.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent evt) {
				card.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
				centerPanel.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent evt) {
				card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
				centerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			}

			@Override
			public void mouseClicked(MouseEvent evt) {
				if (SwingUtilities.isLeftMouseButton(evt)) {
					if (attemptDir.exists() && attemptDir.isDirectory()) {
						openDirectory(attemptDir, executor);
					}
				}
			}
		});

//		card.setToolTipText(String.format("w:%d h:%d", card.getWidth(), card.getHeight()));
		attemptCardCache.put(attempt.getAttemptId(), marginWrapper);

		return marginWrapper;
	}

	/**
	 * Returns a panel with the active modifier icons of the given Colosseum trial
	 */
	private JPanel generateModifierPanel(ColosseumAttemptDTO attempt)
	{

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setOpaque(false);

		List<JPanel> rows = new ArrayList<>();
		for (int i = 0; i < 2; i++)
		{
			JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
			row.setOpaque(false);

			row.setPreferredSize(MODIFIER_SIZE);
			row.setMinimumSize(MODIFIER_SIZE);

			row.setAlignmentX(Component.LEFT_ALIGNMENT);

			mainPanel.add(row);
			rows.add(row);
		}

		List<String> modifiers = attempt.getActiveModifiers();
		if (modifiers == null || modifiers.isEmpty())
		{
			return mainPanel;
		}

		int iconCount = 0;
		for (String modStr : modifiers)
		{
			ColosseumModifier mod = ColosseumModifier.fromUiLabel(modStr);
			if (mod == null)
			{
				try {
					mod = ColosseumModifier.valueOf(modStr.toUpperCase());
				} catch (IllegalArgumentException e) {
					log.warn("Unknown modifier found in history: {}", modStr);
					continue;
				}
			}

			int rowIndex = iconCount / 6;

			while (rows.size() <= rowIndex)
			{
				JPanel newRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
				newRow.setOpaque(false);
				newRow.setPreferredSize(MODIFIER_SIZE);
				newRow.setMinimumSize(MODIFIER_SIZE);
				newRow.setAlignmentX(Component.LEFT_ALIGNMENT);

				mainPanel.add(newRow);
				rows.add(newRow);
			}

			JPanel targetRow = rows.get(rowIndex);

			JLabel iconLabel = new JLabel();
			iconLabel.setToolTipText(mod.getUiLabel());

			targetRow.add(iconLabel);
			iconCount++;

			final int spriteId = mod.getSpriteId();

			if (modifierIconCache.containsKey(spriteId))
			{
				ImageIcon icon = modifierIconCache.get(spriteId);
				iconLabel.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
				iconLabel.setIcon(icon);
			}
			else
			{
				spriteManager.getSpriteAsync(spriteId, 0, img -> {
					if (img != null)
					{
//						Image scaledImg = img.getScaledInstance(img.getWidth(), img.getHeight(), Image.SCALE_SMOOTH);
						Image scaledImg = img.getScaledInstance((int)(img.getWidth()/1.1), (int)(img.getHeight()/1.1), Image.SCALE_SMOOTH);
						ImageIcon scaledIcon = new ImageIcon(scaledImg);
						modifierIconCache.put(spriteId, scaledIcon);
						iconLabel.setPreferredSize(new Dimension(scaledIcon.getIconWidth(), scaledIcon.getIconHeight()));
						iconLabel.setIcon(scaledIcon);

						SwingUtilities.invokeLater(() -> {
							iconLabel.setIcon(scaledIcon);
							iconLabel.revalidate();
							iconLabel.repaint();
						});
					}
				});
			}

		}

		return mainPanel;
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

			int specificWaveTotal = stats.getWaveCompletionCounts().getOrDefault(wave, 0);
			double rate = specificWaveTotal > 0 ? (double) frequency / specificWaveTotal * 100.0 : 0.0;

			tableModel.addRow(new Object[]{bundle, wave, frequency, rate});
		});

		return tableModel;
	}

	private DefaultTableModel buildModifierTable(AggregateStats stats)
	{
		String[] columnNames = {"Modifier Data"};
		DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0)
		{
			@Override public boolean isCellEditable(int row, int col) { return false; }
			@Override public Class<?> getColumnClass(int col) { return ModifierStat.class; }
		};

		for (ModifierStat stat : stats.getRankedModifiers()) tableModel.addRow(new Object[]{ stat });
		return tableModel;
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
	public static class WaveStat
	{
		private final int waveNumber;
		private int timesReached = 0;
		private int timesCompleted = 0;
		private int timesFailed = 0;
		private int timesCancelled = 0;
		private int hitlessRuns = 0;

		private final List<Double> timeTaken = new ArrayList<>();
		private final List<Integer> modifierGlory = new ArrayList<>();
		private final List<Integer> totalGlory = new ArrayList<>();
		private final List<Integer> rewardValue = new ArrayList<>();

		public WaveStat(int waveNumber) {
			this.waveNumber = waveNumber;
		}

		private void incrementReached() { timesReached++; }
		private void incrementCompleted() { timesCompleted++; }
		private void incrementCancelled() { timesCancelled++; }
		private void incrementFailed() { timesFailed++; }
		private void incrementHitlessRuns() { hitlessRuns++; }

		/**
		 * Register wave data of the given ColosseumWaveDTO
		 */
		public void insertWaveData(ColosseumWaveDTO waveDTO)
		{
			incrementReached();

			if (waveDTO.getStatus() != null)
			{
				switch (waveDTO.getStatus().toUpperCase()) {
					case "COMPLETED":
						incrementCompleted();
						if (waveDTO.getDamageTaken() == 0)
							incrementHitlessRuns();
						break;
					case "FAILED": incrementFailed(); break;
					case "CANCELLED": incrementCancelled(); break;
				}
			}

			timeTaken.add(waveDTO.getTimeTaken());
			modifierGlory.add(ColosseumModifier.calculateTotalGlory(waveDTO.getActiveModifiers()));
			totalGlory.add(waveDTO.getTotalGlory());

			int lootValue = waveDTO.getLootValue();
			if (lootValue > 0) rewardValue.add(lootValue);
		}

		/**
		 * Sort all the lists in ascending order.
		 * MUST be called after all data is inserted, before rendering the UI!
		 */
		public void sortLists()
		{
			Collections.sort(timeTaken);
			Collections.sort(modifierGlory);
			Collections.sort(totalGlory);
			Collections.sort(rewardValue);
		}

		public double getAverageTime() {
			if (timeTaken.isEmpty()) return 0;
			return timeTaken.stream().mapToDouble(Double::doubleValue).average().orElse(0);
		}

		public int getAverageRewardValue() {
			if (rewardValue.isEmpty()) return 0;
			return (int) rewardValue.stream().mapToInt(Integer::intValue).average().orElse(0);
		}
	}

	@Getter
	public static class ModifierStat implements Comparable<ModifierStat>
	{
		private final ColosseumModifier modifier;
		private final int wave;
		private int appearedCount = 0;
		private int chosenCount = 0;
		@Setter
		private int totalCount = 0;

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

		private final List<ColosseumAttemptDTO> includedAttempts = new ArrayList<>();
		private final Map<Integer, Integer> waveCompletionCounts = new HashMap<>();
		private final Map<String, ModifierStat> modifierStats = new LinkedHashMap<>();
		private final Map<String, Integer> rewardCounts = new HashMap<>();
		private final Map<String, ItemBundle> rewardBundles = new HashMap<>();
		private final Map<String, Integer> rewardWaves = new HashMap<>();
		private final Map<String, Integer> attemptResultCounts = new HashMap<>();
		private final Map<Integer, WaveStat> waveStats = new HashMap<>();

		public static Map<String, Integer> getBaseMaxTiers() {
			Map<String, Integer> baseMaxTiers = new HashMap<>();
			for (ColosseumModifier m : ColosseumModifier.values()) {
				String name = m.name();
				String base = getBaseModifierName(name);
				int tier;
				if (name.endsWith("_III")) tier = 3;
				else if (name.endsWith("_II")) tier = 2;
				else tier = 1;

				baseMaxTiers.put(base, Math.max(baseMaxTiers.getOrDefault(base, 0), tier));
			}
			return baseMaxTiers;
		}

		/**
		 * Return a List of all modifiers as Strings, including a 0-tier that indicates a lack thereof
		 */
		public static List<String> getAllModifierStates() {
			Map<String, Integer> maxTiers = getBaseMaxTiers();
			List<String> states = new ArrayList<>();
			for (Map.Entry<String, Integer> entry : maxTiers.entrySet()) {
				String base = entry.getKey();
				int max = entry.getValue();
				states.add(base + "_0");
				states.add(base + "_I");
				if (max >= 2) states.add(base + "_II");
				if (max >= 3) states.add(base + "_III");
			}
			states.add("DYNAMIC_DUO");
			states.add("RED_FLAG");
			states.add("QUARTET");
			states.add("TOTEMIC");
			Collections.sort(states);
			return states;
		}

		public static String normalizeState(String baseName, String actualChosen) {
			if (actualChosen == null) return baseName + "_0";
			if (actualChosen.endsWith("_III")) return baseName + "_III";
			if (actualChosen.endsWith("_II")) return baseName + "_II";
			if (actualChosen.endsWith("_I")) return baseName + "_I";
			return baseName + "_I";
		}

		public void processAttempt(ColosseumAttemptDTO attempt, Set<Integer> activeWaves, Set<String> activeResults, Set<String> excludedTags, Set<String> includedTags, Set<String> requiredStates, Set<String> excludedStates)
		{
			if (!activeResults.contains(attempt.getResult())) return;
			if (attempt.getWaves() == null || attempt.getWaves().isEmpty()) return;

			boolean attemptIncluded = false;
			Map<String, String> activeModsByBase = new HashMap<>();
			Map<String, Integer> baseMaxTiers = getBaseMaxTiers();

			for (int i = 0; i < attempt.getWaves().size(); i++)
			{
				int waveNumber = i + 1;
				ColosseumWaveDTO wave = attempt.getWaves().get(i);

				Set<String> currentWaveStates = new HashSet<>();
				for (String base : baseMaxTiers.keySet()) {
					String activeChoice = activeModsByBase.get(base);
					currentWaveStates.add(normalizeState(base, activeChoice));
				}

				boolean hasRequiredMod = true;
				if (requiredStates != null && !requiredStates.isEmpty()) {
					if (!currentWaveStates.containsAll(requiredStates)) hasRequiredMod = false;
				}
				if (hasRequiredMod && excludedStates != null && !excludedStates.isEmpty()) {
					for (String excluded : excludedStates) {
						if (currentWaveStates.contains(excluded)) {
							hasRequiredMod = false;
							break;
						}
					}
				}

				String chosen = wave.getChosenModifier();
				if (chosen != null) {
					String baseName = getBaseModifierName(chosen);
					if (baseName != null) activeModsByBase.put(baseName, chosen);
				}

				if (!hasRequiredMod) continue;
				if (!activeWaves.contains(waveNumber)) continue;

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
					if (!hasIncludedTag) continue;
				}

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

				attemptIncluded = true;
				wavesIncluded++;
				waveCompletionCounts.put(waveNumber, waveCompletionCounts.getOrDefault(waveNumber, 0) + 1);

				WaveStat wStat = waveStats.computeIfAbsent(waveNumber, k -> new WaveStat(waveNumber));
				wStat.insertWaveData(wave);

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

			if (attemptIncluded)
			{
				totalAttempts++;
				totalWavesReached += attempt.getWaves().size();
				includedAttempts.add(attempt);

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

		public static String getBaseModifierName(String modName) {
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
		HISTORY("History", 88, 176, 352),
		MODIFIERS("Modifiers", 21, 42, 84),
		REWARDS("Rewards", 21, 42, 84),
		WAVES("Waves", 104, 208, 416);

		private final String name;
		@Getter
		private final int lowScrollSpeed;
		@Getter
		private final int mediumScrollSpeed;
		@Getter
		private final int highScrollSpeed;

		TableMode(String name, int lowScrollSpeed, int mediumScrollSpeed, int highScrollSpeed)
		{
			this.name = name;
			this.lowScrollSpeed = lowScrollSpeed;
			this.mediumScrollSpeed = mediumScrollSpeed;
			this.highScrollSpeed = highScrollSpeed;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	private void updateModifierTooltip(Set<String> requiredMods, Set<String> excludedMods)
	{
		if (requiredMods.isEmpty() && excludedMods.isEmpty()) {
			requiredModifiersInput.setToolTipText(BASE_MODIFIER_TOOLTIP + "</body></html>");
			return;
		}

		StringBuilder tooltipBuilder = new StringBuilder(BASE_MODIFIER_TOOLTIP);
		tooltipBuilder.append("<br><br><hr><br><b style='color: white;'>Active Filters:</b><br>");

		List<String> reqList = new ArrayList<>(requiredMods);
		Collections.sort(reqList);
		for (String req : reqList) {
			log.debug("req: {}", req);
			String modString = addTier(req);
			String friendlyDescription = getFriendlyModifierDescription(modString, false);
			log.debug("Req: {}", friendlyDescription);
			tooltipBuilder.append("<span style='color: #a5a5a5;'>").append(friendlyDescription).append("</span><br>");
		}

		List<String> excList = new ArrayList<>(excludedMods);
		Collections.sort(excList);
		for (String exc : excList) {
			log.debug("exc: {}", exc);
			String modString = addTier(exc);
			String friendlyDescription = getFriendlyModifierDescription(modString, true);
			log.debug("Exc: {}", friendlyDescription);
			tooltipBuilder.append("<span style='color: #a5a5a5;'>").append(friendlyDescription).append("</span><br>");
		}

		tooltipBuilder.append("</body></html>");

		// Update the Swing component
		SwingUtilities.invokeLater(() -> requiredModifiersInput.setToolTipText(tooltipBuilder.toString()));
	}

	private String addTier(String modifierString)
	{
		if (modifierString.endsWith("DYNAMIC_DUO") ||
			modifierString.endsWith("RED_FLAG") ||
			modifierString.endsWith("QUARTET") ||
			modifierString.endsWith("TOTEMIC"))
			return modifierString + "_I";
		return modifierString;
	}

	/**
	 * Convert the given
	 */
	private String getFriendlyModifierDescription(String state, boolean isExcluded)
	{
		int tier = 1;
		String base = state;

		if (state.endsWith("_III")) {
			tier = 3;
			base = state.substring(0, state.length() - 4);
		} else if (state.endsWith("_II")) {
			tier = 2;
			base = state.substring(0, state.length() - 3);
		} else if (state.endsWith("_I")) {
			tier = 1;
			base = state.substring(0, state.length() - 2);
		} else if (state.endsWith("_0")) {
			tier = 0;
			base = state.substring(0, state.length() - 2);
		}

		String baseUiLabel = base;
		for (ColosseumModifier m : ColosseumModifier.values()) {
			if (m.name().equals(base) || m.name().equals(base + "_I")) {
				baseUiLabel = m.getUiLabel();
				break;
			}
		}

		if (tier == 0)
		{
			if (isExcluded) {
				return "- Any tier of " + baseUiLabel + " must be active";
			} else {
				return "- No tier of " + baseUiLabel + " is active";
			}
		}
		else
		{
			String exactUiLabel = state; // Fallback
			for (ColosseumModifier m : ColosseumModifier.values()) {
				if (m.name().equals(state) || (tier == 1 && m.name().equals(base))) {
					exactUiLabel = m.getUiLabel();
					break;
				}
			}

			if (isExcluded) {
				return "- " + exactUiLabel + " is NOT active";
			} else {
				return "- " + exactUiLabel + " is active";
			}
		}
	}

	/**
	 * Set the scroll speed of the inner scrollbar of this panel to a value that corresponds with the given speed
	 */
	public void setScrollSpeed(UIScrollSpeed scrollSpeed)
	{
		this.scrollSpeed = scrollSpeed;
		if (scrollBar == null) return;
		switch (this.scrollSpeed)
		{
			case LOW:
				scrollBar.setUnitIncrement(currentTableMode.getLowScrollSpeed());
				break;
			case MEDIUM:
				scrollBar.setUnitIncrement(currentTableMode.getMediumScrollSpeed());
				break;
			case HIGH:
				scrollBar.setUnitIncrement(currentTableMode.getHighScrollSpeed());
				break;
		}
	}
}
