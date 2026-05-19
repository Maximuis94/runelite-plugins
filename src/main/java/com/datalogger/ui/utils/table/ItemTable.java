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

import com.datalogger.ui.modes.ItemsModePanel.ItemStat;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.QuantityFormatter;

public class ItemTable
{
	public enum ItemSortType
	{
		NAME("Name"),
		QUANTITY("Qty"),
		VALUE("Value"),
		ACCOUNT("Account");

		private final String name;
		ItemSortType(String name) { this.name = name; }
		@Override public String toString() { return name; }
	}

	private static final String[] COLUMNS = {"Item Data"};

	@Getter
	private final JTable table;
	private final DefaultTableModel model;
	private final TableRowSorter<DefaultTableModel> sorter;

	private ItemSortType primarySortType = ItemSortType.VALUE;
	private boolean primaryAscending = false;

	public ItemTable(ItemManager itemManager)
	{
		this.model = new DefaultTableModel(COLUMNS, 0)
		{
			@Override public boolean isCellEditable(int row, int column) { return false; }
			@Override public Class<?> getColumnClass(int columnIndex) { return ItemStat.class; }
		};

		this.table = new JTable(model);
		this.sorter = new TableRowSorter<>(model);

		setupTableProperties(itemManager);
	}

	private void setupTableProperties(ItemManager itemManager)
	{
		table.setRowSorter(sorter);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Set a default minimum height. The dynamic resizer will stretch it if needed!
		table.setRowHeight(120);
		table.setIntercellSpacing(new Dimension(0, 0));
		table.setShowGrid(false);
		table.setBackground(ColorScheme.DARK_GRAY_COLOR);
		table.setTableHeader(null);

		table.getColumnModel().getColumn(0).setCellRenderer(new ItemRowPanelRenderer(itemManager));
	}

	public void toggleSortType(ItemSortType sortType)
	{
		if (primarySortType == sortType) {
			primaryAscending = !primaryAscending;
		} else {
			primarySortType = sortType;
			primaryAscending = (sortType == ItemSortType.NAME || sortType == ItemSortType.ACCOUNT);
		}
		applySort();
	}

	private void applySort()
	{
		Comparator<ItemStat> baseComp;

		switch (primarySortType)
		{
			case NAME:
				baseComp = Comparator.comparing(ItemStat::getName, String.CASE_INSENSITIVE_ORDER);
				break;
			case QUANTITY:
				baseComp = Comparator.comparingLong(ItemStat::getQuantity);
				break;
			case ACCOUNT:
				baseComp = Comparator.comparing(s -> String.join(", ", s.getAccounts()), String.CASE_INSENSITIVE_ORDER);
				break;
			case VALUE:
			default:
				baseComp = Comparator.comparingLong(ItemStat::getStackValue);
				break;
		}

		if (!primaryAscending) {
			baseComp = baseComp.reversed();
		}

		sorter.setComparator(0, baseComp);
		sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING)));
		sorter.sort();
	}

	public void updateData(DefaultTableModel sourceModel)
	{
		if (sourceModel == null) return;
		model.setRowCount(0);
		final int rowCount = sourceModel.getRowCount();
		if (rowCount == 0) return;

		for (int row = 0; row < rowCount; row++) {
			model.addRow(new Object[]{ sourceModel.getValueAt(row, 0) });
		}
		applySort();
	}

	private static class ItemRowPanelRenderer extends JPanel implements TableCellRenderer
	{
		private final ItemManager itemManager;

		private final JLabel iconLabel = new JLabel();
		private final JLabel nameLabel = new JLabel();
		private final JLabel detailsLabel = new JLabel();

		public ItemRowPanelRenderer(ItemManager itemManager)
		{
			this.itemManager = itemManager;

			setLayout(new BorderLayout(10, 0));
			setOpaque(true);
			setBorder(new EmptyBorder(6, 6, 6, 6));

			JPanel textContainer = new JPanel();
			textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));
			textContainer.setOpaque(false);

			nameLabel.setFont(FontManager.getRunescapeBoldFont());
			nameLabel.setForeground(Color.WHITE);

			detailsLabel.setFont(FontManager.getRunescapeSmallFont());
			detailsLabel.setForeground(Color.LIGHT_GRAY);

			textContainer.add(nameLabel);
			textContainer.add(Box.createVerticalStrut(2));
			textContainer.add(detailsLabel);

			iconLabel.setPreferredSize(new Dimension(36, 32));
			iconLabel.setHorizontalAlignment(JLabel.CENTER);

			add(iconLabel, BorderLayout.WEST);
			add(textContainer, BorderLayout.CENTER);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if (value instanceof ItemStat)
			{
				ItemStat stat = (ItemStat) value;

				nameLabel.setText(stat.getName());

				String priceStr = stat.getGePrice() > 0 ? QuantityFormatter.quantityToStackSize(stat.getGePrice()) + " gp" : "0 gp";
				String quantityStr = stat.getGePrice() > 0 ? QuantityFormatter.quantityToStackSize(stat.getQuantity()) : "0";
				String valueStr = stat.getStackValue() > 0 ? QuantityFormatter.quantityToStackSize(stat.getStackValue()) + " gp" : "0 gp";
				String accs = String.join(", ", stat.getAccounts());
				String srcs = String.join(", ", stat.getSources());

				// The strict 155px width constraint forces the HTML to word-wrap neatly!
				detailsLabel.setText("<html><p style='width: 155px; margin: 0; padding: 0;'>" +
					"Price: <span style='color: white;'>" + priceStr + "</span><br>" +
					"Quantity: <span style='color: white;'>" + quantityStr + "</span><br>" +
					"Value: <span style='color: white;'>" + valueStr + "</span><br>" +
					"Accounts:<br><span style='color: white;'>" + accs + "</span><br>" +
					"Sources:<br><span style='color: white;'>" + srcs + "</span></p></html>");

				int itemId = stat.getItemId();
				int quantity = (int) Math.min(stat.getQuantity(), Integer.MAX_VALUE);

				AsyncBufferedImage image = itemManager.getImage(itemId, quantity, true);
				if (image != null)
				{
					iconLabel.setIcon(new ImageIcon(image));
					image.onLoaded(() -> SwingUtilities.invokeLater(table::repaint));
				}
				else
				{
					iconLabel.setIcon(null);
				}
			}

			setBackground(isSelected ? ColorScheme.DARKER_GRAY_HOVER_COLOR : ColorScheme.DARK_GRAY_COLOR);

			setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARKER_GRAY_COLOR),
				new EmptyBorder(6, 6, 6, 6)
			));

			// --- DYNAMIC ROW HEIGHT CALCULATOR ---
			// Calculate the new preferred height of the panel after the text wraps
			int prefHeight = this.getPreferredSize().height;

			// If the table's row height doesn't match what the panel needs, resize it!
			if (table.getRowHeight(row) != prefHeight && prefHeight >= 90)
			{
				SwingUtilities.invokeLater(() -> {
					// Bounds check to prevent errors when users rapidly swap filters
					if (row < table.getRowCount()) {
						table.setRowHeight(row, prefHeight);
					}
				});
			}

			return this;
		}
	}
}