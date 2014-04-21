package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.api.Attributes;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.model.group.Membership;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gateway to perform operations on the physical remote directory.
 * <p/>
 * Implementations will be provided an directoryId and Map of attributes.
 * <p/>
 * Implementations of <tt>RemoteDirectory</tt> may throw an <tt>OperationNotSupportedException</tt>, if the operation is
 * not supported, and the method declares that it may throw an <tt>OperationFailedException</tt>. Implementations should
 * not knowingly throw a RuntimeException unless it really is a programming error - e.g. attempting to search for users
 * using a group query.
 */
public interface RemoteDirectory extends Attributes
{
    /**
     * Gets the internal unique <code>directoryId</code> of the directory store.
     *
     * @return The <code>directoryId</code>.
     */
    long getDirectoryId();

    /**
     * When a directory store is loaded, the <code>directoryId</code> will be set by the
     * crowd framework.
     *
     * @param directoryId The unique <code>directoryId</code> of the {@link com.atlassian.crowd.model.directory.DirectoryImpl} stored in the database.
     */
    void setDirectoryId(long directoryId);

    /**
     * Returns a descriptive name for the type of directory.
     *
     * @return descriptive name.
     */
    String getDescriptiveName();

    /**
     * When a directory store is loaded, the attributes map will be
     * set by the Crowd framework. Implementations may store a reference
     * to this map in order to implement the Attributes
     * <p/>
     * The Map is immutable and implementations are required to
     * maintain immutability.
     *
     * @param attributes attributes map.
     */
    void setAttributes(Map<String, String> attributes);


    ////////////// USERS ////////////////

    /**
     * Finds the {@link com.atlassian.crowd.model.user.User user} that matches the supplied <code>name</code>.
     *
     * @param name the name of the user (username).
     * @return user entity.
     * @throws UserNotFoundException    a user with the supplied name does not exist.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    User findUserByName(String name) throws UserNotFoundException, OperationFailedException;

    /**
     * Finds the {@link com.atlassian.crowd.model.user.UserWithAttributes user} that matches the supplied <code>name</code>.
     *
     * @param name the name of the user (username).
     * @return user entity with attributes.
     * @throws UserNotFoundException    a user with the supplied name does not exist.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    UserWithAttributes findUserWithAttributesByName(String name) throws UserNotFoundException, OperationFailedException;

    /**
     * Authenticates a {@link com.atlassian.crowd.model.user.User user} with the directory store.
     *
     * @param name       The name of the user (username).
     * @param credential The supplied credentials (password).
     * @return The populated user if the authentication is valid.
     * @throws com.atlassian.crowd.exception.InactiveAccountException
     *                                    The supplied user is inactive.
     * @throws com.atlassian.crowd.exception.InvalidAuthenticationException
     *                                    Authentication with the provided credentials failed.
     * @throws UserNotFoundException      The user wth the supplied name does not exist.
     * @throws ExpiredCredentialException The user's credentials have expired. The user must change their credentials in order to successfully authenticate.
     * @throws OperationFailedException   underlying directory implementation failed to execute the operation.
     */
    User authenticate(String name, PasswordCredential credential) throws UserNotFoundException, InactiveAccountException, InvalidAuthenticationException, ExpiredCredentialException, OperationFailedException;

    /**
     * Adds a {@link com.atlassian.crowd.model.user.User user} to the directory store.
     *
     * @param user       template of the user to add.
     * @param credential password. May be null, since JIRA creates a user in two steps (user THEN password)
     * @return the added user retrieved from the underlying store.
     * @throws InvalidUserException       The supplied user is invalid.
     * @throws InvalidCredentialException The supplied credential is invalid.
     * @throws UserAlreadyExistsException The user already exists
     * @throws OperationFailedException   underlying directory implementation failed to execute the operation.
     */
    User addUser(UserTemplate user, PasswordCredential credential)
            throws InvalidUserException, InvalidCredentialException, UserAlreadyExistsException, OperationFailedException;

