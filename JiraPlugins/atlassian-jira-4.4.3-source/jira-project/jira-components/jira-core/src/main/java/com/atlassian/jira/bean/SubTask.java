package com.atlassian.jira.bean;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import org.ofbiz.core.entity.GenericValue;

public class SubTask extends Object
{
    private Long sequence;
    private GenericValue subTaskIssue;
    private GenericValue parentIssue;

    public SubTask(Long sequence, GenericValue subTaskIssue, GenericValue parentIssue)
    {
        this.sequence = sequence;
        this.subTaskIssue = subTaskIssue;
        this.parentIssue = parentIssue;
    }

    public Long getSequence()
    {
        return sequence;
    }

    public Long getDisplaySequence()
    {
        // Add one as in display the sequences should start with 1 and not 0
        return getSequence() + 1;
    }

    public Issue getSubTaskIssueObject()
    {
        return ComponentAccessor.getIssueFactory().getIssue(subTaskIssue);
    }

    /**
     * @deprecated Use {@link #getSubTaskIssueObject()} instead.
     */
    public GenericValue getSubTaskIssue()
    {
        return subTaskIssue;
    }

    public GenericValue getParentIssue()
    {
        return parentIssue;
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof SubTask)) return false;

        final SubTask subTask = (SubTask) o;

        if (parentIssue != null ? !parentIssue.equals(subTask.parentIssue) : subTask.parentIssue != null) return false;
        if (sequence != null ? !sequence.equals(subTask.sequence) : subTask.sequence != null) return false;
        if (subTaskIssue != null ? !subTaskIssue.equals(subTask.subTaskIssue) : subTask.subTaskIssue != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (sequence != null ? sequence.hashCode() : 0);
        result = 29 * result + (subTaskIssue != null ? subTaskIssue.hashCode() : 0);
        result = 29 * result + (parentIssue != null ? parentIssue.hashCode() : 0);
        return result;
    }
}
