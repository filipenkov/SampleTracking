package com.atlassian.jira.projectconfig.util;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.projectconfig.contextproviders.ContextProviderUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Simple implementation of the TabUrlFactory.
 *
 * @since v4.4
 */
public class DefaultTabUrlFactory implements TabUrlFactory
{
    private final ContextProviderUtils providerUtils;
    private final UrlEncoder encoder;
    
    public DefaultTabUrlFactory(ContextProviderUtils providerUtils, UrlEncoder encoder)
    {
        this.providerUtils = providerUtils;
        this.encoder = encoder;
    }

    public String forSummary()
    {
        return forTab(null);
    }

    public String forComponents()
    {
        return forTab("components");
    }

    public String forVersions()
    {
        return forTab("versions");
    }

    public String forIssueSecurity()
    {
        return forTab("issuesecurity");
    }

    public String forPermissions()
    {
        return forTab("permissions");
    }

    @Override
    public String forWorkflows()
    {
        return forTab("workflows");
    }

    @Override
    public String forFields()
    {
        return forTab("fields");
    }

    @Override
    public String forScreens()
    {
        return forTab("screens");
    }

    public String forNotifications()
    {
        return forTab("notifications");
    }

    public String forIssueTypes()
    {
        return forTab("issuetypes");
    }

    private String forTab(String tab)
    {
        final Project project = providerUtils.getProject();
        final StringBuilder builder = new StringBuilder(StringUtils.stripToEmpty(providerUtils.getBaseUrl()));
        if (builder.length() == 0 || builder.charAt(0) != '/')
        {
            builder.insert(0, '/');
        }
        appendPath(builder, "plugins/servlet/project-config/");
        appendPath(builder, encoder.encode(project.getKey()));

        if (tab != null)
        {
            appendPath(builder, encoder.encode(tab));
        }

        return builder.toString();
    }

    private void appendPath(StringBuilder url, String path)
    {
        if (url.charAt(url.length() - 1) != '/')
        {
            url.append('/');
        }
        url.append(path);
    }
}
