package com.atlassian.jira.pageobjects.project.summary;

import com.atlassian.pageobjects.PageBinder;

import javax.inject.Inject;

/**
 * @since v4.4
 */
public class AbstractSummaryPanel implements SummaryPanel
{
    @Inject
    private PageBinder binder;

    private String project;
    private long projectId;

    @Override
    public void setProject(String projectKey, long projectId)
    {
        this.project = projectKey;
        this.projectId = projectId;
    }

    protected String getProjectKey()
    {
        return project;
    }

    protected long getProjectId()
    {
        return projectId;
    }

    protected PageBinder getBinder()
    {
        return binder;
    }
}
