package com.atlassian.jira.webtest.framework.impl.selenium.query;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.util.AbstractSeleniumTimedObjectBuilder;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Abstract builder for implementations of
 * {@link com.atlassian.jira.webtest.framework.core.query.AbstractConditionBasedQuery}.
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumConditionBasedQueryBuilder<B extends AbstractSeleniumConditionBasedQueryBuilder<B,T,S>,
        T extends AbstractSeleniumConditionBasedQuery<S>, S> extends AbstractSeleniumTimedObjectBuilder<B,T>
{
    private ExpirationHandler expirationHandler;
    private TimedCondition condition; 
    private S nonEvalValue; 

    protected AbstractSeleniumConditionBasedQueryBuilder(SeleniumContext context, Class<B> target)
    {
        super(context, target);
    }

    public final B expirationHandler(ExpirationHandler handler)
    {
        this.expirationHandler = notNull("expirationHandler", handler);
        return asTargetType();
    }

    public final B condition(TimedCondition condition)
    {
        this.condition = notNull("condition", condition);
        return asTargetType();
    }

    public final B nonEvalValue(S nonEval)
    {
        this.nonEvalValue = notNull("nonEvalValue", nonEval);
        return asTargetType();
    }

    public ExpirationHandler expirationHandler()
    {
        return expirationHandler;
    }

    public TimedCondition condition()
    {
        return condition;
    }

    public S nonEvalValue()
    {
        return nonEvalValue;
    }
}
