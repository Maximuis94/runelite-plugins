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

package com.dynamiclos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.CollisionDataFlag;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.Keybind;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

public class LineOfSightOverlay extends Overlay {

	private final Client client;
	private final DynamicLineOfSightConfig config;
	private final ModelOutlineRenderer modelOutlineRenderer;
	private final DynamicLineOfSightPlugin plugin;

	private boolean enabledNpcLos = false;
	private boolean mutualExclusivePlayerNpcLos = false;
	private boolean hotkeyAlwaysHeld = false;
	private Keybind virtualPlayerLosHotkey;

	private boolean enabledVirtualNpcLos = false;
	private Keybind virtualNpcLosHotkey;
	private int virtualNpcSize;
	private int virtualNpcMeleeRange;
	private int virtualNpcRangedRange;
	private int virtualNpcMagicRange;
	private int virtualNpcOtherRange;
	private Color virtualNpcOutlineColor;
	private Color virtualNpcFillColor;
	private float virtualNpcLineWidth;

	private WorldPoint lastVirtualNpcLocation = null;
	private final Map<CombatStyle, Set<WorldPoint>> cachedVirtualNpcTilesByStyle = new EnumMap<>(CombatStyle.class);

	private int activeWeaponRange = 0;
	private int myopiaReduction = 0;
	private boolean isAffectedByMyopia = false;
	private boolean isAffectedByMyopiaRange1 = false;
	private boolean isAffectedByMyopiaRange2 = false;
	private boolean isAffectedByMyopiaRange3 = false;
	private boolean isAffectedByMyopiaRange4 = false;
	private boolean isAffectedByMyopiaRange5 = false;

	private boolean enabledAnyNpcHighlight = false;
	private boolean hasAlteredAnyFixedRange = false;

	private boolean enabledActiveWeaponLos;
	private Color activeWeaponOutlineColor;
	private Color activeWeaponFillColor;
	private float activeRangeLineWidth;
	private boolean enabledHighlightActiveWeaponNpc;

	private boolean enabledMaxRangeLos;
	private Color maxRangeOutlineColor;
	private Color maxRangeFillColor;
	private float maxRangeLineWidth;
	private boolean enabledHighlightMaxRangeNpc;

	private boolean enabledFixed1, enabledFixed1NpcHighlight;
	private int fixed1Range;
	private Color fixed1OutlineColor, fixed1FillColor;
	private float fixed1LineWidth;

	private boolean enabledFixed2, enabledFixed2NpcHighlight;
	private int fixed2Range;
	private Color fixed2OutlineColor, fixed2FillColor;
	private float fixed2LineWidth;

	private boolean enabledFixed3, enabledFixed3NpcHighlight;
	private int fixed3Range;
	private Color fixed3OutlineColor, fixed3FillColor;
	private float fixed3LineWidth;

	private boolean enabledFixed4, enabledFixed4NpcHighlight;
	private int fixed4Range;
	private Color fixed4OutlineColor, fixed4FillColor;
	private float fixed4LineWidth;

	private boolean enabledFixed5, enabledFixed5NpcHighlight;
	private int fixed5Range;
	private Color fixed5OutlineColor, fixed5FillColor;
	private float fixed5LineWidth;

	private Color meleeNpcOutlineColor, meleeNpcFillColor;
	private float meleeNpcLineWidth;

	private Color rangedNpcOutlineColor, rangedNpcFillColor;
	private float rangedNpcLineWidth;

	private Color magicNpcOutlineColor, magicNpcFillColor;
	private float magicNpcLineWidth;

	private Color otherNpcOutlineColor, otherNpcFillColor;
	private float otherNpcLineWidth;

	private WorldPoint lastPlayerLocation = null;
	private int lastCalculatedActiveRange = -1;
	private int lastCalculatedFixed1Range = -1;
	private int lastCalculatedFixed2Range = -1;
	private int lastCalculatedFixed3Range = -1;
	private int lastCalculatedFixed4Range = -1;
	private int lastCalculatedFixed5Range = -1;

	private final Set<WorldPoint> cachedActiveWeaponTiles = new HashSet<>();
	private final Set<WorldPoint> cachedMaxRangeTiles = new HashSet<>();
	private final Set<WorldPoint> cachedFixedRange1Tiles = new HashSet<>();
	private final Set<WorldPoint> cachedFixedRange2Tiles = new HashSet<>();
	private final Set<WorldPoint> cachedFixedRange3Tiles = new HashSet<>();
	private final Set<WorldPoint> cachedFixedRange4Tiles = new HashSet<>();
	private final Set<WorldPoint> cachedFixedRange5Tiles = new HashSet<>();

