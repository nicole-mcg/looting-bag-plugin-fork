package com.lootingbag.integration;

import com.lootingbag.LootingBagPlugin;
import com.lootingbag.handlers.pickup.TelegrabHandler;
import com.lootingbag.integration.parameters.ItemParameters;
import com.lootingbag.integration.parameters.WorldParameters;
import com.lootingbag.integration.pickupitem.PickupItemTestCase;
import com.lootingbag.integration.pickupitem.telegrab.TelegrabItemTestCase;
import com.lootingbag.testutil.MockUtils;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ProjectileMoved;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Random;

import static org.mockito.Mockito.when;

public class Simulator {

    private static final Random random = new Random();

    @Inject
    Client client;

    @Inject
    LootingBagPlugin lootingBagPlugin;

    /**
     * Simulate the player taking a ground item and the item despawning
     */
    public void pickupGroundItem(
        ItemParameters itemParameters,
        WorldParameters worldParameters,
        WorldPoint groundItemLocation,
        WorldPoint playerEndingLocation
    ) {
        // Simulate selecting the 'Take' option on the ground item
        selectTakeGroundItem(
            itemParameters,
            groundItemLocation,
            worldParameters.worldView
        );

        // Setup the player location when the item despawns
        setPlayerLocation(playerEndingLocation);

        // Simulate the item despawning
        despawnGroundItem(itemParameters.itemId, itemParameters.getQuantity(), groundItemLocation);

        if (!worldParameters.suppliesGoIntoInventory) {
            return;
        }

        if (!itemParameters.isSupply) {
            // Simulate 2 game ticks passing so that potential supplies are processed
            lootingBagPlugin.onGameTick(null);
            lootingBagPlugin.onGameTick(null);
            return;
        }

        // Simulate the item being added to the inventory
        addItemToInventory(itemParameters.itemId, itemParameters.getQuantity());
    }

    /**
     * Simulate the player telegrabbing a ground item and the item despawning
     */
    public void telegrabGroundItem(
        ItemParameters itemParameters,
        WorldParameters worldParameters,
        WorldPoint groundItemLocation
    ) {
        int telegrabEndCycle = random.nextInt(10) + 1;

        // Simulate selecting the 'Cast telekinetic grab' option on the ground item
        selectTelegrabGroundItem(
            itemParameters,
            groundItemLocation,
            worldParameters.worldView
        );

        // Start the telegrab projectile
        startTelegrabProjectile(
            worldParameters.playerLocation,
            telegrabEndCycle
        );

        // Setup the player location when the item despawns
//        WorldPoint playerEndingLocation = parameters.playerEndingLocation;
//        setPlayerLocation(playerEndingLocation);

        when(client.getGameCycle()).thenReturn(telegrabEndCycle);

        // Simulate the item despawning
        despawnGroundItem(itemParameters.itemId, itemParameters.getQuantity(), groundItemLocation);

        if (!worldParameters.suppliesGoIntoInventory) {
            return;
        }

        if (!itemParameters.isSupply) {
            // Simulate 2 game ticks passing so that potential supplies are processed
            lootingBagPlugin.onGameTick(null);
            lootingBagPlugin.onGameTick(null);
            return;
        }

        // Simulate the item being added to the inventory
        addItemToInventory(itemParameters.itemId, itemParameters.getQuantity());
    }

    public void setPlayerLocation(WorldPoint worldPoint) {
        LocalPoint localPoint = MockUtils.createLocalPoint(0, 0);
        setPlayerLocation(worldPoint, localPoint);
    }

    public void setPlayerLocation(WorldPoint worldPoint, LocalPoint localPoint) {
        Player player = MockUtils.createPlayer(worldPoint);
        when(player.getLocalLocation()).thenReturn(localPoint);

        when(client.getLocalPlayer()).thenReturn(player);
        when(client.getPlane()).thenReturn(worldPoint.getPlane());
    }

    public void selectTakeGroundItem(
        ItemParameters itemParameters,
        WorldPoint groundItemLocation,
        WorldView worldView
    ) {
        MenuAction menuAction = MenuAction.GROUND_ITEM_THIRD_OPTION;
        String menuOption = "Take";
        MenuOptionClicked menuOptionClicked = MockUtils.createMenuOptionClickedEvent(
            menuAction,
            menuOption,
            itemParameters.itemId,
            groundItemLocation,
            worldView
        );
        when(client.getPlane()).thenReturn(groundItemLocation.getPlane());
        lootingBagPlugin.onMenuOptionClicked(menuOptionClicked);
    }

    public void startTelegrabProjectile(WorldPoint telegrabStartLocation, int endCycle) {
        ProjectileMoved projectileMoved = MockUtils.createProjectileMovedEvent(
            TelegrabHandler.TELEGRAB_PROJECTILE_ID,
            telegrabStartLocation,
            endCycle
        );
        lootingBagPlugin.onProjectileMoved(projectileMoved);
    }

    public void selectTelegrabGroundItem(
        ItemParameters itemParameters,
        WorldPoint groundItemLocation,
        WorldView worldView
    ) {
        MenuAction menuAction = MenuAction.WIDGET_TARGET_ON_GROUND_ITEM;
        String targetString = "Telekinetic Grab -> " + itemParameters.getItemName();
        MenuOptionClicked menuOptionClicked = MockUtils.createMenuOptionClickedEvent(
            menuAction,
            "Cast",
            targetString,
            itemParameters.itemId,
            groundItemLocation,
            worldView
        );
        when(client.getPlane()).thenReturn(groundItemLocation.getPlane());
        lootingBagPlugin.onMenuOptionClicked(menuOptionClicked);
    }

    public void despawnGroundItem(int itemId, int quantity, WorldPoint itemLocation) {
        // Fire the item despawned event
        ItemDespawned itemDespawned = MockUtils.createItemDespawnedEvent(
            itemId,
            quantity,
            itemLocation
        );
        lootingBagPlugin.onItemDespawned(itemDespawned);
    }

    public void addItemToInventory(int itemId, int quantity) {
        ItemContainer previousInventory = client.getItemContainer(InventoryID.INVENTORY);

        Item[] newInventoryItems = Arrays.copyOf(previousInventory.getItems(), previousInventory.getItems().length + 1);
        newInventoryItems[newInventoryItems.length - 1] = new Item(itemId, quantity);

        ItemContainer newInventory = MockUtils.createMockContainer(newInventoryItems);
        lootingBagPlugin.onItemContainerChanged(new ItemContainerChanged(InventoryID.INVENTORY.getId(), newInventory));
    }
}
