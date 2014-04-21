package com.atlassian.jira.webtest.selenium;

/**
 * Listener that can be used to observe the execution of selenium tests.
 *
 * @since v4.2
 */
public interface JiraSeleniumTestListener
{
    void startRestore(JiraSeleniumTest test, String file);
}
