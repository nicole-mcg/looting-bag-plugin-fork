package com.lootingbag.integration.pickupitem;

import com.lootingbag.integration.LootingBagTestCaseBuilder;
import net.runelite.api.coords.WorldPoint;

public class PickupItemTestCaseBuilder extends LootingBagTestCaseBuilder<PickupItemTestCase> {
    public PickupItemTestCaseBuilder(String name) {
        super(new PickupItemTestCase(), name);
    }

    public PickupItemTestCaseBuilder setGroundItemLocation(WorldPoint groundItemLocation) {
        testCase.groundItemLocation = groundItemLocation;
        return this;
    }

    public PickupItemTestCaseBuilder setPlayerEndingLocation(WorldPoint playerEndingLocation) {
        testCase.playerEndingLocation = playerEndingLocation;
        return this;
    }

    @Override
    public PickupItemTestCase build() {
        PickupItemTestCase testCase = super.build();

        if (testCase.groundItemLocation == null) {
            testCase.groundItemLocation = testCase.worldParameters.playerLocation;
        }

        if (testCase.playerEndingLocation == null) {
            testCase.playerEndingLocation = testCase.groundItemLocation;
        }

        return testCase;
    }
}
