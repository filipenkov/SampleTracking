package com.atlassian.jira.startup;

import com.atlassian.jira.config.util.AbstractJiraHome;
import com.atlassian.jira.util.NotNull;

import java.io.File;

/**
 * @since v4.3
 */
public class SystemTenantJiraHomeLocator implements JiraHomePathLocator
{
    final CompositeJiraHomePathLocator pathLocator = new CompositeJiraHomePathLocator(
            new SystemPropertyJiraHomePathLocator(),
            new WebContextJiraHomePathLocator(),
            new ApplicationPropertiesJiraHomePathLocator());

    @Override
    public String getJiraHome()
    {
        return pathLocator.getJiraHome();
    }

    @Override
    public String getDisplayName()
    {
        return "System Tenant JIRA Home Path Locator";
    }

    public static class SystemJiraHome extends AbstractJiraHome
    {
        final JiraHomePathLocator locator = new CompositeJiraHomePathLocator(new SystemTenantJiraHomeLocator(), new SystemPropertyJiraHomePathLocator(), new ApplicationPropertiesJiraHomePathLocator());

        @NotNull
        @Override
        public File getHome()
        {
            String homePath = locator.getJiraHome();
            if (homePath == null)
            {
                // according to the contract we need to throw IllegalStateException here
                throw new IllegalStateException("No valid JIRA Home directory.");
            }
            return new File(homePath);
        }
    }    
}
