package com.lootingbag.testutil;

import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;

import java.util.Random;
import java.util.stream.Stream;

public class RandomUtils {

    private static final int INVENTORY_SIZE = 28;

    private static final Random random = new Random();

    public static Item getRandomItem() {
        return getRandomItem(random.nextBoolean());
    }

    public static Item getRandomItem(boolean itemExists) {
        if (!itemExists) {
            return new Item(MockUtils.NON_EXISTENT_ITEM_ID, 0);
        }

        boolean isStackable = random.nextBoolean();

        int itemId = getRandomItemId();
        int quantity = isStackable
            ? getRandomItemQuantity()
            : 1;
        return new Item(itemId, quantity);
    }

    public static WorldView getRandomWorldView() {
        return MockUtils.createWorldView(
            random.nextInt(100),
            random.nextInt(4),
            random.nextInt(10_000),
            random.nextInt(10_000),
            random.nextInt(10_000),
            random.nextInt(10_000)
        );
    }

    public static WorldPoint getRandomWorldPoint() {
        return getRandomWorldPoint(getRandomWorldView());
    }

    public static WorldPoint getRandomWorldPoint(WorldView worldView) {
        int baseX = worldView.getBaseX();
        int baseY = worldView.getBaseY();

        return new WorldPoint(
            random.nextInt(worldView.getSizeX()) + baseX,
            random.nextInt(worldView.getSizeY()) + baseY,
            worldView.getPlane()
        );
    }

    public static int getRandomItemId() {
        return random.nextInt(20_000);
    }

    public static String randomItemName() {
        return "Item " + random.nextInt(100);
    }

    public static <T extends Enum<T>> T randomEnum(Class<T> clazz) {
        T[] values = clazz.getEnumConstants();
        int x = random.nextInt(values.length);
        return values[x];
    }

    public static ItemContainer createRandomItemContainer() {
        return createRandomItemContainer(INVENTORY_SIZE);
    }

    public static ItemContainer createRandomItemContainer(int size) {
        Item[] items = Stream.generate(RandomUtils::getRandomItem)
            .limit(size)
            .toArray(Item[]::new);
        return MockUtils.createMockContainer(items);
    }

    public static int getRandomItemQuantity() {
        return random.nextInt(Integer.MAX_VALUE);
    }

    public static int getRandomItemPrice() {
        return random.nextInt(Integer.MAX_VALUE);
    }
}
