package com.atlassian.jira.security;

import com.atlassian.core.user.GroupUtils;
import com.atlassian.core.user.UserUtils;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.security.type.SingleUser;
import com.atlassian.jira.util.JiraEntityUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An abstract PermissionManager that implements a lot of the common functionality to all PermissionManagers.
 *
 * @see DefaultPermissionManager
 */
public abstract class AbstractPermissionManager implements PermissionManager
{

    /////////////// Add/Check/Remove Permissions ////////////////////////////////////////

    /**
     * Adds a permission to the system
     *
     * @param permissionsId Permissions value
     * @param scheme        If null permission is global otherwise it is added to the scheme
     * @param parameter     Used for e.g. group name
     * @param securityType  e.g. GroupDropdown.DESC
     * @throws CreateException
     */
    public void addPermission(final int permissionsId, final GenericValue scheme, final String parameter, final String securityType) throws CreateException
    {
        if (isGlobalPermission(permissionsId) && (scheme != null))
        {
            throw new IllegalArgumentException("Can not create a global permissions in a scheme");
        }
        if (scheme == null)
        {
            ManagerFactory.getGlobalPermissionManager().addPermission(permissionsId, parameter);
        }
        else
        {
            final SchemeEntity schemeEntity = new SchemeEntity(securityType, parameter, permissionsId);
            try
            {
                getPermissionSchemeManager().createSchemeEntity(scheme, schemeEntity);
            }
            catch (final GenericEntityException e)
            {
                throw new CreateException(e);
            }
        }
    }

    /**
     * Checks to see if this user has the specified permission<br/>
     * It will check only global permissions as there are no other permissions to check<br/>
     *
     * @param permissionsId permission id
     * @param user          user
     * @return true if user is granted given permission, false otherwise
     */
    public boolean hasPermission(final int permissionsId, final User user)
    {
        if (!isGlobalPermission(permissionsId))
        {
            throw new IllegalArgumentException("PermissionType passed to this function must be a global permission, " + permissionsId + " is not");
        }

        if (user == null)
        {
            return ManagerFactory.getGlobalPermissionManager().hasPermission(permissionsId);
        }
        else
        {
            return ManagerFactory.getGlobalPermissionManager().hasPermission(permissionsId, user);
        }
    }

    /**
     * Checks to see if this has permission to see the specified entity<br>
     * Check Permissions scheme(s) if the entity is project<br>
     * Check Permissions scheme(s) and issue level security scheme(s) if the entity is an issue<br>
     *
     * @param permissionsId  , not a global permission
     * @param projectOrIssue not null must be Project or Issue
     * @param u              User object, possibly null if JIRA is accessed anonymously
     */
    public boolean hasPermission(final int permissionsId, final GenericValue projectOrIssue, final User u)
    {
        return hasPermission(permissionsId, projectOrIssue, u, false);
    }

    /**
     * Returns a query string.
     * <p/>
     * The string looks like '123-anonymous' for null user and permission id of 123
     * or '123-fred' for user with username 'fred' and the permission id of 123.
     *
     * @param permissionsId permission id
     * @param user          user, can be null - anonymous user
     * @return query string, never null
     */
    private String createProfilerQuery(final int permissionsId, final User user)
    {
        return permissionsId + (user == null ? "-anonymous" : "-" + user.getName());
    }

    public boolean hasPermission(final int permissionsId, final Issue issue, final User u)
    {
        // JRA-14788: if generic value of issue object is null, need to defer permission check to project object.
        if (issue.getGenericValue() != null)
        {
            return hasPermission(permissionsId, issue.getGenericValue(), u);
        }
        else
        {
            return hasPermission(permissionsId, issue.getProjectObject(), u, true);
        }
    }

    public boolean hasPermission(final int permissionsId, final Project project, final User user)
    {
        return hasPermission(permissionsId, project, user, false);
    }

    public boolean hasPermission(final int permissionsId, final Project project, final User user, final boolean issueCreation)
    {
        if (isGlobalPermission(permissionsId))
        {
            throw new IllegalArgumentException(
                "PermissionType passed to this function must NOT be a global permission, " + permissionsId + " is global");
        }
        if ((project == null) || (project.getGenericValue() == null))
        {
            throw new IllegalArgumentException("The Project argument and its backing generic value must not be null");
        }

        return hasProjectPermission((long)permissionsId, project.getGenericValue(), user, issueCreation);
    }

