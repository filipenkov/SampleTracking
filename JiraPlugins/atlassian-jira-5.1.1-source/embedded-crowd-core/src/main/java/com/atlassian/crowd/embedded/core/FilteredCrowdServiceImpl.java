package com.atlassian.crowd.embedded.core;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.GroupWithAttributes;
import com.atlassian.crowd.embedded.api.Query;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidMembershipException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.crowd.exception.runtime.GroupNotFoundException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.crowd.exception.runtime.UserNotFoundException;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.query.entity.AliasQuery;
import com.atlassian.crowd.search.query.entity.ApplicationQuery;
import com.atlassian.crowd.search.query.entity.DirectoryQuery;
import com.atlassian.crowd.search.query.entity.GroupQuery;
import com.atlassian.crowd.search.query.entity.TokenQuery;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.membership.GroupMembersOfGroupQuery;
import com.atlassian.crowd.search.query.membership.GroupMembershipQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.search.query.membership.UserMembersOfGroupQuery;
import com.atlassian.crowd.search.query.membership.UserMembershipQuery;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;

import java.util.Collections;
import java.util.Set;

/**
 * A layer on top of CrowdService which performs group filtering. The filtered groups are treated as if they do not exist.
 * That is, get or search operations will never return them. The only exception being {@link #addGroup(com.atlassian.crowd.embedded.api.Group)}
 * which throws an {@link OperationNotPermittedException}} when a filtered group is added.
 *
 * Note that this implementation assumes there is no children groups under the filtered groups.
 */
public class FilteredCrowdServiceImpl extends DelegatingCrowdService
{
    // The set of query types that we support. Please make sure {@link #search(com.atlassian.crowd.embedded.api.Query)}}
    // works with your new query type before adding one here.
    private final Set<Class<? extends Query>> SUPPORTED_QUERY_TYPES
            = ImmutableSet.of(TokenQuery.class, DirectoryQuery.class, AliasQuery.class, GroupQuery.class,
                              ApplicationQuery.class, UserQuery.class, MembershipQuery.class,
                              UserMembersOfGroupQuery.class, GroupMembershipQuery.class,
                              UserMembershipQuery.class, GroupMembersOfGroupQuery.class);

    private final Set<String> filteredGroups;

    public FilteredCrowdServiceImpl(CrowdService crowdService, FilteredGroupsProvider groupProvider)
    {
        super(crowdService);

        // filtered groups are treated in a case-insensitive manner.
        // We convert them all the lowercase for comparison.
        filteredGroups = ImmutableSet.copyOf(Collections2.transform(groupProvider.getGroups(), new Function<String, String>()
        {
            public String apply(String from)
            {
                return toLowerCase(from);
            }
        }));
    }

    @Override
    public Group getGroup(String name)
    {
        if (isGroupToBeFiltered(name))
        {
            return null;
        }

        return super.getGroup(name);
    }

    @Override
    public GroupWithAttributes getGroupWithAttributes(String name)
    {
        if (isGroupToBeFiltered(name))
        {
            return null;
        }

        return super.getGroupWithAttributes(name);
    }

    @Override
    public <T> Iterable<T> search(Query<T> query)
    {
        // if the type is not what we know beforehand, fail badly.
        if (!SUPPORTED_QUERY_TYPES.contains(query.getClass()))
        {
            throw new IllegalStateException("The query type [" + query.getClass() + "] is not understood by [" + this.getClass().getName() + "].");
        }

        // we only bother filter if there is at least one filtered group defined.
        if (filteredGroups.size() > 0 )
        {
            if (query instanceof GroupQuery)
            {
                // normal search.
                final Iterable<T> result = super.search(query);

                // we have to weed some groups out.
                return filterGroups(result, query);
            }

            // We also filter out certain groups as a result of membership query.
            else if (query instanceof MembershipQuery)
            {
                return searchByMembershipQuery((MembershipQuery<T>) query);
            }
        }

        // otherwise, perform normal search.
        return super.search(query);
    }

    private <T> Iterable<T> filterGroups(Iterable<T> result, Query<T> query)
    {
        // a group query can return either strings or groups as specified by {@link Query#getReturnType}
        // either way we filter them by group names.
        if (String.class.equals(query.getReturnType()))
        {
            // filter the names
            return Iterables.filter(result, new Predicate<T>()
            {
                public boolean apply(T groupname)
                {
                    return !isGroupToBeFiltered((String) groupname);
                }
            });
        }
        else if (Group.class.equals((query.getReturnType())))
        {
            // filter the names
            return Iterables.filter(result, new Predicate<T>()
            {
                public boolean apply(T group)
                {
                    return !isGroupToBeFiltered(((Group) group));
                }
            });
        }
        else
        {
            throw new IllegalArgumentException("return type of GroupQuery cannot be " + query.getReturnType().getName());
        }
    }

    private <T> Iterable<T> searchByMembershipQuery(MembershipQuery<T> query)
    {
        // whenever we match by group and the group is to filtered, the result will always be empty.
        if (query.getEntityToMatch().equals(EntityDescriptor.group()) && isGroupToBeFiltered(query.getEntityNameToMatch()))
        {
            return Collections.emptyList();
        }
        // if the return entity is group then there's a possibility that certain groups need to be filtered.
        else if (query.getEntityToReturn().equals(EntityDescriptor.group()))
        {
            // filter groups.
            final Iterable<T> result = super.search(query);
            return filterGroups(result, query);
        }

        // otherwise, there is nothing to filter. Perform normal search.
        return super.search(query);
    }

