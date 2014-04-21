package com.atlassian.jira.user.util;

import com.atlassian.annotations.ExperimentalApi;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.api.IncompatibleReturnType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.NotNull;
import com.atlassian.util.concurrent.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Simple user utilities that do not require an implementation with too many dependencies.
 *
 * @since v4.0
 */
public interface UserManager
{
    /**
     * Returns the total number of users defined in JIRA, regardless of whether they are active or not.
     *
     * @return the total number of users defined in JIRA
     * @since v4.0
     */
    int getTotalUserCount();

    /**
     * Returns all users defined in JIRA, regardless of whether they are active or not.
     * <p/>
     * <b>Warning:</b> previous incarnations of this method returned <code>com.opensymphony.user.User</code>. This class
     * has now been removed from the JIRA API, meaning that the 5.0 version is not binary or source compatible with
     * earlier versions.
     *
     * @return the set of all users
     * @since v4.0
     */
    @NotNull
    @IncompatibleReturnType (since = "5.0", was = "java.util.Set<com.opensymphony.user.User>")
    Set<User> getAllUsers();

    /**
     * Returns all users defined in JIRA, regardless of whether they are active or not.
     * <p/>
     * Legacy synonym for {@link #getAllUsers()}.
     *
     * @return the collection of all users
     * @since v4.3
     * @see #getAllUsers()
     */
    @NotNull
    Collection<User> getUsers();

    /**
     * Returns a {@link User} based on user name.
     * <p/>
     * <b>Warning:</b> previous incarnations of this method returned <code>com.opensymphony.user.User</code>. This class
     * has now been removed from the JIRA API, meaning that the 5.0 version is not binary or source compatible with
     * earlier versions.
     *
     * @param userName the user name of the user
     * @return the User object, or null if the user cannot be found including null userName.
     * @since v4.0
     */
    @IncompatibleReturnType (since = "5.0", was = "com.opensymphony.user.User")
    User getUser(final @Nullable String userName);

    /**
     * Returns a {@link User} based on user name.
     * <p>
     * Legacy synonym for {@link #getUser(String)}.
     *
     * @param userName the user name of the user
     * @return the User object, or null if the user cannot be found including null userName.
     * @since v4.3
     *
     * @see #getUser(String)
     */
    User getUserObject(final @Nullable String userName);

    /**
     * Returns an {@link ApplicationUser} based on user key.
     * <p>
     * If a null key is passed, then null is returned, but it is guaranteed to return a non-null ApplicationUser in all other cases.<br>
     * If the key is not null, but the user is not found then a proxy unknown immutable ApplicationUser object is returned.
     *
     * @param userKey the key of the user
     * @return the ApplicationUser object, or proxy unknown immutable ApplicationUser object (null iff the key is null).
     * @since v5.1.1
     */
    @ExperimentalApi
    ApplicationUser getUserByKey(final @Nullable String userKey);

    /**
     * Returns an {@link ApplicationUser} based on user name.
     * <p>
     * If a null username is passed, then null is returned, but it is guaranteed to return a non-null ApplicationUser in all other cases.<br>
     * If the username is not null, but the user is not found then a proxy unknown immutable ApplicationUser object is returned.
     *
     * @param userName the user name of the user
     * @return the ApplicationUser object, or proxy unknown immutable ApplicationUser object (null iff the username is null).
     * @since v5.1.1
     */
    @ExperimentalApi
    ApplicationUser getUserByName(final @Nullable String userName);

    /**
     * Returns a {@link User} based on user name and directoryId
     *
     * @param userName the user name of the user
     * @param directoryId the Directory to look in
     * @return the User object, or null if the user cannot be found including null userName.
     * @since v4.3.2
     */
    User findUserInDirectory(String userName, Long directoryId);

    /**
     * Returns a {@link User} based on user name.
     * <p>
     * If a null username is passed, then a null User object is returned, but it is guaranteed to return a non-null User in all other cases.<br>
     * If the username is not null, but the User is not found then a proxy unknown immutable User object is returned.
     *
     * @param userName the user name of the user
     * @return the User object, or proxy unknown immutable User object (null iff the username is null).
     * @since v4.3
     */
    User getUserEvenWhenUnknown(String userName);

    /**
     * Test if this user can be updated, i.e. is in a writable directory.
     * This relies upon the local directory configuration and does not guarantee that the actual remote directory, e.g. the
     * remote LDAP directory, will actually allow the user to be updated.
     * <p>
     * If the "External user management" setting is on, then you cannot update the user.
     *
     * @param user The user to update.
     * @return true if the user can be updated.
     */
    boolean canUpdateUser(User user);

