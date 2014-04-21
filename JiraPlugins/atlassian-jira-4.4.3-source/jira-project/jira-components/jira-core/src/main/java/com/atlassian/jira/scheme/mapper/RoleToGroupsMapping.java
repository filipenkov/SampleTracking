package com.atlassian.jira.scheme.mapper;

import com.atlassian.jira.security.roles.ProjectRole;
import com.opensymphony.user.Group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Represents a mapping between many or one group/s and a {@link com.atlassian.jira.security.roles.ProjectRole}. This
 * object also has some convenience methods that allow us to find the {@link java.util.Set} of users that will be
 * unwrapped into the project role as part of the mapping.
 */
public class RoleToGroupsMapping
{
    private GroupToRoleMapping groupToRoleMapping;
    private Set mappedGroup;

    public RoleToGroupsMapping(GroupToRoleMapping groupToRoleMapping)
    {
        this.groupToRoleMapping = groupToRoleMapping;
        this.mappedGroup = new HashSet();
    }

    public ProjectRole getProjectRole()
    {
        return groupToRoleMapping.getProjectRole();
    }

    public void addMappedGroup(Group group)
    {
        this.mappedGroup.add(group);
    }

    public GroupToRoleMapping getGroupToRoleMapping()
    {
        return groupToRoleMapping;
    }

    public Collection getMappedGroups()
    {
        return mappedGroup;
    }

    public Collection getMappedGroupNames()
    {
        Collection groupNames = new ArrayList();
        for (Iterator iterator = mappedGroup.iterator(); iterator.hasNext();)
        {
            Group group = (Group) iterator.next();
            groupNames.add(group.getName());
        }
        return groupNames;
    }

    public Collection getUnpackedUsers()
    {
        Set unpackedUsers = new HashSet();
        for (Iterator iterator = mappedGroup.iterator(); iterator.hasNext();)
        {
            Group group = (Group) iterator.next();
            Collection users = group.getUsers();
            if (users != null)
            {
                unpackedUsers.addAll(users);
            }
        }

        return unpackedUsers;
    }

    public Collection getUnpackedUsersLimited(int limit)
    {
        List unpackedUsers = new ArrayList(getUnpackedUsers());

        if (limit > unpackedUsers.size())
        {
            return unpackedUsers;
        }
        return unpackedUsers.subList(0, limit);
    }
}