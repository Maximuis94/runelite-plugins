package com.slayerbanktab.events;

import lombok.Value;

/**
 * Event fired whenever the player's active Slayer task changes or is initialized. It contains both String- and numerical IDs.
 */
@Value
public class NewSlayerTask {
	String setupKey;

	int taskId;
	String taskName;

	int masterId;
	String masterName;

	int bossId;
	String bossName;

	int areaId;
	String areaName;
}