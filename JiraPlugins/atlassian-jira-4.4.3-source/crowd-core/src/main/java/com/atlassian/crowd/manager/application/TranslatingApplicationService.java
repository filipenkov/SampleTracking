package com.atlassian.crowd.manager.application;

import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.Query;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.event.EventTokenExpiredException;
import com.atlassian.crowd.event.Events;
import com.atlassian.crowd.event.IncrementalSynchronisationNotAvailableException;
import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.BulkAddFailedException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidEmailAddressException;
import com.atlassian.crowd.exception.InvalidGroupException;
import com.atlassian.crowd.exception.InvalidMembershipException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.MembershipNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.NameComparator;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.event.GroupEvent;
import com.atlassian.crowd.model.event.GroupMembershipEvent;
import com.atlassian.crowd.model.event.OperationEvent;
import com.atlassian.crowd.model.event.UserEvent;
import com.atlassian.crowd.model.event.UserMembershipEvent;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupTemplate;
import com.atlassian.crowd.model.group.GroupTemplateWithAttributes;
import com.atlassian.crowd.model.group.GroupWithAttributes;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.crowd.model.user.UserTemplateWithAttributes;
import com.atlassian.crowd.model.user.UserTemplateWithCredentialAndAttributes;
import com.atlassian.crowd.model.user.UserWithAttributes;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.builder.Restriction;
import com.atlassian.crowd.search.query.QueryUtils;
import com.atlassian.crowd.search.query.entity.AliasQuery;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.UserQuery;
import com.atlassian.crowd.search.query.entity.restriction.BooleanRestriction;
import com.atlassian.crowd.search.query.entity.restriction.BooleanRestrictionImpl;
import com.atlassian.crowd.search.query.entity.restriction.MatchMode;
import com.atlassian.crowd.search.query.entity.restriction.PropertyRestriction;
import com.atlassian.crowd.search.query.entity.restriction.TermRestriction;
import com.atlassian.crowd.search.query.entity.restriction.constants.AliasTermKeys;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import com.atlassian.crowd.search.query.membership.MembershipQuery;
import com.atlassian.crowd.search.util.SearchResultsUtil;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;

/**
 * This class is responsible for translating application specific usernames
 * (aliased usernames) to directory specific usernames (unaliased usernames)
 * and the other way around. It also translates outgoing usernames and groups
 * names to lower case if required by the application.
 * 
 * Usernames in the incoming parameters are converted from aliased usernames
 * to unaliased usernames. Usernames in return values are converted to aliased
 * usernames.
 */
public class TranslatingApplicationService implements ApplicationService
{
    private final ApplicationService applicationService;

    private final AliasManager aliasManager;

    public TranslatingApplicationService(ApplicationService applicationService, AliasManager aliasManager)
    {
        this.applicationService = applicationService;
        this.aliasManager = aliasManager;
    }

    public User authenticateUser(Application application, String username, PasswordCredential passwordCredential)
        throws OperationFailedException, InactiveAccountException, InvalidAuthenticationException, ExpiredCredentialException, UserNotFoundException
    {
        final String unaliasedUsername = aliasManager.findUsernameByAlias(application, username);
        final User user = applicationService.authenticateUser(application, unaliasedUsername, passwordCredential);
        return buildApplicationUser(application, user);
    }

    public boolean isUserAuthorised(Application application, String username)
    {
        final String unaliasedUsername = aliasManager.findUsernameByAlias(application, username);
        return applicationService.isUserAuthorised(application, unaliasedUsername);
    }

    public void addAllUsers(Application application, Collection<UserTemplateWithCredentialAndAttributes> users)
            throws ApplicationPermissionException, OperationFailedException, BulkAddFailedException
    {
        applicationService.addAllUsers(application, users);
    }

    public User findUserByName(Application application, String name)
            throws UserNotFoundException
    {
        final String unaliasedUsername = aliasManager.findUsernameByAlias(application, name);
        final User user = applicationService.findUserByName(application, unaliasedUsername);
        return buildApplicationUser(application, user);
    }

    public UserWithAttributes findUserWithAttributesByName(Application application, String name)
            throws UserNotFoundException
    {
        final String unaliasedUsername = aliasManager.findUsernameByAlias(application, name);
        final UserWithAttributes user = applicationService.findUserWithAttributesByName(application, unaliasedUsername);
        return buildApplicationUserWithAttributes(application, user, name);
    }

