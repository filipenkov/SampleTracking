package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.embedded.impl.IdentifierMap;
import com.atlassian.crowd.embedded.impl.IdentifierSet;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.event.DirectoryEvent;
import com.atlassian.crowd.event.group.GroupCreatedEvent;
import com.atlassian.crowd.event.group.GroupDeletedEvent;
import com.atlassian.crowd.event.group.GroupMembershipCreatedEvent;
import com.atlassian.crowd.event.group.GroupMembershipDeletedEvent;
import com.atlassian.crowd.event.group.GroupUpdatedEvent;
import com.atlassian.crowd.event.user.UserCreatedEvent;
import com.atlassian.crowd.event.user.UserDeletedEvent;
import com.atlassian.crowd.event.user.UserUpdatedEvent;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidMembershipException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.ReadOnlyGroupException;
import com.atlassian.crowd.exception.UserAlreadyExistsException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.directory.SynchronisationStatusManager;
import com.atlassian.crowd.model.DirectoryEntity;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.InternalDirectoryGroup;
import com.atlassian.crowd.model.membership.MembershipType;
import com.atlassian.crowd.model.user.TimestampedUser;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.Combine;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.crowd.search.query.entity.restriction.constants.GroupTermKeys;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.crowd.util.BatchResult;
import com.atlassian.crowd.util.InternalEntityUtils;
import com.atlassian.crowd.util.Percentage;
import com.atlassian.event.api.EventPublisher;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DbCachingRemoteChangeOperations implements DirectoryCacheChangeOperations
{
    private static final Logger logger = LoggerFactory.getLogger(DbCachingRemoteChangeOperations.class);

    private final DirectoryDao directoryDao;
    private final RemoteDirectory remoteDirectory;
    private final InternalRemoteDirectory internalDirectory;
    private final SynchronisationStatusManager synchronisationStatusManager;
    private final EventPublisher eventPublisher;

    public DbCachingRemoteChangeOperations(DirectoryDao directoryDao,
                                         RemoteDirectory remoteDirectory,
                                         InternalRemoteDirectory internalDirectory,
                                         SynchronisationStatusManager synchronisationStatusManager,
                                         EventPublisher eventPublisher)
    {
        this.directoryDao = directoryDao;
        this.remoteDirectory = remoteDirectory;
        this.internalDirectory = internalDirectory;
        this.synchronisationStatusManager = synchronisationStatusManager;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Returns a Map (username -> user) of users created and updated before the specified date.
     *
     * @param date date and time that the user must be updated before to be included in the result
     * @return a Map of users created and updated before the specified date
     * @throws OperationFailedException if the search operation failed for any reason
     */
    private Map<String, TimestampedUser> findUsersUpdatedBefore(Date date) throws OperationFailedException
    {
        SearchRestriction restriction = date == null ?
                NullRestrictionImpl.INSTANCE :
                Combine.allOf(
                        Restriction.on(UserTermKeys.CREATED_DATE).lessThan(date),
                        Restriction.on(UserTermKeys.UPDATED_DATE).lessThan(date));

        List<TimestampedUser> list = internalDirectory.searchUsers(
                QueryBuilder.queryFor(TimestampedUser.class, EntityDescriptor.user())
                        .with(restriction)
                        .returningAtMost(EntityQuery.ALL_RESULTS));

        Map<String, TimestampedUser> users = new IdentifierMap<TimestampedUser>(list.size());
        for (TimestampedUser timestampedUser : list)
            users.put(timestampedUser.getName(), timestampedUser);

        return users;
    }

    /**
     * Returns a Map (group name -> group) of group created an updated before the specified date.
     *
     * @param date date and time that the group must be updated before to be included in the result
     * @return a Map of groups created and updated before the specified date
     * @throws OperationFailedException if the search operation failed for any reason
     */
    private Map<String, InternalDirectoryGroup> findGroupsUpdatedBefore(Date date) throws OperationFailedException
    {
        SearchRestriction restriction = date == null ?
                NullRestrictionImpl.INSTANCE :
                Combine.allOf(
                        Restriction.on(GroupTermKeys.CREATED_DATE).lessThan(date),
                        Restriction.on(GroupTermKeys.UPDATED_DATE).lessThan(date));

        List<InternalDirectoryGroup> groups = internalDirectory.searchGroups(QueryBuilder
                .queryFor(InternalDirectoryGroup.class, EntityDescriptor.group())
                .with(restriction)
                .returningAtMost(EntityQuery.ALL_RESULTS)
        );

        List<InternalDirectoryGroup> roles = internalDirectory.searchGroups(QueryBuilder
                .queryFor(InternalDirectoryGroup.class, EntityDescriptor.role())
                .with(restriction)
                .returningAtMost(EntityQuery.ALL_RESULTS)
        );

        Map<String, InternalDirectoryGroup> result = new IdentifierMap<InternalDirectoryGroup>(groups.size() + roles.size());
        for (InternalDirectoryGroup internalGroup : groups)
            result.put(internalGroup.getName(), internalGroup);

        for (InternalDirectoryGroup internalGroup : roles)
            result.put(internalGroup.getName(), internalGroup);

        return result;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Implements DirectoryCache
    // -----------------------------------------------------------------------------------------------------------------


    public void addUsers(Set<UserTemplateWithCredentialAndAttributes> usersToAdd) throws OperationFailedException
    {
        if (!usersToAdd.isEmpty())
        {
            synchronisationStatusManager.syncStatus(getDirectoryId(), "directory.caching.sync.adding.users", usersToAdd.size());
            logger.info("adding [ {} ] users", usersToAdd.size());
            TimerStack.push();
            try
            {
                Directory directory = getDirectory();
                boolean initialSyncHasBeenStarted = initialSyncHasBeenStarted(directory);
                
                final BatchResult<User> result = internalDirectory.addAllUsers(usersToAdd);

                for (User addedUser : result.getSuccessfulEntities())
                {
                    publishEvent(new UserCreatedEvent(this, directory, addedUser), initialSyncHasBeenStarted);
                }

                logFailures(internalDirectory, result);
            }
            catch (DirectoryNotFoundException e)
            {
                throw new OperationFailedException(e);
            }
            finally
            {
                logger.info(TimerStack.pop("added [ " + usersToAdd.size() + " ] users in [ {0} ]"));
            }
        }
    }
    
    public void updateUsers(Set<UserTemplate> usersToUpdate) throws OperationFailedException
    {
        if (!usersToUpdate.isEmpty())
        {
            synchronisationStatusManager.syncStatus(getDirectoryId(), "directory.caching.sync.updating.users", usersToUpdate.size());
            logger.info("updating [ {} ] users", usersToUpdate.size());
            TimerStack.push();
            try
            {
                Directory directory = getDirectory();
                boolean initialSyncHasBeenStarted = initialSyncHasBeenStarted(directory);

                int count = 0;
                for (UserTemplate user : usersToUpdate)
                {
                    if (usersToUpdate.size() > 100 && count % 100 == 0)
                    {
                        logger.info("updated [ {}% ] users", Percentage.get(count, usersToUpdate.size()));
                    }

                    try
                    {
                        final User updatedUser = internalDirectory.updateUser(user);

                        publishEvent(new UserUpdatedEvent(this, directory, updatedUser), initialSyncHasBeenStarted);
                    }
                    catch (InvalidUserException e)
                    {
                        // Make sure that one bogus user coming over the wire doesn't hose the entire sync.
                        logger.warn("Unable to synchronize user " + user.getName() + " from remote directory: " + e.getMessage(), e);
                    }
                    catch (UserNotFoundException e)
                    {
                        logger.warn("Could not find user to " + user.getName() + " in internal directory: " + e.getMessage(), e);
                    }
                }
            }
            catch (DirectoryNotFoundException e)
            {
                throw new OperationFailedException(e);
            }
            finally
            {
                logger.info(TimerStack.pop("updated [ " + usersToUpdate.size() + " ] users in [ {0} ]"));
            }
        }
    }


    public void deleteCachedUsers(Set<String> usernames) throws OperationFailedException
    {
        synchronisationStatusManager.syncStatus(getDirectoryId(), "directory.caching.sync.deleting.users", usernames.size());
        logger.info("deleting [ {} ] users", usernames.size());
        TimerStack.push();
        try
        {
            internalDirectory.removeAllUsers(usernames);

            Directory directory = getDirectory();
            boolean initialSyncHasBeenStarted = initialSyncHasBeenStarted(directory);

            for (String deletedUser : usernames)
            {
                publishEvent(new UserDeletedEvent(this, directory, deletedUser), initialSyncHasBeenStarted);
            }
        }
        catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e.getCause());
        }
        finally
        {
            logger.info(TimerStack.pop("deleted [ " + usernames.size() + " ] users in [ {0} ]"));
        }
    }

    public void deleteCachedUsersNotIn(final List<? extends User> remoteUsers, final Date synchStartDate) throws OperationFailedException
    {
        TimerStack.push();
        try
        {
            // Create a HashSet of REMOTE usernames for easy lookup
            Set<String> remoteUsernames = new IdentifierSet(remoteUsers.size());
            Set<String> usersToDelete = new HashSet<String>();

            TimerStack.push();
            try
            {
                for (User remoteUser : remoteUsers)
                    remoteUsernames.add(remoteUser.getName());

                Map<String, TimestampedUser> users = findUsersUpdatedBefore(synchStartDate);

                // Find all Users in our internal cache.

                for (TimestampedUser internalUser : users.values())
                {
                    String userName = internalUser.getName();
                    if (!remoteUsernames.contains(userName))
                    {
                        logger.debug("user [ {} ] not found, deleting", userName);
                        usersToDelete.add(userName);
                    }
                }
            }
            finally
            {
                logger.info(TimerStack.pop("scanned and compared [ " + remoteUsers.size() + " ] users for delete in DB cache in [ {0} ]"));
            }


            if (!usersToDelete.isEmpty())
            {
                deleteCachedUsers(usersToDelete);
            }
        }
        finally
        {
            logger.info(TimerStack.pop("scanned for deleted users in [ {0} ]"));
        }
    }

    public GroupsToAddUpdateReplace findGroupsToUpdate(final Collection<? extends Group> remoteGroups, final Date syncStartDate) throws OperationFailedException
    {
        Set<GroupTemplate> groupsToAdd = new HashSet<GroupTemplate>();
        Set<GroupTemplate> groupsToUpdate = new HashSet<GroupTemplate>();
        Map<String, GroupTemplate> groupsToReplace = new HashMap<String, GroupTemplate>();

        TimerStack.push();
        try
        {

            Map<String, InternalDirectoryGroup> groups = findGroupsUpdatedBefore(syncStartDate);

            for (Group remoteGroup : remoteGroups)
            {
                InternalDirectoryGroup internalGroup = groups.get(remoteGroup.getName());
                if (internalGroup == null)
                {
                    // Group does not exist at this point - we need to create it.
                    logger.debug("group [ {} ] not found, adding", remoteGroup.getName());
                    groupsToAdd.add(makeGroupTemplate(remoteGroup));
                    continue;
                }

                if (!remoteGroup.getName().equals(internalGroup.getName()))
                {
                    logger.warn("remote group name [ {} ] casing differs from local group name [ {} ]. Group details will be kept updated, but the group name cannot be updated", remoteGroup.getName(), internalGroup.getName());
                }

                if (internalGroup.getUpdatedDate() == null)
                {
                    // This can happen if the Crowd Embedded SPI is not implemented correctly.
                    logger.warn("group [ {} ] in directory [ {} ] has no updated date", remoteGroup.getName(), getDirectoryId());
                }
                // ALWAYS do this comparison with the real millis as these may be SQL Timestamps which will throw away millis whenever they feel like it.
                else if (syncStartDate != null && internalGroup.getUpdatedDate().getTime() > syncStartDate.getTime())
                {
                    // Don't update this group, it was changed locally after we started our search.
                    // Any anomalies will catch up on the next synchronization.
                    logger.debug("group [ {} ] in directory [ {} ] modified after synchronisation start, skipping", remoteGroup.getName(), getDirectoryId());
                    continue;
                }

                if (internalGroup.isLocal())
                {
                    // Looks like the admin created a Local Group, then a Group was created in the remote LDAP server with same name.
                    // We will keep the local group and its members and ignore the remote group.
                    logger.debug("group [ {} ] in directory [ {} ] matches local group of same name, skipping", remoteGroup.getName(), getDirectoryId());
                    continue;
                }
                // The group already exists in the Local Directory cache, we may need to update it.
                // First we need to compare the GroupTypes of the two groups as this will require special handling.
                if (remoteGroup.getType() == GroupType.LEGACY_ROLE && internalGroup.getType() == GroupType.GROUP)
                {
                    // Ignore the incoming Role because we have a Group in the cache. Group has precedence.
                    logger.debug("role [ {} ] in directory [ {} ] matches local group of same name, skipping", remoteGroup.getName(), getDirectoryId());
                    continue;
                }
                if (remoteGroup.getType() == GroupType.GROUP && internalGroup.getType() == GroupType.LEGACY_ROLE)
                {
                    // Let the incoming Group override the Role that is currently in the cache
                    logger.debug("role [ {} ] in directory [ {} ] matches legacy role of same name, replacing", internalGroup.getName(), getDirectoryId());
                    // We can't just do an update of GroupType - we must delete the Role and insert a new Group
                    groupsToReplace.put(internalGroup.getName(), makeGroupTemplate(remoteGroup));
                    continue;
                }
                // GroupTypes are the same - check if any other values need updating (ie description)
                if (hasChanged(remoteGroup, internalGroup))
                {
                    final GroupTemplate groupToUpdate = makeGroupTemplate(remoteGroup);
                    // Ensure that the group name will not be updated
                    groupToUpdate.setName(internalGroup.getName());
                    groupsToUpdate.add(groupToUpdate);
                    continue;
                }

                // Group has not changed
                logger.trace("group [ {} ] unmodified, skipping", remoteGroup.getName());
            }
            
            return new GroupsToAddUpdateReplace(groupsToAdd, groupsToUpdate, groupsToReplace);
        }
        finally
        {
            logger.info(TimerStack.pop("scanned and compared [ " + remoteGroups.size() + " ] groups for update in DB cache in [ {0} ]"));
        }
    }

    public void removeGroups(Collection<String> groupsToRemove) throws OperationFailedException
    {
        if (!groupsToRemove.isEmpty())
        {
            TimerStack.push();
            try
            {
                Directory directory = getDirectory();
                boolean initialSyncHasBeenStarted = initialSyncHasBeenStarted(directory);

                for (String entry : groupsToRemove)
                {
                    try
                    {
                        internalDirectory.removeGroup(entry);

                        publishEvent(new GroupDeletedEvent(this, directory, entry), initialSyncHasBeenStarted);
                    }
                    catch (GroupNotFoundException e)
                    {
                        logger.warn("Could not find group: " + e.getGroupName(), e);
                    }
                    catch (ReadOnlyGroupException e)
                    {
                        logger.warn("Group is read-only and not allowed to be modified: " + e.getGroupName(), e);
                    }
                }
            }
            catch (DirectoryNotFoundException e)
            {
                throw new OperationFailedException(e);
            }
            finally
            {
                logger.info(TimerStack.pop("deleted [ " + groupsToRemove.size() + " ] groups to be replaced [ {0} ]"));
            }
        }
    }
    
    public void addGroups(Set<GroupTemplate> groupsToAdd) throws OperationFailedException
    {
        logger.debug("adding [ {} ] groups", groupsToAdd.size());
        if (!groupsToAdd.isEmpty())
        {
            synchronisationStatusManager.syncStatus(getDirectoryId(), "directory.caching.sync.adding.groups", groupsToAdd.size());
            TimerStack.push();
            try
            {
                Directory directory = getDirectory();
                boolean initialSyncHasBeenStarted = initialSyncHasBeenStarted(directory);

                final BatchResult<Group> result = internalDirectory.addAllGroups(groupsToAdd);

                for (Group addedGroup : result.getSuccessfulEntities())
                {
                    publishEvent(new GroupCreatedEvent(this, directory, addedGroup), initialSyncHasBeenStarted);
                }

                logFailures(internalDirectory, result);
            }
            catch (DirectoryNotFoundException e)
            {
                throw new OperationFailedException(e);
            }
            finally
            {
                logger.info(TimerStack.pop("added [ " + groupsToAdd.size() + " ] groups in [ {0} ]"));
            }
        }
    }

    public void updateGroups(Collection<GroupTemplate> groupsToUpdate) throws OperationFailedException
    {
        logger.debug("updating [ {} ] groups", groupsToUpdate.size());
        if (!groupsToUpdate.isEmpty())
        {
            synchronisationStatusManager.syncStatus(getDirectoryId(), "directory.caching.sync.updating.groups", groupsToUpdate.size());
            try
            {
                TimerStack.push();
                
                Directory directory = getDirectory();
                boolean initialSyncHasBeenStarted = initialSyncHasBeenStarted(directory);

                for (GroupTemplate groupTemplate : groupsToUpdate)
                {
                    try
                    {
                        final Group updatedGroup = internalDirectory.updateGroup(groupTemplate);

                        publishEvent(new GroupUpdatedEvent(this, directory, updatedGroup), initialSyncHasBeenStarted);
                    }
                    catch (InvalidGroupException e)
                    {
                        logger.warn("Unable to synchronise group " + groupTemplate.getName() + " with remote directory: " + e.getMessage(), e);
                    }
                    catch (ReadOnlyGroupException e)
                    {
                        logger.warn("Unable to update read-only group " + groupTemplate.getName() + " with remote directory: " + e.getMessage(), e);
                    }
                    catch (GroupNotFoundException e)
                    {
                        // we have just checked that the group exists so if group is not found, something is wrong
                        logger.warn("Unable to find group " + groupTemplate.getName() + " on update with remote directory: " + e.getMessage(), e);
                    }
                }
            }
            catch (DirectoryNotFoundException e)
            {
                throw new OperationFailedException(e);
            }
            finally
            {
                logger.info(TimerStack.pop("updated [ " + groupsToUpdate.size() + " ] groups in [ {0} ]"));
            }
        }
    }
    
    public void deleteCachedGroupsNotIn(final GroupType groupType, final List<? extends Group> remoteGroups, final Date syncStartDate) throws OperationFailedException
    {
        Set<String> groupsToRemove = new HashSet<String>();

        try
        {
            TimerStack.push();
            // Create a HashSet of REMOTE group names for easy lookup
            Set<String> remoteGroupnames = new IdentifierSet(remoteGroups.size());
            for (Group remoteGroup : remoteGroups)
            {
                remoteGroupnames.add(remoteGroup.getName());
            }

            Map<String, InternalDirectoryGroup> groups = findGroupsUpdatedBefore(syncStartDate);
            for (InternalDirectoryGroup internalGroup : groups.values())
            {
                if (internalGroup.isLocal())
                {
                    continue;
                }
                if (internalGroup.getCreatedDate() == null)
                {
                    logger.warn("group [ " + internalGroup.getName() + " ] in directory [ " + getDirectoryId() + " ] has no created date, skipping");
                }
                // ALWAYS do this comparison with the real millis as these may be SQL Timestamps which will throw away millis whenever they feel like it.
                else if (syncStartDate != null && internalGroup.getCreatedDate().getTime() > syncStartDate.getTime())
                {
                    // Don't remove this group, it was added locally after we started our search.
                    // Any anomalies will catch up on the next synchronization.
                    logger.debug("group [ " + internalGroup.getName() + " ] created after synchronisation start, skipping");
                    continue;
                }
                if (!remoteGroupnames.contains(internalGroup.getName()))
                {
                    logger.debug("group [ " + internalGroup.getName() + " ] not found, deleting");
                    groupsToRemove.add(internalGroup.getName());
                }
            }
        }
        finally
        {
            logger.info(TimerStack.pop("scanned and compared [ " + remoteGroups.size() + " ] groups for delete in DB cache in [ {0} ]"));
        }

        if (!groupsToRemove.isEmpty())
        {
            deleteCachedGroups(groupsToRemove);
        }
    }

    public void deleteCachedGroups(Set<String> groupnames) throws OperationFailedException
    {
        synchronisationStatusManager.syncStatus(getDirectoryId(), "directory.caching.sync.deleting.groups", groupnames.size());
        logger.info("removing [ " + groupnames.size() + " ] groups");
        try
        {
            TimerStack.push();
            internalDirectory.removeAllGroups(groupnames);

            Directory directory = getDirectory();
            boolean initialSyncHasBeenStarted = initialSyncHasBeenStarted(directory);

            for (String groupName : groupnames)
            {
                publishEvent(new GroupDeletedEvent(this, directory, groupName), initialSyncHasBeenStarted);
            }
        }
        catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
        finally
        {
            logger.info(TimerStack.pop("removed [ " + groupnames.size() + " ] groups in [ {0} ]"));
        }
    }

    private boolean hasChanged(User remoteUser, User internalUser)
    {
        return different(remoteUser.getFirstName(), internalUser.getFirstName()) ||
                different(remoteUser.getLastName(), internalUser.getLastName()) ||
                different(remoteUser.getDisplayName(), internalUser.getDisplayName()) ||
                different(remoteUser.getEmailAddress(), internalUser.getEmailAddress()) ||
                (remoteDirectory.supportsInactiveAccounts() && remoteUser.isActive() != internalUser.isActive());
    }

    private static boolean hasChanged(Group remoteGroup, Group internalGroup)
    {
        return different(remoteGroup.getDescription(), internalGroup.getDescription());
    }

    /**
     * Returns true if the two input Strings are different values, where null and empty String are considered equal.
     * <p/>
     * The {@code remoteString} argument is filtered through {@link InternalEntityUtils#truncateValue(String)}
     * before comparison.
     *
     * @param remoteString   remote directory value
     * @param internalString internal directory value
     * @return true if the two input Strings are different values.
     */
    private static boolean different(final String remoteString, final String internalString)
    {
        // Treat null and empty string as equivalent
        if (StringUtils.isEmpty(remoteString))
        {
            return StringUtils.isNotEmpty(internalString);
        }
        return !InternalEntityUtils.truncateValue(remoteString).equals(internalString);
    }

    private static UserTemplate makeUserTemplate(User user)
    {
        UserTemplate template = new UserTemplate(user);
        template.setFirstName(user.getFirstName());
        template.setLastName(user.getLastName());
        template.setDisplayName(user.getDisplayName());
        template.setEmailAddress(user.getEmailAddress());
        return template;
    }

    private static GroupTemplate makeGroupTemplate(Group group)
    {
        GroupTemplate template = new GroupTemplate(group);
        template.setDescription(group.getDescription());
        return template;
    }

    public AddRemoveSets<String> findUserMembershipForGroupChanges(Group group, Collection<String> remoteUsers) throws OperationFailedException
    {
        Set<String> usersToAdd = new HashSet<String>();
        Set<String> usersToRemove = new HashSet<String>();

        try
        {
            TimerStack.push();

            synchronisationStatusManager.syncStatus(getDirectoryId(), "directory.caching.sync.user.memberships", remoteUsers.size(), group.getName());
            logger.debug("synchronising [ " + remoteUsers.size() + " ] user members for group [ " + group.getName() + " ]");
            // Remove any internal users from the group if they are not members of the group in REMOTE
            List<String> internalMembers = internalDirectory.searchGroupRelationships(QueryBuilder.queryFor(String.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(group.getName()).returningAtMost(EntityQuery.ALL_RESULTS));
            logger.debug("internal directory has [ " + internalMembers.size() + " ] members");

            // add missing remote users
            for (String remoteUser : remoteUsers)
            {
                if (!internalMembers.contains(remoteUser))
                    usersToAdd.add(remoteUser);
            }

            // remove extra local users
            for (String internalUser : internalMembers)
            {
                if (!remoteUsers.contains(internalUser))
                    usersToRemove.add(internalUser);
            }
            
            return new AddRemoveSets<String>(usersToAdd, usersToRemove);
        }
        finally
        {
            logger.debug(TimerStack.pop("scanned and compared [ " + remoteUsers.size() + " ] user members from [ " + group.getName() + " ] in [ {0} ]"));
        }
    }
    
    public void removeUserMembershipsForGroup(Group group, Set<String> usersToRemove) throws OperationFailedException
    {
        if (!usersToRemove.isEmpty())
        {
            int failureCount = 0;
            try
            {
                TimerStack.push();
                Directory directory = getDirectory();
                boolean initialSyncHasBeenStarted = initialSyncHasBeenStarted(directory);

                for (String username : usersToRemove)
                {
                    try
                    {
                        internalDirectory.removeUserFromGroup(username, group.getName());

                        publishEvent(new GroupMembershipDeletedEvent(this, directory, username, group.getName(), MembershipType.GROUP_USER), initialSyncHasBeenStarted);
                    }
                    catch (UserNotFoundException e)
                    {
                        ++failureCount;
                        logger.info("Could not remove user [" + username + "] from group [" + group.getName() +"]. User was not found.", e);
                    }
                    catch (GroupNotFoundException e)
                    {
                        ++failureCount;
                        logger.info("Could not remove user [" + username + "] from group [" + group.getName() +"]. Group was not found.", e);
                    }
                    catch (MembershipNotFoundException e)
                    {
                        // This generally happens when the DAO implementation cascades user deletion to remove
                        // memberships. It's safe to ignore regardless, because the membership not existing is
                        // exactly what we were wanting to happen!
                    }
                    catch (ReadOnlyGroupException e)
                    {
                        ++failureCount;
                        logger.info("Could not remove user [" + username + "] from read-only group [" + group.getName() +"].", e);
                    }
                }
            }
            catch (DirectoryNotFoundException e)
            {
                throw new OperationFailedException(e);
            }
            finally
            {
                final int usersRemoved = usersToRemove.size() - failureCount;
                logger.info(TimerStack.pop("removed [ " + usersRemoved + " ] user members from [ " + group.getName() + " ] in [ {0} ]"));
            }
        }
    }

    public void addUserMembershipsForGroup(Group group, Set<String> usersToAdd) throws OperationFailedException
    {
        if (!usersToAdd.isEmpty())
        {
            Collection<String> failedUsernames = null;
            try
            {
                TimerStack.push();
                Directory directory = getDirectory();
                boolean initialSyncHasBeenStarted = initialSyncHasBeenStarted(directory);

                final BatchResult<String> result = internalDirectory.addAllUsersToGroup(usersToAdd, group.getName());
                failedUsernames = result.getFailedEntities();

                for (String username : result.getSuccessfulEntities())
                {
                    publishEvent(new GroupMembershipCreatedEvent(this, directory, username, group.getName(), MembershipType.GROUP_USER), initialSyncHasBeenStarted);
                }

                if (!failedUsernames.isEmpty())
                {
                    logger.warn("Could not add the following missing users to group [ " + group.getName() + " ]: " + failedUsernames);
                }
            }
            catch (GroupNotFoundException e)
            {
                logger.warn("Could not add users to group. Group [" + group.getName() + "] was not found.", e);
            }
            catch (DirectoryNotFoundException e)
            {
                throw new OperationFailedException(e);
            }
            finally
            {
                final int usersAdded = failedUsernames != null ? usersToAdd.size() - failedUsernames.size() : 0;
                logger.info(TimerStack.pop("added [ " + usersAdded + " ] user members to [ " + group.getName() + " ] in [ {0} ]"));
            }
        }
    }

    public AddRemoveSets<String> findGroupMembershipForGroupChanges(Group parentGroup, Collection<String> remoteGroups) throws OperationFailedException
    {
        logger.debug("synchronising [ " + remoteGroups.size() + " ] group members for group [ " + parentGroup.getName() + " ]");

        Set<String> groupsToAdd = new HashSet<String>();
        Set<String> groupsToRemove = new HashSet<String>();

        try
        {
            TimerStack.push();

            synchronisationStatusManager.syncStatus(getDirectoryId(), "directory.caching.sync.group.memberships", remoteGroups.size(), parentGroup.getName());
            
            List<String> internalGroups = internalDirectory.searchGroupRelationships(QueryBuilder.queryFor(String.class, EntityDescriptor.group()).childrenOf(EntityDescriptor.group()).withName(parentGroup.getName()).returningAtMost(EntityQuery.ALL_RESULTS));

            for (String remoteGroup : remoteGroups)
            {
                if (!internalGroups.contains(remoteGroup))
                    groupsToAdd.add(remoteGroup);
            }

            for (String internalGroup : internalGroups)
            {
                if (!remoteGroups.contains(internalGroup))
                    groupsToRemove.add(internalGroup);
            }
            
            return new AddRemoveSets<String>(groupsToAdd, groupsToRemove);
        }
        finally
        {
            logger.debug(TimerStack.pop("scanned and compared [ " + remoteGroups.size() + " ] group members from [ " + parentGroup.getName() + " ] in [ {0} ]"));
        }
    }
    
    public void addGroupMembershipsForGroup(Group parentGroup, Collection<String> groupsToAdd) throws OperationFailedException
    {
        if (!groupsToAdd.isEmpty())
        {
            int failureCount = 0;
            try
            {
                TimerStack.push();

                Directory directory = getDirectory();
                boolean initialSyncHasBeenStarted = initialSyncHasBeenStarted(directory);
                for (String groupname : groupsToAdd)
                {
                    try
                    {
                        internalDirectory.addGroupToGroup(groupname, parentGroup.getName());

                        publishEvent(new GroupMembershipCreatedEvent(this, directory, groupname, parentGroup.getName(), MembershipType.GROUP_GROUP), initialSyncHasBeenStarted);
                    }
                    catch (GroupNotFoundException e)
                    {
                        ++failureCount;
                        logger.warn("Could not add child group [" + groupname + "] to parent group [" + parentGroup.getName() + "]. One or both groups was not found", e);
                    }
                    catch (InvalidMembershipException e)
                    {
                        ++failureCount;
                        logger.warn("Could not add child group [" + groupname + "] to parent group [" + parentGroup.getName() + "]. Membership between child and parent group is invalid", e);
                    }
                    catch (ReadOnlyGroupException e)
                    {
                        ++failureCount;
                        logger.warn("Could not add child group [" + groupname + "] to parent group [" + parentGroup.getName() + "]. " + e.getGroupName() + " is a read-only group.", e);
                    }
                }
            }
            catch (DirectoryNotFoundException e)
            {
                throw new OperationFailedException(e);
            }
            finally
            {
                final int groupsAdded = groupsToAdd.size() - failureCount;
                logger.info(TimerStack.pop("added [ " + groupsAdded + " ] group members to [ " + parentGroup.getName() + " ] in [ {0} ]"));
            }
        }
    }

    public void removeGroupMembershipsForGroup(Group parentGroup, Collection<String> groupsToRemove) throws OperationFailedException
    {
        if (!groupsToRemove.isEmpty())
        {
            int failureCount = 0;
            try
            {
                TimerStack.push();
                Directory directory = getDirectory();
                boolean initialSyncHasBeenStarted = initialSyncHasBeenStarted(directory);
                for (String groupname : groupsToRemove)
                {
                    try
                    {
                        internalDirectory.removeGroupFromGroup(groupname, parentGroup.getName());

                        publishEvent(new GroupMembershipDeletedEvent(this, directory, groupname, parentGroup.getName(), MembershipType.GROUP_GROUP), initialSyncHasBeenStarted);
                    }
                    catch (GroupNotFoundException e)
                    {
                        ++failureCount;
                        logger.info("Could not remove child group [" + groupname + "] from parent group [" + parentGroup.getName() + "]. One or both groups was not found", e);
                    }
                    catch (InvalidMembershipException e)
                    {
                        ++failureCount;
                        logger.warn("Could not remove child group [" + groupname + "] from parent group [" + parentGroup.getName() + "]. Membership between child and parent group is invalid", e);
                    }
                    catch (MembershipNotFoundException e)
                    {
                        ++failureCount;
                        logger.warn("Could not remove child group [" + groupname + "] from parent group [" + parentGroup.getName() + "]. Membership already doesn't exist", e);
                    }
                    catch (ReadOnlyGroupException e)
                    {
                        ++failureCount;
                        logger.warn("Could not remove child group [" + groupname + "] from parent group [" + parentGroup.getName() + "]. " + e.getGroupName() + " is a read-only group.", e);
                    }
                }
            }
            catch (DirectoryNotFoundException e)
            {
                throw new OperationFailedException(e);
            }
            finally
            {
                final int groupsRemoved = groupsToRemove.size() - failureCount;
                logger.info(TimerStack.pop("removed [ " + groupsRemoved + " ] group members from [ " + parentGroup.getName() + " ] in [ {0} ]"));
            }
        }
    }

    /**
     * Returns true if the synchronisation
     * has been started at least once after directory creation or configuration
     * update.
     */
    private static boolean initialSyncHasBeenStarted(Directory directory)
    {
        return directory.getValue(SynchronisableDirectoryProperties.IS_SYNCHRONISING) != null;
    }
    
    private Directory getDirectory() throws DirectoryNotFoundException
    {
        return directoryDao.findById(getDirectoryId());
    }
    
    private long getDirectoryId()
    {
        return remoteDirectory.getDirectoryId();
    }

    private void publishEvent(DirectoryEvent event, boolean initialSyncHasBeenStarted)
    {
        // Fire event only if this is not initial synchronisation
        if (initialSyncHasBeenStarted)
        {
            eventPublisher.publish(event);
        }
    }

    /**
     * Returns true if the given remote Group should not have its memberships synchronised for any reason.
     *
     * @param remoteGroup The Group to test.
     * @return true if the given remote Group should not have its memberships synchronised for any reason.
     * @throws com.atlassian.crowd.exception.OperationFailedException
     *          If there is an error trying to find the group in the Internal Directory (should not occur).
     */
    public boolean ignoreGroupOnSynchroniseMemberships(final Group remoteGroup) throws OperationFailedException
    {
        try
        {
            // Find the version of this group in our Internal cache.
            InternalDirectoryGroup internalGroup = internalDirectory.findGroupByName(remoteGroup.getName());
            return remoteGroup.getType() == GroupType.LEGACY_ROLE && internalGroup.getType() == GroupType.GROUP || internalGroup.isLocal();
        }
        catch (GroupNotFoundException ex)
        {
            // Group does not exist locally - it IS possible someone deleted it while the synchronise is in progress.
            return true;
        }
    }

    /**
     * Returns the users that need to be added or updated given the list of all remote users. Only the internal users
     * modified before <tt>syncStartDate</tt> will be updated. This is done to avoid overriding changes made locally to
     * a user after the synchronisation has started.
     *
     * @param remoteUsers List of all remote users.
     * @param syncStartDate Date and time of the start of the synchronisation. Used to determine which users need to be
     *                      synchronised. Can be null in which case all the users are synchronised.
     * @return a pair of Sets of users to update and update.
     * @throws OperationFailedException if the operation failed for any reason
     */
    public AddUpdateSets<UserTemplateWithCredentialAndAttributes, UserTemplate> getUsersToAddAndUpdate(final Collection<? extends User> remoteUsers, final Date syncStartDate)
            throws OperationFailedException
    {
        Set<UserTemplateWithCredentialAndAttributes> usersToAdd = Sets.newHashSet();
        Set<UserTemplate> usersToUpdate = Sets.newHashSet();
        int count = 0;
        Map<String, TimestampedUser> users = findUsersUpdatedBefore(syncStartDate);

        logger.info("scanning [ {} ] users to add or update", remoteUsers.size());

        for (User remoteUser : remoteUsers)
        {
            if (remoteUsers.size() > 100 && count % 100 == 0)
            {
                logger.info("scanned [ {}% ] users", Percentage.get(count, remoteUsers.size()));
            }
            count++;
            TimestampedUser internalUser = users.get(remoteUser.getName());
            if (internalUser != null)
            {
                if (!remoteUser.getName().equals(internalUser.getName()))
                {
                    logger.warn("remote username [ {} ] casing differs from local username [ {} ]. User details will be kept updated, but the username cannot be updated", remoteUser.getName(), internalUser.getName());
                }
                if (hasChanged(remoteUser, internalUser))
                {
                    final UserTemplate userToUpdate = makeUserTemplate(remoteUser);
                    // Ensure that the username will not be updated
                    userToUpdate.setName(internalUser.getName());
                    // Ignore active flag value from remote directory and manage it locally if the remote directory does not support active flag.
                    if (!remoteDirectory.supportsInactiveAccounts())
                        userToUpdate.setActive(internalUser.isActive());
                    usersToUpdate.add(userToUpdate);
                }
                else
                {
                    logger.trace("user [ {} ] unmodified, skipping", remoteUser.getName());
                }
            }
            else
            {
                logger.debug("user [ {} ] not found, adding", remoteUser.getName());
                usersToAdd.add(new UserTemplateWithCredentialAndAttributes(makeUserTemplate(remoteUser),
                        PasswordCredential.encrypted(DbCachingRemoteDirectory.INTERNAL_USER_PASSWORD)));
            }
        }
        return new AddUpdateSets<UserTemplateWithCredentialAndAttributes, UserTemplate>(usersToAdd, usersToUpdate);
    }


    // Event operations

    public void addOrUpdateCachedUser(User user) throws OperationFailedException
    {
        final UserTemplate newUser = new UserTemplate(user);
        newUser.setDirectoryId(getDirectoryId());
        try
        {
            final Directory directory = getDirectory();
            try
            {
                final User addedUser = internalDirectory.addUser(newUser, PasswordCredential.NONE);

                publishEvent(new UserCreatedEvent(this, directory, addedUser), true);
            }
            catch (UserAlreadyExistsException e)
            {
                try
                {
                    final User updatedUser = internalDirectory.updateUser(newUser);

                    publishEvent(new UserUpdatedEvent(this, directory, updatedUser), true);
                }
                catch (UserNotFoundException unfe)
                {
                    // User must have just been deleted locally
                    logger.debug("User was deleted in the middle of the transaction", unfe);
                }
            }
            catch (InvalidCredentialException e)
            {
                throw new RuntimeException(e); // Should never happen
            }
        }
        catch (InvalidUserException e)
        {
            // Only log the error so that the synchronisation can continue
            logger.error("Could not add or update user '" + newUser.getName() + "'", e);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void deleteCachedUser(String username) throws OperationFailedException
    {
        try
        {
            internalDirectory.removeUser(username);

            publishEvent(new UserDeletedEvent(this, getDirectory(), username), true);
        }
        catch (UserNotFoundException e)
        {
            logger.debug("Deleted user does not exist locally", e);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void addOrUpdateCachedGroup(Group group) throws OperationFailedException
    {
        final GroupTemplate newGroup = new GroupTemplate(group);
        newGroup.setDirectoryId(getDirectoryId());
        try
        {
            final Directory directory = getDirectory();
            try
            {
                final Group updatedGroup = internalDirectory.updateGroup(newGroup);

                publishEvent(new GroupUpdatedEvent(this, directory, updatedGroup), true);
            }
            catch (GroupNotFoundException e)
            {
                final Group addedGroup = internalDirectory.addGroup(newGroup);

                publishEvent(new GroupCreatedEvent(this, directory, addedGroup), true);
            }
            catch (ReadOnlyGroupException e)
            {
                throw new OperationFailedException(e);
            }
        }
        catch (InvalidGroupException e)
        {
            // Only log the error so that the synchronisation can continue
            logger.error("Could not add or update group '" + newGroup.getName() + "'", e);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void deleteCachedGroup(String groupName) throws OperationFailedException
    {
        try
        {
            internalDirectory.removeGroup(groupName);

            publishEvent(new GroupDeletedEvent(this, getDirectory(), groupName), true);
        }
        catch (GroupNotFoundException e)
        {
            logger.debug("Deleted group does not exist locally", e);
        }
        catch (ReadOnlyGroupException e)
        {
            throw new OperationFailedException(e);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void addUserToGroup(String username, String groupName) throws OperationFailedException
    {
        try
        {
            internalDirectory.addUserToGroup(username, groupName);

            publishEvent(new GroupMembershipCreatedEvent(this, getDirectory(), username, groupName, MembershipType.GROUP_USER), true);
        }
        catch (GroupNotFoundException e)
        {
            logger.debug("Cannot have membership without a group", e);
        }
        catch (UserNotFoundException e)
        {
            logger.debug("Cannot have membership without a user", e);
        }
        catch (ReadOnlyGroupException e)
        {
            throw new OperationFailedException(e);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void addGroupToGroup(String childGroup, String parentGroup) throws OperationFailedException
    {
        try
        {
            internalDirectory.addGroupToGroup(childGroup, parentGroup);

            publishEvent(new GroupMembershipCreatedEvent(this, getDirectory(), childGroup, parentGroup, MembershipType.GROUP_GROUP), true);
        }
        catch (GroupNotFoundException e)
        {
            logger.debug("Cannot have membership without a group", e);
        }
        catch (InvalidMembershipException e)
        {
            logger.debug("Later events should fix this problem", e);
        }
        catch (ReadOnlyGroupException e)
        {
            throw new OperationFailedException(e);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void removeUserFromGroup(String username, String groupName) throws OperationFailedException
    {
        try
        {
            internalDirectory.removeUserFromGroup(username, groupName);

            publishEvent(new GroupMembershipDeletedEvent(this, getDirectory(), username, groupName, MembershipType.GROUP_USER), true);
        }
        catch (MembershipNotFoundException e)
        {
            logger.debug("Membership has already been removed", e);
        }
        catch (GroupNotFoundException e)
        {
            logger.debug("Cannot have membership without a group", e);
        }
        catch (UserNotFoundException e)
        {
            logger.debug("Cannot have membership without a user", e);
        }
        catch (ReadOnlyGroupException e)
        {
            throw new OperationFailedException(e);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void removeGroupFromGroup(String childGroup, String parentGroup) throws OperationFailedException
    {
        try
        {
            internalDirectory.removeGroupFromGroup(childGroup, parentGroup);

            publishEvent(new GroupMembershipDeletedEvent(this, getDirectory(), childGroup, parentGroup, MembershipType.GROUP_GROUP), true);
        }
        catch (MembershipNotFoundException e)
        {
            logger.debug("Membership has already been removed", e);
        }
        catch (GroupNotFoundException e)
        {
            logger.debug("Cannot have membership without a group", e);
        }
        catch (InvalidMembershipException e)
        {
            logger.debug("Later events should fix this problem", e);
        }
        catch (ReadOnlyGroupException e)
        {
            throw new OperationFailedException(e);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
    }

    public void syncGroupMembershipsForUser(String childUsername, Set<String> parentGroupNames) throws OperationFailedException
    {
        final Set<String> remoteParentGroupNames = toLowerCaseIdentifiers(parentGroupNames);
        final Set<String> localParentGroupNames = toLowerCaseIdentifiers(internalDirectory.searchGroupRelationships(
                QueryBuilder.queryFor(String.class, EntityDescriptor.group(GroupType.GROUP))
                        .parentsOf(EntityDescriptor.user())
                        .withName(childUsername)
                        .returningAtMost(EntityQuery.ALL_RESULTS)));

        final Set<String> addedParentGroupNames = Sets.difference(remoteParentGroupNames, localParentGroupNames);
        for (String addedParentGroupName : addedParentGroupNames)
        {
            addUserToGroup(childUsername, addedParentGroupName);
        }
        final Set<String> removedParentGroupNames = Sets.difference(localParentGroupNames, remoteParentGroupNames);
        for (String removedParentGroupName : removedParentGroupNames)
        {
            removeUserFromGroup(childUsername, removedParentGroupName);
        }
    }

    public void syncGroupMembershipsAndMembersForGroup(String groupName, Set<String> parentGroupNames, Set<String> childGroupNames) throws OperationFailedException
    {
        // Sync memberships
        final Set<String> remoteParentGroupNames = toLowerCaseIdentifiers(parentGroupNames);
        final Set<String> localParentGroupNames = toLowerCaseIdentifiers(internalDirectory.searchGroupRelationships(
                QueryBuilder.queryFor(String.class, EntityDescriptor.group(GroupType.GROUP))
                            .parentsOf(EntityDescriptor.group(GroupType.GROUP))
                            .withName(groupName)
                            .returningAtMost(EntityQuery.ALL_RESULTS)));

        final Set<String> addedParentGroupNames = Sets.difference(remoteParentGroupNames, localParentGroupNames);
        for (String addedParentGroupName : addedParentGroupNames)
        {
            addGroupToGroup(groupName, addedParentGroupName);
        }
        final Set<String> removedParentGroupNames = Sets.difference(localParentGroupNames, remoteParentGroupNames);
        for (String removedParentGroupName : removedParentGroupNames)
        {
            removeGroupFromGroup(groupName, removedParentGroupName);
        }

        // Sync members
        final Set<String> remoteChildGroupNames = toLowerCaseIdentifiers(childGroupNames);
        final Set<String> localChildGroupNames = toLowerCaseIdentifiers(internalDirectory.searchGroupRelationships(
                QueryBuilder.queryFor(String.class, EntityDescriptor.group(GroupType.GROUP))
                            .childrenOf(EntityDescriptor.group(GroupType.GROUP))
                            .withName(groupName)
                            .returningAtMost(EntityQuery.ALL_RESULTS)));

        final Set<String> addedChildGroupNames = Sets.difference(remoteChildGroupNames, localChildGroupNames);
        for (String addedChildGroupName : addedChildGroupNames)
        {
            addGroupToGroup(addedChildGroupName, groupName);
        }
        final Set<String> removedChildGroupNames = Sets.difference(localChildGroupNames, remoteChildGroupNames);
        for (String removedChildGroupName : removedChildGroupNames)
        {
            removeGroupFromGroup(removedChildGroupName, groupName);
        }
    }

    private Set<String> toLowerCaseIdentifiers(Iterable<String> identifiers)
    {
        return ImmutableSet.copyOf(Iterables.transform(identifiers, IdentifierUtils.TO_LOWER_CASE));
    }

    private void logFailures(InternalRemoteDirectory directory, final BatchResult<? extends DirectoryEntity> result)
    {
        if (result.hasFailures())
        {
            String directoryName = directory.getDescriptiveName();
            for (DirectoryEntity failedEntity : result.getFailedEntities())
            {
                logger.warn("Could not add the following entity to the directory [ {} ]: {}", directoryName, failedEntity.getName());
            }
        }
    }
}
