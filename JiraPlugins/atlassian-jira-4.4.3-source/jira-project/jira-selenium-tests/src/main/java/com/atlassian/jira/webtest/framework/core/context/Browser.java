package com.atlassian.jira.webtest.framework.core.context;

/**
 * A particular version of browser used in the test.
 *
 * @since v4.3
 */
public interface Browser
{
    /**
     * Browser type.
     *
     * @return browser type
     */
    BrowserType type();

    /**
     * Browser version.
     *
     * @return browser version
     */
    String version();
}
