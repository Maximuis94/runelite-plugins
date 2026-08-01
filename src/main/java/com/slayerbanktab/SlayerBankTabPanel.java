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

package com.slayerbanktab;

import com.slayerbanktab.events.NewSlayerTask;
import com.slayerbanktab.models.SlayerMaster;
import com.slayerbanktab.models.SlayerSetup;
import com.slayerbanktab.models.Task;
import com.slayerbanktab.services.ClipboardManager;
import com.slayerbanktab.services.DBTableScraper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.runelite.api.Client;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public class SlayerBankTabPanel extends PluginPanel
{
	private final SlayerBankTabPlugin plugin;
	private final SlayerBankTabConfig config;
	private final ClipboardManager clipboard;
	private final DBTableScraper dbTableScraper;

	private final JComboBox<AccountOption> accountCombo = new JComboBox<>();
	private final JComboBox<MasterOption> masterCombo = new JComboBox<>();
	private final JComboBox<TaskOption> taskCombo = new JComboBox<>();
	private final JComboBox<AreaOption> locationCombo = new JComboBox<>();
	private final JLabel statusLabel = new JLabel(" ");

	private final JPanel projectionContainer = new JPanel();

	private final Map<Integer, String> rawKonarAreasBackup = new LinkedHashMap<>();
	private boolean isUpdatingModels = false;

	private final static AreaOption AREA_NA = new AreaOption(0, "NA");
	private final static AreaOption WILDERNESS_NA = new AreaOption(0, "Wilderness");

	private long lastActiveLoginHash = -1;

	// --- OPTION WRAPPERS ---

	@Getter
	@RequiredArgsConstructor
	public static class AccountOption {
		private final long hash;
		private final String displayName;

		@Override
		public String toString() {
			return displayName;
		}
	}

	@Value
	public static class MasterOption {
		SlayerMaster master;
		String name;
		@Override public String toString() { return name + " (" + master.getId() + ")"; }
	}

	@Value
	public static class TaskOption {
		Task task;
		@Override public String toString() {
			int id = task.isBoss() ? task.getSlayerTargetBossId() : task.getSlayerTargetId();
			return task.getName() + " (" + id + ")";
		}

		public boolean isBossTask() {return task.isBoss(); }
	}

	@Value
	public static class AreaOption {
		int id;
		String name;
		@Override public String toString() { return name + " (" + id + ")"; }
	}

	public SlayerBankTabPanel(SlayerBankTabPlugin plugin, SlayerBankTabConfig config, DBTableScraper dbTableScraper)
	{
		super(false);
		this.plugin = plugin;
		this.config = config;
		this.clipboard = plugin.getClipboardManager();
		this.dbTableScraper = dbTableScraper;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.setBorder(new EmptyBorder(10, 10, 10, 10));
		container.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel titleLabel = new JLabel("Slayer tab");
		titleLabel.setFont(FontManager.getRunescapeBoldFont());
		titleLabel.setForeground(Color.WHITE);
		container.add(titleLabel, BorderLayout.WEST);

		container.add(createComboSection("Account", accountCombo));
		container.add(createComboSection("Slayer Master", masterCombo));
		container.add(createComboSection("Assigned Task Target", taskCombo));
		container.add(createComboSection("Area", locationCombo));

		taskCombo.setRenderer(new SetupDefinedListCellRenderer(true));
		locationCombo.setRenderer(new SetupDefinedListCellRenderer(false));

		accountCombo.addActionListener(e -> { taskCombo.repaint(); locationCombo.repaint(); updateSetupProjection(); });
		masterCombo.addActionListener(e -> updateTaskAndLocationDropdowns());
		taskCombo.addActionListener(e -> { updateTaskAndLocationDropdowns(); locationCombo.repaint(); });
		locationCombo.addActionListener(e -> { taskCombo.repaint(); updateSetupProjection(); });

		// --- BUTTON PANEL ---
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
		buttonPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// Group 0: Live State Anchor & Global Actions
		JButton loadActiveBtn = new JButton("Load active task");
		loadActiveBtn.setFont(FontManager.getRunescapeFont());
		loadActiveBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		loadActiveBtn.setFocusPainted(false);
		loadActiveBtn.setToolTipText("Instantly snaps the exporter options to your live game state");
		loadActiveBtn.addActionListener(e -> loadCurrentlyActiveTask());

		JButton deleteLayoutBtn = new JButton("Delete selected layout");
		deleteLayoutBtn.setFont(FontManager.getRunescapeFont());
		deleteLayoutBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		deleteLayoutBtn.setFocusPainted(false);
		deleteLayoutBtn.setToolTipText("Clears the layout for the currently selected dropdown combination");
		deleteLayoutBtn.addActionListener(e -> deleteSelectedLayout());

		alignAndAddButton(buttonPanel, loadActiveBtn);
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 4)));
		alignAndAddButton(buttonPanel, deleteLayoutBtn);
		buttonPanel.add(Box.createRigidArea(new Dimension(0, 16)));

		// Group 1: Manage Setup Keys
		JButton copyKeyButton = new JButton("Copy");
		copyKeyButton.setFont(FontManager.getRunescapeFont());
		copyKeyButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		copyKeyButton.setFocusPainted(false);
		copyKeyButton.setToolTipText("Copy Key to Clipboard");
		copyKeyButton.addActionListener(e -> exportKeyToClipboard());

		JButton insertKeyButton = new JButton("Paste");
		insertKeyButton.setFont(FontManager.getRunescapeFont());
		insertKeyButton.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		insertKeyButton.setFocusPainted(false);
		insertKeyButton.setToolTipText("Insert key from clipboard and update the comboboxes accordingly");
		insertKeyButton.addActionListener(e -> importKeyFromClipboard());

		buttonPanel.add(createDualButtonPanel("Slayer task key", copyKeyButton, insertKeyButton));

		// Group 2: Manage Single Layouts
		JButton exportLayoutBtn = new JButton("Export");
		exportLayoutBtn.setFont(FontManager.getRunescapeFont());
		exportLayoutBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		exportLayoutBtn.setFocusPainted(false);
		exportLayoutBtn.setToolTipText("Export a single layout to Clipboard");
		exportLayoutBtn.addActionListener(e -> exportLayoutToClipboard());

		JButton importLayoutBtn = new JButton("Import");
		importLayoutBtn.setFont(FontManager.getRunescapeFont());
		importLayoutBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		importLayoutBtn.setFocusPainted(false);
		importLayoutBtn.setToolTipText("Import a single layout from Clipboard");
		importLayoutBtn.addActionListener(e -> importLayoutFromClipboard());

		buttonPanel.add(createDualButtonPanel("Single Layout", importLayoutBtn, exportLayoutBtn));

		// Group 3: Manage Multiple Setups
		JButton exportManyBtn = new JButton("Export");
		exportManyBtn.setFont(FontManager.getRunescapeFont());
		exportManyBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		exportManyBtn.setFocusPainted(false);
		exportManyBtn.setToolTipText("Export multiple layouts to the clipboard");
		exportManyBtn.addActionListener(e -> exportManySetups());

		JButton importManyBtn = new JButton("Import");
		importManyBtn.setFont(FontManager.getRunescapeFont());
		importManyBtn.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		importManyBtn.setFocusPainted(false);
		importManyBtn.setToolTipText("Import multiple layouts from the clipboard");
		importManyBtn.addActionListener(e -> importManySetups());

		buttonPanel.add(createDualButtonPanel("Multiple layouts", importManyBtn, exportManyBtn));

		container.add(buttonPanel);

		statusLabel.setFont(FontManager.getRunescapeSmallFont());
		statusLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		statusLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, statusLabel.getPreferredSize().height));
		container.add(statusLabel);

		add(container, BorderLayout.NORTH);

		projectionContainer.setLayout(new BorderLayout());
		projectionContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		projectionContainer.setBorder(new EmptyBorder(5, 10, 10, 10));
		add(projectionContainer, BorderLayout.CENTER);

		populateMasters();
	}

	private void alignAndAddButton(JPanel panel, JButton button) {
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
		panel.add(button);
	}

	/**
	 * Helper function to create a side-by-side grouped button layout with a shared label header.
	 */
	private JPanel createDualButtonPanel(String title, JButton leftBtn, JButton rightBtn) {
		JPanel groupPanel = new JPanel();
		groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
		groupPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		groupPanel.setBorder(new EmptyBorder(0, 0, 12, 0));

		JLabel titleLabel = new JLabel(title);
		titleLabel.setFont(FontManager.getRunescapeSmallFont());
		titleLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		titleLabel.setBorder(new EmptyBorder(0, 0, 4, 0));
		groupPanel.add(titleLabel);

		JPanel btnContainer = new JPanel(new GridLayout(1, 2, 8, 0));
		btnContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
		btnContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		btnContainer.add(leftBtn);
		btnContainer.add(rightBtn);

		groupPanel.add(btnContainer);

		return groupPanel;
	}

	public void populateMasters()
	{
		if (plugin.getClientThread() == null) return;

		plugin.getClientThread().invokeLater(() -> {
			Client client = plugin.getClient();
			if (client == null) return;

			List<MasterOption> newMasterOptions = new ArrayList<>();
			if (config.mergeOtherSetups()) {
				newMasterOptions.add(new MasterOption(SlayerMaster.NONE, "None"));
				newMasterOptions.add(new MasterOption(SlayerMaster.TURAEL, SlayerMaster.TURAEL.getDisplayName(client)));
				newMasterOptions.add(new MasterOption(SlayerMaster.KRYSTILIA, SlayerMaster.KRYSTILIA.getDisplayName(client)));
				newMasterOptions.add(new MasterOption(SlayerMaster.KONAR, SlayerMaster.KONAR.getDisplayName(client)));
				newMasterOptions.add(new MasterOption(SlayerMaster.MORTIMER, SlayerMaster.MORTIMER.getDisplayName(client)));
				newMasterOptions.add(new MasterOption(SlayerMaster.MERGED_STANDARD, "Standard Merged"));
			}
			else {
				for (SlayerMaster m : SlayerMaster.values()) {
					if (m != SlayerMaster.UNKNOWN) {
						newMasterOptions.add(new MasterOption(m, m != SlayerMaster.NONE ? m.getDisplayName(client) : "No Task Assigned"));
					}
				}
			}


			SwingUtilities.invokeLater(() -> {
				MasterOption current = (MasterOption) masterCombo.getSelectedItem();
				masterCombo.removeAllItems();

				for (MasterOption opt : newMasterOptions) {
					masterCombo.addItem(opt);
				}

				boolean found = false;
				if (current != null) {
					for (int i = 0; i < masterCombo.getItemCount(); i++) {
						if (masterCombo.getItemAt(i).getMaster() == current.getMaster()) {
							masterCombo.setSelectedIndex(i);
							found = true;
							break;
						}
					}
				}

				if (!found && masterCombo.getItemCount() > 0) {
					masterCombo.setSelectedIndex(0);
				}

				updateTaskAndLocationDropdowns();
			});
		});
	}

	private JPanel createComboSection(String labelText, JComboBox<?> combo)
	{
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(new EmptyBorder(0, 0, 12, 0));
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JLabel label = new JLabel(labelText);
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		label.setBorder(new EmptyBorder(0, 0, 4, 0));

		combo.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		combo.setFocusable(false);

		panel.add(label, BorderLayout.NORTH);
		panel.add(combo, BorderLayout.CENTER);
		return panel;
	}

	public void refreshAccounts(Map<Long, String> accountMap)
	{
		SwingUtilities.invokeLater(() -> {
			Client client = plugin.getClient();
			long loggedInHash = client != null ? client.getAccountHash() : -1;

			boolean isFreshLoginSnap = (loggedInHash != -1 && loggedInHash != lastActiveLoginHash);

			long activeHash = -1;
			if (loggedInHash != -1) {
				activeHash = loggedInHash;
				lastActiveLoginHash = loggedInHash;
			} else if (lastActiveLoginHash != -1) {
				activeHash = lastActiveLoginHash;
			} else if (!accountMap.isEmpty()) {
				activeHash = accountMap.keySet().iterator().next();
			}

			AccountOption currentOpt = (AccountOption) accountCombo.getSelectedItem();
			boolean wasCurrentSelected = currentOpt != null && currentOpt.getDisplayName().equals("Current");
			long previousSelectedHash = currentOpt != null ? currentOpt.getHash() : -1;

			accountCombo.removeAllItems();

			if (activeHash != -1) {
				accountCombo.addItem(new AccountOption(activeHash, "Current"));
			}

			for (Map.Entry<Long, String> entry : accountMap.entrySet()) {
				accountCombo.addItem(new AccountOption(entry.getKey(), entry.getValue()));
			}

			if (isFreshLoginSnap || wasCurrentSelected || currentOpt == null) {
				if (accountCombo.getItemCount() > 0) {
					accountCombo.setSelectedIndex(0);
				}
			} else {
				boolean found = false;
				for (int i = 1; i < accountCombo.getItemCount(); i++) {
					if (accountCombo.getItemAt(i).getHash() == previousSelectedHash) {
						accountCombo.setSelectedIndex(i);
						found = true;
						break;
					}
				}

				if (!found && accountCombo.getItemCount() > 0) {
					accountCombo.setSelectedIndex(0);
				}
			}
		});
	}

	public void populateLocations(Map<Integer, String> scrapedAreas)
	{
		SwingUtilities.invokeLater(() -> {
			rawKonarAreasBackup.clear();
			rawKonarAreasBackup.putAll(scrapedAreas);
			updateTaskAndLocationDropdowns();
		});
	}

	private void updateTaskAndLocationDropdowns()
	{
		if (isUpdatingModels) return;
		isUpdatingModels = true;

		try
		{
			MasterOption masterOpt = (MasterOption) masterCombo.getSelectedItem();
			if (masterOpt == null) return;
			SlayerMaster master = masterOpt.getMaster();

			boolean isTuraelMerged = (master == SlayerMaster.TURAEL) && config.mergeTuraelTasks();
			boolean isNone = (master == SlayerMaster.NONE);

			taskCombo.setEnabled(!isTuraelMerged && !isNone);

			TaskOption currentTaskOpt = (TaskOption) taskCombo.getSelectedItem();
			Task currentTask = currentTaskOpt != null ? currentTaskOpt.getTask() : null;

			taskCombo.removeAllItems();
			if (!isTuraelMerged && !isNone) {
				List<Task> assignable = new ArrayList<>(master.getAssignableTasks());
				assignable.sort((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName()));

				boolean reselectedTask = false;
				for (Task t : assignable) {
					TaskOption opt = new TaskOption(t);
					taskCombo.addItem(opt);
					if (currentTask != null && t == currentTask) {
						taskCombo.setSelectedItem(opt);
						reselectedTask = true;
					}
				}

				if (!reselectedTask && taskCombo.getItemCount() > 0) {
					taskCombo.setSelectedIndex(0);
				}
			}

			TaskOption activeTaskOpt = (TaskOption) taskCombo.getSelectedItem();
			Task activeTask = activeTaskOpt != null ? activeTaskOpt.getTask() : null;
			boolean isBossTask = activeTask != null && activeTask.isBoss();

			boolean isKonar = (master == SlayerMaster.KONAR);
			locationCombo.setEnabled(isKonar && activeTask != null && !isBossTask);

			AreaOption currentLocationOpt = (AreaOption) locationCombo.getSelectedItem();
			int currentLocId = currentLocationOpt != null ? currentLocationOpt.getId() : -1;

			locationCombo.removeAllItems();

			if (!isKonar)
			{
				locationCombo.addItem(master == SlayerMaster.KRYSTILIA ? WILDERNESS_NA : AREA_NA);
				locationCombo.setSelectedIndex(0);
			}

			else if (!isBossTask)
			{
				boolean reselectedLoc = false;
				for (Map.Entry<Integer, String> entry : rawKonarAreasBackup.entrySet()) {
					if (entry.getKey() == 0) continue;
					if (activeTask == null || activeTask.isAllowedKonarArea(entry.getValue())) {
						AreaOption opt = new AreaOption(entry.getKey(), entry.getValue());
						locationCombo.addItem(opt);
						if (currentLocId == entry.getKey()) {
							locationCombo.setSelectedItem(opt);
							reselectedLoc = true;
						}
					}
				}

				if (!reselectedLoc && locationCombo.getItemCount() > 1) {
					locationCombo.setSelectedIndex(0);
				}

				if (locationCombo.getItemCount() == 1)
				{
					locationCombo.setSelectedIndex(0);
					locationCombo.setEnabled(false);
				}
			}

			else
			{
				locationCombo.addItem(AREA_NA);
				for (Map.Entry<Integer, String> entry : rawKonarAreasBackup.entrySet()) {
					locationCombo.addItem(new AreaOption(entry.getKey(), entry.getValue()));
				}

				boolean reselectedLoc = false;
				if (currentLocId >= 0) {
					for (int i = 0; i < locationCombo.getItemCount(); i++) {
						if (locationCombo.getItemAt(i).getId() == currentLocId) {
							locationCombo.setSelectedIndex(i);
							reselectedLoc = true;
							break;
						}
					}
				}

				if (isBossTask ||!reselectedLoc && locationCombo.getItemCount() > 0) {
					locationCombo.setSelectedIndex(0);
				}
			}

		}
		finally
		{
			isUpdatingModels = false;
			updateSetupProjection();
		}
	}

	// --- SETUP PROJECTIONS ---

	private String getSelectedKey()
	{
		AccountOption accOpt = (AccountOption) accountCombo.getSelectedItem();
		MasterOption masterOpt = (MasterOption) masterCombo.getSelectedItem();
		TaskOption taskOpt = (TaskOption) taskCombo.getSelectedItem();
		AreaOption areaOpt = (AreaOption) locationCombo.getSelectedItem();

		if (accOpt == null || masterOpt == null) return null;

		long hash = accOpt.getHash();
		SlayerMaster master = masterOpt.getMaster();
		boolean isTuraelMerged = (master == SlayerMaster.TURAEL) && config.mergeTuraelTasks();
		boolean isNone = (master == SlayerMaster.NONE);
		boolean isBoss = taskOpt != null && taskOpt.isBossTask();

		if (!isTuraelMerged && !isNone && taskOpt == null) return null;

		Task task = (isTuraelMerged || isNone) ? null : taskOpt.getTask();
		int areaId = areaOpt != null && !isBoss ? areaOpt.getId() : 0;

		return plugin.compileSpecificKey(hash, master, task, areaId);
	}

	private void updateSetupProjection()
	{
		projectionContainer.removeAll();

		String targetKey = getSelectedKey();
		if (targetKey == null) {
			projectionContainer.revalidate();
			projectionContainer.repaint();
			return;
		}

		SlayerSetup setup = plugin.getSetupManager().getSetupForTask(targetKey);

		if (setup == null || setup.getGridLayout() == null || setup.getGridLayout().length == 0) {
			renderEmptyState("No setup defined for this selection.");
			return;
		}

		int[] grid = setup.getGridLayout();
		boolean hasAnyItem = false;
		for (int id : grid) {
			if (id > 0) { hasAnyItem = true; break; }
		}

		if (!hasAnyItem) {
			renderEmptyState("Setup defined, but contains no items.");
			return;
		}

		JPanel canvas = createLayoutGridPanel(grid);

		JScrollPane scrollPane = new JScrollPane(canvas, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
		scrollPane.setBorder(null);

		projectionContainer.add(scrollPane, BorderLayout.CENTER);
		projectionContainer.revalidate();
		projectionContainer.repaint();
	}

	private void renderEmptyState(String message)
	{
		JLabel noneLabel = new JLabel(message);
		noneLabel.setFont(FontManager.getRunescapeSmallFont());
		noneLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		noneLabel.setHorizontalAlignment(SwingConstants.CENTER);
		noneLabel.setBorder(new EmptyBorder(20, 0, 0, 0));
		projectionContainer.add(noneLabel, BorderLayout.NORTH);
		projectionContainer.revalidate();
		projectionContainer.repaint();
	}

	private JPanel createLayoutGridPanel(int[] grid)
	{
		JPanel canvas = new JPanel();
		canvas.setLayout(new BoxLayout(canvas, BoxLayout.Y_AXIS));
		canvas.setBackground(ColorScheme.DARK_GRAY_COLOR);

		// 1. WORN EQUIPMENT SECTION: Top 7x4
		JLabel equipTitle = new JLabel("Worn Equipment");
		equipTitle.setFont(FontManager.getRunescapeSmallFont());
		equipTitle.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		equipTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
		canvas.add(equipTitle);
		canvas.add(Box.createRigidArea(new Dimension(0, 4)));

		JPanel equipGrid = new JPanel(new GridLayout(7, 4, 2, 2));
		equipGrid.setBackground(ColorScheme.DARK_GRAY_COLOR);

		for (int r = 0; r < 7; r++)
		{
			for (int c = 0; c < 4; c++)
			{
				int bankIdx = (r * 8) + c;
				int itemId = (bankIdx < grid.length) ? grid[bankIdx] : -1;
				equipGrid.add(createItemSlot(itemId, true));
			}
		}

		JPanel equipDamper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		equipDamper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		equipDamper.add(equipGrid);

		canvas.add(equipDamper);
		canvas.add(Box.createRigidArea(new Dimension(0, 15)));

		// 2. INVENTORY SECTION: Middle 7x4
		JLabel invTitle = new JLabel("Inventory");
		invTitle.setFont(FontManager.getRunescapeSmallFont());
		invTitle.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		invTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
		canvas.add(invTitle);
		canvas.add(Box.createRigidArea(new Dimension(0, 4)));

		JPanel invGrid = new JPanel(new GridLayout(7, 4, 2, 2));
		invGrid.setBackground(ColorScheme.DARK_GRAY_COLOR);

		for (int r = 0; r < 7; r++)
		{
			for (int c = 0; c < 4; c++)
			{
				int bankIdx = (r * 8) + (4 + c);
				int itemId = (bankIdx < grid.length) ? grid[bankIdx] : -1;
				invGrid.add(createItemSlot(itemId, true));
			}
		}

		JPanel invDamper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		invDamper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		invDamper.add(invGrid);

		canvas.add(invDamper);
		canvas.add(Box.createRigidArea(new Dimension(0, 15)));

		// 3. ADDITIONAL ITEMS SECTION
		List<Integer> extraItems = new ArrayList<>();
		if (grid.length > 56) {
			for (int i = 56; i < grid.length; i++) {
				if (grid[i] > 0) {
					extraItems.add(grid[i]);
				}
			}
		}

		if (!extraItems.isEmpty()) {
			JLabel extraTitle = new JLabel("Additional Items");
			extraTitle.setFont(FontManager.getRunescapeSmallFont());
			extraTitle.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			extraTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
			canvas.add(extraTitle);
			canvas.add(Box.createRigidArea(new Dimension(0, 4)));

			int cols = 4;
			int rows = (int) Math.ceil(extraItems.size() / (double) cols);
			JPanel extraGrid = new JPanel(new GridLayout(rows, cols, 2, 2));
			extraGrid.setBackground(ColorScheme.DARK_GRAY_COLOR);

			for (int itemId : extraItems) {
				extraGrid.add(createItemSlot(itemId, true));
			}

			int remainder = (rows * cols) - extraItems.size();
			for (int i = 0; i < remainder; i++) {
				extraGrid.add(createItemSlot(-1, false));
			}

			JPanel extraDamper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			extraDamper.setBackground(ColorScheme.DARK_GRAY_COLOR);
			extraDamper.add(extraGrid);

			canvas.add(extraDamper);
			canvas.add(Box.createRigidArea(new Dimension(0, 15)));
		}

		return canvas;
	}

	private JPanel createItemSlot(int itemId, boolean drawBackground)
	{
		JPanel slot = new JPanel(new BorderLayout());

		Dimension square = new Dimension(32, 32);
		slot.setPreferredSize(square);
		slot.setMinimumSize(square);
		slot.setMaximumSize(square);

		if (drawBackground) {
			slot.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			slot.setBorder(new LineBorder(ColorScheme.MEDIUM_GRAY_COLOR, 1));
		} else {
			slot.setBackground(ColorScheme.DARK_GRAY_COLOR);
		}

		if (itemId > 0 && plugin.getItemManager() != null)
		{
			JLabel itemLabel = new JLabel();
			itemLabel.setHorizontalAlignment(SwingConstants.CENTER);
			itemLabel.setVerticalAlignment(SwingConstants.CENTER);

			plugin.getItemManager().getImage(itemId).addTo(itemLabel);

			if (plugin.getClientThread() != null)
			{
				plugin.getClientThread().invokeLater(() -> {
					String name = plugin.getItemManager().getItemComposition(itemId).getName();
					SwingUtilities.invokeLater(() -> itemLabel.setToolTipText(name));
				});
			}

			slot.add(itemLabel, BorderLayout.CENTER);
		}

		return slot;
	}

	// --- UNIVERSAL KEY ANCHOR HELPERS ---

	private String stripAccountHash(String fullKey)
	{
		int idx = fullKey.indexOf('_');
		if (idx == -1) return fullKey;
		return fullKey.substring(idx + 1);
	}

	private String reanchorKey(String keyWithoutHash, long targetHash)
	{
		String clean = keyWithoutHash.startsWith("_") ? keyWithoutHash.substring(1) : keyWithoutHash;
		if (clean.equalsIgnoreCase("notask")) {
			clean = "0_0_0_0_0";
		}
		return targetHash + "_" + clean;
	}

	// --- BATCH EXPORT / IMPORT LOGIC ---

	private String formatKeyToReadable(String key)
	{
		if (key.endsWith(PluginConstants.NO_TASK_KEY_SUFFIX)) {
			return "[No Task Assigned]";
		}

		try {
			String[] parts = key.split("_");
			if (parts.length < 6) return key;

			int masterId = Integer.parseInt(parts[1]);
			int targetId = Integer.parseInt(parts[2]);
			int bossId = Integer.parseInt(parts[3]);
			int areaId = Integer.parseInt(parts[4]);

			SlayerMaster master = (masterId == 99) ? SlayerMaster.MERGED_STANDARD : SlayerMaster.getById(masterId);
			String masterName = (master != null) ? master.getName() : ("Master#" + masterId);

			String taskName;
			if (master == SlayerMaster.TURAEL && targetId == 0 && bossId == 0) {
				taskName = "Merged Turael Tasks";
			} else {
				Task task = Task.getById(targetId, bossId);
				taskName = (task != null) ? task.getName() : ("Target#" + targetId);
			}

			if (master == SlayerMaster.KONAR && areaId > 0) {
				String areaName = rawKonarAreasBackup.getOrDefault(areaId, "Area#" + areaId);
				return String.format("[%s, %s, %s]", masterName, taskName, areaName);
			} else {
				return String.format("[%s, %s]", masterName, taskName);
			}
		} catch (Exception e) {
			return "[" + key + "]";
		}
	}

	private boolean hasValidItems(SlayerSetup setup)
	{
		if (setup == null || setup.getGridLayout() == null) return false;
		for (int id : setup.getGridLayout()) {
			if (id > 0) return true;
		}
		return false;
	}

	private List<String> showBatchSelectionDialog(String title, String subtitle, Map<String, String> keyToReadableMap, Map<String, Boolean> initialCheckStates)
	{
		JPanel panel = new JPanel(new BorderLayout(0, 10));
		panel.setPreferredSize(new Dimension(340, 380));

		JLabel subLabel = new JLabel(subtitle);
		subLabel.setFont(FontManager.getRunescapeFont());
		panel.add(subLabel, BorderLayout.NORTH);

		JPanel listContainer = new JPanel();
		listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
		listContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);

		List<JCheckBox> checkBoxes = new ArrayList<>();

		for (Map.Entry<String, String> entry : keyToReadableMap.entrySet()) {
			boolean isChecked = initialCheckStates.getOrDefault(entry.getKey(), false);
			JCheckBox cb = new JCheckBox(entry.getValue(), isChecked);
			cb.setFont(FontManager.getRunescapeSmallFont());
			cb.setForeground(Color.WHITE);
			cb.setBackground(ColorScheme.DARK_GRAY_COLOR);
			cb.setFocusPainted(false);
			cb.setActionCommand(entry.getKey());

			listContainer.add(cb);
			checkBoxes.add(cb);
		}

		JScrollPane scrollPane = new JScrollPane(listContainer);
		scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
		scrollPane.setBorder(new LineBorder(ColorScheme.DARKER_GRAY_COLOR, 1));
		panel.add(scrollPane, BorderLayout.CENTER);

		JPanel quickSelectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		JButton selectAllBtn = new JButton("Select All");
		JButton deselectAllBtn = new JButton("Deselect All");
		selectAllBtn.setFont(FontManager.getRunescapeSmallFont());
		deselectAllBtn.setFont(FontManager.getRunescapeSmallFont());
		selectAllBtn.setFocusPainted(false);
		deselectAllBtn.setFocusPainted(false);

		selectAllBtn.addActionListener(e -> checkBoxes.forEach(cb -> cb.setSelected(true)));
		deselectAllBtn.addActionListener(e -> checkBoxes.forEach(cb -> cb.setSelected(false)));

		quickSelectPanel.add(selectAllBtn);
		quickSelectPanel.add(deselectAllBtn);
		panel.add(quickSelectPanel, BorderLayout.SOUTH);

		int result = JOptionPane.showConfirmDialog(
			this.getRootPane(),
			panel,
			title,
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.PLAIN_MESSAGE
		);

		if (result == JOptionPane.OK_OPTION) {
			return checkBoxes.stream()
				.filter(JCheckBox::isSelected)
				.map(JCheckBox::getActionCommand)
				.collect(Collectors.toList());
		}
		return null;
	}

	/**
	 * Export 1-N setups to the clipboard. A selection can be made before exporting.
	 */
	private void exportManySetups()
	{
		AccountOption accOpt = (AccountOption) accountCombo.getSelectedItem();
		if (accOpt == null) return;
		long hash = accOpt.getHash();

		String prefix = hash + "_";
		Map<String, SlayerSetup> allSetups = plugin.getSetupManager().getAllSetups();

		Map<String, String> candidateMap = new LinkedHashMap<>();
		Map<String, Boolean> exportCheckStates = new HashMap<>();

		for (Map.Entry<String, SlayerSetup> entry : allSetups.entrySet()) {
			String fullKey = entry.getKey();
			if (fullKey.startsWith(prefix) && hasValidItems(entry.getValue())) {
				candidateMap.put(fullKey, formatKeyToReadable(fullKey));
				exportCheckStates.put(fullKey, false);
			}
		}

		if (candidateMap.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No configured setups found for Profile (" + hash + ").", "Export Many", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		List<String> selectedFullKeys = showBatchSelectionDialog(
			"Export Multiple Setups",
			"Select setups to export for Profile (" + hash + "):",
			candidateMap,
			exportCheckStates
		);

		if (selectedFullKeys == null || selectedFullKeys.isEmpty()) {
			statusLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR); statusLabel.setText("Batch export cancelled.");
			return;
		}

		Map<String, int[]> payload = new HashMap<>();
		for (String k : selectedFullKeys) {
			String stripped = stripAccountHash(k);
			payload.put(stripped, allSetups.get(k).getGridLayout());
		}

		String json = plugin.serializeSetupsToJson(payload);
		clipboard.setText(json);

		statusLabel.setForeground(Color.GREEN); statusLabel.setText("Exported " + selectedFullKeys.size() + " setups!");
	}

	/**
	 * Imports the setup(s) from the clipboard. A selection can be made before actually importing, missing keys are checked by default.
	 */
	private void importManySetups()
	{
		AccountOption accOpt = (AccountOption) accountCombo.getSelectedItem();
		if (accOpt == null) return;
		long targetHash = accOpt.getHash();

		String clipboardData = clipboard.getText();

		if (clipboardData == null || clipboardData.isEmpty())
		{
			statusLabel.setForeground(Color.RED); statusLabel.setText("Clipboard is empty.");
		}

		Map<String, int[]> incomingSetups = plugin.deserializeSetupsFromJson(clipboardData);
		if (incomingSetups == null || incomingSetups.isEmpty()) {
			statusLabel.setForeground(Color.RED); statusLabel.setText("Error: No valid setup batch found in clipboard.");
			return;
		}

		Map<String, String> candidateMap = new LinkedHashMap<>();
		Map<String, Boolean> importCheckStates = new HashMap<>();

		for (String strippedKey : incomingSetups.keySet()) {
			String reanchoredKey = reanchorKey(strippedKey, targetHash);
			candidateMap.put(strippedKey, formatKeyToReadable(reanchoredKey));

			SlayerSetup existingSetup = plugin.getSetupManager().getSetupForTask(reanchoredKey);
			boolean isMissing = !hasValidItems(existingSetup);
			importCheckStates.put(strippedKey, isMissing);
		}

		List<String> confirmedStrippedKeys = showBatchSelectionDialog(
			"Import Multiple Setups",
			"Select setups to save to Profile (" + targetHash + "):",
			candidateMap,
			importCheckStates
		);

		if (confirmedStrippedKeys == null || confirmedStrippedKeys.isEmpty()) {
			statusLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR); statusLabel.setText("Batch import cancelled.");
			return;
		}

		Map<String, int[]> subsetToSave = new HashMap<>();
		for (String origStrippedKey : confirmedStrippedKeys) {
			String newAnchoredKey = reanchorKey(origStrippedKey, targetHash);
			subsetToSave.put(newAnchoredKey, incomingSetups.get(origStrippedKey));
		}

		plugin.saveMultipleImportedSetups(subsetToSave);

		statusLabel.setForeground(Color.GREEN); statusLabel.setText("Imported " + subsetToSave.size() + " setups!");
		taskCombo.repaint();
		locationCombo.repaint();
		updateSetupProjection();
	}

	/**
	 * Exports a specific key to the clipboard.
	 */
	private void exportKeyToClipboard()
	{
		String fullKey = getSelectedKey();
		if (fullKey == null) {
			statusLabel.setForeground(Color.RED);
			statusLabel.setText("Error: Missing selection");
			return;
		}

		clipboard.setText(fullKey);

		statusLabel.setForeground(Color.GREEN);
		String readableKey = formatKeyToReadable(fullKey);
		statusLabel.setText("Copied key for " + readableKey);
	}

	/**
	 * Takes a task key from the clipboard (e.g., "1166583798855853255_7_13_0_0_0"),
	 * ignores the account hash, and updates the Slayer Master, Task Target,
	 * and Location comboboxes accordingly.
	 */
	private void importKeyFromClipboard()
	{
		String rawText = clipboard.getText();
		if (rawText == null || rawText.isEmpty()) {
			statusLabel.setForeground(Color.RED);
			statusLabel.setText("Error: Clipboard is empty.");
			return;
		}

		String cleanKey = rawText.trim();
		if (plugin.getTaskTracker() != null && !plugin.getTaskTracker().isLegalKey(cleanKey)) {
			statusLabel.setForeground(Color.RED);
			statusLabel.setText("Error: Invalid task key in clipboard");
			return;
		}

		String[] parts = cleanKey.split("_");
		if (parts.length < 5) {
			statusLabel.setForeground(Color.RED);
			statusLabel.setText("Error: Invalid task key format");
			return;
		}

		int masterId;
		int targetId;
		int bossId;
		int areaId;
		try {
			masterId = Integer.parseInt(parts[1]);
			targetId = Integer.parseInt(parts[2]);
			bossId = Integer.parseInt(parts[3]);
			areaId = Integer.parseInt(parts[4]);
		} catch (NumberFormatException ex) {
			statusLabel.setForeground(Color.RED);
			statusLabel.setText("Error: Malformed numbers in task key");
			return;
		}

		SlayerMaster targetMaster = (masterId == 99) ? SlayerMaster.MERGED_STANDARD : SlayerMaster.getById(masterId);
		boolean masterFound = false;
		for (int i = 0; i < masterCombo.getItemCount(); i++) {
			if (masterCombo.getItemAt(i).getMaster() == targetMaster) {
				masterCombo.setSelectedIndex(i);
				masterFound = true;
				break;
			}
		}
		if (!masterFound && config.mergeOtherSetups() && SlayerMaster.isMergeableMasterId(masterId)) {
			for (int i = 0; i < masterCombo.getItemCount(); i++) {
				if (masterCombo.getItemAt(i).getMaster() == SlayerMaster.MERGED_STANDARD) {
					masterCombo.setSelectedIndex(i);
					masterFound = true;
					break;
				}
			}
		}
		if (!masterFound) {
			statusLabel.setForeground(Color.RED);
			statusLabel.setText("Error: Slayer Master not available in current mode");
			return;
		}

		Task targetTask = Task.getById(targetId, bossId);
		if (targetTask != null) {
			for (int i = 0; i < taskCombo.getItemCount(); i++) {
				if (taskCombo.getItemAt(i).getTask() == targetTask) {
					taskCombo.setSelectedIndex(i);
					break;
				}
			}
		}

		for (int i = 0; i < locationCombo.getItemCount(); i++) {
			if (locationCombo.getItemAt(i).getId() == areaId) {
				locationCombo.setSelectedIndex(i);
				break;
			}
		}

		updateSetupProjection();

		statusLabel.setForeground(Color.GREEN);
		statusLabel.setText("Loaded selection from clipboard key!");
	}

	private void exportLayoutToClipboard()
	{
		String key = getSelectedKey();
		if (key == null) {
			statusLabel.setForeground(Color.RED);
			statusLabel.setText("Error: Missing selection");
			return;
		}

		SlayerSetup setup = plugin.getSetupManager().getSetupForTask(key);
		if (setup == null || setup.getGridLayout() == null) {
			statusLabel.setForeground(Color.RED);
			statusLabel.setText("Error: No layout exists to export");
			return;
		}

		String csv = Arrays.stream(setup.getGridLayout())
			.mapToObj(String::valueOf)
			.collect(Collectors.joining(","));

		clipboard.setText(csv);

		statusLabel.setForeground(Color.GREEN);
		statusLabel.setText("Layout array copied!");
	}

	private void importLayoutFromClipboard()
	{
		String targetKey = getSelectedKey();
		if (targetKey == null) {
			statusLabel.setForeground(Color.RED);
			statusLabel.setText("Error: Missing target selection");
			return;
		}

		int[] layout;
		String data = clipboard.getText();
		if (data == null || data.isEmpty())
		{
			statusLabel.setForeground(Color.RED);
			statusLabel.setText("Error: No data found");
			return;
		}

		try {
			String[] parts = data.split(",");
			layout = new int[parts.length];
			for (int i = 0; i < parts.length; i++) {
				layout[i] = Integer.parseInt(parts[i].trim());
			}
		} catch (Exception ex) {
			statusLabel.setForeground(Color.RED);
			statusLabel.setText("Error: Invalid layout array in clipboard");
			return;
		}

		JPanel previewCanvas = createLayoutGridPanel(layout);
		JScrollPane scrollPane = new JScrollPane(previewCanvas, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(240, 350));
		scrollPane.setBorder(new LineBorder(ColorScheme.DARKER_GRAY_COLOR, 1));

		JPanel dialogPanel = new JPanel(new BorderLayout(0, 10));
		JLabel confirmLabel = new JLabel("Save this layout to the current selection?");
		confirmLabel.setFont(FontManager.getRunescapeFont());
		dialogPanel.add(confirmLabel, BorderLayout.NORTH);
		dialogPanel.add(scrollPane, BorderLayout.CENTER);

		int result = JOptionPane.showConfirmDialog(
			this.getRootPane(),
			dialogPanel,
			"Confirm Layout Import",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.PLAIN_MESSAGE
		);

		if (result == JOptionPane.YES_OPTION) {
			plugin.saveImportedLayout(targetKey, layout);

			statusLabel.setForeground(Color.GREEN);
			statusLabel.setText("Layout imported successfully!");
			taskCombo.repaint();
			locationCombo.repaint();
			updateSetupProjection();
		} else {
			statusLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			statusLabel.setText("Import cancelled.");
		}
	}

	private boolean checkSetupExists(Task overrideTask, Integer overrideAreaId)
	{
		AccountOption accOpt = (AccountOption) accountCombo.getSelectedItem();
		MasterOption masterOpt = (MasterOption) masterCombo.getSelectedItem();
		if (accOpt == null || masterOpt == null) return false;

		long hash = accOpt.getHash();
		SlayerMaster master = masterOpt.getMaster();
		boolean isTuraelMerged = (master == SlayerMaster.TURAEL) && config.mergeTuraelTasks();
		boolean isNone = (master == SlayerMaster.NONE);

		Task task = (isTuraelMerged || isNone) ? null : (overrideTask != null ? overrideTask : (taskCombo.getSelectedItem() != null ? ((TaskOption) taskCombo.getSelectedItem()).getTask() : null));
		int areaId = overrideAreaId != null ? overrideAreaId : (locationCombo.getSelectedItem() != null ? ((AreaOption) locationCombo.getSelectedItem()).getId() : 0);

		String targetKey = plugin.compileSpecificKey(hash, master, task, areaId);
		SlayerSetup setup = plugin.getSetupManager().getSetupForTask(targetKey);

		if (setup != null && setup.getGridLayout() != null) {
			for (int itemId : setup.getGridLayout()) {
				if (itemId > 0) return true;
			}
		}
		return false;
	}

	public Map<Integer, String> getAreaMap()
	{
		Map<Integer, String> map = new HashMap<>();
		for (int i = 0; i < locationCombo.getItemCount(); i++) {
			AreaOption opt = locationCombo.getItemAt(i);
			map.put(opt.getId(), opt.getName());
		}
		return map;
	}

	private class SetupDefinedListCellRenderer extends DefaultListCellRenderer {
		private final boolean isTaskCombo;

		public SetupDefinedListCellRenderer(boolean isTaskCombo) {
			this.isTaskCombo = isTaskCombo;
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value == null) return c;

			boolean exists;
			if (isTaskCombo) {
				exists = checkSetupExists(((TaskOption) value).getTask(), null);
			} else {
				exists = checkSetupExists(null, ((AreaOption) value).getId());
			}

			if (!exists) {
				c.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
			} else {
				c.setForeground(isSelected ? list.getSelectionForeground() : ColorScheme.LIGHT_GRAY_COLOR);
			}
			return c;
		}
	}

	private void loadCurrentlyActiveTask() {
		if (plugin.getTaskTracker() == null || !plugin.getTaskTracker().hasActiveTask()) {
			statusLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			statusLabel.setText("No active Slayer task detected.");
			return;
		}

		NewSlayerTask activeTask = plugin.getTaskTracker().getCurrentTask();
		if (activeTask == null) {
			statusLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			statusLabel.setText("Active task details not yet cached.");
			return;
		}

		Client client = plugin.getClient();
		long loggedInHash = client != null ? client.getAccountHash() : -1;

		if (loggedInHash != -1) {
			for (int i = 0; i < accountCombo.getItemCount(); i++) {
				if (accountCombo.getItemAt(i).getHash() == loggedInHash) {
					accountCombo.setSelectedIndex(i);
					break;
				}
			}
		}

		SlayerMaster targetMaster = SlayerMaster.getById(activeTask.getMasterId());
		for (int i = 0; i < masterCombo.getItemCount(); i++) {
			if (masterCombo.getItemAt(i).getMaster() == targetMaster) {
				masterCombo.setSelectedIndex(i);
				break;
			}
		}

		Task targetTask = Task.getById(activeTask.getTaskId(), activeTask.getBossId());
		if (targetTask != null) {
			for (int i = 0; i < taskCombo.getItemCount(); i++) {
				if (taskCombo.getItemAt(i).getTask() == targetTask) {
					taskCombo.setSelectedIndex(i);
					break;
				}
			}
		}

		int targetAreaId = activeTask.getAreaId();
		for (int i = 0; i < locationCombo.getItemCount(); i++) {
			if (locationCombo.getItemAt(i).getId() == targetAreaId) {
				locationCombo.setSelectedIndex(i);
				break;
			}
		}

		updateSetupProjection();

		statusLabel.setForeground(Color.GREEN);
		statusLabel.setText("Loaded active task: " + activeTask.getTaskName());
	}

	/**
	 * Safely refreshes the setup projection grid from external plugin threads.
	 */
	public void refreshSetupProjection() {
		SwingUtilities.invokeLater(this::updateSetupProjection);
	}

	private void deleteSelectedLayout() {
		String targetKey = getSelectedKey();
		if (targetKey == null) {
			statusLabel.setForeground(Color.RED);
			statusLabel.setText("Error: Missing selection");
			return;
		}

		SlayerSetup existing = plugin.getSetupManager().getSetupForTask(targetKey);
		if (!hasValidItems(existing)) {
			statusLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			statusLabel.setText("Layout is already empty.");
			return;
		}

		JPanel dialogPanel = new JPanel(new BorderLayout(0, 10));
		JLabel confirmLabel = new JLabel("<html>Are you sure you want to reset and delete the layout for:<br><br><b>" + formatKeyToReadable(targetKey) + "</b>?</html>");
		confirmLabel.setFont(FontManager.getRunescapeFont());
		dialogPanel.add(confirmLabel, BorderLayout.CENTER);

		int result = JOptionPane.showConfirmDialog(
			this.getRootPane(),
			dialogPanel,
			"Confirm Layout Deletion",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE
		);

		if (result == JOptionPane.YES_OPTION) {
			plugin.deleteSetupLayout(targetKey);

			statusLabel.setForeground(Color.GREEN);
			statusLabel.setText("Layout deleted successfully!");

			taskCombo.repaint();
			locationCombo.repaint();
			updateSetupProjection();
		} else {
			statusLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			statusLabel.setText("Deletion cancelled.");
		}
	}
}