package com.atlassian.jira.webtest.framework.gadget;

import com.atlassian.jira.webtest.framework.core.context.AbstractWebTestContextAware;
import com.atlassian.jira.webtest.framework.core.context.WebTestContext;
import com.atlassian.jira.webtest.framework.core.query.TimedQuery;

import java.util.concurrent.TimeUnit;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.lang.JiraStringUtils.asString;

/**
 * {@link com.atlassian.jira.webtest.framework.core.query.TimedQuery} switching focus into gadget frame for
 * the time of invocation.
 *
 * @since v4.3
 */
public class GadgetTimedQuery<T> extends AbstractWebTestContextAware implements TimedQuery<T>
{

    private final TimedQuery<T> query;
    private final Gadget gadget;

    public GadgetTimedQuery(WebTestContext context, TimedQuery<T> query, Gadget gadget)
    {
        super(context);
        this.query = notNull("query", query);
        this.gadget = notNull("gadget", gadget);
    }

    @Override
    public T by(long timeoutInMillis)
    {
        try
        {
            context.ui().switchTo().frame(gadget.frameLocator());
            return query.by(timeoutInMillis);
        }
        finally
        {
            context.ui().switchTo().topFrame();
        }
    }

    @Override
    public T by(long timeout, TimeUnit unit)
    {
        try
        {
            context.ui().switchTo().frame(gadget.frameLocator());
            return query.by(timeout, unit);
        }
        finally
        {
            context.ui().switchTo().topFrame();
        }
    }

    @Override
    public T byDefaultTimeout()
    {
        try
        {
            context.ui().switchTo().frame(gadget.frameLocator());
            return query.byDefaultTimeout();
        }
        finally
        {
            context.ui().switchTo().topFrame();
        }
    }

    @Override
    public T now()
    {
        try
        {
            context.ui().switchTo().frame(gadget.frameLocator());
            return query.now();
        }
        finally
        {
            context.ui().switchTo().topFrame();
        }
    }

    @Override
    public long interval()
    {
        return query.interval();
    }

    @Override
    public long defaultTimeout()
    {
        return query.defaultTimeout();
    }

    @Override
    public String toString()
    {
        return asString(getClass().getName(), "<decorated=",query,">");
    }
}
