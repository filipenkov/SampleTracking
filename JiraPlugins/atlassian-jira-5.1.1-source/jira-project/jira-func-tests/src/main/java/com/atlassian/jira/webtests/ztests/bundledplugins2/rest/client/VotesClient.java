package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * Client for the votes sub-resource.
 *
 * @since v4.3
 */
public class VotesClient extends RestApiClient<VotesClient>
{
    /**
     * Constructs a new VotesClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public VotesClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * Gets the voters sub-resource for the issue with the given key.
     *
     * @param issueKey a String containing an issue key
     * @return a Vote
     */
    public Vote get(String issueKey)
    {
        return votersForIssueWithID(issueKey).get(Vote.class);
    }

    /**
     * Gets the voters sub-resource for the issue with the given key, returning a Response object.
     *
     * @param issueKey a String containing an issue key
     * @return a Response
     */
    public Response getResponse(final String issueKey)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return votersForIssueWithID(issueKey).get(ClientResponse.class);
            }
        });
    }

    /**
     * POSTs the users's vote to the issue with the given key, returning a Response object.
     *
     * @param issueKey a String containing an issue key
     * @return a Response
     */
    public Response postResponse(final String issueKey)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return votersForIssueWithID(issueKey).type(APPLICATION_JSON_TYPE).post(ClientResponse.class, "{}");
            }
        });
    }

    /**
     * DELETEs the user's vote from the issue with the given key, returning a Response object.
     *
     * @param issueKey a String containing an issue key
     * @return a Response
     */
    public Response deleteResponse(final String issueKey)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return votersForIssueWithID(issueKey).delete(ClientResponse.class);
            }
        });
    }

    /**
     * Returns a WebResource for the voters sub-resource of the issue having the given key.
     *
     * @param issueKey a String containing an issue key
     * @return a WebResource
     */
    private WebResource votersForIssueWithID(String issueKey)
    {
        return createResource().path("issue").path(issueKey).path("votes");
    }
}
