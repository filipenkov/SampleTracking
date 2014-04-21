package com.atlassian.jira.webtest.framework.impl.selenium.locator;

import com.atlassian.jira.webtest.framework.core.Timeouts;
import com.atlassian.jira.webtest.framework.core.locator.Element;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.locator.LocatorData;
import com.atlassian.jira.webtest.framework.core.locator.LocatorType;
import com.atlassian.jira.webtest.framework.core.locator.mapper.DefaultLocatorMapper;
import com.atlassian.jira.webtest.framework.core.locator.mapper.LocatorDataBean;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContext;
import com.atlassian.jira.webtest.framework.impl.selenium.core.SeleniumContextAware;

import static com.atlassian.jira.util.lang.JiraStringUtils.asString;

/**
 * Abstract Selenium locator implementation. Implements location functionality
 * by using standard Selenium location syntax. Uses default locator mappings
 * defined in the framework API.
 *
 * @since v4.2
 */
public abstract class AbstractSeleniumLocator extends SeleniumContextAware implements Locator, SeleniumLocator
{
    private static final DefaultLocatorMapper LOCATOR_MAPPER = SeleniumLocators.SELENIUM_MAPPER;

    protected final LocatorData locatorData;
    protected final SeleniumElement element;

    protected AbstractSeleniumLocator(SeleniumContext ctx, LocatorType locatorType, String value)
    {
        super(ctx);
        this.locatorData = new LocatorDataBean(locatorType, value);
        this.element = new SeleniumElement(this, context);
    }

    protected AbstractSeleniumLocator(SeleniumContext ctx, LocatorType locatorType, String value, Timeouts defTimeout)
    {
        super(ctx);
        this.locatorData = new LocatorDataBean(locatorType, value);
        this.element = new SeleniumElement(this, context, defTimeout);
    }

    protected AbstractSeleniumLocator(SeleniumContext ctx, LocatorData locatorData)
    {
        super(ctx);
        this.locatorData = new LocatorDataBean(locatorData);
        this.element = new SeleniumElement(this, context);
    }

    protected AbstractSeleniumLocator(SeleniumContext ctx, LocatorData locatorData, Timeouts defTimeout)
    {
        super(ctx);
        this.locatorData = new LocatorDataBean(locatorData);
        this.element = new SeleniumElement(this, context, defTimeout);
    }

    public final LocatorType type()
    {
        return locatorData.type();
    }

    public final String value()
    {
        return locatorData.value();
    }

    public final Element element()
    {
        return element;
    }

    public final boolean supports(final Locator other)
    {
        return LOCATOR_MAPPER.supports(this, other);
    }

    public final SeleniumLocator combine(final Locator toNest)
    {
        LocatorData result = LOCATOR_MAPPER.combine(this, toNest);
        return SeleniumLocators.create(result, context).withDefaultTimeout(element.defaultTimeout());
    }

    @Override
    public String bareLocator()
    {
        return StringLocators.removeLocatorPrefix(fullLocator());
    }

    @Override
    public Timeouts defaultTimeout()
    {
        return element.defaultTimeout();
    }

    @Override
    public String toString()
    {
        return asString(getClass().getName(),"[type=",locatorData.type(),",value=",locatorData.value(),"]");
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (!AbstractSeleniumLocator.class.isInstance(obj))
        {
            return false;
        }
        AbstractSeleniumLocator that = (AbstractSeleniumLocator) obj;
        return this.locatorData.equals(that.locatorData);
    }

    @Override
    public int hashCode()
    {
        return locatorData.hashCode();
    }
}
