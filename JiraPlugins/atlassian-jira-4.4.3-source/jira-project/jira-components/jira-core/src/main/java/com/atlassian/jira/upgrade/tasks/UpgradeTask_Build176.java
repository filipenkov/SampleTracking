package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.web.action.setup.AbstractSetupAction;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Upgrades the default permission scheme to contain references to roles instead of groups,
 * only if there are no projects currently within JIRA (i.e. we have just setup a new instance of JIRA).
 */
public class UpgradeTask_Build176 extends AbstractUpgradeTask
{
    public static final String PROJECT_ROLE_SECURITY_TYPE = "projectrole";

    private ProjectManager projectManager;
    private PermissionSchemeManager schemeManager;
    private ProjectRoleManager projectRoleManager;

    protected Map groupToRoleMappings;

    public UpgradeTask_Build176(ProjectManager projectManager, PermissionSchemeManager schemeManager, ProjectRoleManager projectRoleManager)
    {
        this.projectManager = projectManager;
        this.schemeManager = schemeManager;
        this.projectRoleManager = projectRoleManager;
        this.groupToRoleMappings = new HashMap();
    }

    public String getShortDescription()
    {
        return "Upgrade the default permission scheme to contain references to roles instead of groups, " +
                "only if there are no projects currently within JIRA (i.e. we have just setup a new instance of JIRA)";
    }

    public String getBuildNumber()
    {
        return "176";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        // Only ever to this upgrade task if there are no projects in the system
        if(projectManager.getProjects().size() == 0)
        {
            initGroupToProjectRoleMappings();
            convertDefaultPermissionSchemeToUseRoles();
        }
    }

    protected void initGroupToProjectRoleMappings()
    {
        for (Iterator iterator = projectRoleManager.getProjectRoles().iterator(); iterator.hasNext();)
        {
            ProjectRole projectRole = (ProjectRole) iterator.next();
            if (UpgradeTask_Build175.ROLE_ADMINISTRATORS.equals(projectRole.getName()))
            {
                groupToRoleMappings.put(AbstractSetupAction.DEFAULT_GROUP_ADMINS, projectRole.getId());
            }
            else if (UpgradeTask_Build175.ROLE_DEVELOPERS.equals(projectRole.getName()))
            {
                groupToRoleMappings.put(AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS, projectRole.getId());
            }
            else if (UpgradeTask_Build175.ROLE_USERS.equals(projectRole.getName()))
            {
                groupToRoleMappings.put(AbstractSetupAction.DEFAULT_GROUP_USERS, projectRole.getId());
            }
        }
    }

    protected void convertDefaultPermissionSchemeToUseRoles() throws GenericEntityException
    {
        GenericValue scheme = schemeManager.getDefaultScheme();
        Collection schemeEntities = schemeManager.getEntities(scheme);
        for (Iterator iterator = schemeEntities.iterator(); iterator.hasNext();)
        {
            GenericValue schemeEntity = (GenericValue) iterator.next();
            // Only replace all the group scheme permission entries with roles entries
            if (GroupDropdown.DESC.equals(schemeEntity.getString("type")))
            {
                Long newParameter = (Long) groupToRoleMappings.get(schemeEntity.getString("parameter"));
                if (newParameter != null)
                {
                    schemeEntity.setString("type", PROJECT_ROLE_SECURITY_TYPE);
                    schemeEntity.setString("parameter", newParameter.toString());
                    schemeEntity.store();
                }
            }
        }
    }
}
