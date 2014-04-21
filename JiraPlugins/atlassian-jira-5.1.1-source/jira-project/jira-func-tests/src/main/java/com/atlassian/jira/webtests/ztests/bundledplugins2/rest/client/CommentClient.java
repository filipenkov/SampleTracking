package com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client;

import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;

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
     * @param issueKey the comment belongs to
     * @param commentID a String containing a comment id
     * @return a Comment
     * @throws UniformInterfaceException if there is a problem getting the comment
     */
    public Response<Comment> get(final String issueKey, final String commentID) throws UniformInterfaceException
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return commentWithID(issueKey, commentID).get(ClientResponse.class);
            }
        }, Comment.class);
    }

    public Response<CommentsWithPaginationBean> getComments(final String issueKey)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource().path("issue").path(issueKey).path("comment").get(ClientResponse.class);
            }
        }, CommentsWithPaginationBean.class);
    }

    public Response<Comment> put(final String issueKey, final Comment comment)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource().path("issue").path(issueKey).path("comment").path(comment.id).type(MediaType.APPLICATION_JSON_TYPE).put(ClientResponse.class, comment);
            }
        }, Comment.class);
    }

    public Response<Comment> post(final String issueKey, final Comment comment)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource().path("issue").path(issueKey).path("comment").type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, comment);
            }
        }, Comment.class);
    }

    public Response delete(final String issueKey, final Comment comment)
    {
        return delete(issueKey, comment.id);
    }

    public Response delete(final String issueKey, final String commentId)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return createResource().path("issue").path(issueKey).path("comment").path(commentId).type(MediaType.APPLICATION_JSON_TYPE).delete(ClientResponse.class);
            }
        });
    }

    /**
     * GETs the comment with the given ID, and returns a Response.
     *
     * @param issueKey the comment belongs to
     * @param commentID a String containing a comment ID
     * @return a Response
     */
    public Response getResponse(final String issueKey, final String commentID)
    {
        return toResponse(new Method()
        {
            @Override
            public ClientResponse call()
            {
                return commentWithID(issueKey, commentID).get(ClientResponse.class);
            }
        });
    }

    /**
     * Returns a WebResource for the comment with the given ID.
     *
     * @param issueKey the comment belongs to
     * @param commentID a String containing a comment ID
     * @return a WebResource
     */
    protected WebResource commentWithID(String issueKey, String commentID)
    {
        return createResource().path("issue").path(issueKey).path("comment").path(commentID);
    }
}
