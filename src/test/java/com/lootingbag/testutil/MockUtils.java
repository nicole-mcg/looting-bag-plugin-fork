package com.lootingbag.testutil;

import com.google.inject.Inject;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ProjectileMoved;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockUtils {
    public static final int NON_EXISTENT_ITEM_ID = -1;

    public static ItemContainer createMockContainer(Item[] items) {
        ItemContainer mockItemContainer = mock(ItemContainer.class);
        when(mockItemContainer.getItems()).thenReturn(items);
        when(mockItemContainer.count(anyInt())).thenAnswer(invocation -> {
            int itemId = invocation.getArgument(0);
            return Arrays.stream(items)
                .filter(item -> item.getId() == itemId)
                .reduce(
                    0,
                    (sum, item) -> sum + item.getQuantity(), Integer::sum
                );
        });
        when(mockItemContainer.contains(anyInt())).thenAnswer(invocation -> {
            int itemId = invocation.getArgument(0);
            return Arrays.stream(items).anyMatch(item -> item.getId() == itemId);
        });
        return mockItemContainer;
    }

    public static ItemDespawned createItemDespawnedEvent(
        int itemId,
        int quantity,
        WorldPoint worldPoint
    ) {
        Tile tile = mock(Tile.class);
        when(tile.getWorldLocation()).thenReturn(worldPoint);

        TileItem tileItem = mock(TileItem.class);
        when(tileItem.getId()).thenReturn(itemId);
        when(tileItem.getQuantity()).thenReturn(quantity);

        return new ItemDespawned(tile, tileItem);
    }

    public static MenuOptionClicked createMenuOptionClickedEvent(
        MenuAction menuAction,
        String menuOption,
        WorldPoint worldPoint,
        WorldView worldView
    ) {
        return createMenuOptionClickedEvent(
            menuAction,
            menuOption,
            RandomUtils.getRandomItemId(),
            worldPoint,
            worldView
        );
    }

    public static MenuOptionClicked createMenuOptionClickedEvent(
        MenuAction menuAction,
        String menuOption,
        int itemId,
        WorldPoint worldPoint,
        WorldView worldView
    ) {
        return createMenuOptionClickedEvent(
            menuAction,
            menuOption,
            "",
            itemId,
            worldPoint,
            worldView
        );
    }

    public static MenuOptionClicked createMenuOptionClickedEvent(
        MenuAction menuAction,
        String menuOption,
        String targetString,
        int itemId,
        WorldPoint worldPoint,
        WorldView worldView
    ) {
        int sceneX = worldPoint.getX() - worldView.getBaseX();
        int sceneY = worldPoint.getY() - worldView.getBaseY();

        MenuEntry menuEntry = mock(MenuEntry.class);
        when(menuEntry.getType()).thenReturn(menuAction);
        when(menuEntry.getOption()).thenReturn(menuOption);
        when(menuEntry.getParam0()).thenReturn(sceneX);
        when(menuEntry.getParam1()).thenReturn(sceneY);
        when(menuEntry.getIdentifier()).thenReturn(itemId);
        when(menuEntry.getTarget()).thenReturn(targetString);
        return new MenuOptionClicked(menuEntry);
    }

    public static ProjectileMoved createProjectileMovedEvent(
        int projectileId,
        WorldPoint location,
        int endCycle
    ) {
        Projectile projectile = mock(Projectile.class);
        when(projectile.getId()).thenReturn(projectileId);
        when(projectile.getX1()).thenReturn(location.getX());
        when(projectile.getY1()).thenReturn(location.getY());
        when(projectile.getEndCycle()).thenReturn(endCycle);

        ProjectileMoved projectileMoved = new ProjectileMoved();
        projectileMoved.setProjectile(projectile);
        return projectileMoved;
    }

    public static ChatMessage createChatMessageEvent(String message) {
        ChatMessage chatMessage = mock(ChatMessage.class);
        when(chatMessage.getMessage()).thenReturn(message);
        when(chatMessage.getType()).thenReturn(ChatMessageType.GAMEMESSAGE);
        return chatMessage;
    }

    public static ItemComposition createItemComposition(
        int itemId,
        String itemName,
        int itemValue,
        boolean isTradeable,
        boolean isStackable,
        boolean hasNote
    ) {
        ItemComposition itemComposition = mock(ItemComposition.class);

        when(itemComposition.getName()).thenReturn("Item " + itemId);

        when(itemComposition.getId()).thenReturn(itemId);
        when(itemComposition.getName()).thenReturn(itemName);
        when(itemComposition.getPrice()).thenReturn(itemValue);
        when(itemComposition.isTradeable()).thenReturn(isTradeable);
        when(itemComposition.isStackable()).thenReturn(isStackable);
        when(itemComposition.getLinkedNoteId()).thenReturn(hasNote ? itemId + 1 : NON_EXISTENT_ITEM_ID);

        return itemComposition;
    }

    public static WorldView createWorldView(
        int id,
        int plane,
        int baseX,
        int baseY,
        int sizeX,
        int sizeY
    ) {
        WorldView worldView = mock(WorldView.class);
        when(worldView.getId()).thenReturn(id);
        when(worldView.getPlane()).thenReturn(plane);
        when(worldView.getBaseX()).thenReturn(baseX);
        when(worldView.getBaseY()).thenReturn(baseY);
        when(worldView.getSizeX()).thenReturn(sizeX);
        when(worldView.getSizeY()).thenReturn(sizeY);
        return worldView;
    }

    public static LocalPoint createLocalPoint(int x, int y) {
        WorldView worldView = mock(WorldView.class);
        return createLocalPoint(x, y, worldView);
    }

    public static LocalPoint createLocalPoint(int x, int y, WorldView worldView) {
        return new LocalPoint(x, y, worldView);
    }

    public static Player createPlayer(WorldPoint worldPoint) {
        return createPlayer(worldPoint, createLocalPoint(0, 0));
    }

    public static Player createPlayer(
        WorldPoint worldPoint,
        LocalPoint localPoint
    ) {
        Player player = mock(Player.class);
        when(player.getWorldLocation()).thenReturn(worldPoint);
        when(player.getLocalLocation()).thenReturn(localPoint);
        return player;
    }

}
