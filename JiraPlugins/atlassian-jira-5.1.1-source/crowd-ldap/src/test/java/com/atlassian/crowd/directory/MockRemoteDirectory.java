package com.atlassian.crowd.directory;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.crowd.model.group.*;
import com.atlassian.crowd.model.user.*;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;

import java.util.*;

public class MockRemoteDirectory implements RemoteDirectory
{
    protected Map<String, TimestampedUser> userMap = new HashMap<String, TimestampedUser>();
    protected Map<String, Map<String, Set<String>>> userAttributesMap = new HashMap<String, Map<String, Set<String>>>();
    private Map<String, InternalDirectoryGroup> groupMap = new HashMap<String, InternalDirectoryGroup>();
    private Map<String, InternalDirectoryGroup> roleMap = new HashMap<String, InternalDirectoryGroup>();
    private Map<String, String> attributeMap = new HashMap<String, String>();
    private long directoryId = 1;

    private boolean supportsInactiveAccounts = false;

    public long getDirectoryId()
    {
        return directoryId;
    }

    public void setDirectoryId(final long directoryId)
    {
        this.directoryId = directoryId;
    }

    public String getDescriptiveName()
    {
        return "MockRemoteDirectory";
    }

    public void setAttributes(final Map<String, String> attributes)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public TimestampedUser findUserByName(final String name) throws UserNotFoundException
    {
        TimestampedUser user = userMap.get(name);
        if (user == null)
        {
            throw new UserNotFoundException(name);
        }
        return user;
    }

    public UserWithAttributes findUserWithAttributesByName(final String name)
            throws UserNotFoundException, OperationFailedException
    {
        UserTemplateWithAttributes user = UserTemplateWithAttributes.ofUserWithNoAttributes(findUserByName(name));
        Map<String, Set<String>> attributes = userAttributesMap.get(name);
        if (attributes != null)
        {
            for (Map.Entry<String, Set<String>> entry : userAttributesMap.get(name).entrySet())
            {
                user.setAttribute(entry.getKey(), entry.getValue());
            }
        }

        return user;
    }

    public User authenticate(final String name, final PasswordCredential credential)
            throws UserNotFoundException, InactiveAccountException, InvalidAuthenticationException, ExpiredCredentialException, OperationFailedException
    {
        if (credential == null)
        {
            throw InvalidAuthenticationException.newInstanceWithName(name);
        }
        return userMap.get(name);
    }

    public User addUser(final UserTemplate user, final PasswordCredential credential)
            throws InvalidUserException, InvalidCredentialException, OperationFailedException
    {
        return putUser(user);
    }

    protected Directory getDirectory()
    {
        return new DirectoryImpl()
        {
            @Override
            public Long getId()
            {
                return directoryId;
            }
        };
    }

    public User updateUser(final UserTemplate user)
            throws InvalidUserException, UserNotFoundException, OperationFailedException
    {
        if (userMap.containsKey(user.getName()))
        {
            return putUser(user);
        } else
        {
            throw new UserNotFoundException(user.getName());
        }
    }

    protected User putUser(final UserTemplate user)
    {
        UserTemplate userTemplate = new UserTemplate(user);
        // All users should be active as this mock doesn't support inactive users
        userTemplate.setActive(true);
        InternalUser newUser = new InternalUser(userTemplate, getDirectory(), null);
        userMap.put(user.getName(), newUser);
        return newUser;
    }

    public void updateUserCredential(final String username, final PasswordCredential credential)
            throws UserNotFoundException, InvalidCredentialException, OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public User renameUser(final String oldName, final String newName)
            throws UserNotFoundException, InvalidUserException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void storeUserAttributes(final String username, final Map<String, Set<String>> attributes)
            throws UserNotFoundException, OperationFailedException
    {
        if (!userMap.containsKey(username))
        {
            throw new UserNotFoundException(username);
        }

        Map<String, Set<String>> cachedAttributes = userAttributesMap.get(username);
        if (cachedAttributes == null)
        {
            cachedAttributes = new HashMap<String, Set<String>>();
            userAttributesMap.put(username, cachedAttributes);
        }
        cachedAttributes.putAll(attributes);
    }

