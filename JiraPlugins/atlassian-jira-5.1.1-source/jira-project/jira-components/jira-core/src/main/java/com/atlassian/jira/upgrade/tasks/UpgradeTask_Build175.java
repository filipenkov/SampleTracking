package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.roles.actor.GroupRoleActorFactory;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.web.action.setup.AbstractSetupAction;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

/**
 * This will create default role data for JIRA. eg. Administrators, Developers and Users roles
 */
public class UpgradeTask_Build175 extends AbstractUpgradeTask
{
    public static final String BUILD_NUMBER = "175";

    private static final Logger LOG = Logger.getLogger(UpgradeTask_Build175.class);

    private ProjectRoleManager projectRoleManager = null;
    private final OfBizDelegator delegator;
    public static final String ROLE_USERS = "Users";
    public static final String ROLE_DEVELOPERS = "Developers";
    public static final String ROLE_ADMINISTRATORS = "Administrators";
    static final String ROLE_ACTOR_ROLETYPE = "roletype";
    static final String ROLE_ACTOR_PROJECTROLEID = "projectroleid";
    static final String ROLE_ACTOR_ENTITY_NAME = "ProjectRoleActor";
    static final String ROLE_ACTOR_PID = "pid";

    public String getBuildNumber()
    {
        return BUILD_NUMBER;
    }

    public String getShortDescription()
    {
        return "Adds the default project roles and populates their members.";
    }

    public UpgradeTask_Build175(ProjectRoleManager projectRoleManager, OfBizDelegator delegator)
    {
        super(false);
        this.projectRoleManager = projectRoleManager;
        this.delegator = delegator;
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        createDefaultRoles();
    }

    private void createDefaultRoles()
    {
        ProjectRole projectRole;
        try
        {
            projectRole = projectRoleManager.createRole(new ProjectRoleImpl("Users", "A project role that represents users in a project"));
            //JRA-11721: Only create the actor if the group exists.
            if (groupExistsInOldTables(AbstractSetupAction.DEFAULT_GROUP_USERS))
            {
                createActorForDefaultRole(projectRole, AbstractSetupAction.DEFAULT_GROUP_USERS);
            }
        }
        catch (IllegalArgumentException e)
        {
            LOG.error("Unable to create a project role named 'Users'", e);
        }

        try
        {
            projectRole = projectRoleManager.createRole(new ProjectRoleImpl("Developers", "A project role that represents developers in a project"));
            //JRA-11721: Only create the actor if the group exists.
            if (groupExistsInOldTables(AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS))
            {
                createActorForDefaultRole(projectRole, AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS);
            }
        }
        catch (IllegalArgumentException e)
        {
            LOG.error("Unable to create a project role named 'Developers'", e);
        }

        try
        {
            projectRole = projectRoleManager.createRole(new ProjectRoleImpl("Administrators", "A project role that represents administrators in a project"));
            //JRA-11721: Only create the actor if the group exists.
            if (groupExistsInOldTables(AbstractSetupAction.DEFAULT_GROUP_ADMINS))
            {
                createActorForDefaultRole(projectRole, AbstractSetupAction.DEFAULT_GROUP_ADMINS);
            }
        }
        catch (IllegalArgumentException e)
        {
            LOG.error("Unable to create a project role named 'Administrators'", e);
        }
    }

    // This must happen by going strait to the DB since going through the store expects Groups to exist which they will
    // not until the CrowdEmbedded upgrade task has run.
    private void createActorForDefaultRole(ProjectRole projectRole, String actorName)
    {
        delegator.createValue(ROLE_ACTOR_ENTITY_NAME, EasyMap.build(ROLE_ACTOR_PID, null, ROLE_ACTOR_PROJECTROLEID,
                projectRole.getId(), ROLE_ACTOR_ROLETYPE, GroupRoleActorFactory.TYPE, "roletypeparameter", actorName));
    }

    boolean groupExistsInOldTables(final String groupName)
    {
        final List<GenericValue> values = delegator.findByAnd("OSGroup", EasyMap.build("name", groupName));
        return values != null && !values.isEmpty();
    }
}
