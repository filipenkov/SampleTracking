package com.atlassian.jira.issue.context;

import com.atlassian.annotations.Internal;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

@Internal
public abstract class AbstractJiraContext implements JiraContextNode
{
    private static final Logger log = Logger.getLogger(AbstractJiraContext.class);

    public boolean isInContext(final IssueContext issueContext)
    {
        boolean matches = true;

        if (issueContext != null)
        {
            if (getProject() != null)
            {
                matches = getProject().equals(issueContext.getProject());
            }

            if (getIssueTypeObject() != null)
            {
                matches = getIssueTypeObject().equals(issueContext.getIssueTypeObject());
            }
        }
        else
        {
            log.warn("The issue passed is null. Returning as a context match");
        }

        return matches;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();

        final Project project = getProjectObject();
        if (project != null)
        {
            sb.append("Project: ").append(project.getName());
        }

        final IssueType issueType = getIssueTypeObject();
        if (issueType != null)
        {
            if (sb.length() > 0) { sb.append(", "); }
            sb.append("Issue type: ").append(issueType.getName());
        }

        final GenericValue projectCategoryGV = getProjectCategory();
        if (projectCategoryGV != null)
        {
            if (sb.length() > 0) { sb.append(", "); }
            sb.append("Project category: ").append(projectCategoryGV.getString("name"));
        }

        String description = sb.length() > 0 ? sb.toString() : GlobalIssueContext.GLOBAL_CONTEXT_STR;
        return getClass().getSimpleName() + "[" + description + "]";
    }

    public int compareTo(final JiraContextNode o)
    {
        return new CompareToBuilder().append(getProject(), o.getProject(), OfBizComparators.NAME_COMPARATOR).toComparison();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (!(o instanceof JiraContextNode))
        {
            return false;
        }
        final JiraContextNode rhs = (JiraContextNode) o;
        return new EqualsBuilder().append(getProject(), rhs.getProject()).isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(77, 127).append(getProject()).toHashCode();
    }
}
