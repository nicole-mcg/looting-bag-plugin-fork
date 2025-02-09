package com.lootingbag.state;

import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Singleton
public class InventoryTracker {

    private Item[] lastInventoryItems = new Item[0];
    private final HashMap<Integer, Integer> lastItemUpdates = new HashMap<>();

    public void updateInventoryItems(final ItemContainer inventory) {
        lastItemUpdates.clear();

        final Item[] nextInventoryItems = Arrays.stream(inventory.getItems())
            .filter(item -> item.getId() != -1)
            .toArray(Item[]::new);

        final IntStream allItemIds = Stream
            .concat(
                Arrays.stream(lastInventoryItems),
                Arrays.stream(nextInventoryItems)
            )
            .mapToInt(Item::getId);

        allItemIds.forEach(itemId -> {
            final int lastCount = Arrays.stream(lastInventoryItems)
                .filter(item -> item.getId() == itemId)
                .mapToInt(Item::getQuantity)
                .sum();
            final int newCount = inventory.count(itemId);
            final int difference = newCount - lastCount;

            lastItemUpdates.put(itemId, difference);
        });

        lastInventoryItems = nextInventoryItems;
    }

    public int getNumAddedToInventory(final int itemId) {
        return lastItemUpdates.getOrDefault(itemId, 0);
    }

    public boolean wasAddedToInventory(final int itemId) {
        return lastItemUpdates.getOrDefault(itemId, 0) > 0;
    }
}
