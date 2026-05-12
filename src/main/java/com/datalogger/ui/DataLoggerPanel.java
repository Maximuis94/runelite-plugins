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
package com.datalogger.ui;

import com.datalogger.models.enums.PanelViewMode;
import com.datalogger.ui.modes.ColosseumReviewModePanel;
import com.datalogger.ui.modes.ColosseumStatisticsModePanel;
import com.datalogger.ui.modes.DebugModePanel;
import com.datalogger.ui.modes.DirectoriesModePanel;
// import com.datalogger.ui.modes.ItemsModePanel;
// import com.datalogger.ui.modes.DebugModePanel;

import com.datalogger.ui.modes.ItemsModePanel;
import com.datalogger.ui.modes.ManualExportModePanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import javax.inject.Inject;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

@Slf4j
public class DataLoggerPanel extends PluginPanel {

	private final CardLayout cardLayout;
	private final JPanel cardContainer;

	@Inject
	public DataLoggerPanel(
		DirectoriesModePanel directoriesPanel,
		ManualExportModePanel exportModePanel,
		ColosseumStatisticsModePanel colosseumPanel,
		ColosseumReviewModePanel colosseumReviewPanel,
		ItemsModePanel itemsPanel,
		DebugModePanel debugPanel
	)
	{
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.setBackground(ColorScheme.DARK_GRAY_COLOR);
		this.setLayout(new BorderLayout(0, 10)); // 10px vertical gap

		JComboBox<PanelViewMode> viewSelector = new JComboBox<>(PanelViewMode.values());
		viewSelector.setFocusable(false);
		viewSelector.setForeground(Color.WHITE);
		viewSelector.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		viewSelector.setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				label.setForeground(Color.WHITE);
				label.setBorder(new EmptyBorder(5, 5, 5, 0)); // Add a little text padding

				if (isSelected)
				{
					label.setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
				}
				else
				{
					label.setBackground(ColorScheme.DARKER_GRAY_COLOR);
				}

				return label;
			}
		});

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		topPanel.add(viewSelector, BorderLayout.CENTER);

		// 2. Setup Card Container
		cardLayout = new CardLayout();
		cardContainer = new JPanel(cardLayout);
		cardContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// 3. Register Panels to CardLayout using Enum names as identifiers
		cardContainer.add(directoriesPanel, PanelViewMode.DIRECTORIES.name());
		cardContainer.add(exportModePanel, PanelViewMode.EXPORTS.name());
		cardContainer.add(colosseumPanel, PanelViewMode.COLOSSEUM_STATISTICS.name());
		cardContainer.add(colosseumReviewPanel, PanelViewMode.COLOSSEUM_REVIEW.name());
		cardContainer.add(itemsPanel, PanelViewMode.ITEMS.name());
		cardContainer.add(debugPanel, PanelViewMode.DEBUG.name());

		// 4. Handle State Switching
		viewSelector.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				PanelViewMode selectedMode = (PanelViewMode) e.getItem();
				cardLayout.show(cardContainer, selectedMode.name());
			}
		});

		// 5. Add to Main Panel
		add(topPanel, BorderLayout.NORTH);
		add(cardContainer, BorderLayout.CENTER);
	}
}