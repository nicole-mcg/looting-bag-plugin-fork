package com.lootingbag.integration.wildernessagility;

import com.lootingbag.integration.LootingBagTestCase;
import com.lootingbag.integration.parameters.ItemParameters;

public class WildernessAgilityDispenserTestCase extends LootingBagTestCase {

    public String itemName1;
    public int itemQuantity1;

    public String itemName2;
    public int itemQuantity2;

    public String extraItemName;
    public int extraItemQuantity;

    @Override
    public boolean checkItemInsertable(ItemParameters itemParameters) {
        // There are no specific conditions for
        // wilderness agility dispenser test cases
        return true;
    }
}
