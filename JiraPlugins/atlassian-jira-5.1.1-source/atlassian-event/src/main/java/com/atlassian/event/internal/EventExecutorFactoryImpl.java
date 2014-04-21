package com.atlassian.event.internal;

import com.atlassian.event.config.EventThreadPoolConfiguration;

/**
 * @deprecated use {@link DirectEventExecutorFactory}
 */
@Deprecated
public class EventExecutorFactoryImpl extends DirectEventExecutorFactory
{
    public EventExecutorFactoryImpl(final EventThreadPoolConfiguration configuration, final EventThreadFactory eventThreadFactory)
    {
        super(configuration, eventThreadFactory);
    }

    public EventExecutorFactoryImpl(final EventThreadPoolConfiguration configuration)
    {
        super(configuration);
    }
}
