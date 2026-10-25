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

import com.activitycounter.models.TrackedActivity;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import net.runelite.client.ui.ColorScheme;

public class HelpControlPanel extends JPanel
{
	private final JComboBox<HelpTopic> topicComboBox = new JComboBox<>();
	private final JEditorPane contentPane = new JEditorPane();

	// Tracks which categories are currently unfolded
	private final Set<String> expandedCategories = new HashSet<>();

	private static class HelpTopic
	{
		final String title;
		final Supplier<String> htmlContentProvider;

		// Constructor for static HTML strings
		HelpTopic(String title, String staticHtml)
		{
			this.title = title;
			this.htmlContentProvider = () -> staticHtml;
		}

		// Constructor for dynamic HTML generation
		HelpTopic(String title, Supplier<String> htmlContentProvider)
		{
			this.title = title;
			this.htmlContentProvider = htmlContentProvider;
		}

		@Override
		public String toString()
		{
			return title;
		}
	}

	public HelpControlPanel()
	{
		setLayout(new BorderLayout(0, 5));
		setBorder(new EmptyBorder(5, 0, 0, 0));

		// Setup Editor Pane (HTML support, read-only, transparent background)
		contentPane.setContentType("text/html");
		contentPane.setEditable(false);
		contentPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		contentPane.setOpaque(false);
		contentPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

		// Wrap in a scroll pane with a fixed preferred height so it doesn't push other UI elements out of bounds
		final JScrollPane scrollPane = new JScrollPane(contentPane);
		scrollPane.setPreferredSize(new Dimension(0, 150));
		scrollPane.setBorder(null);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);

		// Add Hyperlink Listener to catch folding toggles
		contentPane.addHyperlinkListener(e -> {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				String desc = e.getDescription();
				if (desc != null && desc.startsWith("toggle:")) {
					String category = desc.substring(7);

					// Toggle the expanded state
					if (expandedCategories.contains(category)) {
						expandedCategories.remove(category);
					} else {
						expandedCategories.add(category);
					}

					// Save current scroll position so the UI doesn't jump
					int scrollValue = scrollPane.getVerticalScrollBar().getValue();

					// Re-render HTML
					HelpTopic selected = (HelpTopic) topicComboBox.getSelectedItem();
					if (selected != null) {
						contentPane.setText("<html><body style='color: white;'>" + selected.htmlContentProvider.get() + "</body></html>");

						// Restore scroll position after Swing finishes painting the new HTML
						SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(scrollValue));
					}
				}
			}
		});

		// Populate Topics
		topicComboBox.addItem(new HelpTopic(
			"Select a topic...",
			"<i>Select a topic from the dropdown to learn more about the plugin's features.</i>"
		));

		// Use a dynamic method reference so the HTML is freshly built with the current folding states
		topicComboBox.addItem(new HelpTopic(
			"Supported Counters",
			this::buildCounterListHtml
		));

		topicComboBox.addItem(new HelpTopic(
			"Pause button",
			"<b style='color: #ff9933;'>Pause button</b><br>" +
				"The pause button (on the tab panel to the far left) allows you to pause the tracking immediately " +
				"and resume when you see fit. "
		));

		topicComboBox.addItem(new HelpTopic(
			"Configuration Presets",
			"<b style='color: #ff9933;'>Configuration Presets</b><br>" +
				"Presets allow you to quickly save and load your current UI state, including pinned activities and visible counters.<br><br>" +
				"Use this to create custom overlays for specific activities (e.g., 'Zulrah Tracker' or 'Farm Run')."
		));

		topicComboBox.addItem(new HelpTopic(
			"Custom Scoring",
			"<b style='color: #ff9933;'>Custom Scoring</b><br>" +
				"Right-click any counter to assign it a positive or negative score.<br><br>" +
				"During farm runs, it may be used to track money made by setting a negative score for seeds planted " +
				"and a positive score for herbs harvested, both equal to their value."
		));

		topicComboBox.addItem(new HelpTopic(
			"Context Menus",
			"<b style='color: #ff9933;'>Context Menus</b><br>" +
				"Almost everything can be right-clicked! E.g.,<br><br>" +
				"• <b>Activities:</b> Pin to overlay, hide, reset, or set score.<br>" +
				"• <b>Category Headers:</b> Pin/unpin all items in the group.<br>" +
				"• <b>Score/Time Labels:</b> Pin them to the top of your in-game overlay.<br>" +
				"If something can be right-clicked, it is often indicated via a tooltip<br>" +
				"when hovering over it."
		));

		// Add Listener to inject the HTML when a new topic is selected from the dropdown
		topicComboBox.addActionListener(e -> {
			HelpTopic selected = (HelpTopic) topicComboBox.getSelectedItem();
			if (selected != null) {
				contentPane.setText("<html><body style='color: white;'>" + selected.htmlContentProvider.get() + "</body></html>");
				// Reset scroll to top when completely switching topics
				SwingUtilities.invokeLater(() -> contentPane.setCaretPosition(0));
			}
		});

		// Trigger initial load
		topicComboBox.setSelectedIndex(0);

		add(topicComboBox, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Dynamically generates an HTML list of all supported counters, grouped by their Category.
	 * Categories are folded by default and toggleable.
	 */
	private String buildCounterListHtml()
	{
		Map<String, List<String>> groupedCounters = new LinkedHashMap<>();

		// Group every activity by its parsed category name
		for (TrackedActivity activity : TrackedActivity.values())
		{
			String rawCategory = activity.getCategory().name();
			String prettyCategory = rawCategory.substring(0, 1).toUpperCase() +
				rawCategory.substring(1).toLowerCase().replace("_", " ");

			groupedCounters.computeIfAbsent(prettyCategory, k -> new ArrayList<>()).add(activity.getName());
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<b style='color: #ff9933;'>Supported Counters</b><br>");
		sb.append("<i>Click a category to view its tracked items.</i><br><br>");

		for (Map.Entry<String, List<String>> entry : groupedCounters.entrySet())
		{
			String cat = entry.getKey();
			boolean isExpanded = expandedCategories.contains(cat);
			String icon = isExpanded ? "[-]" : "[+]";

			// Build the clickable header link using a custom "toggle:" protocol
			sb.append("<a href=\"toggle:").append(cat).append("\" style=\"color: #aaddff; text-decoration: none; font-weight: bold;\">")
				.append(icon).append(" ").append(cat).append("</a><br>");

			// If expanded, render the list items. Otherwise, insert a small space.
			if (isExpanded)
			{
				sb.append("<ul style='margin-top: 2px; margin-bottom: 10px;'>");
				for (String counterName : entry.getValue())
				{
					sb.append("<li>").append(counterName).append("</li>");
				}
				sb.append("</ul>");
			}
			else
			{
				sb.append("<div style='margin-bottom: 5px;'></div>");
			}
		}

		return sb.toString();
	}
}