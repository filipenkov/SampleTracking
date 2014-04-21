package com.atlassian.jira.webtest.framework.util;

import com.atlassian.jira.util.lang.Builder;

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
public abstract class AbstractTimedObjectBuilder<B extends AbstractTimedObjectBuilder<B,T>,T> implements Builder<T>
{
    private final Class<B> targetType;
    protected long evaluationInterval = 0;
    protected long defaultTimeout = 0;


    protected AbstractTimedObjectBuilder(Class<B> target)
    {
        this.targetType = notNull("target", target);
    }

    public final long defaultTimeout()
    {
        return defaultTimeout;
    }

    public final B defaultTimeout(long defaultTimeout)
    {
        this.defaultTimeout = greaterThan("defaultTimeout", defaultTimeout, 0);
        return asTargetType();
    }

    public final long evaluationInterval()
    {
        return evaluationInterval;
    }

    public final B evaluationInterval(long evaluationInterval)
    {
        this.evaluationInterval = greaterThan("evaluationInterval", evaluationInterval, 0);
        return asTargetType();
    }

    protected final B asTargetType()
    {
        return targetType.cast(this);
    }

    public abstract T build();
}
