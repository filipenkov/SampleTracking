package com.atlassian.jira.webtest.framework.page.admin.plugins;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public interface InstallPlugins extends PluginsTab<InstallPlugins>
{
    InstallPluginDialog openInstallPluginDialog();
    InstallPluginDialog installPluginDialog();
}