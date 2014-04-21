package com.atlassian.instrumentation;

import static com.atlassian.instrumentation.utils.dbc.Assertions.notNull;
import com.atlassian.instrumentation.compare.InstrumentComparator;

/**
 * An ExternalCounter is a Counter that bases its value of an {@link ExternalValue} value
 * <p/>
 * This object is read only and the mutating methods throw {@link UnsupportedOperationException} if called.  The
 * reasoning for this is that it cant know how to change its delegate {@link ExternalValue}.
 *
 * @see ExternalValue
 * @since v4.0
 */
public class ExternalCounter implements Counter
{
    private final ExternalValue externalValue;
    private final String name;

    /**
     * Constructs a {@link Counter} that reads its value from the {@link ExternalValue} provided.
     *
     * @param name the name of the external counter
     * @param externalValue - the external value to read its value from.
     * @throws IllegalArgumentException if the externalValue is null
     */
    public ExternalCounter(String name, ExternalValue externalValue)
    {
        notNull("name", name);
        notNull("externalValue", externalValue);
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
