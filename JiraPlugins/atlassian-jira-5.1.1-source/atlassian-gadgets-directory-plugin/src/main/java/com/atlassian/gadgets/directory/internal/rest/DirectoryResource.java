package com.atlassian.gadgets.directory.internal.rest;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.gadgets.GadgetParsingException;
import com.atlassian.gadgets.GadgetRequestContextFactory;
import com.atlassian.gadgets.GadgetSpecUriNotAllowedException;
import com.atlassian.gadgets.dashboard.PermissionException;
import com.atlassian.gadgets.directory.Directory;
import com.atlassian.gadgets.directory.internal.ConfigurableExternalGadgetSpecStore;
import com.atlassian.gadgets.directory.internal.DirectoryConfigurationPermissionChecker;
import com.atlassian.gadgets.directory.internal.impl.UnavailableFeatureException;
import com.atlassian.gadgets.directory.internal.jaxb.JAXBDirectoryContents;
import com.atlassian.gadgets.directory.spi.ExternalGadgetSpecId;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.message.I18nResolver;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * Provides a JSON representation of the directory.
 */
@Path("/directory")
public class DirectoryResource
{
    private final Log log = LogFactory.getLog(getClass());

    private final Directory directory;
    private final GadgetRequestContextFactory gadgetRequestContextFactory;
    private final ConfigurableExternalGadgetSpecStore configurableDirectory;
    private final DirectoryConfigurationPermissionChecker gadgetUrlChecker;
    private final I18nResolver i18n;

    /**
     * Constructor.
     *
     * @param directory the {@code Directory} implementation to use
     * @param gadgetRequestContextFactory the {@code GadgetRequestContextFactory}
     * implementation to use
     * @param configurableDirectory the {@code ConfigurableExternalGadgetStore}
     * implementation to use
     * @param gadgetUrlChecker the {@code GadgetSpecUrlChecker} implementation to
     * @param i18n the {@code I18nResolver} implementation to use
     */
    public DirectoryResource(Directory directory,
                             GadgetRequestContextFactory gadgetRequestContextFactory,
                             ConfigurableExternalGadgetSpecStore configurableDirectory,
                             DirectoryConfigurationPermissionChecker gadgetUrlChecker,
                             I18nResolver i18n)
    {
        this.directory = directory;
        this.gadgetRequestContextFactory = gadgetRequestContextFactory;
        this.configurableDirectory = configurableDirectory;
        this.gadgetUrlChecker = gadgetUrlChecker;
        this.i18n = i18n;
    }

    /**
     * Returns a representation of the categories and gadgets contained in the directory.
     *
     * @param request context-supplied {@code HttpServletRequest}, used to resolve the user's locale
     * @return a {@code Response} carrying the JSON/XML representation of the directory contents
     */
    @GET
    @AnonymousAllowed
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getDirectory(@Context HttpServletRequest request)
    {
        log.debug("DirectoryResource: GET received and answered (all users allowed)");
        return Response.ok(JAXBDirectoryContents.getDirectoryContents(directory,
                gadgetRequestContextFactory.get(request))).build();
    }

    /**
     * Adds the specified gadget URL to the directory listing.
     *
     * @param request context-supplied {@code HttpServletRequest}, used to retrieve information for the user making the
     * request
     * @param jsonContent the post content in JSON
     * @return a {@code Response} carrying the URL at which the gadget was added
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putGadgetInDirectory(@Context HttpServletRequest request, Reader jsonContent)
    {
        String gadgetUrl = "";
        try
        {
            JSONObject jsonObject = new JSONObject(IOUtils.toString(jsonContent));
            gadgetUrl = jsonObject.getString("url").trim();

            if (isEmpty(gadgetUrl))
            {
                log.error("DirectoryResource: POST rejected due to missing 'url' parameter");
                return Response.status(Response.Status.BAD_REQUEST).
                        entity(i18n.getText("directoryResource.missing.url.parameter")).
                        type("text/plain").
                        build();
            }

            log.debug("DirectoryResource: POST received: url=" + gadgetUrl);
            gadgetUrlChecker.checkForPermissionToConfigureDirectory(request);
            URI validGadgetUri = URI.create(gadgetUrl);
            configurableDirectory.add(validGadgetUri);
            log.debug("DirectoryResource: POST complete: new URL=" + validGadgetUri);
            return Response.created(validGadgetUri).build();
        }
        catch (JSONException e)
        {
            log.error("DirectoryResource: POST rejected due to missing 'url' parameter");
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(i18n.getText("directoryResource.missing.url.parameter")).
                    type("text/plain").
                    build();
        }
        catch (GadgetSpecUriNotAllowedException e)
        {
            log.error("DirectoryResource: POST rejected: " + gadgetUrl + " is an invalid gadget spec", e);
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(i18n.getText("directoryResource.invalid.gadget.spec", gadgetUrl)).
                    type("text/plain").build();
        }
        catch (GadgetParsingException e)
        {
            log.error("DirectoryResource: POST rejected: could not parse gadget at " + gadgetUrl, e);
            final String message = e.getMessage();
            if(message != null && message.contains("HTTP error 403"))
            {
                return Response.status(Response.Status.BAD_REQUEST).
                        entity(i18n.getText("directoryResource.no.applink.configured")).type("text/plain").build();
            }
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(i18n.getText("directoryResource.could.not.parse.gadget", gadgetUrl)).
                    type("text/plain").build();
        }
        catch (UnavailableFeatureException e)
        {
            log.info("DirectoryResource: POST rejected: container does not support feature(s) " + e.getMessage()
                    + " required for gadget at " + gadgetUrl, e);
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(i18n.getText("directoryResource.unsupported.feature", gadgetUrl, e.getMessage())).
                    type("text/plain").build();
        }
        catch (PermissionException e)
        {
            log.warn("DirectoryResource: POST rejected: current user not allowed to write to directory", e);
            return Response.status(Response.Status.FORBIDDEN).
                    entity(i18n.getText("directoryResource.no.write.permission")).
                    type("text/plain").build();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @DELETE
    @Path("/gadget/{gadgetId}")
    public Response deleteGadgetFromDirectory(@Context HttpServletRequest request,
        @PathParam("gadgetId") ExternalGadgetSpecId gadgetId)
    {
        try
        {
            log.debug("DirectoryResource: DELETE received: gadgetId = " + gadgetId);

            // Remove from Configurable Store
            gadgetUrlChecker.checkForPermissionToConfigureDirectory(request);
            configurableDirectory.remove(gadgetId);

            log.debug("DirectoryResource: DELETE complete: gadgetId = " + gadgetId);
            return Response.ok().build();
        }
        catch (PermissionException e)
        {
            log.warn("DirectoryResource: DELETE rejected: current user not allowed to write to directory", e);
            return Response.status(Response.Status.FORBIDDEN).
                    entity(i18n.getText("directoryResource.no.write.permission")).
                    type("text/plain").build();
        }
    }
}
