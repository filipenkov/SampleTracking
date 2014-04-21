package com.atlassian.jira.scheme.mapper;

import com.atlassian.core.user.GroupUtils;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.security.roles.RoleActorDoesNotExistException;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.atlassian.jira.security.roles.actor.UserRoleActorFactory;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link SchemeGroupsToRolesTransformer}.
 */
public class SchemeGroupsToRolesTransformerImpl implements SchemeGroupsToRolesTransformer
{
    private static final Logger log = Logger.getLogger(SchemeGroupsToRolesTransformerImpl.class);

    private SchemeManagerFactory schemeManagerFactory;
    private ProjectRoleManager projectRoleManager;
    private RoleActorFactory roleActorFactory;
    private final IssueSecurityLevelManager issueSecurityLevelManager;
    private static final String BACKUP_OF = "Backup of ";

    public SchemeGroupsToRolesTransformerImpl(SchemeManagerFactory schemeManagerFactory, ProjectRoleManager projectRoleManager, RoleActorFactory roleActorFactory, IssueSecurityLevelManager issueSecurityLevelManager)
    {
        this.schemeManagerFactory = schemeManagerFactory;
        this.projectRoleManager = projectRoleManager;
        this.roleActorFactory = roleActorFactory;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
    }

    public SchemeTransformResults doTransform(List schemes, Set groupToRoleMappings)
    {
        SchemeTransformResults schemeTransformResults = new SchemeTransformResults();

        for (Iterator iterator = schemes.iterator(); iterator.hasNext();)
        {
            Scheme scheme = (Scheme) iterator.next();
            schemeTransformResults.addResult(transformScheme(scheme, groupToRoleMappings));
        }
        return schemeTransformResults;
    }

    public void persistTransformationResults(SchemeTransformResults schemeTransformResults)
    {
        for (Iterator it = schemeTransformResults.getAllSchemeTransformResults().iterator(); it.hasNext();)
        {
            SchemeTransformResult schemeTransformResult = (SchemeTransformResult) it.next();
            persistSchemeTransformResult(schemeTransformResult);
        }
    }

    private void persistSchemeTransformResult(SchemeTransformResult schemeTransformResult)
    {
        persistUsersToRoleActors(schemeTransformResult);
        backupOriginalScheme(schemeTransformResult);
        persistTransformedScheme(schemeTransformResult);
    }

    private void backupOriginalScheme(SchemeTransformResult schemeTransformResult)
    {
        Scheme originalScheme = schemeTransformResult.getOriginalScheme();
        SchemeManager schemeManager = schemeManagerFactory.getSchemeManager(originalScheme.getType());
        String newName = getValidBackupNameForScheme(BACKUP_OF + schemeTransformResult.getOriginalScheme().getName(), schemeManager, 1);
        originalScheme.setName(newName);
        schemeManager.updateScheme(originalScheme);
    }

    private String getValidBackupNameForScheme(String schemeName, SchemeManager schemeManager, int count)
    {
        try
        {
            if (schemeManager.getScheme(schemeName) != null)
            {
                return getValidBackupNameForScheme(schemeName + " (" + count + ")", schemeManager, count + 1);
            }
        }
        catch (GenericEntityException e)
        {
            log.warn("Unable to resolve scheme with name: " + schemeName, e);
        }
        return schemeName;
    }

    private void persistTransformedScheme(SchemeTransformResult schemeTransformResult)
    {
        // Create the new scheme and associated entities
        Scheme transformedScheme = schemeTransformResult.getTransformedScheme();
        SchemeManager schemeManager = schemeManagerFactory.getSchemeManager(transformedScheme.getType());

        Scheme scheme = schemeManager.createSchemeAndEntities(transformedScheme);
        schemeTransformResult.setResultingScheme(scheme);
        modifyAllProjectAssociations(schemeManager, schemeTransformResult);
    }

