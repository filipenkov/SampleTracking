package com.atlassian.crowd.plugin.rest.service.controller;

import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.plugin.rest.entity.*;
import com.atlassian.crowd.plugin.rest.util.EntityTranslator;
import com.atlassian.crowd.plugin.rest.util.GroupEntityUtil;
import com.atlassian.crowd.plugin.rest.util.LinkUriHelper;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.membership.MembershipQuery;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller for the Group resource.
 */
public class GroupsController extends AbstractResourceController
{
    public GroupsController(ApplicationService applicationService, ApplicationManager applicationManager)
    {
        super(applicationService, applicationManager);
    }

    /**
     * Returns the group specified by the name.
     *
     * @param applicationName name of the application
     * @param name name of the group to return
     * @param expandAttributes should attributes be expanded
     * @param baseURI base URI
     * @return group
     */
    public GroupEntity findGroupByName(String applicationName, String name, boolean expandAttributes, URI baseURI)
            throws GroupNotFoundException
    {
        final Application application = getApplication(applicationName);
        final GroupEntity minimalGroup = GroupEntity.newMinimalGroupEntity(name, applicationName, baseURI);

        return GroupEntityUtil.expandGroup(applicationService, application, minimalGroup, expandAttributes);
    }

    /**
     * Returns a group with attributes by the name.
     *
     * @param applicationName name of the application
     * @param name name of the group to return
     * @param baseURI base URI
     * @return group
     */
    private GroupEntity findGroupWithAttributesByName(String applicationName, String name, URI baseURI)
            throws GroupNotFoundException
    {
        Application application = getApplication(applicationName);

        GroupWithAttributes group = applicationService.findGroupWithAttributesByName(application, name);

        return EntityTranslator.toGroupEntity(group, group, LinkUriHelper.buildGroupLink(baseURI, group.getName()));
    }

    /**
     * Adds a new group.
     *
     * @param applicationName name of the application
     * @param group group to add
     * @return group name just added
     * @throws ApplicationPermissionException if none of the application's underlying directories are allowed to create
     *                                        a group
     * @throws OperationFailedException if the operation failed for any other reason
     */
    public String addGroup(String applicationName, GroupEntity group)
            throws ApplicationPermissionException, OperationFailedException, InvalidGroupException, GroupNotFoundException
    {
        Application application = getApplication(applicationName);

        return applicationService.addGroup(application, EntityTranslator.toGroup(group)).getName();
    }

    /**
     * Updates a group.
     *
     * @param applicationName name of the application
     * @param group group to update
     * @param baseURI base URI
     * @return group
     * @throws com.atlassian.crowd.exception.InvalidGroupException if the group already exists in ANY associated directory or the group template does not have the required properties populated.
     * @throws com.atlassian.crowd.exception.OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws ApplicationPermissionException if none of the application's associated directories are allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP}.
     * @throws com.atlassian.crowd.exception.GroupNotFoundException group does not exist in any of the associated directories of the application.
     */
    public GroupEntity updateGroup(String applicationName, GroupEntity group, URI baseURI)
            throws ApplicationPermissionException, OperationFailedException, InvalidGroupException, GroupNotFoundException
    {
        Application application = getApplication(applicationName);

        Group updatedGroup = applicationService.updateGroup(application, EntityTranslator.toGroup(group));

        return EntityTranslator.toGroupEntity(updatedGroup, baseURI);
    }

    /**
     * Removes a group.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @throws com.atlassian.crowd.exception.OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws com.atlassian.crowd.exception.GroupNotFoundException if group with given name does not exist in ANY assigned directory.
     * @throws ApplicationPermissionException if none of the application's associated directories are allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#DELETE_GROUP}.
     */
    public void removeGroup(String applicationName, String groupName)
            throws ApplicationPermissionException, OperationFailedException, GroupNotFoundException
    {
        Application application = getApplication(applicationName);

        applicationService.removeGroup(application, groupName);
    }

    /**
     * Stores the attributes for a group.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param attributes attributes to add/update
     * @throws com.atlassian.crowd.exception.GroupNotFoundException if the group with the supplied groupname does not exist in ANY assigned directory.
     * @throws com.atlassian.crowd.exception.OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws ApplicationPermissionException if none of the application's associated directories are allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP_ATTRIBUTE}.
     */
    public void storeGroupAttributes(String applicationName, String groupName, MultiValuedAttributeEntityList attributes)
            throws ApplicationPermissionException, OperationFailedException, GroupNotFoundException
    {
        Application application = getApplication(applicationName);
        if (attributes != null && !attributes.isEmpty())
        {
            Map<String, Set<String>> groupAttributes = EntityTranslator.toAttributes(attributes);
            applicationService.storeGroupAttributes(application, groupName, groupAttributes);
        }
    }

