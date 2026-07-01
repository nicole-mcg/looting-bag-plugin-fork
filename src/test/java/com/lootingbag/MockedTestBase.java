package com.lootingbag;

import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import net.runelite.api.Client;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public abstract class MockedTestBase {

    @Mock
    @Bind
    public Client client;

    @Mock
    @Bind
    public ItemManager itemManager;

    @Mock
    @Bind
    public OverlayManager overlayManager;

    private AutoCloseable mocks;

    @BeforeEach
    protected void setUp() {
        this.mocks = MockitoAnnotations.openMocks(this);
        Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
    }

    @AfterEach
    protected void cleanUp() throws Exception {
        mocks.close();
    }

}
