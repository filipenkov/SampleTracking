package com.atlassian.crowd.integration.rest.service;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.event.EventTokenExpiredException;
import com.atlassian.crowd.event.Events;
import com.atlassian.crowd.event.IncrementalSynchronisationNotAvailableException;
import com.atlassian.crowd.exception.ApplicationAccessDeniedException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidEmailAddressException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidTokenException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UnsupportedCrowdApiException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.integration.rest.entity.AuthenticationContextEntity;
import com.atlassian.crowd.integration.rest.entity.CookieConfigEntity;
import com.atlassian.crowd.integration.rest.entity.ErrorEntity;
import com.atlassian.crowd.integration.rest.entity.EventEntityList;
import com.atlassian.crowd.integration.rest.entity.GroupEntity;
import com.atlassian.crowd.integration.rest.entity.GroupEntityList;
import com.atlassian.crowd.integration.rest.entity.MembershipsEntity;
import com.atlassian.crowd.integration.rest.entity.MultiValuedAttributeEntityList;
import com.atlassian.crowd.integration.rest.entity.PasswordEntity;
import com.atlassian.crowd.integration.rest.entity.SearchRestrictionEntity;
import com.atlassian.crowd.integration.rest.entity.SessionEntity;
import com.atlassian.crowd.integration.rest.entity.UserEntity;
import com.atlassian.crowd.integration.rest.entity.UserEntityList;
import com.atlassian.crowd.integration.rest.entity.ValidationFactorEntityList;
import com.atlassian.crowd.integration.rest.util.EntityTranslator;
import com.atlassian.crowd.integration.rest.util.SearchRestrictionEntityTranslator;
import com.atlassian.crowd.model.authentication.CookieConfiguration;
import com.atlassian.crowd.model.authentication.UserAuthenticationContext;
import com.atlassian.crowd.model.authentication.ValidationFactor;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.model.group.Membership;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.CrowdClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.Validate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the Crowd Client to access REST services on a remote Crowd Server.
 *
 */
public class RestCrowdClient implements CrowdClient
{
    private static final String USER_NULL_ERROR_MSG = "User must not be null";
    private static final String USERNAME_NULL_ERROR_MSG = "Username must not be null";

    private final RestExecutor executor;

    /**
     * Constructs a new REST Crowd Client instance.
     *
     * This client provides a simple interface for interacting with a remote Crowd server.
     * The client is thread safe.
     * @param clientProperties crowd properties for the client
     */
    public RestCrowdClient(ClientProperties clientProperties)
    {
        executor = new RestExecutor(clientProperties);
    }

