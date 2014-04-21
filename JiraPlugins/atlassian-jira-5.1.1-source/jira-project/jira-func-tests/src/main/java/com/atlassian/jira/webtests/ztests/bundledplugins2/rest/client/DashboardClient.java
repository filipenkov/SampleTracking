package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.rest.api.dashboard.DashboardBean;
import com.atlassian.jira.rest.api.dashboard.DashboardsBean;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import javax.annotation.Nullable;

/**
 * REST client for testing the /dashboard resource.
 *
 * @since v5.0
 */
public class DashboardClient extends RestApiClient<DashboardClient>
{
    public DashboardClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public DashboardClient(JIRAEnvironmentData environmentData, String version)
    {
        super(environmentData, version);
    }

    /**
     * Gets a single dashboard by id.
     *
     * @param dashboardId the dashboard id
     * @return a DashboardBean
     */
    public DashboardBean getSingle(String dashboardId)
    {
        try
        {
            return createResource().path("dashboard").path(dashboardId).get(DashboardBean.class);
        }
        catch (UniformInterfaceException e)
        {
            throw new RuntimeException("Failed to get list of dashboards: " + errorResponse(e.getResponse()), e);
        }
    }

    /**
     * Gets a single dashboard by id, expecting an error.
     *
     *
     * @param dashboardId the dashboard id
     * @return a DashboardBean
     */
    public Response getSingleResponse(final String dashboardId)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource().path("dashboard").path(dashboardId).get(ClientResponse.class);
            }
        });
    }

    /**
     * Gets a list of dashboards, optionally filtered by "my" or "favourite".
     *
     * @param filter an optional String containing the filter
     * @param startAt the index of the first dashboard to return (0-based)
     * @param maxResults the maximum number of dashboards to return in a single request
     * @return a DashboardsBean
     */
    public DashboardsBean getList(@Nullable String filter, @Nullable Integer startAt, @Nullable Integer maxResults)
    {
        try
        {
            WebResource resource = createResource(filter, startAt, maxResults);

            return resource.get(DashboardsBean.class);
        }
        catch (UniformInterfaceException e)
        {
            throw new RuntimeException("Failed to get list of dashboards: " + errorResponse(e.getResponse()), e);
        }
    }

    /**
     * Gets a list of dashboards, expecting an error.
     *
     * @param filter an optional String containing the filter
     * @param startAt the index of the first dashboard to return (0-based)
     * @param maxResults the maximum number of dashboards to return in a single request
     * @return a DashboardsBean
     */
    public Response getListResponse(final String filter, final Integer startAt, final Integer maxResults)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource(filter, startAt, maxResults).get(ClientResponse.class);
            }
        });
    }

    private WebResource createResource(String filter, Integer startAt, Integer maxResults)
    {
        WebResource resource = createResource().path("dashboard");
        if (filter != null) { resource = resource.queryParam("filter", filter); }
        if (startAt != null) { resource = resource.queryParam("startAt", String.valueOf(startAt)); }
        if (maxResults != null) { resource = resource.queryParam("maxResults", String.valueOf(maxResults)); }

        return resource;
    }
}
