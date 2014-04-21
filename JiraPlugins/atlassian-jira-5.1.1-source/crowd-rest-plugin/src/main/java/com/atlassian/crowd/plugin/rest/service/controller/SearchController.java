package com.atlassian.crowd.plugin.rest.service.controller;

import com.atlassian.crowd.cql.parser.CqlQueryParser;
import com.atlassian.crowd.embedded.api.SearchRestriction;
import com.atlassian.crowd.manager.application.ApplicationManager;
import com.atlassian.crowd.manager.application.ApplicationService;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.user.User;
import com.atlassian.crowd.plugin.rest.entity.GroupEntityList;
import com.atlassian.crowd.plugin.rest.entity.SearchRestrictionEntity;
import com.atlassian.crowd.plugin.rest.entity.UserEntityList;
import com.atlassian.crowd.plugin.rest.util.EntityTranslator;
import com.atlassian.crowd.plugin.rest.util.SearchRestrictionEntityTranslator;
import com.atlassian.crowd.search.EntityDescriptor;
import com.atlassian.crowd.search.builder.QueryBuilder;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.query.entity.PropertyTypeService;
import com.atlassian.crowd.search.query.entity.PropertyTypeServiceImpl;
import com.atlassian.crowd.search.query.entity.restriction.NullRestrictionImpl;
import com.atlassian.crowd.search.query.entity.restriction.constants.GroupTermKeys;
import com.atlassian.crowd.search.query.entity.restriction.constants.UserTermKeys;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.util.List;

/**
 * Controller for the Search resource.
 */
public class SearchController extends AbstractResourceController
{
    private final CqlQueryParser cqlQueryParser;
    private final PropertyTypeService userPropertyTypeService;
    private final PropertyTypeService groupPropertyTypeService;

    public SearchController(ApplicationService applicationService, ApplicationManager applicationManager, CqlQueryParser cqlQueryParser)
    {
        super(applicationService, applicationManager);
        this.cqlQueryParser = cqlQueryParser;
        this.userPropertyTypeService = PropertyTypeServiceImpl.newInstance(UserTermKeys.ALL_USER_PROPERTIES);
        this.groupPropertyTypeService = PropertyTypeServiceImpl.newInstance(GroupTermKeys.ALL_GROUP_PROPERTIES);
    }

    /**
     * Searches for groups satisfying the restriction.
     *
     * @param applicationName name of the application.
     * @param searchRestrictionEntity criteria for a group to be returned
     * @param maxResults maximum number of results to return
     * @param startIndex starting index of the result
     * @param expandGroup set to <tt>true</tt> if the group details should be expanded.
     * @param baseUri Base URI of the REST service
     * @return GroupEntityList
     */
    public GroupEntityList searchGroups(final String applicationName, final SearchRestrictionEntity searchRestrictionEntity, final int maxResults, final int startIndex, final boolean expandGroup, final URI baseUri)
    {
        final SearchRestriction searchRestriction = SearchRestrictionEntityTranslator.toSearchRestriction(searchRestrictionEntity);
        return searchGroups(applicationName, searchRestriction, maxResults, startIndex, expandGroup, baseUri);
    }

    /**
     * Searches for users satisfying the restriction.
     *
     * @param applicationName name of the application.
     * @param searchRestrictionEntity criteria for a user to be returned
     * @param maxResults maximum number of results to return
     * @param startIndex starting index of the result
     * @param expandUser set to <tt>true</tt> if the user details should be expanded.
     * @param baseUri Base URI of the REST service
     * @return UserEntityList
     */
    public UserEntityList searchUsers(final String applicationName, final SearchRestrictionEntity searchRestrictionEntity, final int maxResults, final int startIndex, final boolean expandUser, final URI baseUri)
    {
        final SearchRestriction searchRestriction = SearchRestrictionEntityTranslator.toSearchRestriction(searchRestrictionEntity);
        return searchUsers(applicationName, searchRestriction, maxResults, startIndex, expandUser, baseUri);
    }

