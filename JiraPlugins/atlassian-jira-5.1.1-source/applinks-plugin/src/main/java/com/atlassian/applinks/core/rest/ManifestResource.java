package com.atlassian.applinks.core.rest;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.core.rest.context.ContextInterceptor;
import com.atlassian.applinks.core.rest.model.ManifestEntity;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.applinks.spi.Manifest;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.applinks.spi.manifest.ManifestRetriever;
import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.applinks.core.rest.util.RestUtil.*;

/**
 * @since v3.0
 */
@Path("manifest")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@InterceptorChain({ContextInterceptor.class})
public class ManifestResource
{
    private static final Logger LOG = LoggerFactory.getLogger(ManifestResource.class);

    private final InternalHostApplication internalHostApplication;
    private final ManifestRetriever manifestRetriever;
    private final ApplicationLinkService applicationLinkService;
    private final AppLinkPluginUtil pluginUtil;
    private final ApplicationProperties applicationProperties;

    public ManifestResource(final InternalHostApplication internalHostApplication, final ManifestRetriever manifestRetriever,
                            final ApplicationLinkService applicationLinkService, final ApplicationProperties applicationProperties,
                            final AppLinkPluginUtil pluginUtil)
    {
        this.internalHostApplication = internalHostApplication;
        this.manifestRetriever = manifestRetriever;
        this.applicationLinkService = applicationLinkService;
        this.applicationProperties = applicationProperties;
        this.pluginUtil = pluginUtil;
    }

    @GET
    @AnonymousAllowed
    public Response getManifest()
    {
        return ok(new ManifestEntity(internalHostApplication, applicationProperties, pluginUtil));
    }

    @GET
    @Path("{id}")
    public Response getManifestFor(@PathParam("id") final String id)
    {
        final ApplicationId applicationId = new ApplicationId(id);

        ApplicationLink applicationLink = null;

        try
        {
            applicationLink = applicationLinkService.getApplicationLink(applicationId);
        }
        catch (TypeNotInstalledException e)
        {
            // ignore, type not installed is considered equivalent to non-existent
        }

        if (applicationLink == null)
        {
            return badRequest(String.format("No application link with id %s", applicationId));
        }

        final Manifest manifest;
        try
        {
            manifest = manifestRetriever.getManifest(applicationLink.getRpcUrl(), applicationLink.getType());
        }
        catch (ManifestNotFoundException e)
        {
            return notFound(String.format("Couldn't retrieve manifest for link with id %s", applicationId));
        }

        return response(manifest);
    }


    private Response response(final Manifest manifest)
    {
        return ok(new ManifestEntity(manifest));
    }



}
