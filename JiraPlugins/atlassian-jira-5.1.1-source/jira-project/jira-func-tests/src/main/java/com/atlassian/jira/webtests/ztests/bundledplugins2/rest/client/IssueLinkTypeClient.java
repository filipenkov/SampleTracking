package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 *
 * @since v4.3
 */
public class IssueLinkTypeClient extends RestApiClient<IssueLinkTypeClient>
{
   public IssueLinkTypeClient(JIRAEnvironmentData environmentData)
   {
       super(environmentData);
   }

   public IssueLinkTypeClient(JIRAEnvironmentData environmentData, String version)
   {
       super(environmentData, version);
   }

    public IssueLinkTypes getIssueLinkTypes()
    {
        return issueLinkType().get(IssueLinkTypes.class);
    }

    private WebResource issueLinkType()
    {
        return createResource().path("issueLinkType");
    }

    public IssueLinkType getIssueLinkType(String issueLinkTypeID)
    {
        return issueLinkTypeID(issueLinkTypeID).get(IssueLinkType.class);
    }

    private WebResource issueLinkTypeID(String issueLinkTypeID)
    {
        return createResource().path("issueLinkType/" + issueLinkTypeID);
    }

    public Response getResponseForLinkType(final String issueLinkTypeID)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return issueLinkTypeID(issueLinkTypeID).get(ClientResponse.class);
            }
        });
    }

    public Response getResponseForAllLinkTypes()
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return issueLinkType().get(ClientResponse.class);
            }
        });
    }
}
