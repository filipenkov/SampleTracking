package com.atlassian.event.internal;

import com.atlassian.event.config.EventThreadPoolConfiguration;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>Uses a {@link LinkedBlockingQueue} to hand off tasks to the {@link Executor}. Will cause new tasks to wait in the queue when all threads are busy.</p>
 *
 * <p>See {@link ThreadPoolExecutor} for more information.</p>
 *
 * @since 2.1
 */
public class UnboundedEventExecutorFactory extends AbstractEventExecutorFactory
{
    public UnboundedEventExecutorFactory(final EventThreadPoolConfiguration configuration, final EventThreadFactory eventThreadFactory)
    {
        super(configuration, eventThreadFactory);
    }

    public UnboundedEventExecutorFactory(final EventThreadPoolConfiguration configuration)
    {
        super(configuration);
    }

    @Override
    protected BlockingQueue<Runnable> getQueue()
    {
        return new LinkedBlockingQueue<Runnable>();
    }
}
