package com.lootingbag.lootingbagcontainer;

import lombok.Getter;

@Getter
public enum UseItemDialogSetting {
    ASK_AMOUNT(0),
    DEPOSIT_ALL(1);

    private final int value;

    UseItemDialogSetting(int value) {
        this.value = value;
    }

    public static UseItemDialogSetting fromValue(int value) {
        for (UseItemDialogSetting setting : values()) {
            if (setting.value == value) {
                return setting;
            }
        }
        return null;
    }
}
