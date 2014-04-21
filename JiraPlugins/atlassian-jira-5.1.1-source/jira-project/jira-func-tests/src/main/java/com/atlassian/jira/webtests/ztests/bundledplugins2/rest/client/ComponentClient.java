package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;

import java.net.URI;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * Client for the component resource.
 *
 * @since v4.3
 */
public class ComponentClient extends RestApiClient<ComponentClient>
{
    /**
     * Constructs a new ComponentClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public ComponentClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * GETs the component with the given ID.
     *
     * @param componentID a String containing a component ID
     * @return a Component
     * @throws UniformInterfaceException if anything goes wrong
     */
    public Component get(String componentID) throws UniformInterfaceException
    {
        return componentWithId(componentID).get(Component.class);
    }

    /**
     * GETs the component with the given ID, and returns the Response.
     *
     * @param componentID a String containing the component ID
     * @return a Response
     */
    public Response getResponse(final String componentID)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return componentWithId(componentID).get(ClientResponse.class);
            }
        });
    }

    /**
     * Returns the web resource for a given comment.
     *
     * @param componentID a String containing the comment ID
     * @return a WebResource
     */
    protected WebResource componentWithId(String componentID)
    {
        return createResource().path("component").path(componentID);
    }
    
    public Component create(Component component)
    {
        try
        {
            return component().post(Component.class, component);
        }
        catch (UniformInterfaceException e)
        {
            throw new RuntimeException("Failed to create component: " + errorResponse(e.getResponse()), e);
        }
    }

    public Response createResponse(final Component component)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return component().post(ClientResponse.class, component);
            }
        });
    }

    public Response putResponse(final String componentId, final Component component)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return componentWithId(componentId).type(APPLICATION_JSON_TYPE).put(ClientResponse.class, component);
            }
        });
    }

    public Response putResponse(final Component component)
    {
        final String[] selfParts = component.self.split("/");
        final String componentId = selfParts[selfParts.length - 1];
        return putResponse(componentId, component);
    }

    public ComponentIssueCounts getComponentIssueCounts(String componentId) throws UniformInterfaceException
    {
        return componentWithId(componentId).path("relatedIssueCounts").get(ComponentIssueCounts.class);
    }

    public Response getComponentIssueCountsResponse(final String componentId)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return componentWithId(componentId).path("relatedIssueCounts").get(ClientResponse.class);
            }
        });
    }

    public Response delete(final String componentId) throws UniformInterfaceException
    {
        return delete(componentId, null);
    }

    public Response delete(final String componentId, final URI swapComponent) throws UniformInterfaceException
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                WebResource deleteResource = componentWithId(componentId);
                if (swapComponent != null)
                {
                    deleteResource = deleteResource.queryParam("moveIssuesTo", swapComponent.getPath());
                }
                return deleteResource.delete(ClientResponse.class);
            }
        });
    }

    
    /**
     * Returns a WebResponse for the component resource
     *
     * @return a WebResource
     */
    private WebResource.Builder component()
    {
        return createResource().path("component").type(MediaType.APPLICATION_JSON_TYPE);
    }
}
