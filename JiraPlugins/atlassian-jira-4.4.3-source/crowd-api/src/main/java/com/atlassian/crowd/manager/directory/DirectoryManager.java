package com.atlassian.crowd.manager.directory;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectorySynchronisationInformation;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.*;
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

/**
 * A service interface providing:
 * <ol>
 * <li>Directory CRUD Operations</li>
 * <li>Token Operations</li>
 * <li>RemoteDirectory User Operations</li>
 * <li>RemoteDirectory Group Operations</li>
 * <li>RemoteDirectory Membership Operations</li>
 * <li>RemoteDirectory Bulk Operations</li>
 * </ol>
 * <p/>
 * Methods on this interface operate on a single directory whereas methods on the
 * {@link com.atlassian.crowd.manager.application.ApplicationService} interface
 * amalgamate results from multiple directories.
 * <p/>
 * In the future, it is likely that we will break up the DirectoryManager into
 * a DirectoryManager (directory CRUD), RemoteDirectoryManager (remote directory delegater)
 * and a TokenManager (token ops) for better functional decomposition.
 */
public interface DirectoryManager
{
    ///////////// DIRECTORY CRUD OPERATIONS /////////////

    /**
     * Adds the given directory.
     *
     * @param directory the Directory to add
     * @return the added Directory
     * @throws com.atlassian.crowd.exception.DirectoryInstantiationException
     *          if there was an error instantiating the directory
     */
    Directory addDirectory(Directory directory) throws DirectoryInstantiationException;

    /**
     * Returns a Directory based on the passed in <code>directoryId</code>.
     *
     * @param directoryID the id of the directory to find
     * @return the directory
     * @throws DirectoryNotFoundException is thrown if the Directory cannot be found.
     */
    Directory findDirectoryById(long directoryID) throws DirectoryNotFoundException;

    /**
     * Returns a List of all directories in the system.
     *
     * @return List of all Directories.
     */
    List<Directory> findAllDirectories();

    /**
     * Returns a List of Directories matching the search query.
     *
     * @param query the context to search on
     * @return a List of directories, or an Empty List of none are found
     */
    List<Directory> searchDirectories(EntityQuery<Directory> query);

    /**
     * Finds a directory given the supplied <code>name</code>.
     *
     * @param name the name of the directory
     * @return the directory with the given <code>name</code>
     * @throws DirectoryNotFoundException if no Directory is found
     */
    Directory findDirectoryByName(String name) throws DirectoryNotFoundException;

    /**
     * Updates the passed in directory.
     *
     * @param directory the directory with updated attributes
     * @return the updated directory
     * @throws DirectoryNotFoundException if no Directory is found
     */
    Directory updateDirectory(Directory directory) throws DirectoryNotFoundException;

    /**
     * Removes a given directory and all its associated entities and mappings.
     *
     * @param directory the directory to remove
     * @throws DirectoryNotFoundException if the directory cannot be found
     * @throws DirectoryCurrentlySynchronisingException if the directory is currently synchronising
     */
    void removeDirectory(Directory directory) throws DirectoryNotFoundException, DirectoryCurrentlySynchronisingException;

    ///////////// USER OPERATIONS /////////////

    /**
     * @param directoryId        the id of the directory to authenticate against
     * @param username           the username to use for authentication
     * @param passwordCredential the credential to use for authentication
     * @return will return the user if authentication is successful
     * @throws OperationFailedException       underlying directory implementation failed to execute the operation.
     * @throws InactiveAccountException       if the user account is inactive
     * @throws InvalidAuthenticationException if authentication with the provided credentials failed
     * @throws ExpiredCredentialException     if the credentials of the user have expired.
     * @throws DirectoryNotFoundException     if the directory with the given directoryId cannot be found.
     * @throws UserNotFoundException          if no user with the supplied username exists in the directory
     */
    User authenticateUser(long directoryId, String username, PasswordCredential passwordCredential)
            throws OperationFailedException, InactiveAccountException, InvalidAuthenticationException, ExpiredCredentialException, DirectoryNotFoundException, UserNotFoundException;

