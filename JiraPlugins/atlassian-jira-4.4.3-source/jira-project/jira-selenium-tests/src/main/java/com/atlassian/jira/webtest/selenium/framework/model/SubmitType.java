package com.atlassian.jira.webtest.selenium.framework.model;

import com.atlassian.selenium.SeleniumClient;

/**
 * Possible types of submitting page objects.
 *
 * @since 4.2
 */
public enum SubmitType
{
    BY_CLICK
    {
        @Override
        public void execute(final SeleniumClient client, final String locator)
        {
            client.click(locator);
        }
    },
    BY_SHORTCUT
    {
        @Override
        public void execute(final SeleniumClient client, final String locator)
        {
            throw new UnsupportedOperationException("Must be implemented: Alt+S in body");
        }
    };

    public void execute(SeleniumClient client, String locator)
    {
        throw new AbstractMethodError();
    }
}
