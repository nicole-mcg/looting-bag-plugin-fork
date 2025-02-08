package com.looting_bag_plugin;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Looting Bag"
)
public class LootingBagPlugin extends Plugin
{
	public static final int LOOTING_BAG_CONTAINER = 516;

	@Override
	protected void startUp()
	{
		log.warn("Starting up Looting Bag plugin");
	}

	@Override
	protected void shutDown()
	{
		log.warn("Shutting down Looting Bag plugin");
	}
}
