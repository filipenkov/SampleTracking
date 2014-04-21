package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.embedded.spi.GroupDao;
import com.atlassian.crowd.embedded.spi.MembershipDao;
import com.atlassian.crowd.embedded.spi.UserDao;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserAlreadyExistsException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.password.factory.PasswordEncoderFactory;
import com.atlassian.crowd.util.BatchResult;
import org.apache.commons.lang.Validate;

import java.util.Set;

/**
 * This InternalDirectory is used for locally caching Users and Groups from an external Directory.
 *
 * @see com.atlassian.crowd.directory.InternalDirectory
 */
public class CachingDirectory extends AbstractInternalDirectory
{
    public CachingDirectory(InternalDirectoryUtils internalDirectoryUtils,
                            PasswordEncoderFactory passwordEncoderFactory,
                            DirectoryDao directoryDao,
                            UserDao userDao, GroupDao groupDao, MembershipDao membershipDao)
    {
        super(internalDirectoryUtils, passwordEncoderFactory, directoryDao, userDao, groupDao, membershipDao);
    }

    /**
     * Adds a user with no special added logic.
     *
     * @param user       template of the user to add.
     * @param credential password. May be null, since JIRA creates a user in two steps
     * @return added user.
     * @throws com.atlassian.crowd.exception.InvalidCredentialException
     *          the password does not match the regular
     *          expression standard defined by the directory.
     */
    public User addUser(final UserTemplate user, final PasswordCredential credential)
            throws InvalidCredentialException, InvalidUserException, UserAlreadyExistsException, OperationFailedException
    {
        internalDirectoryUtils.validateDirectoryForEntity(user, directoryId);
        User addedUser;
        try
        {
            addedUser = userDao.add(user, credential);
        }
        catch (IllegalArgumentException e)
        {
            throw new InvalidUserException(user, e.getMessage(), e);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e);
        }
        return addedUser;
    }

    public Group addLocalGroup(final GroupTemplate group) throws InvalidGroupException, OperationFailedException
    {
        internalDirectoryUtils.validateDirectoryForEntity(group, directoryId);
        internalDirectoryUtils.validateGroupName(group, group.getName());

        try
        {
            return groupDao.addLocal(group);
        }
        catch (IllegalArgumentException e)
        {
            throw new InvalidGroupException(group, e.getMessage(), e);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new OperationFailedException(e.getMessage(), e);
        }
    }

    public BatchResult<User> addAllUsers(Set<UserTemplateWithCredentialAndAttributes> users)
    {
        for (UserTemplateWithCredentialAndAttributes user : users)
            internalDirectoryUtils.validateDirectoryForEntity(user, directoryId);

        return userDao.addAll(users);
    }

    public BatchResult<Group> addAllGroups(Set<GroupTemplate> groups)
    {
        for (GroupTemplate group : groups)
        {
            internalDirectoryUtils.validateDirectoryForEntity(group, directoryId);
            internalDirectoryUtils.validateGroupName(group, group.getName());
        }

        try
        {
            return groupDao.addAll(groups);
        }
        catch (DirectoryNotFoundException e)
        {
            // shouldn't occur because we have just validated that the directory ID of all the groups matches this
            // directory's ID
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

    @Override
    public User updateUser(final UserTemplate user) throws InvalidUserException, UserNotFoundException
    {
        internalDirectoryUtils.validateDirectoryForEntity(user, directoryId);

        try
        {
            return userDao.update(user);
        }
        catch (IllegalArgumentException e)
        {
            throw new InvalidUserException(user, e.getMessage(), e);
        }
    }
}
