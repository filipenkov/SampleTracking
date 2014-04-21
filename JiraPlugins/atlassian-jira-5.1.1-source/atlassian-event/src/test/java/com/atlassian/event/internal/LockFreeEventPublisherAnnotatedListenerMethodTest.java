package com.atlassian.event.internal;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.event.config.ListenerHandlersConfiguration;
import com.atlassian.event.spi.ListenerHandler;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class LockFreeEventPublisherAnnotatedListenerMethodTest extends AbstractEventPublisherAnnotatedListenerMethodTest
{
    @Override
    EventPublisher getEventPublisherForTest()
    {
        return new LockFreeEventPublisher(new StubEventDispatcher(), new ListenerHandlersConfiguration()
        {
            public List<ListenerHandler> getListenerHandlers()
            {
                return Collections.<ListenerHandler> singletonList(new AnnotatedMethodsListenerHandler());
            }
        });
    }
}
