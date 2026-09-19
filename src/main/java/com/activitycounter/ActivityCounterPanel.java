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

package com.activitycounter;

import com.activitycounter.models.Category;
import com.activitycounter.models.Count;
import com.activitycounter.models.Session;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class ActivityCounterPanel extends PluginPanel {

	private final ActivityCounterPlugin plugin;
	private final JButton toggleSessionButton = new JButton();
	private final JButton deleteSessionButton = new JButton("Delete");
	private final JLabel timePassedLabel = new JLabel();
	private final JComboBox<SessionNode> sessionComboBox = new JComboBox<>();

	private final JPanel kcContainer = new JPanel();

	private volatile boolean forceNextReloadToActive = false;
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm")
		.withZone(ZoneId.systemDefault());

	private static class SessionNode {
		final Session session;
		final String label;

		SessionNode(Session session, String label) {
			this.session = session;
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	public ActivityCounterPanel(ActivityCounterPlugin plugin) {
		this.plugin = plugin;

		setLayout(new BorderLayout(0, 10));
		setBorder(new EmptyBorder(10, 10, 10, 10));

		toggleSessionButton.setFocusable(false);
		toggleSessionButton.addActionListener(e -> {
			if (plugin.getCurrentSession() != null) {
				plugin.closeSession();
			} else {
				forceNextReloadToActive = true;
				plugin.startSession();
			}
		});

		deleteSessionButton.setFocusable(false);
		deleteSessionButton.setVisible(false);
		deleteSessionButton.addActionListener(e -> {
			SessionNode selectedNode = (SessionNode) sessionComboBox.getSelectedItem();
			if (selectedNode != null && selectedNode.session != null) {
				int confirm = JOptionPane.showConfirmDialog(
					this,
					"Are you sure you want to delete this archived session?",
					"Delete Session",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE
				);

				if (confirm == JOptionPane.YES_OPTION) {
					plugin.deleteArchivedSession(selectedNode.session);
				}
			}
		});

		sessionComboBox.addActionListener(e -> refreshKcContainer());

		kcContainer.setLayout(new BoxLayout(kcContainer, BoxLayout.Y_AXIS));

		JPanel kcWrapper = new JPanel(new BorderLayout());
		kcWrapper.add(kcContainer, BorderLayout.NORTH);

		JPanel infoRow = new JPanel(new BorderLayout());
		infoRow.add(timePassedLabel, BorderLayout.WEST);
		infoRow.add(deleteSessionButton, BorderLayout.EAST);

		JPanel northWrapper = new JPanel(new BorderLayout(0, 5));
		northWrapper.add(toggleSessionButton, BorderLayout.NORTH);
		northWrapper.add(sessionComboBox, BorderLayout.CENTER);
		northWrapper.add(infoRow, BorderLayout.SOUTH);

		add(northWrapper, BorderLayout.NORTH);
		add(kcWrapper, BorderLayout.CENTER);
	}

	public void forceActiveSessionSelection() {
		forceNextReloadToActive = true;
	}

	/**
	 * Update the displayed data in the panel with the given Session data
	 */
	public void update(Session session) {
		SwingUtilities.invokeLater(() -> {
			if (session != null && session.isInProgress()) {
				toggleSessionButton.setText("Stop Session");
				toggleSessionButton.setBackground(ColorScheme.PROGRESS_ERROR_COLOR);
			} else {
				toggleSessionButton.setText("Start Session");
				toggleSessionButton.setBackground(ColorScheme.PROGRESS_COMPLETE_COLOR);
			}

			if (session != null) {
				if (session.isInProgress() && !plugin.isShowSessionDuration()) {
					timePassedLabel.setText("");
				} else {
					int totalSeconds = session.getSecondsPassed();
					int hours = totalSeconds / 3600;
					int minutes = (totalSeconds % 3600) / 60;
					int seconds = totalSeconds % 60;
					timePassedLabel.setText(String.format("Time passed: %02d:%02d:%02d", hours, minutes, seconds));
				}
			} else {
				timePassedLabel.setText("Time passed: NA");
			}

			buildKcContainer(session);
		});
	}

	/**
	 * Fill the Combobox with previously archived Sessions
	 */
	public void reloadComboBox() {
		SwingUtilities.invokeLater(() -> {
			String selectedId = null;

			if (!forceNextReloadToActive) {
				SessionNode selectedNode = (SessionNode) sessionComboBox.getSelectedItem();
				if (selectedNode != null && selectedNode.session != null) {
					selectedId = selectedNode.session.getId();
				}
			}

			forceNextReloadToActive = false;
			sessionComboBox.removeAllItems();

			SessionNode toSelect = null;
			if (plugin.getCurrentSession() != null) {
				SessionNode activeNode = new SessionNode(null, "Active session");
				sessionComboBox.addItem(activeNode);
				toSelect = activeNode;
			}

			List<Session> archived = plugin.getArchivedSessions();

			for (Session s : archived) {
				String accountPrefix = (s.getAccountName() != null && !s.getAccountName().isEmpty())
					? s.getAccountName() + " - "
					: "";

				String label = accountPrefix + formatter.format(s.getStartTime());
				SessionNode node = new SessionNode(s, label);
				sessionComboBox.addItem(node);

				if (s.getId().equals(selectedId)) {
					toSelect = node;
				}
			}

			if (toSelect != null) {
				sessionComboBox.setSelectedItem(toSelect);
			}

			refreshKcContainer();
		});
	}

	/**
	 * Update the label that displays the current time
	 */
	public void updateTime() {
		SwingUtilities.invokeLater(() -> {
			SessionNode selectedNode = (SessionNode) sessionComboBox.getSelectedItem();
			Session sessionToDisplay = null;

			if (selectedNode != null) {
				if (selectedNode.session == null) {
					sessionToDisplay = plugin.getCurrentSession();
				} else {
					sessionToDisplay = selectedNode.session;
				}
			}

			if (sessionToDisplay != null) {
				if (sessionToDisplay.isInProgress() && !plugin.isShowSessionDuration()) {
					timePassedLabel.setText("");
				} else {
					int totalSeconds = sessionToDisplay.getSecondsPassed();
					int hours = totalSeconds / 3600;
					int minutes = (totalSeconds % 3600) / 60;
					int seconds = totalSeconds % 60;

					String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
					timePassedLabel.setText("Time passed: " + formattedTime);
				}
			} else {
				timePassedLabel.setText("Time passed: NA");
			}
		});
	}

	/**
	 * Refresh values displayed in the container in the sidebar panel
	 */
	public void refreshKcContainer() {
		SwingUtilities.invokeLater(() -> {
			Session activeSession = plugin.getCurrentSession();
			if (activeSession != null && activeSession.isInProgress()) {
				toggleSessionButton.setText("Stop Session");
				toggleSessionButton.setBackground(ColorScheme.PROGRESS_ERROR_COLOR);
			} else {
				toggleSessionButton.setText("Start Session");
				toggleSessionButton.setBackground(ColorScheme.PROGRESS_COMPLETE_COLOR);
			}

			SessionNode selectedNode = (SessionNode) sessionComboBox.getSelectedItem();
			Session sessionToDisplay = null;

			if (selectedNode != null) {
				if (selectedNode.session == null) {
					sessionToDisplay = plugin.getCurrentSession();
					deleteSessionButton.setVisible(false);
				} else {
					sessionToDisplay = selectedNode.session;
					deleteSessionButton.setVisible(true);
				}
			} else {
				deleteSessionButton.setVisible(false);
			}

			updateTime();
			buildKcContainer(sessionToDisplay);
		});
	}

	/**
	 * Fill the count container with the given Session data
	 */
	private void buildKcContainer(Session sessionToDisplay) {
		kcContainer.removeAll();

		if (sessionToDisplay != null) {
			List<Count> visibleKcs = sessionToDisplay.getAllKillCounts().stream()
				.filter(kc -> kc.getSessionKc() > 0 && plugin.isKcVisible(kc.getVarPlayerId()))
				.collect(Collectors.toList());

			boolean isFirstCategory = true;

			for (Category category : Category.values()) {
				List<Count> categoryKcs = visibleKcs.stream()
					.filter(kc -> plugin.getActivityCategory(kc.getVarPlayerId()) == category)
					.sorted(Comparator.comparingInt(kc -> plugin.getActivityOrder(kc.getVarPlayerId())))
					.collect(Collectors.toList());

				if (!categoryKcs.isEmpty()) {
					if (!isFirstCategory) {
						kcContainer.add(Box.createRigidArea(new Dimension(0, 15)));
					}
					isFirstCategory = false;

					JPanel categoryHeader = new JPanel(new BorderLayout());
					categoryHeader.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
					categoryHeader.setBorder(new EmptyBorder(5, 5, 5, 5));

					String catName = category.name().substring(0, 1).toUpperCase() + category.name().substring(1).toLowerCase();
					JLabel categoryLabel = new JLabel(catName);
					categoryLabel.setForeground(Color.WHITE);
					categoryLabel.setFont(categoryLabel.getFont().deriveFont(Font.BOLD));

					categoryHeader.add(categoryLabel, BorderLayout.WEST);
					kcContainer.add(categoryHeader);

					kcContainer.add(Box.createRigidArea(new Dimension(0, 5)));

					for (int i = 0; i < categoryKcs.size(); i++) {
						Count kc = categoryKcs.get(i);
						JPanel kcPanel = new JPanel(new BorderLayout());
						kcPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
						kcPanel.setBorder(new EmptyBorder(3, 15, 3, 5));

						JLabel nameLabel = new JLabel(kc.getName());
						nameLabel.setForeground(Color.WHITE);

						JLabel countLabel = new JLabel(String.format("%,d", kc.getSessionKc()));
						countLabel.setForeground(ColorScheme.BRAND_ORANGE);

						kcPanel.add(nameLabel, BorderLayout.WEST);
						kcPanel.add(countLabel, BorderLayout.EAST);

						kcContainer.add(kcPanel);

						if (i < categoryKcs.size() - 1) {
							kcContainer.add(Box.createRigidArea(new Dimension(0, 5)));
						}
					}
				}
			}
		}

		kcContainer.revalidate();
		kcContainer.repaint();
	}
}