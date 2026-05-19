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

package com.datalogger.ui.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import net.runelite.client.ui.ColorScheme;
import java.awt.BorderLayout;
import java.awt.LayoutManager;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import net.runelite.client.ui.FontManager;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import net.runelite.client.ui.laf.RuneLiteScrollBarUI;

public final class Components
{

	private Components() {}

	public static RuneLiteScrollBarUI scrollBarUI = new RuneLiteScrollBarUI();

	/**
	 * Creates a standard RuneLite-styled button.
	 */
	public static JButton createStyledButton(String text) {
		JButton button = new JButton(text);
		button.setFocusable(false);
		button.setPreferredSize(new Dimension(0, 30));

		button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		button.setForeground(Color.WHITE);
		button.setBorder(new EmptyBorder(5, 5, 5, 5));

		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent evt) {
				button.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
			}

			@Override
			public void mouseExited(MouseEvent evt) {
				button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			}
		});

		return button;
	}

	public static JButton createStyledButton(String text, @Nonnull ActionListener actionListener) {
		JButton button = createStyledButton(text);
		button.addActionListener(actionListener);


		return button;
	}

	/**
	 * Creates a standard RuneLite-styled text field.
	 * * @param tooltipText Optional tooltip text (HTML supported). Can be null.
	 * @return A styled JTextField.
	 */
	public static JTextField createStyledTextField(String tooltipText) {
		JTextField textField = new JTextField();
		textField.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		textField.setForeground(Color.WHITE);
		textField.setCaretColor(Color.WHITE);
		textField.setBorder(new EmptyBorder(5, 5, 5, 5));

		if (tooltipText != null && !tooltipText.isEmpty()) {
			textField.setToolTipText(tooltipText);
		}

		return textField;
	}

	/**
	 * Creates a styled text field that automatically tracks its previous state.
	 * The provided callback is ONLY triggered if the user presses Enter or clicks away,
	 * AND the text has actually changed since the last trigger.
	 * * @param tooltipText Optional tooltip text.
	 * @param onValueChanged A Consumer that receives the new string when a change is confirmed.
	 * @return A stateful, styled JTextField.
	 */
	public static JTextField createStatefulTextField(@Nonnull Consumer<String> onValueChanged, String tooltipText) {
		JTextField textField = createStyledTextField(tooltipText);

		final String[] lastState = {""};

		textField.addActionListener(e -> {
			if (!textField.getText().equals(lastState[0])) {
				lastState[0] = textField.getText();
				onValueChanged.accept(textField.getText());
			}
		});

		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (!textField.getText().equals(lastState[0])) {
					lastState[0] = textField.getText();
					onValueChanged.accept(textField.getText());
				}
			}
		});

		return textField;
	}

	/**
	 * Internal helper to enforce the RuneLite dark theme on ComboBoxes,
	 * preventing them from defaulting to white when holding custom objects.
	 */
	private static <T> void styleComboBox(JComboBox<T> box) {
		box.setFocusable(false);
		box.setForeground(Color.WHITE);
		box.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		box.setRenderer(new DefaultListCellRenderer() {
			@Override
			public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				if (isSelected) {
					c.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
					c.setForeground(Color.WHITE);
				} else {
					c.setBackground(ColorScheme.DARKER_GRAY_COLOR);
					c.setForeground(Color.LIGHT_GRAY);
				}

				if (c instanceof JLabel) {
					((JLabel) c).setBorder(new EmptyBorder(3, 5, 3, 0));
				}

				return c;
			}
		});
	}

	/**
	 * Creates a standard unfocusable ComboBox styled for RuneLite.
	 */
	public static <T> JComboBox<T> createComboBox() {
		JComboBox<T> box = new JComboBox<>();
		styleComboBox(box);
		return box;
	}

	/**
	 * Creates a standard unfocusable ComboBox styled for RuneLite, pre-filled with items.
	 */
	public static <T> JComboBox<T> createComboBox(T[] items) {
		JComboBox<T> box = new JComboBox<>(items);
		styleComboBox(box);
		return box;
	}

	/**
	 * Creates a JPanel with the standard RuneLite Dark Gray background and a styled TitledBorder.
	 */
	public static JPanel createTitledPanel(String title, LayoutManager layout) {
		JPanel panel = new JPanel(layout);
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		panel.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR),
			title,
			TitledBorder.LEFT, TitledBorder.TOP,
			FontManager.getRunescapeSmallFont(), Color.LIGHT_GRAY
		));
		return panel;
	}

	/**
	 * Creates a stylized statistic card (used in dashboards).
	 */
	public static JPanel createStatCard(String title, String value, String tooltip) {
		JPanel card = new JPanel(new BorderLayout());
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_HOVER_COLOR, 1),
			new EmptyBorder(8, 8, 8, 8)
		));

		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(FontManager.getRunescapeSmallFont());
		titleLabel.setForeground(Color.LIGHT_GRAY);
		titleLabel.setHorizontalAlignment(JLabel.CENTER);

		JLabel valueLabel = new JLabel(value);
		valueLabel.setFont(FontManager.getRunescapeBoldFont());
		valueLabel.setForeground(Color.WHITE);
		valueLabel.setHorizontalAlignment(JLabel.CENTER);

		card.add(titleLabel, BorderLayout.NORTH);
		card.add(valueLabel, BorderLayout.CENTER);

		if (tooltip != null && !tooltip.isEmpty()) {
			card.setToolTipText(tooltip);
			titleLabel.setToolTipText(tooltip);
			valueLabel.setToolTipText(tooltip);
		}

		return card;
	}

	public static JScrollPane createScrollPane(JTable table)
	{
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getVerticalScrollBar().setUI(scrollBarUI);
		return scrollPane;
	}

	public static JScrollPane createScrollPane(Component component)
	{
		JScrollPane scrollPane = new JScrollPane(component) {
			@Override
			public JScrollBar createVerticalScrollBar() {
				return new JScrollBar(javax.swing.JScrollBar.VERTICAL) {
					@Override
					public Dimension getPreferredSize() {
						// Force the width to exactly 8px so it always looks like RuneLite
						return new Dimension(8, super.getPreferredSize().height);
					}
				};
			}
		};

		scrollPane.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getVerticalScrollBar().setUI(scrollBarUI);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Fix mouse wheel speed

		return scrollPane;
	}

	/**
	 * Wraps a component in a JScrollPane with an indestructible RuneLite-styled dark scrollbar.
	 * * @param content The inner component (e.g., a JPanel) you want to be scrollable.
	 * @return A fully styled JScrollPane containing your component.
	 */
	public static JScrollPane wrapWithRuneLiteScrollbar(JComponent content)
	{
		JScrollPane scrollPane = new JScrollPane(content);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JScrollBar customScrollBar = new JScrollBar(JScrollBar.VERTICAL) {
			@Override
			public void updateUI() {
				setUI(new BasicScrollBarUI() {
					@Override
					protected void configureScrollBarColors() {
						this.thumbColor = ColorScheme.MEDIUM_GRAY_COLOR;
						this.trackColor = ColorScheme.DARK_GRAY_COLOR;
					}

					@Override
					protected JButton createDecreaseButton(int orientation) {
						return createZeroButton();
					}

					@Override
					protected JButton createIncreaseButton(int orientation) {
						return createZeroButton();
					}

					private JButton createZeroButton() {
						JButton button = new JButton();
						button.setPreferredSize(new Dimension(0, 0));
						button.setMinimumSize(new Dimension(0, 0));
						button.setMaximumSize(new Dimension(0, 0));
						return button;
					}

					@Override
					protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
						g.setColor(trackColor);
						g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
					}

					@Override
					protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
						if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
						g.setColor(thumbColor);
						g.fillRoundRect(thumbBounds.x + 1, thumbBounds.y + 1, thumbBounds.width - 2, thumbBounds.height - 2, 0, 0);
					}
				});
			}
		};

		customScrollBar.updateUI();
		customScrollBar.setPreferredSize(new Dimension(8, 0));
		customScrollBar.setUnitIncrement(16);

		scrollPane.setVerticalScrollBar(customScrollBar);

		return scrollPane;

	}
}