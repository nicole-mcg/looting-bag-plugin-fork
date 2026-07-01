package com.lootingbag.integration.pickupitem.telegrab;

import com.lootingbag.integration.parameters.ItemParameters;
import com.lootingbag.testutil.RandomUtils;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;

import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class TelegrabItemTestCases {

    public static TelegrabItemTestCase NON_STACKABLE_ITEM_INTO_EMPTY_LOOTING_BAG =
        new TelegrabItemTestCaseBuilder("Non-stackable item into empty looting bag")
            .addInsertedItem(new ItemParameters() {{
                itemId = 1234;
            }})
            .setExpectedInsertedItems(new Item[] {
                new Item(1234, 1),
            })
            .build();

    public static TelegrabItemTestCase NON_STACKABLE_ITEM_INTO_IN_LOOTING_BAG_CONTAINING_SAME_ITEM =
        new TelegrabItemTestCaseBuilder("Non-stackable item into looting bag containing the same item")
            .setLootingBagItems(new Item[] {
                new Item(1234, 1),
            })
            .addInsertedItem(new ItemParameters() {{
                itemId = 1234;
            }})
            .setExpectedInsertedItems(new Item[] {
                new Item(1234, 1),
            })
            .build();

    public static TelegrabItemTestCase NON_STACKABLE_ITEM_INTO_FULL_LOOTING_BAG =
        new TelegrabItemTestCaseBuilder("Non-stackable item into full looting bag")
            .addInsertedItem(new ItemParameters() {{
                itemId = 1234;
            }})
            .setLootingBagItems(Stream
                .generate(() -> RandomUtils.getRandomItem(true))
                .limit(28)
                .toArray(Item[]::new))
            .setExpectedInsertedItems(new Item[0])
            .build();

    public static TelegrabItemTestCase STACKABLE_ITEM_INTO_EMPTY_LOOTING_BAG =
        new TelegrabItemTestCaseBuilder("Stackable item into empty looting bag")
            .addInsertedItem(new ItemParameters() {{
                itemId = 1234;
                isStackable = true;
            }})
            .setExpectedInsertedItems(new Item[]{
                new Item(1234, 1),
            })
            .build();

    public static TelegrabItemTestCase STACKABLE_ITEM_INTO_STACK_IN_LOOTING_BAG =
        new TelegrabItemTestCaseBuilder("Stackable item into looting bag already containing the same item")
            .setLootingBagItems(new Item[]{
                new Item(1234, 1),
            })
            .addInsertedItem(new ItemParameters() {{
                itemId = 1234;
                isStackable = true;
            }})
            .setExpectedInsertedItems(new Item[]{
                new Item(1234, 1),
            })
            .build();

    public static TelegrabItemTestCase STACKABLE_ITEM_INTO_FULL_LOOTING_BAG_STACK =
        new TelegrabItemTestCaseBuilder("Stackable item into full looting bag containing the same item")
            .addInsertedItem(new ItemParameters() {{
                itemId = 1234;
                isStackable = true;
            }})
            .setLootingBagItems(
                IntStream.rangeClosed(0, 27)
                    .mapToObj(i -> new Item(1234 + i, 1))
                    .toArray(Item[]::new)
            )
            .setExpectedInsertedItems(new Item[0])
            .build();

    public static TelegrabItemTestCase STACKABLE_ITEM_INTO_FULL_LOOTING_BAG =
        new TelegrabItemTestCaseBuilder("Stackable item into full looting bag that does not contain the same item")
            .addInsertedItem(new ItemParameters() {{
                itemId = 1234;
                isStackable = true;
            }})
            .setLootingBagItems(
                IntStream.rangeClosed(0, 27)
                    .mapToObj(i -> new Item(1235 + i, 1))
                    .toArray(Item[]::new)
            )
            .setExpectedInsertedItems(new Item[0])
            .build();

    public static TelegrabItemTestCase PLAYER_INSIDE_FEROX =
        new TelegrabItemTestCaseBuilder("Inside Ferox Enclave")
            .setOutsideWilderness()
            .setPlayerLocation(new WorldPoint(3140, 3630, 0))
            .addInsertedItem(new ItemParameters() {{
                itemId = 1234;
                isStackable = true;
            }})
            .setExpectedInsertedItems(new Item[] {
                new Item(1234, 1),
            })
            .build();

    public static TelegrabItemTestCase PLAYER_INSIDE_FEROX_BASEMENT =
        new TelegrabItemTestCaseBuilder("Inside Ferox Enclave basement")
            .setOutsideWilderness()
            .setPlayerLocation(new WorldPoint(3169, 10036, 0))
            .addInsertedItem(new ItemParameters() {{
                itemId = 1234;
                isStackable = true;
            }})
            .setExpectedInsertedItems(new Item[] {
                new Item(1234, 1),
            })
            .build();

    public static TelegrabItemTestCase SUPPLIES_SETTING_SUPPLY_ITEM =
        new TelegrabItemTestCaseBuilder("Supplies go into inventory, item is a supply")
            .enableSuppliesGoIntoInventory()
            .addInsertedItem(new ItemParameters() {{
                isSupply = true;
            }})
            .setExpectedInsertedItems(new Item[0])
            .build();

    public static TelegrabItemTestCase SUPPLIES_SETTING_NON_SUPPLY_ITEM =
        new TelegrabItemTestCaseBuilder("Supplies go into inventory, item is not a supply")
            .enableSuppliesGoIntoInventory()
            .addInsertedItem(new ItemParameters() {{
                itemId = 1234;
                isSupply = false;
            }})
            .setExpectedInsertedItems(new Item[]{
                new Item(1234, 1),
            })
            .build();

    public static TelegrabItemTestCase NON_TRADEABLE_ITEM =
        new TelegrabItemTestCaseBuilder("Non-tradeable item")
            .addInsertedItem(new ItemParameters() {{
                isTradeable = false;
            }})
            .setExpectedInsertedItems(new Item[0])
            .build();

    public static TelegrabItemTestCase PLAYER_NOT_IN_WILDERNESS =
        new TelegrabItemTestCaseBuilder("Not in wilderness")
            .setOutsideWilderness()
            .addInsertedItem()
            .setExpectedInsertedItems(new Item[0])
            .build();

    public static TelegrabItemTestCase PLAYER_NEAR_ITEM_LOCATION =
        new TelegrabItemTestCaseBuilder("Player is near but not on the ground item location")
            .setGroundItemLocation(new WorldPoint(10_000, 10_000, 0))
            .setPlayerLocation(new WorldPoint(10_004, 10_004, 0))
            .addInsertedItem(new ItemParameters() {{
                itemId = 1234;
                isTradeable = false;
            }})
            .setExpectedInsertedItems(new Item[] {
                new Item(1234, 1)
            })
            .build();

    public static TelegrabItemTestCase PLAYER_FAR_FROM_ITEM_LOCATION =
        new TelegrabItemTestCaseBuilder("Player is too far from the ground item location to telegrab it")
            .setGroundItemLocation(new WorldPoint(10_000, 10_000, 0))
            .addInsertedItem()
            .setPlayerLocation(new WorldPoint(10_015, 10_015, 0))
            .setExpectedInsertedItems(new Item[0])
            .build();

    public static TelegrabItemTestCase PLAYER_ON_DIFFERENT_PLANE =
        new TelegrabItemTestCaseBuilder("Player is at same coordinates as item but on different plane")
            .setGroundItemLocation(new WorldPoint(10_000, 10_000, 0))
            .addInsertedItem()
            .setPlayerLocation(new WorldPoint(10_000, 10_000, 1))
            .setExpectedInsertedItems(new Item[0])
            .build();

    public static TelegrabItemTestCase CLOSED_LOOTING_BAG =
        new TelegrabItemTestCaseBuilder("Player has closed looting bag")
            .addInsertedItem()
            .setLootingBagItems(new Item[] { new Item(ItemID.LOOTING_BAG, 1) })
            .setExpectedInsertedItems(new Item[0])
            .build();

    public static TelegrabItemTestCase NO_LOOTING_BAG_IN_INVENTORY =
        new TelegrabItemTestCaseBuilder("Player does not have looting bag")
            .addInsertedItem()
            .setInventoryItems(new Item[0])
            .setExpectedInsertedItems(new Item[0])
            .build();
}
