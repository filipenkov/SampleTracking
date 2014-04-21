package com.atlassian.jira.webtest.framework.gadget;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.context.AbstractWebTestContextAware;
import com.atlassian.jira.webtest.framework.core.context.WebTestContext;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.lang.JiraStringUtils.asString;

/**
 * {@link com.atlassian.jira.webtest.framework.core.condition.TimedCondition} that decorates another target condition
 * and changes target frame of its calls to hit the target gadget.
 *
 * @since v4.3
 */
public class GadgetTimedCondition extends AbstractWebTestContextAware implements TimedCondition
{
    private final TimedCondition condition;
    private final Gadget gadget;

    public GadgetTimedCondition(WebTestContext context, TimedCondition condition, Gadget gadget)
    {
        super(context);
        this.condition = notNull("condition", condition);
        this.gadget = notNull("gadget", gadget);
    }


    @Override
    public boolean by(long timeout)
    {
        try
        {
            context.ui().switchTo().frame(gadget.frameLocator());
            return condition.by(timeout);
        }
        finally
        {
            context.ui().switchTo().topFrame();
        }
    }

    @Override
    public boolean byDefaultTimeout()
    {
        try
        {
            context.ui().switchTo().frame(gadget.frameLocator());
            return condition.byDefaultTimeout();
        }
        finally
        {
            context.ui().switchTo().topFrame();
        }
    }

    @Override
    public boolean now()
    {
        try
        {
            context.ui().switchTo().frame(gadget.frameLocator());
            return condition.now();
        }
        finally
        {
            context.ui().switchTo().topFrame();
        }
    }

    @Override
    public long interval()
    {
        return condition.interval();
    }

    @Override
    public long defaultTimeout()
    {
        return condition.defaultTimeout();
    }

    @Override
    public String toString()
    {
        return asString(getClass().getName(), "<decorated=",condition,">");
    }
}
