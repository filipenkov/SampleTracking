package com.atlassian.jira.webtest.framework.impl.webdriver.locator;

import com.atlassian.jira.webtest.framework.core.locator.Element;
import com.atlassian.jira.webtest.framework.core.locator.Locator;
import com.atlassian.jira.webtest.framework.core.locator.LocatorData;
import com.atlassian.jira.webtest.framework.core.locator.LocatorType;
import com.atlassian.jira.webtest.framework.core.locator.mapper.DefaultLocatorMapper;
import com.atlassian.jira.webtest.framework.core.locator.mapper.LocatorDataBean;
import com.atlassian.jira.webtest.framework.impl.webdriver.core.WebDriverContext;
import com.atlassian.jira.webtest.framework.impl.webdriver.core.WebDriverContextAware;
import org.openqa.selenium.By;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of the {@link com.atlassian.jira.webtest.framework.core.locator.Locator} in the
 * WebDriver world.
 *
 * @since v4.3
 */
public class DefaultWebDriverLocator extends WebDriverContextAware implements WebDriverLocator
{
    private final LocatorData locatorData;
    private final By by;
    private final WebDriverElement element;
    private final DefaultLocatorMapper mapper = WebDriverMapperFactory.newMapper();

    public DefaultWebDriverLocator(LocatorData locatorData, WebDriverContext ctx)
    {
        super(ctx);
        this.locatorData = notNull("locatorData",locatorData);
        this.by = WebDriverLocators.forType(type()).create(value());
        this.element = new WebDriverElement(by, context);
    }

    public DefaultWebDriverLocator(LocatorType type, String locatorValue, WebDriverContext ctx)
    {
        super(ctx);
        this.locatorData = new LocatorDataBean(type, locatorValue);
        this.by = WebDriverLocators.forType(type()).create(value());
        this.element = new WebDriverElement(by, context);
    }

    public LocatorType type()
    {
        return locatorData.type();
    }

    public String value()
    {
        return locatorData.value();
    }

    public By by()
    {
        return by;
    }

    public Element element()
    {
        return element;
    }

    public boolean supports(final Locator other)
    {
        return mapper.supports(this, other);
    }

    public WebDriverLocator combine(final Locator toNest)
    {
        return new DefaultWebDriverLocator(mapper.combine(this, toNest), context);
    }

}
