package com.atlassian.labs.jira4compat.impl;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanel;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptorImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.labs.jira4compat.api.CompatIssueTabPanel;
import com.atlassian.labs.jira4compat.spi.CompatIssueTabPanelFactory;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;

import java.util.List;

/**
 *
 */
public class Jira5CompatIssueTabPanelFactory implements CompatIssueTabPanelFactory
{
    public Object convert(CompatIssueTabPanel panel)
    {
        return new AdaptingIssueTabPanel(panel);
    }

    public ModuleDescriptor createIssueTabPanelModuleDescriptor(JiraAuthenticationContext context, ModuleFactory moduleFactory)
    {
        return new IssueTabPanelModuleDescriptorImpl(context, moduleFactory);
    }

    // This needs to be public for GhettoInitter to work.
    public class AdaptingIssueTabPanel implements IssueTabPanel
    {
        private final CompatIssueTabPanel delegate;

        public AdaptingIssueTabPanel(CompatIssueTabPanel panel)
        {
            this.delegate = panel;
        }

        public void init(IssueTabPanelModuleDescriptor descriptor)
        {
            delegate.init(descriptor);
        }

        public List getActions(Issue issue, User remoteUser)
        {
            return delegate.getActions(issue, remoteUser);
        }

        public boolean showPanel(Issue issue, User remoteUser)
        {
            return delegate.showPanel(issue, remoteUser);
        }
    }
}
