package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;

import javax.ws.rs.core.MediaType;

/**
 * Client for LinkIssueResource
 *
 * @since v4.3
 */
public class LinkIssueClient extends RestApiClient<LinkIssueClient>
{

   public LinkIssueClient(JIRAEnvironmentData environmentData)
   {
       super(environmentData);
   }

   public LinkIssueClient(JIRAEnvironmentData environmentData, String version)
   {
       super(environmentData, version);
   }

    /**
     * Links the two issues specified in the LinkRequest using the specified
     * link type.
     *
     * @param linkRequest contains all information that is required two link two issues
     * 
     * @return the response two determine if the two issues are successfully linked to each other.
     */
    public Response linkIssues(final LinkRequest linkRequest)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource().path("issueLink").type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, linkRequest);
            }
        });
    }
}
