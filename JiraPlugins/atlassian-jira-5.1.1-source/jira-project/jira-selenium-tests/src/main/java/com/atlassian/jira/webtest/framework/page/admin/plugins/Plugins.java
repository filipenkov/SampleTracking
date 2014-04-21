package com.atlassian.jira.webtest.framework.page.admin.plugins;

import com.atlassian.jira.webtest.framework.page.admin.AdminPage;

/**
 * Represents the Universal Plugin Manager page in the JIRA Administration.
 *
 * @since v4.3
 */
public interface Plugins extends AdminPage
{

    /**
     * Perform series of operations leading to enabling of given system plugin on the page.
     *
     * @param pluginKey plugin key
     * @return this page instance
     */
    Plugins enableSystemPlugin(String pluginKey);


    /**
     * Perform series of operations leading to disabling of given system plugin on the page.
     *
     * @param pluginKey plugin key
     * @return this page instance
     */
    Plugins disableSystemPlugin(String pluginKey);

    /**
     * Open given tab.
     *
     * @param tabClass class of the tab page object
     * @param <T> type of the tab
     * @return tab instance
     */
    public <T extends PluginsTab<T>> T openTab(Class<T> tabClass);


    /**
     * Retrieve given plugin tab without performing any operation on it
     *
     * @param tabClass class of the tab page object
     * @param <T> type of the tab
     * @return tab instance
     */
    public <T extends PluginsTab<T>> T pluginTab(Class<T> tabClass);

}