    public User getUser(final String name)
            throws UserNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            return executor.get("/user?username=%s", name).andReceive(UserEntity.class);
        }
        catch (CrowdRestException e)
        {
            handleUserNotFound(e.getErrorEntity(), name);
            throw handleCommonExceptions(e);
        }
    }

    public UserWithAttributes getUserWithAttributes(final String name)
            throws UserNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            return executor.get("/user?username=%s&expand=attributes", name).andReceive(UserEntity.class);
        }
        catch (CrowdRestException e)
        {
            handleUserNotFound(e.getErrorEntity(), name);
            throw handleCommonExceptions(e);
        }
    }

    public User authenticateUser(final String username, final String password)
            throws UserNotFoundException, InactiveAccountException, ExpiredCredentialException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            return executor.post(new PasswordEntity(password), "/authentication?username=%s", username).andReceive(UserEntity.class);
        }
        catch (CrowdRestException e)
        {
            handleUserNotFound(e.getErrorEntity(), username);
            handleInactiveUserAccount(e.getErrorEntity(), username);
            handleExpiredUserCredential(e.getErrorEntity());
            handleInvalidUserAuthentication(e.getErrorEntity(), username);
            throw handleCommonExceptions(e);
        }
    }

    public void addUser(final User user, final PasswordCredential passwordCredential)
            throws ApplicationPermissionException, InvalidUserException, InvalidCredentialException, InvalidAuthenticationException, OperationFailedException
    {
        Validate.notNull(user, USER_NULL_ERROR_MSG);
        Validate.notNull(user.getName(), USERNAME_NULL_ERROR_MSG);
        final UserEntity userEntity = EntityTranslator.toUserEntity(user, passwordCredential);

        try
        {
            executor.post(userEntity, "/user").andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            handleInvalidUser(e.getErrorEntity(), user);
            handleInvalidCredential(e.getErrorEntity());
            throw handleCommonExceptions(e);
        }
    }

    public void updateUser(final User user)
            throws InvalidUserException, UserNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        Validate.notNull(user, USER_NULL_ERROR_MSG);
        Validate.notNull(user.getName(), USERNAME_NULL_ERROR_MSG);
        final UserEntity restUser = EntityTranslator.toUserEntity(user);

        try
        {
            executor.put(restUser, "/user?username=%s", user.getName()).andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            handleInvalidUser(e.getErrorEntity(), user);
            handleUserNotFound(e.getErrorEntity(), user.getName());
            throw handleCommonExceptions(e);
        }
    }

    public void updateUserCredential(final String username, final String password)
            throws InvalidCredentialException, UserNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            executor.put(new PasswordEntity(password), "/user/password?username=%s", username).andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            handleUserNotFound(e.getErrorEntity(), username);
            handleInvalidCredential(e.getErrorEntity());
            throw handleCommonExceptions(e);
        }
    }

    public void requestPasswordReset(final String username)
            throws UserNotFoundException, InvalidEmailAddressException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            executor.postEmpty("/user/mail/password?username=%s", username).andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            handleUserNotFound(e.getErrorEntity(), username);
            handleInvalidEmail(e.getErrorEntity());
            throw handleCommonExceptions(e);
        }
    }

    public void requestUsernames(final String email)
            throws InvalidEmailAddressException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            executor.postEmpty("/user/mail/usernames?email=%s", email).andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            handleInvalidEmail(e.getErrorEntity());
            throw handleCommonExceptions(e);
        }
    }

    public void storeUserAttributes(final String username, final Map<String, Set<String>> attributes)
            throws UserNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final MultiValuedAttributeEntityList restAttributes = EntityTranslator.toMultiValuedAttributeEntityList(attributes);

        try
        {
            executor.post(restAttributes, "/user/attribute?username=%s", username).andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            handleUserNotFound(e.getErrorEntity(), username);
            throw handleCommonExceptions(e);
        }
    }

    public void removeUserAttributes(final String username, final String attributeName)
            throws UserNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            executor.delete("/user/attribute?username=%s&attributename=%s", username, attributeName).andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            handleUserNotFound(e.getErrorEntity(), username);
            throw handleCommonExceptions(e);
        }
    }

    public void removeUser(final String username)
            throws UserNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            executor.delete("/user?username=%s", username).andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            handleUserNotFound(e.getErrorEntity(), username);
            throw handleCommonExceptions(e);
        }
    }

    /**
     * Get a group by name.
     * @param name name of the group to retrieve.
     * @return A Group
     * @throws GroupNotFoundException The group does not exist on the remote server.
     * @throws com.atlassian.crowd.exception.OperationFailedException For any other communication errors.
     */
    public Group getGroup(final String name)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            return executor.get("/group?groupname=%s", name).andReceive(GroupEntity.class);
        }
        catch (CrowdRestException e)
        {
            handleGroupNotFound(e.getErrorEntity(), name);
            throw handleCommonExceptions(e);
        }
    }

    public GroupWithAttributes getGroupWithAttributes(final String name)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            return executor.get("/group?groupname=%s&expand=attributes", name).andReceive(GroupEntity.class);
        }
        catch (CrowdRestException e)
        {
            handleGroupNotFound(e.getErrorEntity(), name);
            throw handleCommonExceptions(e);
        }
    }

    public void addGroup(final Group group)
            throws InvalidGroupException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final Group restGroup = EntityTranslator.toGroupEntity(group);

        try
        {
            executor.post(restGroup, "/group").andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            handleInvalidGroup(e.getErrorEntity(), group);
            throw handleCommonExceptions(e);
        }
    }

    public void updateGroup(final Group group)
            throws InvalidGroupException, GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        Validate.notNull(group);
        Validate.notNull(group.getName());
        final GroupEntity restGroup = EntityTranslator.toGroupEntity(group);

        try
        {
            executor.put(restGroup, "/group?groupname=%s", group.getName()).andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            handleInvalidGroup(e.getErrorEntity(), group);
            handleGroupNotFound(e.getErrorEntity(), group.getName());
            throw handleCommonExceptions(e);
        }
    }

    public void storeGroupAttributes(final String groupName, final Map<String, Set<String>> attributes)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final MultiValuedAttributeEntityList restAttributes = EntityTranslator.toMultiValuedAttributeEntityList(attributes);

        try
        {
            executor.post(restAttributes, "/group/attribute?groupname=%s", groupName).andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            handleGroupNotFound(e.getErrorEntity(), groupName);
            throw handleCommonExceptions(e);
        }
    }

    public void removeGroupAttributes(final String groupName, final String attributeName)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            executor.delete("/group/attribute?groupname=%s&attributename=%s", groupName, attributeName).andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            handleGroupNotFound(e.getErrorEntity(), groupName);
            throw handleCommonExceptions(e);
        }
    }

    public void removeGroup(final String groupName)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            executor.delete("/group?groupname=%s", groupName).andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            handleGroupNotFound(e.getErrorEntity(), groupName);
            throw handleCommonExceptions(e);
        }
    }

    public boolean isUserDirectGroupMember(final String username, final String groupName)
            throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            return executor.get("/group/user/direct?groupname=%s&username=%s", groupName, username).doesExist();
        }
        catch (CrowdRestException e)
        {
            throw handleCommonExceptions(e);
        }
    }

    public boolean isUserNestedGroupMember(final String username, final String groupName)
            throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            return executor.get("/group/user/nested?groupname=%s&username=%s", groupName, username).doesExist();
        }
        catch (CrowdRestException e)
        {
            throw handleCommonExceptions(e);
        }
    }

    public boolean isGroupDirectGroupMember(final String childName, final String parentName)
            throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            return executor.get("/group/child-group/direct?groupname=%s&child-groupname=%s", parentName, childName).doesExist();
        }
        catch (CrowdRestException e)
        {
            throw handleCommonExceptions(e);
        }
    }

    public void addUserToGroup(final String username, final String groupName)
            throws GroupNotFoundException, UserNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final UserEntity user = UserEntity.newMinimalInstance(username);

        try
        {
            executor.post(user, "/group/user/direct?groupname=%s", groupName).andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            handleUserNotFound(e.getErrorEntity(), username);
            handleGroupNotFound(e.getErrorEntity(), groupName);
            throw handleCommonExceptions(e);
        }
    }

    public void addGroupToGroup(final String childGroup, final String parentGroup)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final GroupEntity group = GroupEntity.newMinimalInstance(childGroup);

        try
        {
            executor.post(group, "/group/child-group/direct?groupname=%s", parentGroup).andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            switch (e.getStatusCode())
            {
                case HttpStatus.SC_NOT_FOUND:
                    handleGroupNotFound(e.getErrorEntity(), parentGroup);
                    break;
                case HttpStatus.SC_BAD_REQUEST:
                    handleGroupNotFound(e.getErrorEntity(), childGroup);
                    break;
            }
            throw handleCommonExceptions(e);
        }
    }

    public void removeUserFromGroup(final String username, final String groupName)
            throws MembershipNotFoundException, GroupNotFoundException, UserNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            executor.delete("/group/user/direct?groupname=%s&username=%s", groupName, username).andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            handleMembershipNotFound(e.getErrorEntity(), username, groupName);
            handleGroupNotFound(e.getErrorEntity(), groupName);
            handleUserNotFound(e.getErrorEntity(), username);
            throw handleCommonExceptions(e);
        }
    }

    public void removeGroupFromGroup(final String childGroup, final String parentGroup)
            throws MembershipNotFoundException, GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            executor.delete("/group/child-group/direct?groupname=%s&child-groupname=%s", parentGroup, childGroup).andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            handleMembershipNotFound(e.getErrorEntity(), childGroup, parentGroup);
            getGroup(childGroup);
            getGroup(parentGroup);
            throw handleCommonExceptions(e);
        }
    }

    public void testConnection()
            throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        // This test does a get Users query returning at most 1 user.
        // The test may return no users, but should never fail with an exception if the directory is correctly configured.
        searchUsers(NullRestrictionImpl.INSTANCE, 0, 1);
    }

    public List<User> searchUsers(final SearchRestriction searchRestriction, final int startIndex, final int maxResults)
            throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final SearchRestrictionEntity searchRestrictionEntity = SearchRestrictionEntityTranslator.toSearchRestrictionEntity(searchRestriction);

        final UserEntityList userEntityList;
        try
        {
            userEntityList = executor.post(searchRestrictionEntity, "/search?entity-type=user&start-index=%d&max-results=%d&expand=user", startIndex, maxResults).andReceive(UserEntityList.class);
        }
        catch (CrowdRestException e)
        {
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toUserList(userEntityList);
    }

    public List<String> searchUserNames(final SearchRestriction searchRestriction, final int startIndex, final int maxResults)
            throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final SearchRestrictionEntity searchRestrictionEntity = SearchRestrictionEntityTranslator.toSearchRestrictionEntity(searchRestriction);

        final UserEntityList userEntityList;
        try
        {
            userEntityList = executor.post(searchRestrictionEntity, "/search?entity-type=user&start-index=%d&max-results=%d", startIndex, maxResults).andReceive(UserEntityList.class);
        }
        catch (CrowdRestException e)
        {
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toNameList(userEntityList);
    }

    public List<Group> searchGroups(final SearchRestriction searchRestriction, final int startIndex, final int maxResults)
            throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final SearchRestrictionEntity searchRestrictionEntity = SearchRestrictionEntityTranslator.toSearchRestrictionEntity(searchRestriction);

        final GroupEntityList groupEntityList;
        try
        {
            groupEntityList = executor.post(searchRestrictionEntity, "/search?entity-type=group&start-index=%d&max-results=%d&expand=group", startIndex, maxResults).andReceive(GroupEntityList.class);
        }
        catch (CrowdRestException e)
        {
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toGroupList(groupEntityList);
    }

    public List<String> searchGroupNames(final SearchRestriction searchRestriction, final int startIndex, final int maxResults)
            throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final SearchRestrictionEntity searchRestrictionEntity = SearchRestrictionEntityTranslator.toSearchRestrictionEntity(searchRestriction);

        final GroupEntityList groupEntityList;
        try
        {
            groupEntityList = executor.post(searchRestrictionEntity, "/search?entity-type=group&start-index=%d&max-results=%d", startIndex, maxResults).andReceive(GroupEntityList.class);
        }
        catch (CrowdRestException e)
        {
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toNameList(groupEntityList);
    }

    public List<User> getUsersOfGroup(final String groupName, final int startIndex, final int maxResults)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final UserEntityList restUsers;
        try
        {
            restUsers = executor.get("/group/user/direct?groupname=%s&start-index=%d&max-results=%d&expand=user", groupName, startIndex, maxResults).andReceive(UserEntityList.class);
        }
        catch (CrowdRestException e)
        {
            handleGroupNotFound(e.getErrorEntity(), groupName);
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toUserList(restUsers);
    }

    public List<String> getNamesOfUsersOfGroup(final String groupName, final int startIndex, final int maxResults)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final UserEntityList restUsers;
        try
        {
            restUsers = executor.get("/group/user/direct?groupname=%s&start-index=%d&max-results=%d", groupName, startIndex, maxResults).andReceive(UserEntityList.class);
        }
        catch (CrowdRestException e)
        {
            handleGroupNotFound(e.getErrorEntity(), groupName);
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toNameList(restUsers);
    }

    public List<Group> getChildGroupsOfGroup(final String groupName, final int startIndex, final int maxResults)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final GroupEntityList restGroups;
        try
        {
            restGroups = executor.get("/group/child-group/direct?groupname=%s&start-index=%d&max-results=%d&expand=group", groupName, startIndex, maxResults).andReceive(GroupEntityList.class);
        }
        catch (CrowdRestException e)
        {
            handleGroupNotFound(e.getErrorEntity(), groupName);
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toGroupList(restGroups);
    }

    public List<String> getNamesOfChildGroupsOfGroup(final String groupName, final int startIndex, final int maxResults)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final GroupEntityList restGroups;
        try
        {
            restGroups = executor.get("/group/child-group/direct?groupname=%s&start-index=%d&max-results=%d", groupName, startIndex, maxResults).andReceive(GroupEntityList.class);
        }
        catch (CrowdRestException e)
        {
            handleGroupNotFound(e.getErrorEntity(), groupName);
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toNameList(restGroups);
    }

    public List<Group> getGroupsForUser(final String userName, final int startIndex, final int maxResults)
            throws UserNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final GroupEntityList restGroups;
        try
        {
            restGroups = executor.get("/user/group/direct?username=%s&start-index=%d&max-results=%d&expand=group", userName, startIndex, maxResults).andReceive(GroupEntityList.class);
        }
        catch (CrowdRestException e)
        {
            handleUserNotFound(e.getErrorEntity(), userName);
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toGroupList(restGroups);
    }

    public List<String> getNamesOfGroupsForUser(final String userName, final int startIndex, final int maxResults)
            throws UserNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final GroupEntityList restGroups;
        try
        {
            restGroups = executor.get("/user/group/direct?username=%s&start-index=%d&max-results=%d", userName, startIndex, maxResults).andReceive(GroupEntityList.class);
        }
        catch (CrowdRestException e)
        {
            handleUserNotFound(e.getErrorEntity(), userName);
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toNameList(restGroups);
    }

    public List<Group> getParentGroupsForGroup(final String groupName, final int startIndex, final int maxResults)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final GroupEntityList restGroups;
        try
        {
            restGroups = executor.get("/group/parent-group/direct?groupname=%s&start-index=%d&max-results=%d&expand=group", groupName, startIndex, maxResults).andReceive(GroupEntityList.class);
        }
        catch (CrowdRestException e)
        {
            handleGroupNotFound(e.getErrorEntity(), groupName);
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toGroupList(restGroups);
    }

    public List<String> getNamesOfParentGroupsForGroup(final String groupName, final int startIndex, final int maxResults)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final GroupEntityList restGroups;
        try
        {
            restGroups = executor.get("/group/parent-group/direct?groupname=%s&start-index=%d&max-results=%d", groupName, startIndex, maxResults).andReceive(GroupEntityList.class);
        }
        catch (CrowdRestException e)
        {
            handleGroupNotFound(e.getErrorEntity(), groupName);
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toNameList(restGroups);
    }

    public List<User> getNestedUsersOfGroup(final String groupName, final int startIndex, final int maxResults)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final UserEntityList restUsers;
        try
        {
            restUsers = executor.get("/group/user/nested?groupname=%s&start-index=%d&max-results=%d&expand=user", groupName, startIndex, maxResults).andReceive(UserEntityList.class);
        }
        catch (CrowdRestException e)
        {
            handleGroupNotFound(e.getErrorEntity(), groupName);
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toUserList(restUsers);
    }

    public List<String> getNamesOfNestedUsersOfGroup(final String groupName, final int startIndex, final int maxResults)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final UserEntityList restUsers;
        try
        {
            restUsers = executor.get("/group/user/nested?groupname=%s&start-index=%d&max-results=%d", groupName, startIndex, maxResults).andReceive(UserEntityList.class);
        }
        catch (CrowdRestException e)
        {
            handleGroupNotFound(e.getErrorEntity(), groupName);
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toNameList(restUsers);
    }

    public List<Group> getNestedChildGroupsOfGroup(final String groupName, final int startIndex, final int maxResults)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final GroupEntityList restGroups;
        try
        {
            restGroups = executor.get("/group/child-group/nested?groupname=%s&start-index=%d&max-results=%d&expand=group", groupName, startIndex, maxResults).andReceive(GroupEntityList.class);
        }
        catch (CrowdRestException e)
        {
            handleGroupNotFound(e.getErrorEntity(), groupName);
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toGroupList(restGroups);
    }

    public List<String> getNamesOfNestedChildGroupsOfGroup(final String groupName, final int startIndex, final int maxResults)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final GroupEntityList restGroups;
        try
        {
            restGroups = executor.get("/group/child-group/nested?groupname=%s&start-index=%d&max-results=%d", groupName, startIndex, maxResults).andReceive(GroupEntityList.class);
        }
        catch (CrowdRestException e)
        {
            handleGroupNotFound(e.getErrorEntity(), groupName);
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toNameList(restGroups);
    }

    public List<Group> getGroupsForNestedUser(final String userName, final int startIndex, final int maxResults)
            throws UserNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final GroupEntityList restGroups;
        try
        {
            restGroups = executor.get("/user/group/nested?username=%s&start-index=%d&max-results=%d&expand=group", userName, startIndex, maxResults).andReceive(GroupEntityList.class);
        }
        catch (CrowdRestException e)
        {
            handleUserNotFound(e.getErrorEntity(), userName);
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toGroupList(restGroups);
    }

    public List<String> getNamesOfGroupsForNestedUser(final String userName, final int startIndex, final int maxResults)
            throws UserNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final GroupEntityList groupEntityList;
        try
        {
            groupEntityList = executor.get("/user/group/nested?username=%s&start-index=%d&max-results=%d", userName, startIndex, maxResults).andReceive(GroupEntityList.class);
        }
        catch (CrowdRestException e)
        {
            handleUserNotFound(e.getErrorEntity(), userName);
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toNameList(groupEntityList);
    }

    public List<Group> getParentGroupsForNestedGroup(final String groupName, final int startIndex, final int maxResults)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final GroupEntityList restGroups;
        try
        {
            restGroups = executor.get("/group/parent-group/nested?groupname=%s&start-index=%d&max-results=%d&expand=group", groupName, startIndex, maxResults).andReceive(GroupEntityList.class);
        }
        catch (CrowdRestException e)
        {
            handleGroupNotFound(e.getErrorEntity(), groupName);
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toGroupList(restGroups);
    }

    public List<String> getNamesOfParentGroupsForNestedGroup(final String groupName, final int startIndex, final int maxResults)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final GroupEntityList restGroups;
        try
        {
            restGroups = executor.get("/group/parent-group/nested?groupname=%s&start-index=%d&max-results=%d", groupName, startIndex, maxResults).andReceive(GroupEntityList.class);
        }
        catch (CrowdRestException e)
        {
            handleGroupNotFound(e.getErrorEntity(), groupName);
            throw handleCommonExceptions(e);
        }
        return EntityTranslator.toNameList(restGroups);
    }

    public Iterable<Membership> getMemberships()
        throws OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException, UnsupportedCrowdApiException
    {
        MembershipsEntity memberships;
        
        try
        {
            memberships = executor.get("/group/membership").andReceive(MembershipsEntity.class);
        }
        catch (CrowdRestException e)
        {
            // The REST call failed, but we're talking to Crowd because otherwise ICSE would have been thrown
            if (e.getStatusCode() ==  HttpStatus.SC_NOT_FOUND)
            {
                throw new UnsupportedCrowdApiException("1.1", "to retrieve membership data with a single request");
            }
            else
            {
                throw handleCommonExceptions(e);
            }
        }
        
        return Collections.unmodifiableList(memberships.getList());
    }
    
    public User findUserFromSSOToken(final String token)
            throws InvalidTokenException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            return executor.get("/session/%s?expand=user", token).andReceive(SessionEntity.class).getUser();
        }
        catch (CrowdRestException e)
        {
            handleInvalidSsoToken(e.getErrorEntity());
            throw handleCommonExceptions(e);
        }
    }

    public String authenticateSSOUser(final UserAuthenticationContext userAuthenticationContext)
            throws ApplicationAccessDeniedException, ExpiredCredentialException, InactiveAccountException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        Validate.notNull(userAuthenticationContext);
        Validate.notNull(userAuthenticationContext.getName());
        final AuthenticationContextEntity authContextEntity = AuthenticationContextEntity.newInstance(userAuthenticationContext);
        try
        {
            return executor.post(authContextEntity, "/session?validate-password=true").andReceive(SessionEntity.class).getToken();
        }
        catch (CrowdRestException e)
        {
            final ErrorEntity errorEntity = e.getErrorEntity();
            handleInvalidUserAuthentication(errorEntity, userAuthenticationContext.getName());
            handleInactiveUserAccount(errorEntity, userAuthenticationContext.getName());
            handleExpiredUserCredential(errorEntity);
            handleApplicationAccessDenied(errorEntity);
            throw handleCommonExceptions(e);
        }
    }

    public String authenticateSSOUserWithoutValidatingPassword(final UserAuthenticationContext userAuthenticationContext)
            throws ApplicationAccessDeniedException, InactiveAccountException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        final AuthenticationContextEntity authContextEntity = AuthenticationContextEntity.newInstance(userAuthenticationContext);
        try
        {
            return executor.post(authContextEntity, "/session?validate-password=false").andReceive(SessionEntity.class).getToken();
        }
        catch (CrowdRestException e)
        {
            final ErrorEntity errorEntity = e.getErrorEntity();

            // if user does not exist, an InvalidAuthenticationException should be thrown
            handleInvalidUserAuthentication(errorEntity, userAuthenticationContext.getName());

            handleInactiveUserAccount(errorEntity, userAuthenticationContext.getName());
            handleApplicationAccessDenied(errorEntity);
            throw handleCommonExceptions(e);
        }
    }

    public void validateSSOAuthentication(final String token, final List<ValidationFactor> validationFactors)
            throws InvalidTokenException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        ValidationFactorEntityList validationFactorEntityList = ValidationFactorEntityList.newInstance(validationFactors);
        try
        {
            executor.post(validationFactorEntityList, "/session/%s", token).andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            handleInvalidSsoToken(e.getErrorEntity());
            throw handleCommonExceptions(e);
        }
    }

    public void invalidateSSOToken(final String token)
            throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            executor.delete("/session/%s", token).andCheckResponse();
        }
        catch (CrowdRestException e)
        {
            throw handleCommonExceptions(e);
        }
    }

    public CookieConfiguration getCookieConfiguration()
            throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        CookieConfigEntity cookieConfig;
        try
        {
            cookieConfig = executor.get("/config/cookie").andReceive(CookieConfigEntity.class);
        }
        catch (CrowdRestException e)
        {
            throw handleCommonExceptions(e);
        }
        return new CookieConfiguration(cookieConfig.getDomain(), cookieConfig.isSecure(), cookieConfig.getName());
    }

    public void shutdown()
    {
        executor.shutDown();
    }

    public String getCurrentEventToken()
            throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            return executor.get("/event").andReceive(EventEntityList.class).getNewEventToken();
        }
        catch (CrowdRestException e)
        {
            // The REST call failed, but we're talking to Crowd because otherwise ICSE would have been thrown
            if (e.getStatusCode() ==  HttpStatus.SC_NOT_FOUND)
            {
                throw new UnsupportedCrowdApiException("1.2", "for event-based synchronisation");
            }
            else
            {
                throw handleCommonExceptions(e);
            }
        }
    }

    public Events getNewEvents(String eventToken)
            throws EventTokenExpiredException, IncrementalSynchronisationNotAvailableException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException
    {
        try
        {
            return EntityTranslator.toEvents(executor.get("/event/%s", eventToken).andReceive(EventEntityList.class));
        }
        catch (CrowdRestException e)
        {
            // The REST call failed, but we're talking to Crowd because otherwise ICSE would have been thrown
            if (e.getStatusCode() ==  HttpStatus.SC_NOT_FOUND)
            {
                throw new UnsupportedCrowdApiException("1.2", "for event-based synchronisation");
            }
            handleIncrementalSynchronisationNotAvailableException(e.getErrorEntity());
            handleEventTokenExpiredException(e.getErrorEntity());
            throw handleCommonExceptions(e);
        }
    }

    /**
     * Throws an UserNotFoundException if the method handled the ErrorEntity. Otherwise, the method silently exits.
     *
     * @param errorEntity the ErrorEntity to handle
     * @param userName name of the user that could not be found
     * @throws UserNotFoundException if the method handled the <tt>ErrorEntity</tt>
     */
    private static void handleUserNotFound(final ErrorEntity errorEntity, final String userName)
            throws UserNotFoundException
    {
        if (errorEntity.getReason() == ErrorEntity.ErrorReason.USER_NOT_FOUND)
        {
            throw new UserNotFoundException(userName);
        }
    }

    /**
     * Throws an InvalidAuthenticationException if the method handled the ErrorEntity. Otherwise, the method silently exits.
     *
     * @param errorEntity the ErrorEntity to handle
     * @param userName name of the user that failed authentication
     * @throws InvalidAuthenticationException if the method handled the <tt>ErrorEntity</tt>
     */
    private static void handleInvalidUserAuthentication(final ErrorEntity errorEntity, final String userName)
            throws InvalidAuthenticationException
    {
        if (errorEntity.getReason() == ErrorEntity.ErrorReason.INVALID_USER_AUTHENTICATION)
        {
            throw InvalidAuthenticationException.newInstanceWithName(userName);
        }
    }

    /**
     * Throws an GroupNotFoundException if the method handled the ErrorEntity. Otherwise, the method silently exits.
     *
     * @param errorEntity the ErrorEntity to handle
     * @param groupName name of the group that could not be found
     * @throws GroupNotFoundException if the method handled the <tt>ErrorEntity</tt>
     */
    private static void handleGroupNotFound(final ErrorEntity errorEntity, final String groupName)
            throws GroupNotFoundException
    {
        if (errorEntity.getReason() == ErrorEntity.ErrorReason.GROUP_NOT_FOUND)
        {
            throw new GroupNotFoundException(groupName);
        }
    }

    /**
     * Throws an InvalidUserException if the method handled the ErrorEntity. Otherwise, the method silently exits.
     *
     * @param errorEntity the ErrorEntity to handle
     * @param user the invalid user
     * @throws InvalidUserException if the method handled the <tt>ErrorEntity</tt>
     */
    private static void handleInvalidUser(final ErrorEntity errorEntity, final User user)
            throws InvalidUserException
    {
        if (errorEntity.getReason() == ErrorEntity.ErrorReason.INVALID_USER)
        {
            throw new InvalidUserException(user, errorEntity.getMessage());
        }
    }

    /**
     * Throws an InvalidCredentialException if the method handled the ErrorEntity. Otherwise, the method silently exits.
     *
     * @param errorEntity the ErrorEntity to handle
     * @throws InvalidCredentialException if the method handled the <tt>ErrorEntity</tt>
     */
    private static void handleInvalidCredential(final ErrorEntity errorEntity)
            throws InvalidCredentialException
    {
        if (errorEntity.getReason() == ErrorEntity.ErrorReason.INVALID_CREDENTIAL)
        {
            throw new InvalidCredentialException(errorEntity.getMessage());
        }
    }

    /**
     * Throws an InvalidGroupException if the method handled the ErrorEntity. Otherwise, the method silently exits.
     *
     * @param errorEntity the ErrorEntity to handle
     * @param group the invalid group
     * @throws InvalidGroupException if the method handled the <tt>ErrorEntity</tt>
     */
    private static void handleInvalidGroup(final ErrorEntity errorEntity, final Group group)
            throws InvalidGroupException
    {
        if (errorEntity.getReason() == ErrorEntity.ErrorReason.INVALID_GROUP)
        {
            throw new InvalidGroupException(group, errorEntity.getMessage());
        }
    }

    /**
     * Throws an MembershipNotFoundException if the method handled the ErrorEntity. Otherwise, the method silently exits.
     *
     * @param errorEntity the ErrorEntity to handle
     * @param childName child name
     * @param parentName parent name
     * @throws MembershipNotFoundException if the method handled the <tt>ErrorEntity</tt>
     */
    private static void handleMembershipNotFound(final ErrorEntity errorEntity, final String childName, final String parentName)
            throws MembershipNotFoundException
    {
        if (errorEntity.getReason() == ErrorEntity.ErrorReason.MEMBERSHIP_NOT_FOUND)
        {
            throw new MembershipNotFoundException(childName, parentName);
        }
    }

    /**
     * Throws an InvalidTokenException if the method handled the ErrorEntity. Otherwise, the method silently exits.
     *
     * @param errorEntity the ErrorEntity to handle
     * @throws InvalidTokenException if the method handled the <tt>ErrorEntity</tt>
     */
    private static void handleInvalidSsoToken(final ErrorEntity errorEntity)
            throws InvalidTokenException
    {
        if (errorEntity.getReason() == ErrorEntity.ErrorReason.INVALID_SSO_TOKEN)
        {
            throw new InvalidTokenException(errorEntity.getMessage());
        }
    }

    /**
     * Throws an InactiveAccountException if the method handled the ErrorEntity. Otherwise, the method silently exits.
     *
     * @param errorEntity the ErrorEntity to handle
     * @param userName name of the inactive user
     * @throws InactiveAccountException if the method handled the <tt>ErrorEntity</tt>
     */
    private static void handleInactiveUserAccount(final ErrorEntity errorEntity, final String userName)
            throws InactiveAccountException
    {
        if (errorEntity.getReason() == ErrorEntity.ErrorReason.INACTIVE_ACCOUNT)
        {
            throw new InactiveAccountException(userName);
        }
    }

    /**
     * Throws an ExpiredCredentialException if the method handled the ErrorEntity. Otherwise, the method silently exits.
     *
     * @param errorEntity the ErrorEntity to handle
     * @throws ExpiredCredentialException if the method handled the <tt>ErrorEntity</tt>
     */
    private static void handleExpiredUserCredential(final ErrorEntity errorEntity)
            throws ExpiredCredentialException
    {
        if (errorEntity.getReason() == ErrorEntity.ErrorReason.EXPIRED_CREDENTIAL)
        {
            throw new ExpiredCredentialException(errorEntity.getMessage());
        }
    }

    /**
     * Throws an InvalidEmailAddressException if the method handled the ErrorEntity. Otherwise, the method silently exits.
     *
     * @param errorEntity the ErrorEntity to handle
     * @throws InvalidEmailAddressException if the method handled the <tt>ErrorEntity</tt>
     */
    private static void handleInvalidEmail(final ErrorEntity errorEntity)
            throws InvalidEmailAddressException
    {
        if (errorEntity.getReason() == ErrorEntity.ErrorReason.INVALID_EMAIL)
        {
            throw new InvalidEmailAddressException(errorEntity.getMessage());
        }
    }

    /**
     * Throws an ApplicationAccessDeniedException if the method handled the ErrorEntity. Otherwise, the method silently exits.
     *
     * @param errorEntity the ErrorEntity to handle
     * @throws ApplicationAccessDeniedException if the method handled the <tt>ErrorEntity</tt>
     */
    private static void handleApplicationAccessDenied(final ErrorEntity errorEntity)
            throws ApplicationAccessDeniedException
    {
        if (errorEntity.getReason() == ErrorEntity.ErrorReason.APPLICATION_ACCESS_DENIED)
        {
            throw new ApplicationAccessDeniedException(errorEntity.getMessage());
        }
    }

    /**
     * Throws an EventTokenExpiredException if the method handled the ErrorEntity. Otherwise, the method silently exits.
     *
     * @param errorEntity the ErrorEntity to handle
     * @throws EventTokenExpiredException if the method handled the <tt>ErrorEntity</tt>
     */
    private static void handleEventTokenExpiredException(final ErrorEntity errorEntity)
            throws EventTokenExpiredException
    {
        if (errorEntity.getReason() == ErrorEntity.ErrorReason.EVENT_TOKEN_EXPIRED)
        {
            throw new EventTokenExpiredException();
        }
    }

    /**
     * Throws an IncrementalSynchronisationNotAvailableException if the method handled the ErrorEntity. Otherwise, the method silently exits.
     *
     * @param errorEntity the ErrorEntity to handle
     * @throws IncrementalSynchronisationNotAvailableException if the method handled the <tt>ErrorEntity</tt>
     */
    private static void handleIncrementalSynchronisationNotAvailableException(final ErrorEntity errorEntity)
            throws IncrementalSynchronisationNotAvailableException
    {
        if (errorEntity.getReason() == ErrorEntity.ErrorReason.INCREMENTAL_SYNC_NOT_AVAILABLE)
        {
            throw new IncrementalSynchronisationNotAvailableException(errorEntity.getMessage());
        }
    }

    /**
     * Handles the exceptions common across all REST methods. This method should be used after all the other CrowdRestException
     * handlers. The current implementation is to throw either an ApplicationPermissionException or an OperationFailedException.
     *
     * Declared to return an OperationFailedException so clients of this method can do
     * <code>throw handleCommonExceptions(..)</code> to avoid Java complaining that a non-void method does return any
     * value.
     *
     * @param e CrowdRestException
     * @return Never actually returns an OperationFailedException.
     * @throws ApplicationPermissionException if the application is not permitted to perform the operation
     * @throws OperationFailedException if the exception has caused the operation to fail.
     */
    private static OperationFailedException handleCommonExceptions(final CrowdRestException e) throws ApplicationPermissionException, OperationFailedException
    {
        if (e.getErrorEntity().getReason() == ErrorEntity.ErrorReason.APPLICATION_PERMISSION_DENIED)
        {
            throw new ApplicationPermissionException(e.getErrorEntity().getMessage());
        }
        throw new OperationFailedException(e.getMessage());
    }
}
