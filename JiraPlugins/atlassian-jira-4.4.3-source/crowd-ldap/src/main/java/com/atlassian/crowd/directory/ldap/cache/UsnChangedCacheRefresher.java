package com.atlassian.crowd.directory.ldap.cache;

import com.atlassian.crowd.directory.MicrosoftActiveDirectory;
import com.atlassian.crowd.directory.SynchronisableDirectoryProperties;
import com.atlassian.crowd.directory.ldap.mapper.attribute.group.RFC4519MemberDnMapper;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.Tombstone;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.LDAPGroupWithAttributes;
import com.atlassian.crowd.model.user.LDAPUserWithAttributes;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Retrieves latest changes from MS Active Directory in order to allow "delta" cache refreshes.
 * <p/>
 * See http://msdn.microsoft.com/en-us/library/ms677625.aspx and http://msdn.microsoft.com/en-us/library/ms677627%28VS.85%29.aspx
 * for details on polling Microsoft Active Directory.
 * <p/>
 * This class is guaranteed to be run from a single thread at a time by per directory.  This means it does not need to
 * worry about race-conditions, but still must consider safe publication of variables (per directory).
 */
public class UsnChangedCacheRefresher extends AbstractCacheRefresher implements CacheRefresher
{
    private static final Logger log = Logger.getLogger(UsnChangedCacheRefresher.class);
    private static final long UNINITIALISED = -1;

    private final MicrosoftActiveDirectory activeDirectory;

    // the following caches should be stored in a cluster-safe manner
    // make volatile for safe-publication
    private volatile long highestCommittedUSN = UNINITIALISED;
    private final LDAPEntityNameMap<LDAPUserWithAttributes> userMap = new LDAPEntityNameMap<LDAPUserWithAttributes>();
    private final LDAPEntityNameMap<LDAPGroupWithAttributes> groupMap = new LDAPEntityNameMap<LDAPGroupWithAttributes>();

    private Future<List<LDAPUserWithAttributes>> userListFuture;
    private Future<List<LDAPGroupWithAttributes>> groupListFuture;
    private Future<List<LDAPGroupWithAttributes>> roleListFuture;

    public UsnChangedCacheRefresher(final MicrosoftActiveDirectory activeDirectory)
    {
        super(activeDirectory);
        this.activeDirectory = activeDirectory;
    }

    @Override
    protected void synchroniseMembershipsForGroup(GroupType groupType, Group group, DirectoryCache directoryCache) throws OperationFailedException
    {
        long start = System.currentTimeMillis();

        LDAPGroupWithAttributes ldapGroup = (LDAPGroupWithAttributes) group;

        Set<String> dnList = ldapGroup.getValues(RFC4519MemberDnMapper.ATTRIBUTE_KEY);
        List<String> ldapUsers = new ArrayList<String>();
        List<String> ldapSubGroups = new ArrayList<String>();
        for (String dn : dnList)
        {
            String userMember = userMap.getByDn(dn);
            String groupMember = groupMap.getByDn(dn);
            if (userMember == null && groupMember == null)
            {
                // just because we don't have record of the entity in our memory it doesn't mean it doesn't
                // exist on the server.  at this stage we don't know if its a user or group
                LDAPUserWithAttributes userObj = findRemoteUser(dn);
                if (userObj != null)
                {
                    userMember = userObj.getName();
                }
                else
                {
                    // if its not a user try a group
                    LDAPGroupWithAttributes groupObj = findRemoteGroup(dn);
                    if (groupObj != null)
                    {
                        groupMember = groupObj.getName();
                    }
                }
            }

            if (userMember != null)
            {
                ldapUsers.add(userMember);
            }

            if (groupMember != null)
            {
                ldapSubGroups.add(groupMember);
            }
        }

        log.info(new StringBuilder("found [ ").append(ldapUsers.size()).append(" ] remote user-group memberships in [ ").append(System.currentTimeMillis() - start).append("ms ]").toString());
        log.info(new StringBuilder("found [ ").append(ldapSubGroups.size()).append(" ] remote group-group memberships in [ 0ms ]").toString());

        directoryCache.syncUserMembersForGroup(ldapGroup, ldapUsers);
        directoryCache.syncGroupMembersForGroup(ldapGroup, ldapSubGroups);
    }

    private LDAPGroupWithAttributes findRemoteGroup(String dn) throws OperationFailedException
    {
        try
        {
            return activeDirectory.findEntityByDN(dn, LDAPGroupWithAttributes.class);
        }
        catch (GroupNotFoundException e)
        {
            return null;
        }
        catch (UserNotFoundException e)
        {
            throw new AssertionError("Should not throw UserNotFoundException");
        }
    }