    @Override
    public boolean isUserMemberOfGroup(String userName, String groupName)
    {
        if (isGroupToBeFiltered(groupName))
        {
            return false;
        }

        return super.isUserMemberOfGroup(userName, groupName);
    }

    @Override
    public boolean isUserMemberOfGroup(User user, Group group)
    {
        if (isGroupToBeFiltered(group.getName()))
        {
            return false;
        }

        return super.isUserMemberOfGroup(user, group);
    }

    @Override
    public boolean isGroupMemberOfGroup(String childGroupName, String parentGroupName)
    {
        if (isGroupToBeFiltered(childGroupName))
        {
            return false;
        }
        if (isGroupToBeFiltered(parentGroupName))
        {
            return false;
        }

        return super.isGroupMemberOfGroup(childGroupName, parentGroupName);
    }

    @Override
    public boolean isGroupMemberOfGroup(Group childGroup, Group parentGroup)
    {
        if (isGroupToBeFiltered(childGroup.getName()))
        {
            return false;
        }
        if (isGroupToBeFiltered(parentGroup.getName()))
        {
            return false;
        }

        return super.isGroupMemberOfGroup(childGroup, parentGroup);
    }

    @Override
    public Group addGroup(Group group) throws InvalidGroupException, OperationNotPermittedException, OperationFailedException
    {
        if (isGroupToBeFiltered(group))
        {
            throw new OperationNotPermittedException("group name [" + group.getName() +"] is reserved. cannot add.");
        }

        return super.addGroup(group);
    }

    @Override
    public Group updateGroup(Group group) throws GroupNotFoundException, InvalidGroupException, OperationNotPermittedException, OperationFailedException
    {
        throwGroupNotFoundIfGroupIsToBeFiltered(group);
        return super.updateGroup(group);
    }

    @Override
    public void setGroupAttribute(Group group, String attributeName, String attributeValue) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        throwGroupNotFoundIfGroupIsToBeFiltered(group);
        super.setGroupAttribute(group, attributeName, attributeValue);
    }

    @Override
    public void setGroupAttribute(Group group, String attributeName, Set<String> attributeValues) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        throwGroupNotFoundIfGroupIsToBeFiltered(group);
        super.setGroupAttribute(group, attributeName, attributeValues);
    }

    @Override
    public void removeGroupAttribute(Group group, String attributeName) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        throwGroupNotFoundIfGroupIsToBeFiltered(group);
        super.removeGroupAttribute(group, attributeName);
    }

    @Override
    public void removeAllGroupAttributes(Group group) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        throwGroupNotFoundIfGroupIsToBeFiltered(group);
        super.removeAllGroupAttributes(group);
    }

    @Override
    public boolean removeGroup(Group group) throws OperationNotPermittedException, OperationFailedException
    {
        if (isGroupToBeFiltered(group))
        {
            return false;
        }

        return super.removeGroup(group);
    }

    @Override
    public void addUserToGroup(User user, Group group) throws GroupNotFoundException, UserNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        throwGroupNotFoundIfGroupIsToBeFiltered(group);
        super.addUserToGroup(user, group);
    }

    @Override
    public void addGroupToGroup(Group childGroup, Group parentGroup) throws GroupNotFoundException, OperationNotPermittedException, InvalidMembershipException, OperationFailedException
    {
        throwGroupNotFoundIfGroupIsToBeFiltered(childGroup);
        throwGroupNotFoundIfGroupIsToBeFiltered(parentGroup);

        super.addGroupToGroup(childGroup, parentGroup);
    }

    @Override
    public boolean removeUserFromGroup(User user, Group group) throws GroupNotFoundException, UserNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        throwGroupNotFoundIfGroupIsToBeFiltered(group);
        return super.removeUserFromGroup(user, group);
    }

    @Override
    public boolean removeGroupFromGroup(Group childGroup, Group parentGroup) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        throwGroupNotFoundIfGroupIsToBeFiltered(childGroup);
        throwGroupNotFoundIfGroupIsToBeFiltered(parentGroup);

        return super.removeGroupFromGroup(childGroup, parentGroup);
    }

    @Override
    public boolean isUserDirectGroupMember(User user, Group group) throws OperationFailedException
    {
        if (isGroupToBeFiltered(group))
        {
            return false;
        }

        return super.isUserDirectGroupMember(user, group);
    }

    @Override
    public boolean isGroupDirectGroupMember(Group childGroup, Group parentGroup) throws OperationFailedException
    {
        if (isGroupToBeFiltered(childGroup) || isGroupToBeFiltered(parentGroup))
        {
            return false;
        }

        return super.isGroupDirectGroupMember(childGroup, parentGroup);
    }

    private boolean isGroupToBeFiltered(String groupname)
    {
        return filteredGroups.contains(toLowerCase(groupname));
    }

    private boolean isGroupToBeFiltered(Group group)
    {
        return isGroupToBeFiltered(group.getName());
    }

    private void throwGroupNotFoundIfGroupIsToBeFiltered(Group group) throws GroupNotFoundException
    {
        if (isGroupToBeFiltered(group.getName()))
        {
            throw new GroupNotFoundException(group.getName());
        }
    }
}
