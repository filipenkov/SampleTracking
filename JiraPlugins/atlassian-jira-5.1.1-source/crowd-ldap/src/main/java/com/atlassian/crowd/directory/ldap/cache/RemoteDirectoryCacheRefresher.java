package com.atlassian.crowd.directory.ldap.cache;

import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;

/**
 * A simple implementation of CacheRefresher that will only do "Full Refresh".
 * This is used for all LDAP servers other than AD.
 *
 * @since v2.1
 */
public class RemoteDirectoryCacheRefresher extends AbstractCacheRefresher implements CacheRefresher
{
    private static final Logger log = Logger.getLogger(RemoteDirectoryCacheRefresher.class);

    public RemoteDirectoryCacheRefresher(final RemoteDirectory remoteDirectory)
    {
        super(remoteDirectory);
    }

    public boolean synchroniseChanges(final DirectoryCache directoryCache) throws OperationFailedException
    {
        // We can never do a delta sync
        return false;
    }

    private List<User> findAllRemoteUsers() throws OperationFailedException
    {
        long start = System.currentTimeMillis();
        log.debug("loading remote users");
        List<User> users = remoteDirectory.searchUsers(QueryBuilder.queryFor(User.class, EntityDescriptor.user()).returningAtMost(EntityQuery.ALL_RESULTS));
        log.info(new StringBuilder("found [ ").append(users.size()).append(" ] remote users in [ ").append(System.currentTimeMillis() - start).append("ms ]").toString());
        return users;
    }

    private List<Group> findAllRemoteGroups(GroupType groupType) throws OperationFailedException
    {
        long start = System.currentTimeMillis();
        log.debug("loading remote groups");
        List<Group> groups = remoteDirectory.searchGroups(QueryBuilder.queryFor(Group.class, EntityDescriptor.group(groupType)).returningAtMost(EntityQuery.ALL_RESULTS));
        log.info(new StringBuilder("found [ ").append(groups.size()).append(" ] remote groups in [ ").append(System.currentTimeMillis() - start).append("ms ]").toString());
        return groups;
    }

    @Override
    protected void synchroniseAllUsers(DirectoryCache directoryCache) throws OperationFailedException
    {
        Date syncStartDate = new Date();

        List<? extends User> ldapUsers = findAllRemoteUsers();

        // inserts/updates
        directoryCache.addOrUpdateCachedUsers(ldapUsers, syncStartDate);

        // removes
        directoryCache.deleteCachedUsersNotIn(ldapUsers, syncStartDate);
    }

    @Override
    protected List<? extends Group> synchroniseAllGroups(GroupType groupType, DirectoryCache directoryCache) throws OperationFailedException
    {
        Date syncStartDate = new Date();

        List<? extends Group> ldapGroups = findAllRemoteGroups(groupType);

        // inserts/updates
        directoryCache.addOrUpdateCachedGroups(ldapGroups, syncStartDate);

        // removes
        directoryCache.deleteCachedGroupsNotIn(groupType, ldapGroups, syncStartDate);

        return ldapGroups;
    }
}
