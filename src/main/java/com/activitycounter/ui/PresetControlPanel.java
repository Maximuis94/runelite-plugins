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
import com.activitycounter.managers.PresetManager;
import com.activitycounter.models.ConfigPreset;
import com.activitycounter.models.Session;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class PresetControlPanel extends JPanel
{
	private final PresetManager presetManager;
	private final ActivityCounterPanel parentPanel;

	private final JComboBox<PresetNode> presetComboBox = new JComboBox<>();
	private final JButton savePresetButton = new JButton("Save");
	private final JButton loadPresetButton = new JButton("Load");
	private final JButton deletePresetButton = new JButton("Delete");
	private final ActivityCounterPlugin plugin;

	/**
	 * Wrapper class for the JComboBox to hold the preset and its display name.
	 */
	private static class PresetNode {
		final ConfigPreset preset;

		PresetNode(ConfigPreset preset) {
			this.preset = preset;
		}

		@Override
		public String toString() {
			return preset.getName();
		}
	}

	public PresetControlPanel(PresetManager presetManager, ActivityCounterPanel parentPanel, ActivityCounterPlugin plugin)	{
		this.presetManager = presetManager;
		this.parentPanel = parentPanel;
		this.plugin = plugin;

		setLayout(new BorderLayout(0, 5));
		setBorder(new EmptyBorder(5, 0, 0, 0));

		setupActionListeners();

		JPanel presetActionButtons = new JPanel();
		presetActionButtons.setLayout(new BoxLayout(presetActionButtons, BoxLayout.X_AXIS));
		presetActionButtons.add(savePresetButton);
		presetActionButtons.add(Box.createRigidArea(new Dimension(5, 0)));
		presetActionButtons.add(loadPresetButton);
		presetActionButtons.add(Box.createRigidArea(new Dimension(5, 0)));
		presetActionButtons.add(deletePresetButton);

		add(presetComboBox, BorderLayout.NORTH);
		add(presetActionButtons, BorderLayout.CENTER);

		// Populate the dropdown list immediately upon UI creation
		reloadComboBox();
	}

	private void setupActionListeners()
	{
		savePresetButton.setFocusable(false);
		savePresetButton.addActionListener(e -> {
			String name = JOptionPane.showInputDialog(
				this,
				"Enter a name for the new preset:"
			);
			if (name != null && !name.trim().isEmpty()) {
				String trimmedName = name.trim();

				// Check if the name already exists in the combo box
				boolean exists = false;
				for (int i = 0; i < presetComboBox.getItemCount(); i++) {
					if (presetComboBox.getItemAt(i).preset.getName().equalsIgnoreCase(trimmedName)) {
						exists = true;
						break;
					}
				}

				if (exists) {
					int confirm = JOptionPane.showConfirmDialog(
						this,
						"A preset named '" + trimmedName + "' already exists.\nDo you want to overwrite it?",
						"Overwrite Preset",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE
					);
					if (confirm != JOptionPane.YES_OPTION) {
						return; // Abort if user doesn't want to overwrite
					}
				}

				presetManager.saveCurrentAsPreset(trimmedName);

				Session activeSession = plugin.getCurrentSession();
				if (activeSession != null) {
					activeSession.setAttachedPresetName(trimmedName);
					plugin.saveSession(activeSession);
				}

				reloadComboBox();

				// Automatically select the newly saved preset so it is immediately active
				SwingUtilities.invokeLater(() -> {
					for (int i = 0; i < presetComboBox.getItemCount(); i++) {
						if (presetComboBox.getItemAt(i).preset.getName().equalsIgnoreCase(trimmedName)) {
							presetComboBox.setSelectedIndex(i);
							break;
						}
					}
				});
			}
		});

		loadPresetButton.setFocusable(false);
		loadPresetButton.addActionListener(e -> {
			PresetNode selected = (PresetNode) presetComboBox.getSelectedItem();
			if (selected != null) {
				int confirm = JOptionPane.showConfirmDialog(
					this,
					"Load preset '" + selected.preset.getName() + "'?\nThis will overwrite your current configuration.",
					"Load Preset",
					JOptionPane.YES_NO_OPTION
				);
				if (confirm == JOptionPane.YES_OPTION) {
					presetManager.applyPreset(selected.preset);

					Session activeSession = plugin.getCurrentSession();
					if (activeSession != null) {
						activeSession.setAttachedPresetName(selected.preset.getName());
						plugin.saveSession(activeSession);
					}

					parentPanel.refreshKcContainer();
				}
			}
		});

		deletePresetButton.setFocusable(false);
		deletePresetButton.addActionListener(e -> {
			PresetNode selected = (PresetNode) presetComboBox.getSelectedItem();
			if (selected != null) {
				int confirm = JOptionPane.showConfirmDialog(
					this,
					"Are you sure you want to delete preset '" + selected.preset.getName() + "'?",
					"Delete Preset",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE
				);
				if (confirm == JOptionPane.YES_OPTION) {
					presetManager.deletePreset(selected.preset);
					reloadComboBox();
				}
			}
		});

		presetComboBox.addActionListener(e -> {
			PresetNode selected = (PresetNode) presetComboBox.getSelectedItem();
			boolean isDefault = selected != null && PresetManager.DEFAULT_PRESET_NAME.equals(selected.preset.getName());

			// Disable the buttons if the default preset is currently selected
			savePresetButton.setEnabled(!isDefault);
			loadPresetButton.setEnabled(!isDefault);
			deletePresetButton.setEnabled(!isDefault);
		});
	}

	public void reloadComboBox() {
		SwingUtilities.invokeLater(() -> {
			presetComboBox.removeAllItems();
			List<ConfigPreset> presets = presetManager.getPresets();
			for (ConfigPreset preset : presets) {
				presetComboBox.addItem(new PresetNode(preset));
			}

			// Determine the active preset to select by default
			String targetPreset = PresetManager.DEFAULT_PRESET_NAME;
			Session activeSession = plugin.getCurrentSession();

			if (activeSession != null && activeSession.getAttachedPresetName() != null) {
				targetPreset = activeSession.getAttachedPresetName();
			} else if (plugin.getConfig().lastPresetUsed() != null && !plugin.getConfig().lastPresetUsed().isEmpty()) {
				targetPreset = plugin.getConfig().lastPresetUsed();
			}

			// Force the combobox to highlight the currently targeted preset
			for (int i = 0; i < presetComboBox.getItemCount(); i++) {
				if (presetComboBox.getItemAt(i).preset.getName().equalsIgnoreCase(targetPreset)) {
					presetComboBox.setSelectedIndex(i);
					break;
				}
			}

			PresetNode selected = (PresetNode) presetComboBox.getSelectedItem();
			boolean isDefault = selected != null && PresetManager.DEFAULT_PRESET_NAME.equals(selected.preset.getName());
			boolean hasPresets = presetComboBox.getItemCount() > 0;

			savePresetButton.setEnabled(!isDefault);
			loadPresetButton.setEnabled(!isDefault && hasPresets);
			deletePresetButton.setEnabled(!isDefault && hasPresets);

		});
	}
}