package com.atlassian.jira.action;

import com.atlassian.jira.action.admin.ListenerCreate;
import com.atlassian.jira.action.admin.ListenerDelete;
import com.atlassian.jira.action.component.ComponentEdit;
import com.atlassian.jira.action.issue.IssueDelete;
import com.atlassian.jira.action.issue.IssueUpdate;

/**
 * A basic class to alias all of the action names.
 * <p/>
 * We should abstract this out into actions.xml or something when WW better supports commands not via the web (aliases
 * etc).
 */
public interface ActionNames
{
    /**
     * @deprecated Use {@link com.atlassian.jira.issue.IssueManager#deleteIssue(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.MutableIssue, com.atlassian.jira.event.type.EventDispatchOption, boolean)} instead. Since v4.1.
     */
    public static final String ISSUE_DELETE = IssueDelete.class.getName();
    /**
     * @deprecated Use {@link com.atlassian.jira.issue.IssueManager#updateIssue(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.issue.MutableIssue, com.atlassian.jira.event.type.EventDispatchOption, boolean)} instead. Since v4.1.
     */
    public static final String ISSUE_UPDATE = IssueUpdate.class.getName();
    /**
     * @deprecated Use {@link com.atlassian.jira.event.ListenerManager#createListener(String, Class)} instead. Since v5.0.
     */
    public static final String LISTENER_CREATE = ListenerCreate.class.getName();
    /**
     * @deprecated Use {@link com.atlassian.jira.event.ListenerManager#deleteListener(Class)} instead. Since v5.0.
     */
    public static final String LISTENER_DELETE = ListenerDelete.class.getName();

    /**
     * @deprecated Use {@link com.atlassian.jira.bc.project.component.ProjectComponentService#update(com.atlassian.crowd.embedded.api.User, com.atlassian.jira.util.ErrorCollection, com.atlassian.jira.bc.project.component.MutableProjectComponent)}
     *              or {@link com.atlassian.jira.bc.project.component.ProjectComponentManager#update(com.atlassian.jira.bc.project.component.MutableProjectComponent)} instead. Since v5.0.
     */
    public static final String COMPONENT_EDIT = ComponentEdit.class.getName();
}
