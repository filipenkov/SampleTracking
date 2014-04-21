package com.atlassian.applinks.core.manifest;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.core.rest.ManifestResource;
import com.atlassian.applinks.core.rest.context.CurrentContext;
import com.atlassian.applinks.core.rest.model.ManifestEntity;
import com.atlassian.applinks.core.rest.util.RestUtil;
import com.atlassian.applinks.core.util.Holder;
import com.atlassian.applinks.spi.Manifest;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.manifest.ManifestNotFoundException;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.plugin.util.ChainingClassLoader;
import com.atlassian.plugin.util.ClassLoaderUtils;
import com.atlassian.plugins.rest.common.util.RestUrlBuilder;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.RequestFactory;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.net.ResponseHandler;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import org.apache.commons.lang.ObjectUtils;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Non-public component that is used to download manifests for AppLinks peers.
 *
 * @since 3.0
 */
public class AppLinksManifestDownloader
{
    private static final Logger LOG = LoggerFactory.getLogger(AppLinksManifestDownloader.class);
    private final RequestFactory requestFactory;
    private final TypeAccessor typeAccessor;
    private final RestUrlBuilder restUrlBuilder;
    private static final String CACHE_KEY = "ManifestRequestCache";
    private static final int CONNECTION_TIMEOUT = 10000;    // 10 seconds

    public AppLinksManifestDownloader(final RequestFactory requestFactory,
                                      final TypeAccessor typeAccessor,
                                      final RestUrlBuilder restUrlBuilder)
    {
        this.requestFactory = requestFactory;
        this.typeAccessor = typeAccessor;
        this.restUrlBuilder = restUrlBuilder;
    }

    private Map<URI, DownloadResult> getDownloadCache()
    {
        final HttpServletRequest request = CurrentContext.getHttpServletRequest();
        if (request != null)
        {
            Map<URI, DownloadResult> cache = (Map<URI, DownloadResult>) request.getAttribute(CACHE_KEY);
            if (cache == null)
            {
                synchronized (request)
                {
                    if ((cache = (Map<URI, DownloadResult>) request.getAttribute(CACHE_KEY)) == null)
                    {
                        request.setAttribute(CACHE_KEY, cache = new MapMaker()
                                .makeComputingMap(new Function<URI, DownloadResult>()
                                {
                                    public DownloadResult apply(@Nullable final URI uri)
                                    {
                                        return new DownloadResult()
                                        {
                                            Manifest manifest = null;
                                            ManifestNotFoundException exception = null;
                                            {
                                                try
                                                {
                                                    manifest = download1(uri);
                                                }
                                                catch (ManifestNotFoundException e)
                                                {
                                                    exception = e;
                                                }
                                            }

                                            public Manifest get() throws ManifestNotFoundException
                                            {
                                                if (manifest == null)
                                                {
                                                    LOG.debug("Throwing cached ManifestNotFoundException for: " + uri.toString());
                                                    throw exception;
                                                }
                                                else
                                                {
                                                    LOG.debug("Returning cached manifest for: " + uri.toString());
                                                    return manifest;
                                                }
                                            }
                                        };
                                    }
                                }));
                    }
                }
            }
            return cache;
        }
        return null;
    }

    private interface DownloadResult
    {
        Manifest get() throws ManifestNotFoundException;
    }

    /**
     *
     * @param url   the <strong>baseurl</strong> of the remote application
     * instance from which to retrieve a manifest.
     * @return never {@code null}. If the remote app does not seem to be an AppLinks
     * app (possibly due to a 404), {@ManifestNotFoundException} is thrown.
     * @throws ManifestNotFoundException    when no manifest could be obtained.
     */
    public Manifest download(final URI url) throws ManifestNotFoundException
    {
        final Map<URI, DownloadResult> resultMap = getDownloadCache();
        return resultMap != null ?
                resultMap.get(url).get() :
                download1(url);
    }

    private Manifest download1(final URI url) throws ManifestNotFoundException
    {
        final Holder<Manifest> manifestHolder = new Holder<Manifest>();
        final Holder<Throwable> exception = new Holder<Throwable>();

        final ClassLoader currentContextClassloader = Thread.currentThread().getContextClassLoader();
        final ChainingClassLoader chainingClassLoader = new ChainingClassLoader(currentContextClassloader,
                ClassLoaderUtils.class.getClassLoader(), ClassLoader.getSystemClassLoader());
        try
        {
            Thread.currentThread().setContextClassLoader(chainingClassLoader); // APL-833
            requestFactory
                    .createRequest(Request.MethodType.GET, appLinksManifestUrl(url))
                    .setConnectionTimeout(CONNECTION_TIMEOUT)
                    .setSoTimeout(CONNECTION_TIMEOUT)
                    .execute(new ResponseHandler()
                    {
                        public void handle(Response response) throws ResponseException
                        {
                            if (response.isSuccessful())
                            {
                                try
                                {
                                    manifestHolder.set(asManifest(response.getEntity(ManifestEntity.class)));
                                }
                                catch (Exception ex)
                                {
                                    exception.set(ex);
                                }
                            }
                        }
                    });
        }
        catch (ResponseException re)
        {
            exception.set((Throwable) ObjectUtils.defaultIfNull(re.getCause(), re));
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(currentContextClassloader);
        }
        if (manifestHolder.get() == null)
        {
            if (exception.get() == null)
            {
                throw new ManifestNotFoundException(url.toString());
            }
            else
            {
                throw new ManifestNotFoundException(url.toString(), exception.get());
            }
        }
        return manifestHolder.get();

    }

    private String appLinksManifestUrl(final URI baseUri)
    {
        return restUrlBuilder.getUrlFor(RestUtil.getBaseRestUri(baseUri), ManifestResource.class).getManifest()
                .toString();
    }

    private Manifest asManifest(final ManifestEntity manifest)
    {
        return new Manifest()
        {
            public ApplicationId getId()
            {
                return manifest.getId();
            }

            public String getName()
            {
                return manifest.getName();
            }

            public TypeId getTypeId()
            {
                return manifest.getTypeId();
            }

            public Long getBuildNumber()
            {
                return manifest.getBuildNumber();
            }

            public String getVersion()
            {
                return manifest.getVersion();
            }

            public URI getUrl()
            {
                return manifest.getUrl();
            }

            public Version getAppLinksVersion()
            {
                return manifest.getApplinksVersion();
            }
            
            public Boolean hasPublicSignup()
            {
                return manifest.hasPublicSignup();
            }

            public Set<Class<? extends AuthenticationProvider>> getInboundAuthenticationTypes()
            {
                return loadTypes(manifest.getInboundAuthenticationTypes());
            }

            public Set<Class<? extends AuthenticationProvider>> getOutboundAuthenticationTypes()
            {
                return loadTypes(manifest.getOutboundAuthenticationTypes());
            }

            private Set<Class<? extends AuthenticationProvider>> loadTypes(final Set<String> classNames)
            {
                final Set<Class<? extends AuthenticationProvider>> types = new HashSet<Class<? extends AuthenticationProvider>>();
                for (final String name : classNames)
                {
                    final Class<? extends AuthenticationProvider> c = typeAccessor.getAuthenticationProviderClass(name);
                    if (c != null)
                    {
                        types.add(c);
                    }
                    else
                    {
                        LOG.info(String.format("Authenticator %s specified by remote application %s is not " +
                                "installed locally, and will not be used.", name, getId()));
                    }
                }
                return types;
            }
        };
    }
}
