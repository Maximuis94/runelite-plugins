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
import com.activitycounter.models.Session;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;

public class SessionControlPanel extends JPanel
{
	private final ActivityCounterPlugin plugin;
	private final ActivityCounterPanel parentPanel;

	private final JButton toggleSessionButton = new JButton();
	private final JButton renameSessionButton = new JButton("Rename");
	private final JButton deleteSessionButton = new JButton("Delete");
	private final JComboBox<SessionNode> sessionComboBox = new JComboBox<>();

	private volatile boolean forceNextReloadToActive = false;
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm")
		.withZone(ZoneId.systemDefault());

	/**
	 * Wrapper class for the JComboBox to hold the session and its display label[cite: 4].
	 */
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

	public SessionControlPanel(ActivityCounterPlugin plugin, ActivityCounterPanel parentPanel)
	{
		this.plugin = plugin;
		this.parentPanel = parentPanel;

		// --- INITIAL DEFAULT STATES ---
		toggleSessionButton.setText("Start Session");
		toggleSessionButton.setEnabled(false);
		sessionComboBox.addItem(new SessionNode(null, " "));

		setLayout(new BorderLayout(0, 5));
		setBorder(new EmptyBorder(5, 0, 0, 0));

		setupActionListeners();

		JPanel sessionActionButtons = new JPanel();
		sessionActionButtons.setLayout(new BoxLayout(sessionActionButtons, BoxLayout.X_AXIS));
		sessionActionButtons.add(renameSessionButton);
		sessionActionButtons.add(Box.createRigidArea(new Dimension(5, 0)));
		sessionActionButtons.add(deleteSessionButton);

		// Group the combobox and action buttons tightly
		JPanel comboWrapper = new JPanel(new BorderLayout(0, 5));
		comboWrapper.add(sessionComboBox, BorderLayout.NORTH);
		comboWrapper.add(sessionActionButtons, BorderLayout.CENTER);

		// Group the toggle button and the comboWrapper together
		JPanel topStack = new JPanel(new BorderLayout(0, 5));
		topStack.add(toggleSessionButton, BorderLayout.NORTH);
		topStack.add(comboWrapper, BorderLayout.CENTER);

		// Add the entire stack to the NORTH of the main panel so it never stretches
		add(topStack, BorderLayout.NORTH);
	}

	private void setupActionListeners()
	{
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
					if (confirm == JOptionPane.YES_OPTION) plugin.closeSession();
				} else {
					plugin.closeSession();
				}
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
				JTextField nameInput = new JTextField(selectedNode.label);
				int confirm = JOptionPane.showConfirmDialog(
					this,
					nameInput,
					"Rename Session",
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE
				);
				if (confirm == JOptionPane.OK_OPTION) {
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

		sessionComboBox.addActionListener(e -> parentPanel.refreshKcContainer());
	}

	/**
	 * Allows the main panel to fetch the currently selected session to display its data.
	 * Returns null if no session is selected. Returns the active session if the "Active session" node is selected.
	 */
	public Session getSelectedSession()
	{
		SessionNode selectedNode = (SessionNode) sessionComboBox.getSelectedItem();
		if (selectedNode == null)
		{
			return null;
		}

		if (selectedNode.session == null)
		{
			return plugin.getCurrentSession();
		}

		return selectedNode.session;
	}

	/**
	 * Exposes the flag so the plugin can force the combo box back to the active session when a session is deleted[cite: 4].
	 */
	public void forceActiveSessionSelection()
	{
		forceNextReloadToActive = true;
	}

	/**
	 * Updates the start/stop button colors and rename/delete visibility based on client state[cite: 4].
	 * This replaces the top half of your old ActivityCounterPanel.refreshKcContainer() method.
	 */
	public void updateUIState(boolean isLoggedIn, Session activeSession)
	{
		toggleSessionButton.setEnabled(isLoggedIn);

		if (activeSession != null && activeSession.isInProgress()) {
			toggleSessionButton.setText("Stop Session");
			toggleSessionButton.setBackground(isLoggedIn ? ColorScheme.PROGRESS_ERROR_COLOR : ColorScheme.DARK_GRAY_COLOR);
		} else {
			toggleSessionButton.setText("Start Session");
			toggleSessionButton.setBackground(isLoggedIn ? ColorScheme.PROGRESS_COMPLETE_COLOR : ColorScheme.DARK_GRAY_COLOR);
		}

		SessionNode selectedNode = (SessionNode) sessionComboBox.getSelectedItem();
		if (selectedNode != null && selectedNode.session != null) {
			renameSessionButton.setVisible(true);
			deleteSessionButton.setVisible(true);
		} else {
			renameSessionButton.setVisible(false);
			deleteSessionButton.setVisible(false);
		}
	}

	/**
	 * Rebuilds the combobox list using the plugin's archived sessions[cite: 4].
	 */
	public void reloadComboBox()
	{
		SwingUtilities.invokeLater(() -> {
			String selectedId = null;

			if (!forceNextReloadToActive) {
				SessionNode selectedNode = (SessionNode) sessionComboBox.getSelectedItem();
				if (selectedNode != null && selectedNode.session != null) {
					selectedId = selectedNode.session.getId();
				}
			}

			forceNextReloadToActive = false;

			// Temporarily remove listener to avoid triggering multiple redraws during clearing/adding
			ActionListener[] listeners = sessionComboBox.getActionListeners();
			for (ActionListener listener : listeners) {
				sessionComboBox.removeActionListener(listener);
			}

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
			} else if (sessionComboBox.getItemCount() == 0) {
				// Safety net: if no sessions exist, put the placeholder back
				sessionComboBox.addItem(new SessionNode(null, " "));
			}

			for (ActionListener listener : listeners) {
				sessionComboBox.addActionListener(listener);
			}
			parentPanel.refreshKcContainer();
		});
	}
}
