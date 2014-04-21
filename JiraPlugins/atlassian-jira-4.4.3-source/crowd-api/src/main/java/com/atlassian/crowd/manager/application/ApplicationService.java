package com.atlassian.crowd.manager.application;

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

/**
 * The exposed service provided by Crowd to client applications.
 * <p/>
 * This class performs amalgamation across the active directories assigned
 * to client applications (providing a unified view of their
 * directories).
 */
public interface ApplicationService
{
    ///////////////////// USER OPERATIONS /////////////////////

    /**
     * Will attempt to authenticate the given user against the application.
     *
     * The logic should start by going through directories defined in the application one by one, trying to authenticate user against each directory.
     * When an authentication against directory is positive, the method returns the user. Otherwise, an exception indicating the reason will be thrown.
     *
     * In case that some of the underlying directories cannot perform the operation at the time of authentication, either by technical failures or
     * the application not having the right permission to query the directory, as indicated by {@link OperationFailedException} being thrown,
     * the authentication logic will skip those directories, instead relying on the operative ones, in the order defined in the application's directory mappings.
     * However, if the user, still, cannot be authenticated against any remaining directories, we suspect one of the bad directories must have held
     * the user account, in which case {@link OperationFailedException}, which indicates the underlying cause of the first failing directory,
     * will be thrown from this method.
     *
     * @param application the application to authenticate against
     * @param username the username to authenticate against
     * @param passwordCredential the password to use for authentication
     * @return A user if the user can successfully authenticate.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws ExpiredCredentialException if the users credentials have expired
     * @throws InactiveAccountException if the users account is marked as inactive
     * @throws InvalidAuthenticationException if authentication with the provided credentials failed, or potentially the user does not exist.
     */
    User authenticateUser(Application application, String username, PasswordCredential passwordCredential) throws OperationFailedException, InactiveAccountException, InvalidAuthenticationException, ExpiredCredentialException, UserNotFoundException;

    /**
     * Returns <tt>true</tt> if the user is authorised to authenticate with the application. If the user could not be
     * found, then <tt>false</tt> is returned.
     *
     * <p/>
     * For a user to have access to an application:
     * <ol>
     *   <li>the Application must be active.</li>
     *   <li>and either:
     *     <ul>
     *       <li>the User is stored in a directory which is associated to the Application and the &quot;allow all to authenticate&quot;
     *           flag is true.</li>
     *       <li>the User is a member of a Group that is allowed to authenticate with the Application and both the User and
     *           Group are from the same RemoteDirectory.</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * @param application application user is authenticating against
     * @param username username
     * @return <tt>true</tt> if the user is authorised to authenticate with the application, otherwise <tt>false</tt>.
     *          If the user could not be found, <tt>false</tt> will be returned.
     */
    boolean isUserAuthorised(Application application, String username);

    /**
     * Adds the user to THE FIRST permissible active directory.
     * <p/>
     * If no directories have CREATE_USER permission, an {@link ApplicationPermissionException} is thrown.
     * <p/>
     *
     * @param application add to application's assigned directories.
     * @param users the users to add.
     *
     * @throws ApplicationPermissionException thrown when no CREATE USER permission for any of the directories.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws BulkAddFailedException throw when it failed to create a user in of the directories.
     */
    void addAllUsers(Application application, final Collection<UserTemplateWithCredentialAndAttributes> users)
            throws ApplicationPermissionException, OperationFailedException, BulkAddFailedException;

    /**
     * Returns the first user with the matching username
     * from all the active directories assigned to the application.
     * <p/>
     * The directories are searched in the order they are
     * assigned to the application.
     * @param application search application's assigned directories.
     * @param name the username of the user to find.
     * @return first matching user.
     * @throws UserNotFoundException user not found in any of the directories.
     */
    User findUserByName(Application application, String name) throws UserNotFoundException;

    /**
     * Returns the first user with the matching username
     * from all the active directories assigned to the application.
     * <p/>
     * The directories are searched in the order they are
     * assigned to the application.
     * @param application search application's assigned directories.
     * @param name the username of the user to find.
     * @return first matching user.
     * @throws UserNotFoundException user not found in any of the directories.
     */
    UserWithAttributes findUserWithAttributesByName(Application application, String name) throws UserNotFoundException;

