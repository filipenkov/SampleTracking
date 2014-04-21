package com.atlassian.gadgets.directory.internal.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.atlassian.gadgets.dashboard.PermissionException;
import com.atlassian.gadgets.directory.internal.DirectoryConfigurationPermissionChecker;
import com.atlassian.gadgets.directory.internal.DirectoryUrlBuilder;
import com.atlassian.gadgets.directory.internal.GadgetFeedsSpecProvider;
import com.atlassian.gadgets.directory.internal.jaxb.SubscribedGadgetFeedRepresentation;
import com.atlassian.sal.api.message.I18nResolver;

import static com.google.common.base.Preconditions.checkNotNull;

@Path("/directory/subscribed-gadget-feeds/{id}")
public class SubscribedGadgetFeedResource
{
    private final GadgetFeedsSpecProvider feedsProvider;
    private final I18nResolver i18n;
    private final DirectoryConfigurationPermissionChecker gadgetUrlChecker;
    private final DirectoryUrlBuilder urlBuilder;

    public SubscribedGadgetFeedResource(GadgetFeedsSpecProvider feedsProvider,
            I18nResolver i18n,
            DirectoryConfigurationPermissionChecker gadgetUrlChecker,
            DirectoryUrlBuilder urlBuilder)
    {
        this.feedsProvider = checkNotNull(feedsProvider, "feedsProvider");
        this.i18n = checkNotNull(i18n, "i18n");
        this.gadgetUrlChecker = checkNotNull(gadgetUrlChecker, "gadgetUrlChecker");
        this.urlBuilder = checkNotNull(urlBuilder, "urlBuilder");
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public SubscribedGadgetFeedRepresentation get(@PathParam("id") String feedId)
    {
        checkFeedId(feedId);

        return new SubscribedGadgetFeedRepresentation(feedsProvider.getFeedProvider(feedId), urlBuilder);
    }

    @DELETE
    public Response remove(@PathParam("id") String feedId, @Context HttpServletRequest request)
    {
        checkForPermission(request);
        checkFeedId(feedId);
        feedsProvider.removeFeed(feedId);
        return Response.ok().build();
    }

    private void checkFeedId(String feedId)
    {
        if (!feedsProvider.containsFeed(feedId))
        {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }
    
    private void checkForPermission(HttpServletRequest request)
    {
        try
        {
            gadgetUrlChecker.checkForPermissionToConfigureDirectory(request);
        }
        catch (PermissionException e)
        {
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                .entity(i18n.getText("directoryResource.no.write.permission"))
                .type("text/plain")
                .build());
        }
    }
}
