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

package com.activitycounter.ui;

import com.activitycounter.ActivityCounterConfig;
import com.activitycounter.ActivityCounterPlugin;
import com.activitycounter.listeners.core.ConfigListener;
import com.activitycounter.listeners.core.SoundEffectListener;
import com.activitycounter.managers.IdleMessageManager;
import com.activitycounter.models.Count;
import com.activitycounter.models.Session;
import com.activitycounter.models.TrackedActivity;
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

public class SessionInfoPanel extends JPanel
{
	private final ActivityCounterPlugin plugin;
	private final ConfigListener configListener;
	private final ActivityCounterConfig config;
	private final SoundEffectListener soundEffectListener;
	private final ActivityCounterPanel parentPanel;

	private final JLabel attachedPresetLabel = new JLabel();
	private final JLabel timePassedLabel = new JLabel();
	private final JLabel startTimeLabel = new JLabel();
	private final JLabel endTimeLabel = new JLabel();
	private final JLabel scoreLabel = new JLabel();
	private final JLabel warningLabel = new JLabel();
	private final JTextArea notesArea = new JTextArea();
	private final JScrollPane notesScrollPane = new JScrollPane(notesArea);
	private final IdleMessageManager idleMessageManager;

	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm")
		.withZone(ZoneId.systemDefault());

	// Keep track of the session currently being displayed so context menus and focus listeners target the correct data
	private Session displayedSession = null;

	public SessionInfoPanel(
		ActivityCounterPlugin plugin,
		ConfigListener configListener,
		ActivityCounterConfig config,
		SoundEffectListener soundEffectListener,
		ActivityCounterPanel parentPanel,
		IdleMessageManager idleMessageManager
	)
	{
		this.plugin = plugin;
		this.configListener = configListener;
		this.config = config;
		this.soundEffectListener = soundEffectListener;
		this.parentPanel = parentPanel;
		this.idleMessageManager = idleMessageManager;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		setupComponents();
		setupContextMenus();

		add(Box.createRigidArea(new Dimension(0, 5)));
		add(timePassedLabel);
		add(startTimeLabel);
		add(endTimeLabel);
		add(scoreLabel);
		add(Box.createRigidArea(new Dimension(0, 5)));
		add(warningLabel);
		add(Box.createRigidArea(new Dimension(0, 5)));
		add(notesScrollPane);
	}

