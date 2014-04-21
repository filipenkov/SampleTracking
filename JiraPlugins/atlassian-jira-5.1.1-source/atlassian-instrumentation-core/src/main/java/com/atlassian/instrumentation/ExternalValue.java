package com.atlassian.instrumentation;

/**
 * The {@link ExternalValue} interface is used to provide
 * {@link com.atlassian.instrumentation.Instrument} values from some external piece of code.
 * <p/>
 * For example imagine that another part of the system has a counter/gauge type value that you want to
 * read just like a normal {@link com.atlassian.instrumentation.Instrument}.  You can wrap it in an
 * implementation of this interface.
 *
 * @see com.atlassian.instrumentation.ExternalCounter
 * @see com.atlassian.instrumentation.ExternalGauge
 * @since v4.0
 */
public interface ExternalValue
{
    /**
     * @return the current value of the external object
     */
    long getValue();
}
