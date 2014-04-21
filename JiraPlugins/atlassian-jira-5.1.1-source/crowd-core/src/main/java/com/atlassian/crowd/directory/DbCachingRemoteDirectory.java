package com.atlassian.crowd.directory;

import com.atlassian.crowd.directory.hybrid.LocalGroupHandler;
import com.atlassian.crowd.directory.ldap.cache.*;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.manager.directory.SynchronisationMode;
import com.atlassian.crowd.manager.directory.SynchronisationStatusManager;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.model.group.Membership;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserConstants;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * A {@link RemoteDirectory} that provides LDAP and Crowd integration plus local storage in an internal directory
 * for LDAP user and group attributes, and local groups for LDAP and Crowd users with local caching of remote data.
 * The implementation delegates to an Remote directory for the "source of truth" and an internal directory for caching
 * and some special local storage.
 * <p/>
 * All the attributes for the directory itself (e.g. base DN, other configuration options) are stored on
 * the directory instance.
 * <p/>
 * Terminology used in this class:
 * <dl>
 * <dt>Remote user</dt><dd>A user stored in the LDAP or Remote Crowd directory</dd>
 * <dt>Remote group</dt>
 * <dd>A group stored in the LDAP or Remote Crowd directory.</dd>
 * <dt>Local group</dt>
 * <dd>A group stored in the internal directory. There must
 * not be an Remote group with the same name for a local group to exist.
 * </dl>
 */
public class DbCachingRemoteDirectory implements RemoteDirectory, SynchronisableDirectory
{
    private static final Logger log = Logger.getLogger(DbCachingRemoteDirectory.class);
    public static final String INTERNAL_USER_PASSWORD = "nopass";

    // delegations
    private final RemoteDirectory remoteDirectory;

    // helpers
    private final LocalGroupHandler localGroupHandler;
    private final InternalRemoteDirectory internalDirectory;
    private final DirectoryCacheFactory directoryCacheFactory;
    private final CacheRefresher cacheRefresher;

    public DbCachingRemoteDirectory(RemoteDirectory remoteDirectory, InternalRemoteDirectory internalDirectory, DirectoryCacheFactory directoryCacheFactory)
    {
        this.localGroupHandler = new LocalGroupHandler(internalDirectory);
        this.remoteDirectory = remoteDirectory;
        this.internalDirectory = internalDirectory;
        this.directoryCacheFactory = directoryCacheFactory;
        // If we are connected to AD, then we get a special CacheRefresher that uses the USNChanged value.
        if (remoteDirectory instanceof MicrosoftActiveDirectory)
            cacheRefresher = new UsnChangedCacheRefresher((MicrosoftActiveDirectory) remoteDirectory);
        else if (remoteDirectory instanceof RemoteCrowdDirectory)
            cacheRefresher = new EventTokenChangedCacheRefresher((RemoteCrowdDirectory) remoteDirectory);
        else
            cacheRefresher = new RemoteDirectoryCacheRefresher(remoteDirectory);

        log.debug("DBCached directory created for directory [ " + remoteDirectory.getDirectoryId() + " ]");
    }

    public long getDirectoryId()
    {
        return remoteDirectory.getDirectoryId();
    }

    public void setDirectoryId(long directoryId)
    {
        throw new UnsupportedOperationException("You cannot mutate the directoryID of " + this.getClass().getName());
    }

    public String getDescriptiveName()
    {
        return remoteDirectory.getDescriptiveName();
    }

    public void setAttributes(Map<String, String> attributes)
    {
        throw new UnsupportedOperationException("You cannot mutate the attributes of " + this.getClass().getName());
    }

    public User findUserByName(String name) throws UserNotFoundException, OperationFailedException
    {
        return internalDirectory.findUserByName(name);
    }

    public UserWithAttributes findUserWithAttributesByName(String name) throws UserNotFoundException, OperationFailedException
    {
        return internalDirectory.findUserWithAttributesByName(name);
    }

    public User authenticate(String name, PasswordCredential credential) throws UserNotFoundException, InactiveAccountException, InvalidAuthenticationException, ExpiredCredentialException, OperationFailedException
    {
        if (remoteDirectory instanceof RemoteCrowdDirectory)
        {
            // No need to do special processing when we know the remote directory will do it for us.
            return authenticateAndEnsureInternalUserExists(name, credential);
        } else
        {
            return performAuthenticationAndUpdateAttributes(name, credential);
        }
    }

