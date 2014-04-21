package com.atlassian.jira.webtest.framework.impl.selenium.condition;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;


/**
 * Condition that an element specified by given locator must be present and visible in the current test context.
 *
 * @since v4.2
 */
public class IsVisibleCondition extends AbstractLocatorBasedTimedCondition implements TimedCondition
{
    public static final class Builder extends AbstractLocatorBasedTimedConditionBuilder<Builder,IsVisibleCondition>
    {
        public Builder(SeleniumContext context)
        {
            super(context, Builder.class);
        }

        @Override
        public IsVisibleCondition build()
        {
            return new IsVisibleCondition(this);
        }
    }

    public static Builder forContext(SeleniumContext ctx)
    {
        return new Builder(ctx);
    }


    private IsVisibleCondition(Builder builder)
    {
        super(builder);
    }

    public IsVisibleCondition(SeleniumContext context, String locator)
    {
        super(context, locator);
    }

    public IsVisibleCondition(SeleniumContext context, SeleniumLocator locator)
    {
        super(context, locator);
    }

    public IsVisibleCondition(SeleniumContext context, Locator locator)
    {
        super(context, locator);
    }

    public boolean now()
    {
        return client.isElementPresent(locator) && client.isVisible(locator);
    }
}
