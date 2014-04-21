package com.atlassian.crowd.directory.ldap.cache;

import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.user.User;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * A cache of users, groups and memberships for an external Directory.
 *
 * The {@link CacheRefresher} passes the updated data to this interface for caching.
 *
 * @see UsnChangedCacheRefresher
 */
public interface DirectoryCache
{
    // -----------------------------------------------------------------------------------------------------------------
    // Batch operations
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Synchronises a list of Users from an external directory in the local cache.
     * <p>
     * If the syncStartDate is not null, then it is used to ensure we don't re-introduce stale data if a User is updated
     * locally after we did our search.
     * Some callers may intentionally choose to pass null - eg  when {@link com.atlassian.crowd.directory.ldap.cache.UsnChangedCacheRefresher} does a partial synchronise.
     *
     * @param users A list of Users from the external directory.
     * @param syncStartDate The date that the synchronise started (can be null).
     * @throws com.atlassian.crowd.exception.OperationFailedException If the Internal Directory throws a OperationFailedException - which seems unlikely.
     */
    void addOrUpdateCachedUsers(List<? extends User> users, Date syncStartDate) throws OperationFailedException;

    void deleteCachedUsersNotIn(List<? extends User> users, Date syncStartDate) throws OperationFailedException;
    void deleteCachedUsers(Set<String> usernames) throws OperationFailedException;

    void addOrUpdateCachedGroups(List<? extends Group> groups, Date syncStartDate) throws OperationFailedException;

    void deleteCachedGroupsNotIn(GroupType groupType, List<? extends Group> ldapGroups, Date syncStartDate) throws OperationFailedException;
    void deleteCachedGroups(Set<String> groupnames) throws OperationFailedException;

    void syncUserMembersForGroup(Group ldapGroup, Collection<String> remoteUsers) throws OperationFailedException;
    void syncGroupMembersForGroup(Group ldapGroup, Collection<String> groups) throws OperationFailedException;

    // -----------------------------------------------------------------------------------------------------------------
    // Event operations
    // -----------------------------------------------------------------------------------------------------------------

    void addOrUpdateCachedUser(User user) throws OperationFailedException;
    void deleteCachedUser(String username) throws OperationFailedException;

    void addOrUpdateCachedGroup(Group group) throws OperationFailedException;
    void deleteCachedGroup(String groupName) throws OperationFailedException;

    void addUserToGroup(String username, String groupName) throws OperationFailedException;
    void removeUserFromGroup(String username, String groupName) throws OperationFailedException;
    void addGroupToGroup(String childGroup, String parentGroup) throws OperationFailedException;
    void removeGroupFromGroup(String childGroup, String parentGroup) throws OperationFailedException;

    void syncGroupMembershipsForUser(String childUsername, Set<String> parentGroupNames) throws OperationFailedException;
    void syncGroupMembershipsAndMembersForGroup(String groupName, Set<String> parentGroupNames, Set<String> childGroupNames) throws OperationFailedException;
}