    /**
     * Updates the {@link com.atlassian.crowd.model.user.User user}.
     *
     * @param user The user to update.
     * @return the updated user retrieved from the underlying store.
     * @throws UserNotFoundException    the user does not exist in the directory store.
     * @throws InvalidUserException     the supplied user is invalid.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    User updateUser(UserTemplate user) throws InvalidUserException, UserNotFoundException, OperationFailedException;

    /**
     * Updates the {@link com.atlassian.crowd.embedded.api.PasswordCredential password} for a {@link com.atlassian.crowd.model.user.User user}.
     *
     * @param username   The name of the user (username).
     * @param credential The new credential (password).
     * @throws UserNotFoundException      The user does not exist.
     * @throws InvalidCredentialException The supplied credential is invalid.
     * @throws OperationFailedException   underlying directory implementation failed to execute the operation.
     */
    void updateUserCredential(String username, PasswordCredential credential) throws UserNotFoundException, InvalidCredentialException, OperationFailedException;

    /**
     * Renames a {@link com.atlassian.crowd.model.user.User user}.
     *
     * @param oldName name of existing user.
     * @param newName desired name of user.
     * @return renamed user.
     * @throws UserNotFoundException      if the user with the existing name does not exist.
     * @throws InvalidUserException       if the new username is invalid.
     * @throws UserAlreadyExistsException if the newName already exists.
     * @throws OperationFailedException   if the underlying directory implementation failed to execute the operation.
     */
    User renameUser(String oldName, String newName)
            throws UserNotFoundException, InvalidUserException, UserAlreadyExistsException, OperationFailedException;

    /**
     * Adds or updates a user's attributes with the new Map of attribute values in the directory specified by the passed in <code>directoryId</code>.
     * <p/>
     * The attributes map represents new or updated attributes and does not replace existing attributes unless the key of an attribute
     * matches the key of an existing
     * <p/>
     * Attributes with values of empty sets are not added (these attributes are effectively removed).
     *
     * @param username   name of user to update.
     * @param attributes new or updated attributes (attributes that don't need changing should not appear in this Map).
     * @throws UserNotFoundException    user with supplied username does not exist.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void storeUserAttributes(String username, Map<String, Set<String>> attributes) throws UserNotFoundException, OperationFailedException;

    /**
     * Removes all the values for a single attribute key for a user. If the attribute key does not exist nothing will happen.
     *
     * @param username      name of the user to update.
     * @param attributeName name of attribute to remove.
     * @throws UserNotFoundException    user with supplied username does not exist.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void removeUserAttributes(String username, String attributeName) throws UserNotFoundException, OperationFailedException;

    /**
     * Removes the {@link com.atlassian.crowd.model.user.User user} that matches the supplied <code>name</code>.
     *
     * @param name The name of the user (username).
     * @throws UserNotFoundException    The user does not exist.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void removeUser(String name) throws UserNotFoundException, OperationFailedException;

    /**
     * Searches for {@link com.atlassian.crowd.model.user.User users} that match the supplied query criteria.
     *
     * @param query EntityQuery for Entity.USER.
     * @return <code>List<{@link com.atlassian.crowd.model.user.User}></code> or <code>List<{@link String}></code> of users/usernames
     *         matching the search criteria. An empty <code>List</code> will be returned
     *         if no users matching the criteria are found.
     * @throws OperationFailedException if the underlying directory implementation failed to execute the operation
     * @throws IllegalArgumentException if the query is not a valid user query
     */
    <T> List<T> searchUsers(EntityQuery<T> query) throws OperationFailedException;


    ////////////// GROUPS ////////////////

    /**
     * Finds the {@link com.atlassian.crowd.model.group.Group group} that matches the supplied <code>name</code>.
     *
     * @param name the name of the group.
     * @return group entity.
     * @throws GroupNotFoundException   a group with the supplied name does not exist.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    Group findGroupByName(String name) throws GroupNotFoundException, OperationFailedException;

    /**
     * Finds the {@link com.atlassian.crowd.model.group.GroupWithAttributes group} that matches the supplied <code>name</code>.
     *
     * @param name the name of the group.
     * @return group entity with attributes.
     * @throws GroupNotFoundException   a group with the supplied name does not exist.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    GroupWithAttributes findGroupWithAttributesByName(String name) throws GroupNotFoundException, OperationFailedException;

    /**
     * Adds a {@link com.atlassian.crowd.model.group.Group group} to the directory store.
     *
     * @param group template of the group to add.
     * @return the added group retrieved from the underlying store.
     * @throws InvalidGroupException    The supplied group is invalid.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    Group addGroup(GroupTemplate group) throws InvalidGroupException, OperationFailedException;

    /**
     * Updates the {@link com.atlassian.crowd.model.group.Group group}.
     *
     * @param group The group to update.
     * @return the updated group retrieved from the underlying store.
     * @throws GroupNotFoundException   the group does not exist in the directory store.
     * @throws InvalidGroupException    the supplied group is invalid.
     * @throws ReadOnlyGroupException   the group is read-only
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    Group updateGroup(GroupTemplate group)
            throws InvalidGroupException, GroupNotFoundException, ReadOnlyGroupException, OperationFailedException;

    /**
     * Renames a {@link com.atlassian.crowd.model.group.Group group}.
     *
     * @param oldName name of existing group.
     * @param newName desired name of group.
     * @return renamed group.
     * @throws GroupNotFoundException   if the group with the existing name does not exist.
     * @throws InvalidGroupException    if the new groupname is invalid or already exists in the directory.
     * @throws OperationFailedException if the underlying directory implementation failed to execute the operation.
     */
    Group renameGroup(String oldName, String newName) throws GroupNotFoundException, InvalidGroupException, OperationFailedException;

