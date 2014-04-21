package com.atlassian.instrumentation;

import java.util.concurrent.atomic.AtomicLong;

/**
 * An implementation of {@link DerivedCounter}
 */
public class DerivedAtomicCounter extends AtomicGauge implements DerivedCounter
{

    public DerivedAtomicCounter(final String name)
    {
        super(name);
    }

    public DerivedAtomicCounter(final String name, long value)
    {
        super(name, value);
    }

    public DerivedAtomicCounter(final String name, AtomicLong atomicLongRef)
    {
        super(name, atomicLongRef);
    }
}