package com.atlassian.jira.webtest.framework.impl.selenium.core;

import com.atlassian.jira.webtest.framework.core.context.BrowserType;
import com.atlassian.selenium.Browser;

/**
 * Browser mapping between framework API and atlassian-selenium API.
 *
 * @since v4.3
 */
public enum SeleniumBrowserType
{
    FIREFOX(BrowserType.FIREFOX, Browser.FIREFOX),
    OPERA(BrowserType.OPERA, Browser.OPERA),
    SAFARI(BrowserType.SAFARI, Browser.SAFARI),
    IE(BrowserType.IE, Browser.IE),
    CHROME(BrowserType.CHROME, Browser.CHROME),
    UNKNOWN(BrowserType.UNKNOWN, Browser.UNKNOWN);

    private final BrowserType apiType;
    private final Browser seleniumType;

    SeleniumBrowserType(BrowserType apiType, Browser seleniumType)
    {
        this.apiType = apiType;
        this.seleniumType = seleniumType;
    }

    public static SeleniumBrowserType forSeleniumType(Browser seleniumType)
    {
        for (SeleniumBrowserType browserType : values())
        {
            if (browserType.seleniumType == seleniumType)
            {
                return browserType;
            }
        }
        throw new IllegalArgumentException("No browser for selenium type <" + seleniumType + "> found");
    }

    public BrowserType apiType()
    {
        return apiType;
    }
}
