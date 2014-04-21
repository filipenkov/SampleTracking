/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.permission;

import com.atlassian.jira.security.Permissions;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class gets a list of all the permissions that can be part of a permission scheme
 */
public class SchemePermissions
{
    private LinkedHashMap<Integer, Permission> permissions;
    private LinkedHashMap<Integer, Permission> projectPermissions;
    private LinkedHashMap<Integer, Permission> issuePermissions;
    private LinkedHashMap<Integer, Permission> votersAndWatchersPermissions;
    private LinkedHashMap<Integer, Permission> timeTrackingPermissions;
    private LinkedHashMap<Integer, Permission> commentsPermissions;
    private LinkedHashMap<Integer, Permission> attachmentsPermissions;

    /**
     * @return Map of project related Permissions keyed by their ID
     */
    public synchronized Map<Integer, Permission> getProjectPermissions()
    {
        if (projectPermissions == null)
        {
            projectPermissions = new LinkedHashMap<Integer, Permission>();
            projectPermissions.put(new Integer(Permissions.PROJECT_ADMIN), new PermissionImpl(String.valueOf(Permissions.PROJECT_ADMIN),
                "Administer Projects", "Ability to administer a project in JIRA.", "admin.permissions.PROJECT_ADMIN",
                "admin.permissions.descriptions.PROJECT_ADMIN"));
            projectPermissions.put(new Integer(Permissions.BROWSE), new PermissionImpl(String.valueOf(Permissions.BROWSE), "Browse Projects",
                "Ability to browse projects and the issues within them.", "admin.permissions.BROWSE", "admin.permissions.descriptions.BROWSE"));
            projectPermissions.put(new Integer(Permissions.VIEW_VERSION_CONTROL), new PermissionImpl(
                String.valueOf(Permissions.VIEW_VERSION_CONTROL), "View Version Control",
                "Ability to view Version Control commit information for issues.", "admin.permissions.VIEW_VERSION_CONTROL",
                "admin.permissions.descriptions.VIEW_VERSION_CONTROL"));
            projectPermissions.put(new Integer(Permissions.VIEW_WORKFLOW_READONLY), new PermissionImpl(String.valueOf(Permissions.VIEW_WORKFLOW_READONLY), "View Workflow",
                "Ability to view a read-only workflow diagram for issues.", "admin.permissions.VIEW_WORKFLOW_READONLY", "admin.permissions.descriptions.WORKFLOW_VIEW_READONLY"));
        }
        return projectPermissions;
    }

