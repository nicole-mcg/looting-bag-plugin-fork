package com.lootingbag.handlers.pickup;

import com.lootingbag.MockedTestBase;
import com.lootingbag.testutil.MockUtils;
import com.lootingbag.testutil.RandomUtils;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ItemDespawned;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PickupActionTest extends MockedTestBase {

    @ParameterizedTest(name = "matchesItemDespawnedEvent should return {2} when itemIdMatches={0} and worldPointMatches={1}")
    @ArgumentsSource(MatchesItemDespawnedEventProvider.class)
    void testMatchesItemDespawnedEvent(boolean itemIdMatches, boolean worldPointMatches, boolean shouldMatch) {
        int itemId = RandomUtils.getRandomItemId();
        WorldPoint worldPoint = RandomUtils.getRandomWorldPoint();

        PickupAction pickupAction = new PickupAction(itemId, worldPoint);
        ItemDespawned itemDespawnedEvent = MockUtils.createItemDespawnedEvent(
            itemIdMatches ? itemId : RandomUtils.getRandomItemId(),
            RandomUtils.getRandomItemQuantity(), // Quantity doesn't matter for this test
            worldPointMatches ? worldPoint : RandomUtils.getRandomWorldPoint()
        );

        boolean matches = pickupAction.matchesItemDespawnedEvent(itemDespawnedEvent);
        assertEquals(shouldMatch, matches);
    }

    private static class MatchesItemDespawnedEventProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                Arguments.of(true, true, true),
                Arguments.of(false, true, false),
                Arguments.of(true, false, false),
                Arguments.of(false, false, false)
            );
        }
    }
}
