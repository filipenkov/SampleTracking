package com.atlassian.crowd.manager.directory;

import com.atlassian.crowd.dao.application.ApplicationDAO;
import com.atlassian.crowd.directory.InternalRemoteDirectory;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.directory.SynchronisableDirectory;
import com.atlassian.crowd.directory.loader.DirectoryInstanceLoader;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectorySynchronisationInformation;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.event.directory.DirectoryCreatedEvent;
import com.atlassian.crowd.event.directory.DirectoryDeletedEvent;
import com.atlassian.crowd.event.directory.DirectoryUpdatedEvent;
import com.atlassian.crowd.event.group.GroupAttributeDeletedEvent;
import com.atlassian.crowd.event.group.GroupAttributeStoredEvent;
import com.atlassian.crowd.event.group.GroupCreatedEvent;
import com.atlassian.crowd.event.group.GroupDeletedEvent;
import com.atlassian.crowd.event.group.GroupMembershipCreatedEvent;
import com.atlassian.crowd.event.group.GroupMembershipDeletedEvent;
import com.atlassian.crowd.event.group.GroupUpdatedEvent;
import com.atlassian.crowd.event.user.ResetPasswordEvent;
import com.atlassian.crowd.event.user.UserAttributeDeletedEvent;
import com.atlassian.crowd.event.user.UserAttributeStoredEvent;
import com.atlassian.crowd.event.user.UserCreatedEvent;
import com.atlassian.crowd.event.user.UserCredentialUpdatedEvent;
import com.atlassian.crowd.event.user.UserDeletedEvent;
import com.atlassian.crowd.event.user.UserUpdatedEvent;
import com.atlassian.crowd.exception.DirectoryCurrentlySynchronisingException;
import com.atlassian.crowd.exception.DirectoryInstantiationException;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidEmailAddressException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidMembershipException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.exception.NestedGroupsNotSupportedException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.ReadOnlyGroupException;
import com.atlassian.crowd.exception.UserAlreadyExistsException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.directory.monitor.poller.DirectoryPollerManager;
import com.atlassian.crowd.manager.lock.DirectoryLockManager;
import com.atlassian.crowd.manager.permission.PermissionManager;
import com.atlassian.crowd.model.DirectoryEntity;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.model.membership.MembershipType;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.Entity;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.search.util.SearchResultsUtil;
import com.atlassian.crowd.util.BatchResult;
import com.atlassian.crowd.util.PasswordHelper;
import com.atlassian.event.api.EventPublisher;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;
import static com.google.common.base.Preconditions.checkNotNull;

public class DirectoryManagerGeneric implements DirectoryManager
{
    private static final Logger logger = LoggerFactory.getLogger(DirectoryManagerGeneric.class);

    private final DirectoryDao directoryDao;
    private final ApplicationDAO applicationDAO;
    private final EventPublisher eventPublisher;
    private final PermissionManager permissionManager;
    private final PasswordHelper passwordHelper;
    private final DirectoryInstanceLoader directoryInstanceLoader;
    private final DirectorySynchroniser directorySynchroniser;
    private final DirectoryPollerManager directoryPollerManager;
    private final DirectoryLockManager directoryLockManager;
    private final SynchronisationStatusManager synchronisationStatusManager;

    public DirectoryManagerGeneric(DirectoryDao directoryDao,
                                   ApplicationDAO applicationDAO,
                                   EventPublisher eventPublisher,
                                   PermissionManager permissionManager,
                                   PasswordHelper passwordHelper,
                                   DirectoryInstanceLoader directoryInstanceLoader,
                                   DirectorySynchroniser directorySynchroniser,
                                   DirectoryPollerManager directoryPollerManager,
                                   DirectoryLockManager directoryLockManager,
                                   SynchronisationStatusManager synchronisationStatusManager)
    {
        this.directoryDao = checkNotNull(directoryDao);
        this.applicationDAO = checkNotNull(applicationDAO);
        this.eventPublisher = checkNotNull(eventPublisher);
        this.permissionManager = checkNotNull(permissionManager);
        this.passwordHelper = checkNotNull(passwordHelper);
        this.directoryInstanceLoader = checkNotNull(directoryInstanceLoader);
        this.directorySynchroniser = checkNotNull(directorySynchroniser);
        this.directoryPollerManager = checkNotNull(directoryPollerManager);
        this.directoryLockManager = checkNotNull(directoryLockManager);
        this.synchronisationStatusManager = checkNotNull(synchronisationStatusManager);
    }

    public Directory addDirectory(final Directory directory) throws DirectoryInstantiationException
    {
        // make sure the raw implementation is instantiable
        if (!directoryInstanceLoader.canLoad(directory.getImplementationClass()))
        {
            throw new IllegalArgumentException("Failed to instantiate directory with class: " + directory.getImplementationClass());
        }

        Directory addedDirectory = directoryDao.add(directory);

        eventPublisher.publish(new DirectoryCreatedEvent(this, directory));

        return addedDirectory;
    }

    public Directory findDirectoryById(final long directoryId) throws DirectoryNotFoundException
    {
        return directoryDao.findById(directoryId);
    }

    public List<Directory> findAllDirectories()
    {
        return searchDirectories(QueryBuilder.queryFor(Directory.class, EntityDescriptor.directory()).returningAtMost(EntityQuery.ALL_RESULTS));
    }

    public List<Directory> searchDirectories(final EntityQuery<Directory> query)
    {
        return directoryDao.search(query);
    }

    public Directory findDirectoryByName(final String name) throws DirectoryNotFoundException
    {
        return directoryDao.findByName(name);
    }

    public Directory updateDirectory(final Directory directory) throws DirectoryNotFoundException
    {
        if (directory.getId() == null)
        {
            throw new DirectoryNotFoundException(directory.getId());
        }

        // throw ONFE if directory does not exist
        findDirectoryById(directory.getId());

        Directory updatedDirectory = directoryDao.update(directory);

        eventPublisher.publish(new DirectoryUpdatedEvent(this, updatedDirectory));

        return updatedDirectory;

    }

    public void removeDirectory(final Directory directory) throws DirectoryNotFoundException, DirectoryCurrentlySynchronisingException
    {
        Lock lock = directoryLockManager.getLock(directory.getId());
        if (lock.tryLock())
        {
            try
            {
                // remove all application associations
                applicationDAO.removeDirectoryMappings(directory.getId());

                // remove the directory (and associated internal users/groups/memberships)
                directoryDao.remove(directory);

                eventPublisher.publish(new DirectoryDeletedEvent(this, directory));
            }
            finally
            {
                lock.unlock();
            }
        }
        else
        {
            // unfortunately, due to the way we currently check if a directory is synchronising (i.e. directory is
            // synchronising if we can't acquire the lock), if another thread successfully acquired the lock while
            // checking if the directory is synchronising, this thread will falsely throw a
            // {@link DirectoryCurrentlySynchronisingException}
            throw new DirectoryCurrentlySynchronisingException(directory.getId());
        }
    }

