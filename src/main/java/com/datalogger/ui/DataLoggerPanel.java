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
import com.datalogger.loggers.ItemVaultLogger;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.LinkBrowser;

@Slf4j
public class DataLoggerPanel extends PluginPanel {
	private final JPanel logContentDisplay = new JPanel();
	private final ItemVaultLogger itemVaultLogger;
	private final ScheduledExecutorService executor;

	@Inject
	public DataLoggerPanel(ItemVaultLogger itemVaultLogger, ScheduledExecutorService executor) {
		this.itemVaultLogger = itemVaultLogger;
		this.executor = executor;

		// Matches the standard RuneLite sidebar padding
		this.setBorder(new EmptyBorder(0, 0, 10, 0));
		this.setBackground(ColorScheme.DARK_GRAY_COLOR);
		this.getScrollPane().setBorder(new EmptyBorder(10, 10, 10, 10));
		this.getScrollPane().setBackground(ColorScheme.DARK_GRAY_COLOR);

		// --- CENTER CONTENT SECTION ---
		logContentDisplay.setLayout(new BorderLayout());
		logContentDisplay.setBackground(ColorScheme.DARK_GRAY_COLOR);
		add(logContentDisplay, BorderLayout.CENTER);

		// --- BOTTOM BUTTONS SECTION ---
		JPanel buttonContainer = new JPanel();
		buttonContainer.setLayout(new GridLayout(5, 1, 0, 8));
		buttonContainer.setBorder(new EmptyBorder(10, 5, 0, 5));
		buttonContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JButton openLogsBtn = createStyledButton("Colosseum log directory");
		openLogsBtn.addActionListener(e -> openDirectory(LogType.COLOSSEUM.getLogDirectory().getAbsolutePath()));

		JButton openDataBtn = createStyledButton("Grand exchange directory");
		openDataBtn.addActionListener(e -> openDirectory(LogType.GRAND_EXCHANGE.getLogDirectory().getAbsolutePath()));

		JButton openItemVaultBtn = createStyledButton("Item Vault directory");
		openItemVaultBtn.addActionListener(e -> openDirectory(LogType.ITEM_VAULT.getLogDirectory().getAbsolutePath()));

		JButton exportVaultBtn = createStyledButton("Export Vault Summary");
		exportVaultBtn.addActionListener(e -> {
			itemVaultLogger.exportAggregatedData();
		});

		buttonContainer.add(exportVaultBtn);
		buttonContainer.add(openLogsBtn);
		buttonContainer.add(openItemVaultBtn);
		buttonContainer.add(openDataBtn);

		add(buttonContainer, BorderLayout.SOUTH);
	}

	/**
	 * Helper method to open a directory in the native OS file explorer safely using RuneLite's LinkBrowser.
	 */
	private void openDirectory(String directoryPath) {
		executor.submit(() -> {
			File dir = new File(directoryPath);

			if (!dir.exists()) {
				log.warn("Directory does not exist, attempting to create: {}", directoryPath);
				if (!dir.mkdirs()) {
					log.error("Failed to create directory: {}", directoryPath);
					return;
				}
			}


			LinkBrowser.open(dir.toString());
		});
	}

	private JButton createStyledButton(String text, ActionListener actionListener) {
		JButton button = new JButton(text);
		button.setFocusable(false);
		button.setPreferredSize(new Dimension(0, 30)); // Standard height for panel buttons

		// RuneLite Standard Button Colors
		button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		button.setForeground(Color.WHITE);

		// Remove the default border and add padding
		button.setBorder(new EmptyBorder(5, 5, 5, 5));

		// Hover effects to match the RuneLite UI
		button.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				button.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			}
		});

		if (actionListener != null) {
			button.addActionListener(actionListener);
		}

		return button;
	}
}