    /**
     * Adds or updates a group's attributes with the new Map of attribute values in the directory specified by the passed in <code>directoryId</code>.
     * <p/>
     * The attributes map represents new or updated attributes and does not replace existing attributes unless the key of an attribute
     * matches the key of an existing
     * <p/>
     * Attributes with values of empty sets are not added (these attributes are effectively removed).
     *
     * @param groupName  name of group to update.
     * @param attributes new or updated attributes (attributes that don't need changing should not appear in this Map).
     * @throws GroupNotFoundException   group with supplied groupName does not exist.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void storeGroupAttributes(String groupName, Map<String, Set<String>> attributes) throws GroupNotFoundException, OperationFailedException;

    /**
     * Removes all the values for a single attribute key for a group.
     *
     * @param groupName     name of the group to update.
     * @param attributeName name of attribute to remove.
     * @throws GroupNotFoundException   group with supplied groupName does not exist.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void removeGroupAttributes(String groupName, String attributeName) throws GroupNotFoundException, OperationFailedException;

    /**
     * Removes the {@link com.atlassian.crowd.model.group.Group group} that matches the supplied <code>name</code>.
     *
     * @param name The name of the group.
     * @throws GroupNotFoundException   The group does not exist.
     * @throws ReadOnlyGroupException   if the group is read-only and not allowed to be deleted.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void removeGroup(String name) throws GroupNotFoundException, ReadOnlyGroupException, OperationFailedException;

    /**
     * Searches for {@link com.atlassian.crowd.model.group.Group groups} that match the supplied query criteria.
     *
     * @param query EntityQuery for Entity.GROUP.
     * @return <code>List<Group></code> or <code>List<String></code> of groups/groupnames
     *         matching the search criteria. An empty <code>List</code> will be returned
     *         if no groups matching the criteria are found.
     * @throws OperationFailedException if the underlying directory implementation failed to execute the operation
     * @throws IllegalArgumentException if the query is not a valid group query
     */
    <T> List<T> searchGroups(EntityQuery<T> query) throws OperationFailedException;


    ////////////// MEMBERSHIPS ////////////////

    /**
     * Determines if a user is a direct member of a group.
     * The directory is NOT expected to resolve any transitive
     * group relationships.
     *
     * @param username  name of user.
     * @param groupName name of group.
     * @return <code>true</code> iff the user is a direct member of the group.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    boolean isUserDirectGroupMember(String username, String groupName) throws OperationFailedException;

    /**
     * Determines if a group is a direct member of another group.
     * The directory is NOT expected to resolve any transitive
     * group relationships.
     *
     * @param childGroup  name of child group.
     * @param parentGroup name of parent group.
     * @return <code>true</code> iff the childGroup is a direct member of the parentGroup.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    boolean isGroupDirectGroupMember(String childGroup, String parentGroup) throws OperationFailedException;

    /**
     * Adds a user as a member of a group. This means that all user members of <code>childGroup</code> will
     * appear as members of <code>parentGroup</code> to querying applications.
     *
     * @param username  The user that will become a member of <code>groupName</code>
     * @param groupName The group that will gain a new member.
     * @throws GroupNotFoundException   If the group cannot be found.
     * @throws UserNotFoundException    If the user cannot be found.
     * @throws ReadOnlyGroupException   If the group is read-only
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void addUserToGroup(String username, String groupName)
            throws GroupNotFoundException, UserNotFoundException, ReadOnlyGroupException, OperationFailedException;

    /**
     * Adds a group as a member of a parent group.
     *
     * @param parentGroup The group that will gain a new member
     * @param childGroup  The group that will become a member of <code>parentGroup</code>
     * @throws GroupNotFoundException     One or both of the groups cannot be found.
     * @throws InvalidMembershipException if the childGroup and parentGroup exist but are of different GroupTypes.
     * @throws ReadOnlyGroupException     if either of the groups are read-only
     * @throws OperationFailedException   underlying directory implementation failed to execute the operation.
     */
    void addGroupToGroup(String childGroup, String parentGroup)
            throws GroupNotFoundException, InvalidMembershipException, ReadOnlyGroupException, OperationFailedException;

