package com.atlassian.crowd.plugin.rest.service.controller;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.ApplicationNotFoundException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidEmailAddressException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.manager.login.ForgottenLoginManager;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.plugin.rest.entity.MultiValuedAttributeEntityList;
import com.atlassian.crowd.plugin.rest.entity.GroupEntity;
import com.atlassian.crowd.plugin.rest.entity.GroupEntityList;
import com.atlassian.crowd.plugin.rest.entity.UserEntity;
import com.atlassian.crowd.plugin.rest.util.EntityTranslator;
import com.atlassian.crowd.plugin.rest.util.UserEntityUtil;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.plugins.rest.common.Link;
import org.apache.commons.lang.Validate;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller responsible for user management.
 */
public class UsersController extends AbstractResourceController
{
    private final ForgottenLoginManager forgottenLoginManager;

    public UsersController(final ApplicationService applicationService, final ApplicationManager applicationManager, final ForgottenLoginManager forgottenLoginManager)
    {
        super(applicationService, applicationManager);
        this.forgottenLoginManager = forgottenLoginManager;
    }

    /**
     * Returns a UserEntity specified by the name.
     *
     * @param applicationName name of the application
     * @param name name of the user to retrieve
     * @param userLink link to the user resource
     * @param expandAttributes set to true if the attributes field should be expanded
     * @return UserEntity
     * @throws UserNotFoundException if the user could not be found
     */
    public UserEntity findUserByName(final String applicationName, final String name, final Link userLink, final boolean expandAttributes)
            throws UserNotFoundException
    {
        Application application = getApplication(applicationName);
        return UserEntityUtil.expandUser(applicationService, application, UserEntity.newMinimalUserEntity(name, applicationName, userLink), expandAttributes);
    }

    /**
     * Adds a new user.
     *
     * @param applicationName name of the application
     * @param userEntity new user to add
     * @return canonical username
     * @throws ApplicationPermissionException if none of the application's underlying directories are allowed to perform this operation
     * @throws OperationFailedException if the operation failed for any other reason
     * @throws InvalidCredentialException if the password is not valid
     * @throws InvalidUserException if the user is not valid
     */
    public String addUser(final String applicationName, final UserEntity userEntity)
            throws ApplicationPermissionException, InvalidUserException, InvalidCredentialException, OperationFailedException
    {
        Application application = getApplication(applicationName);

        UserWithAttributes user = EntityTranslator.fromUserEntity(userEntity);
        if (user instanceof UserTemplate)
        {
            User newUser = applicationService.addUser(application, (UserTemplate) user, PasswordCredential.unencrypted(userEntity.getPassword().getValue()));
            return newUser.getName();
        }
        else
        {
            throw new AssertionError("Should be a UserTemplate.");
        }
    }

    /**
     * Updates a user.
     *
     * @param applicationName name of the application
     * @param userEntity updated user information
     * @return canonical username
     * @throws ApplicationPermissionException if none of the application's underlying directories are allowed to perform this operation
     * @throws InvalidUserException if the user template does not have the required properties populated.
     * @throws UserNotFoundException if the user was not found
     * @throws OperationFailedException if the operation failed for any other reason
     */
    public String updateUser(final String applicationName, final UserEntity userEntity)
            throws ApplicationPermissionException, InvalidUserException, UserNotFoundException, OperationFailedException
    {
        Application application = getApplication(applicationName);

        UserWithAttributes user = EntityTranslator.fromUserEntity(userEntity);
        if (user instanceof UserTemplate)
        {
            User updatedUser = applicationService.updateUser(application, (UserTemplate) user);
            return updatedUser.getName();
        }
        else
        {
            throw new AssertionError("Should be a UserTemplate.");
        }
    }

    /**
     * Stores (add/replace) the user attributes. Attribute values will not be overwritten if not specified in attributes.
     *
     * @param applicationName name of the application
     * @param username name of the user
     * @param attributes attributes of the user
     * @throws ApplicationPermissionException if none of the application's underlying directories are allowed to perform this operation
     * @throws OperationFailedException if the operation failed for any other reason
     * @throws UserNotFoundException if the user could not be found
     */
    public void storeUserAttributes(final String applicationName, final String username, final MultiValuedAttributeEntityList attributes)
            throws ApplicationPermissionException, UserNotFoundException, OperationFailedException
    {
        Validate.notNull(attributes);
        
        Application application = getApplication(applicationName);
        Map<String, Set<String>> userAttributes = EntityTranslator.toAttributes(attributes);
        applicationService.storeUserAttributes(application, username, userAttributes);
    }

