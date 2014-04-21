package com.atlassian.jira.security;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;

import java.util.Collection;

public interface GlobalPermissionManager
{
    /**
     * Adds a global permission
     *
     * @param permissionType must be a global permission type
     * @param group          can be null if it is anyone permission
     * @return True if the permission was added
     * @throws CreateException if the permission creation fails
     */
    boolean addPermission(int permissionType, String group) throws CreateException;

    /**
     * Retrieve all the global permissions of a particular type
     *
     * @param permissionType must be a global permission
     * @return Collection of {@link JiraPermission}, must never return null
     */
    Collection<JiraPermission> getPermissions(int permissionType);

    /**
     * Removes a global permission
     *
     * @param permissionType must be a global permission type
     * @param group          can be null if it is anyone permission
     * @return True if the permission was removed, false if not (usually it didn't exist)
     * @throws RemoveException if the permission removal fails
     */
    boolean removePermission(int permissionType, String group) throws RemoveException;

    /**
     * Remove a global permissions that the group passed in
     *
     * @param group must NOT be null and the group must exist
     * @return True all the permissions are removed
     * @throws RemoveException if the permission removal fails
     */
    boolean removePermissions(String group) throws RemoveException;

    /**
     * Check if a global permission is granted for an Anonymous user.
     * <p/>
     * If the permission type is {@link Permissions#ADMINISTER} and the lookup is false then the same
     * query will be executed for the {@link Permissions#SYSTEM_ADMIN} permission type, since
     * it is implied that having a {@link Permissions#SYSTEM_ADMIN} permission grants
     * {@link Permissions#ADMINISTER} rights.
     * <p/>
     * Note: Use {@link #hasPermission(int, User)} method is you have the user object,
     * i.e. user is not anonymous.
     * <p/>
     * <b>If you are using this method directly, consider using
     * {@link com.atlassian.jira.security.PermissionManager#hasPermission(int, User)}
     * instead as it handles logged in and anonymous users as well.</b>
     *
     * @param permissionType must be global permission
     * @return true the anonymous user has the permission of given type, false otherwise
     * @see #hasPermission(int, User)
     */
    boolean hasPermission(int permissionType);

    /**
     * Check if a global permission for one of the users groups exists.
     * <p/>
     * If the permission type is {@link Permissions#ADMINISTER} and the lookup is false then the same
     * query will be executed for the {@link Permissions#SYSTEM_ADMIN} permission type, since
     * it is implied that having a {@link Permissions#SYSTEM_ADMIN} permission grants
     * {@link Permissions#ADMINISTER} rights.
     * <p/>
     * <b>Note:</b> Use {@link #hasPermission(int)} method is you do not have the user object, i.e. user is anonymous.
     * <p/>
     * <b>If you are using this method directly, consider using
     * {@link com.atlassian.jira.security.PermissionManager#hasPermission(int, User)}
     * instead as it handles logged in and anonymous users as well.</b>
     *
     * @param permissionType must be a global permission
     * @param u              must not be null
     * @return true if the given user has the permission of given type, otherwise false
     * @see #hasPermission(int)
     * @see com.atlassian.jira.security.PermissionManager#hasPermission(int, User)
     */
    boolean hasPermission(int permissionType, User u);

    /**
     * Check if a global permission for one of the users groups exists.
     * <p/>
     * If the permission type is {@link Permissions#ADMINISTER} and the lookup is false then the same
     * query will be executed for the {@link Permissions#SYSTEM_ADMIN} permission type, since
     * it is implied that having a {@link Permissions#SYSTEM_ADMIN} permission grants
     * {@link Permissions#ADMINISTER} rights.
     * <p/>
     * <b>Note:</b> Use {@link #hasPermission(int)} method is you do not have the user object, i.e. user is anonymous.
     * <p/>
     * <b>If you are using this method directly, consider using
     * {@link com.atlassian.jira.security.PermissionManager#hasPermission(int, User)}
     * instead as it handles logged in and anonymous users as well.</b>
     *
     * @param permissionType must be a global permission
     * @param u              must not be null
     * @return true if the given user has the permission of given type, otherwise false
     * @see #hasPermission(int)
     * @see com.atlassian.jira.security.PermissionManager#hasPermission(int, User)
     * @deprecated Please use {@link #hasPermission(int, com.atlassian.crowd.embedded.api.User)}. Since v4.3
     */
    boolean hasPermission(int permissionType, com.opensymphony.user.User u);

    /**
     * Retrieve all the groups with this permission. Only groups directly associated with the permission will be
     * returned.
     *
     * @param permissionId must be a global permission
     * @return a Collection of {@link com.opensymphony.user.Group}'s, will never be null.
     * @deprecated Use {@link #getGroupsWithPermission(int)}. Since 4.3 
     */
    Collection<com.opensymphony.user.Group> getGroups(int permissionId);

    /**
     * Retrieve all the groups with this permission. Only groups directly associated with the permission will be
     * returned.
     *
     * @param permissionId must be a global permission
     * @return a Collection of {@link com.opensymphony.user.Group}'s, will never be null.
     */
    Collection<Group> getGroupsWithPermission(int permissionId);

    /**
     * Retrieve all the group names with this permission. Only group names directly associated with the permission will
     * be returned.
     *
     * @param permissionId must be a global permission
     * @return a Collection of String, group names, will never be null.
     */
    Collection<String> getGroupNames(int permissionId);
}
