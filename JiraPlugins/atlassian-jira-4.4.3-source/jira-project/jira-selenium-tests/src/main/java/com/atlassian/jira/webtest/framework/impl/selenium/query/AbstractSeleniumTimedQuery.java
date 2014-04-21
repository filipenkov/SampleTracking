package com.atlassian.jira.webtest.framework.impl.selenium.query;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.query.AbstractTimedQuery;
import com.atlassian.jira.webtest.framework.core.query.ExpirationHandler;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.selenium.SeleniumClient;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A {@link com.atlassian.jira.webtest.framework.core.query.TimedQuery} implementation with access to the
 * {@link com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext}.
 *
 * @since v4.3
 */
public abstract class AbstractSeleniumTimedQuery<T> extends AbstractTimedQuery<T> implements TimedQuery<T>
{
    protected final SeleniumContext context;
    protected final SeleniumClient client;


    protected AbstractSeleniumTimedQuery(SeleniumContext ctx, ExpirationHandler expirationHandler, long defTimeout)
    {
        super(defTimeout, notNull("context", ctx).timeoutFor(Timeouts.EVALUATION_INTERVAL), expirationHandler);
        this.context = ctx;
        this.client = notNull("client", ctx.client());
    }

    protected AbstractSeleniumTimedQuery(SeleniumContext ctx, ExpirationHandler handler, Timeouts defTimeout)
    {
        this(ctx, handler, ctx.timeoutFor(defTimeout));
    }

}
