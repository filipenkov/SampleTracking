package com.atlassian.crowd.directory;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.Membership;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * <p>An {@link Iterable} view of the memberships of a collection of named groups, backed
 * with individual calls to a {@link RemoteDirectory}.</p>
 * <p>Any underlying failures during iteration will be wrapped as {@link Membership.MembershipIterationException}s.</p> 
 */
public class DirectoryMembershipsIterable implements Iterable<Membership>
{
    private final RemoteDirectory remoteDirectory;
    private final Iterable<String> groupNames;
    
    public DirectoryMembershipsIterable(RemoteDirectory remoteDirectory, Iterable<String> groupNames)
    {
        Preconditions.checkNotNull(remoteDirectory);
        Preconditions.checkNotNull(groupNames);
        this.remoteDirectory = remoteDirectory;
        this.groupNames = groupNames;
    }

    public DirectoryMembershipsIterable(RemoteDirectory remoteDirectory) throws OperationFailedException
    {
        Preconditions.checkNotNull(remoteDirectory);

        List<Group> groups = remoteDirectory.searchGroups(QueryBuilder
                .queryFor(Group.class, EntityDescriptor.group(GroupType.GROUP))
                .returningAtMost(EntityQuery.ALL_RESULTS));
        
        this.remoteDirectory = remoteDirectory;
        this.groupNames = Iterables.transform(groups, GROUPS_TO_NAMES);
    }
    
    private final Function<String, Membership> lookUpMembers = new Function<String, Membership>()
    {
        @Override
        public Membership apply(String from)
        {
            try
            {
                return get(from);
            }
            catch (OperationFailedException ofe)
            {
                throw new Membership.MembershipIterationException(ofe);
            }
        }
    };
    
    @Override
    public Iterator<Membership> iterator()
    {
        return Iterators.transform(groupNames.iterator(), lookUpMembers);
    }
    
    private Membership get(final String groupName) throws OperationFailedException
    {
        List<String> userNames, childGroupNames;
        
        userNames = remoteDirectory.searchGroupRelationships(QueryBuilder.queryFor(String.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(groupName).returningAtMost(EntityQuery.ALL_RESULTS));
    
        childGroupNames = remoteDirectory.searchGroupRelationships(QueryBuilder.queryFor(String.class, EntityDescriptor.group()).childrenOf(EntityDescriptor.group()).withName(groupName).returningAtMost(EntityQuery.ALL_RESULTS));

        final Set<String> userNamesSet = ImmutableSet.copyOf(userNames),
                childGroupNamesSet = ImmutableSet.copyOf(childGroupNames);
        
        return new Membership() {
            @Override
            public String getGroupName()
            {
                return groupName;
            }
            
            @Override
            public Set<String> getUserNames()
            {
                return userNamesSet;
            }
            
            @Override
            public Set<String> getChildGroupNames()
            {
                return childGroupNamesSet;
            }
        };
    }
    
    public static final Function<Group, String> GROUPS_TO_NAMES =
            new Function<Group, String>() {
                @Override
                public String apply(Group from)
                {
                    return from.getName();
                }
        };
}
