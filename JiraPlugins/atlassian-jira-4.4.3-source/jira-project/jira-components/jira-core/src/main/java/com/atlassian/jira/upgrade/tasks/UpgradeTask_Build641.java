package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.notification.type.ProjectRoleSecurityAndNotificationType;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

/**
 * Gives View Only Workflow permission to Admin, Users and Dev on SETUP ONLY. See JRADEV-5098.
 * <p/>
 * It's defined as a setup only and hence will only run on new instances, not upgrades to existing instances
 *
 * @since v4.4
 */
public class UpgradeTask_Build641 extends AbstractUpgradeTask {

    private static final Logger log = Logger.getLogger(UpgradeTask_Build641.class);
    private final PermissionSchemeManager permissionSchemeManager;
    private final ProjectRoleManager projectRoleManager;
    private final PermissionManager permissionManager;


    public UpgradeTask_Build641(final PermissionManager permissionManager,
            final PermissionSchemeManager permissionSchemeManager, final ProjectRoleManager projectRoleManager) {
        this.permissionManager = permissionManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.projectRoleManager = projectRoleManager;
    }

    @Override
    public String getBuildNumber() {
        return "641";
    }

    @Override
    public String getShortDescription() {
        return "Gives View Only Workflow permission to Users on SETUP ONLY.";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception {
        log.info("Adding 'View Only Workflow' permissions to the Users project role...");

        final GenericValue defaultScheme = permissionSchemeManager.getDefaultScheme();
        final ProjectRole projectRole = projectRoleManager.getProjectRole(UpgradeTask_Build175.ROLE_USERS);
        if(projectRole != null)
        {
            permissionManager.addPermission(Permissions.VIEW_WORKFLOW_READONLY, defaultScheme, projectRole.getId().toString(),
                    ProjectRoleSecurityAndNotificationType.PROJECT_ROLE);
        }

        log.info("Done adding 'View Only Workflow' permissions to the Users project role.");
    }
}
