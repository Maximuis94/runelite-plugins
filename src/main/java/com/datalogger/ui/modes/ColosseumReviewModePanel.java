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
import static com.datalogger.constants.PluginConstants.INTERNAL_COLOSSEUM_TRIAL_HISTORY;
import com.datalogger.dto.ColosseumAttemptDTO;
import com.datalogger.models.enums.ColosseumModifier;
import static com.datalogger.ui.utils.Components.createStyledButton;
import static com.datalogger.ui.utils.Components.wrapWithRuneLiteScrollbar;
import static com.datalogger.ui.utils.Util.openDirectory;
import com.google.gson.Gson;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.QuantityFormatter;

@Slf4j
public class ColosseumReviewModePanel extends JPanel
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

	private final JPanel listContainer;
	private final PluginErrorPanel errorPanel;

	private final Map<Integer, ImageIcon> scaledIconCache = new ConcurrentHashMap<>();

	// Sorting state
	private List<ColosseumAttemptDTO> allAttempts = new ArrayList<>();
	private SortType currentSortType = SortType.DATE;
	private boolean isAscending = false;
	private final Map<SortType, JButton> sortButtons = new EnumMap<>(SortType.class);

	@Inject
	public ColosseumReviewModePanel(Gson gson, ScheduledExecutorService executor, SpriteManager spriteManager)
	{
		this.gson = gson;
		this.executor = executor;
		this.spriteManager = spriteManager;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);


		JPanel topPanel = new JPanel(new GridLayout(3, 1, 0, 2));
		topPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

		JLabel titleLabel = new JLabel("Colosseum History");
		titleLabel.setFont(FontManager.getRunescapeBoldFont());
		titleLabel.setForeground(Color.WHITE);

		JButton refreshButton = createStyledButton("Refresh", e -> loadHistory());

		JLabel instructionLabel = new JLabel("Click to open the associated directory");
		instructionLabel.setFont(FontManager.getRunescapeSmallFont());
		instructionLabel.setForeground(Color.WHITE);

		topPanel.add(titleLabel);
		topPanel.add(instructionLabel);
		topPanel.add(refreshButton);

		// --- Sorting Header ---
		JPanel sortButtonHeader = new JPanel(new GridLayout(2, 4, 4, 0));
		sortButtonHeader.setBackground(ColorScheme.DARK_GRAY_COLOR);
		sortButtonHeader.setBorder(new EmptyBorder(5, 5, 5, 5));

		for (SortType type : SortType.values())
		{
			JButton sortBtn = new JButton(type.toString());
			sortBtn.setFont(FontManager.getRunescapeSmallFont());
			sortBtn.setForeground(Color.GRAY);
			sortBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			sortBtn.setFocusable(false);
			sortBtn.setBorder(BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_HOVER_COLOR, 1));

			sortBtn.addActionListener(e -> toggleSort(type));

			sortButtons.put(type, sortBtn);
			sortButtonHeader.add(sortBtn);
		}

		JPanel northWrapper = new JPanel(new BorderLayout());
		northWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		northWrapper.add(topPanel, BorderLayout.NORTH);
		northWrapper.add(sortButtonHeader, BorderLayout.SOUTH);

		listContainer = new JPanel();
		listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
		listContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		wrapper.add(listContainer, BorderLayout.NORTH);

		JScrollPane scrollPane = wrapWithRuneLiteScrollbar(wrapper);

		errorPanel = new PluginErrorPanel();
		errorPanel.setContent("No History Found", "Complete a Colosseum attempt to see it here.");

		add(northWrapper, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);

		loadHistory();
	}

	private void toggleSort(SortType type)
	{
		if (currentSortType == type) {
			isAscending = !isAscending; // Flip direction if clicked twice
		} else {
			currentSortType = type;
			isAscending = false; // Default to highest/newest first on new metric
		}
		applySort();
	}

	private void applySort()
	{
		if (allAttempts == null || allAttempts.isEmpty()) return;

		// Update button visuals
		for (Map.Entry<SortType, JButton> entry : sortButtons.entrySet()) {
			SortType type = entry.getKey();
			JButton btn = entry.getValue();
			if (type == currentSortType) {
				btn.setText(type.toString() + (isAscending ? " ▲" : " ▼"));
				btn.setForeground(Color.WHITE);
			} else {
				btn.setText(type.toString());
				btn.setForeground(Color.GRAY);
			}
		}

		// Determine sorting criteria
		Comparator<ColosseumAttemptDTO> comp;
		switch (currentSortType) {
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

		if (!isAscending) {
			comp = comp.reversed();
		}

		// Sort the cached list and redraw the UI
		allAttempts.sort(comp);
		SwingUtilities.invokeLater(() -> rebuildListUi(allAttempts));
	}

	/**
	 * Reads the .jsonl file asynchronously and updates the UI
	 */
	private void loadHistory()
	{
		// Show loading state (must be on EDT)
		SwingUtilities.invokeLater(() -> {
			listContainer.removeAll();
			listContainer.add(errorPanel);
			errorPanel.setContent("Loading...", "Fetching attempt history.");
			revalidate();
			repaint();
		});

		executor.submit(() -> {
			List<ColosseumAttemptDTO> attempts = new ArrayList<>();

			if (INTERNAL_COLOSSEUM_TRIAL_HISTORY.exists())
			{
				try (BufferedReader reader = new BufferedReader(new FileReader(INTERNAL_COLOSSEUM_TRIAL_HISTORY)))
				{
					String line;
					while ((line = reader.readLine()) != null)
					{
						if (line.trim().isEmpty()) continue;

						ColosseumAttemptDTO dto = gson.fromJson(line, ColosseumAttemptDTO.class);
						if (dto != null)
						{
							attempts.add(dto);
						}
					}
				}
				catch (Exception e)
				{
					log.error("Failed to read Colosseum history file", e);
				}
			}

			// Save to class state and apply the default sort, which triggers the UI rebuild
			this.allAttempts = attempts;
			SwingUtilities.invokeLater(this::applySort);
		});
	}

	/**
	 * Reconstructs the stack of cards. Must be called on the Swing EDT.
	 */
	private void rebuildListUi(List<ColosseumAttemptDTO> attempts)
	{
		listContainer.removeAll();

		if (attempts.isEmpty())
		{
			errorPanel.setContent("No History Found", "Complete a Colosseum attempt to see it here.");
			listContainer.add(errorPanel);
		}
		else
		{
			for (ColosseumAttemptDTO attempt : attempts)
			{
				JPanel card = buildAttemptCard(attempt);

				if (card == null) continue;

				listContainer.add(card);
			}
		}

		listContainer.revalidate();
		listContainer.repaint();
	}

	/**
	 * Creates a styled card representation of a single Colosseum attempt.
	 */
	private JPanel buildAttemptCard(ColosseumAttemptDTO attempt)
	{
		int waves = attempt.getWaves() != null ? attempt.getWaves().size() : 0;

		if (waves == 0)
			return null;

		JPanel card = new JPanel(new BorderLayout(5, 5));
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(new EmptyBorder(8, 8, 8, 8));

		// Margin between cards
		JPanel marginWrapper = new JPanel(new BorderLayout());
		marginWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		marginWrapper.setBorder(new EmptyBorder(0, 0, 8, 0));
		marginWrapper.add(card, BorderLayout.CENTER);

		long timestamp = attempt.getTimestamp();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
			.withZone(ZoneId.systemDefault());
		String formattedDate = formatter.format(Instant.ofEpochMilli(timestamp));
		JLabel dateLabel = new JLabel(String.format("%s | %s", attempt.getAccountName(), formattedDate));
		dateLabel.setForeground(Color.WHITE);
		String status = attempt.getResult();
		JLabel wavesLabel = new JLabel(String.format("Waves: %s | Status: %s", waves, status));
		wavesLabel.setForeground(Color.WHITE);


		int rewardValue = attempt.getRewardsValue();
		JLabel rewardsLabel = new JLabel(String.format("Reward value: %s", QuantityFormatter.quantityToStackSize(rewardValue)));
		rewardsLabel.setForeground(Color.WHITE);

		double timeTaken = attempt.getTotalTime(); // e.g., 74.32

		int m = (int) (timeTaken / 60);
		int s = (int) (timeTaken % 60);
		int ms = (int) Math.round((timeTaken % 1) * 10);

		if (ms == 10) {
			ms = 0;
			s++;
			if (s == 60) {
				s = 0;
				m++;
			}
		}
		String formattedTime = String.format("%02d:%02d.%d", m, s, ms);

		JLabel timeLabel = new JLabel(String.format("Time taken: %s", formattedTime));
		timeLabel.setForeground(Color.WHITE);

		JLabel gloryLabel = new JLabel(String.format("Total glory: %d", attempt.getTotalGlory()));
		gloryLabel.setForeground(Color.WHITE);

		int supplyValue = attempt.getConsumedSupplyValue();
		JLabel supplyValueLabel = new JLabel(String.format("Supply value: %s", QuantityFormatter.quantityToStackSize(supplyValue)));
		supplyValueLabel.setForeground(Color.WHITE);

		// 1. Generate your new panel
		JPanel modifierPanel = generateModifierPanel(attempt);

		// 2. Force EVERY component to align to the left (0.0f)
		dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		wavesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		rewardsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		supplyValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
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
			JLabel tagLabel = new JLabel(String.format("Tag: %s", attempt.getTag()));
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

		// Unified MouseListener for hovers and clicking
		card.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				card.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
				centerPanel.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent evt) {
				card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
				centerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			}

			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				if (SwingUtilities.isLeftMouseButton(evt)) {
					if (attemptDir.exists() && attemptDir.isDirectory()) {
						openDirectory(attemptDir, executor);
					}
				}
			}
		});

		return marginWrapper;
	}

	private JPanel generateModifierPanel(ColosseumAttemptDTO attempt)
	{
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setOpaque(false);

		List<String> modifiers = attempt.getActiveModifiers();
		if (modifiers == null || modifiers.isEmpty())
		{
			return mainPanel;
		}

		JPanel currentRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
		currentRow.setOpaque(false);
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

			if (iconCount > 0 && iconCount % 5 == 0)
			{
				mainPanel.add(currentRow);
				currentRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
				currentRow.setOpaque(false);
			}

			JLabel iconLabel = new JLabel();
			iconLabel.setToolTipText(mod.getUiLabel());
			currentRow.add(iconLabel);
			iconCount++;

			final int spriteId = mod.getSpriteId();

			if (scaledIconCache.containsKey(spriteId))
			{
				iconLabel.setIcon(scaledIconCache.get(spriteId));
			}
			else
			{
				spriteManager.getSpriteAsync(spriteId, 0, img -> {
					if (img != null)
					{
						Image scaledImg = img.getScaledInstance(img.getWidth(), img.getHeight(), Image.SCALE_SMOOTH);
						ImageIcon scaledIcon = new ImageIcon(scaledImg);

						scaledIconCache.put(spriteId, scaledIcon);

						SwingUtilities.invokeLater(() -> {
							iconLabel.setIcon(scaledIcon);
							iconLabel.revalidate();
							iconLabel.repaint();
						});
					}
				});
			}
		}

		if (currentRow.getComponentCount() > 0)
		{
			mainPanel.add(currentRow);
		}

		return mainPanel;
	}
}