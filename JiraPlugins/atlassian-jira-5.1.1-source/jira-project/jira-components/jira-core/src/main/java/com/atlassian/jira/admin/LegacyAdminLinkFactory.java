package com.atlassian.jira.admin;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.webfragment.SimpleLinkFactory;
import com.atlassian.jira.plugin.webfragment.SimpleLinkManager;
import com.atlassian.jira.plugin.webfragment.descriptors.SimpleLinkFactoryModuleDescriptor;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.plugin.webfragment.model.SimpleLink;
import com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection;
import webwork.action.ServletActionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Returns links for all legacy plugins that still define web-items in the system.admin section section.
 *
 * @since v4.4
 */
public class LegacyAdminLinkFactory implements SimpleLinkFactory
{
    private static final String SYSTEM_ADMIN_LOCATION = "system.admin";
    
    private final SimpleLinkManager simpleLinkManager;

    public LegacyAdminLinkFactory(final SimpleLinkManager simpleLinkManager)
    {
        this.simpleLinkManager = simpleLinkManager;
    }

    @Override
    public void init(SimpleLinkFactoryModuleDescriptor descriptor)
    {
    }

    @Override
    public List<SimpleLink> getLinks(User user, Map<String, Object> params)
    {
        final JiraHelper helper = new JiraHelper(ServletActionContext.getRequest());
        final List<SimpleLink> ret = new ArrayList<SimpleLink>();

        final List<SimpleLinkSection> sections = simpleLinkManager.getSectionsForLocation(SYSTEM_ADMIN_LOCATION, user, helper);
        for (SimpleLinkSection section : sections)
        {
            ret.addAll(simpleLinkManager.getLinksForSection(SYSTEM_ADMIN_LOCATION + "/" + section.getId(), user, helper));
        }
        return ret;
    }
}
