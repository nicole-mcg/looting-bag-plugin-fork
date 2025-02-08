package com.lootingbag.handlers;

import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ProjectileMoved;

import com.google.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

@Singleton
public class TelegrabHandler {

    private static final int TELEGRAB_PROJECTILE_ID = 143;

    @Inject
    private Client client;

    private int telegrabPickUpCycle = -1;
    private WorldPoint telegrabEndTile;

    public boolean isTelegrabMenuOption(final MenuOptionClicked event) {
        if (event.getMenuAction() != MenuAction.WIDGET_TARGET_ON_GROUND_ITEM ) {
            return false;
        }

        final List<String> widgetGroundItem = Arrays.asList(event.getMenuTarget().split(" -> "));
        final boolean isTelegrab = widgetGroundItem.get(0).contains("Telekinetic Grab");
        if (!isTelegrab) {
            return false;
        }

        // get end tile based click, telegrab check in projectileMoved
        telegrabEndTile = WorldPoint.fromScene(client, event.getParam0(), event.getParam1(), client.getPlane());
        return true;
    }

    public void onProjectileMoved(final ProjectileMoved event) {
        final Player player = client.getLocalPlayer();
        final boolean isTelegrab = event.getProjectile().getId() == TELEGRAB_PROJECTILE_ID;
        if (player == null || !isTelegrab) {
            return;
        }

        final LocalPoint playerLocalPoint = player.getLocalLocation();
        final WorldView worldView = getWorldView(playerLocalPoint);

        final LocalPoint telegrabStartLocation = new LocalPoint(event.getProjectile().getX1(), event.getProjectile().getY1(), worldView);
        final WorldPoint telegrabWorldPoint = WorldPoint.fromLocal(client, telegrabStartLocation);
        final WorldPoint playerWorldPoint = WorldPoint.fromLocal(client, playerLocalPoint);

        if (telegrabWorldPoint.distanceTo(playerWorldPoint) > 5) {
            return;
        }

        telegrabPickUpCycle = event.getProjectile().getEndCycle();
    }

    public boolean isItemDespawnTelegrab(final WorldPoint groundItemLocation) {
        final boolean telegrabOnItemTile = groundItemLocation.equals(telegrabEndTile);
        // can be off by one tick depending on user running/dragged
        final boolean telegrabEndsOnCycle = Math.abs(telegrabPickUpCycle - client.getGameCycle()) <= 1;
        return telegrabOnItemTile && telegrabEndsOnCycle;
    }

    private WorldView getWorldView(final LocalPoint playerLocalPoint) {
        final int worldViewId = playerLocalPoint.getWorldView();
        return client.getWorldView(worldViewId);
    }
}
