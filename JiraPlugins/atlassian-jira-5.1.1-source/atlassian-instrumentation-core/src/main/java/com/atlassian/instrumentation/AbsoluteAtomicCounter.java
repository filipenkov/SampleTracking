package com.atlassian.instrumentation;

import java.util.concurrent.atomic.AtomicLong;

/**
 * An AbsoluteCounter is a {@link Counter} that resets its value to zero every time {@link #getValue()} is called.
 */
public class AbsoluteAtomicCounter extends AtomicCounter implements AbsoluteCounter
{
    public AbsoluteAtomicCounter(String name)
    {
        super(name);
    }

    public AbsoluteAtomicCounter(String name, long value)
    {
        super(name, value);
    }

    public AbsoluteAtomicCounter(String name, AtomicLong atomicLongRef)
    {
        super(name, atomicLongRef);
    }

    /**
     * Whenever this method is called, the counter is reset to zero after the values is returned.
     *
     * @return the current value of the counter
     */
    public long getValue()
    {
        return value.getAndSet(0);
    }
}
