package com.atlassian.gadgets.directory.internal.rest;

import com.atlassian.gadgets.dashboard.PermissionException;
import com.atlassian.gadgets.directory.internal.*;
import com.atlassian.gadgets.directory.internal.GadgetFeedsSpecProvider.FeedSpecProvider;
import com.atlassian.gadgets.directory.internal.jaxb.SubscribedGadgetFeedRepresentation;
import com.atlassian.gadgets.directory.internal.jaxb.SubscribedGadgetFeedsRepresentation;
import com.atlassian.sal.api.message.I18nResolver;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.isEmpty;

@Path("/directory/subscribed-gadget-feeds")
public class SubscribedGadgetFeedsResource
{
    private final Log logger = LogFactory.getLog(getClass());
    
    private final GadgetFeedsSpecProvider provider;
    private final DirectoryConfigurationPermissionChecker gadgetUrlChecker;
    private final I18nResolver i18n;
    private final DirectoryUrlBuilder urlBuilder;
    
    public SubscribedGadgetFeedsResource(
            GadgetFeedsSpecProvider provider,
            DirectoryConfigurationPermissionChecker gadgetUrlChecker,
            I18nResolver i18n,
            DirectoryUrlBuilder urlBuilder)
    {
        this.provider = checkNotNull(provider, "provider");
        this.gadgetUrlChecker = checkNotNull(gadgetUrlChecker, "gadgetUrlChecker");
        this.i18n = checkNotNull(i18n, "i18n");
        this.urlBuilder = checkNotNull(urlBuilder, "urlBuilder");
    }
    
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response get()
    {
        return Response.ok(new SubscribedGadgetFeedsRepresentation(provider.getFeedProviders(), urlBuilder)).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response add(@Context HttpServletRequest request, Reader jsonContent)
    {
        URI feedUri = getFeedUri(jsonContent);

        if (!feedUri.isAbsolute())
        {
            logger.error("Subscribed Gadget Feeds Resource: POST rejected due to invalid 'url' parameter: url must be absolute");
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(i18n.getText("subscribed.gadget.feeds.invalid.url.parameter"))
                .type("text/plain")
                .build();
        }
        try
        {
            logger.debug("Subscribed Gadget Feeds Resource: POST received: url=" + feedUri);
            gadgetUrlChecker.checkForPermissionToConfigureDirectory(request);
            FeedSpecProvider feedProvider = provider.addFeed(feedUri);
            logger.debug("Subscribed Gadget Feeds Resource: POST complete");
            return Response.created(URI.create(urlBuilder.buildSubscribedGadgetFeedUrl(feedProvider.getId())))
                .entity(new SubscribedGadgetFeedRepresentation(feedProvider, urlBuilder))
                .build();
        }
        catch (NonAtomGadgetSpecFeedException e)
        {
            logger.debug("Subscribed Gadget Feeds Resource: POST rejected: " + feedUri + " is not an Atom feed", e);
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(i18n.getText("subscribed.gadget.feeds.non.atom.gadget.feed", feedUri))
                .type("text/plain")
                .build();
        }
        catch (GadgetFeedParsingException e)
        {
            logger.debug("Subscribed Gadget Feeds Resource: POST rejected: " + feedUri + " could not be parsed");
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(i18n.getText("subscribed.gadget.feeds.non.parsable.gadget.feed", feedUri))
                .type("text/plain")
                .build();
        }
        catch (PermissionException e)
        {
            logger.warn("Subscribed Gadget Feeds Resource: POST rejected: current user not allowed to write to directory", e);
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(i18n.getText("directoryResource.no.write.permission"))
                .type("text/plain")
                .build();
        }
    }
    
    private URI getFeedUri(Reader jsonContent)
    {
        String feedUrl = getFeedUrl(jsonContent);

        if (isEmpty(feedUrl))
        {
            logger.error("Subscribed Gadget Feeds Resource: POST rejected due to missing 'url' parameter");
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .entity(i18n.getText("subscribed.gadget.feeds.missing.url.parameter"))
                .type("text/plain")
                .build()
            );
        }
        
        try
        {
            return new URI(feedUrl).normalize();
        }
        catch (URISyntaxException e)
        {
            logger.debug("Subscribed Gadget Feeds Resource: POST rejected due to invalid 'url' parameter " + feedUrl, e);
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .entity(i18n.getText("subscribed.gadget.feeds.invalid.url.parameter", feedUrl))
                .type("text/plain")
                .build()
            );
        }
    }

    private String getFeedUrl(Reader jsonContent)
    {
        try
        {
            return parseJsonObject(jsonContent).getString("url").trim();
        }
        catch (JSONException e)
        {
            logger.debug("Subscribed Gadget Feeds Resource: POST rejected due to missing 'url' parameter");
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .entity(i18n.getText("subscribed.gadget.feeds.missing.url.parameter"))
                .type("text/plain")
                .build()
            );
        }
    }

    private JSONObject parseJsonObject(Reader jsonContent)
    {
        try
        {
            return new JSONObject(IOUtils.toString(jsonContent));
        }
        catch (JSONException e)
        {
            logger.debug("Subscribed Gadget Feeds Resource: POST rejected due invalid JSON data");
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .entity(i18n.getText("subscribed.gadget.feeds.invalid.json"))
                .type("text/plain")
                .build()
            );
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
