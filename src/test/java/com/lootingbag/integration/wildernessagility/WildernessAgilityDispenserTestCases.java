package com.lootingbag.integration.wildernessagility;

import com.lootingbag.integration.LootingBagTestCaseBuilder;
import com.lootingbag.integration.parameters.ItemParameters;
import com.lootingbag.testutil.RandomUtils;
import net.runelite.api.Item;
import net.runelite.api.ItemID;

import java.util.stream.Stream;

@SuppressWarnings("unused")
public class WildernessAgilityDispenserTestCases {

    public static WildernessAgilityDispenserTestCase RECEIVED_ITEMS_FROM_DISPENSER_INTO_EMPTY_LOOTING_BAG =
        getBuilder("Received items from dispenser into empty looting bag")
            .addInsertedItem(new ItemParameters() {{
                itemId = ItemID.BLIGHTED_ANGLERFISH;
            }})
            .addInsertedItem(new ItemParameters() {{
                itemId = ItemID.STEEL_PLATEBODY;
            }})
            .setExpectedInsertedItems(new Item[] {
                new Item(ItemID.BLIGHTED_ANGLERFISH, 1),
                new Item(ItemID.STEEL_PLATEBODY, 1)
            })
            .build();

    public static WildernessAgilityDispenserTestCase RECEIVED_ITEMS_FROM_DISPENSER_WITH_EXISTING_ITEMS_IN_LOOTING_BAG =
        getBuilder("Received items from dispenser with existing items in looting bag")
            .setLootingBagItems(new Item[] {
                new Item(ItemID.RUNE_MED_HELM, 1),
                new Item(ItemID.BLIGHTED_MANTA_RAY, 1)
            })
            .addInsertedItem(new ItemParameters() {{
                itemId = ItemID.RUNE_MED_HELM;
            }})
            .addInsertedItem(new ItemParameters() {{
                itemId = ItemID.BLIGHTED_MANTA_RAY;
            }})
            .setExpectedInsertedItems(new Item[] {
                new Item(ItemID.RUNE_MED_HELM, 1),
                new Item(ItemID.BLIGHTED_MANTA_RAY, 1),
            })
            .build();

    public static WildernessAgilityDispenserTestCase RECEIVED_ITEMS_FROM_DISPENSER_INTO_FULL_LOOTING_BAG =
        getBuilder("Received items from dispenser into full looting bag")
            .setLootingBagItems(Stream
                .generate(() -> RandomUtils.getRandomItem(true))
                .limit(28)
                .toArray(Item[]::new)
            )
            .addInsertedItem(new ItemParameters() {{
                itemId = ItemID.ADAMANT_PLATEBODY;
            }})
            .addInsertedItem(new ItemParameters() {{
                itemId = ItemID.BLIGHTED_KARAMBWAN;
            }})
            .setExpectedInsertedItems(new Item[0])
            .build();

    public static WildernessAgilityDispenserTestCase RECEIVED_NON_DISPENSER_ITEMS =
        getBuilder("Received non-dispenser items")
            .addInsertedItem()
            .addInsertedItem()
            .setExpectedInsertedItems(new Item[0])
            .build();

    private static LootingBagTestCaseBuilder<WildernessAgilityDispenserTestCase> getBuilder(String name) {
        return new LootingBagTestCaseBuilder<>(new WildernessAgilityDispenserTestCase(), name);
    }
}
