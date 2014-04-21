package com.atlassian.crowd.service.client;

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
import com.atlassian.crowd.model.authentication.CookieConfiguration;
import com.atlassian.crowd.model.authentication.UserAuthenticationContext;
import com.atlassian.crowd.model.authentication.ValidationFactor;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.model.group.Membership;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserWithAttributes;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Atlassian Crowd client interface.
 */
public interface CrowdClient
{
    /**
     * Gets a User by user name.
     * @param name Name of the user to retrieve
     * @return A User.
     * @throws UserNotFoundException          if the user is not found
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    User getUser(String name)
            throws UserNotFoundException, OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException;

    /**
     * Gets a User with Attributes by user name.
     * @param name Name of the user to retrieve
     * @return A User.
     * @throws UserNotFoundException          if the user is not found
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    UserWithAttributes getUserWithAttributes(String name)
            throws UserNotFoundException, OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException;

    /**
     * Authenticates a user with the server.
     *
     * @param username Name of the user to authenticate.
     * @param password Password of the user to authenticate.
     * @return user if the user is correctly authenticated
     * @throws UserNotFoundException          if the user could not be found
     * @throws InactiveAccountException       if the user account is not active
     * @throws ExpiredCredentialException     if the user credentials have expired
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    User authenticateUser(String username, String password)
            throws UserNotFoundException, InactiveAccountException, ExpiredCredentialException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException;

    /**
     * Adds a new User to the remote Crowd server.
     * @param user  The user to add
     * @param passwordCredential user password
     * @throws InvalidUserException           if the user is invalid.  This may be because a user of the same name
     *                                        already exists, or does not pass the server side validation rules.
     * @throws InvalidCredentialException     if the password is invalid.  It must conform to the rules set on the server
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    void addUser(User user, PasswordCredential passwordCredential)
            throws InvalidUserException, InvalidCredentialException, OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException;

    /**
     * Updates a user on the remote Crowd server.
     * @param user The user to update
     * @throws InvalidUserException the details of the user to be updated are invalid. This may be because the user details
     *         do not pass the server side validation rules.
     * @throws UserNotFoundException          if the user does not exist on the remote Crowd server
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    void updateUser(User user)
            throws InvalidUserException, UserNotFoundException, OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException;

    /**
     * Updates the user's password on the remote Crowd server.
     * @param username Name of the user to update.
     * @param password New password.
     * @throws UserNotFoundException          if the user does not exist on the remote Crowd server
     * @throws InvalidCredentialException     if the password is invalid.  It must conform to the rules set on the server
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    void updateUserCredential(String username, String password)
            throws UserNotFoundException, InvalidCredentialException, OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException;

    /**
     * Stores the user's attributes on the remote Crowd server.
     * @param username Name of the user.
     * @param attributes Set of Attributes to store.  Attributes will be added or if an attribute with the
     *        same key exists will be replaced.
     * @throws UserNotFoundException          if the user does not exist on the remote Crowd server
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    void storeUserAttributes(String username, Map<String, Set<String>> attributes)
            throws UserNotFoundException, OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException;

    /**
     * Removes a user attribute from the server.
     * If the attribute to be removed does not exist, no error is reported.
     * @param username Name of the user
     * @param attributeName Attribute key.
     * @throws UserNotFoundException          if the user does not exist on the remote Crowd server
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    void removeUserAttributes(String username, String attributeName)
            throws UserNotFoundException, OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException;

    /**
     * Removes a user from the remote Crowd server
     * @param username Name of the user to remove.
     * @throws UserNotFoundException          if the user does not exist on the remote Crowd server
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    void removeUser(String username)
            throws UserNotFoundException, OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException;


    /**
     * Requests a password reset.
     *
     * @param username name of the user
     * @throws UserNotFoundException          if the user does not exist
     * @throws InvalidEmailAddressException   if the user does not have a valid email to send the reset password link to
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    public void requestPasswordReset(final String username) throws UserNotFoundException, InvalidEmailAddressException, OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException;


    /**
     * Sends the usernames associated with the given email address. No email will be sent if there are no usernames
     * associated with a given <code>email</code>.
     *
     * @param email email address of the user
     * @throws InvalidEmailAddressException   if the <code>email</code> is not valid
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    public void requestUsernames(final String email) throws InvalidEmailAddressException, OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException;

    /**
     * Gets a group by name.
     * @param name name of the group to retrieve.
     * @return A Group
     * @throws GroupNotFoundException         if the group does not exist on the remote server
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    Group getGroup(String name)
            throws GroupNotFoundException, OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException;

    /**
     * Gets a group with attributes by name.
     * @param name name of the group to retrieve.
     * @return A Group with attributes.
     * @throws GroupNotFoundException         if the group does not exist on the remote server
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    GroupWithAttributes getGroupWithAttributes(String name)
            throws GroupNotFoundException, OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException;

    /**
     * Adds a group to the remote Crowd server.
     * @param group Group to add.
     * @throws InvalidGroupException          if the group is invalid.  This may be because a group of the same name already exists, or
     *                                        does not pass the server side validation rules
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    void addGroup(Group group)
            throws InvalidGroupException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException;

    /**
     * Updates a group on the remote Crowd server.
     * @param group Group to update.
     * @throws InvalidGroupException The group is invalid.  This may be because the group
     *         does not pass the server side validation rules.
     * @throws GroupNotFoundException         if the group does not exist on the remote server
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    void updateGroup(Group group)
            throws InvalidGroupException, GroupNotFoundException, OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException;

    /**
     * Stores the group's attributes on the remote Crowd server.
     * @param groupName Name of the group.
     * @param attributes Set of Attributes to store.  Attributes will be added or if an attribute with the
     *        same key exists will be replaced.
     * @throws GroupNotFoundException         if the group does not exist on the remote Crowd server
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    void storeGroupAttributes(String groupName, Map<String, Set<String>> attributes)
            throws GroupNotFoundException, OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException;

    /**
      * Removes a group attribute (set) from the server.
      * If the attribute to be removed does not exist, no error is reported.
      * @param groupName Name of the group
      * @param attributeName Attribute key.
      * @throws GroupNotFoundException the group does not exist on the remote Crowd server
      * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
      * @throws InvalidAuthenticationException if the application and password are not valid
      * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
      */
    void removeGroupAttributes(String groupName, String attributeName)
            throws GroupNotFoundException, OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException;

