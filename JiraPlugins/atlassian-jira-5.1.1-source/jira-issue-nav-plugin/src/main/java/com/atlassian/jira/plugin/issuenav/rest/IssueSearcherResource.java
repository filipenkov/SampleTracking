package com.atlassian.jira.plugin.issuenav.rest;

import com.atlassian.jira.plugin.issuenav.service.SearcherService;
import com.atlassian.jira.plugin.issuenav.service.Searchers;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * Supplies {@link com.atlassian.jira.issue.search.searchers.IssueSearcher IssueSearchers} and {@link
 * com.atlassian.jira.issue.search.searchers.SearcherGroup SearcherGroups} for the purpose of displaying an issue search
 * query user interface.
 *
 * @since v5.1
 */
@AnonymousAllowed
@Path ("searchers")
public class IssueSearcherResource
{
    private final SearcherService searcherService;

    public IssueSearcherResource(SearcherService searcherService)
    {
        this.searcherService = searcherService;
    }

    @GET
    @Produces ({ MediaType.APPLICATION_JSON })
    public Response getIssueSearchers(@QueryParam("jqlContext") String jqlContext)
    {
        Searchers searchers = searcherService.getSearchers(jqlContext);
        return Response.ok(searchers).cacheControl(never()).build();
    }

}
