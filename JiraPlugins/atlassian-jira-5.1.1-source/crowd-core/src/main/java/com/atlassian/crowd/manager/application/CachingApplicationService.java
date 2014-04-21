package com.atlassian.crowd.manager.application;

import com.atlassian.crowd.cache.UserAuthorisationCache;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.event.EventTokenExpiredException;
import com.atlassian.crowd.event.Events;
import com.atlassian.crowd.event.IncrementalSynchronisationNotAvailableException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.BulkAddFailedException;
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
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of ApplicationService which caches the result of ApplicationService methods.
 *
 * @since v2.2
 */
public class CachingApplicationService implements ApplicationService
{
    private final ApplicationService applicationService;

    private final UserAuthorisationCache userAuthorisationCache;

    public CachingApplicationService(final ApplicationService applicationService, final UserAuthorisationCache userAuthorisationCache)
    {
        this.applicationService = checkNotNull(applicationService);
        this.userAuthorisationCache = checkNotNull(userAuthorisationCache);
    }

    public User authenticateUser(final Application application, final String username, final PasswordCredential passwordCredential)
            throws OperationFailedException, InactiveAccountException, InvalidAuthenticationException, ExpiredCredentialException, UserNotFoundException
    {
        return applicationService.authenticateUser(application, username, passwordCredential);
    }

    public boolean isUserAuthorised(final Application application, final String username)
    {
        Boolean allowedToAuthenticate = userAuthorisationCache.isPermitted(username, application.getName());
        if (allowedToAuthenticate != null)
        {
            return allowedToAuthenticate;
        }
        else
        {
            boolean permitted = applicationService.isUserAuthorised(application, username);
            // only cache positive results
            if (permitted)
            {
                userAuthorisationCache.setPermitted(username, application.getName(), permitted);
            }
            return permitted;
        }
    }

    public void addAllUsers(final Application application, final Collection<UserTemplateWithCredentialAndAttributes> users)
            throws ApplicationPermissionException, OperationFailedException, BulkAddFailedException
    {
        applicationService.addAllUsers(application, users);
    }

    public User findUserByName(final Application application, final String name) throws UserNotFoundException
    {
        return applicationService.findUserByName(application, name);
    }

    public UserWithAttributes findUserWithAttributesByName(final Application application, final String name)
            throws UserNotFoundException
    {
        return applicationService.findUserWithAttributesByName(application, name);
    }

    public User addUser(final Application application, final UserTemplate user, final PasswordCredential credential)
            throws InvalidUserException, OperationFailedException, InvalidCredentialException, ApplicationPermissionException
    {
        return applicationService.addUser(application, user, credential);
    }

    public User updateUser(final Application application, final UserTemplate user)
            throws InvalidUserException, OperationFailedException, ApplicationPermissionException, UserNotFoundException
    {
        return applicationService.updateUser(application, user);
    }

    public void updateUserCredential(final Application application, final String username, final PasswordCredential credential)
            throws OperationFailedException, UserNotFoundException, InvalidCredentialException, ApplicationPermissionException
    {
        applicationService.updateUserCredential(application, username, credential);
    }

    public void resetUserCredential(final Application application, final String username)
            throws OperationFailedException, UserNotFoundException, InvalidCredentialException, ApplicationPermissionException, InvalidEmailAddressException
    {
        applicationService.resetUserCredential(application, username);
    }

    public void storeUserAttributes(final Application application, final String username, final Map<String, Set<String>> attributes)
            throws OperationFailedException, ApplicationPermissionException, UserNotFoundException
    {
        applicationService.storeUserAttributes(application, username, attributes);
    }

    public void removeUserAttributes(final Application application, final String username, final String attributeName)
            throws OperationFailedException, ApplicationPermissionException, UserNotFoundException
    {
        applicationService.removeUserAttributes(application, username, attributeName);
    }

    public void removeUser(final Application application, final String user)
            throws OperationFailedException, UserNotFoundException, ApplicationPermissionException
    {
        applicationService.removeUser(application, user);
    }

    public <T> List<T> searchUsers(final Application application, final EntityQuery<T> query)
    {
        return applicationService.searchUsers(application, query);
    }

