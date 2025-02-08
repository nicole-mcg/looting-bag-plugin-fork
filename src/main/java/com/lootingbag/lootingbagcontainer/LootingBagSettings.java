package com.lootingbag.lootingbagcontainer;

import net.runelite.api.Client;

import com.google.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LootingBagSettings {

    private static final int LOOTING_BAG_SUPPLIES_SETTING_VARBIT_ID = 15310;

    @Inject
    private Client client;

    public boolean doSuppliesGoIntoInventory() {
        return client.getVarbitValue(LOOTING_BAG_SUPPLIES_SETTING_VARBIT_ID) == 1;
    }
}
