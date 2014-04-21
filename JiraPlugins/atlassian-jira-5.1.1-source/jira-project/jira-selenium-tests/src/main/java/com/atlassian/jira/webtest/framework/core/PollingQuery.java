package com.atlassian.jira.webtest.framework.core;

/**
 * A query over the state of the current test that is capable of being repeated at given intervals in an attempt to
 * wait for a desired result.
 *
 * @since v4.3
 */
public interface PollingQuery
{

    /**
     * An interval (in milliseconds) that will be used to periodically evaluate the query.
     *
     * @return evaluation interval of this query.
     */
    long interval();

    /**
     * Default timeout of this query in the current test context.
     *
     * @return default timeout of this query
     */
    long defaultTimeout();
}