    /**
     * Adds the user to the <b>first</b> permissible active directory.
     * <p/>
     * If the user exists in ANY of the application's active assigned
     * directories, then an {@link com.atlassian.crowd.exception.InvalidUserException} will be thrown.
     * <p/>
     * If the add operation fails on the directory because of permission restrictions, an ApplicationPermissionException is thrown
     * If ALL directories permissions fail, an
     * {@link com.atlassian.crowd.exception.ApplicationPermissionException} is thrown.
     * <p/>
     * If the add operation fails on a directory for any other reason,
     * such as directory failure, update failure, etc., an Exception
     * is thrown immediately.
     * <p/>
     * Returns the added user from the directory operation.
     *
     * @param application add to application's assigned directories.
     * @param user a template of the user to be added. The directoryId of the UserTemplate is ignored, and will be mutated for each directoryMapping.
     * @param credential the password credential of the user (unencrypted).
     * @return the added user returned from {@link #findUserByName(com.atlassian.crowd.model.application.Application , String)}.
     * @throws InvalidCredentialException if the user's credential does not meet the validation requirements for an associated directory.
     * @throws InvalidUserException if the user already exists in ANY associated directory or the user template does not have the required properties populated.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws ApplicationPermissionException if none of the application's associated directories are allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#CREATE_USER}.
     */
    User addUser(Application application, UserTemplate user, PasswordCredential credential)
            throws InvalidUserException, OperationFailedException, InvalidCredentialException, ApplicationPermissionException;

    /**
     * Updates the user in the first active directory the User belongs.
     * <p/>
     * If the user does not exist in ANY of the application's active assigned
     * directories, then a {@link UserNotFoundException} will be thrown.
     * <p/>
     * If the update operation is not allowed on the User's directory, an
     * {@link com.atlassian.crowd.exception.ApplicationPermissionException} is thrown.
     * <p/>
     * If the update operation fails on a directory for any other reason,
     * such as directory failure, update failure, etc., an Exception
     * is thrown immediately.
     * <p/>
     * Returns the updated User.
     *
     * @param application application with assigned directories to operate on.
     * @param user a template of the user to be added. The directoryId of the UserTemplate is ignored, and directories searched for the given username.
     * @return the updated User.
     * @throws InvalidUserException if the user template does not have the required properties populated.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws ApplicationPermissionException if the User's directory is not allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_USER}.
     * @throws UserNotFoundException user does not exist in any of the associated active directories of the application.
     */
    User updateUser(Application application, UserTemplate user) throws InvalidUserException, OperationFailedException, ApplicationPermissionException, UserNotFoundException;

    /**
     * Updates the credentials of the first matching user from all the active directories assigned to the application.
     * <p/>
     * Thus, the method only operates on the same user returned
     * from a call to findUserByName.
     * @param application update in application's assigned directories.
     * @param username name of user.
     * @param credential new (unencrypted) credentials.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws InvalidCredentialException if the user's credential does not meet the validation requirements for an associated directory.
     * @throws ApplicationPermissionException if the first directory in which the user is found doesn't have the permission to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_USER}.
     * @throws UserNotFoundException if no user with the given name exists in ANY assigned directory.
     */
    void updateUserCredential(Application application, String username, PasswordCredential credential) throws OperationFailedException, UserNotFoundException, InvalidCredentialException, ApplicationPermissionException;


    /**
     * Resets the credentials of the first matching user in
     * the application's active directories.
     * <p/>
     * Thus, the method only operates on the same user returned
     * from a call to findUserByName.
     * <p/>
     * This method will only function correctly if the User has an email address, otherwise
     * no email can be sent to the user.
     * @param application update in application's assigned directories.
     * @param username name of user.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws InvalidCredentialException if the generated credential does not meet the validation requirements for an associated directory.
     * @throws ApplicationPermissionException if the first directory in which the user is found doesn't have the permission to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_USER}.
     * @throws UserNotFoundException if no user with the given name exists in ANY assigned directory
     * @throws InvalidEmailAddressException if the user doesn't have a valid email address and can't be notified.
     * @deprecated since 2.1
     */
    void resetUserCredential(Application application, String username) throws OperationFailedException, UserNotFoundException, InvalidCredentialException, ApplicationPermissionException, InvalidEmailAddressException;


