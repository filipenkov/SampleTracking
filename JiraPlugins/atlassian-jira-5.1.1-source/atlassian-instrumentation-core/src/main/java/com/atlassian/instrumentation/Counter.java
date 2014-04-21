package com.atlassian.instrumentation;

/**
 * A Counter is a {@link Instrument} that can only increase in value.  It can never return a result that
 * is less than the last time it was called.
 * <p/>
 * For example a Counter that represents the "number of connections made" will never decrease in value over time, it can only increase in value.
 * <p/>
 * Counters are usually recorded in terms of rate of change over time, eg the rate of change in connections wihin a minute.  The fact that a Counter
 * can never go down in value, allows the recorder to better optimize how it stores is values.
 *
 * @see com.atlassian.instrumentation.Gauge
 * @since v4.0
 */
public interface Counter extends Instrument
{
    /**
     * Returns the new value after one has been added to the Counter.
     *
     * @return the new value after one has been added to the previous value
     */
    long incrementAndGet();

    /**
     * Adds the delta value to the current value and returns the new value.  delta MUSt be >= 0.  It can never be negative
     * otherwise a IllegalArgumentException will be throw
     *
     * @param delta the value to add
     * @return the new value after delta has been added to the current value
     * @throws IllegalArgumentException if the delta value is < 0
     */
    long addAndGet(long delta);
}
