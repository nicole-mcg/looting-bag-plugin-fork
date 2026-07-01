package com.lootingbag.handlers.useitem.optionsdialog;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.events.VarClientIntChanged;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Singleton
public class LootingBagDialogTracker {

    @Inject
    private Client client;

    private final List<ILootingBagDialogListener> dialogListeners = new ArrayList<>();

    /**
     * Used to keep track of whether we expect the options dialog to open.
     * This is used to handle edge cases when a non-looting bag options dialog is opened.
     */
    private boolean isExpectingOptionsDialogToOpen = false;

    /**
     * Used to keep track of whether the options dialog is open.
     */
    private boolean isOptionsDialogOpen = false;

    /**
     * Used to keep track of whether the custom amount dialog is open.
     */
    private boolean isCustomAmountDialogOpen = false;

    @Nullable
    private Integer lastNumberInputChangeCode;

    public void expectOptionsDialogToOpen() {
        isExpectingOptionsDialogToOpen = true;
    }

    public void addOptionsDialogListener(final ILootingBagDialogListener listener) {
        dialogListeners.add(listener);
    }

    public void removeOptionsDialogListener(final ILootingBagDialogListener listener) {
        dialogListeners.remove(listener);
    }

    /**
     * Check if the options dialog is open and notify listeners
     */
    public void onGameTick()
    {
        final Widget optionsWidget = client.getWidget(ComponentID.DIALOG_OPTION_OPTIONS);
        setOptionsDialogOpen(optionsWidget != null);
    }

    /**
     * Detect when the mouse is used to select an option from the dialog
     * @see AmountDialogOption
     */
    public void onMenuOptionClicked(final MenuOptionClicked event) {
        final Widget widget = event.getWidget();
        if (widget == null || !isExpectingOptionsDialogToOpen) {
            return;
        }

        final boolean isDialogOption = event.getMenuAction() == MenuAction.WIDGET_CONTINUE
            && widget.getParentId() == ComponentID.DIALOG_OPTION_OPTIONS;

        // Select amount to deposit in looting bag menu
        if (!isDialogOption) {
            return;
        }

        onOptionSelected(widget.getText());
    }

    /**
     * Detect when the keyboard is used to select an option from the dialog
     * @see AmountDialogOption
     */
    public void onScriptPreFired(final ScriptPreFired scriptPreFired) {
        final boolean isSelectOptionWithKeyboard = scriptPreFired.getScriptId() == ScriptID.CHATBOX_KEYINPUT_MATCHED;
        if (!isSelectOptionWithKeyboard || !isExpectingOptionsDialogToOpen) {
            return;
        }

        final int[] intStack = client.getIntStack();
        final int componentId = intStack[0];
        final int subId = intStack[1];

        if (componentId != ComponentID.DIALOG_OPTION_OPTIONS || subId <= -1) {
            return;
        }

        final Widget widget = client.getWidget(componentId);
        if (widget == null) {
            log.error("Could not get widget from component ID: {}", componentId);
            return;
        }

        final Widget childWidget = widget.getChild(subId);
        if (childWidget == null) {
            log.error("Could not get child widget '{}' from widget '{}'", subId, widget.getId());
            return;
        }

        final String text = childWidget.getText();
        if (text.isEmpty()) {
            return;
        }

        onOptionSelected(text);
    }

    public void onVarClientIntChanged(final VarClientIntChanged event) {
        if (!isCustomAmountDialogOpen) {
            return;
        }

        final boolean isNumberInput = event.getIndex() == VarClientInt.INPUT_TYPE;
        if (!isNumberInput) {
            return;
        }

        // Number input chatbox interface has changed
        final int changeCode = client.getVarcIntValue(VarClientInt.INPUT_TYPE);
        final String text = client.getVarcStrValue(VarClientStr.INPUT_TEXT);

        final NumberInputChangeCode[] changeCodes = NumberInputChangeCode.values();
        final boolean isAnyFirstCode = Arrays.stream(changeCodes)
            .anyMatch(code -> code.firstCodeMatches(changeCode));

        // If we don't have a lastNumberInputChangeCode, we can't determine if this is an open or close event
        if (lastNumberInputChangeCode == null) {
            lastNumberInputChangeCode = isAnyFirstCode ? changeCode : null;
            log.debug("Setting lastNumberInputChangeCode to {}", lastNumberInputChangeCode);
            return;
        }

        final boolean isCloseEvent = NumberInputChangeCode.CLOSE.matches(lastNumberInputChangeCode, changeCode);

        // We only care about close events
        if (!isCloseEvent) {
            log.debug("Ignoring open event for deposit X lastCode={} currentCode={}.", lastNumberInputChangeCode, changeCode);
            lastNumberInputChangeCode = changeCode;
            return;
        }

        log.debug("Deposit X close event: text={}", text);
        // Close event
        lastNumberInputChangeCode = null;
        isCustomAmountDialogOpen = false;

        final AmountDialogOption option = text.isEmpty()
            ? null
            : new AmountDialogOption(Integer.parseInt(text));
        dialogListeners.forEach(listener -> listener.onOptionSelected(option));
    }

    private void onOptionSelected(final String amountText) {
        if (!isOptionsDialogOpen) {
            return;
        }

        final AmountDialogOption option = AmountDialogOption.getOption(amountText);
        if (option == null) {
            log.error("Could not get amount dialog option for text: {}", amountText);
            return;
        }

        dialogListeners.forEach(listener -> listener.onOptionSelected(option));

        isCustomAmountDialogOpen = option.isCustom();
    }

    private void setOptionsDialogOpen(final boolean isOptionsDialogOpen) {
        if (this.isOptionsDialogOpen == isOptionsDialogOpen) {
            return;
        }

        if (isOptionsDialogOpen) {
            // Reset custom amount dialog state because
            // it can't be open at the same time as the options dialog
            isCustomAmountDialogOpen = false;
        } else {
            // Reset whether we are expecting this dialog to open
            isExpectingOptionsDialogToOpen = false;
        }

        this.isOptionsDialogOpen = isOptionsDialogOpen;
    }
}
