package com.atlassian.instrumentation.operations;

/**
 * This will create {@link OpTimer} instances
 */
public interface OpTimerFactory
{
    /**
     * This creates an OpTimer based on the passed in name and that will invoke the callback
     * when end is called.
     *
     *
     * @param name           the name of the OpTimer
     * @param captureCPUCost true of the CPU cost of the operation is to be captured
     * @param endCallback    a call backed to be invokd when end is called on the OpTimer
     * @return an OpTimer
     */
    OpTimer createOpTimer(final String name, final boolean captureCPUCost, final OpTimer.OnEndCallback endCallback);
}
