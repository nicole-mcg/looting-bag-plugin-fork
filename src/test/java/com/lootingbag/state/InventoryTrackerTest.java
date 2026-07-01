package com.lootingbag.state;

import com.lootingbag.MockedTestBase;
import com.lootingbag.testutil.MockUtils;
import com.lootingbag.testutil.RandomUtils;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InventoryTrackerTest extends MockedTestBase {

    private static final Random random = new Random();

    @Inject
    InventoryTracker inventoryTracker;

    @ParameterizedTest(name = "InventoryTracker should correctly track the number of items added/removed from the inventory")
    @ArgumentsSource(RandomInventoriesProvider.class)
    void testWorldsNotIgnored(ItemContainer inventory) {
        inventoryTracker.updateInventoryItems(inventory);

        ItemContainer modifiedInventory = getModifiedInventory(inventory);
        IntStream allItemIds =
            Stream.concat(
                Arrays.stream(inventory.getItems()),
                Arrays.stream(modifiedInventory.getItems())
            )
            .mapToInt(Item::getId);
        Map<Integer, Integer> itemUpdates = allItemIds
            .distinct()
            .collect(
                HashMap::new,
                (map, itemId) -> {
                    final int lastCount = Arrays.stream(inventory.getItems())
                        .filter(item -> item.getId() == itemId)
                        .mapToInt(Item::getQuantity)
                        .sum();
                    final int newCount = modifiedInventory.count(itemId);
                    final int difference = newCount - lastCount;

                    map.put(itemId, difference);
                },
                HashMap::putAll
            );

        inventoryTracker.updateInventoryItems(modifiedInventory);

        itemUpdates.keySet().forEach((itemId) -> {
            int expectedDifference = modifiedInventory.count(itemId) - inventory.count(itemId);
            int actualDifference = inventoryTracker.getNumAddedToInventory(itemId);
            assertEquals(expectedDifference, actualDifference);
        });
    }

    private ItemContainer getModifiedInventory(ItemContainer inventory) {
        Item[] items = inventory.getItems();
        Item[] modifiedItems = Arrays.stream(items)
            .map(item -> {
                boolean itemExists = item.getId() != MockUtils.NON_EXISTENT_ITEM_ID;
                boolean isStackable = item.getQuantity() > 1;

                boolean itemRemoved = itemExists && random.nextBoolean();
                if (itemRemoved) {
                    return new Item(MockUtils.NON_EXISTENT_ITEM_ID, 0);
                }

                boolean itemAdded = !itemExists && random.nextBoolean();
                if (itemAdded) {
                    return RandomUtils.getRandomItem();
                }

                boolean quantityChanged = isStackable && random.nextBoolean();
                if (quantityChanged) {
                    int newQuantity = random.nextInt(Integer.MAX_VALUE);
                    return new Item(item.getId(), newQuantity);
                }

                return item;
            })
            .toArray(Item[]::new);

        return MockUtils.createMockContainer(modifiedItems);
    }

    private static class RandomInventoriesProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            // Create 10 random inventories
            return Stream.generate(RandomUtils::createRandomItemContainer)
                .limit(10)
                .map(Arguments::of);
        }
    }
}