    public List<User> searchUsersAllowingDuplicateNames(final Application application, final EntityQuery<User> query)
    {
        return applicationService.searchUsersAllowingDuplicateNames(application, query);
    }

    public Group findGroupByName(final Application application, final String name) throws GroupNotFoundException
    {
        return applicationService.findGroupByName(application, name);
    }

    public GroupWithAttributes findGroupWithAttributesByName(final Application application, final String name)
            throws GroupNotFoundException
    {
        return applicationService.findGroupWithAttributesByName(application, name);
    }

    public Group addGroup(final Application application, final GroupTemplate group)
            throws InvalidGroupException, OperationFailedException, ApplicationPermissionException
    {
        return applicationService.addGroup(application, group);
    }

    public Group updateGroup(final Application application, final GroupTemplate group)
            throws InvalidGroupException, OperationFailedException, ApplicationPermissionException, GroupNotFoundException
    {
        return applicationService.updateGroup(application, group);
    }

    public void storeGroupAttributes(final Application application, final String groupname, final Map<String, Set<String>> attributes)
            throws OperationFailedException, ApplicationPermissionException, GroupNotFoundException
    {
        applicationService.storeGroupAttributes(application, groupname, attributes);
    }

    public void removeGroupAttributes(final Application application, final String groupname, final String attributeName)
            throws OperationFailedException, ApplicationPermissionException, GroupNotFoundException
    {
        applicationService.removeGroupAttributes(application, groupname, attributeName);
    }

    public void removeGroup(final Application application, final String group)
            throws OperationFailedException, GroupNotFoundException, ApplicationPermissionException
    {
        applicationService.removeGroup(application, group);
    }

    public <T> List<T> searchGroups(final Application application, final EntityQuery<T> query)
    {
        return applicationService.searchGroups(application, query);
    }

    public void addUserToGroup(final Application application, final String username, final String groupName)
            throws OperationFailedException, UserNotFoundException, GroupNotFoundException, ApplicationPermissionException
    {
        applicationService.addUserToGroup(application, username, groupName);
    }

    public void addGroupToGroup(final Application application, final String childGroupName, final String parentGroupName)
            throws OperationFailedException, GroupNotFoundException, ApplicationPermissionException, InvalidMembershipException
    {
        applicationService.addGroupToGroup(application, childGroupName, parentGroupName);
    }

    public void removeUserFromGroup(final Application application, final String username, final String groupName)
            throws OperationFailedException, GroupNotFoundException, UserNotFoundException, ApplicationPermissionException, MembershipNotFoundException
    {
        applicationService.removeUserFromGroup(application, username, groupName);
    }

    public void removeGroupFromGroup(final Application application, final String childGroup, final String parentGroup)
            throws OperationFailedException, GroupNotFoundException, ApplicationPermissionException, MembershipNotFoundException
    {
        applicationService.removeGroupFromGroup(application, childGroup, parentGroup);
    }

    public boolean isUserDirectGroupMember(final Application application, final String username, final String groupName)
    {
        return applicationService.isUserDirectGroupMember(application, username, groupName);
    }

    public boolean isGroupDirectGroupMember(final Application application, final String childGroup, final String parentGroup)
    {
        return applicationService.isGroupDirectGroupMember(application, childGroup, parentGroup);
    }

    public boolean isUserNestedGroupMember(final Application application, final String username, final String groupName)
    {
        return applicationService.isUserNestedGroupMember(application, username, groupName);
    }

    public boolean isGroupNestedGroupMember(final Application application, final String childGroup, final String parentGroup)
    {
        return applicationService.isGroupNestedGroupMember(application, childGroup, parentGroup);
    }

    public <T> List<T> searchDirectGroupRelationships(final Application application, final MembershipQuery<T> query)
    {
        return applicationService.searchDirectGroupRelationships(application, query);
    }

    public <T> List<T> searchNestedGroupRelationships(final Application application, final MembershipQuery<T> query)
    {
        return applicationService.searchNestedGroupRelationships(application, query);
    }

    public String getCurrentEventToken(Application application) throws IncrementalSynchronisationNotAvailableException
    {
        return applicationService.getCurrentEventToken(application);
    }

    public Events getNewEvents(Application application, String eventToken) throws EventTokenExpiredException, OperationFailedException
    {
        return applicationService.getNewEvents(application, eventToken);
    }
}
