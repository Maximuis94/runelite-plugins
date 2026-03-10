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
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

@Slf4j
public class DataLoggerPanel extends PluginPanel {
	private final JPanel logContentDisplay = new JPanel();

	@Inject
	public DataLoggerPanel() {

		// Matches the standard RuneLite sidebar padding
		this.setBorder(new EmptyBorder(0, 0, 10, 0));
		this.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		this.getScrollPane().setBorder(new EmptyBorder(10, 10, 10, 10));
		this.getScrollPane().setBackground(ColorScheme.DARKER_GRAY_COLOR);

		// --- CENTER CONTENT SECTION ---
		logContentDisplay.setLayout(new BorderLayout());
		logContentDisplay.setBackground(ColorScheme.DARK_GRAY_COLOR);
		add(logContentDisplay, BorderLayout.CENTER);

		// --- BOTTOM BUTTONS SECTION ---
		JPanel buttonContainer = new JPanel();
		buttonContainer.setLayout(new GridLayout(3, 1, 0, 8));
		buttonContainer.setBorder(new EmptyBorder(10, 5, 0, 5));
		buttonContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		JButton openLogsBtn = createStyledButton("Colosseum log directory");
		openLogsBtn.addActionListener(e -> openDirectory(LogType.COLOSSEUM.getLogDirectory().getAbsolutePath()));

		JButton openScreenshotsBtn = createStyledButton("Screenshot directory");
		openScreenshotsBtn.addActionListener(e -> openDirectory(LogType.SCREENSHOT.getLogDirectory().getAbsolutePath()));

		JButton openDataBtn = createStyledButton("Grand exchange directory");
		openDataBtn.addActionListener(e -> openDirectory(LogType.GRAND_EXCHANGE.getLogDirectory().getAbsolutePath()));

		buttonContainer.add(openLogsBtn);
		buttonContainer.add(openScreenshotsBtn);
		buttonContainer.add(openDataBtn);

		// Add the button panel to the bottom of the PluginPanel
		add(buttonContainer, BorderLayout.SOUTH);
	}

	/**
	 * Helper to style the buttons to match the RuneLite aesthetic.
	 */
	private JButton createStyledButton(String text) {
		JButton button = new JButton(text);
		button.setFocusable(false); // Removes the annoying selection box around text when clicked
		return button;
	}

	/**
	 * Helper method to open a directory in the native OS file explorer.
	 */
	private void openDirectory(String directoryPath) {
		File dir = new File(directoryPath);

		if (!dir.exists()) {
			log.warn("Directory does not exist, attempting to create: {}", directoryPath);
			dir.mkdirs(); // Creates the directory if it doesn't exist
		}

		try {
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
				Desktop.getDesktop().open(dir);
			} else {
				log.warn("Desktop API is not supported on this platform. Cannot open file explorer.");
			}
		} catch (IOException ex) {
			log.error("Failed to open directory: {}", directoryPath, ex);
		}
	}
}