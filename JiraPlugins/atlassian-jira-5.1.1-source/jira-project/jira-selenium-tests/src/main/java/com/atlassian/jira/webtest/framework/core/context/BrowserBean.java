package com.atlassian.jira.webtest.framework.core.context;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Simple bean implementation of {@link Browser}.
 *
 * @since v4.3
 */
public class BrowserBean implements Browser
{
    private final BrowserType browserType;
    private final String version;

    public BrowserBean(BrowserType browserType, String version)
    {
        this.browserType = notNull("browserType", browserType);
        this.version = notNull("version", version);
    }

    @Override
    public BrowserType type()
    {
        return browserType;
    }

    @Override
    public String version()
    {
        return version;
    }
}
