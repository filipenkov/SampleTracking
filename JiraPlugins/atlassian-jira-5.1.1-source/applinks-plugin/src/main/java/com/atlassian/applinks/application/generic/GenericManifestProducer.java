package com.atlassian.applinks.application.generic;

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

public class GenericManifestProducer extends AppLinksManifestProducer
{
    public GenericManifestProducer(
            final RequestFactory requestFactory,
            final AppLinksManifestDownloader downloader,
            final WebResourceManager webResourceManager,
            final AppLinkPluginUtil AppLinkPluginUtil)
    {
        super(requestFactory, downloader, webResourceManager, AppLinkPluginUtil);
    }

    @Override
    protected TypeId getApplicationTypeId()
    {
        return GenericApplicationTypeImpl.TYPE_ID;
    }

    @Override
    protected String getApplicationName()
    {
        return "Generic Application";
    }

    @Override
    protected Set<Class<? extends AuthenticationProvider>> getSupportedInboundAuthenticationTypes()
    {
       return ImmutableSet.of(BasicAuthenticationProvider.class,
               OAuthAuthenticationProvider.class);
    }

    @Override
    protected Set<Class<? extends AuthenticationProvider>> getSupportedOutboundAuthenticationTypes()
    {
        return ImmutableSet.of(BasicAuthenticationProvider.class,
                OAuthAuthenticationProvider.class);
    }
}