    /**
     * Adds or updates a user's attributes with the new Map of attribute values for the first active directory containing this username.
     * <p/>
     * The attributes map represents new or updated attributes and does not replace existing attributes unless the key of an attribute
     * matches the key of an existing attribute on the user.
     * <p/>
     * This method does not update primary field attributes like firstName, lastName, etc.
     * <p/>
     * If the user does not exist in ANY of the application's assigned
     * directories, then a {@link UserNotFoundException} will be thrown.
     * <p/>
     * If the directory does not have UPDATE_USER permission, an
     * {@link com.atlassian.crowd.exception.ApplicationPermissionException} is thrown.
     * <p/>
     * If the update operation fails on a directory for any other reason,
     * such as directory failure, update failure, etc., an Exception
     * is thrown immediately.
     * <p/>
     * @param application application with assigned directories to operate on.
     * @param username username of the user to update.
     * @param attributes map of one-to-many attribute-values. All attribute keys are treated as new or updated attributes.
     * @throws UserNotFoundException if the user with the supplied username does not exist in ANY assigned directory.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws ApplicationPermissionException if the User's directory does not have permission to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_USER_ATTRIBUTE}.
     */
    public void storeUserAttributes(Application application, String username, Map<String, Set<String>> attributes) throws OperationFailedException, ApplicationPermissionException, UserNotFoundException;

    /**
     * Removes a user's attribute values for the first active directory containing this username.
     * <p/>
     * If the user does not exist in ANY of the application's assigned
     * directories, then a {@link UserNotFoundException} will be thrown.
     * <p/>
     * If the directory does not have UPDATE_USER permission, an
     * {@link com.atlassian.crowd.exception.ApplicationPermissionException} is thrown.
     * <p/>
     * If the update operation fails on a directory for any other reason,
     * such as directory failure, update failure, etc., an Exception
     * is thrown immediately.
     * <p/>
     * @param application application with assigned directories to operate on.
     * @param username username of the user to update.
     * @param attributeName all attribute values for this key will be removed from the user.
     * @throws UserNotFoundException if the user with the supplied username does not exist in ANY assigned directory.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws ApplicationPermissionException if the User's directory does not have permission to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_USER_ATTRIBUTE}.
     */
    public void removeUserAttributes(Application application, String username, String attributeName) throws OperationFailedException, ApplicationPermissionException, UserNotFoundException;

    /**
     * Removes the user from the first active directory they are found in.
     * <p/>
     * If the user does not exist in ANY of the application's assigned directories, then a {@link UserNotFoundException}
     * will be thrown.
     * <p/>
     * If the remove operation fails because of permission restrictions, an ApplicationPermissionException is thrown.
     * <p/>
     *
     * @param application remove from application's assigned directories.
     * @param user the name of the user to remove.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws UserNotFoundException if user with given name does not exist in ANY assigned directory.
     * @throws ApplicationPermissionException if the User's directory does not have permission to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#DELETE_USER}.
     */
    void removeUser(Application application, String user) throws OperationFailedException, UserNotFoundException, ApplicationPermissionException;

    /**
     * Returns a List<User> or List<String> matching the search criteria defined in the query
     * for ALL of the active directories assigned to the application.
     * @param application search application's assigned directories.
     * @param query the search query.
     * @return List<User> user objects or List<String> usernames, depending on the query.
     */
    <T> List<T> searchUsers(Application application, EntityQuery<T> query);

    /**
     * Returns a List<User> matching the search criteria defined in the query for ALL of the active directories assigned
     * to the application.  Users with duplicate user names but different directory IDs can be returned.
     *
     * @param application search application's assigned directories.
     * @param query the search query.
     * @return List<User> user objects.
     */
    List<User> searchUsersAllowingDuplicateNames(Application application, EntityQuery<User> query);


