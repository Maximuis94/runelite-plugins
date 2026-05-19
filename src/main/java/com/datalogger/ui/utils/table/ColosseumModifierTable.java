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

package com.datalogger.ui.utils.table;

import com.datalogger.ui.modes.ColosseumStatisticsModePanel.ModifierStat;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import lombok.Getter;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

public class ColosseumModifierTable
{
	public enum ModifierSortType
	{
		MODIFIER("Mod"),
		WAVE("Wave"),
		PICK_RATE("Pick"),
		APPEAR_RATE("Seen");

		private final String name;
		ModifierSortType(String name) { this.name = name; }
		@Override public String toString() { return name; }
	}

	private static final String[] COLUMNS = {"Modifier Data"};

	@Getter
	private final JTable table;
	private final DefaultTableModel model;
	private final TableRowSorter<DefaultTableModel> sorter;

	private ModifierSortType primarySortType = ModifierSortType.WAVE;
	private boolean primaryAscending = true;

	private ModifierSortType secondarySortType = null;
	private boolean secondaryAscending = true;

	public ColosseumModifierTable(SpriteManager spriteManager)
	{
		this.model = new DefaultTableModel(COLUMNS, 0)
		{
			@Override
			public boolean isCellEditable(int row, int column) { return false; }

			@Override
			public Class<?> getColumnClass(int columnIndex) { return ModifierStat.class; }
		};

		this.table = new JTable(model);
		this.sorter = new TableRowSorter<>(model);

		setupTableProperties(spriteManager);
		applySort();
	}

	private void setupTableProperties(SpriteManager spriteManager)
	{
		table.setRowSorter(sorter);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(42);
		table.setIntercellSpacing(new Dimension(0, 0));
		table.setShowGrid(false);
		table.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		table.setTableHeader(null);

		table.getColumnModel().getColumn(0).setCellRenderer(new ColosseumRowPanelRenderer(spriteManager));
	}

	/**
	 * Toggles sorting direction if clicked consecutively, or demotes the current
	 * primary criteria to a secondary fallback when a new header button is pressed.
	 */
	public void toggleSortType(ModifierSortType sortType)
	{
		if (primarySortType == sortType)
		{
			primaryAscending = !primaryAscending;
		}
		else
		{
			secondarySortType = primarySortType;
			secondaryAscending = primaryAscending;

			primarySortType = sortType;
			primaryAscending = (sortType == ModifierSortType.WAVE || sortType == ModifierSortType.MODIFIER);
		}
		applySort();
	}

