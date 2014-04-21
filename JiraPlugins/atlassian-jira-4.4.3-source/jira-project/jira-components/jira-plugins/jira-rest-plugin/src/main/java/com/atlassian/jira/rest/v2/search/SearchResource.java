package com.atlassian.jira.rest.v2.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.IssueBean;
import com.atlassian.jira.rest.v2.issue.IssueResource;
import com.atlassian.jira.rest.v2.issue.RESTException;
import com.atlassian.jira.rest.v2.issue.ResourceUriBuilder;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.action.issue.IssueSearchLimits;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.base.Function;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Resource for searches.
 *
 * @since v4.3
 */
@Path ("search")
@AnonymousAllowed
@Consumes ( { MediaType.APPLICATION_JSON })
@Produces ( { MediaType.APPLICATION_JSON })
public class SearchResource
{
    /**
     * The default number of issues that will be returned, if another number is not specified.
     */
    static final int DEFAULT_ISSUES_RETURNED = 50;

    /**
     * The service used for performing JQL searches.
     */
    private final SearchService searchService;

    /**
     * A JiraAuthenticationContext.
     */
    private final JiraAuthenticationContext jiraAuthenticationContext;

    /**
     * A ResourceUriBuilder.
     */
    private final ResourceUriBuilder uriBuilder;

    /**
     * A ContextUriInfo.
     */
    private final ContextUriInfo uriInfo;

    /**
     * A IssueSearchLimits.
     */
    private final IssueSearchLimits searchLimits;

    /**
     * Creates a new SearchResource.
     *
     * @param searchService a SearchService
     * @param jiraAuthenticationContext a JiraAuthenticationContext
     * @param uriBuilder a ResourceUriBuilder
     * @param uriInfo a ContextUriInfo
     * @param searchLimits a IssueSearchLimits
     */
    public SearchResource(SearchService searchService, JiraAuthenticationContext jiraAuthenticationContext, ResourceUriBuilder uriBuilder, ContextUriInfo uriInfo, IssueSearchLimits searchLimits)
    {
        this.searchService = searchService;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.uriBuilder = uriBuilder;
        this.uriInfo = uriInfo;
        this.searchLimits = searchLimits;
    }

    /**
     * Searches for issues using JQL. If the JQL query is too large to be encoded as a query param you should instead
     * POST to this resource.
     *
     * @param jql a JQL query string
     * @param startAt the index of the first issue to return (0-based)
     * @param maxResults the maximum number of issues to return (defaults to 50). The maximum allowable value is
     *      dictated by the JIRA property 'jira.search.views.default.max'. If you specify a value that is higher than
     *      this number, your search results will be truncated.
     * @return a SearchResultsBean
     * @throws com.atlassian.jira.issue.search.SearchException if there is a problem performing the search
     *
     * @response.representation.200.qname
     *      searchResults
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      Returns a JSON representation of the search results.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.search.SearchResultsBean#DOC_EXAMPLE}
     *
     * @response.representation.400.doc
     *     Returned if there is a problem with the JQL query.
     */
    @GET
    public SearchResultsBean search(@QueryParam ("jql") String jql, @QueryParam ("startAt") Integer startAt, @QueryParam ("maxResults") Integer maxResults)
            throws SearchException
    {
        User user = jiraAuthenticationContext.getLoggedInUser();
        SearchService.ParseResult parseResult = searchService.parseQuery(user, jql == null ? "" : jql);
        if (!parseResult.isValid())
        {
            throw new RESTException(Response.Status.BAD_REQUEST, ErrorCollection.of(parseResult.getErrors().getErrorMessages()));
        }

        PagerFilter filter = createFilter(startAt, maxResults);
        SearchResults searchResult = searchService.search(user, parseResult.getQuery(), filter);

        return asResultsBean(searchResult, filter);
    }

    /**
     * Performs a search using JQL.
     *
     * @param searchRequest a JSON object containing the search request
     * @return a SearchResultsBean
     * @throws com.atlassian.jira.issue.search.SearchException if there is a problem performing the search
     *
     * @request.representation.qname
     *      searchRequest
     *
     * @request.representation.example
     *      {@link com.atlassian.jira.rest.v2.search.SearchRequestBean#DOC_EXAMPLE}
     *
     * @response.representation.200.qname
     *      searchResults
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returns a JSON representation of the search results.
     *
     * @response.representation.200.example
     *      {@link com.atlassian.jira.rest.v2.search.SearchResultsBean#DOC_EXAMPLE}
     *
     * @response.representation.400.doc
     *     Returned if there is a problem with the JQL query.
     */
    @POST
    public SearchResultsBean searchUsingSearchRequest(SearchRequestBean searchRequest) throws SearchException
    {
        return search(searchRequest.jql, searchRequest.startAt, searchRequest.maxResults);
    }

    /**
     * Creates a SearchResultsBean suitable for returning to the client.
     *
     * @param results a SearchResults
     * @param filter a PagerFilter that was used for the search
     * @return a SearchResultsBean
     */
    protected SearchResultsBean asResultsBean(SearchResults results, PagerFilter filter)
    {
        List<IssueBean> issues = newArrayList(transform(results.getIssues(), new IssueToIssueBean()));
        return new SearchResultsBean(
                results.getStart(),
                filter.getMax(),
                results.getTotal(),
                issues
        );
    }

    /**
     * Creates a new PagerFilter for the given search request. If the maxResults specified in the search request is
     * greater than the value returned by {@link com.atlassian.jira.web.action.issue.IssueSearchLimits#getMaxResults()},
     * the value returned by this method will be used.
     *
     * @param startAt an Integer containing the start index
     * @param maxResults an Integer containing the max results
     * @return a PagerFilter
     */
    protected PagerFilter createFilter(Integer startAt, Integer maxResults)
    {
        return PagerFilter.newPageAlignedFilter(
                startAt != null ? max(0, startAt) : 0,
                maxResults != null ? min(searchLimits.getMaxResults(), maxResults) : DEFAULT_ISSUES_RETURNED
        );
    }

    /**
     * Function that converts an Issue to an IssueBean (short version).
     */
    private class IssueToIssueBean implements Function<Issue, IssueBean>
    {
        public IssueBean apply(@Nullable Issue issue)
        {
            URI uri = uriBuilder.build(uriInfo, IssueResource.class, issue.getKey());
            return new IssueBean(issue.getKey(), uri);
        }
    }
}
