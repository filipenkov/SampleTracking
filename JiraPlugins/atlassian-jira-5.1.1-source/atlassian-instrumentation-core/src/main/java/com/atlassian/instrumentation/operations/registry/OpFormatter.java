package com.atlassian.instrumentation.operations.registry;

import com.atlassian.instrumentation.operations.OpSnapshot;

import java.text.DecimalFormat;

/**
 * A helper class that can format the values of a {@link com.atlassian.instrumentation.operations.OpSnapshot} and work out some basic calculations for you.
 *
 * @since v4.0
 */
public class OpFormatter
{
    public static final String DEFAULT_FORMAT_STR = "#0.00#";

    private final OpSnapshot snapshot;

    public OpFormatter(OpSnapshot snapshot)
    {
        this.snapshot = snapshot;
    }

    public OpSnapshot getSnapshot()
    {
        return snapshot;
    }

    public String getInvocationCount()
    {
        return String.valueOf(snapshot.getInvocationCount());
    }

    public String getMillisecondsTaken()
    {
        return String.valueOf(snapshot.getMillisecondsTaken());
    }

    public String getMillisecondsPerInvocation()
    {
        return getMillisecondsPerInvocation(DEFAULT_FORMAT_STR);
    }

    public String getMillisecondsPerInvocation(String decimalFormatStr)
    {
        return timePerInvocationImpl(decimalFormatStr, snapshot.getMillisecondsTaken(), snapshot.getInvocationCount());
    }

    public String getSecondsTaken()
    {
        return getSecondsTaken(DEFAULT_FORMAT_STR);
    }

    public String getSecondsTaken(String decimalFormatStr)
    {
        final double secondsTaken = (float) snapshot.getMillisecondsTaken() / 1000.0;
        return timePerInvocationImpl(decimalFormatStr, secondsTaken, 1.0); // cheat
    }

    public String getSecondsPerInvocation()
    {
        return getSecondsPerInvocation(DEFAULT_FORMAT_STR);
    }

    public String getSecondsPerInvocation(String decimalFormatStr)
    {
        final double secondsTaken = (float) snapshot.getMillisecondsTaken() / 1000.0;
        return timePerInvocationImpl(decimalFormatStr, secondsTaken, snapshot.getInvocationCount());
    }

    private String timePerInvocationImpl(final String decimalFormatStr, final double timeTaken, final double invoCount)
    {
        DecimalFormat df = new DecimalFormat(decimalFormatStr);
        return df.format(timePerInvocationImpl(timeTaken, invoCount));
    }

    private double timePerInvocationImpl(final double timeTaken, final double invoCount)
    {
        return timeTaken == 0 ? 0 : (timeTaken / invoCount);
    }
}
