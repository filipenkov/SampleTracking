package com.atlassian.jira.user;

import com.atlassian.crowd.embedded.api.ConnectionPoolProperties;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.DirectorySynchronisationInformation;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.GroupWithAttributes;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.Query;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserWithAttributes;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.search.query.entity.GroupQuery;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.membership.GroupMembersOfGroupQuery;
import com.atlassian.crowd.search.query.membership.GroupMembershipQuery;
import com.atlassian.crowd.search.query.membership.UserMembersOfGroupQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since v4.1
 */
public class MockCrowdService implements CrowdService
{
    protected Map<String, User> users = new HashMap<String, User>();
    protected Map<String, PasswordCredential> credentials = new HashMap<String, PasswordCredential>();

    protected Map<String, Map<String, Set<String>>> userAttributes = new HashMap<String, Map<String, Set<String>>>();
    protected Map<String, Group> groups = new HashMap<String, Group>();

    protected Map<String, List<User>> groupMembers = new HashMap<String, List<User>>();

    public User addUser(final User user, final String credential)
            throws InvalidUserException, InvalidCredentialException
    {
        // Create a new User that lives in Directory 1
        User newUser = ImmutableUser.newUser(user).directoryId(1L).toUser();
        users.put(newUser.getName(), newUser);
        credentials.put(newUser.getName(), PasswordCredential.unencrypted(credential));
        return newUser;
    }

    public User updateUser(final User user) throws InvalidUserException
    {
        users.put(user.getName(), user);
        return user;
    }

    public User renameUser(final String oldName, final String newName) throws UserNotFoundException, InvalidUserException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void updateUserCredential(final User user, final String credential)
            throws InvalidCredentialException
    {
       credentials.put(user.getName(), PasswordCredential.unencrypted(credential));
    }

    public void setUserAttribute(final User user, final String key, String value)
    {
        Map<String, Set<String>> allValues = userAttributes.get(user.getName());
        if (allValues == null)
        {
            allValues = new HashMap<String, Set<String>>();
            userAttributes.put(user.getName(), allValues);
        }
        allValues.put(key, Collections.singleton(value));
    }

    public void setUserAttribute(final User user, final String key, Set<String> value)
    {
        throw new UnsupportedOperationException("Not implemented");    }

    public void removeUserAttribute(final User user, final String key)
    {
        Map<String, Set<String>> allValues = userAttributes.get(user.getName());
        if (allValues != null)
        {
            allValues.remove(key);
        }
    }

    public void removeAllUserAttributes(final User user)
    {
        userAttributes.remove(user.getName());
    }

    public boolean removeUser(final User user)
    {
        if (users.containsKey(user.getName()))
        {
            users.remove(user.getName());
            credentials.remove(user.getName());

            for (Group group : groups.values())
            {
                removeUserFromGroup(user, group);
            }
            return true;
        }
        return false;
    }

    public Group addGroup(final Group group)
    {
        groups.put(group.getName(), group);
        return group;
    }

    public Group updateGroup(final Group group)
    {
        groups.put(group.getName(), group);
        return group;
    }

    public Group renameGroup(final String oldName, final String newName) throws GroupNotFoundException, InvalidGroupException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setGroupAttribute(final Group group, final String attributeName, final String attributeValue)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setGroupAttribute(final Group group, final String attributeName, final Set<String> attributeValues)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void removeGroupAttribute(final Group group, final String attributeName)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void removeAllGroupAttributes(final Group group)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean removeGroup(final Group group)
    {
        if (groups.containsKey(group.getName()))
        {
            groups.remove(group.getName());
            groupMembers.remove(group.getName());
            return true;
        }
        return false;
    }

    public void addUserToGroup(final User user, final Group group)
    {
        getGroupMembers(group).add(user);
    }

    public void addGroupToGroup(final Group childGroup, final Group parentGroup)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean removeUserFromGroup(final User user, final Group group)
    {
        final List<User> members = getGroupMembers(group);
        if (members.contains(user))
        {
            members.remove(user);
            return true;
        }
        return false;
    }

    public boolean removeGroupFromGroup(final Group childGroup, final Group parentGroup)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isUserDirectGroupMember(final User user, final Group group)
    {
        return getGroupMembers(group).contains(user);
    }

