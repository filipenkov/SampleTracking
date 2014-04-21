package com.atlassian.event.internal;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.event.config.ListenerHandlersConfiguration;
import com.atlassian.event.legacy.LegacyListenerHandler;
import com.atlassian.event.spi.ListenerHandler;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

/**
 * <strong>Note:</strong> This is more of a (small) integration test as we use a concrete {@link com.atlassian.event.legacy.LegacyListenerHandler}
 * at runtime here (and not a stub/mock). It essentially ensure that we get the same behavior at the legacy event manager.
 */
@RunWith(MockitoJUnitRunner.class)
public class LockFreeEventPublisherLegacyCompatibilityTest extends AbstractEventPublisherLegacyCompatibilityTest
{
    @Override
    EventPublisher getEventPublisher()
    {
        return new LockFreeEventPublisher(new StubEventDispatcher(), new ListenerHandlersConfiguration()
        {
            public List<ListenerHandler> getListenerHandlers()
            {
                return Collections.<ListenerHandler> singletonList(new LegacyListenerHandler());
            }
        });
    }
}