    @Override
    public Iterable<Membership> getMemberships() throws OperationFailedException
    {
        return internalDirectory.getMemberships();
    }

    /**
     * Performs authentication and updates user authentication attributes while not allowing inactive users to log in.
     *
     * @param name       name of the user
     * @param credential credential of the user
     * @return authenticated user
     * @throws UserNotFoundException          if the user does not exist
     * @throws ExpiredCredentialException     if the password has expired and the user is required to change their password
     * @throws InactiveAccountException       if the user account is not active
     * @throws InvalidAuthenticationException if the user name/password combination is invalid
     * @throws OperationFailedException       if the operation failed for any other reason
     */
    private User performAuthenticationAndUpdateAttributes(String name, PasswordCredential credential)
            throws UserNotFoundException, ExpiredCredentialException, InactiveAccountException, OperationFailedException, InvalidAuthenticationException
    {
        final Map<String, Set<String>> attributesToUpdate = new HashMap<String, Set<String>>();
        try
        {
            final User authenticatedUser = authenticateAndEnsureInternalUserExists(name, credential);

            // If remote directory does not handle inactive accounts, do it manually using the internal directory.
            if (!remoteDirectory.supportsInactiveAccounts())
            {
                User internalUser = internalDirectory.findUserByName(name);
                if (!internalUser.isActive())
                {
                    throw new InactiveAccountException(name);
                }
            }

            // authentication worked fine, set the invalid attempts to 0
            attributesToUpdate.put(UserConstants.INVALID_PASSWORD_ATTEMPTS, Collections.singleton(Long.toString(0L)));

            // update the last password authentication
            attributesToUpdate.put(UserConstants.LAST_AUTHENTICATED, Collections.singleton(Long.toString(System.currentTimeMillis())));

            storeUserAttributes(name, attributesToUpdate);

            return authenticatedUser;
        } catch (InvalidAuthenticationException e)
        {
            final UserWithAttributes user = findUserWithAttributesByName(name);
            long currentInvalidAttempts = NumberUtils.toLong(user.getValue(UserConstants.INVALID_PASSWORD_ATTEMPTS), 0L);

            // The user has entered incorrect password details
            // increment the invalid password attempts
            currentInvalidAttempts++;

            // set this on the principal object
            attributesToUpdate.put(UserConstants.INVALID_PASSWORD_ATTEMPTS, Collections.singleton(Long.toString(currentInvalidAttempts)));

            storeUserAttributes(name, attributesToUpdate);

            throw e;
        }
    }

    /**
     * Authenticates user and ensures that internal directory contains the
     * authenticated user.
     * <p/>
     * Does nothing if the user exists in the internal directory. Otherwise
     * tries to add the user and the user's memberships in the internal
     * directory.
     * <p/>
     * User might not exist in the internal directory yet because the user was
     * added after the latest synchronisation. This happens for example when
     * the remote directory uses delegated authentication, and creates users on
     * successful authentication.
     *
     * @param name       of the user
     * @param credential credential of the user
     * @return The populated user if the authentication is valid.
     * @throws UserNotFoundException          if the user does not exist
     * @throws ExpiredCredentialException     if the password has expired and the user is required to change their password
     * @throws InactiveAccountException       if the user account is not active
     * @throws InvalidAuthenticationException if the user name/password combination is invalid
     * @throws OperationFailedException       if the operation failed for any other reason
     */
    private User authenticateAndEnsureInternalUserExists(String name, PasswordCredential credential) throws UserNotFoundException, InactiveAccountException, InvalidAuthenticationException, ExpiredCredentialException, OperationFailedException
    {
        final User user = remoteDirectory.authenticate(name, credential);
        try
        {
            internalDirectory.findUserByName(user.getName());
        } catch (UserNotFoundException e)
        {
            try
            {
                addInternalUser(user);

                final List<String> groupNames = remoteDirectory.searchGroupRelationships(QueryBuilder
                        .queryFor(String.class, EntityDescriptor.group())
                        .parentsOf(EntityDescriptor.user())
                        .withName(user.getName())
                        .returningAtMost(EntityQuery.ALL_RESULTS));

                for (String groupName : groupNames)
                {
                    try
                    {
                        internalDirectory.addUserToGroup(user.getName(), groupName);
                    } catch (GroupNotFoundException e1)
                    {
                        // Group does not exist yet in the internal directory. This will get fixed with the next sync.
                    } catch (ReadOnlyGroupException e1)
                    {
                        // Internal directory should never throw ReadOnlyGroupException
                        throw new RuntimeException("Internal directory should never throw ReadOnlyGroupException", e1);
                    }
                }
            } catch (InvalidUserException e1)
            {
                throw new OperationFailedException(e1);
            } catch (InvalidCredentialException e1)
            {
                throw new OperationFailedException(e1);
            } catch (UserAlreadyExistsException e1)
            {
                // Someone was quicker than us
            }
        }

        return user;
    }

