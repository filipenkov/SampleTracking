package com.atlassian.jira.plugin.versionpanel;

import com.atlassian.jira.plugin.AbstractTabPanelModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;

/**
 * A project version tab panel plugin adds extra panel tabs to JIRA's Browse Version page.
 *
 * @since v3.10
 */
public class VersionTabPanelModuleDescriptor extends AbstractTabPanelModuleDescriptor<VersionTabPanel>
{
    public VersionTabPanelModuleDescriptor(JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    /**
     * Asserts that module class implements {@link VersionTabPanel}
     *
     * @throws PluginParseException if {@link VersionTabPanel} class is not assignable from module class
     */
    protected void assertModuleClass() throws PluginParseException
    {
        assertModuleClassImplements(VersionTabPanel.class);
    }

    public int compareTo(Object o)
    {
        if (o instanceof VersionTabPanelModuleDescriptor)
        {
            VersionTabPanelModuleDescriptor descriptor = (VersionTabPanelModuleDescriptor) o;
            if (order == descriptor.order)
            {
                return 0;
            }
            else if (order > 0 && order < descriptor.order)
            {
                return -1;
            }
            return 1;
        }
        return -1;
    }

}
