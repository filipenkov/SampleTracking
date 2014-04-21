package com.atlassian.jira.index;

import com.atlassian.jira.index.Index.Result;
import com.atlassian.jira.util.NotNull;
import com.atlassian.util.concurrent.Timeout;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Used to build a {@link Result} implementation that aggregates results from
 * other operations and awaits on them all.
 */
public final class CompositeResultBuilder
{
    private final Collection<Index.Result> results = new LinkedBlockingQueue<Index.Result>();
    private final Collection<Runnable> completionTasks = new LinkedList<Runnable>();

    public CompositeResultBuilder add(@NotNull final Index.Result result)
    {
        removeDone();
        results.add(notNull("result", result));
        return this;
    }

    public CompositeResultBuilder addCompletionTask(@NotNull final Runnable runnable)
    {
        completionTasks.add(notNull("runnable", runnable));
        return this;
    }

    public Result toResult()
    {
        return new CompositeResult(results, completionTasks);
    }

    /**
     * Keep the results list small, we don't want to waste too much ram with
     * complete results.
     */
    private void removeDone()
    {
        if (results.size() % 100 == 0)
        {
            for (final Iterator<Index.Result> iterator = results.iterator(); iterator.hasNext();)
            {
                final Index.Result result = iterator.next();
                if (result.isDone())
                {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * This class holds the actual result objects and aggregates them. Once a
     * result has been awaited then it can be discarded.
     */
    static class CompositeResult implements Result
    {
        private final Collection<Index.Result> results;
        private final Queue<Runnable> completionTasks;

        CompositeResult(final Collection<Result> results, final Collection<Runnable> completionTasks)
        {
            this.results = new LinkedBlockingQueue<Index.Result>(results);
            this.completionTasks = new LinkedList<Runnable>(completionTasks);
        }

        public void await()
        {
            for (final Iterator<Result> it = results.iterator(); it.hasNext();)
            {
                // all threads should await
                it.next().await();
                // once run, they should be removed
                it.remove();
            }
            complete();
        }

        private void complete()
        {
            while (!completionTasks.isEmpty())
            {
                // only one thread should run these tasks
                final Runnable task = completionTasks.poll();
                // /CLOVER:OFF
                if (task != null)
                {
                    // /CLOVER:ON

                    task.run();
                }
            }
        }

        public boolean await(final long time, final TimeUnit unit)
        {
            final Timeout timeout = Timeout.getNanosTimeout(time, unit);
            for (final Iterator<Result> it = results.iterator(); it.hasNext();)
            {
                // all threads should await
                final Result result = it.next();
                if (!result.await(timeout.getTime(), timeout.getUnit()))
                {
                    return false;
                }
                // once run, they should be removed
                it.remove();
            }
            complete();
            return true;
        }

        public boolean isDone()
        {
            for (final Index.Result result : results)
            {
                if (!result.isDone())
                {
                    return false;
                }
            }
            return true;
        }
    }
}