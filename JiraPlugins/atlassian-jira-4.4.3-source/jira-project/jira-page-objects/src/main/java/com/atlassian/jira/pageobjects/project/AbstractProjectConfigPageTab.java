package com.atlassian.jira.pageobjects.project;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;

/**
 * @since v4.4
 */
public abstract class AbstractProjectConfigPageTab extends AbstractJiraPage implements ProjectConfigPageTab
{
    @ElementBy(id = "project-config-actions")
    private PageElement operations;

    protected ProjectInfoLocator projectInfoLocator;

    @Init
    public void init()
    {
        projectInfoLocator = pageBinder.bind(ProjectInfoLocator.class);
    }

    @Override
    public String getProjectKey()
    {
        return projectInfoLocator.getProjectKey();
    }

    @Override
    public long getProjectId()
    {
        return projectInfoLocator.getProjectId();
    }

    @Override
    public ProjectConfigTabs getTabs()
    {
        return pageBinder.bind(ProjectConfigTabs.class);
    }

    public ProjectConfigActions openOperations()
    {
        operations.click();
        return pageBinder.bind(ProjectConfigActions.class);
    }

    @Override
    public ProjectConfigHeader getProjectHeader()
    {
        return pageBinder.bind(ProjectConfigHeader.class);
    }
}
