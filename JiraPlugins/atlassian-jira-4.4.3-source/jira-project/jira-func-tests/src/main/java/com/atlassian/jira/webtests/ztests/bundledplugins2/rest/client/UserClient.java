package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.util.EnumSet;

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

    /**
     * GETs the user with the given username, returning a Response object.
     *
     * @param username a String containing the username
     * @return a Response
     */
    public Response getResponse(final String username)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return userWithUsername(username, setOf(User.Expand.class)).get(ClientResponse.class);
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
