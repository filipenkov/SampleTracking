package com.atlassian.jira.webtest.framework.impl.selenium.query;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.query.AbstractConditionBasedQuery;
import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.selenium.SeleniumClient;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Condition based timed query in the Selenium World&trade;.
 *
 * @see com.atlassian.jira.webtest.framework.core.query.AbstractConditionBasedQuery
 * @param <T> query result type
 * @since v4.3
 *
 */
public abstract class AbstractSeleniumConditionBasedQuery<T> extends AbstractConditionBasedQuery<T>
{
    protected final SeleniumContext context;
    protected final SeleniumClient client;

    protected AbstractSeleniumConditionBasedQuery(TimedCondition condition, SeleniumContext ctx,
            ExpirationHandler expirationHandler, long defTimeout)
    {
        super(condition, defTimeout, notNull("context", ctx).timeoutFor(Timeouts.EVALUATION_INTERVAL), expirationHandler);
        this.context = ctx;
        this.client = notNull("client", ctx.client());
    }

    protected AbstractSeleniumConditionBasedQuery(TimedCondition condition, SeleniumContext ctx,
            ExpirationHandler expirationHandler)
    {
        super(condition, expirationHandler);
        this.context = ctx;
        this.client = notNull("client", ctx.client());
    }

    protected AbstractSeleniumConditionBasedQuery(AbstractSeleniumConditionBasedQueryBuilder<?,?,T> builder)
    {
        this(builder.condition(), builder.context(), builder.expirationHandler(),
                builder.defaultTimeout());
    }
}
