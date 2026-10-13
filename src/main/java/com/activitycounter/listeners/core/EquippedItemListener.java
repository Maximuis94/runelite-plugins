package com.activitycounter.listeners.core;

import static com.activitycounter.PluginConstants.EQUIPPED_ITEM_CONTAINER_ID;
import com.activitycounter.listeners.AbstractListenerNode;
import com.activitycounter.listeners.conditional.BoltProcListener;
import com.activitycounter.models.EquipmentWorn;
import com.activitycounter.models.EquippedWeaponType;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import static net.runelite.api.gameval.VarPlayerID.COM_MODE;
import static net.runelite.api.gameval.VarbitID.COMBAT_WEAPON_CATEGORY;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;

/**
 * Centralized class that listens to changes in the equipped items and acts accordingly.
 */
@Slf4j
@Singleton
public class EquippedItemListener extends AbstractListenerNode
{
	private static final EquippedWeaponType.Stance NULL_STANCE = EquippedWeaponType.UNARMED.getStance(0);

	public static final int WEAPON_SLOT_IDX = EquipmentInventorySlot.WEAPON.getSlotIdx();
	public static final int OFFHAND_SLOT_IDX = EquipmentInventorySlot.SHIELD.getSlotIdx();
	public static final int HEAD_SLOT_IDX = EquipmentInventorySlot.HEAD.getSlotIdx();
	public static final int CHEST_SLOT_IDX = EquipmentInventorySlot.BODY.getSlotIdx();
	public static final int LEG_SLOT_IDX = EquipmentInventorySlot.LEGS.getSlotIdx();
	public static final int ARM_SLOT_IDX = EquipmentInventorySlot.ARMS.getSlotIdx();
	public static final int FOOT_SLOT_IDX = EquipmentInventorySlot.BOOTS.getSlotIdx();
	public static final int BACK_SLOT_IDX = EquipmentInventorySlot.SHIELD.getSlotIdx();
	public static final int NECK_SLOT_IDX = EquipmentInventorySlot.AMULET.getSlotIdx();
	public static final int RING_SLOT_IDX = EquipmentInventorySlot.RING.getSlotIdx();
	public static final int AMMO_SLOT_IDX = EquipmentInventorySlot.AMMO.getSlotIdx();

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	public static final int MAX_EQUIPMENT_SLOT_IDX = Arrays.stream(EquipmentInventorySlot.values())
		.mapToInt(EquipmentInventorySlot::getSlotIdx)
		.max()
		.getAsInt();

	@Inject
	private BoltProcListener boltProcListener;

	private final int[] equippedItemIds = new int[MAX_EQUIPMENT_SLOT_IDX+1];

	// Equipment State
	@Getter
	private EquipmentWorn weapon = null;

	private int equippedAmmoId = -1;
	private boolean combatParamUpdateQueued = false;

	private boolean isInitialized = false;

	@Getter
	private EquippedWeaponType equippedWeaponType;

	@Getter
	private EquippedWeaponType.Stance stance;

	@Inject
	private ClientThread clientThread;

	/**
	 * Sets up routing caches for sound listeners
	 */
	public void initializeRoutingCaches() {
		if (isInitialized) return;

		Arrays.fill(equippedItemIds, -1);
		onLogin();

		isInitialized = true;
	}

	@Override
	public void onRegistered() {
		isInitialized = false;
		clientThread.invokeLater(this::initializeRoutingCaches);
		combatParamUpdateQueued = true;
	}

	private void onLogin()
	{
		ItemContainer equipment = client.getItemContainer(EQUIPPED_ITEM_CONTAINER_ID);
		if (equipment != null)
		{
			processContainer(equipment);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			onLogin();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == EQUIPPED_ITEM_CONTAINER_ID)
		{
			processContainer(event.getItemContainer());
		}
	}

	/**
	 * Updates combat parameters such as weapon type equipped and attack stance used
	 */
	private void updateCombatParameters()
	{
		equippedWeaponType = EquippedWeaponType.getByVarbitValue(client.getVarbitValue(COMBAT_WEAPON_CATEGORY));

		int attackStyleIndex = client.getVarpValue(COM_MODE);
		stance = equippedWeaponType != null ? equippedWeaponType.getStance(attackStyleIndex) : NULL_STANCE;
		combatParamUpdateQueued = equippedWeaponType == null;
	}

//	@Subscribe
//	public void onGameTick(GameTick event)
//	{
//		if (combatParamUpdateQueued)
//		{
//			updateCombatParameters();
//		}
//	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarbitId() == COMBAT_WEAPON_CATEGORY || event.getVarpId() == COM_MODE)
		{
			combatParamUpdateQueued = true;
		}
	}

	/**
	 * Synchronizes the entire tracking array with the current state of the equipment container,
	 * then triggers specialized side-effects for specific slots.
	 */
	private void processContainer(ItemContainer inventory)
	{
		if (inventory == null)
		{
			Arrays.fill(equippedItemIds, -1);
			updateWeaponWorn(null);
			updateAmmoWorn(null);
			return;
		}

		Item[] items = inventory.getItems();
		for (int i = 0; i < equippedItemIds.length; i++)
		{
			if (i < items.length && items[i] != null)
			{
				equippedItemIds[i] = items[i].getId();
			}
			else
			{
				equippedItemIds[i] = -1;
			}
		}

		// Fire specific side-effects
		updateWeaponWorn(inventory.getItem(WEAPON_SLOT_IDX));
		updateAmmoWorn(inventory.getItem(AMMO_SLOT_IDX));
	}

	/**
	 * All actions that are to be taken whenever the weapon worn is changed
	 */
	private void updateWeaponWorn(Item item)
	{
		int weaponId = item != null ? item.getId() : -1;
		equippedItemIds[WEAPON_SLOT_IDX] = weaponId;
		weapon = weaponId != -1 ? EquipmentWorn.getEquipment(weaponId) : null;
	}

	/**
	 * All actions that are to be taken whenever the ammo worn is changed
	 */
	private void updateAmmoWorn(Item item)
	{
		int itemId = item != null ? item.getId() : -1;
		if (itemId == equippedAmmoId) return;

		equippedAmmoId = item == null ? -1 : item.getId();
		boltProcListener.setAmmoItem(item);
	}

	public int getHeadId()    { return equippedItemIds[HEAD_SLOT_IDX]; }
	public int getBackId()    { return equippedItemIds[BACK_SLOT_IDX]; }
	public int getNeckId()    { return equippedItemIds[NECK_SLOT_IDX]; }
	public int getWeaponId()  { return equippedItemIds[WEAPON_SLOT_IDX]; }
	public int getChestId()   { return equippedItemIds[CHEST_SLOT_IDX]; }
	public int getOffhandId() { return equippedItemIds[OFFHAND_SLOT_IDX]; }
	public int getLegsId()    { return equippedItemIds[LEG_SLOT_IDX]; }
	public int getGlovesId()  { return equippedItemIds[ARM_SLOT_IDX]; }
	public int getBootsId()   { return equippedItemIds[FOOT_SLOT_IDX]; }
	public int getRingId()    { return equippedItemIds[RING_SLOT_IDX]; }
	public int getAmmoId()    { return equippedItemIds[AMMO_SLOT_IDX]; }
}