package com.atlassian.applinks.core.manifest;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.core.util.URIUtil;
import com.atlassian.applinks.spi.Manifest;
import com.atlassian.applinks.spi.application.ApplicationIdUtil;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.manifest.ApplicationStatus;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.applinks.spi.manifest.ManifestProducer;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ReturningResponseHandler;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Set;

/**
 * <p>
 * Abstract base class for all AppLinks manifest producers.
 * </p>
 * <p>
 * These producers will first attempt to download the manifest and if that
 * fails (either because the peer is offline, is an older non-applinks-capable
 * version, or is a different kind of app altogether), it will delegate to the
 * actual subclass to create a manifest locally.
 * </p>
 *
 * @since   3.0
 */
public abstract class AppLinksManifestProducer implements ManifestProducer
{
    private static final int CONNECTION_TIMEOUT = 10000;    // 10 seconds
    private final AppLinksManifestDownloader downloader;
    private final RequestFactory<Request<Request<?, Response>,Response>> requestFactory;
    protected final WebResourceManager webResourceManager;
    protected final AppLinkPluginUtil appLinkPluginUtil;
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    protected AppLinksManifestProducer(
            final RequestFactory<Request<Request<?, Response>,Response>> requestFactory,
            final AppLinksManifestDownloader downloader,
            final WebResourceManager webResourceManager,
            final AppLinkPluginUtil AppLinkPluginUtil)
    {
        this.downloader = downloader;
        this.requestFactory = requestFactory;
        this.webResourceManager = webResourceManager;
        appLinkPluginUtil = AppLinkPluginUtil;
    }

    public Manifest getManifest(final URI url) throws ManifestNotFoundException
    {
        try
        {
            final Manifest downloadedManifest = downloader.download(url);
            if (downloadedManifest != null && getApplicationTypeId().equals(downloadedManifest.getTypeId()))
            {
                return downloadedManifest;
            }
        }
        catch (ManifestNotFoundException e)
        {
            LOG.debug("Failed to obtain an AppLinks manifest from the peer. " +
                    "Treating the peer as a non-AppLinks capable host instead.");
        }
        return createManifest(url);
    }

    private Manifest createManifest(final URI url)
    {
        return new Manifest()
        {
            public ApplicationId getId()
            {
                return ApplicationIdUtil.generate(url);
            }

            public String getName()
            {
                return getApplicationName();
            }

            public TypeId getTypeId()
            {
                return getApplicationTypeId();
            }

            public String getVersion()
            {
                return getApplicationVersion();
            }

            public Long getBuildNumber()
            {
                return getApplicationBuildNumber();
            }

            public URI getUrl()
            {
                return URIUtil.copyOf(url);
            }

            public Version getAppLinksVersion()
            {
                return getApplicationAppLinksVersion();
            }
            
            public Boolean hasPublicSignup()
            {
                return null;
            }

            public Set<Class<? extends AuthenticationProvider>> getInboundAuthenticationTypes()
            {
                return getSupportedInboundAuthenticationTypes();
            }

            public Set<Class<? extends AuthenticationProvider>> getOutboundAuthenticationTypes()
            {
                return getSupportedOutboundAuthenticationTypes();
            }
        };
    }

    protected Long getApplicationBuildNumber()
    {
         return 0L;
    }

    protected String getApplicationVersion()
    {
        return null;
    }

    protected Version getApplicationAppLinksVersion()
    {
        return null;
    }

    protected abstract TypeId getApplicationTypeId();

    protected abstract String getApplicationName();

    protected abstract Set<Class<? extends AuthenticationProvider>> getSupportedInboundAuthenticationTypes();

    protected abstract Set<Class<? extends AuthenticationProvider>> getSupportedOutboundAuthenticationTypes();

    /**
     * Does a GET on the baseurl and expects a 200 status code.
     *
     * @param url   baseUrl of the peer.
     * @return
     */
    public ApplicationStatus getStatus(final URI url)
    {
        try
        {
            LOG.debug("Querying " + url + " for its online status.");
            final Request<Request<?, Response>,Response> request = requestFactory
                .createRequest(Request.MethodType.GET, url.toString());
            request.setConnectionTimeout(CONNECTION_TIMEOUT).setSoTimeout(CONNECTION_TIMEOUT);
            return request.executeAndReturn(new ReturningResponseHandler<Response,ApplicationStatus>()
                {
                    public ApplicationStatus handle(final Response response) throws ResponseException
                    {
                        return response.isSuccessful() ?
                                ApplicationStatus.AVAILABLE :
                                ApplicationStatus.UNAVAILABLE;
                    }
                });
        }
        catch (ResponseException re)
        {
            return ApplicationStatus.UNAVAILABLE;
        }
    }
}
