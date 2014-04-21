package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import java.util.EnumSet;

/**
 * Client for the issue resource.
 *
 * @since v4.3
 */
public class IssueClient extends RestApiClient<IssueClient>
{
    /**
     * Constructs a new IssueClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public IssueClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * GETs the issue with the given key.
     *
     * @param issueKey a String containing an issue key
     * @param expand the attributes to expand
     * @return an Issue
     * @throws UniformInterfaceException if there's a problem getting the issue
     */
    public Issue get(String issueKey, Issue.Expand... expand) throws UniformInterfaceException
    {
        return issueWithKey(issueKey, setOf(Issue.Expand.class, expand)).get(Issue.class);
    }

    /**
     * GETs the issue from the given URL.
     *
     * @param issueURL a String containing the valid URL for an issue
     * @param expand the attributes to expand
     * @return an Issue
     * @throws UniformInterfaceException if there's a problem getting the issue
     */
    public Issue getFromURL(String issueURL, Issue.Expand... expand) throws UniformInterfaceException
    {
        return expanded(resourceRoot(issueURL), setOf(Issue.Expand.class, expand)).get(Issue.class);
    }

    /**
     * GETs the issue with the given key, returning a Response.
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
                return issueWithKey(issueKey, setOf(Issue.Expand.class)).get(ClientResponse.class);
            }
        });
    }

    /**
     * Returns a WebResource for the issue with the given key.
     *
     * @param issueKey a String containing an issue key
     * @param expand what to expand
     * @return a WebResource
     */
    protected WebResource issueWithKey(String issueKey, EnumSet<Issue.Expand> expand)
    {
        return expanded(createResource().path("issue").path(issueKey), expand);
    }
}
