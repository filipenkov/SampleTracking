package com.atlassian.jira.issue.context;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.ComponentManager.getComponentInstanceOfType;

public class IssueContextImpl implements IssueContext, Comparable
{
    private Long projectId;
    private String issueTypeId;

    /**
     * Create a context.
     * @param projectId Project ID, or null if field is not scoped by project.
     * @param issueTypeId IssueType ID, or null if field is not scoped by issue type.
     */
    public IssueContextImpl(Long projectId, String issueTypeId)
    {
        this.projectId = projectId;
        this.issueTypeId = issueTypeId;
    }

    /**
     * Create a context.
     * @param project Eg. retrieved from {@link com.atlassian.jira.project.ProjectManager#getProjectByKey(String)},
     * or null if field is not scoped by project.
     * @param issueType eg. retrieved from {@link com.atlassian.jira.config.ConstantsManager#getIssueType(String)},
     * or null if field is not scoped by issue type.
     * @deprecated since 4.0 use {@link #IssueContextImpl(Long, String)}
     */
    public IssueContextImpl(GenericValue project, GenericValue issueType)
    {
        this.projectId = project != null ? project.getLong("id") : null;
        this.issueTypeId = issueType != null ? issueType.getString("id") : null;
    }

    public Project getProjectObject()
    {
        return getProjectManager().getProjectObj(projectId);
    }

    public GenericValue getProject()
    {
        return getProjectManager().getProject(projectId);
    }

    private ProjectManager getProjectManager()
    {
        return getComponentInstanceOfType(ProjectManager.class);
    }

    public GenericValue getIssueType()
    {
        return getConstantsManager().getIssueType(issueTypeId);
    }

    public IssueType getIssueTypeObject()
    {
        return  getConstantsManager().getIssueTypeObject(issueTypeId);
    }

    ConstantsManager getConstantsManager()
    {
        return getComponentInstanceOfType(ConstantsManager.class);
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final IssueContextImpl that = (IssueContextImpl) o;

        if (issueTypeId != null ? !issueTypeId.equals(that.issueTypeId) : that.issueTypeId != null)
        {
            return false;
        }
        if (projectId != null ? !projectId.equals(that.projectId) : that.projectId != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (projectId != null ? projectId.hashCode() : 0);
        result = 29 * result + (issueTypeId != null ? issueTypeId.hashCode() : 0);
        return result;
    }

    public int compareTo(Object obj)
    {
        IssueContextImpl o = (IssueContextImpl) obj;
        return new CompareToBuilder()
                .append(projectId, o.projectId)
                .append(issueTypeId, o.issueTypeId)
                .toComparison();
    }
}