    public User addUser(Application application, UserTemplate user, PasswordCredential credential)
            throws InvalidUserException, OperationFailedException, InvalidCredentialException, ApplicationPermissionException
    {
        return buildApplicationUser(application, applicationService.addUser(application, user, credential));
    }

    public User updateUser(Application application, UserTemplate user)
            throws InvalidUserException, OperationFailedException, ApplicationPermissionException, UserNotFoundException
    {
        final String unaliasedUsername = aliasManager.findUsernameByAlias(application, user.getName());
        final UserTemplate unaliasedUser = new UserTemplate(user);
        unaliasedUser.setName(unaliasedUsername);
        final User updatedUser = applicationService.updateUser(application, unaliasedUser);
        return buildApplicationUser(application, updatedUser);
    }

    public void updateUserCredential(Application application, String username, PasswordCredential credential)
            throws OperationFailedException, UserNotFoundException, InvalidCredentialException, ApplicationPermissionException
    {
        final String unaliasedUsername = aliasManager.findUsernameByAlias(application, username);
        applicationService.updateUserCredential(application, unaliasedUsername, credential);
    }

    public void resetUserCredential(Application application, String username)
            throws OperationFailedException, UserNotFoundException, InvalidCredentialException, ApplicationPermissionException, InvalidEmailAddressException
    {
        final String unaliasedUsername = aliasManager.findUsernameByAlias(application, username);
        applicationService.resetUserCredential(application, unaliasedUsername);
    }

    public void storeUserAttributes(Application application, String username, Map<String, Set<String>> attributes)
            throws OperationFailedException, ApplicationPermissionException, UserNotFoundException
    {
        final String unaliasedUsername = aliasManager.findUsernameByAlias(application, username);
        applicationService.storeUserAttributes(application, unaliasedUsername, attributes);
    }

    public void removeUserAttributes(Application application, String username, String attributeName)
            throws OperationFailedException, ApplicationPermissionException, UserNotFoundException
    {
        final String unaliasedUsername = aliasManager.findUsernameByAlias(application, username);
        applicationService.removeUserAttributes(application, unaliasedUsername, attributeName);
    }

    public void removeUser(Application application, String user)
            throws OperationFailedException, UserNotFoundException, ApplicationPermissionException
    {
        final String unaliasedUsername = aliasManager.findUsernameByAlias(application, user);
        applicationService.removeUser(application, unaliasedUsername);
    }

    public <T> List<T> searchUsers(Application application, EntityQuery<T> query)
    {
        if (!application.isAliasingEnabled())
        {
            return buildApplicationList(application, applicationService.searchUsers(application, query), query.getReturnType(), query.getEntityDescriptor());
        }

        final EntityQuery<User> userQuery = convertToUnboundUserQuery(query);

        final Collection<User> users = searchUsersInternal(application, userQuery, new UserSearcher()
        {
            public List<User> searchUsers(Application application, EntityQuery<User> query)
            {
                return applicationService.searchUsers(application, query);
            }
        });

        final List<User> applicationUserList = buildApplicationList(application, users, User.class, EntityDescriptor.user());

        final List<User> uniqueUserList = pruneDuplicates(applicationUserList);

        return convertToType(SearchResultsUtil.constrainResults(uniqueUserList, query.getStartIndex(), query.getMaxResults()), query.getReturnType());
    }

    public List<User> searchUsersAllowingDuplicateNames(Application application, EntityQuery<User> query)
    {
        if (!application.isAliasingEnabled())
        {
            return buildApplicationList(application, applicationService.searchUsersAllowingDuplicateNames(application, query), query.getReturnType(), query.getEntityDescriptor());
        }

        final EntityQuery<User> userQuery = convertToUnboundUserQuery(query);

        final Collection<User> results = searchUsersInternal(application, userQuery, new UserSearcher()
        {
            public List<User> searchUsers(Application application, EntityQuery<User> query)
            {
                return applicationService.searchUsersAllowingDuplicateNames(application, query);
            }
        });

        final List<User> applicationList = buildApplicationList(application, results, query.getReturnType(), query.getEntityDescriptor());

        return SearchResultsUtil.constrainResults(applicationList, query.getStartIndex(), query.getMaxResults());
    }

