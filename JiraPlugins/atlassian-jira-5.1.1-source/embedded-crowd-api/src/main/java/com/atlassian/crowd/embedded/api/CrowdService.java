package com.atlassian.crowd.embedded.api;

import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.AccountNotFoundException;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidMembershipException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.crowd.exception.runtime.GroupNotFoundException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.crowd.exception.runtime.UserNotFoundException;

import java.util.Set;


/**
 * Provides the interface for performing User and Group operations in Crowd for applications embedding Crowd.
 * {@link User#getDirectoryId()} is ignored in all methods having {@link User} in the parameter list. Null parameters
 * for methods may throw {@link NullPointerException} or {@link IllegalArgumentException}.
 *
 * @see CrowdDirectoryService
 */
public interface CrowdService
{
    /**
     * Authenticates a {@link User user} with the given credential.
     *
     * @param name The name of the user (username).
     * @param credential The supplied credential to authenticate with
     * @return The populated user if the authentication is valid.
     *
     * @throws FailedAuthenticationException Authentication with the provided credentials failed. It may indicate that
     *              the user does not exist or the user's account is inactive or the credentials are incorrect
     * @throws InactiveAccountException The supplied user is inactive.
     * @throws ExpiredCredentialException The user's credentials have expired. The user must change their credentials in order to successfully authenticate.
     * @throws AccountNotFoundException User with the given name could not be found
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    User authenticate(String name, String credential) throws FailedAuthenticationException, OperationFailedException;

    /**
     * Returns the {@link User user} that matches the supplied {@code name}.
     * @param name the name of the user (username). Does not allow null, blank or empty strings.
     * @return user entity or {@code null} if the user is not found
     */
    User getUser(String name);

    /**
     * Returns the {@link UserWithAttributes user} that matches the supplied {@code name}.
     * @param name the name of the user (username).
     * @return user entity with attributes or {@code null} if the user is not found
     */
    UserWithAttributes getUserWithAttributes(String name);

    /**
     * Finds the {@link Group group} that matches the supplied {@code name}.
     * @param name the name of the group.
     * @return group entity, {@code null} if not found.
     */
    Group getGroup(String name);

    /**
     * Finds the {@link GroupWithAttributes group} that matches the supplied {@code name}.
     * @param name the name of the group.
     * @return group entity with attributes, {@code null} if not found.
     */
    GroupWithAttributes getGroupWithAttributes(String name);

    /**
     * Searches for entities (e.g. {@link Group} or {@link User}) of type {@code <T>} that match the supplied search query.
     *
     * Search return types must be either {@link String}, {@link User} or {@link Group}.
     *
     * @param query Query for a given entity (e.g. {@link Group} or {@link User})
     * @return entities of type {@code T} matching the search query. An {@link Iterable} containing no results will be
     *         returned if there are no matches for the query. 
     */
    <T> Iterable<T> search(Query<T> query);

    /**
     * Returns {@code true} if the user is a direct or indirect (nested) member of the group.
     * @param userName user to inspect.
     * @param groupName group to inspect.
     * @return {@code true} if and only if the user is a direct or indirect (nested) member of the group.
     *         If the user or group cannot found, then {@code false} is returned.
     *
     * @see #isUserMemberOfGroup(User, Group)
     */
    boolean isUserMemberOfGroup(String userName, String groupName);

    /**
     * Returns {@code true} if the user is a direct or indirect (nested) member of the group.
     * @param user user to inspect.
     * @param group group to inspect.
     * @return {@code true} if and only if the user is a direct or indirect (nested) member of the group, otherwise false.
     *         If the user or group cannot found, then {@code false} is returned.
     *
     * @see #isUserMemberOfGroup(String, String)
     */
    boolean isUserMemberOfGroup(User user, Group group);

    /**
     * Returns {@code true} if {@code childGroupName} is a direct or indirect (nested) member of {@code parentGroupName}.
     * @param childGroupName name of child group to inspect.
     * @param parentGroupName name of parent group to inspect.
     * @return {@code true} if and only if the {@code childGroupName} is a direct or indirect (nested) member of the {@code parentGroupName}.
     *         If any of the groups cannot found, then {@code false} is returned.
     */
    boolean isGroupMemberOfGroup(String childGroupName, String parentGroupName);
    
