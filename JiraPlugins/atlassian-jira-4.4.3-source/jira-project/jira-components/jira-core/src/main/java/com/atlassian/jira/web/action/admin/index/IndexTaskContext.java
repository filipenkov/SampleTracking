package com.atlassian.jira.web.action.admin.index;

import com.atlassian.jira.task.TaskContext;

/**
 * Context for the index operation. Only one index operation is allowed at one time.
 *
 * @since v3.13
 */
class IndexTaskContext implements TaskContext
{
    public String buildProgressURL(final Long taskId)
    {
        return "/secure/admin/jira/IndexProgress.jspa?taskId=" + taskId;
    }

    public boolean equals(final Object o)
    {
        return (o != null) && (getClass() == o.getClass());
    }

    public int hashCode()
    {
        return getClass().hashCode();
    }
}