    private void modifyAllProjectAssociations(SchemeManager schemeManager, SchemeTransformResult schemeResult)
    {
        // update the associations for all projects
        for (Iterator iterator = schemeResult.getAssociatedProjects().iterator(); iterator.hasNext();)
        {
            Project project = (Project) iterator.next();
            // Remove the current scheme
            schemeManager.removeSchemesFromProject(project);
            // Add the new scheme association
            schemeManager.addSchemeToProject(project, schemeResult.getResultingScheme());
        }
    }

    private void persistUsersToRoleActors(SchemeTransformResult schemeTransformResult)
    {
        // We need to run through every GroupRoleMapping
        for (Iterator iterator = schemeTransformResult.getRoleToGroupsMappings().iterator(); iterator.hasNext();)
        {
            RoleToGroupsMapping roleToGroupsMapping = (RoleToGroupsMapping) iterator.next();
            Collection associatedProjects = schemeTransformResult.getAssociatedProjects();
            updateRoleActorsForProjects(roleToGroupsMapping, associatedProjects);
        }
    }

    private void updateRoleActorsForProjects(RoleToGroupsMapping roleToGroupsMapping, Collection associatedProjects)
    {
        ProjectRole projectRole = roleToGroupsMapping.getProjectRole();

        // Then for every associated project we need to add all the users that have been unpacked from the
        // mapped group to the mapped ProjectRole. We need to create a RoleActor for the specific project/ProjectRole
        // combination from the user.
        for (Iterator iterator1 = associatedProjects.iterator(); iterator1.hasNext();)
        {
            Project project = (Project) iterator1.next();
            ProjectRoleActors projectRoleActors = projectRoleManager.getProjectRoleActors(projectRole, project);

            // Run through all the unpacked users and create roleActors from them
            Collection users = roleToGroupsMapping.getUnpackedUsers();
            Set set = new HashSet(users.size());
            for (Iterator iterator2 = users.iterator(); iterator2.hasNext();)
            {
                String userName = (String) iterator2.next();
                // Get a RoleActor for the user
                RoleActor roleActor = null;
                try
                {
                    roleActor = roleActorFactory.createRoleActor(null, projectRole.getId(), project.getId(), UserRoleActorFactory.TYPE, userName);
                }
                catch (RoleActorDoesNotExistException e)
                {
                    throw new IllegalArgumentException("Unexpected error: the user '" + userName + "' does not exist.");
                }

                set.add(roleActor);
            }
            projectRoleActors = (ProjectRoleActors) projectRoleActors.addRoleActors(set);
            // Update the project role
            projectRoleManager.updateProjectRoleActors(projectRoleActors);
            clearIssueSecurityLevelCache();
        }
    }

    private SchemeTransformResult transformScheme(Scheme scheme, Set groupToRoleMappings)
    {
        SchemeTransformResult schemeTransformResult = new SchemeTransformResult(scheme);

        SchemeManager schemeManager = getSchemeManager(scheme.getType());
        // Only lookup associated projects if this scheme is an entity that exists in the db.
        if (scheme.getId() != null)
        {
            schemeTransformResult.setAssociatedProjects(schemeManager.getProjects(scheme));
        }

        for (Iterator iterator = groupToRoleMappings.iterator(); iterator.hasNext();)
        {
            GroupToRoleMapping groupToRoleMapping = (GroupToRoleMapping) iterator.next();
            schemeTransformResult.addRoleMappingForGroup(groupToRoleMapping, GroupUtils.getGroup(groupToRoleMapping.getGroupName()));
        }
        return schemeTransformResult;
    }

    private SchemeManager getSchemeManager(String type)
    {
        return schemeManagerFactory.getSchemeManager(type);
    }

    private void clearIssueSecurityLevelCache()
    {
        try
        {
            if (issueSecurityLevelManager != null)
            {
                issueSecurityLevelManager.clearUsersLevels();
            }
        }
        catch (UnsupportedOperationException uoe)
        {
            log.debug("Unsupported operation was thrown when trying to clear the issue security level manager cache", uoe);
        }
    }
}