    /**
     * Returns the user matching the supplied username in the directory specified by the passed in <code>directoryId</code>..
     *
     * @param directoryId ID of the directory to search.
     * @param username    username of the user to retrieve.
     * @return user matching the supplied username.
     * @throws DirectoryNotFoundException if the directory with the given directoryId cannot be found.
     * @throws UserNotFoundException      if no user with the supplied username exists in the directory
     * @throws OperationFailedException   underlying directory implementation failed to execute the operation.
     * @see com.atlassian.crowd.manager.directory.DirectoryManager#findUserWithAttributesByName(long, String)
     */
    User findUserByName(long directoryId, String username)
            throws DirectoryNotFoundException, UserNotFoundException, OperationFailedException;

    /**
     * Returns the user with all attributes matching the supplied username in the directory specified by the passed in <code>directoryId</code>..
     *
     * @param directoryId ID of the directory to search.
     * @param username    username of the user to retrieve.
     * @return user (with all attributes) matching the supplied username.
     * @throws DirectoryNotFoundException if the directory with the given directoryId cannot be found.
     * @throws UserNotFoundException      if no user with the supplied username exists in the directory
     * @throws OperationFailedException   underlying directory implementation failed to execute the operation.
     * @see com.atlassian.crowd.manager.directory.DirectoryManager#findUserByName(long, String)
     */
    UserWithAttributes findUserWithAttributesByName(long directoryId, String username)
            throws DirectoryNotFoundException, UserNotFoundException, OperationFailedException;

    /**
     * Returns a list of users matching the given query in the directory specified by the passed in <code>directoryId</code>..
     *
     * @param directoryId ID of the directory to search.
     * @param query       query to exectute.
     * @return List of {@link com.atlassian.crowd.model.user.User} entities or {@link String} usernames matching the query criteria.
     * @throws OperationFailedException   underlying directory implementation failed to execute the operation.
     * @throws DirectoryNotFoundException if the directory with the given directoryId cannot be found.
     */
    <T> List<T> searchUsers(long directoryId, EntityQuery<T> query) throws DirectoryNotFoundException, OperationFailedException;

    /**
     * Adds a User to the directory specified by the passed in <code>directoryId</code>.
     *
     * @param directoryId ID of the directory to add the user to.
     * @param user        a template of the user to be added.
     * @param credential  the password credential of the user (unencrypted).
     * @return the added user returned from the directory.
     * @throws com.atlassian.crowd.exception.InvalidCredentialException
     *                                      if the user's credential does not meet the validation requirements for the given directory.
     * @throws InvalidUserException         if the user template does not have the required properties populated.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#CREATE_USER}.
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws UserAlreadyExistsException   if the user already exists in the given directory
     */
    User addUser(long directoryId, UserTemplate user, PasswordCredential credential)
            throws InvalidCredentialException, InvalidUserException, DirectoryPermissionException, DirectoryNotFoundException, OperationFailedException, UserAlreadyExistsException;

    /**
     * Updates a user with the supplied template and returns the updated user retrieved from the directory specified by the passed in <code>directoryId</code>.
     * <p/>
     * This method cannot be used to rename the user, update the user's credentials or update the user's custom attributes.
     *
     * @param directoryId ID of the directory to find and update the user.
     * @param user        template of the user to update.
     * @return the updated user returned from the directory.
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws UserNotFoundException        if no user with the supplied username exists in the directory
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_USER}.
     * @throws InvalidUserException         the user template does not have the required properties populated.
     * @see com.atlassian.crowd.manager.directory.DirectoryManager#renameUser(long, String, String)
     * @see com.atlassian.crowd.manager.directory.DirectoryManager#updateUserCredential(long, String, com.atlassian.crowd.embedded.api.PasswordCredential)
     * @see com.atlassian.crowd.manager.directory.DirectoryManager#storeUserAttributes(long, String, java.util.Map)
     * @see com.atlassian.crowd.manager.directory.DirectoryManager#removeUserAttributes(long, String, String)
     */
    User updateUser(long directoryId, UserTemplate user)
            throws DirectoryNotFoundException, UserNotFoundException, DirectoryPermissionException, InvalidUserException, OperationFailedException;

