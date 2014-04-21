package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.event.group.AutoGroupCreatedEvent;
import com.atlassian.crowd.event.group.AutoGroupMembershipCreatedEvent;
import com.atlassian.crowd.event.group.AutoGroupMembershipDeletedEvent;
import com.atlassian.crowd.event.user.AutoUserCreatedEvent;
import com.atlassian.crowd.event.user.AutoUserUpdatedEvent;
import com.atlassian.crowd.exception.CrowdException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidMembershipException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.OperationNotSupportedException;
import com.atlassian.crowd.exception.ReadOnlyGroupException;
import com.atlassian.crowd.exception.UserAlreadyExistsException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.model.group.InternalDirectoryGroup;
import com.atlassian.crowd.model.group.Membership;
import com.atlassian.crowd.model.membership.MembershipType;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.event.api.EventPublisher;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This implementation of a {@link RemoteDirectory} provides delegated
 * authentication to an underlying remote LDAP implementation.
 * <p/>
 * In essence this means that a User's groups and roles are managed
 * internally to Crowd and only authentication is delegated to the
 * LDAP directory.
 * <p/>
 * Users, group and memberships exist in an internal directory and all
 * query and mutation operations execute on the internal directory.
 * <p/>
 * For a user to successfully authenticate, they must exist in LDAP
 * and must authenticate against LDAP. Passwords are not stored
 * internally.
 * <p/>
 * If the {@link #ATTRIBUTE_CREATE_USER_ON_AUTH} attribute is
 * enabled, the delegated authentication directory will automatically
 * create the user in the internal portion of this directory, once they
 * successfully authenticate against LDAP. The initial user details, in
 * this case, will be obtained from LDAP.
 * <p/>
 * If the {@link #ATTRIBUTE_UPDATE_USER_ON_AUTH} attribute is
 * enabled, the delegated authentication directory will also update
 * the user's details from LDAP automatically whenever they
 * authenticate. The same behaviour will happen if the attribute is not
 * enabled and the user is deleted internally and then re-authenticates.
 * <p/>
 * If the create-on-auth option is not enabled, then users must always
 * be manually created in this directory, before they can authenticate
 * against LDAP. In this scenario, the user details will never be retrieved
 * from LDAP. This is OSUser's default LDAP behaviour.
 */
public class DelegatedAuthenticationDirectory implements RemoteDirectory
{
    private static final Logger logger = LoggerFactory.getLogger(DelegatedAuthenticationDirectory.class);

    public static final String ATTRIBUTE_CREATE_USER_ON_AUTH = "crowd.delegated.directory.auto.create.user";
    public static final String ATTRIBUTE_UPDATE_USER_ON_AUTH = "crowd.delegated.directory.auto.update.user";
    public static final String ATTRIBUTE_LDAP_DIRECTORY_CLASS = "crowd.delegated.directory.type";
    public static final String ATTRIBUTE_KEY_IMPORT_GROUPS = "crowd.delegated.directory.importGroups";

    private final RemoteDirectory ldapDirectory;
    private final RemoteDirectory internalDirectory;
    private final EventPublisher eventPublisher;
    private final DirectoryDao directoryDao;

    public DelegatedAuthenticationDirectory(RemoteDirectory ldapDirectory, RemoteDirectory internalDirectory, EventPublisher eventPublisher, DirectoryDao directoryDao)
    {
        this.ldapDirectory = ldapDirectory;
        this.internalDirectory = internalDirectory;
        this.eventPublisher = eventPublisher;
        this.directoryDao = directoryDao;
    }

    public long getDirectoryId()
    {
        return internalDirectory.getDirectoryId();
    }

    public void setDirectoryId(long directoryId)
    {
        throw new UnsupportedOperationException("You cannot mutate the directoryID of " + this.getClass().getName());
    }

    public String getDescriptiveName()
    {
        return "Delegated Authentication Directory";
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

    /**
     * In addition to the normal authentication behaviour, following a successful
     * authentication the following may occur:
     * <ul>
     * <li>If the user does not exist in the internal directory and
     * {@link #ATTRIBUTE_CREATE_USER_ON_AUTH} is enabled, the user's details
     * will be added to the internal directory.</li>
     * <li>If the user exists in the internal directory and
     * {@link #ATTRIBUTE_UPDATE_USER_ON_AUTH} is enabled, the user's details
     * will be updated in the internal directory.</li>
     * </ul>
     * A user marked as inactive locally will not be authenticated, retrieved or
     * updated from the LDAP server.
     *
     * @see RemoteDirectory#authenticate(String, PasswordCredential)
     */
    public User authenticate(String name, PasswordCredential credential) throws UserNotFoundException, InactiveAccountException, InvalidAuthenticationException, ExpiredCredentialException, OperationFailedException
    {
        User user;

        try
        {
            // find the user internally
            user = findUserByName(name);

            // user must be active locally to be considered for remote authentication.
            if (user.isActive())
            {
                // authenticate the user against LDAP
                try
                {
                    User ldapUser = ldapDirectory.authenticate(name, credential);

                    if (isUserUpdateOnAuthEnabled())
                    {
                        // update user details from LDAP in case they've changed
                        user = updateLdapUser(ldapUser, user);
                    }
                    if (isImportGroupsEnabled())
                    {
                        updateGroups(ldapUser, user);
                    }
                } catch (InvalidUserException e)
                {
                    // couldn't add the user because it was declared invalid by the internal directory
                    throw new InvalidAuthenticationException("Failed to clone LDAP user <" + name + "> to internal directory", e);
                } catch (UserNotFoundException e)
                {
                    /*
                     * User exists in the local directory, but has been removed
                     * from the LDAP directory. We should leave removing this
                     * user to the administrators.
                     */
                    throw new InvalidAuthenticationException("Failed to authenticate principal, no credentials in authenticating delegate directory");
                }
            } else
            {
                throw new InactiveAccountException(user.getName());
            }
        } catch (UserNotFoundException e)
        {
            // user doesn't exist internally
            if (isUserCreateOnAuthEnabled())
            {
                // authenticate against LDAP
                User ldapUser = ldapDirectory.authenticate(name, credential);

                // and clone the LDAP user internally
                try
                {
                    user = addLdapUser(ldapUser);
                } catch (InvalidUserException e1)
                {
                    // couldn't add the user because it was declared invalid by the internal directory
                    throw new InvalidAuthenticationException("Failed to clone LDAP user <" + name + "> to internal directory", e1);
                } catch (UserAlreadyExistsException e1)
                {
                    // This just appears that somebody else has just created the user locally.
                    // In this case, we can simply return the newly created user.
                    if (logger.isInfoEnabled())
                    {
                        logger.info("User " + name + " could not be found initially, but when cloning the user internally, user exists");
                    }

                    // whatever exception thrown from the following line reflects the latest state of the system.
                    // If the user has been suddenly removed, for example, then UserNotFoundException will bubble up.
                    user = findUserByName(name);

                    // since the suddenly appearing user could have been added by either cloning from ldap or
                    // simply by UI, there's a possibility that it's actually inactive. In which case,
                    // we make the decision based on the best available information.
                    if (!user.isActive())
                    {
                        throw new InactiveAccountException(user.getName());
                    }

                    // if the user is active then we can just return the newly created (by somebody else) user
                    // since we have already authenticated the account against the remote LDAP.
                    return user;
                }
            } else
            {
                // if user auto-create is disabled, throw the UNFE immediately
                throw e;
            }
        }

        return user;
    }

    /**
     * Copies or updates a user in the internal directory from their counterpart in the LDAP directory.
     * Used by custom authenticators to ensure users exist when external authentication mechanisms
     * just provide us with just a username.
     *
     * @param name the username of the user to copy
     * @return the newly updated internal user
     * @throws UserNotFoundException    if no user with the given username exists in LDAP
     * @throws OperationFailedException if there was a problem communicating with the LDAP server or the user
     *                                  could not be cloned to the internal directory
     */
    public User addOrUpdateLdapUser(String name) throws UserNotFoundException, OperationFailedException
    {
        User ldapUser = ldapDirectory.findUserByName(name);

        try
        {
            User internalUser = internalDirectory.findUserByName(name);
            User updatedUser = updateLdapUser(ldapUser, internalUser);
            if (isImportGroupsEnabled())
            {
                updateGroups(ldapUser, internalUser);
            }
            return updatedUser;
        } catch (InvalidUserException e)
        {
            throw new OperationFailedException(name, e);
        } catch (UserNotFoundException e)
        {
            // user doesn't exist internally -- fall through to add them
        }

        try
        {
            return addLdapUser(ldapUser);
        } catch (UserAlreadyExistsException e)
        {
            if (logger.isInfoEnabled())
            {
                logger.info("User was added during the internal cloning process. Returning found user.");
            }
            return findUserByName(name);
        } catch (InvalidUserException e)
        {
            throw new OperationFailedException(name, e);
        }
    }

    private User addLdapUser(User user) throws OperationFailedException, InvalidUserException, UserAlreadyExistsException
    {
        try
        {
            final User createdUser = addUser(new UserTemplate(user), null);
            final Directory dir = directoryDao.findById(createdUser.getDirectoryId());
            eventPublisher.publish(new AutoUserCreatedEvent(this, dir, createdUser));

            if (isImportGroupsEnabled())
            {
                importGroups(user, dir);
            }

            return createdUser;
        } catch (InvalidCredentialException e)
        {
            throw new OperationFailedException("Could not create authenticated user <" + user.getName() + "> " +
                    "in underlying InternalDirectory: " + e.getMessage(), e);
        } catch (DirectoryNotFoundException e)
        {
            // directory not found
            throw new ConcurrentModificationException("Directory mapping was removed while cloning a user: " + e.getMessage());
        }
    }

    private void importGroups(final User user, final Directory dir)
    {
        try
        {
            importGroups(user, dir, getGroups(user, ldapDirectory, String.class));
        } catch (final CrowdException exception)
        {
            logger.error("Could not import remote group memberships of user \"" + user.getName() + "\" in directory \"" + getDescriptiveName() + "\".", exception);
        }
    }

    private void importGroups(final User user, final Directory dir, final Iterable<String> groupNames) throws OperationFailedException
    {
        for (final String groupName : groupNames)
        {
            try
            {
                final InternalDirectoryGroup group = (InternalDirectoryGroup) internalDirectory.findGroupByName(groupName);
				if (group.isLocal())
                {
                    logger.warn("Remote group \"" + groupName + "\" in directory \"" + getDescriptiveName() + "\" is shadowed by a local group of the same name and will not be imported.");
                } else
                {
                    logger.debug("Remote group \"" + groupName + "\" in directory \"" + getDescriptiveName() + "\" has already been imported.");
                    importMembership(user, groupName, dir);
                }
            } catch (final GroupNotFoundException exception)
            {
                try
                {
                    final GroupTemplate groupTemplate = new GroupTemplate(groupName, internalDirectory.getDirectoryId());
                    groupTemplate.setLocal(false);
                    final Group createdGroup = internalDirectory.addGroup(groupTemplate);
                    logger.info("Imported remote group \"" + groupName + "\" to directory \"" + getDescriptiveName() + "\".");
                    eventPublisher.publish(new AutoGroupCreatedEvent(this, dir, createdGroup));
                    importMembership(user, groupName, dir);
                } catch (final Exception exception2)
                {
                    logger.error("Could not import remote group \"" + groupName + "\" in directory \"" + getDescriptiveName() + "\".", exception2);
                }
            }
        }
    }

    private void importMembership(final User user, final String groupName, final Directory dir)
    {
        try
        {
            addUserToGroup(user.getName(), groupName);
            logger.info("Imported user \"" + user.getName() + "\"'s membership of remote group \"" + groupName + "\" to directory \"" + getDescriptiveName() + "\".");
            eventPublisher.publish(new AutoGroupMembershipCreatedEvent(this, dir, user.getName(), groupName, MembershipType.GROUP_USER));
        } catch (final Exception exception)
        {
            logger.error("Could not import user \"" + user.getName() + "\"'s membership of remote group \"" + groupName + "\" to directory \"" + getDescriptiveName() + "\".", exception);
        }
    }

    private User updateLdapUser(User ldapUser, User internalUser) throws InvalidUserException, OperationFailedException
    {
        try
        {
            final UserTemplate template = new UserTemplate(ldapUser);
            // maintain active/inactive state from internal directory
            template.setActive(internalUser.isActive());
            if (!ldapUser.getName().equals(internalUser.getName()))
            {
                logger.warn("remote username [ {} ] casing differs from local username [ {} ]. User details will be kept updated, but the username cannot be updated", ldapUser.getName(), internalUser.getName());
                template.setName(internalUser.getName());
            }
            final User updatedUser = updateUser(template);
            final Directory dir = directoryDao.findById(updatedUser.getDirectoryId());
            eventPublisher.publish(new AutoUserUpdatedEvent(this, dir, updatedUser));

            return updatedUser;
        } catch (UserNotFoundException e)
        {
            throw new ConcurrentModificationException("User was removed during cloning process: " + e.getMessage());
        } catch (DirectoryNotFoundException e)
        {
            // directory not found
            throw new ConcurrentModificationException("Directory mapping was removed while cloning a user: " + e.getMessage());
        }
    }

    private void updateGroups(final User ldapUser, final User internalUser)
    {
        try
        {
            final Directory dir = directoryDao.findById(ldapUser.getDirectoryId());
            final Set<String> ldapGroupNames = Sets.newHashSet(getGroups(ldapUser, ldapDirectory, String.class));
            final Map<String, Group> internalGroupsMap = Maps.uniqueIndex(getGroups(internalUser, internalDirectory, Group.class), new Function<Group, String>()
            {

                @Override
                public final String apply(final Group group)
                {
                    return group.getName();
                }

            });
            final Set<String> internalGroupNames = internalGroupsMap.keySet();
            for (final String groupName : Sets.difference(internalGroupNames, ldapGroupNames))
            {
                if (!((InternalDirectoryGroup) internalGroupsMap.get(groupName)).isLocal())
                {
                    try
                    {
                        removeUserFromGroup(internalUser.getName(), groupName);
                        eventPublisher.publish(new AutoGroupMembershipDeletedEvent(this, dir, internalUser.getName(), groupName, MembershipType.GROUP_USER));
                        logger.info("Deleted user \"" + internalUser.getName() + "\"'s imported membership of remote group \"" + groupName + "\" to directory \"" + getDescriptiveName() + "\".");
                    } catch (final Exception exception)
                    {
                        logger.error("Could not delete user \"" + internalUser.getName() + "\"'s imported membership of remote group \"" + groupName + "\" to directory \"" + getDescriptiveName() + "\".", exception);
                    }
                }
            }
            importGroups(internalUser, dir, Sets.difference(ldapGroupNames, internalGroupNames));
        }
        catch (DirectoryNotFoundException e)
        {
            // directory not found
            throw new ConcurrentModificationException("Directory mapping was removed while updating the groups of a user " + e.getMessage());
        }
        catch (final Exception exception)
        {
            logger.error("Could not update remote group imported memberships of user \"" + internalUser.getName() + "\" in directory \"" + getDescriptiveName() + "\".", exception);
        }
    }

    private <T> List<T> getGroups(final User user, final RemoteDirectory directory, final Class<T> returnType) throws OperationFailedException
    {
        return directory.searchGroupRelationships(QueryBuilder.queryFor(returnType, EntityDescriptor.group()).parentsOf(EntityDescriptor.user()).withName(user.getName()).returningAtMost(EntityQuery.ALL_RESULTS));
    }

    public User addUser(UserTemplate user, PasswordCredential credential)
            throws InvalidUserException, InvalidCredentialException, UserAlreadyExistsException, OperationFailedException
    {
        return internalDirectory.addUser(user, credential);
    }

    public User updateUser(UserTemplate user) throws InvalidUserException, UserNotFoundException, OperationFailedException
    {
        return internalDirectory.updateUser(user);
    }

    public void updateUserCredential(String username, PasswordCredential credential) throws UserNotFoundException, InvalidCredentialException, OperationFailedException
    {
        throw new OperationNotSupportedException("Passwords are stored in LDAP and are read-only for delegated authentication directory");
    }

    public User renameUser(String oldName, String newName)
            throws UserNotFoundException, InvalidUserException, UserAlreadyExistsException, OperationFailedException
    {
        return internalDirectory.renameUser(oldName, newName);
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
    	group.setLocal(true);
        return internalDirectory.addGroup(group);
    }

    public Group updateGroup(GroupTemplate group)
            throws InvalidGroupException, GroupNotFoundException, OperationFailedException, ReadOnlyGroupException
    {
        return internalDirectory.updateGroup(group);
    }

    public Group renameGroup(String oldName, String newName) throws GroupNotFoundException, InvalidGroupException, OperationFailedException
    {
        return internalDirectory.renameGroup(oldName, newName);
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
        internalDirectory.removeGroup(name);
    }

    public <T> List<T> searchGroups(EntityQuery<T> query) throws OperationFailedException
    {
        return internalDirectory.searchGroups(query);
    }

    public boolean isUserDirectGroupMember(String username, String groupName) throws OperationFailedException
    {
        return internalDirectory.isUserDirectGroupMember(username, groupName);
    }

    public boolean isGroupDirectGroupMember(String childGroup, String parentGroup)
            throws OperationFailedException
    {
        return internalDirectory.isGroupDirectGroupMember(childGroup, parentGroup);
    }

    public void addUserToGroup(String username, String groupName)
            throws GroupNotFoundException, UserNotFoundException, OperationFailedException, ReadOnlyGroupException
    {
        internalDirectory.addUserToGroup(username, groupName);
    }

    public void addGroupToGroup(String childGroup, String parentGroup)
            throws GroupNotFoundException, InvalidMembershipException, OperationFailedException, ReadOnlyGroupException
    {
        internalDirectory.addGroupToGroup(childGroup, parentGroup);
    }

    public void removeUserFromGroup(String username, String groupName)
            throws GroupNotFoundException, UserNotFoundException, MembershipNotFoundException, OperationFailedException, ReadOnlyGroupException
    {
        internalDirectory.removeUserFromGroup(username, groupName);
    }

    public void removeGroupFromGroup(String childGroup, String parentGroup)
            throws GroupNotFoundException, InvalidMembershipException, MembershipNotFoundException, OperationFailedException, ReadOnlyGroupException
    {
        internalDirectory.removeGroupFromGroup(childGroup, parentGroup);
    }

    public <T> List<T> searchGroupRelationships(MembershipQuery<T> query) throws OperationFailedException
    {
        return internalDirectory.searchGroupRelationships(query);
    }

    public void testConnection() throws OperationFailedException
    {
        ldapDirectory.testConnection();
    }

    public boolean supportsInactiveAccounts()
    {
        return internalDirectory.supportsInactiveAccounts();
    }

    public boolean supportsNestedGroups()
    {
        return internalDirectory.supportsNestedGroups();
    }

    public boolean isRolesDisabled()
    {
        return internalDirectory.isRolesDisabled();
    }

    public Set<String> getValues(String key)
    {
        return internalDirectory.getValues(key);
    }

    public String getValue(String key)
    {
        return internalDirectory.getValue(key);
    }

    public Set<String> getKeys()
    {
        return internalDirectory.getKeys();
    }

    public boolean isEmpty()
    {
        return internalDirectory.isEmpty();
    }

    @Override
    public RemoteDirectory getAuthoritativeDirectory()
    {
        return ldapDirectory;
    }

    private boolean isUserCreateOnAuthEnabled()
    {
        return Boolean.parseBoolean(getValue(ATTRIBUTE_CREATE_USER_ON_AUTH));
    }

    private boolean isUserUpdateOnAuthEnabled()
    {
        return Boolean.parseBoolean(getValue(ATTRIBUTE_UPDATE_USER_ON_AUTH));
    }

    private boolean isImportGroupsEnabled()
    {
        return Boolean.parseBoolean(getValue(ATTRIBUTE_KEY_IMPORT_GROUPS));
    }

    @Override
    public Iterable<Membership> getMemberships() throws OperationFailedException
    {
        return internalDirectory.getMemberships();
    }

}