    public boolean supportsNestedGroups(final long directoryId) throws DirectoryInstantiationException, DirectoryNotFoundException
    {
        RemoteDirectory remoteDirectory = getDirectoryImplementation(directoryId);
        return remoteDirectory.supportsNestedGroups();
    }

    public boolean isSynchronisable(final long directoryId) throws DirectoryInstantiationException, DirectoryNotFoundException
    {
        RemoteDirectory remoteDirectory = getDirectoryImplementation(directoryId);
        return remoteDirectory instanceof SynchronisableDirectory;
    }

    public void synchroniseCache(final long directoryId, final SynchronisationMode mode)
            throws OperationFailedException, DirectoryNotFoundException
    {
        synchroniseCache(directoryId, mode, true);
    }

    public void synchroniseCache(final long directoryId, final SynchronisationMode mode, final boolean runInBackground)
            throws OperationFailedException, DirectoryNotFoundException
    {
        RemoteDirectory remoteDirectory = getDirectoryImplementation(directoryId);
        if (remoteDirectory instanceof SynchronisableDirectory)
        {
            if (runInBackground)
            {
                directoryPollerManager.triggerPoll(directoryId, mode);
            }
            else
            {
                if (isSynchronising(directoryId))
                {
                    throw new OperationFailedException("Directory " + directoryId + " is currently synchronising");
                }
                directorySynchroniser.synchronise((SynchronisableDirectory) remoteDirectory, mode);
            }
        }
    }

    public boolean isSynchronising(final long directoryId)
            throws DirectoryInstantiationException, DirectoryNotFoundException
    {
        return directorySynchroniser.isSynchronising(directoryId);
    }

    public DirectorySynchronisationInformation getDirectorySynchronisationInformation(long directoryId)
            throws DirectoryInstantiationException, DirectoryNotFoundException
    {
        final RemoteDirectory remoteDirectory = getDirectoryImplementation(directoryId);
        if (remoteDirectory instanceof SynchronisableDirectory)
        {
            return synchronisationStatusManager.getDirectorySynchronisationInformation(findDirectoryById(directoryId));
        }
        return null;
    }

    /////////// USER OPERATIONS ///////////

    private RemoteDirectory getDirectoryImplementation(long directoryId) throws DirectoryInstantiationException, DirectoryNotFoundException
    {
        return directoryInstanceLoader.getDirectory(findDirectoryById(directoryId));
    }

    public User authenticateUser(long directoryId, String username, PasswordCredential passwordCredential)
            throws OperationFailedException, InactiveAccountException, InvalidAuthenticationException, ExpiredCredentialException, DirectoryNotFoundException, UserNotFoundException
    {
        return getDirectoryImplementation(directoryId).authenticate(username, passwordCredential);
    }

    public User findUserByName(final long directoryId, final String username)
            throws OperationFailedException, DirectoryNotFoundException, UserNotFoundException
    {
        return getDirectoryImplementation(directoryId).findUserByName(username);
    }

    public UserWithAttributes findUserWithAttributesByName(final long directoryId, final String username)
            throws OperationFailedException, DirectoryNotFoundException, UserNotFoundException
    {
        return getDirectoryImplementation(directoryId).findUserWithAttributesByName(username);
    }

    public <T> List<T> searchUsers(final long directoryId, final EntityQuery<T> query)
            throws OperationFailedException, DirectoryNotFoundException
    {
        return getDirectoryImplementation(directoryId).searchUsers(query);
    }

    public User addUser(final long directoryId, final UserTemplate user, final PasswordCredential credential)
            throws InvalidCredentialException, InvalidUserException, OperationFailedException, DirectoryPermissionException, DirectoryNotFoundException, UserAlreadyExistsException
    {
        if (userExists(directoryId, user.getName()))
        {
            throw new InvalidUserException(user, "User already exists");
        }

        Directory directory = findDirectoryById(directoryId);

        if (permissionManager.hasPermission(directory, OperationType.CREATE_USER))
        {
            User createdUser = getDirectoryImplementation(directoryId).addUser(user, credential);

            eventPublisher.publish(new UserCreatedEvent(this, directory, createdUser));

            return createdUser;
        }
        else
        {
            throw new DirectoryPermissionException("Directory does not allow adding of users");
        }
    }

    /**
     * Returns true if the user exists.
     *
     * @param directoryId directory ID
     * @param username name of the user
     * @return true if the user exists, otherwise false.
     * @throws DirectoryNotFoundException if the directory could not be found
     * @throws OperationFailedException if the operation failed for any other reason
     */
    private boolean userExists(final long directoryId, final String username)
            throws DirectoryNotFoundException, OperationFailedException
    {
        try
        {
            findUserByName(directoryId, username);
            return true;
        }
        catch (UserNotFoundException e)
        {
            return false;
        }
    }

    public User updateUser(final long directoryId, final UserTemplate user)
            throws OperationFailedException, DirectoryPermissionException, InvalidUserException, DirectoryNotFoundException, UserNotFoundException
    {
        Directory directory = findDirectoryById(directoryId);

        if (permissionManager.hasPermission(directory, OperationType.UPDATE_USER))
        {
            User updatedUser = getDirectoryImplementation(directoryId).updateUser(user);

            eventPublisher.publish(new UserUpdatedEvent(this, directory, updatedUser));

            return updatedUser;
        }
        else
        {
            throw new DirectoryPermissionException("Directory does not allow user modifications");
        }
    }

    public User renameUser(final long directoryId, final String oldUsername, final String newUsername)
            throws OperationFailedException, DirectoryPermissionException, InvalidUserException, DirectoryNotFoundException, UserNotFoundException, UserAlreadyExistsException
    {
        Directory directory = findDirectoryById(directoryId);
        if (permissionManager.hasPermission(directory, OperationType.UPDATE_USER))
        {
            User updatedUser = getDirectoryImplementation(directoryId).renameUser(oldUsername, newUsername);

            eventPublisher.publish(new UserUpdatedEvent(this, directory, updatedUser));

            return updatedUser;
        }
        else
        {
            throw new DirectoryPermissionException("Directory does not allow user modifications");
        }
    }

    public void storeUserAttributes(final long directoryId, final String username, final Map<String, Set<String>> attributes)
            throws OperationFailedException, DirectoryPermissionException, DirectoryNotFoundException, UserNotFoundException
    {
        Directory directory = findDirectoryById(directoryId);
        if (permissionManager.hasPermission(directory, OperationType.UPDATE_USER_ATTRIBUTE))
        {
            getDirectoryImplementation(directoryId).storeUserAttributes(username, attributes);

            User updatedUser = getDirectoryImplementation(directoryId).findUserByName(username);

            eventPublisher.publish(new UserAttributeStoredEvent(this, directory, updatedUser, attributes));
        }
        else
        {
            throw new DirectoryPermissionException("Directory does not allow user attribute modifications");
        }
    }

