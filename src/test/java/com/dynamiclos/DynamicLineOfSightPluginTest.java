package com.dynamiclos;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class DynamicLineOfSightPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DynamicLineOfSightPlugin.class);
		RuneLite.main(args);
	}
}