    public Group findGroupByName(Application application, String name)
            throws GroupNotFoundException
    {
        return buildApplicationGroup(application, applicationService.findGroupByName(application, name));
    }

    public GroupWithAttributes findGroupWithAttributesByName(Application application, String name)
            throws GroupNotFoundException
    {
        return buildApplicationGroupWithAttributes(application, applicationService.findGroupWithAttributesByName(application, name));
    }

    public Group addGroup(Application application, GroupTemplate group)
            throws InvalidGroupException, OperationFailedException, ApplicationPermissionException
    {
        return buildApplicationGroup(application, applicationService.addGroup(application, group));
    }

    public Group updateGroup(Application application, GroupTemplate group)
            throws InvalidGroupException, OperationFailedException, ApplicationPermissionException, GroupNotFoundException
    {
        return buildApplicationGroup(application, applicationService.updateGroup(application, group));
    }

    public void storeGroupAttributes(Application application, String groupname, Map<String, Set<String>> attributes)
            throws OperationFailedException, ApplicationPermissionException, GroupNotFoundException
    {
        applicationService.storeGroupAttributes(application, groupname, attributes);
    }

    public void removeGroupAttributes(Application application, String groupname, String attributeName)
            throws OperationFailedException, ApplicationPermissionException, GroupNotFoundException
    {
        applicationService.removeGroupAttributes(application, groupname, attributeName);
    }

    public void removeGroup(Application application, String group)
            throws OperationFailedException, GroupNotFoundException, ApplicationPermissionException
    {
        applicationService.removeGroup(application, group);
    }

    public <T> List<T> searchGroups(Application application, EntityQuery<T> query)
    {
        return buildApplicationList(application, applicationService.searchGroups(application, query), query.getReturnType(), query.getEntityDescriptor());
    }

    public void addUserToGroup(Application application, String username, String groupName)
            throws OperationFailedException, UserNotFoundException, GroupNotFoundException, ApplicationPermissionException
    {
        final String unaliasedUsername = aliasManager.findUsernameByAlias(application, username);
        applicationService.addUserToGroup(application, unaliasedUsername, groupName);
    }

    public void addGroupToGroup(Application application, String childGroupName, String parentGroupName)
            throws OperationFailedException, GroupNotFoundException, ApplicationPermissionException, InvalidMembershipException
    {
        applicationService.addGroupToGroup(application, childGroupName, parentGroupName);
    }

    public void removeUserFromGroup(Application application, String username, String groupName)
            throws OperationFailedException, GroupNotFoundException, UserNotFoundException, ApplicationPermissionException, MembershipNotFoundException
    {
        final String unaliasedUsername = aliasManager.findUsernameByAlias(application, username);
        applicationService.removeUserFromGroup(application, unaliasedUsername, groupName);
    }

    public void removeGroupFromGroup(Application application, String childGroup, String parentGroup)
            throws OperationFailedException, GroupNotFoundException, ApplicationPermissionException, MembershipNotFoundException
    {
        applicationService.removeGroupFromGroup(application, childGroup, parentGroup);
    }

    public boolean isUserDirectGroupMember(Application application, String username, String groupName)
    {
        final String unaliasedUsername = aliasManager.findUsernameByAlias(application, username);
        return applicationService.isUserDirectGroupMember(application, unaliasedUsername, groupName);
    }

    public boolean isGroupDirectGroupMember(Application application, String childGroup, String parentGroup)
    {
        return applicationService.isGroupDirectGroupMember(application, childGroup, parentGroup);
    }

    public boolean isUserNestedGroupMember(Application application, String username, String groupName)
    {
        final String unaliasedUsername = aliasManager.findUsernameByAlias(application, username);
        return applicationService.isUserNestedGroupMember(application, unaliasedUsername, groupName);
    }

    public boolean isGroupNestedGroupMember(Application application, String childGroup, String parentGroup)
    {
        return applicationService.isGroupNestedGroupMember(application, childGroup, parentGroup);
    }

    public <T> List<T> searchDirectGroupRelationships(Application application, MembershipQuery<T> query)
    {
        final MembershipQuery<T> unaliasedQuery = buildUnaliasedMembershipQuery(application, query);
        final List<T> result = applicationService.searchDirectGroupRelationships(application, unaliasedQuery);
        return buildApplicationList(application, result, query.getReturnType(), query.getEntityToReturn());
    }

