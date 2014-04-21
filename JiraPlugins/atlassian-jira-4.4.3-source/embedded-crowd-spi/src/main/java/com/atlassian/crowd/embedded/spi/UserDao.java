package com.atlassian.crowd.embedded.spi;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.UserAlreadyExistsException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.user.TimestampedUser;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.util.BatchResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Responsible for the persistence and retrieval of {@link User}s, {@link PasswordCredential}s and user attributes.
 */
public interface UserDao
{
    /**
     * Finds and returns the user with the given name and directory ID.
     *
     * @throws UserNotFoundException if the user could not be found
     */
    TimestampedUser findByName(long directoryId, String userName) throws UserNotFoundException;

    /**
     * Finds and returns the user with attributes with the given name and directory ID.
     *
     * @throws UserNotFoundException if the user could not be found
     */
    UserWithAttributes findByNameWithAttributes(long directoryId, String userName) throws UserNotFoundException;

    /**
     * Returns the credential for the given user. It will always be encrypted.
     *
     * @throws UserNotFoundException if the user could not be found
     */
    PasswordCredential getCredential(long directoryId, String userName) throws UserNotFoundException;

    /**
     * Returns the previous credentials for the given user, starting with the oldest. May be an empty list
     * if there are no historical credentials.
     *
     * @throws UserNotFoundException if the user could not be found
     */
    List<PasswordCredential> getCredentialHistory(long directoryId, String userName) throws UserNotFoundException;

    /**
     * Creates a new user with the given details and credentials. The user details cannot be null, but the credential can be.
     *
     * @param user the user to create
     * @param credential the encrypted password for the user, which may be null if the user's password is not yet available
     * @return the newly created user
     * @throws UserAlreadyExistsException if a user with the same directory and name (case-insensitive) already exists
     * @throws IllegalArgumentException if the user name, directory or any details are null, or if the credential is not encrypted
     */
    User add(User user, PasswordCredential credential)
            throws UserAlreadyExistsException, IllegalArgumentException, DirectoryNotFoundException;

    /**
     * Adds or updates a user's attributes with the new Map of attribute values.
     *
     * The attributes map represents new or updated attributes and does not replace existing
     * attributes unless the key of an attribute matches the key of an existing attribute.
     *
     * Attributes with values of empty sets in the attributes parameter are removed from the user.
     *
     * @param user the user to store attributes for
     * @param attributes new or updated attributes (attributes that don't need changing should not appear in this Map).
     * @throws UserNotFoundException user with supplied username does not exist.
     */
    void storeAttributes(User user, Map<String, Set<String>> attributes) throws UserNotFoundException;

    /**
     * Updates all the user properties of the user with the same directory and case-insensitive name.
     *
     * @param user the user details, which should have the same name as the user to modify
     * @return the updated user
     * @throws UserNotFoundException if there is no user with the same name (case-insensitive) and directory as the user provided
     * @throws IllegalArgumentException if the user name, directory or any details are null
     */
    User update(User user) throws UserNotFoundException, IllegalArgumentException;

    /**
     * Updates the credential (password) of the user with the same directory and case-insensitive name.
     * The credential must be encrypted.
     *
     * @param user the user whose password will be modified
     * @throws UserNotFoundException if there is no user with the same name (case-insensitive) and directory as the user provided
     * @throws IllegalArgumentException if the credential is null or not encrypted
     */
    void updateCredential(User user, PasswordCredential credential, int maxCredentialHistory) throws UserNotFoundException, IllegalArgumentException;

    /**
     * Changes the user's name to the provided new name.
     *
     * @param user the user to rename
     * @param newName the new name of the user
     * @return the updated user
     * @throws UserNotFoundException if the user cannot be found
     * @throws UserAlreadyExistsException if the new name is already used
     * @throws IllegalArgumentException if the new name is null
     */
    User rename(User user, String newName) throws UserNotFoundException, UserAlreadyExistsException, IllegalArgumentException;

    /**
     * Removes the attributes for the user with the given name. Does nothing if the attribute doesn't
     * exist.
     *
     * @param user the user whose attribute will be removed
     * @param attributeName the name of the attribute to be removed
     * @throws UserNotFoundException if the user cannot be found
     */
    void removeAttribute(User user, String attributeName) throws UserNotFoundException;

    /**
     * Removes the user.
     *
     * @param user the user to remove
     * @throws UserNotFoundException if the user does not exist
     */
    void remove(User user) throws UserNotFoundException;

    /**
     * Returns users matching the search query in the given directory, ordered by name. Returns an empty list
     * if no users match.
     *
     * @param directoryId the ID of the directory to search
     * @param query the search query
     * @param <T> the type of objects to return, which is normally either {@link User} or {@link String}
     * @return the list of matching users, or an empty list if no users match
     */
    <T> List<T> search(long directoryId, EntityQuery<T> query);

    /**
     * Bulk add of users using JDBC batch support.
     *
     * @param users       to be added
     * @return a list of Users that <b>failed</b> to be added
     */
    BatchResult<User> addAll(Set<UserTemplateWithCredentialAndAttributes> users);

    /**
     * Remove all the given users from directory.
     *
     * @param directoryId the ID of the directory to remove users from
     * @param userNames set of users to be removed
     */
    void removeAllUsers(long directoryId, Set<String> userNames);

}