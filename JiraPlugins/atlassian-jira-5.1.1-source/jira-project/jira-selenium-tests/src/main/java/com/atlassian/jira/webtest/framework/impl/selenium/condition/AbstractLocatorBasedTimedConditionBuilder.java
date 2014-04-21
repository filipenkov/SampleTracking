package com.atlassian.jira.webtest.framework.impl.selenium.condition;

import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.StringLocators;

/**
 * Abstract builder for {@link AbstractLocatorBasedTimedCondition}.
 *
 * @since v4.3
 */
public abstract class AbstractLocatorBasedTimedConditionBuilder<B extends AbstractLocatorBasedTimedConditionBuilder<B,T>,
        T extends AbstractLocatorBasedTimedCondition> extends AbstractSeleniumTimedConditionBuilder<B,T>
{
    private String locator;

    protected AbstractLocatorBasedTimedConditionBuilder(SeleniumContext context, Class<B> target)
    {
        super(context, target);
    }

    public final B locator(String locator)
    {
        this.locator = Assertions.notNull("locator", locator);
        return asTargetType();
    }


    public final B locator(SeleniumLocator locator)
    {
        return locator(locator.fullLocator());
    }

    public final B locator(Locator locator)
    {
        if (locator instanceof SeleniumLocator)
        {
            return locator((SeleniumLocator) locator);
        }
        this.locator = StringLocators.create(locator);
        return asTargetType();
    }

    public final String locator()
    {
        return locator;
    }

}
