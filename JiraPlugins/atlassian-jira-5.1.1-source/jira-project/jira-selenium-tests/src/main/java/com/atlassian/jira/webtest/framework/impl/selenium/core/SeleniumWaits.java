package com.atlassian.jira.webtest.framework.impl.selenium.core;

import com.atlassian.jira.webtest.framework.core.Timeouts;

import static com.atlassian.jira.webtest.framework.core.Timeouts.PAGE_LOAD;

/**
 * Convenience method for performing waits.
 *
 * @since v4.3
 */
public final class SeleniumWaits extends SeleniumContextAware
{

    SeleniumWaits(SeleniumContext context)
    {
        super(context);
    }

    /**
     * Wait for page load with default page load timeout.
     *
     * @see com.atlassian.jira.webtest.framework.core.Timeouts#PAGE_LOAD
     * @see com.thoughtworks.selenium.Selenium#waitForPageToLoad(String)
     */
    public void pageLoad()
    {
        client.waitForPageToLoad(context.timeoutFor(PAGE_LOAD));
    }

    /**
     * Wait for page load with default slow page load timeout.
     *
     * @see com.atlassian.jira.webtest.framework.core.Timeouts#SLOW_PAGE_LOAD
     * @see com.thoughtworks.selenium.Selenium#waitForPageToLoad(String)
     */
    public void slowPageLoad()
    {
        client.waitForPageToLoad();
    }

    /**
     * Wait for page load with custom timeout.
     *
     * @param timeout timeout
     * @see com.atlassian.jira.webtest.framework.core.Timeouts
     * @see com.thoughtworks.selenium.Selenium#waitForPageToLoad(String)
     */
    public void pageLoad(Timeouts timeout)
    {
        pageLoad(context.timeoutFor(timeout));
    }

    /**
     * Wait for page load with custom timeout.
     *
     * @param timeout timeout
     * @see com.thoughtworks.selenium.Selenium#waitForPageToLoad(String)
     */
    public void pageLoad(long timeout)
    {
        client.waitForPageToLoad(timeout);
    }

    /**
     * Wait for popup with default timeout (AJAX call)
     *
     * @param windowId ID of the popup window
     * @see Timeouts#AJAX_ACTION
     * @see com.thoughtworks.selenium.Selenium#waitForPopUp(String, String) 
     */
    public void popup(String windowId)
    {
        popup(windowId, Timeouts.AJAX_ACTION);
    }

    /**
     * Wait for popup with custom timeout.
     *
     * @param windowId ID of the popup window
     * @param timeout custom timeout
     * @see Timeouts
     * @see com.thoughtworks.selenium.Selenium#waitForPopUp(String, String)
     */
    public void popup(String windowId, Timeouts timeout)
    {
        client.waitForPopUp(windowId, timeoutAsString(timeout));
    }

    private String timeoutAsString(Timeouts timeout)
    {
        return timeoutAsString(context.timeoutFor(timeout));
    }

    private String timeoutAsString(long timeout)
    {
        return Long.toString(timeout);
    }
}
