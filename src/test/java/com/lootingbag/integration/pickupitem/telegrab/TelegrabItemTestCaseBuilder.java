package com.lootingbag.integration.pickupitem.telegrab;

import com.lootingbag.integration.LootingBagTestCaseBuilder;
import net.runelite.api.coords.WorldPoint;

public class TelegrabItemTestCaseBuilder extends LootingBagTestCaseBuilder<TelegrabItemTestCase> {
    public TelegrabItemTestCaseBuilder(String name) {
        super(new TelegrabItemTestCase(), name);
    }

    public TelegrabItemTestCaseBuilder setGroundItemLocation(WorldPoint groundItemLocation) {
        testCase.groundItemLocation = groundItemLocation;
        return this;
    }

    @Override
    public TelegrabItemTestCase build() {
        TelegrabItemTestCase testCase = super.build();

        if (testCase.groundItemLocation == null) {
            testCase.groundItemLocation = testCase.worldParameters.playerLocation;
        }

        return testCase;
    }
}
