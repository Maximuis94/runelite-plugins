package com.frequentlytradeditemstab;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class FrequentlyTradedItemsTabPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(FrequentlyTradedItemsTabPlugin.class);
		RuneLite.main(args);
	}
}