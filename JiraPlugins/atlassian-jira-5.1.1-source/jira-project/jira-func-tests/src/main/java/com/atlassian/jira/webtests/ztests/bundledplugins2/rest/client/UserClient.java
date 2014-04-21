package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Client for the user resource.
 *
 * @since v4.3
 */
public class UserClient extends RestApiClient<UserClient>
{
    /**
     * Constructs a new UserClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public UserClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * GETs the user with the given username.
     *
     * @param username a String containing the username
     * @param expand a set of attributes to expand
     * @return a User
     */
    public User get(String username, User.Expand... expand)
    {
        return userWithUsername(username, setOf(User.Expand.class, expand)).get(User.class);
    }

    public List<User> searchAssignable(String query, String issueKey, String startAt, String maxResults)
    {
        WebResource resource = getSearchAssignableResource(query, issueKey, startAt, maxResults);
        return Arrays.asList(resource.get(User[].class));
    }

    public List<User> multiProjectSearchAssignable(String query, String projectKeys, String startAt, String maxResults)
    {
        WebResource resource = getMultiProjectSearchAssignableResource(query, projectKeys, startAt, maxResults);
        return Arrays.asList(resource.get(User[].class));
    }
    
    public List<User> searchViewableIssue(String query, String issueKey, String startAt, String maxResults)
    {
        WebResource resource = getSearchViewableIssueResource(query, issueKey, startAt, maxResults);
        return Arrays.asList(resource.get(User[].class));
    }

    public List<User> search(String query, String startAt, String maxResults)
    {
        WebResource resource = getSearchResource(query, startAt, maxResults);
        return Arrays.asList(resource.get(User[].class));
    }

    public WebResource getSearchAssignableResource(String query, String issueKey, String startAt, String maxResults)
    {
        WebResource resource = applyPagingParams(query, startAt, maxResults, createResource().path("user").path("assignable").path("search"));
        resource = resource.queryParam("issueKey", issueKey);
        return resource;
    }

    public WebResource getMultiProjectSearchAssignableResource(String query, String projectKeys, String startAt, String maxResults)
    {
        WebResource resource = applyPagingParams(query, startAt, maxResults, createResource().path("user").path("assignable").path("multiProjectSearch"));
        resource = resource.queryParam("projectKeys", projectKeys);
        return resource;
    }
    
    public WebResource getSearchViewableIssueResource(String query, String issueKey, String startAt, String maxResults)
    {
        WebResource resource = applyPagingParams(query, startAt, maxResults, createResource().path("user").path("viewissue").path("search"));
        resource = resource.queryParam("issueKey", issueKey);
        return resource;
    }

    public WebResource getSearchResource(String query, String startAt, String maxResults)
    {
        return applyPagingParams(query, startAt, maxResults, createResource().path("user").path("search"));
    }

    private WebResource applyPagingParams(String query, String startAt, String maxResults, WebResource resource)
    {
        resource = resource.queryParam("username", query);
        if (StringUtils.isNotBlank(startAt))
        {
            resource = resource.queryParam("startAt", startAt);
        }
        if (StringUtils.isNotBlank(maxResults))
        {
            resource = resource.queryParam("maxResults", maxResults);
        }
        return resource;
    }

    /**
     * GETs the user with the given username, returning a Response object.
     *
     * @param username a String containing the username
     * @return a Response
     */
    public Response getUserResponse(final String username)
    {
        return getResponse(userWithUsername(username, setOf(User.Expand.class)));
    }
    
    public Response getResponse(final WebResource resource)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return resource.get(ClientResponse.class);
            }
        });
    }

    /**
     * Returns a WebResource for the user with the given username.
     *
     * @param username a String containing the username
     * @param expands an EnumSet indicating what attributes to expand
     * @return a WebResource
     */
    private WebResource userWithUsername(String username, EnumSet<User.Expand> expands)
    {
        WebResource result = createResource().path("user");
        if (username != null)
        {
            result = result.queryParam("username", percentEncode(username));
        }

        return expanded(result, expands);
    }
}
