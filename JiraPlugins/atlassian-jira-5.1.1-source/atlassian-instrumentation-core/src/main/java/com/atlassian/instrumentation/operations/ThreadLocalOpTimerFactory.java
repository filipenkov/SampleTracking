package com.atlassian.instrumentation.operations;

import com.atlassian.instrumentation.operations.registry.OpRegistry;

import java.util.List;

import static com.atlassian.instrumentation.utils.dbc.Assertions.notNull;

/**
 * ThreadOpTimerFactory that is implemented using a ThreadLocal. Clients <b>must</b> call {@link #snapshotAndClear()}
 * in a finally block after each request to ensure that the thread local storage is removed. Failure to do so will
 * result in memory leaks.
 */
public class ThreadLocalOpTimerFactory implements ThreadOpTimerFactory
{
    private static final ThreadLocal<OpRegistry> THREAD_LOCAL = new ThreadLocal<OpRegistry>()
    {
        @Override
        protected OpRegistry initialValue()
        {
            return new OpRegistry();
        }
    };

    @Override
    public OpTimer createOpTimer(final String name, final boolean captureCPUCost, final OpTimer.OnEndCallback endCallback)
    {
        notNull("name", name);
        notNull("endCallback", endCallback);

        return new SimpleOpTimer(name, captureCPUCost, new OpTimer.OnEndCallback()
        {
            public void onEndCalled(OpSnapshot opSnapshot)
            {
                recordSnapshot(opSnapshot);
                endCallback.onEndCalled(opSnapshot);
            }
        });
    }

    private void recordSnapshot(OpSnapshot opSnapshot)
    {
        getOpRegistry().add(opSnapshot);
    }

    /**
     * @return the {@link OpRegistry} for the current thread
     */
    @Override
    public OpRegistry getOpRegistry()
    {
        return THREAD_LOCAL.get();
    }

    /**
     * This will snapshot the thread local {@link OpRegistry} and remove its associated storage.
     * <p/>
     * You should call this at the end of a request <b>in a finally block</b> to get a snapshot of the counters for that
     * request thread and then reset it for the next set of requests.
     *
     * @return a mutable list of OpSnapshot objects for this thread
     */
    @Override
    public List<OpSnapshot> snapshotAndClear()
    {
        try
        {
            return getOpRegistry().snapshotAndClear();
        }
        finally
        {
            THREAD_LOCAL.remove();
        }
    }
}
