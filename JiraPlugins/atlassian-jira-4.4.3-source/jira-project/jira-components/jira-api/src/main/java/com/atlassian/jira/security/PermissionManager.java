package com.atlassian.jira.security;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;

/**
 * Manager responsible for <a href="http://www.atlassian.com/software/jira/docs/latest/permissions.html">JIRA Permissions</a>.
 */
public interface PermissionManager extends JiraManager
{

    /**
     * Adds a permission to the system.
     *
     * @param permissionsId Permissions value. E.g. See {@link Permissions#ADMINISTER}
     * @param scheme        If null permission is global otherwise it is added to the scheme
     * @param parameter     Used for e.g. group name
     * @param securityType  e.g. GroupDropdown.DESC
     * @throws CreateException if permission creation fails
     */
    public void addPermission(int permissionsId, GenericValue scheme, String parameter, String securityType)
            throws CreateException;

    /**
     * Checks to see if this user has the specified permission. It will check only global permissions as there are
     * no other permissions to check.
     *
     * @param permissionsId permission id
     * @param user          user, can be null - anonymous user
     * @return true if user is granted given permission, false otherwise
     * @see com.atlassian.jira.security.GlobalPermissionManager#hasPermission(int, User)
     */
    public boolean hasPermission(int permissionsId, User user);

    /**
     * Checks to see if this user has the specified permission. It will check only global permissions as there are
     * no other permissions to check.
     *
     * @param permissionsId permission id
     * @param user          user, can be null - anonymous user
     * @return true if user is granted given permission, false otherwise
     * @see com.atlassian.jira.security.GlobalPermissionManager#hasPermission(int, User)
     * @deprecated Please use {@link #hasPermission(int, com.atlassian.crowd.embedded.api.User)}. Since v4.3
     */
    public boolean hasPermission(int permissionsId, com.opensymphony.user.User user);

    /**
     * Checks to see if this has permission to see the specified entity. Check Permissions scheme(s) if the entity
     * is project. Check Permissions scheme(s) and issue level security scheme(s) if the entity is an issue.
     *
     * @param permissionsId Not a global permission
     * @param entity        Not null.  Must be either an issue or project.
     * @param u             User object, possibly null if JIRA is accessed anonymously
     * @return True if there are sufficient rights to access the entity supplied
     * @throws IllegalArgumentException If the entity supplied is NOT an issue or project.
     * @deprecated use {@link #hasPermission(int, Issue, User)} for Issues or {@link #hasPermission(int, Project, User)}
     *             for Projects since JIRA 3.11
     */
    public boolean hasPermission(int permissionsId, GenericValue entity, User u);

    /**
     * Checks to see if this has permission to see the specified entity. Check Permissions scheme(s) if the entity
     * is project. Check Permissions scheme(s) and issue level security scheme(s) if the entity is an issue.
     *
     * @param permissionsId Not a global permission
     * @param entity        Not null.  Must be either an issue or project.
     * @param u             User object, possibly null if JIRA is accessed anonymously
     * @return True if there are sufficient rights to access the entity supplied
     * @throws IllegalArgumentException If the entity supplied is NOT an issue or project.
     * @deprecated use {@link #hasPermission(int, Issue, User)} for Issues or {@link #hasPermission(int, Project, User)}
     *             for Projects since JIRA 3.11
     */
    public boolean hasPermission(int permissionsId, GenericValue entity, com.opensymphony.user.User u);

    /**
     * Does the same as {@link #hasPermission(int,org.ofbiz.core.entity.GenericValue,User)} except
     * the entity is an issue.
     * <p/>
     * Note that if the issue's generic value is null, it is assumed that the issue is currently being created, and so
     * the permission check call is deferred to the issue's project object, with the issueCreation flag set to true. See
     * JRA-14788 for more info.
     *
     * @param permissionsId Not a global permission
     * @param entity        Not null.
     * @param u             User object, possibly null if JIRA is accessed anonymously
     * @return True if there are sufficient rights to access the entity supplied
     */
    public boolean hasPermission(int permissionsId, Issue entity, User u);

