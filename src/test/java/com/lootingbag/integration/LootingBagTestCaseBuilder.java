package com.lootingbag.integration;

import com.lootingbag.integration.parameters.ItemParameters;
import com.lootingbag.testutil.MockUtils;
import com.lootingbag.testutil.RandomUtils;
import net.runelite.api.Item;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
import java.util.Random;

public class LootingBagTestCaseBuilder<TTestCase extends LootingBagTestCase> {

    private static Random random = new Random();

    protected final TTestCase testCase;
    protected final ArrayList<ItemParameters> insertedItems;

    public LootingBagTestCaseBuilder(TTestCase testCase, String name) {
        this.testCase = testCase;
        this.testCase.name = name;
        insertedItems = new ArrayList<>();
    }

    public LootingBagTestCaseBuilder<TTestCase> setOutsideWilderness() {
        testCase.worldParameters.inWilderness = false;
        return this;
    }

    public LootingBagTestCaseBuilder<TTestCase> enableSuppliesGoIntoInventory() {
        testCase.worldParameters.suppliesGoIntoInventory = true;
        return this;
    }

    public LootingBagTestCaseBuilder<TTestCase> setPlayerLocation(WorldPoint playerLocation) {
        testCase.worldParameters.playerLocation = playerLocation;
        return this;
    }

    public LootingBagTestCaseBuilder<TTestCase> setLootingBagItems(Item ...lootingBagItems) {
        testCase.lootingBagItems = lootingBagItems;
        return this;
    }

    public LootingBagTestCaseBuilder<TTestCase> setInventoryItems(Item ...inventoryItems) {
        testCase.inventoryItems = inventoryItems;
        return this;
    }

    public LootingBagTestCaseBuilder<TTestCase> addInsertedItem() {
        insertedItems.add(new ItemParameters());
        return this;
    }

    public LootingBagTestCaseBuilder<TTestCase> addInsertedItem(ItemParameters insertedItem) {
        insertedItems.add(insertedItem);
        return this;
    }

    public LootingBagTestCaseBuilder<TTestCase> setExpectedInsertedItems(Item ...expectedInsertedItems) {
        testCase.expectedInsertedItems = expectedInsertedItems;
        return this;
    }

    public LootingBagTestCaseBuilder<TTestCase> setNumExpectedFreeSlots(int numExpectedFreeSlots) {
        testCase.numExpectedFreeSlots = numExpectedFreeSlots;
        return this;
    }

    public TTestCase build() {
        testCase.insertedItems = insertedItems.toArray(new ItemParameters[0]);

        if (testCase.worldParameters.worldView == null) {
            testCase.worldParameters.worldView = getDefaultWorldview();
        }

        if (testCase.worldParameters.playerLocation == null) {
            testCase.worldParameters.playerLocation = RandomUtils.getRandomWorldPoint(
                testCase.worldParameters.worldView
            );
        }

        if (testCase.worldParameters.localPlayerLocation == null) {
            testCase.worldParameters.localPlayerLocation = LocalPoint.fromWorld(
                testCase.worldParameters.worldView,
                testCase.worldParameters.playerLocation
            );
        }

        return testCase;
    }

    private WorldView getDefaultWorldview() {
        WorldPoint playerLocation = testCase.worldParameters.playerLocation;

        if (playerLocation == null) {
            return RandomUtils.getRandomWorldView();
        }

        int baseX = playerLocation.getX() - random.nextInt(1_000);
        int baseY = playerLocation.getY() - random.nextInt(1_000);
        int sizeX = playerLocation.getX() + random.nextInt(1_000) - baseX;
        int sizeY = playerLocation.getY() + random.nextInt(1_000) - baseY;
        return MockUtils.createWorldView(
            random.nextInt(100),
            playerLocation.getPlane(),
            baseX,
            baseY,
            sizeX,
            sizeY
        );
    }
}
