package com.venatorpathfinder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

@Slf4j
public class VenatorPathFinderOverlay extends Overlay
{
	private final Client client;
	private final VenatorPathFinderConfig config;
	private final ModelOutlineRenderer modelOutlineRenderer;

	private boolean hasEquippedVenatorBow = false;
	private boolean ignoreVenatorBowCondition = false;
	private boolean isInMultiCombat = false;
	private boolean ignoreMultiCombatCondition = false;
	private boolean shouldRenderPath = false;
	private boolean drawOnlyOnePath = false;
	private boolean limitPathSize = false;

	private Color initialTargetOutline;
	private Color firstBounceOutline;
	private Color secondBounceOutline;
	private Color boomerangBounceOutline;

	private int outlineWidth;
	private int outlineFeather;

	@Inject
	private VenatorPathFinderOverlay(Client client, VenatorPathFinderConfig config, ModelOutlineRenderer modelOutlineRenderer)
	{
		this.client = client;
		this.config = config;
		this.modelOutlineRenderer = modelOutlineRenderer;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		parseConfigs();
	}

	/**
	 * Updates multicombat status, if applicable
	 */
	public void setInMultiCombat(boolean inMultiCombat)
	{
		isInMultiCombat = inMultiCombat;
		if (!ignoreMultiCombatCondition) updateShouldRenderPath();
	}

	/**
	 * Updates Venator bow status, if applicable
	 */
	public void setHasEquippedVenatorBow(boolean equippedVenatorBow)
	{
		hasEquippedVenatorBow = equippedVenatorBow;
		if (!ignoreVenatorBowCondition) updateShouldRenderPath();
	}

	/**
	 * Loads configurations into class variables
	 */
	public void parseConfigs()
	{
		initialTargetOutline = config.initialColor();
		firstBounceOutline = config.bounce1Color();
		secondBounceOutline = config.bounce2Color();
		boomerangBounceOutline = config.returnBounceColor();
		outlineFeather = config.outlineFeather();
		outlineWidth = config.outlineWidth();

		ignoreMultiCombatCondition = config.ignoreMultiCombatPrerequisite();
		ignoreVenatorBowCondition = config.ignoreVenatorBowPrerequisite();
		drawOnlyOnePath = config.drawOnlyOnePath();
		limitPathSize = config.limitPathSize();
		updateShouldRenderPath();
	}

	/**
	 * Updates the shouldRenderPath flag, which is derived from various other flags.
	 */
	private void updateShouldRenderPath()
	{
		shouldRenderPath = (ignoreMultiCombatCondition || isInMultiCombat) && (ignoreVenatorBowCondition || hasEquippedVenatorBow);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!shouldRenderPath) return null;

		Player player = client.getLocalPlayer();
		if (player == null || player.getWorldArea() == null) return null;

		MenuEntry[] menuEntries = client.getMenu().getMenuEntries();
		if (menuEntries.length == 0 || client.isMenuOpen()) return null;

		MenuEntry topEntry = menuEntries[menuEntries.length - 1];
		NPC hoveredNpc = topEntry.getNpc();

		if (!isAttackable(hoveredNpc)) return null;

		WorldView worldView = client.getTopLevelWorldView();

		List<NPC> validNpcs = new ArrayList<>();
		for (NPC npc : worldView.npcs())
		{
			if (isAttackable(npc))
			{
				if (hoveredNpc.getWorldLocation().distanceTo(npc.getWorldLocation()) <= 15)
				{
					validNpcs.add(npc);
				}
			}
		}

		Set<NPC> bounce1 = new HashSet<>();
		Set<NPC> bounce2 = new HashSet<>();
		boolean returnsToSender = false;

		for (NPC b1 : validNpcs)
		{
			if (b1 == hoveredNpc) continue;

			if (canBounce(hoveredNpc, b1) && hasNpcLineOfSight(worldView, hoveredNpc.getWorldArea(), b1.getWorldArea()))
			{
				bounce1.add(b1);

				if (limitPathSize)
				{
					if (drawOnlyOnePath) break;
					else continue;
				}

				for (NPC b2 : validNpcs)
				{
					if (b2 == b1) continue;

					if (canBounce(b1, b2) && hasNpcLineOfSight(worldView, b1.getWorldArea(), b2.getWorldArea()))
					{
						if (b2 == hoveredNpc)
						{
							returnsToSender = true;
						}
						else
						{
							bounce2.add(b2);
						}

						if (drawOnlyOnePath) break;
					}
				}

				if (drawOnlyOnePath) break;
			}
		}

		if (returnsToSender) {
			modelOutlineRenderer.drawOutline(hoveredNpc, outlineWidth+1, boomerangBounceOutline, outlineFeather);
		} else if (!bounce1.isEmpty()) {
			modelOutlineRenderer.drawOutline(hoveredNpc, outlineWidth, initialTargetOutline, outlineFeather);
		}

		for (NPC n : bounce1) modelOutlineRenderer.drawOutline(n, outlineWidth, firstBounceOutline, outlineFeather);
		for (NPC n : bounce2)
		{
			if (!bounce1.contains(n)) {
				modelOutlineRenderer.drawOutline(n, outlineWidth, secondBounceOutline, outlineFeather);
			}
		}

