package com.atlassian.jira.config;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bean.SubTaskBean;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

/**
 * Manages SubTasks - issues that are "part of" other issues.
 */
public interface SubTaskManager
{
    public static final String SUB_TASK_ISSUE_TYPE_STYLE = "jira_subtask";

    public static final String SUB_TASK_LINK_TYPE_NAME = "jira_subtask_link";
    public static final String SUB_TASK_LINK_TYPE_STYLE = "jira_subtask";
    public static final String SUB_TASK_LINK_TYPE_INWARD_NAME = "jira_subtask_inward";
    public static final String SUB_TASK_LINK_TYPE_OUTWARD_NAME = "jira_subtask_outward";

    // General Sub-Task methods
    public void enableSubTasks() throws CreateException;

    public boolean isSubTasksEnabled();

    public void disableSubTasks();

    public boolean isSubTask(GenericValue issue);

    public boolean isSubTaskIssueType(GenericValue issueType);

    public Long getParentIssueId(GenericValue issue);

    public GenericValue getParentIssue(GenericValue issue);

    public SubTaskBean getSubTaskBean(GenericValue issue, com.opensymphony.user.User remoteUser);

    public SubTaskBean getSubTaskBean(GenericValue issue, User remoteUser);

    public void moveSubTask(GenericValue issue, Long currentSequence, Long sequence);

    /**
     * @param issue The Issue
     * @deprecated since 3.9 use {@link #resetSequences(com.atlassian.jira.issue.Issue)}
     */
    public void resetSequences(GenericValue issue);

    public void resetSequences(Issue issue);

    // Subtask Issue Types
    public GenericValue createSubTaskIssueType(String name, Long sequence, String description, String iconurl) throws CreateException;

    public void updateSubTaskIssueType(String id, String name, Long sequence, String description, String iconurl) throws DataAccessException;

    public void removeSubTaskIssueType(String name) throws RemoveException;

    /**
     * Retrieves all the sub-task issue types.
     *
     * @return A Collection of sub-task {@link GenericValue}s.
     * @deprecated Use {@link #getSubTaskIssueTypeObjects} instead. Deprecated since v4.1
     */
    @Deprecated
    public Collection<GenericValue> getSubTasksIssueTypes();

    /**
     * Retrieves all the sub-task issue types
     *
     * @return A Collection of all sub-task {@link IssueType}s.
     * @since 4.1
     */
    public Collection<IssueType> getSubTaskIssueTypeObjects();

    public boolean issueTypeExistsById(String id);

    public boolean issueTypeExistsByName(String name);

    public void moveSubTaskIssueTypeUp(String id) throws DataAccessException;

    public void moveSubTaskIssueTypeDown(String id) throws DataAccessException;

    public GenericValue getSubTaskIssueTypeById(String id);

    // Sub-Task Issue Links
    public void createSubTaskIssueLink(GenericValue parentIssue, GenericValue subTaskIssue, com.opensymphony.user.User remoteUser) throws CreateException;

    // Sub-Task Issue Links
    public void createSubTaskIssueLink(GenericValue parentIssue, GenericValue subTaskIssue, User remoteUser) throws CreateException;

    public void createSubTaskIssueLink(Issue parentIssue, Issue subTaskIssue, com.opensymphony.user.User remoteUser) throws CreateException;

    public void createSubTaskIssueLink(Issue parentIssue, Issue subTaskIssue, User remoteUser) throws CreateException;

    public Collection<Long> getAllSubTaskIssueIds();

    /**
     * Returns a list of issue links associated with the issue
     *
     * @param issueId issue id
     * @return a list of issue links
     */
    public List<IssueLink> getSubTaskIssueLinks(Long issueId);

    /**
     * @param issue the issue
     * @deprecated Use {@link #getSubTaskObjects(com.atlassian.jira.issue.Issue)} instead.
     * @return subtasks as GenericValues
     */
    public Collection<GenericValue> getSubTasks(GenericValue issue);

    public Collection<Issue> getSubTaskObjects(Issue issue);

    /**
     * Change the parent of the given subtask to the given new parent on behalf
     * of the given user.
     *
     * @param subTask The SubTask
     * @param parentIssue The parent Issue
     * @param currentUser The user
     * @return an IssueUpdateBean representing the change action.
     * @throws RemoveException if there's a problem unlinking original parent.
     * @throws CreateException if there's a problem linking new parent.
     */
    public IssueUpdateBean changeParent(GenericValue subTask, GenericValue parentIssue, com.opensymphony.user.User currentUser)
            throws RemoveException, CreateException;

    /**
     * Change the parent of the given subtask to the given new parent on behalf
     * of the given user.
     *
     * @param subTask The SubTask
     * @param parentIssue The parent Issue
     * @param currentUser The user
     * @return an IssueUpdateBean representing the change action.
     * @throws RemoveException if there's a problem unlinking original parent.
     * @throws CreateException if there's a problem linking new parent.
     */
    public IssueUpdateBean changeParent(GenericValue subTask, GenericValue parentIssue, User currentUser)
            throws RemoveException, CreateException;
}
