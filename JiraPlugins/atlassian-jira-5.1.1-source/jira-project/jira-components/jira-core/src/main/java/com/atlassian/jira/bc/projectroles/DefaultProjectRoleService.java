package com.atlassian.jira.bc.projectroles;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.notification.type.ProjectRoleSecurityAndNotificationType;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.DefaultRoleActors;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleActor;
import com.atlassian.jira.security.roles.ProjectRoleActors;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.roles.RoleActor;
import com.atlassian.jira.security.roles.RoleActorDoesNotExistException;
import com.atlassian.jira.security.roles.RoleActorFactory;
import com.atlassian.jira.sharing.SharePermissionDeleteUtils;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.condition.InProjectRoleCondition;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.ConditionDescriptor;
import com.opensymphony.workflow.loader.ConditionsDescriptor;
import com.opensymphony.workflow.loader.RestrictionDescriptor;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of the ProjectRoleService
 */
public class DefaultProjectRoleService implements ProjectRoleService
{
    private static final Logger log = Logger.getLogger(DefaultProjectRoleService.class);
    private ProjectRoleManager projectRoleManager;
    private PermissionManager permissionManager;
    private JiraAuthenticationContext jiraAuthenticationContext;
    private RoleActorFactory roleActorFactory;
    private NotificationSchemeManager notificationSchemeManager;
    private PermissionSchemeManager permissionSchemeManager;
    private WorkflowManager workflowManager;
    private ProjectManager projectManager;
    private SchemeFactory schemeFactory;
    private final IssueSecurityLevelManager issueSecurityLevelManager;
    private final SharePermissionDeleteUtils sharePermissionDeleteUtils;
    private IssueSecuritySchemeManager issueSecuritySchemeManager;

    public DefaultProjectRoleService(final ProjectRoleManager projectRoleManager, final PermissionManager permissionManager,
            final JiraAuthenticationContext jiraAuthenticationContext, final RoleActorFactory roleActorFactory,
            final NotificationSchemeManager notificationSchemeManager, final PermissionSchemeManager permissionSchemeManager,
            final WorkflowManager workflowManager, final ProjectManager projectManager, final SchemeFactory schemeFactory,
            final IssueSecurityLevelManager issueSecurityLevelManager, final SharePermissionDeleteUtils sharePermissionDeleteUtils,
            final IssueSecuritySchemeManager issueSecuritySchemeManager)
    {
        this.projectRoleManager = projectRoleManager;
        this.permissionManager = permissionManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.roleActorFactory = roleActorFactory;
        this.notificationSchemeManager = notificationSchemeManager;
        this.permissionSchemeManager = permissionSchemeManager;
        this.workflowManager = workflowManager;
        this.projectManager = projectManager;
        this.schemeFactory = schemeFactory;
        this.issueSecurityLevelManager = issueSecurityLevelManager;
        this.sharePermissionDeleteUtils = sharePermissionDeleteUtils;
        this.issueSecuritySchemeManager = issueSecuritySchemeManager;
    }

    @Override
    public Collection<ProjectRole> getProjectRoles(User currentUser, ErrorCollection errorCollection)
    {
        return projectRoleManager.getProjectRoles();
    }

    @Override
    public ProjectRole getProjectRole(User currentUser, Long id, ErrorCollection errorCollection)
    {
        ProjectRole projectRole = null;
        if (id == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.id.null"));
        }
        else
        {
            projectRole = projectRoleManager.getProjectRole(id);
        }
        return projectRole;
    }

