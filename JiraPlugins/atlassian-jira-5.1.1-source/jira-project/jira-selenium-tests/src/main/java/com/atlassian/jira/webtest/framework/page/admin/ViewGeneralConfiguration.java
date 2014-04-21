package com.atlassian.jira.webtest.framework.page.admin;

/**
 * Represents the 'Global configuration' administration page.
 *
 * @since v4.3
 */
public interface ViewGeneralConfiguration extends AdminPage
{
    // TODO readProperty(GeneralConfigurationProperty)

    /**
     * Go to the edit mode.
     *
     * @return edit global configuration page instance
     */
    EditGeneralConfiguration edit();
}
