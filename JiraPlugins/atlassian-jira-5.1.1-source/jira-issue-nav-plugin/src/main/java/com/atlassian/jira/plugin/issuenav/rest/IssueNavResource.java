package com.atlassian.jira.plugin.issuenav.rest;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * REST resource for the issue navigator.
 *
 * @since v5.1
 */
@AnonymousAllowed
@Path ("issueNav")
public class IssueNavResource
{
    @GET
    @Produces ({ MediaType.APPLICATION_JSON })
    public Response getAppLinksInfo()
    {
        return Response.ok("{}").cacheControl(never()).build();
    }
}
