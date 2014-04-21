package com.atlassian.instrumentation;

/**
 * A AbsoluteCounter is a marker interface for a {@link Counter} that resets is value to 0 every time {@link
 * #getValue()} is called.
 * <p/>
 * Use this when the value in the counter changes a lot and hence is likely to wrap
 *
 * @since v4.0
 */
public interface AbsoluteCounter extends Counter
{
    /**
     * Whenever this is called, the current value is returned and the value is reset to zero.
     *
     * @return the current value of the {@link Counter}
     */
    long getValue();
}
