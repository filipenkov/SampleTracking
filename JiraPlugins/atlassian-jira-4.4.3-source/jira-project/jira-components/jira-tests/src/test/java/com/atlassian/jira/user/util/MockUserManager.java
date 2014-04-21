package com.atlassian.jira.user.util;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.util.concurrent.Nullable;

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

    @Override
    public int getTotalUserCount()
    {
        return userMap.size();
    }

    @Override
    public Set<com.opensymphony.user.User> getAllUsers()
    {
        return OSUserConverter.convertToOSUserSet(getUsers());
    }

    @Override
    public com.opensymphony.user.User getUser(final @Nullable String userName)
    {
        return OSUserConverter.convertToOSUser(userMap.get(userName));
    }

    @Override
    public User getUserObject(@Nullable String userName)
    {
        return userMap.get(userName);
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
        return true;
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
    public Set<com.opensymphony.user.Group> getAllGroups()
    {
        return Collections.emptySet();
    }

    @Override
    public com.opensymphony.user.Group getGroup(final @Nullable String groupName)
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
    public boolean hasPasswordWritableDirectory()
    {
        return true;
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
