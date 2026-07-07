package com.slayerbanktab.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.slayerbanktab.models.SlayerSetup;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.RuneLite;

@Slf4j
@Singleton
public class SlayerSetupManager {

	private final Gson gson;
	private final File setupFile;

	private final Map<String, SlayerSetup> taskSetups = new HashMap<>();

	private final ExecutorService fileWriteExecutor = Executors.newSingleThreadExecutor();

	@Inject
	public SlayerSetupManager(Gson gson) {
		this.gson = gson;

		File dir = new File(RuneLite.RUNELITE_DIR, "slayer-bank-tabs");
		dir.mkdirs();

		this.setupFile = new File(dir, "global-setups.json");
	}

	/**
	 * Synchronously loads the setups from the local global file.
	 * Called during plugin startUp().
	 */
	public void loadSetups() {
		if (!setupFile.exists()) {
			return;
		}
		try (FileReader reader = new FileReader(setupFile)) {
			Type type = new TypeToken<Map<String, SlayerSetup>>(){}.getType();
			Map<String, SlayerSetup> loaded = gson.fromJson(reader, type);
			if (loaded != null) {
				taskSetups.clear();
				taskSetups.putAll(loaded);
				log.debug("Successfully loaded {} global slayer setups from file.", taskSetups.size());
			}
		} catch (Exception e) {
			log.error("Failed to load slayer setups from local file", e);
		}
	}

	/**
	 * Retrieves a saved setup for a specific task key.
	 */
	public SlayerSetup getSetupForTask(String setupKey) {
		if (setupKey == null || setupKey.isEmpty()) return null;
		String lower = setupKey.toLowerCase();
		return taskSetups.get(lower);
	}

	/**
	 * Caches the setup in memory and triggers a background save to the hard drive.
	 */
	public void saveSetup(String setupKey, SlayerSetup setup) {
		if (setupKey == null || setupKey.isEmpty() || setup == null) return;

		taskSetups.put(setupKey.toLowerCase(), setup);
		saveAsync();
	}

	/**
	 * Serializes the map to JSON and saves it to the local file asynchronously.
	 */
	private void saveAsync() {
		Map<String, SlayerSetup> snapshot = new HashMap<>(taskSetups);

		fileWriteExecutor.submit(() -> {
			try (FileWriter writer = new FileWriter(setupFile)) {
				gson.toJson(snapshot, writer);
			} catch (Exception e) {
				log.error("Failed to serialize and save slayer setups to file", e);
			}
		});
	}

	/**
	 * Optional utility to clear a layout if you ever add a "Delete Setup" button.
	 */
	public void deleteSetup(String setupKey) {
		if (setupKey == null || setupKey.isEmpty()) return;

		if (taskSetups.remove(setupKey.toLowerCase()) != null) {
			saveAsync();
		}
	}

	/**
	 * Returns a detached snapshot of all currently saved task setups.
	 */
	public Map<String, SlayerSetup> getAllSetups()
	{
		return new HashMap<>(taskSetups);
	}
}