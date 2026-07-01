package com.lootingbag.integration.pickupitem.telegrab;

import com.google.inject.testing.fieldbinder.Bind;
import com.lootingbag.LootingBagOverlay;
import com.lootingbag.LootingBagPlugin;
import com.lootingbag.MockedTestBase;
import com.lootingbag.integration.*;
import com.lootingbag.integration.parameters.ItemParameters;
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

public class TelegrabItemIntegrationTest extends MockedTestBase {

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

    @ParameterizedTest(name = "Full telegrab item flow: {0}")
    @ArgumentsSource(TestCaseProvider.class)
    void testTelegrabItem(TelegrabItemTestCase parameters)
    {
        // Set up the test
        setup.setup(parameters);

        // Get expected results
        Item[] expectedItems = expectations.getExpectedLootingBagItems(
            parameters.expectedInsertedItems,
            parameters.lootingBagItems
        );
        BigInteger expectedItemsValue = expectations.getExpectedItemsValue(
            parameters.expectedInsertedItems,
            parameters.lootingBagItems
        );

        // Simulate telegrabbing the items
        Arrays.stream(parameters.insertedItems)
            .forEach(itemParameters ->
                simulator.telegrabGroundItem(
                    itemParameters,
                    parameters.worldParameters,
                    parameters.groundItemLocation
                )
            );

        // Verify the looting bag state
        assertEquals(parameters.numExpectedFreeSlots, lootingBag.getFreeSlots());
        assertEquals(expectedItemsValue, lootingBag.getValueOfItems());

        // Verify the looting bag items
        Assertions.assertLootingBagItems(lootingBag.getItems(), expectedItems);
    }

    private static class TestCaseProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            Stream<TelegrabItemTestCase> testCases = LootingBagTestCase.getAllTestCases(TelegrabItemTestCases.class)
                .map(testCase -> (TelegrabItemTestCase) testCase);

            return testCases.map(Arguments::of);
        }
    }
}
