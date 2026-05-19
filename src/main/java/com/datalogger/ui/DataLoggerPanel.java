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
import com.datalogger.ui.modes.ItemsManagerModePanel;
import com.datalogger.ui.modes.ItemsModePanel;
import com.datalogger.ui.modes.UtilitiesModePanel;
import com.datalogger.ui.utils.Components;
import static com.datalogger.ui.utils.Components.wrapWithRuneLiteScrollbar;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import javax.inject.Inject;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
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
		UtilitiesModePanel utilitiesPanel,
		ColosseumStatisticsModePanel colosseumPanel,
//		ColosseumReviewModePanel colosseumReviewPanel,
		ItemsModePanel itemsPanel,
		ItemsManagerModePanel itemsManagerPanel
	)
	{
		super(false);
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		this.setBackground(ColorScheme.DARK_GRAY_COLOR);
		this.setLayout(new BorderLayout(0, 10));

		JComboBox<PanelViewMode> viewSelector = Components.createComboBox(PanelViewMode.values());

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		topPanel.add(viewSelector, BorderLayout.CENTER);

		cardLayout = new VisibleCardLayout();

		cardContainer = new ScrollableCardContainer(cardLayout);
		cardContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		cardContainer.add(colosseumPanel, PanelViewMode.COLOSSEUM_STATISTICS.name());
//		cardContainer.add(colosseumReviewPanel, PanelViewMode.COLOSSEUM_REVIEW.name());
		cardContainer.add(itemsPanel, PanelViewMode.ITEMS.name());
		cardContainer.add(itemsManagerPanel, PanelViewMode.ITEMS_MANAGER.name());
		cardContainer.add(utilitiesPanel, PanelViewMode.UTILITIES.name());

		viewSelector.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED)
			{
				PanelViewMode selectedMode = (PanelViewMode) e.getItem();
				cardLayout.show(cardContainer, selectedMode.name());

				cardContainer.revalidate();
				cardContainer.repaint();
			}
		});

		JScrollPane scrollPane = wrapWithRuneLiteScrollbar(cardContainer);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(250, 1500));
		scrollPane.setMaximumSize(new Dimension(250, 1500));
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

		// Set it such that it instantly scrolls to the top/bottom
		JScrollBar scrollbar = scrollPane.getVerticalScrollBar();
		scrollbar.setUnitIncrement(512);

		add(topPanel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Custom Scrollable JPanel.
	 * getScrollableTracksViewportWidth() returning TRUE forces child components
	 * to respect the panel width, preventing the horizontal scrollbar from triggering!
	 */
	private static class ScrollableCardContainer extends JPanel implements Scrollable
	{
		public ScrollableCardContainer(CardLayout layout) {
			super(layout);
		}

		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 16;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return visibleRect.height;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}
	}

	/**
	 * Custom CardLayout that only sizes itself to the currently VISIBLE card,
	 * rather than the largest card in the entire deck.
	 */
	private static class VisibleCardLayout extends CardLayout
	{
		@Override
		public Dimension preferredLayoutSize(java.awt.Container parent) {
			for (Component comp : parent.getComponents()) {
				if (comp.isVisible()) {
					java.awt.Insets insets = parent.getInsets();
					Dimension pref = comp.getPreferredSize();
					pref.width += insets.left + insets.right;
					pref.height += insets.top + insets.bottom;
					return pref;
				}
			}
			return super.preferredLayoutSize(parent);
		}
	}
}