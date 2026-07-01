package com.lootingbag.integration.pickupitem;

import com.lootingbag.integration.parameters.ItemParameters;
import com.lootingbag.testutil.RandomUtils;
import net.runelite.api.Item;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;

import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class PickupItemTestCases {

//    public static PickupItemTestCase NON_STACKABLE_ITEM_INTO_EMPTY_LOOTING_BAG =
//        new PickupItemTestCaseBuilder("Non-stackable item into empty looting bag")
//            .addInsertedItem(new ItemParameters() {{ itemId = 1234; isTradeable = true; }})
//            .setNumExpectedFreeSlots(27)
//            .setExpectedInsertedItems(new Item(1234, 1))
//            .build();
//
//    public static PickupItemTestCase NON_STACKABLE_ITEM_INTO_IN_LOOTING_BAG_CONTAINING_SAME_ITEM =
//        new PickupItemTestCaseBuilder("Non-stackable item into looting bag containing the same item")
//            .setLootingBagItems(new Item(1234, 1))
//            .addInsertedItem(new ItemParameters() {{ itemId = 1234; }})
//            .setNumExpectedFreeSlots(26)
//            .setExpectedInsertedItems(new Item(1234, 1))
//            .build();

    public static PickupItemTestCase NON_STACKABLE_ITEM_INTO_FULL_LOOTING_BAG =
        new PickupItemTestCaseBuilder("Non-stackable item into full looting bag")
            .addInsertedItem(new ItemParameters() {{ itemId = 1234; }})
            .setLootingBagItems(
                Stream.generate(() -> RandomUtils.getRandomItem(true))
                    .limit(28)
                    .toArray(Item[]::new)
            )
            .setNumExpectedFreeSlots(0)
            .setExpectedInsertedItems(new Item[0])
            .build();

    public static PickupItemTestCase STACKABLE_ITEM_INTO_EMPTY_LOOTING_BAG =
        new PickupItemTestCaseBuilder("Stackable item into empty looting bag")
            .addInsertedItem(new ItemParameters() {{ itemId = 1234; isStackable = true; }})
            .setNumExpectedFreeSlots(27)
            .setExpectedInsertedItems(new Item(1234, 1))
            .build();

    public static PickupItemTestCase STACKABLE_ITEM_INTO_STACK_IN_LOOTING_BAG =
        new PickupItemTestCaseBuilder("Stackable item into looting bag already containing the same item")
            .setLootingBagItems(new Item(1234, 1))
            .addInsertedItem(new ItemParameters() {{ itemId = 1234; isStackable = true;}})
            .setNumExpectedFreeSlots(27)
            .setExpectedInsertedItems(new Item(1234, 1))
            .build();

    public static PickupItemTestCase STACKABLE_ITEM_INTO_FULL_LOOTING_BAG_STACK =
        new PickupItemTestCaseBuilder("Stackable item into full looting bag containing the same item")
            .setLootingBagItems(
                IntStream.rangeClosed(0, 27)
                    .mapToObj(i -> new Item(1234 + i, 1))
                    .toArray(Item[]::new)
            )
            .addInsertedItem(new ItemParameters() {{ itemId = 1234; isStackable = true; }})
            .setNumExpectedFreeSlots(0)
            .setExpectedInsertedItems(new Item(1234, 1))
            .build();

    public static PickupItemTestCase STACKABLE_ITEM_INTO_FULL_LOOTING_BAG =
        new PickupItemTestCaseBuilder("Stackable item into full looting bag that does not contain the same item")
            .addInsertedItem(new ItemParameters() {{
                itemId = 1234;
                isStackable = true;
            }})
            .setLootingBagItems(
                IntStream.rangeClosed(0, 27)
                    .mapToObj(i -> new Item(1235 + i, 1))
                    .toArray(Item[]::new)
            )
            .setNumExpectedFreeSlots(0)
            .setExpectedInsertedItems(new Item[0])
            .build();


    public static PickupItemTestCase PLAYER_INSIDE_FEROX =
        new PickupItemTestCaseBuilder("Inside Ferox Enclave")
            .setOutsideWilderness()
            .setPlayerLocation(new WorldPoint(3140, 3630, 0))
            .addInsertedItem(new ItemParameters() {{
                itemId = 1234;
            }})
            .setNumExpectedFreeSlots(27)
            .setExpectedInsertedItems(new Item(1234, 1))
            .build();

    public static PickupItemTestCase PLAYER_INSIDE_FEROX_BASEMENT =
        new PickupItemTestCaseBuilder("Inside Ferox Enclave basement")
            .setOutsideWilderness()
            .setPlayerLocation(new WorldPoint(3169, 10036, 0))
            .addInsertedItem(new ItemParameters() {{ itemId = 1234; }})
            .setNumExpectedFreeSlots(27)
            .setExpectedInsertedItems(new Item(1234, 1))
            .build();

    public static PickupItemTestCase SUPPLIES_SETTING_SUPPLY_ITEM =
        new PickupItemTestCaseBuilder("Supplies go into inventory, item is a supply")
            .enableSuppliesGoIntoInventory()
            .addInsertedItem(new ItemParameters() {{ isSupply = true; }})
            .setNumExpectedFreeSlots(28)
            .setExpectedInsertedItems(new Item[0])
            .build();

    public static PickupItemTestCase SUPPLIES_SETTING_NON_SUPPLY_ITEM =
        new PickupItemTestCaseBuilder("Supplies go into inventory, item is not a supply")
            .enableSuppliesGoIntoInventory()
            .addInsertedItem(new ItemParameters() {{  itemId = 1234; isSupply = false; }})
            .setNumExpectedFreeSlots(27)
            .setExpectedInsertedItems(new Item(1234, 1))
            .build();

    public static PickupItemTestCase NON_TRADEABLE_ITEM =
        new PickupItemTestCaseBuilder("Non-tradeable item")
            .addInsertedItem(new ItemParameters() {{ isTradeable = false; }})
            .setNumExpectedFreeSlots(28)
            .setExpectedInsertedItems(new Item[0])
            .build();

    public static PickupItemTestCase PLAYER_NOT_IN_WILDERNESS =
        new PickupItemTestCaseBuilder("Not in wilderness")
            .setOutsideWilderness()
            .addInsertedItem()
            .setNumExpectedFreeSlots(28)
            .setExpectedInsertedItems(new Item[0])
            .build();

    public static PickupItemTestCase PLAYER_NOT_ON_DESPAWN_LOCATION =
        new PickupItemTestCaseBuilder("Player not on item despawn location")
            .setPlayerEndingLocation(RandomUtils.getRandomWorldPoint())
            .addInsertedItem()
            .setNumExpectedFreeSlots(28)
            .setExpectedInsertedItems(new Item[0])
            .build();

    public static PickupItemTestCase PLAYER_ON_DIFFERENT_PLANE =
        new PickupItemTestCaseBuilder("Player on item despawn location on a different plane")
            .setGroundItemLocation(new WorldPoint(10_000, 10_000, 0))
            .setPlayerEndingLocation(new WorldPoint(10_000, 10_000, 1))
            .addInsertedItem()
            .setNumExpectedFreeSlots(28)
            .setExpectedInsertedItems(new Item[0])
            .build();

    public static PickupItemTestCase CLOSED_LOOTING_BAG =
        new PickupItemTestCaseBuilder("Player has closed looting bag")
            .addInsertedItem()
            .setLootingBagItems(new Item[] { new Item(ItemID.LOOTING_BAG, 1) })
            .setNumExpectedFreeSlots(28)
            .setExpectedInsertedItems(new Item[0])
            .build();

    public static PickupItemTestCase NO_LOOTING_BAG_IN_INVENTORY =
        new PickupItemTestCaseBuilder("Player does not have looting bag")
            .addInsertedItem()
            .setInventoryItems(new Item[0])
            .setNumExpectedFreeSlots(28)
            .setExpectedInsertedItems(new Item[0])
            .build();
}