	private NPC lastHoveredNpc = null;
	private WorldPoint lastHoveredNpcLocation = null;

	private static class NpcLosDef {
		int range;
		CombatStyle style;
		boolean diagonalMelee;

		NpcLosDef(int range, CombatStyle style, boolean diagonalMelee) {
			this.range = range;
			this.style = style;
			this.diagonalMelee = diagonalMelee;
		}
	}

	private final Map<String, List<NpcLosDef>> npcLosDefinitionsByName = new HashMap<>();
	private final Map<Integer, List<NpcLosDef>> npcLosDefinitionsById = new HashMap<>();
	private final Map<CombatStyle, Set<WorldPoint>> cachedHoveredNpcTilesByStyle = new EnumMap<>(CombatStyle.class);

	@Inject
	public LineOfSightOverlay(Client client, DynamicLineOfSightConfig config, ModelOutlineRenderer modelOutlineRenderer, DynamicLineOfSightPlugin plugin) {
		this.client = client;
		this.config = config;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.plugin = plugin;
		parseConfigs();
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	/**
	 * Updates the attackRange of the currently equipped weapon and whether it was affected by Myopia or not
	 */
	public void setActiveAttackRange(int attackRange, boolean affectedByMyopia) {
		activeWeaponRange = attackRange;
		isAffectedByMyopia = affectedByMyopia;
	}

	/**
	 * Sets the given attack range reduction that follows from the active Myopia tier and subsequently updates all
	 * affected attack ranges.
	 */
	public void setMyopiaReduction(int myopiaReduction) {
		this.myopiaReduction = myopiaReduction;

		int range1 = config.fixedRange1Distance();
		fixed1Range = Math.max(1, range1 - myopiaReduction);
		isAffectedByMyopiaRange1 = fixed1Range < range1;

		int range2 = config.fixedRange2Distance();
		fixed2Range = Math.max(1, range2 - myopiaReduction);
		isAffectedByMyopiaRange2 = fixed2Range < range2;

		int range3 = config.fixedRange3Distance();
		fixed3Range = Math.max(1, range3 - myopiaReduction);
		isAffectedByMyopiaRange3 = fixed3Range < range3;

		int range4 = config.fixedRange4Distance();
		fixed4Range = Math.max(1, range4 - myopiaReduction);
		isAffectedByMyopiaRange4 = fixed4Range < range4;

		int range5 = config.fixedRange5Distance();
		fixed5Range = Math.max(1, range5 - myopiaReduction);
		isAffectedByMyopiaRange5 = fixed5Range < range5;

		updateHasAlteredAnyFixedRange();
	}

	public void parseConfigs() {
		enabledNpcLos = config.enableNpcLos();
		Keybind key = config.npcLosHotkey();
		hotkeyAlwaysHeld = key == null || key.equals(Keybind.NOT_SET);
		virtualPlayerLosHotkey = config.virtualPlayerLosHotkey();

		enabledActiveWeaponLos = config.drawActiveWeaponRange();
		activeWeaponOutlineColor = config.activeWeaponOutlineColor();
		activeWeaponFillColor = config.activeWeaponFillColor();
		activeRangeLineWidth = (float) config.activeWeaponLineWidth();
		enabledHighlightActiveWeaponNpc = config.highlightAttackableEnemies();

		enabledMaxRangeLos = config.drawMaxAttackRange();
		mutualExclusivePlayerNpcLos = config.mutualExclusivePlayerNpcLos();
		maxRangeOutlineColor = config.maxRangeOutlineColor();
		maxRangeFillColor = config.maxRangeFillColor();
		maxRangeLineWidth = (float) config.maxRangeLineWidth();
		enabledHighlightMaxRangeNpc = config.highlightEnemiesWithinMaxRange();

		enabledFixed1 = config.drawFixedRange1();
		enabledFixed1NpcHighlight = config.highlightEnemiesWithinFixedRange1();
		fixed1Range = Math.max(1, config.fixedRange1Distance() - myopiaReduction);
		fixed1OutlineColor = config.fixedRange1OutlineColor();
		fixed1FillColor = config.fixedRange1FillColor();
		fixed1LineWidth = (float) config.fixedRange1LineWidth();

		enabledFixed2 = config.drawFixedRange2();
		enabledFixed2NpcHighlight = config.highlightEnemiesWithinFixedRange2();
		fixed2Range = Math.max(1, config.fixedRange2Distance() - myopiaReduction);
		fixed2OutlineColor = config.fixedRange2OutlineColor();
		fixed2FillColor = config.fixedRange2FillColor();
		fixed2LineWidth = (float) config.fixedRange2LineWidth();

		enabledFixed3 = config.drawFixedRange3();
		enabledFixed3NpcHighlight = config.highlightEnemiesWithinFixedRange3();
		fixed3Range = Math.max(1, config.fixedRange3Distance() - myopiaReduction);
		fixed3OutlineColor = config.fixedRange3OutlineColor();
		fixed3FillColor = config.fixedRange3FillColor();
		fixed3LineWidth = (float) config.fixedRange3LineWidth();

		enabledFixed4 = config.drawFixedRange4();
		enabledFixed4NpcHighlight = config.highlightEnemiesWithinFixedRange4();
		fixed4Range = Math.max(1, config.fixedRange4Distance() - myopiaReduction);
		fixed4OutlineColor = config.fixedRange4OutlineColor();
		fixed4FillColor = config.fixedRange4FillColor();
		fixed4LineWidth = (float) config.fixedRange4LineWidth();

		enabledFixed5 = config.drawFixedRange5();
		enabledFixed5NpcHighlight = config.highlightEnemiesWithinFixedRange5();
		fixed5Range = Math.max(1, config.fixedRange5Distance() - myopiaReduction);
		fixed5OutlineColor = config.fixedRange5OutlineColor();
		fixed5FillColor = config.fixedRange5FillColor();
		fixed5LineWidth = (float) config.fixedRange5LineWidth();

		enabledAnyNpcHighlight = enabledHighlightActiveWeaponNpc || enabledHighlightMaxRangeNpc ||
			enabledFixed1NpcHighlight || enabledFixed2NpcHighlight ||
			enabledFixed3NpcHighlight || enabledFixed4NpcHighlight ||
			enabledFixed5NpcHighlight;

		meleeNpcOutlineColor = config.meleeNpcOutlineColor();
		meleeNpcFillColor = config.meleeNpcFillColor();
		meleeNpcLineWidth = (float) config.meleeNpcLineWidth();

		rangedNpcOutlineColor = config.rangedNpcOutlineColor();
		rangedNpcFillColor = config.rangedNpcFillColor();
		rangedNpcLineWidth = (float) config.rangedNpcLineWidth();

		magicNpcOutlineColor = config.magicNpcOutlineColor();
		magicNpcFillColor = config.magicNpcFillColor();
		magicNpcLineWidth = (float) config.magicNpcLineWidth();

		otherNpcOutlineColor = config.otherNpcOutlineColor();
		otherNpcFillColor = config.otherNpcFillColor();
		otherNpcLineWidth = (float) config.otherNpcLineWidth();

		enabledVirtualNpcLos = config.enableVirtualNpcLos();
		virtualNpcLosHotkey = config.virtualNpcLosHotkey();
		virtualNpcSize = Math.max(1, config.virtualNpcSize());
		virtualNpcMeleeRange = config.virtualNpcMeleeRange();
		virtualNpcRangedRange = config.virtualNpcRangedRange();
		virtualNpcMagicRange = config.virtualNpcMagicRange();
		virtualNpcOtherRange = config.virtualNpcOtherRange();
		virtualNpcOutlineColor = config.virtualNpcOutlineColor();
		virtualNpcFillColor = config.virtualNpcFillColor();
		virtualNpcLineWidth = (float) config.virtualNpcLineWidth();
		lastVirtualNpcLocation = null;

		if (myopiaReduction > 0)
			setMyopiaReduction(myopiaReduction);

		updateHasAlteredAnyFixedRange();

		parseNpcDefinitions();
	}

	private void parseNpcDefinitions() {
		npcLosDefinitionsByName.clear();
		npcLosDefinitionsById.clear();

		parseNpcConfigString(config.meleeNpcDefs(), CombatStyle.MELEE);
		parseNpcConfigString(config.rangedNpcDefs(), CombatStyle.RANGED);
		parseNpcConfigString(config.magicNpcDefs(), CombatStyle.MAGIC);
		parseNpcConfigString(config.otherNpcDefs(), CombatStyle.OTHER);
	}

	private void parseNpcConfigString(String configValue, CombatStyle style) {
		if (configValue == null || configValue.trim().isEmpty()) return;

		String normalizedConfig = configValue.replace("\r\n", "\n").replace(",", "\n");
		String[] entries = normalizedConfig.split("\n");

		for (String entry : entries) {
			if (entry.trim().isEmpty()) continue;

			String[] parts = entry.split("\\|");
			if (parts.length != 2) continue;

			String identifier = parts[0].trim().toLowerCase();
			try {
				String rangeStr = parts[1].trim();
				boolean diagonalMelee = false;

				if (rangeStr.endsWith("*")) {
					diagonalMelee = true;
					rangeStr = rangeStr.substring(0, rangeStr.length() - 1);
				}

				int range = Integer.parseInt(rangeStr);
				NpcLosDef def = new NpcLosDef(range, style, diagonalMelee);

				try {
					int id = Integer.parseInt(identifier);
					npcLosDefinitionsById.computeIfAbsent(id, k -> new ArrayList<>()).add(def);
				} catch (NumberFormatException e) {
					npcLosDefinitionsByName.computeIfAbsent(identifier, k -> new ArrayList<>()).add(def);
				}
			} catch (NumberFormatException e) { }
		}
	}

	/**
	 * Updates the hasAlteredAnyFixedRange flag
	 */
	private void updateHasAlteredAnyFixedRange() {
		hasAlteredAnyFixedRange = fixed1Range != lastCalculatedFixed1Range ||
			fixed2Range != lastCalculatedFixed2Range ||
			fixed3Range != lastCalculatedFixed3Range ||
			fixed4Range != lastCalculatedFixed4Range ||
			fixed5Range != lastCalculatedFixed5Range;
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		Player player = client.getLocalPlayer();

		if (player == null || player.getWorldArea() == null) {
			return null;
		}

		boolean drawNpcLos = enabledNpcLos && !plugin.isNpcLosToggledOff() && (hotkeyAlwaysHeld || plugin.isHotkeyHeld());

		WorldView wv = client.getTopLevelWorldView();
		WorldArea playerArea = player.getWorldArea();
		WorldPoint currentLocation = player.getWorldLocation();

		boolean mutuallyExcludedPlayerLos = false;
		if (drawNpcLos)
		{
			drawHoveredNpcLos(graphics, wv);
			if (mutualExclusivePlayerNpcLos && lastHoveredNpc != null) {
				mutuallyExcludedPlayerLos = true;
			}
		}

		boolean isVirtualKeyValid = virtualPlayerLosHotkey != null && !virtualPlayerLosHotkey.equals(Keybind.NOT_SET);
		if (isVirtualKeyValid && plugin.isVirtualPlayerLosHotkeyHeld()) {
			Tile hoveredTile = wv.getSelectedSceneTile();

			if (hoveredTile != null && hoveredTile.getWorldLocation() != null) {
				currentLocation = hoveredTile.getWorldLocation();
				playerArea = new WorldArea(currentLocation, 1, 1);
			}
		}

		boolean isVirtualNpcKeyValid = enabledVirtualNpcLos && virtualNpcLosHotkey != null && !virtualNpcLosHotkey.equals(Keybind.NOT_SET);
		if (isVirtualNpcKeyValid && plugin.isVirtualNpcLosHotkeyHeld()) {
			Tile hoveredTile = wv.getSelectedSceneTile();

			if (hoveredTile != null && hoveredTile.getWorldLocation() != null) {
				drawVirtualNpcLos(graphics, wv, hoveredTile.getWorldLocation());

				if (mutualExclusivePlayerNpcLos) {
					mutuallyExcludedPlayerLos = true;
				}
			}
		}

		if (!currentLocation.equals(lastPlayerLocation) ||
			activeWeaponRange != lastCalculatedActiveRange ||
			hasAlteredAnyFixedRange) {

			cachedActiveWeaponTiles.clear();
			cachedMaxRangeTiles.clear();
			cachedFixedRange1Tiles.clear();
			cachedFixedRange2Tiles.clear();
			cachedFixedRange3Tiles.clear();
			cachedFixedRange4Tiles.clear();
			cachedFixedRange5Tiles.clear();

			if (enabledActiveWeaponLos) {
				cachedActiveWeaponTiles.addAll(calculateLineOfSightTiles(playerArea, wv, activeWeaponRange, false, isAffectedByMyopia, false));
			}

			if (enabledMaxRangeLos) {
				cachedMaxRangeTiles.addAll(calculateLineOfSightTiles(playerArea, wv, 10, false, false, false));
			}

			if (enabledFixed1) {
				cachedFixedRange1Tiles.addAll(calculateLineOfSightTiles(playerArea, wv, fixed1Range, false, isAffectedByMyopiaRange1, false));
			}
			if (enabledFixed2) {
				cachedFixedRange2Tiles.addAll(calculateLineOfSightTiles(playerArea, wv, fixed2Range, false, isAffectedByMyopiaRange2, false));
			}
			if (enabledFixed3) {
				cachedFixedRange3Tiles.addAll(calculateLineOfSightTiles(playerArea, wv, fixed3Range, false, isAffectedByMyopiaRange3, false));
			}
			if (enabledFixed4) {
				cachedFixedRange4Tiles.addAll(calculateLineOfSightTiles(playerArea, wv, fixed4Range, false, isAffectedByMyopiaRange4, false));
			}
			if (enabledFixed5) {
				cachedFixedRange5Tiles.addAll(calculateLineOfSightTiles(playerArea, wv, fixed5Range, false, isAffectedByMyopiaRange5, false));
			}

			lastPlayerLocation = currentLocation;
			lastCalculatedActiveRange = activeWeaponRange;
			lastCalculatedFixed1Range = fixed1Range;
			lastCalculatedFixed2Range = fixed2Range;
			lastCalculatedFixed3Range = fixed3Range;
			lastCalculatedFixed4Range = fixed4Range;
			lastCalculatedFixed5Range = fixed5Range;

			hasAlteredAnyFixedRange = false;
		}

		if (!plugin.isPlayerLosToggledOff())
		{
			if (!mutuallyExcludedPlayerLos)
			{
				if (enabledActiveWeaponLos)
				{
					drawTilesArea(graphics, cachedActiveWeaponTiles, activeWeaponFillColor, activeWeaponOutlineColor, activeRangeLineWidth);
				}

				if (enabledMaxRangeLos)
				{
					drawTilesArea(graphics, cachedMaxRangeTiles, maxRangeFillColor, maxRangeOutlineColor, maxRangeLineWidth);
				}
				if (enabledFixed1)
				{
					drawTilesArea(graphics, cachedFixedRange1Tiles, fixed1FillColor, fixed1OutlineColor, fixed1LineWidth);
				}
				if (enabledFixed2)
				{
					drawTilesArea(graphics, cachedFixedRange2Tiles, fixed2FillColor, fixed2OutlineColor, fixed2LineWidth);
				}
				if (enabledFixed3)
				{
					drawTilesArea(graphics, cachedFixedRange3Tiles, fixed3FillColor, fixed3OutlineColor, fixed3LineWidth);
				}
				if (enabledFixed4)
				{
					drawTilesArea(graphics, cachedFixedRange4Tiles, fixed4FillColor, fixed4OutlineColor, fixed4LineWidth);
				}
				if (enabledFixed5)
				{
					drawTilesArea(graphics, cachedFixedRange5Tiles, fixed5FillColor, fixed5OutlineColor, fixed5LineWidth);
				}
			}

			if (enabledAnyNpcHighlight)
			{
				highlightVisibleEnemies(wv, playerArea);
			}
		}

		return null;
	}

	/**
	 * Checks the user's cursor to see if they are hovering over a supported NPC,
	 * and draws its line of sight if they are.
	 */
	private void drawHoveredNpcLos(Graphics2D graphics, WorldView wv) {
		MenuEntry[] menuEntries = client.getMenu().getMenuEntries();
		if (menuEntries == null || menuEntries.length == 0) {
			lastHoveredNpc = null;
			return;
		}

		MenuEntry topEntry = menuEntries[menuEntries.length - 1];
		NPC hoveredNpc = topEntry.getNpc();

		if (hoveredNpc != null && hoveredNpc.getWorldArea() != null) {
			int npcId = hoveredNpc.getId();
			String npcName = hoveredNpc.getName();

			List<NpcLosDef> defs = npcLosDefinitionsById.get(npcId);
			if (defs == null && npcName != null) {
				defs = npcLosDefinitionsByName.get(npcName.toLowerCase());
			}

			if (defs == null || defs.isEmpty()) return;

			WorldPoint currentNpcLocation = hoveredNpc.getWorldLocation();

			if (hoveredNpc != lastHoveredNpc || !currentNpcLocation.equals(lastHoveredNpcLocation)) {
				cachedHoveredNpcTilesByStyle.clear();

				for (NpcLosDef def : defs) {
					if (def.range >= 0) {
						Set<WorldPoint> tiles = calculateLineOfSightTiles(hoveredNpc.getWorldArea(), wv, def.range, true, false, def.diagonalMelee);
						cachedHoveredNpcTilesByStyle.computeIfAbsent(def.style, k -> new HashSet<>()).addAll(tiles);
					}
				}

				lastHoveredNpc = hoveredNpc;
				lastHoveredNpcLocation = currentNpcLocation;
			}

			for (Map.Entry<CombatStyle, Set<WorldPoint>> entry : cachedHoveredNpcTilesByStyle.entrySet()) {
				CombatStyle style = entry.getKey();
				Color outlineColor = getOutlineColorForStyle(style);
				Color fillColor = getFillColorForStyle(style);
				float lineWidth = getLineWidthForStyle(style);
				drawTilesArea(graphics, entry.getValue(), fillColor, outlineColor, lineWidth);
			}
		} else {
			lastHoveredNpc = null;
		}
	}

	private void drawVirtualNpcLos(Graphics2D graphics, WorldView wv, WorldPoint cursorLocation) {
		WorldArea virtualNpcArea = new WorldArea(cursorLocation, virtualNpcSize, virtualNpcSize);

		if (!cursorLocation.equals(lastVirtualNpcLocation)) {
			cachedVirtualNpcTilesByStyle.clear();

			if (virtualNpcMeleeRange > 0) {
				cachedVirtualNpcTilesByStyle.put(CombatStyle.MELEE, calculateLineOfSightTiles(virtualNpcArea, wv, virtualNpcMeleeRange, true, false, false));
			}
			if (virtualNpcRangedRange > 0) {
				cachedVirtualNpcTilesByStyle.put(CombatStyle.RANGED, calculateLineOfSightTiles(virtualNpcArea, wv, virtualNpcRangedRange, true, false, false));
			}
			if (virtualNpcMagicRange > 0) {
				cachedVirtualNpcTilesByStyle.put(CombatStyle.MAGIC, calculateLineOfSightTiles(virtualNpcArea, wv, virtualNpcMagicRange, true, false, false));
			}
			if (virtualNpcOtherRange > 0) {
				cachedVirtualNpcTilesByStyle.put(CombatStyle.OTHER, calculateLineOfSightTiles(virtualNpcArea, wv, virtualNpcOtherRange, true, false, false));
			}

			lastVirtualNpcLocation = cursorLocation;
		}

		for (Map.Entry<CombatStyle, Set<WorldPoint>> entry : cachedVirtualNpcTilesByStyle.entrySet()) {
			CombatStyle style = entry.getKey();
			Color outlineColor = getOutlineColorForStyle(style);
			Color fillColor = getFillColorForStyle(style);
			float lineWidth = getLineWidthForStyle(style);
			drawTilesArea(graphics, entry.getValue(), fillColor, outlineColor, lineWidth);
		}

		Set<WorldPoint> footprintTiles = new HashSet<>();
		int z = cursorLocation.getPlane();
		for (int x = 0; x < virtualNpcSize; x++) {
			for (int y = 0; y < virtualNpcSize; y++) {
				footprintTiles.add(new WorldPoint(cursorLocation.getX() + x, cursorLocation.getY() + y, z));
			}
		}
		drawTilesArea(graphics, footprintTiles, virtualNpcFillColor, virtualNpcOutlineColor, virtualNpcLineWidth);
	}

	/**
	 * Return the outline color configured for the given CombatStyle
	 */
	private Color getOutlineColorForStyle(CombatStyle style) {
		switch (style) {
			case MELEE: return meleeNpcOutlineColor;
			case RANGED: return rangedNpcOutlineColor;
			case MAGIC: return magicNpcOutlineColor;
			case OTHER: return otherNpcOutlineColor;
			default: return null;
		}
	}

	/**
	 * Return the fill color configured for the given CombatStyle
	 */
	private Color getFillColorForStyle(CombatStyle style) {
		switch (style) {
			case MELEE: return meleeNpcFillColor;
			case RANGED: return rangedNpcFillColor;
			case MAGIC: return magicNpcFillColor;
			case OTHER: return otherNpcFillColor;
			default: return null;
		}
	}

	/**
	 * Return the line width configured for the given CombatStyle
	 */
	private float getLineWidthForStyle(CombatStyle style) {
		switch (style) {
			case MELEE: return meleeNpcLineWidth;
			case RANGED: return rangedNpcLineWidth;
			case MAGIC: return magicNpcLineWidth;
			case OTHER: return otherNpcLineWidth;
			default: return 1.0f;
		}
	}

	/**
	 * Scans the environment and outlines valid attackable NPCs, prioritizing the shortest applicable attack range.
	 */
	private void highlightVisibleEnemies(WorldView wv, WorldArea playerArea) {
		int maxEnabledRange = 0;
		if (enabledHighlightActiveWeaponNpc) maxEnabledRange = Math.max(maxEnabledRange, activeWeaponRange);
		if (enabledHighlightMaxRangeNpc) maxEnabledRange = Math.max(maxEnabledRange, 10);
		if (enabledFixed1NpcHighlight) maxEnabledRange = Math.max(maxEnabledRange, fixed1Range);
		if (enabledFixed2NpcHighlight) maxEnabledRange = Math.max(maxEnabledRange, fixed2Range);
		if (enabledFixed3NpcHighlight) maxEnabledRange = Math.max(maxEnabledRange, fixed3Range);
		if (enabledFixed4NpcHighlight) maxEnabledRange = Math.max(maxEnabledRange, fixed4Range);
		if (enabledFixed5NpcHighlight) maxEnabledRange = Math.max(maxEnabledRange, fixed5Range);

		if (maxEnabledRange == 0) return;

		for (NPC npc : wv.npcs()) {
			if (npc == null || npc.isDead() || npc.getCombatLevel() == 0 || npc.getWorldArea() == null) {
				continue;
			}

			WorldArea npcArea = npc.getWorldArea();
			int distance = playerArea.distanceTo(npcArea);

			if (distance > maxEnabledRange) {
				continue;
			}

			boolean inActiveRange = (activeWeaponRange == 1) ? playerArea.isInMeleeDistance(npcArea) : (distance <= activeWeaponRange);
			boolean inFixed1Range = (fixed1Range == 1) ? playerArea.isInMeleeDistance(npcArea) : (distance <= fixed1Range);
			boolean inFixed2Range = (fixed2Range == 1) ? playerArea.isInMeleeDistance(npcArea) : (distance <= fixed2Range);
			boolean inFixed3Range = (fixed3Range == 1) ? playerArea.isInMeleeDistance(npcArea) : (distance <= fixed3Range);
			boolean inFixed4Range = (fixed4Range == 1) ? playerArea.isInMeleeDistance(npcArea) : (distance <= fixed4Range);
			boolean inFixed5Range = (fixed5Range == 1) ? playerArea.isInMeleeDistance(npcArea) : (distance <= fixed5Range);

			Color enemyHighlightColor = null;
			float strokeWidth = 0f;
			int bestRange = Integer.MAX_VALUE;

			if (enabledHighlightMaxRangeNpc && distance <= 10) {
				enemyHighlightColor = maxRangeOutlineColor;
				strokeWidth = maxRangeLineWidth;
				bestRange = 10;
			}

			if (enabledHighlightActiveWeaponNpc && inActiveRange && activeWeaponRange < bestRange) {
				enemyHighlightColor = activeWeaponOutlineColor;
				strokeWidth = activeRangeLineWidth;
				bestRange = activeWeaponRange;
			}

			if (enabledFixed1NpcHighlight && inFixed1Range && fixed1Range < bestRange) {
				enemyHighlightColor = fixed1OutlineColor;
				strokeWidth = fixed1LineWidth;
				bestRange = fixed1Range;
			}

			if (enabledFixed2NpcHighlight && inFixed2Range && fixed2Range < bestRange) {
				enemyHighlightColor = fixed2OutlineColor;
				strokeWidth = fixed2LineWidth;
				bestRange = fixed2Range;
			}

			if (enabledFixed3NpcHighlight && inFixed3Range && fixed3Range < bestRange) {
				enemyHighlightColor = fixed3OutlineColor;
				strokeWidth = fixed3LineWidth;
				bestRange = fixed3Range;
			}

			if (enabledFixed4NpcHighlight && inFixed4Range && fixed4Range < bestRange) {
				enemyHighlightColor = fixed4OutlineColor;
				strokeWidth = fixed4LineWidth;
				bestRange = fixed4Range;
			}

			if (enabledFixed5NpcHighlight && inFixed5Range && fixed5Range < bestRange) {
				enemyHighlightColor = fixed5OutlineColor;
				strokeWidth = fixed5LineWidth;
				bestRange = fixed5Range;
			}

			if (enemyHighlightColor != null && playerArea.hasLineOfSightTo(wv, npcArea)) {
				modelOutlineRenderer.drawOutline(npc, Math.max(1, Math.round(strokeWidth)), enemyHighlightColor, 0);
			}
		}
	}

	/**
	 * Renders a set of WorldPoints to the screen efficiently using edge-detection.
	 * Bypasses the highly unoptimized java.awt.geom.Area CSG calculations.
	 */
	private void drawTilesArea(Graphics2D graphics, Set<WorldPoint> tiles, Color fill, Color outline, float strokeWidth) {
		if (tiles.isEmpty()) return;

		for (WorldPoint wp : tiles) {
			LocalPoint lp = LocalPoint.fromWorld(client, wp);
			if (lp == null) continue;

			Polygon poly = Perspective.getCanvasTilePoly(client, lp);
			if (poly == null) continue;

			if (fill != null) {
				graphics.setColor(fill);
				graphics.fill(poly);
			}

			if (outline != null) {
				graphics.setColor(outline);
				graphics.setStroke(new BasicStroke(strokeWidth));

				if (!tiles.contains(wp.dy(-1))) {
					graphics.drawLine(poly.xpoints[0], poly.ypoints[0], poly.xpoints[1], poly.ypoints[1]);
				}

				if (!tiles.contains(wp.dx(1))) {
					graphics.drawLine(poly.xpoints[1], poly.ypoints[1], poly.xpoints[2], poly.ypoints[2]);
				}

				if (!tiles.contains(wp.dy(1))) {
					graphics.drawLine(poly.xpoints[2], poly.ypoints[2], poly.xpoints[3], poly.ypoints[3]);
				}

				if (!tiles.contains(wp.dx(-1))) {
					graphics.drawLine(poly.xpoints[3], poly.ypoints[3], poly.xpoints[0], poly.ypoints[0]);
				}
			}
		}
	}

	/**
	 * Calculates the line of sight area dynamically based on the size of the source entity
	 * and returns the valid tiles.
	 */
	private Set<WorldPoint> calculateLineOfSightTiles(WorldArea sourceArea, WorldView wv, int range, boolean isNpc, boolean isAffectedByMyopia, boolean diagonalMelee) {
		Set<WorldPoint> tiles = new HashSet<>();
		int z = sourceArea.getPlane();

		int minX = sourceArea.getX() - range;
		int maxX = sourceArea.getX() + sourceArea.getWidth() - 1 + range;
		int minY = sourceArea.getY() - range;
		int maxY = sourceArea.getY() + sourceArea.getHeight() - 1 + range;

		CollisionData[] collisionData = wv.getCollisionMaps();
		int[][] collisionFlags = (collisionData != null && collisionData[z] != null) ? collisionData[z].getFlags() : null;

		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				if (collisionFlags != null) {
					int sceneX = x - wv.getBaseX();
					int sceneY = y - wv.getBaseY();

					if (sceneX >= 0 && sceneX < 104 && sceneY >= 0 && sceneY < 104) {
						int flag = collisionFlags[sceneX][sceneY];
						if ((flag & (CollisionDataFlag.BLOCK_MOVEMENT_OBJECT | CollisionDataFlag.BLOCK_MOVEMENT_FLOOR)) != 0) {
							continue;
						}
					}
				}

				WorldPoint targetPoint = new WorldPoint(x, y, z);
				WorldArea targetTile = new WorldArea(targetPoint, 1, 1);

				boolean inRange;
				if (range == 1 && !isAffectedByMyopia && !diagonalMelee) {
					inRange = sourceArea.isInMeleeDistance(targetTile);
				} else {
					inRange = sourceArea.distanceTo(targetTile) <= range;
				}

				if (inRange) {
					boolean hasLos;

					if (isNpc) {
						int nearestX = Math.max(sourceArea.getX(), Math.min(targetPoint.getX(), sourceArea.getX() + sourceArea.getWidth() - 1));
						int nearestY = Math.max(sourceArea.getY(), Math.min(targetPoint.getY(), sourceArea.getY() + sourceArea.getHeight() - 1));
						WorldArea nearestSourceTile = new WorldArea(nearestX, nearestY, 1, 1, z);

						hasLos = targetTile.hasLineOfSightTo(wv, nearestSourceTile);
					} else {
						hasLos = sourceArea.hasLineOfSightTo(wv, targetTile);
					}

					if (hasLos) {
						tiles.add(targetPoint);
					}
				}
			}
		}
		return tiles;
	}
}