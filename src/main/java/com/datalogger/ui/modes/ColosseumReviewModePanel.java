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

package com.datalogger.ui.modes;

import com.datalogger.constants.PluginConstants;
import static com.datalogger.constants.PluginConstants.INTERNAL_COLOSSEUM_ATTEMPT_HISTORY;
import com.datalogger.dto.ColosseumAttemptDTO;
import com.google.gson.Gson;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.PluginErrorPanel;

@Slf4j
public class ColosseumReviewModePanel extends JPanel
{
	private final Gson gson;
	private final ScheduledExecutorService executor;

	private final JPanel listContainer;
	private final PluginErrorPanel errorPanel;

	@Inject
	public ColosseumReviewModePanel(Gson gson, ScheduledExecutorService executor)
	{
		this.gson = gson;
		this.executor = executor;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Top panel for title and refresh button
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

		JLabel titleLabel = new JLabel("Attempt History");
		titleLabel.setForeground(Color.WHITE);

		JButton refreshButton = new JButton("Refresh");
		refreshButton.setFocusable(false);
		refreshButton.addActionListener(e -> loadHistory());

		topPanel.add(titleLabel, BorderLayout.WEST);
		topPanel.add(refreshButton, BorderLayout.EAST);

		// Container that will hold the stacked attempt cards
		listContainer = new JPanel();
		listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
		listContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Scroll pane wraps the list container
		JScrollPane scrollPane = new JScrollPane(listContainer);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
		scrollPane.setBorder(BorderFactory.createEmptyBorder());

		// Fallback error/empty state panel
		errorPanel = new PluginErrorPanel();
		errorPanel.setContent("No History Found", "Complete a Colosseum attempt to see it here.");

		add(topPanel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);

		// Initial load
		loadHistory();
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

			if (INTERNAL_COLOSSEUM_ATTEMPT_HISTORY.exists())
			{
				try (BufferedReader reader = new BufferedReader(new FileReader(INTERNAL_COLOSSEUM_ATTEMPT_HISTORY)))
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

			// Reverse so newest attempts are at the top
			Collections.reverse(attempts);

			// Update the UI with the loaded data
			SwingUtilities.invokeLater(() -> rebuildListUi(attempts));
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
				listContainer.add(buildAttemptCard(attempt));
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
		JPanel card = new JPanel(new BorderLayout(5, 5));
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(new EmptyBorder(8, 8, 8, 8));

		// Margin between cards
		JPanel marginWrapper = new JPanel(new BorderLayout());
		marginWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		marginWrapper.setBorder(new EmptyBorder(0, 0, 8, 0));
		marginWrapper.add(card, BorderLayout.CENTER);

		// Example details - adjust these getters based on what's actually in your DTO!
		String attemptId = attempt.getAttemptId() != null ? attempt.getAttemptId() : "Unknown";
		int waves = attempt.getWaves() != null ? attempt.getWaves().size() : 0;

		JLabel idLabel = new JLabel("Attempt: " + attemptId);
		idLabel.setFont(FontManager.getRunescapeSmallFont());
		idLabel.setForeground(Color.GRAY);

		JLabel wavesLabel = new JLabel("Waves Completed: " + waves);
		wavesLabel.setForeground(Color.WHITE);

		// Center content
		JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
		centerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		centerPanel.add(idLabel);
		centerPanel.add(wavesLabel);

		// Add components to card
		card.add(centerPanel, BorderLayout.CENTER);

		// Optional: Add a hover effect to the card if you want them to be clickable later
		card.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				card.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
				centerPanel.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
			}
			public void mouseExited(java.awt.event.MouseEvent evt) {
				card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
				centerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			}
		});

		return marginWrapper;
	}
}