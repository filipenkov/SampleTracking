package com.atlassian.jira.webtest.selenium.framework.core;

/**
 * Abstract generic interface of every Selenium page object.  
 *
 * @since v4.2
 * @deprecated use {@link com.atlassian.jira.webtest.framework.core.PageObject} instead.
 * See https://extranet.atlassian.com/display/JIRADEV/JIRA+Web+Test+Framework+Guide for details 
 */
@Deprecated
public interface PageObject
{
    /**
     * Assert that this object is loaded and ready to be exercised by Selenium.
     *
     * @param timeout timeout to wait
     */
    void assertReady(long timeout);
}
