package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.model.DirectoryEntity;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;

/**
 * Internal Directory utility.
 */
public interface InternalDirectoryUtils
{
    /**
     * Validates that the directory entity has the same directory ID as <tt>directoryId</tt>.
     *
     * @param entity DirectoryEntity
     * @param directoryId directory ID to match
     * @throws IllegalArgumentException if the directory IDs do not match
     */
    void validateDirectoryForEntity(DirectoryEntity entity, Long directoryId);

    /**
     * Validates a username.
     *
     * @param username username to validate
     * @throws IllegalArgumentException if the username is not valid
     */
    void validateUsername(String username);

    /**
     * Validates password credential against the given regex.
     *
     * @param credential Password credential.
     * @param regex Regex.
     * @throws InvalidCredentialException If the credential failed validation.
     */
    void validateCredential(PasswordCredential credential, String regex) throws InvalidCredentialException;

    /**
     * Validates group name.
     *
     * @param group Group.
     * @param groupName Group name.
     */
    void validateGroupName(Group group, String groupName);
}
