package com.lootingbag.integration.parameters;

import com.lootingbag.testutil.RandomUtils;

public class ItemParameters {
    public String itemName = "Unnamed Item";
    public int itemId = RandomUtils.getRandomItemId();
    public boolean isTradeable = true;
    public boolean isStackable = false;
    public boolean isSupply = false;
    public int value = RandomUtils.getRandomItemPrice();
    public boolean hasNote = false;

    public ItemParameters() {
    }

    private int quantity = 0;
    public int getQuantity() {
        if (quantity == 0) {
            quantity = isStackable
                ? RandomUtils.getRandomItemQuantity()
                : 1;
        }

        return quantity;
    }

    public String getItemName() {
        return "Item " + itemId;
    }
}