	private void setupComponents()
	{
		warningLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		warningLabel.setVisible(false);
		warningLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		timePassedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		timePassedLabel.setToolTipText("Right-click to configure visibility");

		scoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD));
		scoreLabel.setForeground(ColorScheme.BRAND_ORANGE);
		scoreLabel.setVisible(false);
		scoreLabel.setToolTipText("Right-click to configure score");

		startTimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		startTimeLabel.setToolTipText("Right-click to hide");

		endTimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		endTimeLabel.setToolTipText("Right-click to hide");

		attachedPresetLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		attachedPresetLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		attachedPresetLabel.setToolTipText("Right-click to detach preset");
		attachedPresetLabel.setVisible(false);

		notesArea.setLineWrap(true);
		notesArea.setWrapStyleWord(true);
		notesArea.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		notesArea.setForeground(Color.WHITE);
		notesArea.setToolTipText("Right-click to hide");

		notesScrollPane.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 85));
		notesScrollPane.setVisible(false);
		notesScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

		notesArea.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (displayedSession != null) {
					displayedSession.setNotes(notesArea.getText());
					plugin.saveSession(displayedSession);
				}
			}
		});
	}

	private void setupContextMenus()
	{
		// --- Time Passed & General Visibility Menu ---
		JPopupMenu timeMenu = new JPopupMenu();
		JMenuItem pinTimeItem = new JMenuItem();

		JMenu visibilityMenu = new JMenu("Toggle Panel Visibility");
		JMenuItem toggleStartTimeItem = new JMenuItem();
		JMenuItem toggleEndTimeItem = new JMenuItem();
		JMenuItem toggleNotesItem = new JMenuItem();
		JMenuItem toggleDurationItem = new JMenuItem();

		timeMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				pinTimeItem.setText(config.pinTimeOverlay() ? "Remove duration from overlay" : "Add duration to overlay");
				toggleStartTimeItem.setText(config.showStartTime() ? "Hide start time" : "Show start time");
				toggleEndTimeItem.setText(config.showEndTime() ? "Hide end time" : "Show end time");
				toggleNotesItem.setText(config.showNotes() ? "Hide notes panel" : "Show notes panel");
				toggleDurationItem.setText(config.showSessionDuration() ? "Hide session duration" : "Show session duration");
			}
			@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
			@Override public void popupMenuCanceled(PopupMenuEvent e) {}
		});

		pinTimeItem.addActionListener(e -> configListener.togglePinTime());
		toggleStartTimeItem.addActionListener(e -> { configListener.toggleShowStartTime(); updateInfo(displayedSession); });
		toggleEndTimeItem.addActionListener(e -> { configListener.toggleShowEndTime(); updateInfo(displayedSession); });
		toggleNotesItem.addActionListener(e -> { configListener.toggleShowNotes(); updateInfo(displayedSession); });
		toggleDurationItem.addActionListener(e -> { configListener.toggleShowSessionDuration(); updateInfo(displayedSession); });

		visibilityMenu.add(toggleDurationItem);
		visibilityMenu.add(toggleStartTimeItem);
		visibilityMenu.add(toggleEndTimeItem);
		visibilityMenu.add(toggleNotesItem);

		timeMenu.add(pinTimeItem);
		timeMenu.addSeparator();
		timeMenu.add(visibilityMenu);

		timePassedLabel.setComponentPopupMenu(timeMenu);

		// --- Start Time Menu ---
		JPopupMenu startTimeMenu = new JPopupMenu();
		JMenuItem hideStartTimeItem = new JMenuItem("Hide start time");
		hideStartTimeItem.addActionListener(e -> { configListener.toggleShowStartTime(); updateInfo(displayedSession); });
		startTimeMenu.add(hideStartTimeItem);
		startTimeLabel.setComponentPopupMenu(startTimeMenu);

		// --- End Time Menu ---
		JPopupMenu endTimeMenu = new JPopupMenu();
		JMenuItem hideEndTimeItem = new JMenuItem("Hide end time");
		hideEndTimeItem.addActionListener(e -> { configListener.toggleShowEndTime(); updateInfo(displayedSession); });
		endTimeMenu.add(hideEndTimeItem);
		endTimeLabel.setComponentPopupMenu(endTimeMenu);

		// --- Notes Menu ---
		JPopupMenu notesMenu = new JPopupMenu();
		JMenuItem hideNotesItem = new JMenuItem("Hide notes panel");
		hideNotesItem.addActionListener(e -> { configListener.toggleShowNotes(); updateInfo(displayedSession); });
		notesMenu.add(hideNotesItem);
		notesArea.setComponentPopupMenu(notesMenu);

		// --- Score Menu ---
		JPopupMenu scoreMenu = new JPopupMenu();
		JMenuItem pinScoreItem = new JMenuItem();
		JMenuItem toggleScoreHourlyItem = new JMenuItem();

		scoreMenu.addPopupMenuListener(new PopupMenuListener() {
			@Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				pinScoreItem.setText(config.pinScoreOverlay() ? "Remove from ingame overlay" : "Add to ingame overlay");
				toggleScoreHourlyItem.setText(config.scoreHourlyRate() ? "Show absolute score" : "Show per hour (/hr)");
			}
			@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
			@Override public void popupMenuCanceled(PopupMenuEvent e) {}
		});

		pinScoreItem.addActionListener(e -> configListener.togglePinScore());
		toggleScoreHourlyItem.addActionListener(e -> {
			configListener.toggleScoreHourlyRate();
			updateInfo(displayedSession);
		});

		JMenuItem resetCountsItem = new JMenuItem("Reset all counts to 0");
		resetCountsItem.addActionListener(e -> {
			if (displayedSession != null) {
				int confirm = JOptionPane.showConfirmDialog(
					this,
					"Are you sure you want to reset all counts in this session to 0?",
					"Reset Counts",
					JOptionPane.YES_NO_OPTION
				);
				if (confirm == JOptionPane.YES_OPTION) {
					plugin.resetAllActivityCounts(displayedSession);
				}
			}
		});

		JMenuItem resetScoresItem = new JMenuItem("Reset all scores to 0");
		resetScoresItem.addActionListener(e -> {
			if (displayedSession != null) {
				int confirm = JOptionPane.showConfirmDialog(
					this,
					"Are you sure you want to reset all assigned scores to 0?",
					"Reset Scores",
					JOptionPane.YES_NO_OPTION
				);
				if (confirm == JOptionPane.YES_OPTION) {
					for (Count kc : displayedSession.getAllKillCounts()) {
						if (kc.getScore() != 0f) {
							TrackedActivity activity = plugin.getActivityById(kc.getTrackingId());
							if (activity != null) plugin.setActivityScore(activity, 0f);
						}
					}
					plugin.setRequiresSaveAndRefresh(true);
					updateInfo(displayedSession);
				}
			}
		});

		JMenuItem changeLabelItem = new JMenuItem("Change score label");
		changeLabelItem.addActionListener(e -> {
			String input = JOptionPane.showInputDialog(this, "Enter new score label:", config.customScoreLabel());
			if (input != null && !input.trim().isEmpty()) {
				configListener.setCustomScoreLabel(input.trim());
				updateInfo(displayedSession);
			}
		});

		scoreMenu.add(pinScoreItem);
		scoreMenu.add(toggleScoreHourlyItem);
		scoreMenu.add(resetCountsItem);
		scoreMenu.add(resetScoresItem);
		scoreMenu.add(changeLabelItem);
		scoreLabel.setComponentPopupMenu(scoreMenu);
	}

	/**
	 * Takes a session and refreshes all UI components to reflect its current state.
	 */
	public void updateInfo(Session sessionToDisplay)
	{
		this.displayedSession = sessionToDisplay;

		SwingUtilities.invokeLater(() -> {
			if (sessionToDisplay != null) {

				// Time Passed
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

				// Score Logic
				boolean hasScore = false;
				float totalScore = 0f;

				for (Count kc : sessionToDisplay.getAllKillCounts()) {
					if (kc.getScore() != 0f) {
						hasScore = true;
					}
					totalScore += kc.getSessionKc() * kc.getScore();
				}

				if (hasScore) {
					String scoreDisplayStr;
					if (config.scoreHourlyRate()) {
						int secondsPassed = sessionToDisplay.getSecondsPassed();
						if (secondsPassed > 0) {
							double hours = secondsPassed / 3600.0;
							double rate = totalScore / hours;
							if (rate >= 100.0) scoreDisplayStr = String.format("%,d/hr", Math.round(rate));
							else scoreDisplayStr = String.format("%,.2f/hr", rate);
						} else {
							scoreDisplayStr = "0/hr";
						}
					} else {
						if (Math.abs(totalScore - Math.round(totalScore)) < 0.0001) {
							scoreDisplayStr = String.format("%,d", Math.round(totalScore));
						} else {
							scoreDisplayStr = String.format("%,.2f", totalScore);
						}
					}

					scoreLabel.setText(String.format("%s: %s", config.customScoreLabel(), scoreDisplayStr));
					scoreLabel.setVisible(true);
				} else {
					scoreLabel.setVisible(false);
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

				// Warning Check
				if (sessionToDisplay.isInProgress()) {
					long secondsSinceStart = sessionToDisplay.getStartTime() != null
						? Duration.between(sessionToDisplay.getStartTime(), Instant.now()).getSeconds()
						: 0;

					boolean hasKc = sessionToDisplay.getAllKillCounts().stream().anyMatch(c -> c.getSessionKc() > 0);

					if (secondsSinceStart < 8 || !hasKc) {
						// The manager handles all HTML building and state evaluation dynamically
						warningLabel.setText(idleMessageManager.getIdleMessage());
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
				scoreLabel.setVisible(false);
				warningLabel.setVisible(false);
				notesScrollPane.setVisible(false);
			}

			// Force the UI container to recalculate layout constraints so the elements shift seamlessly
			revalidate();
			repaint();
		});
	}
}