    /**
     * Removes a group from the remote Crowd server
     * @param groupName Name of the group to remove.
     * @throws GroupNotFoundException the group does not exist on the remote Crowd server
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    void removeGroup(String groupName)
            throws GroupNotFoundException, OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException;

    /**
     * Tests if a user is a direct member of a group.
     * @param username User name
     * @param groupName Group Name
     * @return true if the member is a direct member of the group.
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    boolean isUserDirectGroupMember(String username, String groupName)
            throws OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException;

    /**
     * Tests if a user is a nested member of a group.
     * @param username User name
     * @param groupName Group Name
     * @return true if the member is a nested member of the group.
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    boolean isUserNestedGroupMember(String username, String groupName)
            throws OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException;

    /**
     * Tests if a group is a direct member of a group.
     * @param childName Name of the child group
     * @param parentName Name of the Parent group
     * @return true if the child group is a direct member of the parent group.
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    boolean isGroupDirectGroupMember(String childName, String parentName)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException;

    /**
     * Adds a user to a group.
     * @param username Name of the user to add to the group.
     * @param groupName Name of the group to be added to.
     * @throws GroupNotFoundException if the group does not exist.
     * @throws UserNotFoundException  if the user does not exist.
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    void addUserToGroup(String username, String groupName)
            throws GroupNotFoundException, UserNotFoundException, OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException;

    /**
     * Adds a group to a group.
     * @param childGroup Name of the group to add to the parent group.
     * @param parentGroup Name of the group the child will be added to.
     * @throws GroupNotFoundException         if either group does not exist
     * @throws UserNotFoundException          if the user does not exist
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    void addGroupToGroup(String childGroup, String parentGroup)
            throws GroupNotFoundException, UserNotFoundException, OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException;

    /**
     * Removes a user from a group.
     * @param username Name of the user to add to the group.
     * @param groupName Name of the group to be added to.
     * @throws MembershipNotFoundException if the membership does not exist
     * @throws GroupNotFoundException if the group does not exist.
     * @throws UserNotFoundException  if the user does not exist.
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    void removeUserFromGroup(String username, String groupName)
                                            throws MembershipNotFoundException, GroupNotFoundException, UserNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException;

    /**
     * Removes a group to a group.
     * @param childGroup Name of the group to be removed from the parent group.
     * @param parentGroup Name of the group the child group will be removed from.
     * @throws GroupNotFoundException if either group does not exist.
     * @throws MembershipNotFoundException if there is not parent-child relationship between the specified groups
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if for some reason the operation has failed
     */
    void removeGroupFromGroup(String childGroup, String parentGroup)
            throws MembershipNotFoundException, GroupNotFoundException, OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException;

