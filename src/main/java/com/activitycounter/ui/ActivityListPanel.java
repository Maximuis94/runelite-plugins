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

import com.activitycounter.ActivityCounterPlugin;
import com.activitycounter.listeners.core.ConfigListener;
import com.activitycounter.models.Category;
import com.activitycounter.models.Count;
import com.activitycounter.models.Session;
import com.activitycounter.models.TrackedActivity;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import net.runelite.client.ui.ColorScheme;

public class ActivityListPanel extends JPanel
{
	private final ActivityCounterPlugin plugin;
	private final ConfigListener configListener;
	private final ActivityCounterPanel parentPanel;

	private final JPanel listContainer = new JPanel();

	public ActivityListPanel(ActivityCounterPlugin plugin, ConfigListener configListener, ActivityCounterPanel parentPanel)
	{
		this.plugin = plugin;
		this.configListener = configListener;
		this.parentPanel = parentPanel;

		setLayout(new BorderLayout());

		listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
		add(listContainer, BorderLayout.NORTH);
	}

	/**
	 * Clears and rebuilds the entire list of tracked activities for the given session.
	 */
	public void updateList(Session sessionToDisplay)
	{
		listContainer.removeAll();

		if (sessionToDisplay != null) {
			List<Count> visibleKcs = plugin.getDisplayKcs(sessionToDisplay);

			boolean isFirstCategory = true;

			List<Category> sortedCategories = Arrays.asList(Category.values());
			sortedCategories.sort(Comparator.comparingInt(configListener::getCategorySortOrder));

			for (Category category : sortedCategories) {
				List<Count> categoryKcs = visibleKcs.stream()
					.filter(kc -> plugin.getActivityCategory(kc.getTrackingId()) == category)
					.sorted(Comparator.comparingInt(kc -> plugin.getActivityOrder(kc.getTrackingId())))
					.collect(Collectors.toList());

				if (!categoryKcs.isEmpty()) {
					if (!isFirstCategory) {
						listContainer.add(Box.createRigidArea(new Dimension(0, 15)));
					}
					isFirstCategory = false;

					// --- Category Header ---
					JPanel categoryHeader = new JPanel(new BorderLayout());
					categoryHeader.setBackground(ColorScheme.DARKER_GRAY_COLOR.darker());
					categoryHeader.setBorder(new EmptyBorder(5, 5, 5, 5));

					JLabel categoryLabel = new JLabel(category.getHeader());
					categoryLabel.setForeground(Color.WHITE);
					categoryLabel.setFont(categoryLabel.getFont().deriveFont(Font.BOLD));

					// Asynchronously fetch and set the icon to prevent blocking the UI thread
					if (category.getIconId() != -1) {
						plugin.getSpriteManager().getSpriteAsync(category.getIconId(), 0, img -> {
							if (img != null) {
								SwingUtilities.invokeLater(() -> {
									categoryLabel.setIcon(new ImageIcon(img));
									categoryLabel.setIconTextGap(6);
									categoryLabel.revalidate();
									categoryLabel.repaint();
								});
							}
						});
					}

					List<TrackedActivity> activities = categoryKcs.stream()
						.map(kc -> TrackedActivity.getByStorageId(kc.getTrackingId()))
						.filter(Objects::nonNull)
						.collect(Collectors.toList());

					attachHeaderContextMenu(categoryHeader, activities);

					categoryHeader.add(categoryLabel, BorderLayout.WEST);
					listContainer.add(categoryHeader);
					listContainer.add(Box.createRigidArea(new Dimension(0, 5)));

					// --- Activity Rows ---
					for (int i = 0; i < categoryKcs.size(); i++) {
						Count kc = categoryKcs.get(i);
						TrackedActivity activity = TrackedActivity.getByStorageId(kc.getTrackingId());

						JPanel kcPanel = buildActivityRow(kc, activity, sessionToDisplay);
						listContainer.add(kcPanel);

						if (i < categoryKcs.size() - 1) {
							listContainer.add(Box.createRigidArea(new Dimension(0, 5)));
						}
					}
				}
			}
		}

		revalidate();
		repaint();
	}

