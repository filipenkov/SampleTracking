package com.atlassian.crowd.embedded.core;

import com.atlassian.crowd.directory.loader.DirectoryInstanceLoader;
import com.atlassian.crowd.embedded.api.ApplicationFactory;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.GroupWithAttributes;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.Query;
import com.atlassian.crowd.embedded.api.UnfilteredCrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.api.UserWithAttributes;
import com.atlassian.crowd.embedded.core.util.ConversionUtils;
import com.atlassian.crowd.exception.AccountNotFoundException;
import com.atlassian.crowd.exception.FailedAuthenticationException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidMembershipException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.crowd.exception.runtime.CommunicationException;
import com.atlassian.crowd.exception.runtime.GroupNotFoundException;
import com.atlassian.crowd.exception.runtime.MembershipNotFoundException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.crowd.exception.runtime.UserNotFoundException;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.search.query.entity.GroupQuery;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of the CrowdService
 */
public class CrowdServiceImpl implements UnfilteredCrowdService
{
    private final ApplicationService applicationService;
    private final ApplicationFactory applicationFactory;

    public CrowdServiceImpl(ApplicationFactory applicationFactory, ApplicationService applicationService, DirectoryInstanceLoader directoryInstanceLoader)
    {
        this.applicationFactory = checkNotNull(applicationFactory);
        this.applicationService = checkNotNull(applicationService);
        // TODO: Remove DirectoryInstanceLoader parameter from the constructor.
    }

