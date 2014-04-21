package com.atlassian.jira.webtest.selenium.framework.pages;

/**
 * A {@link com.atlassian.jira.webtest.selenium.framework.pages.Page} that is globally accessible
 * within JIRA.
 *
 * @since v4.2
 */
public interface GlobalPage<T extends Page>
{
    /**
     * Go to itself.
     *
     * @return this global page instance.
     */
    T goTo();
}