    /**
     * Does the same as {@link #hasPermission(int,org.ofbiz.core.entity.GenericValue,User)} except
     * the entity is an issue.
     * <p/>
     * Note that if the issue's generic value is null, it is assumed that the issue is currently being created, and so
     * the permission check call is deferred to the issue's project object, with the issueCreation flag set to true. See
     * JRA-14788 for more info.
     *
     * @param permissionsId Not a global permission
     * @param entity        Not null.
     * @param u             User object, possibly null if JIRA is accessed anonymously
     * @return True if there are sufficient rights to access the entity supplied
     * @deprecated Please use {@link #hasPermission(int, com.atlassian.jira.issue.Issue, com.atlassian.crowd.embedded.api.User)}. Since v4.3
     */
    public boolean hasPermission(int permissionsId, Issue entity, com.opensymphony.user.User u);

    /**
     * Checks whether the specified user has a specified permission within the context of a specified project.
     *
     * @param permissionsId A non-global permission, i.e. a permission that is granted via a project context
     * @param project       The project that is the context of the permission check.
     * @param user          The person to perform the permission check for
     * @return true if the user has the specified permission in the context of the supplied project
     */
    public boolean hasPermission(int permissionsId, Project project, User user);

    /**
     * Checks whether the specified user has a specified permission within the context of a specified project.
     *
     * @param permissionsId A non-global permission, i.e. a permission that is granted via a project context
     * @param project       The project that is the context of the permission check.
     * @param user          The person to perform the permission check for
     * @return true if the user has the specified permission in the context of the supplied project
     * @deprecated Please use {@link #hasPermission(int, com.atlassian.jira.project.Project, com.atlassian.crowd.embedded.api.User)}. Since v4.3
     */
    public boolean hasPermission(int permissionsId, Project project, com.opensymphony.user.User user);

    /**
     * Checks whether the specified user has a specified permission within the context of a specified project.
     *
     * @param permissionsId A non-global permission, i.e. a permission that is granted via a project context
     * @param project       The project that is the context of the permission check.
     * @param user          The person to perform the permission check for
     * @param issueCreation Whether this permission is being checked during issue creation
     * @return true if the user has the specified permission in the context of the supplied project
     */
    public boolean hasPermission(int permissionsId, Project project, User user, boolean issueCreation);

    /**
     * Checks whether the specified user has a specified permission within the context of a specified project.
     *
     * @param permissionsId A non-global permission, i.e. a permission that is granted via a project context
     * @param project       The project that is the context of the permission check.
     * @param user          The person to perform the permission check for
     * @param issueCreation Whether this permission is being checked during issue creation
     * @return true if the user has the specified permission in the context of the supplied project
     * @deprecated Please use {@link #hasPermission(int, com.atlassian.jira.project.Project, com.atlassian.crowd.embedded.api.User, boolean)}. Since v4.3
     */
    public boolean hasPermission(int permissionsId, Project project, com.opensymphony.user.User user, boolean issueCreation);

    /**
     * Does the same as {@link #hasPermission(int,org.ofbiz.core.entity.GenericValue,User)} except
     * the entity is a project {@link GenericValue}.
     *
     * @param permissionsId Not a global permission
     * @param project       Not null.
     * @param u             User object, possibly null if JIRA is accessed anonymously
     * @param issueCreation Whether this permission is being checked during issue creation
     * @return True if there are sufficient rights to access the entity supplied
     * @deprecated use {@link #hasPermission(int, Issue, User)} for Issues or
     *             {@link #hasPermission(int, Project, User, boolean)} for Projects since 3.11
     */
    public boolean hasPermission(int permissionsId, GenericValue project, User u, boolean issueCreation);

    /**
     * Does the same as {@link #hasPermission(int,org.ofbiz.core.entity.GenericValue,User)} except
     * the entity is a project {@link GenericValue}.
     *
     * @param permissionsId Not a global permission
     * @param project       Not null.
     * @param u             User object, possibly null if JIRA is accessed anonymously
     * @param issueCreation Whether this permission is being checked during issue creation
     * @return True if there are sufficient rights to access the entity supplied
     * @deprecated use {@link #hasPermission(int, Issue, User)} for Issues or
     *             {@link #hasPermission(int, Project, User, boolean)} for Projects since 3.11
     */
    public boolean hasPermission(int permissionsId, GenericValue project, com.opensymphony.user.User u, boolean issueCreation);

