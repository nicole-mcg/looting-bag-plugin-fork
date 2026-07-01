package com.lootingbag;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RunLootingBagPlugin
{
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(LootingBagPlugin.class);
		RuneLite.main(args);
	}
}
