package com.atlassian.jira.webtest.framework.core;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.dbc.NumberAssertions.greaterThan;

/**
 * Abstract implementation of the {@link PollingQuery} interface.
 *
 * @since v4.3
 */
public class AbstractPollingQuery
{
    protected final long interval;
    protected final long defaultTimeout;

    protected AbstractPollingQuery(long interval, long defaultTimeout)
    {
        this.interval = greaterThan("interval", interval, 0);
        this.defaultTimeout = greaterThan("defaultTimeout", defaultTimeout, 0);
    }

    protected AbstractPollingQuery(PollingQuery other)
    {
        this(notNull("other", other).interval(), other.defaultTimeout());
    }

    public long interval()
    {
        return interval;
    }

    public long defaultTimeout()
    {
        return defaultTimeout;
    }
}
