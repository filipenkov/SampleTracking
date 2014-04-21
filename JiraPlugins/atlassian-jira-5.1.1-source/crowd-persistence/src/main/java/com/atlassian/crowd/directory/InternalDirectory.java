package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.embedded.spi.GroupDao;
import com.atlassian.crowd.embedded.spi.MembershipDao;
import com.atlassian.crowd.embedded.spi.UserDao;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.OperationNotSupportedException;
import com.atlassian.crowd.exception.UserAlreadyExistsException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.util.BatchResult;
import com.atlassian.crowd.util.UserUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.crowd.model.user.UserConstants.PASSWORD_LASTCHANGED;
import static com.atlassian.crowd.model.user.UserConstants.REQUIRES_PASSWORD_CHANGE;

/**
 * Internal directory connector.
 * Stores all entity information to the database used by the Crowd Server.
 * Note that this implementation is used for an Actual InternalDirectory, and there is a related class
 * {@link com.atlassian.crowd.directory.CachingDirectory} that does local caching of remote objects.
 */
public class InternalDirectory extends AbstractInternalDirectory
{
    private static final Logger logger = LoggerFactory.getLogger(InternalDirectory.class);// configuration parameters

    public InternalDirectory(InternalDirectoryUtils internalDirectoryUtils,
                             PasswordEncoderFactory passwordEncoderFactory,
                             DirectoryDao directoryDao,
                             UserDao userDao, GroupDao groupDao, MembershipDao membershipDao)
    {
        super(internalDirectoryUtils, passwordEncoderFactory, directoryDao, userDao, groupDao, membershipDao);
    }

    /**
     * Adds a user and the following custom attributes:
     * - RemotePrincipalConstants.PASSWORD_LASTCHANGED set to the current time.
     * - RemotePrincipalConstants.REQUIRES_PASSWORD_CHANGE set to false.
     *
     * @param user       template of the user to add.
     * @param credential password. May be null, since JIRA creates a user in two steps
     * @return added user.
     * @throws InvalidCredentialException the password does not match the regular expression standard defined by the directory.
     */
    public User addUser(final UserTemplate user, final PasswordCredential credential)
            throws InvalidCredentialException, InvalidUserException, UserAlreadyExistsException, OperationFailedException
    {
        internalDirectoryUtils.validateDirectoryForEntity(user, directoryId);
        internalDirectoryUtils.validateUsername(user.getName());

        // pre-populate names (displayname, firstname, lastname)
        User prepopulatedUser = UserUtils.populateNames(user);

        Map<String, Set<String>> attributes = new HashMap<String, Set<String>>();

        if (credential != null)
        {
            prepareUserCredentialForAdd(credential, attributes);
        }

        User addedUser;
        try
        {
            addedUser = userDao.add(prepopulatedUser, credential);
        }
        catch (IllegalArgumentException e)
        {
            throw new InvalidUserException(user, e.getMessage(), e);
        }
        catch (DirectoryNotFoundException e)
        {
            // directory of the user has been validated to be the same as this directory so the directory must exist
            throw new OperationFailedException(e);
        }

        try
        {
            userDao.storeAttributes(addedUser, attributes);
        }
        catch (UserNotFoundException e)
        {
            throw new OperationFailedException(e);
        }

        return addedUser;
    }

    /**
     * Validates that a user template is good to be added to the internal directory,
     * in particular:
     * - validates the user name is in lower-case
     * - validates the password's with the password regexp and encrypts password if required
     * - adds the 'password last changed' and 'requires password change' attributes.
     *
     * @param credential unencrypted password.
     * @param attributes attributes of the new user. These attribues will be modified accordingly.
     * @throws InvalidCredentialException password is not valid.
     */
    void prepareUserCredentialForAdd(PasswordCredential credential, Map<String, Set<String>> attributes) throws InvalidCredentialException
    {
        // validate the regex, this will throw an InvalidcredentialException if there is a problem
        internalDirectoryUtils.validateCredential(credential, getValue(ATTRIBUTE_PASSWORD_REGEX));

        // set the time the password was last changed
        attributes.put(PASSWORD_LASTCHANGED, Collections.singleton(Long.toString(System.currentTimeMillis())));
        attributes.put(REQUIRES_PASSWORD_CHANGE, Collections.singleton(Boolean.FALSE.toString()));

        // Encrypt the password credentials with the encryption algorithm chosen for this directory
        encryptCredential(credential);
    }