    @Override
    public ProjectRole getProjectRoleByName(User currentUser, String name, ErrorCollection errorCollection)
    {
        ProjectRole projectRole = null;
        if (StringUtils.isBlank(name))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.name.null"));
        }
        else
        {
            projectRole = projectRoleManager.getProjectRole(name);
        }
        return projectRole;
    }

    @Override
    public ProjectRole createProjectRole(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        ProjectRole createdProjectRole = null;
        boolean internalError = false;

        String roleName = null;
        if (projectRole == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.null.create"));
            internalError = true;
        }
        else
        {
            roleName = projectRole.getName();
        }

        if (roleName == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.name.null.create"));
            internalError = true;
        }

        if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
            internalError = true;
        }

        if (!isProjectRoleNameUnique(currentUser, roleName, errorCollection))
        {
            internalError = true;
        }

        if (!internalError)
        {
            createdProjectRole = projectRoleManager.createRole(projectRole);
        }
        return createdProjectRole;
    }

    @Override
    public boolean isProjectRoleNameUnique(User currentUser, String name, ErrorCollection errorCollection)
    {
        boolean roleNameUnique = false;

        if (hasAdminPermission(currentUser))
        {
            roleNameUnique = projectRoleManager.isRoleNameUnique(name);
            if (!roleNameUnique)
            {
                errorCollection.addError("name", getText("admin.projectroles.duplicate.role.name.error", name));
            }
        }
        else
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
        }
        return roleNameUnique;
    }

    @Override
    public void deleteProjectRole(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        boolean internalError = false;

        if (projectRole == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.null.delete"));
            internalError = true;
        }

        if (!internalError && projectRole.getId() == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.null.id.delete"));
            internalError = true;
        }


        if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
            internalError = true;
        }

        if (!internalError)
        {
            // delete all the entries in the notification schemes that are a reference to this role
            try
            {
                notificationSchemeManager.removeEntities(PROJECTROLE_NOTIFICATION_TYPE, projectRole.getId().toString());
            }
            catch (RemoveException e)
            {
                log.error("Unable to remove notification scheme entites for project role: " + projectRole.getName());
            }
            // delete all the entries in the permission schemes that are a reference to this role
            try
            {
                permissionSchemeManager.removeEntities(PROJECTROLE_PERMISSION_TYPE, projectRole.getId().toString());
            }
            catch (RemoveException e)
            {
                log.error("Unable to remove permission scheme entites for project role: " + projectRole.getName());
            }

            try
            {
                issueSecuritySchemeManager.removeEntities(PROJECTROLE_ISSUE_SECURITY_TYPE, projectRole.getId().toString());
            }
            catch (RemoveException e)
            {
                log.error("Unable to remove issue security scheme entites for project role: " + projectRole.getName());
            }

            // clean up all SharePermissions for that role
            sharePermissionDeleteUtils.deleteRoleSharePermissions(projectRole.getId());

            projectRoleManager.deleteRole(projectRole);
            clearIssueSecurityLevelCache();
        }
    }

    @Override
    public void updateProjectRole(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        boolean internalError = false;

        if (projectRole == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.null.update"));
            internalError = true;
        }

        if (!internalError && projectRole.getId() == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.null.id.update"));
            internalError = true;
        }

        if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
            internalError = true;
        }

        if (!internalError)
        {
            // JRA-13157 - we need to let the update, update itself, sometimes users are changing the case of the
            // role name on a case-insensitive db so the name get will return a role, we just need to see if that role
            // is the one we are updating.
            ProjectRole roleByName = projectRoleManager.getProjectRole(projectRole.getName());
            if (roleByName != null && !roleByName.getId().equals(projectRole.getId()))
            {
                errorCollection.addErrorMessage(getText("admin.projectroles.duplicate.role.name.error", projectRole.getName()));
            }
            else
            {
                projectRoleManager.updateRole(projectRole);
            }
        }
    }

    @Override
    public ProjectRoleActors getProjectRoleActors(User currentUser, ProjectRole projectRole, Project project, ErrorCollection errorCollection)
    {
        boolean internalError = false;

        if (projectRole == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.actors.null.project.role"));
            internalError = true;
        }

        if (project == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.actors.null.project"));
            internalError = true;
        }

        ProjectRoleActors projectRoleActors = null;
        if (!internalError && hasProjectRolePermission(currentUser, project))
        {
            projectRoleActors = projectRoleManager.getProjectRoleActors(projectRole, project);
        }
        else
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.permission"));
        }
        return projectRoleActors;
    }

    @Override
    public void addActorsToProjectRole(User currentUser, Collection<String> actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection)
    {
        updateActorsToProjectRole(currentUser, actors, projectRole, project, actorType, errorCollection, true);
    }

    @Override
    public void removeActorsFromProjectRole(User currentUser, Collection<String> actors, ProjectRole projectRole, Project project, String actorType, ErrorCollection errorCollection)
    {
        if (hasProjectRolePermission(currentUser, project))
        {
            if (canRemoveCurrentUser(currentUser, actors, projectRole, project, actorType))
            {
                updateActorsToProjectRole(currentUser, actors, projectRole, project, actorType, errorCollection, false);
            }
            else
            {
                errorCollection.addErrorMessage(getText("project.roles.service.error.removeself.actor"));
            }
        }
        else
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.no.permission.to.remove"));
        }
    }

    @Override
    public void setActorsForProjectRole(User currentUser, Map<String, Set<String>> newRoleActors, ProjectRole projectRole, Project project, ErrorCollection errorCollection)
    {
        // Get all the project role actors for the projectRole and project context
        ProjectRoleActors projectRoleActors = getProjectRoleActors(currentUser, projectRole, project, errorCollection);

        // Permissions may have changed and we may already have errored out. Don't confuse the user with
        // more error messages than needed.
        if(errorCollection.hasAnyErrors())
        {
            return;
        }

        // Validate that we found the project role actors
        if (projectRoleActors == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.actors.null"));
            // We do not want to allow anything to get further than this so return with errors
            return;
        }

        // Validate that we have a non-null set of new role actors
        if (newRoleActors == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.new.role.actors.null"));
            // We do not want to allow anything to get further than this so return with errors
            return;
        }

        final Set<RoleActor> roleActors = projectRoleActors.getRoleActors();

        // First get the existing role actors into the same shape our new role actors are coming in
        // so we can iterate over them nicely
        final Map<String, Set<String>> existingRoleActors = Maps.newHashMap();
        for (final RoleActor roleActor : roleActors)
        {
            final String actorType = roleActor.getType();
            final String actorName = roleActor.getParameter();
            Set<String> actorNames = existingRoleActors.get(actorType);
            if(actorNames == null)
            {
                actorNames = Sets.newHashSet();
            }
            actorNames.add(actorName);
            existingRoleActors.put(actorType, actorNames);
        }

        final Set<String> allActorTypes = CollectionBuilder.<String>newBuilder()
                .addAll(existingRoleActors.keySet())
                .addAll(newRoleActors.keySet())
                .asSet();

        final Map<String, Set<String>> toDelete = Maps.newHashMap(existingRoleActors);
        final Map<String, Set<String>> toAdd = Maps.newHashMap(newRoleActors);

        // Iterate over all actor types and determine, for each actor type what actors are added and deleted.
        // Replace a set if we are modifying it as the maps contain a reference
        //
        // {actorsToDelete} = {existingActors} - {newActors}
        // {actorsToAdd} = {newActors} - {existingActors}
        for (final String actorType : allActorTypes)
        {
            final Set<String> newActors = newRoleActors.get(actorType);
            final Set<String> actorsToDelete = toDelete.get(actorType);

            if(newActors != null && actorsToDelete != null)
            {
                final HashSet<String> actorsToDeleteCopy = Sets.newHashSet(actorsToDelete);
                actorsToDeleteCopy.removeAll(newActors);
                toDelete.put(actorType, actorsToDeleteCopy);
            }

            final Set<String> existingActors = existingRoleActors.get(actorType);
            final Set<String> actorsToAdd = toAdd.get(actorType);

            if(existingActors != null && actorsToAdd != null)
            {
                final HashSet<String> actorsToAddCopy = Sets.newHashSet(actorsToAdd);
                actorsToAddCopy.removeAll(existingActors);
                toAdd.put(actorType, actorsToAddCopy);
            }
        }

        // Finally, rely on existing service methods for doing the actual adding and removing. Use the same
        // errorCollection to keep errors arising from both operations.
        for (final String actorType : allActorTypes)
        {
            final Set<String> actorNamesToAdd = toAdd.get(actorType);
            if(actorNamesToAdd != null && actorNamesToAdd.size() > 0)
            {
                addActorsToProjectRole(currentUser, actorNamesToAdd, projectRole, project, actorType, errorCollection);
            }
            final Set<String> actorNamesToDelete = toDelete.get(actorType);
            if(actorNamesToDelete != null && actorNamesToDelete.size() > 0)
            {
                removeActorsFromProjectRole(currentUser, actorNamesToDelete, projectRole, project, actorType, errorCollection);
            }
        }
    }

    /**
     * This method makes certain that the currentUser can not remove themselves from a project role if it means
     * that they will no longer be a roleMember by the action of the removal.
     * <p/>
     * Note this method is package-private for unit testing purposes.
     * @param currentUser the user making the call
     * @param actorType
     */
    boolean canRemoveCurrentUser(User currentUser, Collection<String> actors, ProjectRole projectRole, Project project, String actorType)
    {
        //JRA-12528: if we are a global admin, we're allowed to do anything.
        if (permissionManager.hasPermission(Permissions.ADMINISTER, currentUser))
        {
            return true;
        }
        // We only need to check if this is the last user reference in the project roles if the project role is in
        // use with the permission scheme in the "Administer Projects" permission.
        if (!doesProjectRoleExistForAdministerProjectsPermission(project, projectRole))
        {
            return true;
        }

        ProjectRoleActors projectRoleActors = projectRoleManager.getProjectRoleActors(projectRole, project);
        List<com.atlassian.crowd.embedded.api.User> allUsers = new ArrayList<com.atlassian.crowd.embedded.api.User>();
        int rolesRemovedWithMeInIt = 0;

        // Iterate through the current role actors
        for (RoleActor roleActorFromProjectRole : projectRoleActors.getRoleActors())
        {
            Collection<? extends User> roleActorFromProjectRoleUsers = roleActorFromProjectRole.getUsers();

            // Always store all the users that are referenced by all the actors currently contained in the db so
            // that we can find out how many times total the currentUser is included in the role.
            allUsers.addAll(roleActorFromProjectRoleUsers);

            // We want to keep track of the amount of times we run into an existing role actor that contains the current
            // user within the users Set that the actor represents
            if (roleActorsToRemoveContainsRoleActorFromProjectRole(roleActorFromProjectRole, actorType, currentUser, actors, projectRole, project))
            {
                rolesRemovedWithMeInIt++;
            }
        }

        // Now that we know how many times the currentUser is being removed via one of the roleActors in the actors
        // collection, we need to find out how many times, total, the currentUser is referenced in the conglomeration
        // of roleActor users
        int amountOfTimesIAmReferenced = getAmountOfTimesUsernameInList(allUsers, currentUser);

        // We will only allow the user to delete himself if they are referenced more times then the amount of times
        // they are removed by the roles being removed.
        return amountOfTimesIAmReferenced > rolesRemovedWithMeInIt;
    }

    /**
     * Check if the Project Role is in "Administer Projects" for the permission scheme that is associated with
     * the Project.
     * <p/>
     * Note this method is package-private for unit testing purposes.
     */
    boolean doesProjectRoleExistForAdministerProjectsPermission(Project project, ProjectRole projectRole)
    {
        if (permissionSchemeManager == null)
        {
            throw new NullPointerException("Instance of " + PermissionSchemeManager.class.getName() + " required.");
        }
        if (schemeFactory == null)
        {
            throw new NullPointerException("Instance of " + SchemeFactory.class.getName() + " required.");
        }
        if (project == null)
        {
            throw new NullPointerException("Instance of " + Project.class.getName() + " required.");
        }
        if (projectRole == null)
        {
            throw new NullPointerException("Instance of " + ProjectRole.class.getName() + " required.");
        }

        // We need to get a hold of the permission scheme that is associated with the project we are looking at
        List<GenericValue> schemesGvs;
        try
        {
            schemesGvs = permissionSchemeManager.getSchemes(project.getGenericValue());
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        // For each of the schemes we are going to look at all the entities for the project admin permission
        for (GenericValue schemeGv : schemesGvs)
        {
            Scheme scheme = schemeFactory.getSchemeWithEntitiesComparable(schemeGv);
            List<SchemeEntity> entitiesForProjectAdmin = scheme.getEntitiesByType(new Long(Permissions.PROJECT_ADMIN));

            // This should be all the scheme entities for the project admin permission
            for (SchemeEntity schemeEntity : entitiesForProjectAdmin)
            {
                // We want to determine if the current project role is referenced in the scheme entities for the
                // project admin permission in the current permission scheme
                boolean schemeEntityIsForProjectRole = (schemeEntity.getParameter() != null) && projectRole.getId().toString().equals(schemeEntity.getParameter());
                boolean schemeEntityIsOfTypeProjectRole = ProjectRoleSecurityAndNotificationType.PROJECT_ROLE.equals(schemeEntity.getType());
                if (schemeEntityIsOfTypeProjectRole && schemeEntityIsForProjectRole)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public DefaultRoleActors getDefaultRoleActors(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        boolean internalError = false;

        if (projectRole == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.actors.null.project.role"));
            internalError = true;
        }

        DefaultRoleActors defaultRoleActors = null;
        if (!internalError && hasAdminPermission(currentUser))
        {
            defaultRoleActors = projectRoleManager.getDefaultRoleActors(projectRole);
        }
        else if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
        }
        return defaultRoleActors;
    }

    @Override
    public void addDefaultActorsToProjectRole(User currentUser, Collection<String> actors, ProjectRole projectRole, String type, ErrorCollection errorCollection)
    {
        updateActorsToDefaultRole(currentUser, actors, projectRole, type, errorCollection, true);
    }

    @Override
    public void removeDefaultActorsFromProjectRole(User currentUser, Collection<String> actors, ProjectRole projectRole, String actorType, ErrorCollection errorCollection)
    {
        updateActorsToDefaultRole(currentUser, actors, projectRole, actorType, errorCollection, false);
    }

    @Override
    public void removeAllRoleActorsByNameAndType(User currentUser, String name, String type, ErrorCollection errorCollection)
    {
        ErrorCollection errors = validateRemoveAllRoleActorsByNameAndType(currentUser, name, type);
        if (errors.hasAnyErrors()) {
            errorCollection.addErrorCollection(errors);
        } else {
            removeAllRoleActorsByNameAndType(name, type);
        }
    }

    @Override
    public ErrorCollection validateRemoveAllRoleActorsByNameAndType(final User currentUser, final String name, final String type/*, final ErrorCollection errorCollection*/)
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();

        if (!TextUtils.stringSet(name))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.name.null.remove"));
        }

        if (!TextUtils.stringSet(type))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.type.null.remove"));
        }

        if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
        }
        return errorCollection;
    }

    @Override
    public void removeAllRoleActorsByNameAndType(final String name, final String type)
    {
        projectRoleManager.removeAllRoleActorsByNameAndType(name, type);
    }

    @Override
    public void removeAllRoleActorsByProject(User currentUser, Project project, ErrorCollection errorCollection)
    {
        boolean internalError = false;
        if (project == null || project.getId() == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.null"));
            internalError = true;
        }
        if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
            internalError = true;
        }

        if (!internalError)
        {
            projectRoleManager.removeAllRoleActorsByProject(project);
        }
    }

    @Override
    public Collection getAssociatedNotificationSchemes(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        boolean internalError = false;
        Collection schemes = new ArrayList();

        if (projectRole == null || projectRole.getId() == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.null"));
            internalError = true;
        }

        if (!internalError)
        {
            schemes = notificationSchemeManager.getSchemesContainingEntity(PROJECTROLE_NOTIFICATION_TYPE, projectRole.getId().toString());
        }
        return schemes;
    }

    @Override
    public Collection<GenericValue> getAssociatedIssueSecuritySchemes(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        boolean internalError = false;
        Collection<GenericValue> schemes = new ArrayList<GenericValue>();

        if (projectRole == null || projectRole.getId() == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.null"));
            internalError = true;
        }

        if (!internalError)
        {
            schemes = issueSecuritySchemeManager.getSchemesContainingEntity(PROJECTROLE_ISSUE_SECURITY_TYPE, projectRole.getId().toString());
        }
        return schemes;
    }

    @Override
    public Collection<GenericValue> getAssociatedPermissionSchemes(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {
        boolean internalError = false;
        Collection<GenericValue> schemes = new ArrayList<GenericValue>();

        if (projectRole == null || projectRole.getId() == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.null"));
            internalError = true;
        }

        if (!internalError)
        {
            schemes = permissionSchemeManager.getSchemesContainingEntity(PROJECTROLE_PERMISSION_TYPE, projectRole.getId().toString());
        }
        return schemes;
    }

    @Override
    public MultiMap getAssociatedWorkflows(User currentUser, ProjectRole projectRole, ErrorCollection errorCollection)
    {

        Collection<JiraWorkflow> workflows = workflowManager.getWorkflows();
        MultiMap associatedWorkflows = new MultiHashMap(workflows.size());
        for (JiraWorkflow jiraWorkflow : workflows)
        {
            Collection<ActionDescriptor> actions = jiraWorkflow.getAllActions();
            for (ActionDescriptor actionDescriptor : actions)
            {
                RestrictionDescriptor restriction = actionDescriptor.getRestriction();
                if (restriction != null)
                {
                    ConditionsDescriptor conditionsDescriptor = restriction.getConditionsDescriptor();
                    if (conditionsDescriptorContainsProjectRoleCondition(conditionsDescriptor, projectRole.getId()))
                    {
                        associatedWorkflows.put(jiraWorkflow, actionDescriptor);
                        // workflow matches, don't need to check the conditions on any more actions
                    }
                }
            }

        }
        return associatedWorkflows;
    }

    @Override
    public Collection<Project> getProjectsContainingRoleActorByNameAndType(User currentUser, String name, String type, ErrorCollection errorCollection)
    {
        boolean internalError = false;

        if (!TextUtils.stringSet(name))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.name.null.remove"));
            internalError = true;
        }

        if (!TextUtils.stringSet(type))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.type.null.remove"));
            internalError = true;
        }

        if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
            internalError = true;
        }
        if (internalError)
        {
            return Collections.emptyList();
        }

        Collection<Long> projectIds = projectRoleManager.getProjectIdsContainingRoleActorByNameAndType(name, type);
        if (projectIds == null)
        {
            return Collections.emptyList();
        }
        return projectManager.convertToProjectObjects(projectIds);
    }

    @Override
    public List<Long> roleActorOfTypeExistsForProjects(User currentUser, List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String projectRoleParameter, ErrorCollection errorCollection)
    {
        List<Long> projectsRoleActorExistsFor = new ArrayList<Long>();
        boolean internalError = false;

        if (projectsToLimitBy == null || projectsToLimitBy.isEmpty())
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.projects.to.limit.needed"));
            internalError = true;
        }

        if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
            internalError = true;
        }

        if (!internalError)
        {
            projectsRoleActorExistsFor.addAll(projectRoleManager.roleActorOfTypeExistsForProjects(projectsToLimitBy, projectRole, projectRoleType, projectRoleParameter));
        }
        return projectsRoleActorExistsFor;
    }

    @Override
    public Map<Long, List<String>> getProjectIdsForUserInGroupsBecauseOfRole(User currentUser, List<Long> projectsToLimitBy, ProjectRole projectRole, String projectRoleType, String userName, ErrorCollection errorCollection)
    {
        boolean internalError = false;

        if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
            internalError = true;
        }

        if (!internalError)
        {
            return projectRoleManager.getProjectIdsForUserInGroupsBecauseOfRole(projectsToLimitBy, projectRole, projectRoleType, userName);
        }
        return new HashMap<Long, List<String>>();
    }

    /**
     * Recursive depth-first search of ConditionsDescriptor tree for Conditions
     * that have an argument name that matches
     * {@link InProjectRoleCondition#KEY_PROJECT_ROLE_ID}. See
     * {@link ConditionsDescriptor} and {@link ConditionDescriptor}
     * (beware the Captain Insano Class naming technique).
     *
     * @param conditionsDescriptor the tree root to search in this invocation.
     * @param projectRoleId the role to search for
     * @return true only if the conditions descriptor tree refers to a matching Condition
     */
    private boolean conditionsDescriptorContainsProjectRoleCondition(ConditionsDescriptor conditionsDescriptor, Long projectRoleId)
    {
        for (Object o : conditionsDescriptor.getConditions())
        {
            if (o instanceof ConditionsDescriptor)
            {
                // recursive step
                if (conditionsDescriptorContainsProjectRoleCondition((ConditionsDescriptor) o, projectRoleId))
                {
                    // Found!
                    return true;
                }
            }
            else
            {
                // leaf
                ConditionDescriptor conditionDescriptor = (ConditionDescriptor) o;
                Map args = conditionDescriptor.getArgs();
                String foundProjectRoleId = (String) args.get(InProjectRoleCondition.KEY_PROJECT_ROLE_ID);
                if (foundProjectRoleId != null && foundProjectRoleId.equals(projectRoleId.toString()))
                {
                    // Found!
                    return true;
                }
            }
        }
        // Not Found :(
        return false;
    }


    private void updateActorsToProjectRole(User currentUser, Collection<String> actorNames, ProjectRole projectRole,
            Project project, String actorType, ErrorCollection errorCollection, boolean add)
    {
        // Get all the project role actors for the projectRole and project context
        ProjectRoleActors projectRoleActors = getProjectRoleActors(currentUser, projectRole, project, errorCollection);

        // Validate that we found the project role actors
        if (projectRoleActors == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.actors.null"));
            // We do not want to allow anything to get further than this so return with errors
            return;
        }

        List<RoleActor> actors = new ArrayList<RoleActor>();
        // Turn the actor name strings into a collection of RoleActor objects, populating them into the actors list
        boolean internalError = createRoleActors(actorNames, projectRole, project, actorType, projectRoleActors, errorCollection, actors, add);

        // If we have not run into an error in this method and the current user has permission to perform this operation,
        // then lets do it!
        final Project projectRoleProject = projectManager.getProjectObj(projectRoleActors.getProjectId());
        if (!internalError && hasProjectRolePermission(currentUser, projectRoleProject ) && actors.size() > 0)
        {
            // adding or removing the role actors
            if (add)
            {
                projectRoleActors = (ProjectRoleActors) projectRoleActors.addRoleActors(actors);
            }
            else
            {
                projectRoleActors = (ProjectRoleActors) projectRoleActors.removeRoleActors(actors);
            }
            projectRoleManager.updateProjectRoleActors(projectRoleActors);
            clearIssueSecurityLevelCache();
        }
        else if (!hasProjectRolePermission(currentUser, projectRoleProject))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.permission"));
        }
    }

    // A user will have permission to update if they are a JIRA admin or, if in enterprise, the user is a project admin
    @Override
    public boolean hasProjectRolePermission(User currentUser, Project project)
    {
        return hasAdminPermission(currentUser) || hasProjectAdminPermission(currentUser, project);
    }

    private boolean createRoleActors(Collection<String> actors, ProjectRole projectRole, Project project, String actorType, DefaultRoleActors roleActors, ErrorCollection errorCollection, List<RoleActor> actorsTo, boolean add)
    {
        boolean internalError = false;
        // Run through the actor names
        for (String actorName : actors)
        {
            ProjectRoleActor projectRoleActor;
            try
            {
                // create a role actor for the provided type, this can thrown an IllegalArgumentException if the
                // roleActor class is not able to resolve the actorName into something it understands

                final Long projectId = (project != null) ? project.getId() : null;
                final Long projectRoleId = (projectRole != null) ? projectRole.getId() : null;
                projectRoleActor = roleActorFactory.createRoleActor(null, projectRoleId, projectId, actorType, actorName);
                // We should only do a contains validation if we are adding, if we are removing then who cares?
                if (add && roleActors.getRoleActors().contains(projectRoleActor))
                {
                    errorCollection.addErrorMessage(getText("admin.user.role.actor.action.error.exists", actorName));
                    internalError = true;
                }
                else
                {
                    actorsTo.add(projectRoleActor);
                }
            }
            catch (RoleActorDoesNotExistException ex)
            {
                errorCollection.addErrorMessage(getText("admin.user.role.actor.action.error.invalid", actorName));
                internalError = true;
            }
        }
        return internalError;
    }

    private String getText(String key)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key);
    }

    private String getText(String key, String param)
    {
        return jiraAuthenticationContext.getI18nHelper().getText(key, param);
    }

    private boolean hasProjectAdminPermission(User currentUser, Project project)
    {
        return permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, currentUser);
    }

    private boolean hasAdminPermission(User currentUser)
    {
        return permissionManager.hasPermission(Permissions.ADMINISTER, currentUser);
    }

    private void updateActorsToDefaultRole(User currentUser, Collection<String> actorNames, ProjectRole projectRole, String actorType, ErrorCollection errorCollection, boolean add)
    {
        // Get all the project role actors for the projectRole and project context
        DefaultRoleActors defaultRoleActors = getDefaultRoleActors(currentUser, projectRole, errorCollection);

        // Validate that we found the project role actors
        if (defaultRoleActors == null)
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.project.role.actors.null"));
            // We do not want to allow anything to get further than this so return with errors
            return;
        }

        List<RoleActor> actors = new ArrayList<RoleActor>();
        // Turn the actor name strings into a collection of RoleActor objects, populating them into the actors list
        boolean internalError = createRoleActors(actorNames, projectRole, null, actorType, defaultRoleActors, errorCollection, actors, add);

        // If we have not run into an error in this method and the current user has permission to perform this operation,
        // then lets do it!
        if (!internalError && hasAdminPermission(currentUser) && actors.size() > 0)
        {
            // adding or removing the role actors
            if (add)
            {
                defaultRoleActors = defaultRoleActors.addRoleActors(actors);
            }
            else
            {
                defaultRoleActors = defaultRoleActors.removeRoleActors(actors);
            }
            projectRoleManager.updateDefaultRoleActors(defaultRoleActors);
        }
        else if (!hasAdminPermission(currentUser))
        {
            errorCollection.addErrorMessage(getText("project.roles.service.error.admin.permission"));
        }
    }

    private int getAmountOfTimesUsernameInList(Collection<com.atlassian.crowd.embedded.api.User> allUsers, com.atlassian.crowd.embedded.api.User currentUser)
    {
        int numberOfTimesIAmReferenced = 0;
        for (User user : allUsers)
        {
            if (user.getName().equals(currentUser.getName()))
            {
                numberOfTimesIAmReferenced++;
            }
        }
        return numberOfTimesIAmReferenced;
    }

    private boolean roleActorsToRemoveContainsRoleActorFromProjectRole(RoleActor roleActorFromProjectRole, String actorType, User currentUser, Collection<String> actors, ProjectRole projectRole, Project project)
    {
        // We want to keep track of the amount of times we run into an existing role actor that contains the current
        // user within the users Set that the actor represents
        if (roleActorFromProjectRole.getType().equals(actorType) && roleActorFromProjectRole.contains(currentUser))
        {
            for (String actorName : actors)
            {
                // Create a roleActor object from the actorName and type so we can compare the equality of the
                // actor to be removed with the roleActorFromProjectRole
                RoleActor roleActorToRemove = null;
                try
                {
                    roleActorToRemove = roleActorFactory.createRoleActor(null, projectRole.getId(), project.getId(), actorType, actorName);
                }
                catch (RoleActorDoesNotExistException e)
                {
                    throw new IllegalArgumentException("Unexpected error: the role actor '" + actorName + "' of type '" + actorType + "' does not exist.");
                }

                // If the role to be removed is the same as the role we are looking at then we keep a count of
                // it since this is a role that is being removed and the role includes the current user.
                if (roleActorToRemove.equals(roleActorFromProjectRole))
                {
                    return true;
                }
            }
        }
        return false;
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