    /**
     * @return Map of issue related Permissions keyed by their ID
     */
    public synchronized Map<Integer, Permission> getIssuePermissions()
    {
        if (issuePermissions == null)
        {
            issuePermissions = new LinkedHashMap<Integer, Permission>();
            issuePermissions.put(new Integer(Permissions.CREATE_ISSUE), new PermissionImpl(String.valueOf(Permissions.CREATE_ISSUE), "Create Issues",
                "Ability to create issues.", "admin.permissions.CREATE_ISSUE", "admin.permissions.descriptions.CREATE_ISSUE"));
            issuePermissions.put(new Integer(Permissions.EDIT_ISSUE), new PermissionImpl(String.valueOf(Permissions.EDIT_ISSUE), "Edit Issues",
                "Ability to edit issues.", "admin.permissions.EDIT_ISSUE", "admin.permissions.descriptions.EDIT_ISSUE"));
            issuePermissions.put(new Integer(Permissions.SCHEDULE_ISSUE), new PermissionImpl(String.valueOf(Permissions.SCHEDULE_ISSUE),
                "Schedule Issues", "Ability to view or edit an issue's due date", "admin.permissions.SCHEDULE_ISSUE",
                "admin.permissions.descriptions.SCHEDULE_ISSUE"));
            issuePermissions.put(
                new Integer(Permissions.MOVE_ISSUE),
                new PermissionImpl(
                    String.valueOf(Permissions.MOVE_ISSUE),
                    "Move Issues",
                    "Ability to move issues between projects or between workflows of the same project (if applicable). Note the user can only move issues to a project he or she has the create permission for.",
                    "admin.permissions.MOVE_ISSUE", "admin.permissions.descriptions.MOVE_ISSUE"));
            issuePermissions.put(new Integer(Permissions.ASSIGN_ISSUE), new PermissionImpl(String.valueOf(Permissions.ASSIGN_ISSUE), "Assign Issues",
                "Ability to assign issues to other people.", "admin.permissions.ASSIGN_ISSUE", "admin.permissions.descriptions.ASSIGN_ISSUE"));
            issuePermissions.put(new Integer(Permissions.ASSIGNABLE_USER), new PermissionImpl(String.valueOf(Permissions.ASSIGNABLE_USER),
                "Assignable User", "Users with this permission may be assigned to issues.", "admin.permissions.ASSIGNABLE_USER",
                "admin.permissions.descriptions.ASSIGNABLE_USER"));
            issuePermissions.put(new Integer(Permissions.RESOLVE_ISSUE), new PermissionImpl(String.valueOf(Permissions.RESOLVE_ISSUE),
                "Resolve Issues", "Ability to resolve and reopen issues. This includes the ability to set a fix version.",
                "admin.permissions.RESOLVE_ISSUE", "admin.permissions.descriptions.RESOLVE_ISSUE"));
            issuePermissions.put(new Integer(Permissions.CLOSE_ISSUE), new PermissionImpl(String.valueOf(Permissions.CLOSE_ISSUE), "Close Issues",
                "Ability to close issues. Often useful where your developers resolve issues, and a QA department closes them.",
                "admin.permissions.CLOSE_ISSUE", "admin.permissions.descriptions.CLOSE_ISSUE"));
            issuePermissions.put(new Integer(Permissions.MODIFY_REPORTER), new PermissionImpl(String.valueOf(Permissions.MODIFY_REPORTER),
                "Modify Reporter", "Ability to modify the reporter when creating or editing an issue.", "admin.permissions.MODIFY_REPORTER",
                "admin.permissions.descriptions.MODIFY_REPORTER"));
            issuePermissions.put(new Integer(Permissions.DELETE_ISSUE), new PermissionImpl(String.valueOf(Permissions.DELETE_ISSUE), "Delete Issues",
                "Ability to delete issues.", "admin.permissions.DELETE_ISSUE", "admin.permissions.descriptions.DELETE_ISSUE"));
            issuePermissions.put(new Integer(Permissions.LINK_ISSUE), new PermissionImpl(String.valueOf(Permissions.LINK_ISSUE), "Link Issues",
                "Ability to link issues together and create linked issues. Only useful if issue linking is turned on.",
                "admin.permissions.LINK_ISSUE", "admin.permissions.descriptions.LINK_ISSUE"));
            issuePermissions.put(new Integer(Permissions.SET_ISSUE_SECURITY), new PermissionImpl(String.valueOf(Permissions.SET_ISSUE_SECURITY),
                "Set Issue Security",
                "Ability to set the level of security on an issue so that only people in that security level can see the issue.",
                "admin.permissions.SET_ISSUE_SECURITY", "admin.permissions.descriptions.SET_ISSUE_SECURITY"));
        }
        return issuePermissions;
    }

    /**
     * @return Map of voters/watchers related Permissions keyed by their ID
     */
    public synchronized Map<Integer, Permission> getVotersAndWatchersPermissions()
    {
        if (votersAndWatchersPermissions == null)
        {
            votersAndWatchersPermissions = new LinkedHashMap<Integer, Permission>();
            votersAndWatchersPermissions.put(new Integer(Permissions.VIEW_VOTERS_AND_WATCHERS), new PermissionImpl(
                String.valueOf(Permissions.VIEW_VOTERS_AND_WATCHERS), "View Voters and Watchers",
                "Ability to view the voters and watchers of an issue.", "admin.permissions.VIEW_VOTERS_AND_WATCHERS",
                "admin.permissions.descriptions.VIEW_VOTERS_AND_WATCHERS"));
            votersAndWatchersPermissions.put(new Integer(Permissions.MANAGE_WATCHER_LIST), new PermissionImpl(
                String.valueOf(Permissions.MANAGE_WATCHER_LIST), "Manage Watchers", "Ability to manage the watchers of an issue.",
                "admin.permissions.MANAGE_WATCHER_LIST", "admin.permissions.descriptions.MANAGE_WATCHER_LIST"));
        }
        return votersAndWatchersPermissions;
    }

