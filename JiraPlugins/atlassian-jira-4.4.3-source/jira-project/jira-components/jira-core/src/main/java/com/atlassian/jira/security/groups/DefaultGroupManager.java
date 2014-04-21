package com.atlassian.jira.security.groups;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableGroup;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.GroupQuery;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.jira.user.util.OSUserConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import static com.atlassian.crowd.search.EntityDescriptor.group;
import static com.atlassian.crowd.search.query.entity.EntityQuery.ALL_RESULTS;

/**
 * Default implementation of GroupManager.
 *
 * @since v3.13
 */
public class DefaultGroupManager implements GroupManager
{
    private final CrowdService crowdService;

    public DefaultGroupManager(final CrowdService crowdService)
    {
        this.crowdService = crowdService;
    }

    @Override
    public Collection<Group> getAllGroups()
    {
        SearchRestriction restriction = NullRestrictionImpl.INSTANCE;
        final com.atlassian.crowd.embedded.api.Query<Group> query = new GroupQuery<Group>(Group.class, GroupType.GROUP,restriction, 0, -1);
        Iterable<Group> crowdGroups = crowdService.search(query);
        if (crowdGroups instanceof Collection)
        {
            return (Collection) crowdGroups;
        }

        HashSet<Group> allGroups= new HashSet<Group>();
        for (Group group : crowdGroups)
        {
            allGroups.add(group);
        }
        return allGroups;
    }

    public boolean groupExists(final String groupName)
    {
        return getGroup(groupName) != null;
    }

    public Group createGroup(final String groupName) throws OperationNotPermittedException, InvalidGroupException
    {
        return crowdService.addGroup(new ImmutableGroup(groupName));
    }

    public com.opensymphony.user.Group getGroup(final String groupName)
    {
        return OSUserConverter.convertToOSGroup(getGroupObject(groupName));
    }

    @Override
    public Group getGroupObject(String groupName)
    {
        return crowdService.getGroup(groupName);
    }

    @Override
    public boolean isUserInGroup(final String username, final String groupname)
    {
        if (username == null || groupname == null)
        {
            // This is done because this was the old behaviour in OSUser for group.containsUser() and User.inGroup()
            return false;
        }
        return crowdService.isUserMemberOfGroup(username, groupname);
    }

    @Override
    public boolean isUserInGroup(final User user, final Group group)
    {
        if (user == null || group == null)
        {
            // This is done because this was the old behaviour in OSUser for group.containsUser() and User.inGroup()
            return false;
        }
        return crowdService.isUserMemberOfGroup(user, group);
    }

    @Override
    public boolean isUserInGroup(com.opensymphony.user.User user, com.opensymphony.user.Group group)
    {
        return isUserInGroup((User) user, (Group) group);
    }

    public Collection<User> getUsersInGroup(final String groupName)
    {
        Iterable<User> usersIterable = crowdService.search(
                QueryBuilder.queryFor(User.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(groupName).returningAtMost(ALL_RESULTS));
        return convertIterableToCollection(usersIterable);
    }

    public Collection<User> getUsersInGroup(final Group group)
    {
        return getUsersInGroup(group.getName());
    }

    public Collection<User> getDirectUsersInGroup(final Group group)
    {
        Collection<User> usersInGroup = getUsersInGroup(group.getName());
        // Remove any indirect members
        for (Iterator<User> iter = usersInGroup.iterator(); iter.hasNext(); )
        {
            if (!crowdService.isUserDirectGroupMember(iter.next(), group))
            {
                iter.remove();
            }
        }
        return usersInGroup;
    }

    public Collection<Group> getGroupsForUser(final String userName)
    {
        Iterable<Group> searchResults = crowdService.search(
                QueryBuilder.queryFor(Group.class, group()).parentsOf(EntityDescriptor.user()).withName(userName).returningAtMost(ALL_RESULTS));
        return convertIterableToCollection(searchResults);
    }

    public Collection<String> getGroupNamesForUser(final String userName)
    {
        Iterable<String> usersIterable = crowdService.search(
                QueryBuilder.queryFor(String.class, group()).parentsOf(EntityDescriptor.user()).withName(userName).returningAtMost(ALL_RESULTS));
        return convertIterableToCollection(usersIterable);
    }

    private <T> Collection<T> convertIterableToCollection(Iterable<T> iterable)
    {
        // try to just cast if possible (will work on current implementation of Crowd)
        if (iterable instanceof Collection)
        {
            return (Collection<T>) iterable;
        }
        // Cast didn't work - do the work to create a Collection.
        Collection<T> collection = new ArrayList<T>();
        for (T member : iterable)
        {
            collection.add(member);
        }
        return collection;
    }

    public void addUserToGroup(User user, Group group) throws GroupNotFoundException, UserNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        crowdService.addUserToGroup(user, group);
    }
}
