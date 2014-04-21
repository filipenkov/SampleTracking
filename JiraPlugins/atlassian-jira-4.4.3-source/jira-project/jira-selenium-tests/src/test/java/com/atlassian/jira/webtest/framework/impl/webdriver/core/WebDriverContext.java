package com.atlassian.jira.webtest.framework.impl.webdriver.core;

import org.openqa.selenium.WebDriver;

/**
 * WebDriver test context.
 *
 * @since v4.3
 */
public interface WebDriverContext
{

    /**
     * Get WebDriver engine of the current test context.
     *
     * @return WebDriver engine implementation
     *
     * @see org.openqa.selenium.WebDriver
     */
    WebDriver engine();


    /**
     * TODO move to dedicated timeouts class? possibly move to core framework as a Timeout interface
     *
     * @return
     */
    long conditionInterval();
}