    /**
     * Removes the user attribute.
     *
     * @param applicationName name of the application
     * @param username name of the user
     * @param attributeName name of attribute to remove
     * @throws ApplicationPermissionException if none of the application's underlying directories have permission to perform the operation
     * @throws OperationFailedException if the operation failed for any other reason
     * @throws UserNotFoundException if the user could not be found
     */
    public void removeUserAttribute(final String applicationName, final String username, final String attributeName)
            throws ApplicationPermissionException, UserNotFoundException, OperationFailedException
    {
        Application application = getApplication(applicationName);

        applicationService.removeUserAttributes(application, username, attributeName);
    }

    /**
     * Requests a password reset.
     *
     * @param applicationName name of the application
     * @param username name of the user
     * @throws UserNotFoundException if the user could not be found
     * @throws InvalidEmailAddressException if the user does not have a valid email to send the reset password link to
     * @throws ApplicationPermissionException if none of the application's underlying directories can update the user's password
     * @throws ApplicationNotFoundException if the application could not be found
     */
    public void requestPasswordReset(String applicationName, String username)
            throws InvalidEmailAddressException, ApplicationPermissionException, ApplicationNotFoundException, UserNotFoundException
    {
        Application application = applicationManager.findByName(applicationName);

        forgottenLoginManager.sendResetLink(application, username);
    }

    /**
     * Requests an email to be sent containing usernames associated with the given email address.
     *
     * @param applicationName name of the application
     * @param email email address of the user
     * @throws InvalidEmailAddressException if the <code>email</code> is not valid
     * @throws ApplicationNotFoundException if the application could not be found
     */
    public void requestUsernames(String applicationName, String email)
            throws InvalidEmailAddressException, ApplicationNotFoundException
    {
        Application application = applicationManager.findByName(applicationName);

        forgottenLoginManager.sendUsernames(application, email);
    }

    /**
     * Removes the user.
     *
     * @param applicationName name of the application
     * @param username name of the user
     * @throws ApplicationPermissionException if none of the application's underlying directories can update the user's password
     * @throws OperationFailedException if the operation failed for any other reason
     * @throws UserNotFoundException if the user could not be found
     */
    public void removeUser(String applicationName, String username)
            throws ApplicationPermissionException, UserNotFoundException, OperationFailedException
    {
        Application application = getApplication(applicationName);

        applicationService.removeUser(application, username);
    }

    /**
     * Updates a user's password.
     *
     * @param applicationName name of the application
     * @param username name of the user
     * @param password new password
     * @throws ApplicationPermissionException if none of the application's underlying directories are allowed to perform this operation
     * @throws UserNotFoundException if the user was not found
     * @throws OperationFailedException if the operation failed for any other reason
     * @throws InvalidCredentialException if the new password is not valid
     */
    public void updateUserPassword(String applicationName, String username, String password)
            throws ApplicationPermissionException, UserNotFoundException, InvalidCredentialException, OperationFailedException
    {
        Application application = getApplication(applicationName);

        applicationService.updateUserCredential(application, username, PasswordCredential.unencrypted(password));
    }

    /**
     * Returns the groups that the user is a direct member of.
     *
     * @param applicationName name of the application
     * @param userName name of the user
     * @param expandGroups should groups be expanded
     * @param maxResults maximum number of results returned. If -1, then all the results are returned.
     * @param startIndex starting index of the results
     * @param baseUri base URI of the REST service
     * @return groups that the user is a direct member of
     */
    public GroupEntityList getDirectGroups(final String applicationName, final String userName, final boolean expandGroups, final int maxResults, final int startIndex, final URI baseUri)
    {
        Application application = getApplication(applicationName);

        if (expandGroups)
        {
            MembershipQuery<Group> query = QueryBuilder.createMembershipQuery(maxResults, startIndex, false, EntityDescriptor.group(), Group.class, EntityDescriptor.user(), userName);
            List<Group> groups = applicationService.searchDirectGroupRelationships(application, query);

            return EntityTranslator.toGroupEntities(groups, baseUri);
        }
        else
        {
            MembershipQuery<String> query = QueryBuilder.createMembershipQuery(maxResults, startIndex, false, EntityDescriptor.group(), String.class, EntityDescriptor.user(), userName);
            List<String> groupNames = applicationService.searchDirectGroupRelationships(application, query);

            return EntityTranslator.toMinimalGroupEntities(groupNames, baseUri);
        }
    }

