package com.lootingbag.integration.wildernessagility;

import com.google.inject.testing.fieldbinder.Bind;
import com.lootingbag.LootingBagOverlay;
import com.lootingbag.LootingBagPlugin;
import com.lootingbag.MockedTestBase;
import com.lootingbag.integration.*;
import com.lootingbag.integration.parameters.ItemParameters;
import com.lootingbag.lootingbagcontainer.LootingBag;
import com.lootingbag.testutil.MockUtils;
import com.lootingbag.testutil.RandomUtils;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemID;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WildernessAgilityDispenserIntegrationTest extends MockedTestBase {

    @Mock
    @Bind
    ConfigManager configManager;

    @Mock
    @Bind
    private LootingBagOverlay overlay;

    @Bind
    @InjectMocks
    LootingBagPlugin lootingBagPlugin;

    @Bind
    @InjectMocks
    LootingBag lootingBag;

    @Bind
    @InjectMocks
    Simulator simulator;

    @Bind
    @InjectMocks
    Setup setup;

    @Bind
    @InjectMocks
    Expectations expectations;

    @ParameterizedTest(name = "Looting dispenser item flow: {0}")
    @ArgumentsSource(TestCaseProvider.class)
    void testPickupItem(WildernessAgilityDispenserTestCase testCase) {
        setup.setup(testCase);
        setupDispenserItems();

        // Get expected results
        Item[] expectedItems = expectations.getExpectedLootingBagItems(
            testCase.expectedInsertedItems,
            testCase.lootingBagItems
        );
        BigInteger expectedItemsValue = expectations.getExpectedItemsValue(
            testCase.expectedInsertedItems,
            testCase.lootingBagItems
        );

        // Simulate the game message
        String message = getGameMessage(testCase.insertedItems);
        ChatMessage chatMessageEvent = MockUtils.createChatMessageEvent(message);
        lootingBagPlugin.onChatMessage(chatMessageEvent);

        // Verify the looting bag state
        assertEquals(testCase.numExpectedFreeSlots, lootingBag.getFreeSlots());
        assertEquals(expectedItemsValue, lootingBag.getValueOfItems());

        // Verify the looting bag items
        Assertions.assertLootingBagItems(lootingBag.getItems(), expectedItems);
    }

    private String getGameMessage(ItemParameters[] insertedItems) {
        ItemParameters item1 = insertedItems[0];
        ItemParameters item2 = insertedItems[1];
        String itemName1 = itemManager.getItemComposition(item1.itemId).getName();
        String itemName2 = itemManager.getItemComposition(item2.itemId).getName();

        String message = "You have been awarded "
            + getItemMessage(itemName1, item1.getQuantity())
            + " and "
            + getItemMessage(itemName2, item2.getQuantity());

        if (insertedItems.length > 2) {
            ItemParameters extraItem = insertedItems[2];
            String extraItemName = itemManager.getItemComposition(extraItem.itemId).getName();
            message += ", and an extra "
                + getItemMessage(extraItemName, extraItem.getQuantity());
        }

        return message + " from the Agility dispenser.";
    }

    private String getItemMessage(String itemName, int itemQuantity) {
        return "<col=ff0000>"
            + itemQuantity
            + " x "
            + itemName
            + "</col>";
    }

    private void setupDispenserItems() {
        ArrayList<Pair<Integer, String>> itemNames = new ArrayList<>();
        itemNames.add(Pair.of(ItemID.BLIGHTED_ANGLERFISH, "Blighted anglerfish"));
        itemNames.add(Pair.of(ItemID.BLIGHTED_MANTA_RAY, "Blighted manta ray"));
        itemNames.add(Pair.of(ItemID.BLIGHTED_KARAMBWAN, "Blighted karambwan"));
        itemNames.add(Pair.of(ItemID.BLIGHTED_SUPER_RESTORE4, "Blighted super restore(4)"));
        itemNames.add(Pair.of(ItemID.MITHRIL_PLATESKIRT, "Mithril plateskirt"));
        itemNames.add(Pair.of(ItemID.MITHRIL_PLATELEGS, "Mithril platelegs"));
        itemNames.add(Pair.of(ItemID.ADAMANT_PLATEBODY, "Adamant platebody"));
        itemNames.add(Pair.of(ItemID.ADAMANT_FULL_HELM, "Adamant full helm"));
        itemNames.add(Pair.of(ItemID.ADAMANT_PLATELEGS, "Adamant platelegs"));
        itemNames.add(Pair.of(ItemID.RUNE_MED_HELM, "Rune med helm"));

        itemNames.add(Pair.of(ItemID.STEEL_PLATEBODY, "Steel platebody"));

        itemNames.add(Pair.of(ItemID.MITHRIL_CHAINBODY, "Mithril chainbody"));

        itemNames.add(Pair.of(ItemID.RUNE_CHAINBODY, "Rune chainbody"));
        itemNames.add(Pair.of(ItemID.RUNE_KITESHIELD, "Rune kiteshield"));

        itemNames.forEach(pair ->
            setup.setupItem(new ItemParameters() {{
                itemId = pair.getLeft();
                itemName = pair.getRight();
                value = RandomUtils.getRandomItemPrice();
                isTradeable = true;
                hasNote = true;
            }})
        );
    }

    private static class TestCaseProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            Stream<WildernessAgilityDispenserTestCase> testCases = LootingBagTestCase.getAllTestCases(WildernessAgilityDispenserTestCases.class)
                .map(testCase -> (WildernessAgilityDispenserTestCase) testCase);
            return testCases.map(Arguments::of);
        }
    }
}
