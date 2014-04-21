package com.atlassian.jira.user.util;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
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
     *
     * @return the set of all users
     * @since v4.0
     * @deprecated Use {@link #getUsers()}. Since v4.3
     */
    @NotNull
    Set<com.opensymphony.user.User> getAllUsers();

    /**
     * Returns all users defined in JIRA, regardless of whether they are active or not.
     *
     * @return the collection of all users
     * @since v4.3
     */
    @NotNull
    Collection<User> getUsers();

    /**
     * Returns a {@link com.opensymphony.user.User} based on user name.
     *
     * @param userName the user name of the user
     * @return the User object, or null if the user cannot be found including null userName.
     * @since v4.0
     * @deprecated Since v4.3. Use {@link #getUserObject(String)}.
     */
    com.opensymphony.user.User getUser(final @Nullable String userName);

    /**
     * Returns a {@link User} based on user name.
     *
     * @param userName the user name of the user
     * @return the User object, or null if the user cannot be found including null userName.
     * @since v4.3
     */
    User getUserObject(final @Nullable String userName);

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
     *
     * @return the set of all groups
     * @since v4.0
     * @deprecated Use {@link #getGroups}. Since v4.3
     */
    Set<com.opensymphony.user.Group> getAllGroups();

    /**
     * Returns all groups defined in JIRA.
     *
     * @return the set of all groups
     * @since v4.3
     */
    Collection<Group> getGroups();

    /**
     * Returns a {@link com.opensymphony.user.Group} based on user name.
     *
     * @param groupName the user name of the group
     * @return the Group object, or null if the user cannot be found including null groupName.
     * @since v4.0
     * @deprecated Since v4.3. Use {@link #getGroupObject(String)}.

     */
    com.opensymphony.user.Group getGroup(final @Nullable String groupName);

    /**
     * Returns a {@link Group} based on user name.
     * <p>
     *  <b>WARNING</b>:  This method will be changed to return a {@link com.atlassian.crowd.embedded.api.Group}. Since v4.3
     *
     * @param groupName the user name of the group
     * @return the Group object, or null if the user cannot be found including null groupName.
     * @since v4.3
     */
    Group getGroupObject(final @Nullable String groupName);

    /**
     * Returns an ordered list of directories that have "read-write" permission.
     * ie those directories that we can add a user to.
     *
     * @return an ordered list of directories that have "read-write" permission.
     */
    List<Directory> getWritableDirectories();

    /**
     * Returns true if any of the directories have permission to update user passwords, false if otherwise.
     *
     * @return true if any of the directories have permission to update user passwords, false if otherwise.
     */
    boolean hasPasswordWritableDirectory();

    /**
     * Checks if the given directory is able to update user passwords.
     *
     * @param directory
     * @return true if the directory can update user passwords, false if otherwise.
     */
    boolean canDirectoryUpdateUserPassword(Directory directory);

    Directory getDirectory(Long directoryId);
}