    ///////////////////// GROUP OPERATIONS /////////////////////

    /**
     * Returns the first group with the matching groupname
     * from all the active directories assigned to the application.
     * <p/>
     * The directories are searched in the order they are
     * assigned to the application.
     * @param application search application's assigned directories.
     * @param name the groupname of the group to find.
     * @return first matching group.
     * @throws GroupNotFoundException group not found in any of the directories.
     */
    Group findGroupByName(Application application, String name) throws GroupNotFoundException;

    /**
     * Returns the first group with the matching groupname
     * from all the active directories assigned to the application.
     * <p/>
     * The directories are searched in the order they are
     * assigned to the application.
     * @param application search application's assigned directories.
     * @param name the groupname of the group to find.
     * @return first matching group.
     * @throws GroupNotFoundException group not found in any of the directories.
     */
    GroupWithAttributes findGroupWithAttributesByName(Application application, String name) throws GroupNotFoundException;

    /**
     * Adds the group to ALL the active permissible directories.
     * <p/>
     * If the group exists in ANY of the application's active assigned
     * directories, then an {@link com.atlassian.crowd.exception.InvalidGroupException} will be thrown.
     * <p/>
     * If the add operation fails on a directory because of
     * permissioning restrictions, an INFO message is logged.
     * If ALL directories permissions fail, an
     * {@link com.atlassian.crowd.exception.ApplicationPermissionException} is thrown.
     * <p/>
     * If the add operation fails on a directory for any other reason,
     * such as directory failure, update failure, etc., an Exception
     * is thrown immediately.
     * <p/>
     * Returns the group from the first directory containing
     * the group.
     * @param application add to application's assigned directories.
     * @param group a template of the group to be added. The directoryId of the GroupTemplate is ignored, and will be mutated for each directoryMapping.
     * @return the added group returned from {@link #findGroupByName(com.atlassian.crowd.model.application.Application , String)}.
     * @throws InvalidGroupException if the group already exists in ANY associated directory or the group template does not have the required properties populated.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws ApplicationPermissionException if none of the application's associated directories are allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#CREATE_GROUP}.
     */
    Group addGroup(Application application, GroupTemplate group)
            throws InvalidGroupException, OperationFailedException, ApplicationPermissionException;

    /**
     * Updates the group in ALL the active permissible directories.
     * <p/>
     * If the group does not exist in ANY of the application's assigned
     * directories, then a {@link GroupNotFoundException} will be thrown.
     * <p/>
     * If the update operation fails on a directory because of
     * permissioning restrictions, an INFO message is logged.
     * If ALL directories permissions fail, an
     * {@link com.atlassian.crowd.exception.ApplicationPermissionException} is thrown.
     * <p/>
     * If the update operation fails on a directory for any other reason,
     * such as directory failure, update failure, etc., an Exception
     * is thrown immediately.
     * <p/>
     * Returns the group from the first directory containing
     * the group.
     * @param application application with assigned directories to operate on.
     * @param group a template of the group to be added. The directoryId of the GroupTemplate is ignored, and will be mutated for each directoryMapping.
     * @return the added group returned from {@link #findGroupByName(com.atlassian.crowd.model.application.Application , String)}.
     * @throws InvalidGroupException if the group already exists in ANY associated directory or the group template does not have the required properties populated.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws ApplicationPermissionException if none of the application's associated directories are allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP}.
     * @throws GroupNotFoundException group does not exist in any of the associated directories of the application.
     */
    Group updateGroup(Application application, GroupTemplate group) throws InvalidGroupException, OperationFailedException, ApplicationPermissionException, GroupNotFoundException;

