package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.web.action.setup.AbstractSetupAction;
import com.atlassian.jira.web.action.setup.SetupOldUserHelper;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

public class UpgradeTask1_2 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask1_2.class);

    public UpgradeTask1_2()
    {
        super(false);
    }

    public String getBuildNumber()
    {
        // JRADEV-6192: Originally this returned "1.2", but was altered to make all build numbers simple integers.
        return "1";
    }

    public String getShortDescription()
    {
        return "Update database for JIRA 1.2";
    }

    public void doUpgrade(boolean setupMode)
    {
        /* SETUP DEFAULT PERMISSSIONS FOR NEW PERMISSIONS SECTION */
        log.debug("UpgradeTask1_2 - setting up default permissions");
        doAddPermissions();
    }

    private void doAddPermissions()
    {
        // add the default permissions
        try
        {
            // Add required groups
            SetupOldUserHelper.addGroup(AbstractSetupAction.DEFAULT_GROUP_USERS);
            SetupOldUserHelper.addGroup(AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS);
            SetupOldUserHelper.addGroup(AbstractSetupAction.DEFAULT_GROUP_ADMINS);

            // The Permissions.ADMINISTER Permission is added in the setup code because it is equired before upgrade tasks are run
            ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.USE, AbstractSetupAction.DEFAULT_GROUP_USERS);
            ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.USER_PICKER, AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS);
            ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.MANAGE_GROUP_FILTER_SUBSCRIPTIONS, AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS);

            GenericValue scheme = ManagerFactory.getPermissionSchemeManager().createDefaultScheme();
            ManagerFactory.getPermissionManager().addPermission(Permissions.PROJECT_ADMIN, scheme, AbstractSetupAction.DEFAULT_GROUP_ADMINS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.BROWSE, scheme, AbstractSetupAction.DEFAULT_GROUP_USERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.CREATE_ISSUE, scheme, AbstractSetupAction.DEFAULT_GROUP_USERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.COMMENT_ISSUE, scheme, AbstractSetupAction.DEFAULT_GROUP_USERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.CREATE_ATTACHMENT, scheme, AbstractSetupAction.DEFAULT_GROUP_USERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.ASSIGN_ISSUE, scheme, AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.ASSIGNABLE_USER, scheme, AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.RESOLVE_ISSUE, scheme, AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.LINK_ISSUE, scheme, AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.EDIT_ISSUE, scheme, AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.DELETE_ISSUE, scheme, AbstractSetupAction.DEFAULT_GROUP_ADMINS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.CLOSE_ISSUE, scheme, AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.MOVE_ISSUE, scheme, AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.SCHEDULE_ISSUE, scheme, AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.MODIFY_REPORTER, scheme, AbstractSetupAction.DEFAULT_GROUP_ADMINS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.WORK_ISSUE, scheme, AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.WORKLOG_DELETE_ALL, scheme, AbstractSetupAction.DEFAULT_GROUP_ADMINS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.WORKLOG_DELETE_OWN, scheme, AbstractSetupAction.DEFAULT_GROUP_USERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.WORKLOG_EDIT_ALL, scheme, AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.WORKLOG_EDIT_OWN, scheme, AbstractSetupAction.DEFAULT_GROUP_USERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.VIEW_VOTERS_AND_WATCHERS, scheme, AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.MANAGE_WATCHER_LIST, scheme, AbstractSetupAction.DEFAULT_GROUP_ADMINS, GroupDropdown.DESC);
            // I know this is stupid because the default permission scheme no longer uses groups, instead it uses roles but the UpgradeTask_Build176 will convert the groups to roles
            ManagerFactory.getPermissionManager().addPermission(Permissions.COMMENT_EDIT_ALL, scheme, AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.COMMENT_EDIT_OWN, scheme, AbstractSetupAction.DEFAULT_GROUP_USERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.COMMENT_DELETE_ALL, scheme, AbstractSetupAction.DEFAULT_GROUP_ADMINS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.COMMENT_DELETE_OWN, scheme, AbstractSetupAction.DEFAULT_GROUP_USERS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.ATTACHMENT_DELETE_ALL, scheme, AbstractSetupAction.DEFAULT_GROUP_ADMINS, GroupDropdown.DESC);
            ManagerFactory.getPermissionManager().addPermission(Permissions.ATTACHMENT_DELETE_OWN, scheme, AbstractSetupAction.DEFAULT_GROUP_USERS, GroupDropdown.DESC);
        }
        catch (Exception e)
        {
            addError(getI18nBean().getText("admin.errors.exception") + " " + e);
            log.error("Exception: " + e);
        }
    }
}
