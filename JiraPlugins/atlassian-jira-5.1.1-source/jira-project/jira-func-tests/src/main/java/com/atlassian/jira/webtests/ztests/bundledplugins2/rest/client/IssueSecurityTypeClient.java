package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

/**
 * Client for issue type.
 *
 * @since v4.3
 */
public class IssueSecurityTypeClient extends RestApiClient<IssueSecurityTypeClient>
{
    /**
     * Constructs a new IssueSecurityTypeClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public IssueSecurityTypeClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * GETs the issue type with the given id.
     *
     * @param issueSecurityTypeID a String containing the issue type id
     * @return an IssueSecurityType
     * @throws com.sun.jersey.api.client.UniformInterfaceException if there is a problem getting the issue type
     */
    public IssueSecurityType get(String issueSecurityTypeID) throws UniformInterfaceException
    {
        return issueSecurityTypeWithID(issueSecurityTypeID).get(IssueSecurityType.class);
    }

    /**
     * GETs the issue type with the given id, returning a Response.
     *
     * @param issueSecurityTypeID a String containing the issue type id
     * @return a Response
     */
    public Response getResponse(final String issueSecurityTypeID)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return issueSecurityTypeWithID(issueSecurityTypeID).get(ClientResponse.class);
            }
        });
    }

    /**
     * Creates a WebResource for the issue type with the given id.
     *
     * @param issueSecurityTypeID a String containing the issue type id
     * @return a WebResource
     */
    private WebResource issueSecurityTypeWithID(String issueSecurityTypeID)
    {
        return createResource().path("securitylevel").path(issueSecurityTypeID);
    }
}