    public void removeUserAttributes(final long directoryId, final String username, final String attributeName)
            throws OperationFailedException, DirectoryPermissionException, DirectoryNotFoundException, UserNotFoundException
    {
        Directory directory = findDirectoryById(directoryId);
        if (permissionManager.hasPermission(directory, OperationType.UPDATE_USER_ATTRIBUTE))
        {
            getDirectoryImplementation(directoryId).removeUserAttributes(username, attributeName);
            User updatedUser = getDirectoryImplementation(directoryId).findUserByName(username);

            eventPublisher.publish(new UserAttributeDeletedEvent(this, directory, updatedUser, attributeName));
        }
        else
        {
            throw new DirectoryPermissionException("Directory does not allow user attribute modifications");
        }
    }

    public void updateUserCredential(final long directoryId, final String username, final PasswordCredential credential)
            throws OperationFailedException, DirectoryPermissionException, InvalidCredentialException, DirectoryNotFoundException, UserNotFoundException
    {
        Directory directory = findDirectoryById(directoryId);
        if (permissionManager.hasPermission(directory, OperationType.UPDATE_USER))
        {
            logger.info("The password for \"" + username + "\" in \"" + directory.getName() + "\" is being changed.");

            getDirectoryImplementation(directoryId).updateUserCredential(username, credential);
            eventPublisher.publish(new UserCredentialUpdatedEvent(this, directory, username, credential));
        }
        else
        {
            throw new DirectoryPermissionException("Directory does not allow user modifications");
        }
    }

    public void resetPassword(final long directoryId, final String username)
            throws OperationFailedException, InvalidEmailAddressException, DirectoryPermissionException, InvalidCredentialException, DirectoryNotFoundException, UserNotFoundException
    {
        Directory directory = findDirectoryById(directoryId);

        if (permissionManager.hasPermission(directory, OperationType.UPDATE_USER))
        {
            // generate a random password
            String password = passwordHelper.generateRandomPassword();

            // build the credential
            PasswordCredential credential = new PasswordCredential(password);

            User user = getDirectoryImplementation(directoryId).findUserByName(username);

            // only reset the password if a valid email address exists
            if (StringUtils.isNotBlank(user.getEmailAddress()))
            {
                logger.info("The password for \"" + username + "\" in \"" + directory.getName() + "\" is being reset to a random value.");

                // Perform the Password Reset on the given Directory!
                getDirectoryImplementation(directoryId).updateUserCredential(username, credential);
            }
            else
            {
                throw new InvalidEmailAddressException("Cannot email a new password; user's email address is blank.");
            }

            eventPublisher.publish(new ResetPasswordEvent(this, directory, user, password));
        }
        else
        {
            throw new DirectoryPermissionException("Directory does not allow user modifications");
        }
    }

    public void removeUser(final long directoryId, final String username)
            throws DirectoryPermissionException, OperationFailedException, DirectoryNotFoundException, UserNotFoundException
    {
        Directory directory = findDirectoryById(directoryId);

        if (permissionManager.hasPermission(directory, OperationType.DELETE_USER))
        {
            getDirectoryImplementation(directoryId).removeUser(username);

            eventPublisher.publish(new UserDeletedEvent(this, directory, username));
        }
        else
        {
            throw new DirectoryPermissionException("Directory does not allow user removal");
        }
    }

    /////////// GROUP OPERATIONS ///////////

    public Group findGroupByName(final long directoryId, final String groupName)
            throws OperationFailedException, GroupNotFoundException, DirectoryNotFoundException
    {
        return getDirectoryImplementation(directoryId).findGroupByName(groupName);
    }

    public GroupWithAttributes findGroupWithAttributesByName(final long directoryId, final String groupName)
            throws OperationFailedException, GroupNotFoundException, DirectoryNotFoundException
    {
        return getDirectoryImplementation(directoryId).findGroupWithAttributesByName(groupName);
    }

    public <T> List<T> searchGroups(final long directoryId, final EntityQuery<T> query)
            throws OperationFailedException, DirectoryNotFoundException
    {
        return getDirectoryImplementation(directoryId).searchGroups(query);
    }

    public Group addGroup(final long directoryId, final GroupTemplate group)
            throws InvalidGroupException, OperationFailedException, DirectoryPermissionException, DirectoryNotFoundException
    {
        Group createdGroup;
        Directory directory = findDirectoryById(directoryId);
        try
        {
            findGroupByName(directoryId, group.getName());
            throw new InvalidGroupException(group, "Group with name <" + group.getName() + "> already exists in directory <" + directory.getName() + ">");
        }
        catch (GroupNotFoundException e)
        {
            final OperationType operationType = getCreateOperationType(group);

            // only add a group if we can't find it
            if (permissionManager.hasPermission(directory, operationType))
            {
                createdGroup = getDirectoryImplementation(directoryId).addGroup(group);

                eventPublisher.publish(new GroupCreatedEvent(this, directory, createdGroup));
            }
            else
            {
                if (operationType.equals(OperationType.CREATE_GROUP))
                {
                    throw new DirectoryPermissionException("Directory does not allow adding of groups");
                }
                else
                {
                    throw new DirectoryPermissionException("Directory does not allow adding of roles");
                }
            }
        }
        return createdGroup;
    }

    public Group updateGroup(final long directoryId, final GroupTemplate group)
            throws OperationFailedException, DirectoryPermissionException, InvalidGroupException, DirectoryNotFoundException, GroupNotFoundException, ReadOnlyGroupException
    {
        Group updatedGroup;
        Directory directory = findDirectoryById(directoryId);

        final OperationType operationType = getUpdateOperationType(group);

        if (permissionManager.hasPermission(directory, operationType))
        {
            updatedGroup = getDirectoryImplementation(directoryId).updateGroup(group);

            eventPublisher.publish(new GroupUpdatedEvent(this, directory, updatedGroup));
        }
        else
        {
            if (operationType.equals(OperationType.UPDATE_GROUP))
            {
                throw new DirectoryPermissionException("Directory does not allow group modifications");
            }
            else
            {
                throw new DirectoryPermissionException("Directory does not allow role modifications");
            }

        }

        return updatedGroup;
    }

    public Group renameGroup(final long directoryId, final String oldGroupname, final String newGroupname)
            throws OperationFailedException, DirectoryPermissionException, InvalidGroupException, DirectoryNotFoundException, GroupNotFoundException
    {
        Directory directory = findDirectoryById(directoryId);

        final Group groupToUpdate = findGroupByName(directoryId, oldGroupname);
        final OperationType operationType = getUpdateOperationType(groupToUpdate);

        if (permissionManager.hasPermission(directory, operationType))
        {
            final Group updatedGroup = getDirectoryImplementation(directoryId).renameGroup(oldGroupname, newGroupname);

            eventPublisher.publish(new GroupUpdatedEvent(this, directory, updatedGroup));

            return updatedGroup;
        }
        else
        {
            if (operationType.equals(OperationType.UPDATE_GROUP))
            {
                throw new DirectoryPermissionException("Directory does not allow group modifications");
            }
            else
            {
                throw new DirectoryPermissionException("Directory does not allow role modifications");
            }
        }
    }

