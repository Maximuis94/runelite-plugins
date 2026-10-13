package com.activitycounter.models;

public enum GameMode
{
	NORMAL,
	IRONMAN,
	ULTIMATE_IRONMAN,
	HARDCORE_IRONMAN,
	GROUP_IRONMAN,
	HARDCORE_GROUP_IRONMAN,
	UNRANKED_GROUP_IRONMAN;

	private static final GameMode[] byVarbitValue = GameMode.values();

	public static GameMode fromVarbitValue(int value)
	{
		return byVarbitValue[value];
	}
}
