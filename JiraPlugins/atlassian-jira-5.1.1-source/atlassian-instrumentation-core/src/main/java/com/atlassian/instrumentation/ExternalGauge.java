package com.atlassian.instrumentation;

import com.atlassian.instrumentation.utils.dbc.Assertions;
import com.atlassian.instrumentation.compare.InstrumentComparator;

/**
 * An ExternalGauge is a Gauge that bases its value of an {@link ExternalValue} value
 * <p/>
 * This object is read only and the mutating methods throw {@link UnsupportedOperationException} if called.  The
 * reasoning for this is that it cant know how to change its delegate {@link ExternalValue}.
 *
 * @since v4.0
 */
public class ExternalGauge implements Gauge
{
    private final ExternalValue externalValue;
    private final String name;

    /**
     * Constructs a {@link com.atlassian.instrumentation.Gauge} that reads its value from the {@link ExternalValue}
     * provided.
     *
     * @param name the name of the Gauge
     * @param externalValue - the ExternalInstrument to read its value from.
     * @throws IllegalArgumentException if the externalValue is null
     */
    public ExternalGauge(String name, ExternalValue externalValue)
    {
        Assertions.notNull("name", name);
        Assertions.notNull("externalValue", externalValue);
        this.name = name;
        this.externalValue = externalValue;
    }

    public String getName()
    {
        return name;
    }

    public long incrementAndGet()
    {
        throw new UnsupportedOperationException("Changing the value from this object is not supported");
    }

    public long decrementAndGet()
    {
        throw new UnsupportedOperationException("Changing the value from this object is not supported");
    }

    public long addAndGet(final long delta)
    {
        throw new UnsupportedOperationException("Changing the value from this object is not supported");
    }

    public long getValue()
    {
        return externalValue.getValue();
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