    private LDAPUserWithAttributes findRemoteUser(String dn) throws OperationFailedException
    {
        try
        {
            return activeDirectory.findEntityByDN(dn, LDAPUserWithAttributes.class);
        }
        catch (UserNotFoundException e)
        {
            return null;
        }
        catch (GroupNotFoundException e)
        {
            throw new AssertionError("Should not throw GroupNotFoundException");
        }
    }

    public boolean synchroniseChanges(DirectoryCache directoryCache) throws OperationFailedException
    {
        // When restarting the app, we must do a full refresh the first time.
        // If Roles are enabled then force full refreshes each time (this is in line with old functionality of not allowing caching with roles turned on).
        if (highestCommittedUSN == UNINITIALISED ||
            !activeDirectory.isRolesDisabled() ||
            !Boolean.parseBoolean(activeDirectory.getValue(SynchronisableDirectoryProperties.INCREMENTAL_SYNC_ENABLED)))
        {
            return false;
        }

        // Find the latest USN-Changed value for the AD server
        long currentHighestCommittedUSN = activeDirectory.fetchHighestCommittedUSN();

        synchroniseUserChanges(directoryCache);
        synchroniseGroupChanges(directoryCache);

        // Remember the USN-Changed value that we were on when we started  (this ensures we don't miss anything, but we may get duplicates)
        this.highestCommittedUSN = currentHighestCommittedUSN;

        return true;
    }

    public void synchroniseAll(DirectoryCache directoryCache) throws OperationFailedException
    {
        ExecutorService queryExecutor = Executors.newFixedThreadPool(3);
        try
        {
            userListFuture = queryExecutor.submit(new Callable<List<LDAPUserWithAttributes>>()
            {
                public List<LDAPUserWithAttributes> call() throws Exception
                {
                    long start = System.currentTimeMillis();
                    log.debug("loading remote users");
                    List<LDAPUserWithAttributes> ldapUsers = activeDirectory.searchUsers(QueryBuilder
                            .queryFor(LDAPUserWithAttributes.class, EntityDescriptor.user())
                            .returningAtMost(EntityQuery.ALL_RESULTS));
                    log.info("found [ " + ldapUsers.size() + " ] remote users in [ " + (System.currentTimeMillis() - start) + "ms ]");
                    return ldapUsers;
                }
            });
            groupListFuture = queryExecutor.submit(new Callable<List<LDAPGroupWithAttributes>>()
            {
                public List<LDAPGroupWithAttributes> call() throws Exception
                {
                    long start = System.currentTimeMillis();
                    log.debug("loading remote groups");
                    List<LDAPGroupWithAttributes> ldapGroups = activeDirectory.searchGroups(QueryBuilder
                            .queryFor(LDAPGroupWithAttributes.class, EntityDescriptor.group(GroupType.GROUP))
                            .returningAtMost(EntityQuery.ALL_RESULTS));
                    log.info("found [ " + ldapGroups.size() + " ] remote groups in [ " + (System.currentTimeMillis() - start) + "ms ]");
                    return ldapGroups;
                }
            });
            roleListFuture = queryExecutor.submit(new Callable<List<LDAPGroupWithAttributes>>()
            {
                public List<LDAPGroupWithAttributes> call() throws Exception
                {
                    List<LDAPGroupWithAttributes> ldapRoles = null;
                    if(!activeDirectory.isRolesDisabled()) {
                        long start = System.currentTimeMillis();
                        log.debug("loading remote roles");
                        ldapRoles = activeDirectory.searchGroups(QueryBuilder
                                .queryFor(LDAPGroupWithAttributes.class, EntityDescriptor.group(GroupType.LEGACY_ROLE))
                                .returningAtMost(EntityQuery.ALL_RESULTS));
                        log.info("found [ " + ldapRoles.size() + " ] remote groups in [ " + (System.currentTimeMillis() - start) + "ms ]");
                    } else {
                        log.debug("roles disabled, not loading remote roles");
                    }
                    return ldapRoles;
                }
            });

            // Find the latest USN-Changed value for the AD server
            long currentHighestCommittedUSN = activeDirectory.fetchHighestCommittedUSN();
            // Do standard synchroniseAll
            super.synchroniseAll(directoryCache);
            // Remember the USN-Changed value that we were on when we started  (this ensures we don't miss anything, but we may get duplicates)
            this.highestCommittedUSN = currentHighestCommittedUSN;

        }
        finally
        {
            queryExecutor.shutdown();

            userListFuture = null;
            groupListFuture = null;
            roleListFuture = null;
        }

    }