    public void storeGroupAttributes(final long directoryId, final String groupName, final Map<String, Set<String>> attributes)
            throws OperationFailedException, DirectoryPermissionException, DirectoryNotFoundException, GroupNotFoundException
    {
        Directory directory = findDirectoryById(directoryId);

        final Group groupToUpdate = findGroupByName(directoryId, groupName);
        final OperationType operationType = getUpdateAttributeOperationType(groupToUpdate);

        if (permissionManager.hasPermission(directory, operationType))
        {
            getDirectoryImplementation(directoryId).storeGroupAttributes(groupName, attributes);

            final Group updateGroup = findGroupByName(directoryId, groupName);

            eventPublisher.publish(new GroupAttributeStoredEvent(this, directory, updateGroup, attributes));
        }
        else
        {
            if (operationType.equals(OperationType.UPDATE_GROUP))
            {
                throw new DirectoryPermissionException("Directory does not allow group modifications");
            }
            else
            {
                throw new DirectoryPermissionException("Directory does not allow role modifications");
            }
        }
    }

    public void removeGroupAttributes(final long directoryId, final String groupName, final String attributeName)
            throws OperationFailedException, DirectoryPermissionException, DirectoryNotFoundException, GroupNotFoundException
    {
        Directory directory = findDirectoryById(directoryId);

        final Group groupToUpdate = findGroupByName(directoryId, groupName);
        final OperationType operationType = getUpdateAttributeOperationType(groupToUpdate);

        if (permissionManager.hasPermission(directory, operationType))
        {
            getDirectoryImplementation(directoryId).removeGroupAttributes(groupName, attributeName);

            final Group updateGroup = findGroupByName(directoryId, groupName);

            eventPublisher.publish(new GroupAttributeDeletedEvent(this, directory, updateGroup, attributeName));
        }
        else
        {
            if (operationType.equals(OperationType.UPDATE_GROUP_ATTRIBUTE))
            {
                throw new DirectoryPermissionException("Directory does not allow group attribute modifications");
            }
            else
            {
                throw new DirectoryPermissionException("Directory does not allow role attribute modifications");
            }
        }
    }

    public void removeGroup(final long directoryId, final String groupName)
            throws DirectoryPermissionException, OperationFailedException, DirectoryNotFoundException, GroupNotFoundException, ReadOnlyGroupException
    {
        Directory directory = findDirectoryById(directoryId);

        final Group groupToDelete = findGroupByName(directoryId, groupName);
        final OperationType operationType = getDeleteOperationType(groupToDelete);

        if (permissionManager.hasPermission(directory, operationType))
        {
            // remove the group from the underlying directory implementation
            getDirectoryImplementation(directoryId).removeGroup(groupName);

            // remove application-group associations
            applicationDAO.removeGroupMappings(directoryId, groupName);

            eventPublisher.publish(new GroupDeletedEvent(this, directory, groupName));
        }
        else
        {
            if (operationType.equals(OperationType.DELETE_GROUP))
            {
                throw new DirectoryPermissionException("Directory does not allow group removal");
            }
            else
            {
                throw new DirectoryPermissionException("Directory does not allow role removal");
            }
        }
    }

    public boolean isUserDirectGroupMember(final long directoryId, final String username, final String groupName)
            throws OperationFailedException, DirectoryNotFoundException
    {
        return getDirectoryImplementation(directoryId).isUserDirectGroupMember(username, groupName);
    }

    public boolean isGroupDirectGroupMember(final long directoryId, final String childGroup, final String parentGroup)
            throws OperationFailedException, DirectoryNotFoundException
    {
        if (childGroup.equals(parentGroup))
        {
            return false;
        }

        RemoteDirectory remoteDirectory = getDirectoryImplementation(directoryId);
        return remoteDirectory.supportsNestedGroups() && remoteDirectory.isGroupDirectGroupMember(childGroup, parentGroup);
    }

    public void addUserToGroup(final long directoryId, final String username, final String groupName)
            throws DirectoryPermissionException, OperationFailedException, DirectoryNotFoundException, GroupNotFoundException, UserNotFoundException, ReadOnlyGroupException
    {
        // only add member to group if it's currently not a member
        if (!isUserDirectGroupMember(directoryId, username, groupName))
        {
            Directory directory = findDirectoryById(directoryId);

            final Group groupToUpdate = findGroupByName(directoryId, groupName);
            final OperationType operationType = getUpdateOperationType(groupToUpdate);

            if (permissionManager.hasPermission(directory, operationType))
            {
                getDirectoryImplementation(directoryId).addUserToGroup(username, groupName);

                eventPublisher.publish(new GroupMembershipCreatedEvent(this, directory, username, groupName, MembershipType.GROUP_USER));
            }
            else
            {
                if (operationType.equals(OperationType.UPDATE_GROUP))
                {
                    throw new DirectoryPermissionException("Directory does not allow group modifications");
                }
                else
                {
                    throw new DirectoryPermissionException("Directory does not allow role modifications");
                }
            }
        }
    }

    public void addGroupToGroup(final long directoryId, final String childGroup, final String parentGroup)
            throws DirectoryPermissionException, OperationFailedException, InvalidMembershipException, NestedGroupsNotSupportedException, DirectoryNotFoundException, GroupNotFoundException, ReadOnlyGroupException
    {
        RemoteDirectory remoteDirectory = getDirectoryImplementation(directoryId);
        if (!remoteDirectory.supportsNestedGroups())
        {
            throw new NestedGroupsNotSupportedException(directoryId);
        }

        if (!isGroupDirectGroupMember(directoryId, childGroup, parentGroup))
        {
            Directory directory = findDirectoryById(directoryId);

            final Group parentGroupToUpdate = findGroupByName(directoryId, parentGroup);
            final OperationType operationType = getUpdateOperationType(parentGroupToUpdate);

            if (permissionManager.hasPermission(directory, operationType))
            {
                if (childGroup.equals(parentGroup))
                {
                    throw new InvalidMembershipException("Cannot add direct circular group membership reference");
                }

                remoteDirectory.addGroupToGroup(childGroup, parentGroup);

                eventPublisher.publish(new GroupMembershipCreatedEvent(this, directory, childGroup, parentGroup, MembershipType.GROUP_GROUP));
            }
            else
            {
                if (operationType.equals(OperationType.UPDATE_GROUP))
                {
                    throw new DirectoryPermissionException("Directory does not allow group modifications");
                }
                else
                {
                    throw new DirectoryPermissionException("Directory does not allow role modifications");
                }
            }
        }
    }

