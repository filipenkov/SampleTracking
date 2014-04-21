package com.atlassian.crowd.util;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.event.config.EventThreadPoolConfiguration;
import com.atlassian.event.config.ListenerHandlersConfiguration;
import com.atlassian.event.internal.AsynchronousAbleEventDispatcher;
import com.atlassian.event.internal.DirectEventExecutorFactory;
import com.atlassian.event.internal.EventPublisherImpl;
import com.atlassian.event.internal.EventThreadPoolConfigurationImpl;
import com.atlassian.event.internal.ListenerHandlerConfigurationImpl;
import com.atlassian.event.spi.EventDispatcher;
import com.atlassian.event.spi.EventExecutorFactory;

/**
 * Factory for event publisher instances.
 */
public class CrowdEventPublisherFactory
{
    private CrowdEventPublisherFactory()
    {
        // Not to be instantiated.
    }

    public static EventPublisher createEventPublisher()
    {
        final EventThreadPoolConfiguration eventThreadPoolConfiguration = new EventThreadPoolConfigurationImpl();
        final EventExecutorFactory eventExecutorFactory = new DirectEventExecutorFactory(eventThreadPoolConfiguration);
        final EventDispatcher eventDispatcher = new AsynchronousAbleEventDispatcher(eventExecutorFactory);
        final ListenerHandlersConfiguration listenerHandlersConfiguration = new ListenerHandlerConfigurationImpl();

        return new EventPublisherImpl(eventDispatcher, listenerHandlersConfiguration);
    }
}
