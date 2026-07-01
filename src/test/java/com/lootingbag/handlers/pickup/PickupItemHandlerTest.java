package com.lootingbag.handlers.pickup;

import com.google.inject.testing.fieldbinder.Bind;
import com.lootingbag.MockedTestBase;
import com.lootingbag.testutil.MockUtils;
import com.lootingbag.lootingbagcontainer.LootingBag;
import com.lootingbag.lootingbagcontainer.LootingBagSettings;
import com.lootingbag.state.InventoryTracker;
import com.lootingbag.testutil.RandomUtils;
import net.runelite.api.MenuAction;
import net.runelite.api.events.MenuOptionClicked;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PickupItemHandlerTest extends MockedTestBase {

    @Mock
    @Bind
    InventoryTracker inventoryTracker;

    @Mock
    @Bind
    LootingBag lootingBag;

    @Mock
    @Bind
    LootingBagSettings lootingBagSettings;

    @Mock
    @Bind
    TelegrabHandler telegrabHandler;

    @Bind
    @InjectMocks
    PickupItemHandler pickupItemHandler;

    @ParameterizedTest(name = "isPickupMenuOption should return {2} when menuActionMatches={0} and menuOptionMatches={1}")
    @ArgumentsSource(MatchesItemDespawnedEventProvider.class)
    void isPickupMenuOptionTest(boolean menuActionMatches, boolean menuOptionMatches, boolean shouldMatch) {
        MenuAction menuAction = menuActionMatches
            ? MenuAction.GROUND_ITEM_THIRD_OPTION
            : RandomUtils.randomEnum(MenuAction.class);
        String menuOption = menuOptionMatches
            ? "Take"
            : RandomUtils.randomItemName();
        MenuOptionClicked menuOptionClicked = MockUtils.createMenuOptionClickedEvent(
            menuAction,
            menuOption,
            RandomUtils.getRandomWorldPoint(),
            RandomUtils.getRandomWorldView()
        );

        boolean matches = pickupItemHandler.isPickupMenuOption(menuOptionClicked);
        assertEquals(shouldMatch, matches);
    }

    private static class MatchesItemDespawnedEventProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(true, true, true),
                Arguments.of(true, false, false),
                Arguments.of(false, true, false),
                Arguments.of(false, false, false)
            );
        }
    }
}
