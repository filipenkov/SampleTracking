package com.atlassian.crowd.directory;

import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.InternalDirectoryGroup;
import com.atlassian.crowd.model.user.TimestampedUser;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.util.BatchResult;

import java.util.Set;

/**
 * This interface represents a specialised extension of {@link RemoteDirectory} that is used by InternalDirectories.
 * <p>
 * In particular, the {@link #findUserByName(String)} and {@link #findGroupByName(String)} have been redefined to return
 * {@link TimestampedUser} and {@link InternalDirectoryGroup}. The {@link InternalDirectoryGroup} allows clients to
 * determine whether the group is "local".
 */
public interface InternalRemoteDirectory extends RemoteDirectory
{
    /**
     * @return {@link TimestampedUser} entity.
     */
    TimestampedUser findUserByName(String name) throws UserNotFoundException;

    /**
     * @return {@link InternalDirectoryGroup} entity.
     */
    InternalDirectoryGroup findGroupByName(String name) throws GroupNotFoundException;

    /**
     * Adds a "local" group to the directory.
     *
     * This method can be used to store groups that aren't clones of
     * "external" groups. For example, if an LDAP directory is cloned
     * in an internal directory, it's possible to define "local" groups
     * that exist internally but not in LDAP.
     *
     * This functionality was added to meet the functionality that Confluence
     * provided.
     *
     * @param group template of the group to add.
     * @return the added group retrieved from the underlying store.
     * @throws InvalidGroupException The supplied group is invalid.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    Group addLocalGroup(final GroupTemplate group) throws InvalidGroupException, OperationFailedException;

    /**
     * Adds a collection of users to the directory.
     * 
     * The bulk adding of users may be significantly faster than adding 
     * users one-by-one for large collections.
     *
     * Caller must ensure that the users don't already exist.
     * 
     * @param users templates of users to add.
     * @return result containing both successful and failed users
     * @throws IllegalArgumentException if any of the users' directory ID does not match the directory's ID.
     */
    BatchResult<User> addAllUsers(final Set<UserTemplateWithCredentialAndAttributes> users);

    /**
     * Adds a collection of groups to the directory.
     * 
     * The bulk adding of groups may be significantly faster than adding 
     * groups one-by-one for large collections.
     *
     * Caller must ensure that the users don't already exist.
     * 
     * @param groups templates of groups to add.
     * @return result containing both successful and failed groups
     * @throws IllegalArgumentException if any of the groups' directory ID does not match the directory's ID.
     */
    BatchResult<Group> addAllGroups(final Set<GroupTemplate> groups);

    /**
     * Adds a collection of users to a group.
     *
     * Caller must ensure that the memberships don't already exist.
     *
     * @param userNames names of users to add to group.
     * @param groupName name of group to add users to.
     * @return result containing both successful and failed users
     * @throws GroupNotFoundException group with supplied {@code groupName} does not exist.
     */
    BatchResult<String> addAllUsersToGroup(Set<String> userNames, String groupName) throws GroupNotFoundException;

    /**
     * Removes all users from the directory.
     *
     * If a user with the supplied username does not exist in the directory, the username will be ignored.
     *
     * @param usernames usernames of users to remove.
     */
    void removeAllUsers(Set<String> usernames);

    /**
     * Removes all groups from the directory.
     *
     * If a group with the supplied group name does not exist in the directory, the group name will be ignored.
     *
     * @param groupNames names of groups to remove.
     */
    void removeAllGroups(Set<String> groupNames);

}
