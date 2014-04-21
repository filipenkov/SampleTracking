package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import java.util.List;

/**
 * Client for the Filter resource.
 *
 * @since v5.0
 */
public class FilterClient extends RestApiClient<FilterClient>
{
    /**
     * Constructs a new FilterClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public FilterClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * GETs the filter with the given ID.
     *
     * @param filterId a String containing a filter id
     * @return a Filter
     * @throws com.sun.jersey.api.client.UniformInterfaceException if there is a problem getting the filter
     */
    public Filter get(String filterId) throws UniformInterfaceException
    {
        return filterWithId(filterId).get(Filter.class);
    }

    /**
     * GETs the filter with the given ID, and returns a Response.
     *
     * @param filterId a String containing a filter ID
     * @return a Response
     */
    public Response getResponse(final String filterId)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return filterWithId(filterId).get(ClientResponse.class);
            }
        });
    }

    public List<Filter> getFavouriteFilters()
    {
        return createResource().path("filter").path("favourite").get(Filter.FILTER_TYPE);
    }

    /**
     * Returns a WebResource for the filter with the given ID.
     *
     * @param filterId a String containing a filter ID
     * @return a WebResource
     */
    protected WebResource filterWithId(String filterId)
    {
        return createResource().path("filter").path(filterId);
    }
}
