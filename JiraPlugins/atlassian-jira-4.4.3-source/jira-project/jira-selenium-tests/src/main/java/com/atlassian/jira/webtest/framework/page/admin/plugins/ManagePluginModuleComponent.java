package com.atlassian.jira.webtest.framework.page.admin.plugins;

import com.atlassian.jira.webtest.framework.core.condition.TimedCondition;

/**
 * Represents a single plugin module table row on the {@link com.atlassian.jira.webtest.framework.page.admin.plugins.ManageExistingPlugins} tab.
 *
 * @since v4.3
 */
public interface ManagePluginModuleComponent extends PluginModuleComponent<ManagePluginModuleComponent>
{
    /**
     * Check if the plugin is enabled.
     *
     * @return timed condition checking if the plugin module is enabled
     */
    public TimedCondition isEnabled();

    /**
     * Check if the plugin is disabled.
     *
     * @return timed condition checking if the plugin module is disabled
     */
    public TimedCondition isDisabled();

    /**
     * Check if the plugin module can be disabled.
     *
     * @return timed condition checking if the plugin module can be disabled
     */
    public TimedCondition canBeDisabled();

    /**
     * Check if the plugin module cannot be disabled.
     * @return timed condition checking if the plugin module cannot be disabled
     */
    public TimedCondition cannotBeDisabled();


    /**
     * Enable the plugin module. The plugin's module list has to be expanded to be able to perform this operation, as the 'Enable'
     * button is only visible in the expanded mode.
     *
     * @return this plugin module component
     * @see #isExpanded()
     * @see #expand()
     */
    public ManagePluginModuleComponent enable();

    /**
     * Disable the plugin module. This plugin's module list has to be expanded to be able to perform this operation, as the 'Disable'
     * button is only visible in the expanded mode.
     *
     * @return this plugin module component
     * @see #isExpanded()
     * @see #expand()
     */
    public ManagePluginModuleComponent disable();
}