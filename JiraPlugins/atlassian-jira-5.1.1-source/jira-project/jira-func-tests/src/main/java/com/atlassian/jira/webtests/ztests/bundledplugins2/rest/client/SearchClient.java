package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;

import static com.atlassian.jira.rest.api.util.StringList.fromList;

/**
 * Client for the search resource.
 *
 * @since v4.3
 */
public class SearchClient extends RestApiClient<SearchClient>
{
    /**
     * Constructs a new SearchClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public SearchClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * Performs a search using a GET. The JQL query and other parameters are passed as query parameters.
     *
     * @param searchRequest a SearchRequest object
     * @return a SearchResult
     */
    public SearchResult getSearch(SearchRequest searchRequest)
    {
        return searchResourceForGet(searchRequest).get(SearchResult.class);
    }

    /**
     * Performs a search using a GET, and returns a Response. The JQL query and other parameters are passed as query
     * parameters.
     *
     * @param searchRequest a SearchRequest object
     * @return a Response
     */
    public Response getSearchResponse(final SearchRequest searchRequest)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return searchResourceForGet(searchRequest).get(ClientResponse.class);
            }
        });
    }

    /**
     * Performs a search using a POST. The JQL query and other parameters are passed in the JSON payload.
     *
     * @param searchRequest a SearchRequest object
     * @return a SearchResult
     */
    public SearchResult postSearch(SearchRequest searchRequest)
    {
        return searchResourceForPost().post(SearchResult.class, searchRequest);
    }

    /**
     * Performs a search using a GET, and returns a Response. The JQL query and other parameters are passed in the JSON
     * payload.
     *
     * @param searchRequest a SearchRequest object
     * @return a Response
     */
    public Response<SearchResult> postSearchResponse(final SearchRequest searchRequest)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return searchResourceForPost().post(ClientResponse.class, searchRequest);
            }
        }, SearchResult.class);
    }

    /**
     * Creates a WebResource.Builder for the search resource.
     *
     * @return a WebResource.Builder
     */
    private WebResource searchResource()
    {
        return createResource().path("search");
    }

    /**
     * Returns a WebResource.Builder that can be used to POST a search.
     *
     * @return a WebResource.Builder
     */
    private WebResource.Builder searchResourceForPost()
    {
        return searchResource().type(MediaType.APPLICATION_JSON_TYPE);
    }

    /**
     * Creates a WebResource that can be used to GET a search.
     *
     * @param searchRequest a SearchRequest
     * @return a WebResource
     */
    private WebResource searchResourceForGet(SearchRequest searchRequest)
    {
        WebResource resource = searchResource();
        if (searchRequest.jql != null)
        {
            resource = resource.queryParam("jql", searchRequest.jql);
        }

        if (searchRequest.startAt != null)
        {
            resource = resource.queryParam("startAt", searchRequest.startAt.toString());
        }

        if (searchRequest.maxResults != null)
        {
            resource = resource.queryParam("maxResults", searchRequest.maxResults.toString());
        }

        if (searchRequest.fields != null)
        {
            resource = resource.queryParam("fields", fromList(searchRequest.fields).toQueryParam());
        }

        if (searchRequest.expand != null)
        {
            resource = resource.queryParam("expand", fromList(searchRequest.expand).toQueryParam());
        }

        return resource;
    }
}
