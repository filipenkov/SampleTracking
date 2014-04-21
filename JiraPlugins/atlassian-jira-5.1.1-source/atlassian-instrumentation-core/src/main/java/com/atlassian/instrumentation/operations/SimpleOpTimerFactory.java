package com.atlassian.instrumentation.operations;

/**
 * An implementation of OpTimerFactory
 */
public class SimpleOpTimerFactory implements OpTimerFactory
{
    @Override
    public OpTimer createOpTimer(final String name, final boolean captureCPUCost, final OpTimer.OnEndCallback endCallback)
    {
        return new SimpleOpTimer(name, captureCPUCost, endCallback);
    }
}