    /**
     * Returns the specified group that the user is a direct member of.
     *
     * @param applicationName name of the application
     * @param userName name of the user
     * @param groupName name of the group the user is a member of
     * @param baseUri base URI of the REST service
     * @return group that the user is a direct member of
     * @throws MembershipNotFoundException if the membership was not found
     */
    public GroupEntity getDirectGroup(final String applicationName, final String userName, final String groupName, final URI baseUri)
            throws MembershipNotFoundException
    {
        Application application = getApplication(applicationName);

        final boolean isMember = applicationService.isUserDirectGroupMember(application, userName, groupName);

        if (!isMember)
        {
            throw new MembershipNotFoundException(userName, groupName);
        }

        return GroupEntity.newMinimalGroupEntity(groupName, null, baseUri);
    }

    /**
     * Adds a user to a group.
     *
     * @param applicationName name of the application
     * @param userName name of the user
     * @param groupName name of the group
     * @throws ApplicationPermissionException if none of the application's associated directories are allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP}.
     * @throws UserNotFoundException if the user could not be found
     * @throws GroupNotFoundException if the group could not be found
     * @throws OperationFailedException if the operation failed for any other reason
     */
    public void addUserToGroup(final String applicationName, final String userName, final String groupName)
            throws ApplicationPermissionException, UserNotFoundException, GroupNotFoundException, OperationFailedException
    {
        Application application = getApplication(applicationName);

        applicationService.addUserToGroup(application, userName, groupName);
    }

    /**
     * Removes a user from a group.
     *
     * @param applicationName name of the application
     * @param userName name of the user
     * @param groupName name of the group
     * @throws ApplicationPermissionException if none of the application's associated directories are allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP}.
     * @throws GroupNotFoundException if the group could not be found
     * @throws MembershipNotFoundException if the membership does not exist
     * @throws UserNotFoundException if the user could not be found
     * @throws OperationFailedException if the operation failed for any other reason
     */
    public void removeUserFromGroup(final String applicationName, final String userName, final String groupName)
            throws ApplicationPermissionException, MembershipNotFoundException, UserNotFoundException, GroupNotFoundException, OperationFailedException
    {
        Application application = getApplication(applicationName);

        applicationService.removeUserFromGroup(application, userName, groupName);
    }

    /**
     * Returns the groups that the user is a nested member of.
     *
     * @param applicationName name of the application
     * @param userName name of the user
     * @param expandGroups should groups be expanded
     * @param maxResults maximum number of results returned. If -1, then all the results are returned.
     * @param startIndex starting index of the results
     * @param baseUri base URI of the REST service
     * @return groups that the user is a nested member of
     */
    public GroupEntityList getNestedGroups(final String applicationName, final String userName, final boolean expandGroups, final int maxResults, final int startIndex, final URI baseUri)
    {
        Application application = getApplication(applicationName);

        if (expandGroups)
        {
            MembershipQuery<Group> query = QueryBuilder.createMembershipQuery(maxResults, startIndex, false, EntityDescriptor.group(), Group.class, EntityDescriptor.user(), userName);
            List<Group> groups = applicationService.searchNestedGroupRelationships(application, query);

            return EntityTranslator.toGroupEntities(groups, baseUri);
        }
        else
        {
            MembershipQuery<String> query = QueryBuilder.createMembershipQuery(maxResults, startIndex, false, EntityDescriptor.group(), String.class, EntityDescriptor.user(), userName);
            List<String> groupNames = applicationService.searchNestedGroupRelationships(application, query);

            return EntityTranslator.toMinimalGroupEntities(groupNames, baseUri);
        }
    }

    /**
     * Returns the specified group that the user is a nested member of.
     *
     * @param applicationName name of the application
     * @param userName name of the user
     * @param groupName name of the group the user is a member of
     * @param baseUri base URI of the REST service
     * @return group that the user is a nested member of
     * @throws MembershipNotFoundException if the membership was not found
     */
    public GroupEntity getNestedGroup(final String applicationName, final String userName, final String groupName, final URI baseUri)
            throws MembershipNotFoundException
    {
        Application application = getApplication(applicationName);

        final boolean isMember = applicationService.isUserNestedGroupMember(application, userName, groupName);

        if (!isMember)
        {
            throw new MembershipNotFoundException(userName, groupName);
        }

        return GroupEntity.newMinimalGroupEntity(groupName, null, baseUri);
    }
}
