package com.atlassian.jira.util.concurrent;

import com.atlassian.jira.util.RuntimeInterruptedException;
import com.atlassian.multitenant.juc.MultiTenantExecutors;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * BoundedExecutor is an Executor wrapper that bounds the number of runnables
 * allowed on the Executor queue. {@link #execute(Runnable)} waits if the number
 * of runnables already on the queue equals the maximum number of permits available.
 */
public class BoundedExecutor implements Executor
{
    private final ExecutorService executor;
    private final Lock lock;

    public BoundedExecutor(final ExecutorService executor, final int permits)
    {
        this.executor = MultiTenantExecutors.wrap(executor);
        lock = new SemaphoreLock(permits);
    }

    public void execute(final Runnable command)
    {
        lock.lock();
        try
        {
            executor.execute(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        command.run();
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
            });
        }
        catch (final RejectedExecutionException rej)
        {
            lock.unlock();
            throw rej;
        }
    }

    public <T> Future<T> submit(final Callable<T> task)
    {
        lock.lock();
        try
        {
            return executor.submit(new Callable<T>()
            {
                public T call() throws Exception
                {
                    try
                    {
                        return task.call();
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
            });
        }
        catch (final RejectedExecutionException rej)
        {
            lock.unlock();
            throw rej;
        }
    }

    /**
     * shutdown the ExecutorService and wait for it. This method is not interruptible. 
     */
    public void shutdownAndWait()
    {
        executor.shutdown();
        while (true)
        {
            try
            {
                while (!executor.awaitTermination(60L, TimeUnit.SECONDS))
                {
                    // Wait for the pool to shutdown.
                }
                break;
            }
            catch (final InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void shutdownAndIgnoreQueue()
    {
        executor.shutdownNow();
    }

    private final class SemaphoreLock implements Lock
    {
        private final Semaphore semaphore;

        public SemaphoreLock(final int permits)
        {
            semaphore = new Semaphore(permits);
        }

        public void lock()
        {
            try
            {
                semaphore.acquire();
            }
            catch (final InterruptedException e)
            {
                throw new RuntimeInterruptedException(e);
            }
        }

        public void unlock()
        {
            semaphore.release();
        }

        public void lockInterruptibly() throws InterruptedException
        {
            throw new UnsupportedOperationException();
        }

        public Condition newCondition()
        {
            throw new UnsupportedOperationException();
        }

        public boolean tryLock()
        {
            throw new UnsupportedOperationException();
        }

        public boolean tryLock(final long time, final TimeUnit unit) throws InterruptedException
        {
            throw new UnsupportedOperationException();
        }
    }
}