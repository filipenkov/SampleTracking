package com.atlassian.instrumentation.operations;

import com.atlassian.instrumentation.Instrument;
import com.atlassian.instrumentation.InstrumentToStringBuilder;
import com.atlassian.instrumentation.utils.dbc.Assertions;
import com.atlassian.instrumentation.compare.InstrumentComparator;

/**
 * This class represents an {@link OpInstrument} that is calculated externally on demand
 * from values that are "external" to this class.
 * <p/>
 * The passed in {@link ExternalOpValue} is used to calculate the external {@link OpSnapshot}
 * value.
 */
public class ExternalOpInstrument implements OpInstrument
{
    private final String name;
    private final ExternalOpValue externalOpValue;

    public ExternalOpInstrument(String name, ExternalOpValue externalOpValue)
    {
        Assertions.notNull("name", name);
        Assertions.notNull("externalOpValue", externalOpValue);
        this.name = name;
        this.externalOpValue = externalOpValue;
    }

    public long getInvocationCount()
    {
        return externalOpValue.getSnapshot().getInvocationCount();
    }

    public long getMillisecondsTaken()
    {
        return externalOpValue.getSnapshot().getMillisecondsTaken();
    }

    public long getResultSetSize()
    {
        return externalOpValue.getSnapshot().getResultSetSize();
    }

    public long getCpuTime()
    {
        return externalOpValue.getSnapshot().getCpuTime();
    }

    public String getName()
    {
        return name;
    }

    public long getValue()
    {
        return externalOpValue.getSnapshot().getValue();
    }

    public int compareTo(final Instrument that)
    {
        return new InstrumentComparator().compare(this, that);
    }

    @Override
    public String toString()
    {
        return InstrumentToStringBuilder.toString(this);
    }
}
