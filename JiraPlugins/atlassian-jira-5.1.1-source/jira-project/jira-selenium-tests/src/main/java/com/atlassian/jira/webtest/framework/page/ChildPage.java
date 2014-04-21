package com.atlassian.jira.webtest.framework.page;


/**
 * A page that is accessed via a main, parent page and has a link to it. 
 *
 * @param <T> type of the parent page
 * @since v4.3
 */
public interface ChildPage<T extends Page> extends Page
{
    /**
     * Go back to the parent page.
     *
     * @return parent page instance
     */
    T back();
}
