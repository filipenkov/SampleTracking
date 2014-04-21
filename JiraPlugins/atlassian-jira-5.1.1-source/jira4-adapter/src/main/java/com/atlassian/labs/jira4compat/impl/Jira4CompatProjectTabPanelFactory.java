package com.atlassian.labs.jira4compat.impl;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.labs.jira4compat.api.CompatProjectTabPanel;
import com.atlassian.labs.jira4compat.spi.CompatProjectTabPanelFactory;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

/**
 */
public class Jira4CompatProjectTabPanelFactory implements CompatProjectTabPanelFactory
{

    public Object convert(CompatProjectTabPanel panel)
    {
        return new AdaptingProjectTabPanel(panel);
    }

    public ModuleDescriptor createProjectTabPanelModuleDescriptor(JiraAuthenticationContext context, ModuleFactory moduleFactory)
    {
        return new ProjectTabPanelModuleDescriptor(context, moduleFactory);
    }

    // This needs to be public for the GhettoInitter to work.
    public static class AdaptingProjectTabPanel implements ProjectTabPanel
    {
        private final CompatProjectTabPanel delegate;

        public AdaptingProjectTabPanel(CompatProjectTabPanel panel)
        {
            this.delegate = panel;
        }

        public void init(ProjectTabPanelModuleDescriptor descriptor)
        {
            delegate.init(descriptor);
        }

        public String getHtml(BrowseContext ctx)
        {
            return delegate.getHtml(OsUserAdapter.build(ctx.getUser()), ctx.getProject());
        }

        public boolean showPanel(BrowseContext ctx)
        {
            return delegate.showPanel();
        }
    }
}
