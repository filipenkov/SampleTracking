package com.atlassian.jira.bc;

/**
 * A service result that also has an value.
 *
 * @since v4.2
 */
public interface ServiceOutcome<T> extends ServiceResult
{
    /**
     * Returns the value that was returned by the service, or null.
     *
     * @return the value returned by the service, or null
     */
    T getReturnedValue();
}