    /**
     * Removes a group's attribute.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param attributeName attribute to remove
     * @throws com.atlassian.crowd.exception.GroupNotFoundException if the group with the supplied groupname does not exist in ANY assigned directory.
     * @throws com.atlassian.crowd.exception.OperationFailedException underlying directory implementation failed to execute the operation.
     * @throws ApplicationPermissionException if none of the application's associated directories are allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP_ATTRIBUTE}.
     */
    public void removeGroupAttributes(String applicationName, String groupName, String attributeName)
            throws ApplicationPermissionException, OperationFailedException, GroupNotFoundException
    {
        Application application = getApplication(applicationName);

        applicationService.removeGroupAttributes(application, groupName, attributeName);
    }

    /**
     * Retrieves the users that are direct members of the specified group.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param expandUsers should users be expanded
     * @param maxResults maximum number of results returned. If -1, then all the results are returned.
     * @param startIndex starting index of the results
     * @param baseUri base URI
     * @return users that are direct members of the specified group
     */
    public UserEntityList getDirectUsers(String applicationName, String groupName, boolean expandUsers, int maxResults, int startIndex, URI baseUri)
    {
        Application application = getApplication(applicationName);

        if (expandUsers)
        {
            MembershipQuery<User> query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(groupName).startingAt(startIndex).returningAtMost(maxResults);
            List<User> users = applicationService.searchDirectGroupRelationships(application, query);

            return EntityTranslator.toUserEntities(users, baseUri);
        }
        else
        {
            MembershipQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(groupName).startingAt(startIndex).returningAtMost(maxResults);
            List<String> usernames = applicationService.searchDirectGroupRelationships(application, query);

            return EntityTranslator.toMinimalUserEntities(usernames, baseUri);
        }
    }

    /**
     * Adds user as direct member of group.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param username name of the child group
     * @throws ApplicationPermissionException if none of the application's associated directories are allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP}.
     */
    public void addDirectUser(String applicationName, String groupName, String username)
            throws ApplicationPermissionException, UserNotFoundException, GroupNotFoundException, OperationFailedException
    {
        Application application = getApplication(applicationName);

        applicationService.addUserToGroup(application, username, groupName);
    }

    /**
     * Retrieves the user that is a direct member of the specified group.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param username name of the child group
     * @param baseUri base URI
     * @return user that is a direct member of the specified group
     */
    public UserEntity getDirectUser(String applicationName, String groupName, String username, URI baseUri)
            throws MembershipNotFoundException
    {
        Application application = getApplication(applicationName);

        final boolean isMember = applicationService.isUserDirectGroupMember(application, username, groupName);

        if (!isMember)
        {
            throw new MembershipNotFoundException(username, groupName);
        }

        return UserEntity.newMinimalUserEntity(username, null, LinkUriHelper.buildUserLink(baseUri, username));
    }

    /**
     * Removes the user membership.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param username name of the user
     * @throws ApplicationPermissionException if none of the application's associated directories are allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP}.
     */
    public void deleteDirectUser(String applicationName, String groupName, String username)
            throws ApplicationPermissionException, MembershipNotFoundException, UserNotFoundException, GroupNotFoundException, OperationFailedException
    {
        Application application = getApplication(applicationName);

        applicationService.removeUserFromGroup(application, username, groupName);
    }