    public void removeUserAttributes(final String username, final String attributeName)
            throws UserNotFoundException, OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void removeUser(final String name) throws UserNotFoundException, OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public <T> List<T> searchUsers(final EntityQuery<T> query) throws OperationFailedException
    {
        return new ArrayList<T>((Collection<? extends T>) userMap.values());
    }

    public InternalDirectoryGroup findGroupByName(final String name) throws GroupNotFoundException
    {
        InternalDirectoryGroup group = groupMap.get(name);
        if (group == null)
        {
            group = roleMap.get(name);
            if (group == null)
            {
                throw new GroupNotFoundException(name);
            }
        }
        return group;
    }

    public GroupWithAttributes findGroupWithAttributesByName(final String name)
            throws GroupNotFoundException, OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Group addGroup(final GroupTemplate group)
            throws InvalidGroupException, OperationFailedException
    {
        switch (group.getType())
        {
            case GROUP:
                groupMap.put(group.getName(), new InternalGroup(group, getDirectory()));
                return group;
            case LEGACY_ROLE:
                roleMap.put(group.getName(), new InternalGroup(group, getDirectory()));
                return group;
        }
        throw new IllegalArgumentException("Unknown Group Type");
    }

    public Group updateGroup(final GroupTemplate group)
            throws InvalidGroupException, GroupNotFoundException, OperationFailedException
    {
        if (groupMap.containsKey(group.getName()) || roleMap.containsKey(group.getName()))
        {
            return addGroup(group);
        } else
        {
            throw new GroupNotFoundException(group.getName());
        }
    }

    public Group renameGroup(final String oldName, final String newName)
            throws GroupNotFoundException, InvalidGroupException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void storeGroupAttributes(final String groupName, final Map<String, Set<String>> attributes)
            throws GroupNotFoundException, OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void removeGroupAttributes(final String groupName, final String attributeName)
            throws GroupNotFoundException, OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void removeGroup(final String name) throws GroupNotFoundException, OperationFailedException
    {
        groupMap.remove(name);
        roleMap.remove(name);
    }

    @SuppressWarnings({"unchecked"})
    public <T> List<T> searchGroups(final EntityQuery<T> query) throws OperationFailedException
    {
        // assume "search all" for Group objects
        switch (query.getEntityDescriptor().getGroupType())
        {
            case GROUP:
                return new ArrayList(groupMap.values());
            case LEGACY_ROLE:
                return new ArrayList(roleMap.values());
        }
        throw new IllegalArgumentException("Unknown Group Type");
    }

    public boolean isUserDirectGroupMember(final String username, final String groupName)
            throws OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public boolean isGroupDirectGroupMember(final String childGroup, final String parentGroup)
            throws OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void addUserToGroup(final String username, final String groupName)
            throws GroupNotFoundException, UserNotFoundException, OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void addGroupToGroup(final String childGroup, final String parentGroup)
            throws GroupNotFoundException, InvalidMembershipException, OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void removeUserFromGroup(final String username, final String groupName)
            throws GroupNotFoundException, UserNotFoundException, MembershipNotFoundException, OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void removeGroupFromGroup(final String childGroup, final String parentGroup)
            throws GroupNotFoundException, InvalidMembershipException, MembershipNotFoundException, OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public <T> List<T> searchGroupRelationships(final MembershipQuery<T> query) throws OperationFailedException
    {
        // Trivial implementation
        return Collections.emptyList();
    }

    public void testConnection() throws OperationFailedException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void setSupportsInactiveAccounts(boolean supportsInactiveAccounts)
    {
        this.supportsInactiveAccounts = supportsInactiveAccounts;
    }

    public boolean supportsInactiveAccounts()
    {
        return supportsInactiveAccounts;
    }

    public boolean supportsNestedGroups()
    {
        return true;
    }

    public boolean isRolesDisabled()
    {
        return false;
    }

    public Set<String> getValues(final String key)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public String getValue(final String key)
    {
        return attributeMap.get(key);
    }

    public Set<String> getKeys()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public boolean isEmpty()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Group getGroup(final String name)
    {
        return groupMap.get(name);
    }

    public Group getRole(final String name)
    {
        return roleMap.get(name);
    }

    @Override
    public RemoteDirectory getAuthoritativeDirectory()
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Iterable<Membership> getMemberships() throws OperationFailedException
    {
        return new DirectoryMembershipsIterable(this);
    }
}
