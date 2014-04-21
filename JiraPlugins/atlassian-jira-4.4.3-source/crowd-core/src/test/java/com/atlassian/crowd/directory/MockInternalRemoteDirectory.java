package com.atlassian.crowd.directory;

import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.InternalDirectoryGroup;
import com.atlassian.crowd.model.user.*;
import com.atlassian.crowd.util.BatchResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MockInternalRemoteDirectory extends MockRemoteDirectory implements InternalRemoteDirectory
{
    private Map<String, Map<String, Set<String>>> userAttributesMap = new HashMap<String, Map<String, Set<String>>>();

    public MockInternalRemoteDirectory()
    {
        setSupportsInactiveAccounts(true);
    }

    public TimestampedUser findUserByName(String name) throws UserNotFoundException
    {
        return super.findUserByName(name);
    }

    public InternalDirectoryGroup findGroupByName(String name) throws GroupNotFoundException
    {
        return super.findGroupByName(name);
    }

    public Group addLocalGroup(final GroupTemplate group) throws InvalidGroupException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public BatchResult<User> addAllUsers(Set<UserTemplateWithCredentialAndAttributes> users)
    {
        final BatchResult<User> result = new BatchResult<User>(users.size());
        for (UserTemplateWithCredentialAndAttributes user : users)
        {
            try
            {
                result.addSuccess(addUser(user, user.getCredential()));
            } catch (InvalidUserException e)
            {
                result.addFailure(user);
            } catch (InvalidCredentialException e)
            {
                result.addFailure(user);
            } catch (OperationFailedException e)
            {
                result.addFailure(user);
            } catch (UserAlreadyExistsException e)
            {
                result.addFailure(user);
            }
        }
        return result;
    }

    public BatchResult<Group> addAllGroups(Set<GroupTemplate> groups)
    {
        final BatchResult<Group> result = new BatchResult<Group>(groups.size());
        for (GroupTemplate group : groups)
        {
            try
            {
                result.addSuccess(addGroup(group));
            } catch (InvalidGroupException e)
            {
                result.addFailure(group);
            } catch (OperationFailedException e)
            {
                result.addFailure(group);
            }
        }
        return result;
    }

    public BatchResult<String> addAllUsersToGroup(Set<String> userNames, String groupName)
            throws GroupNotFoundException
    {
        final BatchResult<String> result = new BatchResult<String>(userNames.size());
        for (String userName : userNames)
        {
            try
            {
                addUserToGroup(userName, groupName);
                result.addSuccess(userName);
            } catch (UserNotFoundException e)
            {
                result.addFailure(userName);
            } catch (OperationFailedException e)
            {
                result.addFailure(userName);
            }
        }
        return result;
    }

    @Override
    protected User putUser(final UserTemplate user)
    {
        InternalUser newUser = new InternalUser(user, getDirectory(), null);
        userMap.put(user.getName(), newUser);
        return newUser;
    }

    public void removeAllUsers(Set<String> usernames)
    {
        for (String username : usernames)
        {
            try
            {
                removeUser(username);
            } catch (UserNotFoundException e)
            {
                // ignore
            } catch (OperationFailedException e)
            {
                // ignore
            }
        }
    }

    public void removeAllGroups(Set<String> groupNames)
    {
        for (String groupName : groupNames)
        {
            try
            {
                removeGroup(groupName);
            } catch (GroupNotFoundException e)
            {
                // ignore
            } catch (OperationFailedException e)
            {
                // ignore
            }
        }
    }

    @Override
    public RemoteDirectory getAuthoritativeDirectory()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
