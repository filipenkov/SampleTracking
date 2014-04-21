package com.atlassian.streams.jira.rest.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import com.atlassian.streams.jira.JiraInlineActionHandler;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

/**
 * REST resource for any JIRA-specific inline action tasks.
 */
@Path("/actions")
public class JiraInlineActionResource
{
    private final JiraInlineActionHandler inlineActionHandler;

    public JiraInlineActionResource(JiraInlineActionHandler inlineActionHandler)
    {
        this.inlineActionHandler = checkNotNull(inlineActionHandler, "inlineActionHandler");
    }

    @Path("issue-watch/{issueKey}")
    @POST
    public Response watchIssue(@PathParam("issueKey") String issueKey)
    {
        boolean success = inlineActionHandler.startWatching(issueKey);
        if (success)
        {
            return Response.noContent().build();
        }
        else
        {
            return Response.status(PRECONDITION_FAILED).build();
        }
    }

    @Path("issue-vote/{issueKey}")
    @POST
    public Response voteIssue(@PathParam("issueKey") String issueKey)
    {
        boolean success = inlineActionHandler.voteOnIssue(issueKey);
        if (success)
        {
            return Response.noContent().build();
        }
        else
        {
            return Response.status(PRECONDITION_FAILED).build();
        }
    }
}