    public User addUser(UserTemplate user, PasswordCredential credential)
            throws InvalidUserException, InvalidCredentialException, UserAlreadyExistsException, OperationFailedException
    {
        User addedUser = remoteDirectory.addUser(user, credential);

        // if successful
        return addInternalUser(addedUser);
    }

    private User addInternalUser(User user)
            throws InvalidUserException, InvalidCredentialException, UserAlreadyExistsException, OperationFailedException
    {
        return internalDirectory.addUser(new UserTemplate(user), PasswordCredential.encrypted(INTERNAL_USER_PASSWORD));
    }

    public User updateUser(UserTemplate user) throws InvalidUserException, UserNotFoundException, OperationFailedException
    {
        User updatedUser = remoteDirectory.updateUser(user);

        // if successful

        final UserTemplate updatedUserTemplate = new UserTemplate(updatedUser);
        if (!remoteDirectory.supportsInactiveAccounts())
        {
            updatedUserTemplate.setActive(user.isActive());
        }

        return internalDirectory.updateUser(updatedUserTemplate);
    }

    public void updateUserCredential(String username, PasswordCredential credential) throws UserNotFoundException, InvalidCredentialException, OperationFailedException
    {
        remoteDirectory.updateUserCredential(username, credential);
    }

    public User renameUser(String oldName, String newName) throws UserNotFoundException, InvalidUserException
    {
        throw new UnsupportedOperationException("Renaming users is not supported");
    }

    public void storeUserAttributes(String username, Map<String, Set<String>> attributes) throws UserNotFoundException, OperationFailedException
    {
        internalDirectory.storeUserAttributes(username, attributes);
    }

    public void removeUserAttributes(String username, String attributeName) throws UserNotFoundException, OperationFailedException
    {
        internalDirectory.removeUserAttributes(username, attributeName);
    }

    public void removeUser(String name) throws UserNotFoundException, OperationFailedException
    {
        try
        {
            remoteDirectory.removeUser(name);
        } catch (UserNotFoundException ex)
        {
            // Looks like some one else did it on the server already - remove from cache
            internalDirectory.removeUser(name);
            throw ex;
        }
        internalDirectory.removeUser(name);
    }

    public <T> List<T> searchUsers(EntityQuery<T> query) throws OperationFailedException
    {
        return internalDirectory.searchUsers(query);
    }

    public Group findGroupByName(String name) throws GroupNotFoundException, OperationFailedException
    {
        return internalDirectory.findGroupByName(name);
    }

    public GroupWithAttributes findGroupWithAttributesByName(String name) throws GroupNotFoundException, OperationFailedException
    {
        return internalDirectory.findGroupWithAttributesByName(name);
    }

    public Group addGroup(GroupTemplate group)
            throws InvalidGroupException, OperationFailedException
    {
        if (localGroupHandler.isLocalGroupsEnabled())
        {
            if (isRemoteGroup(group.getName()))
            {
                throw new InvalidGroupException(group, "Group already exists in the Remote Directory");
            }

            try
            {
                return localGroupHandler.createLocalGroup(makeGroupTemplate(group));
            } catch (DirectoryNotFoundException e)
            {
                throw new OperationFailedException(e);
            }
        } else
        {
            // Add to the server
            Group addedGroup = remoteDirectory.addGroup(group);

            // now update the cache
            return internalDirectory.addGroup(new GroupTemplate(addedGroup));
        }
    }

    public Group updateGroup(GroupTemplate group)
            throws InvalidGroupException, GroupNotFoundException, OperationFailedException, ReadOnlyGroupException
    {
        if (localGroupHandler.isLocalGroupsEnabled())
        {
            if (isRemoteGroup(group.getName()))
            {
                throw new ReadOnlyGroupException(group.getName());
            }

            return localGroupHandler.updateLocalGroup(makeGroupTemplate(group));
        } else
        {
            // Update on server
            Group updatedGroup = remoteDirectory.updateGroup(group);
            // now update the cache
            return internalDirectory.updateGroup(new GroupTemplate(updatedGroup));
        }
    }