    public boolean hasPermission(final int permissionsId, final GenericValue entity, final User u, final boolean issueCreation)
    {
        if (isGlobalPermission(permissionsId))
        {
            throw new IllegalArgumentException(
                "PermissionType passed to this function must NOT be a global permission, " + permissionsId + " is global");
        }
        if (entity == null)
        {
            throw new IllegalArgumentException("The entity passed must not be null");
        }

        //Convert permissionType to a Long as the scheme manager requires a long
        final Long permissionTypeLong = (long) permissionsId;

        if ("Project".equals(entity.getEntityName()))
        {
            return hasProjectPermission(permissionTypeLong, entity, u, issueCreation);
        }
        else if ("Issue".equals(entity.getEntityName()))
        {
            //Check that the user can actually see the project this issue is in
            if (!hasPermission(permissionsId, getProjectManager().getProject(entity), u))
            {
                return false;
            }

            // Check the project permissions apply to this issue
            if (!hasProjectPermission(permissionTypeLong, entity, u, issueCreation))
            {
                return false;
            }

            //If there is issue level security check that otherwise the must be able to see the issue
            final IssueSecuritySchemeManager manager = ManagerFactory.getIssueSecuritySchemeManager();
            if (u == null)
            {
                return manager.hasSchemeAuthority(entity.getLong("security"), entity);
            }
            else
            {
                return manager.hasSchemeAuthority(entity.getLong("security"), entity, u, issueCreation);
            }
        }
        else
        {
            throw new IllegalArgumentException("The entity passed must be a Project or an Issue not a " + entity.getEntityName());
        }
    }

    /**
     * Return true if the supplied user has the specified permission in the context of the supplied entity
     *
     * @param permissionTypeLong A non-global permission, i.e. a permission that is granted via a context
     * @param entity             The entity that is the context of the permission check.
     * @param user               The person to perform the permission check for
     * @param issueCreation      Whether this permission is being checked during issue creation (passed through the
     *                           {@link PermissionSchemeManager} to {@link com.atlassian.jira.scheme.SchemeType#hasPermission(
     *org.ofbiz.core.entity.GenericValue, String, User, boolean)})
     * @return true if the user has the specified permission in the context of the supplied entity
     */
    protected boolean hasProjectPermission(final Long permissionTypeLong, final GenericValue entity, final User user, final boolean issueCreation)
    {
        //Check scheme manager for the project to see it this project has the permission
        if (user == null)
        {
            return getPermissionSchemeManager().hasSchemeAuthority(permissionTypeLong, entity);
        }
        else
        {
            return getPermissionSchemeManager().hasSchemeAuthority(permissionTypeLong, entity, user, issueCreation);
        }
    }

    protected PermissionSchemeManager getPermissionSchemeManager()
    {
        return ManagerFactory.getPermissionSchemeManager();
    }

    /**
     * Remove all permissions that have used this group
     *
     * @param group The name of the group that needs to be removed, must NOT be null and must be a real group
     * @throws RemoveException
     */
    public void removeGroupPermissions(final String group) throws RemoveException
    {
        if (group == null)
        {
            throw new IllegalArgumentException("Group passed must NOT be null");
        }
        if (!GroupUtils.existsGroup(group))
        {
            throw new IllegalArgumentException("Group passed must exist");
        }

        ManagerFactory.getGlobalPermissionManager().removePermissions(group);
        getPermissionSchemeManager().removeEntities(GroupDropdown.DESC, group);

        //If there is issue level security check that otherwise the must be able to see the issue
        ManagerFactory.getIssueSecuritySchemeManager().removeEntities(GroupDropdown.DESC, group);
    }

    public void removeUserPermissions(final String username) throws RemoveException
    {
        if (username == null)
        {
            throw new IllegalArgumentException("Username passed must NOT be null");
        }
        if (!UserUtils.existsUser(username))
        {
            throw new IllegalArgumentException("User with '" + username + "' username passed must exist");
        }

        getPermissionSchemeManager().removeEntities(SingleUser.DESC, username);

        //If there is issue level security check that otherwise the must be able to see the issue
        ManagerFactory.getIssueSecuritySchemeManager().removeEntities(SingleUser.DESC, username);
    }

    /////////////// Project Permission Methods //////////////////////////////////////////

    /**
     * Can this user see at least one project with this permission
     *
     * @param permissionId must NOT be a global permission
     * @param user         user
     * @throws Exception
     */
    public boolean hasProjects(final int permissionId, final User user) 
    {
        if (isGlobalPermission(permissionId))
        {
            throw new IllegalArgumentException("Permission type passed must NOT be a global permission " + permissionId + " is global");
        }

        for (final GenericValue projectGV : getProjectManager().getProjects())
        {
            if (hasPermission(permissionId, projectGV, user))
            {
                //short circuit if we find at least one project that they can view
                return true;
            }
        }
        return false;
    }

    protected ProjectManager getProjectManager()
    {
        return ManagerFactory.getProjectManager();
    }

    
    public Collection<Project> getProjectObjects(final int permissionId, final User user)
    {
        if (isGlobalPermission(permissionId))
        {
            throw new IllegalArgumentException("Permission type passed must NOT be a global permission " + permissionId + " is global");
        }

        final Collection<Project> projects = getProjectManager().getProjectObjects();
        return getProjectObjectsWithPermission(projects, permissionId, user);
    }

