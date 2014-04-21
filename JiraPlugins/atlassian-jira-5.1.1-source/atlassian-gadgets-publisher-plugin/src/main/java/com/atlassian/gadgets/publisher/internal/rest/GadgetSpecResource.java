package com.atlassian.gadgets.publisher.internal.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.gadgets.publisher.internal.PublishedGadgetSpecNotFoundException;
import com.atlassian.gadgets.publisher.internal.PublishedGadgetSpecWriter;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * Provides a REST endpoint for serving gadget specs provided by plugins.
 */
@Path("/g/{pluginKey}/{gadgetSpecName:.+}")
public final class GadgetSpecResource
{
    private static final Log log = LogFactory.getLog(GadgetSpecResource.class);

    private final PublishedGadgetSpecWriter writer;

    /**
     * Constructor.
     * @param writer the store to retrieve gadget specs from
     */
    public GadgetSpecResource(PublishedGadgetSpecWriter writer)
    {
        this.writer = writer;
    }

    /**
     * Returns the full XML gadget spec corresponding to the resource specified
     * in the URI.
     * @param pluginKey the injected plugin key from the request
     * @param gadgetSpecName the injected string path to the gadget spec
     * @return a {@code Response} carrying the gadget spec
     */
    @GET
    @AnonymousAllowed
    @Produces(MediaType.APPLICATION_XML)
    public Response getGadgetSpec(@PathParam("pluginKey") String pluginKey,
                                  @PathParam("gadgetSpecName") String gadgetSpecName)
    {
        if (pluginKey.contains(":"))
        {
            pluginKey = pluginKey.substring(0, pluginKey.indexOf(":"));
        }
        log.debug("GET received: allowing anonymous access to " + pluginKey + "/" + gadgetSpecName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            writer.writeGadgetSpecTo(pluginKey, gadgetSpecName, baos);
        }
        catch (IOException ioe)
        {
            return Response.serverError().
                    type(MediaType.TEXT_PLAIN).
                    entity(ioe.getMessage()).build();
        }
        catch (PublishedGadgetSpecNotFoundException pgsnfe)
        {
            return Response.status(NOT_FOUND).build();
        }
        
        log.debug("GET processed; request complete");
        return Response.ok(baos.toByteArray()).type(MediaType.APPLICATION_XML).build();
    }
}