    public void removeUserFromGroup(final long directoryId, final String username, final String groupName)
            throws DirectoryPermissionException, OperationFailedException, MembershipNotFoundException, DirectoryNotFoundException, GroupNotFoundException, UserNotFoundException, ReadOnlyGroupException
    {
        Directory directory = findDirectoryById(directoryId);

        final Group groupToUpdate = findGroupByName(directoryId, groupName);
        final OperationType operationType = getUpdateOperationType(groupToUpdate);

        if (permissionManager.hasPermission(directory, operationType))
        {
            getDirectoryImplementation(directoryId).removeUserFromGroup(username, groupName);

            eventPublisher.publish(new GroupMembershipDeletedEvent(this, directory, username, groupName, MembershipType.GROUP_USER));
        }
        else
        {
            if (operationType.equals(OperationType.UPDATE_GROUP))
            {
                throw new DirectoryPermissionException("Directory does not allow group modifications");
            }
            else
            {
                throw new DirectoryPermissionException("Directory does not allow role modifications");
            }
        }
    }

    public void removeGroupFromGroup(final long directoryId, final String childGroup, final String parentGroup)
            throws DirectoryPermissionException, OperationFailedException, InvalidMembershipException, MembershipNotFoundException, DirectoryNotFoundException, GroupNotFoundException, ReadOnlyGroupException
    {
        RemoteDirectory remoteDirectory = getDirectoryImplementation(directoryId);
        if (!remoteDirectory.supportsNestedGroups())
        {
            throw new UnsupportedOperationException("Directory with id [" + directoryId + "] does not support nested groups");
        }

        Directory directory = findDirectoryById(directoryId);

        final Group groupToUpdate = findGroupByName(directoryId, parentGroup);
        final OperationType operationType = getUpdateOperationType(groupToUpdate);

        if (permissionManager.hasPermission(directory, operationType))
        {
            if (childGroup.equals(parentGroup))
            {
                throw new InvalidMembershipException("Cannot remove direct circular group membership reference");
            }

            remoteDirectory.removeGroupFromGroup(childGroup, parentGroup);

            eventPublisher.publish(new GroupMembershipDeletedEvent(this, directory, childGroup, parentGroup, MembershipType.GROUP_GROUP));
        }
        else
        {
            if (operationType.equals(OperationType.UPDATE_GROUP))
            {
                throw new DirectoryPermissionException("Directory does not allow group modifications");
            }
            else
            {
                throw new DirectoryPermissionException("Directory does not allow role modifications");
            }
        }
    }

    public <T> List<T> searchDirectGroupRelationships(final long directoryId, final MembershipQuery<T> query)
            throws OperationFailedException, DirectoryNotFoundException
    {
        final RemoteDirectory remoteDirectory = getDirectoryImplementation(directoryId);

        // check for nested groups disabled (pre-optimisation)
        if (!remoteDirectory.supportsNestedGroups() && query.getEntityToMatch().getEntityType() == Entity.GROUP && query.getEntityToReturn().getEntityType() == Entity.GROUP)
        {
            return Collections.emptyList();
        }

        // Skip if this is a role query and roles are disabled
        if (remoteDirectory.isRolesDisabled()
                && ((query.getEntityToMatch().getEntityType() == Entity.GROUP && query.getEntityToMatch().getGroupType() == GroupType.LEGACY_ROLE)
                || (query.getEntityToReturn().getEntityType() == Entity.GROUP && query.getEntityToReturn().getGroupType() == GroupType.LEGACY_ROLE)))
        {
            return Collections.emptyList();
        }

        return getDirectoryImplementation(directoryId).searchGroupRelationships(query);
    }

    public boolean isUserNestedGroupMember(final long directoryId, final String username, final String groupName)
            throws OperationFailedException, DirectoryNotFoundException
    {
        if (getDirectoryImplementation(directoryId).supportsNestedGroups())
        {
            return isUserNestedGroupMember(directoryId, username, groupName, new HashSet<String>());
        }
        else
        {
            return isUserDirectGroupMember(directoryId, username, groupName);
        }
    }

    private boolean isUserNestedGroupMember(long directoryId, String username, String groupName, Set<String> visitedGroups)
            throws OperationFailedException, DirectoryNotFoundException
    {
        if (visitedGroups.contains(toLowerCase(groupName)))
        {
            // cycled around and still haven't been able to prove membership
            return false;
        }

        // first check if the user is a direct member
        boolean isMember = isUserDirectGroupMember(directoryId, username, groupName);

        visitedGroups.add(toLowerCase(groupName));

        if (!isMember)
        {
            // if he's not a direct member, then check if he's a nested member of any of the subgroups (depth first)
            List<Group> subGroups = searchDirectGroupRelationships(directoryId, QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).childrenOf(EntityDescriptor.group()).withName(groupName).returningAtMost(EntityQuery.ALL_RESULTS));

            for (Group childGroup : subGroups)
            {
                isMember = isUserNestedGroupMember(directoryId, username, childGroup.getName(), visitedGroups);

                if (isMember)
                {
                    // true value can shortcut answer
                    break;
                }
            }
        }