    /**
     * Renames a user in the directory specified by the passed in <code>directoryId</code>.
     *
     * @param directoryId ID of the directory to find and update the user.
     * @param oldUsername current username of the user.
     * @param newUsername desired username of the user.
     * @return updated user returned from the directory.
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws UserNotFoundException        if no user with the supplied username exists in the directory
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_USER}.
     * @throws InvalidUserException         if the new username does not meet the username requirements of the directory
     * @throws UserAlreadyExistsException   if the <tt>newUsername</tt> user already exists in the given directory
     */
    User renameUser(long directoryId, String oldUsername, String newUsername)
            throws DirectoryNotFoundException, UserNotFoundException, OperationFailedException, DirectoryPermissionException, InvalidUserException, UserAlreadyExistsException;

    /**
     * Adds or updates a user's attributes with the new Map of attribute values in the directory specified by the passed in <code>directoryId</code>.
     * <p/>
     * The attributes map represents new or updated attributes and does not replace existing attributes unless the key of an attribute
     * matches the key of an existing attribute on the user.
     * <p/>
     * This method does not update primary field attributes like firstName, lastName, etc.
     *
     * @param directoryId ID of the directory to find and update the user.
     * @param username    username of the user to update.
     * @param attributes  map of one-to-many attribute-values. All attribute keys are treated as new or updated attributes.
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws UserNotFoundException        if no user with the supplied username exists in the directory
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_USER_ATTRIBUTE}.
     * @see com.atlassian.crowd.manager.directory.DirectoryManager#updateUser(long, com.atlassian.crowd.model.user.UserTemplate)
     */
    public void storeUserAttributes(long directoryId, String username, Map<String, Set<String>> attributes)
            throws DirectoryPermissionException, DirectoryNotFoundException, UserNotFoundException, OperationFailedException;

    /**
     * Removes a user's attribute values in the directory specified by the passed in <code>directoryId</code>.
     *
     * @param directoryId   ID of the directory to find and update the user.
     * @param username      username of the user to update.
     * @param attributeName all attribute values for this key will be removed from the user.
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws UserNotFoundException        if no user with the supplied username exists in the directory
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_USER_ATTRIBUTE}.
     * @see com.atlassian.crowd.manager.directory.DirectoryManager#updateUser(long, com.atlassian.crowd.model.user.UserTemplate)
     */
    public void removeUserAttributes(long directoryId, String username, String attributeName)
            throws DirectoryPermissionException, DirectoryNotFoundException, UserNotFoundException, OperationFailedException;

    /**
     * This will update the user's credential in the given directory specified by the passed in <code>directoryId</code>.
     *
     * @param directoryId ID of the directory to find and update the user.
     * @param username    username of the user to update.
     * @param credential  the new password credential for the user (unencrypted).
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_USER}.
     * @throws InvalidCredentialException   if the new credential does not meet the requirements for the given directory.
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws UserNotFoundException        if no user with the supplied username exists in the directory
     */
    void updateUserCredential(long directoryId, String username, PasswordCredential credential)
            throws DirectoryPermissionException, InvalidCredentialException, DirectoryNotFoundException, UserNotFoundException, OperationFailedException;

    /**
     * Resets the password of user in the directory specified by the passed in <code>directoryId</code>.
     *
     * @param directoryId ID of the directory to find and update the user credential.
     * @param username    username of the user to update.
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws UserNotFoundException        if no user with the supplied username exists in the directory
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws InvalidEmailAddressException if the user does not have a valid email address to send the password reset email to.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_USER}.
     * @throws InvalidCredentialException   if the randomly generated credential does not meet the requirements for the given directory.
     * @deprecated since 2.1
     */
    void resetPassword(long directoryId, String username)
            throws DirectoryNotFoundException, UserNotFoundException, InvalidEmailAddressException, DirectoryPermissionException, InvalidCredentialException, OperationFailedException;

    /**
     * Removes a user matching the supplied username in the directory specified by the passed in <code>directoryId</code>.
     *
     * @param directoryId ID of the directory to remove the user from.
     * @param username    username of the user to remove.
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws UserNotFoundException        if no user with the supplied username exists in the directory
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#DELETE_USER}.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void removeUser(long directoryId, String username)
            throws DirectoryNotFoundException, UserNotFoundException, DirectoryPermissionException, OperationFailedException;


    ///////////// GROUP OPERATIONS /////////////

    /**
     * Returns the group matching the supplied groupName in the directory specified by the passed in <code>directoryId</code>..
     *
     * @param directoryId ID of the directory to search.
     * @param groupName   groupName of the group to retrieve.
     * @return group matching the supplied groupName.
     * @throws GroupNotFoundException     if no group with the supplied groupName exists in the directory
     * @throws DirectoryNotFoundException if the directory with the given directoryId cannot be found.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @see com.atlassian.crowd.manager.directory.DirectoryManager#findGroupWithAttributesByName(long, String)
     */
    Group findGroupByName(long directoryId, String groupName)
            throws GroupNotFoundException, DirectoryNotFoundException, OperationFailedException;

