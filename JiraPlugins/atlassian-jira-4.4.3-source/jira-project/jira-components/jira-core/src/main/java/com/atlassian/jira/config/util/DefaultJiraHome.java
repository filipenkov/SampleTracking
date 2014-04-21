package com.atlassian.jira.config.util;

import com.atlassian.jira.startup.ApplicationPropertiesJiraHomePathLocator;
import com.atlassian.jira.startup.CompositeJiraHomePathLocator;
import com.atlassian.jira.startup.MultiTenantJiraHomeLocator;
import com.atlassian.jira.startup.SystemPropertyJiraHomePathLocator;

import java.io.File;

/**
 * Simple implementation of {@link JiraHome}.
*
* @since v4.1
*/
public final class DefaultJiraHome extends AbstractJiraHome
{
    public File getHome()
    {
        final CompositeJiraHomePathLocator locator = new CompositeJiraHomePathLocator(new MultiTenantJiraHomeLocator(), new SystemPropertyJiraHomePathLocator(), new ApplicationPropertiesJiraHomePathLocator());
        final String jiraHome = locator.getJiraHome();

        if (jiraHome == null)
        {
            throw new IllegalStateException("No valid JIRA Home directory.");
        }
        return new File(jiraHome);
    }
}