        return isMember;
    }

    public boolean isGroupNestedGroupMember(final long directoryId, final String childGroup, final String parentGroup)
            throws OperationFailedException, DirectoryNotFoundException
    {
        if (childGroup.equals(parentGroup))
        {
            return false;
        }

        if (getDirectoryImplementation(directoryId).supportsNestedGroups())
        {
            return isGroupNestedGroupMember(directoryId, childGroup, parentGroup, new HashSet<String>());
        }
        else
        {
            return isGroupDirectGroupMember(directoryId, childGroup, parentGroup);
        }
    }

    private boolean isGroupNestedGroupMember(long directoryId, String childGroupName, String parentGroupName, Set<String> visitedGroups)
            throws OperationFailedException, DirectoryNotFoundException
    {
        if (visitedGroups.contains(toLowerCase(parentGroupName)))
        {
            // cycled around and still haven't been able to prove membership
            return false;
        }

        // first check if the child group is a direct member
        boolean isMember = isGroupDirectGroupMember(directoryId, childGroupName, parentGroupName);

        visitedGroups.add(toLowerCase(parentGroupName));

        if (!isMember)
        {
            // if it's not a direct member, then check if it's a nested member of any of the subgroups (depth first)
            List<Group> subGroups = searchDirectGroupRelationships(directoryId, QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).childrenOf(EntityDescriptor.group()).withName(parentGroupName).returningAtMost(EntityQuery.ALL_RESULTS));

            for (Group childGroup : subGroups)
            {
                isMember = isGroupNestedGroupMember(directoryId, childGroupName, childGroup.getName(), visitedGroups);

                if (isMember)
                {
                    // true value can shortcut answer
                    break;
                }
            }
        }

        return isMember;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> searchNestedGroupRelationships(final long directoryId, final MembershipQuery<T> query)
            throws OperationFailedException, DirectoryNotFoundException
    {
        if (getDirectoryImplementation(directoryId).supportsNestedGroups())
        {
            List<? extends DirectoryEntity> relations;

            int totalResults = query.getStartIndex() + query.getMaxResults();
            if (query.getMaxResults() == EntityQuery.ALL_RESULTS)
            {
                totalResults = EntityQuery.ALL_RESULTS;
            }

            if (query.isFindChildren())
            {
                if (query.getEntityToMatch().getEntityType() == Entity.GROUP)
                {
                    if (query.getEntityToReturn().getEntityType() == Entity.USER)
                    {
                        // query is to find USER members of GROUP
                        relations = findNestedUserMembersOfGroup(directoryId, query.getEntityNameToMatch(), query.getEntityToMatch().getGroupType(), totalResults);
                    }
                    else if (query.getEntityToReturn().getEntityType() == Entity.GROUP)
                    {
                        // query is to find GROUP members of GROUP
                        relations = findNestedGroupMembersOfGroup(directoryId, query.getEntityNameToMatch(), query.getEntityToMatch().getGroupType(), totalResults);
                    }
                    else
                    {
                        throw new IllegalArgumentException("You can only find the GROUP or USER members of a GROUP");
                    }
                }
                else
                {
                    throw new IllegalArgumentException("You can only find the GROUP or USER members of a GROUP");
                }
            }
            else
            {
                // find memberships
                if (query.getEntityToReturn().getEntityType() == Entity.GROUP)
                {
                    if (query.getEntityToMatch().getEntityType() == Entity.USER)
                    {
                        // query is to find GROUP memberships of USER
                        relations = findNestedGroupMembershipsOfUser(directoryId, query.getEntityNameToMatch(), query.getEntityToReturn().getGroupType(), totalResults);

                    }
                    else if (query.getEntityToMatch().getEntityType() == Entity.GROUP)
                    {
                        // query is to find GROUP memberships of GROUP
                        relations = findNestedGroupMembershipsOfGroup(directoryId, query.getEntityNameToMatch(), query.getEntityToReturn().getGroupType(), totalResults);
                    }
                    else
                    {
                        throw new IllegalArgumentException("You can only find the GROUP memberships of USER or GROUP");
                    }
                }
                else
                {
                    throw new IllegalArgumentException("You can only find the GROUP memberships of USER or GROUP");
                }
            }

            relations = SearchResultsUtil.constrainResults(relations, query.getStartIndex(), query.getMaxResults());

            if (query.getReturnType() == String.class) // as name
            {
                return (List<T>) SearchResultsUtil.convertEntitiesToNames(relations);
            }
            else
            {
                return (List<T>) relations;
            }
        }
        else
        {
            return searchDirectGroupRelationships(directoryId, query);
        }
    }

    private List<Group> findNestedGroupMembershipsOfGroup(final long directoryId, final String groupName, GroupType groupType, final int maxResults)
            throws OperationFailedException, DirectoryNotFoundException
    {
        Group group;
        try
        {
            group = findGroupByName(directoryId, groupName);
        }
        catch (GroupNotFoundException e)
        {
            return Collections.emptyList();
        }

        List<Group> nestedParents = findNestedGroupMembershipsIncludingGroups(directoryId, Arrays.asList(group), groupType, maxResults, false);

        return new ArrayList<Group>(nestedParents);
    }

    private List<Group> findNestedGroupMembershipsIncludingGroups(final long directoryId, List<Group> groups, GroupType groupType, final int maxResults, boolean includeOriginal)
            throws OperationFailedException, DirectoryNotFoundException
    {
        Queue<Group> groupsToVisit = new LinkedList<Group>();
        Set<Group> nestedParents = new LinkedHashSet<Group>();

        groupsToVisit.addAll(groups);

        // Should the original groups be included in the results?
        int totalResults = maxResults;
        if (maxResults != EntityQuery.ALL_RESULTS && !includeOriginal)
        {
            totalResults = maxResults + groups.size();
        }

        // now find the nested parents of the direct group memberships (similar to findNestedGroupMembershipsOfGroup)
        // keep iterating while there are more groups to explore AND (we are searching for everything OR we haven't found enough results)
        while (!groupsToVisit.isEmpty() && (totalResults == EntityQuery.ALL_RESULTS || nestedParents.size() < totalResults))
        {
            Group groupToVisit = groupsToVisit.remove();

            // avoid cycles
            if (!nestedParents.contains(groupToVisit))
            {
                // add this group to nested parents
                nestedParents.add(groupToVisit);

                // find direct parent groups
                List<Group> directParents = searchDirectGroupRelationships(directoryId, QueryBuilder.queryFor(Group.class, EntityDescriptor.group(groupType)).parentsOf(EntityDescriptor.group(groupType)).withName(groupToVisit.getName()).returningAtMost(maxResults));

                // visit them later
                groupsToVisit.addAll(directParents);
            }
        }

        if (!includeOriginal)
        {
            nestedParents.removeAll(groups);
        }

        return new ArrayList<Group>(nestedParents);
    }

    private List<Group> findNestedGroupMembershipsOfUser(final long directoryId, final String username, GroupType groupType, final int maxResults)
            throws OperationFailedException, DirectoryNotFoundException
    {
        List<Group> directGroupMemberships = searchDirectGroupRelationships(directoryId, QueryBuilder.queryFor(Group.class, EntityDescriptor.group(groupType)).parentsOf(EntityDescriptor.user()).withName(username).returningAtMost(maxResults));

        return findNestedGroupMembershipsIncludingGroups(directoryId, directGroupMemberships, groupType, maxResults, true);
    }

    private List<Group> findNestedGroupMembersOfGroup(long directoryId, String groupName, GroupType groupType, int maxResults)
            throws OperationFailedException, DirectoryNotFoundException
    {
        Group group;
        try
        {
            group = findGroupByName(directoryId, groupName);
        }
        catch (GroupNotFoundException e)
        {
            return Collections.emptyList();
        }

        Queue<Group> groupsToVisit = new LinkedList<Group>();
        Set<Group> nestedMembers = new LinkedHashSet<Group>();

        groupsToVisit.add(group);

        // keep iterating while there are more groups to explore AND (we are searching for everything OR we haven't found enough results)
        while (!groupsToVisit.isEmpty() && (maxResults == EntityQuery.ALL_RESULTS || nestedMembers.size() < maxResults + 1))
        {
            Group groupToVisit = groupsToVisit.remove();

            // avoid cycles
            if (!nestedMembers.contains(groupToVisit))
            {
                // add this group to nested members
                nestedMembers.add(groupToVisit);

                // find direct subgroups
                List<Group> directMembers = searchDirectGroupRelationships(directoryId, QueryBuilder.queryFor(Group.class, EntityDescriptor.group(groupType)).childrenOf(EntityDescriptor.group(groupType)).withName(groupToVisit.getName()).returningAtMost(maxResults));

                // visit them later
                groupsToVisit.addAll(directMembers);
            }
        }

        // remove the original group we are finding the members of (this will be in the nested members set to prevent cycles)
        nestedMembers.remove(group);

        return new ArrayList<Group>(nestedMembers);
    }

    private List<User> findNestedUserMembersOfGroup(long directoryId, String groupName, GroupType groupType, int maxResults)
            throws OperationFailedException, DirectoryNotFoundException
    {
        Group group;
        try
        {
            group = findGroupByName(directoryId, groupName);
        }
        catch (GroupNotFoundException e)
        {
            return Collections.emptyList();
        }

        Queue<Group> groupsToVisit = new LinkedList<Group>();
        Set<Group> nestedGroupMembers = new LinkedHashSet<Group>();
        Set<User> nestedUserMembers = new LinkedHashSet<User>();

        groupsToVisit.add(group);

        // keep iterating while there are more groups to explore AND (we are searching for everything OR we haven't found enough results)
        while (!groupsToVisit.isEmpty() && (maxResults == EntityQuery.ALL_RESULTS || nestedUserMembers.size() < maxResults))
        {
            Group groupToVisit = groupsToVisit.remove();

            List<User> directUserMembers = searchDirectGroupRelationships(directoryId, QueryBuilder.queryFor(User.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group(groupType)).withName(groupToVisit.getName()).returningAtMost(maxResults));
            nestedUserMembers.addAll(directUserMembers);

            // avoid cycles
            if (!nestedGroupMembers.contains(groupToVisit))
            {
                // add this group to nested members
                nestedGroupMembers.add(groupToVisit);

                // find direct subgroups
                List<Group> directGroupMembers = searchDirectGroupRelationships(directoryId, QueryBuilder.queryFor(Group.class, EntityDescriptor.group(groupType)).childrenOf(EntityDescriptor.group(groupType)).withName(groupToVisit.getName()).returningAtMost(EntityQuery.ALL_RESULTS));

                // visit them later
                groupsToVisit.addAll(directGroupMembers);
            }
        }

        return new ArrayList<User>(nestedUserMembers);
    }


    /////////////// BULK OPERATIONS ///////////////

    public BulkAddResult<User> addAllUsers(final long directoryId, final Collection<UserTemplateWithCredentialAndAttributes> users, final boolean overwrite)
            throws DirectoryPermissionException, OperationFailedException, DirectoryNotFoundException
    {
        Directory directory = findDirectoryById(directoryId);

        if (permissionManager.hasPermission(directory, OperationType.CREATE_USER))
        {
            RemoteDirectory remoteDirectory = getDirectoryImplementation(directoryId);

            List<UserTemplateWithCredentialAndAttributes> usersToAdd = new ArrayList<UserTemplateWithCredentialAndAttributes>();
            BulkAddResult<User> result = new BulkAddResult<User>(users.size(), overwrite);

            for (UserTemplateWithCredentialAndAttributes user : users)
            {
                try
                {
                    findUserByName(directoryId, user.getName());

                    if (overwrite)
                    {
                        try
                        {
                            logger.info("Removing existing user: " + user);
                            removeUser(directoryId, user.getName());
                            usersToAdd.add(user);
                        }
                        catch (Exception e)
                        {
                            logger.error("Could not remove user for bulk import overwrite: " + user, e);
                            result.addExistingEntity(user);
                        }
                    }
                    else
                    {
                        logger.info("User <" + user + "> already exists in directory. Skipping over this entity.");
                        result.addExistingEntity(user);
                    }
                }
                catch (UserNotFoundException e)
                {
                    // entity does not exist in directory, so we can safely add it
                    usersToAdd.add(user);
                }
            }

            // retain unique entities
            Set<UserTemplateWithCredentialAndAttributes> uniqueUsersToAdd = retainUniqueEntities(usersToAdd);

            // perform the add operation
            final Collection<User> successfulEntities;
            final Collection<User> failedEntities;
            if (remoteDirectory instanceof InternalRemoteDirectory)
            {
                final BatchResult<User> batchResult = ((InternalRemoteDirectory) remoteDirectory).addAllUsers(uniqueUsersToAdd);
                successfulEntities = batchResult.getSuccessfulEntities();
                failedEntities = batchResult.getFailedEntities();
            }
            else
            {
                successfulEntities = new ArrayList<User>(uniqueUsersToAdd);
                failedEntities = new ArrayList<User>();
                for (UserTemplateWithCredentialAndAttributes user : uniqueUsersToAdd)
                {
                    try
                    {
                        successfulEntities.add(remoteDirectory.addUser(user, user.getCredential()));
                    }
                    catch (Exception e)
                    {
                        failedEntities.add(user);
                    }
                }
            }

            result.addFailedEntities(failedEntities);

            logFailedEntities(remoteDirectory, failedEntities);

            // Fire events
            for (User addedUser : successfulEntities)
            {
                eventPublisher.publish(new UserCreatedEvent(this, directory, addedUser));
            }

            return result;
        }
        else
        {
            throw new DirectoryPermissionException("Directory does not allow adding of users");
        }
    }

    private <T> Set<T> retainUniqueEntities(final Collection<T> entities)
    {
        Set<T> uniqueEntities = new HashSet<T>(entities.size());
        for (T entity : entities)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Going to add: " + entity);
            }

            boolean added = uniqueEntities.add(entity);

            if (!added)
            {
                logger.warn("Duplicate entity. Entity is already in the set of entities to bulk add: " + entity);
            }
        }
        return uniqueEntities;
    }

    public BulkAddResult<Group> addAllGroups(final long directoryId, final Collection<GroupTemplate> groups, final boolean overwrite)
            throws DirectoryPermissionException, OperationFailedException, DirectoryNotFoundException, InvalidGroupException
    {
        Directory directory = findDirectoryById(directoryId);

        if (permissionManager.hasPermission(directory, OperationType.CREATE_GROUP))
        {
            RemoteDirectory remoteDirectory = getDirectoryImplementation(directoryId);

            List<GroupTemplate> groupsToAdd = new ArrayList<GroupTemplate>();
            BulkAddResult<Group> result = new BulkAddResult<Group>(groups.size(), overwrite);

            for (GroupTemplate group : groups)
            {
                try
                {
                    findGroupByName(directoryId, group.getName());

                    if (overwrite)
                    {
                        try
                        {
                            logger.info("Removing existing group: " + group);
                            removeGroup(directoryId, group.getName());
                            groupsToAdd.add(group);
                        }
                        catch (Exception e)
                        {
                            logger.error("Could not remove group for bulk import overwrite: " + group, e);
                            result.addExistingEntity(group);
                        }
                    }
                    else
                    {
                        logger.info("Group <" + group + "> already exists in directory. Skipping over this entity.");
                        result.addExistingEntity(group);
                    }
                }
                catch (GroupNotFoundException e)
                {
                    // entity does not exist in directory, so we can safely add it
                    groupsToAdd.add(group);
                }
            }

            // retain unique entities
            Set<GroupTemplate> uniqueGroupsToAdd = retainUniqueEntities(groupsToAdd);

            // perform the add operation
            final Collection<Group> successfulEntities;
            final Collection<Group> failedEntities;
            if (remoteDirectory instanceof InternalRemoteDirectory)
            {
                final BatchResult<Group> batchResult = ((InternalRemoteDirectory) remoteDirectory).addAllGroups(uniqueGroupsToAdd);
                successfulEntities = batchResult.getSuccessfulEntities();
                failedEntities = batchResult.getFailedEntities();
            }
            else
            {
                successfulEntities = new ArrayList<Group>(uniqueGroupsToAdd.size());
                failedEntities = new ArrayList<Group>();
                for (GroupTemplate group : uniqueGroupsToAdd)
                {
                    try
                    {
                        successfulEntities.add(remoteDirectory.addGroup(group));
                    }
                    catch (Exception e)
                    {
                        failedEntities.add(group);
                    }
                }
            }

            result.addFailedEntities(failedEntities);
            logFailedEntities(remoteDirectory, failedEntities);

            // Fire events
            for (Group addedGroup : successfulEntities)
            {
                eventPublisher.publish(new GroupCreatedEvent(this, directory, addedGroup));
            }

            return result;
        }
        else
        {
            throw new DirectoryPermissionException("Directory does not allow adding of groups");
        }
    }

    public BulkAddResult<String> addAllUsersToGroup(final long directoryId, final Collection<String> userNames, final String groupName)
            throws DirectoryPermissionException, OperationFailedException, DirectoryNotFoundException, GroupNotFoundException, UserNotFoundException
    {
        Directory directory = findDirectoryById(directoryId);

        // fail-fast if container not found (throws ONFE)
        final Group groupToUpdate = findGroupByName(directoryId, groupName);
        final OperationType operationType = getUpdateOperationType(groupToUpdate);

        if (permissionManager.hasPermission(directory, operationType))
        {
            RemoteDirectory remoteDirectory = directoryInstanceLoader.getDirectory(directory);


            // NOTE: we could check if the principal is a member or not but there is no use
            // as we want to saveOrUpdate to the C_MEMBERSHIP table regardless.
            // also, by not performing the check, we achieve a 3x speed improvement.

            // build a list of memberships to add
            Set<String> usersToAdd = retainUniqueEntities(userNames);

            BulkAddResult<String> result = new BulkAddResult<String>(userNames.size(), true);

            // bulk add
            final Collection<String> successfulUsers;
            final Collection<String> failedUsers;
            if (remoteDirectory instanceof InternalRemoteDirectory)
            {
                final BatchResult<String> batchResult = ((InternalRemoteDirectory) remoteDirectory).addAllUsersToGroup(usersToAdd, groupName);
                successfulUsers = batchResult.getSuccessfulEntities();
                failedUsers = batchResult.getFailedEntities();
            }
            else
            {
                successfulUsers = new ArrayList<String>(usersToAdd.size());
                failedUsers = new ArrayList<String>();
                for (String username : usersToAdd)
                {
                    try
                    {
                        addUserToGroup(directoryId, username, groupName);
                        successfulUsers.add(username);
                    }
                    catch (Exception e)
                    {
                        failedUsers.add(username);
                        logger.error(e.getMessage());
                    }
                }
            }

            result.addFailedEntities(failedUsers);

            // Fire events
            for (String username : successfulUsers)
            {
                eventPublisher.publish(new GroupMembershipCreatedEvent(this, directory, username, groupName, MembershipType.GROUP_USER));
            }

            for (String failedUser : failedUsers)
            {
                logger.warn("Could not add the following user to the group [ {} ]: {}", groupName, failedUser);
            }
            
            return result;
        }
        else
        {
            if (operationType.equals(OperationType.UPDATE_GROUP))
            {
                throw new DirectoryPermissionException("Directory does not allow group modifications");
            }
            else
            {
                throw new DirectoryPermissionException("Directory does not allow role modifications");
            }
        }
    }

    /**
     * Returns either CREATE_GROUP or CREATE_ROLE depending on the GroupType of the given Group
     *
     * @param group The Group
     * @return either CREATE_GROUP or CREATE_ROLE depending on the GroupType of the given Group
     */
    private OperationType getCreateOperationType(final Group group)
    {
        switch (group.getType())
        {
            case GROUP:
                return OperationType.CREATE_GROUP;
            case LEGACY_ROLE:
                return OperationType.CREATE_ROLE;
            default:
                throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns either UPDATE_GROUP or UPDATE_ROLE depending on the GroupType of the given Group
     *
     * @param group The Group
     * @return either UPDATE_GROUP or UPDATE_ROLE depending on the GroupType of the given Group
     */
    private OperationType getUpdateOperationType(final Group group)
    {
        switch (group.getType())
        {
            case GROUP:
                return OperationType.UPDATE_GROUP;
            case LEGACY_ROLE:
                return OperationType.UPDATE_ROLE;
            default:
                throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns either UPDATE_GROUP_ATTRIBUTE or UPDATE_ROLE_ATTRIBUTE depending on the GroupType
     * of the given Group.
     *
     * @param group The Group
     * @return either UPDATE_GROUP_ATTRIBUTE or UPDATE_ROLE_ATTRIBUTE depending on the GroupType
     * of the given Group
     */
    private OperationType getUpdateAttributeOperationType(final Group group)
    {
        switch (group.getType())
        {
            case GROUP:
                return OperationType.UPDATE_GROUP_ATTRIBUTE;
            case LEGACY_ROLE:
                return OperationType.UPDATE_ROLE_ATTRIBUTE;
            default:
                throw new UnsupportedOperationException();
        }
    }

    /**
     * Returns either DELETE_GROUP or DELETE_ROLE depending on the GroupType of the given Group
     *
     * @param group The Group
     * @return either DELETE_GROUP or DELETE_ROLE depending on the GroupType of the given Group
     */
    private OperationType getDeleteOperationType(final Group group)
    {
        switch (group.getType())
        {
            case GROUP:
                return OperationType.DELETE_GROUP;
            case LEGACY_ROLE:
                return OperationType.DELETE_ROLE;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private void logFailedEntities(RemoteDirectory remoteDirectory, final Collection<? extends DirectoryEntity> failedEntities)
    {
        if (!failedEntities.isEmpty())
        {
            String directoryName = remoteDirectory.getDescriptiveName();
            for (DirectoryEntity failedEntity : failedEntities)
            {
                logger.warn("Could not add the following entity to the directory [ {} ]: {}", directoryName, failedEntity.getName());
            }
        }
    }
}
