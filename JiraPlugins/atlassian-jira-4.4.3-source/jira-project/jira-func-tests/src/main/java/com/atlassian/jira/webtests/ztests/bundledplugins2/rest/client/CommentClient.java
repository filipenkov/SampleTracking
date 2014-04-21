package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

/**
 * Client for the Comment resource.
 *
 * @since v4.3
 */
public class CommentClient extends RestApiClient<CommentClient>
{
    /**
     * Constructs a new CommentClient for a JIRA instance.
     *
     * @param environmentData The JIRA environment data
     */
    public CommentClient(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    /**
     * GETs the comment with the given ID.
     *
     * @param commentID a String containing a comment id
     * @return a Comment
     * @throws UniformInterfaceException if there is a problem getting the comment
     */
    public Comment get(String commentID) throws UniformInterfaceException
    {
        return commentWithID(commentID).get(Comment.class);
    }

    /**
     * GETs the comment with the given ID, and returns a Response.
     *
     * @param commentID a String containing a comment ID
     * @return a Response
     */
    public Response getResponse(final String commentID)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return commentWithID(commentID).get(ClientResponse.class);
            }
        });
    }

    /**
     * Returns a WebResource for the comment with the given ID.
     *
     * @param commentID a String containing a comment ID
     * @return a WebResource
     */
    protected WebResource commentWithID(String commentID)
    {
        return createResource().path("comment").path(commentID);
    }
}
