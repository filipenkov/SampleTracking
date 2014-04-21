package com.atlassian.labs.jira4compat.impl;

import com.atlassian.jira.plugin.profile.OptionalUserProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanel;
import com.atlassian.jira.plugin.profile.ViewProfilePanelModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.labs.jira4compat.api.CompatViewProfilePanel;
import com.atlassian.labs.jira4compat.spi.CompatViewProfilePanelFactory;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.opensymphony.user.User;

/**
 *
 */
public class Jira4CompatViewProfilePanelFactory implements CompatViewProfilePanelFactory
{
    public Object convert(final CompatViewProfilePanel panel)
    {
        return new AdaptingViewProfilePanel(panel);
    }

    public ModuleDescriptor createViewProfilePanelModuleDescriptor(JiraAuthenticationContext context, ModuleFactory moduleFactory)
    {
        return new ViewProfilePanelModuleDescriptor(context, moduleFactory);
    }

    public static class AdaptingViewProfilePanel implements ViewProfilePanel, OptionalUserProfilePanel
    {
        private final CompatViewProfilePanel delegate;

        public AdaptingViewProfilePanel(CompatViewProfilePanel delegate)
        {
            this.delegate = delegate;
        }

        public boolean showPanel(User user, User user1)
        {
            return delegate.showPanel(OsUserAdapter.build(user), OsUserAdapter.build(user1));
        }

        public void init(ViewProfilePanelModuleDescriptor viewProfilePanelModuleDescriptor)
        {
            delegate.init(viewProfilePanelModuleDescriptor);
        }

        public String getHtml(User user)
        {
            return delegate.getHtml(OsUserAdapter.build(user));
        }
    }
}
