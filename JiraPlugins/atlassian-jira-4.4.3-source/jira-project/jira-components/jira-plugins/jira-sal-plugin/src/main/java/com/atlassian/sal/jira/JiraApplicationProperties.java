package com.atlassian.sal.jira;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.sal.api.ApplicationProperties;

import java.util.Date;
import java.io.File;

/**
 * JIRA implementation of WebProperties
 */
public class JiraApplicationProperties implements ApplicationProperties
{
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final JiraHome jiraHome;
    private final com.atlassian.jira.config.properties.ApplicationProperties jiraApplicationProperties;
    private final BuildUtilsInfo buildUtilsInfo;

    public JiraApplicationProperties(VelocityRequestContextFactory velocityRequestContextFactory, JiraHome jiraHome, final com.atlassian.jira.config.properties.ApplicationProperties jiraApplicationProperties, final BuildUtilsInfo buildUtilsInfo)
    {
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.jiraHome = jiraHome;
        this.jiraApplicationProperties = jiraApplicationProperties;
        this.buildUtilsInfo = buildUtilsInfo;
    }

    public String getBaseUrl()
    {
        return velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
    }

    public String getApplicationName()
    {
        return jiraApplicationProperties.getText(APKeys.JIRA_TITLE);
    }

    public String getDisplayName()
    {
        return "JIRA";
    }

    public String getVersion()
    {
        return buildUtilsInfo.getVersion();
    }

    public Date getBuildDate()
    {
        return buildUtilsInfo.getCurrentBuildDate();
    }

    public String getBuildNumber()
    {
        return buildUtilsInfo.getCurrentBuildNumber();
    }

    public File getHomeDirectory()
    {
        return jiraHome.getHome();
    }

    public String getPropertyValue(String key)
    {
        return jiraApplicationProperties.getDefaultBackedString(key);
    }
}
