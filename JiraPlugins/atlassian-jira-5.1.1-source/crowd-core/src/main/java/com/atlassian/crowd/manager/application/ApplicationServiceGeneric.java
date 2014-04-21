package com.atlassian.crowd.manager.application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.crowd.directory.DirectoryProperties;
import com.atlassian.crowd.directory.RemoteDirectory;
import com.atlassian.crowd.directory.loader.DirectoryInstanceLoader;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.UserComparator;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.crowd.event.EventStore;
import com.atlassian.crowd.event.EventTokenExpiredException;
import com.atlassian.crowd.event.Events;
import com.atlassian.crowd.event.IncrementalSynchronisationNotAvailableException;
import com.atlassian.crowd.event.user.UserAuthenticatedEvent;
import com.atlassian.crowd.event.user.UserAuthenticationFailedInvalidAuthenticationEvent;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.BulkAddFailedException;
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
import com.atlassian.crowd.manager.directory.BulkAddResult;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.manager.directory.DirectoryPermissionException;
import com.atlassian.crowd.manager.permission.PermissionManager;
import com.atlassian.crowd.model.NameComparator;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.DirectoryMapping;
import com.atlassian.crowd.model.application.GroupMapping;
import com.atlassian.crowd.model.event.GroupEvent;
import com.atlassian.crowd.model.event.GroupMembershipEvent;
import com.atlassian.crowd.model.event.Operation;
import com.atlassian.crowd.model.event.OperationEvent;
import com.atlassian.crowd.model.event.UserEvent;
import com.atlassian.crowd.model.event.UserMembershipEvent;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.QueryUtils;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.event.api.EventPublisher;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.BooleanUtils.isFalse;
import static org.apache.commons.lang.BooleanUtils.toBooleanObject;

