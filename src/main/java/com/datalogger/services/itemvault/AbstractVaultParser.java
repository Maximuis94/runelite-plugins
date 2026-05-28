package com.datalogger.services.itemvault;

import com.datalogger.DataLoggerConfig;
import com.datalogger.events.AccountSessionStarted;
import com.datalogger.events.DataLoggerConfigChanged;
import com.datalogger.loggers.ItemVaultLogger;
import com.datalogger.models.itemvault.BankedItem;
import com.datalogger.services.AccountHashMapper;
import java.io.File;
import java.util.List;
import javax.inject.Inject;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public abstract class AbstractVaultParser implements VaultParser
{
	@Inject protected Client client;
	@Inject protected DataLoggerConfig config;
	@Inject protected AccountHashMapper accountHashMapper;
	@Inject protected ItemVaultLogger itemVaultLogger;

	protected long currentAccountHash = -1;
	protected String currentAccountName = "Unknown";
	protected File vaultFile = null;

	protected boolean isOnPermanentWorld = false;
	protected boolean hasValidAccountHash = false;
	protected boolean hasGlobalConfigEnabled = false;
	protected boolean hasItemConfigEnabled = true;

	protected boolean isEnabled = true;

	@Value
	protected static class ItemRatio
	{
		int itemId;
		int quantityPerVarbit;
	}

	protected abstract void loadSessionData(File cacheFile);

	@Subscribe
	public void onDataLoggerConfigChanged(DataLoggerConfigChanged event)
	{
		updateConfigFlags();
	}

	@Subscribe
	public void onAccountSessionStarted(AccountSessionStarted event)
	{
		isOnPermanentWorld = event.isOnRegularWorld();
		updateAccountHash(event.getAccountHash(), event.getAccountName());
		updateConfigFlags();
	}

	public void setupAccountHash()
	{
		long hash = client.getAccountHash();
		String name = client.getLocalPlayer() != null ? client.getLocalPlayer().getName() : null;
		updateAccountHash(hash, name);
	}

	protected File getInternalVaultFile()
	{
		return itemVaultLogger.getInternalVaultFile(getVaultType(), String.valueOf(currentAccountHash));
	}

	public File getInternalVaultFile(long accountHash)
	{
		return itemVaultLogger.getInternalVaultFile(getVaultType(), String.valueOf(accountHash));
	}

	protected void updateAccountHash(long accountHash, String accountName)
	{
		hasValidAccountHash = accountHash != -1;

		if (!hasValidAccountHash)
		{
			updateIsEnabled();
			return;
		}

		if (accountName != null)
			currentAccountName = accountName;

		if (currentAccountHash != accountHash)
		{
			currentAccountHash = accountHash;
			vaultFile = getInternalVaultFile();
			loadSessionData(vaultFile);
		}
		updateIsEnabled();
	}

	public void updateConfigFlags()
	{
		hasGlobalConfigEnabled = config.logItemVault();
		updateItemSpecificConfig();
		updateIsEnabled();
	}

	protected void updateItemSpecificConfig() {}

	private void updateIsEnabled()
	{
		isEnabled = hasValidAccountHash && hasGlobalConfigEnabled && hasItemConfigEnabled && isOnPermanentWorld;
	}

	protected void ensureAccountNameIsCached()
	{
		if (currentAccountName.equals("Unknown") && client.getLocalPlayer() != null)
		{
			currentAccountName = client.getLocalPlayer().getName();
		}
	}

	@Override
	public String getFilePrefix() {
		return getVaultType().name().toLowerCase().replace("_", "-");
	}

	@Override
	public List<BankedItem> parseOfflineFile(long accountHash, File vaultFile)
	{
		return itemVaultLogger.parseOfflineFile(accountHash, vaultFile, getVaultLabel());
	}

	@Override
	public String getVaultLabel() {
		return getVaultType().name();
	}

	/**
	 * Submits the parsed list of BankedItems to the ItemVaultLogger.
	 * The Logger acts as the exclusive data sink, handling all caching and disk I/O.
	 */
	protected void submitVault(List<BankedItem> items)
	{
		if (!hasValidAccountHash) return;

		ensureAccountNameIsCached();
		itemVaultLogger.logVault(currentAccountHash, currentAccountName, getVaultType(), items);
	}

	@Deprecated
	protected void saveSlimVaultCache(List<BankedItem> items)
	{
		submitVault(items);
	}
}