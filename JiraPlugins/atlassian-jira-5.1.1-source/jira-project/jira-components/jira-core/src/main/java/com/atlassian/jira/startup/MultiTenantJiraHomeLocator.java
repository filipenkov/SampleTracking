package com.atlassian.jira.startup;

import com.atlassian.multitenant.MultiTenantContext;

/**
 * @since v4.3
 */
public class MultiTenantJiraHomeLocator implements JiraHomePathLocator
{
    @Override
    public String getJiraHome()
    {
        if (MultiTenantContext.isEnabled())
        {
            return MultiTenantContext.getTenantReference().get().getHomeDir();
        }
        else
        {
            return null;
        }
    }

    @Override
    public String getDisplayName()
    {
        return "Multi-Tenant JIRA Home Path Locator";
    }
}
