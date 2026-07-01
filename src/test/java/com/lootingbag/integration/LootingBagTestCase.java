package com.lootingbag.integration;

import com.lootingbag.integration.parameters.ItemParameters;
import com.lootingbag.integration.parameters.WorldParameters;
import net.runelite.api.Item;
import net.runelite.api.ItemID;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;

public abstract class LootingBagTestCase {
    public String name;

    public WorldParameters worldParameters = new WorldParameters();

    public Item[] lootingBagItems = new Item[0];
    public Item[] inventoryItems = new Item[] { new Item(ItemID.LOOTING_BAG_22586, 1) };

    public ItemParameters[] insertedItems = null;

    public Item[] expectedInsertedItems = new Item[0];

    public int numExpectedFreeSlots = 0;

    public LootingBagTestCase() {
    }

    /**
     * Used to determine test-specific conditions for whether
     * an item can be inserted into the looting bag
     * E.g. if checking an item despawn, the player must be on the despawn location
     * @return true if the item is eligible inserted into the looting bag according to the test case
     */
    public abstract boolean checkItemInsertable(ItemParameters itemParameters);

    public String toString() {
        if (name != null) {
            return name;
        }

        return super.toString();
    }

    public static <TTestCaseProvider> Stream<LootingBagTestCase> getAllTestCases(Class<TTestCaseProvider> testCasesClass) {
        // Get all test cases using reflection
        // To ensure that any new test cases are automatically included in the test suite
        Field[] fields = testCasesClass.getDeclaredFields();

        return Arrays.stream(fields)
            .filter(field -> LootingBagTestCase.class.isAssignableFrom(field.getType()))
            .map(field -> {
                try {
                    return (LootingBagTestCase) field.get(null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                        "Failed to get test case '"
                            + field.getName()
                            + "'. Make sure the class is public and the field is public and static",
                        e
                    );
                }
            });
    }

    private static Item[] cloneItems(Item[] items) {
        return Arrays.stream(items)
            .map(item -> new Item(item.getId(), item.getQuantity()))
            .toArray(Item[]::new);
    }
}
