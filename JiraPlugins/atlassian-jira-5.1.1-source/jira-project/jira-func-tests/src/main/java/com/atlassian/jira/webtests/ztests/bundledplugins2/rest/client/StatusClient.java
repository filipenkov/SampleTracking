package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import java.util.List;

/**
 * Client for the status resource.
 *
 * @since v4.3
 */
public class StatusClient extends RestApiClient<StatusClient>
{
    /**
     * Constructs a new StatusClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public StatusClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * GETs the status with the given id.
     *
     * @param statusID a String containing the status id
     * @return a Status
     * @throws UniformInterfaceException if there's a problem getting the status
     */
    public Status get(String statusID) throws UniformInterfaceException
    {
        return statusWithID(statusID).get(Status.class);
    }

    /**
     * GETs all statuses
     *
     * @return a List of Statuses
     * @throws UniformInterfaceException if there's a problem getting the status
     */
    public List<Status> get() throws UniformInterfaceException
    {
        return status().get(new GenericType<List<Status>>(){});
    }

    /**
     * GETs the status with the given id, returning a Response object.
     *
     * @param statusID a String containing the status id
     * @return a Response
     */
    public Response getResponse(final String statusID)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return statusWithID(statusID).get(ClientResponse.class);
            }
        });
    }

    /**
     * Returns a WebResource for the all status.
     *
     * @return a WebResource
     */
    protected WebResource status()
    {
        return createResource().path("status");
    }

    /**
     * Returns a WebResource for the status having the given id.
     *
     * @param statusID a String containing the status id
     * @return a WebResource
     */
    protected WebResource statusWithID(String statusID)
    {
        return createResource().path("status").path(statusID);
    }
}
