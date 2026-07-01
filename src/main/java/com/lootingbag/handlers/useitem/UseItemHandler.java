package com.lootingbag.handlers.useitem;

import com.lootingbag.lootingbagcontainer.LootingBagSettings;
import com.lootingbag.state.InventoryTracker;
import com.lootingbag.lootingbagcontainer.LootingBag;
import com.lootingbag.handlers.useitem.optionsdialog.AmountDialogOption;
import com.lootingbag.handlers.useitem.optionsdialog.ILootingBagDialogListener;
import com.lootingbag.handlers.useitem.optionsdialog.LootingBagDialogTracker;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;

import javax.annotation.Nullable;
import com.google.inject.Inject;

import javax.inject.Singleton;

@Slf4j
@Singleton
public class UseItemHandler implements ILootingBagDialogListener {

    @Inject
    private Client client;

    @Inject
    private LootingBag lootingBag;

    @Inject
    private LootingBagSettings lootingBagSettings;

    @Inject
    private InventoryTracker inventoryTracker;

    @Inject
    private LootingBagDialogTracker lootingBagDialogTracker;

    @Nullable
    private Integer lastItemIdUsedOnLootingBag;

    public void initialize() {
        lootingBagDialogTracker.addOptionsDialogListener(this);
    }

    public void onItemUsedOnLootingBag(final int itemId) {
        final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null) {
            log.error("Could not get inventory ItemContainer when using item on looting bag.");
            return;
        }

        final int amountInInventory = inventory.count(itemId);
        if (lootingBagSettings.useItemDepositsAll()) {
            lootingBag.addItem(itemId, amountInInventory);
            return;
        }

        // If there's only 1 then it doesn't need to ask for an amount
        if (amountInInventory == 1)
        {
            lootingBag.addItem(itemId, 1);
            return;
        }

        log.debug("Starting use item on looting bag flow: id={}", itemId);
        lastItemIdUsedOnLootingBag = itemId;
        lootingBagDialogTracker.expectOptionsDialogToOpen();
    }

    @Override
    public void onOptionSelected(final AmountDialogOption option) {
        if (lastItemIdUsedOnLootingBag == null) {
            return;
        }

        // Deposit a specific amount of the item
        // into the looting bag (E.g. "One", "Two", "Both", "Five", "X")
        if (option.hasAmount()) {
            final int amount = option.getAmount();
            addItemToLootingBag(lastItemIdUsedOnLootingBag, amount);
            return;
        }

        // If the option isn't "All", then it's an invalid option
        if (option != AmountDialogOption.ALL) {
            log.error("Unknown amount dialog option selected when depositing to looting bag: {}", option.getOptionText());
            return;
        }

        // Deposit all of the item into the looting bag
        final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null) {
            log.error("Could not get inventory ItemContainer when selected put 'All' into looting bag.");
            return;
        }

        log.debug("Depositing all of item {} into looting bag.", lastItemIdUsedOnLootingBag);
        final int amount = inventory.count(lastItemIdUsedOnLootingBag);
        addItemToLootingBag(lastItemIdUsedOnLootingBag, amount);
    }

    private void addItemToLootingBag(final int itemId, final int quantity) {
        log.debug("Depositing {} of item {} into looting bag.", quantity, lastItemIdUsedOnLootingBag);
        lootingBag.addItem(itemId, quantity);
        lastItemIdUsedOnLootingBag = null;
    }

}
