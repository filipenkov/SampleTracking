package com.atlassian.crowd.embedded.core;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.GroupWithAttributes;
import com.atlassian.crowd.embedded.api.Query;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserWithAttributes;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidMembershipException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.crowd.exception.runtime.GroupNotFoundException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.crowd.exception.runtime.UserNotFoundException;

import java.util.Set;

/**
 * Abstract implementation of CrowdService which simply delegates all the operations.
 */
abstract class DelegatingCrowdService implements CrowdService
{
    protected final CrowdService crowdService;

    public DelegatingCrowdService(CrowdService crowdService)
    {
        this.crowdService = crowdService;
    }

    public User authenticate(String name, String credential) throws FailedAuthenticationException, OperationFailedException
    {
        return crowdService.authenticate(name, credential);
    }

    public User getUser(String name)
    {
        return crowdService.getUser(name);
    }

    public UserWithAttributes getUserWithAttributes(String name)
    {
        return crowdService.getUserWithAttributes(name);
    }

    public Group getGroup(String name)
    {
        return crowdService.getGroup(name);
    }

    public GroupWithAttributes getGroupWithAttributes(String name)
    {
        return crowdService.getGroupWithAttributes(name);
    }

    public <T> Iterable<T> search(Query<T> query)
    {
        return crowdService.search(query);
    }

    public boolean isUserMemberOfGroup(String userName, String groupName)
    {
        return crowdService.isUserMemberOfGroup(userName, groupName);
    }

    public boolean isUserMemberOfGroup(User user, Group group)
    {
        return crowdService.isUserMemberOfGroup(user, group);
    }

    public boolean isGroupMemberOfGroup(String childGroupName, String parentGroupName)
    {
        return crowdService.isGroupMemberOfGroup(childGroupName, parentGroupName);
    }

    public boolean isGroupMemberOfGroup(Group childGroup, Group parentGroup)
    {
        return crowdService.isGroupMemberOfGroup(childGroup, parentGroup);
    }

    public User addUser(User user, String credential) throws InvalidUserException, InvalidCredentialException, OperationNotPermittedException, OperationFailedException
    {
        return crowdService.addUser(user, credential);
    }

    public User updateUser(User user) throws UserNotFoundException, InvalidUserException, OperationNotPermittedException, OperationFailedException
    {
        return crowdService.updateUser(user);
    }

    public void updateUserCredential(User user, String credential) throws UserNotFoundException, InvalidCredentialException, OperationNotPermittedException, OperationFailedException
    {
        crowdService.updateUserCredential(user, credential);
    }

    public void setUserAttribute(User user, String attributeName, String attributeValue) throws UserNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        crowdService.setUserAttribute(user, attributeName, attributeValue);
    }

    public void setUserAttribute(User user, String attributeName, Set<String> attributeValues) throws UserNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        crowdService.setUserAttribute(user, attributeName,  attributeValues);
    }

    public void removeUserAttribute(User user, String attributeName) throws UserNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        crowdService.removeUserAttribute(user, attributeName);
    }

    public void removeAllUserAttributes(User user) throws UserNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        crowdService.removeAllUserAttributes(user);
    }

    public boolean removeUser(User user) throws OperationNotPermittedException, OperationFailedException
    {
        return crowdService.removeUser(user);
    }

    public Group addGroup(Group group) throws InvalidGroupException, OperationNotPermittedException, OperationFailedException
    {
        return crowdService.addGroup(group);
    }

    public Group updateGroup(Group group) throws GroupNotFoundException, InvalidGroupException, OperationNotPermittedException, OperationFailedException
    {
        return crowdService.updateGroup(group);
    }

    public void setGroupAttribute(Group group, String attributeName, String attributeValue) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        crowdService.setGroupAttribute(group, attributeName, attributeValue);
    }

    public void setGroupAttribute(Group group, String attributeName, Set<String> attributeValues) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        crowdService.setGroupAttribute(group, attributeName, attributeValues);
    }

    public void removeGroupAttribute(Group group, String attributeName) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        crowdService.removeGroupAttribute(group, attributeName);
    }

    public void removeAllGroupAttributes(Group group) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        crowdService.removeAllGroupAttributes(group);
    }

    public boolean removeGroup(Group group) throws OperationNotPermittedException, OperationFailedException
    {
        return crowdService.removeGroup(group);
    }

    public void addUserToGroup(User user, Group group) throws GroupNotFoundException, UserNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        crowdService.addUserToGroup(user, group);
    }

    public void addGroupToGroup(Group childGroup, Group parentGroup) throws GroupNotFoundException, OperationNotPermittedException, InvalidMembershipException, OperationFailedException
    {
        crowdService.addGroupToGroup(childGroup, parentGroup);
    }

    public boolean removeUserFromGroup(User user, Group group) throws GroupNotFoundException, UserNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        return crowdService.removeUserFromGroup(user, group);
    }

    public boolean removeGroupFromGroup(Group childGroup, Group parentGroup) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        return crowdService.removeGroupFromGroup(childGroup, parentGroup);
    }

    public boolean isUserDirectGroupMember(User user, Group group) throws OperationFailedException
    {
        return crowdService.isUserDirectGroupMember(user, group);
    }

    public boolean isGroupDirectGroupMember(Group childGroup, Group parentGroup) throws OperationFailedException
    {
        return crowdService.isGroupDirectGroupMember(childGroup, parentGroup);
    }

    public Iterable<User> searchUsersAllowingDuplicateNames(Query<User> query)
    {
        return crowdService.searchUsersAllowingDuplicateNames(query);
    }
}