    /**
     * Returns the group with all attributes matching the supplied groupName in the directory specified by the passed in <code>directoryId</code>..
     *
     * @param directoryId ID of the directory to search.
     * @param groupName   groupName of the group to retrieve.
     * @return group (with all attributes) matching the supplied groupName.
     * @throws GroupNotFoundException     if no group with the supplied groupName exists in the directory
     * @throws DirectoryNotFoundException if the directory with the given directoryId cannot be found.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @see com.atlassian.crowd.manager.directory.DirectoryManager#findGroupByName(long, String)
     */
    GroupWithAttributes findGroupWithAttributesByName(long directoryId, String groupName)
            throws GroupNotFoundException, DirectoryNotFoundException, OperationFailedException;

    /**
     * Returns a list of groups matching the given query in the directory specified by the passed in <code>directoryId</code>..
     *
     * @param directoryId ID of the directory to search.
     * @param query       query to exectute.
     * @return List of {@link com.atlassian.crowd.model.group.Group} entities or {@link String} groupNames matching the query criteria.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryNotFoundException if the directory with the given directoryId cannot be found.
     */
    <T> List<T> searchGroups(long directoryId, EntityQuery<T> query)
            throws DirectoryNotFoundException, OperationFailedException;

    /**
     * Adds a Group to the directory specified by the passed in <code>directoryId</code>.
     *
     * @param directoryId ID of the directory to add the group to.
     * @param group       a template of the group to be added.
     * @return the added group returned from the directory.
     * @throws InvalidGroupException        if the group already exists in the given directory.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#CREATE_GROUP}.
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found after the add operation.
     */
    Group addGroup(long directoryId, GroupTemplate group)
            throws InvalidGroupException, DirectoryPermissionException, DirectoryNotFoundException, OperationFailedException;

    /**
     * Updates a group with the supplied template and returns the updated group retrieved from the directory specified by the passed in <code>directoryId</code>.
     * <p/>
     * This method cannot be used to rename the group, update the group's credentials or update the group's custom attributes.
     *
     * @param directoryId ID of the directory to find and update the group.
     * @param group       template of the group to update.
     * @return the updated group returned from the directory.
     * @throws GroupNotFoundException       if no group with the supplied groupName exists in the directory
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP}.
     * @throws InvalidGroupException if the group template does not have the required properties populated.
     * @throws ReadOnlyGroupException if the group is read-only
     * @see com.atlassian.crowd.manager.directory.DirectoryManager#renameGroup(long, String, String)
     * @see com.atlassian.crowd.manager.directory.DirectoryManager#storeGroupAttributes(long, String, java.util.Map)
     * @see com.atlassian.crowd.manager.directory.DirectoryManager#removeGroupAttributes(long, String, String)
     */
    Group updateGroup(long directoryId, GroupTemplate group)
            throws GroupNotFoundException, DirectoryNotFoundException, DirectoryPermissionException, InvalidGroupException, OperationFailedException, ReadOnlyGroupException;

    /**
     * Renames a group in the directory specified by the passed in <code>directoryId</code>.
     *
     * @param directoryId  ID of the directory to find and update the group.
     * @param oldGroupname current groupName of the group.
     * @param newGroupname desired groupName of the group.
     * @return updated group returned from the directory.
     * @throws GroupNotFoundException       if the group with the oldGroupname does not exist in the directory or if the directory with the given directoryId cannot be found.
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP}.
     * @throws InvalidGroupException        if the new groupName does not meet the groupName requirements of the directory or if a group already exists with the new groupName.
     */
    Group renameGroup(long directoryId, String oldGroupname, String newGroupname)
            throws GroupNotFoundException, DirectoryNotFoundException, DirectoryPermissionException, InvalidGroupException, OperationFailedException;