    public <T> List<T> searchNestedGroupRelationships(Application application, MembershipQuery<T> query)
    {
        final MembershipQuery<T> unaliasedQuery = buildUnaliasedMembershipQuery(application, query);
        final List<T> result = applicationService.searchNestedGroupRelationships(application, unaliasedQuery);
        return buildApplicationList(application, result, query.getReturnType(), query.getEntityToReturn());
    }

    public String getCurrentEventToken()
    {
        return applicationService.getCurrentEventToken();
    }

    public Events getNewEvents(Application application, String eventToken) throws EventTokenExpiredException, OperationFailedException, IncrementalSynchronisationNotAvailableException
    {
        final Events result =  applicationService.getNewEvents(application, eventToken);

        if (!application.isAliasingEnabled() && !application.isLowerCaseOutput())
            return result;

        final List<OperationEvent> applicationEvents = new ArrayList<OperationEvent>();
        for (OperationEvent event : result.getEvents())
        {
            final OperationEvent applicationEvent;
            if (event instanceof UserEvent)
            {
                final UserEvent userEvent = (UserEvent) event;
                final User applicationUser = buildApplicationUser(application, userEvent.getUser());
                applicationEvent = new UserEvent(event.getOperation(), event.getDirectory(), applicationUser, userEvent.getStoredAttributes(), userEvent.getDeletedAttributes());
            }
            else if (event instanceof GroupEvent)
            {
                final GroupEvent groupEvent = (GroupEvent) event;
                final Group applicationGroup = buildApplicationGroup(application, groupEvent.getGroup());
                applicationEvent = new GroupEvent(event.getOperation(), event.getDirectory(), applicationGroup, groupEvent.getStoredAttributes(), groupEvent.getDeletedAttributes());
            }
            else if (event instanceof UserMembershipEvent)
            {
                final UserMembershipEvent userMembershipEvent = (UserMembershipEvent) event;
                final String applicationChildUsername = buildApplicationUsername(application, userMembershipEvent.getChildUsername());
                final Set<String> applicationGroupNames = ImmutableSet.copyOf(buildApplicationGroupNames(application, userMembershipEvent.getParentGroupNames()));
                applicationEvent = new UserMembershipEvent(event.getOperation(), event.getDirectory(), applicationChildUsername, applicationGroupNames);
            }
            else if (event instanceof GroupMembershipEvent)
            {
                final GroupMembershipEvent groupMembershipEvent = (GroupMembershipEvent) event;
                final String applicationGroupName = buildApplicationGroupName(application, groupMembershipEvent.getGroupName());
                final Set<String> applicationParentGroupNames = ImmutableSet.copyOf(buildApplicationGroupNames(application, groupMembershipEvent.getParentGroupNames()));
                final Set<String> applicationChildGroupNames = ImmutableSet.copyOf(buildApplicationGroupNames(application, groupMembershipEvent.getChildGroupNames()));
                applicationEvent = new GroupMembershipEvent(event.getOperation(), event.getDirectory(), applicationGroupName, applicationParentGroupNames, applicationChildGroupNames);
            }
            else
            {
                throw new IllegalArgumentException("Event type " + event.getClass() + " not supported.");
            }
            applicationEvents.add(applicationEvent);
        }

        return new Events(applicationEvents, result.getNewEventToken());
    }

    /**
     * Converts the given list of users to a list of given {@code returnType}.
     *
     * @param applicationList list of users ready to be returned to the application
     * @param returnType Type of the returned list
     * @return list of given {@code returnType}
     */
    @SuppressWarnings("unchecked")
    private <T> List<T> convertToType(List<User> applicationList, Class<T> returnType)
    {
        QueryUtils.checkAssignableFrom(returnType, String.class, User.class);

        if (String.class.isAssignableFrom(returnType))
        {
            return (List<T>) SearchResultsUtil.convertEntitiesToNames(applicationList);
        }

        return (List<T>) applicationList;
    }

    /**
     * Searches for users in an aliasing aware way.
     *
     * @param application current application
     * @param query search query to perform
     * @param searcher searcher that performs queries
     * @return results that matched the query
     */
    private Collection<User> searchUsersInternal(Application application, EntityQuery<User> query, UserSearcher searcher)
    {
        final SearchRestriction searchRestrictions = replaceAliasesWithUsernames(application, query.getSearchRestriction());
        final EntityQuery<User> aliasedQuery = QueryBuilder.queryFor(query.getReturnType(), query.getEntityDescriptor(), searchRestrictions, query.getStartIndex(), query.getMaxResults());

        return doSearchUsers(application, aliasedQuery, searcher);
    }

