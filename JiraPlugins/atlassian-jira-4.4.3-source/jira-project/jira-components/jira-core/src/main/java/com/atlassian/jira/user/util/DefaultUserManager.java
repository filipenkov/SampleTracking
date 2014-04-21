package com.atlassian.jira.user.util;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.Query;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.search.query.entity.GroupQuery;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.util.concurrent.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DefaultUserManager implements UserManager
{
    private final CrowdService crowdService;
    private final CrowdDirectoryService crowdDirectoryService;
    private final ApplicationProperties applicationProperties;

    public DefaultUserManager(final CrowdService crowdService, CrowdDirectoryService crowdDirectoryService, ApplicationProperties applicationProperties)
    {
        this.crowdService = crowdService;
        this.crowdDirectoryService = crowdDirectoryService;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public int getTotalUserCount()
    {
        Iterable<User> crowdUsers = getAllUsersFromCrowd();
        if (crowdUsers instanceof Collection)
        {
            return ((Collection) crowdUsers).size();
        }
        int count = 0;
        for (User crowdUser : crowdUsers)
        {
            count++;
        }
        return count;
    }

    @Override
    public Collection<User> getUsers()
    {
        Iterable<User> crowdUsers = getAllUsersFromCrowd();
        // hope for a short-cut
        if (crowdUsers instanceof Collection)
        {
            return (Collection<User>) crowdUsers;
        }
        // A real streaming Iterable:
        List<User> users = new LinkedList<User>();
        for (User user : crowdUsers)
        {
            users.add(user);
        }
        return users;
    }

    private Iterable<User> getAllUsersFromCrowd()
    {
        final Query<User> query = new UserQuery<User>(User.class, NullRestrictionImpl.INSTANCE, 0, -1);
        return crowdService.search(query);
    }

    @Override
    public Set<com.opensymphony.user.User> getAllUsers()
    {
        return OSUserConverter.convertToOSUserSet(getUsers());
    }

    private User getCrowdUser(final String userName)
    {
        // Make sure that null userName is handled in a uniform way by all OSUser implementations - eg see JRA-15821, CWD-1275 
        if (userName == null)
        {
            return null;
        }
        return crowdService.getUser(userName);
    }

    @Override
    public com.opensymphony.user.User getUser(final String userName)
    {
        return OSUserConverter.convertToOSUser(getCrowdUser(userName));
    }

    @Override
    public User getUserObject(@Nullable String userName)
    {
        return getCrowdUser(userName);
    }

    @Override
    public User findUserInDirectory(String userName, Long directoryId)
    {
        try
        {
            return ComponentAccessor.getComponentOfType(DirectoryManager.class).findUserByName(directoryId, userName);
        }
        catch (DirectoryNotFoundException e)
        {
            throw new IllegalArgumentException(e);
        }
        catch (UserNotFoundException e)
        {
            return null;
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            throw new OperationFailedException(e);
        }
    }

    @Override
    public User getUserEvenWhenUnknown(final String userName)
    {
        if (userName == null)
        {
            // This is the only case when we can return null
            return null;
        }
        User user = getCrowdUser(userName);
        if (user == null)
        {
            // Build a "fake" user object for the deleted User.
            user = new ImmutableUser.Builder().name(userName).displayName(userName).active(false).toUser();
        }
        return user;
    }

    @Override
    public boolean canUpdateUser(User user)
    {
        if (user == null)
        {
            return false;
        }
        // Check global External User Management setting
        if (applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT))
        {
            return false;
        }
        // Check if the directory allows user modification
        Directory directory = crowdDirectoryService.findDirectoryById(user.getDirectoryId());
        if (directory == null)
        {
            return false;
        }
        if (directory.getAllowedOperations().contains(OperationType.UPDATE_USER))
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean canUpdateUserPassword(User user)
    {
        // If we can't update the user we can't update the password
        if (!canUpdateUser(user))
        {
            return false;
        }
        final Directory directory = crowdDirectoryService.findDirectoryById(user.getDirectoryId());
        return canDirectoryUpdateUserPassword(directory);
    }

    @Override
    public boolean canUpdateGroupMembershipForUser(User user)
    {
        if (user == null)
        {
            return false;
        }
        Directory directory = crowdDirectoryService.findDirectoryById(user.getDirectoryId());
        if (directory == null)
        {
            return false;
        }
        if (directory.getAllowedOperations().contains(OperationType.UPDATE_GROUP))
        {
            return true;
        }
        return false;
    }

    @Override
    public Collection<Group> getGroups()
    {
        final Query<Group> query =
                new GroupQuery<Group>(Group.class, GroupType.GROUP, NullRestrictionImpl.INSTANCE, 0, -1);
        Iterable<Group> crowdGroups = crowdService.search(query);
        // Hope for a quick conversion:
        if (crowdGroups instanceof Collection)
        {
            return (Collection<Group>) crowdGroups;
        }
        LinkedList<Group> groups = new LinkedList<Group>();
        for (Group group : crowdGroups)
        {
            groups.add(group);
        }
        return groups;
    }

    @Override
    public Set<com.opensymphony.user.Group> getAllGroups()
    {
        return OSUserConverter.convertToOSGroups(getGroups());
    }

    private Group getCrowdGroup(final String groupName)
    {
        // Make sure that null groupName is handled in a uniform way by all OSUser implementations - eg see JRA-15821, CWD-1275
        if (groupName == null)
        {
            return null;
        }
        return crowdService.getGroup(groupName);
    }

    @Override
    public com.opensymphony.user.Group getGroup(final String groupName)
    {
        return OSUserConverter.convertToOSGroup(getCrowdGroup(groupName));
    }

    @Override
    public Group getGroupObject(@Nullable String groupName)
    {
        return getCrowdGroup(groupName);
    }

    @Override
    public List<Directory> getWritableDirectories()
    {
        final List<Directory> allDirectories = crowdDirectoryService.findAllDirectories();
        List<Directory> writableDirectories = new ArrayList<Directory>(allDirectories.size());
        for (Directory directory : allDirectories)
        {
            if (directory.getAllowedOperations().contains(OperationType.CREATE_USER) && directory.isActive())
            {
                writableDirectories.add(directory);
            }
        }
        return writableDirectories;
    }


    @Override
    public boolean hasPasswordWritableDirectory()
    {
        final List<Directory> writableDirectories = getWritableDirectories();
        for (Directory directory : writableDirectories)
        {
            if (canDirectoryUpdateUserPassword(directory))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Directory getDirectory(Long directoryId)
    {
        return crowdDirectoryService.findDirectoryById(directoryId);
    }

    @Override
    public boolean canDirectoryUpdateUserPassword(final Directory directory)
    {
        if (directory == null)
        {
            return false;
        }
        // For Delegated LDAP directories, we can modify user properties, except for the password
        if (directory.getType() == DirectoryType.DELEGATING)
        {
            return false;
        }
        return true;
    }
}
