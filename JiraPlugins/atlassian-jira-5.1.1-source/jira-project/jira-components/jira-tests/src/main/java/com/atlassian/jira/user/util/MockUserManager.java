package com.atlassian.jira.user.util;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.util.concurrent.Nullable;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Really simple mock implementation
 *
 * @since v4.1
 */
public class MockUserManager implements UserManager
{
    Map<String, User> userMap = new HashMap<String, User>();

    private boolean writableDirectory = true;
    private boolean groupWritableDirectory = true;

    @Override
    public int getTotalUserCount()
    {
        return userMap.size();
    }

    @Override
    public Set<User> getAllUsers()
    {
        return Sets.newHashSet(getUsers());
    }

    @Override
    public User getUser(final @Nullable String userName)
    {
        return userMap.get(userName);
    }

    @Override
    public User getUserObject(@Nullable String userName)
    {
        return userMap.get(userName);
    }

    @Override
    public ApplicationUser getUserByKey(@Nullable String userKey)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ApplicationUser getUserByName(@Nullable String userName)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public User findUserInDirectory(String userName, Long directoryId)
    {
        return null;
    }

    @Override
    public User getUserEvenWhenUnknown(final String userName)
    {
        return userMap.get(userName);
    }

    @Override
    public boolean canUpdateUser(User user)
    {
        return writableDirectory;
    }

    @Override
    public void updateUser(User user)
    {
        userMap.put(user.getName(), user);
    }

    @Override
    public boolean canUpdateUserPassword(User user)
    {
        return true;
    }

    @Override
    public boolean canUpdateGroupMembershipForUser(User user)
    {
        return true;
    }

    @Override
    public Set<Group> getAllGroups()
    {
        return Collections.emptySet();
    }

    @Override
    public Group getGroup(final @Nullable String groupName)
    {
        return null;
    }

    @Override
    public Group getGroupObject(@Nullable String groupName)
    {
        return null;
    }

    @Override
    public List<Directory> getWritableDirectories()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasWritableDirectory()
    {
        return writableDirectory;
    }

    public void setWritableDirectory(boolean writableDirectory)
    {
        this.writableDirectory = writableDirectory;
    }

    @Override
    public boolean hasPasswordWritableDirectory()
    {
        return true;
    }

    @Override
    public boolean hasGroupWritableDirectory()
    {
        return groupWritableDirectory;
    }

    public void setGroupWritableDirectory(boolean groupWritableDirectory)
    {
        this.groupWritableDirectory = groupWritableDirectory;
    }

    @Override
    public boolean canDirectoryUpdateUserPassword(Directory directory)
    {
        return true;
    }

    @Override
    public Directory getDirectory(Long directoryId)
    {
        return null;
    }

    @Override
    public Collection<User> getUsers()
    {
        return userMap.values();
    }

    @Override
    public Collection<com.atlassian.crowd.embedded.api.Group> getGroups()
    {
        return Collections.emptySet();
    }

    public void addUser(User user)
    {
        userMap.put(user.getName(), user);
    }
}
