package com.atlassian.applinks.application.bamboo;

import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.api.auth.types.BasicAuthenticationProvider;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.core.AppLinkPluginUtil;
import com.atlassian.applinks.core.manifest.AppLinksManifestDownloader;
import com.atlassian.applinks.core.manifest.AppLinksManifestProducer;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.net.RequestFactory;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 *
 * @since   3.0
 */
public class BambooManifestProducer extends AppLinksManifestProducer
{
    public BambooManifestProducer(
            final RequestFactory requestFactory,
            final AppLinksManifestDownloader downloader,
            final WebResourceManager webResourceManager,
            final AppLinkPluginUtil appLinkPluginUtil)
    {
        super(requestFactory, downloader, webResourceManager, appLinkPluginUtil);
    }

    @Override
    protected TypeId getApplicationTypeId()
    {
        return BambooApplicationTypeImpl.TYPE_ID;
    }

    @Override
    protected String getApplicationName()
    {
        return "Bamboo";
    }

    @Override
    protected Set<Class<? extends AuthenticationProvider>> getSupportedInboundAuthenticationTypes()
    {
        return ImmutableSet.of(
                        BasicAuthenticationProvider.class,
                        OAuthAuthenticationProvider.class);
    }

    @Override
    protected Set<Class<? extends AuthenticationProvider>> getSupportedOutboundAuthenticationTypes()
    {
        return ImmutableSet.<Class<? extends AuthenticationProvider>>of(BasicAuthenticationProvider.class);
    }
}
