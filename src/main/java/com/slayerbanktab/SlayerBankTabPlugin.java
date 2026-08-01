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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import static com.slayerbanktab.PluginConstants.ACCOUNT_HASHES_CONFIG_KEY;
import static com.slayerbanktab.PluginConstants.BOSS_TASK_ID;
import static com.slayerbanktab.PluginConstants.CONFIG_GROUP;
import static com.slayerbanktab.PluginConstants.NO_TASK_KEY_SUFFIX;
import static com.slayerbanktab.PluginConstants.PLUGIN_NAME;
import com.slayerbanktab.events.NewSlayerTask;
import com.slayerbanktab.models.ExtendedEquipmentSlot;
import com.slayerbanktab.models.ItemMapping;
import com.slayerbanktab.models.SlayerArea;
import com.slayerbanktab.models.SlayerMaster;
import com.slayerbanktab.models.SlayerSetup;
import com.slayerbanktab.models.Task;
import com.slayerbanktab.services.AccountHashManager;
import com.slayerbanktab.services.ClipboardManager;
import com.slayerbanktab.services.DBTableScraper;
import com.slayerbanktab.services.SetupGridBuilder;
import com.slayerbanktab.services.SlayerSetupManager;
import com.slayerbanktab.services.SlayerTaskTracker;
import com.slayerbanktab.services.TaskKeyCompiler;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EnumComposition;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.KeyCode;
import net.runelite.api.MenuAction;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import static net.runelite.api.gameval.InterfaceID.BANKMAIN;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarPlayerID;
import static net.runelite.api.gameval.VarPlayerID.DIZANAS_QUIVER_TEMP_AMMO;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.bank.BankSearch;
import net.runelite.client.plugins.banktags.BankTagsPlugin;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Slayer Bank Tab",
	description = "Plugin that manages a dedicated bank tag tab to show a user-defined, task-specific layout based on your active Slayer task",
	tags = {"bank", "tag", "tab", "slayer", "equipment", "inventory"}
)
@PluginDependency(BankTagsPlugin.class)
public class SlayerBankTabPlugin extends Plugin {

	@Inject
	private Client client;

	@Inject
	private SlayerTaskTracker taskTracker;

	@Inject
	private ClientThread clientThread;

	@Inject
	private EventBus eventBus;

	@Inject
	private SlayerSetupManager setupManager;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private SlayerBankTabConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private BankSearch bankSearch;

	@Inject
	private ItemMapping auxiliaryItemMapping;

	@Inject
	private ChatboxPanelManager chatboxPanelManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Gson gson;

	@Inject
	private AccountHashManager accountHashManager;

	@Inject
	private BankTagsPlugin bankTagsPlugin;

	@Inject
	private DBTableScraper dbTableScraper;

	@Inject
	private ClipboardManager clipboard;

	@Inject
	private TaskKeyCompiler taskKeyCompiler;

	private String slayerTab = null;
	private String slayerTabHeaderKey = null;
	private String slayerTabHeaderText = null;
	private String slayerTabKey = null;
	private String slayerTabConfigKey = null;
	private boolean allowInventoryMenuOption = false;
	private boolean isSyncingConfig = false;
	private String lastSyncedBankTagsCsv = "";
	private boolean isSlayerTabActive = false;

	private String currentSetupKey = null;
	private String currentTaskTarget = "NA";
	private int currentSlayerMasterId = -1;
	private String currentSlayerMaster = "NA";
	private String currentSlayerLocation = "NA";

	private boolean allowSetupCaching = false;
	private int setupCacheSlayerCount = -1;
	private SlayerSetup temporaryCachedSetup = null;

	private boolean allowAutoSetupNotification = false;
	private final Set<String> notifiedTaskKeysSession = new HashSet<>();

	private SlayerBankTabPanel panel;
	private NavigationButton navButton;

	private final Set<Long> registeredAccountHashes = new HashSet<>();
	private final Map<Integer, String> konarSlayerAreas = new HashMap<>();

