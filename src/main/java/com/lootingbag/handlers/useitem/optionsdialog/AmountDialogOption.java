package com.lootingbag.handlers.useitem.optionsdialog;

import lombok.Getter;

public class AmountDialogOption {
    public static final AmountDialogOption ONE = new AmountDialogOption("One", 1);
    public static final AmountDialogOption TWO = new AmountDialogOption("Two", 2);
    public static final AmountDialogOption FIVE = new AmountDialogOption("Five", 5);
    public static final AmountDialogOption BOTH = new AmountDialogOption("Both", 2);
    public static final AmountDialogOption ALL = new AmountDialogOption("All");

    private static final String CUSTOM_OPTION_TEXT = "X";
    private static final AmountDialogOption[] PRESET_AMOUNTS = new AmountDialogOption[] {
        ONE,
        TWO,
        FIVE,
        BOTH,
        ALL
    };

    @Getter
    private final String optionText;

    private final Integer amount;

    public AmountDialogOption(final int amount) {
        this(CUSTOM_OPTION_TEXT, amount);
    }

    private AmountDialogOption(final String optionText) {
        this(optionText, null);
    }

    private AmountDialogOption(final String optionText, final Integer amount) {
        this.optionText = optionText;
        this.amount = amount;
    }

    public boolean isCustom() {
        return optionText.equals(CUSTOM_OPTION_TEXT);
    }

    public boolean hasAmount() {
        return amount != null;
    }

    public int getAmount() {
        if (amount == null) {
            throw new IllegalStateException("Cannot get amount for option " + optionText);
        }

        return amount;
    }

    public static AmountDialogOption getOption(final String optionText) {
        for (final AmountDialogOption option : PRESET_AMOUNTS) {
            if (option.optionText.equals(optionText)) {
                return option;
            }
        }

        if (optionText.equals(CUSTOM_OPTION_TEXT)) {
            return new AmountDialogOption(optionText);
        }

        return null;
    }
}
