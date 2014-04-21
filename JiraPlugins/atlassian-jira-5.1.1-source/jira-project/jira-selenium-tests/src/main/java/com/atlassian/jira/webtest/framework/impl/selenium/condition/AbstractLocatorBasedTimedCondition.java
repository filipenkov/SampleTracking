package com.atlassian.jira.webtest.framework.impl.selenium.condition;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.SeleniumLocator;
import com.atlassian.jira.webtest.framework.impl.selenium.locator.StringLocators;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.atlassian.jira.util.lang.JiraStringUtils.asString;

/**
 * Abstract implementation of the {@link com.atlassian.jira.webtest.framework.core.condition.TimedCondition}
 * based on a Selenium locator.
 *
 * @since v4.2
 */
public abstract class AbstractLocatorBasedTimedCondition extends AbstractSeleniumTimedCondition
{
    protected final String locator;

    protected AbstractLocatorBasedTimedCondition(AbstractLocatorBasedTimedConditionBuilder<?,?> builder)
    {
        this(builder.context(), builder.locator(), builder.defaultTimeout());
    }

    protected AbstractLocatorBasedTimedCondition(SeleniumContext context, String locator)
    {
        super(context);
        this.locator = notNull("locator", locator);
    }

    protected AbstractLocatorBasedTimedCondition(SeleniumContext context, Locator locator)
    {
        super(context);
        this.locator = notNull("locator", StringLocators.create(locator));
    }

    protected AbstractLocatorBasedTimedCondition(SeleniumContext context, SeleniumLocator locator)
    {
        super(context);
        this.locator = notNull("locator", locator.fullLocator());
    }

    protected AbstractLocatorBasedTimedCondition(SeleniumContext context, String locator, long defaultTimeout)
    {
        super(context, defaultTimeout);
        this.locator = notNull("locator", locator);
    }

    protected AbstractLocatorBasedTimedCondition(SeleniumContext context, Locator locator, long defaultTimeout)
    {
        super(context, defaultTimeout);
        this.locator = notNull("locator", StringLocators.create(locator));
    }

    protected AbstractLocatorBasedTimedCondition(SeleniumContext context, SeleniumLocator locator, long defaultTimeout)
    {
        super(context, defaultTimeout);
        this.locator = notNull("locator", locator.fullLocator());
    }

    public final String locator()
    {
        return locator;
    }

    @Override
    public String toString()
    {
        return asString(getClass().getName(),"[locator=",locator,",interval=",interval,",defaultTimeout=",defaultTimeout,"]");
    }

}