    /**
     * Returns {@code true} if the {@code childGroup} is a direct or indirect (nested) member of the {@code parentGroup}.
     * @param childGroup group to inspect.
     * @param parentGroup group to inspect.
     * @return {@code true} if and only if the {@code childGroup} is a direct or indirect (nested) member of the {@code parentGroup}.
     *         If any of the groups cannot found, then {@code false} is returned.
     */
    boolean isGroupMemberOfGroup(Group childGroup, Group parentGroup);

    /**
     * Adds a {@link User user} to the directory store. The user must have non-null names and email address.
     *
     * @param user       template of the user to add.
     * @param credential password. May not be null or blank.
     * @return the added user retrieved from the underlying store.
     * @throws com.atlassian.crowd.exception.InvalidUserException           The supplied user's details are invalid and/or incomplete.
     * @throws com.atlassian.crowd.exception.InvalidCredentialException     The supplied credential is invalid, this may be due the credential not matching required directory constraints.
     * @throws com.atlassian.crowd.exception.OperationNotPermittedException if the directory has been configured to not allow the operation to be performed
     * @throws OperationFailedException if the operation failed for any other reason
     */
    User addUser(User user, String credential) throws InvalidUserException, InvalidCredentialException, OperationNotPermittedException, OperationFailedException;

