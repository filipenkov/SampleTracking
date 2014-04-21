package com.atlassian.applinks.core.concurrent;

import com.atlassian.sal.api.executor.ThreadLocalDelegateExecutorFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Provides asynchronous execution.
 *
 * @since   v3.1
 */
public class ConcurrentExecutor implements DisposableBean
{
    private final ExecutorService executor;

    public ConcurrentExecutor(final ThreadLocalDelegateExecutorFactory delegateExecutorFactory)
    {
        this.executor = delegateExecutorFactory.createExecutorService(
                Executors.newCachedThreadPool());
    }

    public void destroy() throws Exception
    {
        executor.shutdown();
    }

    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks) throws InterruptedException
    {
        return executor.invokeAll(tasks);
    }

    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException
    {
        return executor.invokeAll(tasks, timeout, unit);
    }

    public <T> T invokeAny(Collection<Callable<T>> tasks) throws InterruptedException, ExecutionException
    {
        return executor.invokeAny(tasks);
    }

    public <T> T invokeAny(Collection<Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
    {
        return executor.invokeAny(tasks, timeout, unit);
    }

    public <T> Future<T> submit(Callable<T> task)
    {
        return executor.submit(task);
    }

    public Future<?> submit(Runnable task)
    {
        return executor.submit(task);
    }

    public <T> Future<T> submit(Runnable task, T result)
    {
        return executor.submit(task, result);
    }

    public void execute(Runnable command)
    {
        executor.execute(command);
    }
}
