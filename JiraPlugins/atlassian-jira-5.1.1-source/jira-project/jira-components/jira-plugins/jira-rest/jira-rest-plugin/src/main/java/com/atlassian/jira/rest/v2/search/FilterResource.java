package com.atlassian.jira.rest.v2.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.RESTException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.List;

/**
 * Resource for searches.
 *
 * @since v5.0
 */
@Path ("filter")
@AnonymousAllowed
@Consumes ({ MediaType.APPLICATION_JSON })
@Produces ({ MediaType.APPLICATION_JSON })
public class FilterResource
{


    private final JiraAuthenticationContext authenticationContext;
    private final SearchRequestService searchRequestService;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final UserUtil userUtil;
    private final FavouritesService favouritesService;

    public FilterResource(final JiraAuthenticationContext authenticationContext,
            final SearchRequestService searchRequestService,
            final VelocityRequestContextFactory velocityRequestContextFactory,
            final UserUtil userUtil, final FavouritesService favouritesService)
    {
        this.authenticationContext = authenticationContext;
        this.searchRequestService = searchRequestService;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.userUtil = userUtil;
        this.favouritesService = favouritesService;
    }

    /**
     * Returns a filter given an id
     *
     * @param id the id of the filter being looked up
     * @param uriInfo info needed to construct URLs.
     * @return a {@link FilterBean}
     *
     * @response.representation.200.qname
     *      filter
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a JSON representation of a filter
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.search.FilterBean#DOC_EXAMPLE_1}
     *
     * @response.representation.400.doc
     *     Returned if there is a problem looking up the filter given the id
     */
    @Path ("{id}")
    @GET
    public FilterBean getFilter(@PathParam ("id") Long id, @Context UriInfo uriInfo)
    {
        final User user = authenticationContext.getLoggedInUser();
        final JiraServiceContextImpl context = new JiraServiceContextImpl(user);
        final SearchRequest filter = searchRequestService.getFilter(context, id);
        if (filter == null)
        {
            throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(context.getErrorCollection()));
        }

        final String canonicalBaseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();

        //if we've got no user it's not a favourite filter.
        boolean favourite = user != null && favouritesService.isFavourite(user, filter);
        return new FilterBeanBuilder().filter(filter).
                context(uriInfo, canonicalBaseUrl).owner(userUtil.getUser(filter.getOwnerUserName())).
                favourite(favourite).build();
    }

    /**
     * Returns the favourite filters of the logged-in user.
     *
     * @param uriInfo info needed to construct URLs.
     * @return a List of {@link FilterBean}
     *
     * @response.representation.200.qname
     *      filter
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a JSON representation of a list of filters
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.search.FilterBean#DOC_FILTER_LIST_EXAMPLE}
     *
     * @response.representation.400.doc
     *     Returned if there is a problem looking up the filter given the id
     */
    @Path("favourite")
    @GET
    public List<FilterBean> getFavouriteFilters(final @Context UriInfo uriInfo)
    {
        Collection<SearchRequest> favouriteFilters = searchRequestService.getFavouriteFilters(authenticationContext.getLoggedInUser());
        final String canonicalBaseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
        Iterable<FilterBean> favouriteFilterBeans = Iterables.transform(favouriteFilters, new Function<SearchRequest, FilterBean>()
        {
            @Override
            public FilterBean apply(@Nullable SearchRequest filter)
            {
                return new FilterBeanBuilder().filter(filter).
                        context(uriInfo, canonicalBaseUrl).owner(userUtil.getUser(filter.getOwnerUserName())).
                        favourite(true).build();
            }
        });
        return Lists.newArrayList(favouriteFilterBeans);
    }
}