    /**
     * Adds or updates a group's attributes with the new Map of attribute values in the directory specified by the passed in <code>directoryId</code>.
     * <p/>
     * The attributes map represents new or updated attributes and does not replace existing attributes unless the key of an attribute
     * matches the key of an existing attribute on the group.
     * <p/>
     * This method does not update primary field attributes like firstName, lastName, etc.
     *
     * @param directoryId ID of the directory to find and update the group.
     * @param groupName   groupName of the group to update.
     * @param attributes  map of one-to-many attribute-values. All attribute keys are treated as new or updated attributes.
     * @throws GroupNotFoundException       if no group with the supplied groupName exists in the directory
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP_ATTRIBUTE}.
     * @see com.atlassian.crowd.manager.directory.DirectoryManager#updateGroup(long, com.atlassian.crowd.model.group.GroupTemplate)
     */
    public void storeGroupAttributes(long directoryId, String groupName, Map<String, Set<String>> attributes)
            throws DirectoryPermissionException, GroupNotFoundException, DirectoryNotFoundException, OperationFailedException;

    /**
     * Removes a group's attribute values in the directory specified by the passed in <code>directoryId</code>.
     *
     * @param directoryId   ID of the directory to find and update the group.
     * @param groupName     groupName of the group to update.
     * @param attributeName all attribute values for this key will be removed from the group.
     * @throws GroupNotFoundException       if no group with the supplied groupName exists in the directory
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP_ATTRIBUTE}.
     * @see com.atlassian.crowd.manager.directory.DirectoryManager#updateGroup(long, com.atlassian.crowd.model.group.GroupTemplate)
     */
    public void removeGroupAttributes(long directoryId, String groupName, String attributeName)
            throws DirectoryPermissionException, GroupNotFoundException, DirectoryNotFoundException, OperationFailedException;

    /**
     * Removes a group matching the supplied groupName in the directory specified by the passed in <code>directoryId</code>.
     *
     * @param directoryId ID of the directory to remove the group from.
     * @param groupName   groupName of the group to remove.
     * @throws GroupNotFoundException       if no group with the supplied groupName exists in the directory
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#DELETE_GROUP}.
     * @throws ReadOnlyGroupException if the group is read-only
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void removeGroup(long directoryId, String groupName)
            throws GroupNotFoundException, DirectoryNotFoundException, DirectoryPermissionException, OperationFailedException, ReadOnlyGroupException;


    ///////////// MEMBERSHIP OPERATIONS /////////////

    /**
     * Returns <code>true</code> if the user is a direct member of the group in the directory specified by the passed in <code>directoryId</code>.
     *
     * @param directoryId ID of the directory to inspect membership information.
     * @param username    name of the user to inspect.
     * @param groupName   name of the group to inspect.
     * @return <code>true</code> if and only if the user is a direct member of the group. If the group or user does not exist in the directory, <code>false</code> is returned.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryNotFoundException if the directory with the given directoryId cannot be found.
     */
    boolean isUserDirectGroupMember(long directoryId, String username, String groupName)
            throws DirectoryNotFoundException, OperationFailedException;

    /**
     * Returns <code>true</code> if the childGroup is a direct member of the parentGroup in the directory specified by the passed in <code>directoryId</code>.
     *
     * @param directoryId ID of the directory to inspect membership information.
     * @param childGroup  name of the group to inspect.
     * @param parentGroup name of the group to inspect.
     * @return <code>true</code> if and only if the childGroup is a direct member of the parentGroup. If either group does not exist in the directory, <code>false</code> is returned.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryNotFoundException if the directory with the given directoryId cannot be found.
     */
    boolean isGroupDirectGroupMember(long directoryId, String childGroup, String parentGroup)
            throws DirectoryNotFoundException, OperationFailedException;

    /**
     * Adds an existing user as a direct member of an existing group in the directory specified by the passed in <code>directoryId</code>.
     *
     * @param directoryId ID of the directory to add membership information.
     * @param username    username of the user.
     * @param groupName   name of the group.
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws UserNotFoundException        if the user does not exist in the directory
     * @throws GroupNotFoundException       if the group does not exist in the directory
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP}.
     * @throws ReadOnlyGroupException if the group is read-only
     *
     */
    void addUserToGroup(long directoryId, String username, String groupName)
            throws DirectoryPermissionException, DirectoryNotFoundException, UserNotFoundException, GroupNotFoundException, OperationFailedException, ReadOnlyGroupException;

