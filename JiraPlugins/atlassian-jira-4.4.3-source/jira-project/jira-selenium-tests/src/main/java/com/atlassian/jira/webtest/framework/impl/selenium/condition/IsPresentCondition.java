package com.atlassian.jira.webtest.framework.impl.selenium.condition;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import org.apache.log4j.Logger;


/**
 * Condition that an element specified by given locator must be present.
 *
 * @since v4.2
 */
public class IsPresentCondition extends AbstractLocatorBasedTimedCondition implements TimedCondition
{
    private static final Logger log = Logger.getLogger(IsPresentCondition.class);


    public static final class Builder extends AbstractLocatorBasedTimedConditionBuilder<Builder,IsPresentCondition>
    {
        public Builder(SeleniumContext context)
        {
            super(context, Builder.class);
        }

        @Override
        public IsPresentCondition build()
        {
            return new IsPresentCondition(this);
        }
    }

    public static Builder forContext(SeleniumContext ctx)
    {
        return new Builder(ctx);
    }

    private IsPresentCondition(Builder builder)
    {
        super(builder);
    }

    public IsPresentCondition(SeleniumContext context, String locator)
    {
        super(context, locator);
    }

    public IsPresentCondition(SeleniumContext context, SeleniumLocator locator)
    {
        super(context, locator);
    }

    public IsPresentCondition(SeleniumContext context, Locator locator)
    {
        super(context, locator);
    }

    public boolean now()
    {
        log.debug("IsPresentCondition: evaluation SeleniumClient.isElementPresent(\"" + locator + "\")");
        return context.client().isElementPresent(locator);
    }
}
