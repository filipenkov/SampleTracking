package com.atlassian.jira.jql.context;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * @since v4.0
 */
public class ProjectIssueTypeContextImpl implements ProjectIssueTypeContext
{
    private final static ProjectIssueTypeContext INSTANCE = new ProjectIssueTypeContextImpl(AllProjectsContext.INSTANCE, AllIssueTypesContext.INSTANCE);

    private final ProjectContext projectContext;
    private final IssueTypeContext issueTypeContext;

    public static ProjectIssueTypeContext createGlobalContext()
    {
        return INSTANCE;
    }

    public ProjectIssueTypeContextImpl(final ProjectContext projectContext, final IssueTypeContext issueTypeContext)
    {
        this.projectContext = notNull("projectContext", projectContext);
        this.issueTypeContext = notNull("issueTypeContext", issueTypeContext);
    }

    public ProjectContext getProjectContext()
    {
        return projectContext;
    }

    public IssueTypeContext getIssueTypeContext()
    {
        return issueTypeContext;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final ProjectIssueTypeContextImpl that = (ProjectIssueTypeContextImpl) o;

        if (!issueTypeContext.equals(that.issueTypeContext))
        {
            return false;
        }
        if (!projectContext.equals(that.projectContext))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = projectContext.hashCode();
        result = 31 * result + issueTypeContext.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).
                append("projectContext", projectContext).
                append("issueTypeContext", issueTypeContext).
                toString();
    }
}
