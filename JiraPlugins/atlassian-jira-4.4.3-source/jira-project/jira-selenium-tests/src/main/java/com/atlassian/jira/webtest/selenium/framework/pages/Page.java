package com.atlassian.jira.webtest.selenium.framework.pages;

import com.atlassian.jira.webtest.selenium.framework.core.PageObject;

/**
 * Common interface of all objects representing Selenium JIRA pages.
 *
 * @since v4.2
 * @deprecated use {@link com.atlassian.jira.webtest.framework.page.Page} instead.
 * See https://extranet.atlassian.com/display/JIRADEV/JIRA+Web+Test+Framework+Guide for details
 */
@Deprecated
public interface Page extends PageObject
{
    /**
     * Check if the test driver is currently at this page
     *
     * @return <code>true</code>, if the test driver is currently at this page, <code>false<code> otherwise
     */
    boolean isAt();
}