    /**
     * Retrieves the users that are nested members of the specified group.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param expandUsers should groups be expanded
     * @param maxResults maximum number of results returned. If -1, then all the results are returned.
     * @param startIndex starting index of the results
     * @param baseUri base URI
     * @return users that are nested members of the specified group
     */
    public UserEntityList getNestedUsers(String applicationName, String groupName, boolean expandUsers, int maxResults, int startIndex, URI baseUri)
    {
        Application application = getApplication(applicationName);

        if (expandUsers)
        {
            MembershipQuery<User> query = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(groupName).startingAt(startIndex).returningAtMost(maxResults);
            List<User> users = applicationService.searchNestedGroupRelationships(application, query);

            return EntityTranslator.toUserEntities(users, baseUri);
        }
        else
        {
            MembershipQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).childrenOf(EntityDescriptor.group()).withName(groupName).startingAt(startIndex).returningAtMost(maxResults);
            List<String> usernames = applicationService.searchNestedGroupRelationships(application, query);

            return EntityTranslator.toMinimalUserEntities(usernames, baseUri);
        }
    }

    /**
     * Retrieves the user that is a nested member of the specified group.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param username name of the user
     * @param baseUri base URI
     * @return user that is a nested member of the specified group
     */
    public UserEntity getNestedUser(String applicationName, String groupName, String username, URI baseUri)
            throws MembershipNotFoundException
    {
        Application application = getApplication(applicationName);

        final boolean isMember = applicationService.isUserNestedGroupMember(application, username, groupName);

        if (!isMember)
        {
            throw new MembershipNotFoundException(username, groupName);
        }

        return UserEntity.newMinimalUserEntity(username, null, LinkUriHelper.buildUserLink(baseUri, username));
    }

    /**
     * 	 Retrieves the groups that are direct parents of the specified group.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param expandGroups should groups be expanded
     * @param maxResults maximum number of results returned. If -1, then all the results are returned.
     * @param startIndex starting index of the results
     * @param baseUri base URI
     * @return groups that are direct parents of the specified group
     */
    public GroupEntityList getDirectParentGroups(String applicationName, String groupName, boolean expandGroups, int maxResults, int startIndex, URI baseUri)
    {
        Application application = getApplication(applicationName);

        if (expandGroups)
        {
            MembershipQuery<Group> query = QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.group()).withName(groupName).startingAt(startIndex).returningAtMost(maxResults);
            List<Group> groups =  applicationService.searchDirectGroupRelationships(application, query);

            return EntityTranslator.toGroupEntities(groups, baseUri);
        }
        else
        {
            MembershipQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.group()).withName(groupName).startingAt(startIndex).returningAtMost(maxResults);
            List<String> groupNames =  applicationService.searchDirectGroupRelationships(application, query);

            return EntityTranslator.toMinimalGroupEntities(groupNames, baseUri);
        }
    }

    /**
     * Adds a direct parent group membership.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param parentGroupName name of the parent group
     * @throws ApplicationPermissionException if none of the application's associated directories are allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP}.
     */
    public void addDirectParentGroup(String applicationName, String groupName, String parentGroupName)
            throws ApplicationPermissionException, InvalidMembershipException, GroupNotFoundException, OperationFailedException
    {
        Application application = getApplication(applicationName);

        applicationService.addGroupToGroup(application, groupName, parentGroupName);
    }

    /**
     * Retrieves the group that is a direct parent of the specified group.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param parentGroupName name of the child group
     * @param baseUri base URI
     * @return group that is a direct parent of the specified group
     */
    public GroupEntity getDirectParentGroup(String applicationName, String groupName, String parentGroupName, URI baseUri)
            throws MembershipNotFoundException
    {
        Application application = getApplication(applicationName);

        final boolean isMember = applicationService.isGroupDirectGroupMember(application, groupName, parentGroupName);

        if (!isMember)
        {
            throw new MembershipNotFoundException(groupName, parentGroupName);
        }

        return GroupEntity.newMinimalGroupEntity(parentGroupName, null, baseUri);
    }

    /**
     * Retrieves the groups that are nested parents of the specified group.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param expandGroups should groups be expanded
     * @param maxResults maximum number of results returned. If -1, then all the results are returned.
     * @param startIndex starting index of the results
     * @param baseUri base URI
     * @return groups that are nested parents of the specified group
     */
    public GroupEntityList getNestedParentGroups(String applicationName, String groupName, boolean expandGroups, int maxResults, int startIndex, URI baseUri)
    {
        Application application = getApplication(applicationName);

        if (expandGroups)
        {
            MembershipQuery<Group> query = QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.group()).withName(groupName).startingAt(startIndex).returningAtMost(maxResults);
            List<Group> groups =  applicationService.searchNestedGroupRelationships(application, query);

            return EntityTranslator.toGroupEntities(groups, baseUri);
        }
        else
        {
            MembershipQuery<String> query = QueryBuilder.queryFor(String.class, EntityDescriptor.group()).parentsOf(EntityDescriptor.group()).withName(groupName).startingAt(startIndex).returningAtMost(maxResults);
            List<String> groupNames =  applicationService.searchNestedGroupRelationships(application, query);

            return EntityTranslator.toMinimalGroupEntities(groupNames, baseUri);
        }
    }

    /**
     * Retrieves the group that is a nested parent of the specified group.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param parentGroupName name of the parent group
     * @param baseUri base URI
     * @return group that is a nested parent of the specified group
     */
    public GroupEntity getNestedParentGroup(String applicationName, String groupName, String parentGroupName, URI baseUri)
            throws MembershipNotFoundException
    {
        Application application = getApplication(applicationName);

        final boolean isMember = applicationService.isGroupNestedGroupMember(application, groupName, parentGroupName);

        if (!isMember)
        {
            throw new MembershipNotFoundException(groupName, parentGroupName);
        }

        return GroupEntity.newMinimalGroupEntity(parentGroupName, null, baseUri);
    }

    /**
     * Retrieves the groups that are direct members of the specified group.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param expandGroups should groups be expanded
     * @param maxResults maximum number of results returned. If -1, then all the results are returned.
     * @param startIndex starting index of the results
     * @param baseUri base URI
     * @return groups that are direct members of the specified group
     */
    public GroupEntityList getDirectChildGroups(String applicationName, String groupName, boolean expandGroups, int maxResults, int startIndex, URI baseUri)
    {
        Application application = getApplication(applicationName);

        if (expandGroups)
        {
            MembershipQuery<Group> query = QueryBuilder.createMembershipQuery(maxResults, startIndex, true, EntityDescriptor.group(), Group.class, EntityDescriptor.group(), groupName);
            List<Group> groups = applicationService.searchDirectGroupRelationships(application, query);

            return EntityTranslator.toGroupEntities(groups, baseUri);
        }
        else
        {
            MembershipQuery<String> query = QueryBuilder.createMembershipQuery(maxResults, startIndex, true, EntityDescriptor.group(), String.class, EntityDescriptor.group(), groupName);
            List<String> groupNames = applicationService.searchDirectGroupRelationships(application, query);

            return EntityTranslator.toMinimalGroupEntities(groupNames, baseUri);
        }
    }

    /**
     * Adds a direct child group membership.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param childGroupName name of the child group
     * @throws ApplicationPermissionException if none of the application's associated directories are allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP}.
     */
    public void addDirectChildGroup(String applicationName, String groupName, String childGroupName)
            throws ApplicationPermissionException, InvalidMembershipException, GroupNotFoundException, OperationFailedException
    {
        Application application = getApplication(applicationName);

        applicationService.addGroupToGroup(application, childGroupName, groupName);
    }

    /**
     * Retrieves the group that is a direct child of the specified group.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param childGroupName name of the child group
     * @param baseUri base URI
     * @return group that is a direct child of the specified group
     */
    public GroupEntity getDirectChildGroup(String applicationName, String groupName, String childGroupName, URI baseUri)
            throws MembershipNotFoundException
    {
        Application application = getApplication(applicationName);

        final boolean isMember = applicationService.isGroupDirectGroupMember(application, childGroupName, groupName);

        if (!isMember)
        {
            throw new MembershipNotFoundException(childGroupName, groupName);
        }

        return GroupEntity.newMinimalGroupEntity(childGroupName, null, baseUri);
    }

    /**
     * Deletes a child group membership.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param childGroupName name of the child group
     * @throws ApplicationPermissionException if none of the application's associated directories are allowed to perform operations of type {@link com.atlassian.crowd.embedded.api.OperationType#UPDATE_GROUP}.
     */
    public void deleteDirectChildGroup(String applicationName, String groupName, String childGroupName)
            throws ApplicationPermissionException, MembershipNotFoundException, GroupNotFoundException, OperationFailedException
    {
        Application application = getApplication(applicationName);

        applicationService.removeGroupFromGroup(application, childGroupName, groupName);
    }

    /**
     * Retrieves the groups that are nested children of the specified group.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param expandGroups should groups be expanded
     * @param maxResults maximum number of results returned. If -1, then all the results are returned.
     * @param startIndex starting index of the results
     * @param baseUri base URI
     * @return groups that are nested children of the specified group
     */
    public GroupEntityList getNestedChildGroups(String applicationName, String groupName, boolean expandGroups, int maxResults, int startIndex, URI baseUri)
    {
        Application application = getApplication(applicationName);

        if (expandGroups)
        {
            MembershipQuery<Group> query = QueryBuilder.createMembershipQuery(maxResults, startIndex, true, EntityDescriptor.group(), Group.class, EntityDescriptor.group(), groupName);
            List<Group> groups = applicationService.searchNestedGroupRelationships(application, query);

            return EntityTranslator.toGroupEntities(groups, baseUri);
        }
        else
        {
            MembershipQuery<String> query = QueryBuilder.createMembershipQuery(maxResults, startIndex, true, EntityDescriptor.group(), String.class, EntityDescriptor.group(), groupName);
            List<String> groupNames = applicationService.searchNestedGroupRelationships(application, query);

            return EntityTranslator.toMinimalGroupEntities(groupNames, baseUri);
        }
    }

    /**
     * Retrieves the group that is a nested child of the specified group.
     *
     * @param applicationName name of the application
     * @param groupName name of the group
     * @param childGroupName name of the child group
     * @param baseUri base URI
     * @return group that is a nested child of the specified group
     */
    public GroupEntity getNestedChildGroup(String applicationName, String groupName, String childGroupName, URI baseUri)
            throws MembershipNotFoundException
    {
        Application application = getApplication(applicationName);

        final boolean isMember = applicationService.isGroupNestedGroupMember(application, childGroupName, groupName);

        if (!isMember)
        {
            throw new MembershipNotFoundException(childGroupName, groupName);
        }

        return GroupEntity.newMinimalGroupEntity(childGroupName, null, baseUri);
    }
}
