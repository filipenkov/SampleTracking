package com.atlassian.crowd.directory.ldap.cache;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.directory.DirectoryMembershipsIterable;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.Membership;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import org.apache.log4j.Logger;
import com.google.common.base.Function;
import com.google.common.collect.Maps;

import org.slf4j.LoggerFactory;

/**
 * @since v2.1
 */
public abstract class AbstractCacheRefresher implements CacheRefresher
{
    private static final Logger log = Logger.getLogger(AbstractCacheRefresher.class);

    static final int MEMBERSHIP_LOG_FREQUENCY = 5;

    protected final RemoteDirectory remoteDirectory;

    public AbstractCacheRefresher(final RemoteDirectory remoteDirectory)
    {
        this.remoteDirectory = remoteDirectory;
    }

    public void synchroniseAll(DirectoryCache directoryCache) throws OperationFailedException
    {
        synchroniseAllUsers(directoryCache);
        // Synchronise groups of type GROUP
        List<? extends Group> allGroups = synchroniseAllGroups(GroupType.GROUP, directoryCache);
        // We have to finish adding new Groups BEFORE we can add Nested Group memberships.
        synchroniseMemberships(allGroups, directoryCache);
        // Synchronise groups of type ROLE
        if (!remoteDirectory.isRolesDisabled())
        {
            List<? extends Group> allRoles = synchroniseAllGroups(GroupType.LEGACY_ROLE, directoryCache);
            synchroniseMemberships(GroupType.LEGACY_ROLE, allRoles, directoryCache);
        }
    }

    protected abstract void synchroniseAllUsers(final DirectoryCache directoryCache) throws OperationFailedException;

    protected abstract List<? extends Group> synchroniseAllGroups(final GroupType legacyRole, final DirectoryCache directoryCache) throws OperationFailedException;

    protected void synchroniseMemberships(final GroupType groupType, final List<? extends Group> remoteGroups, final DirectoryCache directoryCache)
            throws OperationFailedException
    {
        if (groupType == GroupType.GROUP)
        {
            synchroniseMemberships(remoteGroups, directoryCache);
            return;
        }
        
        if (log.isDebugEnabled())
        {
            log.debug("Updating memberships for " + remoteGroups.size() + " " + groupType.name() + "s from " + directoryDescription());
        }

        int total = remoteGroups.size();
        int logEvery = total / MEMBERSHIP_LOG_FREQUENCY;

        for (int i=0; i < total; i++)
        {
            synchroniseMembershipsForGroup(groupType, remoteGroups.get(i), directoryCache);

            if (logEvery == 0  || i % logEvery == 0)
            {
                log.info(new StringBuilder("Migrated memberships for [").append(i).append("] of [").append(total).append("] groups").toString());
            }
        }
    }

    private List<String> findAllUserMembersOfGroup(String name, GroupType type) throws OperationFailedException
    {
        long start = System.currentTimeMillis();
        List<String> names = remoteDirectory.searchGroupRelationships(QueryBuilder.queryFor(String.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group(type)).withName(name).returningAtMost(EntityQuery.ALL_RESULTS));
        log.info(new StringBuilder("found [ ").append(names.size()).append(" ] remote user-group memberships in [ ").append(System.currentTimeMillis() - start).append("ms ]").toString());
        return names;
    }

    private List<String> findAllGroupMembersOfGroup(String name, GroupType type) throws OperationFailedException
    {
        long start = System.currentTimeMillis();
        List<String> names = remoteDirectory.searchGroupRelationships(QueryBuilder.queryFor(String.class, EntityDescriptor.group(type)).childrenOf(EntityDescriptor.group(type)).withName(name).returningAtMost(EntityQuery.ALL_RESULTS));
        log.info(new StringBuilder("found [ ").append(names.size()).append(" ] remote group-group memberships in [ ").append(System.currentTimeMillis() - start).append("ms ]").toString());
        return names;
    }

    protected void synchroniseMembershipsForGroup(final GroupType groupType, final Group ldapGroup, final DirectoryCache directoryCache)
            throws OperationFailedException
    {
        // TODO: You can potentially save further calls to search by getting GroupWithAttributes and using the "members" attribute.
        List<String> ldapUsers = findAllUserMembersOfGroup(ldapGroup.getName(), groupType);
        directoryCache.syncUserMembersForGroup(ldapGroup, ldapUsers);

        if (remoteDirectory.supportsNestedGroups())
        {
            List<String> ldapSubGroups = findAllGroupMembersOfGroup(ldapGroup.getName(), groupType);
            directoryCache.syncGroupMembersForGroup(ldapGroup, ldapSubGroups);
        }
    }

    protected void synchroniseMemberships(final List<? extends Group> remoteGroups, final DirectoryCache directoryCache)
            throws OperationFailedException
    {
        if (log.isDebugEnabled())
        {
            log.debug("Updating memberships for " + remoteGroups.size() + " groups from " + directoryDescription());
        }

        int total = remoteGroups.size();
        int logEvery = total / MEMBERSHIP_LOG_FREQUENCY;

        Map<String, ? extends Group> groupsByName = Maps.uniqueIndex(remoteGroups, DirectoryMembershipsIterable.GROUPS_TO_NAMES);
        
        int i = 0;
        
        Iterable<Membership> memberships = remoteDirectory.getMemberships();
        
        Iterator<Membership> iter = memberships.iterator();
        
        while (iter.hasNext())
        {
            Membership membership;
            
            long start = System.currentTimeMillis();
            membership = iter.next();
            long finish = System.currentTimeMillis();
            
            long duration = finish - start;
            
            log.info("found [ " + membership.getUserNames().size() + " ] remote user-group memberships, "
                    + "[ " +  membership.getChildGroupNames().size() + " ] remote group-group memberships in [ "
                    + duration + "ms ]");
            
            Group g = groupsByName.get(membership.getGroupName());
            if (g == null)
            {
                log.debug("Unexpected group in response: " + membership.getGroupName());
                continue;
            }
            
            directoryCache.syncUserMembersForGroup(g, membership.getUserNames());

            if (remoteDirectory.supportsNestedGroups())
            {
                directoryCache.syncGroupMembersForGroup(g, membership.getChildGroupNames());
            }
            
            i++;
            if (logEvery == 0  || i % logEvery == 0)
            {
                log.info("Migrated memberships for [" + i + "] of [" + total + "] groups");
            }
        }
    }

    protected String directoryDescription()
    {
        return remoteDirectory.getDescriptiveName() + " Directory " + remoteDirectory.getDirectoryId();
    }
}
