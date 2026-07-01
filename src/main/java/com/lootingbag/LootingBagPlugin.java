package com.lootingbag;

import com.google.gson.Gson;
import com.google.inject.Provides;
import com.google.inject.Inject;

import com.lootingbag.handlers.pickup.PickupItemHandler;
import com.lootingbag.handlers.pickup.TelegrabHandler;
import com.lootingbag.handlers.useitem.UseItemHandler;
import com.lootingbag.handlers.WildernessAgilityDispenserHandler;
import com.lootingbag.lootingbagcontainer.LootingBag;
import com.lootingbag.state.InventoryTracker;
import com.lootingbag.handlers.useitem.optionsdialog.LootingBagDialogTracker;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Looting Bag"
)
public class LootingBagPlugin extends Plugin
{
	public static final int LOOTING_BAG_CONTAINER = 516;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private LootingBagOverlay overlay;

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	@Inject
	private InventoryTracker inventoryTracker;

	@Inject
	private LootingBag lootingBag;

	@Inject
	private WildernessAgilityDispenserHandler wildernessAgilityDispenserHandler;

	@Inject
	private TelegrabHandler telegrabHandler;

	@Inject
	private PickupItemHandler pickupItemHandler;

	@Inject
	private UseItemHandler useItemHandler;

	@Inject
	private LootingBagDialogTracker lootingBagDialogTracker;

	@Inject
	private Gson gson;

	private boolean isLoggingIn = false;

	@Provides
	public LootingBagPluginConfig provideConfig(final ConfigManager configManager) {
		return configManager.getConfig(LootingBagPluginConfig.class);
	}

	@Override
	public void startUp()
	{
		useItemHandler.initialize();
		overlayManager.add(overlay);
	}

	@Override
	public void shutDown()
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onGameStateChanged(final GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGING_IN)
		{
			isLoggingIn = true;
		}
	}

	@Subscribe
	public void onGameTick(final GameTick gameTick)
	{
		if (isLoggingIn) {
			isLoggingIn = false;
			lootingBag.onLogin();
		}

		pickupItemHandler.processPossibleSuppliesPickupActions();
		lootingBagDialogTracker.onGameTick();
	}

	@Subscribe
	public void onWidgetLoaded(final WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.LOOTING_BAG)
		{
			// We can use the ItemContainer as a source of truth!
			final ItemContainer lootingBagContainer = client.getItemContainer(LOOTING_BAG_CONTAINER);
			lootingBag.syncItems(lootingBagContainer);
		}
	}

	@Subscribe
	public void onItemContainerChanged(final ItemContainerChanged event) {
		if (event.getContainerId() == InventoryID.INVENTORY.getId()) {
			handleInventoryUpdated(event.getItemContainer());
		}

		if (event.getContainerId() == LOOTING_BAG_CONTAINER) {
			lootingBag.syncItems(event.getItemContainer());
		}
	}

	@Subscribe
	public void onMenuOptionClicked(final MenuOptionClicked event) {
		lootingBagDialogTracker.onMenuOptionClicked(event);

		// Use an item on another item
		if (event.getMenuAction() == MenuAction.WIDGET_TARGET_ON_WIDGET
			&& event.getMenuOption().equals("Use")
		) {
			final Widget selectedWidget = client.getSelectedWidget();
			if (selectedWidget == null)
			{
				return;
			}

			log.debug("Using item " + itemManager.getItemComposition(selectedWidget.getItemId()).getName()
				+ " on item " + itemManager.getItemComposition(event.getItemId()).getName());
			handleItemUsedOnItem(selectedWidget.getItemId(), event.getItemId());
			return;
		}

		final boolean isTelegrab = telegrabHandler.isTelegrabMenuOption(event);
		final boolean isTakeItemOffGround = pickupItemHandler.isPickupMenuOption(event);

		// Take an item off the ground, or telegrab
		if (!isTakeItemOffGround && !isTelegrab) {
			return;
		}

		pickupItemHandler.onTakeItemClicked(event);
	}

	@Subscribe
	public void onProjectileMoved(final ProjectileMoved event) {
		telegrabHandler.onProjectileMoved(event);
	}

	@Subscribe
	public void onItemDespawned(final ItemDespawned event) {
		pickupItemHandler.onItemDespawned(event);
	}

	@Subscribe
	public void onScriptPreFired(final ScriptPreFired scriptPreFired) {
		lootingBagDialogTracker.onScriptPreFired(scriptPreFired);
	}

	@Subscribe
	public void onVarClientIntChanged(final VarClientIntChanged event) {
        lootingBagDialogTracker.onVarClientIntChanged(event);
	}

	@Subscribe
	public void onChatMessage(final ChatMessage event)
	{
		final ChatMessageType type = event.getType();
		final String message = event.getMessage();

		if (type == ChatMessageType.GAMEMESSAGE)
		{
			wildernessAgilityDispenserHandler.onGameMessage(message);
		}
	}

	private void handleInventoryUpdated(final ItemContainer inventory) {
		inventoryTracker.updateInventoryItems(inventory);

		final boolean isLootingBagAdded = inventoryTracker.wasAddedToInventory(ItemID.LOOTING_BAG)
			|| inventoryTracker.wasAddedToInventory(ItemID.LOOTING_BAG_22586);
		final boolean isBankOpen = client.getItemContainer(InventoryID.BANK) != null;
		if (isLootingBagAdded && !isBankOpen) {
			lootingBag.clearItems();
		}

		pickupItemHandler.onInventoryUpdated();
	}

	private void handleItemUsedOnItem(final int itemId1, final int itemId2) {
		if (!isLootingBag(itemId1) && !isLootingBag(itemId2)) {
			return;
		}

		final int itemId = isLootingBag(itemId1)
			? itemId2
			: itemId1;
		useItemHandler.onItemUsedOnLootingBag(itemId);
	}

	private boolean isLootingBag(final int itemId)
	{
		return itemId == ItemID.LOOTING_BAG
			|| itemId == ItemID.LOOTING_BAG_22586;
	}
}
