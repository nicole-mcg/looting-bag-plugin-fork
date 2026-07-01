package com.lootingbag.integration.parameters;

import com.lootingbag.testutil.MockUtils;
import com.lootingbag.testutil.RandomUtils;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import java.util.Random;

public class WorldParameters {
    private static Random random = new Random();

    public WorldParameters() {
    }

    public boolean inWilderness = true;
    public boolean suppliesGoIntoInventory = false;

    public WorldView worldView;
    public WorldPoint playerLocation;

    public LocalPoint localPlayerLocation;
}