    @Override
    protected void synchroniseAllUsers(DirectoryCache directoryCache) throws OperationFailedException
    {
        userMap.clear();

        Date syncStartDate = new Date();

        try
        {
            List<LDAPUserWithAttributes> ldapUsers = userListFuture.get();

            // update the user map
            for (LDAPUserWithAttributes ldapUser : ldapUsers)
            {
                userMap.put(ldapUser);
            }

            directoryCache.addOrUpdateCachedUsers(ldapUsers, syncStartDate);
            directoryCache.deleteCachedUsersNotIn(ldapUsers, syncStartDate);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new OperationFailedException("background query interrupted", e);
        }
        catch (ExecutionException e)
        {
            throw new OperationFailedException(e);
        }
    }

    @Override
    protected List<? extends Group> synchroniseAllGroups(GroupType groupType, DirectoryCache directoryCache) throws OperationFailedException
    {
        groupMap.clear();

        Date syncStartDate = new Date();

        try
        {
            List<LDAPGroupWithAttributes> ldapGroups;
            ldapGroups = groupType==GroupType.GROUP ? groupListFuture.get() : roleListFuture.get();
            ldapGroups = Collections.unmodifiableList(ldapGroups);
            
            for (LDAPGroupWithAttributes ldapGroup : ldapGroups)
            {
                groupMap.put(ldapGroup);
            }

            directoryCache.addOrUpdateCachedGroups(ldapGroups, syncStartDate);
            directoryCache.deleteCachedGroupsNotIn(groupType, ldapGroups, syncStartDate);
            return ldapGroups;
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new OperationFailedException("background query interrupted", e);
        }
        catch (ExecutionException e)
        {
            throw new OperationFailedException(e);
        }
    }

    private void synchroniseUserChanges(DirectoryCache directoryCache) throws OperationFailedException
    {
        long start = System.currentTimeMillis();
        log.debug("loading changed remote users");
        List<LDAPUserWithAttributes> updatedUsers = activeDirectory.findAddedOrUpdatedUsersSince(highestCommittedUSN);
        List<Tombstone> tombstones = activeDirectory.findUserTombstonesSince(highestCommittedUSN);
        log.info(new StringBuilder("found [ ").append(updatedUsers.size() + tombstones.size()).append(" ] changed remote users in [ ").append(System.currentTimeMillis() - start).append("ms ]").toString());

        for (LDAPUserWithAttributes user : updatedUsers)
        {
            userMap.put(user);
        }

        // Send a null sync date - we want to force this change else we may miss it.
        // If we put a stale value in then it will be fixed on next refresh.
        directoryCache.addOrUpdateCachedUsers(updatedUsers, null);

        // calculate removed principals
        start = System.currentTimeMillis();
        Set<String> usernames = new HashSet<String>();
        for (Tombstone tombstone : tombstones)
        {
            String username = userMap.getByGuid(tombstone.getObjectGUID());
            if (username != null)
            {
                usernames.add(username);
            }
        }
        log.info(new StringBuilder("scanned and compared [ ").append(tombstones.size()).append(" ] groups for delete in DB cache in [ ").append(System.currentTimeMillis() - start).append("ms ]").toString());

        directoryCache.deleteCachedUsers(usernames);
    }

    private void synchroniseGroupChanges(DirectoryCache directoryCache) throws OperationFailedException
    {
        long start = System.currentTimeMillis();
        log.debug("loading changed remote groups");
        List<LDAPGroupWithAttributes> updatedGroups = activeDirectory.findAddedOrUpdatedGroupsSince(highestCommittedUSN);
        List<Tombstone> tombstones = activeDirectory.findGroupTombstonesSince(highestCommittedUSN);
        log.info(new StringBuilder("found [ ").append(updatedGroups.size() + tombstones.size()).append(" ] changed remote groups in [ ").append(System.currentTimeMillis() - start).append("ms ]").toString());

        for (LDAPGroupWithAttributes group : updatedGroups)
        {
            groupMap.put(group);
        }

        // Send a null sync date - we want to force this change else we may miss it.
        // If we put a stale value in then it will be fixed on next refresh.
        directoryCache.addOrUpdateCachedGroups(updatedGroups, null);

        // Now update the memberships for the changed groups
        // (After new Groups are added because we may have nested group memberships)
        synchroniseMemberships(GroupType.GROUP, updatedGroups, directoryCache);

        // calculate removed groups
        start = System.currentTimeMillis();
        Set<String> groupnames = new HashSet<String>();
        for (Tombstone tombstone : tombstones)
        {
            String groupName = groupMap.getByGuid(tombstone.getObjectGUID());
            if (groupName != null)
            {
                groupnames.add(groupName);
            }
        }
        log.info(new StringBuilder("scanned and compared [ ").append(tombstones.size()).append(" ] groups for delete in DB cache in [ ").append(System.currentTimeMillis() - start).append("ms ]").toString());

        directoryCache.deleteCachedGroups(groupnames);
    }
}
