package com.atlassian.jira.webtest.framework.core.query;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An abstract builder for {@link com.atlassian.jira.webtest.framework.core.query.AbstractConditionBasedQuery}
 * implementations.
 *
 * @since v4.3
 */
public abstract class AbstractConditionBasedTimedQueryBuilder<B extends AbstractConditionBasedTimedQueryBuilder<B,Q,V>, Q extends AbstractConditionBasedQuery<V>,V>
        extends AbstractTimedQueryBuilder<B,Q,V>
{
    private TimedCondition condition;
    private V nonEvalValue; 

    protected AbstractConditionBasedTimedQueryBuilder(Class<B> target)
    {
        super(target);
    }

    public final B condition(TimedCondition condition)
    {
        this.condition = notNull("condition", condition);
        return asTargetType();
    }

    public final B substituteValue(V nonEval)
    {
        this.nonEvalValue = notNull("nonEvalValue", nonEval);
        return asTargetType();
    }

    public final TimedCondition condition()
    {
        return condition;
    }

    public final V substituteValue()
    {
        return nonEvalValue;
    }

}