    /**
     * Remove all permissions that have used this group
     *
     * @param group The name of the group that needs to be removed, must NOT be null and must be a real group
     * @throws RemoveException if permission removal fails
     */
    public void removeGroupPermissions(String group) throws RemoveException;

    /**
     * Remove all permissions that have used this username
     *
     * @param username username of the user whose permissions are to be removed
     * @throws RemoveException if permission removal fails
     */
    public void removeUserPermissions(String username) throws RemoveException;

    /////////////// Project Permission Methods //////////////////////////////////////////

    /**
     * Can this user see at least one project with this permission
     *
     * @param permissionId must NOT be a global permission
     * @param user         user being checked
     * @return true the given user can see at least one project with the given permission, false otherwise
     */
    public boolean hasProjects(int permissionId, User user);

    /**
     * Can this user see at least one project with this permission
     *
     * @param permissionId must NOT be a global permission
     * @param user         user being checked
     * @return true the given user can see at least one project with the given permission, false otherwise
     * 
     * @deprecated Please use {@link #hasProjects(int, com.atlassian.crowd.embedded.api.User)}. Since v4.3
     */
    public boolean hasProjects(int permissionId, com.opensymphony.user.User user);

    /**
     * Retrieve a list of projects this user has the permission for
     *
     * @param permissionId must NOT be a global permission
     * @param user         user
     * @return a collection of {@link GenericValue} objects
     * @deprecated Please use {@link #getProjectObjects(int, com.atlassian.crowd.embedded.api.User)}. Since v4.3
     */
    public Collection<GenericValue> getProjects(int permissionId, User user);

    /**
     * Retrieve a list of projects this user has the permission for
     *
     * @param permissionId must NOT be a global permission
     * @param user         user
     * @return a collection of {@link GenericValue} objects
     * @deprecated Please use {@link #getProjectObjects(int, com.atlassian.crowd.embedded.api.User)}. Since v4.3
     */
    public Collection<GenericValue> getProjects(int permissionId, com.opensymphony.user.User user);

    /**
     * Retrieve a list of project objects this user has the permission for
     *
     * @param permissionId must NOT be a global permission
     * @param user user
     * @return a collection of {@link Project} objects
     */
    public Collection<Project> getProjectObjects(int permissionId, User user);

    /**
     * Retrieve a list of project objects this user has the permission for
     *
     * @param permissionId must NOT be a global permission
     * @param user user
     * @return a collection of {@link Project} objects
     * @deprecated Please use {@link #getProjectObjects(int, com.atlassian.crowd.embedded.api.User)}. Since v4.3
     */
    public Collection<Project> getProjectObjects(int permissionId, com.opensymphony.user.User user);

    /**
     * Retrieve a list of projects associated with the specified category, that this user has the permissions for
     *
     * @param permissionId permission id
     * @param user         user
     * @param category     GenericValue representing category
     * @return a collection of {@link GenericValue} objects
     */
    public Collection<GenericValue> getProjects(int permissionId, User user, GenericValue category);

    /**
     * Retrieve a list of projects associated with the specified category, that this user has the permissions for
     *
     * @param permissionId permission id
     * @param user         user
     * @param category     GenericValue representing category
     * @return a collection of {@link GenericValue} objects
     *
     * @deprecated Please use {@link #getProjects(int, com.atlassian.crowd.embedded.api.User, org.ofbiz.core.entity.GenericValue)}. Since v4.3
     */
    public Collection<GenericValue> getProjects(int permissionId, com.opensymphony.user.User user, GenericValue category);

    /////////////// Group Permission Methods //////////////////////////////////////////

    /**
     * Retrieve all groups that are used in the permission globally and in the project.
     *
     * @param permissionId permission id
     * @param project      project from which to retrieve groups
     * @return a collection of Groups
     */
    public Collection<Group> getAllGroups(int permissionId, Project project);

    /**
     * Retrieve all groups that are used in the permission globally and in the project entity
     *
     * @param permissionId permission id
     * @param project      project to retrieve groups from
     * @return a collection of Groups
     *
     * @deprecated Use {@link #getAllGroups(int, com.atlassian.jira.project.Project)} instead. Since v4.3
     */
    public Collection<com.opensymphony.user.Group> getAllGroups(int permissionId, GenericValue project);
}