    /**
     * Updates the {@link User}. The user must have non-null names and email address.
     *
     * @param user The user to update.
     *
     * @throws com.atlassian.crowd.exception.runtime.UserNotFoundException If the supplied user does not exist in the {@link User#getDirectoryId() directory}.
     * @throws com.atlassian.crowd.exception.runtime.OperationFailedException If the underlying directory implementation failed to execute the operation.
     *
     * @since v5.0
     */
    void updateUser(User user);

    /**
     * Test if this user's password can be updated, i.e. is in a writable directory
     * which is not a Delegated LDAP directory.
     * This relies upon the local directory configuration and does not guarantee that the actual remote directory, e.g. the
     * remote LDAP directory, will actually allow the user to be updated.
     * <p>
     * If the "External user management", or "External password management" setting is on, then you cannot update the password.
     *
     * @param user The user to update.
     * @return true if the user's password can be updated.
     */
    boolean canUpdateUserPassword(User user);

    /**
     * Test if this user's group membership can be updated, i.e. is in a writable directory or
     * a directory with Local Group support.
     * This relies upon the local directory configuration and does not guarantee that the actual remote directory, e.g. the
     * remote LDAP directory, will actually allow the user membership to be updated.
     *
     * @param user The user to update.
     * @return true if the user can be updated.
     */
    boolean canUpdateGroupMembershipForUser(User user);

    /**
     * Returns all groups defined in JIRA.
     * <p/>
     * <b>Warning:</b> previous incarnations of this method returned <code>com.opensymphony.user.User</code>. This class
     * has now been removed from the JIRA API, meaning that the 5.0 version is not binary or source compatible with
     * earlier versions.
     *
     * @return the set of all groups
     * @since v4.0
     */
    @IncompatibleReturnType (since = "5.0", was = "java.util.Set<com.opensymphony.user.Group>")
    Set<Group> getAllGroups();

    /**
     * Returns all groups defined in JIRA.
     * <p/>
     * Legacy synonym for {@link #getAllGroups()}.
     *
     * @return the set of all groups
     * @since v4.3
     * @see #getAllGroups()
     */
    Collection<Group> getGroups();

    /**
     * Returns a {@link Group} based on user name.
     * <p/>
     * <b>Warning:</b> previous incarnations of this method returned <code>com.opensymphony.user.User</code>. This class
     * has now been removed from the JIRA API, meaning that the 5.0 version is not binary or source compatible with
     * earlier versions.
     *
     * @param groupName the user name of the group
     * @return the Group object, or null if the user cannot be found including null groupName.
     * @since v4.0
     */
    @IncompatibleReturnType (since = "5.0", was = "com.opensymphony.user.User")
    Group getGroup(final @Nullable String groupName);

    /**
     * Returns a {@link Group} based on user name.
     * <p/>
     * Legacy synonym for {@link #getGroup(String)}.
     *
     * @param groupName the user name of the group
     * @return the Group object, or null if the user cannot be found including null groupName.
     * @since v4.3
     * @see #getGroup(String)
     */
    Group getGroupObject(final @Nullable String groupName);

    /**
     * Returns an ordered list of directories that have "read-write" permission.
     * ie those directories that we can add a user to.
     *
     * @return an ordered list of directories that have "read-write" permission.
     *
     * @see #hasWritableDirectory()
     */
    List<Directory> getWritableDirectories();

    /**
     * Returns true if at least one User Directory has "read-write" permission.
     * <p>
     * This is equivalent to:<br>
     *     <tt>&nbsp;&nbsp;getWritableDirectories().size() > 0</tt>
     *
     *
     * @return true if at least one User Directory has "read-write" permission.
     *
     * @see #getWritableDirectories()
     * @see #hasPasswordWritableDirectory()
     * @see #hasGroupWritableDirectory()
     */
    boolean hasWritableDirectory();

    /**
     * Returns true if any of the directories have permission to update user passwords, false if otherwise.
     * <p>
     * Note that this is not quite the same as {@link #hasWritableDirectory()} because of "Internal with LDAP Authentication" directories.
     * These directories are generally read-write but passwords are read-only.
     *
     * @return true if any of the directories have permission to update user passwords, false if otherwise.
     *
     * @see #hasWritableDirectory()
     */
    boolean hasPasswordWritableDirectory();

    /**
     * Returns true if any of the directories have permission to update groups.
     * <p>
     * Note that this will not always return the same results as {@link #hasWritableDirectory()} because you can set "Read-Only with Local Groups" to LDAP directories.
     * These directories are generally read-only but you can create local gropus and assign users to them.
     *
     * @return true if any of the directories have permission to update groups, false if otherwise.
     *
     * @see #hasWritableDirectory()
     */
    boolean hasGroupWritableDirectory();

    /**
     * Checks if the given directory is able to update user passwords.
     *
     * @param directory the Directory
     * @return true if the directory can update user passwords, false if otherwise.
     */
    boolean canDirectoryUpdateUserPassword(Directory directory);

    Directory getDirectory(Long directoryId);
}