    /**
     * Searches for groups satisfying the restriction.
     *
     * @param applicationName name of the application.
     * @param cqlSearchRestriction criteria for a group to be returned in the Crowd Query Language
     * @param maxResults maximum number of results to return
     * @param startIndex starting index of the result
     * @param expandGroup set to <tt>true</tt> if the group details should be expanded.
     * @param baseUri Base URI of the REST service
     * @return GroupEntityList
     */
    public GroupEntityList searchGroups(final String applicationName, final String cqlSearchRestriction, final int maxResults, final int startIndex, final boolean expandGroup, final URI baseUri)
    {
        final SearchRestriction searchRestriction = StringUtils.isBlank(cqlSearchRestriction) ? NullRestrictionImpl.INSTANCE : cqlQueryParser.parseQuery(cqlSearchRestriction, groupPropertyTypeService);
        return searchGroups(applicationName, searchRestriction, maxResults, startIndex, expandGroup, baseUri);
    }

    /**
     * Searches for users satisfying the restriction.
     *
     * @param applicationName name of the application.
     * @param cqlSearchRestriction criteria for a user to be returned in the Crowd Query Language
     * @param maxResults maximum number of results to return
     * @param startIndex starting index of the result
     * @param expandUser set to <tt>true</tt> if the user details should be expanded.
     * @param baseUri Base URI of the REST service
     * @return UserEntityList
     */
    public UserEntityList searchUsers(final String applicationName, final String cqlSearchRestriction, final int maxResults, final int startIndex, final boolean expandUser, final URI baseUri)
    {
        final SearchRestriction searchRestriction = StringUtils.isBlank(cqlSearchRestriction) ? NullRestrictionImpl.INSTANCE : cqlQueryParser.parseQuery(cqlSearchRestriction, userPropertyTypeService);
        return searchUsers(applicationName, searchRestriction, maxResults, startIndex, expandUser, baseUri);
    }

    /**
     * Searches for groups satisfying the restriction.
     *
     * @param applicationName name of the application.
     * @param searchRestriction criteria for a group to be returned
     * @param maxResults maximum number of results to return
     * @param startIndex starting index of the result
     * @param expandGroup set to <tt>true</tt> if the group details should be expanded.
     * @param baseUri Base URI of the REST service
     * @return GroupEntityList
     */
    private GroupEntityList searchGroups(final String applicationName, final SearchRestriction searchRestriction, final int maxResults, final int startIndex, final boolean expandGroup, final URI baseUri)
    {
        final Application application = getApplication(applicationName);

        if (expandGroup)
        {
            final EntityQuery<Group> entityQuery = QueryBuilder.queryFor(Group.class, EntityDescriptor.group()).with(searchRestriction).startingAt(startIndex).returningAtMost(maxResults);
            final List<Group> groups = applicationService.searchGroups(application, entityQuery);
            return EntityTranslator.toGroupEntities(groups, baseUri);
        }
        else
        {
            final EntityQuery<String> entityQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.group()).with(searchRestriction).startingAt(startIndex).returningAtMost(maxResults);
            final List<String> groupNames = applicationService.searchGroups(application, entityQuery);
            return EntityTranslator.toMinimalGroupEntities(groupNames, baseUri);
        }
    }

    /**
     * Searches for users satisfying the restriction.
     *
     * @param applicationName name of the application.
     * @param searchRestriction criteria for a user to be returned
     * @param maxResults maximum number of results to return
     * @param startIndex starting index of the result
     * @param expandUser set to <tt>true</tt> if the user details should be expanded.
     * @param baseUri Base URI of the REST service
     * @return UserEntityList
     */
    private UserEntityList searchUsers(final String applicationName, final SearchRestriction searchRestriction, final int maxResults, final int startIndex, final boolean expandUser, final URI baseUri)
    {
        final Application application = getApplication(applicationName);

        if (expandUser)
        {
            final EntityQuery<User> entityQuery = QueryBuilder.queryFor(User.class, EntityDescriptor.user()).with(searchRestriction).startingAt(startIndex).returningAtMost(maxResults);
            final List<User> users = applicationService.searchUsers(application, entityQuery);
            return EntityTranslator.toUserEntities(users, baseUri);
        }
        else
        {
            final EntityQuery<String> entityQuery = QueryBuilder.queryFor(String.class, EntityDescriptor.user()).with(searchRestriction).startingAt(startIndex).returningAtMost(maxResults);
            final List<String> userNames = applicationService.searchUsers(application, entityQuery);
            return EntityTranslator.toMinimalUserEntities(userNames, baseUri);
        }
    }
}