    /**
     * Returns a list of users with unique usernames.
     *
     * Uniqueness among multiple users with the same username is achieved by
     * returning only the first user from the user list with a given username.
     *
     * @param users list of users to prune
     * @return list of users with unique usernames
     */
    private List<User> pruneDuplicates(Collection<User> users)
    {
        final Set<User> pruned =  new TreeSet<User>(NameComparator.of(User.class));
        pruned.addAll(users);

        return new ArrayList<User>(pruned);
    }

    /**
     * Goes through each username restriction in the given search restrictions,
     * and replaces the value of exact username matches with unaliased
     * usernames.
     *
     * @param application current application
     * @param restrictions search restrictions
     * @return search restrictions with unaliased usernames
     */
    private SearchRestriction replaceAliasesWithUsernames(Application application, SearchRestriction restrictions)
    {
        if (restrictions instanceof BooleanRestriction)
        {
            final BooleanRestriction restriction = (BooleanRestriction) restrictions;

            final List<SearchRestriction> childRestrictions = new ArrayList<SearchRestriction>(restriction.getRestrictions().size());
            for (SearchRestriction childRestriction : restriction.getRestrictions())
            {
                childRestrictions.add(replaceAliasesWithUsernames(application, childRestriction));
            }

            return new BooleanRestrictionImpl(restriction.getBooleanLogic(), childRestrictions);
        }
        else if (restrictions instanceof PropertyRestriction)
        {
            final PropertyRestriction<?> restriction = (PropertyRestriction<?>) restrictions;
            if (UserTermKeys.USERNAME.equals(restriction.getProperty()) && restriction.getMatchMode().isExact())
            {
                final String username = aliasManager.findUsernameByAlias(application, (String) restriction.getValue());
                return new TermRestriction<String>(UserTermKeys.USERNAME, restriction.getMatchMode(), username);
            }
        }
        return restrictions;
    }

    /**
     * Recurses through the query searching for results while handling non exact username restrictions differently.
     *
     * @param application current application
     * @param query search query to perform
     * @param searcher searcher that performs queries
     * @return results that matched the query
     */
    private Collection<User> doSearchUsers(Application application, EntityQuery<User> query, UserSearcher searcher)
    {
        final SearchRestriction restrictions = query.getSearchRestriction();
        if (containsNonExactUsernameRestrictions(restrictions)) {
            if (restrictions instanceof BooleanRestriction)
            {
                final BooleanRestriction restriction = (BooleanRestriction) restrictions;
                final ResultCombiner<User> combiner = new ResultCombiner<User>(restriction.getBooleanLogic());
                for (SearchRestriction childRestriction : restriction.getRestrictions())
                {
                    final EntityQuery<User> childQuery = QueryBuilder.queryFor(query.getReturnType(), query.getEntityDescriptor(), childRestriction, 0, EntityQuery.ALL_RESULTS);
                    final Collection<User> childResults = doSearchUsers(application, childQuery, searcher);
                    combiner.combine(childResults);
                }
                return combiner.getValues();
            }
            else if (restrictions instanceof PropertyRestriction)
            {
                @SuppressWarnings("unchecked")
                final PropertyRestriction<String> usernameRestriction = (PropertyRestriction<String>) restrictions;
                return searchWithNonExactUsernameRestriction(application, usernameRestriction, searcher);
            }
            else
            {
                throw new IllegalArgumentException("Unexpected restriction");
            }
        }
        else
        {
            return searcher.searchUsers(application, query);
        }
    }

