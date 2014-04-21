package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import java.util.List;

/**
 * Client class for the Resolution resource.
 *
 * @since v4.3
 */
public class ResolutionClient extends RestApiClient<ResolutionClient>
{
    /**
     * Constructs a new ResolutionClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public ResolutionClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * GETs all priorities
     *
     * @return a list of Resolution
     * @throws UniformInterfaceException if anything goes wrong
     */
    public List<Resolution> get() throws UniformInterfaceException
    {
        return resolution().get(new GenericType<List<Resolution>>(){});
    }

    /**
     * GETs the resolution having the given id.
     *
     * @param resolutionID a String containing the resolution id
     * @return a Resolution
     * @throws UniformInterfaceException if there is an error
     */
    public Resolution get(String resolutionID) throws UniformInterfaceException
    {
        return resolutionWithID(resolutionID).get(Resolution.class);
    }

    /**
     * GETs the resolution having the given id, and returns a Reponse.
     *
     * @param resolutionID a String containing the resolution id
     * @return a Response
     */
    public Response getResponse(final String resolutionID)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return resolutionWithID(resolutionID).get(ClientResponse.class);
            }
        });
    }

    /**
     * Creates a WebResource for the resolution having the given id.
     *
     * @param resolutionID a String containing the resolution id
     * @return a WebResource
     */
    private WebResource resolutionWithID(String resolutionID)
    {
        return createResource().path("resolution").path(resolutionID);
    }

    /**
     * Creates a WebResource for the resolutions
     *
     * @return a WebResource
     */
    private WebResource resolution()
    {
        return createResource().path("resolution");
    }
}
