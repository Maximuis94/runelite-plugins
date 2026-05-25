package com.venatorpathfinder;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class VenatorPathFinderPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(VenatorPathFinderPlugin.class);
		RuneLite.main(args);
	}
}