    /**
     * Adds an existing child group as direct member of an existing parent group in the directory specified by the passed in <code>directoryId</code>.
     *
     * @param directoryId ID of the directory to add membership information.
     * @param childGroup  name of child group.
     * @param parentGroup name of the parent group.
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws GroupNotFoundException       if the group does not exist in the directory
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP}.
     * @throws InvalidMembershipException   GroupType of childGroup does not match parentGroup.
     * @throws NestedGroupsNotSupportedException
     *                                      If the directory does not support nested groups.
     * @throws ReadOnlyGroupException if the group is read-only
     */
    void addGroupToGroup(long directoryId, String childGroup, String parentGroup)
            throws DirectoryPermissionException, DirectoryNotFoundException, GroupNotFoundException,
            InvalidMembershipException, NestedGroupsNotSupportedException, OperationFailedException, ReadOnlyGroupException;

    /**
     * Removes an existing user from being a direct member of an existing group in the directory specified by the passed in <code>directoryId</code>.
     *
     * @param directoryId ID of the directory to add membership information.
     * @param username    username of the user.
     * @param groupName   name of the group.
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws UserNotFoundException        if the user does not exist in the directory
     * @throws GroupNotFoundException       if the group does not exist in the directory
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP}.
     * @throws MembershipNotFoundException  user is not a direct member of group.
     * @throws ReadOnlyGroupException if the group is read-only
     */
    void removeUserFromGroup(long directoryId, String username, String groupName)
            throws DirectoryPermissionException, DirectoryNotFoundException, UserNotFoundException, GroupNotFoundException, MembershipNotFoundException, OperationFailedException, ReadOnlyGroupException;

    /**
     * Removes an existing child group from being a direct member of an existing parent group in the directory specified by the passed in <code>directoryId</code>.
     *
     * @param directoryId ID of the directory to add membership information.
     * @param childGroup  name of child group.
     * @param parentGroup name of the parent group.
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws GroupNotFoundException       if the group does not exist in the directory
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP}.
     * @throws MembershipNotFoundException  group is not a direct member of group.
     * @throws InvalidMembershipException   GroupType of childGroup does not match parentGroup.
     * @throws ReadOnlyGroupException if the group is read-only
     */
    void removeGroupFromGroup(long directoryId, String childGroup, String parentGroup)
            throws DirectoryPermissionException, GroupNotFoundException, DirectoryNotFoundException, InvalidMembershipException, MembershipNotFoundException, OperationFailedException, ReadOnlyGroupException;

    /**
     * Searches for direct group relationships in the directory specified by the passed in <code>directoryId</code>.
     *
     * @param directoryId ID of the directory to inspect membership information.
     * @param query       membership query.
     * @return List of {@link com.atlassian.crowd.model.user.User} entities,
     *         {@link com.atlassian.crowd.model.group.Group} entites,
     *         {@link String} usernames or {@link String} group names matching the query criteria. If there are no
     *         results, returns an empty List.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryNotFoundException if the directory with the given directoryId cannot be found.
     */
    <T> List<T> searchDirectGroupRelationships(long directoryId, MembershipQuery<T> query)
            throws DirectoryNotFoundException, OperationFailedException;

    /**
     * Returns <code>true</code> if the user is a direct or indirect (nested) member of the group in the directory specified by the passed in <code>directoryId</code>.
     * <p/>
     * If the directory does not support nested groups, this call will be equivalent to {@link com.atlassian.crowd.manager.directory.DirectoryManager#isUserDirectGroupMember(long, String, String)}.
     * <p/>
     * <b>WARNING: this method could be very slow if the underlying RemoteDirectory does not employ caching.</b>
     *
     * @param directoryId ID of the directory to inspect membership information.
     * @param username    name of the user to inspect.
     * @param groupName   name of the group to inspect.
     * @return <code>true</code> if and only if the user is a direct or indirect (nested) member of the group. If the group or user does not exist in the directory, <code>false</code> is returned.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryNotFoundException if the directory with the given directoryId cannot be found.
     */
    boolean isUserNestedGroupMember(long directoryId, String username, String groupName)
            throws DirectoryNotFoundException, OperationFailedException;

