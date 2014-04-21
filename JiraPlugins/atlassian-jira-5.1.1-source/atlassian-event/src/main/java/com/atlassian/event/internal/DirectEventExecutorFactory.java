package com.atlassian.event.internal;

import com.atlassian.event.config.EventThreadPoolConfiguration;
import com.atlassian.event.spi.EventExecutorFactory;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>Uses a {@link SynchronousQueue} to hand off tasks to the {@link Executor}. An attempt to to queue a task will fail if no threads are immediately available to run it</p>
 *
 * <p>See {@link ThreadPoolExecutor} for more information.</p>
 *
 * @since 2.1
 */
public class DirectEventExecutorFactory extends AbstractEventExecutorFactory
{
    public DirectEventExecutorFactory(final EventThreadPoolConfiguration configuration, final EventThreadFactory eventThreadFactory)
    {
        super(configuration, eventThreadFactory);
    }

    public DirectEventExecutorFactory(final EventThreadPoolConfiguration configuration)
    {
        super(configuration);
    }

    /**
     * @return a new {@link SynchronousQueue<Runnable>}
     */
    @Override
    protected BlockingQueue<Runnable> getQueue()
    {
        return new SynchronousQueue<Runnable>();
    }
}
