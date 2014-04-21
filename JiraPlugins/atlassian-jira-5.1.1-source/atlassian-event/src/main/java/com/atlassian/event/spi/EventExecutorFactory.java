package com.atlassian.event.spi;

import java.util.concurrent.Executor;

/**
 * <p>A factory to create executors for asynchronous event handling</p>
 */
public interface EventExecutorFactory
{
    /**
     * @return a new {@link Executor}
     */
    Executor getExecutor();
}
