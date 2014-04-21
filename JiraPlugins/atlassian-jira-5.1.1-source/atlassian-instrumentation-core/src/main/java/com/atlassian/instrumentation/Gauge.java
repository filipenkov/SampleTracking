package com.atlassian.instrumentation;

/**
 * A Gauge is a {@link Instrument} that can oscillate its value over time.  It may be less than
 * the last time you called , the same or more than the last time you called it.
 * <p/>
 * A Gauge is used to represent a monitored value that can go up and down in value.  For example the number
 * of "simultaneous users" can go up and down over value.
 *
 * @see com.atlassian.instrumentation.Counter
 * @since v4.0
 */
public interface Gauge extends Instrument
{
    /**
     * Adds one to the Gauge and returns the new value
     *
     * @return the new value after one has been added to the previous value
     */
    public long incrementAndGet();

    /**
     * Decrements one from the Gauge and returns the new value
     *
     * @return the new value after one has been decremented from the previous value
     */
    public long decrementAndGet();

    /**
     * Adds the delta value to the Gauge and returns the new value.
     *
     * @param delta this can be positive or negative
     * @return the new value after the delta has been added to the previous value.
     */
    public long addAndGet(long delta);
}
