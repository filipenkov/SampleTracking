package com.atlassian.jira.security;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;

import java.util.Collection;

/**
 * Use this manager to add/remove or check the following global permissions.
 * <p>
 * Global permissions are:
 * <ul>
 * <li>{@link Permissions#SYSTEM_ADMIN}</li>
 * <li>{@link Permissions#ADMINISTER}</li>
 * <li>{@link Permissions#USE}</li>
 * <li>{@link Permissions#USER_PICKER}</li>
 * <li>{@link Permissions#CREATE_SHARED_OBJECTS}</li>
 * <li>{@link Permissions#MANAGE_GROUP_FILTER_SUBSCRIPTIONS}</li>
 * <li>{@link Permissions#BULK_CHANGE}</li>
 * </ul>
 *
 * For all other project specific permissions use {@link PermissionManager}.
 * <p>
 * To check programmatically if a permission is global use {@link Permissions#isGlobalPermission(int)}.
 */
public interface GlobalPermissionManager
{
    /**
     * Grants a user group a global permission.
     *
     * @param permissionType the global permission.
     * @param group          the name of the group. Null means "anyone" group.
     *                       The JIRA use permission cannot be granted to anyone.
     * @return true if the permission was added
     *
     * @throws CreateException if the permission creation fails
     */
    boolean addPermission(int permissionType, String group) throws CreateException;

    /**
     * Retrieve a list of user groups which have been granted a specified permission.
     * The returned {@link JiraPermission} contains a reference to the user group.
     *
     * {@link JiraPermission#getScheme()} is always NULL, because Global permission are not configured using schemes.
     * {@link JiraPermission#getType()} will always return "group", because global permissions can only be granted to groups.
     *
     * @param permissionType The permission. Must be a global permission.
     * @return Collection of {@link JiraPermission#getPermType}, must never return null.
     */
    Collection<JiraPermission> getPermissions(int permissionType);

    /**
     * Revokes a global permission for a user group
     *
     * @param permissionType  the global permission.
     * @param group           the group name. NULL means the anyone group.
     *
     * @return true if the permission was revoked, false if not (e.g. the group does not have this permission)
     *
     * @throws RemoveException if the permission removal fails
     */
    boolean removePermission(int permissionType, String group) throws RemoveException;

    /**
     * Revoke all global permissions for a user group.
     *
     * @param group cannot NOT be null and the group must exist.
     *
     * @return true, if this group does not have any global permissions
     *
     * @throws RemoveException if the permission removal fails
     */
    boolean removePermissions(String group) throws RemoveException;

    /**
     * Check if a global permission is granted for an anonymous user.
     * <p/>
     * If the permission is {@link Permissions#ADMINISTER} and the lookup is false then the same
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
     * Retrieve all the groups with this permission. Only groups directly associated with the permission will be
     * returned.
     *
     * @param permissionId must be a global permission
     * @return a Collection of {@link Group}'s, will never be null.
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
