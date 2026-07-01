package com.lootingbag.lootingbagcontainer;

import net.runelite.api.Client;

import com.google.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LootingBagSettings {


    public static final int LOOTING_BAG_SUPPLIES_SETTING_VARBIT_ID = 15310;

    public static final int LOOTING_BAG_USE_DIALOG_SETTING_VARBIT_ID = 6068;

    @Inject
    private Client client;

    public boolean doSuppliesGoIntoInventory() {
        return client.getVarbitValue(LOOTING_BAG_SUPPLIES_SETTING_VARBIT_ID) == 1;
    }

    public UseItemDialogSetting getUseItemDialogSetting() {
        int varbitValue = client.getVarbitValue(LOOTING_BAG_USE_DIALOG_SETTING_VARBIT_ID);
        return UseItemDialogSetting.fromValue(varbitValue);
    }

    public boolean useItemAsksAmount() {
        return getUseItemDialogSetting() == UseItemDialogSetting.ASK_AMOUNT;
    }

    public boolean useItemDepositsAll() {
        return getUseItemDialogSetting() == UseItemDialogSetting.DEPOSIT_ALL;
    }
}
