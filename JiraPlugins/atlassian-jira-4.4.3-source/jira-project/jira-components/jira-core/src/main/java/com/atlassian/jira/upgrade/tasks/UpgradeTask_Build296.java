package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.JiraPermission;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;

/**
 * Upgrade task that will populate the {@link Permissions#SYSTEM_ADMIN} permissions with those from the
 * {@link Permissions#ADMINISTER} permissions.
 *
 * @since v3.12
 */
public class UpgradeTask_Build296 extends AbstractUpgradeTask
{

    private static final Logger log = Logger.getLogger(UpgradeTask_Build296.class);
    private final GlobalPermissionManager globalPermissionManager;

    public UpgradeTask_Build296(GlobalPermissionManager globalPermissionManager)
    {
        this.globalPermissionManager = globalPermissionManager;
    }

    public String getShortDescription()
    {
        return "Populates the JIRA System Administrator global permission with all groups associated with the JIRA Administrators global permission.";
    }

    public String getBuildNumber()
    {
        return "296";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        // Find out if we have any SysAdmin perms
        Collection groupsForSysAdmin = globalPermissionManager.getPermissions(Permissions.SYSTEM_ADMIN);

        // If are configured permissions for the SYS_ADMIN then don't do anything
        if (groupsForSysAdmin != null && groupsForSysAdmin.isEmpty())
        {
            // Get all the groups for Admin
            Collection groupsForAdmin = globalPermissionManager.getPermissions(Permissions.ADMINISTER);
            // For each existing Admin perm entry we want to create one for SYS_ADMIN
            if (groupsForAdmin != null)
            {
                for (Iterator iterator = groupsForAdmin.iterator(); iterator.hasNext();)
                {
                    JiraPermission jiraPermission = (JiraPermission) iterator.next();
                    // Create the Sys Admin against the same group, we do this through the manager so that the cache will be correctly cleared.
                    log.info("Creating a JIRA System Administrators permission entry for group '" + jiraPermission.getGroup() + "'.");
                    try
                    {
                        globalPermissionManager.addPermission(Permissions.SYSTEM_ADMIN, jiraPermission.getGroup());
                    }
                    catch (CreateException e)
                    {
                        log.error("Unable to create a JIRA System Administrators permission entry for group '" + jiraPermission.getGroup() + "'.");
                        throw e;
                    }
                }
            }
        }
        else
        {
            log.warn("The JIRA System Administrators Global Permission has group entries, this should not happen, this upgrade task will not run.");
        }
    }
}
