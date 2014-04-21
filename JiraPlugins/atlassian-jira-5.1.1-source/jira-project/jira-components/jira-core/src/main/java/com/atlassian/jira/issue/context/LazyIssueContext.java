package com.atlassian.jira.issue.context;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import org.ofbiz.core.entity.GenericValue;

public class LazyIssueContext implements IssueContext
{
    private Long projectId;
    private String issueTypeId;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();
    private final ProjectManager projectManager = ComponentAccessor.getProjectManager();

    // ---------------------------------------------------------------------------------------------------- Constructors
    public LazyIssueContext(Long projectId, String issueTypeId)
    {
        this.projectId = projectId;
        this.issueTypeId = issueTypeId;
    }

    public Project getProjectObject()
    {
        return projectManager.getProjectObj(projectId);
    }

    public GenericValue getProject()
    {
        return projectManager.getProject(projectId);
    }

    public IssueType getIssueTypeObject()
    {
        return constantsManager.getIssueTypeObject(issueTypeId);
    }

    public GenericValue getIssueType()
    {
        return constantsManager.getIssueType(issueTypeId);
    }
}
