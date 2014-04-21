package com.atlassian.administration.quicksearch.jira.rest;

import com.atlassian.administration.quicksearch.rest.AdminLinkResourceSupport;
import com.atlassian.administration.quicksearch.spi.AdminLinkManager;
import com.atlassian.administration.quicksearch.spi.AliasProviderConfiguration;
import com.atlassian.administration.quicksearch.spi.UserContextProvider;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * JIRA administration link REST resource.
 *
 * @since 1.0
 */
@Path("/links")
@Produces(MediaType.APPLICATION_JSON)
public class JiraAdminLinkResource
{

    private final AdminLinkResourceSupport support;

    public JiraAdminLinkResource(UserContextProvider userContextProvider, AdminLinkManager linkManager,
                                 AliasProviderConfiguration aliasProviderConfiguration)
    {
        this.support = new AdminLinkResourceSupport(userContextProvider, linkManager, aliasProviderConfiguration);
    }

    @GET
    @Path("/{location}")
    public Response getAdminLinks(@PathParam("location") String location, @Context HttpServletRequest request)
    {
        return support.getAdminLinksResponse(location, request);
    }

    @GET
    @Path("/default")
    public Response getAdminLinksInDefaultLocation(@Context HttpServletRequest request)
    {
        return support.getAdminLinksResponse(getDefaultLocation(), request);
    }

    protected String getDefaultLocation()
    {
        return "system.admin.top.navigation.bar";
    }
}
