package com.atlassian.jira.webtest.selenium.framework.core;

/**
 * Page object that can be localized on a page by means of a specific locator.
 *
 * @since v4.2
 */
public interface LocalizablePageObject
{

    /**
     * Locator of this page object.
     *
     * @return locator
     */
    String locator();
}
