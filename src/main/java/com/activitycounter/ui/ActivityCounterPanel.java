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
import static com.activitycounter.PluginConstants.FileName.ICON_FILE_ABOUT;
import static com.activitycounter.PluginConstants.FileName.ICON_FILE_PAUSE;
import static com.activitycounter.PluginConstants.FileName.ICON_FILE_PLAY;
import static com.activitycounter.PluginConstants.FileName.ICON_FILE_PRESETS;
import static com.activitycounter.PluginConstants.FileName.ICON_FILE_SESSION;
import com.activitycounter.listeners.core.ConfigListener;
import com.activitycounter.listeners.core.SoundEffectListener;
import com.activitycounter.managers.IdleMessageManager;
import com.activitycounter.managers.PresetManager;
import com.activitycounter.models.Session;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;

public class ActivityCounterPanel extends PluginPanel
{

	private final ActivityCounterPlugin plugin;

	private final JTabbedPane topTabs;
	private final JButton pauseButton = new JButton();
	public static final int TOP_TAB_WIDTH = 55;
	public static final int TOP_TAB_HEIGHT = 35;

	private final ImageIcon playIcon;
	private final ImageIcon pauseIcon;
	private final ImageIcon sessionIcon;
	private final ImageIcon presetIcon;
	private final ImageIcon helpIcon;

	private final SessionControlPanel sessionControlPanel;
	private final PresetControlPanel presetControlPanel;
	private final SessionInfoPanel sessionInfoPanel;
	private final HelpControlPanel helpControlPanel;
	private final ActivityListPanel activityListPanel;
	private final IdleMessageManager idleMessageManager;

	public ActivityCounterPanel(
		ActivityCounterPlugin plugin,
		SoundEffectListener soundEffectListener,
		ConfigListener configListener,
		PresetManager presetManager,
		IdleMessageManager idleMessageManager)
	{
		this.plugin = plugin;
		ActivityCounterConfig config = plugin.getConfig();

		setLayout(new BorderLayout(0, 10));
		setBorder(new EmptyBorder(10, 10, 10, 10));

		this.sessionControlPanel = new SessionControlPanel(plugin, this);
		this.presetControlPanel = new PresetControlPanel(presetManager, this, plugin);
		this.sessionInfoPanel = new SessionInfoPanel(plugin, configListener, config, soundEffectListener, this, idleMessageManager);
		this.activityListPanel = new ActivityListPanel(plugin, configListener, this);
		this.idleMessageManager = idleMessageManager;
		this.helpControlPanel = new HelpControlPanel();

		topTabs = new JTabbedPane();

		final BufferedImage playImg = ImageUtil.loadImageResource(ActivityCounterPlugin.class, ICON_FILE_PLAY);
		this.playIcon = playImg != null ? new ImageIcon(playImg.getScaledInstance(14, 14, Image.SCALE_SMOOTH)) : null;

		final BufferedImage pauseImg = ImageUtil.loadImageResource(ActivityCounterPlugin.class, ICON_FILE_PAUSE);
		this.pauseIcon = pauseImg != null ? new ImageIcon(pauseImg.getScaledInstance(14, 14, Image.SCALE_SMOOTH)) : null;

		final BufferedImage sessionImg = ImageUtil.loadImageResource(ActivityCounterPlugin.class, ICON_FILE_SESSION);
		this.sessionIcon = sessionImg != null ? new ImageIcon(sessionImg.getScaledInstance(18, 18, Image.SCALE_SMOOTH)) : null;

		final BufferedImage presetImg = ImageUtil.loadImageResource(ActivityCounterPlugin.class, ICON_FILE_PRESETS);
		this.presetIcon = presetImg != null ? new ImageIcon(presetImg.getScaledInstance(18, 18, Image.SCALE_SMOOTH)) : null;

		final BufferedImage helpImg = ImageUtil.loadImageResource(ActivityCounterPlugin.class, ICON_FILE_ABOUT);
		this.helpIcon = helpImg != null ? new ImageIcon(helpImg.getScaledInstance(18, 18, Image.SCALE_SMOOTH)) : null;

		// Configure the pause button
		pauseButton.setPreferredSize(new Dimension(24, 24));
		pauseButton.setFocusable(false);
		pauseButton.setContentAreaFilled(false);
		pauseButton.setBorder(new EmptyBorder(0, 4, 0, 4));

		pauseButton.addActionListener(e -> {
			if (plugin.getCurrentSession() != null && plugin.getCurrentSession().isInProgress())
			{
				plugin.toggleSessionPaused();
			}
		});

		// Build tabs natively using icons and tooltips instead of text
		topTabs.putClientProperty("JTabbedPane.leadingComponent", pauseButton);
		topTabs.addTab("", sessionIcon, sessionControlPanel, "Sessions");

		if (config.showPresetsTab())
		{
			topTabs.addTab("", presetIcon, presetControlPanel, "Presets");
		}

		topTabs.addTab("", helpIcon, helpControlPanel, "Help");

		// Use BorderLayout to anchor elements tightly to the top and fill the remaining space
		JPanel northWrapper = new JPanel(new BorderLayout(0, 5));
		northWrapper.add(topTabs, BorderLayout.NORTH);
		northWrapper.add(sessionInfoPanel, BorderLayout.SOUTH);

		add(northWrapper, BorderLayout.NORTH);
		add(activityListPanel, BorderLayout.CENTER);

		// Force the JTabbedPane to shrink/expand based on the currently selected tab
		topTabs.addChangeListener(e -> {
			Component selected = topTabs.getSelectedComponent();
			if (selected != null)
			{
				// Calculate the height of the tab content + ~35px for the tab header bar
				int requiredHeight = selected.getPreferredSize().height + 35;

				// Apply the new height (width is 0 because BorderLayout automatically stretches width to 100%)
				topTabs.setPreferredSize(new Dimension(0, requiredHeight));

				// Force the main panel layout to update instantly
				revalidate();
				repaint();
			}
		});

		// Force the visual state to correctly sync before the plugin even renders
		updatePauseButtonState();

		Timer updateTimer = new Timer(1000, e -> updateTime());
		updateTimer.start();
	}

