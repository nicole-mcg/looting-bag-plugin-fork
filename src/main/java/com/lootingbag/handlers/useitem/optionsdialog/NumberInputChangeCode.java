package com.lootingbag.handlers.useitem.optionsdialog;

import lombok.Getter;

@Getter
public enum NumberInputChangeCode {
    CLOSE(0, 0),
    OPEN(0, 7);

    private final int firstCode;
    private final int secondCode;

    NumberInputChangeCode(final int firstCode, final int secondCode) {
        this.firstCode = firstCode;
        this.secondCode = secondCode;
    }

    public boolean matches(final int firstCode, final int secondCode) {
        return this.firstCode == firstCode && this.secondCode == secondCode;
    }

    public boolean firstCodeMatches(final int firstCode) {
        return this.firstCode == firstCode;
    }
}
