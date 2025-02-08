package com.looting_bag_plugin;

import com.google.gson.Gson;
import com.google.inject.Provides;
import com.google.inject.Inject;

import com.looting_bag_plugin.handlers.PickupHandler;
import com.looting_bag_plugin.handlers.TelegrabHandler;
import com.looting_bag_plugin.handlers.UseItemHandler;
import com.looting_bag_plugin.handlers.WildernessAgilityDispenserHandler;
import com.looting_bag_plugin.looting_bag.LootingBag;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.ComponentID;
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
	private PickupHandler pickupHandler;

	@Inject
	private UseItemHandler useItemHandler;

	@Inject
	private Gson gson;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onGameTick(final GameTick gameTick)
	{
		pickupHandler.processPossibleSuppliesPickupActions();
	}

	@Subscribe
	public void onVarClientIntChanged(final VarClientIntChanged event) {
		if (event.getIndex() == VarClientInt.INPUT_TYPE) {
			final int value = client.getVarcIntValue(VarClientInt.INPUT_TYPE);
			final String text = client.getVarcStrValue(VarClientStr.INPUT_TEXT);

			useItemHandler.onNumberInputChanged(value, text);
		}
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
		final Widget widget = event.getWidget();

		// Select amount to deposit in looting bag menu
		if (event.getMenuAction() == MenuAction.WIDGET_CONTINUE
				&& widget != null
				&& widget.getParentId() == ComponentID.DIALOG_OPTION_OPTIONS) {
			useItemHandler.onDialogOptionSelected(widget.getText());
		}

		// Use an item on another item
		if (event.getMenuAction() == MenuAction.WIDGET_TARGET_ON_WIDGET
				&& event.getMenuOption().equals("Use")) {
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
		final boolean isTakeItemOffGround = pickupHandler.isPickupMenuOption(event);

		// Take an item off the ground, or telegrab
		if (!isTakeItemOffGround || !isTelegrab) {
			return;
		}

		pickupHandler.onItemPickedUp(event);
	}

	@Subscribe
	public void onProjectileMoved(final ProjectileMoved event) {
		telegrabHandler.onProjectileMoved(event);
	}

	@Subscribe
	public void onItemDespawned(final ItemDespawned event) {
		pickupHandler.onItemDespawned(event);
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

		useItemHandler.onInventoryUpdated();
		pickupHandler.onInventoryUpdated();
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

	@Provides
	LootingBagPluginConfig provideConfig(final ConfigManager configManager) {
		return configManager.getConfig(LootingBagPluginConfig.class);
	}

	private boolean isLootingBag(final int itemId)
	{
		return itemId == ItemID.LOOTING_BAG
			|| itemId == ItemID.LOOTING_BAG_22586;
	}
}
