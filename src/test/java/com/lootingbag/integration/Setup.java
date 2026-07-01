package com.lootingbag.integration;

import com.google.inject.Inject;
import com.lootingbag.LootingBagPlugin;
import com.lootingbag.integration.parameters.ItemParameters;
import com.lootingbag.integration.parameters.WorldParameters;
import com.lootingbag.lootingbagcontainer.LootingBag;
import com.lootingbag.testutil.MockUtils;
import com.lootingbag.testutil.RandomUtils;
import net.runelite.api.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.game.ItemManager;

import java.util.Arrays;
import java.util.Random;

import static com.lootingbag.lootingbagcontainer.LootingBagSettings.LOOTING_BAG_SUPPLIES_SETTING_VARBIT_ID;
import static org.mockito.Mockito.when;

public class Setup {

    private static final Random random = new Random();

    @Inject
    Client client;

    @Inject
    ItemManager itemManager;

    @Inject
    LootingBagPlugin lootingBagPlugin;

    @Inject
    LootingBag lootingBag;

    @Inject
    Simulator simulator;

    public void setup(LootingBagTestCase parameters) {
        lootingBagPlugin.startUp();

        setupWorld(parameters.worldParameters);
        setupInventory(parameters.inventoryItems);

        // Set up the item compositions for the items in the looting bag
        Arrays.stream(parameters.lootingBagItems)
            .forEach(this::setupRandomItem);
        Arrays.stream(parameters.insertedItems).forEach(this::setupItem);
        setupLootingBag(parameters.lootingBagItems);
    }

    public void setupWorld(WorldParameters parameters) {
        setupWorldView(parameters.worldView);
        setInWilderness(parameters.inWilderness);
        setSuppliesGoInInventorySetting(parameters.suppliesGoIntoInventory);
        simulator.setPlayerLocation(
            parameters.playerLocation,
            parameters.localPlayerLocation
        );
    }

    public void setupWorldView(WorldView worldView) {
        when(client.getTopLevelWorldView()).thenReturn(worldView);
        when(client.getWorldView(worldView.getId())).thenReturn(worldView);
    }

    public void setupInventory(Item[] items) {
        ItemContainer inventoryContainer = MockUtils.createMockContainer(items);
        when(client.getItemContainer(InventoryID.INVENTORY))
            .thenReturn(inventoryContainer);

        ItemContainerChanged itemContainerChangedEvent = new ItemContainerChanged(
            InventoryID.INVENTORY.getId(),
            inventoryContainer
        );
        lootingBagPlugin.onItemContainerChanged(itemContainerChangedEvent);
    }

    public void setupLootingBag(Item[] items) {
        ItemContainer lootingBagContainer = MockUtils.createMockContainer(items);
        lootingBag.syncItems(lootingBagContainer);
    }

    public void setInWilderness(boolean inWilderness) {
        when(client.getVarbitValue(Varbits.IN_WILDERNESS))
            .thenReturn(inWilderness ? 1 : 0);
    }

    public void setSuppliesGoInInventorySetting(boolean suppliesGoInInventory) {
        when(client.getVarbitValue(LOOTING_BAG_SUPPLIES_SETTING_VARBIT_ID))
            .thenReturn(suppliesGoInInventory ? 1 : 0);
    }

    public void setupItem(ItemParameters itemParameters) {
        // Set up the item composition for the item being picked up
        int itemId = itemParameters.itemId;
        ItemComposition itemComposition = MockUtils.createItemComposition(
            itemId,
            itemParameters.itemName,
            itemParameters.value,
            itemParameters.isTradeable,
            itemParameters.isStackable,
            itemParameters.hasNote
        );

        when(itemManager.getItemComposition(itemId))
            .thenReturn(itemComposition);
        when(itemManager.getItemPrice(itemId))
            .thenReturn(itemParameters.value);

        if (itemParameters.hasNote) {
            int noteId = itemComposition.getLinkedNoteId();
            setupItem(new ItemParameters() {{
                itemId = noteId;
                itemName = itemParameters.itemName;
                value = itemParameters.value;
                isTradeable = itemParameters.isTradeable;
                isStackable = true;
                hasNote = false;
            }});
        }
    }

    public void setupRandomItem(Item item) {
        int itemPrice = RandomUtils.getRandomItemPrice();
        setupItem(new ItemParameters() {{
            itemId = item.getId();
            value = itemPrice;
            isTradeable = true;
            isStackable = item.getQuantity() > 1;
            hasNote = random.nextBoolean();
        }});
    }
}
