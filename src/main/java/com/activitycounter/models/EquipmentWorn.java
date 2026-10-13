package com.activitycounter.models;

import com.activitycounter.PluginConstants;
import static com.activitycounter.PluginConstants.MAX_ITEM_ID;
import lombok.Getter;

/**
 * Model class that defines certain relations between itemIds and other tracked modalities like soundEffects
 * E.g., it maps Scythe of vitur itemIds to soundEffectIds produced by the Scythe of vitur.
 */
public enum EquipmentWorn
{
	SCYTHE_OF_VITUR(new int[]{22325, 25736, 25739}, PluginConstants.SoundIDs.SCYTHE_SOUND_EFFECTS, TrackedActivity.SCYTHE_SWIPE),
	SANGUINESTI_STAFF(new int[]{22323, 25731}, PluginConstants.SoundIDs.POWERED_STAFF_ATTACK, TrackedActivity.SANGUINESTI_STAFF),
	TRIDENT_OF_THE_SEAS(new int[]{11905, 11907, 22288, 33322, 33323, 33326}, PluginConstants.SoundIDs.POWERED_STAFF_ATTACK, TrackedActivity.TRIDENT_OF_THE_SEAS),
	TRIDENT_OF_THE_SWAMP(new int[]{12899, 22292, 33314, 33318}, PluginConstants.SoundIDs.POWERED_STAFF_ATTACK, TrackedActivity.TRIDENT_OF_THE_SWAMP),
	TUMEKENS_SHADOW(new int[]{27275}, PluginConstants.SoundIDs.POWERED_STAFF_ATTACK, TrackedActivity.TUMEKENS_SHADOW),
	WILDY_WEAPONS(new int[]{22550, 22545, 27660, 27655}, PluginConstants.SoundIDs.ETHER_USED, TrackedActivity.REVENANT_ETHER),
	WILDY_SCEPTRE(new int[]{22555, 27665, 27788, 27679}, PluginConstants.SoundIDs.POWERED_STAFF_ATTACK, TrackedActivity.REVENANT_ETHER),
	;

	EquipmentWorn(int[] itemIds, TrackedActivity trackedActivity)
	{
		assert trackedActivity.getTrackerType() == TrackerType.EQUIPMENT_SOUND_EFFECT;
		this.itemIds = itemIds;

		this.trackedActivity = trackedActivity;
		this.soundEffectIds = trackedActivity.getGameSourceIds();
	}

	@Getter
	private final TrackedActivity trackedActivity;

	private final int[] itemIds;
	private final int[] soundEffectIds;

	private static final EquipmentWorn[] itemIdEquipmentMapping = new EquipmentWorn[MAX_ITEM_ID];

	static
	{
		for (EquipmentWorn equipment : values())
		{
			for (int itemId : equipment.itemIds)
			{
				itemIdEquipmentMapping[itemId] = equipment;
			}
		}
	}

	EquipmentWorn(int[] itemIds, int[] soundEffectIds, TrackedActivity trackedActivity)
	{
		this.itemIds = itemIds;
		this.soundEffectIds = soundEffectIds;
		this.trackedActivity = trackedActivity;
	}

	public boolean hasItemId(int itemId)
	{
		if (itemId < 0 || itemId >= MAX_ITEM_ID)
		{
			return false;
		}
		return itemIdEquipmentMapping[itemId] == this;
	}

	public boolean hasSoundEffect(int soundId)
	{
		for (int id : soundEffectIds)
		{
			if (id == soundId)
			{
				return true;
			}
		}
		return false;
	}

	public static EquipmentWorn getEquipment(int itemId)
	{
		if (itemId < 0 || itemId >= MAX_ITEM_ID)
		{
			return null;
		}
		return itemIdEquipmentMapping[itemId];
	}
}