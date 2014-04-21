package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

/**
 * Client for the work log resource.
 *
 * @since v4.3
 */
public class WorklogClient extends RestApiClient<WorklogClient>
{
    /**
     * Constructs a new WorklogClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public WorklogClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * GETs the work log with the given id.
     *
     * @param worklogID a String containing the work log id
     * @return a Worklog
     * @throws UniformInterfaceException if there is a problem getting the work log
     */
    public Worklog get(String worklogID) throws UniformInterfaceException
    {
        return worklogWithID(worklogID).get(Worklog.class);
    }

    /**
     * GETs the work log with the given id, returning a Response object.
     *
     * @param worklogID a String containing the work log id
     * @return a Response
     */
    public Response getResponse(final String worklogID)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return worklogWithID(worklogID).get(ClientResponse.class);
            }
        });
    }

    /**
     * Returns a WebResource for the work log with the given id.
     *
     * @param worklogID a String containing the work log id
     * @return a WebResource
     */
    protected WebResource worklogWithID(String worklogID)
    {
        return createResource().path("worklog").path(worklogID);
    }
}
