package com.lootingbag.lootingbagcontainer;

import com.google.common.collect.ImmutableSet;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.IntStream;

import com.lootingbag.constants.GeUntradables;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.client.game.ItemManager;

import com.google.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class LootingBag
{
	public static final int LOOTING_BAG_SIZE = 28;

	public static final Set<Integer> FEROX_REGION = ImmutableSet.of(12600, 12344);

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	private final ArrayList<Item> items = new ArrayList<>();

	@Getter
	private boolean isSynced = false;

	@Getter
    private boolean isQuantityOfItemsAccurate = true;

	@Getter
	private BigInteger valueOfItems = BigInteger.ZERO;

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
		valueOfItems = BigInteger.valueOf(0);
		isSynced = true;
		isQuantityOfItemsAccurate = true;
	}

	public void addItem(final int itemId, final int quantity) {
		addItem(itemId, quantity, true);
	}

	public void addItem(
		final int itemId,
		final int quantity,
		final boolean isQuantityConfirmed
	) {
		// Check that we can deposit any item into looting bag
		// 		E.g. We're in the wilderness
		if (!canDepositItems()) {
			return;
		}

		// Check that the item can go in the looting bag
		//		E.g. It's tradeable
		if (!canItemGoInLootingBag(itemId)) {
            log.debug("Item can not go in looting bag: {}", itemManager.getItemComposition(itemId).getName());
			return;
		}

		final ItemComposition itemComposition = itemManager.getItemComposition(itemId);

		// Check that we have room in the looting bag
        final int existingStackIndex = getItemIndex(itemId);
        final boolean canJoinStack = itemComposition.isStackable() && existingStackIndex != -1;
		if (getFreeSlots() == 0 && !canJoinStack) {
			return;
		}

		if (!isQuantityConfirmed) {
			isQuantityOfItemsAccurate = false;
		}

        // If the item isn't stackable, or we don't have a stack of it, add a new item
        if (existingStackIndex == -1) {
            items.add(new Item(itemId, quantity));
            return;
        }

        // Add item to the existing stack
        final Item existingStack = items.get(existingStackIndex);
        final Item newStack = new Item(itemId, quantity + existingStack.getQuantity());
        items.set(existingStackIndex, newStack);
	}

	public int getFreeSlots()
	{
        return LOOTING_BAG_SIZE - items.size();
	}

    public Item[] getItems() {
        return items.toArray(new Item[0]);
    }

	public void syncItems(final ItemContainer lootingBagContainer) {
		items.clear();

		// The looting bag container will be null when it is empty
		if (lootingBagContainer == null) {
			isSynced = true;
			calculateValueOfItems();
			return;
		}

        List<Item> itemList = Arrays.asList(lootingBagContainer.getItems());
		items.addAll(itemList);

		calculateValueOfItems();
		isQuantityOfItemsAccurate = true;
		isSynced = true;
	}

    private int getItemIndex(final int itemId) {
        return IntStream.range(0, items.size())
            .filter(i -> items.get(i).getId() == itemId)
            .findFirst()
            .orElse(-1);
    }

	private boolean canItemGoInLootingBag(final int itemId) {
		return isItemTradeable(itemId)
			|| isItemTradeable(itemManager.getItemComposition(itemId).getLinkedNoteId());
	}

	private boolean isItemTradeable(final int itemId) {
        if (itemId == -1) {
            return false;
        }

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
		valueOfItems = items.stream()
            .reduce(
                BigInteger.ZERO,
                (sum, item) -> {
                    final int itemId = item.getId();
                    final int quantity = item.getQuantity();

                    final long price = getPriceOfItem(itemId, quantity);
                    return sum.add(BigInteger.valueOf(price));
                },
                BigInteger::add
            );
	}

	private long getPriceOfItem(final int itemId, final int quantity) {
		return (long) itemManager.getItemPrice(itemId) * quantity;
	}
}
