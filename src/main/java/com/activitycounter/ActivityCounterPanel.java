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

import com.activitycounter.listeners.SoundEffectListener;
import com.activitycounter.models.Category;
import com.activitycounter.models.Count;
import com.activitycounter.models.Session;
import com.activitycounter.models.TrackedActivity;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class ActivityCounterPanel extends PluginPanel {

	private final ActivityCounterPlugin plugin;
	private final JButton toggleSessionButton = new JButton();
	private final JButton renameSessionButton = new JButton("Rename");
	private final JButton deleteSessionButton = new JButton("Delete");
	private final JComboBox<SessionNode> sessionComboBox = new JComboBox<>();

	// Session Info Components
	private final JLabel timePassedLabel = new JLabel();
	private final JLabel startTimeLabel = new JLabel();
	private final JLabel endTimeLabel = new JLabel();
	private final JLabel warningLabel = new JLabel();
	private final JTextArea notesArea = new JTextArea();
	private final JScrollPane notesScrollPane = new JScrollPane(notesArea);

	private final JPanel kcContainer = new JPanel();
	private final SoundEffectListener soundEffectListener;

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

	public ActivityCounterPanel(ActivityCounterPlugin plugin, SoundEffectListener soundEffectListener) {
		this.plugin = plugin;
		this.soundEffectListener = soundEffectListener;

		setLayout(new BorderLayout(0, 10));
		setBorder(new EmptyBorder(10, 10, 10, 10));

		toggleSessionButton.setFocusable(false);
		toggleSessionButton.addActionListener(e -> {
			if (plugin.getCurrentSession() != null) {
				if (plugin.isConfirmSessionTermination()) {
					int confirm = JOptionPane.showConfirmDialog(
						this,
						"Are you sure you want to terminate the active session?",
						"Terminate Session",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE
					);

					if (confirm == JOptionPane.YES_OPTION) {
						plugin.closeSession();
					}
				} else plugin.closeSession();
			} else {
				forceNextReloadToActive = true;
				plugin.startSession();
			}
		});

		renameSessionButton.setFocusable(false);
		renameSessionButton.setVisible(false);
		renameSessionButton.addActionListener(e -> {
			SessionNode selectedNode = (SessionNode) sessionComboBox.getSelectedItem();
			if (selectedNode != null && selectedNode.session != null) {
				String currentName = selectedNode.label;
				JTextField nameInput = new JTextField(currentName);
				int result = JOptionPane.showConfirmDialog(
					this,
					nameInput,
					"Rename Session",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE
				);

				if (result == JOptionPane.OK_OPTION) {
					String newName = nameInput.getText();
					if (newName != null && !newName.trim().isEmpty()) {
						plugin.renameSession(selectedNode.session, newName);
					}
				}
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

		// Action Buttons Panel
		JPanel actionButtonsPanel = new JPanel();
		actionButtonsPanel.setLayout(new BoxLayout(actionButtonsPanel, BoxLayout.X_AXIS));
		actionButtonsPanel.add(renameSessionButton);
		actionButtonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		actionButtonsPanel.add(deleteSessionButton);

		// Setup Warning Label
		warningLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		warningLabel.setVisible(false);

		// Setup Notes Area
		notesArea.setLineWrap(true);
		notesArea.setWrapStyleWord(true);
		notesArea.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		notesArea.setForeground(Color.WHITE);
		notesArea.setToolTipText("Session notes (auto-saves when you click away)");
		notesScrollPane.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 85));
		notesScrollPane.setVisible(false);

		notesArea.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				SessionNode selectedNode = (SessionNode) sessionComboBox.getSelectedItem();
				Session active = (selectedNode != null && selectedNode.session != null)
					? selectedNode.session
					: plugin.getCurrentSession();

				if (active != null) {
					active.setNotes(notesArea.getText());
					plugin.saveSession(active);
				}
			}
		});

		actionButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		timePassedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		startTimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		endTimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		warningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		notesScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel infoRow = new JPanel();
		infoRow.setLayout(new BoxLayout(infoRow, BoxLayout.Y_AXIS));
		infoRow.add(actionButtonsPanel);
		infoRow.add(Box.createRigidArea(new Dimension(0, 5)));
		infoRow.add(timePassedLabel);
		infoRow.add(startTimeLabel);
		infoRow.add(endTimeLabel);
		infoRow.add(Box.createRigidArea(new Dimension(0, 5)));
		infoRow.add(warningLabel);
		infoRow.add(Box.createRigidArea(new Dimension(0, 5)));
		infoRow.add(notesScrollPane);

		JPanel northWrapper = new JPanel(new BorderLayout(0, 5));
		northWrapper.add(toggleSessionButton, BorderLayout.NORTH);
		northWrapper.add(sessionComboBox, BorderLayout.CENTER);
		northWrapper.add(infoRow, BorderLayout.SOUTH);

		add(northWrapper, BorderLayout.NORTH);
		add(kcWrapper, BorderLayout.CENTER);

		// Start a background timer to naturally update the duration and hide the warning label
		Timer updateTimer = new Timer(1000, e -> updateTime());
		updateTimer.start();
	}

	public void forceActiveSessionSelection() {
		forceNextReloadToActive = true;
	}

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
				String label;

				if (s.getSessionName() != null && !s.getSessionName().trim().isEmpty()) {
					label = s.getSessionName();
				} else {
					String accountPrefix = (s.getAccountName() != null && !s.getAccountName().isEmpty())
						? s.getAccountName() + " - "
						: "";
					label = accountPrefix + formatter.format(s.getStartTime());
				}

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
				ActivityCounterConfig config = plugin.getConfig();

				// Duration
				if (sessionToDisplay.isInProgress() && !config.showSessionDuration()) {
					timePassedLabel.setText("");
				} else {
					int totalSeconds = sessionToDisplay.getSecondsPassed();
					int hours = totalSeconds / 3600;
					int minutes = (totalSeconds % 3600) / 60;
					int seconds = totalSeconds % 60;
					timePassedLabel.setText(String.format("Time passed: %02d:%02d:%02d", hours, minutes, seconds));
				}

				// Start Time
				if (config.showStartTime() && sessionToDisplay.getStartTime() != null) {
					startTimeLabel.setText("Started: " + formatter.format(sessionToDisplay.getStartTime()));
					startTimeLabel.setVisible(true);
				} else {
					startTimeLabel.setVisible(false);
				}

				// End Time
				if (config.showEndTime() && sessionToDisplay.getEndTime() != null && !sessionToDisplay.isInProgress()) {
					endTimeLabel.setText("Ended: " + formatter.format(sessionToDisplay.getEndTime()));
					endTimeLabel.setVisible(true);
				} else {
					endTimeLabel.setVisible(false);
				}

				// Notes
				if (config.showNotes()) {
					if (!notesArea.hasFocus()) {
						notesArea.setText(sessionToDisplay.getNotes() != null ? sessionToDisplay.getNotes() : "");
					}
					notesScrollPane.setVisible(true);
				} else {
					notesScrollPane.setVisible(false);
				}

				// Warning / Status Label
				if (sessionToDisplay.isInProgress()) {
					long secondsSinceStart = sessionToDisplay.getStartTime() != null
						? Duration.between(sessionToDisplay.getStartTime(), Instant.now()).getSeconds()
						: 0;

					boolean hasKc = sessionToDisplay.getAllKillCounts().stream().anyMatch(c -> c.getSessionKc() > 0);



					// Stay visible for 8 seconds, or indefinitely until the first count is acquired
					if (secondsSinceStart < 8 || !hasKc) {
						int hiddenIndividualCount = 0;
						for (TrackedActivity act : TrackedActivity.values()) {
							if (!plugin.isKcVisible(act)) {
								hiddenIndividualCount++;
							}
						}

						List<String> disabledGroups = new ArrayList<>();
						if (!config.enableBoltProcListener()) {
							disabledGroups.add("Enchanted bolt proc counters");
						}
						// Can easily append other disabled groups here in the future

						StringBuilder sb = new StringBuilder();
						sb.append("<html><div style='margin-top: 5px; margin-bottom: 5px;'>");
						sb.append("<b style='color: #40ff40;'>Counter is ");

						if (hiddenIndividualCount > 0 || !disabledGroups.isEmpty()) {
							sb.append("partially ");
						}
						sb.append("active!</b><br><br>");

						if (hiddenIndividualCount > 0) {
							sb.append("A total of <b>").append(hiddenIndividualCount)
								.append("</b> individual counters have been disabled/hidden. ")
								.append("They can be made visible by removing the key from the list in the textbox at the bottom of the plugin config panel.<br><br>");
						}

						if (!soundEffectListener.isHasSoundEffects())
						{
							sb.append("Sound effects are currently muted. This may impact the performance of some counters like the teletab/enchanted bolt proc counters. " +
								"If you wish to use these counters, consider setting the volume to 1 instead.<br><br>");
						}

						if (!disabledGroups.isEmpty()) {
							sb.append("The following groups have been completely disabled:<br>");
							for (String group : disabledGroups) {
								sb.append("- ").append(group).append("<br>");
							}
							sb.append("They can be re-enabled by checking their respective boxes in the 'Group filters' config section.");
						}

						sb.append("</div></html>");

						warningLabel.setText(sb.toString());
						warningLabel.setVisible(true);
					} else {
						warningLabel.setVisible(false);
					}
				} else {
					warningLabel.setVisible(false);
				}

			} else {
				timePassedLabel.setText("Time passed: NA");
				startTimeLabel.setVisible(false);
				endTimeLabel.setVisible(false);
				warningLabel.setVisible(false);
				notesScrollPane.setVisible(false);
			}
		});
	}

	public void refreshKcContainer() {
		SwingUtilities.invokeLater(() -> {
			boolean loggedIn = plugin.isLoggedIn();
			toggleSessionButton.setEnabled(loggedIn);

			Session activeSession = plugin.getCurrentSession();
			if (activeSession != null && activeSession.isInProgress()) {
				toggleSessionButton.setText("Stop Session");
				toggleSessionButton.setBackground(loggedIn ? ColorScheme.PROGRESS_ERROR_COLOR : ColorScheme.DARK_GRAY_COLOR);
			} else {
				toggleSessionButton.setText("Start Session");
				toggleSessionButton.setBackground(loggedIn ? ColorScheme.PROGRESS_COMPLETE_COLOR : ColorScheme.DARK_GRAY_COLOR);
			}

			SessionNode selectedNode = (SessionNode) sessionComboBox.getSelectedItem();
			Session sessionToDisplay = null;

			if (selectedNode != null) {
				if (selectedNode.session == null) {
					sessionToDisplay = plugin.getCurrentSession();
					renameSessionButton.setVisible(false);
					deleteSessionButton.setVisible(false);
				} else {
					sessionToDisplay = selectedNode.session;
					renameSessionButton.setVisible(true);
					deleteSessionButton.setVisible(true);
				}
			} else {
				renameSessionButton.setVisible(false);
				deleteSessionButton.setVisible(false);
			}

			updateTime();
			buildKcContainer(sessionToDisplay);
		});
	}

	private void buildKcContainer(Session sessionToDisplay) {
		kcContainer.removeAll();

		if (sessionToDisplay != null) {
			List<Count> visibleKcs = plugin.getDisplayKcs(sessionToDisplay);

			boolean isFirstCategory = true;

			List<Category> sortedCategories = Arrays.asList(Category.values());
			sortedCategories.sort(Comparator.comparingInt(plugin::getCategorySortOrder));

			for (Category category : sortedCategories) {
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
						kcPanel.setToolTipText("Right-click to hide");

						JPopupMenu popupMenu = new JPopupMenu();
						JMenuItem hideItem = new JMenuItem("Hide " + kc.getName());
						hideItem.addActionListener(e -> {
							TrackedActivity activityToHide = plugin.getActivityById(kc.getVarPlayerId());
							if (activityToHide != null) {
								plugin.disableActivity(activityToHide);
							}
						});
						popupMenu.add(hideItem);
						kcPanel.setComponentPopupMenu(popupMenu);

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