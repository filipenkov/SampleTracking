package com.atlassian.instrumentation;

import static com.atlassian.instrumentation.utils.dbc.Assertions.notNull;
import com.atlassian.instrumentation.compare.InstrumentComparator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A simple implementation of {@link com.atlassian.instrumentation.Gauge}
 * <p/>
 * THREAD SAFETY - This classes uses AtomicLong under the covers to ensure consistent reads and writes of value
 *
 * @since v4.0
 */
public class AtomicGauge implements Gauge
{
    protected final AtomicLong value;
    protected final String name;

    public AtomicGauge(final String name)
    {
        this(name, new AtomicLong());
    }

    public AtomicGauge(final String name, long value)
    {
        this(name, new AtomicLong(value));
    }

    /**
     * This constructor takes a reference to an AtomicLong that may be outside this Gauge.  This could be used to expose
     * an external AtomicLong as a gauge.
     *
     * @param name the name of the gauge
     * @param atomicLongRef the reference to an AtomicLong
     */
    public AtomicGauge(final String name, AtomicLong atomicLongRef)
    {
        notNull("name", name);
        notNull("atomicLongRef", atomicLongRef);
        this.name = name;
        this.value = atomicLongRef;
    }

    public String getName()
    {
        return name;
    }

    public long incrementAndGet()
    {
        return value.incrementAndGet();
    }

    public long decrementAndGet()
    {
        return value.decrementAndGet();
    }

    public long addAndGet(final long delta)
    {
        return value.addAndGet(delta);
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