    public Group renameGroup(String oldName, String newName) throws GroupNotFoundException, InvalidGroupException
    {
        // don't even support it for local groups until we can properly support it in LDAP / Crowd too
        throw new UnsupportedOperationException("Renaming groups is not supported");
    }

    public void storeGroupAttributes(String groupName, Map<String, Set<String>> attributes) throws GroupNotFoundException, OperationFailedException
    {
        internalDirectory.storeGroupAttributes(groupName, attributes);
    }

    public void removeGroupAttributes(String groupName, String attributeName) throws GroupNotFoundException, OperationFailedException
    {
        internalDirectory.removeGroupAttributes(groupName, attributeName);
    }

    public void removeGroup(String name) throws GroupNotFoundException, OperationFailedException, ReadOnlyGroupException
    {
        if (localGroupHandler.isLocalGroupsEnabled())
        {
            if (isRemoteGroup(name))
            {
                throw new ReadOnlyGroupException(name);
            }
            internalDirectory.removeGroup(name);
        } else
        {
            try
            {
                remoteDirectory.removeGroup(name);
            } catch (GroupNotFoundException e)
            {
                // Clear our cache anyway.
                internalDirectory.removeGroup(name);
                throw e;
            }
            internalDirectory.removeGroup(name);
        }
    }

    /**
     * This method avoids using exception handling for the case where you don't actually need a reference to
     * the resulting group. It is an expensive lookup however, so only use it where you're replacing a call to
     * findGroupByName in a try-catch block.
     *
     * @param groupName name of the group.
     * @return true if the group exists in the Remote directory, false if the group doesn't exist in the Remote directory (it may exist elsewhere).
     * @throws com.atlassian.crowd.exception.OperationFailedException
     *          badness.
     */
    private boolean isRemoteGroup(String groupName) throws OperationFailedException
    {
        try
        {
            remoteDirectory.findGroupByName(groupName);
            return true;
        } catch (GroupNotFoundException e)
        {
            return false;
        }
    }

    public <T> List<T> searchGroups(EntityQuery<T> query) throws OperationFailedException
    {
        return internalDirectory.searchGroups(query);
    }

    public boolean isUserDirectGroupMember(String username, String groupName) throws OperationFailedException
    {
        return internalDirectory.isUserDirectGroupMember(username, groupName);
    }

    public boolean isGroupDirectGroupMember(String childGroup, String parentGroup) throws OperationFailedException
    {
        return internalDirectory.isGroupDirectGroupMember(childGroup, parentGroup);
    }

    public void addUserToGroup(String username, String groupName)
            throws GroupNotFoundException, UserNotFoundException, OperationFailedException, ReadOnlyGroupException
    {
        if (localGroupHandler.isLocalGroupsEnabled())
        {
            if (isRemoteGroup(groupName))
            {
                throw new ReadOnlyGroupException(groupName);
            }

            localGroupHandler.addUserToLocalGroup(username, groupName);
        } else
        {
            // add membership on server
            remoteDirectory.addUserToGroup(username, groupName);
            // update the cache
            internalDirectory.addUserToGroup(username, groupName);
        }
    }

    public void addGroupToGroup(String childGroup, String parentGroup)
            throws GroupNotFoundException, InvalidMembershipException, OperationFailedException, ReadOnlyGroupException
    {
        if (localGroupHandler.isLocalGroupsEnabled())
        {
            if (isRemoteGroup(parentGroup))
            {
                throw new ReadOnlyGroupException(parentGroup);
            }
            if (isRemoteGroup(childGroup))
            {
                throw new ReadOnlyGroupException(childGroup);
            }

            localGroupHandler.addLocalGroupToLocalGroup(childGroup, parentGroup);
        } else
        {
            // add membership on server
            remoteDirectory.addGroupToGroup(childGroup, parentGroup);
            // update the cache
            internalDirectory.addGroupToGroup(childGroup, parentGroup);
        }
    }

