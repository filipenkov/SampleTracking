package com.atlassian.instrumentation.operations;

import com.atlassian.instrumentation.operations.registry.OpRegistry;

import java.util.List;

/**
 * This factory allows you to track values for a given thread.  It can be used say for web request threads to know what
 * operations happened on that thread of execution.  You can snapshot it and then clear it at the end of the request.
 */
public interface ThreadOpTimerFactory extends OpTimerFactory
{
    OpTimer createOpTimer(String name, boolean captureCPUCost, OpTimer.OnEndCallback endCallback);

    /**
     * @return the {@link OpRegistry} for the current thread
     */
    OpRegistry getOpRegistry();

    /**
     * This will snapshot the thread local {@link OpRegistry} and clear its contents.
     * <p/>
     * You should call this say at the end of a request to get a snapshot of the counters for that request thread
     * and then reset it for the next set of requests.
     *
     * @return a Mutable list of OpSnapshot objects for this thread
     */
    List<OpSnapshot> snapshotAndClear();
}