	@Provides
	SlayerBankTabConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(SlayerBankTabConfig.class);
	}

	public Client getClient() {
		return client;
	}

	public ClientThread getClientThread() {
		return clientThread;
	}

	public SlayerTaskTracker getTaskTracker() {
		return taskTracker;
	}

	public ClipboardManager getClipboardManager()
	{
		return clipboard;
	}

	@Override
	protected void startUp() {
		accountHashManager.load();
		updateConfigurationVariables();
		setupManager.loadSetups();
		auxiliaryItemMapping.reloadCustomMappings();

		panel = new SlayerBankTabPanel(this, config, dbTableScraper);
		loadAccountHashesFromConfig();
		panel.refreshAccounts(accountHashManager.getSnapshot());

		clientThread.invokeLater(() -> {
			final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");

			navButton = NavigationButton.builder()
				.tooltip(PLUGIN_NAME)
				.icon(icon)
				.priority(10)
				.panel(panel)
				.build();
			clientToolbar.addNavigation(navButton);
		});

		loadAreas();
		eventBus.register(taskTracker);

		if (client.getGameState() == GameState.LOGGED_IN) {
			refreshAccountState();
		}
	}

	@Subscribe
	public void onClientTick(ClientTick event) {
		if (accountHashManager.checkTickRegistration()) {
			if (panel != null) {
				panel.refreshAccounts(accountHashManager.getSnapshot());
			}
		}
	}

	@Override
	protected void shutDown() {
		eventBus.unregister(taskTracker);
		if (navButton != null) {
			clientToolbar.removeNavigation(navButton);
		}
	}

	private void loadAccountHashesFromConfig() {
		String fromConfig = configManager.getConfiguration(CONFIG_GROUP, ACCOUNT_HASHES_CONFIG_KEY);
		if (fromConfig == null) return;
		for (String accountHash : fromConfig.split(",")) {
			registeredAccountHashes.add(Long.parseLong(accountHash));
		}
	}

	private boolean addAccountHash(long accountHash) {
		if (registeredAccountHashes.contains(accountHash)) return false;
		registeredAccountHashes.add(accountHash);
		configManager.setConfiguration(CONFIG_GROUP, ACCOUNT_HASHES_CONFIG_KEY, registeredAccountHashes.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(",")));
		return true;
	}

	private void loadAreas() {
		clientThread.invokeLater(() -> {
			konarSlayerAreas.clear();
			for (SlayerArea area : SlayerArea.values()) {
				konarSlayerAreas.put(area.getId(), area.getName());
			}
			if (panel != null) panel.populateLocations(konarSlayerAreas);
		});
	}

	public String compileSpecificKey(long hash, SlayerMaster master, Task task, int areaId) {
		if (master == SlayerMaster.NONE) return hash + "_0_0_0_0_0";

		int masterId = master.getId();
		String masterToken = String.valueOf(masterId);
		boolean isBoss = (task != null && task.isBoss());

		if (isBoss && master != SlayerMaster.KRYSTILIA) {
			masterToken = "99";
		} else if (config.mergeOtherSetups() && master != SlayerMaster.TURAEL && master != SlayerMaster.KRYSTILIA && master != SlayerMaster.KONAR && master != SlayerMaster.MORTIMER) {
			masterToken = "99";
		}

		int targetId;
		int bossId;

		if (master == SlayerMaster.TURAEL && config.mergeTuraelTasks()) {
			targetId = 0;
			bossId = 0;
		} else {
			targetId = (task != null) ? task.getSlayerTargetId() : 0;
			bossId = (task != null) ? task.getSlayerTargetBossId() : 0;
		}

		int safeAreaId = (master == SlayerMaster.KONAR && !masterToken.equals("99")) ? areaId : 0;

		String definitiveKey = String.format("%d_%s_%d_%d_%d_0", hash, masterToken, targetId, bossId, safeAreaId);
		String searchPrefix = String.format("%d_%s_%d_%d_%d_", hash, masterToken, targetId, bossId, safeAreaId);

		for (String savedKey : setupManager.getAllSetups().keySet()) {
			if (savedKey.startsWith(searchPrefix)) return savedKey;
		}
		return definitiveKey;
	}

	// --- ACCOUNT STATE MANAGEMENT ---
	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		accountHashManager.onGameStateChanged(event.getGameState());
		if (event.getGameState() == GameState.LOGGED_IN) {
			refreshAccountState();
		} else if (event.getGameState() == GameState.LOGIN_SCREEN) {
			slayerTabConfigKey = null;
			slayerTab = null;
			slayerTabKey = null;
			isSlayerTabActive = false;
			notifiedTaskKeysSession.clear();
		}
	}

	@Subscribe
	private void onScriptPreFired(ScriptPreFired event) {
		if (slayerTab == null && event.getScriptId() == ScriptID.BANKMAIN_INIT) {
			refreshAccountState();
		}
	}

	private void refreshAccountState() {
		long hash = client.getAccountHash();
		if (hash != -1) {
			boolean isNewHash = addAccountHash(hash);
			if (panel != null && isNewHash) panel.refreshAccounts(accountHashManager.getSnapshot());

			slayerTabConfigKey = "slayerTagTab" + hash;
			updateSlayerTag();
			verifySlayerTag();

			clientThread.invokeLater(() -> {
				taskTracker.startUp();

				if (taskTracker.hasActiveTask()) {
					currentSetupKey = taskTracker.getSetupKey();
					syncLayoutToBankTags();
				}
			});
		}
	}

	// --- CORE LOGIC: TASK ASSIGNMENT & CONFIG SYNC ---
	@Subscribe
	public void onNewSlayerTask(NewSlayerTask event) {
		currentTaskTarget = event.getTaskId() == BOSS_TASK_ID ? event.getBossName() : event.getTaskName();
		currentSlayerMaster = event.getMasterName();
		currentSlayerMasterId = event.getMasterId();
		currentSlayerLocation = event.getAreaName();
		temporaryCachedSetup = null;
		currentSetupKey = event.getSetupKey();
		if (currentSetupKey != null) {
			updateAdditionalItemsForSetup(currentSetupKey, false);
		}
		syncLayoutToBankTags();
	}

	private boolean currentSetupExists()
	{
		String taskConfigKey = "layout_" + currentSetupKey;
		String currentLayoutCsv = configManager.getConfiguration(PluginConstants.CONFIG_GROUP, taskConfigKey);
		return currentLayoutCsv != null && !currentLayoutCsv.isEmpty();
	}

	private void syncLayoutToBankTags() {
		if (slayerTab == null || slayerTabKey == null) return;
		if (isSyncingConfig) return;
		isSyncingConfig = true;
		try {

			if (currentSetupKey == null) {
				if (!lastSyncedBankTagsCsv.isEmpty()) {
					configManager.unsetConfiguration("banktags", slayerTabKey);
					lastSyncedBankTagsCsv = "";
				}
				return;
			}
			String taskConfigKey = "layout_" + currentSetupKey;
			String currentLayoutCsv;
			currentLayoutCsv = currentSetupExists() ? configManager.getConfiguration(PluginConstants.CONFIG_GROUP, taskConfigKey) : null;

			if (currentLayoutCsv == null) {
				SlayerSetup jsonSetup = setupManager.getSetupForTask(currentSetupKey);
				if (jsonSetup != null && jsonSetup.getGridLayout() != null) {
					currentLayoutCsv = Arrays.stream(jsonSetup.getGridLayout()).mapToObj(String::valueOf).collect(Collectors.joining(","));
					configManager.setConfiguration(PluginConstants.CONFIG_GROUP, taskConfigKey, currentLayoutCsv);
				} else {
					currentLayoutCsv = "";
					configManager.setConfiguration(PluginConstants.CONFIG_GROUP, taskConfigKey, currentLayoutCsv);
				}
			}

			if (currentLayoutCsv.isEmpty()) {
				if (!allowSetupCaching && allowAutoSetupNotification && !notifiedTaskKeysSession.contains(currentSetupKey)) {
					String decoded = taskKeyCompiler.formatKeyToReadable(currentSetupKey);
					sendChatMessage("[Slayer bank tab] No layout for task " + decoded + " exists.");
					notifiedTaskKeysSession.add(currentSetupKey);
				}
				configManager.setConfiguration("banktags", slayerTabKey, "");
				lastSyncedBankTagsCsv = "";
				return;
			} else {
				String liveWovenCsv = Arrays.stream(currentLayoutCsv.split(","))
					.map(String::trim)
					.map(token -> {
						try {
							int id = Integer.parseInt(token);
							return id > 0 ? String.valueOf(resolveToPlayerOwnedVariant(id)) : token;
						} catch (NumberFormatException e) {
							return token;
						}
					}).collect(Collectors.joining(","));

				if (!liveWovenCsv.equals(lastSyncedBankTagsCsv)) {
					configManager.setConfiguration("banktags", slayerTabKey, liveWovenCsv);
					String standardizedTabName = Text.standardize(slayerTab);
					String uniqueTagMembers = Arrays.stream(liveWovenCsv.split(","))
						.map(String::trim)
						.filter(s -> !s.isEmpty() && !s.equals("-1"))
						.distinct()
						.collect(Collectors.joining(","));
					configManager.setConfiguration("banktags", standardizedTabName, uniqueTagMembers);
					lastSyncedBankTagsCsv = liveWovenCsv;
				}
			}

		} finally {
			isSyncingConfig = false;
		}

		reloadTagTab();
	}

	private String getFormattedTaskName() {
		if (currentTaskTarget == null || currentTaskTarget.equalsIgnoreCase("None") || currentTaskTarget.equalsIgnoreCase("NA")) {
			return "No task assigned";
		}
		Task taskEnum = Task.getById(taskTracker.getActiveTaskId(), taskTracker.getActiveBossId());
		if (taskEnum != null && taskEnum != Task.UNKNOWN_BOSS) {
			if (taskEnum.isBoss()) {
				return taskEnum.getName();
			}
		}

		String[] words = currentTaskTarget.split(" ");
		StringBuilder sb = new StringBuilder();
		for (String word : words) {
			if (word.isEmpty()) continue;
			if (sb.length() > 0) sb.append(" ");
			sb.append(word.substring(0, 1).toUpperCase());
			if (word.length() > 1) {
				sb.append(word.substring(1).toLowerCase());
			}
		}
		return sb.toString();
	}

	private String getTaskHeaderText() {
		if (currentTaskTarget == null || currentTaskTarget.equalsIgnoreCase("None") || currentTaskTarget.equalsIgnoreCase("NA")) {
			return "No task assigned";
		}

		String taskName = getFormattedTaskName();

		SlayerMaster master = SlayerMaster.getById(currentSlayerMasterId);
		String masterName = (master != null && master != SlayerMaster.UNKNOWN) ? master.getName() : currentSlayerMaster;
		if (masterName == null || masterName.equalsIgnoreCase("NA")) {
			masterName = "";
		}

		String masterPart = masterName.isEmpty() ? "" : (" by " + masterName);

		String locationPart = "";
		if (currentSlayerMasterId == SlayerMaster.KONAR.getId() && currentSlayerLocation != null && !currentSlayerLocation.equalsIgnoreCase("NA") && !currentSlayerLocation.isEmpty()) {
			masterPart = "";
			String locLower = currentSlayerLocation.toLowerCase();
			if (locLower.startsWith("in ") || locLower.startsWith("at ") || locLower.startsWith("on ")) {
				locationPart = " " + currentSlayerLocation;
			} else {
				locationPart = " in " + currentSlayerLocation;
			}
		}

		String prefix = taskTracker.isActiveTaskKey(currentSetupKey) ? "[Current] " : "             ";

		return  prefix + taskName + masterPart + locationPart;
	}

	private void updateBankTitleBar() {
		if (client.getGameState() != GameState.LOGGED_IN || slayerTab == null) {
			isSlayerTabActive = false;
			return;
		}

		Widget bankTitleWidget = client.getWidget(BANKMAIN, 3);
		if (bankTitleWidget == null || bankTitleWidget.isHidden()) {
			isSlayerTabActive = false;
			return;
		}

		if (slayerTabHeaderText == null || !slayerTabHeaderKey.equals(currentSetupKey))
		{
			slayerTabHeaderText = getTaskHeaderText();
			slayerTabHeaderKey = currentSetupKey;
		}

		String currentText = Text.removeTags(bankTitleWidget.getText()).toLowerCase().strip();
		if (currentText.equals(slayerTabHeaderText)) return;

		if (currentText.endsWith(slayerTab.toLowerCase())) {
			boolean wasActive = isSlayerTabActive;
			isSlayerTabActive = true;
			bankTitleWidget.setText(slayerTabHeaderText);

			if (!wasActive && currentSetupKey != null) {
				updateAdditionalItemsForSetup(currentSetupKey, false);
			}
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event) {
		if (event.getScriptId() == ScriptID.BANKMAIN_BUILD) {
			clientThread.invokeLater(this::updateBankTitleBar);
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event) {
		if (event.getGroupId() == BANKMAIN) {
			isSlayerTabActive = false;
			handleBankCloseCache();
		}
	}

	private void handleBankCloseCache() {
		if (!allowSetupCaching || currentSetupKey == null) return;
		int currentTaskCount = taskTracker.getActiveTaskCount();
		if (currentTaskCount == 0) return;

		SlayerSetup existingSetup = setupManager.getSetupForTask(currentSetupKey);
		if (existingSetup != null && existingSetup.getGridLayout() != null) {
			for (int id : existingSetup.getGridLayout()) {
				if (id > 0) return;
			}
		}

		temporaryCachedSetup = extractSetupFromGear(currentSetupKey);
		setupCacheSlayerCount = currentTaskCount;

		if (allowAutoSetupNotification && !notifiedTaskKeysSession.contains(currentSetupKey)) {
			notifiedTaskKeysSession.add(currentSetupKey);
			sendChatMessage("[Slayer bank tab] No layout exists for " + currentTaskTarget + " assigned by " + currentSlayerMaster + ". Current equipment and inventory have been cached and will be saved automatically after your first killcount.");
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event) {
		if (temporaryCachedSetup != null && event.getVarpId() == VarPlayerID.SLAYER_COUNT) {
			int newCount = client.getVarpValue(VarPlayerID.SLAYER_COUNT);
			if (newCount == 0) return;
			if (newCount < setupCacheSlayerCount) {
				autoSaveCachedSetup();
			}
		}
	}

	private void autoSaveCachedSetup() {
		if (temporaryCachedSetup != null && currentSetupKey != null) {
			setupManager.saveSetup(currentSetupKey, temporaryCachedSetup);
			temporaryCachedSetup = null;
			setupCacheSlayerCount = -1;
			syncLayoutToBankTags();
			sendChatMessage("Auto-saved cached layout for task " + currentTaskTarget + " by " + currentSlayerMaster);
		}
	}

	private void updateConfigurationVariables() {
		allowSetupCaching = config.autoAssignUndefinedSetups();
		allowAutoSetupNotification = allowSetupCaching && config.notifyOnCache();
		allowInventoryMenuOption = config.enableInventoryIconMenuOption();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		String group = event.getGroup();
		String key = event.getKey();
		boolean isPluginConfig = group.equals(PluginConstants.CONFIG_GROUP);

		if (isPluginConfig) {
			updateConfigurationVariables();
			if ((key.equals("mergeTuraelTasks") || key.equals("mergeOtherSetups")) && panel != null) {
				panel.populateMasters();
			}
			if (key.equals("customAdditionalItemMappings")) {
				auxiliaryItemMapping.reloadCustomMappings();
				return;
			} else if (key.equals(slayerTabConfigKey)) {
				updateSlayerTag();
				verifySlayerTag();
				return;
			}
		}

		if (isSyncingConfig) return;

		verifySlayerTag();
		if (slayerTab == null || currentSetupKey == null) return;

		if (group.equals("banktags") && key.equals(slayerTabKey)) {
			if (isUndoingDrag) return;
			String newCsvLayout = event.getNewValue();
			if (newCsvLayout == null) newCsvLayout = "";
			isSyncingConfig = true;
			configManager.setConfiguration(PluginConstants.CONFIG_GROUP, "layout_" + currentSetupKey, newCsvLayout);
			isSyncingConfig = false;
			updateJsonFallback(currentSetupKey, newCsvLayout);

			String standardizedTabName = Text.standardize(slayerTab);
			String uniqueTagMembers = Arrays.stream(newCsvLayout.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty() && !s.equals("-1"))
				.distinct()
				.collect(Collectors.joining(","));
			configManager.setConfiguration("banktags", standardizedTabName, uniqueTagMembers);
			lastSyncedBankTagsCsv = newCsvLayout;
			return;
		}

		if (group.equals("banktags") && key.startsWith("item_")) {
			if (isUndoingDrag) return;

			String newVal = event.getNewValue() == null ? "" : event.getNewValue();
			String oldVal = event.getOldValue();

			if (containsTag(newVal, slayerTab) && !containsTag(oldVal == null ? "" : oldVal, slayerTab)) {
				isUndoingDrag = true;
				try {
					if (oldVal == null || oldVal.trim().isEmpty()) {
						configManager.unsetConfiguration("banktags", key);
					} else {
						configManager.setConfiguration("banktags", key, oldVal);
					}
				} finally {
					isUndoingDrag = false;
				}

				try {
					int itemId = Integer.parseInt(key.substring(5));
					clientThread.invokeLater(() -> addItemToCurrentSlayerTab(itemId, true));
				} catch (NumberFormatException ignored) {}
			}
		}
	}

	private boolean isUndoingDrag = false;

	/**
	 * Checks if a comma-separated Bank Tags string contains our target tab name.
	 */
	private boolean containsTag(String tagString, String targetTag) {
		if (tagString == null || targetTag == null) return false;
		String standardizedTarget = Text.standardize(targetTag);
		return Arrays.stream(tagString.split(","))
			.map(String::trim)
			.map(Text::standardize)
			.anyMatch(standardizedTarget::equals);
	}

	/**
	 * Removes the active Slayer tab's tag from an item's personal configuration entry.
	 * This ensures the item only appears when our CSV explicitly lists it, allowing it
	 * to vanish cleanly when switching tasks.
	 */
	private void stripGlobalItemTag(int... itemIds) {
		if (slayerTab == null) return;
		String targetTag = Text.standardize(slayerTab);

		for (int id : itemIds) {
			String configKey = "item_" + id;
			String existingTags = configManager.getConfiguration("banktags", configKey);
			if (existingTags == null || existingTags.isEmpty()) continue;

			String cleanedTags = Arrays.stream(existingTags.split(","))
				.map(String::trim)
				.filter(t -> !t.isEmpty() && !Text.standardize(t).equals(targetTag))
				.collect(Collectors.joining(","));

			if (cleanedTags.isEmpty()) {
				configManager.unsetConfiguration("banktags", configKey);
			} else if (!cleanedTags.equals(existingTags)) {
				configManager.setConfiguration("banktags", configKey, cleanedTags);
			}
		}
	}

	private void updateSlayerTag() {
		String tabConfig = configManager.getConfiguration(PluginConstants.CONFIG_GROUP, slayerTabConfigKey);
		if (tabConfig == null || tabConfig.trim().isEmpty()) {
			slayerTab = null;
			slayerTabKey = null;
			return;
		}
		slayerTab = tabConfig;
		slayerTabKey = "layout_" + Text.standardize(tabConfig);
	}

	private void verifySlayerTag() {
		if (slayerTab == null) return;
		String activeTagsStr = configManager.getConfiguration("banktags", "tagtabs");
		if (activeTagsStr == null || activeTagsStr.trim().isEmpty()) return;

		boolean tabExists = false;
		String standardizedSlayerTab = Text.standardize(slayerTab);
		for (String tag : activeTagsStr.split(",")) {
			if (Text.standardize(tag).equals(standardizedSlayerTab)) {
				tabExists = true;
				break;
			}
		}
		if (!tabExists) {
			clearSlayerTabConfig();
		}
	}

	private void clearSlayerTabConfig() {
		configManager.unsetConfiguration(PluginConstants.CONFIG_GROUP, slayerTabConfigKey);
		slayerTab = null;
		slayerTabKey = null;
	}

	private void updateJsonFallback(String key, String csv) {
		if (csv == null || csv.trim().isEmpty()) {
			setupManager.saveSetup(key, new SlayerSetup(new int[0]));
			return;
		}
		try {
			int[] layout = Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).mapToInt(Integer::parseInt).toArray();
			setupManager.saveSetup(key, new SlayerSetup(layout));
		} catch (Exception e) {
			log.error("Failed to parse native CSV to int array for JSON weaving", e);
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event) {
		String menuOption = event.getOption();

		if ("View tag tab".equals(menuOption)) {
			String targetTag = Text.removeTags(event.getTarget());
			boolean slayerTabExists = slayerTab != null;

			if (slayerTabExists && targetTag.equalsIgnoreCase(slayerTab)) {
				addSlayerTabOptions(event.getTarget());
			} else if (!slayerTabExists) {
				addNonSlayerTabOptions(event.getTarget());
			}

		} else if ("New tag tab".equals(menuOption)) {
			if (slayerTab != null) {
				client.getMenu().createMenuEntry(-1)
					.setOption("Unset slayer tab")
					.setTarget(event.getTarget())
					.setType(MenuAction.RUNELITE)
					.onClick(e -> {
						configManager.unsetConfiguration("banktags", "layout_" + Text.standardize(slayerTab));
						configManager.unsetConfiguration(PluginConstants.CONFIG_GROUP, slayerTabConfigKey);
						updateSlayerTag();
						clientThread.invokeLater(() -> bankSearch.layoutBank());
					});
			} else {
				client.getMenu().createMenuEntry(-1)
					.setOption("Add Slayer tab")
					.setTarget(event.getTarget())
					.setType(MenuAction.RUNELITE)
					.onClick(e -> addNewSlayerTagTab());
			}
		} else if (allowInventoryMenuOption && "Inventory".equals(menuOption) && client.isKeyPressed(KeyCode.KC_SHIFT)) {
			client.getMenu().createMenuEntry(-1)
				.setOption("Save Slayer tab setup for this task")
				.setTarget(event.getTarget())
				.setType(MenuAction.RUNELITE)
				.onClick(e -> {
					if (currentSetupKey != null) {
						createSetupFromInventoryAndEquipment(currentSetupKey);
					}
				});
		}

		if (config.enableAddItemToSlayTabMenuOption() && "Examine".equals(menuOption) && currentSetupKey != null) {
			Widget bankRoot = client.getWidget(BANKMAIN, 1);
			if (bankRoot == null || bankRoot.isHidden()) {
				return;
			}

			int itemId = event.getMenuEntry().getItemId();
			if (itemId > 0) {
				client.getMenu().createMenuEntry(-1)
					.setOption("Add item to current slayer tab")
					.setTarget(event.getTarget())
					.setType(MenuAction.RUNELITE)
					.onClick(e -> addItemToCurrentSlayerTab(itemId, false));
			}
		}
	}

	/**
	 * Resolves the base item ID and appends it to the additional items layer (index 64+)
	 * of the currently active Slayer setup.
	 */
	private void addItemToCurrentSlayerTab(int rawItemId, boolean fromDrag) {
		if (currentSetupKey == null) {
			if (!fromDrag) {
				sendChatMessage("You do not currently have an active Slayer task setup loaded.");
			}
			return;
		}

		ItemComposition comp = itemManager.getItemComposition(rawItemId);
		int itemId = rawItemId;
		if (comp.getNote() != -1) {
			itemId = comp.getLinkedNoteId();
		} else if (comp.getPlaceholderTemplateId() != -1) {
			itemId = comp.getPlaceholderId();
		}

		stripGlobalItemTag(rawItemId, itemId);

		SlayerSetup setup = setupManager.getSetupForTask(currentSetupKey);
		int[] oldGrid = (setup != null && setup.getGridLayout() != null) ? setup.getGridLayout() : new int[0];
		int[] buffer = new int[PluginConstants.MAX_TAB_ITEMS];
		Arrays.fill(buffer, -1);
		int highestOccupied = 64;
		boolean alreadyExists = false;

		for (int i = 0; i < oldGrid.length; i++) {
			if (i < buffer.length) {
				buffer[i] = oldGrid[i];
				if (oldGrid[i] > 0) {
					highestOccupied = Math.max(highestOccupied, i);
					if (oldGrid[i] == itemId) {
						alreadyExists = true;
					}
				}
			}
		}

		if (alreadyExists) {
			if (!fromDrag) {
				sendChatMessage("That item is already saved in your active Slayer tab setup.");
			}
			return;
		}

		final int coreGearBoundary = 64;
		int insertIndex = Math.max(coreGearBoundary, highestOccupied + 1);
		if (insertIndex >= buffer.length) {
			if (!fromDrag) {
				sendChatMessage("Your Slayer tab layout has reached the maximum item limit (" + PluginConstants.MAX_TAB_ITEMS + ").");
			}
			return;
		}

		buffer[insertIndex] = itemId;
		highestOccupied = Math.max(highestOccupied, insertIndex);

		int[] newGrid = Arrays.copyOf(buffer, highestOccupied + 1);
		setupManager.saveSetup(currentSetupKey, new SlayerSetup(newGrid));
		String csv = Arrays.stream(newGrid).mapToObj(String::valueOf).collect(Collectors.joining(","));
		configManager.setConfiguration(PluginConstants.CONFIG_GROUP, "layout_" + currentSetupKey, csv);
		syncLayoutToBankTags();

		if (panel != null) {
			panel.refreshSetupProjection();
		}
		if (!fromDrag) {
			sendChatMessage("Added " + comp.getName() + " to the additional items layer of your current setup!");
		}
	}

	/**
	 * Creates a new tag tab that is marked as slayer tag tab
	 */
	private void addNewSlayerTagTab() {
		chatboxPanelManager.openTextInput("Enter Slayer tab name")
			.onDone((String tagName) -> {
				if (tagName == null || tagName.trim().isEmpty()) return;
				clientThread.invokeLater(() -> {
					String cleanName = tagName.trim();
					String standardized = Text.standardize(cleanName);
					String activeTagsStr = configManager.getConfiguration("banktags", "tagtabs");
					List<String> currentTabs = new ArrayList<>();

					if (activeTagsStr != null && !activeTagsStr.trim().isEmpty()) {
						currentTabs.addAll(Arrays.asList(activeTagsStr.split(",")));
					}

					if (currentTabs.stream().noneMatch(t -> Text.standardize(t).equals(standardized))) {
						currentTabs.add(cleanName);
						configManager.setConfiguration("banktags", "tagtabs", String.join(",", currentTabs));
					}

					configManager.setConfiguration(PluginConstants.CONFIG_GROUP, slayerTabConfigKey, cleanName);
					updateSlayerTag();
					syncLayoutToBankTags();
					bankSearch.layoutBank();
				});
			}).build();
		verifySlayerTag();
	}

	/**
	 * Adds menu options to a regular tag tab
	 */
	private void addNonSlayerTabOptions(String target) {
		client.getMenu().createMenuEntry(-1)
			.setOption("Set as Slayer tab")
			.setTarget(target)
			.setType(MenuAction.RUNELITE)
			.onClick(e -> {
				configManager.setConfiguration(PluginConstants.CONFIG_GROUP, slayerTabConfigKey, Text.removeTags(target));
				updateSlayerTag();
				syncLayoutToBankTags();
				clientThread.invokeLater(() -> bankSearch.layoutBank());
			});
	}

	private void addSlayerTabOptions(String target) {
		client.getMenu().createMenuEntry(-1)
			.setOption("Unset as Slayer tab")
			.setTarget(target)
			.setType(MenuAction.RUNELITE)
			.onClick(e -> {
				configManager.unsetConfiguration(PluginConstants.CONFIG_GROUP, slayerTabConfigKey);
				configManager.unsetConfiguration("banktags", "layout_" + Text.standardize(Text.removeTags(e.getTarget())));
				updateSlayerTag();
				clientThread.invokeLater(() -> bankSearch.layoutBank());
			});

		client.getMenu().createMenuEntry(-1)
			.setOption("Save current layout")
			.setTarget(target)
			.setType(MenuAction.RUNELITE)
			.onClick(e -> {
				if (currentSetupKey != null) {
					createSetupFromInventoryAndEquipment(currentSetupKey);
				}
			});

		client.getMenu().createMenuEntry(-1)
			.setOption("Reload active task")
			.setTarget(target)
			.setType(MenuAction.RUNELITE)
			.onClick(e -> {
				clientThread.invokeLater(() -> {
					if (taskTracker.hasActiveTask()) {
						onNewSlayerTask(taskTracker.getCurrentTask());
					}
				});
			});

		client.getMenu().createMenuEntry(-1)
			.setOption("Load layout key from clipboard")
			.setTarget(target)
			.setType(MenuAction.RUNELITE)
			.onClick(e -> {
				loadSetupKeyFromClipboard();
			});
	}

	/**
	 * Sets the key from the clipboard as the task loaded in the slayer tab instead of the currently active task
	 */
	private void loadSetupKeyFromClipboard()
	{
		String rawText = clipboard.getText();
		if (rawText == null || rawText.isEmpty())
		{
			sendChatMessage("Error: System clipboard is empty.");
			return;
		}

		String cleanKey = rawText.trim();
		if (!taskTracker.isLegalKey(cleanKey))
		{
			String snippet = cleanKey.length() < 40 ? (" '" + cleanKey + "'") : "";
			sendChatMessage("Error: clipboard text" + snippet + " is not a valid task key.");
			return;
		}

		String[] parts = cleanKey.split("_");
		long activeHash = client.getAccountHash();
		if (activeHash == -1 || !parts[0].equals(String.valueOf(activeHash)))
		{
			log.debug("AccountHash was changed from {} to {}.", parts[0], activeHash);
			cleanKey = cleanKey.replace(parts[0], String.valueOf(activeHash));
		}

		int masterToken = Integer.parseInt(parts[1]);
		int targetId    = Integer.parseInt(parts[2]);
		int bossId      = Integer.parseInt(parts[3]);
		int areaId      = Integer.parseInt(parts[4]);

		SlayerMaster master = SlayerMaster.getById(masterToken);
		Task task = Task.getById(targetId, bossId);

		currentSetupKey = cleanKey;
		currentSlayerMasterId = master.getId();
		currentSlayerMaster   = master.getDisplayName(client);
		currentSlayerLocation = (areaId == 0) ? "NA" : SlayerArea.getById(areaId).getName();

		if (master == SlayerMaster.TURAEL && targetId == 0 && bossId == 0)
		{
			currentTaskTarget = "Merged Turael Tasks";
		}
		else if (currentSetupKey.endsWith(NO_TASK_KEY_SUFFIX))
		{
			currentTaskTarget = "No active task";
		}
		else if (task == null)
		{
			currentTaskTarget = "Undefined task masterId=" + master.getId() + " TargetId=" + targetId;
		}
		else
		{
			currentTaskTarget = task.getName();
		}

		temporaryCachedSetup = null;
		lastSyncedBankTagsCsv = "";
		if (currentSetupKey != null) {
			updateAdditionalItemsForSetup(currentSetupKey, false);
		}
		syncLayoutToBankTags();

		String readableTask = taskKeyCompiler.formatKeyToReadable(cleanKey);
		if (readableTask.startsWith("[") && readableTask.endsWith("]"))
		{
			readableTask = readableTask.substring(1, readableTask.length() - 1);
		}
		sendChatMessage("Loaded Slayer tab from clipboard: " + readableTask);
	}

	/**
	 * Reloads the tag tab and updates the layout while doing so
	 */
	private void reloadTagTab() {
		clientThread.invokeLater(() -> {
			String bankTag = bankTagsPlugin.getActiveTag();

			if (bankTag != null && !bankTag.isEmpty()) {
				bankTagsPlugin.closeBankTag();
				Widget bankItemContainer = client.getWidget(BANKMAIN, 13);
				if (bankItemContainer != null) {
					bankItemContainer.setScrollY(0);
				}
				bankTagsPlugin.openBankTag(bankTag);
			}

			bankSearch.layoutBank();
			updateBankTitleBar();
		});
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted) {
		if (currentSetupKey == null) return;
		String command = commandExecuted.getCommand();

		switch (command) {
			case "createslayersetupcurrent":
				createSetupFromInventoryAndEquipment(currentSetupKey);
				break;
		}
	}

	/**
	 * Completely deletes a saved layout from both the hard drive JSON store
	 * and the local RuneLite ConfigManager cache.
	 */
	public void deleteSetupLayout(String targetKey) {
		if (targetKey == null || targetKey.isEmpty()) return;

		setupManager.deleteSetup(targetKey);
		configManager.unsetConfiguration(PluginConstants.CONFIG_GROUP, "layout_" + targetKey);
		if (targetKey.equalsIgnoreCase(currentSetupKey)) {
			clientThread.invokeLater(this::syncLayoutToBankTags);
		}
	}

	private void createSetupFromInventoryAndEquipment(String setupKey) {
		if (setupKey == null) return;
		SlayerSetup newSetup = extractSetupFromGear(setupKey);
		String csvLayout = Arrays.stream(newSetup.getGridLayout()).mapToObj(String::valueOf).collect(Collectors.joining(","));

		isSyncingConfig = true;
		configManager.setConfiguration(PluginConstants.CONFIG_GROUP, "layout_" + setupKey, csvLayout);
		isSyncingConfig = false;

		setupManager.saveSetup(setupKey, newSetup);
		syncLayoutToBankTags();
		sendUpdatedSetupChatMessage();
	}

	private SlayerSetup extractSetupFromGear(String setupKey) {
		Map<ExtendedEquipmentSlot, Integer> equipmentMap = new HashMap<>();
		Integer[] inventoryArray = new Integer[28];
		boolean hasQuiver = false;
		boolean hasRunePouch = false;
		boolean hasBoltPouch = false;
		boolean hasLootingBag = false;

		ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
		if (equipment != null) {
			for (EquipmentInventorySlot slot : EquipmentInventorySlot.values()) {
				Item item = equipment.getItem(slot.getSlotIdx());
				if (item != null && item.getId() > 0) {
					int itemId = item.getId();
					ExtendedEquipmentSlot extendedSlot = ExtendedEquipmentSlot.fromVanilla(slot);
					if (extendedSlot != null) {
						equipmentMap.put(extendedSlot, itemId);
						if (isQuiverVariant(itemId)) hasQuiver = true;
					}
				}
			}
		}

		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory != null) {
			Item[] items = inventory.getItems();
			for (int i = 0; i < items.length; i++) {
				int itemId = items[i].getId();
				if (itemId > 0) {
					inventoryArray[i] = itemId;
					if (isQuiverVariant(itemId)) hasQuiver = true;
					else if (isRunePouchVariant(itemId)) hasRunePouch = true;
					else if (itemId == ItemID.XBOWS_BOLT_POUCH) hasBoltPouch = true;
					if (isLootingBagVariant(items[i].getId())) hasLootingBag = true;
				}
			}
		}

		if (hasQuiver) {
			int quiverItemId = client.getVarpValue(DIZANAS_QUIVER_TEMP_AMMO);
			if (quiverItemId != -1) {
				equipmentMap.put(ExtendedEquipmentSlot.QUIVER_AMMO, quiverItemId);
			}
		}

		List<Integer> finalAuxiliary = new ArrayList<>();
		Set<Integer> uniqueAuxItems = new HashSet<>();

		for (int itemId : equipmentMap.values()) {
			for (int auxId : auxiliaryItemMapping.getAuxiliaryItems(itemId)) {
				if (uniqueAuxItems.add(auxId)) finalAuxiliary.add(auxId);
			}
		}
		for (Integer itemId : inventoryArray) {
			if (itemId != null && itemId > 0) {
				for (int auxId : auxiliaryItemMapping.getAuxiliaryItems(itemId)) {
					if (uniqueAuxItems.add(auxId)) finalAuxiliary.add(auxId);
				}
			}
		}

		if (config.autoAddRunePouchRunes() && hasRunePouch) {
			for (int runeItemId : getRunePouchContents()) {
				if (uniqueAuxItems.add(runeItemId)) {
					finalAuxiliary.add(runeItemId);
				}
			}
		}

		if (config.autoAddBoltPouchBolts() && hasBoltPouch) {
			for (int boltItemId : getBoltPouchContents()) {
				if (uniqueAuxItems.add(boltItemId)) {
					finalAuxiliary.add(boltItemId);
				}
			}
		}

		if (config.autoAddLootingBagContents() && hasLootingBag) {
			for (int lootItemId : getLootingBagContents()) {
				if (uniqueAuxItems.add(lootItemId)) {
					finalAuxiliary.add(lootItemId);
				}
			}
		}

		SlayerSetup existingSetup = setupManager.getSetupForTask(setupKey);
		if (existingSetup != null && existingSetup.getGridLayout() != null) {
			int[] oldLayout = existingSetup.getGridLayout();
			for (int i = 64; i < oldLayout.length; i++) {
				if (oldLayout[i] > 0 && uniqueAuxItems.add(oldLayout[i])) {
					finalAuxiliary.add(oldLayout[i]);
				}
			}
		}

		int[] gridLayout = SetupGridBuilder.buildDraftArray(equipmentMap, inventoryArray, finalAuxiliary);
		return new SlayerSetup(gridLayout);
	}

	private boolean isQuiverVariant(int itemId) {
		return itemId == ItemID.DIZANAS_QUIVER_UNCHARGED
			|| itemId == ItemID.DIZANAS_QUIVER_CHARGED
			|| itemId == ItemID.DIZANAS_QUIVER_BROKEN
			|| itemId == ItemID.DIZANAS_QUIVER_INFINITE
			|| itemId == ItemID.DIZANAS_QUIVER_INFINITE_BROKEN
			|| itemId == ItemID.DIZANAS_QUIVER_INFINITE_TROUVER
			|| itemId == ItemID.DIZANAS_QUIVER_UNCHARGED_TROUVER
			|| itemId == ItemID.SKILLCAPE_MAX_DIZANAS_BROKEN;
	}

	private void sendChatMessage(String text) {
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAMEMESSAGE)
			.runeLiteFormattedMessage(text)
			.build());
	}

	private void sendUpdatedSetupChatMessage() {
		if ("None".equals(currentTaskTarget) || currentTaskTarget == null) {
			sendChatMessage("Setup saved for non-Slayer task layout!");
			return;
		}
		String area = taskTracker.getCurrentArea();
		sendChatMessage("Setup saved for " + currentTaskTarget + " assigned by " + currentSlayerMaster + (area != null && currentSlayerMasterId == SlayerMaster.KONAR.getId() ? " at " + currentSlayerLocation + "!" : "!"));
	}

	private int resolveToPlayerOwnedVariant(int layoutItemId) {
		if (layoutItemId <= 0) return layoutItemId;
		ItemContainer bank = client.getItemContainer(InventoryID.BANK);
		if (bank == null) return layoutItemId;

		for (Item item : bank.getItems()) {
			if (item != null && item.getId() > 0) {
				if (auxiliaryItemMapping.canonicalize(item.getId()) == layoutItemId) {
					return item.getId();
				}
			}
		}
		return layoutItemId;
	}

	public boolean updateAdditionalItemsForSetup(String setupKey, boolean notify) {
		if (setupKey == null) return false;
		SlayerSetup oldSetup = setupManager.getSetupForTask(setupKey);
		if (oldSetup == null || oldSetup.getGridLayout() == null) return false;
		int[] oldGrid = oldSetup.getGridLayout();
		int[] newGridBuffer = new int[PluginConstants.MAX_TAB_ITEMS];
		Arrays.fill(newGridBuffer, -1);
		Set<Integer> recalculatedAdditionalItems = new LinkedHashSet<>();
		int highestOccupiedIndex = -1;
		final int coreGearBoundary = 64;
		int coreLimit = Math.min(oldGrid.length, coreGearBoundary);
		boolean hasRunePouch = false;
		boolean hasBoltPouch = false;
		boolean hasLootBag = false;

		for (int i = 0; i < coreLimit; i++) {
			int itemId = oldGrid[i];
			if (itemId > 0) {
				newGridBuffer[i] = itemId;
				highestOccupiedIndex = Math.max(highestOccupiedIndex, i);
				recalculatedAdditionalItems.addAll(auxiliaryItemMapping.getAuxiliaryItems(itemId));
				if (!hasRunePouch && isRunePouchVariant(itemId)) hasRunePouch = true;
				else if (!hasBoltPouch && isBoltPouchVariant(itemId)) hasBoltPouch = true;
				else if (!hasLootBag && isLootingBagVariant(itemId)) hasLootBag = true;
			}
		}
		if (config.autoAddRunePouchRunes() && hasRunePouch) {
			recalculatedAdditionalItems.addAll(getRunePouchContents());
		}
		if (config.autoAddBoltPouchBolts() && hasBoltPouch) {
			recalculatedAdditionalItems.addAll(getBoltPouchContents());
		}
		if (config.autoAddLootingBagContents() && hasLootBag) {
			recalculatedAdditionalItems.addAll(getLootingBagContents());
		}

		for (int i = coreGearBoundary; i < oldGrid.length; i++) {
			if (oldGrid[i] > 0) {
				recalculatedAdditionalItems.add(oldGrid[i]);
			}
		}

		int currentPointer = coreGearBoundary;
		for (int additionalId : recalculatedAdditionalItems) {
			if (currentPointer >= PluginConstants.MAX_TAB_ITEMS) break;
			newGridBuffer[currentPointer] = additionalId;
			highestOccupiedIndex = Math.max(highestOccupiedIndex, currentPointer);
			currentPointer++;
		}
		int[] finalGrid = highestOccupiedIndex == -1 ? new int[0] : Arrays.copyOf(newGridBuffer, highestOccupiedIndex + 1);

		if (Arrays.equals(oldGrid, finalGrid)) {
			return false;
		}

		SlayerSetup updatedSetup = new SlayerSetup(finalGrid);
		setupManager.saveSetup(setupKey, updatedSetup);
		String csv = Arrays.stream(finalGrid).mapToObj(String::valueOf).collect(Collectors.joining(","));
		configManager.setConfiguration(PluginConstants.CONFIG_GROUP, "layout_" + setupKey, csv);
		if (setupKey.equalsIgnoreCase(currentSetupKey)) {
			syncLayoutToBankTags();
		}
		return true;
	}

	public SlayerSetupManager getSetupManager() {
		return setupManager;
	}

	public ItemManager getItemManager() {
		return itemManager;
	}

	public boolean duplicateSetup(String sourceKey, String targetKey) {
		SlayerSetup sourceSetup = setupManager.getSetupForTask(sourceKey);
		if (sourceSetup == null || sourceSetup.getGridLayout() == null) return false;

		int[] clonedLayout = sourceSetup.getGridLayout().clone();
		String csvLayout = Arrays.stream(clonedLayout)
			.mapToObj(String::valueOf)
			.collect(Collectors.joining(","));

		configManager.setConfiguration(PluginConstants.CONFIG_GROUP, "layout_" + targetKey, csvLayout);
		setupManager.saveSetup(targetKey, new SlayerSetup(clonedLayout));

		if (targetKey.equals(currentSetupKey)) {
			clientThread.invokeLater(this::syncLayoutToBankTags);
		}
		return true;
	}

	public void saveImportedLayout(String targetKey, int[] layout) {
		String csvLayout = Arrays.stream(layout)
			.mapToObj(String::valueOf)
			.collect(Collectors.joining(","));

		configManager.setConfiguration(PluginConstants.CONFIG_GROUP, "layout_" + targetKey, csvLayout);
		setupManager.saveSetup(targetKey, new SlayerSetup(layout));

		if (targetKey.equals(currentSetupKey)) {
			clientThread.invokeLater(this::syncLayoutToBankTags);
		}
	}

	public String serializeSetupsToJson(Map<String, int[]> setups) {
		return gson.toJson(setups);
	}

	public Map<String, int[]> deserializeSetupsFromJson(String json) {
		try {
			Type type = new TypeToken<Map<String, int[]>>(){}.getType();
			return gson.fromJson(json, type);
		} catch (Exception e) {
			return null;
		}
	}

	public void saveMultipleImportedSetups(Map<String, int[]> reanchoredSetups) {
		for (Map.Entry<String, int[]> entry : reanchoredSetups.entrySet()) {
			saveImportedLayout(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Verifies if an item ID is any standard, divine, or dyed variant of the Rune Pouch.
	 */
	private boolean isRunePouchVariant(int itemId) {
		return itemId == ItemID.BH_RUNE_POUCH ||
			itemId == ItemID.DIVINE_RUNE_POUCH ||
			itemId == ItemID.DIVINE_RUNE_POUCH_TROUVER ||
			itemId == ItemID.BH_RUNE_POUCH_TROUVER;
	}

	/**
	 * Inspects the player's live varbits and translates stored rune pouch data into a list of ItemIDs.
	 */
	private List<Integer> getRunePouchContents() {
		List<Integer> runeItemIds = new ArrayList<>();
		try {
			EnumComposition runeEnum = client.getEnum(982);
			if (runeEnum == null) return runeItemIds;

			int[] pouchVarbits = { 29, 1622, 1623, 14285 };
			for (int varbitId : pouchVarbits) {
				int enumVal = client.getVarbitValue(varbitId);
				if (enumVal > 0) {
					int itemId = runeEnum.getIntValue(enumVal);
					if (itemId > 0) {
						runeItemIds.add(itemId);
					}
				}
			}
		} catch (Exception e) {
			log.warn("Failed to inspect live Rune Pouch varbits", e);
		}
		return runeItemIds;
	}

	/**
	 * Return true if itemId refers to a bolt pouch
	 */
	private boolean isBoltPouchVariant(int itemId) {
		return itemId == ItemID.XBOWS_BOLT_POUCH;
	}

	/**
	 * Inspects the player's live varbits to instantly extract the ItemIDs of all bolts
	 * currently stored inside the Keldagrim Bolt Pouch.
	 */
	private List<Integer> getBoltPouchContents() {
		List<Integer> boltItemIds = new ArrayList<>();
		try {
			int[] slotVarbits = {
				VarbitID.XBOWS_POUCH_SLOT1,
				VarbitID.XBOWS_POUCH_SLOT2,
				VarbitID.XBOWS_POUCH_SLOT3,
				VarbitID.XBOWS_POUCH_SLOT4
			};
			int[] numVarbits = {
				VarbitID.XBOWS_POUCH_NUM1,
				VarbitID.XBOWS_POUCH_NUM2,
				VarbitID.XBOWS_POUCH_NUM3,
				VarbitID.XBOWS_POUCH_NUM4
			};

			for (int i = 0; i < slotVarbits.length; i++) {
				int quantity = client.getVarbitValue(numVarbits[i]);
				if (quantity > 0) {
					int itemId = client.getVarbitValue(slotVarbits[i]);
					if (itemId > 0) {
						boltItemIds.add(itemId);
					}
				}
			}
		} catch (Exception e) {
			log.warn("Failed to inspect live Bolt Pouch varbits", e);
		}
		return boltItemIds;
	}

	/**
	 * Verifies if an item ID is the closed or open variant of the Looting Bag.
	 */
	private boolean isLootingBagVariant(int itemId) {
		return itemId == ItemID.LOOTING_BAG || itemId == ItemID.LOOTING_BAG_OPEN;
	}

	/**
	 * Inspects the player's Looting Bag container and returns a de-duplicated list of base ItemIDs.
	 */
	private List<Integer> getLootingBagContents() {
		List<Integer> lootItemIds = new ArrayList<>();
		try {
			ItemContainer lootingBag = client.getItemContainer(InventoryID.LOOTING_BAG);
			if (lootingBag != null) {
				for (Item item : lootingBag.getItems()) {
					if (item != null && item.getId() > 0) {
						int baseId = item.getId();
						ItemComposition comp = itemManager.getItemComposition(baseId);

						if (comp.getNote() != -1) {
							baseId = comp.getLinkedNoteId();
						}
						if (!lootItemIds.contains(baseId)) {
							lootItemIds.add(baseId);
						}
					}
				}
			}
		} catch (Exception e) {
			log.warn("Failed to inspect Looting Bag container contents", e);
		}
		return lootItemIds;
	}
}