    public Collection<GenericValue> getProjects(final int permissionId, final User user, final GenericValue category)
    {
        if (isGlobalPermission(permissionId))
        {
            throw new IllegalArgumentException("Permission type passed must NOT be a global permission, " + permissionId + " is global");
        }

        final Collection<GenericValue> projects;
        if (category == null)
        {
            projects = getProjectManager().getProjectsWithNoCategory();
        }
        else
        {
            projects = getProjectManager().getProjectsFromProjectCategory(category);
        }

        return getProjectsWithPermission(projects, permissionId, user);
    }

    private Collection<GenericValue> getProjectsWithPermission(final Collection<GenericValue> projects, final int permissionId, final User user)
    {
        final List<GenericValue> permissibleProjects = new ArrayList<GenericValue>();
        for (final GenericValue project : projects)
        {
            if (hasPermission(permissionId, project, user))
            {
                permissibleProjects.add(project);
            }
        }
        return permissibleProjects;
    }

    private Collection<Project> getProjectObjectsWithPermission(final Collection<Project> projects, final int permissionId, final User user)
    {
        final List<Project> permissibleProjects = new ArrayList<Project>();
        for (final Project project : projects)
        {
            if (hasPermission(permissionId, project, user))
            {
                permissibleProjects.add(project);
            }
        }
        return permissibleProjects;
    }

    protected boolean isGlobalPermission(final int permissionId)
    {
        return Permissions.isGlobalPermission(permissionId);
    }

    /////////////// Group Permission Methods //////////////////////////////////////////
    public Collection<Group> getAllGroups(int permissionId, Project project)
    {
        // get a set of the groups we're talking about
        final Set<Group> groups = new HashSet<Group>();
        groups.addAll(getPermissionSchemeManager().getGroups((long) permissionId, project.getGenericValue()));
        groups.addAll(ManagerFactory.getGlobalPermissionManager().getGroups(permissionId));
        return groups;
    }

    /**
     * Retrieve all groups that are used in the permission globally and in the project entity
     *
     * @param permissionId permission id
     * @param project      project to retireve groups from
     */
    public Collection<com.opensymphony.user.Group> getAllGroups(final int permissionId, final GenericValue project)
    {
        // get a set of the groups we're talking about
        final Set<com.opensymphony.user.Group> groups = new HashSet<com.opensymphony.user.Group>();
        groups.addAll(getPermissionSchemeManager().getGroups((long) permissionId, JiraEntityUtils.getProject(project)));
        groups.addAll(ManagerFactory.getGlobalPermissionManager().getGroups(permissionId));
        return groups;
    }

    public final boolean hasPermission(final int permissionsId, final com.opensymphony.user.User user)
    {
        return hasPermission(permissionsId, (User) user);
    }

    public final boolean hasPermission(final int permissionsId, final GenericValue entity, final com.opensymphony.user.User u)
    {
        return hasPermission(permissionsId, entity, (User) u);
    }

    public final boolean hasPermission(final int permissionsId, final Issue entity, final com.opensymphony.user.User u)
    {
        return hasPermission(permissionsId, entity, (User) u);
    }

    public final boolean hasPermission(final int permissionsId, final Project project, final com.opensymphony.user.User user)
    {
        return hasPermission(permissionsId, project, (User) user);
    }

    public final boolean hasPermission(final int permissionsId, final Project project, final com.opensymphony.user.User user, final boolean issueCreation)
    {
        return hasPermission(permissionsId, project, (User) user, issueCreation);
    }

    public final boolean hasPermission(final int permissionsId, final GenericValue project, final com.opensymphony.user.User u, final boolean issueCreation)
    {
        return hasPermission(permissionsId, project, (User) u, issueCreation);
    }

    public final boolean hasProjects(final int permissionId, final com.opensymphony.user.User user)
    {
        return hasProjects(permissionId, (User) user);
    }

    public Collection<GenericValue> getProjects(int permissionId, User user)
    {
        if (isGlobalPermission(permissionId))
        {
            throw new IllegalArgumentException("Permission type passed must NOT be a global permission " + permissionId + " is global");
        }

        final Collection<GenericValue> projects = getProjectManager().getProjects();
        return getProjectsWithPermission(projects, permissionId, user);
    }

    public Collection<GenericValue> getProjects(final int permissionId, final com.opensymphony.user.User user)
    {
        if (isGlobalPermission(permissionId))
        {
            throw new IllegalArgumentException("Permission type passed must NOT be a global permission " + permissionId + " is global");
        }

        final Collection<GenericValue> projects = getProjectManager().getProjects();
        return getProjectsWithPermission(projects, permissionId, user);
    }

    public final Collection<Project> getProjectObjects(final int permissionId, final com.opensymphony.user.User user)
    {
        return getProjectObjects(permissionId, (User) user);
    }

    public final Collection<GenericValue> getProjects(final int permissionId, final com.opensymphony.user.User user, final GenericValue category)
    {
        return getProjects(permissionId, (User) user, category);
    }
}
