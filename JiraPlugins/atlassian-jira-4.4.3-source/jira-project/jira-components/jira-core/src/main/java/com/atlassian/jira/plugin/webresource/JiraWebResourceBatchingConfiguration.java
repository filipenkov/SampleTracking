package com.atlassian.jira.plugin.webresource;

import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.plugin.webresource.ResourceBatchingConfiguration;
import com.atlassian.plugin.webresource.WebResourceFilter;

import java.util.ArrayList;
import java.util.List;


/**
 * Determins which resources are included superbatched on every page!
 *
 * @since v4.3
 */
public class JiraWebResourceBatchingConfiguration implements ResourceBatchingConfiguration
{
    private static final boolean BUNDLED_PLUGINS_DISABLED = JiraSystemProperties.isBundledPluginsDisabled();
    private static final List<String> resources = new ArrayList<String>();

    static
    {
        resources.add("com.atlassian.auiplugin:ajs");
        resources.add("jira.webresources:util-lite");
        resources.add("jira.webresources:util");
        resources.add("jira.webresources:inline-layer");
        resources.add("jira.webresources:content-retrievers");
        resources.add("jira.webresources:list");
        resources.add("jira.webresources:dropdown");
        resources.add("jira.webresources:issue-table");
        resources.add("jira.webresources:dropdown-select");
        resources.add("jira.webresources:select-pickers");
        resources.add("jira.webresources:dialogs");
        resources.add("jira.webresources:set-focus");
        resources.add("jira.webresources:jira-global");
        resources.add("jira.webresources:key-commands");
        resources.add("jira.webresources:header");
    }


    @Override
    public boolean isSuperBatchingEnabled()
    {
        return !resources.isEmpty() && !BUNDLED_PLUGINS_DISABLED;
    }

    @Override
    public List<String> getSuperBatchModuleCompleteKeys()
    {
        return resources;
    }
}
