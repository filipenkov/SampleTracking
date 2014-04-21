package com.atlassian.jira.workflow.migration;

import com.atlassian.jira.task.TaskContext;
import com.atlassian.jira.util.dbc.Assertions;

/**
 * The context used by enterprise workflow migration.  Only unique within project via .equals()
 *
 * @since v3.13
 */
public class EnterpriseWorkflowTaskContext implements TaskContext
{
    private final Long projectId;
    private final Long schemeId;

    public EnterpriseWorkflowTaskContext(final Long projectId, final Long schemeId)
    {
        Assertions.notNull("projectId", projectId);
        this.schemeId = schemeId;
        this.projectId = projectId;
    }

    public String buildProgressURL(final Long taskId)
    {
        String url = "/secure/project/SelectProjectWorkflowSchemeStep3.jspa?projectId=" + getProjectId() + "&taskId=" + taskId;
        if (getSchemeId() != null)
        {
            url = url + "&schemeId=" + getSchemeId();
        }
        return url;
    }

    public Long getProjectId()
    {
        return projectId;
    }

    public Long getSchemeId()
    {
        return schemeId;
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final EnterpriseWorkflowTaskContext that = (EnterpriseWorkflowTaskContext) o;

        return projectId.equals(that.projectId);
    }

    public int hashCode()
    {
        return projectId.hashCode();
    }
}
