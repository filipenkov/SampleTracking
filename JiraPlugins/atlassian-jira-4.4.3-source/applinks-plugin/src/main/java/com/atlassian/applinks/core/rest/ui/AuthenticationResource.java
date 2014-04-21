package com.atlassian.applinks.core.rest.ui;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.core.manifest.AppLinksManifestDownloader;
import com.atlassian.applinks.core.rest.auth.AdminApplicationLinksInterceptor;
import com.atlassian.applinks.core.rest.context.ContextInterceptor;
import com.atlassian.applinks.spi.Manifest;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;

import java.net.URI;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.applinks.core.rest.util.RestUtil.notFound;
import static com.atlassian.applinks.core.rest.util.RestUtil.ok;

@Path ("authenticationinfo")
@Consumes ({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces ({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@InterceptorChain ({ ContextInterceptor.class, AdminApplicationLinksInterceptor.class })
public class AuthenticationResource
{
    private final AppLinksManifestDownloader downloader;

    public AuthenticationResource (
            final AppLinksManifestDownloader downloader)
    {
        this.downloader = downloader;
    }

    @GET
    public Response getIsAdminUser()
    {
        return ok();
    }

    /**
     * The URL is passed in a path parameter by old client code, so we must accept requests of that form.
     * New clients should use the query parameter which survives proxies better. If the query parameter
     * is present, use it and ignore the path parameter. (APL-663)
     */
    @GET
    @Path ("id/{applinkId}/url/{url:.*$}")
    public Response rpcUrlIsReachable(@PathParam ("applinkId") String applicationId, @PathParam ("url") URI uri,
            @QueryParam("url") URI qUrl)
    {
        if (qUrl != null)
        {
            uri = qUrl;
        }
        
        try
        {
            final Manifest manifest = downloader.download(uri);
            if (manifest.getId().equals(new ApplicationId(applicationId)))
            {
                return ok();
            }
            else
            {
                return notFound("");
            }
        }
        catch (ManifestNotFoundException e)
        {
            return notFound("");
        }
    }
}