    /**
     * @return Map of comment related Permissions keyed by their ID
     */
    public synchronized Map<Integer, Permission> getCommentsPermissions()
    {
        if (commentsPermissions == null)
        {
            commentsPermissions = new LinkedHashMap<Integer, Permission>();
            commentsPermissions.put(new Integer(Permissions.COMMENT_ISSUE), new PermissionImpl(String.valueOf(Permissions.COMMENT_ISSUE),
                "Add Comments", "Ability to comment on issues.", "admin.permissions.COMMENT_ISSUE", "admin.permissions.descriptions.COMMENT_ISSUE"));
            commentsPermissions.put(new Integer(Permissions.COMMENT_EDIT_ALL), new PermissionImpl(String.valueOf(Permissions.COMMENT_EDIT_ALL),
                "Edit All Comments", "Ability to edit all comments made on issues.", "admin.permissions.COMMENT_EDIT_ALL",
                "admin.permissions.descriptions.COMMENT_EDIT_ALL"));
            commentsPermissions.put(new Integer(Permissions.COMMENT_EDIT_OWN), new PermissionImpl(String.valueOf(Permissions.COMMENT_EDIT_OWN),
                "Edit Own Comments", "Ability to edit own comments made on issues.", "admin.permissions.COMMENT_EDIT_OWN",
                "admin.permissions.descriptions.COMMENT_EDIT_OWN"));
            commentsPermissions.put(new Integer(Permissions.COMMENT_DELETE_ALL), new PermissionImpl(String.valueOf(Permissions.COMMENT_DELETE_ALL),
                "Delete All Comments", "Ability to delete all comments made on issues.", "admin.permissions.COMMENT_DELETE_ALL",
                "admin.permissions.descriptions.COMMENT_DELETE_ALL"));
            commentsPermissions.put(new Integer(Permissions.COMMENT_DELETE_OWN), new PermissionImpl(String.valueOf(Permissions.COMMENT_DELETE_OWN),
                "Delete Own Comments", "Ability to delete own comments made on issues.", "admin.permissions.COMMENT_DELETE_OWN",
                "admin.permissions.descriptions.COMMENT_DELETE_OWN"));
        }
        return commentsPermissions;
    }

    /**
     * @return Map of attachment related Permissions keyed by their ID
     */
    public synchronized Map<Integer, Permission> getAttachmentsPermissions()
    {
        if (attachmentsPermissions == null)
        {
            attachmentsPermissions = new LinkedHashMap<Integer, Permission>();
            attachmentsPermissions.put(new Integer(Permissions.CREATE_ATTACHMENT), new PermissionImpl(String.valueOf(Permissions.CREATE_ATTACHMENT),
                "Create Attachments", "Users with this permission may create attachments.", "admin.permissions.CREATE_ATTACHMENT",
                "admin.permissions.descriptions.CREATE_ATTACHMENT"));
            attachmentsPermissions.put(new Integer(Permissions.ATTACHMENT_DELETE_ALL), new PermissionImpl(
                String.valueOf(Permissions.ATTACHMENT_DELETE_ALL), "Delete All Attachments",
                "Users with this permission may delete all attachments.", "admin.permissions.ATTACHMENT_DELETE_ALL",
                "admin.permissions.descriptions.ATTACHMENT_DELETE_ALL"));
            attachmentsPermissions.put(new Integer(Permissions.ATTACHMENT_DELETE_OWN), new PermissionImpl(
                String.valueOf(Permissions.ATTACHMENT_DELETE_OWN), "Delete Own Attachments",
                "Users with this permission may delete own attachments.", "admin.permissions.ATTACHMENT_DELETE_OWN",
                "admin.permissions.descriptions.ATTACHMENT_DELETE_OWN"));
        }

        return attachmentsPermissions;
    }

    /**
     * @return Map of time tracking related Permissions keyed by their ID
     */
    public synchronized Map<Integer, Permission> getTimeTrackingPermissions()
    {
        if (timeTrackingPermissions == null)
        {
            timeTrackingPermissions = new LinkedHashMap<Integer, Permission>();
            timeTrackingPermissions.put(new Integer(Permissions.WORK_ISSUE), new PermissionImpl(String.valueOf(Permissions.WORK_ISSUE),
                "Work On Issues", "Ability to log work done against an issue. Only useful if Time Tracking is turned on.",
                "admin.permissions.WORK_ISSUE", "admin.permissions.descriptions.WORK_ISSUE"));
            timeTrackingPermissions.put(new Integer(Permissions.WORKLOG_EDIT_OWN), new PermissionImpl(String.valueOf(Permissions.WORKLOG_EDIT_OWN),
                "Edit Own Worklogs", "Users with this permission may edit own worklogs.", "admin.permissions.WORKLOG_EDIT_OWN",
                "admin.permissions.descriptions.WORKLOG_EDIT_OWN"));
            timeTrackingPermissions.put(new Integer(Permissions.WORKLOG_EDIT_ALL), new PermissionImpl(String.valueOf(Permissions.WORKLOG_EDIT_ALL),
                "Edit All Worklogs", "Users with this permission may edit all worklogs.", "admin.permissions.WORKLOG_EDIT_ALL",
                "admin.permissions.descriptions.WORKLOG_EDIT_ALL"));
            timeTrackingPermissions.put(new Integer(Permissions.WORKLOG_DELETE_OWN), new PermissionImpl(
                String.valueOf(Permissions.WORKLOG_DELETE_OWN), "Delete Own Worklogs", "Users with this permission may delete own worklogs.",
                "admin.permissions.WORKLOG_DELETE_OWN", "admin.permissions.descriptions.WORKLOG_DELETE_OWN"));
            timeTrackingPermissions.put(new Integer(Permissions.WORKLOG_DELETE_ALL), new PermissionImpl(
                String.valueOf(Permissions.WORKLOG_DELETE_ALL), "Delete All Worklogs", "Users with this permission may delete all worklogs.",
                "admin.permissions.WORKLOG_DELETE_ALL", "admin.permissions.descriptions.WORKLOG_DELETE_ALL"));
        }
        return timeTrackingPermissions;
    }

