package com.lootingbag.integration;

import com.google.inject.Inject;
import com.lootingbag.integration.parameters.ItemParameters;
import com.lootingbag.integration.parameters.WorldParameters;
import com.lootingbag.integration.pickupitem.PickupItemTestCase;
import com.lootingbag.lootingbagcontainer.LootingBag;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.ItemManager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Expectations {

    @Inject
    ItemManager itemManager;

    public Item[] getExpectedLootingBagItems(
        Item[] expectedInsertedItems,
        Item[] lootingBagItems
    ) {
        if (expectedInsertedItems == null || expectedInsertedItems.length == 0) {
            return lootingBagItems;
        }

        ArrayList<Item> newLootingBagItems = new ArrayList<>(Arrays.asList(lootingBagItems));
        for (final Item insertedItem : expectedInsertedItems) {
            final int insertedItemId = insertedItem.getId();
            final int insertedItemQuantity = insertedItem.getQuantity();
            final ItemComposition itemComposition = itemManager.getItemComposition(insertedItemId);

            if (!itemComposition.isStackable()) {
                newLootingBagItems.add(insertedItem);
                continue;
            }

            final int existingItemIndex = newLootingBagItems.stream()
                .filter(item -> item.getId() == insertedItemId)
                .map(newLootingBagItems::indexOf)
                .findFirst()
                .orElse(-1);

            if (existingItemIndex == -1) {
                newLootingBagItems.add(insertedItem);
                continue;
            }

            final Item existingItem = newLootingBagItems.get(existingItemIndex);

            final int newQuantity = existingItem.getQuantity() + insertedItemQuantity;
            newLootingBagItems.set(existingItemIndex, new Item(insertedItemId, newQuantity));
        }

        return newLootingBagItems.toArray(new Item[0]);
    }

    public BigInteger getExpectedItemsValue(
        Item[] insertedItems,
        Item[] lootingBagItems
    ) {
        BigInteger previousItemsValue = Arrays.stream(lootingBagItems)
            .reduce(
                BigInteger.ZERO,
                (sum, item) -> {
                    final long price = (long) itemManager.getItemPrice(item.getId()) * item.getQuantity();
                    return sum.add(BigInteger.valueOf(price));
                },
                BigInteger::add
            );

        if (insertedItems == null || insertedItems.length == 0) {
            return previousItemsValue;
        }

        BigInteger addedValue = Arrays.stream(insertedItems)
            .map(item -> {
                final long price = (long) itemManager.getItemPrice(item.getId()) * item.getQuantity();
                return BigInteger.valueOf(price);
            })
            .reduce(BigInteger.ZERO, BigInteger::add);
        return previousItemsValue.add(addedValue);
    }
}
