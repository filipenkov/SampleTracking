package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import java.util.Map;

/**
 * Client for the Attachment resource.
 *
 * @since v4.3
 */
public class AttachmentClient extends RestApiClient<AttachmentClient>
{
    /**
     * Constructs a new AttachmentClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public AttachmentClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * GETs the attachment with the given id.
     *
     * @param attachmentID a String containing the attachment id
     * @return an Attachment
     * @throws com.sun.jersey.api.client.UniformInterfaceException if there is a problem
     */
    public Attachment get(String attachmentID) throws UniformInterfaceException
    {
        return attachmentWithID(attachmentID)
                .get(Attachment.class);
    }

    /**
     * GETs the attachment with the given ID, and returns the Response.
     *
     * @param attachmentID a String containing the attachment ID
     * @return a Response
     */
    public Response getResponse(final String attachmentID)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return attachmentWithID(attachmentID).get(ClientResponse.class);
            }
        });
    }

    /**
     * Deletes the attachment with the given ID, and returns the Response.
     *
     * @param attachmentID a String containing the attachment ID
     * @return a Response
     */
    public Response deleteResponse(final String attachmentID)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return attachmentWithID(attachmentID).delete(ClientResponse.class);
            }
        });
    }

    /**
     * Returns the WebResource for the attachment having the given id.
     *
     * @param attachmentID a String containing the attachment id
     * @return a WebResource
     */
    protected WebResource attachmentWithID(String attachmentID)
    {
        return createResource().path("attachment").path(attachmentID);
    }

    /**
     * GETs the global attachment meta
     *
     * @return a Response
     */
    public Map getMeta()
    {
        return attachmentMeta().get(Map.class);
    }

    /**
     * Returns the WebResource for the attachment meta
     *
     * @return a WebResource
     */
    protected WebResource attachmentMeta()
    {
        return createResource().path("attachment").path("meta");
    }

}
