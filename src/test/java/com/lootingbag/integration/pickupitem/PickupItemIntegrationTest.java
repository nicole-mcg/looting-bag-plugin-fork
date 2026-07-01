package com.lootingbag.integration.pickupitem;

import com.google.inject.testing.fieldbinder.Bind;
import com.lootingbag.LootingBagOverlay;
import com.lootingbag.LootingBagPlugin;
import com.lootingbag.MockedTestBase;
import com.lootingbag.integration.*;
import com.lootingbag.lootingbagcontainer.LootingBag;
import net.runelite.api.Item;
import net.runelite.client.config.ConfigManager;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PickupItemIntegrationTest extends MockedTestBase {

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

    @ParameterizedTest(name = "Full pickup item flow: {0}")
    @ArgumentsSource(TestCaseProvider.class)
    void testPickupItem(PickupItemTestCase testCase)
    {
        // Set up the test
        setup.setup(testCase);

        // Get expected results
        Item[] expectedItems = expectations.getExpectedLootingBagItems(
            testCase.expectedInsertedItems,
            testCase.lootingBagItems
        );
        BigInteger expectedItemsValue = expectations.getExpectedItemsValue(
            testCase.expectedInsertedItems,
            testCase.lootingBagItems
        );

        // Simulate picking up the items
        Arrays.stream(testCase.insertedItems)
            .forEach(itemParameters ->
                simulator.pickupGroundItem(
                    itemParameters,
                    testCase.worldParameters,
                    testCase.groundItemLocation,
                    testCase.playerEndingLocation
                )
            );

        // Verify the looting bag state
        Assertions.assertLootingBagItems(lootingBag.getItems(), expectedItems);
        assertEquals(testCase.numExpectedFreeSlots, lootingBag.getFreeSlots(), "Number of free slots");
        assertEquals(expectedItemsValue, lootingBag.getValueOfItems(), "Value of items");
    }

    private static class TestCaseProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            PickupItemTestCase[] x = LootingBagTestCase.getAllTestCases(PickupItemTestCases.class)
                .map(testCase -> (PickupItemTestCase) testCase)
                .toArray(PickupItemTestCase[]::new);
            Stream<PickupItemTestCase> testCases = LootingBagTestCase.getAllTestCases(PickupItemTestCases.class)
                .map(testCase -> (PickupItemTestCase) testCase);
            return testCases.map(Arguments::of);
        }
    }
}
