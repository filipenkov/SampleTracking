package com.atlassian.jira.webtest.selenium.framework.model;

import com.atlassian.selenium.SeleniumClient;
import com.atlassian.selenium.keyboard.SeleniumTypeWriter;
import com.atlassian.webtest.ui.keys.SpecialKeys;
import com.atlassian.webtest.ui.keys.TypeMode;

/**
 * Types of possible cancel actions in Selenium.
 *
 * @since 4.2
 */
public enum CancelType
{
    BY_CLICK
    {
        @Override
        public void execute(final SeleniumClient client, final String locator)
        {
            client.click(locator);
        }
    },
    BY_ESCAPE
    {
        @Override
        public void execute(final SeleniumClient client, final String locator)
        {
            new SeleniumTypeWriter(client, "css=body", TypeMode.TYPE).type(SpecialKeys.ESC);
        }
    };

    public void execute(SeleniumClient client, String locator)
    {
        throw new AbstractMethodError();
    }
}
