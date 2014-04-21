package com.atlassian.jira.issue.context;

import com.atlassian.jira.issue.comparator.OfBizComparators;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

public abstract class AbstractJiraContext implements JiraContextNode
{
    private static final Logger log = Logger.getLogger(ProjectContext.class);

    public boolean isInContext(final IssueContext issueContext)
    {
        boolean matches = true;

        if (issueContext != null)
        {
            if (getProject() != null)
            {
                matches = getProject().equals(issueContext.getProject());
            }

            if (getIssueType() != null)
            {
                matches = getIssueType().equals(issueContext.getIssueType());
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
        final StringBuffer sb = new StringBuffer();

        final GenericValue projectGV = getProject();
        if (projectGV != null)
        {
            sb.append("Project: ");
            sb.append(projectGV.getString("name"));
        }

        final GenericValue issueTypeGV = getIssueType();
        if (issueTypeGV != null)
        {
            if (sb.length() > 0)
            {
                sb.append(", ");
            }
            sb.append("Issue type: ");
            sb.append(issueTypeGV.getString("name"));
        }
        return sb.length() > 0 ? sb.toString() : GlobalIssueContext.GLOBAL_CONTEXT_STR;
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
