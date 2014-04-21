package com.atlassian.event.internal;

import com.atlassian.event.config.EventThreadPoolConfiguration;
import com.atlassian.event.spi.EventExecutorFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link EventExecutorFactory} that allows the {@link Executor} to be produced with a custom {@link BlockingQueue}
 * @since 2.1
 */
public abstract class AbstractEventExecutorFactory implements EventExecutorFactory
{
    private final EventThreadPoolConfiguration configuration;
    private final EventThreadFactory eventThreadFactory;

    /**
     * @since 2.0.5
     * @param configuration
     * @param eventThreadFactory
     */
    public AbstractEventExecutorFactory(EventThreadPoolConfiguration configuration, EventThreadFactory eventThreadFactory)
    {
        this.configuration = checkNotNull(configuration);
        this.eventThreadFactory = checkNotNull(eventThreadFactory);
    }

    public AbstractEventExecutorFactory(EventThreadPoolConfiguration configuration)
    {
        this(configuration, new EventThreadFactory());
    }

    /**
     * @return a new {@link BlockingQueue<Runnable>} for the construction of a new {@link Executor}
     */
    protected abstract BlockingQueue<Runnable> getQueue();

    public Executor getExecutor()
    {
        return new ThreadPoolExecutor(
                configuration.getCorePoolSize(),
                configuration.getMaximumPoolSize(),
                configuration.getKeepAliveTime(),
                configuration.getTimeUnit(),
                getQueue(),
                eventThreadFactory);
    }
}
