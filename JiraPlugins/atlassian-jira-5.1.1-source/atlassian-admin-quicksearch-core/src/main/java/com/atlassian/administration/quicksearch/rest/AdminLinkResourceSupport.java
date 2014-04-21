package com.atlassian.administration.quicksearch.rest;

import com.atlassian.administration.quicksearch.spi.AdminLinkManager;
import com.atlassian.administration.quicksearch.spi.AliasProviderConfiguration;
import com.atlassian.administration.quicksearch.spi.UserContext;
import com.atlassian.administration.quicksearch.spi.UserContextProvider;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

/**
 * Generic, reusable administration link REST resource. Delegate calls from your annotated REST resource
 * to {@link #getAdminLinksResponse(String, javax.servlet.http.HttpServletRequest)}.
 *
 * @since 1.0
 */
public class AdminLinkResourceSupport extends AbstractAdminLinkResource
{

    private final UserContextProvider userContextProvider;
    private final boolean stripRootSections;

    public AdminLinkResourceSupport(UserContextProvider userContextProvider, AdminLinkManager linkManager,
                                    AliasProviderConfiguration aliasProviderConfiguration)
    {
        this(userContextProvider, linkManager, aliasProviderConfiguration, true);
    }

    public AdminLinkResourceSupport(UserContextProvider userContextProvider, AdminLinkManager linkManager,
                                    AliasProviderConfiguration aliasProviderConfiguration,
                                    boolean stripRootSections)
    {
        super(linkManager, aliasProviderConfiguration.getAliasProvider());
        this.userContextProvider = userContextProvider;
        this.stripRootSections = stripRootSections;
    }

    public LocationBean getAdminLinks(String location, HttpServletRequest request)
    {
        checkLocation(location);
        return getAdminLinks(ImmutableList.of(location), request);
    }

    public Response getAdminLinksResponse(String location, HttpServletRequest request)
    {
        return getSuccessfulResponse(getAdminLinks(location, request));
    }

    public LocationBean getAdminLinks(Iterable<String> locations, HttpServletRequest request)
    {
        final UserContext userContext = userContextProvider.getUserContext(request);
        checkUser(userContext);
        checkLocations(locations);
        return getLinksFor(locations, userContext, stripRootSections);
    }


    public Response getAdminLinksResponse(Iterable<String> locations, HttpServletRequest request)
    {
        return getSuccessfulResponse(getAdminLinks(locations, request));
    }

    public Response getSuccessfulResponse(LocationBean locationBean)
    {
        // TODO no-cache?
        return Response.ok(locationBean).build();
    }

    private void checkUser(UserContext userContext)
    {
        if (userContext == null)
        {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }

    private void checkLocation(String location)
    {
        if (StringUtils.isEmpty(location))
        {
            throw new WebApplicationException(badRequest("Parameter 'location' required"));
        }
    }

    private void checkLocations(Iterable<String> locations)
    {
        if (Iterables.isEmpty(locations))
        {
            throw new WebApplicationException(badRequest("Parameter 'location' required"));
        }
    }

    public Response badRequest(String message)
    {
        return Response.status(Response.Status.BAD_REQUEST).entity(message).cacheControl(never()).build();
    }

    private CacheControl never()
    {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoStore(true);
        cacheControl.setNoCache(true);
        return cacheControl;
    }


}
