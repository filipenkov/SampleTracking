package com.atlassian.jira.webtest.framework.impl.webdriver.core;

import org.openqa.selenium.WebDriver;

import static com.atlassian.jira.util.dbc.Assertions.notNull;


/**
 * Abstract base class for all classes in the WebDriver test framework implementations
 * that are aware of and make use of the test's {@link WebDriverContext}. 
 *
 * @since v4.3
 */
public abstract class WebDriverContextAware
{
    protected final WebDriverContext context;
    protected final WebDriver engine;

    public WebDriverContextAware(final WebDriverContext context)
    {
        this.context = notNull("context", context);
        this.engine = notNull("engine", context.engine());
    }


}
