package com.atlassian.jira.webtest.framework.core.condition;

import com.atlassian.jira.webtest.framework.core.PollingQuery;
import net.jcip.annotations.NotThreadSafe;

/**
 * <p>
 * Boolean condition with timeout.
 *
 * <p>
 * Implementations of this interface are not supposed to be thread-safe.
 *
 * @since v4.2
 */
@NotThreadSafe
public interface TimedCondition extends PollingQuery
{
    /**
     * Evaluate the condition by given timeout. That is, return <code>true</code> as soon as the
     * condition is <code>true</code>, otherwise return <code>false<code> when the given timeout expires.
     * 
     * @param timeout timeout in milliseconds (must be greater than 0)
     * @return <code>true</code>, if the underlying condition evaluates to <code>true</code> before the <tt>timeout</tt>
     * expires
     */
    boolean by(long timeout);

    /**
     * Evaluate the condition by a timeout deemed default by the condition in the given test context.
     *
     * @return <code>true</code>, if the underlying condition evaluates to <code>true</code> before the default timeout
     * expires
     */
    boolean byDefaultTimeout();

    /**
     * Evaluate the condition immediately.
     *
     * @return current evaluation of the underlying condition.
     */
    boolean now();

}
