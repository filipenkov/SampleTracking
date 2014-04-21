package com.atlassian.jira.webtest.selenium.framework.model;

import static com.atlassian.jira.webtest.selenium.framework.model.Locators.CSS;

/**
 * Enumeration of the application properties exposed in the JIRA UI.
 *
 * @since 4.2
 */
public enum ApplicationProperty
{

    ENCODING(CSS.create("input[name='encoding']"));

    private final String globalConfigEditLocator;


    ApplicationProperty(final String globalConfigEditLocator)
    {
        this.globalConfigEditLocator = globalConfigEditLocator;
    }

    /**
     * Locator of the input on the Edit Global Configuration page.
     *
     * @return locator
     */
    public String editConfigurationLocator()
    {
        return globalConfigEditLocator;
    }
}