@SuppressWarnings( { "deprecation" })
public class ApplicationServiceGeneric implements ApplicationService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final DirectoryManager directoryManager;
    private final PermissionManager permissionManager;
    private final DirectoryInstanceLoader directoryInstanceLoader;
    private final EventPublisher eventPublisher;
    private final EventStore eventStore;

    public ApplicationServiceGeneric(final DirectoryManager directoryManager, final PermissionManager permissionManager, final DirectoryInstanceLoader directoryInstanceLoader, final EventPublisher eventPublisher, final EventStore eventStore)
    {
        this.directoryInstanceLoader = directoryInstanceLoader;
        this.directoryManager = checkNotNull(directoryManager);
        this.permissionManager = checkNotNull(permissionManager);
        this.eventPublisher = eventPublisher;
        this.eventStore = eventStore;
    }

    public User authenticateUser(final Application application, final String username, final PasswordCredential passwordCredential) throws OperationFailedException, InactiveAccountException, InvalidAuthenticationException, ExpiredCredentialException, UserNotFoundException
    {
        if ((application.getDirectoryMappings() == null) || (application.getDirectoryMappings().isEmpty()))
        {
            throw new InvalidAuthenticationException("Unable to authenticate user as there are no directories mapped to the application " + application.getName());
        }

        OperationFailedException failedException = null;

        for (final Directory directory : getActiveDirectories(application))
        {
            try
            {
                final User user =  directoryManager.authenticateUser(directory.getId(), username, passwordCredential);

                eventPublisher.publish(new UserAuthenticatedEvent(this, directory, application, user));

                return user;
            }
            catch (final OperationFailedException e)
            {
                logger.error("Directory '" + directory.getName() + "' is not functional during authentication of '" + username + "'. Skipped.");

                // we only remember the first one since we can throw only one and the first one should be the most important.
                if (failedException == null)
                {
                    failedException = e;
                }
            }
            catch (final UserNotFoundException e)
            {
                // om nom nom..eat this exception..this can happen if the user does not exist in this directory, so just skip over to the next directory
            }
            catch (final InvalidAuthenticationException e)
            {
                eventPublisher.publish(new UserAuthenticationFailedInvalidAuthenticationEvent(this, directory, username));
                throw e;
            }
            catch (DirectoryNotFoundException e)
            {
                // directory not found
                throw new ConcurrentModificationException("Directory mapping was removed while iterating through directories: " + e.getMessage());
            }
        }

        // at the stage, if the user did not get authenticated by any directory and an OperationFailedException was recorded,
        // we throw that OperationFailedException since it's the prime suspect that should have been the cause of the failed authentication.
        if (failedException != null)
        {
            throw failedException;
        }

        // otherwise, simply the user does not exist in any of the directories.
        throw new UserNotFoundException(username);
    }

    public boolean isUserAuthorised(final Application application, final String username)
    {
        try
        {
            // only cache the result of the authorisation if the user exists and there are no operation failures
            User user = fastFailingFindUser(application, username);
            return isAllowedToAuthenticate(username, user.getDirectoryId(), application);
        }
        catch (OperationFailedException e)
        {
            logger.error(e.getMessage(), e);
            return false;
        }
        catch (DirectoryNotFoundException e)
        {
            throw new ConcurrentModificationException("Directory mapping removed while determining if the user is authorised to authenticate with an application");
        }
        catch (UserNotFoundException e)
        {
            return false;
        }
    }

    public void addAllUsers(final Application application, final Collection<UserTemplateWithCredentialAndAttributes> userTemplates) throws ApplicationPermissionException, OperationFailedException, BulkAddFailedException
    {
        logger.debug("Adding users for application {}", application);

        // if the user doesn't exist in ANY of the directories,
        // add the principal to the FIRST directory with CREATE_USER permission
        final Set<String> failedUsers = new HashSet<String>();
        final Set<String> existingUsers = new HashSet<String>();

        // iterate through directories, find the first one with CREATE_USER permission
        final Directory directory = findFirstDirectoryWithCreateUserPermission(application);
        if (directory == null)
        {
            // None have the required permission
            throw new ApplicationPermissionException("Application '" + application.getName() + "' has no directories that allow adding of users.");
        }
        final BulkAddResult<User> result;
        try
        {
            // Set the Directory ID in each of the UserTemplates
            for (final UserTemplateWithCredentialAndAttributes userTemplate : userTemplates)
            {
                userTemplate.setDirectoryId(directory.getId());
            }

            result = directoryManager.addAllUsers(directory.getId(), userTemplates, false);
            for (final User user : result.getExistingEntities())
            {
                existingUsers.add(user.getName());
            }

            for (final User user : result.getFailedEntities())
            {
                failedUsers.add(user.getName());
            }
        }
        catch (final DirectoryPermissionException ex)
        {
            // PermissionManager said we had CREATE_USER permission, but the Directory threw a PermissionException
            throw new ApplicationPermissionException(
                "Permission Exception when trying to add users to directory '" + directory.getName() + "'. " + ex.getMessage(), ex);
        }
        catch (final DirectoryNotFoundException ex)
        {
            // The Directory has disappeared from underneath us - smells like concurrent modification.
            throw new OperationFailedException("Directory Not Found when trying to add users to directory '" + directory.getName() + "'.", ex);
        }

        if ((failedUsers.size() > 0) || (existingUsers.size() > 0))
        {
            throw new BulkAddFailedException(failedUsers, existingUsers);
        }
    }

    /**
     * Returns the first directory with CREATE_USER permission, or null if none have CREATE_USER permission.
     *
     * @param application the application to explore for DirectoryMappings
     * @return the first directory with CREATE_USER permission, or null if none have CREATE_USER permission.
     */
    private Directory findFirstDirectoryWithCreateUserPermission(final Application application)
    {
        for (final Directory directory : getActiveDirectories(application))
        {
            if (permissionManager.hasPermission(application, directory, OperationType.CREATE_USER))
            {
                return directory;
            }
        }
        return null;
    }

    public User findUserByName(final Application application, final String name) throws UserNotFoundException
    {
        for (final Directory directory : getActiveDirectories(application))
        {
            try
            {
                return directoryManager.findUserByName(directory.getId(), name);
            }
            catch (final UserNotFoundException e)
            {
                // user not in directory, keep cycling
            }
            catch (final DirectoryNotFoundException e)
            {
                // directory not found
                throw new ConcurrentModificationException("Directory mapping was removed while iterating through directories: " + e.getMessage());
            }
            catch (final OperationFailedException e)
            {
                // directory has some massive error, keep cycling
                logger.error(e.getMessage(), e);
            }
        }

        // could not find user in any of the directories
        throw new UserNotFoundException(name);
    }

    /**
     * This method is exactly like findUserByName except that it does not keep cycling if
     * a directory throws an OperationFailedException when attempting to find the user in
     * the particular directory. Use in mutation operations.
     *
     * @param application name of application.
     * @param name        name of user.
     * @return user in first directory that contains the user.
     * @throws OperationFailedException   if any directory fails to perform the find operation.
     * @throws UserNotFoundException      if none of the directories contain the user.
     */
    private User fastFailingFindUser(final Application application, final String name) throws UserNotFoundException, OperationFailedException
    {
        for (final Directory directory : getActiveDirectories(application))
        {
            try
            {
                return directoryManager.findUserByName(directory.getId(), name);
            }
            catch (final UserNotFoundException e)
            {
                // user not in directory, keep cycling
            }
            catch (final DirectoryNotFoundException e)
            {
                // directory not found
                throw new ConcurrentModificationException("Directory mapping was removed while iterating through directories: " + e.getMessage());
            }
        }
        // could not find user in any of the directories
        throw new UserNotFoundException(name);
    }

    public UserWithAttributes findUserWithAttributesByName(final Application application, final String name) throws UserNotFoundException
    {
        for (final Directory directory : getActiveDirectories(application))
        {
            try
            {
                return directoryManager.findUserWithAttributesByName(directory.getId(), name);
            }
            catch (final UserNotFoundException e)
            {
                // user not in directory, keep cycling
            }
            catch (final OperationFailedException e)
            {
                // directory has some massive error, keep cycling
                logger.error(e.getMessage(), e);
            }
            catch (DirectoryNotFoundException e)
            {
                // directory not found
                throw new ConcurrentModificationException("Directory mapping was removed while iterating through directories: " + e.getMessage());
            }
        }

        // could not find user in any of the directories
        throw new UserNotFoundException(name);
    }

    public User addUser(final Application application, final UserTemplate user, final PasswordCredential credential)
            throws InvalidUserException, OperationFailedException, InvalidCredentialException, ApplicationPermissionException
    {
        logger.debug("Adding user <{}> for application <{}>", user.getName(), application.getName());

        try
        {
            // see if user already exists in any of the directories
            fastFailingFindUser(application, user.getName());
            throw new InvalidUserException(user, "User already exists");
        }
        catch (final UserNotFoundException e)
        {
            // Good - the user doesn't currently exist in ANY of the directories.
        }

        // Add the user to the first directory with ADD permission.
        final Directory directory = findFirstDirectoryWithCreateUserPermission(application);
        if (directory == null)
        {
            // None have the required permission
            throw new ApplicationPermissionException("Application '" + application.getName() + "' has no directories that allow adding of users.");
        }

        try
        {
            user.setDirectoryId(directory.getId());
            final User newUser = directoryManager.addUser(directory.getId(), user, credential);
            logger.debug("User '{}' was added to directory '{}'.", new Object[] {user.getName(), directory.getName(), user.getName()});
            return newUser;
        }
        catch (final DirectoryPermissionException dpe)
        {
            // permissionManager said we had CREATE_USER permission, but the Directory threw a PermissionException
            throw new ApplicationPermissionException(
                "Permission Exception when trying to add user '" + user.getName() + "' to directory '" + directory.getName() + "'. " + dpe.getMessage(),
                dpe);
        }
        catch (final DirectoryNotFoundException de)
        {
            // The Directory has disappeared from underneath us - smells like concurrent modification.
            throw new OperationFailedException(
                "Directory not found when trying to add user '" + user.getName() + "' to directory '" + directory.getName() + "'.", de);
        }
        catch (UserAlreadyExistsException e)
        {
            throw new OperationFailedException("User " + user.getName() + " already exists.");
        }
    }

    public User updateUser(final Application application, final UserTemplate user) throws InvalidUserException, OperationFailedException, ApplicationPermissionException, UserNotFoundException
    {
        logger.debug("Updating user <{}> for application <{}>", user.getName(), application.getName());

        // Eagerly throw a UserNotFoundException if the user does not exist
        final User existingUser = fastFailingFindUser(application, user.getName());

        // Check if this existingUser is in the directory that the caller expected.
        if (user.getDirectoryId() <= 0)
        {
            // Caller didn't specify a directory - set this one
            user.setDirectoryId(existingUser.getDirectoryId());
        }
        else
        {
            // Caller specified a directory - verify it is the same one we just found.
            if (user.getDirectoryId() != existingUser.getDirectoryId())
            {
                // Not good - passed user has a different ID to the existing user in this application with given username.
                throw new InvalidUserException(
                    user,
                    "Attempted to update user '" + user.getName() + "' with invalid directory ID " + user.getDirectoryId() + ", we expected ID " + existingUser.getDirectoryId() + ".");
            }
        }

        final Directory directory = findDirectoryById(existingUser.getDirectoryId());

        if (!permissionManager.hasPermission(application, directory, OperationType.UPDATE_USER))
        {
            throw new ApplicationPermissionException(
                "Cannot update user '" + user.getName() + "' because directory '" + directory.getName() + "' does not allow updates.");
        }

        try
        {
            return directoryManager.updateUser(directory.getId(), user);
        }
        catch (final DirectoryPermissionException dpe)
        {
            // permissionManager said we had CREATE_USER permission, but the Directory threw a PermissionException
            throw new ApplicationPermissionException(
                "Permission Exception when trying to update user '" + user.getName() + "' in directory '" + directory.getName() + "'.", dpe);
        }
        catch (DirectoryNotFoundException e)
        {
            // directory not found
            throw new ConcurrentModificationException("Directory mapping was removed while updating user: " + e.getMessage());
        }
    }

    public void updateUserCredential(final Application application, final String username, final PasswordCredential credential) throws OperationFailedException, InvalidCredentialException, ApplicationPermissionException, UserNotFoundException
    {
        final User user = fastFailingFindUser(application, username);

        final Directory directory = findDirectoryById(user.getDirectoryId());

        if (permissionManager.hasPermission(application, directory, OperationType.UPDATE_USER))
        {
            try
            {
                directoryManager.updateUserCredential(user.getDirectoryId(), username, credential);
            }
            catch (final DirectoryPermissionException e)
            {
                // Shouldn't happen because we just checked the permission
                throw new ApplicationPermissionException(e);
            }
            catch (DirectoryNotFoundException e)
            {
                // directory not found
                throw new ConcurrentModificationException("Directory mapping was removed while updating user credential: " + e.getMessage());
            }
        }
        else
        {
            // Permission denied
            throw new ApplicationPermissionException(
                "Not allowed to update user '" + user.getName() + "' in directory '" + directory.getName() + "'.");
        }
    }

    public void resetUserCredential(final Application application, final String username) throws OperationFailedException, InvalidCredentialException, ApplicationPermissionException, InvalidEmailAddressException, UserNotFoundException
    {
        final User user = fastFailingFindUser(application, username);

        final Directory directory = findDirectoryById(user.getDirectoryId());

        if (permissionManager.hasPermission(application, directory, OperationType.UPDATE_USER))
        {
            try
            {
                directoryManager.resetPassword(user.getDirectoryId(), username);
            }
            catch (final DirectoryPermissionException e)
            {
                // Shouldn't happen because we just checked the permission
                throw new ApplicationPermissionException(e);
            }
            catch (DirectoryNotFoundException e)
            {
                // directory not found
                throw new ConcurrentModificationException("Directory mapping was removed while resetting the user credential: " + e.getMessage());
            }
        }
        else
        {
            // Permission denied
            throw new ApplicationPermissionException(
                "Not allowed to update user '" + user.getName() + "' in directory '" + directory.getName() + "'.");
        }
    }

    public void storeUserAttributes(final Application application, final String username, final Map<String, Set<String>> attributes) throws OperationFailedException, ApplicationPermissionException, UserNotFoundException
    {
        logger.debug("Storing user attributes for user <{}> and application <{}>", username, application.getName());

        // Find the user (or throw UserNotFoundException)
        final User user = fastFailingFindUser(application, username);

        final Directory directory = findDirectoryById(user.getDirectoryId());

        if (!permissionManager.hasPermission(application, directory, OperationType.UPDATE_USER_ATTRIBUTE))
        {
            // Permission denied
            throw new ApplicationPermissionException(
                "Not allowed to update user attributes '" + user.getName() + "' in directory '" + directory.getName() + "'.");
        }

        try
        {
            directoryManager.storeUserAttributes(directory.getId(), username, attributes);
        }
        catch (final DirectoryPermissionException ex)
        {
            // Shouldn't happen because we just checked the permission
            throw new ApplicationPermissionException(ex);
        }
        catch (DirectoryNotFoundException e)
        {
            // directory not found
            throw new ConcurrentModificationException("Directory mapping was removed while storing the user attributes: " + e.getMessage());
        }
    }

    public void removeUserAttributes(final Application application, final String username, final String attributeName) throws OperationFailedException, ApplicationPermissionException, UserNotFoundException
    {
        logger.debug("Removing user attributes for user <{}> and application <{}>", username, application.getName());

        // Find the user (or throw UserNotFoundException)
        final User user = fastFailingFindUser(application, username);

        final Directory directory = findDirectoryById(user.getDirectoryId());

        if (!permissionManager.hasPermission(application, directory, OperationType.UPDATE_USER_ATTRIBUTE))
        {
            // Permission denied
            throw new ApplicationPermissionException(
                "Not allowed to update user attributes '" + user.getName() + "' in directory '" + directory.getName() + "'.");
        }

        try
        {
            directoryManager.removeUserAttributes(directory.getId(), username, attributeName);
        }
        catch (final DirectoryPermissionException ex)
        {
            // Shouldn't happen because we just checked the permission
            throw new ApplicationPermissionException(ex);
        }
        catch (DirectoryNotFoundException e)
        {
            // directory not found
            throw new ConcurrentModificationException("Directory mapping was removed while removing the user attributes: " + e.getMessage());
        }
    }

    public void removeUser(final Application application, final String username) throws OperationFailedException, ApplicationPermissionException, UserNotFoundException
    {
        // Find the user (or throw UserNotFoundException)
        final User user = fastFailingFindUser(application, username);

        final Directory directory = findDirectoryById(user.getDirectoryId());

        if (!permissionManager.hasPermission(application, directory, OperationType.DELETE_USER))
        {
            // Permission denied
            throw new ApplicationPermissionException(
                "Not allowed to delete user '" + user.getName() + "' from directory '" + directory.getName() + "'.");
        }

        try
        {
            directoryManager.removeUser(directory.getId(), username);
        }
        catch (final DirectoryPermissionException ex)
        {
            // Shouldn't happen because we just checked the permission
            throw new ApplicationPermissionException(ex);
        }
        catch (DirectoryNotFoundException e)
        {
            // directory not found
            throw new ConcurrentModificationException("Directory mapping was removed while removing the user: " + e.getMessage());
        }
    }

    public <T> List<T> searchUsers(final Application application, final EntityQuery<T> query)
    {
        return searchUsers(application, query, getAggregatingAndSortingComparatorFor(query.getReturnType()));
    }

    public List<User> searchUsersAllowingDuplicateNames(final Application application, final EntityQuery<User> query)
    {
        return searchUsers(application, query, UserComparator.KEY_MAKER);
    }

    private <T, K extends Comparable<? super K>> List<T> searchUsers(final Application application, final EntityQuery<T> query, final Function<? super T, K> comparator)
    {
        // explicitly disallow searches for embedded users
        QueryUtils.checkAssignableFrom(query.getReturnType(), String.class, User.class);

        ResultsAggregator<T> results = ResultsAggregator.with(comparator, query);

        // TODO: implement this so it's more efficient with indexing + max results

        // we require every directory to return <code>totalResults</code> results instead of
        // <code>totalResults - results.size()</code> because the users returned could be duplicates of existing results

        for (final Directory directory : getActiveDirectories(application))
        {
            try
            {
                final EntityQuery<T> totalQuery = QueryBuilder.queryFor(query.getReturnType(), query.getEntityDescriptor(),
                    query.getSearchRestriction(), 0, results.getRequiredResultCount());

                // perform the search
                results.addAll(directoryManager.searchUsers(directory.getId(), totalQuery));
            }
            catch (final DirectoryNotFoundException e)
            {
                // directory does not exist, just skip
            }
            catch (final OperationFailedException e)
            {
                // keep cycling
                logger.error(e.getMessage(), e);
            }
        }

        return results.constrainResults();
    }

    ///////////////////// GROUP OPERATIONS /////////////////////

    public Group findGroupByName(final Application application, final String name) throws GroupNotFoundException
    {
        for (final Directory directory : getActiveDirectories(application))
        {
            try
            {
                return directoryManager.findGroupByName(directory.getId(), name);
            }
            catch (final GroupNotFoundException e)
            {
                // group not in directory, keep cycling
            }
            catch (final DirectoryNotFoundException e)
            {
                // directory not found
                throw new ConcurrentModificationException("Directory mapping was removed while iterating through directories: " + e.getMessage());
            }
            catch (final OperationFailedException e)
            {
                // directory has some massive error, keep cycling
                logger.error(e.getMessage(), e);
            }
        }

        // could not find group in any of the directories
        throw new GroupNotFoundException(name);
    }

    /**
     * This method is exactly like findGroupByName except that it does not keep cycling if
     * a directory throws an OperationFailedException when attempting to find the group in
     * the particular directory.
     *
     * @param application name of application.
     * @param name        name of group.
     * @return user in first directory that contains the user.
     * @throws OperationFailedException if any directory fails to perform the find operation.
     * @throws GroupNotFoundException   if none of the directories contain the group.
     */
    private Group getGroup(final Application application, final String name) throws OperationFailedException, GroupNotFoundException
    {
        for (final Directory directory : getActiveDirectories(application))
        {
            try
            {
                return directoryManager.findGroupByName(directory.getId(), name);
            }
            catch (final GroupNotFoundException e)
            {
                // group not in directory, keep cycling
            }
            catch (final DirectoryNotFoundException e)
            {
                // directory not found
                throw new ConcurrentModificationException("Directory mapping was removed while iterating through directories: " + e.getMessage());
            }
        }

        // could not find group in any of the directories
        throw new GroupNotFoundException(name);
    }

    public GroupWithAttributes findGroupWithAttributesByName(final Application application, final String name) throws GroupNotFoundException
    {
        for (final Directory directory : getActiveDirectories(application))
        {
            try
            {
                return directoryManager.findGroupWithAttributesByName(directory.getId(), name);
            }
            catch (final GroupNotFoundException e)
            {
                // group not in directory, keep cycling
            }
            catch (final OperationFailedException e)
            {
                // directory has some massive error, keep cycling
                logger.error(e.getMessage(), e);
            }
            catch (DirectoryNotFoundException e)
            {
                // directory not found
                throw new ConcurrentModificationException("Directory mapping was removed while iterating through directories: " + e.getMessage());
            }
        }

        // could not find group in any of the directories
        throw new GroupNotFoundException(name);
    }

    public Group addGroup(final Application application, final GroupTemplate group)
            throws InvalidGroupException, OperationFailedException, ApplicationPermissionException
    {
        logger.debug("Adding group <{}> for application <{}>", group.getName(), application.getName());

        try
        {
            // see if group already exists in any of the directories
            getGroup(application, group.getName());
            throw new InvalidGroupException(group, "Group already exists");
        }
        catch (final GroupNotFoundException e)
        {
            // if the group doesn't exist in ANY of the directories,
            // add the group to ALL of the directories with ADD permission
            final OperationType operationType = getCreateOperationType(group);

            // iterate through directories, try to add to all
            for (final Directory directory : getActiveDirectories(application))
            {
                if (permissionManager.hasPermission(application, directory, operationType))
                {
                    try
                    {
                        group.setDirectoryId(directory.getId());
                        directoryManager.addGroup(directory.getId(), group);
                    }
                    catch (final DirectoryPermissionException dpe)
                    {
                        // this is a legitimate alternate-flow, skip over this directory
                        logger.info("Could not add group <{}> to directory <{}>", group.getName(), directory.getName());
                        logger.info(dpe.getMessage());
                    }
                    catch (final DirectoryNotFoundException onfe)
                    {
                        // Log the error and keep trying, the given directory could not be found
                        logger.error(onfe.getMessage(), onfe);
                    }
                }
            }
        }

        try
        {
            // return the added group by finding it
            return getGroup(application, group.getName());
        }
        catch (final GroupNotFoundException e)
        {
            // no application/directory had add permissions
            throw new ApplicationPermissionException("Application \"" + application.getName() + "\" does not allow adding of groups");
        }
    }

    public Group updateGroup(final Application application, final GroupTemplate group)
            throws InvalidGroupException, OperationFailedException, ApplicationPermissionException, GroupNotFoundException
    {
        logger.debug("Updating group <{}> for application <{}>", group.getName(), application.getName());

        // make sure group exists in at least one of the directories
        final Group groupToUpdate = getGroup(application, group.getName());
        final OperationType operationType = getUpdateOperationType(groupToUpdate);

        boolean atleastOneDirectoryHasPermission = false;

        // iterate through directories, try to add to all
        for (final Directory directory : getActiveDirectories(application))
        {
            if (permissionManager.hasPermission(application, directory, operationType))
            {
                try
                {
                    group.setDirectoryId(directory.getId());
                    directoryManager.updateGroup(directory.getId(), group);

                    atleastOneDirectoryHasPermission = true;
                }
                catch (final DirectoryPermissionException dpe)
                {
                    // this is a legitimate alternate-flow, skip over this directory
                    logger.info("Could not update group <{}> to directory <{}>", group.getName(), directory.getName());
                    logger.info(dpe.getMessage());
                }
                catch (final GroupNotFoundException e)
                {
                    // group does not exist in this directory, skip
                }
                catch (DirectoryNotFoundException e)
                {
                    // directory not found
                    throw new ConcurrentModificationException("Directory mapping was removed while iterating through directories: " + e.getMessage());
                }
                catch (ReadOnlyGroupException e)
                {
                    if (logger.isInfoEnabled())
                    {
                        logger.info(MessageFormatter.format("Could not update group <{}> to directory <{}> because the group is read-only.", group.getName(), directory.getName()), e);
                    }
                }
            }
        }

        if (!atleastOneDirectoryHasPermission)
        {
            throw new ApplicationPermissionException("Application \"" + application.getName() + "\" does not allow group modifications");
        }

        // return the updated group by finding it
        return getGroup(application, group.getName());
    }

    public void storeGroupAttributes(final Application application, final String groupname, final Map<String, Set<String>> attributes)
            throws OperationFailedException, ApplicationPermissionException, GroupNotFoundException
    {
        logger.debug("Storing group attributes for group <{}> and application <{}>", groupname, application.getName());

        // make sure group exists in at least one of the directories
        final Group groupToUpdate = getGroup(application, groupname);
        final OperationType operationType = getUpdateAttributeOperationType(groupToUpdate);

        boolean atleastOneDirectoryHasPermission = false;

        // iterate through directories, try to add to all
        for (final Directory directory : getActiveDirectories(application))
        {
            if (permissionManager.hasPermission(application, directory, operationType))
            {
                try
                {
                    directoryManager.storeGroupAttributes(directory.getId(), groupname, attributes);

                    atleastOneDirectoryHasPermission = true;
                }
                catch (final DirectoryPermissionException dpe)
                {
                    // this is a legitimate alternate-flow, skip over this directory
                    logger.info("Could not update group <{}> to directory <{}>", groupname, directory.getName());
                    logger.info(dpe.getMessage());
                }
                catch (final GroupNotFoundException e)
                {
                    // group does not exist in this directory, skip
                }
                catch (DirectoryNotFoundException e)
                {
                    // directory not found
                    throw new ConcurrentModificationException("Directory mapping was removed while iterating through directories: " + e.getMessage());
                }
            }
        }

        if (!atleastOneDirectoryHasPermission)
        {
            throw new ApplicationPermissionException("Application \"" + application.getName() + "\" does not allow group attribute modifications");
        }
    }

    public void removeGroupAttributes(final Application application, final String groupname, final String attributeName)
            throws OperationFailedException, ApplicationPermissionException, GroupNotFoundException
    {
        logger.debug("Removing group attributes for group <{}> and application <{}>", groupname, application.getName());

        // make sure group exists in at least one of the directories
        final Group groupToUpdate = getGroup(application, groupname);

        boolean atleastOneDirectoryHasPermission = false;

        final OperationType operationType = getUpdateAttributeOperationType(groupToUpdate);

        // iterate through directories, try to add to all
        for (final Directory directory : getActiveDirectories(application))
        {
            if (permissionManager.hasPermission(application, directory, operationType))
            {
                try
                {
                    directoryManager.removeGroupAttributes(directory.getId(), groupname, attributeName);

                    atleastOneDirectoryHasPermission = true;
                }
                catch (final DirectoryPermissionException dpe)
                {
                    // this is a legitimate alternate-flow, skip over this directory
                    logger.info("Could not update group <{}> to directory <{}>", groupname, directory.getName());
                    logger.info(dpe.getMessage());
                }
                catch (final GroupNotFoundException e)
                {
                    // group does not exist in this directory, skip
                }
                catch (DirectoryNotFoundException e)
                {
                    // directory not found
                    throw new ConcurrentModificationException("Directory mapping was removed while iterating through directories: " + e.getMessage());
                }
            }
        }

        if (!atleastOneDirectoryHasPermission)
        {
            throw new ApplicationPermissionException("Application \"" + application.getName() + "\" does not allow group attribute modifications");
        }
    }

    public void removeGroup(final Application application, final String groupname)
            throws OperationFailedException, ApplicationPermissionException, GroupNotFoundException
    {
        // eagerly throw GroupNotFoundException
        final Group groupToRemove = getGroup(application, groupname);

        boolean permissibleByAnyDirectory = false;

        final OperationType operationType = getDeleteOperationType(groupToRemove);

        // remove each group
        for (final Directory directory : getActiveDirectories(application))
        {
            if (permissionManager.hasPermission(application, directory, operationType))
            {
                try
                {
                    directoryManager.removeGroup(directory.getId(), groupname);
                    permissibleByAnyDirectory = true;
                }
                catch (final DirectoryPermissionException e)
                {
                    // this is a legitimate alternate-flow, skip over this directory
                    logger.info("Could not remove group <{}> from directory <{}>", groupname, directory.getName());
                }
                catch (final GroupNotFoundException e)
                {
                    // this directory does not have the group, skip
                }
                catch (DirectoryNotFoundException e)
                {
                    // directory not found
                    throw new ConcurrentModificationException("Directory mapping was removed while iterating through directories: " + e.getMessage());
                }
                catch (ReadOnlyGroupException e)
                {
                    if (logger.isInfoEnabled())
                    {
                        logger.info(MessageFormatter.format("Could not update group <{}> to directory <{}> because the group is read-only.", groupname, directory.getName()), e);
                    }
                }
            }
        }

        if (!permissibleByAnyDirectory)
        {
            // no directory had remove permissions
            throw new ApplicationPermissionException("Application \"" + application.getName() + "\" does not allow group removal");
        }

    }

    public <T> List<T> searchGroups(final Application application, final EntityQuery<T> query)
    {
        // explicitly disallow searches for embedded groups
        QueryUtils.checkAssignableFrom(query.getReturnType(), String.class, Group.class);

        ResultsAggregator<T> results = ResultsAggregator.with(getAggregatingAndSortingComparatorFor(query.getReturnType()), query);

        // TODO: implement this so it's more efficient with indexing + max results

        for (final Directory directory : getActiveDirectories(application))
        {
            try
            {
                final EntityQuery<T> totalQuery = QueryBuilder.queryFor(query.getReturnType(), query.getEntityDescriptor(),
                    query.getSearchRestriction(), 0, results.getRequiredResultCount());

                // perform the search
                final List<T> groups = directoryManager.searchGroups(directory.getId(), totalQuery);
                results.addAll(groups);
            }
            catch (final DirectoryNotFoundException e)
            {
                // directory does not exist, just skip
            }
            catch (final OperationFailedException e)
            {
                // keep cycling
                logger.error(e.getMessage(), e);
            }
        }

        return results.constrainResults();
    }

    ///////////////////// MEMBERSHIP OPERATIONS /////////////////////

    public void addUserToGroup(final Application application, final String username, final String groupName) throws OperationFailedException, ApplicationPermissionException, UserNotFoundException, GroupNotFoundException
    {
        // Find the user or throw UserNotFoundException
        final User user = fastFailingFindUser(application, username);
        // try to find this group within the User's directory
        Group group;
        try
        {
            group = ensureGroupExistsInDirectory(user.getDirectoryId(), groupName, application);
        }
        catch (DirectoryNotFoundException e)
        {
            // directory not found
            throw new ConcurrentModificationException("Directory mapping was removed while adding user to a group: " + e.getMessage());
        }

        final OperationType operationType = getUpdateOperationType(group);

        final Directory directory = findDirectoryById(user.getDirectoryId());

        if (!permissionManager.hasPermission(application, directory, operationType))
        {
            // no directory had remove permissions
            throw new ApplicationPermissionException(
                "Cannot update group '" + groupName + "' because directory '" + directory.getName() + "' does not allow updates.");
        }

        try
        {
            directoryManager.addUserToGroup(directory.getId(), username, groupName);
        }
        catch (final DirectoryPermissionException ex)
        {
            // Should not occur as we already checked
            throw new ApplicationPermissionException(
                "Permission Exception when trying to update group '" + groupName + "' in directory '" + directory.getName() + "'.", ex);
        }
        catch (DirectoryNotFoundException e)
        {
            // directory not found
            throw new ConcurrentModificationException("Directory mapping was removed while adding user to a group: " + e.getMessage());
        }
        catch (ReadOnlyGroupException e)
        {
            throw new ApplicationPermissionException(String.format("Could not add user %s to group %s in directory %s because the directory or group is read-only.", username, groupName, directory.getName()));
        }
    }

    /**
     * Finds the directory. If the directory could not be found, catches DirectoryNotFoundException and throws ConcurrentModificationException
     *
     * @param directoryId The directory id
     * @throws ConcurrentModificationException when the directory mapping has been concurrently modified
     * @return the requested directory
     */
    private Directory findDirectoryById(final long directoryId) throws ConcurrentModificationException
    {
        try
        {
            // Get the Directory that the existing user lives in
            return directoryManager.findDirectoryById(directoryId);
        }
        catch (final DirectoryNotFoundException e)
        {
            // directory not found
            throw new ConcurrentModificationException("Directory mapping was removed while iterating through directories: " + e.getMessage());
        }
    }

    /**
     * Tries to find the given group in the given directory.
     * <p/>
     * If this group exists in another directory, but not this one, it is added to this directory.
     * If the group does not currently exist in any directory, it throws GroupNotFoundException.
     * <p/>
     * This is the desired amalgamation behaviour for adding a user to a group.
     *
     * @param directoryId The Directory
     * @param groupName   The Group
     * @param application The application
     * @return The Group object
     * @throws GroupNotFoundException         If the group does not exist in ANY directory
     * @throws ApplicationPermissionException If the group does not exist in the given directory, and cannot be added
     * @throws OperationFailedException       Underlying directory implementation failed to execute the operation.
     * @throws DirectoryNotFoundException     If the given directory could not be found
     */
    private Group ensureGroupExistsInDirectory(final long directoryId, final String groupName, final Application application)
            throws GroupNotFoundException, ApplicationPermissionException, OperationFailedException, DirectoryNotFoundException
    {
        try
        {
            // See if the Group already exists in the given directory
            return directoryManager.findGroupByName(directoryId, groupName);
        }
        catch (final GroupNotFoundException ex)
        {
            // assert that the group exists in at least one directory (or throw GroupNotFoundException)
            final Group group = findGroupByName(application, groupName);
            // The Group does not currently exist in the User's directory, but the group DOES exist.
            // Groups exist virtually across all directories - attempt to add it on the fly to our directory
            try
            {
                logger.info("Creating group '{}' in directory {} so membership can be added", groupName, directoryId);
                return directoryManager.addGroup(directoryId, new GroupTemplate(groupName, directoryId, group.getType()));
            }
            catch (final DirectoryPermissionException ex2)
            {
                throw new ApplicationPermissionException("Group '" + groupName + "' does not exist in the directory of the user and cannot be added.");
            }
            catch (InvalidGroupException e)
            {
                throw new OperationFailedException(e.getMessage(), e);
            }
        }
    }

    public void addGroupToGroup(final Application application, final String childGroupName, final String parentGroupName) throws OperationFailedException, ApplicationPermissionException, GroupNotFoundException, InvalidMembershipException
    {
        // Verify that the 2 groups exist (or throw GroupNotFoundException)
        final Group parentGroup = getGroup(application, parentGroupName);
        final Group childGroup = getGroup(application, childGroupName);
        // Check that the groups are the same "type"
        if (parentGroup.getType() != childGroup.getType())
        {
            throw new InvalidMembershipException(
                "Cannot add group of type " + childGroup.getType().name() + " to group of type " + parentGroup.getType().name());
        }
        // Trivial circular reference: same group
        if (childGroupName.equals(parentGroupName))
        {
            throw new InvalidMembershipException("Cannot add a group to itself.");
        }
        // Check for circular reference - is the "parent" already a member of the "child" in our application?
        // (Note that this could still cause problems in other applications.)
        if (isGroupNestedGroupMember(application, parentGroupName, childGroupName))
        {
            throw new InvalidMembershipException(
                "Cannot add child group '" + childGroupName + "' to parent group '" + parentGroupName + "' - this would cause a circular dependency.");
        }

        final OperationType operationType = getUpdateOperationType(parentGroup);
        boolean applicationHasPermission = false;

        // This flag will be set to true if the add operation is successful on at least one directory.
        boolean success = false;
        for (final Directory directory : getActiveDirectories(application))
        {
            if (permissionManager.hasPermission(application, directory, operationType))
            {
                applicationHasPermission = true;
                try
                {
                    directoryManager.addGroupToGroup(directory.getId(), childGroupName, parentGroupName);
                    success = true;
                }
                catch (final DirectoryPermissionException e)
                {
                    // Should not occur - we already checked this.
                    logger.warn("Could not add child group <{}> to parent group <{}> in directory <{}> - unexpected DirectoryPermissionException", new Object[] {childGroup, parentGroup, directory.getName()});
                }
                catch (final NestedGroupsNotSupportedException e)
                {
                    // Nested Groups Not Supported - skip
                    logger.debug("Could not add child group <{}> to parent group <{}> in directory <{}> - Nested Groups not supported.", new Object[] {childGroup, parentGroup, directory.getName()});
                }
                catch (final GroupNotFoundException e)
                {
                    // this directory does not have the group, skip
                }
                catch (final InvalidMembershipException e)
                {
                    // in this directory the groups are not of the same type
                    logger.info("Could not add child group <{}> to parent group <{}> in directory <{}>: {}", new Object[] {childGroup, parentGroup, directory.getName(), e.getMessage()});
                    // skip over and try next directory
                }
                catch (DirectoryNotFoundException e)
                {
                    // directory not found
                    throw new ConcurrentModificationException("Directory mapping was removed while iterating through directories: " + e.getMessage());
                }
                catch (ReadOnlyGroupException e)
                {
                    logger.info(e.getMessage(), e);
                }
            }
        }

        if (success)
        {
            return;
        }

        if (applicationHasPermission)
        {
            // We just failed. Trying to pin down a single problem is kind of silly - just that the parent and child didn't both exist in a writable directory that supports nested groups
            throw new ApplicationPermissionException("Could not add child group '" + childGroupName + "' to parent group '" + parentGroupName + "'.");
        }
        else
        {
            // no directory had remove permissions
            throw new ApplicationPermissionException("Application '" + application.getName() + "' does not allow group modifications.");
        }
    }

    public void removeUserFromGroup(final Application application, final String username, final String groupName)
            throws OperationFailedException, ApplicationPermissionException, MembershipNotFoundException, UserNotFoundException, GroupNotFoundException
    {
        // eagerly throw UserNotFoundException
        final User user = fastFailingFindUser(application, username);

        final Group groupToUpdate;
        try
        {
            groupToUpdate = directoryManager.findGroupByName(user.getDirectoryId(), groupName);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new ConcurrentModificationException("Directory was removed while processing directory: " + e.getMessage());
        }

        final OperationType operationType = getUpdateOperationType(groupToUpdate);

        if (!isUserDirectGroupMember(application, username, groupName))
        {
            throw new MembershipNotFoundException(username, groupName);
        }

        Directory directory = findDirectoryById(user.getDirectoryId());
        if (permissionManager.hasPermission(application, directory, operationType))
        {
            try
            {
                directoryManager.removeUserFromGroup(directory.getId(), username, groupName);
            }
            catch (final DirectoryPermissionException e)
            {
                // We don't have the permission we just checked
                throw new ConcurrentModificationException("Directory permissions changed while processing directory: " + e.getMessage());
            }
            catch (DirectoryNotFoundException e)
            {
                // directory not found
                throw new ConcurrentModificationException("Directory mapping was removed while processing directory: " + e.getMessage());
            }
            catch (ReadOnlyGroupException e)
            {
                throw new ApplicationPermissionException(String.format("Could not add user %s to group %s in directory %s because the directory or group is read-only.", username, groupName, directory.getName()));
            }
        }
        else
        {
            // no directory had remove permissions
            throw new ApplicationPermissionException("Application \"" + application.getName() + "\" does not allow group modifications");
        }
    }

    public void removeGroupFromGroup(final Application application, final String childGroup, final String parentGroup)
            throws OperationFailedException, ApplicationPermissionException, MembershipNotFoundException, GroupNotFoundException
    {
        boolean permissibleByAnyDirectory = false;

        if (!isGroupDirectGroupMember(application, childGroup, parentGroup))
        {
            throw new MembershipNotFoundException(childGroup, parentGroup);
        }

        final Group parentGroupToModify = getGroup(application, parentGroup);

        final OperationType operationType = getUpdateOperationType(parentGroupToModify);

        for (final Directory directory : getActiveDirectories(application))
        {
            if (permissionManager.hasPermission(application, directory, operationType) && getDirectoryImplementation(directory).supportsNestedGroups())
            {
                try
                {
                    directoryManager.removeGroupFromGroup(directory.getId(), childGroup, parentGroup);
                    permissibleByAnyDirectory = true;
                }
                catch (final DirectoryPermissionException e)
                {
                    // this is a legitimate alternate-flow, skip over this directory
                    logger.info("Could not remove child group <{}> to parent group <{}> from directory <{}>", new Object[] {childGroup, parentGroup, directory.getName()});
                }
                catch (final GroupNotFoundException e)
                {
                    // this directory does not have the group, skip
                    permissibleByAnyDirectory = true;
                }
                catch (final InvalidMembershipException e)
                {
                    // in this directory the groups are not of the same type
                    logger.info("Could not remove child group <{}> from parent group <{}> from directory <{}>: {}", new Object[] {childGroup, parentGroup, directory.getName(), e.getMessage()});

                    // skip over and try next directory
                }
                catch (DirectoryNotFoundException e)
                {
                    // directory not found
                    throw new ConcurrentModificationException("Directory mapping was removed while iterating through directories: " + e.getMessage());
                }
                catch (ReadOnlyGroupException e)
                {
                    logger.info(e.getMessage(), e);
                }
            }
        }

        if (!permissibleByAnyDirectory)
        {
            // no directory had remove permissions
            throw new ApplicationPermissionException("Application \"" + application.getName() + "\" does not allow group modifications");
        }
    }

    public boolean isUserDirectGroupMember(final Application application, final String username, final String groupName)
    {
        User user;
        try
        {
            user = findUserByName(application, username);
            return directoryManager.isUserDirectGroupMember(user.getDirectoryId(), username, groupName);
        }
        catch (final UserNotFoundException e)
        {
            // do nothing
        }
        catch (final OperationFailedException e)
        {
            logger.error(e.getMessage(), e);
        }
        catch (DirectoryNotFoundException e)
        {
            // directory not found
            throw new ConcurrentModificationException("Directory mapping was removed while determining if the user is a direct group member: " + e.getMessage());
        }
        return false;
    }

    public boolean isGroupDirectGroupMember(final Application application, final String childGroup, final String parentGroup)
    {
        for (final Directory directory : getActiveDirectories(application))
        {
            try
            {
                if (directoryManager.isGroupDirectGroupMember(directory.getId(), childGroup, parentGroup))
                {
                    return true;
                }
            }
            catch (final DirectoryNotFoundException e)
            {
                // skip
            }
            catch (final OperationFailedException e)
            {
                // skip
                logger.error(e.getMessage(), e);
            }
        }

        return false;
    }

    public boolean isUserNestedGroupMember(final Application application, final String username, final String groupName)
    {
        User user;
        try
        {
            user = findUserByName(application, username);
            return directoryManager.isUserNestedGroupMember(user.getDirectoryId(), username, groupName);
        }
        catch (final UserNotFoundException e)
        {
            // do nothing
        }
        catch (final OperationFailedException e)
        {
            logger.error(e.getMessage(), e);
        }
        catch (DirectoryNotFoundException e)
        {
            // directory not found
            throw new ConcurrentModificationException("Directory mapping was removed while determining if user is a nested group member: " + e.getMessage());
        }
        return false;
    }

    public boolean isGroupNestedGroupMember(final Application application, final String childGroup, final String parentGroup)
    {
        for (final Directory directory : getActiveDirectories(application))
        {
            try
            {
                if (directoryManager.isGroupNestedGroupMember(directory.getId(), childGroup, parentGroup))
                {
                    return true;
                }
            }
            catch (final DirectoryNotFoundException e)
            {
                // skip
            }
            catch (final OperationFailedException e)
            {
                // skip
                logger.error(e.getMessage(), e);
            }
        }

        return false;
    }

    public <T> List<T> searchDirectGroupRelationships(final Application application, final MembershipQuery<T> query)
    {
        // explicitly disallow searches for embedded objects
        QueryUtils.checkAssignableFrom(query.getReturnType(), String.class, Group.class, User.class);

        ResultsAggregator<T> results = ResultsAggregator.with(getAggregatingAndSortingComparatorFor(query.getReturnType()), query);

        if (query.getEntityToMatch().equals(EntityDescriptor.user()))
        {
            // Only search the directory of the user when searching groups for a user
            try
            {
                User user = findUserByName(application, query.getEntityNameToMatch());
                results.addAll(doDirectDirectoryMembershipQuery(query, results.getRequiredResultCount(), user.getDirectoryId()));
            }
            catch (UserNotFoundException e)
            {
                // Don't worry.  No results.
            }
        }
        else
        {
            for (final Directory directory : getActiveDirectories(application))
            {
                results.addAll(doDirectDirectoryMembershipQuery(query, results.getRequiredResultCount(), directory.getId()));
            }
        }

        return results.constrainResults();
    }


    /**
     * Searches for direct group relationships in a single directory.
     * @param query membership query.
     * @param totalResults Maximum results to return
     * @param directoryId Directory to search.
     * @return List of {@link com.atlassian.crowd.model.user.User} entities,
     *         {@link com.atlassian.crowd.model.group.Group} entities,
     *         {@link String} usernames or {@link String} group names matching the query criteria.
     */
    private <T> List<T> doDirectDirectoryMembershipQuery(final MembershipQuery<T> query, final int totalResults, final Long directoryId)
    {
        try
        {
            final MembershipQuery<T> totalQuery = QueryBuilder.createMembershipQuery(totalResults, 0, query.isFindChildren(),
                query.getEntityToReturn(), query.getReturnType(), query.getEntityToMatch(), query.getEntityNameToMatch());

            // perform the search
            return directoryManager.searchDirectGroupRelationships(directoryId, totalQuery);
        }
        catch (final DirectoryNotFoundException e)
        {
            // directory does not exist, just skip
        }
        catch (final OperationFailedException e)
        {
            // skip
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<T>();
    }

    public <T> List<T> searchNestedGroupRelationships(final Application application, final MembershipQuery<T> query)
    {
        // explicitly disallow searches for embedded objects
        QueryUtils.checkAssignableFrom(query.getReturnType(), String.class, Group.class, User.class);

        ResultsAggregator<T> results = ResultsAggregator.with(getAggregatingAndSortingComparatorFor(query.getReturnType()), query);

        if (query.getEntityToMatch().equals(EntityDescriptor.user()))
        {
            // Only search the directory of the user when searching groups for a user
            try
            {
                User user = findUserByName(application, query.getEntityNameToMatch());
                results.addAll(doNestedDirectoryMembershipQuery(query, results.getRequiredResultCount(), user.getDirectoryId()));
            }
            catch (UserNotFoundException e)
            {
                // Don't worry.  No results.
            }
        }
        else
        {
            for (final Directory directory : getActiveDirectories(application))
            {
                results.addAll(doNestedDirectoryMembershipQuery(query, results.getRequiredResultCount(), directory.getId()));
            }
        }

        return results.constrainResults();
    }

    public String getCurrentEventToken(Application application) throws IncrementalSynchronisationNotAvailableException
    {
        final List<Directory> activeDirectories = ImmutableList.copyOf(getActiveDirectories(application));

        // We don't need to check this in getNewEvents, because all configuration changes will force this method to be
        // called first.
        assertIncrementalSynchronisationIsAvailable(activeDirectories);

        return eventStore.getCurrentEventToken();
    }

    public Events getNewEvents(Application application, String eventToken) throws EventTokenExpiredException, OperationFailedException
    {
        final Events events = eventStore.getNewEvents(eventToken);

        final List<OperationEvent> applicationEvents = new ArrayList<OperationEvent>();

        if (!Iterables.isEmpty(events.getEvents())) // Make the normal case fast
        {
            final List<Directory> activeDirectories = ImmutableList.copyOf(getActiveDirectories(application));

            for (final OperationEvent event : events.getEvents())
            {
                final int eventDirectoryIndex = activeDirectories.indexOf(event.getDirectory());
                if (eventDirectoryIndex == -1)
                {
                    continue; // Event is not in the active directories
                }

                final List<? extends OperationEvent> eventApplicationEvents;
                if (event instanceof UserEvent)
                {
                    final UserEvent userEvent = (UserEvent) event;
                    eventApplicationEvents = processUserEvent(activeDirectories, eventDirectoryIndex, userEvent);
                }
                else if (event instanceof GroupEvent)
                {
                    final GroupEvent groupEvent = (GroupEvent) event;
                    eventApplicationEvents = processGroupEvent(activeDirectories, eventDirectoryIndex, groupEvent);
                }
                else if (event instanceof UserMembershipEvent)
                {
                    final UserMembershipEvent userMembershipEvent = (UserMembershipEvent) event;
                    eventApplicationEvents = processUserMembershipEvent(activeDirectories, userMembershipEvent);
                }
                else if (event instanceof GroupMembershipEvent)
                {
                    final GroupMembershipEvent groupMembershipEvent = (GroupMembershipEvent) event;
                    eventApplicationEvents = processGroupMembershipEvent(activeDirectories, groupMembershipEvent);
                }
                else
                {
                    throw new IllegalArgumentException("Event type " + event.getClass() + " not supported.");
                }
                applicationEvents.addAll(eventApplicationEvents);
            }
        }

        return new Events(applicationEvents, events.getNewEventToken());
    }

    private void assertIncrementalSynchronisationIsAvailable(List<Directory> activeDirectories) throws IncrementalSynchronisationNotAvailableException
    {
        for (Directory directory : activeDirectories)
        {
            // No events are generated for cacheable directories that are not cached
            if (isFalse(toBooleanObject(directory.getValue(DirectoryProperties.CACHE_ENABLED))))
            {
                throw new IncrementalSynchronisationNotAvailableException("Directory '" + directory.getName() + "' is not cached and so cannot be incrementally synchronised");
            }
        }
    }

    private List<? extends OperationEvent> processUserEvent(List<Directory> activeDirectories, int eventDirectoryIndex, UserEvent event) throws OperationFailedException
    {
        final List<? extends OperationEvent> events;
        final String username = event.getUser().getName();
        final List<Directory> earlierDirectories = activeDirectories.subList(0, eventDirectoryIndex);
        if (findUser(earlierDirectories, username) != null) // Masked
        {
            if (event.getOperation() == Operation.DELETED)
            {
                final Set<String> parentGroupNames = getParentGroupNames(activeDirectories, EntityDescriptor.user(), username);
                events = ImmutableList.of(new UserMembershipEvent(Operation.UPDATED, null, username, parentGroupNames));
            }
            else
            {
                // Event entity is masked by an entity in earlier directory
                events = ImmutableList.of();
            }
        }
        else if (event.getOperation() == Operation.CREATED)
        {
            final List<Directory> laterDirectories = activeDirectories.subList(eventDirectoryIndex + 1, activeDirectories.size());
            if (findUser(laterDirectories, username) != null) // Masking
            {
                events = ImmutableList.of(new UserEvent(Operation.UPDATED,
                        null, // Not used
                        event.getUser(),
                        event.getStoredAttributes(),
                        event.getDeletedAttributes()));
            }
            else
            {
                events = ImmutableList.of(event);
            }
        }
        else if (event.getOperation() == Operation.DELETED)
        {
            final List<Directory> laterDirectories = activeDirectories.subList(eventDirectoryIndex + 1, activeDirectories.size());
            final User laterUser = findUser(laterDirectories, username);
            if (laterUser != null) // Masking
            {
                final Set<String> parentGroupNames = getParentGroupNames(activeDirectories, EntityDescriptor.user(), username);
                final OperationEvent userEvent = new UserEvent(Operation.UPDATED,
                        null, // Not used
                        laterUser,
                        null,
                        null);
                final OperationEvent membershipEvent = new UserMembershipEvent(Operation.UPDATED, null, username, parentGroupNames);
                events = ImmutableList.of(userEvent, membershipEvent);
            }
            else
            {
                events = ImmutableList.of(event);
            }
        }
        else // Updated
        {
            events = ImmutableList.of(event);
        }
        return events;
    }

    private List<? extends OperationEvent> processGroupEvent(List<Directory> activeDirectories, int eventDirectoryIndex, GroupEvent event) throws OperationFailedException
    {
        final List<? extends OperationEvent> events;
        final String groupName = event.getGroup().getName();
        final List<Directory> earlierDirectories = activeDirectories.subList(0, eventDirectoryIndex);
        if (findGroup(earlierDirectories, groupName) != null) // Masked
        {
            if (event.getOperation() == Operation.DELETED)
            {
                final Set<String> parentGroupNames = getParentGroupNames(activeDirectories, EntityDescriptor.group(GroupType.GROUP), groupName);
                final Set<String> childGroupNames = getChildGroupNames(activeDirectories, groupName);
                events = ImmutableList.of(new GroupMembershipEvent(Operation.UPDATED, null, groupName, parentGroupNames, childGroupNames));
            }
            else
            {
                // Event entity is masked by an entity in earlier directory
                events = ImmutableList.of();
            }
        }
        else if (event.getOperation() == Operation.CREATED)
        {
            final List<Directory> laterDirectories = activeDirectories.subList(eventDirectoryIndex + 1, activeDirectories.size());
            if (findGroup(laterDirectories, groupName) != null) // Masking
            {
                events = ImmutableList.of(new GroupEvent(
                        Operation.UPDATED,
                        null, // Not used
                        event.getGroup(),
                        event.getStoredAttributes(),
                        event.getDeletedAttributes()));
            }
            else
            {
                events = ImmutableList.of(event);
            }
        }
        else if (event.getOperation() == Operation.DELETED)
        {
            final List<Directory> laterDirectories = activeDirectories.subList(eventDirectoryIndex + 1, activeDirectories.size());
            final Group laterGroup = findGroup(laterDirectories, groupName);
            if (laterGroup != null) // Masking
            {
                final OperationEvent groupEvent = new GroupEvent(
                        Operation.UPDATED,
                        null, // Not used
                        laterGroup,
                        null,
                        null);

                final Set<String> parentGroupNames = getParentGroupNames(activeDirectories, EntityDescriptor.group(GroupType.GROUP), groupName);
                final Set<String> childGroupNames = getChildGroupNames(activeDirectories, groupName);
                final OperationEvent membershipEvent = new GroupMembershipEvent(Operation.UPDATED, null, groupName, parentGroupNames, childGroupNames);
                events = ImmutableList.of(groupEvent, membershipEvent);
            }
            else
            {
                events = ImmutableList.of(event);
            }
        }
        else // Updated
        {
            events = ImmutableList.of(event);
        }
        return events;
    }

    private List<OperationEvent> processUserMembershipEvent(List<Directory> activeDirectories, UserMembershipEvent event) throws OperationFailedException
    {
        final OperationEvent applicationEvent;
        if (event.getOperation() == Operation.DELETED)
        {
            // Remove only if last
            final String username = event.getChildUsername();
            final Set<String> deletedParentGroupNames = toLowerCaseIdentifiers(event.getParentGroupNames());
            final Set<String> remainingParentGroupNames = toLowerCaseIdentifiers(getParentGroupNames(activeDirectories, EntityDescriptor.user(), username));
            final Set<String> disappearedParentGroupNames = Sets.difference(deletedParentGroupNames, remainingParentGroupNames);
            applicationEvent = new UserMembershipEvent(Operation.DELETED, event.getDirectory(), username, disappearedParentGroupNames);
        }
        else
        {
            applicationEvent = event;
        }
        return ImmutableList.of(applicationEvent);
    }

    private List<OperationEvent> processGroupMembershipEvent(List<Directory> activeDirectories, GroupMembershipEvent event) throws OperationFailedException
    {
        final OperationEvent applicationEvent;
        if (event.getOperation() == Operation.DELETED)
        {
            // Remove only if last
            final String groupName = event.getGroupName();

            final Set<String> deletedParentGroupNames = toLowerCaseIdentifiers(event.getParentGroupNames());
            final Set<String> remainingParentGroupNames = toLowerCaseIdentifiers(getParentGroupNames(activeDirectories, EntityDescriptor.group(), groupName));
            final Set<String> parentGroupNames = Sets.difference(deletedParentGroupNames, remainingParentGroupNames);

            final Set<String> deletedChildGroupNames = toLowerCaseIdentifiers(event.getChildGroupNames());
            final Set<String> remainingChildGroupNames = toLowerCaseIdentifiers(getChildGroupNames(activeDirectories, groupName));
            final Set<String> childGroupNames = Sets.difference(deletedChildGroupNames, remainingChildGroupNames);

            applicationEvent = new GroupMembershipEvent(Operation.DELETED, event.getDirectory(), groupName, parentGroupNames, childGroupNames);
        }
        else
        {
            applicationEvent = event;
        }
        return ImmutableList.of(applicationEvent);
    }

    /**
     * Returns parent group names across all directories.
     *
     * @param activeDirectories active directories for the application
     * @param entityDescriptor type of the entity
     * @param name name of the entity
     * @return list of parent group names of the entity
     * @throws OperationFailedException if one of the active directories does not exist
     */
    private Set<String> getParentGroupNames(List<Directory> activeDirectories, EntityDescriptor entityDescriptor, String name) throws OperationFailedException
    {
        final Set<String> parentGroupNames = new HashSet<String>();
        for (Directory directory : activeDirectories)
        {
            try
            {
                parentGroupNames.addAll(directoryManager.searchDirectGroupRelationships(directory.getId(),
                        QueryBuilder.queryFor(String.class, EntityDescriptor.group(GroupType.GROUP))
                                    .parentsOf(entityDescriptor)
                                    .withName(name)
                                    .returningAtMost(EntityQuery.ALL_RESULTS)));
            }
            catch (DirectoryNotFoundException e)
            {
                // Next sync should pick up this change.
                throw new OperationFailedException("Directory has been removed", e);
            }
        }

        return parentGroupNames;
    }

    /**
     * Returns child group names for a group across all directories.
     *
     * @param activeDirectories active directories for the application
     * @param groupName name of the group
     * @return list of child group names of the group
     * @throws OperationFailedException if one of the active directories does not exist
     */
    private Set<String> getChildGroupNames(List<Directory> activeDirectories, String groupName) throws OperationFailedException
    {
        final Set<String> childGroupNames = new HashSet<String>();
        for (Directory directory : activeDirectories)
        {
            try
            {
                childGroupNames.addAll(directoryManager.searchDirectGroupRelationships(directory.getId(),
                        QueryBuilder.queryFor(String.class, EntityDescriptor.group(GroupType.GROUP))
                                    .childrenOf(EntityDescriptor.group(GroupType.GROUP))
                                    .withName(groupName)
                                    .returningAtMost(EntityQuery.ALL_RESULTS)));
            }
            catch (DirectoryNotFoundException e)
            {
                // Next sync should pick up this change.
                throw new OperationFailedException("Directory has been removed", e);
            }
        }

        return childGroupNames;
    }

    private User findUser(Iterable<Directory> directories, String username) throws OperationFailedException
    {
        try
        {
            for (Directory directory : directories)
            {
                try
                {
                    return directoryManager.findUserByName(directory.getId(), username);
                }
                catch (UserNotFoundException e)
                {
                    // Continue looping
                }
            }
            return null;
        }
        catch (DirectoryNotFoundException e)
        {
            throw new ConcurrentModificationException("Directory was removed in the middle of this operation");
        }
    }

    private Group findGroup(Iterable<Directory> directories, String groupName) throws OperationFailedException
    {
        try
        {
            for (Directory directory : directories)
            {
                try
                {
                    return directoryManager.findGroupByName(directory.getId(), groupName);
                }
                catch (GroupNotFoundException e)
                {
                    // Continue looping
                }
            }
            return null;
        }
        catch (DirectoryNotFoundException e)
        {
            throw new ConcurrentModificationException("Directory was removed in the middle of this operation");
        }
    }

    /**
     * Searches for direct and indirect (nested) group relationships in a single directory.
     * @param query membership query.
     * @param totalResults Maximum results to return
     * @param directoryId Directory to search.
     * @return List of {@link com.atlassian.crowd.model.user.User} entities,
     *         {@link com.atlassian.crowd.model.group.Group} entities,
     *         {@link String} usernames or {@link String} group names matching the query criteria.
     */
    private <T> List<T> doNestedDirectoryMembershipQuery(final MembershipQuery<T> query, final int totalResults, final Long directoryId)
    {
        try
        {
            final MembershipQuery<T> totalQuery = QueryBuilder.createMembershipQuery(totalResults, 0, query.isFindChildren(),
                query.getEntityToReturn(), query.getReturnType(), query.getEntityToMatch(), query.getEntityNameToMatch());

            // perform the search
            return directoryManager.searchNestedGroupRelationships(directoryId, totalQuery);
        }
        catch (final DirectoryNotFoundException e)
        {
            // directory does not exist, just skip
        }
        catch (final OperationFailedException e)
        {
            // skip
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<T>();
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

    private RemoteDirectory getDirectoryImplementation(final Directory directory) throws DirectoryInstantiationException
    {
        return directoryInstanceLoader.getDirectory(directory);
    }

    private Iterable<Directory> getActiveDirectories(final Application application)
    {
        return Iterables.filter(Iterables.transform(application.getDirectoryMappings(), DirectoryResolver.INSTANCE), ActiveDirectorFilter.INSTANCE);
    }

    enum DirectoryResolver implements Function<DirectoryMapping, Directory>
    {
        INSTANCE;

        public Directory apply(final DirectoryMapping from)
        {
            return from.getDirectory();
        }
    }

    enum ActiveDirectorFilter implements Predicate<Directory>
    {
        INSTANCE;

        public boolean apply(final Directory from)
        {
            return from.isActive();
        }
    }

    /**
     * Returns a comparator for aggregating and sorting the results.
     * <p/>
     * For:
     * <ul>
     * <li>String: names are aggregated based on case-insensitive comparison.
     * <li>User: users are aggregated based on case-insensitive comparison and directoryId. Two users with the same
     * name but from different directories are treated as different users.
     * <li>Group: groups are aggregated based on case-insensitive comparison. Two groups with the same name but
     * from different directories are treated as the same group.
     * </ul>
     *
     * @param type type we are aggregating.
     * @return comparator for the type.
     */
    private static <T> Function<T, String> getAggregatingAndSortingComparatorFor(final Class<T> type)
    {
        if (String.class.isAssignableFrom(type))
        {
            return NameComparator.normaliserOf(type);
        }
        else if (User.class.isAssignableFrom(type))
        {
            // by default user names should always be unique. only {@link #searchUsersAllowingDuplicateNames} will allow
            // users with the same user name but different directory ID to be returned.
            return NameComparator.normaliserOf(type);
        }
        else if (Group.class.isAssignableFrom(type))
        {
            return NameComparator.normaliserOf(type);
        }
        else
        {
            throw new IllegalArgumentException("Cannot find normaliser for type: " + type.getCanonicalName());
        }
    }

    /**
     * Determines if a user is authorised to authenticate with a given application.
     * <p/>
     * For a user to have access to an application:
     * <ol>
     * <li>the Application must be active.</li>
     * <li>and either:
     * <ul>
     * <li>the User is stored in a directory which is associated to the Application and the "allow all to authenticate"
     *     flag is true.</li>
     * <li>the User is a member of a Group that is allowed to authenticate with the Application and both the User and
     *     Group are from the same RemoteDirectory.</li>
     * </ul></li>
     * </ol>
     * <p/>
     * Note that this call is not cached and does not affect the
     * cache.
     * @param application application the user wants to authenticate with.
     * @param username the username of the user that wants to authenticate with the application.
     * @param directoryId the directoryId of the user that wants to authenticate with the application.
     * @return <code>true</code> iff the user is authorised to authenticate with the application.
     * @throws OperationFailedException if the directory implementation could not be loaded when performing a membership check.
     * @throws DirectoryNotFoundException
     */
    private boolean isAllowedToAuthenticate(String username, long directoryId, Application application)
            throws OperationFailedException, DirectoryNotFoundException
    {
        // first make sure the application is not inactive
        if (!application.isActive())
        {
            logger.debug("User does not have access to application '{}' as the application is inactive", application.getName());
            return false;
        }

        // see if the application has a mapping for the user's directory
        DirectoryMapping directoryMapping = application.getDirectoryMapping(directoryId);
        if (directoryMapping != null)
        {
            if (directoryMapping.isAllowAllToAuthenticate())
            {
                // all users of this directory can authenticate
                return true;
            }
            else
            {
                // check individual group mappings
                for (GroupMapping groupMapping : directoryMapping.getAuthorisedGroups())
                {
                    if (directoryManager.isUserNestedGroupMember(directoryId, username, groupMapping.getGroupName()))
                    {
                        return true;
                    }
                }
            }
        }

        // did not have allow all on directory OR meet group membership requirements
        logger.debug("User does not have access to application '{}' as the directory is not allow all to authenticate and the user is not a member of any of the authorised groups", application.getName());
        return false;
    }

    private Set<String> toLowerCaseIdentifiers(Iterable<String> identifiers)
    {
        return ImmutableSet.copyOf(Iterables.transform(identifiers, IdentifierUtils.TO_LOWER_CASE));
    }
}
