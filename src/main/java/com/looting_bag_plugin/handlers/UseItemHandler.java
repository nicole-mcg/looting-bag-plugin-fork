package com.looting_bag_plugin.handlers;

import com.google.common.collect.ImmutableMap;
import com.looting_bag_plugin.InventoryTracker;
import com.looting_bag_plugin.looting_bag.LootingBag;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;

import javax.annotation.Nullable;
import com.google.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Slf4j
@Singleton
public class UseItemHandler {

    @Getter
    private enum NumberInputChangeCode {
        CLOSE(0),
        OPEN(7);

        private final int code;

        NumberInputChangeCode(final int code) {
            this.code = code;
        }
    }

    private static final Map<String, Integer> AmountTextToInt = ImmutableMap.of(
        "One", 1,
        "Two", 2,
        "Both", 2,
        "Five", 5
    );

    @Inject
    private Client client;

    @Inject
    private LootingBag lootingBag;

    @Inject
    private InventoryTracker inventoryTracker;

    /**
     * Used to keep track of whether the deposit X input is open.
     */
    private boolean isDepositingX;

    private int lastNumberInputChangeCode;

    /**
     * The last amount entered into the deposit X interface
     */
    @Nullable
    private Integer lastDepositedXAmount;

    @Nullable
    private Integer lastItemIdUsedOnLootingBag;

    public void onItemUsedOnLootingBag(final int itemId) {
        final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null) {
            log.error("Could not get inventory ItemContainer when using item on looting bag.");
            return;
        }

        final int amount = inventory.count(itemId);
        if (amount == 1)
        {
            lootingBag.addItem(itemId, 1);
            return;
        }

        log.debug("Starting use item on looting bag flow: id={}", itemId);
        lastItemIdUsedOnLootingBag = itemId;
    }

    public void onNumberInputChanged(final int changeCode, final String text) {
        // Number input closed
        if (changeCode != NumberInputChangeCode.CLOSE.getCode()) {
            lastNumberInputChangeCode = changeCode;
            return;
        }

        // Make sure we were depositing X
        if (isDepositingX && !text.isEmpty()) {
            lastDepositedXAmount = Integer.parseInt(text);
        }

        // If the last code was open, but it is now close,
        // then we are no longer depositing
        if (lastNumberInputChangeCode == NumberInputChangeCode.OPEN.getCode()) {
            isDepositingX = false;
            lastDepositedXAmount = null;
        }

        lastNumberInputChangeCode = changeCode;
    }

    public void onDialogOptionSelected(final String amountText) {
        if (lastItemIdUsedOnLootingBag == null) {
            return;
        }

        // Deposit a preset amount of the item
        // into the looting bag (E.g. "One", "Two", "Both", "Five")
        if (AmountTextToInt.containsKey(amountText)) {
            final int amount = AmountTextToInt.get(amountText);
            addItemToLootingBag(lastItemIdUsedOnLootingBag, amount);
            return;
        }

        // Deposit all the items with this ID into the looting bag
        if (amountText.equals("All"))
        {
            final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
            if (inventory == null) {
                log.error("Could not get inventory ItemContainer when selected put 'All' into looting bag.");
                return;
            }

            log.debug("Depositing all of item {} into looting bag.", lastItemIdUsedOnLootingBag);
            final int amount = inventory.count(lastItemIdUsedOnLootingBag);
            lootingBag.addItem(lastItemIdUsedOnLootingBag, amount);
            return;
        }

        // Start the deposit X flow
        if (amountText.equals("X")) {
            log.debug("Starting deposit X flow for item {}.", lastItemIdUsedOnLootingBag);
            isDepositingX = true;
            return;
        }

        log.error("Unknown item amount '{}' selected when depositing to looting bag.", amountText);
    }

    public void onInventoryUpdated() {
        if (lastDepositedXAmount == null || lastItemIdUsedOnLootingBag == null) {
            lastDepositedXAmount = null;
            lastItemIdUsedOnLootingBag = null;
            return;
        }

        final int numAddedToInventory = inventoryTracker.getNumAddedToInventory(lastItemIdUsedOnLootingBag);

        // Check if the amount of items added to the inventory is the same as the amount
        // entered into the deposit X interface
        if (numAddedToInventory != -lastDepositedXAmount) {
            return;
        }

        // Deposit X has completed
        log.debug("Finished deposit X flow for item {}.", lastItemIdUsedOnLootingBag);
        addItemToLootingBag(lastItemIdUsedOnLootingBag, lastDepositedXAmount);
    }

    private void addItemToLootingBag(final int itemId, final int quantity) {
        log.debug("Depositing {} of item {} into looting bag.", lastDepositedXAmount, lastItemIdUsedOnLootingBag);
        lootingBag.addItem(itemId, quantity);
        lastDepositedXAmount = null;
        lastItemIdUsedOnLootingBag = null;
        isDepositingX = false;
    }
}