    public User authenticate(String name, String credential) throws FailedAuthenticationException, OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            throw new FailedAuthenticationException("No application to authenticate user against.");
        }
        try
        {
            return applicationService.authenticateUser(application, name, PasswordCredential.unencrypted(credential));
        }
        catch (com.atlassian.crowd.exception.UserNotFoundException e)
        {
            throw new AccountNotFoundException(e.getUserName(), e);
        }
        catch (InvalidAuthenticationException e)
        {
            throw new FailedAuthenticationException(e);
        }
        catch (com.atlassian.crowd.exception.OperationFailedException ex)
        {
            throw convertOperationFailedException(ex);
        }
    }

    public User getUser(String name)
    {
        Application application = getApplication();
        if (application == null)
        {
            return null;
        }
        try
        {
            return applicationService.findUserByName(application, name);
        }
        catch (com.atlassian.crowd.exception.UserNotFoundException e)
        {
            return null;
        }
    }

    public UserWithAttributes getUserWithAttributes(String name)
    {
        Application application = getApplication();
        if (application == null)
        {
            return null;
        }
        try
        {
            return applicationService.findUserWithAttributesByName(application, name);
        }
        catch (com.atlassian.crowd.exception.UserNotFoundException e)
        {
            return null;
        }
    }

    public Group getGroup(String name)
    {
        Application application = getApplication();
        if (application == null)
        {
            return null;
        }
        try
        {
            return ConversionUtils.toEmbeddedGroup(applicationService.findGroupByName(application, name));
        }
        catch (com.atlassian.crowd.exception.GroupNotFoundException e)
        {
            return null;
        }
    }

    public GroupWithAttributes getGroupWithAttributes(String name)
    {
        Application application = getApplication();
        if (application == null)
        {
            return null;
        }
        try
        {
            return ConversionUtils.toEmbeddedGroupWithAttributes(applicationService.findGroupWithAttributesByName(application, name));
        }
        catch (com.atlassian.crowd.exception.GroupNotFoundException e)
        {
            return null;
        }
    }

    public <T> Iterable<T> search(Query<T> query)
    {
        checkNotNull(query, "You cannot query with a null query object");

        if (MembershipQuery.class.isInstance(query))
        {
            return searchNestedGroupRelationships((MembershipQuery<T>) query);
        }
        else if (UserQuery.class.isInstance(query))
        {
            return searchUsers((UserQuery<T>) query);
        }
        else if (GroupQuery.class.isInstance(query))
        {
            return searchGroups((GroupQuery<T>) query);
        }
        else
        {
            throw new IllegalArgumentException("Cannot search with a query of type <" + query.getClass().getName() + ">");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> searchUsers(UserQuery<T> query)
    {
        Application application = getApplication();
        if (application == null)
        {
            return(List<T>) new ArrayList<User>();
        }
        if (String.class.equals(query.getReturnType()))
        {
            return applicationService.searchUsers(application, query);
        }
        else if (User.class.equals((query.getReturnType())))
        {
            List<com.atlassian.crowd.model.user.User> modelUsers = applicationService.searchUsers(application, ConversionUtils.toModelUserQuery(query));

            // embedded.User is superinterface of model.User, so no conversion needed
            return (List<T>) new ArrayList<User>(modelUsers);
        }
        else
        {
            throw new IllegalArgumentException("User search queries can only be specified to return String or " + User.class.getCanonicalName() + " objects");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> searchGroups(GroupQuery<T> query)
    {
        Application application = getApplication();
        if (application == null)
        {
            return(List<T>) new ArrayList<User>();
        }
        if (String.class.equals(query.getReturnType()))
        {
            return applicationService.searchGroups(application, query);
        }
        else if (Group.class.equals((query.getReturnType())))
        {
            List<com.atlassian.crowd.model.group.Group> modelGroups = applicationService.searchGroups(application, ConversionUtils.toModelGroupQuery(query));

            // explicit conversion needed
            return (List<T>) ConversionUtils.toEmbeddedGroups(modelGroups);
        }
        else
        {
            throw new IllegalArgumentException("Group search queries can only be specified to return String or " + Group.class.getCanonicalName() + " objects");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> searchNestedGroupRelationships(MembershipQuery<T> query)
    {
        Application application = getApplication();
        if (application == null)
        {
            return(List<T>) new ArrayList<User>();
        }
        if (String.class.equals(query.getReturnType()))
        {
            return applicationService.searchNestedGroupRelationships(application, query);
        }
        else if (User.class.equals((query.getReturnType())))
        {
            List<com.atlassian.crowd.model.user.User> modelUsers = applicationService.searchNestedGroupRelationships(application, ConversionUtils.toModelUserMembershipQuery(query));

            return (List<T>) ConversionUtils.toEmbeddedUsers(modelUsers);
        }
        else if (Group.class.equals((query.getReturnType())))
        {
            List<com.atlassian.crowd.model.group.Group> modelGroups = applicationService.searchNestedGroupRelationships(application, ConversionUtils.toModelGroupMembershipQuery(query));

            // explicit conversion needed
            return (List<T>) ConversionUtils.toEmbeddedGroups(modelGroups);
        }
        else
        {
            throw new IllegalArgumentException("Membership search queries can only be specified to return String, " + User.class.getCanonicalName() + ", or " + Group.class.getCanonicalName() + " objects");
        }
    }

    public boolean isUserMemberOfGroup(String userName, String groupName)
    {
        Application application = getApplication();
        if (application == null)
        {
            return false;
        }
        return applicationService.isUserNestedGroupMember(application, userName, groupName);
    }

    public boolean isUserMemberOfGroup(User user, Group group)
    {
        return isUserMemberOfGroup(user.getName(), group.getName());
    }

    public boolean isGroupMemberOfGroup(String childGroupName, String parentGroupName)
    {
        Application application = getApplication();
        if (application == null)
        {
            return false;
        }
        return applicationService.isGroupNestedGroupMember(application, childGroupName, parentGroupName);
    }

    public boolean isGroupMemberOfGroup(Group childGroup, Group parentGroup)
    {
        return isGroupMemberOfGroup(childGroup.getName(), parentGroup.getName());
    }

    public User addUser(User user, String credential) throws InvalidUserException, InvalidCredentialException, OperationNotPermittedException, OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            throw new OperationFailedException("No Crowd Application available.");
        }
        try
        {
            return applicationService.addUser(application, new UserTemplate(user), new PasswordCredential(credential));
        }
        catch (com.atlassian.crowd.exception.ApplicationPermissionException e)
        {
            throw new OperationNotPermittedException(e);
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    public User updateUser(User user) throws UserNotFoundException, InvalidUserException, OperationNotPermittedException, OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            throw new OperationFailedException("No Crowd Application available.");
        }
        try
        {
            return applicationService.updateUser(application, new UserTemplate(user));
        }
        catch (com.atlassian.crowd.exception.ApplicationPermissionException e)
        {
            throw new OperationNotPermittedException(e);
        }
        catch (com.atlassian.crowd.exception.UserNotFoundException e)
        {
            // re-throw as runtime UserNotFoundException
            throw new UserNotFoundException(e.getUserName(), e.getCause());
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    public void updateUserCredential(User user, String credential) throws UserNotFoundException, InvalidCredentialException, OperationNotPermittedException, OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            throw new OperationFailedException("No Crowd Application available.");
        }
        try
        {
            applicationService.updateUserCredential(application, user.getName(), new PasswordCredential(credential));
        }
        catch (com.atlassian.crowd.exception.ApplicationPermissionException e)
        {
            throw new OperationNotPermittedException(e);
        }
        catch (com.atlassian.crowd.exception.UserNotFoundException e)
        {
            // re-throw as runtime UserNotFoundException
            throw new UserNotFoundException(e.getUserName(), e.getCause());
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    public void setUserAttribute(User user, String attributeName, String attributeValue) throws UserNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        final Set<String> attributeValues = Sets.newHashSet(attributeValue);
        setUserAttribute(user, attributeName, attributeValues);
    }

    public void setUserAttribute(User user, String attributeName, Set<String> attributeValues) throws UserNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            throw new OperationFailedException("No Crowd Application available.");
        }
        try
        {
            applicationService.storeUserAttributes(application, user.getName(), buildAttributesAsMap(attributeName, attributeValues));
        }
        catch (com.atlassian.crowd.exception.ApplicationPermissionException e)
        {
            throw new OperationNotPermittedException(e);
        }
        catch (com.atlassian.crowd.exception.UserNotFoundException e)
        {
            // re-throw as runtime UserNotFoundException
            throw new UserNotFoundException(e.getUserName(), e.getCause());
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    private Map<String, Set<String>> buildAttributesAsMap(String attributeName, Set<String> attributeValues)
    {
        return new ImmutableMap.Builder<String, Set<String>>().put(attributeName, attributeValues).build();
    }

    public void removeUserAttribute(User user, String attributeName) throws UserNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            throw new OperationFailedException("No Crowd Application available.");
        }
        try
        {
            applicationService.removeUserAttributes(application, user.getName(), attributeName);
        }
        catch (com.atlassian.crowd.exception.ApplicationPermissionException e)
        {
            throw new OperationNotPermittedException(e);
        }
        catch (com.atlassian.crowd.exception.UserNotFoundException e)
        {
            // re-throw as runtime UserNotFoundException
            throw new UserNotFoundException(e.getUserName(), e.getCause());
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    public void removeAllUserAttributes(User user) throws UserNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            throw new OperationFailedException("No Crowd Application available.");
        }
        final String userName = user.getName();
        final UserWithAttributes userWithAttributes = getUserWithAttributes(userName);
        final Set<String> attributeNames = userWithAttributes.getKeys();

        try
        {
            for (String attributeName : attributeNames)
            {
                applicationService.removeUserAttributes(application, userName, attributeName);
            }
        }
        catch (com.atlassian.crowd.exception.ApplicationPermissionException e)
        {
            throw new OperationNotPermittedException(e);
        }
        catch (com.atlassian.crowd.exception.UserNotFoundException e)
        {
            // re-throw as runtime UserNotFoundException
            throw new UserNotFoundException(e.getUserName(), e.getCause());
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    public boolean removeUser(User user) throws OperationNotPermittedException, OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            throw new OperationFailedException("No Crowd Application available.");
        }
        try
        {
            applicationService.removeUser(application, user.getName());
            return true;
        }
        catch (com.atlassian.crowd.exception.UserNotFoundException e)
        {
            return false;
        }
        catch (com.atlassian.crowd.exception.ApplicationPermissionException e)
        {
            throw new OperationNotPermittedException(e);
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    public Group addGroup(Group group) throws InvalidGroupException, OperationNotPermittedException, OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            throw new OperationFailedException("No Crowd Application available.");
        }
        try
        {
            return ConversionUtils.toEmbeddedGroup(applicationService.addGroup(application, new GroupTemplate(group)));
        }
        catch (com.atlassian.crowd.exception.InvalidGroupException ex)
        {
            // Convert the model.Group based InvalidGroupException into an embedded.Group based one
            throw new InvalidGroupException(ConversionUtils.getEmbeddedGroup(ex), ex);
        }
        catch (com.atlassian.crowd.exception.ApplicationPermissionException e)
        {
            throw new OperationNotPermittedException(e);
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    public Group updateGroup(Group group) throws InvalidGroupException, OperationNotPermittedException, GroupNotFoundException, OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            throw new OperationFailedException("No Crowd Application available.");
        }
        try
        {
            return ConversionUtils.toEmbeddedGroup(applicationService.updateGroup(application, new GroupTemplate(group)));
        }
        catch (com.atlassian.crowd.exception.InvalidGroupException ex)
        {
            // Convert the model.Group based InvalidGroupException into an embedded.Group based one
            throw new InvalidGroupException(ConversionUtils.getEmbeddedGroup(ex), ex);
        }
        catch (com.atlassian.crowd.exception.ApplicationPermissionException e)
        {
            throw new OperationNotPermittedException(e);
        }
        catch (com.atlassian.crowd.exception.GroupNotFoundException e)
        {
            // re-throw as runtime GroupNotFoundException
            throw new GroupNotFoundException(e.getGroupName(), e.getCause());
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    public void setGroupAttribute(Group group, String attributeName, String attributeValue) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        final Set<String> attributeValues = Sets.newHashSet(attributeValue);
        setGroupAttribute(group, attributeName, attributeValues);
    }

    public void setGroupAttribute(Group group, String attributeName, Set<String> attributeValues) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            throw new OperationFailedException("No Crowd Application available.");
        }
        try
        {
            applicationService.storeGroupAttributes(application, group.getName(), buildAttributesAsMap(attributeName, attributeValues));
        }
        catch (com.atlassian.crowd.exception.ApplicationPermissionException e)
        {
            throw new OperationNotPermittedException(e);
        }
        catch (com.atlassian.crowd.exception.GroupNotFoundException e)
        {
            // re-throw as runtime GroupNotFoundException
            throw new GroupNotFoundException(e.getGroupName(), e.getCause());
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    public void removeGroupAttribute(Group group, String attributeName) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            throw new OperationFailedException("No Crowd Application available.");
        }
        try
        {
            applicationService.removeGroupAttributes(application, group.getName(), attributeName);
        }
        catch (com.atlassian.crowd.exception.ApplicationPermissionException e)
        {
            throw new OperationNotPermittedException(e);
        }
        catch (com.atlassian.crowd.exception.GroupNotFoundException e)
        {
            // re-throw as runtime GroupNotFoundException
            throw new GroupNotFoundException(e.getGroupName(), e.getCause());
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    public void removeAllGroupAttributes(Group group) throws GroupNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            throw new OperationFailedException("No Crowd Application available.");
        }
        final String groupName = group.getName();
        final com.atlassian.crowd.embedded.api.GroupWithAttributes groupWithAttributes = getGroupWithAttributes(groupName);
        final Set<String> attributeNames = groupWithAttributes.getKeys();

        try
        {
            for (String attributeName : attributeNames)
            {
                applicationService.removeGroupAttributes(application, groupName, attributeName);
            }
        }
        catch (com.atlassian.crowd.exception.ApplicationPermissionException e)
        {
            throw new OperationNotPermittedException(e);
        }
        catch (com.atlassian.crowd.exception.GroupNotFoundException e)
        {
            // re-throw as runtime GroupNotFoundException
            throw new GroupNotFoundException(e.getGroupName(), e.getCause());
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    public boolean removeGroup(Group group) throws OperationNotPermittedException, GroupNotFoundException, OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            throw new OperationFailedException("No Crowd Application available.");
        }
        try
        {
            applicationService.removeGroup(application, group.getName());
            return true;
        }
        catch (com.atlassian.crowd.exception.GroupNotFoundException e)
        {
            return false;
        }
        catch (com.atlassian.crowd.exception.ApplicationPermissionException e)
        {
            throw new OperationNotPermittedException(e);
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    public void addUserToGroup(User user, Group group) throws GroupNotFoundException, UserNotFoundException, OperationNotPermittedException, OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            throw new OperationFailedException("No Crowd Application available.");
        }
        try
        {
            applicationService.addUserToGroup(application, user.getName(), group.getName());
        }
        catch (com.atlassian.crowd.exception.ApplicationPermissionException e)
        {
            throw new OperationNotPermittedException(e);
        }
        catch (com.atlassian.crowd.exception.UserNotFoundException e)
        {
            // re-throw as runtime UserNotFoundException
            throw new UserNotFoundException(e.getUserName(), e.getCause());
        }
        catch (com.atlassian.crowd.exception.GroupNotFoundException e)
        {
            // re-throw as runtime GroupNotFoundException
            throw new GroupNotFoundException(e.getGroupName(), e.getCause());
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    public void addGroupToGroup(Group childGroup, Group parentGroup) throws GroupNotFoundException, OperationNotPermittedException, InvalidMembershipException, OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            throw new OperationFailedException("No Crowd Application available.");
        }
        try
        {
            applicationService.addGroupToGroup(application, childGroup.getName(), parentGroup.getName());
        }
        catch (com.atlassian.crowd.exception.ApplicationPermissionException e)
        {
            throw new OperationNotPermittedException(e);
        }
        catch (com.atlassian.crowd.exception.GroupNotFoundException e)
        {
            // re-throw as runtime GroupNotFoundException
            throw new GroupNotFoundException(e.getGroupName(), e.getCause());
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    public boolean removeUserFromGroup(User user, Group group) throws GroupNotFoundException, UserNotFoundException, OperationNotPermittedException, MembershipNotFoundException, OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            throw new OperationFailedException("No Crowd Application available.");
        }
        try
        {
            applicationService.removeUserFromGroup(application, user.getName(), group.getName());
            return true;
        }
        catch (com.atlassian.crowd.exception.MembershipNotFoundException e)
        {
            return false;
        }
        catch (com.atlassian.crowd.exception.ApplicationPermissionException e)
        {
            throw new OperationNotPermittedException(e);
        }
        catch (com.atlassian.crowd.exception.UserNotFoundException e)
        {
            // re-throw as runtime UserNotFoundException
            throw new UserNotFoundException(e.getUserName(), e.getCause());
        }
        catch (com.atlassian.crowd.exception.GroupNotFoundException e)
        {
            // re-throw as runtime GroupNotFoundException
            throw new GroupNotFoundException(e.getGroupName(), e.getCause());
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    public boolean removeGroupFromGroup(Group childGroup, Group parentGroup) throws GroupNotFoundException, OperationNotPermittedException, MembershipNotFoundException, OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            throw new OperationFailedException("No Crowd Application available.");
        }
        try
        {
            applicationService.removeGroupFromGroup(application, childGroup.getName(), parentGroup.getName());
            return true;
        }
        catch (com.atlassian.crowd.exception.MembershipNotFoundException e)
        {
            return false;
        }
        catch (com.atlassian.crowd.exception.ApplicationPermissionException e)
        {
            throw new OperationNotPermittedException(e);
        }
        catch (com.atlassian.crowd.exception.GroupNotFoundException e)
        {
            // re-throw as runtime GroupNotFoundException
            throw new GroupNotFoundException(e.getGroupName(), e.getCause());
        }
        catch (com.atlassian.crowd.exception.OperationFailedException e)
        {
            // re-throw as runtime OperationFailedException
            throw new OperationFailedException(e.getMessage(), e.getCause());
        }
    }

    public boolean isUserDirectGroupMember(User user, Group group) throws OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            return false;
        }
        return applicationService.isUserDirectGroupMember(application, user.getName(), group.getName());
    }

    public boolean isGroupDirectGroupMember(Group childGroup, Group parentGroup) throws OperationFailedException
    {
        Application application = getApplication();
        if (application == null)
        {
            return false;
        }
        return applicationService.isGroupDirectGroupMember(application, childGroup.getName(), parentGroup.getName());
    }

    /**
     * {@inheritDoc}
     */
    public Iterable<User> searchUsersAllowingDuplicateNames(final Query<User> query)
    {
        Application application = getApplication();
        if (application == null)
        {
            return new ArrayList<User>();
        }
        if (UserQuery.class.isInstance(query))
        {
            List<com.atlassian.crowd.model.user.User> modelUsers = applicationService.searchUsersAllowingDuplicateNames(application, ConversionUtils.toModelUserQuery((UserQuery<User>)query));

            return ConversionUtils.toEmbeddedUsers(modelUsers);
        }
        else
        {
            throw new IllegalArgumentException("Cannot search with a query of type <" + query.getClass().getName() + ">");
        }
    }
    
    private Application getApplication()
    {
        return applicationFactory.getApplication();
    }

    /**
     * Converts a checked OperationFailedException that is thrown by Crowd core into a runtime OperationFailedException
     * as is thrown by the CrowdService API.
     * This is also able to detect if the original OperationFailedException was caused by a communication error with the
     * remote user Server (LDAP or Crowd/REST), and will return a {@link CommunicationException} to indicate this.
     *
     * @param ex the original checked OperationFailedException
     * @return a runtime OperationFailedException (or subclass).
     */
    private OperationFailedException convertOperationFailedException(final com.atlassian.crowd.exception.OperationFailedException ex)
    {
        Throwable cause = ex.getCause();
        if (cause == null)
        {
            return new OperationFailedException(ex.getMessage(), ex);
        }
        // look for known exceptions
        if (cause instanceof org.springframework.ldap.CommunicationException)
        {
            // Failure to communicate with with remote LDAP server
            return new CommunicationException(cause);
        }
        if (cause instanceof java.net.ConnectException)
        {
            // Failure to communicate with with remote Crowd/REST server
            return new CommunicationException(cause);
        }
        // Unknown error
        return new OperationFailedException(cause);
    }

}
