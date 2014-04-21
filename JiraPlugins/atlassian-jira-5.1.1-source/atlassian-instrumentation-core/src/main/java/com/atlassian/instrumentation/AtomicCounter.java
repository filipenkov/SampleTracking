package com.atlassian.instrumentation;

import static com.atlassian.instrumentation.utils.dbc.Assertions.notNegative;
import static com.atlassian.instrumentation.utils.dbc.Assertions.notNull;
import com.atlassian.instrumentation.compare.InstrumentComparator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * An implementation of {@link com.atlassian.instrumentation.Counter} that uses an {@link AtomicLong} for its value.
 * <p/>
 * THREAD SAFETY - This classes uses AtomicLong under the covers to ensure consistent reads and writes of value
 *
 * @since v4.0
 */
public class AtomicCounter implements Counter
{
    protected final AtomicLong value;
    protected final String name;

    public AtomicCounter(final String name)
    {
        this(name, new AtomicLong());
    }

    public AtomicCounter(final String name, long value)
    {
        this(name, new AtomicLong(value));
    }

    /**
     * This constructor takes a reference to an AtomicLong that may be outside this Counter.  This could be used to
     * expose an external AtomicLong as a counter.
     *
     * @param name the name of the counter
     * @param atomicLongRef the reference to an AtomicLong
     */
    public AtomicCounter(final String name, AtomicLong atomicLongRef)
    {
        notNull("name", name);
        notNull("atomicLongRef", atomicLongRef);
        this.name = name;
        this.value = atomicLongRef;
    }

    public long incrementAndGet()
    {
        return value.incrementAndGet();
    }

    public long addAndGet(final long delta)
    {
        notNegative("delta", delta);
        return value.addAndGet(delta);
    }

    public String getName()
    {
        return name;
    }

    public long getValue()
    {
        return value.get();
    }

    public int compareTo(final Instrument that)
    {
        return new InstrumentComparator().compare(this,that);
    }

    @Override
    public String toString()
    {
        return InstrumentToStringBuilder.toString(this);
    }
    
}
