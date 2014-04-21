package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import java.util.List;

/**
 * Client for the field resource.
 *
 * @since v5.0
 */
public class FieldClient extends RestApiClient<FieldClient>
{
    /**
     * Constructs a new FieldClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public FieldClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * GETs all fieldes
     *
     * @return a Field
     * @throws com.sun.jersey.api.client.UniformInterfaceException if there's a problem getting the field
     */
    public List<Field> get() throws UniformInterfaceException
    {
        return field().get(new GenericType<List<Field>>(){});
    }

    /**
     * GETs the field with the given id, returning a Response object.
     *
     * @param fieldID a String containing the field id
     * @return a Response
     */
    public Response getResponse(final String fieldID)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return fieldWithID(fieldID).get(ClientResponse.class);
            }
        });
    }

    /**
     * Returns a WebResource for the all field.
     *
     * @return a WebResource
     */
    protected WebResource field()
    {
        return createResource().path("field");
    }

    /**
     * Returns a WebResource for the field having the given id.
     *
     * @param fieldID a String containing the field id
     * @return a WebResource
     */
    protected WebResource fieldWithID(String fieldID)
    {
        return createResource().path("field").path(fieldID);
    }
}
