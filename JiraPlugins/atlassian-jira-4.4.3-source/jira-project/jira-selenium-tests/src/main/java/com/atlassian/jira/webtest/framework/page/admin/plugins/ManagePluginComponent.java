package com.atlassian.jira.webtest.framework.page.admin.plugins;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;

/**
 * Represents a single plugins table row on the {@link ManageExistingPlugins} tab.
 *
 * @since v4.3
 */
public interface ManagePluginComponent extends PluginComponent<ManagePluginComponent>
{
    /**
     * Check if the plugin is enabled.
     *
     * @return timed condition checking if the plugin is enabled
     */
    public TimedCondition isEnabled();

    /**
     * Check if the plugin is disabled.
     *
     * @return timed condition checking if the plugin is disabled
     */
    public TimedCondition isDisabled();

    /**
     * Enable the plugin. This component has to be expanded to be able to perform this operation, as the 'Enable'
     * button is only visible in the expanded mode.
     *
     * @return this plugin component
     * @see #isExpanded()
     * @see #expand()
     */
    public ManagePluginComponent enable();

    /**
     * Disable the plugin. This component has to be expanded to be able to perform this operation, as the 'Disable'
     * button is only visible in the expanded mode.
     *
     * @return this plugin component
     * @see #isExpanded()
     * @see #expand()
     */
    public ManagePluginComponent disable();

    public TimedCondition isModuleListExpanded();

    public TimedCondition isModuleListCollapsed();

    /**
     * Expands or Collapses the modules section of a manage plugin component
     */
    public ManagePluginComponent toggleExpandModulesList();

    public PluginModulesList<ManagePluginModuleComponent> moduleList();    
}
