package com.atlassian.jira.webtest.framework.impl.webdriver.core;

import org.openqa.selenium.WebDriver;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implementation of the {@link WebDriverContext}.
 *
 * @since v4.3
 */
public class DefaultWebDriverContext implements WebDriverContext
{
    private final WebDriver webDriver;

    public DefaultWebDriverContext(final WebDriver webDriver)
    {
        this.webDriver = notNull("webDriver", webDriver);
    }

    public WebDriver engine()
    {
        return webDriver;
    }

    public long conditionInterval()
    {
        return 50L;
    }
}
