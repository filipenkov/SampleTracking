package com.atlassian.jira.webtest.selenium.harness.util;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.selenium.SeleniumClient;
import com.thoughtworks.selenium.Selenium;

/**
 * All util implementations should extend from this class to get access to the
 * seleniumclient and environment data.
 */
public abstract class AbstractSeleniumUtil
{

    protected static final String PAGE_LOAD_WAIT = "60000";

    /**
     * A member variable back to the Selenium interface
     */
    protected SeleniumClient selenium;

    protected JIRAEnvironmentData environmentData;


    public AbstractSeleniumUtil(SeleniumClient selenium, JIRAEnvironmentData environmentData)
    {
        this.selenium = selenium;
        this.environmentData = environmentData;
    }

    public Selenium getSelenium()
    {
        return selenium;
    }

    public JIRAEnvironmentData getEnvironmentData()
    {
        return environmentData;
    }
}
