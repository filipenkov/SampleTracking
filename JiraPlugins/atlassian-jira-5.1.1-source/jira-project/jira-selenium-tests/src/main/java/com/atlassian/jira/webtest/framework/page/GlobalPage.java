package com.atlassian.jira.webtest.framework.page;

/**
 * A {@link com.atlassian.jira.webtest.framework.page.Page} that is globally accessible
 * within JIRA.
 *
 * @since v4.2
 */
public interface GlobalPage<T extends GlobalPage<T>> extends Page
{
    /**
     * Go to itself.
     *
     * @return this global page instance.
     */
    T goTo();
}
