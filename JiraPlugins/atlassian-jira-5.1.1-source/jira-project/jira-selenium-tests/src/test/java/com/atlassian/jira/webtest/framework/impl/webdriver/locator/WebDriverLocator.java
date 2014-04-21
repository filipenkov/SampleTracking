package com.atlassian.jira.webtest.framework.impl.webdriver.locator;

import com.atlassian.jira.webtest.framework.core.locator.Locator;
import org.openqa.selenium.By;

/**
 * WebDriver locator.
 *
 * @since v4.3
 */
public interface WebDriverLocator extends Locator
{
    /**
     * Returns WebDriver-compatible {@link org.openqa.selenium.By} locator.
     *
     * @return WebDriver locator class instance corresponding to this locator
     * @see org.openqa.selenium.By
     */
    By by();

    /**
     * A version of {@link com.atlassian.jira.webtest.framework.core.locator.Locator#combine(com.atlassian.jira.webtest.framework.core.locator.Locator)}
     * with covariant return type for easier usage in the WebDriver implementation of the framework.
     *
     * @param locator locator to nest
     * @return new locator resulting from nesting
     *
     * @see com.atlassian.jira.webtest.framework.core.locator.Locator#combine(com.atlassian.jira.webtest.framework.core.locator.Locator)
     */
    WebDriverLocator combine(Locator locator);
}
