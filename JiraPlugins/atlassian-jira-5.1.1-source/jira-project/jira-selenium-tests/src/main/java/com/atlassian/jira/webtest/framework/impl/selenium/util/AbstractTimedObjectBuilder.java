package com.atlassian.jira.webtest.framework.impl.selenium.util;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContextAware;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.dbc.NumberAssertions.greaterThan;

/**
 * Abstract builder for timed objects that need to be provided a default timeout.
 *
 * @param <T> type of the target object
 * @see com.atlassian.jira.webtest.framework.core.condition.TimedCondition
 * @see com.atlassian.jira.webtest.framework.core.query.TimedQuery
 * @since v4.3
 */
public abstract class AbstractTimedObjectBuilder<B extends  AbstractTimedObjectBuilder<B,T>,T> extends SeleniumContextAware
{
    private final Class<B> targetType;
    private long defaultTimeout = 0;

    protected AbstractTimedObjectBuilder(SeleniumContext context, Class<B> target)
    {
        super(context);
        this.targetType = notNull("target", target);
    }

    public long defaultTimeout()
    {
        return defaultTimeout;
    }

    public final B defaultTimeout(long defaultTimeout)
    {
        this.defaultTimeout = greaterThan("defaultTimeout", defaultTimeout, 0);
        return asTargetType();
    }

    public final B defaultTimeout(Timeouts defaultTimeout)
    {
        final long inMillis = context.timeoutFor(notNull("defaultTimeout", defaultTimeout));
        this.defaultTimeout = greaterThan("defaultTimeout", inMillis, 0);
        return asTargetType();
    }

    protected final B asTargetType()
    {
        return targetType.cast(this);
    }

    public abstract T build();
}