	public void forceActiveSessionSelection()
	{
		sessionControlPanel.forceActiveSessionSelection();
	}

	public void reloadComboBox()
	{
		sessionControlPanel.reloadComboBox();
	}

	public void updateTime()
	{
		Session sessionToDisplay = sessionControlPanel.getSelectedSession();
		sessionInfoPanel.updateInfo(sessionToDisplay);
	}

	public void refreshKcContainer()
	{
		SwingUtilities.invokeLater(() -> {
			Session activeSession = plugin.getCurrentSession();
			boolean loggedIn = plugin.isLoggedIn();

			sessionControlPanel.updateUIState(loggedIn, activeSession);

			updatePauseButtonState();

			Session sessionToDisplay = sessionControlPanel.getSelectedSession();
			updateTime();
			activityListPanel.updateList(sessionToDisplay);
		});
	}

	public void updatePresetsTabVisibility()
	{
		SwingUtilities.invokeLater(() -> {
			boolean show = plugin.getConfig().showPresetsTab();
			int index = topTabs.indexOfComponent(presetControlPanel);

			if (show && index == -1)
			{
				// Insert the raw tab, then inject the fixed-width label over it
				topTabs.insertTab(null, null, presetControlPanel, null, 1);
				topTabs.setTabComponentAt(1, createTabLabel(presetIcon, "Presets", 55, 30));
			}
			else if (!show && index != -1)
			{
				// Remove it if it is currently visible
				topTabs.removeTabAt(index);
			}
		});
	}

	public void updatePauseButtonState()
	{
		SwingUtilities.invokeLater(() -> {
			boolean hasActiveSession = plugin.getCurrentSession() != null && plugin.getCurrentSession().isInProgress();
			boolean isPaused = plugin.isSessionPaused() || !hasActiveSession;
			ImageIcon iconToDisplay = isPaused ? playIcon : pauseIcon;

			if (iconToDisplay != null)
			{
				pauseButton.setText("");
				pauseButton.setIcon(iconToDisplay);
				// Ensures the icon remains visible even when setEnabled(false) is active
				pauseButton.setDisabledIcon(iconToDisplay);
			}
			else
			{
				pauseButton.setIcon(null);
				pauseButton.setDisabledIcon(null);
				pauseButton.setText(isPaused ? "►" : "||");
			}

			// Correctly silences and grays out the button when no session exists
			pauseButton.setEnabled(hasActiveSession);

			if (!hasActiveSession)
			{
				pauseButton.setToolTipText("No active session");
			}
			else if (plugin.isSessionPaused())
			{
				pauseButton.setToolTipText("Resume session");
			}
			else
			{
				pauseButton.setToolTipText("Pause session");
			}

			pauseButton.revalidate();
			pauseButton.repaint();
		});
	}

	private JLabel createTabLabel(ImageIcon icon, String tooltip, int width, int height)
	{
		JLabel label = new JLabel(icon);
		label.setToolTipText(tooltip);
		label.setPreferredSize(new Dimension(width, height));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		return label;
	}
}