    public boolean isGroupDirectGroupMember(final Group childGroup, final Group parentGroup)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Directory addDirectory(Directory directory)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void testConnection(final Directory directory)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<Directory> findAllDirectories()
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Directory findDirectoryById(long directoryId)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Directory updateDirectory(Directory directory)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void setDirectoryPosition(long directoryId, int position)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean removeDirectory(long directoryId)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isDirectorySynchronisable(final long directoryId)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void synchroniseDirectory(final long directoryId)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public User authenticate(final String name, final String credential)
            throws InactiveAccountException, FailedAuthenticationException
    {
        //Authenticate if we find the user
        User user = getUser(name);
        if (user == null)
        {
            throw new  InactiveAccountException(name);
        }

        final PasswordCredential passwordCredential = credentials.get(name);
        if (passwordCredential == null || passwordCredential.isEncryptedCredential())
        {
            throw new FailedAuthenticationException(name);
        }
        final String knownCredential = passwordCredential.getCredential();

        if (knownCredential != null && knownCredential.equals(credential))
        {
            return user;
        }
        throw new  FailedAuthenticationException(name);
    }

    public User getUser(final String name)
    {
        return users.get(name);
    }

    public UserWithAttributes getUserWithAttributes(final String name)
    {
        User user = getUser(name);
        if (user != null)
        {
            return new MockUser(name, user.getDisplayName(), user.getEmailAddress(), userAttributes.get(user.getName()));
        }
        return null;
    }

    public Group getGroup(final String name)
    {
        return groups.get(name);
    }

    public GroupWithAttributes getGroupWithAttributes(final String name)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @SuppressWarnings ({ "unchecked" })
    public <T> Iterable<T> search(final Query<T> query)
    {
        if (query instanceof UserQuery)
        {
            if (query.getReturnType().isAssignableFrom(String.class))
            {
                return (Iterable<T>) users.keySet();
            }
            else
            {
                return (Iterable<T>) users.values();
            }
        }
        else if (query instanceof GroupQuery)
        {
            if (query.getReturnType().isAssignableFrom(String.class))
            {
                return (Iterable<T>) groups.keySet();
            }
            else
            {
                return (Iterable<T>) groups.values();
            }
        }
        else if (query instanceof GroupMembershipQuery)
        {
            List<T> groupList = new ArrayList<T>();
            String userName = ((GroupMembershipQuery) query).getEntityNameToMatch();
            User user = getUser(userName);
            for (Group group : groups.values())
            {
                if (isUserMemberOfGroup(user, group))
                {
                    if (query.getReturnType().isAssignableFrom(String.class))
                    {
                        groupList.add((T) group.getName());
                    }
                    else
                    {
                        groupList.add((T) group);
                    }
                }
            }
            return groupList;
        }
        else if (query instanceof UserMembersOfGroupQuery)
        {
            List<T> userList = new ArrayList<T>();
            String groupName = ((UserMembersOfGroupQuery) query).getEntityNameToMatch();
            Group group = getGroup(groupName);
            if (group != null)
            {
                for (User user : getGroupMembers(group))
                {
                    if (query.getReturnType().isAssignableFrom(String.class))
                    {
                        userList.add((T) user.getName());
                    }
                    else
                    {
                        userList.add((T) user);
                    }
                }
            }
            return userList;
        }
        else if (query instanceof GroupMembersOfGroupQuery)
        {
            return new ArrayList<T>();
        }
        throw new UnsupportedOperationException("Unrecognized Query type '" + query + "'.");
    }

    public boolean isUserMemberOfGroup(final User user, final Group group)
    {
        final List<User> groupMembers = getGroupMembers(group);
        for (User groupMember : groupMembers)
        {
            if (user.getName().equals(groupMember.getName()))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isUserMemberOfGroup(final String userName, final String groupName)
    {
        Group group = getGroup(groupName);
        if (group == null)
        {
            return false;
        }
        final List<User> groupMembers = getGroupMembers(group);
        for (User groupMember : groupMembers)
        {
            if (userName.equals(groupMember.getName()))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isGroupMemberOfGroup(final String user, final String group)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isGroupMemberOfGroup(final Group childGroup, final Group parentGroup)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isDirectorySynchronising(final long l)
    {
        return false;
    }

    public DirectorySynchronisationInformation getDirectorySynchronisationInformation(final long directoryId)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void setConnectionPoolProperties(final ConnectionPoolProperties connectionPoolProperties)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ConnectionPoolProperties getStoredConnectionPoolProperties()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public ConnectionPoolProperties getSystemConnectionPoolProperties()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Iterable<User> searchUsersAllowingDuplicateNames(final Query<User> userQuery)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    private List<User> getGroupMembers(Group group)
    {
        if (group == null)
        {
            throw new IllegalArgumentException("null group");
        }
        List<User> members = groupMembers.get(group.getName());
        if (members == null)
        {
            members = new ArrayList<User>();
            groupMembers.put(group.getName(), members);
        }
        return members;
    }
}
