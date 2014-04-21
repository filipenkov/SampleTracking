package com.atlassian.jira.rest.auth;

import com.atlassian.jira.security.websudo.InternalWebSudoManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * @since v4.3
 */
@Path ("websudo")
@Consumes ( { MediaType.APPLICATION_JSON } )
public class WebSudo
{
    private final InternalWebSudoManager internalWebSudoManager;

    public WebSudo (final InternalWebSudoManager internalWebSudoManager)
    {
        this.internalWebSudoManager = internalWebSudoManager;
    }

    @DELETE
    public Response release (@Context HttpServletRequest servletRequest, @Context HttpServletResponse servletResponse)
    {
        internalWebSudoManager.invalidateSession (servletRequest, servletResponse);

        return Response.ok().cacheControl(never()).build();
    }
}
