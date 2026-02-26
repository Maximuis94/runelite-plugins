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
package com.datalogger.ui;

import com.datalogger.framework.LogType;
import com.datalogger.models.GrandExchangeOfferData;
import com.datalogger.services.FileIOService;
import java.awt.BorderLayout;
import java.util.List;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.PluginErrorPanel;

public class GrandExchangeOfferPanel extends LogTypePanel {
	private final FileIOService fileService;
	private final JPanel listContainer = new JPanel();
	private final JScrollPane scrollPane;

	@Inject
	public GrandExchangeOfferPanel(FileIOService fileService) {
		super(LogType.GRAND_EXCHANGE);
		this.fileService = fileService;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// List container setup
		listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
		listContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// ScrollPane with the "Flat" look you requested
		scrollPane = new JScrollPane(listContainer);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setPreferredSize(new java.awt.Dimension(6, 0));

		add(scrollPane, BorderLayout.CENTER);
	}

	@Override
	public void refresh(String accountName) {
		listContainer.removeAll();

		// Use the generic loadLogs from our service
		List<GrandExchangeOfferData> logs = fileService.loadLogs(type, accountName);

		if (logs.isEmpty()) {
			renderEmptyState();
		} else {
			for (GrandExchangeOfferData data : logs) {
				listContainer.add(createLogEntry(data));
			}
		}

		listContainer.revalidate();
		listContainer.repaint();
	}

	private void renderEmptyState() {
		PluginErrorPanel errorPanel = new PluginErrorPanel();
		errorPanel.setContent("No GE Logs", "Trade activity for this account will appear here.");
		listContainer.add(errorPanel);
	}

	private JPanel createLogEntry(GrandExchangeOfferData data) {
		JPanel entry = new JPanel(new BorderLayout());
		entry.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		entry.setBorder(new EmptyBorder(5, 5, 5, 5));
		entry.setPreferredSize(new java.awt.Dimension(0, 40));

		// Placeholder for a more complex row (Item ID + Quantity)
		JLabel text = new JLabel(String.format("Item %d x %d", data.getItemId(), data.getQuantity()));
		text.setForeground(data.getQuantity() > 0 ? ColorScheme.PROGRESS_COMPLETE_COLOR : ColorScheme.PROGRESS_ERROR_COLOR);

		entry.add(text, BorderLayout.CENTER);

		// Add a small spacer between rows
		JPanel marginWrapper = new JPanel(new BorderLayout());
		marginWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		marginWrapper.setBorder(new EmptyBorder(2, 0, 2, 0));
		marginWrapper.add(entry, BorderLayout.CENTER);

		return marginWrapper;
	}
}