    /**
     * Adds or updates a group's attributes with the new Map of attribute values for all active permissible directories assigned to the application.
     * <p/>
     * The attributes map represents new or updated attributes and does not replace existing attributes unless the key of an attribute
     * matches the key of an existing attribute on the group.
     * <p/>
     * This method does not update primary field attributes like firstName, lastName, etc.
     * <p/>
     * If the group does not exist in ANY of the application's assigned
     * directories, then a {@link GroupNotFoundException} will be thrown.
     * <p/>
     * If the update operation fails on a directory because of
     * permissioning restrictions, an INFO message is logged.
     * If ALL directories permissions fail, an
     * {@link com.atlassian.crowd.exception.ApplicationPermissionException} is thrown.
     * <p/>
     * If the update operation fails on a directory for any other reason,
     * such as directory failure, update failure, etc., an Exception
     * is thrown immediately.
     * <p/>
     * @param application application with assigned directories to operate on.
     * @param groupname groupname of the group to update.
     * @param attributes map of one-to-many attribute-values. All attribute keys are treated as new or updated attributes.
     * @throws GroupNotFoundException if the group with the supplied groupname does not exist in ANY assigned directory.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws ApplicationPermissionException if none of the application's associated directories are allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP_ATTRIBUTE}.
     */
    public void storeGroupAttributes(Application application, String groupname, Map<String, Set<String>> attributes) throws OperationFailedException, ApplicationPermissionException, GroupNotFoundException;

    /**
     * Removes a group's attribute values for all active permissible directories assigned to the application.
     * <p/>
     * If the group does not exist in ANY of the application's assigned
     * directories, then a {@link GroupNotFoundException} will be thrown.
     * <p/>
     * If the update operation fails on a directory because of
     * permissioning restrictions, an INFO message is logged.
     * If ALL directories permissions fail, an
     * {@link com.atlassian.crowd.exception.ApplicationPermissionException} is thrown.
     * <p/>
     * If the update operation fails on a directory for any other reason,
     * such as directory failure, update failure, etc., an Exception
     * is thrown immediately.
     * <p/>
     * @param application application with assigned directories to operate on.
     * @param groupname groupname of the group to update.
     * @param attributeName all attribute values for this key will be removed from the group.
     * @throws GroupNotFoundException if the group with the supplied groupname does not exist in ANY assigned directory.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws ApplicationPermissionException if none of the application's associated directories are allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP_ATTRIBUTE}.
     */
    public void removeGroupAttributes(Application application, String groupname, String attributeName) throws OperationFailedException, ApplicationPermissionException, GroupNotFoundException;

    /**
     * Removes ALL groups from each of the application's assigned
     * directories that are active.
     * <p/>
     * If the group doesn't exist in ANY of the application's assigned
     * directories that are active, then a GroupNotFoundException will be thrown.
     * <p/>
     * If the remove operation fails on a directory because of
     * permissioning restrictions, an INFO message is logged.
     * If ALL directories permissions fail, a
     * ApplicationPermissionException is thrown.
     * <p/>
     * If the remove operation fails on a directory for any other reason,
     * such as directory failure, update failure, etc., an Exception
     * is thrown immediately.
     * @param application remove from application's assigned directories.
     * @param group the name of the group to remove.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws GroupNotFoundException if group with given name does not exist in ANY assigned directory.
     * @throws ApplicationPermissionException if none of the application's associated directories are allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#DELETE_GROUP}.
     */
    void removeGroup(Application application, String group) throws OperationFailedException, GroupNotFoundException, ApplicationPermissionException;

    /**
     * Returns a List<Group> matching the search criteria defined in the query
     * for ALL of the active directories assigned to the application.
     * @param application search application's assigned directories.
     * @param query the search query.
     * @return List<Group> group objects or List<String> groupnames, depending on the query.
     */
    <T> List<T> searchGroups(Application application, EntityQuery<T> query);


    ///////////////////// MEMBERSHIP OPERATIONS /////////////////////

