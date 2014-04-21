package com.atlassian.jira.webtest.framework.impl.selenium.core.component;

import com.atlassian.jira.webtest.framework.core.component.Option;

/**
 * Utils for {@link com.atlassian.jira.webtest.framework.core.component.Option}s in the Selenium World&trade;
 *
 * @since v4.3
 */
public final class SeleniumOptions
{
    private SeleniumOptions()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static String create(Option option)
    {
        if (option.value() != null)
        {
            return createValue(option);
        }
        if (option.id() != null)
        {
            return createId(option);
        }
        if (option.label() != null)
        {
            return createLabel(option);
        }
        throw new IllegalArgumentException("Invalid option - no property set");
    }

    public static String createId(Option option)
    {
        return "id=" + option.id();
    }

    public static String createValue(Option option)
    {
        return "value=" + option.value();
    }

    public static String createLabel(Option option)
    {
        return "label=" + option.label();
    }
}
