package com.slayerbanktab.models;

import com.slayerbanktab.PluginConstants;
import com.slayerbanktab.SlayerBankTabConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;

@Slf4j
@Singleton
public class ItemMapping
{
	private static final Map<Integer, Integer> CANONICAL_OVERRIDES = new HashMap<>();

	private final Map<Integer, Set<Integer>> customMappings = new HashMap<>();
	private final Map<String, Integer> nameToIdCache = new HashMap<>();

	@Inject private ItemManager itemManager;
	@Inject private ClientThread clientThread;
	@Inject private SlayerBankTabConfig config;

	public int canonicalize(int itemId) {
		return CANONICAL_OVERRIDES.getOrDefault(itemId, ItemVariationMapping.map(itemId));
	}

	public Set<Integer> getAuxiliaryItems(int primaryItemId) {
		Set<Integer> result = new LinkedHashSet<>();

		Set<Integer> customAux = customMappings.get(primaryItemId);
		if (customAux != null) {
			result.addAll(customAux);
		}

		return result;
	}

	/**
	 * Reload custom item mappings from the configurations
	 */
	public void reloadCustomMappings() {
		clientThread.invokeLater(() -> {
			customMappings.clear();
			nameToIdCache.clear();

			String rawConfig = config.customAdditionalItemMappings();
			if (rawConfig == null || rawConfig.trim().isEmpty()) {
				return;
			}

			for (String line : rawConfig.split("\\r?\\n")) {
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) continue;

				// --- TWO-WAY MAPPING (Pipe Separated) ---
				if (trimmed.contains("|")) {
					String[] parts = trimmed.split("\\|");
					List<Integer> resolvedIds = new ArrayList<>();

					for (String part : parts) {
						if (part.trim().isEmpty()) continue;
						int id = resolveToken(part.trim());
						if (id > 0) {
							resolvedIds.add(id);
						}
					}

					for (int i = 0; i < resolvedIds.size(); i++) {
						int primaryId = resolvedIds.get(i);
						Set<Integer> auxSet = customMappings.computeIfAbsent(primaryId, k -> new LinkedHashSet<>());
						for (int j = 0; j < resolvedIds.size(); j++) {
							if (i != j) {
								auxSet.add(resolvedIds.get(j));
							}
						}
					}
				}

				// --- ONE-WAY MAPPING (Comma Separated) ---
				else {
					String[] parts = trimmed.split(",");
					if (parts.length < 2) continue;

					int primaryId = resolveToken(parts[0].trim());
					if (primaryId <= 0) continue;

					Set<Integer> auxSet = customMappings.computeIfAbsent(primaryId, k -> new LinkedHashSet<>());

					for (int i = 1; i < parts.length; i++) {
						String auxToken = parts[i].trim();
						if (auxToken.isEmpty()) continue;

						int auxId = resolveToken(auxToken);
						if (auxId > 0) {
							auxSet.add(auxId);
						}
					}
				}
			}
		});
	}

	private int resolveToken(String token) {
		try {
			int id = Integer.parseInt(token);
			if (id > 0 && id <= PluginConstants.MAX_ITEM_ID) {
				return id;
			}
		} catch (NumberFormatException ignored) {}

		String lowerToken = token.toLowerCase();
		if (nameToIdCache.containsKey(lowerToken)) {
			return nameToIdCache.get(lowerToken);
		}

		for (int i = 0; i <= PluginConstants.MAX_ITEM_ID; i++) {
			ItemComposition comp = itemManager.getItemComposition(i);
			if (comp.getName() != null && !comp.getName().equals("null")) {
				if (comp.getNote() != -1 || comp.getPlaceholderTemplateId() != -1) {
					continue;
				}
				if (comp.getName().equalsIgnoreCase(token)) {
					nameToIdCache.put(lowerToken, comp.getId());
					return comp.getId();
				}
			}
		}

		nameToIdCache.put(lowerToken, -1);
		return -1;
	}
}