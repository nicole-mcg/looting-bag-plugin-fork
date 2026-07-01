package com.lootingbag.handlers.pickup;

import com.google.gson.Gson;
import com.lootingbag.state.InventoryTracker;
import com.lootingbag.lootingbagcontainer.LootingBag;
import com.lootingbag.lootingbagcontainer.LootingBagSettings;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.game.ItemManager;

import com.google.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class PickupItemHandler {

    @Inject
    private Gson gson;

    @Inject
    private Client client;

    @Inject
    private ItemManager itemManager;

    @Inject
    private InventoryTracker inventoryTracker;

    @Inject
    private LootingBag lootingBag;

    @Inject
    private LootingBagSettings lootingBagSettings;

    @Inject
    private TelegrabHandler telegrabHandler;

    private PickupAction lastPickUpAction;
    private final ArrayList<PickupAction> possibleSuppliesPickupActions = new ArrayList<>();

    public boolean isPickupMenuOption(final MenuOptionClicked event) {
        return event.getMenuAction() == MenuAction.GROUND_ITEM_THIRD_OPTION
            && event.getMenuOption().equals("Take");
    }

    public void onTakeItemClicked(final MenuOptionClicked event) {
        final WorldPoint point = WorldPoint.fromScene(client, event.getParam0(), event.getParam1(), client.getPlane());
        lastPickUpAction = new PickupAction(event.getId(), point);
    }

    public void processPossibleSuppliesPickupActions() {
        possibleSuppliesPickupActions.removeIf(action -> {
            if (action.getTicksSincePickup() < 1) {
                action.incrementTicksSincePickup();
                return false;
            }

            // This pickup event hasn't been satisfied
            // Whatever we picked up must have gone into our looting bag
            log.debug("Item was not supply and got added to looting bag: {}", getItemName(action.getItemId()));
            lootingBag.addItem(action.getItemId(), action.getQuantity());
            return true;
        });
    }

    public void onItemDespawned(final ItemDespawned event) {
        // Check if player has open looting bag in inventory
        final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null || !inventory.contains(ItemID.LOOTING_BAG_22586)) {
            return;
        }

        if (lastPickUpAction == null || !lastPickUpAction.matchesItemDespawnedEvent(event)) {
            return;
        }

        final WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        final WorldPoint groundItemLocation = event.getTile().getWorldLocation();

        // Check that the item despawned on the same tile the player is on
        final boolean isPlayerPickup = groundItemLocation.equals(playerLocation);

        // Check that the item despawned on the same tile of telegrab target and same cycle telegrab 1ends
        final boolean isTelegrabPickup = telegrabHandler.isItemDespawnTelegrab(groundItemLocation);

        if (!isPlayerPickup && !isTelegrabPickup) {
            return;
        }

        final int itemId = event.getItem().getId();
        final int quantity = event.getItem().getQuantity();
        final ItemComposition itemComposition = itemManager.getItemComposition(itemId);

        // This might be a "supply"
        // If the setting is enabled and the item isn't stackable
        final boolean isPossibleSupply = !itemComposition.isStackable()
           && lootingBagSettings.doSuppliesGoIntoInventory();
        if (isPossibleSupply) {
            log.debug("Possibly picked up a supply: {}", itemComposition.getName());
            lastPickUpAction.setQuantity(quantity);
            possibleSuppliesPickupActions.add(lastPickUpAction);
            return;
        }

        // We've picked up an item into our looting bag!
        lastPickUpAction = null;
        final boolean isQuantityConfirmed = quantity < 65535;
        lootingBag.addItem(
            event.getItem().getId(),
            quantity,
            isQuantityConfirmed
        );
    }

    public void onInventoryUpdated() {
        if (!lootingBagSettings.doSuppliesGoIntoInventory()) {
            return;
        }

        final List<PickupAction> satisfiedPickupActions = possibleSuppliesPickupActions
            .stream()
            .filter(this::isPotentialSupplyPickupActionSatisfied)
            .collect(Collectors.toList());

        log.debug("Clearing {} of {} pickup actions", satisfiedPickupActions.size(), possibleSuppliesPickupActions.size());
        possibleSuppliesPickupActions.removeAll(satisfiedPickupActions);
    }

    private String getItemName(final int itemId) {
        return itemManager.getItemComposition(itemId).getName();
    }

    private boolean isPotentialSupplyPickupActionSatisfied(final PickupAction action) {
        final WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        if (!action.getWorldPoint().equals(playerLocation)) {
            return false;
        }

        final int itemId = action.getItemId();
        final int numAddedToInventory = inventoryTracker.getNumAddedToInventory(itemId);

        // We picked up a supply, and it didn't go into the looting bag
        // Only unnoted items can be considered supplies
        if (numAddedToInventory == 1) {
            log.debug("Supply got added to inventory: {}", getItemName(itemId));
            return true;
        }

        return false;
    }
}
