package com.atlassian.jira.functest.framework.model;

/**
 * Config property indicting JIRA operation mode: private or public.
 *
 * @since 5.1
 */
public enum JiraMode
{
    PUBLIC,
    PRIVATE;

    public static JiraMode fromValue(String optionValue)
    {
        for (JiraMode mode : values())
        {
            if (mode.optionValue().equals(optionValue))
            {
                return mode;
            }
        }
        throw new IllegalArgumentException("No mode with option value '" + optionValue + "'");
    }

    public static JiraMode forPublicModeEnabledValue(boolean publicModeEnabled)
    {
        return publicModeEnabled ? PUBLIC : PRIVATE;
    }


    public String optionValue()
    {
        return name().toLowerCase();
    }

}
