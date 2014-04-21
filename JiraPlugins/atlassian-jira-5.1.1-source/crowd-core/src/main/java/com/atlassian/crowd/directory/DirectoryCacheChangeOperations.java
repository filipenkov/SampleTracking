package com.atlassian.crowd.directory;

import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Individual methods that should be performed in transactions. No transactional integrity is
 * presumed or required between methods.
 */
interface DirectoryCacheChangeOperations
{
    // Operations mirrored from {@link DirectoryCache}.
    void deleteCachedUsersNotIn(List<? extends User> users, Date syncStartDate) throws OperationFailedException;
    void deleteCachedUsers(Set<String> usernames) throws OperationFailedException;
    void deleteCachedGroupsNotIn(GroupType groupType, List<? extends Group> ldapGroups, Date syncStartDate) throws OperationFailedException;
    void deleteCachedGroups(Set<String> groupnames) throws OperationFailedException;

    // For addOrUpdateCachedUsers
    AddUpdateSets<UserTemplateWithCredentialAndAttributes, UserTemplate> getUsersToAddAndUpdate(
            Collection<? extends User> remoteUsers, Date syncStartDate) throws OperationFailedException;
    void addUsers(Set<UserTemplateWithCredentialAndAttributes> usersToAdd) throws OperationFailedException;
    void updateUsers(Set<UserTemplate> usersToUpdate) throws OperationFailedException;
    
    // For addOrUpdateCachedGroups
    GroupsToAddUpdateReplace findGroupsToUpdate(Collection<? extends Group> remoteGroups, Date syncStartDate) throws OperationFailedException;
    void removeGroups(Collection<String> keySet) throws OperationFailedException;
    void addGroups(Set<GroupTemplate> allToAdd) throws OperationFailedException;
    void updateGroups(Collection<GroupTemplate> groupsToUpdate) throws OperationFailedException;

    boolean ignoreGroupOnSynchroniseMemberships(Group group) throws OperationFailedException;

    // For syncUserMembershipsForGroup
    AddRemoveSets<String> findUserMembershipForGroupChanges(Group group, Collection<String> remoteUsers) throws OperationFailedException;
    void addUserMembershipsForGroup(Group group, Set<String> toAdd) throws OperationFailedException;
    void removeUserMembershipsForGroup(Group group, Set<String> toRemove) throws OperationFailedException;

    // For syncGroupMembershipsForGroup
    AddRemoveSets<String> findGroupMembershipForGroupChanges(Group parentGroup, Collection<String> remoteGroups) throws OperationFailedException;
    void addGroupMembershipsForGroup(Group parentGroup, Collection<String> toAdd) throws OperationFailedException;
    void removeGroupMembershipsForGroup(Group parentGroup, Collection<String> toRemove) throws OperationFailedException;

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

    public static class AddUpdateSets<A, U>
    {
        private final Set<A> toAddSet;
        private final Set<U> toUpdateSet;

        public AddUpdateSets(Set<A> addSet, Set<U> updateSet)
        {
            toAddSet = addSet;
            toUpdateSet = updateSet;
        }

        public Set<A> getToAddSet()
        {
            return toAddSet;
        }

        public Set<U> getToUpdateSet()
        {
            return toUpdateSet;
        }
    }
    
    public static class GroupsToAddUpdateReplace
    {
        final Set<GroupTemplate> groupsToAdd, groupsToUpdate;
        final Map<String, GroupTemplate> groupsToReplace;

        GroupsToAddUpdateReplace(Set<GroupTemplate> groupsToAdd, Set<GroupTemplate> groupsToUpdate,
                Map<String, GroupTemplate> groupsToReplace)
        {
            this.groupsToAdd = groupsToAdd;
            this.groupsToUpdate = groupsToUpdate;
            this.groupsToReplace = groupsToReplace;
        }
    }
    
    public static class AddRemoveSets<T>
    {
        final Set<T> toAdd;
        final Set<T> toRemove;
        
        public AddRemoveSets(Set<T> toAdd, Set<T> toRemove)
        {
            this.toAdd = toAdd;
            this.toRemove = toRemove;
        }
    }
}
