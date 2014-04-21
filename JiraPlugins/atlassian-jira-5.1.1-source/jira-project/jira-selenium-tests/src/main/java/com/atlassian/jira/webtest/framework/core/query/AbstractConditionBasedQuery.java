package com.atlassian.jira.webtest.framework.core.query;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import net.jcip.annotations.NotThreadSafe;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A {@link TimedQuery} that will only return if a given {@link com.atlassian.jira.webtest.framework.core.condition.TimedCondition}
 * is met.
 *
 * @param <T> type of the query result
 * @since v4.3
 */
@NotThreadSafe
public abstract class AbstractConditionBasedQuery<T> extends AbstractTimedQuery<T>
{
    private final TimedCondition conditon;
    private boolean lastConditionEval = false;


    protected AbstractConditionBasedQuery(TimedCondition condition, long defTimeout, long interval,
            ExpirationHandler expirationHandler)
    {
        super(defTimeout, interval, expirationHandler);
        this.conditon = notNull("condition", condition);
    }

    /**
     * Use timeout configuration from the underlying <tt>condition</tt>. User custom <tt>expirationHandler</tt>.
     *
     * @param condition underlying condition
     * @param expirationHandler expiration handler
     */
    protected AbstractConditionBasedQuery(TimedCondition condition, ExpirationHandler expirationHandler)
    {
        this(condition, condition.defaultTimeout(), condition.interval(), expirationHandler);
    }

    /**
     * By default, this query will return <code>null</code>, if the underlying <tt>condition</tt> is not met before
     * timeout.
     *
     * @param condition underlying conditon
     */
    protected AbstractConditionBasedQuery(TimedCondition condition)
    {
        this(condition, condition.defaultTimeout(), condition.interval(), ExpirationHandler.RETURN_NULL);
    }

    @Override
    protected final boolean shouldReturn(T currentEval)
    {
        return lastConditionEval;
    }

    @Override
    protected final T currentValue()
    {
        lastConditionEval = conditon.now();
        if (!lastConditionEval)
        {
            // don't evaluate query if condition is not met
            return substituteValue();
        }
        return evaluateNow();
    }


    /**
     * Hook for subclasses, semantics are basically the same as {@link #currentValue()}.
     *
     * @return current evaluation of this query
     * @see AbstractTimedQuery#currentValue() 
     */
    protected abstract T evaluateNow();

    /**
     * <p>
     * A 'null' value for given context that will be returned by {@link #currentValue()} in case the underlying condition
     * evaluates to <code>false<code>.
     *
     * <p>
     * Subclasses may override this method to provide non-null empty values, depending on the query context
     *
     * @return 'null' substitution of the query result
     */
    protected T substituteValue()
    {
        return null;   
    }

    public final TimedCondition condition()
    {
        return conditon;
    }
}
