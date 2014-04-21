package com.atlassian.jira.plugin.componentpanel;

import com.atlassian.jira.plugin.AbstractTabPanelModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;

/**
 * A project component tab panel plugin adds extra panel tabs to JIRA's Browse Component page.
 *
 * @since v3.10
 */
public class ComponentTabPanelModuleDescriptor extends AbstractTabPanelModuleDescriptor<ComponentTabPanel>
{
    public ComponentTabPanelModuleDescriptor(JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    /**
     * Asserts that module class implements {@link ComponentTabPanel}
     *
     * @throws com.atlassian.plugin.PluginParseException if {@link ComponentTabPanel} class is not assignable from module class
     */
    protected void assertModuleClass() throws PluginParseException
    {
        assertModuleClassImplements(ComponentTabPanel.class);
    }

    public int compareTo(Object o)
    {
        if (o instanceof ComponentTabPanelModuleDescriptor)
        {
            ComponentTabPanelModuleDescriptor descriptor = (ComponentTabPanelModuleDescriptor) o;
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
