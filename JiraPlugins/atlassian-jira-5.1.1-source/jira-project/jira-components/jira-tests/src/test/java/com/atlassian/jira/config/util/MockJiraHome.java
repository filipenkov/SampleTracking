package com.atlassian.jira.config.util;

import com.atlassian.jira.util.NotNull;

import java.io.File;

public class MockJiraHome extends AbstractJiraHome
{
    private String homePath;

    public MockJiraHome()
    {
        this("/jira_home/");
    }

    public MockJiraHome(final String homePath)
    {
        this.homePath = homePath;
    }


    @NotNull
    public File getHome()
    {
        return new File(homePath);
    }

    public void setHomePath(final String homePath)
    {
        this.homePath = homePath;
    }
}