	/**
	 * Combines the primary and secondary comparators to execute a multi-tier sort pass.
	 */
	private void applySort()
	{
		Comparator<ModifierStat> finalComparator = createComparator(primarySortType, primaryAscending);

		if (secondarySortType != null && secondarySortType != primarySortType)
		{
			finalComparator = finalComparator.thenComparing(createComparator(secondarySortType, secondaryAscending));
		}

		sorter.setComparator(0, finalComparator);
		sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)));
		sorter.sort();
	}

	/**
	 * Helper factory that constructs independent standalone comparators based on type state.
	 */
	private Comparator<ModifierStat> createComparator(ModifierSortType type, boolean ascending)
	{
		Comparator<ModifierStat> baseComp;

		switch (type)
		{
			case MODIFIER:
				baseComp = Comparator.comparing(
					(ModifierStat s) -> s.getModifier().getUiLabel(),
					String.CASE_INSENSITIVE_ORDER
				);
				break;
			case PICK_RATE:
				baseComp = Comparator.comparingDouble(ModifierStat::getPickRate);
				break;
			case APPEAR_RATE:
				baseComp = (s1, s2) -> {
					Object countsMapAttr = table.getClientProperty("waveCompletionCounts");
					if (countsMapAttr instanceof Map)
					{
						@SuppressWarnings("unchecked")
						Map<Integer, Integer> waveCompletionCounts = (Map<Integer, Integer>) countsMapAttr;
						int totalWaves1 = waveCompletionCounts.getOrDefault(s1.getWave(), 0);
						int totalWaves2 = waveCompletionCounts.getOrDefault(s2.getWave(), 0);
						double rate1 = totalWaves1 > 0 ? ((double) s1.getAppearedCount() / totalWaves1) : 0.0;
						double rate2 = totalWaves2 > 0 ? ((double) s2.getAppearedCount() / totalWaves2) : 0.0;
						return Double.compare(rate1, rate2);
					}
					return Integer.compare(s1.getAppearedCount(), s2.getAppearedCount());
				};
				break;
			case WAVE:
			default:
				baseComp = Comparator.comparingInt(ModifierStat::getWave);
		}

		return ascending ? baseComp : baseComp.reversed();
	}

	public void updateData(DefaultTableModel sourceModel)
	{
		if (sourceModel == null) return;

		model.setRowCount(0);
		final int rowCount = sourceModel.getRowCount();
		if (rowCount == 0) return;

		for (int row = 0; row < rowCount; row++)
		{
			Object modifierStat = sourceModel.getValueAt(row, 0);
			model.addRow(new Object[]{ modifierStat });
		}

		applySort();
	}

	private static class ColosseumRowPanelRenderer extends JPanel implements TableCellRenderer
	{
		private final SpriteManager spriteManager;
		private final Map<Integer, ImageIcon> iconCache = new HashMap<>();
		private final Set<Integer> pendingRequests = new HashSet<>();

		private final JLabel iconLabel = new JLabel();
		private final JLabel descriptionLabel = new JLabel();
		private final JLabel metricsLabel = new JLabel();

		public ColosseumRowPanelRenderer(SpriteManager spriteManager)
		{
			this.spriteManager = spriteManager;

			setLayout(new BorderLayout(10, 0));
			setOpaque(true);
			setBorder(new EmptyBorder(4, 6, 4, 6));

			JPanel textContainer = new JPanel(new GridLayout(2, 1, 0, 2));

			textContainer.setOpaque(false);

			descriptionLabel.setFont(FontManager.getRunescapeSmallFont());
			descriptionLabel.setForeground(Color.WHITE);

			metricsLabel.setFont(FontManager.getRunescapeSmallFont());
			metricsLabel.setForeground(Color.WHITE);

			textContainer.add(descriptionLabel);
			textContainer.add(metricsLabel);

			add(iconLabel, BorderLayout.WEST);
			add(textContainer, BorderLayout.CENTER);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
													   boolean isSelected, boolean hasFocus,
													   int row, int column)
		{
			if (value instanceof ModifierStat)
			{
				ModifierStat stat = (ModifierStat) value;

				Object countsMapAttr = table.getClientProperty("waveCompletionCounts");
				@SuppressWarnings("unchecked")
				java.util.Map<Integer, Integer> waveCompletionCounts =
					countsMapAttr instanceof java.util.Map ? (java.util.Map<Integer, Integer>) countsMapAttr : null;

				int totalSpecificWaves = 0;
				if (waveCompletionCounts != null)
				{
					totalSpecificWaves = waveCompletionCounts.getOrDefault(stat.getWave(), 0);
				}

				int appearCount = stat.getAppearedCount();
				double appearRate = totalSpecificWaves > 0 ? ((double) appearCount / totalSpecificWaves) * 100.0 : 0.0;

				descriptionLabel.setText("W" + stat.getWave() + " " + String.format("Seen: %d/%d (%.1f%%)",
					appearCount,
					totalSpecificWaves,
					appearRate
				));

				metricsLabel.setText(String.format("Picked: %d/%d (%.1f%%)",
					stat.getChosenCount(),
					appearCount,
					stat.getPickRate()
				));

				int spriteId = stat.getModifier().getSpriteId();
				ImageIcon icon = iconCache.get(spriteId);

				if (icon != null)
				{
					iconLabel.setIcon(icon);
				}
				else
				{
					iconLabel.setIcon(null);

					if (!pendingRequests.contains(spriteId))
					{
						pendingRequests.add(spriteId);

						spriteManager.getSpriteAsync(spriteId, 0, img -> {
							if (img != null)
							{
								SwingUtilities.invokeLater(() -> {
									iconCache.put(spriteId, new ImageIcon(img));
									pendingRequests.remove(spriteId);
									table.repaint();
								});
							}
							else
							{
								SwingUtilities.invokeLater(() -> pendingRequests.remove(spriteId));
							}
						});
					}
				}
			}
			setBackground(ColorScheme.DARK_GRAY_COLOR);
			metricsLabel.setForeground(Color.WHITE); // Make subtext fully white on hover/selection
			descriptionLabel.setForeground(Color.WHITE);

			return this;
		}
	}
}