    /**
     * Tests if the connection is OK. This test uses a user search to validate the connection.
     * It will fail if the application does not have permission to perform this very basic operation.
     *
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws OperationFailedException       if the test fails
     */
    void testConnection()
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException;

    /**
     * Searches for users matching the following criteria.
     *
     * @param searchRestriction restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of users satisfying the search restriction.
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<User> searchUsers(final SearchRestriction searchRestriction, int startIndex, int maxResults)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException;

    /**
     * Searches for usernames matching the <tt>searchRestriction</tt> criteria.
     *
     * @param searchRestriction restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of usernames satisfying the search restriction.
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<String> searchUserNames(final SearchRestriction searchRestriction, final int startIndex, final int maxResults)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException;

    /**
     * Searches for groups matching the following criteria.
     *
     * @param searchRestriction restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of groups satisfying the search restriction.
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<Group> searchGroups(final SearchRestriction searchRestriction, int startIndex, int maxResults)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException;

    /**
     * Searches for group names matching the <tt>searchRestriction</tt> criteria.
     *
     * @param searchRestriction restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of group names satisfying the search restriction.
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<String> searchGroupNames(final SearchRestriction searchRestriction, final int startIndex, final int maxResults)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException;

    /**
     * Searches for users who are direct members of a group.
     *
     * @param groupName restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of users satisfying the search restriction.
     * @throws GroupNotFoundException         if the group could not be found
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<User> getUsersOfGroup(final String groupName, final int startIndex, final int maxResults)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException;

    /**
     * Searches for users who are direct members of a group, returning the user names.
     *
     * @param groupName restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of user names satisfying the search restriction.
     * @throws GroupNotFoundException         if the group could not be found
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<String> getNamesOfUsersOfGroup(final String groupName, final int startIndex, final int maxResults)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException;

    /**
     * Searches for groups who are direct members of a group.
     *
     * @param groupName restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of groups satisfying the search restrictions
     * @throws GroupNotFoundException         if the group could not be found
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<Group> getChildGroupsOfGroup(final String groupName, final int startIndex, final int maxResults)
            throws GroupNotFoundException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException;

    /**
     * Searches for groups who are direct members of a group, returning the group names.
     *
     * @param groupName restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of group names satisfying the search restriction.
     * @throws GroupNotFoundException         if the group could not be found
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<String> getNamesOfChildGroupsOfGroup(final String groupName, final int startIndex, final int maxResults)
            throws OperationFailedException, GroupNotFoundException, InvalidAuthenticationException, ApplicationPermissionException;

    /**
     * Searches for groups that a user is a direct member of.
     *
     * @param userName restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of groups satisfying the search restriction.
     * @throws UserNotFoundException          if the user could not be found
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<Group> getGroupsForUser(final String userName, final int startIndex, final int maxResults)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException, UserNotFoundException;

    /**
     * Searches for groups that a user is a direct member of, returning the group names.
     *
     * @param userName restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of group names satisfying the search restriction.
     * @throws UserNotFoundException          if the user could not be found
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<String> getNamesOfGroupsForUser(final String userName, final int startIndex, final int maxResults)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException, UserNotFoundException;

    /**
     * Searches for groups that a group is a direct member of.
     *
     * @param groupName restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of groups satisfying the search restriction.
     * @throws GroupNotFoundException         if the group could not be found
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<Group> getParentGroupsForGroup(final String groupName, final int startIndex, final int maxResults)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException, GroupNotFoundException;

    /**
     * Searches for groups that a group is a direct member of, returning the group names.
     *
     * @param groupName restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of group names satisfying the search restriction
     * @throws GroupNotFoundException         if the group could not be found
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<String> getNamesOfParentGroupsForGroup(final String groupName, final int startIndex, final int maxResults)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException, GroupNotFoundException;

    /**
     * Searches for users who are nested members of a group.
     *
     * @param groupName restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of users satisfying the search restriction.
     * @throws GroupNotFoundException         if the group could not be found
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<User> getNestedUsersOfGroup(final String groupName, final int startIndex, final int maxResults)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException, GroupNotFoundException;

    /**
     * Searches for users who are nested members of a group, returning the user names.
     *
     * @param groupName restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of user names satisfying the search restriction.
     * @throws GroupNotFoundException         if the group could not be found
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<String> getNamesOfNestedUsersOfGroup(final String groupName, final int startIndex, final int maxResults)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException, GroupNotFoundException;

    /**
     * Searches for groups who are nested members of a group.
     *
     * @param groupName restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of groups satisfying the search restriction.
     * @throws GroupNotFoundException         if the group could not be found
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<Group> getNestedChildGroupsOfGroup(final String groupName, final int startIndex, final int maxResults)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException, GroupNotFoundException;

    /**
     * Searches for groups who are nested members of a group, returning the group names.
     *
     * @param groupName restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of group names satisfying the search restriction.
     * @throws GroupNotFoundException         if the group could not be found
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<String> getNamesOfNestedChildGroupsOfGroup(final String groupName, final int startIndex, final int maxResults)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException, GroupNotFoundException;

    /**
     * Searches for groups that a user is a nested member of.
     *
     * @param userName restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of groups satisfying the search restriction.
     * @throws UserNotFoundException          if the user could not be found
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<Group> getGroupsForNestedUser(final String userName, final int startIndex, final int maxResults)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException, UserNotFoundException;

    /**
     * Searches for groups that a user is a nested member of, returning the group names.
     *
     * @param userName restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of group names satisfying the search restriction.
     * @throws UserNotFoundException          if the user could not be found
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<String> getNamesOfGroupsForNestedUser(final String userName, final int startIndex, final int maxResults)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException, UserNotFoundException;

    /**
     * Searches for groups that a group is a nested member of.
     *
     * @param groupName restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of groups satisfying the search restriction.
     * @throws GroupNotFoundException         if the group could not be found
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<Group> getParentGroupsForNestedGroup(final String groupName, final int startIndex, final int maxResults)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException, GroupNotFoundException;

    /**
     * Searches for groups that a group is a nested member of, returning the group names.
     *
     * @param groupName restriction on the search
     * @param startIndex starting index of the search results
     * @param maxResults maximum number of results returned from the search
     * @return List of group names satisfying the search restriction.
     * @throws GroupNotFoundException          if the group could not be found
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server.
     * @throws InvalidAuthenticationException if the application and password are not valid.
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    List<String> getNamesOfParentGroupsForNestedGroup(final String groupName, final int startIndex, final int maxResults)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException, GroupNotFoundException;

    /**
     * <p>Gets the full group membership details for all groups with all direct user members and child groups. The result
     * may be large and this operation may be slow.</p>
     * 
     * <p>This method is only supported when the server supports version 1.1 of the user management API. Clients
     * should be ready to catch {@link UnsupportedCrowdApiException} and fall back to another technique
     * if they need to remain backwards compatible.</p>
     * 
     * @return an {@link Iterable} of the memberships for all groups
     * @throws UnsupportedCrowdApiException if the server does not support version 1.1 of the user management API
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    Iterable<Membership> getMemberships()
        throws OperationFailedException, ApplicationPermissionException, InvalidAuthenticationException, UnsupportedCrowdApiException;
    
    /**
     * Returns the user from the specified user token.
     *
     * @param token user token used to find the authenticated user.
     * @return User associated with the token.
     * @throws InvalidTokenException          if the provided token is not valid
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    User findUserFromSSOToken(final String token)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException, InvalidTokenException;

    /**
     * Authenticates a Single-Sign-On (SSO) User.
     *
     * @param userAuthenticationContext the user's authentication details.
     * @return SSO token if successful.
     * @throws ApplicationAccessDeniedException if the user does not have access to authenticate against the application
     * @throws InactiveAccountException         if the user account is inactive
     * @throws ExpiredCredentialException       if the user password has expired and the user is required to change their password
     * @throws ApplicationPermissionException   if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException   if the application and password are not valid
     * @throws OperationFailedException         if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    String authenticateSSOUser(final UserAuthenticationContext userAuthenticationContext)
            throws ApplicationAccessDeniedException, InactiveAccountException, ExpiredCredentialException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException;

    /**
     * Authenticates a Single-Sign-On (SSO) User without validating password.
     *
     * @param userAuthenticationContext the user's authentication details.
     * @return SSO token if successful.
     * @throws ApplicationAccessDeniedException if the user does not have access to authenticate against the application
     * @throws InactiveAccountException         if the user account has expired
     * @throws ApplicationPermissionException   if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException   if the application and password are not valid
     * @throws OperationFailedException         if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    String authenticateSSOUserWithoutValidatingPassword(UserAuthenticationContext userAuthenticationContext)
            throws ApplicationPermissionException, InactiveAccountException, ApplicationAccessDeniedException, InvalidAuthenticationException, OperationFailedException;

    /**
     * Validates the SSO authentication. Throws InvalidAuthenticationException if the SSO authentication is not valid.
     *
     * @param token Crowd SSO token
     * @param validationFactors Details of where the user's come from. If presented, must match those presented during authentication.
     * @throws InvalidTokenException          if the supplied token is not valid
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    void validateSSOAuthentication(final String token, final List<ValidationFactor> validationFactors)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException, InvalidTokenException;

    /**
     * Invalidates a token.  If the token does not exist, the token should still return silently.
     *
     * @param token token to invalidate
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server
     * @throws InvalidAuthenticationException if the application and password are not valid
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    void invalidateSSOToken(final String token)
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException;

    /**
     * Returns the cookie configuration.
     *
     * @return cookie configuration
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server.
     * @throws InvalidAuthenticationException if the application and password are not valid.
     * @throws OperationFailedException       if the operation has failed for any other reason, including invalid arguments and the operation not being supported on the server.
     */
    CookieConfiguration getCookieConfiguration()
            throws OperationFailedException, InvalidAuthenticationException, ApplicationPermissionException;

