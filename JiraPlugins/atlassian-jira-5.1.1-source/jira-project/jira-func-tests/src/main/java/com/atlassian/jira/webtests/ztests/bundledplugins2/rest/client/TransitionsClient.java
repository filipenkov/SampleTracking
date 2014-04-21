package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * Client for the transitions sub-resource.
 *
 * @since v4.3
 */
public class TransitionsClient extends RestApiClient<TransitionsClient>
{
    /**
     * Constructs a new TransitionsClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public TransitionsClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * GETs the transitions sub-resource for the issue with the given key.
     *
     * @param issueKey a String containing an issue key
     * @return a Transitions
     * @throws UniformInterfaceException if there's a problem
     */
    public IssueTransitionsMeta get(String issueKey) throws UniformInterfaceException
    {
        return transitionsForIssueWithKey(issueKey).get(IssueTransitionsMeta.class);
    }

    public Response postResponse(final String issueKey, final IssueUpdateRequest issueUpdateRequest)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return transitionsForIssueWithKey(issueKey).type(APPLICATION_JSON_TYPE).post(ClientResponse.class, issueUpdateRequest);
            }
        });
    }

    /**
     * Returns a WebResource for the transitions of the issue with the given key.
     *
     * @param issueKey a String containing an issue key
     * @return a WebResource
     */
    private WebResource transitionsForIssueWithKey(String issueKey)
    {
        return createResource().path("issue").path(issueKey).path("transitions").queryParam("expand", "transitions.fields");
    }
}
