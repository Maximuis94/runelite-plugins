package com.slayerbanktab.models;

import net.runelite.api.EquipmentInventorySlot;

/**
 * The standard Equipment slots with an additional ammo slot provided by Dizana's quiver.
 */
public enum ExtendedEquipmentSlot {
	HEAD, CAPE, AMULET, WEAPON, BODY, SHIELD, LEGS, GLOVES, BOOTS, RING, AMMO, QUIVER_AMMO;

	public static ExtendedEquipmentSlot fromVanilla(EquipmentInventorySlot vanillaSlot) {
		try {
			return valueOf(vanillaSlot.name());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}