    /**
     * Closes this Crowd Client and releases any system resources associated
     * with the client.
     * <p>
     * The client can no longer be used once shutdown.
     * <p>
     * Calling this method more than once will have no effect.
     */
    void shutdown();

    /**
     * Returns a token that can be used for querying events that have happened
     * after the token was generated.
     * <p>
     * If the event token has not changed since the last call to this method,
     * it is guaranteed that no new events have been received.
     * <p>
     * The format of event token is implementation specific and can change
     * without a warning.
     *
     * @return token that can be used for querying events that have happened after the token was generated
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server.
     * @throws InvalidAuthenticationException if the application and password are not valid.
     * @throws UnsupportedCrowdApiException if the remote server does not support this operation
     * @throws OperationFailedException if the operation has failed for any other reason, including invalid arguments
     * @since 2.3
     */
    String getCurrentEventToken()
            throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException;

    /**
     * Returns an events object which contains a new eventToken and events that
     * happened after the given {@code eventToken} was generated.
     * <p>
     * If for any reason event store is unable to retrieve events that happened
     * after the event token was generated, an
     * {@link EventTokenExpiredException} will be thrown. The caller is then
     * expected to call {@link #getCurrentEventToken()} again before asking for
     * new events.
     *
     * @param eventToken event token that was retrieved by a call to {@link #getCurrentEventToken()} or {@link #getNewEvents(String)}
     * @return events object which contains a new eventToken and events that happened after the given {@code eventToken} was generated
     * @throws EventTokenExpiredException if events that happened after the event token was generated can not be retrieved
     * @throws IncrementalSynchronisationNotAvailableException if the application cannot provide incremental synchronisation
     * @throws ApplicationPermissionException if the application is not permitted to perform the requested operation on the server.
     * @throws InvalidAuthenticationException if the application and password are not valid.
     * @throws UnsupportedCrowdApiException if the remote server does not support this operation
     * @throws OperationFailedException if the operation has failed for any other reason, including invalid arguments
     * @since 2.3
     */
    Events getNewEvents(String eventToken)
            throws EventTokenExpiredException, IncrementalSynchronisationNotAvailableException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException;
}
