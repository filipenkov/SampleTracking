package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import java.util.List;

/**
 * TODO: Document this class / interface here
 *
 * @since v4.3
 */
public class PriorityClient extends RestApiClient<PriorityClient>
{
    /**
     * Constructs a new PriorityClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public PriorityClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * GETs all priorities
     *
     * @return a list of Priority
     * @throws UniformInterfaceException if anything goes wrong
     */
    public List<Priority> get() throws UniformInterfaceException
    {
        return priority().get(new GenericType<List<Priority>>(){});
    }

    /**
     * GETs the priority with the given ID.
     *
     * @param priorityID a String containing a priority ID
     * @return a Priority
     * @throws UniformInterfaceException if anything goes wrong
     */
    public Priority get(String priorityID) throws UniformInterfaceException
    {
        return priorityWithID(priorityID).get(Priority.class);
    }

    /**
     * GETs the priority with the given ID, returning a Response object.
     *
     * @param priorityID a String containing a priority ID
     * @return a Response
     */
    public Response getResponse(final String priorityID)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return priorityWithID(priorityID).get(ClientResponse.class);
            }
        });
    }

    /**
     * Returns a WebResource for priorities.
     *
     * @return a WebResource
     */
    protected WebResource priority()
    {
        return createResource().path("priority");
    }

    /**
     * Returns a WebResource for the priority having the given ID.
     *
     * @param priorityID a String containing a priority ID
     * @return a WebResource
     */
    protected WebResource priorityWithID(String priorityID)
    {
        return createResource().path("priority").path(priorityID);
    }
}