    /**
     * Updates the {@link User}. The user must have non-null names and email address.
     *
     * @param user The user to update.
     * @return the updated user retrieved from the underlying store. This might be a new object instance, depending on the underlying {@link Directory}
     * @throws UserNotFoundException          if the supplied user does not exist in the {@link User#getDirectoryId() directory}.
     * @throws InvalidUserException           The supplied user's details are invalid and/or incomplete.
     * @throws OperationNotPermittedException if the directory has been configured to not allow the operation to be performed
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    User updateUser(User user) throws UserNotFoundException, InvalidUserException, OperationNotPermittedException, OperationFailedException;

    /**
     * Updates the {@link PasswordCredential password} for a {@link User user}.
     *
     * @param user       The name of the user (username).
     * @param credential The new credential (password). May not be null or blank.
     * @throws InvalidCredentialException     The supplied credential is invalid, this may be due the credential not matching required directory constraints.
     * @throws UserNotFoundException          if the supplied user does not exist in the {@link User#getDirectoryId() directory}.
     * @throws OperationNotPermittedException if the directory has been configured to not allow the operation to be performed
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void updateUserCredential(User user, String credential) throws UserNotFoundException, InvalidCredentialException, OperationNotPermittedException, OperationFailedException;


    /**
     * Adds or updates a user's attribute with the new attribute value. The attributes represents new or
     * updated attributes and does not replace existing attributes unless the key of an attribute matches the key of an
     * existing attribute. This will not remove any attributes.
     *
     * @param user           user to update.
     * @param attributeName  the name of the attribute
     * @param attributeValue the new value of the attribute; any existing values will be replaced
     * @throws UserNotFoundException          the supplied user does not exist.
     * @throws OperationNotPermittedException if the directory has been configured to not allow the operation to be performed
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void setUserAttribute(User user, String attributeName, String attributeValue) throws UserNotFoundException, OperationNotPermittedException, OperationFailedException;

    /**
     * Adds or updates a user's attribute with the new attribute values. The attributes represents new or
     * updated attributes and does not replace existing attributes unless the key of an attribute matches the key of an
     * existing. This will not remove any attributes.
     *
     * @param user            user to update.
     * @param attributeName   the name of the attribute
     * @param attributeValues the new set of values; any existing values will be replaced
     * @throws UserNotFoundException          the supplied user does not exist.
     * @throws OperationNotPermittedException if the directory has been configured to not allow the operation to be performed
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void setUserAttribute(User user, String attributeName, Set<String> attributeValues) throws UserNotFoundException, OperationNotPermittedException, OperationFailedException;

    /**
     * Removes all the values for a single attribute key for a user.
     *
     * @param user          user to update.
     * @param attributeName name of attribute to remove.
     * @throws UserNotFoundException          user with supplied username does not exist.
     * @throws OperationNotPermittedException if the directory has been configured to not allow the operation to be performed
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void removeUserAttribute(User user, String attributeName) throws UserNotFoundException, OperationNotPermittedException, OperationFailedException;

    /**
     * Remove all attributes for a user.
     *
     * @param user user to update.
     * @throws UserNotFoundException          user with supplied username does not exist.
     * @throws OperationNotPermittedException if the directory has been configured to not allow the operation to be performed
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void removeAllUserAttributes(User user) throws UserNotFoundException, OperationNotPermittedException, OperationFailedException;

    /**
     * Removes the {@link User user} that matches the supplied {@code name}.
     *
     * @param user user to remove.
     * @return <tt>true</tt> if the user was removed as a result of this call, <tt>false</tt> if the user does not exist.
     * @throws OperationNotPermittedException if the directory has been configured to not allow the operation to be performed
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    boolean removeUser(User user) throws OperationNotPermittedException, OperationFailedException;

    /**
     * Adds a {@link Group group} to the directory store.
     *
     * @param group template of the group to add.
     * @return the added group retrieved from the underlying store.
     * @throws com.atlassian.crowd.exception.embedded.InvalidGroupException if the group already exists in ANY associated directory or the group template does not have the required properties populated.
     * @throws OperationNotPermittedException if the directory has been configured to not allow the operation to be performed
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    Group addGroup(Group group) throws InvalidGroupException, OperationNotPermittedException, OperationFailedException;

    /**
     * Updates the {@link Group group}.
     *
     * @param group The group to update.
     * @return the updated group retrieved from the underlying store.
     * @throws com.atlassian.crowd.exception.runtime.GroupNotFoundException if group with given name does not exist in ANY assigned directory.
     * @throws InvalidGroupException          the supplied group is invalid.
     * @throws OperationNotPermittedException if the directory has been configured to not allow the operation to be performed
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    Group updateGroup(Group group) throws GroupNotFoundException, InvalidGroupException, OperationNotPermittedException, OperationFailedException;

    /**
     * Adds or updates a group's attributes with the new attributes. The attributes represents new or
     * updated attributes and does not replace existing attributes unless the key of an attribute matches the key of an
     * existing. This will not remove any attributes.
     *
     * @param group          name of group to update.
     * @param attributeName  the name up the attribute to add or update
     * @param attributeValue the value of the attribute
     * @throws GroupNotFoundException         if the {@code group} could not be found
     * @throws OperationNotPermittedException if the directory has been configured to not allow the operation to be performed
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void setGroupAttribute(Group group, String attributeName, String attributeValue) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException;

    /**
     * Adds or updates a group's attributes with the new {@link Attributes attributes}. The attributes represents new or
     * updated attributes and does not replace existing attributes unless the key of an attribute matches the key of an
     * existing. This will not remove any attributes.
     *
     * @param group           name of group to update.
     * @param attributeName   the name up the attribute to add or update
     * @param attributeValues a set of values to update
     * @throws GroupNotFoundException         if the {@code group} could not be found
     * @throws OperationNotPermittedException if the directory has been configured to not allow the operation to be performed
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void setGroupAttribute(Group group, String attributeName, Set<String> attributeValues) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException;

    /**
     * Removes all the values for a single attribute key for a group.
     *
     * @param group         to update.
     * @param attributeName name of attribute to remove.
     * @throws GroupNotFoundException         if the {@code group} could not be found
     * @throws OperationNotPermittedException if the directory has been configured to not allow the operation to be performed
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void removeGroupAttribute(Group group, String attributeName) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException;

    /**
     * Removes all group attributes.
     *
     * @param group to update.
     * @throws GroupNotFoundException if the {@code group} could not be found
     * @throws OperationNotPermittedException if the directory has been configured to not allow the operation to be performed
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void removeAllGroupAttributes(Group group) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException;

    /**
     * Removes the {@link Group group} that matches the supplied <code>name</code>.
     *
     * @param group to remove
     * @return <tt>true</tt> if the group was removed as a result of this call, <tt>false</tt> if the group does not exist.
     * @throws OperationNotPermittedException if the directory does not allow removal of this group
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    boolean removeGroup(Group group) throws OperationNotPermittedException, OperationFailedException;

    /**
     * Adds a user as a member of a group. This means that all user members of <code>childGroup</code> will
     * appear as members of <code>parentGroup</code> to querying applications.
     *
     * @param user  The user that will become a member of the {@code group}
     * @param group The group that will gain a new member.
     * @throws UserNotFoundException  if the {@code user} could not be found
     * @throws GroupNotFoundException if the {@code group} could not be found
     * @throws OperationNotPermittedException if the directory has been configured to not allow the operation to be performed
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void addUserToGroup(User user, Group group) throws GroupNotFoundException, UserNotFoundException, OperationNotPermittedException, OperationFailedException;

    /**
     * Adds a group as a member of a parent group. Cyclic group membership are allowed (mainly because LDAP allows it)
     * but not recommended. I.e. group A can have group B as its member and group B can have group A as its member at
     * the same time.
     *
     * @param childGroup  The group that will become a member of {@code parentGroup}
     * @param parentGroup The group that will gain a new member
     * @throws GroupNotFoundException if any of the group could not be found. Use {@link GroupNotFoundException#getGroupName()}
     *                                to find out which group wasn't found
     * @throws OperationNotPermittedException
     *                                if the directory has been configured to not allow the operation to be performed
     * @throws com.atlassian.crowd.exception.InvalidMembershipException If the relationship would cause a circular reference.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void addGroupToGroup(Group childGroup, Group parentGroup) throws GroupNotFoundException, OperationNotPermittedException, InvalidMembershipException, OperationFailedException;

    /**
     * Removes a user as a member of a group.
     *
     * @param group The group that will lose the member.
     * @param user  The user that will be removed from the {@code group}
     * @return <tt>true</tt> if the user was removed from the group as a result of this call, <tt>false</tt> if the user is not a member of the group.
     * @throws UserNotFoundException        if the {@code user} could not be found
     * @throws GroupNotFoundException       if the {@code group} could not be found
     * @throws OperationNotPermittedException if the directory has been configured to not allow the operation to be performed
     * @throws OperationFailedException if the operation failed for any other reason
     */
    boolean removeUserFromGroup(User user, Group group) throws GroupNotFoundException, UserNotFoundException, OperationNotPermittedException, OperationFailedException;

