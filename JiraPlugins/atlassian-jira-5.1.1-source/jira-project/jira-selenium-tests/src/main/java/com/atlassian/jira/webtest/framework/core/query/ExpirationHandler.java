package com.atlassian.jira.webtest.framework.core.query;

import junit.framework.AssertionFailedError;

import static com.atlassian.jira.util.lang.JiraStringUtils.asString;

/**
 * Strategies for handling expired timeouts of the {@link TimedQuery}.
 *
 * @since v4.3
 */
public interface ExpirationHandler
{

    /**
     * Handle timeout expiration for given query.
     *
     * @param <T> type of the query result
     * @param query timed query, whose timeout has expired
     * @param currentValue current evaluation of the query
     * @param timeout timeout of the query
     * @return result value to be returned by the query ater the timeout expiration
     */
    <T> T expired(TimedQuery<T> query, T currentValue, long timeout);


    
    public static final ExpirationHandler RETURN_CURRENT = new ExpirationHandler()
    {
        @Override
        public <T> T expired(TimedQuery<T> query, T currentValue, long timeout)
        {
            return currentValue;
        }
    };

    public static final ExpirationHandler RETURN_NULL = new ExpirationHandler()
    {
        @Override
        public <T> T expired(TimedQuery<T> query, T currentValue, long timeout)
        {
            return null;
        }
    };

    public static final ExpirationHandler THROW_ASSERTION_ERROR = new ExpirationHandler()
    {
        @Override
        public <T> T expired(TimedQuery<T> query, T currentValue, long timeout)
        {
            throw new AssertionFailedError(asString("Timeout <",timeout,"> expired for: ", query));
        }
    };

    public static final ExpirationHandler THROW_ILLEGAL_STATE = new ExpirationHandler()
    {
        @Override
        public <T> T expired(TimedQuery<T> query, T currentValue, long timeout)
        {
            throw new IllegalStateException(asString("Timeout <",timeout,"> expired for: ", query));
        }
    };

}
