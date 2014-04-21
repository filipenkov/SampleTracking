package com.atlassian.jira.config;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bean.SubTaskBean;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Collections;

/**
 * @since v4.0
 */
public class MockSubTaskManager implements SubTaskManager
{
    private boolean subTasksEnabled = true;

    public void enableSubTasks() throws CreateException
    {
        subTasksEnabled = true;
    }

    public boolean isSubTasksEnabled()
    {
        return subTasksEnabled;
    }

    public void disableSubTasks()
    {
        subTasksEnabled = false;
    }

    public boolean isSubTask(final GenericValue issue)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public boolean isSubTaskIssueType(final GenericValue issueType)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public Long getParentIssueId(final GenericValue issue)
    {
        // simple implementation for now - make this clever if we need it.
        return null;
    }

    public GenericValue getParentIssue(final GenericValue issue)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public SubTaskBean getSubTaskBean(GenericValue issue, com.opensymphony.user.User remoteUser)
    {
        // Old User object
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public SubTaskBean getSubTaskBean(final GenericValue issue, final User remoteUser)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public void moveSubTask(final GenericValue issue, final Long currentSequence, final Long sequence)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public void resetSequences(final GenericValue issue)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public void resetSequences(final Issue issue)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public GenericValue createSubTaskIssueType(final String name, final Long sequence, final String description, final String iconurl)
            throws CreateException
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public void updateSubTaskIssueType(final String id, final String name, final Long sequence, final String description, final String iconurl)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public void removeSubTaskIssueType(final String name) throws RemoveException
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public Collection getSubTasksIssueTypes()
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public Collection<IssueType> getSubTaskIssueTypeObjects()
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public boolean issueTypeExistsById(final String id)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public boolean issueTypeExistsByName(final String name)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public void moveSubTaskIssueTypeUp(final String id)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public void moveSubTaskIssueTypeDown(final String id)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public GenericValue getSubTaskIssueTypeById(final String id)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public void createSubTaskIssueLink(GenericValue parentIssue, GenericValue subTaskIssue, com.opensymphony.user.User remoteUser)
            throws CreateException
    {
        // Old User object
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void createSubTaskIssueLink(final GenericValue parentIssue, final GenericValue subTaskIssue, final User remoteUser)
            throws CreateException
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    @Override
    public void createSubTaskIssueLink(Issue parentIssue, Issue subTaskIssue, com.opensymphony.user.User remoteUser)
            throws CreateException
    {
        // Old User object
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void createSubTaskIssueLink(final Issue parentIssue, final Issue subTaskIssue, final User remoteUser)
            throws CreateException
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public Collection getAllSubTaskIssueIds()
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public List getSubTaskIssueLinks(final Long issueId)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public Collection getSubTasks(final GenericValue issue)
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

    public Collection getSubTaskObjects(final Issue issue)
    {
        // Dumb implementation for no Issues with subtasks - fix this if/when required for tests.
        return Collections.emptyList();
    }

    @Override
    public IssueUpdateBean changeParent(GenericValue subTask, GenericValue parentIssue, com.opensymphony.user.User currentUser)
            throws RemoveException, CreateException
    {
        // Old User object
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public IssueUpdateBean changeParent(final GenericValue subTask, final GenericValue parentIssue, final User currentUser)
            throws RemoveException, CreateException
    {
        throw new UnsupportedOperationException("Method not implemented yet");
    }
}