    /**
     * Makes the primary user of the given username a direct member of the group on the directory where the primary user resides.
     * <p>
     * A user exists in one individual directory, however a group is thought to "span" all directories (users from different
     * directories can belong to the same group).
     * With this in mind, if the group does not exist in the User's directory (but does already exist), then this method
     * will attempt to automatically add the group to that directory for you.
     * 
     * @param application modify groups in application's assigned directories.
     * @param username username of the user.
     * @param groupName name of the group.
     * @throws UserNotFoundException when the user cannot be found in ANY directory
     * @throws GroupNotFoundException when the group cannot be found in ANY directory
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws ApplicationPermissionException if the application's directory where the primary user resides does not allow operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP} or the group is readonly.
     */
    void addUserToGroup(Application application, String username, String groupName) throws OperationFailedException, UserNotFoundException, GroupNotFoundException, ApplicationPermissionException;

    /**
     * Makes groups matching the given name (childGroupName) direct members of the group (parentGroupName) across ALL active directories.
     *
     * @param application modify groups in the application's assigned directories.
     * @param childGroupName name of child group.
     * @param parentGroupName name of parent group.
     * @throws GroupNotFoundException when the parent or child group do not exist
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws ApplicationPermissionException if we were unable to create the membership in any directory. This is based on Edit permissions, Create permissions, and whether Nested Groups is supported by the individual directories.
     * @throws InvalidMembershipException The child and parent are of different group types or would cause a circular reference.
     */
    void addGroupToGroup(Application application, String childGroupName, String parentGroupName)
            throws OperationFailedException, GroupNotFoundException, ApplicationPermissionException, InvalidMembershipException;

    /**
     * Makes the primary user of the given username no longer a member of the group on the directory where the primary user resides.
     *
     * @param application modify groups in application's assigned directories.
     * @param username username of the user.
     * @param groupName name of the group.
     * @throws UserNotFoundException when the user cannot be found in ANY directory
     * @throws GroupNotFoundException when the group does not exist in the directory where the primary user resides.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws ApplicationPermissionException if the application's directory where the primary user resides does not allow operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP}.
     * @throws MembershipNotFoundException if the user is not a direct member of the group in an assigned directory.
     */
    void removeUserFromGroup(Application application, String username, String groupName) throws OperationFailedException, GroupNotFoundException, UserNotFoundException, ApplicationPermissionException, MembershipNotFoundException;

    /**
     * Makes child group matching the given name not members of the parent group across ALL active directories.
     * @param application modify groups in application's assigned directories.
     * @param childGroup name of child group.
     * @param parentGroup name of parent group.
     * @throws GroupNotFoundException when the child group cannot be found in ANY directory OR when ALL child groups are in directories which don't have the requested parent group.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws ApplicationPermissionException if none of the application's associated directories are allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP}.
     * @throws MembershipNotFoundException if the user is not a direct member of the group in an assigned directory.
     */
    void removeGroupFromGroup(Application application, String childGroup, String parentGroup) throws OperationFailedException, GroupNotFoundException, ApplicationPermissionException, MembershipNotFoundException;

    /**
     * Returns <code>true</code> if the user is a direct member of the group in the directory of the first user found with the specified username.
     * @param application search groups in application's assigned directories.
     * @param username name of the user to inspect.
     * @param groupName name of the group to inspect.
     * @return <code>true</code> if and only if the user is a direct member of the group. If the group or user does not exist in any directory, <code>false</code> is returned.
     */
    boolean isUserDirectGroupMember(Application application, String username, String groupName);

    /**
     * Returns <code>true</code> if the childGroup is a direct member of the parentGroup in any of the application's assigned directories.
     * @param application search groups in application's assigned directories.
     * @param childGroup name of the group to inspect.
     * @param parentGroup name of the group to inspect.
     * @return <code>true</code> if and only if the childGroup is a direct member of the parentGroup. If either group does not exist in any directory, <code>false</code> is returned.
     */
    boolean isGroupDirectGroupMember(Application application, String childGroup, String parentGroup);

