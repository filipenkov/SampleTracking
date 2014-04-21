package com.atlassian.event.inject;

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
import com.atlassian.inject.AbstractModule;

public final class AtlassianEventModule extends AbstractModule
{
    public static final String EVENT_PUBLISHER = "eventPublisher";

    protected void configure()
    {
        bind(EventPublisher.class).to(EventPublisherImpl.class).named(EVENT_PUBLISHER).availableToPlugins();
        bind(EventDispatcher.class).to(AsynchronousAbleEventDispatcher.class);
        bind(EventExecutorFactory.class).to(DirectEventExecutorFactory.class);

        // override those following binding to customise this module
        bind(EventThreadPoolConfiguration.class).to(EventThreadPoolConfigurationImpl.class);
        bind(ListenerHandlersConfiguration.class).to(ListenerHandlerConfigurationImpl.class);
    }
}