    public Group addLocalGroup(final GroupTemplate group) throws OperationFailedException
    {
        throw new OperationNotSupportedException("addLocalGroup() is not supported for InternalDirectory");
    }

    public BatchResult<User> addAllUsers(final Set<UserTemplateWithCredentialAndAttributes> users)
    {
        Validate.notNull(users, "users cannot be null");

        Set<UserTemplateWithCredentialAndAttributes> preparedUsers = new HashSet<UserTemplateWithCredentialAndAttributes>();
        List<User> failedUsers = new ArrayList<User>();

        // cleanse
        for (UserTemplateWithCredentialAndAttributes uncleansedUser : users)
        {
            // prepoulate names (displayname, firstname, lastname)
            User prepopulatedUser = UserUtils.populateNames(uncleansedUser);

            UserTemplateWithCredentialAndAttributes user = new UserTemplateWithCredentialAndAttributes(prepopulatedUser, uncleansedUser.getAttributes(), uncleansedUser.getCredential());

            try
            {
                internalDirectoryUtils.validateDirectoryForEntity(user, getDirectoryId());
                internalDirectoryUtils.validateUsername(user.getName());
                prepareUserCredentialForAdd(user.getCredential(), user.getAttributes());
                preparedUsers.add(user);
            }
            catch (IllegalArgumentException e)
            {
                if (uncleansedUser == null)
                {
                    throw new IllegalArgumentException("Cannot add null user. " + e);
                }
                else
                {
                    failedUsers.add(user);
                    logger.error("Cannot add invalid user " + uncleansedUser.getName(), e);
                }
            }
            catch (InvalidCredentialException e)
            {
                failedUsers.add(uncleansedUser);
                logger.error("Cannot add user with invalid password " + uncleansedUser.getName(), e);
            }
        }

        // persist
        BatchResult<User> result = userDao.addAll(preparedUsers);
        result.addFailures(failedUsers);
        return result;
    }

    public BatchResult<Group> addAllGroups(final Set<GroupTemplate> groups)
    {
        Validate.notNull(groups, "groups cannot be null");

        Set<Group> preparedGroups = new HashSet<Group>();
        List<Group> failedGroups = new ArrayList<Group>();

        // cleanse
        for (GroupTemplate group : groups)
        {
            try
            {
                internalDirectoryUtils.validateDirectoryForEntity(group, getDirectoryId());
                internalDirectoryUtils.validateGroupName(group, group.getName());
                preparedGroups.add(group);
            }
            catch (IllegalArgumentException e)
            {
                if (group == null)
                {
                    throw new IllegalArgumentException("Cannot add null group. " + e);
                }
                else
                {
                    failedGroups.add(group);
                    logger.error("Cannot add invalid group " + group.getName(), e);
                }

            }
        }

        // persist
        try
        {
            BatchResult<Group> result = groupDao.addAll(preparedGroups);
            result.addFailures(failedGroups);
            return result;
        }
        catch (DirectoryNotFoundException e)
        {
            // directory must exist since the group's directory has been validated to match this directory's ID
            throw new RuntimeException(e);
        }
    }

    public BatchResult<String> addAllUsersToGroup(Set<String> userNames, String groupName)
            throws GroupNotFoundException
    {
        Validate.notNull(userNames, "userNames cannot be null");
        Validate.notEmpty(groupName, "groupName cannot be null or empty");

        return membershipDao.addAllUsersToGroup(getDirectoryId(), userNames, groupName);
    }
}
