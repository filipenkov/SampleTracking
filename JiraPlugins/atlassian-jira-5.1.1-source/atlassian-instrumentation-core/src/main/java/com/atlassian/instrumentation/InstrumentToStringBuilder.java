package com.atlassian.instrumentation;

/**
 * A string builder for {@link Instrument}s
 */
public class InstrumentToStringBuilder
{
    public static String toString(Instrument instrument)
    {
        return new StringBuilder(instrument.getName()).append(":").append(instrument.getValue()).toString();
    }
}
