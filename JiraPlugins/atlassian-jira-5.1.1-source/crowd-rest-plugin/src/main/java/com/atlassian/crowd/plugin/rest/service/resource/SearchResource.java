package com.atlassian.crowd.plugin.rest.service.resource;

import com.atlassian.crowd.plugin.rest.entity.*;
import com.atlassian.crowd.plugin.rest.service.controller.*;
import com.atlassian.crowd.plugin.rest.service.util.CacheControl;
import com.atlassian.crowd.plugin.rest.util.*;
import com.atlassian.plugins.rest.common.security.*;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import static javax.ws.rs.core.MediaType.*;

/**
 * Search resource.
 */
@Path("search")
@Consumes({APPLICATION_XML, APPLICATION_JSON})
@Produces({APPLICATION_XML, APPLICATION_JSON})
@AnonymousAllowed
public class SearchResource extends AbstractResource
{
    private static final String GROUP_ENTITY_TYPE = "group";
    private static final String USER_ENTITY_TYPE = "user";

    private static final String DEFAULT_SEARCH_RESULT_SIZE_STRING = "" + DEFAULT_SEARCH_RESULT_SIZE;

    private final SearchController searchController;

    public SearchResource(SearchController searchController)
    {
        this.searchController = searchController;
    }

    /**
     * Searches for entities satisfying the given search restriction.
     *
     * @param entityType type of the entity to search
     * @param maxResults maximum number of results returned
     * @param startIndex starting index of the results
     * @param searchRestriction restriction entities must satisfy
     */
    @POST
    public Response search(@QueryParam("entity-type") final String entityType, @DefaultValue(DEFAULT_SEARCH_RESULT_SIZE_STRING) @QueryParam(MAX_RESULTS_PARAM) final int maxResults, @DefaultValue("0") @QueryParam(START_INDEX_PARAM) final int startIndex, final SearchRestrictionEntity searchRestriction)
    {
        final String applicationName = getApplicationName();

        if (entityType == null)
        {
            throw new IllegalArgumentException("entity-type query parameter required.");
        }
        else if (GROUP_ENTITY_TYPE.equalsIgnoreCase(entityType))
        {
            final boolean expandGroup = EntityExpansionUtil.shouldExpandField(GroupEntityList.class, GroupEntityList.GROUP_LIST_FIELD_NAME, request);
            final GroupEntityList groupEntityList = searchController.searchGroups(applicationName, searchRestriction, maxResults, startIndex, expandGroup, uriInfo.getBaseUri());
            return Response.ok(groupEntityList).cacheControl(CacheControl.NO_CACHE).build();
        }
        else if (USER_ENTITY_TYPE.equalsIgnoreCase(entityType))
        {
            final boolean expandUser = EntityExpansionUtil.shouldExpandField(UserEntityList.class, UserEntityList.USER_LIST_FIELD_NAME, request);
            final UserEntityList userEntityList = searchController.searchUsers(applicationName, searchRestriction, maxResults, startIndex, expandUser, uriInfo.getBaseUri());
            return Response.ok(userEntityList).cacheControl(CacheControl.NO_CACHE).build();
        }
        else
        {
            throw new IllegalArgumentException("Unknown type: " + entityType);
        }
    }

    /**
     * Searches for entities satisfying the given search restriction.
     *
     * @param entityType type of the entity to search
     * @param maxResults maximum number of results returned
     * @param startIndex starting index of the results
     * @param cqlSearchRestriction restriction entities must satisfy in the Crowd Query Language
     */
    @GET
    public Response search(@QueryParam("entity-type") final String entityType, @DefaultValue(DEFAULT_SEARCH_RESULT_SIZE_STRING) @QueryParam(MAX_RESULTS_PARAM) final int maxResults, @DefaultValue("0") @QueryParam(START_INDEX_PARAM) final int startIndex, @DefaultValue("") @QueryParam("restriction") final String cqlSearchRestriction)
    {
        final String applicationName = getApplicationName();

        if (entityType == null)
        {
            throw new IllegalArgumentException("entity-type query parameter required.");
        }
        else if (GROUP_ENTITY_TYPE.equalsIgnoreCase(entityType))
        {
            final boolean expandGroup = EntityExpansionUtil.shouldExpandField(GroupEntityList.class, GroupEntityList.GROUP_LIST_FIELD_NAME, request);
            final GroupEntityList groupEntityList = searchController.searchGroups(applicationName, cqlSearchRestriction, maxResults, startIndex, expandGroup, uriInfo.getBaseUri());
            return Response.ok(groupEntityList).cacheControl(CacheControl.NO_CACHE).build();
        }
        else if (USER_ENTITY_TYPE.equalsIgnoreCase(entityType))
        {
            final boolean expandUser = EntityExpansionUtil.shouldExpandField(UserEntityList.class, UserEntityList.USER_LIST_FIELD_NAME, request);
            final UserEntityList userEntityList = searchController.searchUsers(applicationName, cqlSearchRestriction, maxResults, startIndex, expandUser, uriInfo.getBaseUri());
            return Response.ok(userEntityList).cacheControl(CacheControl.NO_CACHE).build();
        }
        else
        {
            throw new IllegalArgumentException("Unknown type: " + entityType);
        }
    }
}
