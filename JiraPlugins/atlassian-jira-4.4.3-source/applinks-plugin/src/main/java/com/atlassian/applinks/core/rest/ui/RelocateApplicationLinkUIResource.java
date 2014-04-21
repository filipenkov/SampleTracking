package com.atlassian.applinks.core.rest.ui;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.core.InternalTypeAccessor;
import com.atlassian.applinks.core.manifest.AppLinksManifestDownloader;
import com.atlassian.applinks.core.rest.AbstractResource;
import com.atlassian.applinks.core.rest.auth.AdminApplicationLinksInterceptor;
import com.atlassian.applinks.core.rest.context.ContextInterceptor;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.spi.Manifest;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.applinks.spi.link.MutableApplicationLink;
import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;
import com.atlassian.applinks.spi.manifest.ApplicationStatus;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.applinks.spi.manifest.ManifestRetriever;
import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.atlassian.plugins.rest.common.util.RestUrlBuilder;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.net.RequestFactory;
import com.sun.jersey.spi.resource.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.net.URI;

import static com.atlassian.applinks.core.rest.util.RestUtil.noContent;
import static com.atlassian.applinks.core.rest.util.RestUtil.notFound;

/**
 * Modifies an application link's RPC URL.
 *
 * @since   3.0
 */
@Path("relocateApplicationlink")
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Singleton
@InterceptorChain ({ ContextInterceptor.class, AdminApplicationLinksInterceptor.class })
public class RelocateApplicationLinkUIResource extends AbstractResource
{
    private static final Logger LOG = LoggerFactory.getLogger(RelocateApplicationLinkUIResource.class);

    private final MutatingApplicationLinkService applicationLinkService;
    private final ManifestRetriever manifestRetriever;
    private final AppLinksManifestDownloader manifestDownloader;
    private final I18nResolver i18nResolver;

    public RelocateApplicationLinkUIResource(
            final RestUrlBuilder restUrlBuilder,
            final MutatingApplicationLinkService applicationLinkService,
            final I18nResolver i18nResolver,
            final ManifestRetriever manifestRetriever,
            final AppLinksManifestDownloader manifestDownloader,
            final InternalTypeAccessor internalTypeAccessor,
            final RequestFactory requestFactory)
    {
        super(restUrlBuilder, internalTypeAccessor, requestFactory, applicationLinkService);
        this.applicationLinkService = applicationLinkService;
        this.i18nResolver = i18nResolver;
        this.manifestRetriever = manifestRetriever;
        this.manifestDownloader = manifestDownloader;
    }

    /**
     * Status codes:
     * <ul>
     * <li>204: Updated successfully (this is the only mutating operation)</li>
     * <li>400: The peer is of a different application type (possibly one we can't deserialize)</li>
     * <li>404: The specified server ID doesn't exist</li>
     * <li>409: Peer is offline, resubmit with "?nowarning=true" to update nevertheless</li>
     * </ul>
     *
     * @throws TypeNotInstalledException    will be translated into a 400.
     */
    @POST
    @Path ("{applinkId}")
    public Response relocate(@PathParam ("applinkId") final String applicationId,
                             @QueryParam ("newUrl") final String urlString,
                             @QueryParam ("nowarning") final boolean nowarning)
            throws TypeNotInstalledException
    {
        final MutableApplicationLink link = applicationLinkService.getApplicationLink(new ApplicationId(applicationId));
        final URI url = URIUtil.uncheckedToUri(urlString);

        if (link == null)
        {
            return notFound(i18nResolver.getText("applinks.notfound", applicationId));
        }
        else
        {
            if (manifestRetriever.getApplicationStatus(url, link.getType()) == ApplicationStatus.UNAVAILABLE)
            {
                if (nowarning)
                {
                    return update(link, url);
                }
                else
                {
                    /**
                     * IMPLEMENTATION NOTE:
                     *
                     * The restfulness of this pattern is debatable:
                     * http://stackoverflow.com/questions/2539394/rest-http-delete-and-parameters
                     * However, as is often the case with rest, few
                     * alternatives are suggested.
                     */
                    return Response
                            .status(409)
                            .build();
                }
            }
            else
            {
                try
                {
                    final Manifest manifest = manifestDownloader.download(url);
                    if (!typeAccessor.loadApplicationType(manifest.getTypeId()).equals(link.getType()))
                    {
                        return Response
                                .status(400)
                                .entity(i18nResolver.getText("applinks.error.relocate.type", urlString,
                                        i18nResolver.getText(typeAccessor.loadApplicationType(manifest.getTypeId()).getI18nKey()),
                                        i18nResolver.getText(link.getType().getI18nKey())))
                                .build();
                    }
                }
                catch (ManifestNotFoundException e)
                {
                    // the peer is online, but doesn't serve a manifest: must be non-UAL, that's ok
                }
                return update(link, url);
            }
        }
    }

    private Response update(final MutableApplicationLink link, final URI rpcUrl)
    {
        link.update(ApplicationLinkDetails
                .builder(link)
                .rpcUrl(rpcUrl)
                .build());
        LOG.info("Changed RPC URL of ApplicationLink {} from {} to {}.",
                new Object[] {link.getId(), link.getRpcUrl().toString(), rpcUrl.toString()});
        return noContent(); // returning the updated applink might be more restful
    }
}