    /**
     * Returns <code>true</code> if the childGroup is a direct or indirect (nested) member of the parentGroup in the directory specified by the passed in <code>directoryId</code>.
     * <p/>
     * If the directory does not support nested groups, this call will be equivalent to {@link com.atlassian.crowd.manager.directory.DirectoryManager#isGroupDirectGroupMember(long, String, String)}.
     * <p/>
     * <b>WARNING: this method could be very slow if the underlying RemoteDirectory does not employ caching.</b>
     *
     * @param directoryId ID of the directory to inspect membership information.
     * @param childGroup  name of the user to inspect.
     * @param parentGroup name of the group to inspect.
     * @return <code>true</code> if and only if the childGroup is a direct or indirect (nested) member of the parentGruop. If either group does not exist in the directory, <code>false</code> is returned.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryNotFoundException if the directory with the given directoryId cannot be found.
     */
    boolean isGroupNestedGroupMember(long directoryId, String childGroup, String parentGroup)
            throws DirectoryNotFoundException, OperationFailedException;

    /**
     * Searches for direct and indirect (nested) group relationships in the directory specified by the passed in <code>directoryId</code>.
     * <p/>
     * If the directory does not support nested groups, this call will be equivalent to {@link com.atlassian.crowd.manager.directory.DirectoryManager#searchDirectGroupRelationships(long, com.atlassian.crowd.search.query.membership.MembershipQuery)}.
     * <p/>
     * <b>WARNING: this method could be very slow if the underlying RemoteDirectory does not employ caching.</b>
     *
     * @param directoryId ID of the directory to inspect membership information.
     * @param query       membership query.
     * @return List of {@link com.atlassian.crowd.model.user.User} entities,
     *         {@link com.atlassian.crowd.model.group.Group} entites,
     *         {@link String} usernames or {@link String} group names matching the query criteria.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryNotFoundException if the directory with the given directoryId cannot be found.
     */
    <T> List<T> searchNestedGroupRelationships(long directoryId, MembershipQuery<T> query)
            throws DirectoryNotFoundException, OperationFailedException;


    ///////////// BULK OPERATIONS /////////////

    /**
     * Will add a collection of users to the directory specified by the passed in <code>directoryId</code> param.
     * <p/>
     * If adding a particular user fails then this user will be skipped
     * and the error will be reported in the logs and the BulkAddResult object.
     * <p/>
     * If the underlying directory supports
     * bulk adding of entities (ie. implements {@link com.atlassian.crowd.directory.InternalRemoteDirectory}),
     * it may be faster than manual iteration of each entity.
     *
     * @param directoryId the directory to add the User too.
     * @param users       the templates of the users to add.
     * @param overwrite   <code>true</code> if you want to remove any existing user matching a username in the users to add prior to adding the user.
     *                    <code>false</code> if you want to skip over users that already exist (same username exists).
     * @return results for bulk add process.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#CREATE_USER}.
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     */
    BulkAddResult<User> addAllUsers(long directoryId, Collection<UserTemplateWithCredentialAndAttributes> users, boolean overwrite)
            throws DirectoryPermissionException, DirectoryNotFoundException, OperationFailedException;

    /**
     * Will add a collection of Group to the directory specified by the passed in <code>directoryId</code> param.
     * <p/>
     * If adding a group fails then this user will be skipped
     * and the error will be reported in the logs and the BulkAddResult object.
     * <p/>
     * If the underlying directory supports
     * bulk adding of entities (ie. implements {@link com.atlassian.crowd.directory.InternalRemoteDirectory}),
     * it may be faster than manual iteration of each entity.
     *
     * @param directoryId the directory to add the Group too.
     * @param groups      the Groups to add.
     * @param overwrite   <code>true</code> if you want to remove any existing group matching a username in the groups to add prior to adding the group.
     *                    <code>false</code> if you want to skip over groups that already exist (same group name exists).
     * @return results for bulk add process.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#CREATE_GROUP}.
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws InvalidGroupException        if any of the group is invalid
     */
    BulkAddResult<Group> addAllGroups(long directoryId, Collection<GroupTemplate> groups, boolean overwrite)
            throws DirectoryPermissionException, DirectoryNotFoundException, OperationFailedException, InvalidGroupException;