    /**
     * Returns <code>true</code> if the user is a direct or indirect (nested) member of the group in the directory of the first user found with the specified username.
     * <p/>
     * If the directory does not support nested groups, this call will be equivalent to {@link com.atlassian.crowd.manager.directory.DirectoryManager#isUserDirectGroupMember(long, String, String)}.
     * <p/>
     * <b>WARNING: this method could be very slow if the underlying RemoteDirectory does not employ caching.</b>
     * <p/>
     * See CWD-1485 for explanation of logic in amalgamation.
     * <p/>
     * Nesting is <b>not</b> resolved across directories.
     * 
     * @param application search groups in application's assigned directories.
     * @param username name of the user to inspect.
     * @param groupName name of the group to inspect.
     * @return <code>true</code> if and only if the user is a direct or indirect (nested) member of the group. If the group or user does not exist in the directory, <code>false</code> is returned.
     */
    boolean isUserNestedGroupMember(Application application, String username, String groupName);

    /**
     * Returns <code>true</code> if the childGroup is a direct or indirect (nested) member of the parentGroup in any of the application's active assigned directories.
     * <p/>
     * If the directory does not support nested groups, this call will be equivalent to {@link com.atlassian.crowd.manager.directory.DirectoryManager#isGroupDirectGroupMember(long, String, String)}.
     * <p/>
     * <b>WARNING: this method could be very slow if the underlying RemoteDirectory does not employ caching.</b>
     * <p/>
     * See CWD-1485 for explanation of logic in amalgamation.
     * <p/>
     * Nesting is <b>not</b> resolved across directories.
     *
     * @param application search groups in application's assigned directories.
     * @param childGroup name of the user to inspect.
     * @param parentGroup name of the group to inspect.
     * @return <code>true</code> if and only if the childGroup is a direct or indirect (nested) member of the parentGroup. If either group does not exist in the directory, <code>false</code> is returned.
     */
    boolean isGroupNestedGroupMember(Application application, String childGroup, String parentGroup);

    /**
     * Searches for direct group relationships in any of the application's active assigned directories.
     * When searching for the groups a user is a member of only the directory of the user (as determined by findUserByName)
     * is searched.  When searching for memberships of a group or groups a group is a member of all directories are searched and the results amalgamated.
     * @param application search groups in application's assigned directories.
     * @param query membership query.
     * @return List of {@link com.atlassian.crowd.model.user.User} entities,
     *         {@link com.atlassian.crowd.model.group.Group} entities,
     *         {@link String} usernames or {@link String} group names matching the query criteria.
     */
    <T> List<T> searchDirectGroupRelationships(Application application, MembershipQuery<T> query);

    /**
     * Searches for direct and indirect (nested) group relationships in any of the application's active assigned directories.
     * <p/>
     * If the directory does not support nested groups, this call will be equivalent to {@link com.atlassian.crowd.manager.directory.DirectoryManager#searchDirectGroupRelationships(long, com.atlassian.crowd.search.query.membership.MembershipQuery)}.
     * <p/>
     * <b>WARNING: this method could be very slow if the underlying RemoteDirectory does not employ caching.</b>
     * <p/>
     * When searching for the groups a user is a member of only the directory of the user (as determined by findUserByName)
     * is searched.  When searching for memberships of a group or groups a group is a member of all directories are searched and the results amalgamated.
     * @param application search groups in application's assigned directories.
     * @param query membership query.
     * @return List of {@link com.atlassian.crowd.model.user.User} entities,
     *         {@link com.atlassian.crowd.model.group.Group} entities,
     *         {@link String} usernames or {@link String} group names matching the query criteria.
     */
    <T> List<T> searchNestedGroupRelationships(Application application, MembershipQuery<T> query);

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
     * @since 2.3
     */
    String getCurrentEventToken();

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
     * @param application return events visible to application
     * @param eventToken event token that was retrieved by a call to {@link #getCurrentEventToken()} or this method
     * @return events object which contains a new eventToken and events that happened after the given {@code eventToken} was generated
     * @throws EventTokenExpiredException if events that happened after the event token was generated can not be retrieved
     * @throws IncrementalSynchronisationNotAvailableException if the application cannot provide incremental synchronisation
     * @throws OperationFailedException if the operation has failed for any other reason, including invalid arguments
     * @since 2.3
     */
    Events getNewEvents(Application application, String eventToken) throws EventTokenExpiredException, OperationFailedException, IncrementalSynchronisationNotAvailableException;
}
