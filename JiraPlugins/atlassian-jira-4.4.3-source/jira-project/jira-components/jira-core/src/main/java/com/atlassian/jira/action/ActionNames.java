package com.atlassian.jira.action;

import com.atlassian.jira.action.admin.ListenerCreate;
import com.atlassian.jira.action.admin.ListenerDelete;
import com.atlassian.jira.action.component.ComponentEdit;
import com.atlassian.jira.action.issue.IssueDelete;
import com.atlassian.jira.action.issue.IssueUpdate;
import com.atlassian.jira.action.projectcategory.ProjectCategoryDelete;
import com.atlassian.jira.action.projectcategory.ProjectCategoryEdit;

/**
 * A basic class to alias all of the action names.
 * <p/>
 * We should abstract this out into actions.xml or something when WW better supports commands not via the web (aliases
 * etc).
 */
public interface ActionNames
{
    public static final String ISSUE_DELETE = IssueDelete.class.getName();
    public static final String ISSUE_UPDATE = IssueUpdate.class.getName();
    public static final String LISTENER_CREATE = ListenerCreate.class.getName();
    public static final String LISTENER_DELETE = ListenerDelete.class.getName();
    public static final String COMPONENT_EDIT = ComponentEdit.class.getName();

    public static final String PROJECTCATEGORY_EDIT = ProjectCategoryEdit.class.getName();
    public static final String PROJECTCATEGORY_DELETE = ProjectCategoryDelete.class.getName();
}
