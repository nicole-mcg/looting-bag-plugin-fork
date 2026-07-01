package com.lootingbag.integration.pickupitem;

import com.lootingbag.integration.LootingBagTestCase;
import com.lootingbag.integration.parameters.ItemParameters;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;

import java.util.Arrays;

public class PickupItemTestCase extends LootingBagTestCase {

    public WorldPoint groundItemLocation;
    public WorldPoint playerEndingLocation;

    public PickupItemTestCase() {
        super();
    }

    @Override
    @SuppressWarnings("redundant")
    public boolean checkItemInsertable(ItemParameters itemParameters) {
        // Check if the player is on the same location as the ground item
        boolean isPlayerAtGroundItem = playerEndingLocation.equals(groundItemLocation);
        boolean hasOpenLootingBag = Arrays.stream(inventoryItems)
            .anyMatch(item -> item.getId() == ItemID.LOOTING_BAG_22586);
        return isPlayerAtGroundItem && hasOpenLootingBag;
    }
}
