package com.lootingbag.integration;

import com.google.gson.Gson;
import net.runelite.api.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Assertions {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void assertLootingBagItems(Item[] actualItems, Item[] expectedItems) {
        final ArrayList<Item> extraItems = new ArrayList<>();
        final ArrayList<Item> missingItems = new ArrayList<>();

        final int numItemsToCheck = Math.max(actualItems.length, expectedItems.length);
        for (int i = 0; i < numItemsToCheck; i++) {
            final Item actualItem = i < actualItems.length ? actualItems[i] : null;
            final Item expectedItem = i < expectedItems.length ? expectedItems[i] : null;

            if (actualItem == null) {
                missingItems.add(expectedItem);
                continue;
            }

            if (expectedItem == null) {
                extraItems.add(actualItem);
                continue;
            }

            if (actualItem.getId() != expectedItem.getId() || actualItem.getQuantity() != expectedItem.getQuantity()) {
                missingItems.add(expectedItem);
                continue;
            }
        }

        if (extraItems.isEmpty() && missingItems.isEmpty() ) {
            return;
        }

//        final List<Integer> missingItemIndexes = IntStream.range(0, expectedItems.length)
//            .filter(i -> missingItems.contains(expectedItems[i]))
//            .collect(Collectors.toList());
//        final List<Integer> extraItemIndexes = IntStream.range(0, actualItems.length)
//            .filter(i -> extraItems.contains(actualItems[i]))
//            .collect(Collectors.toList());

        org.junit.jupiter.api.Assertions.fail("noodle");
        return;

//        final String message = getErrorString(
//            expectedItems,
//            actualItems,
//            missingItemIndexes,
//            extraItemIndexes
//        );
//        org.junit.jupiter.api.Assertions.fail(message);
    }

    private static String getErrorString(
        Item[] expectedItems,
        Item[] actualItems,
        List<Integer> missingItemIndexes,
        List<Integer> extraItemIndexes
    ) {
        Gson gson = new Gson();
        final StringBuilder s = new StringBuilder();
        s.append(ANSI_WHITE);
        s.append("incorrect items: ");

        return s.toString();
    }
}
