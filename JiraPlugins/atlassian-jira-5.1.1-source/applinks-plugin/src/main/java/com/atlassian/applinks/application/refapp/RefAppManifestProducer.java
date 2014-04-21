package com.atlassian.applinks.application.refapp;

import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.api.auth.types.BasicAuthenticationProvider;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.api.auth.types.TrustedAppsAuthenticationProvider;
import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.core.manifest.AppLinksManifestDownloader;
import com.atlassian.applinks.core.manifest.AppLinksManifestProducer;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.net.RequestFactory;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * @since 3.0
 */
public class RefAppManifestProducer extends AppLinksManifestProducer
{
    public RefAppManifestProducer(
            final RequestFactory requestFactory,
            final AppLinksManifestDownloader downloader,
            final WebResourceManager webResourceManager,
            final AppLinkPluginUtil appLinkPluginUtil)
    {
        super(requestFactory, downloader, webResourceManager, appLinkPluginUtil);
    }

    protected TypeId getApplicationTypeId()
    {
        return RefAppApplicationTypeImpl.TYPE_ID;
    }

    @Override
    protected String getApplicationName()
    {
        return "REFAPP";
    }

    @Override
    protected Set<Class<? extends AuthenticationProvider>> getSupportedInboundAuthenticationTypes()
    {
        return ImmutableSet.of(
                BasicAuthenticationProvider.class,
                TrustedAppsAuthenticationProvider.class,
                OAuthAuthenticationProvider.class);
    }

    @Override
    protected Set<Class<? extends AuthenticationProvider>> getSupportedOutboundAuthenticationTypes()
    {
        return ImmutableSet.of(
                BasicAuthenticationProvider.class,
                TrustedAppsAuthenticationProvider.class,
                OAuthAuthenticationProvider.class);
    }
}