    /**
     * Removes a user as a member of a group.
     *
     * @param groupName The group that will lose the member.
     * @param username  The user that will be removed from <code>parentGroup</code>
     * @throws GroupNotFoundException      If the group cannot be found.
     * @throws UserNotFoundException       If the user cannot be found.
     * @throws MembershipNotFoundException if the user is not a direct member of the group.
     * @throws ReadOnlyGroupException      if the group is read-only
     * @throws OperationFailedException    underlying directory implementation failed to execute the operation.
     */
    void removeUserFromGroup(String username, String groupName)
            throws GroupNotFoundException, UserNotFoundException, MembershipNotFoundException, ReadOnlyGroupException, OperationFailedException;

    /**
     * Removes a group as a member of a parent group.
     *
     * @param parentGroup The group that will lose the member.
     * @param childGroup  The group that will be removed from <code>parentGroup</code>
     * @throws GroupNotFoundException      One or both of the groups cannot be found.
     * @throws InvalidMembershipException  if the childGroup and parentGroup exist but are of different GroupTypes.
     * @throws MembershipNotFoundException if the childGroup is not a direct member of the parentGroup.
     * @throws ReadOnlyGroupException      if the groups are read-only
     * @throws OperationFailedException    underlying directory implementation failed to execute the operation.
     */
    void removeGroupFromGroup(String childGroup, String parentGroup)
            throws GroupNotFoundException, InvalidMembershipException, MembershipNotFoundException, ReadOnlyGroupException, OperationFailedException;

    /**
     * Searches for membership information.
     *
     * @param query query for memberships.
     * @return a List of Users or Groups or Strings depending on the query criteria. An empty List if there are no
     *         results.
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws IllegalArgumentException if the query is not a valid membership query
     */
    <T> List<T> searchGroupRelationships(MembershipQuery<T> query) throws OperationFailedException;

    ////////////// MISCELLANEOUS ////////////////

    /**
     * Test if a connection to the directory server can be established.
     *
     * @throws OperationFailedException underlying directory implementation failed to execute the operation.
     */
    void testConnection() throws OperationFailedException;

    /**
     * Return true if this directory supports inactive users and groups.
     * <p/>
     * Currently inactive users and groups are not supported for LDAP directories.
     *
     * @return true if the directory supports inactive users and groups
     */
    boolean supportsInactiveAccounts();

    /**
     * Allows us to only display nested-group related UI for directories that support it.
     *
     * @return true if the directory can handle having a group added to a group.
     */
    boolean supportsNestedGroups();

    /**
     * Expose whether the directory has roles disabled.  Always true for directory that don't allow disabling.
     *
     * @return true if the directory has roles manually disabled.
     */
    boolean isRolesDisabled();
    
   /**
     * <p>Get an iterable view of the available group memberships. This may be implemented as a single remote
     * call or separate calls, depending on the directory.</p>
     * <p>If there is a failure in the underlying retrieval, the iterator may throw
     * {@link Membership.MembershipIterationException} at runtime.</p>
     * <p>If the directory does not have a bulk call interface then a typical implementation would be:</p>
     * <pre>
     * {@code
     * return new DirectoryMembershipsIterable(this);
     * }
     * </pre>
     *
     * @return an iterable view of the available group memberships
     * @throws OperationFailedException if the underlying directory implementation failed to execute the operation
     */
    Iterable<Membership> getMemberships() throws OperationFailedException;

    /**
     * @return the directory that is the authoritative source of data for this directory, possibly itself.
     */
    RemoteDirectory getAuthoritativeDirectory();

}