    /**
     * Get a map of the permissions that can be part of a permission scheme. This map contains the permission id and the permission name
     * @return Map containing the permissions
     */
    public synchronized Map<Integer, Permission> getSchemePermissions()
    {
        if (permissions == null)
        {
            permissions = new LinkedHashMap<Integer, Permission>();

            permissions.putAll(getProjectPermissions());
            permissions.putAll(getIssuePermissions());
            permissions.putAll(getVotersAndWatchersPermissions());
            permissions.putAll(getCommentsPermissions());
            permissions.putAll(getAttachmentsPermissions());
            permissions.putAll(getTimeTrackingPermissions());

            //            permissions.put(new Integer(Permissions.CREATE_SHARED_OBJECTS), "Create Shared Filter");
            //            permissions.put(new Integer(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS), "Manage Group Filter Subscriptions");
        }

        /*
        if (permissions == null)
        {
            permissions = new LinkedHashMap<Integer, Permission>();

            permissions.put(new Integer(Permissions.PROJECT_ADMIN), "Administer Projects");
            permissions.put(new Integer(Permissions.BROWSE), "Browse Projects");
            permissions.put(new Integer(Permissions.CREATE_ISSUE), "Create Issues");
            permissions.put(new Integer(Permissions.EDIT_ISSUE), "Edit Issues");
            permissions.put(new Integer(Permissions.SCHEDULE_ISSUE), "Schedule Issues");
            permissions.put(new Integer(Permissions.MOVE_ISSUE), "Move Issues");
            permissions.put(new Integer(Permissions.ASSIGN_ISSUE), "Assign Issues");
            permissions.put(new Integer(Permissions.ASSIGNABLE_USER), "Assignable User");
            permissions.put(new Integer(Permissions.RESOLVE_ISSUE), "Resolve Issues");
            permissions.put(new Integer(Permissions.CLOSE_ISSUE), "Close Issues");
            permissions.put(new Integer(Permissions.MODIFY_REPORTER), "Modify Reporter");
            permissions.put(new Integer(Permissions.COMMENT_ISSUE), "Add Comments");
            permissions.put(new Integer(Permissions.DELETE_ISSUE), "Delete Issues");
            permissions.put(new Integer(Permissions.WORK_ISSUE), "Work On Issues");
            permissions.put(new Integer(Permissions.LINK_ISSUE), "Link Issues");
            permissions.put(new Integer(Permissions.CREATE_ATTACHMENT), "Create Attachments");
            permissions.put(new Integer(Permissions.VIEW_VERSION_CONTROL), "View Version Control");
            permissions.put(new Integer(Permissions.VIEW_VOTERS_AND_WATCHERS), "View Voters and Watchers");
            permissions.put(new Integer(Permissions.MANAGE_WATCHER_LIST), "Manage Watcher List");

            //            permissions.put(new Integer(Permissions.CREATE_SHARED_OBJECTS), "Create Shared Filter");
            //            permissions.put(new Integer(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS), "Manage Group Filter Subscriptions");
            License license = JiraLicenseUtils.getLicense();
            if (license != null && license.isLicenseLevel(EasyList.build(JiraLicenseUtils.JIRA_ENTERPRISE_LEVEL)))
            {
                permissions.put(new Integer(Permissions.SET_ISSUE_SECURITY), "Set Issue Security");
            }
        */

        return permissions;
    }

    /**
     * Checks to see if the permission exists
     * @param id The permission Id
     * @return True / False
     */
    public boolean schemePermissionExists(final Integer id)
    {
        return getSchemePermissions().containsKey(id);
    }

    /**
     * Get the name of the permission
     * @param id The permission Id
     * @return The name of the permission
     */
    public String getPermissionName(final Integer id)
    {
        return ((Permission) getSchemePermissions().get(id)).getNameKey();
    }

    /**
     * Gets the description for the permission
     * @param id Id of the permission that you want to get the description for
     * @return String containing the description
     */
    public String getPermissionDescription(final int id)
    {
        if (getSchemePermissions().get(new Integer(id)) != null)
        {
            return ((Permission) getSchemePermissions().get(new Integer(id))).getDescription();
        }
        else
        {
            // this is the old way, here as a fallback but could probably be removed
            return Permissions.getDescription(id);
        }
    }
}