    /**
     * A bulk version of {@link com.atlassian.crowd.manager.directory.DirectoryManager#addUserToGroup(long, String, String)}.
     * <p/>
     * If creating a particular membership fails, it will be
     * skipped and the error will be reported in the logs and the BulkAddResult object.
     * <p/>
     * If the underlying directory supports bulk adding of entities
     * (ie. implements {@link com.atlassian.crowd.directory.InternalRemoteDirectory}),
     * it may be faster than manual iteration of each entity.
     *
     * @param directoryID the directory to add the membership to.
     * @param userNames   usernames of users to add membership to.
     * @param groupName   name of group to add users to.
     * @return results for bulk add process consisting of the names of the users which could not be added to the group.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryPermissionException if the directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP}.
     * @throws DirectoryNotFoundException   if the directory with the given directoryId cannot be found.
     * @throws GroupNotFoundException       if the groupName did not resolve to a group
     * @throws UserNotFoundException        if any of the user does not exist
     */
    BulkAddResult<String> addAllUsersToGroup(long directoryID, Collection<String> userNames, String groupName)
            throws DirectoryPermissionException, DirectoryNotFoundException, GroupNotFoundException, OperationFailedException, UserNotFoundException;

    /**
     * Returns true if the underlying directory implementation supports nested groups.
     *
     * @param directoryId ID of directory.
     * @return true if the directory supports nested groups
     * @throws DirectoryInstantiationException if there was an error instantiating the directory
     * @throws DirectoryNotFoundException if the directory could not be found.
     */
    boolean supportsNestedGroups(long directoryId) throws DirectoryInstantiationException, DirectoryNotFoundException;

    /**
     * Returns true if the underlying directory implementation supports manual synchronisation of the directory's local cache.
     * <p/>
     * That is if we keep a local cache that is periodically updated from the remote server.
     *
     * @param directoryId ID of directory.
     * @return true if the directory supports synchronisation
     * @throws DirectoryInstantiationException if there was an error instantiating the directory
     * @throws DirectoryNotFoundException if the directory could not be found.
     */
    boolean isSynchronisable(long directoryId) throws DirectoryInstantiationException, DirectoryNotFoundException;

    /**
     * Requests that this directory should update its cache by synchronising with the remote User data.
     * The synchronisation will occur asynchronously, i.e. this method returns immediately and the
     * synchronization continues in the background.
     * <p/>
     * If a synchronisation is currently in progress when this method is called, then this method does nothing.
     *
     * @param directoryId ID of directory.
     * @param mode the mode of the synchronisation
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryNotFoundException if the directory could not be found.
     */
    void synchroniseCache(long directoryId, SynchronisationMode mode)
            throws OperationFailedException, DirectoryNotFoundException;

    /**
     * Requests that this directory should update its cache by synchronising with the remote User data.
     * <p/>
     * If a synchronisation is currently in progress when this method is called,
     * then this method does nothing if runInBackGround is true, otherwise it will throw OperationFailedException.
     *
     * @param directoryId ID of directory.
     * @param mode the mode of the synchronisation
     * @param runInBackground If True the synchronise will happen asynchronously.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws DirectoryNotFoundException if the directory could not be found.
     */
    void synchroniseCache(long directoryId, SynchronisationMode mode, boolean runInBackground)
            throws OperationFailedException, DirectoryNotFoundException;

    /**
     * Returns true if the given Directory is currently synchronising. This method should only be used to give an
     * indication regarding the synchronisation state in the UI and should not be used to control whether or not to
     * start another synchronisation.
     *
     * @param directoryId ID of directory.
     * @return true if the given Directory is currently synchronising, otherwise false.
     * @throws DirectoryInstantiationException if there was an error instantiating the directory
     * @throws DirectoryNotFoundException if the directory could not be found
     */
    boolean isSynchronising(long directoryId) throws DirectoryInstantiationException, DirectoryNotFoundException;

    /**
     * Retrieves the sync info for the directory - last sync start time &amp; duration, current sync start time (if directory is currently synchronising)
     *
     * @param directoryId ID of directory
     * @return a DirectorySynchronisationInformation object that contains the synchronisation information for the directory.
     *      null if the RemoteDirectory is not an instance of SynchronisableDirectory
     * @throws DirectoryInstantiationException if there was an error instantiating the directory
     * @throws DirectoryNotFoundException if the directory could not be found.
     */
    DirectorySynchronisationInformation getDirectorySynchronisationInformation(long directoryId)
            throws DirectoryInstantiationException, DirectoryNotFoundException;
}
