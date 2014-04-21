package com.atlassian.jira.webtest.framework.impl.selenium.query;

import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.util.AbstractTimedObjectBuilder;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Abstract builder for timed query containing setter for the
 * {@link com.atlassian.jira.webtest.framework.core.query.ExpirationHandler} of the target
 * {@link com.atlassian.jira.webtest.framework.core.query.TimedQuery}.
 *
 * @since v4.3
 */
public abstract class AbstractTimedQueryBuilder<B extends AbstractTimedObjectBuilder<B,T>, T extends AbstractSeleniumTimedQuery<S>, S>
        extends AbstractTimedObjectBuilder<B,T>
{
    protected ExpirationHandler expirationHandler;

    protected AbstractTimedQueryBuilder(SeleniumContext context, Class<B> target)
    {
        super(context, target);
    }

    public final B expirationHandler(ExpirationHandler handler)
    {
        this.expirationHandler = notNull("expirationHandler", handler);
        return asTargetType();
    }

    public ExpirationHandler expirationHandler()
    {
        return expirationHandler;
    }
}
