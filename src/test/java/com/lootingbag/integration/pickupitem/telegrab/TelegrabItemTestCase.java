package com.lootingbag.integration.pickupitem.telegrab;

import com.lootingbag.integration.LootingBagTestCase;
import com.lootingbag.integration.parameters.ItemParameters;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;

import java.util.Arrays;

public class TelegrabItemTestCase extends LootingBagTestCase {

    public WorldPoint groundItemLocation;

    @Override
    public boolean checkItemInsertable(ItemParameters itemParameters) {
        // Check if the player is within 5 tiles of the ground item
        boolean isPlayerInTelegrabDistance = worldParameters.playerLocation.distanceTo(groundItemLocation) <= 5;

        // Check if the player has an open looting bag
        boolean hasOpenLootingBag = Arrays.stream(inventoryItems)
            .anyMatch(item -> item.getId() == ItemID.LOOTING_BAG_22586);

        return isPlayerInTelegrabDistance && hasOpenLootingBag;
    }
}