    /**
     * Removes a group as a member of a parent group.
     *
     * @param childGroup  The group that will be removed from {@code parentGroup}
     * @param parentGroup The group that will lose the member.
     * @return <tt>true</tt> if childGroup was removed from parentGroup as a result of this call, <tt>false</tt> if childGroup is not a member of the parentGroup.
     * @throws GroupNotFoundException       if any of the groups could not be found. Use {@link GroupNotFoundException#getGroupName()}
     *                                      to find out which group wasn't found
     * @throws OperationNotPermittedException
     *                                      if the directory has been configured to not allow the operation to be performed
     * @throws OperationFailedException if the operation failed for any other reason
     */
    boolean removeGroupFromGroup(Group childGroup, Group parentGroup) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException;

    /**
     * Determines if a user is a direct member of a group.
     *
     * @param user  the user for whom to check the group membership
     * @param group the group the {@code user} is believed to belong to
     * @return {@code true} if the user is a direct member of the group, {@code false} otherwise
     *         (including if the user and/or group could not be found)
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    boolean isUserDirectGroupMember(User user, Group group) throws OperationFailedException;

    /**
     * Determines if a group is a direct member of another group.
     *
     * @param childGroup  the group for which to check the {@code parentGroup} membership
     * @param parentGroup the group the {@code childGroup} is believed to belong to
     * @return {@code true} if the {@code childGroup} is a direct member of the {@code parentGroup}, {@code false} otherwise
     *         (including if neither group could be found)
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    boolean isGroupDirectGroupMember(Group childGroup, Group parentGroup) throws OperationFailedException;

    /**
     * Searches for {@link User} entities that match the supplied search query.
     * Users with duplicate user names can be returned.
     *
     * @param query the search query.
     * @return {@link User} entities matching the search query. An {@link Iterable} containing no results will be
     *         returned if there are no matches for the query.
     */
    Iterable<User> searchUsersAllowingDuplicateNames(Query<User> query);
}