	/**
	 * Extracts the row-generation logic for a single activity to keep the main loop clean.
	 */
	private JPanel buildActivityRow(Count kc, TrackedActivity activity, Session sessionToDisplay)
	{
		JPanel kcPanel = new JPanel(new BorderLayout());
		kcPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		kcPanel.setBorder(new EmptyBorder(3, 15, 3, 5));
		kcPanel.setToolTipText("Right-click to configure");

		JPopupMenu popupMenu = new JPopupMenu();

		// --- TOGGLE /HR CONTEXT MENU ITEM ---
		boolean isHourly = configListener.isHourlyRate(kc.getTrackingId());
		JMenuItem toggleHourlyItem = new JMenuItem(isHourly ? "Show absolute count" : "Show per hour (/hr)");
		toggleHourlyItem.addActionListener(e -> {
			if (activity != null) {
				configListener.toggleHourlyRate(activity);
				parentPanel.refreshKcContainer(); // Refresh full UI to update formatting
			}
		});
		popupMenu.add(toggleHourlyItem);

		JMenuItem resetCountItem = new JMenuItem("Reset count to 0");
		resetCountItem.addActionListener(e -> {
			int confirm = JOptionPane.showConfirmDialog(
				this,
				"Are you sure you want to reset the count for " + kc.getName() + " to 0?",
				"Reset Count",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE
			);

			if (confirm == JOptionPane.YES_OPTION) {
				if (activity != null) {
					plugin.resetActivityCount(sessionToDisplay, activity);
				}
			}
		});
		popupMenu.add(resetCountItem);

		JMenuItem hideItem = new JMenuItem("Hide " + kc.getName());
		hideItem.addActionListener(e -> {
			if (activity != null) {
				plugin.disableActivity(activity);
			}
		});
		popupMenu.add(hideItem);

		JMenuItem setScoreItem = new JMenuItem("Set score for " + kc.getName());
		setScoreItem.addActionListener(e -> {
			if (activity != null) {
				String currentScore = (Math.abs(kc.getScore() - Math.round(kc.getScore())) < 0.0001) ?
					String.valueOf(Math.round(kc.getScore())) : String.valueOf(kc.getScore());

				String input = JOptionPane.showInputDialog(this,
					"Enter score per count for " + kc.getName() + " (e.g. 1.5, -0.5):", currentScore);

				if (input != null) {
					try {
						float newScore = Float.parseFloat(input.trim());
						plugin.setActivityScore(activity, newScore);
					} catch (NumberFormatException ex) {
						JOptionPane.showMessageDialog(this, "Invalid number format. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		popupMenu.add(setScoreItem);

		// Dynamic In-Game Overlay Pin Menu
		JMenuItem pinItem = new JMenuItem();
		JMenuItem moveToTopItem = new JMenuItem("Move to overlay top");

		popupMenu.addPopupMenuListener(new PopupMenuListener()
		{
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{
				boolean isPinned = plugin.isActivityPinned(activity);
				pinItem.setText(isPinned ? "Remove from ingame overlay" : "Add to ingame overlay");
				moveToTopItem.setVisible(isPinned);
			}
			@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
			@Override public void popupMenuCanceled(PopupMenuEvent e) {}
		});

		pinItem.addActionListener(e -> plugin.togglePinnedActivity(activity));
		moveToTopItem.addActionListener(e -> plugin.movePinnedActivityToTop(activity));

		popupMenu.add(pinItem);
		popupMenu.add(moveToTopItem);

		kcPanel.setComponentPopupMenu(popupMenu);

		JLabel nameLabel = new JLabel(kc.getName());
		nameLabel.setForeground(Color.WHITE);

		// --- COMPUTE COUNT OR /HR STRING ---
		String countDisplay;
		if (isHourly)
		{
			int seconds = sessionToDisplay.getSecondsPassed();
			if (seconds > 0)
			{
				double hours = seconds / 3600.0;
				double ratePerHour = kc.getSessionKc() / hours;

				if (ratePerHour >= 100.0)
				{
					countDisplay = String.format("%,d/hr", Math.round(ratePerHour));
				}
				else
				{
					countDisplay = String.format("%,.1f/hr", ratePerHour);
				}
			}
			else
			{
				countDisplay = "0/hr";
			}
		}
		else
		{
			countDisplay = String.format("%,d", kc.getSessionKc());
		}

		JLabel countLabel = new JLabel(countDisplay);
		countLabel.setForeground(ColorScheme.BRAND_ORANGE);

		kcPanel.add(nameLabel, BorderLayout.WEST);
		kcPanel.add(countLabel, BorderLayout.EAST);

		return kcPanel;
	}

	private void attachHeaderContextMenu(JComponent headerComponent, java.util.List<TrackedActivity> categoryActivities)
	{
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem toggleItem = new JMenuItem();
		popupMenu.add(toggleItem);

		popupMenu.addPopupMenuListener(new PopupMenuListener()
		{
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{
				boolean allPinned = categoryActivities.stream().allMatch(plugin::isActivityPinned);
				toggleItem.setText(allPinned ? "Remove all from ingame overlay" : "Add all to ingame overlay");
			}
			@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
			@Override public void popupMenuCanceled(PopupMenuEvent e) {}
		});

		toggleItem.addActionListener(e ->
		{
			boolean allPinned = categoryActivities.stream().allMatch(plugin::isActivityPinned);
			if (allPinned)
			{
				plugin.unpinAllActivities(categoryActivities);
			}
			else
			{
				plugin.pinAllActivities(categoryActivities);
			}
		});

		headerComponent.setComponentPopupMenu(popupMenu);
	}
}