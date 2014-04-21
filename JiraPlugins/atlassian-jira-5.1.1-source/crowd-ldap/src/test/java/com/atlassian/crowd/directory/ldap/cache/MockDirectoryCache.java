package com.atlassian.crowd.directory.ldap.cache;

import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.user.User;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockDirectoryCache implements DirectoryCache
{
    private final Map<String, User> userMap = new HashMap<String, User>();
    private final Map<String, Group> groupMap = new HashMap<String, Group>();
    private final Map<String, Group> roleMap = new HashMap<String, Group>();

    public void addOrUpdateCachedUsers(List<? extends User> users, Date syncStartDate) throws OperationFailedException
    {
        for (User user : users)
            addOrUpdateCachedUser(user);
    }

    public void deleteCachedUsersNotIn(final List<? extends User> users, final Date syncStartDate)
            throws OperationFailedException
    {
        userMap.clear();
        for (User user : users)
        {
            addOrUpdateCachedUser(user);
        }
    }

    public void deleteCachedUsers(final Set<String> usernames) throws OperationFailedException
    {
        for (String username : usernames)
            deleteCachedUser(username);
    }

    public void deleteCachedGroups(Set<String> groupnames) throws OperationFailedException
    {
        for (String groupname : groupnames)
            deleteCachedGroup(groupname);
    }

    public void addOrUpdateCachedGroups(final List<? extends Group> groups, final Date syncStartDate)
            throws OperationFailedException
    {
        for (Group group : groups)
        {
            addOrUpdateCachedGroup(group);
        }
    }

    public void deleteCachedGroupsNotIn(GroupType groupType, final List<? extends Group> ldapGroups, final Date syncStartDate)
            throws OperationFailedException
    {
        if (groupType == GroupType.GROUP)
        {
            groupMap.clear();
        }
        else
        {
            roleMap.clear();
        }
        addOrUpdateCachedGroups(ldapGroups, null);
    }

    public void deleteCachedGroups(List<String> groupnames) throws OperationFailedException
    {
        for (String groupname : groupnames)
            deleteCachedGroup(groupname);
    }

    public Collection<User> getUsers()
    {
        return userMap.values();
    }

    public Collection<Group> getGroups()
    {
        return groupMap.values();
    }

    public Collection<Group> getRoles()
    {
        return roleMap.values();
    }

    public void syncUserMembersForGroup(final Group ldapGroup, final Collection<String> users)
            throws OperationFailedException
    {
        // Not implemented yet
    }

    public void syncGroupMembersForGroup(final Group ldapGroup, final Collection<String> groups)
            throws OperationFailedException
    {
        // Not implemented yet
    }

    public void addOrUpdateCachedUser(User user) throws OperationFailedException
    {
        userMap.put(user.getName(), user);
    }

    public void deleteCachedUser(final String username) throws OperationFailedException
    {
        userMap.remove(username);
    }

    public void addOrUpdateCachedGroup(Group group) throws OperationFailedException
    {
        if (group.getType() == GroupType.GROUP)
        {
            groupMap.put(group.getName(), group);
        }
        else
        {
            roleMap.put(group.getName(), group);
        }
    }

    public void deleteCachedGroup(final String groupname) throws OperationFailedException
    {
        groupMap.remove(groupname);
    }

    public void addUserToGroup(String username, String groupName) throws OperationFailedException
    {
        // Not implemented yet
    }

    public void removeUserFromGroup(String username, String groupName) throws OperationFailedException
    {
        // Not implemented yet
    }

    public void addGroupToGroup(String childGroup, String parentGroup) throws OperationFailedException
    {
        // Not implemented yet
    }

    public void removeGroupFromGroup(String childGroup, String parentGroup) throws OperationFailedException
    {
        // Not implemented yet
    }

    public void syncGroupMembershipsForUser(String childUsername, Set<String> parentGroupNames) throws OperationFailedException
    {
        // Not implemented yet
    }

    public void syncGroupMembershipsAndMembersForGroup(String groupName, Set<String> parentGroupNames, Set<String> childGroupNames) throws OperationFailedException
    {
        // Not implemented yet
    }

    public Group getGroup(String name)
    {
        return groupMap.get(name);
    }

    public Group getRole(String name)
    {
        return roleMap.get(name);
    }
}
