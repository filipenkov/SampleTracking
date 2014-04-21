package com.atlassian.jira.webtest.framework.core.context;

/**
 * Enumeration of browser types.
 *
 * @since v4.3
 */
public enum BrowserType
{
    FIREFOX("firefox"),
    OPERA("opera"),
    SAFARI("safari"),
    UNKNOWN("unkown"),
    IE("ie"),
    CHROME("chrome");

    private final String name;

    BrowserType(String name)
    {
        this.name = name;
    }

    public String browserName()
    {
        return name;
    }

    public static BrowserType forName(String browserName)
    {
        for (BrowserType browser : values())
        {
            if(browserName.equals(browser.browserName()))
            {
                return browser;
            }
        }
        throw new IllegalArgumentException("No browser named <" + browserName + ">");
    }

}
