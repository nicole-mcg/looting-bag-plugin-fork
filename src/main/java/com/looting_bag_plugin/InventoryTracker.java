package com.looting_bag_plugin;

import com.google.common.collect.Multimap;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashMap;

@Singleton
public class InventoryTracker {

    private Item[] lastInventoryItems = new Item[0];
    private final HashMap<Integer, Integer> lastItemUpdates = new HashMap<>();

    public void updateInventoryItems(final ItemContainer inventory) {
        lastItemUpdates.clear();

        Arrays.stream(lastInventoryItems)
            .forEach(item -> {
                final int itemId = item.getId();
                final int lastCount = item.getQuantity();
                final int newCount = inventory.count(itemId);
                final int difference = newCount - lastCount;

                lastItemUpdates.put(itemId, difference);
            });

        lastInventoryItems = inventory.getItems();
    }

    public int getNumAddedToInventory(final int itemId) {
        return lastItemUpdates.getOrDefault(itemId, 0);
    }
}
