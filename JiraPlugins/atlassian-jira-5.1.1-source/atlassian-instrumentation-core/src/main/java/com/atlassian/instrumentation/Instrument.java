package com.atlassian.instrumentation;

/**
 * An Instrument is a "named" monitored object that can return a value.  Its represents the value of something that is
 * being monitored over time.
 *
 * @since v4.0
 */
public interface Instrument extends Comparable<Instrument>
{
    /**
     * @return the name of the Instrument.
     */
    String getName();
    /**
     * @return returns the value of the monitored reading at the time the call was made
     */
    long getValue();
}