		return null;
	}

	/**
	 * Determines Line of Sight specifically between two NPCs by projecting from the target tile
	 * back to the nearest tile of the source NPC.
	 */
	private boolean hasNpcLineOfSight(WorldView wv, WorldArea sourceArea, WorldArea targetArea)
	{
		int nearestX = Math.max(sourceArea.getX(), Math.min(targetArea.getX(), sourceArea.getX() + sourceArea.getWidth() - 1));
		int nearestY = Math.max(sourceArea.getY(), Math.min(targetArea.getY(), sourceArea.getY() + sourceArea.getHeight() - 1));
		WorldArea nearestSourceTile = new WorldArea(nearestX, nearestY, 1, 1, sourceArea.getPlane());

		return targetArea.hasLineOfSightTo(wv, nearestSourceTile);
	}

	/**
	 * Returns true if both conditions for bouncing off a projectile from sender to target are met
	 */
	private boolean canBounce(NPC sender, NPC target)
	{
		return canSend(sender, target) && canAccept(sender, target);
	}

	/**
	 * Calculates the set of sender tiles for an NPC based on its physical size.
	 * The Venator Bow's bounce logic requires evaluating specific tiles (Centre
	 * and/or SW) within the NPC's area to determine if a projectile can be bounced off
	 */
	private List<Point> getSenderTiles(NPC sender)
	{
		int sSize = sender.getComposition().getSize();
		WorldPoint sw = sender.getWorldLocation();
		List<Point> tiles = new ArrayList<>();

		if (sSize % 2 != 0) {
			tiles.add(new Point(sw.getX() + sSize / 2, sw.getY() + sSize / 2));
		} else if (sSize == 2) {
			tiles.add(new Point(sw.getX(), sw.getY()));
			tiles.add(new Point(sw.getX() + 1, sw.getY()));
			tiles.add(new Point(sw.getX(), sw.getY() + 1));
			tiles.add(new Point(sw.getX() + 1, sw.getY() + 1));
		} else if (sSize == 4) {
			tiles.add(new Point(sw.getX() + 1, sw.getY() + 1));
			tiles.add(new Point(sw.getX() + 2, sw.getY() + 1));
			tiles.add(new Point(sw.getX() + 1, sw.getY() + 2));
			tiles.add(new Point(sw.getX() + 2, sw.getY() + 2));
		}
		return tiles;
	}

	/**
	 * Determines if a sender NPC is capable of initiating a Venator Bow bounce to a target.
	 * The bounce mechanics are based on the sender's size and the target's size,
	 * requiring specific hitboxes (SW tile and/or Centre tile) to be within 2 tiles.
	 */
	private boolean canSend(NPC sender, NPC target)
	{
		int sSize = sender.getComposition().getSize();
		int tSize = target.getComposition().getSize();
		WorldPoint tSW = target.getWorldLocation();

		Point tSWPoint = new Point(tSW.getX(), tSW.getY());
		Point tCentre = new Point(tSW.getX() + tSize / 2, tSW.getY() + tSize / 2);
		Point tCentreSW = new Point(tSW.getX() + (tSize / 2) - 1, tSW.getY() + (tSize / 2) - 1);

		for (Point sPoint : getSenderTiles(sender))
		{
			boolean passes = false;
			if (sSize % 2 != 0) {
				passes = tSize <= 2 ? finds(sPoint, tSWPoint) : finds(sPoint, tSWPoint) && finds(sPoint, tCentre);
			} else if (sSize == 2) {
				passes = tSize <= 3 ? finds(sPoint, tCentre) : finds(sPoint, tCentre) && finds(sPoint, tCentreSW);
			} else if (sSize == 4) {
				passes = finds(sPoint, tSWPoint) && finds(sPoint, tCentreSW);
			}
			if (passes) return true;
		}
		return false;
	}

	/**
	 * Determines if a target NPC is capable of receiving a bounce from a sender.
	 * This verifies the geometry requirements for the target to accept a projectile
	 * based on its size relative to the sender.
	 */
	private boolean canAccept(NPC sender, NPC target)
	{
		int sSize = sender.getComposition().getSize();
		int tSize = target.getComposition().getSize();
		WorldPoint tSW = target.getWorldLocation();

		Point tSWPoint = new Point(tSW.getX(), tSW.getY());
		Point tCentre = new Point(tSW.getX() + tSize / 2, tSW.getY() + tSize / 2);
		Point tCentreSW = new Point(tSW.getX() + (tSize / 2) - 1, tSW.getY() + (tSize / 2) - 1);

		for (Point sPoint : getSenderTiles(sender))
		{
			boolean passes = false;
			if (tSize == 1 || tSize == 2) {
				passes = finds(sPoint, tSWPoint);
			} else if (tSize == 3) {
				if (sSize % 2 != 0) passes = finds(sPoint, tCentre) && finds(sPoint, tSWPoint);
				else if (sSize == 2) passes = finds(sPoint, tCentre);
				else if (sSize == 4) passes = finds(sPoint, tSWPoint);
			} else if (tSize >= 4) {
				if (sSize % 2 != 0) passes = finds(sPoint, tCentre) && finds(sPoint, tSWPoint);
				else if (sSize == 2) passes = finds(sPoint, tCentre) && finds(sPoint, tCentreSW);
				else if (sSize == 4) passes = finds(sPoint, tSWPoint) && finds(sPoint, tCentreSW);
			}
			if (passes) return true;
		}
		return false;
	}

	/**
	 * Returns true if the Chebyshev between sender and target is lesser than or equal to 2
	 */
	private boolean finds(Point sender, Point target)
	{
		return Math.max(Math.abs(sender.x - target.x), Math.abs(sender.y - target.y)) <= 2;
	}

	/**
	 * Return true if npc can be attacked
	 */
	private boolean isAttackable(NPC npc)
	{
		if (npc == null || npc.isDead())
		{
			return false;
		}

		if (npc.getCombatLevel() > 0)
		{
			return true;
		}

		if (npc.getComposition() != null && npc.getComposition().getActions() != null)
		{
			for (String action : npc.getComposition().getActions())
			{
				if ("Attack".equalsIgnoreCase(action))
				{
					return true;
				}
			}
		}

		return false;
	}
}