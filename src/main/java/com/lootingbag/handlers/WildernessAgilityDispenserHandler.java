package com.lootingbag.handlers;

import com.google.common.collect.ImmutableSet;
import com.lootingbag.lootingbagcontainer.LootingBag;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.client.game.ItemManager;

import com.google.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.runelite.api.ItemID.*;

@Singleton
public class WildernessAgilityDispenserHandler {
    private static final Pattern DISPENSER_MESSAGE_REGEX = Pattern.compile("You have been awarded <[A-Za-z0-9=/]+>(\\d+) x ([ a-zA-Z(4)]+)<[A-Za-z0-9=/]+> and <[A-Za-z0-9=/]+>(\\d+) x ([ a-zA-Z]+)<[A-Za-z0-9=/]+> from the Agility dispenser.");
    private static final Pattern DISPENSER_MESSAGE_EXTRA_REGEX = Pattern.compile("You have been awarded <[A-Za-z0-9=/]+>(\\d+) x ([ a-zA-Z(4)]+)<[A-Za-z0-9=/]+> and <[A-Za-z0-9=/]+>(\\d+) x ([ a-zA-Z]+)<[A-Za-z0-9=/]+>, and an extra <[A-Za-z0-9=/]+>[ a-zA-Z(4)]+<[A-Za-z0-9=/]+> from the Agility dispenser.");

    @Inject
    private Client client;

    @Inject
    private ItemManager itemManager;

    @Inject
    private LootingBag lootingBag;

    private final HashMap<String, Integer> nameToItemId = new HashMap<>();

    // Items from https://oldschool.runescape.wiki/w/Agility_dispenser
    static final ImmutableSet<Integer> ITEM_IDS = ImmutableSet.of(
        // All Laps
        BLIGHTED_ANGLERFISH, BLIGHTED_MANTA_RAY, BLIGHTED_KARAMBWAN, BLIGHTED_SUPER_RESTORE4,
        MITHRIL_PLATESKIRT, MITHRIL_PLATELEGS, ADAMANT_PLATEBODY, RUNE_MED_HELM, ADAMANT_FULL_HELM, ADAMANT_PLATELEGS,

        // Laps 1-15
        STEEL_PLATEBODY,

        // Lap 1-30
        MITHRIL_CHAINBODY,

        // Lap 16-60+
        RUNE_CHAINBODY, RUNE_KITESHIELD
    );

    public void onGameMessage(final String message) {
        final Matcher defaultMatcher = DISPENSER_MESSAGE_REGEX.matcher(message);
        final Matcher extraMatcher = DISPENSER_MESSAGE_EXTRA_REGEX.matcher(message);
        final boolean matches = defaultMatcher.matches() || extraMatcher.matches();
        if (!matches) {
            return;
        }

        // Used wilderness agility dispenser
        final Matcher matcher = defaultMatcher.matches() ? defaultMatcher : extraMatcher;

        final int quantity = Integer.parseInt(matcher.group(1));
        final String item = extraMatcher.group(2);
        final int quantity2 = Integer.parseInt(extraMatcher.group(3));
        final String item2 = extraMatcher.group(4);

        addWildernessItems(quantity, item, quantity2, item2);
    }

    private Integer nameToItemId(final String name) {
        if(nameToItemId.isEmpty()) {
            // Populate name to item ID map
            for (final Integer itemID : ITEM_IDS) {
                nameToItemId.put(itemManager.getItemComposition(itemID).getName(), itemID);
            }
        }

        // All wilderness agility items noted so get noted version
        return itemManager.getItemComposition(nameToItemId.get(name)).getLinkedNoteId();
    }

    private void addWildernessItems(final int quantity, final String itemName, final int quantity2, final String itemName2) {
        // Check if player has open looting bag in inventory
        final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        if (inventory == null || !inventory.contains(ItemID.LOOTING_BAG_22586)) {
            return;
        }

        final int itemId = nameToItemId(itemName);
        final int itemId2 = nameToItemId(itemName2);

        lootingBag.addItem(itemId, quantity);
        lootingBag.addItem(itemId2, quantity2);
    }
}
