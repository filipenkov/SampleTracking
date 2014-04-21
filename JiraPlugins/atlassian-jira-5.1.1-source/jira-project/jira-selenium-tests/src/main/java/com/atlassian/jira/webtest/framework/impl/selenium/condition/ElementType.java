package com.atlassian.jira.webtest.framework.impl.selenium.condition;

import com.atlassian.selenium.SeleniumClient;

/**
 * Represents the value type of the target element. Different methods of retrieving value
 * will are used for different value types.
 *
 */
public enum ElementType
{
    /**
     * An input element.
     */
    INPUT
            {
                @Override
                String retrieve(final String locator, final SeleniumClient client)
                {
                    return client.getValue(locator);
                }
            },

    /**
     * A read-only text output
     *
     */
    TEXT
            {
                @Override
                String retrieve(final String locator, final SeleniumClient client)
                {
                    return client.getText(locator);
                }
            };

    String retrieve(String locator, SeleniumClient client)
    {
        throw new AbstractMethodError();
    }
}
