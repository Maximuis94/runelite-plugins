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

import com.datalogger.framework.LogType;
import com.datalogger.services.FileIOService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.inject.Inject;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;

public class DataLoggerPanel extends PluginPanel {
	private final FileIOService fileService;
	private final JComboBox<LogType> logTypeSelector = new JComboBox<>(LogType.values());
	private final JPanel logContentDisplay = new JPanel();

	@Inject
	public DataLoggerPanel(FileIOService fileService) {
		this.fileService = fileService;

		// Matches the standard RuneLite sidebar padding
		this.setBorder(new EmptyBorder(0, 0, 10, 0));
		this.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		this.getScrollPane().setBorder(new EmptyBorder(10, 10, 10, 10));
		this.getScrollPane().setBackground(ColorScheme.DARKER_GRAY_COLOR);

		// --- TOP NAVIGATION SECTION ---
		JPanel navContainer = new JPanel();
		navContainer.setLayout(new GridLayout(2, 1, 2, 7));
		// Subtle bottom border to separate the selector from content
		navContainer.setBorder(new CompoundBorder(
			new MatteBorder(0, 0, 1, 0, ColorScheme.DARKER_GRAY_COLOR),
			new EmptyBorder(0, 4, 1, 4)
		));
		navContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);



		JLabel label = new JLabel("Select Log Type");
		label.setForeground(ColorScheme.BRAND_ORANGE); // Branding orange from your image
		label.setFont(net.runelite.client.ui.FontManager.getRunescapeBoldFont());
		navContainer.add(label, BorderLayout.WEST);

		JPanel comboWrapper = new JPanel(new BorderLayout());
		comboWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		comboWrapper.setBorder(new EmptyBorder(0, 4, 0, 4));
		logTypeSelector.addActionListener(e -> updateView());
		logTypeSelector.setForeground(ColorScheme.TEXT_COLOR);
		logTypeSelector.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		logTypeSelector.setFocusable(false);

		comboWrapper.add(logTypeSelector, BorderLayout.CENTER);

		navContainer.add(comboWrapper);
		add(navContainer, BorderLayout.NORTH);

		// --- CENTER CONTENT SECTION ---
		logContentDisplay.setLayout(new BorderLayout());
		logContentDisplay.setBackground(ColorScheme.DARK_GRAY_COLOR);
		add(logContentDisplay, BorderLayout.CENTER);

		updateView();
	}

	private void updateView() {
		logContentDisplay.removeAll();

		LogType selected = (LogType) logTypeSelector.getSelectedItem();
		if (selected == null) return;

		// The ErrorPanel is a standard way to show placeholders in RuneLite
		PluginErrorPanel errorPanel = new PluginErrorPanel();
		errorPanel.setBorder(new EmptyBorder(50, 20, 20, 20));
		errorPanel.setContent("Viewing " + selected.getDirectoryName(),
			"Data for this category will appear here.");

		logContentDisplay.add(errorPanel, BorderLayout.NORTH);

		logContentDisplay.revalidate();
		logContentDisplay.repaint();
	}
}