package com.atlassian.jira.user.util;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.Query;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.crowd.exception.DirectoryNotFoundException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.crowd.manager.directory.DirectoryManager;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.search.query.entity.GroupQuery;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.DelegatingApplicationUser;
import com.atlassian.util.concurrent.Nullable;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultUserManager implements UserManager
{
    private static final Logger log = Logger.getLogger(DefaultUserManager.class);

    private final CrowdService crowdService;
    private final CrowdDirectoryService crowdDirectoryService;
    private final DirectoryManager directoryManager;

    public DefaultUserManager(final CrowdService crowdService, CrowdDirectoryService crowdDirectoryService, DirectoryManager directoryManager)
    {
        this.crowdService = crowdService;
        this.crowdDirectoryService = crowdDirectoryService;
        this.directoryManager = directoryManager;
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
        //noinspection UnusedDeclaration
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
        long startTime = System.currentTimeMillis();

        // Going through Crowd Service is a bit slow because the current implementation in ApplicationService
        // Will sort the results for paging even if we ask for ALL_RESULTS
        // We therefore have an optimised version here because JIRA loves calling get all users.

        // Find all the active directories in reverse order
        List<Directory> activeDirectories = new LinkedList<Directory>();
        for (Directory directory : crowdDirectoryService.findAllDirectories())
        {
            if (directory.isActive())
            {
                activeDirectories.add(0, directory);
            }
        }

        final UserQuery<User> query = new UserQuery<User>(User.class, NullRestrictionImpl.INSTANCE, 0, UserQuery.ALL_RESULTS);

        final Collection<User> allUsers;
        if (activeDirectories.size() == 1)
        {
            // No removal of shadowed users is required
            try
            {
                allUsers = directoryManager.searchUsers(activeDirectories.get(0).getId(), query);
            }
            catch (DirectoryNotFoundException e)
            {
                throw new OperationFailedException(e);
            }
            catch (com.atlassian.crowd.exception.OperationFailedException e)
            {
                throw new OperationFailedException(e);
            }
        }
        else
        {
            // Use a map to remove duplicate usernames
            Map<String, User> userMap = new HashMap<String, User>();
            // We are looping in reverse order so that higher users push out shadowed users
            for (Directory activeDirectory : activeDirectories)
            {
                try
                {
                    List<User> users = directoryManager.searchUsers(activeDirectory.getId(), query);
                    // add to our map letting shadowed users get replaced
                    for (User user : users)
                    {
                        userMap.put(IdentifierUtils.toLowerCase(user.getName()), user);
                    }
                }
                catch (DirectoryNotFoundException e)
                {
                    // ignore this directory - no longer exists
                }
                catch (com.atlassian.crowd.exception.OperationFailedException e)
                {
                    // keep cycling - this is what Application Service does. Shouldn't happen with cached directories anyway
                    log.error("Unexpected error retrieving users from directory " + activeDirectory.getId(), e);
                }
            }
            allUsers = userMap.values();
        }
        if (log.isDebugEnabled())
            log.info("Found " + allUsers.size() + " users in " + (System.currentTimeMillis() - startTime) + "ms.");
        return allUsers;
    }

    @Override
    public Set<User> getAllUsers()
    {
        HashSet<User> allUsers = new HashSet<User>();
        for (User user : getUsers())
        {
            allUsers.add(user);
        }
        return allUsers;
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
    public User getUser(final String userName)
    {
        return getUserObject(userName);
    }

    @Override
    public User getUserObject(@Nullable String userName)
    {
        return getCrowdUser(userName);
    }

    @Override
    public ApplicationUser getUserByKey(@Nullable String userKey)
    {
        // TODO implement this properly when we can break API compatibility
        return getUserByName(userKey);
    }

    @Override
    public ApplicationUser getUserByName(@Nullable String userName)
    {
        if (userName == null)
        {
            return null;
        }

        // TODO implement this properly when we can break API compatibility
        return new DelegatingApplicationUser(IdentifierUtils.toLowerCase(userName), getUserEvenWhenUnknown(userName));
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
        // Check if the directory allows user modification
        return userDirectoryAllowsUpdateUser(user);
    }

    @Override
    public void updateUser(User user)
    {
        try
        {
            crowdService.updateUser(user);
            // Might have changed the active/inactive flag.
            // We clear on EVERY add to group, so may as well clear here without checking current status.
            ComponentAccessor.getUserUtil().clearActiveUserCount();
        }
        catch (InvalidUserException ex)
        {
            // This occurs when the passed User does not have the expected DirectoryId
            throw new OperationFailedException(ex);
        }
        catch (OperationNotPermittedException ex)
        {
            // Permission Violation
            throw new OperationFailedException(ex);
        }
    }

    @Override
    public boolean canUpdateUserPassword(User user)
    {
        // If we can't update the user we can't update the password
        if (!userDirectoryAllowsUpdateUser(user))
        {
            return false;
        }
        final Directory directory = crowdDirectoryService.findDirectoryById(user.getDirectoryId());
        return canDirectoryUpdateUserPassword(directory);
    }

    private boolean userDirectoryAllowsUpdateUser(User user)
    {
        Directory directory = crowdDirectoryService.findDirectoryById(user.getDirectoryId());
        if (directory == null)
        {
            return false;
        }
        return directory.getAllowedOperations().contains(OperationType.UPDATE_USER);
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
        return directory.getAllowedOperations().contains(OperationType.UPDATE_GROUP);
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
        Set<Group> groups = new LinkedHashSet<Group>();
        for (Group group : crowdGroups)
        {
            groups.add(group);
        }
        return groups;
    }

    @Override
    public Set<Group> getAllGroups()
    {
        Collection<Group> groups = getGroups();
        if (groups instanceof Set)
        {
            return (Set<Group>) groups;
        }
        return new LinkedHashSet<Group>(groups);
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
    public Group getGroup(final String groupName)
    {
        return getCrowdGroup(groupName);
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
    public boolean hasWritableDirectory()
    {
        return getWritableDirectories().size() > 0;
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
    public boolean hasGroupWritableDirectory()
    {
        final List<Directory> allDirectories = crowdDirectoryService.findAllDirectories();
        for (Directory directory : allDirectories)
        {
            if (directory.isActive() && directory.getAllowedOperations().contains(OperationType.CREATE_GROUP))
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
        // For Delegated LDAP directories, we can never modify the password
        if (directory.getType() == DirectoryType.DELEGATING)
        {
            return false;
        }
        return directory.getAllowedOperations().contains(OperationType.UPDATE_USER);
    }
}
