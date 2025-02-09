package com.lootingbag.lootingbagcontainer;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.lootingbag.constants.GeUntradables;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.Varbits;
import net.runelite.client.game.ItemManager;

import com.google.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class LootingBag
{
	private static final int LOOTING_BAG_SIZE = 28;

	private static final Set<Integer> FEROX_REGION = ImmutableSet.of(12600, 12344);

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	private final Map<Integer, Integer> items = new HashMap<>();

	@Getter
	private boolean isSynced = false;

	@Getter
	private boolean isQuantityOfItemsAccurate = true;

	@Getter
	private long valueOfItems = 0;

	private String lastPlayerName;

	public void onLogin() {
		// Clear items if the player has changed
		if (lastPlayerName != null && !lastPlayerName.equals(client.getLocalPlayer().getName())) {
			clearItems();
			isSynced = false;
		}

		lastPlayerName = client.getLocalPlayer().getName();
	}

	public void clearItems() {
		items.clear();
		valueOfItems = 0;
		isSynced = true;
		isQuantityOfItemsAccurate = true;
	}

	public void addItem(
		final int itemId,
		final int quantity
	) {
		addItem(itemId, quantity, true);
	}

	public void addItem(
            final int itemId,
            final int quantity,
            final boolean isQuantityConfirmed) {

		// Check that we can deposit any item into looting bag
		// 		E.g. We're in the wilderness
		if (!canDepositItems()) {
			return;
		}

		// Check that the item can go in the looting bag
		//		E.g. It's tradeable
		if (!canItemGoInLootingBag(itemId)) {
			log.debug("Item can not go in looting bag: " + itemManager.getItemComposition(itemId).getName());
			return;
		}

		final ItemComposition itemComposition = itemManager.getItemComposition(itemId);

		// Check that we have room in the looting bag
		if (getFreeSlots() == 0
				&& (!itemComposition.isStackable() || !items.containsKey(itemId))) {
			return;
		}

		if (!isQuantityConfirmed) {
			isQuantityOfItemsAccurate = false;
		}

		log.debug("Successfully added item to looting bag: " + itemComposition.getName() + "x" + quantity);
		items.merge(itemId, quantity, Integer::sum);
		calculateValueOfItems();
	}

	public int getFreeSlots()
	{
		return LOOTING_BAG_SIZE -
			items.keySet().stream()
				.mapToInt(itemId ->
					itemManager.getItemComposition(itemId).isStackable()
						? 1
						: items.get(itemId)
				).sum();
	}

	public void syncItems(final ItemContainer lootingBagContainer) {
		items.clear();

		// The looting bag container will be null when it is empty
		if (lootingBagContainer == null) {
			isSynced = true;
			calculateValueOfItems();
			return;
		}

		items.putAll(Arrays.stream(lootingBagContainer.getItems())
			.reduce(
				new HashMap<>(),
				(map, item) -> {
					map.merge(item.getId(), item.getQuantity(), Integer::sum);
					return map;
				},
				(map1, map2) -> {
					map1.putAll(map2);
					return map1;
				}));

		calculateValueOfItems();
		isQuantityOfItemsAccurate = true;
		isSynced = true;
	}

	private boolean canItemGoInLootingBag(final int itemId) {
		return isItemTradeable(itemId)
			|| isItemTradeable(itemManager.getItemComposition(itemId).getLinkedNoteId());
	}

	private boolean isItemTradeable(final int itemId) {
		final ItemComposition itemComposition = itemManager.getItemComposition(itemId);

		return itemComposition.isTradeable() // GE tradeable items
			|| itemComposition.getName().matches("Ensouled [a-z]+ head")
			|| GeUntradables.ItemIds.contains(itemId);
	}

	private boolean canDepositItems() {
		// Can't deposit items into looting bag if not in wilderness or Ferox
		return client.getVarbitValue(Varbits.IN_WILDERNESS) != 0
			|| FEROX_REGION.contains(client.getLocalPlayer().getWorldLocation().getRegionID());
	}

	private void calculateValueOfItems() {
		valueOfItems = items.keySet().stream()
			.mapToLong(itemId -> getPriceOfItem(itemId, items.get(itemId)))
			.sum();
	}

	private long getPriceOfItem(final int itemId, final int quantity) {
		return itemManager.getItemPrice(itemId) * (long) quantity;
	}
}
