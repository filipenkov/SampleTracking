package com.atlassian.jira.servlet;

import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.ProjectManager;

public class MockQuickLinkServlet extends QuickLinkServlet
{
    private ProjectManager projectManager;
    private IssueManager issueManager;

    public MockQuickLinkServlet(ProjectManager projectManager, IssueManager issueManager)
    {
        super();
        this.projectManager = projectManager;
        this.issueManager = issueManager;
    }

    ProjectManager getProjectManager()
    {
        return projectManager;
    }

    IssueManager getIssueManager()
    {
        return issueManager;
    }
}
