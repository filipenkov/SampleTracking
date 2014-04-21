package com.atlassian.jira.security.groups;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;

import java.util.Collection;

/**
 * This object can be used to manage groups in JIRA.
 * This manager offers the following advantages over using the Open Symphony UserManager class:
 * <ul>
 * <li>It is consistent with the general Service/Manager architecture of JIRA.</li>
 * <li>It provides a lightweight interface that may be mocked for unit tests.</li>
 * <li>It works around some of the bugs in OSUser - eg see JRA-12762.</li>
 * </ul>
 *
 * @since v3.13
 */
public interface GroupManager
{
    /**
     * Returns <code>true</code> if the given group name exists.
     *
     * @param groupName The group name.
     * @return <code>true</code> if the given group name exists.
     *
     * @since v3.13
     */
    boolean groupExists(String groupName);

    /**
     * Get all groups.
     *
     * @return Collection of all Groups.
     * @since v4.3
     */
    Collection<Group> getAllGroups();

    /**
     * Create a group with the given name.
     *
     * @param groupName The group name.
     * @return the newly created Group.
     * @throws com.atlassian.crowd.exception.InvalidGroupException if the group already exists in ANY associated directory or the group template does not have the required properties populated.
     * @throws com.atlassian.crowd.exception.OperationNotPermittedException if the directory has been configured to not allow the operation to be performed
     * @throws com.atlassian.crowd.exception.OperationFailedException underlying directory implementation failed to execute the operation.
     *
     * @since v4.3
     */
    Group createGroup(String groupName) throws OperationNotPermittedException, InvalidGroupException;

    /**
     * Returns the Group for this groupName, else null if no such Group exists.
     *
     * @param groupName The group name.
     * @return The Group for this groupName, else null if no such Group exists.
     * @deprecated Use {@link #getGroupObject(String groupName)} Since v4.3.
     */
    com.opensymphony.user.Group getGroup(String groupName);

    /**
     * Returns the Group for this groupName, else null if no such Group exists.
     *
     * @param groupName The group name.
     * @return The Group for this groupName, else null if no such Group exists.
     */
    Group getGroupObject(String groupName);

    /**
     * Returns {@code true} if the user is a member of the group.
     * <p>
     * Note that if the username or groupname is null, then it will return false.
     *
     * @param username user to inspect.
     * @param groupname group to inspect.
     * @return {@code true} if and only if the user is a direct or indirect (nested) member of the group.
     *
     * @see #isUserInGroup(com.atlassian.crowd.embedded.api.User, com.atlassian.crowd.embedded.api.Group)
     */
    public boolean isUserInGroup(final String username, final String groupname);

    /**
     * Returns {@code true} if the user is a member of the group.
     * <p>
     * Note that if the User or Group object is null, then it will return false.
     * This was done to retain consistency with the old OSUser behaviour of User.inGroup() and Group.containsUser() 
     *
     * @param user user to inspect.
     * @param group group to inspect.
     * @return {@code true} if and only if the user is a direct or indirect (nested) member of the group.
     *
     * @see #isUserInGroup(String, String)
     */
    boolean isUserInGroup(User user, Group group);

    /**
     * Returns {@code true} if the user is a member of the group.
     * <p>
     * Note that if the User or Group object is null, then it will return false.
     * This was done to retain consistency with the old OSUser behaviour of User.inGroup() and Group.containsUser()
     *
     * @param user user to inspect.
     * @param group group to inspect.
     * @return {@code true} if and only if the user is a direct or indirect (nested) member of the group.
     *
     * @deprecated Use {@link #isUserInGroup(com.atlassian.crowd.embedded.api.User, com.atlassian.crowd.embedded.api.Group)} instead. Since v4.4.
     */
    boolean isUserInGroup(com.opensymphony.user.User user, com.opensymphony.user.Group group);

    /**
     * Returns all the users in a group.
     * 
     * @param groupName The group
     * @return all the users that belongs to the group.
     * @since v4.3
     *
     * @see {@link #getUsersInGroup(Group)}
     */
    Collection<User> getUsersInGroup(String groupName);

    /**
     * Returns all the users in a group.
     * This will include nested group members.
     *
     * @param group The group
     * @return all the users that belongs to the group.
     * @since v4.3
     *
     * @see {@link #getUsersInGroup(String)}
     *
     * @throws NullPointerException if the group is null
     */
    Collection<User> getUsersInGroup(Group group);

    /**
     * Returns all the users that are direct members of the group.
     * This will NOT include nested group members.
     *
     * @param group The group
     * @return all the users that belongs to the group.
     * @since v4.3
     *
     * @see {@link #getUsersInGroup(String)}
     *
     * @throws NullPointerException if the group is null
     */
    Collection<User> getDirectUsersInGroup(Group group);

    /**
     * Returns all the groups that the given user belongs to.
     * @param userName The user
     * @return all the groups that the given user belongs to.
     * @since v4.3
     * @see #getGroupNamesForUser(String)
     */
    Collection<Group> getGroupsForUser(String userName);

    /**
     * Returns the names of all the groups that the given user belongs to.
     * @param userName The user
     * @return all the groups that the given user belongs to.
     * @since v4.3
     * @see #getGroupsForUser(String)
     */
    Collection<String> getGroupNamesForUser(String userName);

    /**
     * Adds a user as a member of a group.
     *
     * @param user  The user that will become a member of the group.
     * @param group The group that will gain a new member.
     * @throws UserNotFoundException  if the {@code user} could not be found
     * @throws GroupNotFoundException if the {@code group} could not be found
     * @throws OperationNotPermittedException if the directory has been configured to not allow the operation to be performed
     * @throws OperationFailedException If the underlying directory implementation failed to execute the operation.
     *
     * @since v4.3
     */
    void addUserToGroup(User user, Group group) throws GroupNotFoundException, UserNotFoundException, OperationNotPermittedException, OperationFailedException;
}
