package com.atlassian.instrumentation.operations;

import com.atlassian.instrumentation.Instrument;

/**
 * An OpInstrument is a type of @{link Instrument} that exposes 4 values, number times it as invoked, the time taken,
 * the number of nanoseconds of cpu time and the result set size.
 * <p/>
 * It is up to the implementation to decide what is exposed via {@link com.atlassian.instrumentation.Instrument#getValue()}
 * but it is expected to be a synonym of one of these methods
 *
 * @since v4.0
 */
public interface OpInstrument extends Instrument
{

    long getInvocationCount();

    long getMillisecondsTaken();

    long getCpuTime();

    long getResultSetSize();
}