    public void removeUserFromGroup(String username, String groupName)
            throws GroupNotFoundException, UserNotFoundException, MembershipNotFoundException, OperationFailedException, ReadOnlyGroupException
    {
        if (localGroupHandler.isLocalGroupsEnabled())
        {
            if (isRemoteGroup(groupName))
            {
                throw new ReadOnlyGroupException(groupName);
            }

            localGroupHandler.removeUserFromLocalGroup(username, groupName);
        } else
        {
            // remove membership on server
            remoteDirectory.removeUserFromGroup(username, groupName);
            // update the cache
            internalDirectory.removeUserFromGroup(username, groupName);
        }
    }

    public void removeGroupFromGroup(String childGroup, String parentGroup)
            throws GroupNotFoundException, InvalidMembershipException, MembershipNotFoundException, OperationFailedException, ReadOnlyGroupException
    {
        if (localGroupHandler.isLocalGroupsEnabled())
        {
            if (isRemoteGroup(parentGroup))
            {
                throw new ReadOnlyGroupException(parentGroup);
            }
            if (isRemoteGroup(childGroup))
            {
                throw new ReadOnlyGroupException(childGroup);
            }

            localGroupHandler.removeLocalGroupFromLocalGroup(childGroup, parentGroup);
        } else
        {
            // remove membership on server
            remoteDirectory.removeGroupFromGroup(childGroup, parentGroup);
            // update the cache
            internalDirectory.removeGroupFromGroup(childGroup, parentGroup);
        }
    }

    public <T> List<T> searchGroupRelationships(MembershipQuery<T> query) throws OperationFailedException
    {
        return internalDirectory.searchGroupRelationships(query);
    }

    public void testConnection() throws OperationFailedException
    {
        remoteDirectory.testConnection();
    }

    /**
     * This implementation will store the active flag locally in the internal directory if the active flag cannot be
     * persisted on the underlying remote directory.
     *
     * @return true if the internal directory supports inactive accounts (which it should always do).
     */
    public boolean supportsInactiveAccounts()
    {
        return internalDirectory.supportsInactiveAccounts();
    }

    public boolean supportsNestedGroups()
    {
        return remoteDirectory.supportsNestedGroups();
    }

    public boolean isRolesDisabled()
    {
        return remoteDirectory.isRolesDisabled();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Implementation of Attributes
    // -----------------------------------------------------------------------------------------------------------------

    public Set<String> getValues(final String name)
    {
        return remoteDirectory.getValues(name);
    }

    public String getValue(final String name)
    {
        return remoteDirectory.getValue(name);
    }

    public boolean isEmpty()
    {
        return remoteDirectory.isEmpty();
    }

    public Set<String> getKeys()
    {
        return remoteDirectory.getKeys();
    }

    public void synchroniseCache(SynchronisationMode mode, SynchronisationStatusManager synchronisationStatusManager) throws OperationFailedException
    {
        final long directoryId = getDirectoryId();
        SynchronisationMode synchronisedMode = null;
        TimerStack.push();
        try
        {
            log.info("synchronisation for directory [ " + directoryId + " ] starting");
            DirectoryCache directoryCache = directoryCacheFactory.createDirectoryCache(remoteDirectory, internalDirectory);
            if (mode == SynchronisationMode.INCREMENTAL)
            {
                // Only sync the delta
                synchronisationStatusManager.syncStatus(directoryId, "directory.caching.sync.incremental");
                if (cacheRefresher.synchroniseChanges(directoryCache))
                {
                    synchronisedMode = SynchronisationMode.INCREMENTAL;
                }
            }

            if (synchronisedMode == null)
            {
                // Full sync
                synchronisationStatusManager.syncStatus(directoryId, "directory.caching.sync.full");
                cacheRefresher.synchroniseAll(directoryCache);
                synchronisedMode = SynchronisationMode.FULL;
            }
        } finally
        {
            if (synchronisedMode != null)
            {
                log.info(TimerStack.pop(synchronisedMode + " synchronisation complete in [ {0} ]"));
                synchronisationStatusManager.syncStatus(directoryId, "directory.caching.sync.completed." + synchronisedMode);
            } else
            {
                log.info(TimerStack.pop("failed synchronisation complete in [ {0} ]"));
                synchronisationStatusManager.syncStatus(directoryId, "directory.caching.sync.completed.error");
            }

        }
    }

    @Override
    public RemoteDirectory getAuthoritativeDirectory()
    {
        return remoteDirectory;
    }

    private GroupTemplate makeGroupTemplate(Group group)
    {
        GroupTemplate template = new GroupTemplate(group);
        template.setDescription(group.getDescription());
        return template;
    }
}