    /**
     * Return true if the search restrictions contain non exact username matches.
     *
     * @param restrictions search restrictions
     * @return true if the search restrictions contain non exact username matches
     */
    private boolean containsNonExactUsernameRestrictions(SearchRestriction restrictions)
    {
        if (restrictions instanceof BooleanRestriction)
        {
            final BooleanRestriction restriction = (BooleanRestriction) restrictions;
            for (SearchRestriction childRestriction : restriction.getRestrictions())
            {
                if (containsNonExactUsernameRestrictions(childRestriction))
                {
                    return true;
                }
            }
        }
        else if (restrictions instanceof PropertyRestriction)
        {
            final PropertyRestriction<?> restriction = (PropertyRestriction<?>) restrictions;
            if (UserTermKeys.USERNAME.equals(restriction.getProperty()) && !restriction.getMatchMode().isExact())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Search for users with non exact username restriction.
     *
     * 1. Search for users with matching alias.
     * 2. Search for users with matching username and no alias.
     *
     * @param application current application
     * @param restriction non exeact username restriction
     * @param searcher user searcher to use
     * @return results matching the search restriction
     */
    private Collection<User> searchWithNonExactUsernameRestriction(Application application, PropertyRestriction<String> restriction, UserSearcher searcher)
    {
        final Collection<User> users = new ArrayList<User>();

        // Add all users matched by the alias
        final SearchRestriction aliasRestriction = new BooleanRestrictionImpl(BooleanRestriction.BooleanLogic.AND,
                new TermRestriction<String>(AliasTermKeys.ALIAS, restriction.getMatchMode(), restriction.getValue()),
                new TermRestriction<Long>(AliasTermKeys.APPLICATION_ID, MatchMode.EXACTLY_MATCHES, application.getId()));
        final EntityQuery<String> aliasQuery = new AliasQuery(aliasRestriction, 0, EntityQuery.ALL_RESULTS);
        for (String aliasMatchUsername : aliasManager.search(aliasQuery))
        {
            EntityQuery<User> aliasedUserQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user(), Restriction.on(UserTermKeys.USERNAME).exactlyMatching(aliasMatchUsername), 0, EntityQuery.ALL_RESULTS);
            users.addAll(searcher.searchUsers(application, aliasedUserQuery));
        }

        // Add users matched by the username which have no alias
        final EntityQuery<User> userQuery = new UserQuery<User>(User.class, restriction, 0, EntityQuery.ALL_RESULTS);
        final List<User> matchingUsers = searcher.searchUsers(application, userQuery);
        for (User user : matchingUsers)
        {
            if (!aliasExists(application, user))
            {
                users.add(user);
            }
        }

        return users;
    }

    private boolean aliasExists(Application application, User user)
    {
        final String alias = aliasManager.findAliasByUsername(application, user.getName());
        return !user.getName().equalsIgnoreCase(alias);
    }

    /**
     * Replaces username in given membership query with unaliased username.
     *
     * @param application current application
     * @param query query to process
     * @param <T> query return type
     * @return membership query with unaliased usernames
     */
    private <T> MembershipQuery<T> buildUnaliasedMembershipQuery(Application application, MembershipQuery<T> query)
    {
        if (query.getEntityToMatch().equals(EntityDescriptor.user()))
        {
            final String username = aliasManager.findUsernameByAlias(application, query.getEntityNameToMatch());
            return QueryBuilder.createMembershipQuery(query.getMaxResults(), query.getStartIndex(), query.isFindChildren(), query.getEntityToReturn(), query.getReturnType(), query.getEntityToMatch(), username);
        }
        return query;
    }

    /**
     * Builds a list of elements suitable for returning to the application.
     *
     * @param application current application
     * @param collection elements to process
     * @param returnType element type
     * @param entityToReturn element entity descriptor
     * @return ist of elements suitable for returning to the application
     */
    @SuppressWarnings("unchecked")
    private <T> List<T> buildApplicationList(Application application, Collection<T> collection, Class<T> returnType, EntityDescriptor entityToReturn)
    {
        if (EntityDescriptor.user().equals(entityToReturn) && String.class.isAssignableFrom(returnType))
        {
            return (List<T>) buildApplicationUsernames(application, (Collection<String>) collection);
        }
        else if (User.class.isAssignableFrom(returnType))
        {
            return (List<T>) buildApplicationUsers(application, (Collection<User>) collection);
        }
        else if (EntityDescriptor.group().equals(entityToReturn) && String.class.isAssignableFrom(returnType))
        {
            return (List<T>) buildApplicationGroupNames(application, (Collection<String>) collection);
        }
        else if (Group.class.isAssignableFrom(returnType))
        {
            return (List<T>) buildApplicationGroups(application, (Collection<Group>) collection);
        }

        return asList(collection);
    }

    private <T> List<T> asList(Collection<T> collection)
    {
        if (collection instanceof List)
        {
            return (List<T>) collection;
        }

        return new ArrayList<T>(collection);
    }

    /**
     * Builds a list of usernames suitable for returning to the application.
     *
     * If a username has an application specific alias, the username will be
     * replaced with the alias.
     *
     * If the application requires lower case usernames, the usernames will
     * be lower cased.
     *
     * @param application current application
     * @param usernames usernames to process
     * @return application usernames
     */
    private List<String> buildApplicationUsernames(final Application application, final Collection<String> usernames)
    {
        final List<String> applicationUsernames = new ArrayList<String>(usernames.size());
        for (String username : usernames)
        {
            applicationUsernames.add(buildApplicationUsername(application, username));
        }

        Collections.sort(applicationUsernames);

        return applicationUsernames;
    }

    private String buildApplicationUsername(Application application, String username)
    {
        final String alias = aliasManager.findAliasByUsername(application, username);
        return application.isLowerCaseOutput() ? toLowerCase(alias) : alias;
    }


    /**
     * Builds a list of users suitable for returning to the application
     *
     * @param application current application
     * @param users unaliased users
     * @return application users
     */
    private List<User> buildApplicationUsers(final Application application, final Collection<User> users)
    {
        final List<User> applicationUsers = new ArrayList<User>(users.size());
        for (User user : users)
        {
            applicationUsers.add(buildApplicationUser(application, user));
        }

        Collections.sort(applicationUsers, new UserComparator(application));

        return applicationUsers;
    }

    /**
     * Builds a user suitable for returning to the application.
     *
     * If the user has an application specific alias, the username will be
     * replaced with the alias.
     *
     * If the application requires lower case usernames, the username will
     * be lower cased.
     *
     * Otherwise the user is returned unchanged.
     *
     * @param application current application
     * @param user unaliased user
     * @return aliased user
     */
    private User buildApplicationUser(Application application, User user)
    {
        final String applicationUsername = buildApplicationUsername(application, user.getName());

        if (user.getName().equals(applicationUsername))
        {
            return user;
        }

        UserTemplate applicationUser = new UserTemplate(user);
        applicationUser.setName(applicationUsername);
        return applicationUser;
    }

    /**
     * Builds a user suitable for returning to the application.
     *
     * If the user has an application specific alias, the username will be
     * replaced with the alias.
     *
     * If the application requires lower case usernames, the username will
     * be lower cased.
     *
     * Otherwise the user is returned unchanged.
     *
     * @param application current application
     * @param user unaliased user
     * @param alias aliased username
     * @return application user
     */
    private UserWithAttributes buildApplicationUserWithAttributes(Application application, UserWithAttributes user, String alias)
    {
        final String applicationUsername = application.isLowerCaseOutput() ? toLowerCase(alias) : alias;

        if (user.getName().equals(applicationUsername))
        {
            return user;
        }

        UserTemplateWithAttributes applicationUser = new UserTemplateWithAttributes(user);
        applicationUser.setName(applicationUsername);
        return applicationUser;
    }

    /**
     * Builds a list of group names suitable for returning to the application.
     *
     * If the application requires lower case group names, the group names will
     * be lower cased.
     *
     * @param application current application
     * @param groupNames group names to process
     * @return application group names
     */
    private List<String> buildApplicationGroupNames(Application application, Collection<String> groupNames)
    {
        if (application.isLowerCaseOutput())
        {
            final List<String> lowerCaseGroupNames = new ArrayList<String>(groupNames.size());
            for (String groupName : groupNames)
            {
                lowerCaseGroupNames.add(toLowerCase(groupName));
            }
            return lowerCaseGroupNames;
        }

        return asList(groupNames);
    }

    private String buildApplicationGroupName(Application application, String groupName)
    {
        return application.isLowerCaseOutput() ? toLowerCase(groupName) : groupName;
    }

    /**
     * Builds a list of groups suitable for returning to the application.
     *
     * @param application current application
     * @param groups groups to process
     * @return application groups
     */
    private List<Group> buildApplicationGroups(Application application, Collection<Group> groups)
    {
        if (application.isLowerCaseOutput())
        {
            final List<Group> lowerCaseGroups = new ArrayList<Group>(groups.size());
            for (Group group : groups)
            {
                lowerCaseGroups.add(buildApplicationGroup(application, group));
            }
            return lowerCaseGroups;
        }

        return asList(groups);
    }

    /**
     * Builds a group suitable for returning to the application.
     *
     * If the application requires lower case group names, the group name will
     * be lower cased.
     *
     * Otherwise the group is returned unchanged.
     *
     * @param application current application
     * @param group group to process
     * @return application group
     */
    private Group buildApplicationGroup(Application application, Group group)
    {
        if (application.isLowerCaseOutput())
        {
            final GroupTemplate groupTemplate = new GroupTemplate(group);
            groupTemplate.setName(toLowerCase(group.getName()));
            return groupTemplate;
        }

        return group;
    }

    /**
     * Builds a group suitable for returning to the application.
     *
     * If the application requires lower case group names, the group name will
     * be lower cased.
     *
     * Otherwise the group is returned unchanged.
     *
     * @param application current application
     * @param group group to process
     * @return application group
     */
    private GroupWithAttributes buildApplicationGroupWithAttributes(Application application, GroupWithAttributes group)
    {
        if (application.isLowerCaseOutput())
        {
            final GroupTemplateWithAttributes groupTemplate = new GroupTemplateWithAttributes(group);
            groupTemplate.setName(toLowerCase(group.getName()));
            return groupTemplate;
        }

        return group;
    }

    /**
     * Converts the given query to a query that returns a list of users.
     *
     * @param query query to convert
     * @return query that returns a list of users.
     */
    private EntityQuery<User> convertToUnboundUserQuery(Query<?> query)
    {
        QueryUtils.checkAssignableFrom(query.getReturnType(), String.class, User.class);

        return QueryBuilder.queryFor(User.class, EntityDescriptor.user(), query.getSearchRestriction(), 0, EntityQuery.ALL_RESULTS);
    }

    /**
     * Contains the logic to combine collections using different set operations.
     *
     * @param <T> element type
     */
    private static class ResultCombiner<T>
    {
        private final BooleanRestriction.BooleanLogic logic;

        private Set<T> values =  null;

        /**
         * Creates a new combiner with the given combining operation.
         *
         * @param logic combining operation
         */
        ResultCombiner(BooleanRestriction.BooleanLogic logic)
        {
            this.logic = logic;
        }

        /**
         * Combines new values with the existing values.
         *
         * @param newValues new values to combine
         */
        void combine(Collection<T> newValues)
        {
            if (values == null)
            {
                values = Sets.newHashSet(newValues);
            }
            else if (logic == BooleanRestriction.BooleanLogic.AND)
            {
                values.retainAll(newValues);
            }
            else if (logic == BooleanRestriction.BooleanLogic.OR)
            {
                values.addAll(newValues);
            }
        }

        /**
         * Returns combined values
         *
         * @return combined values
         */
        Set<T> getValues()
        {
            return values == null ? Collections.<T>emptySet() : values;
        }
    }

    /**
     * Interface implemented by searchers that are able to execute user queries
     */
    private interface UserSearcher
    {
        /**
         * Executes given user search query and returns search results.
         *
         * @param application current application
         * @param query user search query
         * @return search results
         */
        List<User> searchUsers(Application application, EntityQuery<User> query);
    }

    /**
     * Comparator that sorts users by username and directory order for the
     * given application.
     */
    private static class UserComparator implements Comparator<User>
    {
        private final Map<Long, Integer> directoryIdToIndex;

        /**
         * Creates a UserComparator for the given application.
         *
         * @param application current application
         */
        UserComparator(Application application)
        {
            directoryIdToIndex = new HashMap<Long, Integer>();
            for (int i = 0; i < application.getDirectoryMappings().size(); ++i)
            {
                directoryIdToIndex.put(application.getDirectoryMappings().get(i).getDirectory().getId(), i);
            }
        }

        /**
         * Compares users by their name and directory.
         *
         * @param o1 first user
         * @param o2 second user
         * @return ordering of the given users
         */
        public int compare(User o1, User o2)
        {
            final int nameComparison = NameComparator.of(User.class).compare(o1, o2);
            if (nameComparison != 0)
            {
                return nameComparison;
            }
            final Integer directoryOrderO1 = directoryIdToIndex.get(o1.getDirectoryId());
            final Integer directoryOrderO2 = directoryIdToIndex.get(o2.getDirectoryId());
            return directoryOrderO1.compareTo(directoryOrderO2);
        }
    }
}
